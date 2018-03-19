package org.batfish.malfalo;

import org.antlr.v4.runtime.ParserRuleContext;
import org.batfish.common.ParseTreeSentences;
import org.batfish.common.Warnings;
import org.batfish.common.util.CommonUtil;
import org.batfish.datamodel.ConfigurationFormat;
import org.batfish.datamodel.answers.ParseVendorConfigurationAnswerElement;
import org.batfish.job.ParseVendorConfigurationJob;
import org.batfish.job.ParseVendorConfigurationResult;
import org.batfish.main.Batfish;


import org.batfish.malfalo.config.Settings;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

public class MalFalo {

    private final Settings _settings;

    public MalFalo(String[] args) {
        _settings = new Settings(args);
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
        }
    }

    private void play(String path){

        System.out.println(path);
        List<Path> list  = Batfish.listAllFiles(Paths.get(path));
        Map<Path, String> configContentMap = new TreeMap<>();
        Map<String, ParseTreeSentences> configParseTreeSentencesMap  = new TreeMap<>();
        Map<String, ParserRuleContext> configParserRuleContextMap = new TreeMap<>();

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
        }
    }
}
