/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package se.milu.maltparser;

import java.io.IOException;
import java.net.MalformedURLException;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

/**
 *
 * @author jolin1337
 */
class SynonymAPI {
    private static final String SYNONYMS = "synonym";
    private static final String SYNONYMS_NEG = "negated_words";

    private String word = "bra";
    private Document html_dom_parser_data;
    private List<String> found_synomyms = new ArrayList<>();
    private Map<String, String> parameters = new HashMap<>();

    public String URL_TO_DATA = "http://www.synonymer.se/?query=";

    /**
     * Skickar en query till servern och hämtar ut resultatet utifrån angivna
     * parametrar.
     */
    public Map<String, List<String>> query(String data) {
        return query(data, true);
    }

    public Map<String, List<String>> query(String params, boolean output) {
        try {
            parameters = get_parameters(params);

            if (!parameters.containsKey("word")) {
                error("Du måste ange ett ord");
            }
            word = encode(parameters.get("word"));
            get_data();
            Map<String, List<String>> data = extract_data();
            found_synomyms = data.get(SYNONYMS);//extract_syn_data();
            // found_conta_synomyms = data.get("negated_words"); // extract_neg_syn_data();

            if (parameters.get("sort").equals("true")) {
                sort(parameters.get("sort"));
            }

            if (parameters.get("text_transform").equals("true")) {
                transform(parameters.get("text_transform"));
            }

            if (output) {
                success();
            }

            return data;
        } catch (IOException ex) {
            return null;
        }
    }

    /**
     * Hämtar parametrar ur medskickad data. Anger default-värden om en viss
     * parameter inte skickats med.
     */
    private static Map<String, String> get_parameters(String data) {
        String[] dataParams = data.split("&");
        Map<String, String> params = new HashMap<>();
        params.put("language", "swe");
        params.put("include_negated_words", "false");
        params.put("sort", "false");
        params.put("text_transform", "normal");
        for (String dataParam : dataParams) {
            String[] keyVal = dataParam.split("=");
            String key = keyVal[0];
            String val = keyVal[1];
            switch (key) {
                case "word":
                    params.put(key, val);
                    break;
                case "language":
                    if (!val.equals("swe") && !val.equals("eng")) {
                        params.put(key, "swe");
                    } else {
                        params.put(key, val);
                    }
                    break;
                case "include_negated_words":
                    if (val.equals("true") || val.equals("1")) {
                        params.put(key, "true");
                    } else {
                        params.put(key, "false");
                    }
                    break;
                case "sort":
                    if (!val.equals("desc") && !val.equals("asc")) {
                        params.put(key, "asc");
                    } else {
                        params.put(key, val);
                    }
                    break;
                case "text_transform":
                    if (val.equals("uppercase") || val.equals("lowercase")) {
                        params.put(key, val);
                    }
                    break;
                case "format":
                    if (val.equals("json") || val.equals("xml")) {
                        params.put(key, val);
                    } else {
                        params.put(key, "json");
                    }
                    break;
            }
        }

        return params;
    }

    /**
     * Sorterar på alfabetisk ordning, stigande eller fallande.
     */
    private void sort(String order) {
        switch (order) {
            case "asc":
                Collections.sort(found_synomyms);
                break;
            case "desc":
                Collections.sort(found_synomyms, Collections.reverseOrder());
                break;
        }
    }

    /**
     * Gör om alla ord till stora/små bokstäver
     */
    private void transform(String type) {

        for (String syn : found_synomyms) {
            switch (type) {
                case "lowercase":
                    syn = syn.toLowerCase();
                    break;
                case "uppercase":
                    syn = syn.toUpperCase();
                    break;
                default:
                    syn = syn.substring(0, 1).toUpperCase() + syn.substring(1).toLowerCase();
                    break;
            }
        }
    }

    /**
     * Gör om å ä ö till det format som www.synonymer.se kan tolka
     */
    private static String encode(String str) {
        str = str.replaceAll("[åÅ]", "%E5");
        str = str.replaceAll("[äÄ]", "%E4");
        str = str.replaceAll("[öÖ]", "%F6");
        return str;
    }

    /**
     * Hämtar datat från www.synonymer.se.
     */
    private void get_data() throws IOException {
        // Sätter språket. Svenska anges genom att inte skicka med variabeln alls.
        String language = parameters.get("language").equals("swe") ? "" : "&lang=engsyn";
        // echo (file_get_contents(self::URL_TO_DATA.self::$word.$language));
        html_dom_parser_data = Jsoup.connect(URL_TO_DATA + word + language).get();
    }

