package com.ct.analysis;

import com.ct.analysis.tool.AnalysisBeanTool;
import org.apache.hadoop.util.ToolRunner;

public class AnalysisData {
    public static void main(String[] args) throws Exception {
        int result = ToolRunner.run(new AnalysisBeanTool(), args);
    }
}
