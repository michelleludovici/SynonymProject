package se.milu.maltparser;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.*;

/**
 * Created by Michelle on 2015-12-09.
 */
public class test {

    public static void main(String [] args) throws IOException {

        //File file1= new File("posAdjAdv_filtered.txt");
       // File file2= new File("C:/Users/Michelle/Desktop/SentimentAnalysis/synonymExtractor/classified_words_copy.txt");

       List<String> file1= Files.readAllLines(Paths.get("Lista.txt"));
        System.out.println(file1.size());
        List<String> file3= new ArrayList<>();
        List<String> file2= Files.readAllLines(Paths.get("generalInquirer.txt"));
        System.out.println(file2.size());
        for(String i:file1){
           // boolean wordEquals = false;
            for (String j:file2){

                if(i.equalsIgnoreCase(j)){
                    //wordEquals = true;
                    file3.add(i);
                }
            }
            //if(wordEquals == false)


        }
    System.out.println(file3.size());
        Files.write(Paths.get("SameIn.txt"), file3);
    }


}
