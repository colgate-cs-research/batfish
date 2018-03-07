package org.batfish.main;

import com.google.common.base.Throwables;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.io.Serializable;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Map;
import java.util.Map.Entry;
import java.util.SortedMap;
import java.util.TreeMap;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.BiFunction;
import java.util.stream.Collectors;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;
import javax.annotation.Nullable;
import org.batfish.common.BatfishException;
import org.batfish.common.BatfishLogger;
import org.batfish.common.BfConsts;
import org.batfish.common.Version;
import org.batfish.common.util.CommonUtil;
import org.batfish.datamodel.Configuration;
import org.batfish.datamodel.answers.ConvertConfigurationAnswerElement;

/** A utility class that abstracts the underlying file system storage used by {@link Batfish}. */
final class BatfishStorage {
  private final BatfishLogger _logger;
  private final Path _containerDir;
  private final BiFunction<String, Integer, AtomicInteger> _newBatch;

  /** Create a new {@link BatfishStorage} instance that uses the given root path as a container. */
  public BatfishStorage(
      Path containerDir,
      BatfishLogger logger,
      BiFunction<String, Integer, AtomicInteger> newBatch) {
    _containerDir = containerDir;
    _logger = logger;
    _newBatch = newBatch;
  }

  /**
   * Returns the configuration files for the given testrig. If a serialized copy of these
   * configurations is not already present, then this function returns {@code null}.
   */
  @Nullable
  public SortedMap<String, Configuration> loadConfigurations(String testrig) {
    Path testrigDir = getTestrigDir(testrig);
    Path indepDir = testrigDir.resolve(BfConsts.RELPATH_VENDOR_INDEPENDENT_CONFIG_DIR);

    // If the directory that would contain these configs does not even exist, no cache exists.
    if (!Files.exists(indepDir)) {
      _logger.debugf("Unable to load configs for %s from disk: no cache directory", testrig);
      return null;
    }

    // If the directory exists, then likely the configs exist and are useful. Still, we need to
    // confirm that they were serialized with a compatible version of Batfish first.
    if (!cachedConfigsAreCompatible(testrig)) {
      _logger.debugf(
          "Unable to load configs for %s from disk: error or incompatible version", testrig);
      return null;
    }

    _logger.info("\n*** DESERIALIZING VENDOR-INDEPENDENT CONFIGURATION STRUCTURES ***\n");
    Map<Path, String> namesByPath = new TreeMap<>();
    try (DirectoryStream<Path> stream = Files.newDirectoryStream(indepDir)) {
      for (Path serializedConfig : stream) {
        String name = serializedConfig.getFileName().toString();
        namesByPath.put(serializedConfig, name);
      }
    } catch (IOException e) {
      throw new BatfishException(
          "Error reading vendor-independent configs directory: '" + indepDir + "'", e);
    }
    SortedMap<String, Configuration> configurations =
        deserializeObjects(namesByPath, Configuration.class);
    return configurations;
  }

  @Nullable
  public ConvertConfigurationAnswerElement loadConvertConfigurationAnswerElement(String testrig) {
    Path ccaePath = getTestrigDir(testrig).resolve(BfConsts.RELPATH_CONVERT_ANSWER_PATH);
    if (!Files.exists(ccaePath)) {
      return null;
    }
    return deserializeObject(ccaePath, ConvertConfigurationAnswerElement.class);
  }

