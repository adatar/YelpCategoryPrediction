package outliers;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.math.BigDecimal;
import java.math.RoundingMode;

import luceneIndexingAndReading.SearchingFromLucene;

public class OutliersEvaluation {
    BufferedReader outlierFileReader;
    SearchingFromLucene businessDataSearch;

    public OutliersEvaluation(String filePath, String indexPath) {
        try {
            this.outlierFileReader = new BufferedReader(new FileReader(filePath));
            this.businessDataSearch = new SearchingFromLucene(indexPath);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
    }

    public void evaluateOutliers() {
        try {
            
            String outlierReviewId = outlierFileReader.readLine();
            int truePositives = 0;
            int falsePositives = 0;
            
            while (outlierReviewId != null) {
                String userId = this.businessDataSearch.getUserForReview(outlierReviewId);
                String businessId = this.businessDataSearch.getBusinessForReview(outlierReviewId);
                double businessRating = this.businessDataSearch.getBusinessRating(businessId);
                
                int[] reviewDocs = this.businessDataSearch.getAllReviewsByUser(userId);
                if (reviewDocs.length < 5){
                    truePositives += 1;
                    outlierReviewId = this.outlierFileReader.readLine();
                    continue;
                }
                
                int extremeReviews = 0;
                int normalReviews = 0;
                
                for(int reviewDoc : reviewDocs){
                    double reviewRating = this.businessDataSearch.getRatingForReview(reviewDoc);
                    
                    if (Outliers.isOutlier(businessRating, reviewRating))
                        extremeReviews += 1;
                    else
                        normalReviews += 1;
                    
                    double credibilityScore = (double) normalReviews / (extremeReviews + normalReviews);
                    System.out.println("Credibility score: " + credibilityScore);
                    
                    if (credibilityScore < 0.6)
                        truePositives += 1;
                    else
                        falsePositives += 1;
                }
                outlierReviewId = this.outlierFileReader.readLine();
            }
            
            double accuracy = (double) truePositives / (truePositives + falsePositives);
            
            BigDecimal bd = new BigDecimal(accuracy);
            bd = bd.setScale(2, RoundingMode.HALF_UP);
            System.out.println("ACCURACY : " + bd.doubleValue());
            this.outlierFileReader.close();
            
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
