package com.ecm.portal.archivegc.controller;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.flowable.engine.HistoryService;
import org.flowable.engine.IdentityService;
import org.flowable.engine.ManagementService;
import org.flowable.engine.ProcessEngine;
import org.flowable.engine.RepositoryService;
import org.flowable.engine.RuntimeService;
import org.flowable.engine.TaskService;
import org.flowable.engine.repository.Deployment;
import org.flowable.engine.repository.ProcessDefinition;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
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
import com.ecm.core.cache.manager.impl.CacheManagerCfgActivity;
import com.ecm.core.entity.EcmCfgActivity;
import com.ecm.core.entity.EcmContent;
import com.ecm.core.entity.EcmDocument;
import com.ecm.core.entity.EcmFolder;
import com.ecm.core.entity.EcmRelation;
import com.ecm.core.entity.Pager;
import com.ecm.core.exception.AccessDeniedException;
import com.ecm.core.exception.EcmException;
import com.ecm.core.exception.NoPermissionException;
import com.ecm.core.service.DocumentService;
import com.ecm.core.service.FolderPathService;
import com.ecm.core.service.FolderService;
import com.ecm.core.service.RelationService;
import com.ecm.flowable.service.ActivitiService;
import com.ecm.portal.controller.ControllerAbstract;

@Controller
public class WorkFlowCenter extends ControllerAbstract {
	private Logger log = LoggerFactory.getLogger(WorkFlowCenter.class);
	@Autowired
	private RepositoryService repositoryService;
	@Autowired
	public TaskService taskService;
	@Autowired
	HistoryService historyService;

	@Autowired
	private ProcessEngine processEngine;
	@Autowired
	private RuntimeService runtimeService;
	@Autowired
	private ManagementService managementService;
	@Autowired
	private IdentityService identityService;
	@Autowired
	private DocumentService documentService;
	@Autowired
	private FolderPathService folderPathService;
	@Autowired
	private FolderService folderService;
	@Autowired
	private RelationService relationService;

