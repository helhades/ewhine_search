import java.math.BigDecimal;
import java.math.MathContext;


public class TestJavaPowerCalulate {
	
	public static void main(String[] args) {
		double doc_score =  0.93152386d;
		int doc_updated_at = 1349579244;
		long current_time = System.currentTimeMillis();
		
		
		System.out.println("curentlong:" + current_time);
		int passed_time = ((int)(current_time/1000) - doc_updated_at)/3600;
		MathContext mc = new MathContext(10);
		
		System.out.println("current time:" + (int)(current_time/1000));
		BigDecimal base = new BigDecimal(passed_time + 2);
		
		System.out.println("base:" + base);
		BigDecimal score = new BigDecimal(doc_score).movePointRight(20);
		System.out.println("f:" + score.multiply(base.pow(10),mc));
		BigDecimal out = score.multiply(base.pow(10)).divide(base.pow(18),mc);
		System.out.println("out:" + out.toPlainString());
	}

}
