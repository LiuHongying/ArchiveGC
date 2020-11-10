package com.ecm.portal.archivegc.workflowEvent;

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
import com.ecm.core.service.AuthService;
import com.ecm.core.service.DocumentService;
import com.ecm.core.service.FolderPathService;
import com.ecm.core.service.FolderService;
import com.ecm.icore.service.IEcmSession;
import com.ecm.portal.log.LogAopAction;
@Component(value = "completeListener")
public class DocCommitComplete implements JavaDelegate{
	@Autowired
	private AuthService authService;
	@Autowired
	private Environment env;
	@Autowired
    private FolderPathService folderPathService;
	@Autowired
	private DocumentService documentService;
	@Autowired
	private FolderService folderService;
	private final Logger logger = LoggerFactory.getLogger(DocCommitComplete.class);
	@Override
	public void execute(DelegateExecution execution) {
		// TODO Auto-generated method stub
		String workflowSpecialUserName = env.getProperty("ecm.username");
		IEcmSession ecmSession = null;
		try {
			ecmSession = authService.login("workflow", workflowSpecialUserName, env.getProperty("ecm.password"));
			Map<String, Object> varMap = execution.getVariables();
			String formId = varMap.get("formId").toString();
			
			EcmDocument ecmObject = documentService.getObjectById(ecmSession.getToken(), formId);
			ecmObject.setStatus("完成");
			String folderId="";
			folderId= folderPathService.getFolderId(ecmSession.getToken(), ecmObject.getAttributes(),"3");
			EcmFolder folder= folderService.getObjectById(ecmSession.getToken(), folderId);
			ecmObject.setFolderId(folderId);
			ecmObject.setAclName(folder.getAclName());
			documentService.updateObject(ecmSession.getToken(), ecmObject,null);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		logger.info("完成");
	}
	
}
