package luceneIndexingAndReading;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.DoubleField;
import org.apache.lucene.document.Field;
import org.apache.lucene.document.StringField;
import org.apache.lucene.document.TextField;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.IndexWriterConfig;
import org.apache.lucene.index.IndexWriterConfig.OpenMode;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;
import org.apache.lucene.util.Version;

public class IndexUsingLucene {
	
	
	Directory dir;
	Analyzer analyzer;
	IndexWriterConfig iwc;
	IndexWriter writer;
	
	public IndexUsingLucene(String indexPath) {
		
		analyzer = new StandardAnalyzer();
		iwc = new IndexWriterConfig(Version.LUCENE_4_10_1, analyzer);
		iwc.setOpenMode(OpenMode.CREATE_OR_APPEND);
		
		try {
			
			dir = FSDirectory.open(new File(indexPath));
			writer = new IndexWriter(dir, iwc);
			
		} catch (IOException e) {
			e.printStackTrace();
		}
	
	}
	
	private Document addArrayValuesTODocument(Document luceneDoc, String arrayAsString, String fieldName, boolean store)
	{
		String categoryArray[] = arrayAsString.split(",");
		
		for(String s : categoryArray){
			
			s = s.trim();
			if(s != null)
			{
				if(store)	luceneDoc.add(new StringField(fieldName,s,Field.Store.YES));
				else	luceneDoc.add(new StringField(fieldName,s,Field.Store.NO));
			}
		}
		
		return luceneDoc;
	}
	
	
	private Document addBusinessFieldsToDocument(Document luceneDoc, HashMap<String,String> businessFieldValuePairs)
	{
		String businessName = businessFieldValuePairs.get("businessName");
		String businessId = businessFieldValuePairs.get("businessId");
		String businessCategories = businessFieldValuePairs.get("categories");
		String businessRating = businessFieldValuePairs.get("businessRating");
		
		if(businessName != null) luceneDoc.add(new StringField("businessName",businessName,Field.Store.YES));
		if(businessId != null) luceneDoc.add(new StringField("businessId",businessId,Field.Store.YES));
		if(businessRating != null) luceneDoc.add(new DoubleField("businessRating",Double.parseDouble(businessRating),Field.Store.YES));
		luceneDoc.add(new StringField("type","business",Field.Store.NO));
		
		luceneDoc = addArrayValuesTODocument(luceneDoc, businessCategories,"businessCategories", true);
		
		return luceneDoc;

	}
	
	private Document addReviewFieldsToDocument(Document luceneDoc, HashMap<String,String> reviewFieldValuePairs)
	{
		
		String reviewId = reviewFieldValuePairs.get("reviewId");
		String businessId = reviewFieldValuePairs.get("businessId");
		String userId = reviewFieldValuePairs.get("userId");
		String reviewRating = reviewFieldValuePairs.get("reviewRating");
		String reviewDate = reviewFieldValuePairs.get("date");
		String reviewText = reviewFieldValuePairs.get("text");
		
		if(reviewId != null) luceneDoc.add(new StringField("reviewId",reviewId,Field.Store.YES));
		if(businessId != null) luceneDoc.add(new StringField("businessId",businessId,Field.Store.YES));
		if(userId != null) luceneDoc.add(new StringField("userId",userId,Field.Store.YES));
		if(reviewDate != null) luceneDoc.add(new StringField("reviewDate",reviewDate,Field.Store.YES));
		if(reviewRating != null) luceneDoc.add(new DoubleField("reviewRating",Double.parseDouble(reviewRating),Field.Store.YES));
		if(reviewText != null) luceneDoc.add(new TextField("reviewText",reviewText,Field.Store.NO));
		
		luceneDoc.add(new StringField("type","review",Field.Store.NO));
		
		return luceneDoc;
		
	}
	
	private void addDocumentToIndex(Document luceneDoc)
	{
		try {
			
			writer.addDocument(luceneDoc);
		
		} catch (IOException e) {
			e.printStackTrace();
		}	
	}
	
	public void indexBothFields(HashMap<String,String> businessFieldValuePairs, HashMap<String,String> reviewFieldValuePairs, boolean indexBusiness)
	{
		Document luceneDoc = new Document();
		
		if(indexBusiness)
		{
			luceneDoc = addBusinessFieldsToDocument(luceneDoc, businessFieldValuePairs);
			addDocumentToIndex(luceneDoc);	
		}
		
		luceneDoc = new Document();
		luceneDoc = addReviewFieldsToDocument(luceneDoc, reviewFieldValuePairs);
		luceneDoc = addArrayValuesTODocument(luceneDoc,businessFieldValuePairs.get("categories"),"businessCategories",false);
		
		addDocumentToIndex(luceneDoc);
		
	}
	
	public void indexBusinessFields(HashMap<String, String> fieldValuePairs)
	{
	}
	
	public void indexReviewFields(HashMap<String, String> fieldValuePairs)
	{
	}
	
	
	public void closeLuceneLocks()
	{
		try {

			writer.close();
			dir.close();
		
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
}
