package org.batfish.malfalo.config;

public class Settings {

    public static final int TRAIN = 0;
    public static final int TEST = 1;
    public static final int CLASSIFY=2;
    public static final int PLAY=3;

    private String trainDir;
    private String testDir;
    private String classifyDir;
    private String playDir;
    private int commandType;

    public Settings(String[] args){
        if (args.length!=2){
            System.out.println("Use : -train [TRAIN_DIR]" +
                    "; -test [TEST_DIR]" +
                    "; -classify [CLASSIFY_DIR]");
            System.exit(1);
        }
        switch(args[0]){
            case "-train":
                commandType = TRAIN;
                trainDir = args[1];
                break;
            case "-test":
                commandType=TEST;
                testDir = args[1];
                break;
            case "-classify":
                commandType = CLASSIFY;
                classifyDir = args[1];
                break;
            case "-play":
                commandType = PLAY;
                playDir = args[1];
                break;
            default:
                System.out.println("Use : -train [TRAIN_DIR]" +
                        "; -test [TEST_DIR]" +
                        "; -classify [CLASSIFY_DIR]");
                System.exit(1);
        }

    }

    public int getCommandType(){
        return commandType;
    }

    public String getTrainDir() {
        return trainDir;
    }

    public String getTestDir() {
        return testDir;
    }

    public String getClassifyDir() {
        return classifyDir;
    }

    public String getPlayDir(){
        return playDir;
    }

}
