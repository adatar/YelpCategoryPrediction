package unitTesting;

import java.io.IOException;

import naiveBayesPrediction.PredictUsingNaiveBayes;

public class TestPredictUsingNaiveBayesModel {

    public static void main(String[] args) throws IOException {
        
        String trainingIndexPath = "/u/adatar/Z534/index/train";
        String testIndexPath = "/u/adatar/Z534/index/test";
        String testBusinessFile = "/u/adatar/Z534/Yelp Dataset/yelp_business_17.json";
        String testReviewsFile = "/u/adatar/Z534/Yelp Dataset/yelp_review_200.json";

        PredictUsingNaiveBayes punb = new PredictUsingNaiveBayes(trainingIndexPath, testIndexPath, testReviewsFile, testBusinessFile);
        punb.predict();

    }
}
