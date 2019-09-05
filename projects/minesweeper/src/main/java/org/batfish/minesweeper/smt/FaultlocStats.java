package org.batfish.minesweeper.smt;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Path;
import java.util.StringJoiner;
import org.apache.commons.configuration2.ImmutableConfiguration;
import org.batfish.common.BfConsts;
import org.batfish.common.plugin.IBatfish;

/**
 * Helper class to write out faultloc experiment results into a CSV file.
 */

public class FaultlocStats {
    static final String FILE_HEADER = 
            "num-CEs,foundpreds,missedpreds,extraconfigpred,extracomputepred," +
            "includecomputable,notnegating,minimize,slice," +
            "unfoundpred,firstCEGenTime,allCEGenTime," +
            "firstMUSGenTime,firstMCSGenTime," +
            "firstFailSetMUSGenTime,firstFailSetMCSGenTime,allM_SGenTime," +
            "numMUSGenerated,numMCSGenerated,\n";
    static final String EXPERIMENT_RESULT_FILE_NAME = "experiment.csv";
    static final String COMMA=",";
    static final String NEW_LINE="\n";

    private IBatfish _batfish;
    private ImmutableConfiguration _settings;
    private String questionString;

    private int nCounterExamples;
    private int nFoundPreds;
    private int nUnfoundPreds;
    private int nExtraConfigPreds;
    private int nExtraComputePreds;
    private boolean includeComputable;
    private boolean noNegateProperty;
    private boolean minimize;
    private boolean slice;
    private String unfoundPreds;
    private long firstCEGenTime;
    private long timeToUNSAT;
    private long firstMUSGenTime;
    private long firstMCSGenTime;
    private long failSetMUSGenTime; //all MUSes for first failure set.
    private long failSetMCSGenTime; //all MUSes for first failure set.
    private int nMUSesGenerated;
    private int nMCSesGenerated;
    private long timeElapsedDuringMUSGeneration;


    /**
     * Creates a FaultlocStats object with a Batfish settings object.
     * @param batfish Batfish settings required to obtain path to testrigDir and options for including computable predicates,
     *                 negating property, enabling slicing and minimizing unsat core
     * @param questionString Policy being violated.
     */
    public FaultlocStats(IBatfish batfish, String questionString) {
        this._batfish = batfish;
        this._settings = _batfish.getSettingsConfiguration();
        this.questionString = questionString;
        initStatsFields();
    }

    /**
     * Initialize result fields to default values & obtain options used from Settings object
     */
    private void initStatsFields(){
        this.nCounterExamples = 0;
        this.nFoundPreds = 0;
        this.nUnfoundPreds = 0;
        this.nExtraConfigPreds = 0;
        this.nExtraComputePreds = 0;
        this.includeComputable = _settings.getBoolean(
                Encoder.ARG_INCLUDE_COMPUTABLE);
        this.noNegateProperty = _settings.getBoolean(
                Encoder.ARG_NO_NEGATE_PROPERTY);
        this.minimize = _settings.getBoolean(Encoder.ARG_MINIMIZE_UNSAT_CORE);
        this.slice = _settings.getBoolean(Encoder.ARG_ENABLE_SLICING);
        this.unfoundPreds = "";
        this.firstCEGenTime = 0;
        this.timeToUNSAT = 0;
        this.firstMUSGenTime = 0;
        this.firstMCSGenTime = 0;
        this.nMUSesGenerated = 0;
        this.timeElapsedDuringMUSGeneration = 0;
    }

    public void setNumCounterExamples(int numCounterExamples){
        this.nCounterExamples = numCounterExamples;
    }

    public void setNumFoundPreds(int nFoundPreds) {
        this.nFoundPreds = nFoundPreds;
    }

    public void setNumUnfoundPreds(int nUnfoundPreds) {
        this.nUnfoundPreds = nUnfoundPreds;
    }

