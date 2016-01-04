package se.milu.maltparser;

import java.io.*;
import java.nio.charset.Charset;
import java.util.*;

/**
 * Created by Michelle on 2015-12-04.
 */
public class ShufflingWords {
    private final SynonymAPI api;

    ArrayList<String> wordlist = new ArrayList<>();


    public ShufflingWords() {
        //private final List<String> wordlist;
        api = new SynonymAPI();

    }



    public List<String> addWordsToWordList(File fromWordlist) throws IOException {

        Scanner bucky = new Scanner(new FileInputStream(fromWordlist));
        while (bucky.hasNext()) {
            String word = bucky.nextLine();
            wordlist.add(word + "\n");
        }
        Collections.shuffle(wordlist);
        FileWriter writer = new FileWriter("shuffledVerbList.txt");
        for(String str: wordlist) {
            writer.write(str);
            Charset.forName("UTF-8").newDecoder();
        }
        writer.close();

        return wordlist;
    }


}
