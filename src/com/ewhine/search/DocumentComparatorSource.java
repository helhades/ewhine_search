package com.ewhine.search;

import java.io.IOException;
import java.math.BigDecimal;

import org.apache.lucene.search.FieldComparator;
import org.apache.lucene.search.FieldComparatorSource;

public class DocumentComparatorSource extends FieldComparatorSource {


	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	@Override
	public FieldComparator<BigDecimal> newComparator(String fieldName, int numHits, int sortPos,
			boolean reversed) throws IOException {
		
		return new DocumentScoreComarator(fieldName,numHits);
	}

}
