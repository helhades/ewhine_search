package com.ewhine.profile;

import java.math.BigDecimal;
import java.util.List;

import cn.gov.cbrc.db.StoreManager;
import cn.gov.cbrc.db.TableClass;
import cn.gov.cbrc.db.annotation.Column;
import cn.gov.cbrc.db.annotation.Table;

@Table(name="user_search_profiles")
public class KeyWord implements Comparable {

	@Column(name="keyword")
	String word = null;
	@Column(name="weight")
	BigDecimal weight = BigDecimal.ZERO;
	int freq = 1;
	int term_id = 0;
	int t0 = 0;
	
	public KeyWord() {
	}
	
	public int getT0() {
		return t0;
	}
	public void setT0(int t0) {
		this.t0 = t0;
	}

	public KeyWord(String keyword) {
		this.word = keyword;
	}
	
	public void calculate() {
		
	}
	
	public void increaseFreq() {
		freq++;
	}
	
	public int getFreq() {
		return freq;
	}
	
	public String word() {
		return word;
	}
	
	public void setWeight(BigDecimal weight) {
		this.weight = weight;
	}
	
	public BigDecimal getWeight() {
		return weight;
	}
	
	@Override
	public String toString() {
	
		return "KeyWord:"+ this.word+",weight:" + this.weight;
	}

	@Override
	public int hashCode() {
		
		if (word != null)
			return word.hashCode();
		else
			return super.hashCode();
	}
	
	public static void main(String[] args) {
		TableClass<KeyWord> profileTable = StoreManager.open(KeyWord.class);
		List<KeyWord> words = profileTable.where("user_id",1);
		for (KeyWord kw : words) {
		System.out.println("keywords:" + kw);
		}
	}

	@Override
	public int compareTo(Object arg0) {
		KeyWord obj = (KeyWord)arg0;
		
		return weight.compareTo(obj.getWeight());
	}

}
