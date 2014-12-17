package naiveBayesPrediction;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.PriorityQueue;

import org.apache.lucene.document.Document;

import unitTesting.DS;
import luceneIndexingAndReading.SearchFromLucene;

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
	            
	            if(category.contains("Indian") || category.contains("Delis"))
		    		System.out.println("POSITIVE Cond: " + conditional + " " + countForTermCategory + " " + word);
		      //  System.out.println("NEGATIVE Cond: " + (1 - conditional));
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
    		ArrayList<String> predictedLabels = new ArrayList<String>();
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
    		
    		printPrediction(predictedCategories, actualCategories, businessId);
    		
    		//Call functions getPrecision & getRecall
    		
    	}
    	
    }
    
    
    private void printPrediction(ArrayList<PredictedCategory> predictions, ArrayList<String> actual, String businessId)
    {
    	System.out.println("BUSINESS_ID: " + businessId);
    	System.out.print("PREDICTIONS: ");
    	for(PredictedCategory category : predictions)
    	{
    		System.out.print(category + "\t");
    	}
    	
    	System.out.println();
    	System.out.print("ACTUAL: ");
    	for(String category : actual)
    	{
    		System.out.print(category + "\t");
    	}
    	System.out.println();
    	
    }
    
    
    //-------------------------------------------------------------------------------------------------------------
    //PREDICTION CODE - TO MODIFY
    /*
    public static Map<String, Double> sortByValue(Map<String, Double> unsortMap) {   
        List<Entry<String, Double>> list = new LinkedList<Entry<String, Double>>(unsortMap.entrySet());
     
        Collections.sort(list, new Comparator<Entry<String, Double>>() {
            @Override
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
	    int[] docIds = this.testingData.getAllBusinessDocuments();
	    HashMap<String, Double> scores = new HashMap<String, Double>();
	    for(int docId : docIds){
	        
	        Document doc = this.testingData.getDocumentById(docId);
	        ArrayList<String> terms = this.testingData.getReviewTermsForDocument(docId);
	        
	        for (String category : this.trainingData.getAllCategories())
	        {
	            double prior = this.naiveBayesModel.getPrior(category);
	            double score = prior > 0 ? Math.log(prior) : 0.0;
	            
	            for(String term: terms)
	            {
	                double conditional = this.naiveBayesModel.getConditional(category, term);
	                score += conditional > 0 ? Math.log(conditional) : 0;
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
	    this.processTestData.commitLuceneIndex();
	    this.processTestData.closeLuceneLocks();
	}
	*/
	
	
	//---PRECISION--AND--RECALL--USE--THIS--CODE \/
	
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
            sb.append(value).append(",");
        
        return sb.toString();
    }
}