  /**
   * Stores the configuration information into the given testrig. Will replace any previously-stored
   * configurations.
   */
  public void storeConfigurations(
      Map<String, Configuration> configurations,
      ConvertConfigurationAnswerElement convertAnswerElement,
      String testrig) {
    String batchName =
        String.format(
            "Serializing %s vendor-independent configuration structures for testrig %s",
            configurations.size(), testrig);
    _logger.infof("\n*** %s***\n", batchName.toUpperCase());
    AtomicInteger progressCount = _newBatch.apply(batchName, configurations.size());

    Path testrigDir = getTestrigDir(testrig);
    if (!testrigDir.toFile().exists() && !testrigDir.toFile().mkdirs()) {
      throw new BatfishException(
          String.format("Unable to create testrig directory '%s'", testrigDir));
    }

    // Save the convert configuration answer element.
    Path ccaePath = testrigDir.resolve(BfConsts.RELPATH_CONVERT_ANSWER_PATH);
    CommonUtil.deleteIfExists(ccaePath);
    serializeObject(convertAnswerElement, ccaePath);

    Path outputDir = testrigDir.resolve(BfConsts.RELPATH_VENDOR_INDEPENDENT_CONFIG_DIR);

    // Delete any existing output, then recreate.
    CommonUtil.deleteDirectory(outputDir);
    if (!outputDir.toFile().exists() && !outputDir.toFile().mkdirs()) {
      throw new BatfishException(
          String.format("Unable to create output directory '%s'", outputDir));
    }

    configurations.forEach(
        (name, c) -> {
          Path currentOutputPath = outputDir.resolve(name);
          serializeObject(c, currentOutputPath);
          progressCount.incrementAndGet();
        });
  }

  /**
   * Returns a single object of the given class deserialized from the given file. Uses the {@link
   * BatfishStorage} default file encoding including serialization format and compression.
   */
  private static <S extends Serializable> S deserializeObject(Path inputFile, Class<S> outputClass)
      throws BatfishException {
    try (FileInputStream fis = new FileInputStream(inputFile.toFile());
        GZIPInputStream gis = new GZIPInputStream(fis, 8192 /* enlarge buffer */);
        ObjectInputStream ois = new ObjectInputStream(gis)) {
      return outputClass.cast(ois.readObject());
    } catch (IOException | ClassNotFoundException e) {
      throw new BatfishException(
          String.format(
              "Failed to deserialize object of type %s from file %s",
              outputClass.getCanonicalName(), inputFile),
          e);
    }
  }

  private <S extends Serializable> SortedMap<String, S> deserializeObjects(
      Map<Path, String> namesByPath, Class<S> outputClass) {
    String outputClassName = outputClass.getName();
    AtomicInteger completed =
        _newBatch.apply(
            String.format("Deserializing objects of type '%s' from files", outputClassName),
            namesByPath.size());
    return new TreeMap<>(
        namesByPath
            .entrySet()
            .parallelStream()
            .collect(
                Collectors.toMap(
                    Entry::getValue,
                    entry -> {
                      Path inputPath = entry.getKey();
                      String name = entry.getValue();
                      _logger.debugf(
                          "Reading %s '%s' from '%s'\n", outputClassName, name, inputPath);
                      S output = deserializeObject(inputPath, outputClass);
                      completed.incrementAndGet();
                      return output;
                    })));
  }

  /**
   * Writes a single object of the given class to the given file. Uses the {@link BatfishStorage}
   * default file encoding including serialization format and compression.
   */
  private static void serializeObject(Serializable object, Path outputFile) {
    try {
      try (OutputStream out = Files.newOutputStream(outputFile);
          GZIPOutputStream gos = new GZIPOutputStream(out, 8192 /* enlarged buffer size */);
          ObjectOutputStream oos = new ObjectOutputStream(gos)) {
        oos.writeObject(object);
      }
    } catch (IOException e) {
      throw new BatfishException("Failed to serialize object to output file: " + outputFile, e);
    }
  }

  private boolean cachedConfigsAreCompatible(String testrig) {
    try {
      ConvertConfigurationAnswerElement ccae = loadConvertConfigurationAnswerElement(testrig);
      return ccae != null
          && Version.isCompatibleVersion(
              BatfishStorage.class.getCanonicalName(),
              "Old processed configurations",
              ccae.getVersion());
    } catch (BatfishException e) {
      _logger.warnf(
          "Unexpected exception caught while deserializing configs for testrig %s: %s",
          testrig, Throwables.getStackTraceAsString(e));
      return false;
    }
  }

  private Path getTestrigDir(String testrig) {
    return _containerDir.resolve(BfConsts.RELPATH_TESTRIGS_DIR).resolve(testrig);
  }
}
