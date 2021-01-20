package com.ecm.portal.archivegc.workflowEvent;

import java.util.List;
import java.util.Map;
import org.flowable.engine.delegate.DelegateExecution;
import org.flowable.engine.delegate.JavaDelegate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Component;

import com.ecm.core.entity.EcmDocument;
import com.ecm.core.service.AuthService;
import com.ecm.core.service.DocumentService;
import com.ecm.core.service.RelationService;
import com.ecm.icore.service.IEcmSession;

/**
 * 
 * @author Matthew
 * change on 2020年11月11日14:10:51
 */
@Component(value = "judgeListener")
public class JudgeDocIncludePaper implements JavaDelegate{

	@Autowired
	private DocumentService documentService;
	@Autowired
	private AuthService authService;
	@Autowired
	private Environment env;
	@Autowired
	private RelationService relationService;
	
	@Override
	public void execute(DelegateExecution execution) {
		String workflowSpecialUserName = env.getProperty("ecm.username");
		IEcmSession ecmSession = null;
		try {
			ecmSession = authService.login("workflow", workflowSpecialUserName, env.getProperty("ecm.password"));
			Map<String, Object> varMap = execution.getVariables();
			String formId = varMap.get("formId").toString();
			String formSqlStr = "select distinct C_INCLUDE_PAPER as hasPaper from ecm_document where id in (select child_id from ecm_relation where parent_id ='"+formId+"' and name = 'irel_children')";
			List<Map<String, Object>> list = documentService.getMapList(ecmSession.getToken(), formSqlStr);
			Boolean flag = false;
			for (Map<String, Object> map : list) {
				if(map == null) {
					continue;
				}
				if (map.get("hasPaper").equals("有")) {
					flag = true;
					break;
				}
			}
			EcmDocument ecmObject = documentService.getObjectById(ecmSession.getToken(), formId);
			if (flag) {
				ecmObject.addAttribute("C_INCLUDE_PAPER", "有");
				execution.setVariable("C_INCLUDE_PAPER", "有");
			}else {
				ecmObject.addAttribute("C_INCLUDE_PAPER", "无");
				execution.setVariable("C_INCLUDE_PAPER", "无");
			}
			documentService.updateObject(ecmSession.getToken(), ecmObject, null);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
}
