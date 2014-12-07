package naiveBayesPrediction;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.index.DocsEnum;
import org.apache.lucene.index.IndexableField;
import org.apache.lucene.index.Terms;
import org.apache.lucene.index.TermsEnum;
import org.apache.lucene.search.DocIdSetIterator;
import org.apache.lucene.util.BytesRef;

import dataPreprocessing.ProcessReviewsWithBusiness;
import luceneIndexingAndReading.SearchFromLucene;
import luceneIndexingAndReading.SearchTestDataFromLucene;

public class PredictUsingNaiveBayes {
	
	private NaiveBayesModel naiveBayesModel;
	private SearchFromLucene trainingData;
	private SearchTestDataFromLucene testData;
	private ProcessReviewsWithBusiness processTestData;
	
	public NaiveBayesModel getNaiveBayesModel() {
        return naiveBayesModel;
    }

    public SearchFromLucene getTrainingData() {
        return trainingData;
    }

    public SearchTestDataFromLucene getTestData() {
        return testData;
    }

    public PredictUsingNaiveBayes(String trainingIndexPath, String testIndexPath, String testReviewsFile, String testBusinessFile)
	{
		naiveBayesModel = new NaiveBayesModel();
        trainingData = new SearchFromLucene(trainingIndexPath);
        
        this.processTestData = new ProcessReviewsWithBusiness(testIndexPath, testReviewsFile, testBusinessFile);
        this.processTestData.readLineAndParseJson();
        testData = new SearchTestDataFromLucene(testIndexPath);

		this.updatePriorsAndConditionals();
	}

    private void updatePriorsAndConditionals()
	{
	    int totalBusinesses = this.trainingData.getTotalDocumentCount();
	    for (String category : this.trainingData.getAllCategories())
	    {
	        double priorForCategory = this.trainingData.getCategoryCount(category) / (double)totalBusinesses;
	        this.naiveBayesModel.addPrior(category, priorForCategory);
	        
	        int totalTermCategoryCounts = 0;
	        
	        for(String word : this.trainingData.getAllWordsInReview())
	        {
	            int countForTermCategory = this.trainingData.getWordCountForGivenCategoryAndWord(word, category) + 1;
	            totalTermCategoryCounts += countForTermCategory;
	            this.naiveBayesModel.addOrUpdateConditional(category, word, (double)countForTermCategory);
	        }
	        
            for(String word : this.trainingData.getAllWordsInReview())
            {
                double conditional = this.naiveBayesModel.getConditional(category, word);
                this.naiveBayesModel.addOrUpdateConditional(category, word, conditional/totalTermCategoryCounts);
            }
	    }
	}

    public static Map<String, Double> sortByValue(Map<String, Double> unsortMap) {   
        List<Entry<String, Double>> list = new LinkedList<Entry<String, Double>>(unsortMap.entrySet());
     
        Collections.sort(list, new Comparator<Entry<String, Double>>() {
            public int compare(Entry<String, Double> o1, Entry<String, Double> o2) {
                return o1.getValue().compareTo(o2.getValue());
            }
        });
     
        Map<String, Double> sortedMap = new LinkedHashMap<String, Double>();
        for (Iterator<Entry<String, Double>> it = list.iterator(); it.hasNext();) {
            Entry<String, Double> entry = it.next();
            sortedMap.put(entry.getKey(), entry.getValue());
        }
        return sortedMap;
    }
    
	public void processAndPredictTest(int k) throws IOException
	{
	    HashMap<Integer, Document> documents = this.testData.getAllBusinessDocuments();
	    HashMap<String, Double> scores = new HashMap<String, Double>();
	    for(Entry<Integer, Document> document : documents.entrySet()){
	        
	        int docId = document.getKey();
	        Document doc = document.getValue();
	        
	        ArrayList<String> terms = this.testData.getReviewTermsForDocument(docId);
	        
	        for (String category : this.trainingData.getAllCategories())
	        {
	            double score = Math.log(this.naiveBayesModel.getPrior(category));
	            
	            for(String term: terms)
	            {
	                score += Math.log(this.naiveBayesModel.getConditional(category, term));
	            }
	            scores.put(category, score);
	        }
	        
	        Map<String, Double> descendingScores = sortByValue(scores);
	        
	        int top = 0;
	        
	        ArrayList<String> predictedCategories = new ArrayList<String>(); 
	        for(String category : descendingScores.keySet())
	        {
	            if(top >= k)
	                break;
	            predictedCategories.add(category);
	            top++;
	        }
	        
	        String businessCategories = doc.get("businessCategories");
	        double precision = this.getPrecision(this.listFromString(businessCategories), predictedCategories);
	        double recall = this.getRecall(this.listFromString(businessCategories), predictedCategories);
	        String predictedCategoriesString = this.stringFromList(predictedCategories);

	        HashMap<String, String> predictionValuesMapPairs = new HashMap<>();
	        predictionValuesMapPairs.put("businessId", doc.get("businessId"));
	        predictionValuesMapPairs.put("businessCategories", businessCategories);
	        predictionValuesMapPairs.put("predictedCategories", predictedCategoriesString);
	        predictionValuesMapPairs.put("precision", String.valueOf(precision));
	        predictionValuesMapPairs.put("recall", String.valueOf(recall));
	        
	        this.processTestData.addPrediction(predictionValuesMapPairs);
	    }
	}
	
	private double getPrecision(List<String> businessCategories, List<String> predictedCategories)
	{
	    int truePositives = 0;
	    for(String predictedCategory : predictedCategories)
	    {
	        if(businessCategories.contains(predictedCategory))
	            truePositives += 1;
	    }
	    return (double) truePositives / predictedCategories.size();
	}
	
    private double getRecall(List<String> businessCategories, List<String> predictedCategories)
    {
        int truePositives = 0;
        for(String predictedCategory : predictedCategories)
        {
            if(businessCategories.contains(predictedCategory))
                truePositives += 1;
        }
        return (double) truePositives / businessCategories.size();
    }
    
    private List<String> listFromString(String commaSeperated)
    {
        String[] values = commaSeperated.split(",");
        return Arrays.asList(values);
    }
    
    private String stringFromList(List<String> values)
    {
        StringBuilder sb = new StringBuilder();
        
        for(String value : values)
            sb.append(value);
        
        return sb.toString();
    }
}
