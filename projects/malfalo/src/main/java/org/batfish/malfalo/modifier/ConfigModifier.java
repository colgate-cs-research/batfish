package org.batfish.malfalo.modifier;


import org.antlr.v4.runtime.tree.ParseTreeWalker;
import org.apache.commons.io.FileUtils;
import org.batfish.malfalo.configuration.CiscoConfigParseTreeListener;
import org.batfish.malfalo.configuration.ConfigurationFile;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.Map;


/** Builder patter**/
public class ConfigModifier {

    Path _modifiedDestination;
    ConfigurationFile _config;

    public ConfigModifier(ConfigurationFile config){
        _config = config;
        _modifiedDestination = Paths.get(config.getCfgFile().getParentFile().getAbsolutePath());
    }

    //another test method
    public void exploreAST(){
        System.out.println("Exploring AST");
        System.out.println(_config.getParserRuleContext().toStringTree());
        CiscoConfigParseTreeListener listener = new CiscoConfigParseTreeListener(_config);
        ParseTreeWalker walker = new ParseTreeWalker();
        walker.walk(listener,_config.getParserRuleContext());

        Map<String, Integer> ifaceLineNumMap = listener.getInterfaceLinesMap();

    }

    //trying things out (to create lots of configuration files.)
    public void addLineTest(String line){
        System.out.println(_config.toString() + " " + line);
        System.out.println(_modifiedDestination.toString());
        //want to start by adding a line at the start of the file
        //and creating a new file, with the finish() method.

        try {
            List<String> fileLines =FileUtils.readLines(_config.getCfgFile());
            fileLines.add(2, "New Line 1");
            fileLines.add(4, "New Line 2");
            File outFile = new File(_modifiedDestination.toString() + "/abc");
            FileUtils.writeLines(outFile,fileLines);
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    public void addAcl(Modification modification){

    }

    public void rmNetwork(Modification modification){

    }

    public void rmAcl(Modification modification){

    }

    public void overrideDefaultDestination(Path destPath){
        _modifiedDestination = destPath;
    }

    public ConfigurationFile finish(){
        return null;
    }



}
