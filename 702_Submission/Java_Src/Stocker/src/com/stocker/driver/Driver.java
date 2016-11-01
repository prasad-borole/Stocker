package com.stocker.driver;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.conf.Configured;
import org.apache.hadoop.fs.FileSystem;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.io.IntWritable;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapred.FileInputFormat;
import org.apache.hadoop.mapred.FileOutputFormat;
import org.apache.hadoop.mapred.JobClient;
import org.apache.hadoop.mapred.JobConf;
import org.apache.hadoop.util.Tool;
import org.apache.hadoop.util.ToolRunner;

import com.stocker.mapper.CleanerMap;
import com.stocker.mapper.MoodDimensionScorerMap;
import com.stocker.mapper.WordScorerMap;
import com.stocker.reducer.CleanerReduce;
import com.stocker.reducer.MoodDimensionScorerReduce;
import com.stocker.reducer.WordScorerReduce;

public class Driver extends Configured implements Tool{
    

    @Override
    public int run(String[] args) throws Exception {
        //creating a JobConf object and assigning a job name for identification purposes
        System.out.println(getConf());
        String intermediateOP1 = "intermediateOutput1";
        String intermediateOP2 = "intermediateOutput2";
        
        /* Initial cleaning/sentiment filtering job */ 
        
        JobConf cleanerJobConf = new JobConf(getConf(), Driver.class);
        cleanerJobConf.setJobName("StockerCleaner");
        
        //Setting configuration object with the Data Type of output Key and Value
        cleanerJobConf.setOutputKeyClass(Text.class);
        cleanerJobConf.setOutputValueClass(IntWritable.class);
        
        //Providing the mapper and reducer class names
        cleanerJobConf.setMapperClass(CleanerMap.class);
        cleanerJobConf.setReducerClass(CleanerReduce.class);
        
        
        /*
         * conf.setNumMapTasks(5);
         * conf.setNumReduceTasks(2);
         */
        
      //We wil give 2 arguments at the run time, one in input path and other is output path
        Path ip = new Path(args[0]);
        Path op = new Path(intermediateOP1);
      
        FileSystem fs = FileSystem.get(cleanerJobConf);
        
        if(fs.exists(op)){
           /*If exist delete the output path*/
           fs.delete(op,true);
        }
        
        
        //the hdfs input and output directory to be fetched from the command line
        FileInputFormat.addInputPath(cleanerJobConf, ip);
        FileOutputFormat.setOutputPath(cleanerJobConf, op);
        
        JobClient.runJob(cleanerJobConf);
        
        /* Word scorer job */
        
        JobConf worderScorerJobConf = new JobConf(getConf(), Driver.class);
        worderScorerJobConf.setJobName("WordScorer");

        //Setting configuration object with the Data Type of output Key and Value
        worderScorerJobConf.setOutputKeyClass(Text.class);
        worderScorerJobConf.setOutputValueClass(Text.class);

        //Providing the mapper and reducer class names
        worderScorerJobConf.setMapperClass(WordScorerMap.class);
        worderScorerJobConf.setReducerClass(WordScorerReduce.class);


        //We wil give 2 arguments at the run time, one in input path and other is output path
        ip = new Path(intermediateOP1);
        op = new Path(intermediateOP2);

        fs = FileSystem.get(worderScorerJobConf);

        if(fs.exists(op)){
           //If exist delete the output path
           fs.delete(op,true);
        }

        //the hdfs input and output directory to be fetched from the command line
        FileInputFormat.addInputPath(worderScorerJobConf, ip);
        FileOutputFormat.setOutputPath(worderScorerJobConf, op);

        JobClient.runJob(worderScorerJobConf);

        
        
        /* Mood dimension score Job */
        
        JobConf moodDimensionScorerConf = new JobConf(getConf(), Driver.class);
        moodDimensionScorerConf.setJobName("MoodDimensionScorer");

      //Setting configuration object with the Data Type of output Key and Value
        moodDimensionScorerConf.setOutputKeyClass(Text.class);
        moodDimensionScorerConf.setOutputValueClass(Text.class);

      //Providing the mapper and reducer class names
        moodDimensionScorerConf.setMapperClass(MoodDimensionScorerMap.class);
        moodDimensionScorerConf.setReducerClass(MoodDimensionScorerReduce.class);


      //We wil give 2 arguments at the run time, one in input path and other is output path
        ip = new Path(intermediateOP2);
        op = new Path(args[1]);

        fs = FileSystem.get(moodDimensionScorerConf);

        if(fs.exists(op)){
           //If exist delete the output path
           fs.delete(op,true);
        }

        //the hdfs input and output directory to be fetched from the command line
        FileInputFormat.addInputPath(moodDimensionScorerConf, ip);
        FileOutputFormat.setOutputPath(moodDimensionScorerConf, op);

        JobClient.runJob(moodDimensionScorerConf);     
        return 0;        
    }

    
    public static void main(String[] args) throws Exception {
        
        int exitCode = ToolRunner.run(new Configuration(), new Driver(), args);
        System.exit(exitCode);

    }
}
