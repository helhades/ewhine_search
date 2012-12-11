package com.ewhine.util;

import java.util.ArrayList;
import java.util.List;

import net.sourceforge.pinyin4j.PinyinHelper;
import net.sourceforge.pinyin4j.format.HanyuPinyinCaseType;
import net.sourceforge.pinyin4j.format.HanyuPinyinOutputFormat;
import net.sourceforge.pinyin4j.format.HanyuPinyinToneType;
import net.sourceforge.pinyin4j.format.HanyuPinyinVCharType;
import net.sourceforge.pinyin4j.format.exception.BadHanyuPinyinOutputFormatCombination;

public class Pinyin4j {

	/**
	 * 获取拼音集合
	 * 
	 * @author JimrokLiu
	 * @param src
	 * @return Set<String>
	 */
	public static List<String[]> ofString(String src) {

		if (src != null && src.length() > 0) {

			char[] srcChar = src.toCharArray();
			// 汉语拼音格式输出类
			HanyuPinyinOutputFormat hanYuPinOutputFormat = new HanyuPinyinOutputFormat();

			// 输出设置，大小写，音标方式等
			hanYuPinOutputFormat.setCaseType(HanyuPinyinCaseType.LOWERCASE);
			hanYuPinOutputFormat.setToneType(HanyuPinyinToneType.WITHOUT_TONE);
			hanYuPinOutputFormat.setVCharType(HanyuPinyinVCharType.WITH_V);

			List<String[]> temp = new ArrayList<String[]>();
			int extend_size = 1;
			StringBuilder english_string = new StringBuilder();
			for (int i = 0; i < srcChar.length; i++) {
				char c = srcChar[i];
				// 是中文或者a-z或者A-Z转换拼音(我的需求，是保留中文或者a-z或者A-Z)
				String[] output = null;
				if (String.valueOf(c).matches("[\\u4E00-\\u9FA5]+")) {
					try {
						output = PinyinHelper.toHanyuPinyinStringArray(
								srcChar[i], hanYuPinOutputFormat);

					} catch (BadHanyuPinyinOutputFormatCombination e) {
						System.err.println("Fatal output pinyin for:" + c);
					}

				} else if (((int) c >= 65 && (int) c <= 90)
						|| ((int) c >= 97 && (int) c <= 122)) {
					english_string.append(srcChar[i]);

				}

				if (output != null) {
					// process English string first.
					if (english_string.length() != 0) {
						temp.add(new String[] { english_string.toString() });
						english_string.delete(0, english_string.length());
					}
					// remove duplicate value.
					ArrayList<String> t = new ArrayList<String>();
					for (String each : output) {
						if (!t.contains(each)) {
							t.add(each);
						}
					}
					temp.add(t.toArray(new String[] {}));
					extend_size = extend_size * output.length;
				}
			}
			if (english_string.length() != 0) {
				temp.add(new String[] { english_string.toString() });
				english_string = null;
			}

			String[][] collector = new String[extend_size][temp.size()];
			int block_deep = 1;
			for (int i = 0, n = temp.size(); i < n; i++) {
				String[] each_word = temp.get(i);
				// copy block of string.
				if (each_word.length > 1 && i > 0) {

					int copy_deep = block_deep * (each_word.length - 1);
					for (int k = 0; k < copy_deep; k++) {
						for (int o = 0; o < i; o++) {
							collector[k + block_deep][o] = collector[k][o];
						}

					}

				}
				// write each block.
				for (int j = 0; j < each_word.length; j++) {
					for (int k = 0; k < block_deep; k++) {
						collector[k + j * block_deep][i] = each_word[j];
					}
				}
				block_deep = block_deep * each_word.length;

			}

			List<String[]> pinyin = new ArrayList<String[]>();
			for (int i = 0; i < block_deep; i++) {
				pinyin.add(collector[i]);
			}
			return pinyin;
		}
		return null;
	}

	public static String[] abbr(String input) {
		List<String[]> multi = ofString(input);
		String[] out = new String[multi.size()];
		for (int i = 0, n = multi.size(); i < n; i++) {
			String[] each = multi.get(i);
			StringBuilder mo = new StringBuilder();
			for (String e : each) {
				mo.append(e.charAt(0));

			}
			out[i] = mo.toString();
		}

		return out;

	}

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		String str = "刘江行";
		String[] out = Pinyin4j.abbr(str);
		System.out.print("[");
		int c = 0;
		for (String e : out) {
			if (c > 0) {
				System.out.print(",");
			}
			c++;
			System.out.print(e);
		}
		System.out.println("]");
		
		List<String[]> multi = ofString(str);
		for (String[] each : multi) {
			System.out.print("[");
			int i = 0;
			for (String e : each) {
				if (i > 0) {
					System.out.print(",");
				}
				i++;
				System.out.print(e);
			}
			System.out.println("]");
		}

		// System.out.println(ofString(str));
		// System.out.println(Pinyin4j.ofStringSet(ofString(str)));

	}
}
