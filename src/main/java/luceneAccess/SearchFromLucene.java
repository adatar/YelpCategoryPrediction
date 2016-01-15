package main.java.luceneAccess;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import main.java.constants.Constants;
import org.apache.lucene.document.Document;
import org.apache.lucene.index.IndexableField;
import org.apache.lucene.index.Term;
import org.apache.lucene.index.Terms;
import org.apache.lucene.index.TermsEnum;
import org.apache.lucene.search.BooleanClause.Occur;
import org.apache.lucene.search.BooleanQuery;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.TermQuery;
import org.apache.lucene.search.TopDocs;
import org.apache.lucene.util.BytesRef;

public class SearchFromLucene {
	
	private SearchFromLuceneHelper searchHelper;
	
	public SearchFromLucene(){
        searchHelper = new SearchFromLuceneHelper();
	}
	
	public List<String> getCategoriesForDocument(int docId) {

        Document doc = searchHelper.getDocumentById(docId);
        return Arrays.asList(doc.getValues(Constants.BUSINESS_CATEGORIES));
	}

	public ArrayList<String> getAllBusinessIds() {
		return getVocabularyForField(Constants.BUSINESS_ID);
	}
	
	public ArrayList<String> getAllCategories() {
		return getVocabularyForField(Constants.BUSINESS_CATEGORIES);
	}
	
	public ArrayList<String> getAllWordsInReview() {
		return getVocabularyForField(Constants.REVIEW_TEXT);
	}
	
	public int[] getAllBusinessDocuments() {
	    return searchHelper.getAllDocumentsByTag(Constants.BUSINESS);
	}
	
	public ArrayList<String> getReviewTermsForDocument(int docId) {
		return searchHelper.getFieldTermsFromDocument(docId, Constants.REVIEW_TEXT);
	}
	
	public long getReviewTextVocabSizeForDocument(int docId) {
		return searchHelper.getDocumentVocabSize(docId, Constants.REVIEW_TEXT);
	}
	
	public void getTopCategories() {
		ArrayList<String> cat = getAllCategories();
		
		for(String c : cat) {
			System.out.println(c + "\t" + getCategoryCount(c));
		}	
	}
	
	private ArrayList<String> getVocabularyForField(String field) {
		
		ArrayList<String> fieldVocabulary = new ArrayList<>();
		
		try {
		
			TermsEnum iterator = searchHelper.getTermsEnumForField(field);
			BytesRef byteRef;
		
			while ((byteRef = iterator.next()) != null) {
				String term = byteRef.utf8ToString();
				if(term.length() > 0) fieldVocabulary.add(term);
			}
			
		} catch (IOException e) {
			e.printStackTrace();
		}
				
		return fieldVocabulary;		
	}

	
	public int getDocumentCountContainingWord(String word) {
		
		Query query = createFieldQuery(Constants.REVIEW_TEXT, word);
		TopDocs docs = searchHelper.queryIndex(query, 1);
		
		return docs.totalHits;
	}
	
	public int getNumberOfBusinesses() {
		Query query = createFieldQuery(Constants.TYPE, Constants.BUSINESS);
		TopDocs docs = searchHelper.queryIndex(query, 1);
		
		return docs.totalHits;
	}
	
	private Query createFieldQuery(String field, String term) {
            return new TermQuery(new Term(field, term));
	}
	
	public int getWordCountForGivenCategoryAndWord(String word, String category) {
		Query reviewQuery = createFieldQuery(Constants.REVIEW_TEXT, word);
		Query categoryQuery = createFieldQuery(Constants.BUSINESS_CATEGORIES, category);
		
		BooleanQuery booleanQuery = new BooleanQuery();
		booleanQuery.add(reviewQuery, Occur.MUST);
		booleanQuery.add(categoryQuery, Occur.MUST);
		
		TopDocs docs = searchHelper.queryIndex(booleanQuery, 1);
		return docs.totalHits;
		
	}
	
	public int getCategoryCount(String category) {
		Query typeQuery = createFieldQuery(Constants.TYPE, Constants.BUSINESS);
		Query categoryQuery = createFieldQuery(Constants.BUSINESS_CATEGORIES, category);
		
		BooleanQuery booleanQuery = new BooleanQuery();
		booleanQuery.add(typeQuery, Occur.MUST);
		booleanQuery.add(categoryQuery, Occur.MUST);
		
		TopDocs docs = searchHelper.queryIndex(booleanQuery, 1);
		return docs.totalHits;
	}
	
	public String getBusinessName(String businessId) {
		
		Query typeQuery = createFieldQuery(Constants.TYPE, Constants.BUSINESS);
		Query businessQuery = createFieldQuery(Constants.BUSINESS_ID, businessId);
		
		BooleanQuery booleanQuery = new BooleanQuery();
		booleanQuery.add(typeQuery, Occur.MUST);
		booleanQuery.add(businessQuery, Occur.MUST);
		
		TopDocs docs = searchHelper.queryIndex(booleanQuery, 1);
		
		if(docs.scoreDocs.length > 0) {
			Document document = searchHelper.getDocument(docs.scoreDocs[0].doc);
			IndexableField[] fields = document.getFields(Constants.BUSINESS_NAME);
			return fields[0].stringValue();	
		}
		
		else return "";
	}

	public Document getBusinessDocument(String businessId) {
	        
		Query typeQuery = createFieldQuery(Constants.TYPE, Constants.BUSINESS);
	    Query businessQuery = createFieldQuery(Constants.BUSINESS_ID, businessId);
	        
	    BooleanQuery booleanQuery = new BooleanQuery();
	    booleanQuery.add(typeQuery, Occur.MUST);
	    booleanQuery.add(businessQuery, Occur.MUST);
	        
	    TopDocs docs = searchHelper.queryIndex(booleanQuery, 1);
	        
	    if(docs.scoreDocs.length > 0) {
	    	return searchHelper.getDocument(docs.scoreDocs[0].doc);
	    }
	    
	    else return null;
	}

    public long getDocumentCount(){return searchHelper.getTotalDocumentCount();}

    public Terms getDocumentVocab(int docId, String field){return searchHelper.getDocumentVocab(docId, field);}

    public TermsEnum getTermsEnumForField(String field) throws IOException {
        return searchHelper.getTermsEnumForField(field);
    }
}
