package main.java.helper;

public class TFIDFComparator implements Comparable<TFIDFComparator>{
	
	
	public String term;
	public double TFIDFScore;
	
	public TFIDFComparator(String term, double TFIDFScore)
	{
		this.TFIDFScore = TFIDFScore;
		this.term = term;
	}
	
	@Override
	public int compareTo(TFIDFComparator o) {
	    Double oD = new Double(o.TFIDFScore);
	    Double thisD = new Double(this.TFIDFScore);
	    
	    return oD.compareTo(thisD);
	}
	
}
