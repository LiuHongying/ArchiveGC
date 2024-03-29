package com.ecm.portal.archivegc.workflowEvent;

import org.flowable.engine.delegate.DelegateExecution;
import org.flowable.engine.delegate.ExecutionListener;
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
@Component(value="dirDraft2DeliveryListener")
public class DirDraft2DeliveryListener implements ExecutionListener {

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
	public void notify(DelegateExecution execution) {

		IEcmSession session=EcmSessionFactory.getWorkflowSession(env, authService);
		String token=session.getToken();
		String formId= execution.getVariable("formId").toString();
		EcmDocument document= documentService.getObjectById(token, formId);
		try {
			String folderId= folderPathService.getFolderId(token, document.getAttributes(), "1");
			
			EcmFolder folder= folderService.getObjectById(token, folderId);
			document.setFolderId(folderId);
			document.setAclName(folder.getAclName());
			
			documentService.updateObject(token, document, null);
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

}
