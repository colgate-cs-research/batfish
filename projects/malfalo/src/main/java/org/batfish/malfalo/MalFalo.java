package org.batfish.malfalo;

import org.batfish.main.Batfish;
import org.batfish.client.Client;

import org.batfish.malfalo.config.Settings;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;

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

        try {
            Client c = new Client(new String[]{});
        } catch (Exception e) {
            e.printStackTrace();
        }
        List<Path> list  = Batfish.listAllFiles(Paths.get(path));
        for (Path p: list){
            System.out.println(p.toString());
        }
        //     CiscoCombinedParser ciscoCombinedParser = new CiscoCombinedParser()
    }
}
