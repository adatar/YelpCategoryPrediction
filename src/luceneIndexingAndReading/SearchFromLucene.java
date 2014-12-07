package luceneIndexingAndReading;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;

import org.apache.lucene.document.Document;
import org.apache.lucene.index.DirectoryReader;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.IndexableField;
import org.apache.lucene.index.MultiFields;
import org.apache.lucene.index.Term;
import org.apache.lucene.index.Terms;
import org.apache.lucene.index.TermsEnum;
import org.apache.lucene.search.BooleanClause.Occur;
import org.apache.lucene.search.BooleanQuery;
import org.apache.lucene.search.Collector;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.TermQuery;
import org.apache.lucene.search.TopDocs;
import org.apache.lucene.store.FSDirectory;
import org.apache.lucene.util.BytesRef;

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
	
    protected HashMap<Integer, Document> getAllDocumentsByTag(String tag)
    {
        HashMap<Integer, Document> documents = new HashMap<Integer, Document>();
        for (int i = 0; i < this.indexReader.maxDoc(); i++) {
            Document doc;
            try {
                doc = this.indexReader.document(i);
                boolean isBusiness = doc.getField("type").stringValue().equalsIgnoreCase(tag);
                
                if(isBusiness)
                    documents.put(i, doc);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return documents;
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

	public HashMap<Integer, Document> getAllBusinessDocuments()
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
		
	//----CAN INTEGRATE START BOOLEAN QUERIES-------
	
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
	   
	//----CAN INTEGRATE END------
	
	protected void finalize()
	{
		try {
			
			indexReader.close();
		
		} catch (IOException e) {
			e.printStackTrace();
		}
	
	}

}
