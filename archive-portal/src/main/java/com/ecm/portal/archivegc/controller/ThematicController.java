package com.ecm.portal.archivegc.controller;

import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.multipart.MultipartFile;

import com.ecm.common.util.FileUtils;
import com.ecm.common.util.JSONUtils;
import com.ecm.core.ActionContext;
import com.ecm.core.db.DBFactory;
import com.ecm.core.entity.EcmContent;
import com.ecm.core.entity.EcmDocument;
import com.ecm.core.entity.EcmFolder;
import com.ecm.core.entity.EcmRelation;
import com.ecm.core.exception.AccessDeniedException;
import com.ecm.core.exception.EcmException;
import com.ecm.core.service.DocumentService;
import com.ecm.core.service.ExcSynDetailService;
import com.ecm.core.service.FolderPathService;
import com.ecm.core.service.FolderService;
import com.ecm.core.service.RelationService;
import com.ecm.portal.controller.ControllerAbstract;
import com.ecm.portal.util.CustomInfo;
@Controller
public class ThematicController extends ControllerAbstract {

	@Autowired
	private DocumentService documentService;

	@Autowired
	private FolderService folderService;
	
	@Autowired
	private FolderPathService folderPathService;
	
	@Autowired
	private RelationService relationService;
	
	@RequestMapping(value = "/Thematic/newThematic", method = RequestMethod.POST)
	@ResponseBody
	public Map<String, Object> newThematic(String metaData, MultipartFile uploadFile) throws Exception {
		Map<String, Object> mp = new HashMap<String, Object>();
		Map<String, Object> args = JSONUtils.stringToMap(metaData);
		EcmContent en = null; 
		EcmDocument doc = new EcmDocument();
		doc.setAttributes(args);
		if (uploadFile != null) {
			en = new EcmContent();
			en.setName(uploadFile.getOriginalFilename());
			en.setContentSize(uploadFile.getSize());
			en.setFormatName(FileUtils.getExtention(uploadFile.getOriginalFilename()));
			en.setInputStream(uploadFile.getInputStream());
		}
		Object fid= args.get("folderId");
		String folderId="";
		if(fid==null) {
			folderId= folderPathService.getFolderId(getToken(), doc.getAttributes(), "3");
		}else {
			folderId=fid.toString();
		}
		doc.setStatus("新建");
		EcmFolder folder= folderService.getObjectById(getToken(), folderId);
		doc.setFolderId(folderId);
		doc.setAclName(folder.getAclName());
		String id ="";
		id= documentService.newObject(getToken(),doc,en);
		mp.put("code", ActionContext.SUCESS);
		mp.put("id", id);
		return mp;
	}
	
	@RequestMapping(value = "/Thematic/newDcRelation", method = RequestMethod.POST)
	@ResponseBody
	public Map<String, Object> newDcRelation(String metaData,String ID) throws Exception {
		Map<String, Object> mp = new HashMap<String, Object>();
		Map<String, Object> args = JSONUtils.stringToMap(metaData);
		List<String> list = JSONUtils.stringToArray(ID);
		String parentId=args.get("parentID").toString();
		for (String id : list) {
			EcmRelation relation=new EcmRelation();
			relation.setParentId(parentId);
			relation.setChildId(id);
			relation.setName("专题");
			String ids=relationService.newObject(getToken(), relation);
		}
		mp.put("code", ActionContext.SUCESS);
		return mp;
	}
	@RequestMapping(value = "/Thematic/DelDcRelation", method = RequestMethod.POST)
	@ResponseBody
	public Map<String, Object> DelDcRelation(@RequestBody String argStr) throws Exception {
		Map<String, Object> mp = new HashMap<String, Object>();
		System.out.print(argStr);
		List<String> list = JSONUtils.stringToArray(argStr);
		for (String id : list) {
			System.out.print(id);
			relationService.deleteByChildIdAndRelationName(getToken(), id, "专题");
		}
		mp.put("code", ActionContext.SUCESS);
		return mp;
	}
}
