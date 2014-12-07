package luceneIndexingAndReading;

public class SearchTestDataFromLucene extends SearchFromLucene {

    public SearchTestDataFromLucene(String indexPath) {
        super(indexPath);
    }

    public int[] getAllPredictions()
    {
        return this.getAllDocumentsByTag("prediction");
    }
}
