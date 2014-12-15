package unitTesting;

import java.util.ArrayList;

import luceneIndexingAndReading.SearchingFromLucene;

public class TestIndexReading {
	
	public static void main(String args[])
	{
		String indexPath = "index";
		
		SearchingFromLucene searchingFromLucene = new SearchingFromLucene(indexPath);
		
		System.out.println("NO OF REVIEWS: " + searchingFromLucene.getNumberOfReviews());
		
		System.out.println("NO OF BUSINESSES: " + searchingFromLucene.getAllBusinessIds().size());
		
		System.out.println("NO OF BUSINESSES WHICH ARE RESTAURANT: " + searchingFromLucene.getCategoryCount("Restaurants")); //Should be 9
		
		System.out.println("Suck in review: " +searchingFromLucene.getDocumentCountContainingWord("suck"));
		
		System.out.println("Suck in review and category: " +searchingFromLucene.getWordCountForGivenCategoryAndWord("suck","Mass Media"));
		
		System.out.println("Get Business Name: " +searchingFromLucene.getBusinessName("zOc8lbjViUZajbY7M0aUCQ"));
		
		//printAList(searchingFromLucene.getAllBusinessIds());
	}
	
	public static void printAList(ArrayList<String> listObj)
	{
		for(String s : listObj)
		{
			System.out.println(s);
		}
		System.out.println();
	}

}
