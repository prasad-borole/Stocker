package com.stocker.reducer;

import java.io.IOException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map.Entry;

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

public class WordScorerReduce extends MapReduceBase implements Reducer<Text, Text, Text, Text>{
    static HashMap<String, Integer> wordCount = new HashMap<String, Integer>(); 
    @Override
    public void reduce(Text key, Iterator<Text> values, OutputCollector<Text, Text> output, Reporter reporter)
            throws IOException {
     
        String date = key.toString();
        int totalMatches = 0;
        while(values.hasNext()){
            String val = values.next().toString();
            String[] valSplit = val.split("\\$");
            
            String word = valSplit[0];
            int count = Integer.parseInt(valSplit[1]);
            wordCount.put(word, count);
            totalMatches += count;
        }
        
        for(Entry<String, Integer> entry: wordCount.entrySet()){
            String currWord = entry.getKey();
            double wordScore = (entry.getValue() * 1.0)/ totalMatches;
            output.collect(new Text(date), new Text(currWord+"$"+wordScore));
        }
        
        
    }

}
