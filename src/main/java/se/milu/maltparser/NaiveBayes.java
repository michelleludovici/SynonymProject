package se.milu.maltparser;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.util.*;

/**
 * Created by Michelle on 2015-12-01.
 */
public class NaiveBayes {


    protected final List<String> positiveWords;
    protected final List<String> negativeWords;
    protected final Map<String, Integer> positiveWordCount;
    protected final Map<String, Integer> negativeWordCount;
    protected final Set<String> positiveVocabulary;
    protected final Set<String> negativeVocabulary;
    protected final SynonymAPI api;

    public NaiveBayes (File pos, File neg) throws FileNotFoundException {

        api = new SynonymAPI();
        //Words = list of all words of class c
        positiveWords = new ArrayList<>();
        negativeWords = new ArrayList<>();
        //frequencyCounts are in WordCount
        positiveWordCount = new HashMap<>();
        negativeWordCount = new HashMap<>();
        //unique words are in Vocabulary
        positiveVocabulary = new HashSet<>();
        negativeVocabulary = new HashSet<>();

        analyzeFrequencies(pos, positiveWords, positiveWordCount, positiveVocabulary);
        analyzeFrequencies(neg, negativeWords, negativeWordCount, negativeVocabulary);

    }

    public void analyzeFrequencies(File filelist, List<String> wordlist, Map<String, Integer> wordCount, Set<String> uniqueWords) throws FileNotFoundException {
        Scanner bucky = new Scanner(new FileInputStream(filelist), "UTF-8");
        while (bucky.hasNext()) {
            String word = bucky.nextLine();
            uniqueWords.add(word);
            wordlist.add(word);
            if (wordCount.containsKey(word)) {
                wordCount.put(word, wordCount.get(word)+1);
            } else{
                wordCount.put(word, 1);
            }
        }
    }

    public float naiveBayesClassify(String word){
       // if(positiveVocabulary.contains(word)) return 1;
        //if(negativeVocabulary.contains(word)) return 0;
        Map<String, List<String>> query = api.query("format=json&word=" + word);
        List<String> synonyms = query.get("synonym");

        System.out.println("Synonymer: " + synonyms);

        Iterator<String> iterator = synonyms.iterator();
        while(iterator.hasNext()){
            String syn = iterator.next();
            if(!positiveVocabulary.contains(syn) && !negativeVocabulary.contains(syn)){
                iterator.remove();
                continue;
            }
        }

        if(synonyms.size()==0){
           // throw new RuntimeException("Word not recognized: " + word);
            return (float) 0.49;
        }

        float sumPositive = probabilityForClass(synonyms, positiveWordCount, positiveWords);
        float sumNegative = probabilityForClass(synonyms, negativeWordCount, negativeWords);

        float P_pos = sumPositive / (sumPositive + sumNegative);
        float P_neg = sumNegative / (sumPositive + sumNegative);

        System.out.println(String.format("Word: %s, P(+) = %f  P(-) = %f", word, P_pos, P_neg));

        return P_pos;
    }

    private float probabilityForClass(List<String> synonyms, Map<String, Integer> wordCount, List<String> words) {
        float sum = (float) Math.log(words.size()/(float)(positiveWords.size()+negativeWords.size()));
        for (String synonym : synonyms) {
            Integer wordOccurrance = wordCount.get(synonym);
            if (wordOccurrance == null) continue;
            sum += Math.log(wordOccurrance /(float)words.size());
        }/*TODO: add synonym to pos or neg list after classification*/
        return sum;
    }
}
