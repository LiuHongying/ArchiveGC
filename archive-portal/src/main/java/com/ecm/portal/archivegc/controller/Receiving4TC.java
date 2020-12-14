package com.ecm.portal.archivegc.controller;

import java.io.FileInputStream;
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
import com.ecm.portal.archivegc.impl.CustomCacheService;
import com.ecm.portal.controller.ControllerAbstract;
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
			//
			for(String childId : list) {
				EcmDocument doc= documentService.getObjectById(getToken(), childId);
				doc.setStatus("已接收");
				documentService.updateObject(getToken(), doc, null);
			}
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
			for(String childId : list) {
				EcmDocument doc= documentService.getObjectById(getToken(), childId);
				doc.setStatus("已驳回");
				doc.addAttribute("C_REJECT_COMMENT", rejectCommon);
				doc.addAttribute("C_REJECTOR", this.getSession().getCurrentUser().getUserName());
				doc.addAttribute("C_REJECT_DATE", DBFactory.getDBConn().getDBUtils().getDBDateNow());
				documentService.updateObject(getToken(), doc, null);
			}
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
			for(String id : list) {
				EcmDocument doc = documentService.getObjectById(getToken(), id);
				String coding=doc.getCoding();
				String condition = "CODING='"+coding+"' and C_ITEM_TYPE='案卷'";
				List<EcmDocument> res = documentService.getObjects(getToken(), condition);
				if(res != null && res.size() > 0) {
					//获取当前移交单下文件的属性
					String sql = "select CHILD_ID from ecm_relation where PARENT_ID='"+id+"'";
					List<Map<String, Object>> lis = relationService.getMapList(getToken(), sql);
					String childId = lis.get(0).toString();
					EcmDocument result = documentService.getObjectById(getToken(), childId);
					//库位号
					String LOCATION = result.getAttributeValue("C_LOCATION").toString()==null?""
							:result.getAttributeValue("C_LOCATION").toString();
					//库号
					String StoreCoding = result.getAttributeValue("C_STORE_CODING").toString()==null?""
							:result.getAttributeValue("C_STORE_CODING").toString();
					//档案号
					String ArchiveCoding = result.getAttributeValue("C_ARCHIVE_CODING").toString()==null?""
							:result.getAttributeValue("C_ARCHIVE_CODING").toString();
					//获取查询到的案卷的文件属性
					String sql1 = "select CHILD_ID from ecm_relation where PARENT_ID='"+res.get(0).toString()+"'";
					List<Map<String, Object>> lis2 = relationService.getMapList(getToken(), sql1);
					String childId2 = lis2.get(0).toString();
					String condition2= "ID='"+childId2+"'";
					List<Map<String,Object>> result2 = documentService.getObjectMap(getToken(), condition2);
					for(Map<String, Object> re2:result2) {
						String folderId=folderPathService.getFolderId(getToken(), re2, "2");
						re2.put("C_LOCATION", LOCATION);
						re2.put("C_STORE_CODING", StoreCoding);
						re2.put("C_ARCHIVE_CODING", ArchiveCoding);
						re2.put("FOLDER_ID", folderId);
						EcmFolder folder = folderService.getObjectById(getToken(), folderId);
						re2.put("ACL_NAME", folder.getAclName());
						re2.put("STATUS", "整编");
						documentService.updateObject(getToken(), re2);
					}
					
				}else {
					mp.put("code", ActionContext.FAILURE);
					mp.put("message", "文件"+coding+"没有对应案卷");
					return mp;
				}
				documentService.updateStatus(getToken(), id, "整编", "");
			}
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
