import net.sourceforge.pinyin4j.PinyinHelper;
import net.sourceforge.pinyin4j.format.HanyuPinyinCaseType;
import net.sourceforge.pinyin4j.format.HanyuPinyinOutputFormat;
import net.sourceforge.pinyin4j.format.HanyuPinyinToneType;
import net.sourceforge.pinyin4j.format.exception.BadHanyuPinyinOutputFormatCombination;


public class TestPinyin {
	public static void main(String[] args) {
		String inputString = "’≈Ô£";
		StringBuffer pybf = new StringBuffer(); 
        char[] arr = inputString.toCharArray(); 
        HanyuPinyinOutputFormat defaultFormat = new HanyuPinyinOutputFormat(); 
        defaultFormat.setCaseType(HanyuPinyinCaseType.LOWERCASE); 
        defaultFormat.setToneType(HanyuPinyinToneType.WITHOUT_TONE); 
        for (int i = 0; i < arr.length; i++) { 
                if (arr[i] > 128) { 
                        try { 
                                String[] _t = PinyinHelper.toHanyuPinyinStringArray(arr[i], defaultFormat); 
                                if (_t != null) { 
                                        pybf.append(_t[0].charAt(0)); 
                                } 
                        } catch (BadHanyuPinyinOutputFormatCombination e) { 
                                e.printStackTrace(); 
                        } 
                } else { 
                        pybf.append(arr[i]); 
                } 
        } 
        String out = pybf.toString().replaceAll("\\W", "").trim();
        System.out.println("out:" + out);
	}

}
