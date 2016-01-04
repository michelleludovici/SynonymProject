package se.milu.maltparser;

import java.io.*;
import java.util.*;

/**
 * Created by Michelle on 2015-12-09.
 */
public class SeedExpansionComparer extends NaiveBayes {

    public SeedExpansionComparer(File pos, File neg) throws FileNotFoundException {
        super(pos, neg);
    }



    public void compareHumanSeedList(File human) throws IOException {
        Map<String,Float> humanPosClassification= new HashMap<>();
        Scanner bucky = new Scanner(new FileInputStream(human), "UTF8");
        while (bucky.hasNext()){
            String line = bucky.nextLine();
            String[] split = line.split(":");
            String word = split[0];
            String positive = split[1];
            String negative = split[2];
            float positiveRatio = Integer.parseInt(positive)/(float)(Integer.parseInt(positive) + Integer.parseInt(negative) );
            humanPosClassification.put(word, positiveRatio);
        }

        Map<String, Float> robotPosClassification = new HashMap<>();
        Set<String> allRobotWords = new HashSet<>();
        allRobotWords.addAll(this.positiveVocabulary);
        allRobotWords.addAll(this.negativeVocabulary);

        for (String word : allRobotWords) {
            int posCount = this.positiveWordCount.containsKey(word) ? this.positiveWordCount.get(word) : 0;
            int negCount = this.negativeWordCount.containsKey(word) ? this.negativeWordCount.get(word) : 0;
            float positive = posCount / (float)(posCount + negCount);
            robotPosClassification.put(word, positive);
        }


        FileWriter fw=new FileWriter(new File("MatlabFile.txt"));


        // Print out
        for (String word : humanPosClassification.keySet()) {

            float humanPos = humanPosClassification.containsKey(word) ? humanPosClassification.get(word) : 0;
            float robotPos = robotPosClassification.containsKey(word) ? robotPosClassification.get(word) : 0;
            String output = String.format("Word:  %30s  \t\t\t HumanPositive: %f \t\t\t RobotPositive: %f \t\t\t Skillnad: %f",
                    word,
                    humanPos,
                    robotPos,
                    humanPos - robotPos);
            System.out.println(output);
            fw.write(output);
            fw.write("\r\n");
        }
        fw.close();

        int correctlyClassified = 0;
        int countHumanTrue=0;
        int countRobotTrue=0;
        for (String word : humanPosClassification.keySet()) {

            boolean humanIsPos = humanPosClassification.get(word) >= 0.5;
            boolean robotIsPos = robotPosClassification.get(word) >= 0.5;
            boolean correct = humanIsPos == robotIsPos;
            correctlyClassified += correct ? 1 : 0;
            if (humanIsPos==true) {
                countHumanTrue+= 1;
            }
            if (robotIsPos==true) {
                countRobotTrue+= 1;
            }
        }


        System.out.println(String.format("Total classification agreement i procent:  %d / %d = %f %%",
                correctlyClassified,
                humanPosClassification.size(),
                100 * (correctlyClassified / (float) humanPosClassification.size())));

        System.out.println("CorrectlyClassifiedWords: " + correctlyClassified + " Nr Words in Human Pos Classification: " + humanPosClassification.size());
        System.out.println("CountHumanTrue: " + countHumanTrue);
        System.out.println("CountRobotTrue: " + countRobotTrue);

    }
}
