package com.stocker.combinations;

import java.util.Arrays;
import java.util.Set;
import java.util.TreeMap;

public class Combinations {
    public static TreeMap<String, Double> combinationToScoreMap = new TreeMap<>(); 
    public static void main(String[] args){
        String[] arr = {"A","B","C","D","E","F"};
        double[] scores = {1.0,2.0, 3.0, 4.0, 5.0, 6.0};
        
        combinations2(arr, scores, 3, 0, new String[3], new double[3]);
        
        Set<String> keySet = combinationToScoreMap.keySet();
        for(String key : keySet){
            System.out.println(key+" => "+combinationToScoreMap.get(key));
        }
    }
    
    public static double sum(double[] values) {
        double result = 0;
        for (double value : values) {
            result += value;
        }
        return result;
    }
    
    /*
     * Accepts baseDimensions[] = {Tension, Depression,...., Tension_Negation, Depression_Negation,....}
     * scores[] = their score array from MR
     * choose = how many to choose among N
     * startPosition = 0
     * result[] = empty array of Strings of length N
     * resultScores[] = empty array of doubles of length N
     */

    public static void combinations2(String[] baseDimensions, double[] scores, int choose, int startPosition, String[] result, double[] resultScores){
        if (choose == 0){
            combinationToScoreMap.put(Arrays.toString(result), sum(resultScores));
            //System.out.println(Arrays.toString(result));
            //System.out.println(Arrays.toString(resultScores));
            return;
        }       
        for (int i = startPosition; i <= baseDimensions.length-choose; i++){
            result[result.length - choose] = baseDimensions[i];
            resultScores[resultScores.length - choose] = scores[i];
            combinations2(baseDimensions, scores, choose-1, i+1, result, resultScores);
        }
    }       
}