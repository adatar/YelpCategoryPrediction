package helper;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class TextFileReader {
	
	
	private static BufferedReader fileReader;
	
	
	public static List<String> readFile(String path){
		
		List<String> lineList = new ArrayList<>();
		openLocation(path);
		
		try {
			
			String line = fileReader.readLine(); 
			while(line != null){
				
				lineList.add(line);
				
			}
			
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		return lineList;
	}
	
	public static void openLocation(String filePath)	
	{
		try
		{
			fileReader = new BufferedReader(new FileReader(filePath));
			
		} 
		catch (IOException ioexception)
		{
			System.out.println("ERROR: File not found.");
		}
		System.out.println("OPENED");
	}

}
