package com.stocker.mapper;

import java.io.IOException;
import java.util.HashSet;

import org.apache.hadoop.io.LongWritable;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapred.MapReduceBase;
import org.apache.hadoop.mapred.Mapper;
import org.apache.hadoop.mapred.OutputCollector;
import org.apache.hadoop.mapred.Reporter;

import com.stocker.poms.POMSHelper;

/*
Project Members:

    UBIT    Person #
    apimple 50169906
    pborole 50170322

*/


public class MoodDimensionScorerMap extends MapReduceBase implements Mapper<LongWritable, Text, Text, Text>{

    @Override
    public void map(LongWritable key, Text value, OutputCollector<Text, Text> output, Reporter reporter)
            throws IOException {
        
        populat65WordsMap();
        String[] currentLine = value.toString().split("\\t");
        String date = currentLine[0].trim();
        String[] wordAndScore = currentLine[1].trim().split("\\$");
        String word = wordAndScore[0];
        String score = wordAndScore[1];
        
        HashSet<String> pomsWordSet = POMSHelper.wordToPoms65Word.get(word);
        
        /*
         * Key = date
         * Value = poms65Word$score
         */
        
        for(String pomsWord : pomsWordSet){
            output.collect(new Text(date), new Text(pomsWord+"$"+score));
        }
    }
    
    
    public static void populat65WordsMap(){
        if(POMSHelper.wordToPoms65Word.isEmpty()){
            POMSHelper.populateWordtoPomsWord65();
        }
    }
}
