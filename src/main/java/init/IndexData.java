package main.java.init;

import main.java.dataPreprocessing.ProcessReviewsWithBusiness;

public class IndexData {

	public static void main(String[] args) {

		String trainIndexPath = "/home/abhishek/Documents/YelpDataAndIndex/index/train";
		String testIndexPath = "/home/abhishek/Documents/YelpDataAndIndex/index/test";
		String businessFile = "/home/abhishek/Documents/YelpDataAndIndex/yelp_dataset_challenge_academic_dataset/yelp_academic_dataset_business.json";
		String reviewFile = "/home/abhishek/Documents/YelpDataAndIndex/yelp_dataset_challenge_academic_dataset/yelp_academic_dataset_review.json";

		ProcessReviewsWithBusiness processReviewsWithBusiness = new ProcessReviewsWithBusiness(trainIndexPath, testIndexPath, reviewFile, businessFile);
		processReviewsWithBusiness.readLineAndParseJson();
		processReviewsWithBusiness.closeLuceneLocks();

	}
}