	/**
	 * 创建文件或关联文件
	 * 
	 * @param metaData
	 * @param uploadFile
	 * @return
	 * @throws Exception
	 */
	@RequestMapping(value = "/dc/createWorkflowFormData", method = RequestMethod.POST)
	@ResponseBody
	public Map<String, Object> createWorkflowFormData(String metaData, MultipartFile uploadFile) throws Exception {

		Map<String, Object> mp = new HashMap<String, Object>();
		Map<String, Object> args = JSONUtils.stringToMap(metaData);
		Object saveType = args.get("saveType");
		Object childObj = args.get("childFileId");
		args.keySet().remove("childFileId");
		args.keySet().remove("saveType");
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
		Object fid = args.get("folderId");

		String folderId = "";
		if (fid == null) {
			folderId = folderPathService.getFolderId(getToken(), doc.getAttributes(),
					(saveType != null && !"".equals(saveType.toString())) ? saveType.toString() : "3");
		} else {
			folderId = fid.toString();
		}
		doc.setStatus("新建");
		EcmFolder folder = folderService.getObjectById(getToken(), folderId);
		doc.setFolderId(folderId);
		doc.setAclName(folder.getAclName());

		String id = "";

		if (childObj != null && !"".equals(childObj.toString())) {

			String relationName = "irel_children";
			relationName = (args.get("relationName") != null && !"".equals(args.get("relationName").toString()))
					? args.get("relationName").toString()
					: "irel_children";
			String childFileIds = childObj.toString();
			id = documentService.newObject(getToken(), doc, en);
			if (id != null && !"".equals(id)) {

				List<String> childIdList = JSONUtils.stringToArray(childFileIds);
				for (int i = 0; childIdList != null && i < childIdList.size(); i++) {
					String childId = childIdList.get(i);
					EcmRelation relation = new EcmRelation();
					relation.setParentId(id);

					relation.setChildId(childId);
					relation.setName(relationName);
					try {
						relationService.newObject(getToken(), relation);
					} catch (EcmException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
						mp.put("code", ActionContext.FAILURE);
						mp.put("MES", "");
						mp.put("message", e.getMessage());
						return mp;
					}
				}

			} else {
				mp.put("code", ActionContext.FAILURE);
				mp.put("MES", "对象已存在");
				return mp;
			}

		} else {
			id = documentService.newObject(getToken(), doc, en);
		}

		mp.put("code", ActionContext.SUCESS);
		mp.put("MES", "");
		mp.put("id", id);
		return mp;

	}

	
	@RequestMapping(value = "/dc/createWorkflowFormData4Appraisal", method = RequestMethod.POST)
	@ResponseBody
	public Map<String, Object> createWorkflowFormData4Appraisal(String metaData, MultipartFile uploadFile) throws Exception {

		Map<String, Object> mp = new HashMap<String, Object>();
		Map<String, Object> args = JSONUtils.stringToMap(metaData);
		Object saveType = args.get("saveType");

		args.keySet().remove("childFileId");
		args.keySet().remove("saveType");
		EcmDocument doc = new EcmDocument();
		doc.setAttributes(args);

		Object fid = args.get("folderId");

		String folderId = "";
		if (fid == null) {
			folderId = folderPathService.getFolderId(getToken(), doc.getAttributes(),
					(saveType != null && !"".equals(saveType.toString())) ? saveType.toString() : "3");
		} else {
			folderId = fid.toString();
		}
		doc.setFolderId(folderId);
		String id = doc.getId();
			if(id!=null) {
				documentService.updateObject(getToken(), doc.getAttributes());
			
		mp.put("code", ActionContext.SUCESS);
		mp.put("MES", "");
		mp.put("id", id);}
			if(id==null) {
				mp.put("code", ActionContext.FAILURE);
			}
		return mp;
	}
	
	
	
	
	@RequestMapping(value = "/dc/createWorkflowFormData4Copy", method = RequestMethod.POST)
	@ResponseBody
	public Map<String, Object> createWorkflowFormData4Copy(String metaData, MultipartFile uploadFile) throws Exception {

		Map<String, Object> mp = new HashMap<String, Object>();
		Map<String, Object> args = JSONUtils.stringToMap(metaData);
		Object saveType = args.get("saveType");
		Object childObj = args.get("childFileId");
		args.keySet().remove("childFileId");
		args.keySet().remove("saveType");
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
		Object fid = args.get("folderId");

		String folderId = "";
		if (fid == null) {
			folderId = folderPathService.getFolderId(getToken(), doc.getAttributes(),
					(saveType != null && !"".equals(saveType.toString())) ? saveType.toString() : "3");
		} else {
			folderId = fid.toString();
		}
		doc.setStatus("新建");
		EcmFolder folder = folderService.getObjectById(getToken(), folderId);
		doc.setFolderId(folderId);
		doc.setAclName(folder.getAclName());

		String id = doc.getId();

		if (childObj != null && !"".equals(childObj.toString())) {

			String relationName = "irel_children";
			relationName = (args.get("relationName") != null && !"".equals(args.get("relationName").toString()))
					? args.get("relationName").toString()
					: "irel_children";
			String childFileIds = childObj.toString();
			if(id!=null) {
				documentService.updateObject(getToken(), doc.getAttributes());
			}
			if(id==null)
			id = documentService.newObject(getToken(), doc, en);
			if (id != null && !"".equals(id)) {

				List<String> childIdList = JSONUtils.stringToArray(childFileIds);
				for (int i = 0; childIdList != null && i < childIdList.size(); i++) {
					String childId = childIdList.get(i);
					EcmRelation relation = new EcmRelation();
					relation.setParentId(id);

					relation.setChildId(childId);
					relation.setName(relationName);
					try {
						relationService.newObject(getToken(), relation);
					} catch (EcmException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
						mp.put("code", ActionContext.FAILURE);
						mp.put("MES", "");
						mp.put("message", e.getMessage());
						return mp;
					}
				}

			} else {
				mp.put("code", ActionContext.FAILURE);
				mp.put("MES", "对象已存在");
				return mp;
			}

		} else {
			id = documentService.newObject(getToken(), doc, en);
		}

		mp.put("code", ActionContext.SUCESS);
		mp.put("MES", "");
		mp.put("id", id);
		return mp;

	}
	
	
	
	/**
	 * 添加关系
	 * 
	 * @param argStr
	 * @return
	 */
	@RequestMapping(value = "/dc/delRelation", method = RequestMethod.POST) // PostMapping("/dc/getDocumentCount")
	@ResponseBody
	public Map<String, Object> delRelation(@RequestBody String argStr) {

		Map<String, Object> mp = new HashMap<String, Object>();

		List<String> childIdList = JSONUtils.stringToArray(argStr);
		try {
			for (int i = 0; childIdList != null && i < childIdList.size(); i++) {

				relationService.deleteObjectById(getToken(), childIdList.get(i));

			}
		} catch (EcmException | AccessDeniedException | NoPermissionException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			log.error(e.getMessage());
			mp.put("code", ActionContext.FAILURE);
			mp.put("message",e.getMessage());
			return mp;
		}
		mp.put("code", ActionContext.SUCESS);
		return mp;

	}

