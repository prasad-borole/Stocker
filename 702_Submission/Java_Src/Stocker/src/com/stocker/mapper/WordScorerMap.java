package com.stocker.mapper;

import java.io.IOException;

import org.apache.hadoop.io.LongWritable;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapred.MapReduceBase;
import org.apache.hadoop.mapred.Mapper;
import org.apache.hadoop.mapred.OutputCollector;
import org.apache.hadoop.mapred.Reporter;

/*
Project Members:

    UBIT    Person #
    apimple 50169906
    pborole 50170322

*/

public class WordScorerMap extends MapReduceBase implements Mapper<LongWritable, Text, Text, Text>{

    @Override
    public void map(LongWritable key, Text value, OutputCollector<Text, Text> output, Reporter reporter)
            throws IOException {
        
        String[] currLineSplit = value.toString().split("\\t");
        String date = currLineSplit[0];
        String valueEmit = currLineSplit[1];
        
        /*
        * Key = date
        * Value = word$numOfMatchesinADay
        */
        output.collect(new Text(date), new Text(valueEmit));
        
        
    }
    
}
