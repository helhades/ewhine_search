package com.ewhine.search;

import java.io.IOException;
import java.util.List;

import org.apache.velocity.VelocityContext;

public interface ISearchResult {

	public abstract int getTotalDocs();

	public abstract List<String> getQuery_terms();
	public void setQuery_term(String termText);
	
	public void addToContext(VelocityContext context) throws IOException ;

}