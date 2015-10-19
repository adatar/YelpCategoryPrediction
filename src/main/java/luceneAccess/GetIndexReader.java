package main.java.luceneAccess;

import java.io.File;
import java.io.IOException;

import main.java.constants.Constants;

import org.apache.lucene.index.DirectoryReader;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.store.FSDirectory;

public class GetIndexReader {
	
	private static IndexReader indexReader = null;
	
	public static IndexReader getIndexReader(){
		
		if(indexReader == null){	
			try {
				
				indexReader = DirectoryReader.open(FSDirectory.open(new File(Constants.trainIndexPath)));
				
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		
		return indexReader;
		
	}
	
}
