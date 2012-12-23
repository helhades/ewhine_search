import java.math.BigDecimal;


public class TestNumberComputing {

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		BigDecimal base = new BigDecimal("2349003244455");
		BigDecimal exp = new BigDecimal("1.2");
		BigDecimal out = base.pow(1);
		
		System.out.println("out:" + out.toPlainString());

	}

}
