package dataPreprocessing;

import java.util.HashMap;

import org.json.JSONObject;

public class ProcessReviewData extends ProcessDataFromJson {

	public ProcessReviewData(String indexPath) {
		super(indexPath);
	}

	@Override
	void parseJson(String jsonLine) {
		
		JSONObject jsonObject = new JSONObject(jsonLine);
		
		
		String businessId = jsonObject.getString("business_id");
		String userId = jsonObject.getString("user_id");
		String reviewId = jsonObject.getString("review_id");
		double reviewStars = jsonObject.getDouble("stars");
		String date = jsonObject.getString("date");
		String text = jsonObject.getString("text");
				
		HashMap<String,String> fieldValuePairs = new HashMap<>();
		
		fieldValuePairs.put("businessId", businessId);
		fieldValuePairs.put("userId", userId);
		fieldValuePairs.put("reviewId", reviewId);
		fieldValuePairs.put("reviewRating", new Double(reviewStars).toString());
		fieldValuePairs.put("date", date.toString());
		fieldValuePairs.put("text", text);
		
		indexUsingLucene(fieldValuePairs);
		
	}
	
	@Override
	public void indexUsingLucene(HashMap<String,String> fieldValuePairs) {
		
		indexUsingLucene.indexReviewFields(fieldValuePairs);
		
	}
}
