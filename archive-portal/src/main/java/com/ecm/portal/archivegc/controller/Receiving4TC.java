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
				if(!StringUtils.isEmpty(id)) {
					EcmDocument doc = documentService.getObjectById(getToken(), id);
					String sql = "select CHILD_ID from ecm_relation where PARENT_ID = '"+id+"'";
					List<Map<String, Object>> childId = relationService.getMapList(getToken(), sql);
					EcmDocument docu = documentService.getObjectById(getToken(), childId.get(0).get("CHILD_ID").toString());
					if(docu != null) {
						ReplyCfgEntity en = customCacheService.getReplyCfg(getToken(), doc.getTypeName());
						if( en == null) {
							mp.put("code", ActionContext.FAILURE);
						}else {
							mp.put("code", ActionContext.SUCESS);
							mp.put("typeName", en.getToType());
							mp.put("includeRefDoc", en.isIncludeRefDoc());
							for(String attr: en.getAttrNames().keySet()) {
								valmp.put(attr, docu.getAttributeValue(en.getAttrNames().get(attr)));
							}
							valmp.put("IS_CHILD", 0);
							valmp.put("TYPE_NAME",en.getToType());
							valmp.put("C_ITEM_TYPE","案卷");
							String folderId = folderPathService.getFolderByPath(getToken(), "/整编库/工程设计");
							valmp.put("FOLDER_ID", folderId);
							String parentId = documentService.newObject(getToken(), valmp);
							newRelation(id,parentId);
						}
					}
				}else {
					mp.put("code", ActionContext.FAILURE);
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
	private void newRelation(String oldParentId,String parentId) throws Exception {
		String sql = "select CHILD_ID from ecm_relation where PARENT_ID = '"+oldParentId+"'";
		List<Map<String, Object>> childId = relationService.getMapList(getToken(), sql);
		for(Map<String,Object> id:childId) {
			EcmRelation relation=new EcmRelation();
			relation.setParentId(parentId);
			relation.setChildId(id.get("CHILD_ID").toString());
			relation.setName("irel_children");
			String ids=relationService.newObject(getToken(), relation);
			documentService.updateStatus(getToken(), id.get("CHILD_ID").toString(), "整编", "");
		} 
	}
}
