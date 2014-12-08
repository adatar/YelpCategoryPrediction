package unitTesting;

import java.util.ArrayList;

import org.apache.lucene.document.Document;

import luceneIndexingAndReading.SearchFromLucene;
import luceneIndexingAndReading.SearchTestDataFromLucene;

public class TestIndexReading {
	
	public static void main(String args[])
	{
		String indexPath = "trainingIndex";
		
		SearchFromLucene searchingFromLucene = new SearchFromLucene(indexPath);
		
		System.out.println("NO OF BUSINESSES: " + searchingFromLucene.getTotalDocumentCount());
		
		System.out.println("NO OF BUSINESSES WHICH ARE RESTAURANT: " + searchingFromLucene.getCategoryCount("Restaurants")); //Should be 9
		
		System.out.println("Suck in review: " +searchingFromLucene.getDocumentCountContainingWord("suck"));
		
		System.out.println("Suck in review and category: " +searchingFromLucene.getWordCountForGivenCategoryAndWord("suck","Mass Media"));
		
		System.out.println("Get Business Name: " +searchingFromLucene.getBusinessName("zOc8lbjViUZajbY7M0aUCQ"));

		for(int docId : searchingFromLucene.getAllBusinessDocuments()){
		    System.out.println(searchingFromLucene.getDocumentById(docId));
		}
		System.out.println("All categories: ");
		printAList(searchingFromLucene.getAllCategories());
	
		System.out.println("\nPREDICTION FOR TEST BUSINESSES:\n");
		int count = 0;
		String testIndexPath = "testIndex";
        SearchTestDataFromLucene searchTestData = new SearchTestDataFromLucene(testIndexPath);
        for(int docId : searchTestData.getAllPredictions())
        {
            if(count > 10)
                break;
            Document doc = searchTestData.getDocumentById(docId);
            String businessId = doc.get("businessId");
            double precision = Double.parseDouble(doc.get("precision"));
            double recall = Double.parseDouble(doc.get("recall"));
            String predictedCategories = doc.get("predictedCategories");
            String businessCategories = doc.get("businessCategories");
            System.out.println("BUSINESS NAME : " + searchTestData.getBusinessName(businessId)
            + "\tPREDICTED: (" + predictedCategories + ")\tACTUAL : (" + businessCategories + ")\tPRECISION : " + precision + "\tRECALL : " + recall);
        }

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
