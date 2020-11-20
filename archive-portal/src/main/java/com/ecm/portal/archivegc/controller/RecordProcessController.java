package com.ecm.portal.archivegc.controller;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import com.ecm.common.util.JSONUtils;
import com.ecm.core.ActionContext;
import com.ecm.core.dao.EcmDocumentMapper;
import com.ecm.core.entity.EcmDocument;
import com.ecm.core.service.DocumentService;
import com.ecm.core.service.FolderPathService;
import com.ecm.portal.controller.ControllerAbstract;

public class RecordProcessController extends ControllerAbstract {
	
	@Autowired
	private DocumentService documentService;
	@Autowired
	private FolderPathService folderpathService;
	
	@RequestMapping(value = "/record/archiveStorage", method = RequestMethod.POST)
	@ResponseBody
	public void archiveStorage(@RequestBody String argStr) throws Exception {
		List<String> list = JSONUtils.stringToArray(argStr);
		
		for(String fileId:list) {
			//EcmDocument doc= this.getObjectById(getToken(), fileId);
			EcmDocument doc = documentService.getObjectById(getToken(), fileId);
			doc.addAttribute("Status", "待入库");
			doc.addAttribute("IS_RELEASED", "1");
			documentService.updateObject(getToken(), doc);
		}		
	}
	
	@RequestMapping(value = "/record/createStorageNum", method = RequestMethod.POST)
	@ResponseBody
	public Map<String, Object>  createStorageNum(@RequestBody String argStr) throws Exception {		
		Map<String,Object> params= JSONUtils.stringToMap(argStr);
		String ID= params.get("ids").toString();
		String Store= params.get("Store").toString();
		String Location= params.get("Location").toString();
		List<String> listID = JSONUtils.stringToArray(ID);
		List<String> listStore = JSONUtils.stringToArray(Store);
		List<String> listLocation = JSONUtils.stringToArray(Location);
		
		int i = 0;
		
		for(String fileId: listID) {
			EcmDocument doc = documentService.getObjectById(getToken(), fileId);
			doc.addAttribute("C_STORE_CODING", listStore.get(i));
			doc.addAttribute("C_LOCATION", listLocation.get(i));
			i += 1;
		}
		
		Map<String, Object> mp = new HashMap<String, Object>();
		mp.put("code", ActionContext.SUCESS);
		return mp;
		
	}
	
	
	@RequestMapping(value = "/record/handOverRecord", method = RequestMethod.POST)
	@ResponseBody
	public void handOverRecord(@RequestBody String argStr) throws Exception {
		Map<String, Object> args = JSONUtils.stringToMap(argStr);
		String folderRelease = folderpathService.getReleaseFolderId(getToken(), args);
	}
	
	
}
