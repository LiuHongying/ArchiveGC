package com.zisecm.httptools.core;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Properties;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.StatusLine;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.util.EntityUtils;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.zisecm.httptools.core.config.EcmConfig;
/**
 * 客户端http请求管理
 * @author Atos
 *
 */
public class EcmHttp {
	
	private Properties props = null;
	private EcmConfig config = null;
	public EcmHttp() {
		// 加载连接配置
		String file= this.getClass().getClassLoader().getResource("config.properties").getFile();
		props = new Properties();
		try {
			props.load(new FileInputStream(file));
			
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		init(props);
	}
	
	/**
	 * 初始化参数配置
	 * @param props
	 */
	private void init(Properties props) {
		config = new EcmConfig();
		config.setUsername(props.getProperty("ecm.username", "tcadmin"));
		config.setPassword(props.getProperty("ecm.password", "Cnpe.2020!"));
		config.setBaseurl(props.getProperty("ecm.baseurl", "http://localhost:8080/zisecm"));
	}
	
	
	public EcmConfig getConfig() {
		return this.config;
	}
	
	/**
	 * 登录
	 * @param username 用户名
	 * @param password 密码
	 * @param httpClient 客户端
	 * @return
	 */
	public String login(String username,String password,CloseableHttpClient httpClient) {
		
		HttpPost httpPost=null;
		try {			
			httpPost = new HttpPost(config.getBaseurl()+"/userLogin");
			RequestConfig timeoutConfig = RequestConfig.custom()
					.setConnectTimeout(5000).setConnectionRequestTimeout(1000)
					.setSocketTimeout(5000).build();
			httpPost.setConfig(timeoutConfig);
			httpPost.setHeader("Content-Type","application/json;charset=UTF-8");
			JSONObject jsObj = new JSONObject();
			jsObj.put("username", username);
			jsObj.put("password", password);
						
			StringEntity strEntity=new StringEntity(jsObj.toJSONString());
			strEntity.setContentType("text/json");
			httpPost.setEntity(strEntity);
			
			HttpResponse response = httpClient.execute(httpPost);
			
			StatusLine statusLine = response.getStatusLine();
			int statusCode = statusLine.getStatusCode();
			// 请求成功
	        if (statusCode == 200) {
	        	HttpEntity responseResult = response.getEntity();
	        	String responseEntityStr = EntityUtils.toString(responseResult, "UTF-8");
	        	JSONObject jsonResult = JSON.parseObject(responseEntityStr);
	        	if(jsonResult.containsKey("token")) {
	        		return jsonResult.getString("token");
	        	}
	        	
	        }
		}catch (Exception e) {
			e.printStackTrace();
		}
		return "";
	}
	
	public boolean logout(String token,CloseableHttpClient httpClient) {
		
		HttpPost httpPost=null;
		try {			
			httpPost = new HttpPost(config.getBaseurl()+"/userLogout");
			RequestConfig timeoutConfig = RequestConfig.custom()
					.setConnectTimeout(5000).setConnectionRequestTimeout(1000)
					.setSocketTimeout(5000).build();
			httpPost.setConfig(timeoutConfig);
			httpPost.setHeader("Content-Type","application/json;charset=UTF-8");
			JSONObject jsObj = new JSONObject();
			jsObj.put("token", token);

						
			StringEntity strEntity=new StringEntity(jsObj.toJSONString());
			strEntity.setContentType("text/json");
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
	        		return jsonResult.getString("code").equals("1");
	        	}
	        	
	        }
		}catch (Exception e) {
			e.printStackTrace();
		}
		return false;
	}
}
