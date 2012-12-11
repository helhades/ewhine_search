import java.math.BigDecimal;

import com.ewhine.util.BigDecimalMath;


public class TestNumberComputing {

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		BigDecimal base = new BigDecimal("2349003244455");
		BigDecimal exp = new BigDecimal("1.2");
		BigDecimal out = BigDecimalMath.pow(base, exp);
		
		System.out.println("out:" + out.toPlainString());

	}

}
