package outliers;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.math.BigDecimal;
import java.math.RoundingMode;

import luceneIndexingAndReading.SearchingFromLucene;

public class OutliersEvaluation {
	BufferedReader outlierFileReader;
	BufferedWriter outlierEvalWriter;
	BufferedWriter outlierEvalWriter_Ex;
	FileWriter fw = null;
	FileWriter extremeOnly = null;
	SearchingFromLucene businessDataSearch;
	File file = null;
	File file_ExtremeOnly = null;

	public OutliersEvaluation(String filePath, String indexPath) {
		try {
			this.outlierFileReader = new BufferedReader(
					new FileReader(filePath));
			this.businessDataSearch = new SearchingFromLucene(indexPath);
			this.file = new File("outlierEval_All.txt");
			if (!this.file.exists())
				this.file.createNewFile();
			else {
				this.file.delete();
				this.file.createNewFile();
			}

			this.file_ExtremeOnly = new File("outlierEval_Extreme.txt");
			if (!this.file_ExtremeOnly.exists())
				this.file_ExtremeOnly.createNewFile();
			else {
				this.file_ExtremeOnly.delete();
				this.file_ExtremeOnly.createNewFile();
			}

			fw = new FileWriter(this.file.getAbsolutePath(), true);
			outlierEvalWriter = new BufferedWriter(fw);

			extremeOnly = new FileWriter(
					this.file_ExtremeOnly.getAbsolutePath(), true);
			outlierEvalWriter_Ex = new BufferedWriter(extremeOnly);

			this.outlierEvalWriter
					.write("REVIEWID"
							+ "\t\t\t\t"
							+ "USERID\t\t\t\t\tCRED_SCORE TOTAL_REVIEWS EXTR_REV NORM_REV TRUE_POS IS_ORIG_EXTREME\n");

			this.outlierEvalWriter_Ex
					.write("REVIEWID"
							+ "\t\t\t\t"
							+ "USERID\t\t\t\t\tCRED_SCORE TOTAL_REVIEWS EXTR_REV NORM_REV TRUE_POS IS_ORIG_EXTREME\n");

		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException i) {
			i.printStackTrace();
		}

	}

	public void evaluateOutliers() {
		try {

			String outlierReviewId = outlierFileReader.readLine();
			int truePositives = 0;
			int falsePositives = 0;
			int totalOutliers =  0;
			int trueExtreme = 0;
			int falseExtreme = 0;
			int insufRev = 0;
			while (outlierReviewId != null) {
				totalOutliers++;
				
				String userId = this.businessDataSearch
						.getUserForReview(outlierReviewId);
				String businessId = this.businessDataSearch
						.getBusinessForReview(outlierReviewId);
				double businessRating = this.businessDataSearch
						.getBusinessRating(businessId);

				int[] reviewDocs = this.businessDataSearch
						.getAllReviewsByUser(userId);
				if (reviewDocs.length < 2) {
					insufRev++;
//					outlierEvalWriter.write(outlierReviewId + "\t" + userId
//							+ "\t" + "1.0\t\t\t\t" + reviewDocs.length
//							+ "\t\t\tNA\t\tNA\t\tNA\t\t\tNO\n");
					outlierReviewId = this.outlierFileReader.readLine();
					continue;
				}

				int extremeReviews = 0;
				int normalReviews = 0;
				int totalReviewCount = 0;
				double credibilityScore = 0.;
				for (int reviewDoc : reviewDocs) {
					totalReviewCount++;
					double reviewRating = this.businessDataSearch
							.getRatingForReview(reviewDoc);

					//Check if all the User Reviews are Extreme Reviews
					if (Outliers.isOutlier(businessRating, reviewRating))
						extremeReviews += 1;
					else
						normalReviews += 1;

				}
				//Calculate the Credibility score of the user
				credibilityScore = (double) normalReviews
						/ (extremeReviews + normalReviews);
				//System.out.println("Credibility score: " + credibilityScore);

				//If user is less credible then the original review in consideration is an extreme review
				if (credibilityScore < 0.6)
					truePositives = 1;
				//Else this is a False positive and the original review is normal
				else
					truePositives = 0;

				
				if (truePositives == 1) {
					trueExtreme++;
					outlierEvalWriter.write(outlierReviewId + "\t" + userId
							+ "\t" + String.format("%.1f", credibilityScore)
							+ "\t\t\t\t" + totalReviewCount + "\t\t\t"
							+ extremeReviews + "\t\t" + normalReviews + "\t\t"
							+ truePositives + "\t\t\tYES\n");
					outlierEvalWriter_Ex.write(outlierReviewId + "\t" + userId
							+ "\t" + String.format("%.1f", credibilityScore)
							+ "\t\t\t\t" + totalReviewCount + "\t\t\t"
							+ extremeReviews + "\t\t" + normalReviews + "\t\t"
							+ truePositives + "\t\t\tYES\n");
				} else {
					falseExtreme++;
					outlierEvalWriter.write(outlierReviewId + "\t" + userId
							+ "\t" + String.format("%.1f", credibilityScore)
							+ "\t\t\t\t" + totalReviewCount + "\t\t\t"
							+ extremeReviews + "\t\t" + normalReviews + "\t\t"
							+ truePositives + "\t\t\tNO\n");
//					outlierEvalWriter_Ex.write(outlierReviewId + "\t" + userId
//							+ "\t" + String.format("%.1f", credibilityScore)
//							+ "\t\t\t\t" + totalReviewCount + "\t\t\t"
//							+ extremeReviews + "\t\t" + normalReviews + "\t\t"
//							+ truePositives + "\t\t\tNO\n");
				}
				outlierReviewId = this.outlierFileReader.readLine();
			}
			
			// double accuracy = (double) truePositives
			// / (truePositives + falsePositives);
			//
			// BigDecimal bd = new BigDecimal(accuracy);
			// bd = bd.setScale(2, RoundingMode.HALF_UP);
			// System.out.println("ACCURACY : " + bd.doubleValue());
			this.outlierEvalWriter.close();
			this.outlierFileReader.close();
			this.outlierEvalWriter_Ex.close();
			
			System.out.println("TOTAL_OUTLIERS FOUND:"+totalOutliers);
			System.out.println("TOTAL TRUE EXTREME REVIEWS:"+trueExtreme);
			System.out.println("TOTAL FALSE EXTREME REVIEWS:"+falseExtreme);
			System.out.println("TOTAL USERS WITH VERY LESS REVIEWS:"+insufRev);
			System.out.println("Check file outlierEval_Extreme.txt and outlierEval_All.txt for more info....");
			

		} catch (IOException e) {
			e.printStackTrace();
		}
	}
}
