package main.java.luceneAccess;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.PriorityQueue;
import main.java.constants.Constants;
import org.apache.lucene.document.Document;
import org.apache.lucene.index.DocsEnum;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.IndexableField;
import org.apache.lucene.index.MultiFields;
import org.apache.lucene.index.Term;
import org.apache.lucene.index.Terms;
import org.apache.lucene.index.TermsEnum;
import org.apache.lucene.search.BooleanClause.Occur;
import org.apache.lucene.search.BooleanQuery;
import org.apache.lucene.search.DocIdSetIterator;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.TermQuery;
import org.apache.lucene.search.TopDocs;
import org.apache.lucene.util.BytesRef;
import test.unitTesting.DS;

import com.google.common.primitives.Ints;

public class SearchFromLucene {
	
	private IndexReader indexReader;
	private IndexSearcher indexSearcher;
	
	public SearchFromLucene(String indexPath) 
	{
		indexReader = GetIndexReader.getIndexReader();
		indexSearcher = new IndexSearcher(indexReader);
	}
	
	public SearchFromLucene() 
	{
		indexReader = GetIndexReader.getIndexReader();
		indexSearcher = new IndexSearcher(indexReader);			
	}

	//------------------------START PART - GET VOCABULARY-------------------------------------
	
	public TermsEnum getTermsEnumForField(String field){
			
		try {
			Terms vocabulary = MultiFields.getTerms(indexReader, field);
			
			TermsEnum termsIterator = vocabulary.iterator(null); 
			
			return termsIterator;
		
		} catch (IOException e) {
			e.printStackTrace();
		} 
		
		return null;
		
	}
	