    /**
     * Hämtar ut alla ord som hittats, dock bara ord som INTE HAR skickats in av
     * användare av tjänsten.
     */
    private Map<String, List<String>> extract_data() {
        Elements standard_data_row = html_dom_parser_data.select("#middlebanner > div.boxContent");	// Hämtar datat

        List<String> negated = new ArrayList<>();
        List<String> syns = new ArrayList<>();
        if (standard_data_row != null && !standard_data_row.isEmpty()) {
            String typeOfWord = "adj.";
            Element el = standard_data_row.first().child(0);
            while ((el = el.nextElementSibling()) != null) {
                // If this element has a classname named mini_header
                if (el.hasClass("mini_header")
                        || (el.tagName().equals("font")
                        && !el.select("b.width").isEmpty()/* && $el->find('div.index_mar') != null*/)) {
                    typeOfWord = el.text();
                }

                // If this element is a anchor tag and has href="query*"
                if (el.hasAttr("href") && el.attr("href").contains("query")) {
                    if (typeOfWord.contains("adj.")) {
                        syns.add(el.text());
                    } else if (typeOfWord.contains("adv.")) {
                        syns.add(el.text());
                    } else if (typeOfWord.contains("motsatsord")) {
                        negated.add(el.text());
                    } else if (typeOfWord.contains("verb")) {
                        syns.add(el.text());
                    }
                }
            }
        }
        Map<String, List<String>> res = new HashMap<>();
        res.put(SYNONYMS_NEG, negated);
        res.put(SYNONYMS, syns);
        return res;
    }

    /**
     * Hämtar ut alla ord som hittats, dock bara ord som HAR skickats in av
     * användare av tjänsten.
     */
//    private static function extract_context_data() {
//        $standard_data_row = self::$html_dom_parser_data
//        ->find('#middlebanner > div.boxContent'
//        , 1);	// Hämtar datat
//                $words = array();
//
//        if (!is_null($standard_data_row)) {
//        }
//        return $words;
//    }
    /**
     * Skriver ut ett lyckat resultatet i angivet format.
     */
    private void success() {
        switch (parameters.get("format")) {
            case "json":
                //System.out.println("{}");
                // SynonymsOutputJSON::success(self::$found_synomyms, self::$parameters);	
                break;
            case "xml":
                System.out.println("<xml></xml>");
                // SynonymsOutputXML::success(self::$found_synomyms, self::$parameters);		
                break;
        }
    }

    /**
     * Skriver ut fel i angivet format. Avbryter efter anrop.
     */
    private void error(String error) {
        switch (parameters.get("format")) {
            case "json":
                System.err.println("{error: \"" + error + "\"}");
                // SynonymsOutputJSON::error($error);	
                break;
            case "xml":
                System.out.println("<xml><error>" + error +  "</error></xml>");
                break;
        }
        //SynonymsOutputXML::error($error);
        System.exit(1);
    }
    
    
    public static void main(String[] args) throws MalformedURLException, IOException {

        List<String> words = Files.readAllLines(Paths.get(/*args[0]*/ "C:/Users/Michelle/IdeaProjects/SynonymProject/negAdjAdv.txt"), Charset.forName("utf-8"));
        SynonymAPI synAPI = new SynonymAPI();
        int synsCount = 0;
        final List<String> syns = new ArrayList<>();
        List<String> synsNeg = new ArrayList<>();

        for(String word : words) {
            Map<String, List<String>> synsWord = synAPI.query("word=" + word + "&format=json", false);
            System.out.println("Collecting synonyms of " + word);
            syns.add(word);
            syns.addAll(synsWord.get(SYNONYMS));
            synsNeg.addAll(synsWord.get(SYNONYMS_NEG));
//            for(String syn : synsWord) {
//                System.out.println(syn);
//            }
//            System.out.println();
        }
        Files.write(Paths.get(/*args[1] + ".txt"*/"C:/Users/Michelle/IdeaProjects/SynonymProject/ThreeIter/ThreeIterNegAdjAdv.txt"), syns, Charset.forName("utf-8"));
        Files.write(Paths.get(/*args[1] + "neg.txt"*/"C:/Users/Michelle/IdeaProjects/SynonymProject/ThreeIter/ThreeIterNegAdjAdvneg.txt"), synsNeg, Charset.forName("utf-8"), StandardOpenOption.APPEND);
        System.out.println("Antal synonymer:" + /*synsCount*/ syns.size());
    }
}
