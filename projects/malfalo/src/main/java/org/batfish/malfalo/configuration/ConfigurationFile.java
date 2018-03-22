package org.batfish.malfalo.configuration;

import org.antlr.v4.runtime.ParserRuleContext;
import org.antlr.v4.runtime.Token;
import org.antlr.v4.runtime.Vocabulary;
import org.batfish.common.Warnings;
import org.batfish.common.util.CommonUtil;
import org.batfish.config.Settings;
import org.batfish.datamodel.ConfigurationFormat;
import org.batfish.grammar.BatfishCombinedParser;
import org.batfish.job.ParseVendorConfigurationJob;
import org.batfish.job.ParseVendorConfigurationResult;
import org.batfish.vendor.VendorConfiguration;

import java.io.File;
import java.nio.file.Path;
import java.util.List;

/** Represents one network configuration file (for a host). **/
public class ConfigurationFile {

    private ParserRuleContext _parserRuleContext; //Abstract Syntax Tree ??
    private String _rawFileText;
    private File _cfgFile;
    private BatfishCombinedParser<?,?> _parser;
    private List<? extends Token> _tokens;
    private Vocabulary _lexerVocabulary;

    /* Keep track of vendor-specific configuration file information */
    private VendorConfiguration _vendorConfiguration;

    public ConfigurationFile(Path cfgPath, Settings batfishSettings) {
        _rawFileText = CommonUtil.readFile(cfgPath.toAbsolutePath());
        _cfgFile = new File(cfgPath.toUri());
        //Parse the configuration file using batfish.
        ParseVendorConfigurationJob parseJob =
                new ParseVendorConfigurationJob(batfishSettings,
                        _rawFileText,
                        cfgPath,
                        new Warnings(),
                        ConfigurationFormat.CISCO_IOS); //TODO:  Fix Config Format.

        try{
            ParseVendorConfigurationResult parseResult = parseJob.call();
            _parserRuleContext = parseResult.getParserRuleContext();
            _vendorConfiguration = parseResult.getVendorConfiguration();
            _parser = parseResult.getParser();
            _parser.getLexer().reset();
            _lexerVocabulary = _parser.getLexer().getVocabulary();
            _tokens = _parser.getLexer().getAllTokens();

        }catch(Exception e) {
            e.printStackTrace();
        }
    }

    public ParserRuleContext getParserRuleContext() {
        return _parserRuleContext;
    }

    public VendorConfiguration getVendorConfiguration() {
        return _vendorConfiguration;
    }

    public File getCfgFile(){
        return _cfgFile;
    }

    public BatfishCombinedParser<?, ?> getParser() {
        return _parser;
    }

    public List<? extends Token> getTokens() {
        return _tokens;
    }

    public List<? extends Token> getTokens(int start, int end) {
        return _tokens.subList(start, end);
    }

    public Vocabulary getLexerVocabulary() {
        return _lexerVocabulary;
    }

    @Override
    public String toString() {
        return String.format("<%s :: ConfigurationFile instance> ",_vendorConfiguration.getHostname());
    }
}
