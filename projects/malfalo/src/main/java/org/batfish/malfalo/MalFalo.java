package org.batfish.malfalo;

import org.antlr.v4.runtime.ParserRuleContext;
import org.antlr.v4.runtime.TokenSource;
import org.antlr.v4.runtime.tree.ParseTree;
import org.apache.http.impl.client.StandardHttpRequestRetryHandler;
import org.batfish.common.ParseTreeSentences;
import org.batfish.common.Warnings;
import org.batfish.common.util.CommonUtil;
import org.batfish.datamodel.ConfigurationFormat;
import org.batfish.job.ParseVendorConfigurationJob;
import org.batfish.job.ParseVendorConfigurationResult;
import org.batfish.main.Batfish;


import org.batfish.malfalo.config.Settings;
import org.batfish.malfalo.configuration.ConfigurationFile;
import org.batfish.representation.cisco.CiscoConfiguration;
import org.batfish.representation.cisco.StandardAccessList;
import org.batfish.representation.cisco.StandardAccessListLine;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

public class MalFalo {

    private final Settings _settings;
    private org.batfish.config.Settings _batfishSettings;

    private Map<String, ConfigurationFile> _configurationFileMap;

    public MalFalo(String[] args) {
        _settings = new Settings(args);
        initMalFalo();
    }

    private void initMalFalo(){
        //Arguments for creating a ParseVendorConfigurationJob.
        _batfishSettings = new org.batfish.config.Settings();
        _batfishSettings.setPrintParseTree(true);

        _configurationFileMap = new TreeMap<>();
    }
    public void run(){
        switch (_settings.getCommandType()){
            case Settings.TRAIN:
                System.out.println("TRAINING " + _settings.getTrainDir());
                break;
            case Settings.TEST:
                System.out.println("TESTING " + _settings.getTestDir());
                break;
            case Settings.CLASSIFY:
                System.out.println("CLASSIFYING " + _settings.getClassifyDir());
                break;
            case Settings.PLAY:
                System.out.println("TEMPORARY PLAY");
                play(_settings.getPlayDir());
                break;
            case Settings.GENERATE_DATA:
                System.out.println("GENERATE LABELED DATA");
                generate(_settings.getGenDataDir());
                break;
        }
    }

    private void generate(String cfgDir){
        //Setting up Batfish Settings for parsing..
        org.batfish.config.Settings.TestrigSettings testrigSettings =
                new org.batfish.config.Settings.TestrigSettings();
        testrigSettings.setBasePath(Paths.get(cfgDir));
        _batfishSettings.setActiveTestrigSettings(testrigSettings);

        //Iterate through all config files in the directory to build configMap.
        List<Path> cfgFilePaths = Batfish.listAllFiles(Paths.get(cfgDir));
        for(Path path : cfgFilePaths){
            if (!path.toString().endsWith("cfg"))
                continue;

            String fileName = path.getFileName().toString();
            _configurationFileMap.put(fileName, new ConfigurationFile(path, _batfishSettings));
        }

        ConfigurationFile cfg = _configurationFileMap.entrySet().iterator().next().getValue();
        Map<String, StandardAccessList> acls =
                ((CiscoConfiguration) cfg.getVendorConfiguration()).getStandardAcls();
        for (Map.Entry<String, StandardAccessList>  entry :acls.entrySet()){
            System.out.println(entry.getKey());
            StandardAccessList acl = entry.getValue();
            for (StandardAccessListLine line : acl.getLines()){
                System.out.println(String.format("%s -- %s -- %s ",line.getName(),
                        line.getAction().toString(),
                        line.getIpWildcard().toString()));
            }

        }
    }

    //Playing around with using batfish functionality that look useful at this point (March)
    private void play(String path){

        System.out.println(path);
        List<Path> list  = Batfish.listAllFiles(Paths.get(path));
        Map<Path, String> configContentMap = new TreeMap<>();
        Map<String, ParseTreeSentences> configParseTreeSentencesMap  = new TreeMap<>();
        Map<String, ParserRuleContext> configParserRuleContextMap = new TreeMap<>();
        Map<String, CiscoConfiguration> vendorConfigMap = new TreeMap<>();


        /************************************************************************
         Creating Objects required for parsing configuration files with batfish
         ***********************************************************************/

        //TestrigSettings for ParseVendorConfigurationJob
        org.batfish.config.Settings.TestrigSettings testrigSettings =
                new org.batfish.config.Settings.TestrigSettings();
        testrigSettings.setBasePath(Paths.get(path));

        //Arguments for creating a ParseVendorConfigurationJob.
        org.batfish.config.Settings batfishSettings = new org.batfish.config.Settings();
        batfishSettings.setActiveTestrigSettings(testrigSettings);
        batfishSettings.setPrintParseTree(true);

        ConfigurationFormat configurationFormat = ConfigurationFormat.CISCO_IOS;



        for (Path file: list){
            if (!file.toString().endsWith("cfg")) {
                System.out.println(file.toString());
                continue;
            }
            System.out.println("Parsing " + file.toString());
            String rawFileText = CommonUtil.readFile(file.toAbsolutePath());
            configContentMap.put(file,rawFileText);
            ParseVendorConfigurationJob job =
                    new ParseVendorConfigurationJob(batfishSettings,
                            rawFileText,
                            file,
                            new Warnings(),
                            configurationFormat);
            try {
                ParseVendorConfigurationResult result = job.call();
                System.out.println(result.toString());
                configParseTreeSentencesMap.put(file.getFileName().toString(),result.getParseTree());
                configParserRuleContextMap.put(file.getFileName().toString(), result.getParserRuleContext());
                vendorConfigMap.put(file.getFileName().toString(), (CiscoConfiguration) result.getVendorConfiguration());
            }catch(Exception e){
                e.printStackTrace();
            }
        }


        for (Map.Entry<String, ParseTreeSentences> entry : configParseTreeSentencesMap.entrySet()){
            System.out.println(entry.getKey());
            System.out.println(entry.getValue().isEmpty());
            int i = 1;
            for (String sentence : entry.getValue().getSentences()){
                System.out.println(i + " : " + sentence);
                i++;
            }
        }
        // CiscoCombinedParser ciscoCombinedParser = new CiscoCombinedParser()

        System.out.println("Parser Rule Context");
        for (Map.Entry<String, ParserRuleContext> entry : configParserRuleContextMap.entrySet()){
            System.out.println("Start token" + entry.getValue().getStart());
            TokenSource tokenSource = entry.getValue().getStart().getTokenSource();

            List<ParseTree> parseTreeList = entry.getValue().children;
            for (ParseTree tree : parseTreeList){
                System.out.println(tree.getText());
            }
        }


        System.out.println("Tokens");
        for (Map.Entry<String, CiscoConfiguration> entry: vendorConfigMap.entrySet()){
            CiscoConfiguration config = entry.getValue();
            Map<String, StandardAccessList> aclsMap = config.getStandardAcls();
            System.out.println("aclsMap.size() = " +aclsMap.size());
            System.out.println(entry.getValue() + " (iface count): " + config.getInterfaces().size());
            for (Map.Entry<String, StandardAccessList> aclsMapEntry : aclsMap.entrySet()){
                List<StandardAccessListLine> aclLines = aclsMapEntry.getValue().getLines();
                for (StandardAccessListLine line : aclLines){
                    System.out.println(line.getIpWildcard().toString() + " " + line.getAction().toString());
                }
            }
            System.out.println();
        }
    }


}
