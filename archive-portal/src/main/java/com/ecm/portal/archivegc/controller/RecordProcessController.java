package com.ecm.portal.archivegc.controller;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import com.alibaba.druid.util.StringUtils;
import com.ecm.common.util.JSONUtils;
import com.ecm.core.ActionContext;
import com.ecm.core.dao.EcmDocumentMapper;
import com.ecm.core.entity.EcmDocument;
import com.ecm.core.service.DocumentService;
import com.ecm.core.service.FolderPathService;
import com.ecm.portal.archive.common.Constants;
import com.ecm.portal.controller.ControllerAbstract;

@Controller
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
	public Map<String, Object> createStorageNum(@RequestBody String argStr) throws Exception {		
		Map<String,Object> params= JSONUtils.stringToMap(argStr);
		String ID= params.get("ids").toString();
		List<String> listID = JSONUtils.stringToArray(ID);
		
		String strID =  "'" + listID.get(0) + "'";
		for(int i=1;i<listID.size();i++){
			strID += ", '";
			strID += listID.get(i);
			strID += "'";
		}
		
		String sqlSearchStore = "select * from ecm_document ed where ID in ("+ strID +") order by C_ARCHIVE_DATE, C_DRAFT_DATE desc";
		List<Map<String, Object>> store = documentService.getMapList(getToken(), sqlSearchStore);
		
		for(int i = 0; i<store.size(); i++) {
			String storeID = (String) store.get(i).get("ID");
			String typeNameStore = (String) store.get(i).get("TYPE_NAME");
			
			EcmDocument doc = documentService.getObjectById(getToken(), storeID);
			
			String archiveDate = (String) doc.getAttributes().get("C_STORE_CODING");
			
			if (StringUtils.isEmpty(archiveDate)) {
				String sqlSearchComment = "select ITEM_CONTENT, C_ORDER_INDEX from ecm_document ed where C_COMMENT like '%"+ typeNameStore +"%'";
				List<Map<String, Object>> docType = documentService.getMapList(getToken(), sqlSearchComment);
				int orderIndex = (docType.get(i).get("C_ORDER_INDEX") != null)?(int)docType.get(i).get("C_ORDER_INDEX")+1:1;
				
				doc.addAttribute("C_STORE_CODING", orderIndex);
				documentService.updateObject(getToken(), doc, null);
			}
		}
		
		Map<String, Object> mp = new HashMap<String, Object>();
		mp.put("code", ActionContext.SUCESS);
		return mp;
		
	}
	
	
	@RequestMapping(value = "/record/handOverRecord", method = RequestMethod.POST)
	@ResponseBody
	public Map<String, Object> handOverRecord(@RequestBody String argStr) throws Exception {
		Map<String, Object> args = JSONUtils.stringToMap(argStr);
		String ID= args.get("ids").toString();
		List<String> listID = JSONUtils.stringToArray(ID);
		
		
		for (String fileId: listID) {
			
			EcmDocument parentDoc= documentService.getObjectById(getToken(), fileId);
			
			String parentSecurityLevel= parentDoc.getSecurityLevel();
			if(parentSecurityLevel==null||"".equals(parentSecurityLevel)) {
				parentSecurityLevel="内部公开";
			}
			String sqlAcl = "select NAME from ecm_acl ea where DESCRIPTION = '" + parentSecurityLevel + "'";
			List<Map<String, Object>> listAcl = documentService.getMapList(getToken(), sqlAcl);
			String parentAclName= listAcl.get(0).get("NAME").toString();
			
			String parentFolderId = folderpathService.getReleaseFolderId(getToken(), parentDoc.getAttributes());
			parentDoc.addAttribute("FOLDER_ID", parentFolderId);
			parentDoc.addAttribute("STATUS", Constants.INSTORAGE);
			parentDoc.addAttribute("IS_RELEASED", "1");
			parentDoc.addAttribute("ACL_NAME", parentAclName);
			
			documentService.updateObject(getToken(), parentDoc, null);
			
			String sql1="select child_id from ecm_relation where parent_id='"+fileId+"' "+ " and name='irel_children'";
			List<Map<String,Object>> childrenIds= documentService.getMapList(getToken(), sql1);
			
			for(int n=0;childrenIds!=null&&n<childrenIds.size();n++ ) {
				Map<String,Object> child= childrenIds.get(n);
				String childId=child.get("child_id").toString();
				EcmDocument childDoc=documentService.getObjectById(getToken(), childId);
				String childSecurityLevel= childDoc.getSecurityLevel();
				if(childSecurityLevel==null||"".equals(childSecurityLevel)) {
					childSecurityLevel="内部公开";
				}
				String sqlAclChild = "select NAME from ecm_acl ea where DESCRIPTION = '" + childSecurityLevel + "'";
				
				List<Map<String, Object>> listAclChild = documentService.getMapList(getToken(), sqlAclChild);
				String childAclName= listAclChild.get(0).get("NAME").toString();
				childDoc.addAttribute("FOLDER_ID", parentFolderId);
				childDoc.addAttribute("STATUS", Constants.INSTORAGE);
				childDoc.addAttribute("IS_RELEASED", "1");
				childDoc.addAttribute("ACL_NAME", childAclName);
				documentService.updateObject(getToken(), childDoc, null);
			}
			
		}
		
		Map<String, Object> mp = new HashMap<String, Object>();
		mp.put("code", ActionContext.SUCESS);
		return mp;
		
	}
	
	
}
