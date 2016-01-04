package se.milu.maltparser;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.ObjectInputStream;
import java.io.Reader;
import java.io.StringWriter;
import java.net.URL;
import java.util.ArrayList;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.zip.GZIPInputStream;

import org.maltparser.concurrent.ConcurrentMaltParserModel;
import org.maltparser.concurrent.ConcurrentMaltParserService;
import org.maltparser.concurrent.graph.ConcurrentDependencyGraph;
import se.su.ling.stagger.CTBTagger;
import se.su.ling.stagger.EnglishTokenizer;
import se.su.ling.stagger.GenericTagger;
import se.su.ling.stagger.LatinTokenizer;
import se.su.ling.stagger.PTBTagger;
import se.su.ling.stagger.SUCTagger;
import se.su.ling.stagger.SwedishTokenizer;
import se.su.ling.stagger.TagNameException;
import se.su.ling.stagger.TaggedData;
import se.su.ling.stagger.TaggedToken;
import se.su.ling.stagger.Tagger;
import se.su.ling.stagger.Token;
import se.su.ling.stagger.Tokenizer;


public class ParsingUtillity {
    public static boolean plainOutput = false;
    public static boolean extendLexicon = true;
    public static boolean hasNE = true;
    
        /**
     * Creates and returns a tokenizer for the given language.
     */
    private static Tokenizer getTokenizer(Reader reader, String lang) {
        Tokenizer tokenizer;
        if(lang.equals("sv")) {
            tokenizer = new SwedishTokenizer(reader);
        } else if(lang.equals("en")) {
            tokenizer = new EnglishTokenizer(reader);
        } else if(lang.equals("any")) {
            tokenizer = new LatinTokenizer(reader);
        } else {
            throw new IllegalArgumentException();
        }
        return tokenizer;
    }
    /**
     * Creates and returns a tagger for the given language.
     */
    private static Tagger getTagger(String lang, TaggedData td, int posBeamSize, int neBeamSize) {
        Tagger tagger = null;
        switch (lang) {
            case "sv":
                tagger = new SUCTagger(
                        td, posBeamSize, neBeamSize);
                break;
            case "en":
                tagger = new PTBTagger(
                        td, posBeamSize, neBeamSize);
                break;
            case "any":
                tagger = new GenericTagger(
                        td, posBeamSize, neBeamSize);
                break;
            case "zh":
                tagger = new CTBTagger(
                        td, posBeamSize, neBeamSize);
                break;
            default:
                System.err.println("Invalid language: "+lang);
                break;
        }
        return tagger;
    }
    private static BufferedReader openUTF8File(String name) throws IOException {
        if(name.equals("-"))
            return new BufferedReader(
                new InputStreamReader(System.in, "UTF-8"));
        else if(name.endsWith(".gz"))
            return new BufferedReader(new InputStreamReader(
                        new GZIPInputStream(
                            new FileInputStream(name)), "UTF-8"));
        return new BufferedReader(new InputStreamReader(
                    new FileInputStream(name), "UTF-8"));
    }
    public String[] parseConllFile(String modelFile, String[] inputFiles) throws IOException {
        try {
            String[] result = new String[inputFiles.length];
            //ArrayList<String> inputFiles = new ArrayList<String>();
            
            if(inputFiles.length < 1) {
                System.err.println("No files to tag.");
                System.exit(1);
            }
            
            TaggedToken[][] inputSents = null;
            
            ObjectInputStream modelReader = new ObjectInputStream(
                    new FileInputStream(modelFile));
            System.err.println( "Loading Stagger model ...");
            Tagger tagger = (Tagger)modelReader.readObject();
            String lang = tagger.getTaggedData().getLanguage();
            modelReader.close();
            
            // TODO: experimental feature, might remove later
            tagger.setExtendLexicon(extendLexicon);
            if(!hasNE) tagger.setHasNE(false);
            
            int indexResult = 0;
            for(String inputFile : inputFiles) {
                String fileID =
                        (new File(inputFile)).getName().split(
                                "\\.")[0];
                BufferedReader reader = openUTF8File(inputFile);
                BufferedWriter writer;
                StringWriter sb = new StringWriter();
                writer = new BufferedWriter(sb);//
                        //new OutputStreamWriter(System.out, "UTF-8"));
                
                Tokenizer tokenizer = getTokenizer(reader, lang);
                ArrayList<Token> sentence;
                int sentIdx = 0;
                while((sentence=tokenizer.readSentence())!=null) {
                    TaggedToken[] sent =
                            new TaggedToken[sentence.size()];
                    if(tokenizer.sentID != null) {
                        if(!fileID.equals(tokenizer.sentID)) {
                            fileID = tokenizer.sentID;
                            sentIdx = 0;
                        }
                    }
                    for(int j=0; j<sentence.size(); j++) {
                        Token tok = sentence.get(j);
                        String id;
                        id = fileID + ":" + sentIdx + ":" +
                                tok.offset;
                        sent[j] = new TaggedToken(tok, id);
                    }
                    TaggedToken[] taggedSent =
                            tagger.tagSentence(sent, true, false);
                    try {
                        tagger.getTaggedData().writeConllSentence(
                                writer,
                                taggedSent,
                                plainOutput);
                    } catch (TagNameException ex) {
                        Logger.getLogger(ParsingUtillity.class.getName()).log(Level.SEVERE, null, ex);
                    }
                    sentIdx++;
                }
                tokenizer.yyclose();
                writer.close();
                result[indexResult++] = sb.toString();
            }
            return result;
        } catch (ClassNotFoundException ex) {
            Logger.getLogger(ParsingUtillity.class.getName()).log(Level.SEVERE, null, ex);
        }
        return null;
    }

    public static void main(String[] args) throws IOException {
        ParsingUtillity mp = new ParsingUtillity();
        String base = "./";
        String[] res = mp.parseConllFile(base + "swedish.bin", new String[] {base + "testmeningar.txt"});
        
        
        ConcurrentDependencyGraph outputGraph = null;
        // Loading the Swedish model swemalt-mini
        ConcurrentMaltParserModel model = null;
        try {
            URL swemaltMiniModelURL = new File(base + "swemalt-1.7.2.mco").toURI().toURL();
            model = ConcurrentMaltParserService.initializeParserModel(swemaltMiniModelURL);
        } catch (Exception e) {
            e.printStackTrace();
        }
        for(String tokensStr : res) {
            System.out.println("Token from stagger:");
            System.out.println(tokensStr);
            System.out.println("\nToken generated with maltparser");

            // Creates an array of tokens, which contains the Swedish sentence 'Samtidigt får du högsta sparränta plus en skattefri sparpremie.'
            // in the CoNLL data format.
            String[] tokens = tokensStr.split("\n");
            try {
                outputGraph = model.parse(tokens);
            } catch (Exception e) {
               e.printStackTrace();
            }
            System.out.println(outputGraph);
        }
    }
}
