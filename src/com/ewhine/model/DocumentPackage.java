package com.ewhine.model;

import org.apache.lucene.document.Document;

import com.ewhine.redis.DocumentMessage;


public class DocumentPackage {	
	

	public static Document map(DocumentMessage qm) {
		int dtype = qm.getData_type();
		if (Message.type == dtype) {
			return Message.map(qm);
		}
		
		return null;
	}

}
