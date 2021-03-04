package com.zisecm.httptools.core;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;

import com.alibaba.fastjson.JSONObject;
import com.zisecm.httptools.core.servcie.DocumentService;
/**
 * 测试样例
 * @author Atos
 *
 */
public class RunningMain {

	public static void main(String[] args) throws FileNotFoundException {
		
		String folderId = "f665a97d6986400481079c98f7183b33";
		
		EcmHttp ecmClient = new EcmHttp();
		CloseableHttpClient httpClient = HttpClients.createDefault();
		DocumentService ds =  new DocumentService();
		String username = ecmClient.getConfig().getUsername();
		String password = ecmClient.getConfig().getPassword();
		String token = ecmClient.login(username, password, httpClient);
		
		//String condition="SYNC_ID='"+tcId+"'";
		String condition="ID='a5f69c6a59e14e759adc24689a3da31b'";
		ds.queryDocument(httpClient, token, ecmClient, condition);
		
		InputStream stream = new FileInputStream("c:\\temp\\1.pdf");
		
		JSONObject jsObj = new JSONObject();
		//系统属性
		jsObj.put("ID", "a5f69c6a59e14e759adc24689a3da31b");
		// 业务属性
	    jsObj.put("CODING", "TEST-009862111");
		jsObj.put("C_COMMENT","测试移交单1111");
		
		ds.upateDocument(httpClient, token, ecmClient, jsObj, stream, "测试文件.pdf");
		
		
		/*
		
		InputStream stream = new FileInputStream("c:\\temp\\1.pdf");
		//创建移交单
		JSONObject jsObj = new JSONObject();
		//系统属性
		jsObj.put("TYPE_NAME", "移交单");
		jsObj.put("FOLDER_ID", folderId);
		jsObj.put("STATUS","新建");
		// 业务属性
		jsObj.put("CODING", "TEST-009862");
		jsObj.put("C_COMMENT","测试移交单");
		//没有电子文件stream和fileName可以为null
		String delivryId = ds.newDocument(httpClient,token,ecmClient,jsObj,stream,"传递单.pdf");
		
		// 添加附件，可以多个
		stream = new FileInputStream("c:\\temp\\1-1.pdf");
		ds.addAttachments(httpClient, token, ecmClient, stream, "附件1.pdf", delivryId);
				
		//创建设计文件1
		jsObj = new JSONObject();
		//系统属性
		jsObj.put("TYPE_NAME", "设计文件");
		jsObj.put("FOLDER_ID", folderId);
		jsObj.put("STATUS","新建");
		// 业务属性
		jsObj.put("CODING", "DES-0000003");
		jsObj.put("REVISION", "A");
		jsObj.put("TITLE", "测试设计文件1");
		//添加其他属性
		
		stream = new FileInputStream("c:\\temp\\2.pdf");
		
		String designId = ds.newDocument(httpClient,token,ecmClient,jsObj,stream,"设计文件1.pdf");
		//添加移交单和设计文件关系
		ds.newRelation(httpClient, token, ecmClient, delivryId, designId, "irel_children");
		// 添加附件，可以多个
		stream = new FileInputStream("c:\\temp\\2-1.pdf");
		ds.addAttachments(httpClient, token, ecmClient, stream, "附件1.pdf", designId);
		
		// 创建设计文件2
		jsObj = new JSONObject();
		jsObj.put("TYPE_NAME", "设计文件");
		jsObj.put("CODING", "DES-0000004");
		jsObj.put("REVISION", "B");
		jsObj.put("TITLE", "测试设计文件2");
		jsObj.put("FOLDER_ID", folderId);
		jsObj.put("STATUS","新建");
		//添加其他属性
		
		stream = new FileInputStream("c:\\temp\\3.pdf");
		
		designId = ds.newDocument(httpClient,token,ecmClient,jsObj,stream,"设计文件2.pdf");
		
		ds.newRelation(httpClient, token, ecmClient, delivryId, designId, "irel_children");
		
		stream = new FileInputStream("c:\\temp\\3-1.pdf");
		ds.addAttachments(httpClient, token, ecmClient, stream, "附件1.pdf", designId);
		//登出
		ecmClient.logout(token, httpClient);
		*/
	}

}
