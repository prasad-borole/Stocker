package com.stocker.combinations;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;

public class MergeMonths {
    public static final int START_MONTH = 6;
    public static final int END_MONTH = 12;
    public static String resultFoler = "finalResults/";

    public static void main(String[] args) {
        // Read all text files and prepare one combined file for results from
        // start month to end month
        try {
            BufferedWriter bw = new BufferedWriter(new FileWriter(resultFoler + "mergedResults.txt", true));
            for (int i = START_MONTH; i <= END_MONTH; i++) {

                BufferedReader br = new BufferedReader(new FileReader(resultFoler + i + ".txt"));

                String line = "";
                while ((line = br.readLine()) != null) {
                    bw.write(line);
                    bw.write("\n");

                }
                br.close();

            }
            bw.close();
            System.out.println("MERGING DONE!");
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }

    }

}
