package unitTesting;

public class DS implements Comparable<DS>{
	
	
	int freq;
	String term;
	
	public DS(String term, int freq)
	{
		this.freq = freq;
		this.term = term;
	}
	
	@Override
	public int compareTo(DS o) {
		if(this.freq < o.freq)
			return 1;
		
		if(this.freq > o.freq)
			return -1;
		
		return 0;
	}
	
	

}