    public void setNumExtraConfigPreds(int nExtraConfigPreds) {
        this.nExtraConfigPreds = nExtraConfigPreds;
    }

    public void setNumExtraComputePreds(int nExtraComputePreds) {
        this.nExtraComputePreds = nExtraComputePreds;
    }

    public void setFirstCEGenTime(long firstCEGenTime) {
        this.firstCEGenTime = firstCEGenTime;
    }

    public void setTimeToUNSAT(long minimizeTime) {
        this.timeToUNSAT = minimizeTime;
    }

    public void setFirstMUSGenTime(long checkTime) {
        this.firstMUSGenTime = checkTime;
    }

    public void setFirstMCSGenTime(long checkTime) {
        this.firstMCSGenTime = checkTime;
    }

    public void setFailSetMUSGenTime(long failSetMUSGenTime){
        this.failSetMUSGenTime = failSetMUSGenTime;
    }
    
    public void setFailSetMCSGenTime(long failSetMCSGenTime){
        this.failSetMCSGenTime = failSetMCSGenTime;
    }

    public void setUnfoundPreds(String unfoundPreds) {
        this.unfoundPreds = unfoundPreds;
    }

    public void setTimeElapsedDuringMUSGeneration(long musTime) {this.timeElapsedDuringMUSGeneration = musTime;}

    public void setNumMUSesGenerated(int nMUSes){
        this.nMUSesGenerated = nMUSes;
    }

    public void setNumMCSesGenerated(int nMCSes){
        this.nMCSesGenerated = nMCSes;
    }

    /**
     * Write experiment results into a CSV file (name specified as a constant EXPERIMENT_RESULT_FILE_NAME)
     */
    public void writeOut(){
        FileWriter experimentWriter = null;
        try{
            experimentWriter = new FileWriter(getExperimentOutputDir(), true);
            experimentWriter.append(questionString);
            experimentWriter.append(NEW_LINE);
            experimentWriter.append(FILE_HEADER);
            StringJoiner resultBuilder = new StringJoiner(COMMA);
            resultBuilder.add(Integer.toString(nCounterExamples))
                    .add(Integer.toString(nFoundPreds))
                    .add(Integer.toString(nUnfoundPreds))
                    .add(Integer.toString(nExtraConfigPreds))
                    .add(Integer.toString(nExtraComputePreds))
                    .add(Boolean.toString(includeComputable))
                    .add(Boolean.toString(noNegateProperty))
                    .add(Boolean.toString(minimize))
                    .add(Boolean.toString(slice))
                    .add(unfoundPreds)
                    .add(Long.toString(firstCEGenTime))
                    .add(Long.toString(timeToUNSAT))
                    .add(Long.toString(firstMUSGenTime))
                    .add(Long.toString(firstMCSGenTime))
                    .add(Long.toString(failSetMUSGenTime))
                    .add(Long.toString(failSetMCSGenTime))
                    .add(Long.toString(timeElapsedDuringMUSGeneration))
                    .add(Integer.toString(nMUSesGenerated))
                    .add(Integer.toString(nMCSesGenerated));
            String result = resultBuilder.toString();
            experimentWriter.append(result);
            experimentWriter.append(NEW_LINE);
        }catch(IOException ioe){
            ioe.printStackTrace();
        }finally{
            try {
                experimentWriter.flush();
                experimentWriter.close();
            } catch(IOException e) {
                e.printStackTrace();
            }
        }
    }

    /** Obtain file object for writing out experiment result.
     *
     * @return File for storing experiment results
     */
    public File getExperimentOutputDir(){
        Path outputFilePath = _batfish.getTestrigPath()
                .resolve(BfConsts.RELPATH_OUTPUT)
                .resolve(EXPERIMENT_RESULT_FILE_NAME);
        System.out.printf("Writing out experiment results to %s", outputFilePath.toString());
        return outputFilePath.toFile();
    }
}
