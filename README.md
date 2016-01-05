# SynonymProject
This project contains files for the creation of a Swedish sentiment lexicon (via seed word expansion of synonyms and antonyms) and files for a sentiment classification model for Swedish words and sentences.

The file SynonymAPI.java contains the code to extract synonyms and antonyms (from initial seed words in a txt-file) from the website www.synonymer.se. This way an expanded seed word list is created. 
The expanded seed word lists of negative and positive adjectives+adverbs and verbs are the sentiment lexicon. It is at the basis for word and sentence classifications with the Naive Bayes and an Averaging Method. 
For sentence classification, the word classifications are aggregated per sentence. For a detailed description of the process, read the pdf 'Building a Swedish Sentiment model.

**Word classification**
The 'NaiveBayes.java' and 'AveragingMethod.java' contain code for word classifications based on positive and negative word lists of adjectives+adjverbs and verbs.
The NaiveBayesTest.java runs methods in the above files.


**Sentence classification**

The 'ParsingUtility.java' uses the Stockholm part-of-speech tagger (Stagger) to tag Swedish sentences for sentiment analysis. 
The project here does not include the Swedish tagger model, since it was to big to upload. 
Sentiment analysis only works if you download the Stagger POS-file (swedish.bin)!! You can download that file from here:
http://www.ling.su.se/english/nlp/tools/stagger/stagger-the-stockholm-tagger-1.98986

The 'ParsingUtility.java' also contains code for the Swedish maltparser (http://www.maltparser.org/mco/swedish_parser/swemalt.html), but that code is not used in the sentiment analysis program.
If you anyways want to use the maltparser, its model is called 'swemalt-1.7.2.mco'.

The 'SentenceClassifierNB.java' and 'SentenceClassifierAM.java' use the files 'NaiveBayes.java', 'AveragingMethod.java' and 'ParsingUtility.java' in order to do sentence classification. 
The sentence classification includes a negation handler that flags negations and reverses sentiment of adjectives+adverbs and verbs within a default window of 5 words after negation (window can easily be changed).
The Swedish sentiment model also matches all Swedish idiomatic expressions with verbs  (ex: 'vara ute och cyklar', meaning 'being confused' or 'having misunderstood') in the verb lexicon with those that may be in the sentences to be classified. If the program finds matching expressions from the lexicon in the sentences, it replaces those expressions in the sentence with one word of the same sentiment in the lexicon in order to get the total sentence classification right.

The sentences to be classified should be put in 'testmeningar.txt'. You need to have a '.' after each sentence.

The 'SeedExpansionComparer.java' just compares classifications of different files and may be used for evaluation.
