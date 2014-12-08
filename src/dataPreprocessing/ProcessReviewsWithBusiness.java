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
	
	public ProcessReviewsWithBusiness(String indexPath, String reviewFile, String businessFile) {
		
		indexUsingLucene = new IndexUsingLucene(indexPath);
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
		
		for(int i = 0; i< categoriesArray.length(); i++)
			categories = categories + categoriesArray.getString(i) + ", ";	
		
				
		HashMap<String,String> businessFieldValuePairs = new HashMap<>();
		
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
			
			while (reviewline != null && businessline != null) 
			{
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

			businessFileReader.close();
			reviewFileReader.close();
			this.commitLuceneIndex();
		} catch (IOException e)
		{
			e.printStackTrace();
		}
	
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
