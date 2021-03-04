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
import com.ecm.core.service.AuthService;
import com.ecm.core.service.DocumentService;
import com.ecm.core.service.FolderPathService;
import com.ecm.core.service.FolderService;
import com.ecm.icore.service.IEcmSession;
@Component(value = "changeAclListener")
public class changeAclListener implements JavaDelegate {
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
			Map<String,Object> ecmAttr = ecmObject.getAttributes();
			//开始改文件Acl
			String sql = "select * from ecm_relation where parent_id = '"+formId+"'";		
			List<Map<String,Object>> mps = documentService.getMapList(ecmSession.getToken(), sql);		//找到表单挂载文件关系集
			if(mps!=null) {				//有文件就改，没文件就跳过
			for(Map<String,Object> mp : mps) {
			String id = mp.get("ID").toString();
			String sql4AJ = "select * from ecm_relation where parent_id = '"+id+"'";
			List<Map<String,Object>> AJmps = documentService.getMapList(ecmSession.getToken(), sql4AJ);
			if(AJmps!=null) {
			for(Map<String,Object> AJmp:AJmps) {
				String childId = AJmp.get("CHILD_ID").toString();
				EcmDocument child = documentService.getObjectById(ecmSession.getToken(), childId);
				Map<String,Object> childAttr = child.getAttributes();
				childAttr.put("ACL_NAME", "acl_all_write");
				documentService.updateObject(ecmSession.getToken(),childAttr);
				}
			}

		}
	}
				
		
	}
		catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
}
}
