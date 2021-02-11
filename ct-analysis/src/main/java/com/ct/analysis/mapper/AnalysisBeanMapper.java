package com.ct.analysis.mapper;

import com.ct.analysis.kv.AnalysisKey;
import com.ct.analysis.kv.AnalysisValue;
import org.apache.hadoop.hbase.client.Result;
import org.apache.hadoop.hbase.io.ImmutableBytesWritable;
import org.apache.hadoop.hbase.mapreduce.TableMapper;
import org.apache.hadoop.hbase.util.Bytes;
import org.apache.hadoop.io.Text;

import java.io.IOException;

/**
 * 分析数据Mapper
 */
public class AnalysisBeanMapper extends TableMapper<AnalysisKey, Text> {
    @Override
    protected void map(ImmutableBytesWritable key, Result value, Context context) throws IOException, InterruptedException {

        String rowkey = Bytes.toString(key.get());

        String[] values = rowkey.split("_");


        String call1=values[1];
        String call2=values[3];
        String calltime=values[2];
        String duration=values[4];

        String year = calltime.substring(0,4);
        String month = calltime.substring(0,6);
        String date = calltime.substring(0,8);

        //主叫用户
        context.write(new AnalysisKey(call1,year),new Text(duration));
        context.write(new AnalysisKey(call1,month),new Text(duration));
        context.write(new AnalysisKey(call1,date),new Text(duration));

        //被叫用户
        context.write(new AnalysisKey(call2,year),new Text(duration));
        context.write(new AnalysisKey(call2,month),new Text(duration));
        context.write(new AnalysisKey(call2,date),new Text(duration));
    }
}
