package main.java.luceneAccess;

import com.google.common.primitives.Ints;
import main.java.constants.Constants;
import org.apache.lucene.document.Document;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.MultiFields;
import org.apache.lucene.index.Terms;
import org.apache.lucene.index.TermsEnum;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.TopDocs;
import org.apache.lucene.util.BytesRef;
import java.io.IOException;
import java.util.ArrayList;

/**
 * Created by adatar on 1/14/2016.
 */

public class SearchFromLuceneHelper {

    private IndexReader indexReader;
    private IndexSearcher indexSearcher;

    public SearchFromLuceneHelper() {
        indexReader = GetIndexReader.getIndexReader();
        indexSearcher = new IndexSearcher(indexReader);
    }

    protected long getTotalDocumentCount()
    {
        return indexReader.maxDoc();
    }

    protected TermsEnum getTermsEnumForField(String field) throws IOException {
        Terms vocabulary = MultiFields.getTerms(indexReader, field);
        return vocabulary.iterator(null);
    }

    protected int[] getAllDocumentsByTag(String tag) {

        ArrayList<Integer> documents = new ArrayList<>();
        for (int i = 0; i < indexReader.maxDoc(); i++) {
            Document doc;
            try {
                doc = indexReader.document(i);
                boolean isBusiness = doc.getField(Constants.TYPE).stringValue().equalsIgnoreCase(tag);
                if(isBusiness) documents.add(i);

            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return Ints.toArray(documents);
    }

    protected Document getDocumentById(int docId) {
        Document doc = null;

        try {
            doc = indexReader.document(docId);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return doc;
    }

    protected ArrayList<String> getFieldTermsFromDocument(int docId, String field) {
        ArrayList<String> words = new ArrayList<>();

        try {
            Terms terms = indexReader.getTermVector(docId, field);
            if (terms != null && terms.size() > 0) {

                TermsEnum termsEnum = terms.iterator(null);
                BytesRef term;

                while ((term = termsEnum.next()) != null) {
                    words.add(term.utf8ToString());
                }
            }

        }
        catch(IOException e) {
            e.printStackTrace();
        }
        return words;
    }

    protected long getDocumentVocabSize(int docId, String field) {
        long size = 0;
        Terms terms = getDocumentVocab(docId, field);

        try {
            size = terms.size();
        } catch (IOException e) {
            e.printStackTrace();
        }

        return size;
    }

    protected Terms getDocumentVocab(int docId, String field) {

        Terms terms = null;
        try {
            terms = this.indexReader.getTermVector(docId, field);
        }
        catch(IOException e) {
            e.printStackTrace();
        }
        return terms;
    }

    protected TopDocs queryIndex(Query query, int numTopHits) {
        TopDocs docs = null;

        try {
            docs = indexSearcher.search(query, numTopHits);
        } catch (IOException e) {e.printStackTrace();}

        return docs;
    }

    protected Document getDocument(int docId) {
        Document document = null;
        try {
            document = indexReader.document(docId);
        } catch (IOException e) {
            e.printStackTrace();
        }

        return document;
    }

    @Override
    protected void finalize() {
        try {
            indexReader.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

}
