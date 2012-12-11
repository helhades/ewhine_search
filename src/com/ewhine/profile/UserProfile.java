package com.ewhine.profile;

import java.io.IOException;
import java.io.StringReader;
import java.math.BigDecimal;
import java.math.MathContext;
import java.math.RoundingMode;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;

import org.apache.lucene.analysis.TokenStream;
import org.apache.lucene.analysis.tokenattributes.CharTermAttribute;
import org.wltea.analyzer.lucene.IKAnalyzer;

import cn.gov.cbrc.db.StoreManager;
import cn.gov.cbrc.db.TableClass;

import com.ewhine.model.Action;
import com.ewhine.model.ActionType;
import com.ewhine.model.Activity;
import com.ewhine.model.Message;

public class UserProfile {

	HashMap<String, KeyWord> profile_keysets = new HashMap<String, KeyWord>();
	int keep_size = 60;

	public UserProfile(List<KeyWord> keys) {
		for (KeyWord kw : keys) {
			profile_keysets.put(kw.word(), kw);
		}
	}

	public void training(Activity activity) {

		// User u = User.find_by_id(activity.getUser_id());
		Action action = activity.getAction();
		if (action.getTarget_type_id() == ActionType.REPLY_MESSSAGE) {
			Message msg = Message.find_by_id(action.getTarget_id());
			String msgText = msg.getRich();
			System.out.println("activity:" + activity.getId() + "msg["
					+ msg.getId() + "]:" + msgText);
			KeyWord[] keywords = analysis(msgText);
			updateProfile(keywords, activity.getCreated_at());

		}

	}

	private void updateProfile(KeyWord[] keywords, Timestamp created_at) {
		HashMap<String, KeyWord> keysets = new HashMap<String, KeyWord>();

		for (KeyWord kw : keywords) {

			KeyWord keyword = keysets.get(kw.word());
			if (keyword == null) {
				keysets.put(kw.word(), kw);
			} else {
				keyword.increaseFreq();
			}
		}
		int key_count = keywords.length;
		int t = (int) (created_at.getTime() / 3600000);
		MathContext mc = new MathContext(10);
		BigDecimal base = new BigDecimal(Math.pow(Math.E, 0.2));
		BigDecimal w0 = new BigDecimal("0.5");

		for (String k : keysets.keySet()) {

			KeyWord keyword = keysets.get(k);

			int freq = keyword.getFreq();
			BigDecimal weight = w0.multiply(new BigDecimal(freq)).divide(
					new BigDecimal(key_count), mc);

			if (!profile_keysets.containsKey(k)) {

				keyword.setT0(t);
				keyword.setWeight(weight);
				profile_keysets.put(k, keyword);
				// System.out.println("-->keyword" + keyword);

			} else {
				KeyWord old = profile_keysets.get(k);
				BigDecimal old_weight = old.getWeight();
				int t0 = old.getT0();

				BigDecimal out = old_weight.multiply((BigDecimal.ONE.divide(base, 10,RoundingMode.HALF_UP)).pow(t-t0)); // e 0.2

				BigDecimal wt = out.add(weight);

				old.setWeight(wt);
			}

			if (k.equals("em")) {
				System.err.println("created_at:" + created_at);
				System.err.println("t:" + t);
				int t0 = profile_keysets.get(k).getT0();
				System.err.println("t0:" + profile_keysets.get(k).getT0());
				System.err.println("ex pow10:" + base.pow(10, mc) );
				System.err.println("ex:" + base.pow(2 * (t - t0)));
				System.err.println("weight:"
						+ profile_keysets.get(k).getWeight());
			}
		}

		if (profile_keysets.size() > keep_size) {
			dropLastLightWord();
		}

	}

	private void dropLastLightWord() {
		String dropKey = null;
		BigDecimal minWeight = new BigDecimal(Integer.MAX_VALUE);
		for (String k : profile_keysets.keySet()) {
			KeyWord keyword = profile_keysets.get(k);
			BigDecimal weight = keyword.getWeight();
			if (weight.compareTo(minWeight) == -1) {

				minWeight = weight;
				dropKey = k;
			}
		}

		profile_keysets.remove(dropKey);

	}

	private static UserProfile find_by_user_id(long user_id) {
		TableClass<KeyWord> profileTable = StoreManager.open(KeyWord.class);
		List<KeyWord> words = profileTable.where("user_id", 1);

		return new UserProfile(words);
	}

	public KeyWord[] analysis(String text) {
		HashMap<String, KeyWord> keys = new HashMap<String, KeyWord>();

		IKAnalyzer analyzer = new IKAnalyzer(true);

		TokenStream ts = analyzer.tokenStream("text", new StringReader(text));

		try {
			while (ts.incrementToken()) {
				String key = ts.getAttribute(CharTermAttribute.class)
						.toString();
				if (key.equals("em")) {
					System.err.println("em:" + text);
				}

				KeyWord o = keys.get(key);
				if (o == null) {
					keys.put(key, new KeyWord(key));
				} else {
					o.increaseFreq();
				}

			}
		} catch (IOException ioe) {
			ioe.printStackTrace();
		} finally {
			if (analyzer != null) {
				analyzer.close();
			}
		}

		return keys.values().toArray(new KeyWord[keys.size()]);

	}

	@Override
	public String toString() {

		Collection<KeyWord> kwsets = profile_keysets.values();
		ArrayList<KeyWord> alist = new ArrayList(kwsets);
		Collections.sort(alist);

		StringBuilder sb = new StringBuilder();
		sb.append("Size:" + profile_keysets.size());

		for (KeyWord kw : alist) {
			sb.append("[").append(kw.word).append(",")
					.append(kw.weight.toPlainString()).append("]");
			sb.append("\n");
		}
		return sb.toString();
	}

	public static void main(String[] args) {
		UserProfile jimrok_profile = UserProfile.find_by_user_id(1);
		List<Activity> activities = Activity.find_by_user_id(1);
		int i = 0;
		for (Activity activity : activities) {
			i++;
			// if (i>19) break;
			// if (activity.getId() == 424 ||activity.getId() == 425 )
			jimrok_profile.training(activity);
		}

		System.out.println("profile:" + jimrok_profile.toString());
	}

}
