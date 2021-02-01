package com.ecm.portal.archivegc.controller;

import java.io.FileInputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import com.ecm.common.util.JSONUtils;
import com.ecm.core.ActionContext;
import com.ecm.core.cache.manager.CacheManagerOper;
import com.ecm.core.entity.EcmContent;
import com.ecm.core.entity.EcmDocument;
import com.ecm.core.entity.EcmFolder;
import com.ecm.core.entity.EcmForm;
import com.ecm.core.entity.EcmFormItem;
import com.ecm.core.entity.EcmGridView;
import com.ecm.core.exception.AccessDeniedException;
import com.ecm.core.service.DocumentService;
import com.ecm.core.service.FolderService;
import com.ecm.core.service.FormItemService;
import com.ecm.core.service.FormService;
import com.ecm.core.service.GridViewService;
import com.ecm.portal.controller.ControllerAbstract;

@Controller
public class FormItemController extends ControllerAbstract{
	
	@Autowired
	private FormService formService;
	
	@Autowired
	private FormItemService formItemService;
	
	@Autowired
	private DocumentService documentService;
	
	@Autowired
	private FolderService folderService;

	
	@ResponseBody
	@RequestMapping("/archive/getConfigList")
	public Map<String,Object> getConfigList(@RequestBody String param){
		Map<String, Object> mp = new HashMap<String, Object>();
		Map<String,Object> p= JSONUtils.stringToMap(param);
		try {
			StringBuilder sb = new StringBuilder("select id,name,c_from from ecm_document where ");
			sb.append(" creator='").append(this.getSession().getCurrentUser().getUserName()).append("'");
			if(p.get("typeName")!=null) {
				sb.append(" and sub_type ='").append(p.get("typeName")).append("'");
			}
			if (p.get("C_FROM")!=null) {
				String cfrom = (String) p.get("C_FROM");
				if (cfrom.contains("_CUSTOM")) {
					EcmDocument ecmDocument = documentService.getObjectById(getToken(), cfrom.replace("_CUSTOM", ""));
					cfrom = (String) ecmDocument.getAttributeValue("C_FROM");
				}
				sb.append(" and C_FROM ='").append(cfrom).append("'");
			}
			sb.append(" and type_name = '用户列表配置' ");
			sb.append(" ORDER BY CREATION_DATE DESC");
			List<Map<String, Object>> dataList = new ArrayList<>();
			dataList = documentService.getMapList(getToken(), sb.toString());
			mp.put("code", ActionContext.SUCESS);
			mp.put("data", dataList);
		} catch (Exception e) {
			mp.put("code", ActionContext.TIME_OUT);
		}
		return mp;
	}
	
	@ResponseBody
	@RequestMapping("/archive/deleteOrUpdateConfig")
	public Map<String,Object> deleteOrUpdateConfig(@RequestBody String param){
		Map<String, Object> mp = new HashMap<String, Object>();
		Map<String,Object> p= JSONUtils.stringToMap(param);
		try {
			String action = (String) p.get("aciton");
			String id = (String) p.get("id");
			if("update".equals(action)) {
				String itemContent = (String) p.get("configContent");
				EcmDocument document = documentService.getObjectById(getToken(), id);
				Map<String, Object> map = new HashMap<String, Object>();
				map.put("ID", document.getId());
				map.put("ITEM_CONTENT", itemContent);
				documentService.updateObject(getToken(), map);
				mp.put("code", ActionContext.SUCESS);
			}else if("delete".equals(action)) {
				boolean success = documentService.deleteObjectById(getToken(), id);
				if (success) {
					mp.put("code", ActionContext.SUCESS);
				}else {
					mp.put("code", ActionContext.FAILURE);
				}
			}else {
				
			}
		} catch (Exception e) {
			mp.put("code", ActionContext.FAILURE);
		}
		return mp;
	}
	
