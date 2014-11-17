package dataPreprocessing;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.HashMap;

import luceneIndexingAndReading.IndexUsingLucene;


public abstract class ProcessDataFromJson {
	
	BufferedReader textReader;
	IndexUsingLucene indexUsingLucene;
	
	abstract void parseJson(String jsonLine);
	abstract public void indexUsingLucene(HashMap<String,String> fieldValuePairs);
	
	ProcessDataFromJson(String indexPath)
	{
		indexUsingLucene = new IndexUsingLucene(indexPath);	
	}
	
	public void closeLuceneIndex()
	{
		indexUsingLucene.closeLuceneLocks();
	}
		
	public void openLocation(String filePath)	
	{
		try
		{
			textReader = new BufferedReader(new FileReader(filePath));
		} 
		catch (IOException ioexception)
		{
			System.out.println("ERROR: File not found.");
		}
		
	}
		
	public void readLineAndParseJson() 
	{
		try
		{
			String line = textReader.readLine();
			
			while (line != null) 
			{
				parseJson(line);
				line = textReader.readLine();
			}
			
			textReader.close();
			closeLuceneIndex();
			
		} catch (IOException e)
		{
			e.printStackTrace();
		}
	}
	
}
