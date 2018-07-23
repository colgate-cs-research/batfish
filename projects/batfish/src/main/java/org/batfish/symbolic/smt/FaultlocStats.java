package org.batfish.symbolic.smt;
import org.batfish.config.Settings;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Path;
import java.util.StringJoiner;

/**
 * Helper class to write out faultloc experiment results into a CSV file.
 */

public class FaultlocStats {
    static final String FILE_HEADER= "examples,foundpreds,unfoundpreds,extraconfigpred," +
            "extracomputepred,includecomputable,notnegating,minimize" +
            ",slice,unfoundpred,slicetime,minimizationtime,checktime\n";
    static final String EXPERIMENT_RESULT_FILE_NAME = "experiment.csv";
    static final String COMMA=",";
    static final String NEW_LINE="\n";

    private Settings settings;
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
    private long sliceTime;
    private long minimizeTime;
    private long checkTime;


    /**
     * Creates a FaultlocStats object with a Batfish settings object.
     * @param settings Batfish settings required to obtain path to testrigDir and options for including computable predicates,
     *                 negating property, enabling slicing and minimizing unsat core
     * @param questionString Policy being violated.
     */
    public FaultlocStats(Settings settings, String questionString) {
        this.settings = settings;
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
        this.includeComputable = settings.shouldIncludeComputable();
        this.noNegateProperty = settings.shouldNotNegateProperty();
        this.minimize = settings.shouldMinimizeUnsatCore();
        this.slice = settings.shouldEnableSlicing();
        this.unfoundPreds = "";
        this.sliceTime = 0;
        this.minimizeTime = 0;
        this.checkTime = 0;
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

    public void setSliceTime(long sliceTime) {
        this.sliceTime = sliceTime;
    }

    public void setMinimizeTime(long minimizeTime) {
        this.minimizeTime = minimizeTime;
    }

    public void setCheckTime(long checkTime) {
        this.checkTime = checkTime;
    }

    public void setUnfoundPreds(String unfoundPreds) {
        this.unfoundPreds = unfoundPreds;
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
                    .add(Long.toString(sliceTime))
                    .add(Long.toString(minimizeTime))
                    .add(Long.toString(checkTime));
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
        Path testRigPath = settings.getActiveTestrigSettings().getTestRigPath();
        Path outputFilePath = testRigPath.resolve(EXPERIMENT_RESULT_FILE_NAME);
        System.out.printf("Writing out experiment results to %s", outputFilePath.toString());
        return outputFilePath.toFile();
    }
}
