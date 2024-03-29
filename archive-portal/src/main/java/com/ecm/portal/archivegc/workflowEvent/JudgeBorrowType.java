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
import com.ecm.core.service.AuthService;
import com.ecm.core.service.DocumentService;
import com.ecm.core.service.FolderPathService;
import com.ecm.core.service.FolderService;
import com.ecm.icore.service.IEcmSession;
@Component(value = "JudgeBorrowTypeListener")
public class JudgeBorrowType implements JavaDelegate {
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
		String workflowSpecialUserName = env.getProperty("ecm.username");
		IEcmSession ecmSession = null;
		try {
			ecmSession = authService.login("workflow", workflowSpecialUserName, env.getProperty("ecm.password"));
			Map<String, Object> varMap = execution.getVariables();
			String formId = varMap.get("formId").toString();
			
			EcmDocument ecmObject = documentService.getObjectById(ecmSession.getToken(), formId);
			String type = varMap.get("SUB_TYPE").toString();
			if(!type.equals("纸质到馆借阅")||!type.equals("纸质复制")) {
				ecmObject.addAttribute("C_INCLUDE_PAPER", "无");
				execution.setVariable("C_INCLUDE_PAPER", "无");
			}
			if(type.equals("纸质到馆借阅")||type.equals("纸质复制")||type.equals("纸质到馆查阅")) {
				execution.setVariable("C_INCLUDE_PAPER", "有");
				ecmObject.addAttribute("C_INCLUDE_PAPER", "有");
			}
			documentService.updateObject(ecmSession.getToken(), ecmObject, null);
	}
		catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
}
}
