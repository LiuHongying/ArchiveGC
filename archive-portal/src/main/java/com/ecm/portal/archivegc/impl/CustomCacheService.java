package com.ecm.portal.archivegc.impl;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.ecm.core.cache.manager.CacheManagerOper;
import com.ecm.core.entity.EcmDocument;
import com.ecm.core.exception.EcmException;
import com.ecm.core.service.DocumentService;
import com.ecm.portal.archivegc.entity.ReplyCfgEntity;
/**
 * 缓存
 * @Title:
 * @author Haihong Rong
 * @date:   2020-9-20 17:19:43 
 * @Description:
 */
@Service
public class CustomCacheService {
	
	@Autowired
	private DocumentService documentService;
	
	private String replyCacheName = "DocReplyCache";
	private String IEDCacheName = "IEDReplyCache";
	/**
	 * 获取回复配置
	 * @Title:
	 * @author Haihong Rong
	 * @date:   2020-9-20 17:20:16 
	 * @Description:       
	 * @param token
	 * @param fromType 源对象类型
	 * @return
	 */
	public ReplyCfgEntity getReplyCfg(String token,String fromType) {
		if(CacheManagerOper.getCustomCache().get(replyCacheName)==null) {
			initReplayCache(token);
		}
		List<ReplyCfgEntity> clist = (List<ReplyCfgEntity>)CacheManagerOper.getCustomCache().get(replyCacheName);
		for(ReplyCfgEntity en: clist) {
			if(en.getFromType().equals(fromType)) {
				return en;
			}
		}
		return null;
	}
	
	
	public EcmDocument getIEDCfg(String token,String project) {
		if(CacheManagerOper.getCustomCache().get(IEDCacheName)==null) {
			initIEDCache(token);
		}
		List<EcmDocument> elist = (List<EcmDocument>)CacheManagerOper.getCustomCache().get(IEDCacheName);
		for(EcmDocument ecm : elist) {
			String projectName = ecm.getAttributes().get("C_PROJECT_NAME").toString();
			if(projectName.equals(project)) {
				return ecm;
			}
		}
		return null;
	}
	/**
	 * 初始化回复配置缓存
	 * @Title:
	 * @author Haihong Rong
	 * @date:   2020-9-20 17:21:26 
	 * @Description:       
	 * @param token
	 */
	private void initReplayCache(String token) {
		String sql  = "select ID,C_FROM,C_TO,ITEM_CONTENT,C_COMMENT,STATUS from ecm_document where TYPE_NAME='文函回复配置'";
		List<ReplyCfgEntity> clist = new ArrayList<ReplyCfgEntity>();
		try {
			List<Map<String, Object>> list = documentService.getMapList(token, sql);
			for(Map<String, Object> mp: list) {
				ReplyCfgEntity en = new ReplyCfgEntity();
				en.setFromType(mp.get("C_FROM").toString());
				en.setToType(mp.get("C_TO").toString());
				en.setEnabled(mp.get("STATUS").toString().equals("是"));
				en.setDescription((String)mp.get("ITEM_CONTENT"));
				String policy = (String)mp.get("ITEM_CONTENT");
				if(policy !=null && policy.length()>0) {
					policy = policy.trim();
					String[] attrs = policy.split(";");
					for(String str : attrs) {
						if(!StringUtils.isEmpty(str)) {
							String[]  strs = str.split(":");
							if(strs.length>1) {
								// 配置中为 源属性：目标属性，需要转换成key目标属性，value源属性
								en.getAttrNames().put(strs[1], strs[0]);
							}else {
								en.getAttrNames().put(str, str);
							}
						}
					}
					
				}
				clist.add(en);
			}
		} catch (EcmException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		CacheManagerOper.getCustomCache().put(replyCacheName, clist);
	}
	
	private void initIEDCache(String token) {				//IED缓存初始化，就找仨值
		String sql  = "select ID,C_PROJECT_NAME,C_ITEM_STATUS1 from ecm_document where TYPE_NAME='项目'";
		List<EcmDocument> elist = new ArrayList<EcmDocument>();
		try {
			List<Map<String, Object>> list = documentService.getMapList(token, sql);
			for(Map<String, Object> mp: list) {
			EcmDocument ecm = new EcmDocument();
			ecm.setAttributes(mp);
				elist.add(ecm);
			}
		} catch (EcmException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		CacheManagerOper.getCustomCache().put(IEDCacheName, elist);
	}
	
	
	
	
}
