package unitTesting;

import java.util.HashMap;

public class TestJavaConcepts {

	public static void main(String[] args) {
		// TODO Auto-generated method stub
		
		
		/*
		String test = "American Food, Indo Chinese";
		String testarr[] = test.split(",");
		
		for(String s : testarr)
			System.out.println(s.trim());
		System.out.println(Math.log(0));
		
		*/
		
		
		HashMap<String, String> thm = new HashMap<>();
		thm.put("A","1");
		
		
		System.out.println(thm.get("A"));
		System.out.println(thm.get("B"));

	}

}
