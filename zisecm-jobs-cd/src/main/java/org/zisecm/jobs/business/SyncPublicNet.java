package org.zisecm.jobs.business;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.List;

import org.apache.commons.io.Charsets;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.apache.http.impl.client.CloseableHttpClient;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.zisecm.jobs.entity.SyncUserIdBean;
import org.zisecm.jobs.entity.SyncUserIpBean;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.ecm.core.cache.manager.CacheManagerOper;
import com.ecm.core.entity.EcmContent;
import com.ecm.core.entity.EcmDocument;
import com.ecm.core.service.AuthService;
import com.ecm.core.service.DocumentService;
import com.ecm.icore.service.IEcmSession;

import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;

public class SyncPublicNet implements ISyncPublicNet{
	private Logger logger = LoggerFactory.getLogger(this.getClass());
	private static final Charset DEFAULT_CHARSET = Charset.forName("UTF-8");
	@Autowired
	private DocumentService documentService;
	
	@Autowired
	private AuthService authService;
	@Autowired
	private Environment env;
	
	/**
	 * 保存所有用戶IP信息
	 * @param url
	 * @return
	 * @throws Exception
	 */
	public  void saveAllUserIp(String url) throws Exception {
		 CloseableHttpClient closeableHttpClient = HttpClients.createDefault();
		 HttpGet httpGet=new HttpGet(url);
		 try {
			CloseableHttpResponse response = closeableHttpClient.execute(httpGet);
			String result = EntityUtils.toString(response.getEntity());
			 System.out.println(result);
			 JSONObject jsonObject=new JSONObject();
			 List<SyncUserIpBean> allUserIpList =jsonObject.parseArray(result, SyncUserIpBean.class);
			 
			 String ipPath = env.getProperty("sync.user.allip.path");
			 String newId  = writeJsonFile(allUserIpList,ipPath,"allUserIP");
			 System.out.println("IPPath" + ipPath);
			 System.out.println("newId" + newId);
			
		} catch (ClientProtocolException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		//return httpGet;
	}
	
	
	/**
	 * 保存所有用戶id信息
	 * @param url
	 * @return
	 * @throws Exception
	 */
	public  void saveAllUserId(String url) throws Exception {
		 CloseableHttpClient closeableHttpClient = HttpClients.createDefault();
		 HttpGet httpGet=new HttpGet(url);
		 try {
			CloseableHttpResponse response = closeableHttpClient.execute(httpGet);
			String result = EntityUtils.toString(response.getEntity());
			 System.out.println(result);
			 JSONObject jsonObject=new JSONObject();
			 List<SyncUserIdBean> allUserIpList =jsonObject.parseArray(result, SyncUserIdBean.class);
			 
			 String ipPath = env.getProperty("sync.user.allid.path");
			 String newId  = writeJsonFile(allUserIpList,ipPath,"allUserID");
			 System.out.println("IDPath" + ipPath);
			 System.out.println("newId" + newId);
			
		} catch (ClientProtocolException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		//return httpGet;
	}
	
	
	
	private  String writeJsonFile(Object obj, String fullPath,String typeName) throws Exception {
		String objId="";
		
		
		IEcmSession ecmSession = null;
		ecmSession = authService.login("syncUser", env.getProperty("sync.username"),env.getProperty("sync.password"));
	
		//查询是否存在相同coding的文件
		String condition = "TYPE_NAME = '" + typeName +"' ";
	    List<EcmDocument> res = documentService.getObjects(ecmSession.getToken(), condition);
	    EcmDocument doc = null;
	    if(res != null && res.size() > 0) {
	    	doc = res.get(0);
	    }else {
	    	doc = new EcmDocument();
	    }
		doc.setTypeName(typeName);
		EcmContent en = new EcmContent();
		String jsonString = JSONObject.toJSONString(obj, true);
		
//		Path ConfPath = Paths.get(fullPath);
//		if (!Files.exists(ConfPath)) {
//			FileUtils.forceMkdirParent(ConfPath.toFile());
//			Files.createFile(ConfPath);
//		}
		//Files.write(ConfPath, jsonString.getBytes(StandardCharsets.UTF_8), StandardOpenOption.CREATE);
		InputStream sbs = new ByteArrayInputStream(jsonString.getBytes(StandardCharsets.UTF_8));
		en.setName(typeName);
		en.setContentType(1);
		en.setContentSize((long) sbs.available());
		String fileName = fullPath.substring(fullPath.lastIndexOf("\\")+1);
		en.setFormatName(fileName);
		en.setInputStream(sbs);
		
		objId= documentService.creatOrUpdateObject(ecmSession.getToken(), doc, en);
		return objId;

	}
	
	//List<SyncBean> syncBeanList = readJsonResult(zipFile.toString().replace(".zip", "") + "/"+ zipFile.getName().replace(".zip", "") + ".json");
	private List<SyncUserIpBean> readJsonResult(String fileName) {
		List<SyncUserIpBean> objList = JSON.parseArray(readJsonFile(fileName), SyncUserIpBean.class);
		return objList;
	}
	
	
	
	private String readJsonFile(String filePath) {
		Path ConfPath = Paths.get("", filePath);
		String jsonString = "";
		try (InputStream in = FileUtils.openInputStream(ConfPath.toFile())) {
			jsonString = IOUtils.toString(in, Charsets.toCharset(DEFAULT_CHARSET));
		} catch (Exception e) {
			logger.error("读取文件失败{}", ConfPath.toAbsolutePath(), e);
		}
		return jsonString;
	}
	 public static void main(String[] args) {
		 String  fileName= "D:\\work\\ipall.json";
			 String str  = fileName.substring(fileName.lastIndexOf("\\")+1);
			 System.out.println(str);
		 
	}
	 
	
	 
}
