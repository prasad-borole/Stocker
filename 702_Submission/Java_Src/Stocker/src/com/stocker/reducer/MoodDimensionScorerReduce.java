package com.stocker.reducer;

import java.io.IOException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Set;
import java.util.TreeMap;

import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapred.MapReduceBase;
import org.apache.hadoop.mapred.OutputCollector;
import org.apache.hadoop.mapred.Reducer;
import org.apache.hadoop.mapred.Reporter;

import com.stocker.poms.POMSHelper;

/*
Project Members:

    UBIT    Person #
    apimple 50169906
    pborole 50170322

*/

public class MoodDimensionScorerReduce extends MapReduceBase implements Reducer<Text, Text, Text, Text>{
    static HashMap<String, Double> poms65WordToScore = new HashMap<String, Double>();
    static TreeMap<String, Double> moodDimensionScore = new TreeMap<String, Double>();
    @Override
    public void reduce(Text key, Iterator<Text> values, OutputCollector<Text, Text> output, Reporter reporter)
            throws IOException {
        
        populateMoodDimensionMap();
        initializeMoodDimensionScoreMap();

        
        while(values.hasNext()){
            String[] currValues = values.next().toString().split("\\$");
            String poms65Word = currValues[0];
            double score = Double.parseDouble(currValues[1]);
            //Aggregating scores
            if(poms65WordToScore.containsKey(poms65Word)){
                double previousScore = poms65WordToScore.get(poms65Word);
                poms65WordToScore.put(poms65Word, previousScore+score);
            }else{
                poms65WordToScore.put(poms65Word, score);
            }
        }
        
        Set<String> poms65KeySet = poms65WordToScore.keySet();
        
        for(String wordKey : poms65KeySet){
            String pomsDimension = POMSHelper.word65toMoodDimension.get(wordKey);
            if(pomsDimension != null){
                double previousScore = moodDimensionScore.get(pomsDimension);
                double newScore = previousScore + poms65WordToScore.get(wordKey);
                moodDimensionScore.put(pomsDimension, newScore);
            }
        }
        
        String valueEmit = buildStringToEmit();
        
        //Clear maps to make them ready for next reduce call
        poms65WordToScore.clear();
        moodDimensionScore.clear();
        
        
        /*
         * Key = date
         * Value = anger_score  confusion_score   depression_score   fatigue_score   tensions_score   vigour_score    
         */
        output.collect(key, new Text(valueEmit));
       
    }
    
    public static String buildStringToEmit(){
        StringBuilder sb = new StringBuilder();
        Set<String> keySet = moodDimensionScore.keySet();
        for(String moodDimension : keySet){
            sb.append(moodDimensionScore.get(moodDimension));
            sb.append("\t");
        }
        
        return sb.toString();
    }
    public static void initializeMoodDimensionScoreMap(){
        moodDimensionScore.put("tension", 0.0d);
        moodDimensionScore.put("depression", 0.0d);
        moodDimensionScore.put("anger", 0.0d);
        moodDimensionScore.put("fatigue", 0.0d);
        moodDimensionScore.put("confusion", 0.0d);
        moodDimensionScore.put("vigour", 0.0d);
    }
    
    public static void populateMoodDimensionMap(){
        if(POMSHelper.word65toMoodDimension.isEmpty()){
            POMSHelper.populateWord65ToMoodDimensionMap();
        }
    }
}
