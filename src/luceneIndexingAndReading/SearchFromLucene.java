package luceneIndexingAndReading;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.PriorityQueue;

import org.apache.lucene.document.Document;
import org.apache.lucene.index.DirectoryReader;
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
import org.apache.lucene.store.FSDirectory;
import org.apache.lucene.util.BytesRef;

import unitTesting.DS;

import com.google.common.primitives.Ints;

public class SearchFromLucene {
	
	private IndexReader indexReader;
	private IndexSearcher indexSearcher;
	
	public SearchFromLucene(String indexPath) 
	{
		try {
			
			indexReader = DirectoryReader.open(FSDirectory.open(new File(indexPath)));
			indexSearcher = new IndexSearcher(indexReader);
			
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	//------------------------START PART - GET VOCABULARY-------------------------------------
	
	private ArrayList<String> getVocabularyForField(String field)
	{
		
		ArrayList<String> fieldVocabulary = new ArrayList<String>();
		
		try {
			Terms vocabulary = MultiFields.getTerms(indexReader,field);
			
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
	
		
    protected int[] getAllDocumentsByTag(String tag)
    {
        ArrayList<Integer> documents = new ArrayList<Integer>();
        
        for (int i = 0; i < this.indexReader.maxDoc(); i++) {
            Document doc;
            try {
                doc = this.indexReader.document(i);
                
                boolean isBusiness = doc.getField("type").stringValue().equalsIgnoreCase(tag);
                
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
		return getVocabularyForField("businessId");
	}
	
	public ArrayList<String> getAllCategories()
	{
		return getVocabularyForField("businessCategories");
	}
	
	public ArrayList<String> getAllWordsInReview()
	{
		return getVocabularyForField("reviewText");
	}

	public int[] getAllBusinessDocuments()
	{
	    return this.getAllDocumentsByTag("business");
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
	
	
	public PriorityQueue<DS> getVocabWithFreqForReview()
	{
		
		//HM<Term,Freq>>
		HashMap<String, Integer> termWordCount = new HashMap<>();
		
		//For TF
		//TF(t) = (Number of times term t appears in a document) / (Total number of terms in the document).
		
		//HM<Term, HM<DocNO, Freq>>
		HashMap<String, HashMap<Integer,Integer>> documentWordCOunt = new HashMap<>();
		
		//HM<DocNO, Length>>
		HashMap<String, HashMap<Integer,Integer>> documentLength = new HashMap<>();
		
		
		//FOR IDF
		//IDF(t) = log_e(Total number of documents / Number of documents with term t in it). 
		
		int totalNoOfDocs = getTotalDocumentCount();
		
		//HM<Term,#Docs>
		HashMap<String, Integer> termContainingDoumentCount = new HashMap<>();
		
		//TF-IDF = TF * IDF
		
		

		for (int i = 0; i < this.indexReader.maxDoc(); i++) 
		{
			try {
				
				Terms terms = indexReader.getTermVector(i, "reviewText");
				
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
				        documentWordCOunt.put(term.utf8ToString(), tempDocFreqMap);
				
				    }

				}
			
			} catch (IOException e) {
             e.printStackTrace();
			}
				
		}
		
		//Get doc length
		
		PriorityQueue<DS> fpq = new PriorityQueue<>();
		
		for(String word : termWordCount.keySet())
		{
			int freq = termWordCount.get(word);
			DS tds = new DS(word,freq);
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
		
		Query query = createFieldQuery("reviewText", word);
		TopDocs docs = searchIndex(query, 1);
		
		return docs.totalHits;
	}
	
	public int getNumberOfBusinesses()
	{
		Query query = createFieldQuery("type", "business");
		TopDocs docs = searchIndex(query, 1);
		
		return docs.totalHits;
	}
		
	//----CAN INTEGRATE BELOW METHODS -- START BOOLEAN QUERIES-------
	
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
		
	public int getWordCountForGivenCategoryAndWord(String word, String category)
	{
		Query reviewQuery = createFieldQuery("reviewText", word);
		Query categoryQuery = createFieldQuery("businessCategories", category);
		
		BooleanQuery booleanQuery = new BooleanQuery();
		booleanQuery.add(reviewQuery, Occur.MUST);
		booleanQuery.add(categoryQuery, Occur.MUST);
		
		TopDocs docs = searchIndex(booleanQuery, 1);
		
		return docs.totalHits;
		
	}
	
	public int getCategoryCount(String category)
	{	
		Query typeQuery = createFieldQuery("type", "business");
		Query categoryQuery = createFieldQuery("businessCategories", category);
		
		BooleanQuery booleanQuery = new BooleanQuery();
		booleanQuery.add(typeQuery, Occur.MUST);
		booleanQuery.add(categoryQuery, Occur.MUST);
		
		TopDocs docs = searchIndex(booleanQuery, 1);
		
		return docs.totalHits;
	}
	

	public String getBusinessName(String businessId)
	{
		
		Query typeQuery = createFieldQuery("type", "business");
		Query businessQuery = createFieldQuery("businessId", businessId);
		
		BooleanQuery booleanQuery = new BooleanQuery();
		booleanQuery.add(typeQuery, Occur.MUST);
		booleanQuery.add(businessQuery, Occur.MUST);
		
		TopDocs docs = searchIndex(booleanQuery, 1);
		
		if(docs.scoreDocs.length > 0)
		{
			Document document = getDocument(docs.scoreDocs[0].doc);
			IndexableField[] fields = document.getFields("businessName");
			return fields[0].stringValue();	
		}
		
		else return "";
		
	}

	   public Document getBusinessDocument(String businessId)
	    {
	        
	        Query typeQuery = createFieldQuery("type", "business");
	        Query businessQuery = createFieldQuery("businessId", businessId);
	        
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
	       return this.getFieldTermsFromDocument(docId, "reviewText");
	   }
	   
	   public ArrayList<String> getCategoriesForDocument(int docId)
	   {
		   //return this.getFieldTermsFromDocument(docId, "businessCategories");
		   
		   ArrayList<String> categoryList = new ArrayList<>();
		   
		   Document doc = this.getDocumentById(docId);
		   for(String s: doc.getValues("businessCategories"))
		   {
			   categoryList.add(s);
			   System.out.print(s + " ");
		   }
		   System.out.println();
		   
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
