package com.ecm.portal.archivegc.controller;

import java.io.FileInputStream;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.poi.ss.usermodel.Row;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import com.alibaba.druid.util.StringUtils;
import com.ecm.common.util.FileUtils;
import com.ecm.common.util.JSONUtils;
import com.ecm.core.ActionContext;
import com.ecm.core.db.DBFactory;
import com.ecm.core.entity.EcmContent;
import com.ecm.core.entity.EcmDocument;
import com.ecm.core.entity.EcmFolder;
import com.ecm.core.entity.EcmRelation;
import com.ecm.core.exception.AccessDeniedException;
import com.ecm.core.service.DocumentService;
import com.ecm.core.service.FolderPathService;
import com.ecm.core.service.FolderService;
import com.ecm.core.service.RelationService;
import com.ecm.portal.archivegc.entity.ReplyCfgEntity;
import com.ecm.portal.archivegc.tc.TCService;
import com.ecm.portal.controller.ControllerAbstract;
import com.ecm.portal.service.CustomCacheService;
import com.ecm.portal.util.CustomInfo;
@Controller
public class Receiving4TC extends ControllerAbstract {
	@Autowired
	private DocumentService documentService;

	@Autowired
	private CustomCacheService customCacheService;

	@Autowired
	private RelationService relationService;

	@Autowired
	private FolderService folderService;
	
	@Autowired
	private FolderPathService folderPathService;
	
	@Autowired
	private TCService tCService;
	
