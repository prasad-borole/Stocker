package com.stocker.combinations;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Set;

public class CombinationsDriver {
    public static String resultFoler = "finalResults/";
    public static void main(String[] args) {

        try {
            BufferedWriter bw = new BufferedWriter(new FileWriter(resultFoler + "combinationResults.txt", true));
            BufferedReader br = new BufferedReader(new FileReader(resultFoler + "mergedResults.txt"));
            /*anger_score  confusion_score   depression_score   fatigue_score   tensions_score   vigour_score*/
            String[] current12Dimension = new String[12];
            current12Dimension[0] = "anger";
            current12Dimension[1] = "confusion";
            current12Dimension[2] = "depression";
            current12Dimension[3] = "fatigue";
            current12Dimension[4] = "tension";
            current12Dimension[5] = "vigour";
            
            current12Dimension[6] = "neg_anger";
            current12Dimension[7] = "neg_confusion";
            current12Dimension[8] = "neg_depression";
            current12Dimension[9] = "neg_fatigue";
            current12Dimension[10] = "neg_tension";
            current12Dimension[11] = "neg_vigour";
            
            
            String line = "";
            int lineNum = 0;
            while ((line = br.readLine()) != null) {      
                StringBuilder finalScoreString = new StringBuilder();
                StringBuilder columnNameString = new StringBuilder();

                double[] current12DimensionScores = new double[12];
                String[] lineSplit = line.split("\t");
                String date = lineSplit[0];
                finalScoreString.append(date);
                finalScoreString.append("\t");
    
                
                for(int i = 1; i < lineSplit.length; i++){
                    int index = i-1;
                    double dimScore = Double.parseDouble(lineSplit[i]);
                    double neg_dimScore = 1 - dimScore;
                    current12DimensionScores[index] = dimScore;
                    current12DimensionScores[index+6] = neg_dimScore;
                }
                Combinations.combinations2(current12Dimension, current12DimensionScores, 3, 0, new String[3], new double[3]);
                Combinations.combinations2(current12Dimension, current12DimensionScores, 2, 0, new String[2], new double[2]);
                Set<String> keys = Combinations.combinationToScoreMap.keySet();
                
                if(lineNum == 0){
                    columnNameString.append("Date");
                    columnNameString.append("\t");
                }

                
                for(String key : keys){
                    if(lineNum == 0){
                        columnNameString.append(key);
                        columnNameString.append("\t");
                    }
                    finalScoreString.append(Combinations.combinationToScoreMap.get(key));
                    finalScoreString.append("\t");
                }
         
                
                
                if(lineNum == 0){
                    bw.write(columnNameString.toString());
                    bw.write("\n");
                    
                }
                bw.write(finalScoreString.toString());
                bw.write("\n");
                
                lineNum += 1;
                Combinations.combinationToScoreMap.clear();
                

            }
            
            br.close();
            bw.close();
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }

    }

}
