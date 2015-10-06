package main.java.luceneAccess;

import main.java.helper.StopWord;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Random;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.analysis.util.CharArraySet;
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

import main.java.constants.Constants;

public class IndexUsingLucene {
	
	Analyzer analyzer;
	
	Directory trainDir;
	IndexWriterConfig trainIwc;
	IndexWriter trainWriter;
	
	Directory testDir;
	IndexWriterConfig testIwc;
	IndexWriter testWriter;
	
	String trainIndexPath;
	String testIndexPath;
	
	Random random;
	double trainingSplit;
	
	public IndexUsingLucene(String trainIndexPath, String testIndexPath) {
		
		this.trainingSplit = Constants.TRAINING_SPLIT;
	    this.trainIndexPath = trainIndexPath;
	    this.testIndexPath = testIndexPath;
	    this.random = new Random();
	    initializeWriter();
	}
	
	private void initializeWriter()
	{
		CharArraySet stopSet = getStopSet();
		
        analyzer = new StandardAnalyzer(stopSet);
        
        trainIwc = new IndexWriterConfig(Version.LATEST, analyzer);
        trainIwc.setOpenMode(OpenMode.CREATE_OR_APPEND);
        
        testIwc = new IndexWriterConfig(Version.LATEST, analyzer);
        testIwc.setOpenMode(OpenMode.CREATE_OR_APPEND);
        
        try {
            trainDir = FSDirectory.open(new File(this.trainIndexPath));
            trainWriter = new IndexWriter(trainDir, trainIwc);
            
            testDir = FSDirectory.open(new File(this.testIndexPath));
            testWriter = new IndexWriter(testDir, testIwc);
            
        } catch (IOException e) {
            e.printStackTrace();
        }
	}
	
	private CharArraySet getStopSet() {
		
		List<String> stopWords = StopWord.getStopWords();
		CharArraySet stopSet = new CharArraySet(stopWords, true);
		return stopSet;
	}

	private boolean isTraningExample()
	{
		double d = random.nextDouble();
		
		if(d < trainingSplit)
			return true;
		
		return false;
		
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
		String businessName = businessFieldValuePairs.get(Constants.BUSINESS_NAME);
		String businessId = businessFieldValuePairs.get(Constants.BUSINESS_ID);
		String businessCategories = businessFieldValuePairs.get(Constants.CATEGORIES);
		String businessRating = businessFieldValuePairs.get(Constants.BUSINESS_RATING);
		String reviewText = businessFieldValuePairs.get(Constants.REVIEW_TEXT);
		
		if(businessName != null) luceneDoc.add(new StringField(Constants.BUSINESS_NAME,businessName,Field.Store.YES));
		if(businessId != null) luceneDoc.add(new StringField(Constants.BUSINESS_ID,businessId,Field.Store.YES));
		
		if(businessRating != null) luceneDoc.add(new DoubleField(Constants.BUSINESS_RATING,Double.parseDouble(businessRating),Field.Store.YES));
		//if(businessRating != null) luceneDoc.add(new DoubleField("businessRating",Double.parseDouble(businessRating),Field.Store.YES));
		
		FieldType reviewFieldType = new FieldType();
		reviewFieldType.setIndexed(true);
		reviewFieldType.setTokenized(true);
		reviewFieldType.setStored(false);
		reviewFieldType.setStoreTermVectors(true);
		Field reviewField = new Field(Constants.REVIEW_TEXT, reviewText, reviewFieldType); 
		if(reviewText != null) luceneDoc.add(reviewField);if(reviewText != null) luceneDoc.add(reviewField); 
		
		luceneDoc.add(new StringField(Constants.TYPE,Constants.BUSINESS,Field.Store.YES));
		luceneDoc = addArrayValuesTODocument(luceneDoc, businessCategories,Constants.BUSINESS_CATEGORIES, true);
		
		return luceneDoc;
	}
	
	private void addDocumentToIndex(Document luceneDoc, boolean isTraining)
	{
		try {
			if(isTraining) trainWriter.addDocument(luceneDoc);
			else testWriter.addDocument(luceneDoc);
		
		} catch (IOException e) {
			e.printStackTrace();
		}	
	}
	
	public void indexBusiness(HashMap<String,String> businessFieldValuePairs)
	{
		if(businessFieldValuePairs.containsKey(Constants.IS_RESTAURANT))
		{
			Document luceneDoc = new Document();
			luceneDoc = addBusinessFieldsToDocument(luceneDoc, businessFieldValuePairs);
			
			boolean isTraining = isTraningExample();
			
			addDocumentToIndex(luceneDoc, isTraining);
		}
		
	}

    public void indexPrediction(HashMap<String,String> predictionFieldValuePairs)
    {
        Document luceneDoc = new Document();
        luceneDoc = addPredictionFieldsToDocument(luceneDoc, predictionFieldValuePairs);      
        addDocumentToIndex(luceneDoc,false);
        
    }
	
	private Document addPredictionFieldsToDocument(Document luceneDoc, HashMap<String, String> predictionFieldValuePairs) {
        
		String businessId = predictionFieldValuePairs.get(Constants.BUSINESS_ID);
        String predictedCategories = predictionFieldValuePairs.get(Constants.PREDICTED_CATEGORIES);
        String businessCategories = predictionFieldValuePairs.get(Constants.BUSINESS_CATEGORIES);
        String precision = predictionFieldValuePairs.get(Constants.PRECISION);
        String recall = predictionFieldValuePairs.get(Constants.RECALL);
        
        if(businessId != null) luceneDoc.add(new StringField(Constants.BUSINESS_ID,businessId,Field.Store.YES));
        if(precision != null) luceneDoc.add(new DoubleField(Constants.PRECISION,Double.parseDouble(precision),Field.Store.YES));
        if(recall != null) luceneDoc.add(new DoubleField(Constants.RECALL,Double.parseDouble(recall),Field.Store.YES));
        luceneDoc.add(new StringField(Constants.TYPE,Constants.PREDICTION,Field.Store.YES));
        
        luceneDoc = addArrayValuesTODocument(luceneDoc, predictedCategories,Constants.PREDICTED_CATEGORIES, true);
        luceneDoc = addArrayValuesTODocument(luceneDoc, businessCategories,Constants.BUSINESS_CATEGORIES, true);
        
        return luceneDoc;
    }

	public void commitLuceneIndex()
	{
	    try {
	        this.trainWriter.forceMerge(1);
            this.trainWriter.commit();
            
	        this.testWriter.forceMerge(1);
            this.testWriter.commit();
            
        } catch (IOException e) {
            e.printStackTrace();
        }
	}
	
	public void closeLuceneLocks()
	{
		try {

			trainWriter.close();
			trainDir.close();
			
			testWriter.close();
			testDir.close();
		
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
}
