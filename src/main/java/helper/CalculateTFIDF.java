package main.java.helper;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.PriorityQueue;

import org.apache.lucene.index.DocsEnum;
import org.apache.lucene.index.TermsEnum;
import org.apache.lucene.search.DocIdSetIterator;
import org.apache.lucene.util.BytesRef;

import main.java.constants.Constants;
import main.java.luceneAccess.SearchFromLucene;

public class CalculateTFIDF {
	
	PriorityQueue<TFIDFComparator> tfIdfQueue;
	HashMap<String, Double> TFIDFScoreMap;
	SearchFromLucene searchFromLucene;
	ArrayList<String> reviewVocabulary;
	
	public CalculateTFIDF(){
		
		TFIDFScoreMap = new HashMap<>();
		searchFromLucene = new SearchFromLucene();
		tfIdfQueue = new PriorityQueue<>();
		
		reviewVocabulary = searchFromLucene.getAllWordsInReview();
		
	}
	
	public PriorityQueue<TFIDFComparator> calculateTFIDFScore(){
		
		HashMap<String, Double> IDFScoreMap = calculateIDFScores();
		HashMap<String, HashMap<Integer, Double>> TFScoreMap = calculateTFScores();
		
		
		for(String word : reviewVocabulary){
			
			double idfScore = IDFScoreMap.get(word);
			double tfScore = 0;
			
			HashMap<Integer, Double> doctfMap = TFScoreMap.get(word);
			
			for(int docId : doctfMap.keySet()){
				
				tfScore += (doctfMap.get(docId) * idfScore);
				
			}
			
			System.out.println(word + "\t" + tfScore);
			
			TFIDFScoreMap.put(word, tfScore);
			tfIdfQueue.add(new TFIDFComparator(word, tfScore));
			
		}
		
		return tfIdfQueue;
	}
	
	private HashMap<String, HashMap<Integer, Double>> calculateTFScores(){
			
		HashMap<String, HashMap<Integer, Double>> docTFMap = new HashMap<>();
		
		try {
			
			TermsEnum termsIiterator = searchFromLucene.getTermsEnumForField(Constants.REVIEW_TEXT); 
			BytesRef byteRef = null; 
		
			while ((byteRef = termsIiterator.next()) != null) 
			{ 
				String term = byteRef.utf8ToString();
				
				DocsEnum docsEnum = termsIiterator.docs(null, null);  	
				
				@SuppressWarnings("unused")
				int docIdEnum;
				
				HashMap<Integer, Double> docFreqMap = new HashMap<>();
				
		        while ((docIdEnum = docsEnum.nextDoc()) != DocIdSetIterator.NO_MORE_DOCS) 
		        {	
		        	int freq = docsEnum.freq();
		        	long vocabSize = searchFromLucene.getReviewTextVocabSizeForDocument(docsEnum.docID());
		        	
		        	docFreqMap.put(docsEnum.docID(), freq/(vocabSize * 1.0));
		        }
		        
		        docTFMap.put(term, docFreqMap);
			
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		return docTFMap;
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
	
	public HashMap<String, Double> getTFIDFScoreMap() {
		return TFIDFScoreMap;
	}

}
