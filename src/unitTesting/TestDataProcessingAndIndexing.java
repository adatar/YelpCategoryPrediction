package unitTesting;

import dataPreprocessing.ProcessReviewsWithBusiness;

public class TestDataProcessingAndIndexing {

	public static void main(String[] args) {
		
		String trainIndexPath = "/u/adatar/Z534/index/train";
		String testIndexPath = "/u/adatar/Z534/index/test";
		String businessFile = "/u/adatar/Z534/Yelp Dataset/yelp_academic_dataset_business.json";
		String reviewFile = "/u/adatar/Z534/Yelp Dataset/yelp_academic_dataset_review.json";

		ProcessReviewsWithBusiness processReviewsWithBusiness = new ProcessReviewsWithBusiness(trainIndexPath, testIndexPath, reviewFile, businessFile);
		processReviewsWithBusiness.readLineAndParseJson();
		processReviewsWithBusiness.closeLuceneLocks();
	}

}