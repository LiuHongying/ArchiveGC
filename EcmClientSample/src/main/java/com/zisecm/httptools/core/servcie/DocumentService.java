package com.zisecm.httptools.core.servcie;

import java.io.FileInputStream;
import java.io.InputStream;
import java.util.Map;

import org.apache.http.Consts;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.StatusLine;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.StringEntity;
import org.apache.http.entity.mime.HttpMultipartMode;
import org.apache.http.entity.mime.MultipartEntityBuilder;
import org.apache.http.entity.mime.content.ContentBody;
import org.apache.http.entity.mime.content.StringBody;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.zisecm.httptools.core.EcmHttp;
import com.zisecm.httptools.core.entity.EcmRelation;

/**
 * 文档访问接口
 * @author Atos
 *
 */
public class DocumentService {
	private EcmHttp ecmClient;
	public DocumentService() {
		ecmClient = new EcmHttp();
	}
	/**
	 * 查询文档
	 */
	public void queryDocument() {
		System.out.println("[METHOD]GetDocument");
		HttpPost httpPost=null;
		CloseableHttpClient httpClient = HttpClients.createDefault();
		
		String username = ecmClient.getConfig().getUsername();
		String password = ecmClient.getConfig().getPassword();
		//登录
		String token = ecmClient.login(username, password, httpClient);
		
		String url = ecmClient.getConfig().getBaseurl()+"/dc/getDocuments";
		httpPost=new HttpPost(url);
		try {
			RequestConfig timeoutConfig = RequestConfig.custom()
					.setConnectTimeout(5000).setConnectionRequestTimeout(1000)
					.setSocketTimeout(5000).build();
			httpPost.setConfig(timeoutConfig);
			JSONObject jsObj = new JSONObject();
			jsObj.put("pageSize", 20);
			jsObj.put("pageIndex", 1);
			jsObj.put("gridName", "RecyclebinGrid");
			jsObj.put("folderId", "");
			jsObj.put("condition", "");
			jsObj.put("orderBy", "");
			StringEntity strEntity=new StringEntity(jsObj.toJSONString());
			strEntity.setContentType("text/json");
			httpPost.addHeader("token", token);
			httpPost.setEntity(strEntity);
			
			HttpResponse response = httpClient.execute(httpPost);
			
			StatusLine statusLine = response.getStatusLine();
			int statusCode = statusLine.getStatusCode();
			// 请求成功
	        if (statusCode == 200) {
	        	HttpEntity responseResult = response.getEntity();
	        	String responseEntityStr = EntityUtils.toString(responseResult, "UTF-8");
	        	JSONObject jsonResult = JSON.parseObject(responseEntityStr);
	        	
	        	if(jsonResult.containsKey("code")) {
	        		int code = jsonResult.getInteger("code");
	        		if(code==1) {
	        			if(jsonResult.containsKey("data")) {
	        				System.out.println(jsonResult.get("data"));
	        			}
	        			if(jsonResult.containsKey("pager")) {
		        			System.out.println(jsonResult.get("pager"));
		        		}
	        		}
	        	}
	        	
	        }
		}catch (Exception e) {
			e.printStackTrace();
		}
		//登出
		ecmClient.logout(token, httpClient);
	}
	
