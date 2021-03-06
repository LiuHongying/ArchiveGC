package com.ecm.portal.archivegc.workflowEvent;

import java.util.Date;
import java.util.List;
import java.util.Map;

import org.flowable.engine.delegate.TaskListener;
import org.flowable.task.service.delegate.DelegateTask;
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
 * 将库房中的案卷（合装文件）移交至整编库
 * @author Administrator
 *
 */
@Component(value="moveReorganizeListener")
public class MoveReorganizeListener implements TaskListener{
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
	public void notify(DelegateTask task) {
		// TODO Auto-generated method stub
		if ("create".equals(task.getEventName())) {
			IEcmSession session=EcmSessionFactory.getWorkflowSession(env, authService);
			String token=session.getToken();
			String formId= task.getVariable("formId").toString();
			String sql="select child_id as ID from ecm_relation where name='irel_parent' and parent_id='"+formId+"'";
			try {
				List<Map<String,Object>> objList= documentService.getMapList(token, sql);
				for (Map<String, Object> map : objList) {
					String relevantArchiveId= map.get("ID").toString();
					EcmDocument arrchive= documentService.getObjectById(token, relevantArchiveId);
					
					String folderId= folderPathService.getFolderId(token, arrchive.getAttributes(), "2");
					
					EcmFolder folder= folderService.getObjectById(token, folderId);
					arrchive.setFolderId(folderId);
					arrchive.setAclName(folder.getAclName());
					arrchive.getAttributes().put("C_RECEIVE", task.getAssignee());
					arrchive.getAttributes().put("C_RECEIVE_DATE", new Date());
					documentService.updateObject(token, arrchive, null);
					
				}
			} catch (EcmException | NoPermissionException | AccessDeniedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
				log.error(e.getMessage());
				
			}finally {
				if(session!=null) {
					EcmSessionFactory.releaseSession(authService, session);
				}
			}
		}
		
	}

}
