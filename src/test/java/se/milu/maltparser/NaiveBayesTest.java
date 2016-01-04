package se.milu.maltparser;

import org.junit.Test;

import java.io.*;
import java.util.HashSet;
import java.util.Scanner;
import java.util.Set;

/**
 * Created by Michelle on 2015-12-01.
 */
public class NaiveBayesTest {

    public static final String WORD_VERB = "konkurrera";
    public static final String WORD_Adj = "bottenlös";

    @Test
    public void test_naive_bayes() throws IOException {


        NaiveBayes nb_adj = new NaiveBayes(new File("posAdjAdv.txt"), new File("negAdjAdv.txt"));
        NaiveBayes nb_verb = new NaiveBayes(new File("posVerbs.txt"), new File("negVerbs.txt"));;
        float classification = nb_verb.naiveBayesClassify(WORD_VERB);
        float classificationAdj = nb_adj.naiveBayesClassify(WORD_Adj);

        System.out.println("");
        System.out.println("Classification NB_Verb = " + classification);
        System.out.println("Classification NB_Adj = " + classificationAdj);
    }


   // @Test - Does not work (yet), not needed either
    /*public void test_naive_bayes_prod() throws IOException {

        File pos = new File ("posAdjAdv.txt");
        File neg= new File ("negAdjAdv.txt");

        NaiveBayesProd nbp = new NaiveBayesProd(pos,neg);
        float classification = nbp.naiveBayesClassify(WORD_VERB);

        System.out.println("Classification NBprod = " + classification);

    }*/

    @Test
    public void test_averaging_method() throws IOException {


        File pos = new File ("posVerbs.txt");
        File neg= new File ("negVerbs.txt");

        AveragingMethod am_Adj = new AveragingMethod(new File("posAdjAdv.txt"), new File("negAdjAdv.txt"));
        AveragingMethod am_Verb = new AveragingMethod(new File("posVerbs.txt"), new File("negVerbs.txt"));
        float classification = am_Verb.averagingMethodClassify(WORD_VERB);
        float classificationAdj = am_Adj.averagingMethodClassify(WORD_Adj);

        System.out.println("");
        System.out.println("Classification AM Verb = " + classification);
        System.out.println("Classification AM Adj = " + classificationAdj);
    }

    @Test
    public void test_Shuffling_Words() throws IOException {
            File wordlist = new File ("shuffledVerbList.txt");
        ShufflingWords sw = new ShufflingWords();
        sw.addWordsToWordList(wordlist);

    }

    @Test
    public void filter_sample_lines() throws IOException {
        int maxLines = 5000;
        Set<String> uniqueLines = new HashSet<>();
        File input = new File("sampleVerbs.txt");
        File output = new File("sampleVerbs.txt");

        Scanner bucky = new Scanner(new FileInputStream(input));
        while(bucky.hasNext() && uniqueLines.size() < maxLines) {
            String line = bucky.nextLine();
            uniqueLines.add(line);
        }

        FileWriter writer = new FileWriter(output);
        for (String line : uniqueLines) {
            writer.write(line + "\r\n");
        }

        writer.close();
        //System.out.println(output.length());
    }
}
