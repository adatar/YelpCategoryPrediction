package unitTesting;

import dataPreprocessing.ProcessReviewsWithBusiness;

public class TestDataProcessingAndIndexing {

	public static void main(String[] args) {
		
		String indexPath = "index";
		String businessFile = "data/yelp_train_business.json";
		String reviewFile = "data/yelp_train_reviews.json";
		
		ProcessReviewsWithBusiness processReviewsWithBusiness = new ProcessReviewsWithBusiness(indexPath, reviewFile, businessFile);
		processReviewsWithBusiness.readLineAndParseJson();
	}
}
