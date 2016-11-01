package com.stocker.reducer;

import java.io.IOException;
import java.util.Iterator;

import org.apache.hadoop.io.IntWritable;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapred.MapReduceBase;
import org.apache.hadoop.mapred.OutputCollector;
import org.apache.hadoop.mapred.Reducer;
import org.apache.hadoop.mapred.Reporter;

/*
Project Members:

    UBIT    Person #
    apimple 50169906
    pborole 50170322

*/

public class CleanerReduce extends MapReduceBase implements Reducer<Text, IntWritable, Text, Text>{
    
    @Override
    public void reduce(Text key, Iterator<IntWritable> values, OutputCollector<Text, Text> output, Reporter reporter)
            throws IOException {
        
        String[] keySplit = key.toString().split("\\$");
        String date = keySplit[0];
        String word = keySplit[1];
        /*Count to measure number of matches of given POMS word in a given day*/ 
        int count = 0;
        while(values.hasNext()){
            count += values.next().get();
        }
        output.collect(new Text(date), new Text(word+"$"+count));
        
    }

}