	/**
	 * 添加关系
	 * 
	 * @param argStr
	 * @return
	 */
	@RequestMapping(value = "/dc/addRelation", method = RequestMethod.POST) // PostMapping("/dc/getDocumentCount")
	@ResponseBody
	public Map<String, Object> addRelation(@RequestBody String argStr) {
		Map<String, Object> args = JSONUtils.stringToMap(argStr);
		Map<String, Object> mp = new HashMap<String, Object>();
		Object childObj = args.get("childIds");
		Object parentIdObj = args.get("parentId");
		if (childObj == null || parentIdObj == null) {
			mp.put("code", ActionContext.FAILURE);
			mp.put("msg", "没有选择文件ID");
			return mp;
		}

		String relationName = "irel_children";
		relationName = (args.get("relationName") != null && !"".equals(args.get("relationName").toString()))
				? args.get("relationName").toString()
				: "irel_children";
		String childFileIds = childObj.toString();
		List<String> childIdList = JSONUtils.stringToArray(childFileIds);
		for (int i = 0; childIdList != null && i < childIdList.size(); i++) {
			String childId = childIdList.get(i);
			EcmRelation relation = new EcmRelation();
			relation.setParentId(parentIdObj.toString());

			relation.setChildId(childId);
			relation.setName(relationName);
			try {
				relationService.newObject(getToken(), relation);
			} catch (EcmException | AccessDeniedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
				mp.put("code", ActionContext.FAILURE);
				mp.put("MES", "");
				mp.put("message", e.getMessage());
				log.error(e.getMessage());
				return mp;
			}
		}

		mp.put("code", ActionContext.SUCESS);
		return mp;
	}
	
	@RequestMapping(value = "/dc/getWorkflow", method = RequestMethod.POST) // PostMapping("/dc/getDocumentCount")
	@ResponseBody
	public Map<String, Object> getWorkFlow(@RequestBody String argStr) {
		Map<String, Object> args = JSONUtils.stringToMap(argStr);
		Map<String, Object> mp = new HashMap<String, Object>();
		Object processDefinitionKey = args.get("processDefinitionKey");
		List<ProcessDefinition> lists = null;
		if(processDefinitionKey != null&&!"".equals(processDefinitionKey)) {
			lists = repositoryService.createProcessDefinitionQuery()
					.processDefinitionKey(processDefinitionKey.toString())
					.latestVersion().list();
		}
//    	lists.get(0).getName();

		List<Map<String, Object>> workflowData = new ArrayList<Map<String, Object>>();

		for (int i = 0; lists != null && i < lists.size(); i++) {
			ProcessDefinition workflow = lists.get(i);

			Map<String, Object> row = new HashMap<String, Object>();
			row.put("ID", workflow.getId());
			row.put("NAME", workflow.getName());
			row.put("KEY", workflow.getKey());
			row.put("REVISION", workflow.getVersion());
			EcmCfgActivity ecmCfgActivityObj = CacheManagerCfgActivity.getCfgActivity(workflow.getName(), "start");
			row.put("FORMNAME",
					ecmCfgActivityObj == null ? workflow.getDescription() : ecmCfgActivityObj.getFormAttribute());// 流程表单名放在description中
			workflowData.add(row);
		}
		
		mp.put("data", workflowData);
		mp.put("code", ActionContext.SUCESS);
		return mp;
	}
	
	@RequestMapping(value = "/dc/getWorkflows", method = RequestMethod.POST) // PostMapping("/dc/getDocumentCount")
	@ResponseBody
	public Map<String, Object> getWorkFlowList(@RequestBody String argStr) {
		Map<String, Object> args = JSONUtils.stringToMap(argStr);
		Map<String, Object> mp = new HashMap<String, Object>();
		Object condition = args.get("condition");
		List<ProcessDefinition> lists = null;
		int pageSize = Integer.parseInt(args.get("pageSize").toString());
		int pageIndex = Integer.parseInt(args.get("pageIndex").toString());
		if (condition != null && "lastVersion".equals(condition.toString())) {
			lists = repositoryService.createProcessDefinitionQuery().latestVersion().listPage(pageIndex, pageSize);
		}else if(condition != null) {
			lists = repositoryService.createProcessDefinitionQuery()
					.processDefinitionKeyLike(condition.toString())
					.latestVersion().listPage(pageIndex, pageSize);
		} else {
			lists = repositoryService.createProcessDefinitionQuery().listPage(pageIndex, pageSize);
		}

//    	lists.get(0).getName();

		List<Map<String, Object>> workflowData = new ArrayList<Map<String, Object>>();

		for (int i = 0; lists != null && i < lists.size(); i++) {
			ProcessDefinition workflow = lists.get(i);

			Map<String, Object> row = new HashMap<String, Object>();
			row.put("ID", workflow.getId());
			row.put("NAME", workflow.getName());
			row.put("KEY", workflow.getKey());
			row.put("REVISION", workflow.getVersion());
			EcmCfgActivity ecmCfgActivityObj = CacheManagerCfgActivity.getCfgActivity(workflow.getName(), "start");
			row.put("FORMNAME",
					ecmCfgActivityObj == null ? workflow.getDescription() : ecmCfgActivityObj.getFormAttribute());// 流程表单名放在description中
			workflowData.add(row);
		}
		Pager pager = new Pager();
		pager.setPageIndex(pageIndex);
		pager.setPageSize(pageSize);
		mp.put("data", workflowData);
		mp.put("pager", pager);
		mp.put("code", ActionContext.SUCESS);
		return mp;
	}
}
