package unitTesting;

import dataPreprocessing.ProcessReviewsWithBusiness;

public class TestDataProcessingAndIndexing {

	public static void main(String[] args) {
		
		String indexPath = "./index";
		String businessFile = "/u/adatar/Z534/Yelp Dataset/yelp_business_17.json";
		String reviewFile = "/u/adatar/Z534/Yelp Dataset/yelp_review_200.json";
		
		ProcessReviewsWithBusiness processReviewsWithBusiness = new ProcessReviewsWithBusiness(indexPath);
		processReviewsWithBusiness.openLocation(reviewFile, businessFile);
		processReviewsWithBusiness.readLineAndParseJson();
		
	}

}
