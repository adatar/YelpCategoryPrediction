package dataPreprocessing;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
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
	
	private HashMap<String,String> parseReviewJson(String reviewJsonLine)
	{
		JSONObject jsonObject = new JSONObject(reviewJsonLine);
		
		String reviewId = jsonObject.getString("review_id");
		String businessId = jsonObject.getString("business_id");
		String userId = jsonObject.getString("user_id");
		double reviewStars = jsonObject.getInt("stars");
		String date = jsonObject.getString("date");
		String text = jsonObject.getString("text");
		

		HashMap<String,String> reviewFieldValuePairs = new HashMap<>();
		
		reviewFieldValuePairs.put("reviewId", reviewId);
		reviewFieldValuePairs.put("businessId", businessId);
		reviewFieldValuePairs.put("userId", userId);
		reviewFieldValuePairs.put("reviewRating", new Double(reviewStars).toString());
		reviewFieldValuePairs.put("date", date.toString());
		reviewFieldValuePairs.put("text", text);
		
		return reviewFieldValuePairs;
	}
		
	
	private HashMap<String,String> parseBusinessJson(String businessJsonLine)
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
	
	private String getBusinessId(HashMap<String, String> fieldValuePair)
	{
			return fieldValuePair.get("businessId");
	}
	
	private void indexUsingLucene(HashMap<String, String> reviewFieldValueMap,
			HashMap<String, String> businessFieldValueMap, boolean businessChanged) {
		
		reviewFieldValueMap.remove("businessId");
		indexUsingLucene.indexBothFields(businessFieldValueMap, reviewFieldValueMap, businessChanged);
		printWhatToParse(reviewFieldValueMap, businessFieldValueMap);
		
	}
	
	private void printWhatToParse(HashMap<String, String> reviewFieldValueMap,
			HashMap<String, String> businessFieldValueMap){
		
		System.out.println(businessFieldValueMap.get("businessId") + "\t" + reviewFieldValueMap.get("userId"));
		
	}
	

	public void readLineAndParseJson() 
	{
		try
		{
		    String restBusinessFile = "data/yelp_rest_business.json";
		    String restReviewsFile = "data/yelp_rest_reviews.json";
		    
		    Path restBusinessPath = FileSystems.getDefault().getPath(restBusinessFile);
		    Path restReviewsPath = FileSystems.getDefault().getPath(restReviewsFile);
		    if (! Files.exists(restBusinessPath)) Files.createFile(restBusinessPath);
		    if (! Files.exists(restReviewsPath)) Files.createFile(restReviewsPath);
		    
		    BufferedWriter restBusinessWriter = new BufferedWriter(new FileWriter(restBusinessFile));
		    BufferedWriter restReviewsWriter = new BufferedWriter(new FileWriter(restReviewsFile));
		    
			String businessline = businessFileReader.readLine();
			String reviewline = reviewFileReader.readLine();
			
			HashMap<String, String> reviewFieldValueMap = parseReviewJson(reviewline);
			HashMap<String, String> businessFieldValueMap = parseBusinessJson(businessline);
			
			String businessBusinessId = getBusinessId(businessFieldValueMap);
			String reviewBusinessId = getBusinessId(reviewFieldValueMap);
			boolean businessChanged = true;
			
			while (reviewline != null && businessline != null)
			{
				if(businessBusinessId.equals(reviewBusinessId))
				{
//					indexUsingLucene(reviewFieldValueMap, businessFieldValueMap, businessChanged);
					if(businessFieldValueMap.get("categories").toLowerCase().contains("restaurant"))
					{
					    restReviewsWriter.write(reviewline + '\n');
					}
					    
					businessChanged = false;
					
					reviewline = reviewFileReader.readLine();
					if(reviewline != null)
					{
						reviewFieldValueMap = parseReviewJson(reviewline);
						reviewBusinessId = getBusinessId(reviewFieldValueMap);
					}
				}
				
				else
				{
                    if(businessFieldValueMap.get("categories").toLowerCase().contains("restaurant"))
                    {
                        restBusinessWriter.write(businessline + '\n');
                    }
                    
					businessline = businessFileReader.readLine();
					if(businessline != null)
					{
						businessFieldValueMap = parseBusinessJson(businessline);
						businessBusinessId = getBusinessId(businessFieldValueMap);
						businessChanged = true;
					}
				}			
			}		
			
			businessFileReader.close();
			reviewFileReader.close();
			indexUsingLucene.closeLuceneLocks();
			restBusinessWriter.close();
			restReviewsWriter.close();
			
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
