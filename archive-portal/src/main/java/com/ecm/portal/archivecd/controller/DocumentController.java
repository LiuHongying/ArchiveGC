package com.ecm.portal.archivecd.controller;

import java.io.File;
import java.io.FileInputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import com.ecm.common.util.FileUtils;
import com.ecm.common.util.JSONUtils;
import com.ecm.core.ActionContext;
import com.ecm.core.cache.manager.CacheManagerOper;
import com.ecm.core.entity.EcmContent;
import com.ecm.core.entity.EcmDocument;
import com.ecm.core.entity.EcmFolder;
import com.ecm.core.entity.EcmGridView;
import com.ecm.core.entity.LoginUser;
import com.ecm.core.exception.AccessDeniedException;
import com.ecm.core.service.ContentService;
import com.ecm.core.service.DocumentService;
import com.ecm.core.service.FolderService;
import com.ecm.portal.archive.controller.ArchiveFolderController;
import com.ecm.portal.controller.ControllerAbstract;

@Controller
@RequestMapping(value = "/cd")
public class DocumentController extends ControllerAbstract {
	@Autowired
	private ContentService contentService;
	@Autowired
	private FolderService folderService;
	@Autowired
	private DocumentService documentService;
	private static final Logger logger = LoggerFactory.getLogger(DocumentController.class);
	/**
	 * 获取模板树
	 * @param param
	 * @return
	 */
	@RequestMapping(value="/dc/getTemplateTreeData", method = RequestMethod.POST)
	@ResponseBody	
	public Map<String, Object> getFolderByConfigeGC(@RequestBody String parentId) {

		Map<String, Object> mp = new HashMap<String, Object>();
		try {
			List<EcmFolder> list = folderService.getFoldersByParentId(getToken(), parentId);
//			List<Map<String,Object>> docs=documentService.getMapList(getToken(), 
//					"select * from ecm_document where  folder_id='"+parentId+"'");
			List<EcmDocument> docs= documentService.getObjectsAllColumn(getToken(), " folder_id='"+parentId+"'");
			List<Object> data=new ArrayList<>();
			data.addAll(list);
			data.addAll(docs);
			mp.put("code", ActionContext.SUCESS);
			mp.put("data", data);
		}
		catch(Exception ex) {
			ex.printStackTrace();
			mp.put("code", ActionContext.FAILURE);
			mp.put("message", ex.getMessage());
		}
		return mp;
	
	}
	
	/**
	 * 创建文档待模板
	 * @param param
	 * @return
	 */
	@RequestMapping(value="/dc/newDocumentWithTemplate", method = RequestMethod.POST)
	@ResponseBody	
	public Map<String, Object> newDocumentWithTemplate(@RequestBody String param) {

		Map<String, Object> mp = new HashMap<String, Object>();
		Map<String,Object> p= JSONUtils.stringToMap(param);
		try {
			String typeName="";
			if(p.get("typeName")!=null) {
				typeName=p.get("typeName").toString();
			}
			String templateId="";
			EcmContent en=null;
			if(p.get("templateId")!=null) {
				templateId=p.get("templateId").toString();
				EcmContent content = contentService.getPrimaryContent(getToken(), templateId);
				String fullPath = CacheManagerOper.getEcmStores().get(content.getStoreName()).getStorePath();
//				File f = new File(fullPath+en.getFilePath());
				FileInputStream itemStream = new FileInputStream(fullPath+content.getFilePath());
				
				en = new EcmContent();
				en.setName(content.getName());
				en.setContentSize(content.getContentSize());
				en.setFormatName(content.getFormatName());
				en.setInputStream(itemStream);
			}
			if("".equals(typeName)) {
				mp.put("code", ActionContext.FAILURE);
				mp.put("message", "类型为空");
				return mp;
			}
			
			EcmDocument document=new EcmDocument();
			document.setTypeName(typeName);
			String newObjId= documentService.newObject(getToken(), document, en);
			
			mp.put("code", ActionContext.SUCESS);
			mp.put("id", newObjId);
		}
		catch(Exception ex) {
			ex.printStackTrace();
			mp.put("code", ActionContext.FAILURE);
			mp.put("message", ex.getMessage());
		}
		return mp;
	
	}
	/**
	 * 获取所有表单
	 * 
	 * @return
	 */
	@ResponseBody
	@RequestMapping(value="/admin/getFolder", method = RequestMethod.POST)
	public Map<String, Object> getFolder(@RequestBody String parentId) {
		Map<String, Object> mp = new HashMap<String, Object>();
		try {
			List<EcmFolder> list = folderService.getFoldersByParentId(getToken(), parentId);
			mp.put("code", ActionContext.SUCESS);
			mp.put("data", list);
		}
		catch(Exception ex) {
			ex.printStackTrace();
			mp.put("code", ActionContext.FAILURE);
			mp.put("message", ex.getMessage());
		}
		return mp;
	}
	
	
}
