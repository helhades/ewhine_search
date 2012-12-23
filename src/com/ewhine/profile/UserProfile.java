package com.ewhine.profile;

import java.io.IOException;
import java.io.StringReader;
import java.math.BigDecimal;
import java.math.MathContext;
import java.math.RoundingMode;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

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
	int keep_size = 30;

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
		MathContext mc = new MathContext(10,RoundingMode.HALF_UP);
		BigDecimal base = new BigDecimal(Math.pow(Math.E, 0.2),mc);
		BigDecimal w0 = new BigDecimal("0.5",mc);

		for (String k : keysets.keySet()) {

			KeyWord keyword = keysets.get(k);

			int freq = keyword.getFreq();
			BigDecimal weight = w0.multiply(new BigDecimal(freq,mc)).divide(
					new BigDecimal(key_count,mc),10, RoundingMode.HALF_UP);

			if (!profile_keysets.containsKey(k)) {

				keyword.setT0(t);
				keyword.setWeight(weight);
				profile_keysets.put(k, keyword);
				// System.out.println("-->keyword" + keyword);

			} else {
				KeyWord old = profile_keysets.get(k);
				BigDecimal old_weight = old.getWeight();
				int t0 = old.getT0();

				BigDecimal out = old_weight.multiply((BigDecimal.ONE.divide(base, 10,RoundingMode.HALF_UP)).pow(t-t0),mc); // e 0.2
				BigDecimal wt = out.add(weight,mc);

				old.setWeight(wt);
			}

//			if (k.equals("em")) {
//				System.err.println("created_at:" + created_at);
//				System.err.println("t:" + t);
//				int t0 = profile_keysets.get(k).getT0();
//				System.err.println("t0:" + profile_keysets.get(k).getT0());
//				System.err.println("ex pow10:" + base.pow(10, mc) );
//				System.err.println("ex:" + base.pow(2 * (t - t0)));
//				System.err.println("weight:"
//						+ profile_keysets.get(k).getWeight());
//			}
		}

		if (profile_keysets.size() > keep_size) {
			dropLastLightWord();
		}

	}

	private void dropLastLightWord() {
		
		int size_of_profile = profile_keysets.size();
		int drop_size = size_of_profile - this.keep_size;
		KeyWord[] all_sorted = new KeyWord[size_of_profile];
		Collection<KeyWord> all = profile_keysets.values();
		all.toArray(all_sorted);
		Arrays.sort(all_sorted, new Comparator<KeyWord>() {

			@Override
			public int compare(KeyWord a, KeyWord b) {
				return a.weight.compareTo(b.weight);
			}
			
		});
		for (int i=0;i<drop_size;i++ ) {
			profile_keysets.remove(all_sorted[i].word());
			System.err.println("drop:" + all_sorted[i].word()+",w:" + all_sorted[i].weight);
		}

		

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
		ArrayList<KeyWord> alist = new ArrayList<KeyWord>(kwsets);
		Collections.sort(alist,new Comparator<KeyWord>() {

			@Override
			public int compare(KeyWord a, KeyWord b) {
				// From big to small number.
				return b.getWeight().compareTo(a.getWeight());
			}
			
		});

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
		UserProfile herry = UserProfile.find_by_user_id(5);
		List<Activity> activities = Activity.find_by_user_id(5);
		int i = 0;
		for (Activity activity : activities) {
			i++;
			//if (i>19) break;
			// if (activity.getId() == 424 ||activity.getId() == 425 )
			herry.training(activity);
		}
		
		UserProfile jimrok_profile = UserProfile.find_by_user_id(1);
		activities = Activity.find_by_user_id(1);
		
		for (Activity activity : activities) {
			i++;
			//if (i>19) break;
			// if (activity.getId() == 424 ||activity.getId() == 425 )
			jimrok_profile.training(activity);
		}
		
		HashSet<String> keys = new HashSet<String>();
		for (String k : herry.profile_keysets.keySet()) {
			if (!keys.contains(k)) {
				keys.add(k);
			}
		}
		for (String k : jimrok_profile.profile_keysets.keySet()) {
			if (!keys.contains(k)) {
				keys.add(k);
			}
		}
		for (String k : keys) {
			KeyWord h_kw = herry.profile_keysets.get(k);
			KeyWord j_kw = jimrok_profile.profile_keysets.get(k);
			BigDecimal j_w = (j_kw == null ? BigDecimal.ZERO : j_kw.getWeight()) ;
			BigDecimal h_w = (h_kw == null ? BigDecimal.ZERO : h_kw.getWeight()) ;
			System.out.println(k+","+ h_w + "," + j_w);
		}

		//System.out.println("profile:" + jimrok_profile.toString());
	}

}
