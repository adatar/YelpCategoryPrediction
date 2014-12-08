package unitTesting;

import java.io.IOException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;

import luceneIndexingAndReading.SearchTestDataFromLucene;

import org.apache.lucene.document.Document;

import naiveBayesPrediction.PredictUsingNaiveBayes;

public class TestPredictUsingNaiveBayesModel {

    public static void main(String[] args) throws IOException {
        
        String trainingIndexPath = "trainingIndex";
        String testIndexPath = "testIndex";
        String testBusinessFile = "data/yelp_test_business.json";
        String testReviewsFile = "data/yelp_test_reviews.json";

        PredictUsingNaiveBayes punb = new PredictUsingNaiveBayes(trainingIndexPath, testIndexPath, testReviewsFile, testBusinessFile);
        
        int count = 0;
        System.out.println("\nPRIOR PROBABILITIES:");
        
        for(Map.Entry<String, Double> prior : punb.getNaiveBayesModel().getPriors().entrySet())
        {
            if(count > 100)
                break;
            System.out.println(prior.getKey() + " : " + prior.getValue());
            count++;
        }
        
        System.out.println("\nCONDITIONAL PROBABILITIES:");
        
        count = 0;
        Iterator<Entry<String, HashMap<String, Double>>> itr = punb.getNaiveBayesModel().getConditionals().entrySet().iterator();
        while(itr.hasNext())
        {
            Entry<?, ?> conditional = itr.next();
            System.out.println("CATEGORY: " + conditional.getKey());
            HashMap<String, Double> innerConditional = (HashMap<String, Double>) conditional.getValue();
            for(Map.Entry<String, Double> termProbability : innerConditional.entrySet())
            {
                if(count >= 100) break;
                System.out.println("\tTERM: " + termProbability.getKey());
                System.out.println("\tPROBABILITY: " + termProbability.getValue() + "\n");
                count++;
            }
        }

        // Top 4 categories reported using Naive Bayes Model
        count = 0;
        punb.processAndPredictTest(4);
    }
}