	@ResponseBody
	@RequestMapping("/archive/getConfigById")
	public Map<String,Object> getConfigById(@RequestBody String param){
		Map<String, Object> mp = new HashMap<String, Object>();
		Map<String,Object> p= JSONUtils.stringToMap(param);
		try {
			EcmDocument document = documentService.getObjectById(getToken(), (String)p.get("id"));
			mp.put("code", ActionContext.SUCESS);
			mp.put("data", document.getAttributeValue("ITEM_CONTENT"));
		} catch (Exception e) {
			mp.put("code", ActionContext.TIME_OUT);
		}
		return mp;
	}
	
	@ResponseBody
	@RequestMapping("/archive/getFormItems/{typeName}/{language}")
	public Map<String,Object> getFormItemsByGridViewName(@PathVariable("typeName") String typeName,@PathVariable("language") String language){
		Map<String, Object> mp = new HashMap<String, Object>();
		try {
			List<EcmFormItem> datalist = new ArrayList<EcmFormItem>();
				EcmForm formObj = this.getECMForm(typeName, "NEW");
				String formId = formObj.getId();
				List<EcmFormItem> list = formItemService.getFormItems(getToken(),formId);
				for (EcmFormItem item : list) {
					datalist.add(item.clone(language));
				}
			mp.put("code", ActionContext.SUCESS);
			mp.put("data", datalist); 
		} catch (AccessDeniedException e) {
			mp.put("code", ActionContext.TIME_OUT);
		}
		return mp;
	}
	
	/**
	 * Matthew creates on 2021年1月26日11:02:13
	 * @param param
	 * @return
	 */
	@RequestMapping(value="/archive/customListConfig", method = RequestMethod.POST)
	@ResponseBody	
	public Map<String, Object> customListConfig(@RequestBody String param) {

		Map<String, Object> mp = new HashMap<String, Object>();
		Map<String,Object> p= JSONUtils.stringToMap(param);
		try {
			String typeName="";
			String configName = "";
			String configContent = "";
			String attrsStr = "";
			String labelStr = "";
			String cFrom = "";
			if(p.get("typeName")!=null&&!"".equals(p.get("typeName"))) {
				typeName=p.get("typeName").toString();
			}else {
				mp.put("code", ActionContext.FAILURE);
				mp.put("message", "类型为空");
				return mp;
			}
			if(p.get("configName")!=null&&!"".equals(p.get("configName"))) {
				configName=p.get("configName").toString();
			}else {
				mp.put("code", ActionContext.FAILURE);
				mp.put("message", "配置名称为空");
				return mp;
			}
			if(p.get("configContent")!=null&&!"".equals(p.get("configContent"))) {
				configContent=p.get("configContent").toString();
			}else {
				mp.put("code", ActionContext.FAILURE);
				mp.put("message", "配置信息为空");
				return mp;
			}
			attrsStr = (String) p.get("attrsStr");
			labelStr = (String) p.get("labelStr");
			cFrom = (String) p.get("cfrom");
			EcmDocument document=new EcmDocument();
			EcmFolder folder = new EcmFolder();
			String folderId = "";
			folder = folderService.getObjectByPath(getToken(), "/表单/用户列表配置");
			if (folder!=null) {
				folderId = folder.getId();
			}else {
				mp.put("code", ActionContext.FAILURE);
				mp.put("message", "未找到文件夹目录");
				return mp;
			}
			document.setFolderId(folderId);
			document.setTypeName("用户列表配置");
			document.setName(configName);
			document.addAttribute("C_COMMENT", attrsStr);
			document.setSubType(typeName);
			document.addAttribute("ITEM_CONTENT", configContent);
			document.addAttribute("C_FROM", cFrom);
			document.setTitle(labelStr);
			String newObjId= documentService.newObject(getToken(), document);
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
	
	private EcmForm getECMForm(String name, String action) throws AccessDeniedException {
		List<EcmForm> list = formService.getAllObject(getToken());
		for (EcmForm frm : list) {
			if (frm.getTypeName().equals(name) && frm.getAction().equals(action))
				return frm;
		}
		return null;
	}
	
}
