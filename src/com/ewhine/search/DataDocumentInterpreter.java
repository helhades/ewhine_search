package com.ewhine.search;

import proj.zoie.api.indexing.ZoieIndexable;
import proj.zoie.api.indexing.ZoieIndexableInterpreter;

public class DataDocumentInterpreter implements ZoieIndexableInterpreter<DataDocument> {

	@Override
	public ZoieIndexable convertAndInterpret(DataDocument src) {
		return src;
	}
}