	/**
	 * 新建文档
	 */
	public void newDocument() {
		System.out.println("[METHOD]createDocument");
		HttpPost httpPost=null;
		CloseableHttpClient httpClient = HttpClients.createDefault();
		
		String username = ecmClient.getConfig().getUsername();
		String password = ecmClient.getConfig().getPassword();
		String token = ecmClient.login(username, password, httpClient);
		
		String url = ecmClient.getConfig().getBaseurl()+"/dc/newDocument";
		httpPost=new HttpPost(url);
		try {
			RequestConfig timeoutConfig = RequestConfig.custom()
					.setConnectTimeout(5000).setConnectionRequestTimeout(1000)
					.setSocketTimeout(5000).build();
			httpPost.setConfig(timeoutConfig);
			// 使用表单形式提交参数
	        MultipartEntityBuilder fileBuilder = MultipartEntityBuilder.create();
	        fileBuilder.setMode(HttpMultipartMode.RFC6532);
	        InputStream stream = new FileInputStream("C:\\temp\\1.pdf");
			String fileName = "1.pdf";
			// file为提交参数名,stream为要上传的文件的文件流 inputStream，最后一个参数为上传文件名称
	        fileBuilder.addBinaryBody("uploadFile", stream, ContentType.create("multipart/form-data"), fileName);
	        JSONObject jsObj = new JSONObject();
			jsObj.put("TYPE_NAME", "移交单");
			jsObj.put("CODING", "TEST-009861");
			jsObj.put("FOLDER_ID", "f665a97d6986400481079c98f7183b33");
			jsObj.put("STATUS","新建");
			jsObj.put("C_COMMENT","测试移交单");
			StringBody metaData = new StringBody(jsObj.toString(), ContentType.create("text/plain", Consts.UTF_8));
			fileBuilder.addPart("metaData", metaData);
	        HttpEntity fileEntity = fileBuilder.build();
	        httpPost.addHeader("token", token);
	        httpPost.setEntity(fileEntity);
	        HttpResponse response = httpClient.execute(httpPost);
			StatusLine statusLine = response.getStatusLine();
	        // 响应码
	        int statusCode = statusLine.getStatusCode();
			// 请求成功
	        if (statusCode == 200) {
	        	HttpEntity responseResult = response.getEntity();
	        	String responseEntityStr = EntityUtils.toString(responseResult, "UTF-8");
	        	JSONObject jsonResult = JSON.parseObject(responseEntityStr);
	        	
	        	if(jsonResult.containsKey("code")) {
	        		int code = jsonResult.getInteger("code");
	        		if(code==1) {
	        			if(jsonResult.containsKey("id")) {
	        				System.out.println(jsonResult.get("id"));
	        			}
	        		}
	        	}
	        	
	        }
		}catch (Exception e) {
			e.printStackTrace();
		}
		//登出
		ecmClient.logout(token, httpClient);
	}
	/**
	 * 添加附件
	 */
	public void addAttachments() {
		System.out.println("[METHOD]addAttachments");
		HttpPost httpPost=null;
		CloseableHttpClient httpClient = HttpClients.createDefault();
		
		String username = ecmClient.getConfig().getUsername();
		String password = ecmClient.getConfig().getPassword();
		String token = ecmClient.login(username, password, httpClient);
		
		String url = ecmClient.getConfig().getBaseurl()+"/dc/addAttachment";
		httpPost=new HttpPost(url);
		try {
			RequestConfig timeoutConfig = RequestConfig.custom()
					.setConnectTimeout(5000).setConnectionRequestTimeout(1000)
					.setSocketTimeout(5000).build();
			httpPost.setConfig(timeoutConfig);
			// 使用表单形式提交参数
	        MultipartEntityBuilder fileBuilder = MultipartEntityBuilder.create();
	        fileBuilder.setMode(HttpMultipartMode.RFC6532);
	        InputStream stream = new FileInputStream("C:\\temp\\1.pdf");
			String fileName = "1.pdf";
			// file为提交参数名,stream为要上传的文件的文件流 inputStream，最后一个参数为上传文件名称
	        fileBuilder.addBinaryBody("uploadFile", stream, ContentType.create("multipart/form-data"), fileName);
	        JSONObject jsObj = new JSONObject();
			jsObj.put("parentDocId", "6eb192a485bd45e5b5de5ac73aaf5ba5");
			jsObj.put("TYPE_NAME", "附件");
			jsObj.put("relationName", "附件");
			StringBody metaData = new StringBody(jsObj.toString(), ContentType.create("text/plain", Consts.UTF_8));
			fileBuilder.addPart("metaData", metaData);
	        HttpEntity fileEntity = fileBuilder.build();
	        httpPost.addHeader("token", token);
	        httpPost.setEntity(fileEntity);
	        HttpResponse response = httpClient.execute(httpPost);
			StatusLine statusLine = response.getStatusLine();
	        // 响应码
	        int statusCode = statusLine.getStatusCode();
			// 请求成功
	        if (statusCode == 200) {
	        	HttpEntity responseResult = response.getEntity();
	        	String responseEntityStr = EntityUtils.toString(responseResult, "UTF-8");
	        	JSONObject jsonResult = JSON.parseObject(responseEntityStr);
	        	
	        	if(jsonResult.containsKey("code")) {
	        		int code = jsonResult.getInteger("code");
	        		if(code==1) {
	        			System.out.println("SUCCESS");
	        		}
	        	}
	        	
	        }
		}catch (Exception e) {
			e.printStackTrace();
		}
		//登出
		ecmClient.logout(token, httpClient);
	}
	/**
	 * 新建关联关系
	 */
	public void newRelation() {
		System.out.println("[METHOD]newRelation");
		HttpPost httpPost=null;
		CloseableHttpClient httpClient = HttpClients.createDefault();
		
		String username = ecmClient.getConfig().getUsername();
		String password = ecmClient.getConfig().getPassword();
		String token = ecmClient.login(username, password, httpClient);
		
		String url = ecmClient.getConfig().getBaseurl()+"/dc/createRelation";
		httpPost=new HttpPost(url);
		try {
			
			RequestConfig timeoutConfig = RequestConfig.custom()
					.setConnectTimeout(5000).setConnectionRequestTimeout(1000)
					.setSocketTimeout(5000).build();
			httpPost.setConfig(timeoutConfig);
			
	        JSONObject jsObj = new JSONObject();
			jsObj.put("childId", "86b26d3e6dfb457b8d433f43e8a5447f");
			jsObj.put("parentId","6eb192a485bd45e5b5de5ac73aaf5ba5");
			jsObj.put("name","111");
			StringEntity strEntity=new StringEntity(jsObj.toJSONString());
			httpPost.addHeader("Content-Type","application/json;charset=utf-8");
			httpPost.addHeader("token", token);
			httpPost.setEntity(strEntity);
			
			HttpResponse response = httpClient.execute(httpPost);
			
			StatusLine statusLine = response.getStatusLine();
			int statusCode = statusLine.getStatusCode();
			// 请求成功
	        if (statusCode == 200) {
	        	HttpEntity responseResult = response.getEntity();
	        	String responseEntityStr = EntityUtils.toString(responseResult, "UTF-8");
	        	JSONObject jsonResult = JSON.parseObject(responseEntityStr);
	        	if(jsonResult.containsKey("code")) {
	        		int code = jsonResult.getInteger("code");
	        		if(code==1) {
	        			System.out.println("ok");
	        		}else {
	        			if(jsonResult.containsKey("msg")) {
	        				System.out.println(jsonResult.get("msg"));
	        			}
	        		}
	        	}
	        	
	        }
		}catch (Exception e) {
			e.printStackTrace();
		}
		//登出
		ecmClient.logout(token, httpClient);
	}
}
