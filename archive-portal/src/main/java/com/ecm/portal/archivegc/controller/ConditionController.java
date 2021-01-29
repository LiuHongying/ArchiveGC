package com.ecm.portal.archivegc.controller;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

import com.ecm.common.util.JSONUtils;
import com.ecm.core.ActionContext;
import com.ecm.core.entity.EcmDocument;
import com.ecm.core.entity.EcmFolder;
import com.ecm.core.exception.AccessDeniedException;
import com.ecm.core.service.DocumentService;
import com.ecm.core.service.FolderService;
import com.ecm.portal.controller.ControllerAbstract;

@RestController
@RequestMapping("/condition")
public class ConditionController extends ControllerAbstract {
	@Autowired
	private DocumentService documentService;
	
	@Autowired
	private FolderService folderService;
	
	@PostMapping("save")
	@ResponseBody
	public Map<String, Object> saveCondition(@RequestBody String argStr) throws Exception{
		Map<String, Object> mp = new HashMap<String, Object>();
		Map<String, Object> conditionMap = JSONUtils.stringToMap(argStr);
		EcmDocument doc = new EcmDocument();
		EcmFolder fld;
		try {
			fld = folderService.getObjectByPath(getToken(), "/表单/订阅单");
			doc.setFolderId(fld.getId());
			doc.setAclName(fld.getAclName());
			doc.addAttribute("NAME", conditionMap.get("Name").toString());
			doc.addAttribute("ITEM_CONTENT", conditionMap.get("Condition").toString());
			doc.addAttribute("TYPE_NAME", "订阅单");
			String id = documentService.newObject(getToken(), doc, null);
			mp.put("code", ActionContext.SUCESS);
		} catch (AccessDeniedException e) {
			mp.put("code", ActionContext.TIME_OUT);
		}
		return mp;
	}
	@ResponseBody
	@RequestMapping("load")
	public Map<String, Object> loadCondition() throws Exception{
		Map<String, Object> mp = new HashMap<String, Object>();
		List<Map<String, Object> > outList = new ArrayList<Map<String, Object>>();
		Map<String, Object> projMap = new HashMap<String, Object>();
		
		String loginName= this.getSession().getCurrentUser().getUserName();
		EcmFolder fld=folderService.getObjectByPath(getToken(), "/表单/订阅单");
		String searchCondition = " select * from ecm_document ed where CREATOR = '" + loginName + "' and FOLDER_ID='"+fld.getId()+"'";
		try {
			List<Map<String,Object>> conditionList =documentService.getMapList(getToken(), searchCondition);
			for(int i = 0; i < conditionList.size(); i++) {
				projMap = new HashMap<String, Object>();
				projMap.put("Name", conditionList.get(i).get("NAME"));
				projMap.put("id", conditionList.get(i).get("ID"));
				projMap.put("Condition", conditionList.get(i).get("ITEM_CONTENT"));
				outList.add(projMap);
			}
			mp.put("data", outList);
		}catch (Exception ex) {
			ex.printStackTrace();
			mp.put("code", ActionContext.FAILURE);
		}
		
		return mp;
	}
	@PostMapping("delete")
	@ResponseBody
	public Map<String, Object> deletCondition(@RequestBody String argStr) throws Exception{
		Map<String, Object> mp = new HashMap<String, Object>();
		Map<String, Object> args = JSONUtils.stringToMap(argStr);
		try {
			String id = args.get("id").toString();
			documentService.deleteObjectById(getToken(), id);
			mp.put("code", ActionContext.SUCESS);
		} catch (AccessDeniedException e) {
			mp.put("code", ActionContext.TIME_OUT);
		}
		return mp;
	}
	
}
