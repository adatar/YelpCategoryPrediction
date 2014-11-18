package naiveBayesPrediction;

import luceneIndexingAndReading.SearchFromLucene;

public class PredictUsingNaiveBayes {
	
	String indexPath;
	
	BuildNaiveBayesModel naiveBayesModel;
	SearchFromLucene searchFromLucene;
	
	PredictUsingNaiveBayes(String indexPath)
	{
		naiveBayesModel = new BuildNaiveBayesModel();
		searchFromLucene = new SearchFromLucene(indexPath);
		this.indexPath = indexPath;
	}
	
	

}
