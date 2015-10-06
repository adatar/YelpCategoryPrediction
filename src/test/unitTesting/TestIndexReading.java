package test.unitTesting;

import java.util.ArrayList;
import java.util.PriorityQueue;

import main.java.luceneAccess.SearchFromLucene;


public class TestIndexReading {
	
	public static void main(String args[])
	{
		String indexPath = "/u/adatar/Z534/index/train";
		
		SearchFromLucene searchingFromLucene = new SearchFromLucene(indexPath);
		
		System.out.println("NO OF BUSINESSES: " + searchingFromLucene.getTotalDocumentCount());
		
		System.out.println("NO OF BUSINESSES WHICH ARE RESTAURANT: " + searchingFromLucene.getCategoryCount("Restaurants")); //Should be 9
		
		System.out.println("Suck in review: " +searchingFromLucene.getDocumentCountContainingWord("cake"));
		
		System.out.println("Suck in review and category: " +searchingFromLucene.getWordCountForGivenCategoryAndWord("cake","American (Traditional)"));
		
		System.out.println("Get Business Name: " +searchingFromLucene.getBusinessName("zOc8lbjViUZajbY7M0aUCQ"));
		
		
		//Get word with frequency
		PriorityQueue<DS> fpq = searchingFromLucene.getVocabWithFreqForReview();
		
		while(!fpq.isEmpty())
		{
			DS ds = fpq.poll();
			System.out.println(ds.term + " " + ds.TFIDFScore);
		}
				
		//Get all words in review
		//ArrayList<String> al = searchingFromLucene.getAllWordsInReview();
		
		//for(String s : al)
			//System.out.println(s);


		//System.out.println("All categories: ");
		//printAList(searchingFromLucene.getAllCategories());
		//searchingFromLucene.getTopCategories();
		
	}
	
			
	public static void printAList(ArrayList<String> listObj)
	{
		for(String s : listObj)
		{
			System.out.println("\t" + s);
		}
		System.out.println();
	}

}
