package com.ewhine.search;

import java.io.IOException;

import org.apache.lucene.search.FieldComparator;
import org.apache.lucene.search.FieldComparatorSource;

public class DocumentComparatorSource extends FieldComparatorSource {


	@Override
	public FieldComparator newComparator(String fieldName, int numHits, int sortPos,
			boolean reversed) throws IOException {
		
		return new DocumentScoreComarator(fieldName,numHits);
	}

}
