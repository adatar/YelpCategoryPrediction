package unitTesting;

public class DS implements Comparable<DS>{
	
	
	double TFIDFScore;
	String term;
	
	public DS(String term, double TFIDFScore)
	{
		this.TFIDFScore = TFIDFScore;
		this.term = term;
	}
	
	@Override
	public int compareTo(DS o) {
	    Double oD = new Double(o.TFIDFScore);
	    Double thisD = new Double(this.TFIDFScore);
	    
	    return oD.compareTo(thisD);
	}
	
}