package com.ecm.portal.archivegc.workflowEvent;

import java.util.List;
import java.util.Map;

import org.flowable.common.engine.impl.el.FixedValue;
import org.flowable.engine.delegate.DelegateExecution;
import org.flowable.engine.delegate.JavaDelegate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Component;

import com.alibaba.druid.util.StringUtils;
import com.ecm.core.entity.EcmDocument;
import com.ecm.core.service.AuthService;
import com.ecm.core.service.DocumentService;
import com.ecm.icore.service.IEcmSession;


/**
 * 
 * @author Matthew
 * change on 2020年11月12日17:06:55
 */
@Component(value = "ChangeStatusListener")
public class ChangeFormDocStatus implements JavaDelegate{
	@Autowired
	private AuthService authService;
	@Autowired
	private Environment env;
	@Autowired
	private DocumentService documentService;
	
	//流程中配置的表单的状态
	private FixedValue formStatus;
	//流程中配置的文档的状态
	private FixedValue docStatus;
	
	@Override
	public void execute(DelegateExecution execution) {
		String workflowSpecialUserName = env.getProperty("ecm.username");
		IEcmSession ecmSession = null;
		String formChangeStatus = formStatus.getExpressionText();
		String docChangeStatus = docStatus.getExpressionText();
		
		try {
			ecmSession = authService.login("workflow", workflowSpecialUserName, env.getProperty("ecm.password"));
			Map<String, Object> varMap = execution.getVariables();
			String formId = varMap.get("formId").toString();
			//获取表单关联子文件的id
			String getChildSql = "select id from ecm_document where id in (select child_id from ecm_relation where parent_id ='"+formId+"' and name = 'irel_children')";
			EcmDocument ecmObject = documentService.getObjectById(ecmSession.getToken(), formId);
			if (!StringUtils.isEmpty(formChangeStatus)) {
				ecmObject.setStatus(formChangeStatus);
				documentService.updateObject(ecmSession.getToken(), ecmObject, null);
			}
			//如果传了文档状态再改变文档状态
			if (!"无".equals(docChangeStatus)) {
				List<Map<String, Object>> list = documentService.getMapList(ecmSession.getToken(), getChildSql);
				for (Map<String, Object> map : list) {
					EcmDocument doc = documentService.getObjectById(ecmSession.getToken(), (String)map.get("id"));
					doc.addAttribute("C_STORE_STATUS", docChangeStatus);
					documentService.updateObject(ecmSession.getToken(), doc, null);
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}