    /**
	 *接收文档
	 */
	@RequestMapping(value = "/TC/Receivingdc", method = RequestMethod.POST)
	@ResponseBody
	public Map<String, Object> Receivingdc(@RequestBody String argStr) throws Exception {
		Map<String, Object> mp = new HashMap<String, Object>();
		try {
			Map<String, Object> args = JSONUtils.stringToMap(argStr);
			String idsStr=args.get("ids").toString();
			List<String> list = JSONUtils.stringToArray(idsStr);
			List<String> tcIds = new ArrayList<String>();
			//
			for(String childId : list) {
				EcmDocument doc= documentService.getObjectById(getToken(), childId);
				doc.setStatus("已接收");
				doc.getAttributes().put("C_RECEIVE", documentService.getSession(getToken()).getCurrentUser().getUserName());
				doc.getAttributes().put("C_RECEIVE_DATE", new Date());
				documentService.updateObject(getToken(), doc, null);
				if(doc.getAttributes().get("SYN_ID")!=null) {
					tcIds.add((String)doc.getAttributes().get("SYN_ID"));
				}
			}
			tCService.updateTCStatus(getToken(), tcIds);
			mp.put("code", ActionContext.SUCESS);
			
		}catch (Exception e) {
			// TODO: handle exception
			mp.put("code", ActionContext.FAILURE);
			mp.put("message", e.getMessage());
			e.printStackTrace();
		}
		return mp;
	}
	/**
	 *驳回文档
	 */
	@RequestMapping(value = "/TC/Rejectdc", method = RequestMethod.POST)
	@ResponseBody
	public Map<String, Object> Rejectdc(@RequestBody String argStr) throws Exception {
		Map<String, Object> mp = new HashMap<String, Object>();
		try {
			Map<String, Object> args = JSONUtils.stringToMap(argStr);
			String idsStr=args.get("ids").toString();
			List<String> list = JSONUtils.stringToArray(idsStr);
			String rejectCommon=args.get("rejectCommon")!=null?args.get("rejectCommon").toString():"";
			tCService.rejectToTc(getToken(), list, rejectCommon);
			mp.put("code", ActionContext.SUCESS);
			
		}catch (Exception e) {
			// TODO: handle exception
			mp.put("code", ActionContext.FAILURE);
			mp.put("message", e.getMessage());
			e.printStackTrace();
		}
		return mp;
	}
	/**
	 *提交整编
	 */
	@RequestMapping(value = "/TC/Arrangedc", method = RequestMethod.POST)
	@ResponseBody
	public Map<String, Object> Arrangedc(@RequestBody String argStr) throws Exception {
		Map<String, Object> mp = new HashMap<String, Object>();
		Map<String, Object> valmp = new HashMap<String, Object>();
		try {
			Map<String, Object> args = JSONUtils.stringToMap(argStr);
			String idsStr=args.get("ids").toString();
			List<String> list = JSONUtils.stringToArray(idsStr);
			List<String> tcIds = new ArrayList<String>();
			for(String id : list) {
				//查询移交单下的文件的id
				String sql = "select CHILD_ID from ecm_relation where PARENT_ID='"+id+"' and NAME !='附件'";
				List<Map<String, Object>> lis = relationService.getMapList(getToken(), sql);
				for(Map<String,Object> a:lis) {
					//查询移交单下文件的属性信息
					Map<String, Object> doc = documentService.getObjectMapById(getToken(), a.get("CHILD_ID").toString());
					String coding=doc.get("CODING").toString();
					if(doc.get("SYN_ID")!=null && doc.get("STATUS")!=null && !"已接收".equals((String)doc.get("STATUS"))) {
						tcIds.add((String)doc.get("SYN_ID"));
					}
					//查询是否存在相同coding的文件
					String condition = "CODING='"+coding+"' and C_ITEM_TYPE='文件' and id !='"+a.get("CHILD_ID")+"' and IS_RELEASED=1";
					List<EcmDocument> res = documentService.getObjects(getToken(), condition);
					if(res != null && res.size() > 0) {
						//获取文件的案卷id
						String sql1 = "select PARENT_ID from ecm_relation where CHILD_ID='"+res.get(0).getId()+"'";
						List<Map<String, Object>> lis2 = relationService.getMapList(getToken(), sql1);
						//通过id获取属性信息
						String parentId2 = lis2.get(0).get("PARENT_ID").toString();
						EcmDocument result2 = documentService.getObjectById(getToken(), parentId2);
						//库位号
						String LOCATION = result2.getAttributeValue("C_LOCATION").toString()==null?""
								:result2.getAttributeValue("C_LOCATION").toString();
						//库号
						String StoreCoding = result2.getAttributeValue("C_STORE_CODING").toString()==null?""
								:result2.getAttributeValue("C_STORE_CODING").toString();
						//档案号
						String ArchiveCoding = result2.getAttributeValue("C_ARCHIVE_CODING").toString()==null?""
								:result2.getAttributeValue("C_ARCHIVE_CODING").toString();
						
						String folderId=folderPathService.getFolderId(getToken(), doc, "2");
						doc.put("C_LOCATION", LOCATION);
						doc.put("C_STORE_CODING", StoreCoding);
						doc.put("C_ARCHIVE_CODING", ArchiveCoding);
						doc.put("FOLDER_ID", folderId);
						EcmFolder folder = folderService.getObjectById(getToken(), folderId);
						doc.put("ACL_NAME", folder.getAclName());
						doc.put("STATUS", "整编");
						doc.put("C_RECEIVE", documentService.getSession(getToken()).getCurrentUser().getUserName());
						doc.put("C_RECEIVE_DATE", new Date());
						documentService.updateObject(getToken(), doc);
					}else {
						String folderId=folderPathService.getFolderId(getToken(), doc, "2");
						EcmFolder folder = folderService.getObjectById(getToken(), folderId);
						doc.put("ACL_NAME", folder.getAclName());
						doc.put("STATUS", "整编");
						doc.put("C_RECEIVE", documentService.getSession(getToken()).getCurrentUser().getUserName());
						doc.put("C_RECEIVE_DATE", new Date());
						doc.put("FOLDER_ID", folderId);
						documentService.updateObject(getToken(), doc);
						
						//mp.put("code", ActionContext.FAILURE);
						//mp.put("message", "文件"+coding+"没有对应案卷");
						//return mp;
					}
				}
				documentService.updateStatus(getToken(), id, "整编", "");
			}
			tCService.updateTCStatus(getToken(), tcIds);
			mp.put("code", ActionContext.SUCESS);
			
		}catch (Exception e) {
			// TODO: handle exception
			mp.put("code", ActionContext.FAILURE);
			mp.put("message", e.getMessage());
			e.printStackTrace();
		}
		return mp;
	}
}
