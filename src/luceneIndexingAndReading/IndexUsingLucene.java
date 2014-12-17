package luceneIndexingAndReading;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;
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
		
		this.trainingSplit = 0.7;
	    this.trainIndexPath = trainIndexPath;
	    this.testIndexPath = testIndexPath;
	    this.random = new Random();
	    this.initializeWriter();
	}
	
	private void initializeWriter()
	{
		
		CharArraySet stopSet = getStopSet();
		
        analyzer = new StandardAnalyzer(stopSet);
        
        trainIwc = new IndexWriterConfig(Version.LUCENE_4_10_1, analyzer);
        trainIwc.setOpenMode(OpenMode.CREATE_OR_APPEND);
        
        testIwc = new IndexWriterConfig(Version.LUCENE_4_10_1, analyzer);
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
		
		final List<String> stopWords = Arrays.asList(
				   "a", "an", "and", "are", "as", "at", "be", "but", "by",
				   "for", "if", "in", "into", "is", "it",
				   "no", "not", "of", "on", "or", "such",
				   "that", "the", "their", "then", "there", "these",
				   "they", "this", "to", "was", "will", "with",
				   "a", "about", "above", "after", "again", "against", "all", "am", "an", "and", "any", "are", "aren't", 
				   "as", "at", "be", "because", "been", "before", "being", "below", "between", "both", "but", "by", 
				   "can't", "cannot", "could", "couldn't", "did", "didn't", "do", "does", "doesn't", "doing", "don't", 
				   "down", "during", "each", "few", "for", "from", "further", "had", "hadn't", "has", "hasn't", "have", 
				   "haven't", "having", "he", "he'd", "he'll", "he's", "her", "here", "here's", "hers", "herself", "him", 
				   "himself", "his", "how", "how's", "i", "i'd", "i'll", "i'm", "i've", "if", "in", "into", "is", "isn't", 
				   "it", "it's", "its", "itself", "let's", "me", "more", "most", "mustn't", "my", "myself", "no", "nor", 
				   "not", "of", "off", "on", "once", "only", "or", "other", "ought", "our", "ours", "ourselves", "out", 
				   "over", "own", "same", "shan't", "she", "she'd", "she'll", "she's", "should", "shouldn't", "so", 
				   "some", "such", "than", "that", "that's", "the", "their", "theirs", "them", "themselves", "then", 
				   "there", "there's", "these", "they", "they'd", "they'll", "they're", "they've", "this", "those", 
				   "through", "to", "too", "under", "until", "up", "very", "was", "wasn't", "we", "we'd", "we'll", 
				   "we're", "we've", "were", "weren't", "what", "what's", "when", "when's", "where", "where's", "which", 
				   "while", "who", "who's", "whom", "why", "why's", "with", "won't", "would", "wouldn't", "you", "you'd", 
				   "you'll", "you're", "you've", "your", "yours", "yourself", "yourselves"
				 );
		
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
		String businessName = businessFieldValuePairs.get("businessName");
		String businessId = businessFieldValuePairs.get("businessId");
		String businessCategories = businessFieldValuePairs.get("categories");
		String businessRating = businessFieldValuePairs.get("businessRating");
		String reviewText = businessFieldValuePairs.get("reviewText");
		
		if(businessName != null) luceneDoc.add(new StringField("businessName",businessName,Field.Store.YES));
		if(businessId != null) luceneDoc.add(new StringField("businessId",businessId,Field.Store.YES));
		
		if(businessRating != null) luceneDoc.add(new DoubleField("businessRating",Double.parseDouble(businessRating),Field.Store.YES));
		if(businessRating != null) luceneDoc.add(new DoubleField("businessRating",Double.parseDouble(businessRating),Field.Store.YES));
		
		//if(reviewText.length() > 0) luceneDoc.add(new TextField("reviewText",reviewText,Field.Store.YES));
				
		FieldType reviewFieldType = new FieldType();
		reviewFieldType.setIndexed(true);
		reviewFieldType.setTokenized(true);
		reviewFieldType.setStored(false);
		reviewFieldType.setStoreTermVectors(true);
		Field reviewField = new Field("reviewText", reviewText, reviewFieldType); 
		if(reviewText != null) luceneDoc.add(reviewField);if(reviewText != null) luceneDoc.add(reviewField); 
		
		luceneDoc.add(new StringField("type","business",Field.Store.YES));
		luceneDoc = addArrayValuesTODocument(luceneDoc, businessCategories,"businessCategories", true);
		
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
		//if(businessFieldValuePairs.containsKey("isRestaurant"))
		//{
			
			Document luceneDoc = new Document();
			luceneDoc = addBusinessFieldsToDocument(luceneDoc, businessFieldValuePairs);
			
			boolean isTraining = isTraningExample();
			
			addDocumentToIndex(luceneDoc, isTraining);
		//}
		
	}

    public void indexPrediction(HashMap<String,String> predictionFieldValuePairs)
    {
        Document luceneDoc = new Document();
        
        luceneDoc = addPredictionFieldsToDocument(luceneDoc, predictionFieldValuePairs);
        
        addDocumentToIndex(luceneDoc,false);
        
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
