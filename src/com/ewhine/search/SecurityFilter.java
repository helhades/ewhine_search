package com.ewhine.search;

import java.io.IOException;
import java.util.List;

import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.Term;
import org.apache.lucene.index.TermDocs;
import org.apache.lucene.search.DocIdSet;
import org.apache.lucene.search.Filter;
import org.apache.lucene.util.NumericUtils;
import org.apache.lucene.util.OpenBitSet;

import com.ewhine.model.Group;

public class SecurityFilter extends Filter {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private List<Group> groups;

	public SecurityFilter(List<Group> u_groups) {
		this.groups = u_groups;

	}

	@Override
	public DocIdSet getDocIdSet(IndexReader reader) throws IOException {
		OpenBitSet bits = new OpenBitSet(reader.maxDoc());
		//System.out.println("get doc group:" + group.getId());
		//long[] digital = FieldCache.DEFAULT.getLongs(reader,"g_id");
		
		for (Group group : groups) {
			TermDocs termDocs = reader.termDocs(new Term("g_id", NumericUtils
					.longToPrefixCoded(group.getId())));
			while(termDocs.next()) {
				bits.set(termDocs.doc());
			}
		}

		return bits;

	}

}
