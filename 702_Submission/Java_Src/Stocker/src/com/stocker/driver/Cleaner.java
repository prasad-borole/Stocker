/*package com.stocker.driver;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map.Entry;
import java.util.Set;
import java.util.TreeMap;
import java.util.logging.Logger;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.conf.Configured;
import org.apache.hadoop.fs.FileSystem;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.io.IntWritable;
import org.apache.hadoop.io.LongWritable;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapred.FileInputFormat;
import org.apache.hadoop.mapred.FileOutputFormat;
import org.apache.hadoop.mapred.JobClient;
import org.apache.hadoop.mapred.JobConf;
import org.apache.hadoop.mapred.MapReduceBase;
import org.apache.hadoop.mapred.Mapper;
import org.apache.hadoop.mapred.OutputCollector;
import org.apache.hadoop.mapred.Reducer;
import org.apache.hadoop.mapred.Reporter;
import org.apache.hadoop.util.Tool;
import org.apache.hadoop.util.ToolRunner;

import com.stocker.expressions.EmojiReader;
import com.stocker.expressions.SlangReader;
import com.stocker.poms.POMSHelper;


Project Members:

    UBIT    Person #
    apimple 50169906
    pborole 50170322


class CleanerMap extends MapReduceBase implements Mapper<LongWritable, Text, Text, IntWritable>{
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
                tweetText = tweetText.replaceAll("\\s", " ");
                
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
                
                String[] tweeTextSplit1 = tweetText.split(" ");
                for(String word : tweeTextSplit1){
                    if(isPOMSWord(word)){ 
                        //Key = date$word
                        String keyEmit = new StringBuilder(date.get(0)).append("$").append(word).toString();
                        System.out.println("Emoji slang change count :"+mycount);
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
    
    
    Mapper Helper functions Start
    
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
    
     Mapper Helper functions End 

}



class CleanerReduce extends MapReduceBase implements Reducer<Text, IntWritable, Text, Text>{
    
    @Override
    public void reduce(Text key, Iterator<IntWritable> values, OutputCollector<Text, Text> output, Reporter reporter)
            throws IOException {
        
        String[] keySplit = key.toString().split("\\$");
        String date = keySplit[0];
        String word = keySplit[1];
        Count to measure number of matches of given POMS word in a given day 
        int count = 0;
        while(values.hasNext()){
            count += values.next().get();
        }
        output.collect(new Text(date), new Text(word+"$"+count));
        
    }

}



class WordScorerMap extends MapReduceBase implements Mapper<LongWritable, Text, Text, Text>{

    @Override
    public void map(LongWritable key, Text value, OutputCollector<Text, Text> output, Reporter reporter)
            throws IOException {
        
        String[] currLineSplit = value.toString().split("\\t");
        String date = currLineSplit[0];
        String valueEmit = currLineSplit[1];
        
        
        * Key = date
        * Value = word$numOfMatchesinADay
        
        output.collect(new Text(date), new Text(valueEmit));
        
        
    }
    
}

class WordScorerReduce extends MapReduceBase implements Reducer<Text, Text, Text, Text>{
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

class MoodDimensionScorerMap extends MapReduceBase implements Mapper<LongWritable, Text, Text, Text>{

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
        
        
         * Key = date
         * Value = poms65Word$score
         
        
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

class MoodDimensionScorerReduce extends MapReduceBase implements Reducer<Text, Text, Text, Text>{
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
        
        
        
         * Key = date
         * Value = anger_score  confusion_score   depression_score   fatigue_score   tensions_score   vigour_score    
         
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

public class Cleaner extends Configured implements Tool{
    

    @Override
    public int run(String[] args) throws Exception {
      //creating a JobConf object and assigning a job name for identification purposes
        System.out.println(getConf());
        String intermediateOP1 = "intermediateOutput1";
        String intermediateOP2 = "intermediateOutput2";
        JobConf conf = new JobConf(getConf(), Cleaner.class);
        conf.setJobName("StockerCleaner");
        
      //Setting configuration object with the Data Type of output Key and Value
        conf.setOutputKeyClass(Text.class);
        conf.setOutputValueClass(IntWritable.class);
        
      //Providing the mapper and reducer class names
        conf.setMapperClass(CleanerMap.class);
        conf.setReducerClass(CleanerReduce.class);
        
        
        conf.setNumMapTasks(5);
        conf.setNumReduceTasks(2);
        
      //We wil give 2 arguments at the run time, one in input path and other is output path
        Path ip = new Path(args[0]);
        Path op = new Path(intermediateOP1);
      
        FileSystem fs = FileSystem.get(conf);
        
        if(fs.exists(op)){
           If exist delete the output path
           fs.delete(op,true);
        }
        
        
        //the hdfs input and output directory to be fetched from the command line
        FileInputFormat.addInputPath(conf, ip);
        FileOutputFormat.setOutputPath(conf, op);
        
        JobClient.runJob(conf);
        
        ////////////////////////////////////////////////////////////
        
        JobConf conf2 = new JobConf(getConf(), Cleaner.class);
        conf2.setJobName("WordScorer");

      //Setting configuration object with the Data Type of output Key and Value
        conf2.setOutputKeyClass(Text.class);
        conf2.setOutputValueClass(Text.class);

      //Providing the mapper and reducer class names
        conf2.setMapperClass(WordScorerMap.class);
        conf2.setReducerClass(WordScorerReduce.class);


      //We wil give 2 arguments at the run time, one in input path and other is output path
        ip = new Path(intermediateOP1);
        op = new Path(intermediateOP2);

        fs = FileSystem.get(conf2);

        if(fs.exists(op)){
           //If exist delete the output path
           fs.delete(op,true);
        }

        //the hdfs input and output directory to be fetched from the command line
        FileInputFormat.addInputPath(conf2, ip);
        FileOutputFormat.setOutputPath(conf2, op);

        JobClient.runJob(conf2);

        
        
 ////////////////////////////////////////////////////////////
        
        JobConf conf3 = new JobConf(getConf(), Cleaner.class);
        conf3.setJobName("MoodDimensionScorer");

      //Setting configuration object with the Data Type of output Key and Value
        conf3.setOutputKeyClass(Text.class);
        conf3.setOutputValueClass(Text.class);

      //Providing the mapper and reducer class names
        conf3.setMapperClass(MoodDimensionScorerMap.class);
        conf3.setReducerClass(MoodDimensionScorerReduce.class);


      //We wil give 2 arguments at the run time, one in input path and other is output path
        ip = new Path(intermediateOP2);
        op = new Path(args[1]);

        fs = FileSystem.get(conf3);

        if(fs.exists(op)){
           //If exist delete the output path
           fs.delete(op,true);
        }

        //the hdfs input and output directory to be fetched from the command line
        FileInputFormat.addInputPath(conf3, ip);
        FileOutputFormat.setOutputPath(conf3, op);

        JobClient.runJob(conf3);     
        return 0;        
    }

    
    public static void main(String[] args) throws Exception {
        
        int exitCode = ToolRunner.run(new Configuration(), new Cleaner(), args);
        System.exit(exitCode);

    }

}
*/