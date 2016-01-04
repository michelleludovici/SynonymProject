package se.milu.maltparser;

import org.apache.commons.io.FileUtils;
import org.junit.Test;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

/**
 * Created by Michelle on 2015-12-12.
 */
public class SentenceClassifier {

    public static final File FILE_POS_ADJ_ADV = new File("posAdjAdv.txt");
    public static final File FILE_NEG_ADJ_ADV = new File("negAdjAdv.txt");
    public static final File FILE_POS_VERBS = new File("posVerbs.txt");
    public static final File FILE_NEG_VERBS = new File("negVerbs.txt");
    //public static final File FILE_POS_ADJ_ADV = new File("C:/Users/Michelle/IdeaProjects/SynonymProject/ThreeIter/ThreeIterPosAdjAdv.txt");
    //public static final File FILE_NEG_ADJ_ADV = new File("C:/Users/Michelle/IdeaProjects/SynonymProject/ThreeIter/ThreeIterNegAdjAdv.txt");
    //public static final File FILE_POS_VERBS = new File("C:/Users/Michelle/IdeaProjects/SynonymProject/ThreeIter/ThreeIterPosVerbs.txt");
    //public static final File FILE_NEG_VERBS = new File("C:/Users/Michelle/IdeaProjects/SynonymProject/ThreeIter/ThreeIterNegVerbs.txt");

    class WordWithMetadata {
        public WordWithMetadata(String word, boolean negated) {
            this.word = word;
            this.negated = negated;
        }

        public String word;
        public boolean negated = false;

        @Override
        public String toString() {
            return "{" +
                    "word='" + word + '\'' +
                    ", negated=" + negated +
                    '}';
        }
    }

