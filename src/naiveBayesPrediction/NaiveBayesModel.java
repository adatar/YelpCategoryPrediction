package naiveBayesPrediction;

import java.util.HashMap;

public class BuildNaiveBayesModel {
	
	HashMap<String,Double> priors;
	HashMap<String, HashMap<String, Double>> conditionals;
	
	BuildNaiveBayesModel()
	{
		
		priors = new HashMap<>();
		conditionals = new HashMap<>();
		
	}
	
	public void addPrior(String label, Double probability)
	{
		if(!priors.containsKey(label))
		{
			priors.put(label, probability);
			
		}
	}
	
	public double getPrior(String label)
	{
		Double d = priors.get(label);
		
		if (d!= null) return d.doubleValue();
		else return 0;
	}
	
	
	public void addConditional(String label, String word, Double probability)
	{
		HashMap<String, Double> innerConditional = conditionals.get(label);
		
		if(innerConditional != null)
		{
			innerConditional.put(word, probability);
			return;
		}
		else
		{
			innerConditional = new HashMap<>();
			innerConditional.put(word, probability);
			conditionals.put(label, innerConditional);
		}
		
	}
	
	public double getConditional(String label, String word, Double probability)
	{
		HashMap<String, Double> innerConditional = conditionals.get(label);
		
		if(innerConditional != null)
		{
			Double d = innerConditional.get(word);
			if(d != null) return d.doubleValue();
		}
		
		return 0;
	}
}
