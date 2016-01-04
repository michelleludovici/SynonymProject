package se.milu.maltparser;

        import java.io.File;
        import java.io.FileNotFoundException;
        import java.util.*;

/**
 * Created by Michelle on 2015-12-03.
 */
public class AveragingMethod {
    protected final List<String> positiveWords;
    protected final List<String> negativeWords;
    protected final Map<String, Integer> positiveWordCount;
    protected final Map<String, Integer> negativeWordCount;
    protected final Set<String> positiveVocabulary;
    protected final Set<String> negativeVocabulary;
    protected final SynonymAPI api;

    public AveragingMethod (File pos, File neg) throws FileNotFoundException {
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

        NaiveBayes nb = new NaiveBayes(pos,neg);
        nb.analyzeFrequencies(pos, positiveWords, positiveWordCount, positiveVocabulary);
        nb.analyzeFrequencies(neg, negativeWords, negativeWordCount, negativeVocabulary);
    }

    public float averagingMethodClassify(String word) {
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

        float sum = 0;
        for (String synonym : synonyms) {
            Integer wordOccurrance = wordCount.get(synonym);
            if (wordOccurrance == null) continue;
            sum += wordOccurrance;
        }/*TODO: add synonym to pos or neg list after classification*/
        sum = sum/words.size();
        sum = sum* (float) (words.size()/(float)(positiveWords.size()+negativeWords.size()));
        return sum;
    }

}
