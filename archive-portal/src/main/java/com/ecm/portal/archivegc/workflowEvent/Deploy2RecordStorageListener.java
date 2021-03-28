package com.ecm.portal.archivegc.workflowEvent;

import java.util.Date;
import java.util.List;
import java.util.Map;

import org.flowable.engine.delegate.DelegateExecution;
import org.flowable.engine.delegate.JavaDelegate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Component;

import com.ecm.core.entity.EcmDocument;
import com.ecm.core.entity.EcmFolder;
import com.ecm.core.exception.AccessDeniedException;
import com.ecm.core.exception.EcmException;
import com.ecm.core.exception.NoPermissionException;
import com.ecm.core.service.AuthService;
import com.ecm.core.service.DocumentService;
import com.ecm.core.service.FolderPathService;
import com.ecm.core.service.FolderService;
import com.ecm.icore.service.IEcmSession;
import com.ecm.portal.archivegc.utils.EcmSessionFactory;
/**
 * 文档提交流程，发布到整编库
 * @author Atos
 *
 */
@Component(value="deploy2RecordStorage")
public class Deploy2RecordStorageListener implements JavaDelegate {
	private Logger log=LoggerFactory.getLogger(this.getClass());
	@Autowired
	DocumentService documentService;
	@Autowired
	private Environment env;
	@Autowired
	private AuthService authService;
	@Autowired
	private FolderService folderService;
	@Autowired
	private FolderPathService folderPathService;
	@Override
	public void execute(DelegateExecution execution) {
		// TODO Auto-generated method stub

		IEcmSession session=EcmSessionFactory.getWorkflowSession(env, authService);
		String token=session.getToken();
		String formId= execution.getVariable("formId").toString();
		String sql="select child_id as ID from ecm_relation where name='irel_children' and parent_id='"+formId+"'"
				+ " union select child_id as ID from ecm_relation where parent_id in(select child_id from ecm_relation"
				+ " where parent_id ='"+formId+"')";
		String processId =execution.getProcessInstanceId();
		String receiver = getReceiver(token, processId);
		try {
			List<Map<String,Object>> objList= documentService.getMapList(token, sql);
			for (Map<String, Object> map : objList) {
				String relevantArchiveId= map.get("ID").toString();
				EcmDocument arrchive= documentService.getObjectById(token, relevantArchiveId);
				
				String folderId= folderPathService.getFolderId(token, arrchive.getAttributes(), "2");
				
				EcmFolder folder= folderService.getObjectById(token, folderId);
				arrchive.setFolderId(folderId);
				arrchive.setAclName(folder.getAclName());
				arrchive.setStatus("整编");
				if(receiver!=null) {
					arrchive.getAttributes().put("C_RECEIVE", receiver);
					arrchive.getAttributes().put("C_RECEIVE_DATE", new Date());
				}
				documentService.updateObject(token, arrchive, null);
				
			}
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			log.error(e.getMessage());
			
		}finally {
			if(session!=null) {
				EcmSessionFactory.releaseSession(authService, session);
			}
		}
	
	}
	
	private String getReceiver(String token, String processId) {
		String sql = "select ASSIGNEE from ecm_audit_workitem where PROCESS_INSTANCE_ID='"+processId+"' and TASK_NAME='文档提交检查' order by CREATE_TIME desc";
		List<Map<String, Object>> objList;
		try {
			objList = documentService.getMapList(token, sql);
			for (Map<String, Object> map : objList) {
				return map.get("ASSIGNEE").toString();
			}
		} catch (EcmException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return null;
	}

}
