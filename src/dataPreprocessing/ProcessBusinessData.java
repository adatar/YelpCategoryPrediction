package dataPreprocessing;

import java.util.HashMap;

import org.json.JSONArray;
import org.json.JSONObject;

public class ProcessBusinessData extends ProcessDataFromJson {
	
	public ProcessBusinessData(String indexPath) {
		super(indexPath);
	}

	@Override
	void parseJson(String jsonLine)
	{
		
		JSONObject jsonObject = new JSONObject(jsonLine);
		
		String businessId = jsonObject.getString("business_id");
		String name = jsonObject.getString("name");
		double stars = jsonObject.getInt("stars");
		JSONArray categoriesArray = jsonObject.getJSONArray("categories");
		
		String categories = "";
		
		for(int i = 0; i< categoriesArray.length(); i++)
			categories = categories + categoriesArray.getString(i) + ", ";
		
		HashMap<String,String> fieldValuePairs = new HashMap<>();
		
		fieldValuePairs.put("businessId", businessId);
		fieldValuePairs.put("businessName", name);
		fieldValuePairs.put("businessRating", new Double(stars).toString());
		fieldValuePairs.put("categories", categories);
		
		indexUsingLucene(fieldValuePairs);
		
	}
	
	@Override
	public void indexUsingLucene(HashMap<String,String> fieldValuePairs) {
		
		indexUsingLucene.indexBusinessFields(fieldValuePairs);
		
	}
}
