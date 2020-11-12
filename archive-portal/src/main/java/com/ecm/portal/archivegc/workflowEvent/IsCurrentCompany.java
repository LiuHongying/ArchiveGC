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

@Component(value = "companyListener")
public class IsCurrentCompany implements JavaDelegate{
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
			EcmDocument form = documentService.getObjectById(ecmSession.getToken(), formId);	//表单对象
			String creator = form.getCreator();			//表单创建人
			String sqlCreate="select * from ecm_user where Name = '" + creator + "'";
			List<Map<String,Object>> createRes = documentService.getMapList(ecmSession.getToken(), sqlCreate);
			String creatorGroup = createRes.get(0).get("GROUP_NAME").toString();	//表单创建人所属部门
			
			String sql = "select distinct C_CREATE_UNIT from ecm_document where id in(select CHILD_ID from ecm_relation where parent_id = '"+formId+"')";
			List<Map<String,Object>> mps = documentService.getMapList(ecmSession.getToken(), sql);
			for(Map<String,Object> mp : mps) {
				if(mp.get("C_CREATE_UNIT")==null){			//要是文件没所属部门就当是自己部门的
					execution.setVariable("IS_CURRENT_COMPANY", "是");
				}
				if(mp.get("C_CREATE_UNIT")!=null){
				String department = mp.get("C_CREATE_UNIT").toString();
				if(department.equals(creatorGroup)) {		//要是自己部门的文件就先设置为true
					execution.setVariable("IS_CURRENT_COMPANY", "是");
				}
				if(!department.equals(creatorGroup)) {		//只要遇到一次不是自己部门的文件，就直接设为false，后跳出循环
					execution.setVariable("IS_CURRENT_COMPANY", "否");
					break;
				}
				}
			}
			
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		logger.info("完成");
	}
}
