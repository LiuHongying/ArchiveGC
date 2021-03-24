package com.ecm.portal.archivegc.tc;




import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.StatusLine;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.DateUtil;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.usermodel.WorkbookFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import com.alibaba.druid.util.StringUtils;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.ecm.common.util.DateUtils;
import com.ecm.common.util.FileUtils;
import com.ecm.core.cache.manager.CacheManagerOper;
import com.ecm.core.entity.EcmAttribute;
import com.ecm.core.entity.EcmContent;
import com.ecm.core.entity.EcmDocument;
import com.ecm.core.entity.EcmFolder;
import com.ecm.core.entity.EcmForm;
import com.ecm.core.entity.EcmFormClassification;
import com.ecm.core.entity.EcmFormItem;
import com.ecm.core.entity.EcmRelation;
import com.ecm.core.entity.Pager;
import com.ecm.core.exception.AccessDeniedException;
import com.ecm.core.exception.EcmException;
import com.ecm.core.exception.NoPermissionException;
import com.ecm.core.exception.NoTakeNumberRuleException;
import com.ecm.core.service.DocumentService;
import com.ecm.core.service.EcmService;
import com.ecm.core.service.FolderPathService;
import com.ecm.core.service.FolderService;
import com.ecm.core.service.NumberService;
import com.ecm.core.service.RelationService;
import com.ecm.icore.service.IEcmSession;

@Service
public class TCService {
	
	CloseableHttpClient httpClient = HttpClients.createDefault();
	
	private static final Logger logger = LoggerFactory.getLogger(TCService.class);
	
	@Autowired
	private DocumentService documentService;
	
	@Autowired
	private Environment env;

	public boolean rejectToTc(String token,List<String> idList,String message) throws Exception {
		String url  = env.getProperty("tc.url");
		if(idList!=null && idList.size()>0) {
			List<Map<String,String>> list = new ArrayList<Map<String,String>>();
			//List<String> idList2 =new ArrayList<String>();
			for(String id: idList) {
				Map<String,String> mp = new HashMap<String,String>();
				EcmDocument doc = documentService.getObjectById(token, id);
				if(doc != null) {
					mp.put("uid", (String)doc.getAttributeValue("SYN_ID"));
					mp.put("msg", message);
					list.add(mp);
				}
			}
			String msg = executeMethod(url,JSONObject.toJSONString(list));
			if("1".equals(msg)){
				String userName = documentService.getSession(token).getCurrentUser().getUserName();
				for(String id: idList) {
					Map<String,Object> mp = new HashMap<String,Object>();
					mp.put("ID", id);
					mp.put("STATUS", "驳回");
					mp.put("C_REJECT_COMMENT", message);
					mp.put("C_REJECTOR", userName);
					mp.put("C_REJECT_DATE", new Date());
					try {
						documentService.updateObject(token, mp);
					} catch (NoPermissionException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					} catch (AccessDeniedException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					} catch (EcmException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
				}
				return true;
			}else {
				throw new Exception(msg);
			}
		}
		return false;
	}
	
	public void updateTCStatus(String token, List<String> ids) throws Exception {
		String url  = env.getProperty("tc.url");
		if(ids.size()>0) {
			List<Map<String,String>> list = new ArrayList<Map<String,String>>();
			for(String id : ids) {
				Map<String,String> mp = new HashMap<String,String>();
				mp.put("uid", id);
				list.add(mp);
			}
			String msg = updateStatus(url,JSONObject.toJSONString(list));
			if(!"1".equals(msg)) {
				throw new Exception(msg);
			}
		}
	}
	
	private String updateStatus(String url,String jsObj) {
		HttpPost httpPost=new HttpPost(url+"/putArchiveStatus");
		logger.info(url);
		try {
			RequestConfig timeoutConfig = RequestConfig.custom()
					.setConnectTimeout(30000).setConnectionRequestTimeout(5000)
					.setSocketTimeout(30000).build();
			httpPost.setConfig(timeoutConfig);
			//httpPost.getParams().setParameter("uids", jsObj);
			StringEntity strEntity=new StringEntity(jsObj,"UTF-8");
			strEntity.setContentType("application/json");
			httpPost.setEntity(strEntity);
			
			HttpResponse response = httpClient.execute(httpPost);
			
			StatusLine statusLine = response.getStatusLine();
			int statusCode = statusLine.getStatusCode();
			// 请求成功
	        if (statusCode == 200) {
	        	HttpEntity responseResult = response.getEntity();
	        	String responseEntityStr = EntityUtils.toString(responseResult, "UTF-8");
	        	logger.info(responseEntityStr);
	        	JSONObject jsonResult = JSON.parseObject(responseEntityStr);
	        	
	        	if(jsonResult.containsKey("code")) {
	        		String code = jsonResult.getString("code");
	        		if("1".equals(code)) {
	        			return "1";
	        		}else {
	        			return jsonResult.getString("message");
	        		}
	        	}else {
	        		return "返回参数错误:"+jsonResult.toJSONString();
	        	}
	        	
	        }else {
	        	return "调用TC接口状态错误::"+statusCode;
	        }
		}catch (Exception e) {
			e.printStackTrace();
			return e.getMessage();
		}
	}
	
	private String executeMethod(String url,String jsObj) {
		HttpPost httpPost=new HttpPost(url+"/returnWF");
		logger.info(url);
		try {
			RequestConfig timeoutConfig = RequestConfig.custom()
					.setConnectTimeout(30000).setConnectionRequestTimeout(5000)
					.setSocketTimeout(30000).build();
			httpPost.setConfig(timeoutConfig);
			
			StringEntity strEntity=new StringEntity(jsObj,"UTF-8");
			strEntity.setContentType("application/json");
			httpPost.setEntity(strEntity);
			
			HttpResponse response = httpClient.execute(httpPost);
			
			StatusLine statusLine = response.getStatusLine();
			int statusCode = statusLine.getStatusCode();
			// 请求成功
	        if (statusCode == 200) {
	        	HttpEntity responseResult = response.getEntity();
	        	String responseEntityStr = EntityUtils.toString(responseResult, "UTF-8");
	        	logger.info(responseEntityStr);
	        	JSONObject jsonResult = JSON.parseObject(responseEntityStr);
	        	
	        	if(jsonResult.containsKey("code")) {
	        		String code = jsonResult.getString("code");
	        		if("1".equals(code)) {
	        			return "1";
	        		}else {
	        			return jsonResult.getString("message");
	        		}
	        	}else {
	        		return "返回参数错误:"+jsonResult.toJSONString();
	        	}
	        	
	        }else {
	        	return "调用TC接口状态错误::"+statusCode;
	        }
		}catch (Exception e) {
			e.printStackTrace();
			return e.getMessage();
		}
	}
}
