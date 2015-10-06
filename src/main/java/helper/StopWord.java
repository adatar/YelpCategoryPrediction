package main.java.helper;

import java.util.List;

public class StopWord {
	
	private static List<String> stopWords = null;

	public static List<String> getStopWords() {
		
		if(stopWords == null){	
			readStopWords();
		}
		
		return stopWords;
	}
	
	private static void readStopWords() {
		
		stopWords = TextFileReader.readFile("/home/abhishek/git/YelpCategoryPrediction/src/main/resources/stopword");
	}

}