    @Test
    public void read_and_classify_sentences() throws IOException, ClassNotFoundException {

        // Filter double-, tripple-, ... -words with one word of same sentiment
        // Ex: "ta hand om" --> stödja
        replace_multiword_expression(FILE_POS_VERBS, FILE_NEG_VERBS, FILE_POS_VERBS, "testmeningar.txt", "stödja", "beskylla", "testmeningar_filtrerad.txt");
        replace_multiword_expression(FILE_POS_VERBS, FILE_NEG_VERBS, FILE_NEG_VERBS, "testmeningar_filtrerad.txt", "stödja", "beskylla", "testmeningar_filtrerad.txt");

        replace_multiword_expression(FILE_POS_ADJ_ADV, FILE_NEG_ADJ_ADV, FILE_POS_ADJ_ADV, "testmeningar_filtrerad.txt", "bästa", "förkrossande", "testmeningar_filtrerad.txt");
        replace_multiword_expression(FILE_POS_ADJ_ADV, FILE_NEG_ADJ_ADV, FILE_NEG_ADJ_ADV, "testmeningar_filtrerad.txt", "bästa", "förkrossande", "testmeningar_filtrerad.txt");

        ParsingUtillity taggerUtil = new ParsingUtillity();
        String base = "./";
        String[] res = taggerUtil.parseConllFile(base + "swedish.bin", new String[] {base + "testmeningar_filtrerad.txt"});


        /*ConcurrentDependencyGraph outputGraph = null;
        // Loading the Swedish model swemalt-mini
        ConcurrentMaltParserModel model = null;
        try {
            URL swemaltMiniModelURL = new File(base + "swemalt-1.7.2.mco").toURI().toURL();
            model = ConcurrentMaltParserService.initializeParserModel(swemaltMiniModelURL);
        } catch (Exception e) {
            e.printStackTrace();
        }*/

        /*for (String sentence : res) {
            System.out.println(sentence);
        }*/



        /*StanfordCoreNLP.main(new String[]{
                "-annotators", "tokenize, ssplit, pos"
                , "-file", "testmeningar.txt"
                , "-pos.model", "swedish-pos-tagger-model"
                , "-outputDirectory", "./output/"
                , "-outputFormat", "conll"
        });*/





        //Scanner bucky= new Scanner(new FileInputStream("./output/testmeningar.txt.conll"));
        StringReader reader = new StringReader(String.join("\n\n", res));
        Scanner bucky= new Scanner(reader);
        List<WordWithMetadata> adjAdvBucket= new ArrayList<>();
        List<WordWithMetadata> verbBucket= new ArrayList<>();
        List<String> nounBucket= new ArrayList<>();

        int negatedCounter = -1;
        while (true){
            negatedCounter = negatedCounter - 1;
            String line= bucky.nextLine();

            if (line.trim().length() == 0 || !bucky.hasNext()) {

                System.out.println("End of line. Insert classification here.");

                System.out.println("AdjAdv Bucket: " + adjAdvBucket);
                System.out.println("verb Bucket: " + verbBucket);
                System.out.println("noun Bucket: " + nounBucket);

                //adding sentiments
                String sentimentClassificationStrategy = "plus";
                if (sentimentClassificationStrategy.equals("plus")) {

                    NaiveBayes nb_adj = new NaiveBayes(FILE_POS_ADJ_ADV, FILE_NEG_ADJ_ADV);
                    NaiveBayes nb_verb = new NaiveBayes(FILE_POS_VERBS, FILE_NEG_VERBS);
                    float totalSentiment=0;
                    for (WordWithMetadata wordWithMetadata : adjAdvBucket) {
                        float wordSentiment = nb_adj.naiveBayesClassify(wordWithMetadata.word);
                        //if 'inte' was present, word was flagged 'negated' and wordSentiment reverses polarity
                        if(wordWithMetadata.negated==true){
                            wordSentiment= 1-wordSentiment;
                        }
                        totalSentiment+= wordSentiment;
                    }
                    for (WordWithMetadata wordWithMetadata : verbBucket) {
                        float wordSentiment = nb_verb.naiveBayesClassify(wordWithMetadata.word);
                        //if 'inte' was present, word was flagged 'negated' and wordSentiment reverses polarity
                        if(wordWithMetadata.negated==true){
                            wordSentiment= 1-wordSentiment;
                        }
                        totalSentiment+= wordSentiment;
                    }

                    totalSentiment= totalSentiment/(adjAdvBucket.size()+ verbBucket.size());
                    System.out.println("Topic: " + nounBucket + " Sentence Sentiment: " + totalSentiment);
                    System.out.println("----------------------------------------------");
                }

                negatedCounter = -1;
                adjAdvBucket.clear();
                nounBucket.clear();
                verbBucket.clear();

                if (bucky.hasNext()) continue;
                else break;
            }

            String[] split = line.split("\\t");
            String word = split[2];
            String postag = split[3];
            switch (postag){
                case "AB":
                    if(word.equals("inte")){
                        negatedCounter=5;
                    }else {
                        adjAdvBucket.add(new WordWithMetadata(word, negatedCounter>=0));
                    }

                    break;
                case "JJ":
                    adjAdvBucket.add(new WordWithMetadata(word, negatedCounter>=0));
                    break;
                case "VB":
                    verbBucket.add(new WordWithMetadata(word, negatedCounter>=0));
                    break;
                case "NN":
                    nounBucket.add(word);
                    break;
            }

        }


    }

    private void replace_multiword_expression(File filePosWords, File fileNegWords, File fileSentimentWords, String fileSentences, String replacePositiveWord, String replaceNegativeWord, String fileSentencesOutput) throws IOException {
        NaiveBayes nb_prepare = new NaiveBayes(filePosWords, fileNegWords);
        Scanner bucky1 = new Scanner(new FileInputStream(fileSentimentWords), "UTF-8");
        String sentences = FileUtils.readFileToString(new File(fileSentences), "UTF-8");
        while(bucky1.hasNext()){
            String line = bucky1.nextLine();
            if (line.contains(" ")) {
                Integer posWordCount = nb_prepare.positiveWordCount.containsKey(line) ? nb_prepare.positiveWordCount.get(line) : 0;
                Integer negWordCount = nb_prepare.negativeWordCount.containsKey(line) ? nb_prepare.negativeWordCount.get(line) : 0;
                boolean positive = posWordCount > negWordCount;
                if (positive) {
                    sentences = sentences.replaceAll(line, replacePositiveWord);
                } else {
                    sentences = sentences.replaceAll(line, replaceNegativeWord);
                }
            }
        }
        FileUtils.write(new File(fileSentencesOutput), sentences, "UTF-8");
    }


}
