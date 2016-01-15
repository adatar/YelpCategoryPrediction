package main.java.helper;

import main.java.constants.Constants;
import main.java.luceneAccess.GetIndexReader;
import main.java.luceneAccess.SearchFromLucene;
import org.apache.lucene.analysis.payloads.IntegerEncoder;
import org.apache.lucene.index.DocsEnum;
import org.apache.lucene.index.Term;
import org.apache.lucene.index.Terms;
import org.apache.lucene.index.TermsEnum;
import org.apache.lucene.search.DocIdSetIterator;
import org.apache.lucene.util.BytesRef;
import test.unitTesting.DS;

import java.io.IOException;
import java.util.*;

/**
 * Created by adatar on 10/21/2015.
 */
public class CalculateTFIDFNew {

    SearchFromLucene searchFromLucene;
    ArrayList<String> reviewVocabulary;

    CalculateTFIDFNew(){
        searchFromLucene = new SearchFromLucene();
        reviewVocabulary = searchFromLucene.getAllWordsInReview();
        calculateTFIDF();
    }


    private void calculateTFIDF(){

        HashMap<String, Double> IDFScoreMap = calculateIDFScores();
        ArrayList<Integer> documents = new ArrayList<>();

        HashSet<String> highTfIdfTerms = new HashSet<>();

        for(Integer doc : documents) {
            PriorityQueue<TFIDFComparator> tfIdfQueue = new PriorityQueue<>();
            Terms terms = searchFromLucene.getDocumentVocab(doc, Constants.TEXT);
            long numberOfTerms = extractWordCount(terms);

            TermsEnum termsEnum = null;
            try {
                termsEnum = terms.iterator(termsEnum);
            } catch (IOException e) {
                e.printStackTrace();
            }

            BytesRef term;

            try {
                while((term = termsEnum.next()) != null) {
                    long termFreq = termsEnum.totalTermFreq();
                    double tf = termFreq / numberOfTerms;
                    double idf = IDFScoreMap.get(term.utf8ToString());
                    double tfIdf = tf * idf;
                    tfIdfQueue.add(new TFIDFComparator(term.utf8ToString(), tfIdf));
                }
            } catch (IOException e) {
                    e.printStackTrace();
            }

            getTopXPercent(tfIdfQueue,highTfIdfTerms);

        }
    }

    private long extractWordCount(Terms terms){

        long numberOfTerms = 0;
        try {
            numberOfTerms = terms.getSumTotalTermFreq();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return numberOfTerms;
    }

    private void getTopXPercent(PriorityQueue<TFIDFComparator> tfIdfQueue, HashSet<String> highTfIdfTerms){
        //get q size
        //get x percent value
        //iterate and add to HS
    }


    private HashMap<String, Double> calculateIDFScores(){

        HashMap<String, Double> IDFScoreMap = new HashMap<>();
        long totalDocuments = searchFromLucene.getDocumentCount();

        for(String word : reviewVocabulary){
            double termDocCount = searchFromLucene.getDocumentCountContainingWord(word) * 1.0;
            double idfScore = Math.log(totalDocuments/termDocCount);

            IDFScoreMap.put(word, idfScore);
        }

        return IDFScoreMap;
    }

}