package com.stocker.poms;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;


public class POMSHelper {
    public static HashMap<String, HashSet<String>> globalWordMap = new HashMap<>();
    public static HashMap<String, HashSet<String>> wordToPoms65Word = new HashMap<>();
    public static HashMap<String, String> word65toMoodDimension = new HashMap<>();
    public static HashSet<String> allPOMSWord = new HashSet<String>();
    public static void populateGlobalMap(){
        try {
            
            BufferedReader br = new BufferedReader(new FileReader("poms_expanded.txt"));
            String line = null;
            
            while(null != (line = br.readLine())){
                String[] lineSplit = line.split(",");
                HashSet<String> relatedWordSet = new HashSet<>();
                String keyWord = lineSplit[0].trim();
                relatedWordSet.add(keyWord);
                allPOMSWord.add(keyWord);
                for(int i = 1; i < lineSplit.length; i++){
                    String valWord = lineSplit[i].trim();
                    relatedWordSet.add(valWord);
                    allPOMSWord.add(valWord);
                }
                globalWordMap.put(keyWord, relatedWordSet);
            }
            //Closing file now
            br.close();
        } catch (FileNotFoundException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }
    
    
    public static void populateWordtoPomsWord65(){
        try {           
            BufferedReader br = new BufferedReader(new FileReader("poms_expanded.txt"));
            String line = null;
            
            while(null != (line = br.readLine())){
                String[] lineSplit = line.split(",");
                String pom65Word = lineSplit[0].trim();
                for(int i = 0; i < lineSplit.length; i++){
                    String currentWord = lineSplit[i].trim();
                    // For example, word "sad" will belong to 5 POM 65 words viz. "unhappy", "sorry", "sad", "gloomy" and "bitter"
                    if(wordToPoms65Word.containsKey(currentWord)){
                        wordToPoms65Word.get(currentWord).add(pom65Word);
                    }else{
                        HashSet<String> relatedList = new HashSet<String>();
                        relatedList.add(pom65Word);
                        wordToPoms65Word.put(currentWord, relatedList);
                    }
                }
            }
            //Closing file now
            br.close();
        } catch (FileNotFoundException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }
    
    public static void populateWord65ToMoodDimensionMap(){
        try {
            
            BufferedReader br = new BufferedReader(new FileReader("wordToMood.txt"));
            String line = null;
            
            while(null != (line = br.readLine())){
                String[] local = line.split(" ");;
                String pomsMoodDimension = local[0];
                String[] wordList = local[1].split(",");
                for(String word : wordList){
                    word65toMoodDimension.put(word, pomsMoodDimension);
                }
            }
            //Closing file now
            br.close();
        } catch (FileNotFoundException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }
    public static void makeLoweCaseFile(){
        try {
            File file = new File("wordToMood.txt");
            FileWriter fw = new FileWriter(file.getAbsoluteFile());
            BufferedWriter bw = new BufferedWriter(fw);
            BufferedReader br = new BufferedReader(new FileReader("wordToMoodMappingOld.txt"));
            String line = null;
            
            while(null != (line = br.readLine())){
               line = line.replaceAll("\\s", " ");
               line = line.replaceAll(", ", ",");
               StringBuilder sb = new StringBuilder(line);
               sb.append("\n");
               bw.write(sb.toString().toLowerCase());
            }
            System.out.println("DONE!");
            bw.close();
            br.close();
        } catch (FileNotFoundException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }
    public static void main(String[] args) {
        //unit testing function
        populateGlobalMap();
        
        HashSet<String> x = globalWordMap.get("spiteful");
        for(String s : x){
            System.out.println(s);
        }
        
        populateWord65ToMoodDimensionMap();
        Set<String> keyset = word65toMoodDimension.keySet();
        for(String s : keyset){
            System.out.println(word65toMoodDimension.get(s));
        }
        
        
        
        populateWordtoPomsWord65();
        HashSet<String> test = wordToPoms65Word.get("sad");
        for(String s : test){
            System.out.println(s);
        }
        
        
        
/*        makeLoweCaseFile();*/

    }

}
