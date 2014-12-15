package outliers;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Properties;


import edu.stanford.nlp.ling.CoreAnnotations;
import edu.stanford.nlp.neural.rnn.RNNCoreAnnotations;
import edu.stanford.nlp.pipeline.Annotation;
import edu.stanford.nlp.pipeline.StanfordCoreNLP;
import edu.stanford.nlp.sentiment.SentimentCoreAnnotations;
import edu.stanford.nlp.util.CoreMap;

public class Outliers {
    BufferedWriter outlierFileWriter;
    StanfordCoreNLP pipeline;

    public Outliers(String filePath) {
        
        try {
            this.checkExistsOrCreate(filePath);
            this.outlierFileWriter = new BufferedWriter(new FileWriter(filePath));
            
            Properties props = new Properties();
            props.put("annotators", "tokenize, ssplit, pos, parse, sentiment");
            this.pipeline = new StanfordCoreNLP(props);
            
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void closeWriter() {
        try {
            this.outlierFileWriter.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void checkExistsOrCreate(String filePath) throws IOException {
        File indexDir = new File(filePath);
        if (!indexDir.exists()) {
            Path path = FileSystems.getDefault().getPath(filePath);
            Files.createFile(path);
        }
    }

    public void addOutlier(String reviewId) {
        try {
            outlierFileWriter.write(reviewId + "\n");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static boolean isOutlier(double sourceRating, double targetRating) {
        if (sourceRating == 3.0)
            return targetRating == 1.0 || targetRating == 5.0;
        else if (sourceRating > 3.0)
            return targetRating < sourceRating - 2;
        else
            return targetRating > sourceRating + 2;
    }
    
    public int annotateAndScore(String reviewText) {
        String[] SentimentText =
                        {"Very Negative", "Negative", "Neutral", "Positive", "Very Positive"};
        List<Integer> scores = new ArrayList<Integer>();

        Annotation annotation = new Annotation(reviewText);

        this.pipeline.annotate(annotation);
        for (CoreMap sentence : annotation.get(CoreAnnotations.SentencesAnnotation.class)) {

            edu.stanford.nlp.trees.Tree annotatedTree =
                            (edu.stanford.nlp.trees.Tree) sentence
                                            .get(SentimentCoreAnnotations.AnnotatedTree.class);
            int score = RNNCoreAnnotations.getPredictedClass(annotatedTree) + 1;
            scores.add(score);
        }
        return Collections.max(scores);

    }

    public boolean isOutlier(String reviewText, double businessRating) {
        return this.isOutlier(businessRating, this.annotateAndScore(reviewText));
    }
}
