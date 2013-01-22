package com.ewhine.search;

import proj.zoie.api.ZoieException;

public interface ISearchModel {
	
	ISearchResult search(long user_id, String queryString, String type_id) throws ZoieException;

}
