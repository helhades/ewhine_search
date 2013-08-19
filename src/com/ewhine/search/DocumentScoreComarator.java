package com.ewhine.search;

import java.io.IOException;
import java.math.BigDecimal;
import java.math.MathContext;

import org.apache.log4j.Logger;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.search.FieldCache;
import org.apache.lucene.search.FieldComparator;
import org.apache.lucene.search.ScoreCachingWrappingScorer;
import org.apache.lucene.search.Scorer;

public class DocumentScoreComarator extends FieldComparator<BigDecimal> {
	
	private static final Logger log = Logger
			.getLogger(DocumentScoreComarator.class);
	private String fieldName;
	private BigDecimal[] values;
	private int[] docUpdated;
	private long[] doc_id;
	private Scorer scorer;
	private BigDecimal bottom;

	public DocumentScoreComarator(String fieldName, int numHits) {
		this.values = new BigDecimal[numHits];
		this.fieldName = fieldName;

	}

	@Override
	public int compare(int slot1, int slot2) {
		return values[slot2].compareTo(values[slot1]);
	}

	@Override
	public int compareBottom(int doc) throws IOException {
		BigDecimal docScore = getComparatorScore(doc);
		return bottom.compareTo(docScore);
	}

	@Override
	public void copy(int slot, int doc) throws IOException {

		values[slot] = getComparatorScore(doc);

	}

	@Override
	public void setBottom(int slot) {
		bottom = values[slot];
	}

	@Override
	public void setNextReader(IndexReader reader, int docBase)
			throws IOException {
		docUpdated = FieldCache.DEFAULT.getInts(reader, fieldName);
		doc_id = FieldCache.DEFAULT.getLongs(reader, "o_id");
	}

	@Override
	public void setScorer(Scorer scorer) {
		this.scorer = new ScoreCachingWrappingScorer(scorer);
	}

	@Override
	public BigDecimal value(int slot) {
		return values[slot];
	}

	private BigDecimal getComparatorScore(int doc) {
		try {
			float doc_score = scorer.score();

			int doc_updated_at = docUpdated[doc];
			long current_time = System.currentTimeMillis();

			int passed_time = ((int) (current_time / 1000) - doc_updated_at) / 3600;
			MathContext mc = new MathContext(10);
			BigDecimal base = new BigDecimal(passed_time + 2);
			BigDecimal score = new BigDecimal(doc_score);
			BigDecimal base_pow = base.multiply(new BigDecimal(Math.pow(
					base.doubleValue(), 0.8))); // b^1.8 = b*b^0.8
			BigDecimal out = score.divide(base_pow, mc);
			// System.out.println("doc:" + doc_id[doc] +",score" +
			// out.toPlainString()+",updated_at:" + doc_updated_at);
			if (log.isInfoEnabled()) {
				StringBuilder sb = new StringBuilder("qid:3217,");
				sb.append("doc_id:");
				sb.append(doc_id[doc]);
				sb.append(",tf-idf:");
				sb.append(doc_score);
				sb.append(",doc_socore:");
				sb.append(out.multiply(new BigDecimal(10000)).toPlainString());
				log.info(sb.toString());
			}
			return out;

		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return BigDecimal.ZERO;
	}

}
