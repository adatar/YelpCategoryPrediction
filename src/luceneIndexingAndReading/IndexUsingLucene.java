package luceneIndexingAndReading;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.DoubleField;
import org.apache.lucene.document.Field;
import org.apache.lucene.document.FieldType;
import org.apache.lucene.document.StringField;
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
	String indexPath;
	
	public IndexUsingLucene(String indexPath) {
		
	    this.indexPath = indexPath;
	    this.initializeWriter();
	}
	
	private void initializeWriter()
	{
        analyzer = new StandardAnalyzer();
        iwc = new IndexWriterConfig(Version.LUCENE_4_10_1, analyzer);
        iwc.setOpenMode(OpenMode.CREATE_OR_APPEND);
        try {
            dir = FSDirectory.open(new File(this.indexPath));
            writer = new IndexWriter(dir, iwc);
            
        } catch (IOException e) {
            e.printStackTrace();
        }
	}
	
	protected Document addArrayValuesTODocument(Document luceneDoc, String arrayAsString, String fieldName, boolean store)
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
	
	
	protected Document addBusinessFieldsToDocument(Document luceneDoc, HashMap<String,String> businessFieldValuePairs)
	{
		String businessName = businessFieldValuePairs.get("businessName");
		String businessId = businessFieldValuePairs.get("businessId");
		String businessCategories = businessFieldValuePairs.get("categories");
		String businessRating = businessFieldValuePairs.get("businessRating");
		String reviewText = businessFieldValuePairs.get("reviewText");
		
		if(businessName != null) luceneDoc.add(new StringField("businessName",businessName,Field.Store.YES));
		if(businessId != null) luceneDoc.add(new StringField("businessId",businessId,Field.Store.YES));
		
		if(businessRating != null) luceneDoc.add(new DoubleField("businessRating",Double.parseDouble(businessRating),Field.Store.YES));
		FieldType reviewFieldType = new FieldType();
		reviewFieldType.setIndexed(true);
		reviewFieldType.setTokenized(true);
		reviewFieldType.setStored(true);
		reviewFieldType.setStoreTermVectors(true);
		Field reviewField = new Field("reviewText", reviewText, reviewFieldType);
		
		if(reviewText != null) luceneDoc.add(reviewField);
		luceneDoc.add(new StringField("type","business",Field.Store.YES));
		
		luceneDoc = addArrayValuesTODocument(luceneDoc, businessCategories,"businessCategories", true);
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
	
	public void indexBusiness(HashMap<String,String> businessFieldValuePairs)
	{
		Document luceneDoc = new Document();
		
		luceneDoc = addBusinessFieldsToDocument(luceneDoc, businessFieldValuePairs);
		
		addDocumentToIndex(luceneDoc);
		
	}

    public void indexPrediction(HashMap<String,String> predictionFieldValuePairs)
    {
        Document luceneDoc = new Document();
        
        luceneDoc = addPredictionFieldsToDocument(luceneDoc, predictionFieldValuePairs);
        
        addDocumentToIndex(luceneDoc);
        
    }
	
	private Document addPredictionFieldsToDocument(Document luceneDoc, HashMap<String, String> predictionFieldValuePairs) {
        String businessId = predictionFieldValuePairs.get("businessId");
        String predictedCategories = predictionFieldValuePairs.get("predictedCategories");
        String businessCategories = predictionFieldValuePairs.get("businessCategories");
        String precision = predictionFieldValuePairs.get("precision");
        String recall = predictionFieldValuePairs.get("recall");
        
        if(businessId != null) luceneDoc.add(new StringField("businessId",businessId,Field.Store.YES));
        if(precision != null) luceneDoc.add(new DoubleField("precision",Double.parseDouble(precision),Field.Store.YES));
        if(recall != null) luceneDoc.add(new DoubleField("recall",Double.parseDouble(recall),Field.Store.YES));
        luceneDoc.add(new StringField("type","prediction",Field.Store.YES));
        
        luceneDoc = addArrayValuesTODocument(luceneDoc, predictedCategories,"predictedCategories", true);
        luceneDoc = addArrayValuesTODocument(luceneDoc, businessCategories,"businessCategories", true);
        
        return luceneDoc;
    }

    public void indexBusinessFields(HashMap<String, String> fieldValuePairs)
	{
	}
	
	public void indexReviewFields(HashMap<String, String> fieldValuePairs)
	{
	}
	
	public void commitLuceneIndex()
	{
	    try {
	        this.writer.forceMerge(1);
            this.writer.commit();
        } catch (IOException e) {
            e.printStackTrace();
        }
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
