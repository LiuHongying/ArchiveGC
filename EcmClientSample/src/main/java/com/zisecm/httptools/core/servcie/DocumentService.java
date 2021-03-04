package com.zisecm.httptools.core.servcie;

import java.io.FileInputStream;
import java.io.InputStream;
import java.util.List;
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

	public DocumentService() {
		
	}
	/**
	 * 查询文档
	 */
	public void queryDocument(CloseableHttpClient httpClient,String token,EcmHttp ecmClient,String condition) {
		System.out.println("[METHOD]GetDocument");
		HttpPost httpPost=null;
		
		String url = ecmClient.getConfig().getBaseurl()+"/dc/getDocuments";
		httpPost=new HttpPost(url);
		try {
			RequestConfig timeoutConfig = RequestConfig.custom()
					.setConnectTimeout(5000).setConnectionRequestTimeout(1000)
					.setSocketTimeout(5000).build();
			httpPost.setConfig(timeoutConfig);
			JSONObject jsObj = new JSONObject();
			jsObj.put("pageSize", 20);
			jsObj.put("pageIndex", 0);
			jsObj.put("gridName", "GeneralGrid");
			jsObj.put("folderId", "");
			jsObj.put("condition", condition);
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
	}
	
	public void deleteDocument(CloseableHttpClient httpClient,String token,EcmHttp ecmClient,List<String> ids) {
		System.out.println("[METHOD]deleteDocument");
		HttpPost httpPost=null;
		
		String url = ecmClient.getConfig().getBaseurl()+"/dc/delDocument";
		httpPost=new HttpPost(url);
		try {
			RequestConfig timeoutConfig = RequestConfig.custom()
					.setConnectTimeout(5000).setConnectionRequestTimeout(1000)
					.setSocketTimeout(5000).build();
			httpPost.setConfig(timeoutConfig);

			StringEntity strEntity=new StringEntity(JSONObject.toJSONString(ids));
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
	        			System.out.println("删除成功！");
	        		}else {
	        			System.out.println(jsonResult.getString("message"));
	        		}
	        	}
	        	
	        }
		}catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	/**
	 * 
	 * @param httpClient 连接
	 * @param token 
	 * @param ecmClient 配置
	 * @param jsObj 元数据
	 * @param stream 电子文件
	 * @param fileName 电子文件名
	 * @return
	 */
	public String newDocument(CloseableHttpClient httpClient,String token,EcmHttp ecmClient,JSONObject jsObj,InputStream stream,String fileName) {
		System.out.println("[METHOD]createDocument");
		HttpPost httpPost=null;
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
	        
			// file为提交参数名,stream为要上传的文件的文件流 inputStream，最后一个参数为上传文件名称
			if(stream!=null) {
				fileBuilder.addBinaryBody("uploadFile", stream, ContentType.create("multipart/form-data"), fileName);
			}
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
	        				return jsonResult.getString("id");
	        			}
	        		}
	        	}
	        	
	        }
		}catch (Exception e) {
			e.printStackTrace();
		}
		return null;
	}
	
	/**
	 * 更新文档
	 * @param httpClient
	 * @param token
	 * @param ecmClient
	 * @param jsObj ID必需，其他属性添加需要更新的即可
	 * @param stream
	 * @param fileName
	 * @return
	 */
	public String upateDocument(CloseableHttpClient httpClient,String token,EcmHttp ecmClient,JSONObject jsObj,InputStream stream,String fileName) {
		System.out.println("[METHOD]upateDocument");
		HttpPost httpPost=null;
		String url = ecmClient.getConfig().getBaseurl()+"/dc/createOrUpdateDoc";
		httpPost=new HttpPost(url);
		try {
			RequestConfig timeoutConfig = RequestConfig.custom()
					.setConnectTimeout(5000).setConnectionRequestTimeout(1000)
					.setSocketTimeout(5000).build();
			httpPost.setConfig(timeoutConfig);
			// 使用表单形式提交参数
	        MultipartEntityBuilder fileBuilder = MultipartEntityBuilder.create();
	        fileBuilder.setMode(HttpMultipartMode.RFC6532);
	        
			// file为提交参数名,stream为要上传的文件的文件流 inputStream，最后一个参数为上传文件名称
			if(stream!=null) {
				fileBuilder.addBinaryBody("uploadFile", stream, ContentType.create("multipart/form-data"), fileName);
			}
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
	        				return jsonResult.getString("id");
	        			}
	        		}
	        	}
	        	
	        }
		}catch (Exception e) {
			e.printStackTrace();
		}
		return null;
	}
	/**
	 * 
	 * @param httpClient
	 * @param token
	 * @param ecmClient 客户端配置
	 * @param stream 电子文件
	 * @param fileName 电子文件名
	 * @param parentId 父Id
	 */
	public void addAttachments(CloseableHttpClient httpClient,String token,EcmHttp ecmClient,InputStream stream,String fileName,String parentId) {
		System.out.println("[METHOD]addAttachments");
		HttpPost httpPost=null;
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
			// file为提交参数名,stream为要上传的文件的文件流 inputStream，最后一个参数为上传文件名称
	        fileBuilder.addBinaryBody("uploadFile", stream, ContentType.create("multipart/form-data"), fileName);
	        JSONObject jsObj = new JSONObject();
			jsObj.put("parentDocId", parentId);
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
	}
	/**
	 * 创建关系
	 * @param httpClient
	 * @param token
	 * @param ecmClient
	 * @param parentId 父ID
	 * @param childId 子ID
 	 * @param relationName 关系名称
	 */
	public void newRelation(CloseableHttpClient httpClient,String token,EcmHttp ecmClient,String parentId,String childId,String relationName) {
		System.out.println("[METHOD]newRelation");
		HttpPost httpPost=null;
		String url = ecmClient.getConfig().getBaseurl()+"/dc/createRelation";
		httpPost=new HttpPost(url);
		try {
			
			RequestConfig timeoutConfig = RequestConfig.custom()
					.setConnectTimeout(5000).setConnectionRequestTimeout(1000)
					.setSocketTimeout(5000).build();
			httpPost.setConfig(timeoutConfig);
			
	        JSONObject jsObj = new JSONObject();
			jsObj.put("childId", childId);
			jsObj.put("parentId",parentId);
			jsObj.put("name",relationName);
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
	}
}
