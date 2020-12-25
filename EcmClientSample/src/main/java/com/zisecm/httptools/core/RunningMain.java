package com.zisecm.httptools.core;

import java.util.HashMap;
import java.util.Map;

import com.zisecm.httptools.core.servcie.DocumentService;
/**
 * 测试样例
 * @author Atos
 *
 */
public class RunningMain {

	public static void main(String[] args) {
		
		DocumentService ds =  new DocumentService();
		//ds.queryDocument();
		ds.newDocument();
		//ds.addAttachments();
		//ds.newRelation();
	}

}
