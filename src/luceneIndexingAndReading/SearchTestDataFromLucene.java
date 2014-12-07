package luceneIndexingAndReading;

import java.util.HashMap;

import org.apache.lucene.document.Document;

public class SearchTestDataFromLucene extends SearchFromLucene {

    public SearchTestDataFromLucene(String indexPath) {
        super(indexPath);
    }

    public HashMap<Integer, Document> getAllPredictions()
    {
        return this.getAllDocumentsByTag("prediction");
    }
}
