package com.ecm.portal.archivegc.workflowEvent;

import java.text.SimpleDateFormat;
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
import com.ecm.core.service.AuthService;
import com.ecm.core.service.DocumentService;
import com.ecm.core.service.FolderPathService;
import com.ecm.core.service.FolderService;
import com.ecm.core.service.RelationService;
import com.ecm.icore.service.IEcmSession;
@Component(value = "CancelListener")
public class CancelListener implements JavaDelegate {
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
	@Autowired
	private RelationService relationService;
	private final Logger logger = LoggerFactory.getLogger(DocCommitComplete.class);
	@Override
	public void execute(DelegateExecution execution) {
		SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd");
		String workflowSpecialUserName = env.getProperty("ecm.username");
		IEcmSession ecmSession = null;
		try {
			ecmSession = authService.login("workflow", workflowSpecialUserName, env.getProperty("ecm.password"));
			Map<String, Object> varMap = execution.getVariables();
			String formId = varMap.get("formId").toString();
			String sql = "select * from ecm_relation where parent_id = '"+formId+"'";
			List<Map<String,Object>> mps = documentService.getMapList(ecmSession.getToken(), sql);
			EcmDocument ecmObject = documentService.getObjectById(ecmSession.getToken(), formId);
			execution.setVariable("COMPLETE", "是");
			Map<String,Object> forms = ecmObject.getAttributes();
			forms.put("STATUS", "已完成");
			documentService.updateObject(ecmSession.getToken(), forms);
			for(Map<String,Object> mp : mps) {
				String id = mp.get("CHILD_ID").toString();
				EcmDocument child = documentService.getObjectById(ecmSession.getToken(), id);
				Map<String,Object> childAttr = child.getAttributes();
				childAttr.put("STATUS", "作废");
				childAttr.put("TITLE","(作废)"+child.getTitle());
				Date now = new Date();
				String dateStr = df.format(now);
				childAttr.put("C_COMMENT",dateStr+"作废");
				childAttr.put("IS_RELEASED", "0");
				childAttr.put("ACL_NAME", "acl_canceled");
				childAttr.put("C_CANCEL_DATE",now);
				childAttr.put("C_STORE_STATUS", "已作废");
				documentService.updateObject(ecmSession.getToken(), childAttr);
			}
		
	}
		catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
}
}