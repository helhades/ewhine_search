import java.util.Date;


public class TestRubyTime {
	
	public static void main(String[] args) {
		int t = 1349579244;
		long tl = (long)t * 1000;
		Date d = new Date(tl);
		System.out.println(d);
		System.out.println("d long:" + d.getTime());
		
	}

}
