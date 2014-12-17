package dataPreprocessing;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.HashMap;

import luceneIndexingAndReading.IndexUsingLucene;

import org.json.JSONArray;
import org.json.JSONObject;

public class ProcessReviewsWithBusiness {
	
	BufferedReader reviewFileReader;
	BufferedReader businessFileReader;
	IndexUsingLucene indexUsingLucene;
	
	public ProcessReviewsWithBusiness(String trainIndexPath, String testIndexPath, String reviewFile, String businessFile) {
		
		indexUsingLucene = new IndexUsingLucene(trainIndexPath, testIndexPath);
		this.openLocation(reviewFile, businessFile);
	}
	
	public void setIndexUsingLucene(IndexUsingLucene indexUsingLucene) {
        this.indexUsingLucene = indexUsingLucene;
    }

	public void addPrediction(HashMap<String, String> predictionValuesMapPairs)
	{
	    this.indexUsingLucene.indexPrediction(predictionValuesMapPairs);
	}
	
	public void closeLuceneLocks()
	{
	    this.indexUsingLucene.closeLuceneLocks();
	}
	
	public void commitLuceneIndex()
	{
	    this.indexUsingLucene.commitLuceneIndex();
	}
	
    protected HashMap<String,String> parseReviewJson(String reviewJsonLine)
	{
		JSONObject jsonObject = new JSONObject(reviewJsonLine);
		
		String businessId = jsonObject.getString("business_id");
		String text = jsonObject.getString("text");
		

		HashMap<String,String> reviewFieldValuePairs = new HashMap<>();
		
		reviewFieldValuePairs.put("businessId", businessId);
		reviewFieldValuePairs.put("text", text);
		
		return reviewFieldValuePairs;
	}
		
	
	protected HashMap<String,String> parseBusinessJson(String businessJsonLine)
	{
		
		JSONObject jsonObject = new JSONObject(businessJsonLine);
		
		
		String businessId = jsonObject.getString("business_id");
		String name = jsonObject.getString("name");
		double stars = jsonObject.getInt("stars");
		JSONArray categoriesArray = jsonObject.getJSONArray("categories");
		
		String categories = "";
		
		for(int i = 0; i< categoriesArray.length() -1 ; i++)
			categories = categories + categoriesArray.getString(i) + ", ";	
		
		if(categoriesArray.length() > 0) categories = categories + categoriesArray.getString(categoriesArray.length()-1);
		
		
		HashMap<String,String> businessFieldValuePairs = new HashMap<>();
		
		if(isRestaurant(categoriesArray))
			businessFieldValuePairs.put("isRestaurant", "T");
		
		
		businessFieldValuePairs.put("businessId", businessId);
		businessFieldValuePairs.put("businessName", name);
		businessFieldValuePairs.put("businessRating", new Double(stars).toString());
		businessFieldValuePairs.put("categories", categories);
		
		return businessFieldValuePairs;
		
	}
	
	protected String getBusinessId(HashMap<String, String> fieldValuePair)
	{
			return fieldValuePair.get("businessId");
	}
	
	
	public void readLineAndParseJson() 
	{
		try
		{
			String businessline = businessFileReader.readLine();
			String reviewline = reviewFileReader.readLine();
			
			HashMap<String, String> reviewFieldValueMap = parseReviewJson(reviewline);
			HashMap<String, String> businessFieldValueMap = parseBusinessJson(businessline);
			
			String businessBusinessId = getBusinessId(businessFieldValueMap);
			String reviewBusinessId = getBusinessId(reviewFieldValueMap);
			
			StringBuilder reviewText = new StringBuilder();
			System.out.println("Indexing..");
			int business_count = 1;
			while (reviewline != null && businessline != null && business_count < 7000) 
			{

				//System.out.println(business_count);
				
				if(businessBusinessId.equals(reviewBusinessId))
				{
				    reviewText.append(reviewFieldValueMap.get("text") + " ");
					reviewline = reviewFileReader.readLine();
					
					if(reviewline != null)
					{
						reviewFieldValueMap = parseReviewJson(reviewline);
						reviewBusinessId = getBusinessId(reviewFieldValueMap);
					}
				}
				
				else
				{
				    businessFieldValueMap.put("reviewText", reviewText.toString());
				    indexUsingLucene.indexBusiness(businessFieldValueMap);
				    business_count++;
					businessline = businessFileReader.readLine();
					if(businessline != null)
					{
						businessFieldValueMap = parseBusinessJson(businessline);
						businessBusinessId = getBusinessId(businessFieldValueMap);
					}
				}
				
			}		
            businessFieldValueMap.put("reviewText", reviewText.toString());
            indexUsingLucene.indexBusiness(businessFieldValueMap);
            
            System.out.println("Indexing Complete..");

			businessFileReader.close();
			reviewFileReader.close();
			this.commitLuceneIndex();
			
		} catch (IOException e)
		{
			e.printStackTrace();
		}
	
	}

	private boolean isRestaurant(JSONArray categoriesArray) {
		
		for(int i = 0; i< categoriesArray.length(); i++)
		{
			if(categoriesArray.getString(i).equals("Restaurants")) return true;					
		}
		return false;
	}

	public void openLocation(String reviewFile, String businessFile)	
	{
		try
		{
			reviewFileReader = new BufferedReader(new FileReader(reviewFile));
			businessFileReader = new BufferedReader(new FileReader(businessFile));
			
		} 
		catch (IOException ioexception)
		{
			System.out.println("ERROR: File not found.");
		}
		System.out.println("OPENED");
	}

}
