package com.ecm.portal.archivegc.workflowEvent;

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
import com.ecm.core.entity.EcmUser;
import com.ecm.core.service.AuthService;
import com.ecm.core.service.DocumentService;
import com.ecm.core.service.FolderPathService;
import com.ecm.core.service.FolderService;
import com.ecm.icore.service.IEcmSession;

@Component(value = "JudgeInsideListener")
public class JudgeInsideListener implements JavaDelegate{
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
			if(varMap.get("SUB_TYPE").toString().equals("纸质到馆查阅")) {
				execution.setVariable("isOK", "是");				//这个时候先默认是查阅&&密级为内部公开
			
			String sql = "select  distinct C_SECURITY_LEVEL from ecm_document where id in(select CHILD_ID from ecm_relation where parent_id = '"+formId+"')";
			List<Map<String,Object>> Res = documentService.getMapList(ecmSession.getToken(), sql);
			for(Map<String,Object> mp : Res) {
			String level = mp.get("C_SECURITY_LEVEL").toString();
			if((!level.equals("非密")||!level.equals("内部公开"))&&varMap.get("SUB_TYPE").toString().equals("纸质到馆查阅")) {
				execution.setVariable("isOK", "否");				//有任意子文件不满足条件，跳出
				break;
			}
			}
			}
			else if(!varMap.get("SUB_TYPE").toString().equals("纸质到馆查阅")) {
				execution.setVariable("isOK", "否");
			}
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		logger.info("完成");
	}
}
