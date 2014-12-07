package naiveBayesPrediction;

import java.util.HashMap;

public class NaiveBayesModel {
	
	private HashMap<String,Double> priors;
	private HashMap<String, HashMap<String, Double>> conditionals;
	
	public HashMap<String, Double> getPriors() {
        return priors;
    }

    public HashMap<String, HashMap<String, Double>> getConditionals() {
        return conditionals;
    }

    NaiveBayesModel()
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
	
	
	public void addOrUpdateConditional(String label, String word, Double probability)
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
	
	public double getConditional(String label, String word)
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
