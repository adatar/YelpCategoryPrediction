package unitTesting;

import outliers.Outliers;
import outliers.OutliersEvaluation;

public class TestOutliers {

    public static void main(String[] args) {
//        Outliers o = new Outliers("outliers1.txt");
//
//        // business rating : 3.5, review rating by user: 5.0 
//        String review1 = "dr. goldberg offers everything i look for in a general practitioner.  he's nice and easy to talk to without being patronizing; he's always on time in seeing his patients; he's affiliated with a top-notch hospital (nyu) which my parents have explained to me is very important in case something happens and you need surgery; and you can get referrals to see specialists without having to see him first.  really, what more do you need?  i'm sitting here trying to think of any complaints i have about him, but i'm really drawing a blank.";
//        
//        // business rating : 5.0 , review rating by user: 5.0
//        String review2 = "I have been seeing Dr. Milana for about 7 years and have referred many friends. He is funny, kind, and very knowledgeable in his field. I found him thru his wife who I had once worked with. And I love them both!  The check-ups have always been enjoyable. He has replaced numerous old fillings and even put in a couple of crowns and I've never had a problem and little discomfort for those procedures. He has been advising me for the past 4 years when i have my check-ups to have my wisdom teeth removed as they weren't taken out when I was a teenager and now are beginning to cause issues. Well, I have put off doing it for all his time because I was afraid and had heard horror stories about this procedure in general. I finally had it done TODAY and it was SOOOO much easier than I thought it would be!! A complete non-issue. Cant believe i was so silly and put it off so long!! And that was all 4 wisdom teeth! I have NO swelling and so far , not even soreness. \n\nCan't say enough good things about Dr. Milana and Kelly, his receptionist. Kelly is terrific! They will help you no matter what's wrong. His dental hygienists have always been terrific as well. And the office is easy to get to at 42nd & Indian school. They are easy to work with re: insurance. Go see them yourself!";
//        
//        // stanford rating : 4
//        System.out.println(o.isOutlier(review1, 3.5));
//        
//        // stanford rating : 5
//        System.out.println(o.isOutlier(review2, 5.0));
//        
//        o.addOutlier("outlier1");
//        o.addOutlier("outlier2");
//        o.addOutlier("outlier3");
//        o.closeWriter();
        
        OutliersEvaluation oe = new OutliersEvaluation("outliers.txt", "index");
        oe.evaluateOutliers();
    }

}
