package com.stocker.mapper;

import java.io.IOException;
import java.util.ArrayList;
import java.util.logging.Logger;

import org.apache.hadoop.io.IntWritable;
import org.apache.hadoop.io.LongWritable;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapred.MapReduceBase;
import org.apache.hadoop.mapred.Mapper;
import org.apache.hadoop.mapred.OutputCollector;
import org.apache.hadoop.mapred.Reporter;

import com.stocker.expressions.EmojiReader;
import com.stocker.expressions.SlangReader;
import com.stocker.poms.POMSHelper;

/*
Project Members:

    UBIT    Person #
    apimple 50169906
    pborole 50170322

*/
public class CleanerMap extends MapReduceBase implements Mapper<LongWritable, Text, Text, IntWritable>{
    private java.util.logging.Logger logger = Logger.getLogger(CleanerMap.class.getName());
    
  //hadoop supported data types
    private static final IntWritable one = new IntWritable(1);
    private Text word = new Text();
    
    private static ArrayList<String> date = new ArrayList<String>();
    private static ArrayList<String> tweets = new ArrayList<String>();
    private static long count = 0;
    public static int mycount = 0;
    @SuppressWarnings("deprecation")
    @Override
    public void map(LongWritable key, Text value, OutputCollector<Text, IntWritable> output, Reporter reporter)
            throws IOException {
        
        populateGlobalPOMSSets();
        String line = value.toString();
        if(count == 3){ // Accumulated three lines into one record.
            count = 0;
            if(!date.isEmpty() && !tweets.isEmpty()){
                String tweetText = tweets.get(0);
                tweetText = tweetText.replaceAll(",", " ");
                tweetText = tweetText.replaceAll("\\s+", " ");
                
                String[] tweeTextSplit = tweetText.split(" ");
                
                for(String word : tweeTextSplit){
                    if(EmojiReader.emojiList.containsKey(word)){
                        mycount++;
                        String newText = EmojiReader.emojiList.get(word);
                        tweetText = tweetText.replace(word, newText.toLowerCase()).trim();
                    }
                    if(SlangReader.slangList.containsKey(word)){
                        mycount++;
                        String newText = SlangReader.slangList.get(word);
                        tweetText = tweetText.replace(word, newText.toLowerCase()).trim();
                    }
                }
                
                tweeTextSplit = tweetText.split(" ");
                for(String word : tweeTextSplit){
                    if(isPOMSWord(word)){ 
                        //Key = date$word
                        String keyEmit = new StringBuilder(date.get(0)).append("$").append(word).toString();
                        //System.out.println("Emoji slang change count :"+mycount);
                        output.collect(new Text(keyEmit), one);
                    }
                }
                
            }
            date.clear();
            tweets.clear();
            return;
        }
        
        if(line.trim().isEmpty()){ 
            count = 0;
            return;
        }
        
        String[] typeVal = line.split("\\t");
        String type = typeVal[0];
        
        if(type.equals("T")){
            String timeStamp = typeVal[1].trim().substring(0, 10);
            date.add(timeStamp);
        }else if(type.equals("W")){
            String text = typeVal[1].trim();
            text = text.toLowerCase();
            
            //If tweet is not an explicit sentiment defining tweet
            if(!isExplicitSentiment(text)){
                count = 0;
                return;
            }
            tweets.add(text);
        }
        count += 1;
    }
    
    
    /*Mapper Helper functions Start*/
    
    public boolean isExplicitSentiment(String tweet){
        if(tweet.contains("i'm") || tweet.contains("feel") || tweet.contains("feeling") 
               || tweet.contains("feelin") || tweet.contains("makes me") || tweet.contains("i am")){
            return true;
        }
          
        return false;
    }
    
    public boolean isPOMSWord(String word){
        if(POMSHelper.allPOMSWord.contains(word)){
            return true;
        }
        return false;
    }
    
    public void populateGlobalPOMSSets(){
        if(POMSHelper.globalWordMap.isEmpty() || POMSHelper.allPOMSWord.isEmpty()){
            POMSHelper.populateGlobalMap();
        }
        if(EmojiReader.emojiList.isEmpty()){
            EmojiReader.emojiReader();
        }
        if(SlangReader.slangList.isEmpty()){
            SlangReader.slangReader();;
        }
    }
    
    /* Mapper Helper functions End */

}