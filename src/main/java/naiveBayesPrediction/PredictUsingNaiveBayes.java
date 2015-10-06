package main.java.naiveBayesPrediction;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.PriorityQueue;

import main.java.luceneAccess.SearchFromLucene;

import org.apache.lucene.document.Document;

import test.unitTesting.DS;

public class PredictUsingNaiveBayes {
	
	private HashMap<String, NaiveBayesModel> naiveBayesModelMap;
	
	private SearchFromLucene trainingData;
	private SearchFromLucene testingData;
	private HashSet<String> highTFIDFWords = new HashSet<>();
	
	private ArrayList<String> categories;
	
    public SearchFromLucene getTrainingData() {
        return trainingData;
    }

    public SearchFromLucene getTestData() {
        return testingData;
    }

    public PredictUsingNaiveBayes(String trainingIndexPath, String testIndexPath, String testReviewsFile, String testBusinessFile)
	{
    	naiveBayesModelMap = new HashMap<String, NaiveBayesModel>();
   	
        trainingData = new SearchFromLucene(trainingIndexPath);
        testingData = new SearchFromLucene(testIndexPath);
        
        this.categories = this.trainingData.getAllCategories();
        
		this.updatePriorsAndConditionals();
	}

    private void updatePriorsAndConditionals()
	{
	    int totalBusinesses = this.trainingData.getTotalDocumentCount();
	    
	    System.out.println("Creating models..");
	    
	    PriorityQueue<DS> fpq = trainingData.getVocabWithFreqForReview();
        int counter = 0;
		while(!fpq.isEmpty() && counter < 600)
		{
			DS ds = fpq.poll();
			highTFIDFWords.add(ds.term);
			counter++;
		}
	    
	    for (String category : categories)
	    {
	    	

	    	System.out.println("For Category: " + category);
	    	
	    	NaiveBayesModel naiveBayesModel = new NaiveBayesModel(category);
	    	
	    	int categoryCount = this.trainingData.getCategoryCount(category);
	    	System.out.println("Count " +categoryCount);
	    	
	        double priorForCategory = (categoryCount + 1) / ((double) totalBusinesses + 2);
	        
	        naiveBayesModel.addPrior("POSITIVE", priorForCategory);
	        naiveBayesModel.addPrior("NEGATIVE", 1 - priorForCategory);
	        
	        System.out.println("POSITIVE PRIOR: " + priorForCategory);
	        System.out.println("NEGATIVE PRIOR: " + (1 - priorForCategory));
	        System.out.println();
	        
	        for(String word : highTFIDFWords)
	        {
	        	int countForTermCategory = this.trainingData.getWordCountForGivenCategoryAndWord(word, category) + 1;
	        	
	        	double conditional = (double) countForTermCategory / (categoryCount + 2);
	            naiveBayesModel.addOrUpdateConditional("POSITIVE", word, conditional);
	            naiveBayesModel.addOrUpdateConditional("NEGATIVE", word, 1 - conditional);
	            
	        }
	        	        
            naiveBayesModelMap.put(category, naiveBayesModel);
	    }
	}
    
    class PredictedCategory implements Comparable<PredictedCategory>	
    {
    	String category;
    	double prior;
    	
    	public PredictedCategory(String category, double prior) {
    		this.category = category;
    		this.prior = prior;
		}
    	
    	@Override
    	public String toString(){
    		return this.category + "-" + this.prior;
    	}
    	
		@Override
		public int compareTo(PredictedCategory o) {
			return new Double(o.prior).compareTo(new Double(this.prior));
		}
    }
    
    public void predict()
    {
    	int[] docIds = this.testingData.getAllBusinessDocuments();
    	System.out.println(docIds.length);
    	
    	for(int docId : docIds)
    	{
    		ArrayList<PredictedCategory> predictedCategories = new ArrayList<PredictedCategory>();
    		
    		ArrayList<String> terms = this.testingData.getReviewTermsForDocument(docId);
    		
    		Document doc = this.testingData.getDocumentById(docId);
    		String businessId = doc.get("businessId");
    		
    		for(String category : categories)
        	{
    			NaiveBayesModel categoryModel = naiveBayesModelMap.get(category);
    			
	            double positivePrior = categoryModel.getPrior("POSITIVE");
	            double negativePrior = categoryModel.getPrior("NEGATIVE");
	            
	            double positiveProbability = positivePrior;
	            double negativeProbability = negativePrior;
	            
    			for(String term : terms)
    			{
    				if(! this.highTFIDFWords.contains(term))
    					continue;
    				double posConditional = categoryModel.getConditional("POSITIVE", term);
    				double negConditional = 1 - posConditional; 
    				
    				positiveProbability *= posConditional;
    				negativeProbability *= negConditional;
    			}
        		
    			if(positiveProbability > negativeProbability)
    				predictedCategories.add(new PredictedCategory(category, positiveProbability));
        	}

    		ArrayList<String> actualCategories = this.testingData.getCategoriesForDocument(docId);
    		
    		ArrayList<String> predictedList = printPrediction(predictedCategories, actualCategories, businessId);
    		
    		double precision = getPrecision(actualCategories, predictedList);
    		double recall = getRecall(actualCategories, predictedList);
    		System.out.println("PRECISION: " + precision);
    		System.out.println("RECALL: " + recall);
    		
    		
    	}
    	
    }
    
    
    private ArrayList<String> printPrediction(ArrayList<PredictedCategory> predictions, ArrayList<String> actual, String businessId)
    {
    	System.out.println("BUSINESS_ID: " + businessId);
    	System.out.print("PREDICTIONS: ");
    	
    	PriorityQueue<PredictedCategory> predictedPQueue = new PriorityQueue<>();
    	
    	for(PredictedCategory category : predictions)
    	{
    		predictedPQueue.add(category);
    	}
    	int count = 0;
    	
    	ArrayList<String> predictedCategories = new ArrayList<>();
    	while(!predictedPQueue.isEmpty() && count < 4)
    	{

    		PredictedCategory pc = predictedPQueue.poll();
    		predictedCategories.add(pc.category);
    		System.out.print(pc.category+ "\t");
    		count++;
    		
    	}
    	
    	System.out.println();
    	System.out.print("ACTUAL: ");
    	for(String category : actual)
    	{
    		System.out.print(category + "\t");
    	}
    	System.out.println();
    	
    	return predictedCategories;
    }
    
    
    //---PRECISION--AND--RECALL--USE--THIS--CODE \/
	
	private double getPrecision(ArrayList<String> businessCategories, ArrayList<String> predictedCategories)
	{
	    int truePositives = 0;
	    for(String predictedCategory : predictedCategories)
	    {
	        if(businessCategories.contains(predictedCategory))
	            truePositives += 1;
	    }
	    return (double) truePositives / predictedCategories.size();
	}
	
    private double getRecall(ArrayList<String> businessCategories, ArrayList<String> predictedCategories)
    {
        int truePositives = 0;
        for(String predictedCategory : predictedCategories)
        {
            if(businessCategories.contains(predictedCategory))
                truePositives += 1;
        }
        return (double) truePositives / businessCategories.size();
    }
    
    private ArrayList<String> listFromString(String commaSeperated)
    {
        String[] values = commaSeperated.split(",");
        
        return (ArrayList<String>) Arrays.asList(values);
    }
    
    private String stringFromList(ArrayList<String> values)
    {
        StringBuilder sb = new StringBuilder();
        
        for(String value : values)
            sb.append(value).append(",");
        
        return sb.toString();
    }
}
