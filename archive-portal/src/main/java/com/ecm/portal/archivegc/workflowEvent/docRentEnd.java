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
@Component(value = "docRentEndListener")
public class docRentEnd implements JavaDelegate {
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
			Map<String,Object> ecmAttr = ecmObject.getAttributes();
			//下面开始改密级
			String sqls = "select  distinct C_SECURITY_LEVEL from ecm_document where id in(select CHILD_ID from ecm_relation where parent_id = '"+formId+"')";
			List<Map<String,Object>> Res = documentService.getMapList(ecmSession.getToken(), sqls);
			for(Map<String,Object> mp : Res) {
			String level = mp.get("C_SECURITY_LEVEL").toString();
			if(level.equals("普通商密")) {
			ecmAttr.put("C_SECURITY_LEVEL", level);
			break;
			}
			if(level.equals("受限")) {
			ecmAttr.put("C_SECURITY_LEVEL", level);
			break;
			}
			if(level.equals("内部公开")){
			ecmAttr.put("C_SECURITY_LEVEL", level);
			break;	
			}
			}
			//改密级完毕，现在开始改表单
			if(!type.equals("纸质借阅")||!type.equals("纸质复制")) {
				ecmAttr.put("STATUS", "已审批");
				documentService.updateObject(ecmSession.getToken(), ecmAttr);
			}
			
			if(type.equals("纸质借阅")||type.equals("纸质复制")) {
				
				String sql = "select * from ecm_relation where parent_id = '"+formId+"'";		
				List<Map<String,Object>> mps = documentService.getMapList(ecmSession.getToken(), sql);		//找到表单挂载文件关系集
				if(mps!=null) {
				for(Map<String,Object> mp : mps) {
				String id =	mp.get("CHILD_ID").toString();
				EcmDocument doc = documentService.getObjectById(ecmSession.getToken(), id);		//找到表单挂载文件了
				Map<String,Object> docAttr = doc.getAttributes();
				docAttr.put("STATUS", "待出库");
				documentService.updateObject(ecmSession.getToken(),docAttr);
				}}
				ecmAttr.put("STATUS", "待出库");
				documentService.updateObject(ecmSession.getToken(), ecmAttr);

			}
	}
		catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
}
}