	private ArrayList<String> getVocabularyForField(String field)
	{
		
		ArrayList<String> fieldVocabulary = new ArrayList<String>();
		
		try {
			Terms vocabulary = MultiFields.getTerms(indexReader, field);
			
			TermsEnum iterator = vocabulary.iterator(null); 
			BytesRef byteRef = null; 
		
			while ((byteRef = iterator.next()) != null) 
			{ 
				
					String term = byteRef.utf8ToString();
					if(term != null && term.length() > 0) fieldVocabulary.add(term);
			}
		
		} catch (IOException e) {
			e.printStackTrace();
		} 
		
		return fieldVocabulary;		
	}
	
		
    private int[] getAllDocumentsByTag(String tag)
    {
        ArrayList<Integer> documents = new ArrayList<Integer>();
        
        for (int i = 0; i < this.indexReader.maxDoc(); i++) {
            Document doc;
            try {
                doc = this.indexReader.document(i);
                
                boolean isBusiness = doc.getField(Constants.TYPE).stringValue().equalsIgnoreCase(tag);
                
                if(isBusiness)
                    documents.add(i);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return Ints.toArray(documents);
    }

    public Document getDocumentById(int docId)
    {
        Document doc = null;
        try {
            doc = this.indexReader.document(docId);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return doc;
    }
    
	public ArrayList<String> getAllBusinessIds()
	{
		return getVocabularyForField(Constants.BUSINESS_ID);
	}
	
	public ArrayList<String> getAllCategories()
	{
		return getVocabularyForField(Constants.BUSINESS_CATEGORIES);
	}
	
	public ArrayList<String> getAllWordsInReview()
	{
		return getVocabularyForField(Constants.REVIEW_TEXT);
	}

	public int[] getAllBusinessDocuments()
	{
	    return this.getAllDocumentsByTag(Constants.BUSINESS);
	}
	
	private ArrayList<String> getFieldTermsFromDocument(int docId, String field)
	{
        ArrayList<String> words = new ArrayList<String>();
	    try
	    {
	        Terms terms = this.indexReader.getTermVector(docId, field);
	        if (terms != null && terms.size() > 0) {
	            
	            TermsEnum termsEnum = terms.iterator(null); // access the terms for this field
	            BytesRef term = null;

	            while ((term = termsEnum.next()) != null) { // explore the terms for this field
	                words.add(term.utf8ToString());
	            }
	        }
	        
	    }
	    catch(IOException e)
	    {
	        e.printStackTrace();
	    }
	    return words;
	}
	
	private long getDocumentVocabSize(int docId, String field)
	{
		long size = 0;
		
	    try
	    {
	        Terms terms = this.indexReader.getTermVector(docId, field);
	        size = terms.size();
	        
	    }
	    catch(IOException e)
	    {
	        e.printStackTrace();
	    }
	    return size;
	}
	
	
	public PriorityQueue<DS> getVocabWithFreqForReview()
	{
		
		HashMap<String, Integer> termWordCount = new HashMap<>();
		
		HashMap<String, HashMap<Integer,Integer>> termDocWordCount = new HashMap<>();
		
		int totalNoOfDocs = getTotalDocumentCount();
		
		HashMap<String, Double> IDFScores = new HashMap<>();
		
		
		for (int i = 0; i < this.indexReader.maxDoc(); i++) 
		{
			try {
				
				Terms terms = indexReader.getTermVector(i, Constants.REVIEW_TEXT);

				if (terms != null && terms.size() > 0) {
					
					TermsEnum termsEnum = terms.iterator(null);
				    BytesRef term = null;
				    
				    while ((term = termsEnum.next()) != null) 
				    {
				    	DocsEnum docsEnum = termsEnum.docs(null, null);
				    	
				    	HashMap<Integer,Integer> tempDocFreqMap = new HashMap<>();
				    	
						int docIdEnum;
				        while ((docIdEnum = docsEnum.nextDoc()) != DocIdSetIterator.NO_MORE_DOCS) 
				        {
				          
				          if(!termWordCount.containsKey(term.utf8ToString()))
				        	  termWordCount.put(term.utf8ToString(), docsEnum.freq());
				          else
				        	  termWordCount.put(term.utf8ToString(), termWordCount.get(term.utf8ToString()) + docsEnum.freq());

				          tempDocFreqMap.put(docIdEnum, docsEnum.freq());
				          
				        
				        }
				        termDocWordCount.put(term.utf8ToString(), tempDocFreqMap);
				        
				        if (!IDFScores.containsKey(term.utf8ToString()))
				            IDFScores.put(term.utf8ToString(), (double)totalNoOfDocs / termsEnum.docFreq());
				    }

				}
				
			
			} catch (IOException e) {
             e.printStackTrace();
			}
				
		}
		
		HashMap<String, Double> TFIDFScores = new HashMap<>();
		
        for(String term : termDocWordCount.keySet())
        {
            double docLength = 0;
            double TFIDFScore = 0;
            
            for(Integer docID : termDocWordCount.get(term).keySet())
            {
                for (String docTerm : termDocWordCount.keySet())
                {
                    if (termDocWordCount.get(docTerm).containsKey(docID))
                        docLength += termDocWordCount.get(docTerm).get(docID);
                }
                
                int termFreq = termDocWordCount.get(term).get(docID);
                double TFScore = termFreq/docLength;
                TFIDFScore += TFScore;
            }
            int numOfDocs = termDocWordCount.get(term).size();
            double avgTFScore = TFIDFScore / numOfDocs;
            
            TFIDFScores.put(term, avgTFScore * IDFScores.get(term));
        }
		
		
		PriorityQueue<DS> fpq = new PriorityQueue<>();
		
		for(String word : TFIDFScores.keySet())
		{
			double score = TFIDFScores.get(word);
			DS tds = new DS(word,score);
			fpq.add(tds);
		}
		
		return fpq;	
	}
	
	public void getTopCategories()
	{
		ArrayList<String> cat = getAllCategories();
		
		for(String c : cat)
		{
			System.out.println(c + "\t" + getCategoryCount(c));
		}	
	}
	
	//------------------------START PART - QUERYING---------------------------------
	
	private Query createFieldQuery(String field, String term)
	{
			Query query = new TermQuery(new Term(field, term));			
			return query;
	}
	
	private TopDocs searchIndex(Query query, int numTopHits)
	{
		TopDocs docs = null;
		
		try {
			docs = indexSearcher.search(query, numTopHits);
		} catch (IOException e) {e.printStackTrace();}
		
		return docs;
		
	}
	
	public int getTotalDocumentCount()
	{
	    return this.indexReader.maxDoc();
	}
	
	public int getDocumentCountContainingWord(String word)
	{
		
		Query query = createFieldQuery(Constants.REVIEW_TEXT, word);
		TopDocs docs = searchIndex(query, 1);
		
		return docs.totalHits;
	}
	
	public int getNumberOfBusinesses()
	{
		Query query = createFieldQuery(Constants.TYPE, Constants.BUSINESS);
		TopDocs docs = searchIndex(query, 1);
		
		return docs.totalHits;
	}
		
	//TODO: ----CAN INTEGRATE BELOW METHODS -- START BOOLEAN QUERIES-------
	
	private Document getDocument(int docId)
	{
		Document document = null;
		try {
			document = indexReader.document(docId);
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		return document;
	}
	
	//TODO: Can use single query with filter here TopDocs search(Query query, Filter filter, int n)
	public int getWordCountForGivenCategoryAndWord(String word, String category)
	{
		Query reviewQuery = createFieldQuery(Constants.REVIEW_TEXT, word);
		Query categoryQuery = createFieldQuery(Constants.BUSINESS_CATEGORIES, category);
		
		BooleanQuery booleanQuery = new BooleanQuery();
		booleanQuery.add(reviewQuery, Occur.MUST);
		booleanQuery.add(categoryQuery, Occur.MUST);
		
		TopDocs docs = searchIndex(booleanQuery, 1);
		
		return docs.totalHits;
		
	}
	
	//TODO: Can use single query with filter here TopDocs search(Query query, Filter filter, int n)
	public int getCategoryCount(String category)
	{	
		Query typeQuery = createFieldQuery(Constants.TYPE, Constants.BUSINESS);
		Query categoryQuery = createFieldQuery(Constants.BUSINESS_CATEGORIES, category);
		
		BooleanQuery booleanQuery = new BooleanQuery();
		booleanQuery.add(typeQuery, Occur.MUST);
		booleanQuery.add(categoryQuery, Occur.MUST);
		
		TopDocs docs = searchIndex(booleanQuery, 1);
		
		return docs.totalHits;
	}
	
	//TODO: Can use single query with filter here TopDocs search(Query query, Filter filter, int n)
	public String getBusinessName(String businessId)
	{
		
		Query typeQuery = createFieldQuery(Constants.TYPE, Constants.BUSINESS);
		Query businessQuery = createFieldQuery(Constants.BUSINESS_ID, businessId);
		
		BooleanQuery booleanQuery = new BooleanQuery();
		booleanQuery.add(typeQuery, Occur.MUST);
		booleanQuery.add(businessQuery, Occur.MUST);
		
		TopDocs docs = searchIndex(booleanQuery, 1);
		
		if(docs.scoreDocs.length > 0)
		{
			Document document = getDocument(docs.scoreDocs[0].doc);
			IndexableField[] fields = document.getFields(Constants.BUSINESS_NAME);
			return fields[0].stringValue();	
		}
		
		else return "";
		
	}

	   public Document getBusinessDocument(String businessId)
	    {
	        
	        Query typeQuery = createFieldQuery(Constants.TYPE, Constants.BUSINESS);
	        Query businessQuery = createFieldQuery(Constants.BUSINESS_ID, businessId);
	        
	        BooleanQuery booleanQuery = new BooleanQuery();
	        booleanQuery.add(typeQuery, Occur.MUST);
	        booleanQuery.add(businessQuery, Occur.MUST);
	        
	        TopDocs docs = searchIndex(booleanQuery, 1);
	        
	        if(docs.scoreDocs.length > 0)
	        {
	            return getDocument(docs.scoreDocs[0].doc);
	        }
	        else return null;
	        
	    }

	   public ArrayList<String> getReviewTermsForDocument(int docId)
	   {
	       return this.getFieldTermsFromDocument(docId, Constants.REVIEW_TEXT);
	   }
	   
	   public long getReviewTextVocabSizeForDocument(int docId)
	   {
	       return this.getDocumentVocabSize(docId, Constants.REVIEW_TEXT);
	   }
	   
	   public ArrayList<String> getCategoriesForDocument(int docId)
	   {
		   ArrayList<String> categoryList = new ArrayList<>();
		   
		   Document doc = this.getDocumentById(docId);
		   for(String s: doc.getValues(Constants.BUSINESS_CATEGORIES))
		   {
			   categoryList.add(s);
		   }
		   
		   return categoryList;
	    
	   }
	   
	//----CAN INTEGRATE END------
	
	@Override
    protected void finalize()
	{
		try {
			
			indexReader.close();
		
		} catch (IOException e) {
			e.printStackTrace();
		}
	
	}

}
