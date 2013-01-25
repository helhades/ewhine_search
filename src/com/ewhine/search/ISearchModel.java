package com.ewhine.search;

import proj.zoie.api.ZoieException;

public interface ISearchModel {
	/*
	 * 
	 */
	ISearchResult search(long user_id, String queryString, String type_name, int i_page_size, int i_page) throws ZoieException;

}
