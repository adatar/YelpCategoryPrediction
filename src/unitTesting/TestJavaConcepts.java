package unitTesting;

public class TestJavaConcepts {

	public static void main(String[] args) {
		// TODO Auto-generated method stub
		
		String test = "American Food, Indo Chinese";
		String testarr[] = test.split(",");
		
		for(String s : testarr)
			System.out.println(s.trim());
		System.out.println(Math.log(0));

	}

}
