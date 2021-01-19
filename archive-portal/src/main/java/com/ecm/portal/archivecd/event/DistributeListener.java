package com.ecm.portal.archivecd.event;

import java.util.Map;

import org.flowable.task.service.delegate.DelegateTask;
import org.flowable.task.service.delegate.TaskListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Component;

import com.ecm.core.service.AuthService;
import com.ecm.icore.service.IEcmSession;
@Component(value = "distributeListener")
public class DistributeListener implements TaskListener {
	@Autowired
	private Environment env;
	@Autowired
	private Distribute distributeComponent;
	
	@Autowired
	private AuthService authService;
	@Override
	public void notify(DelegateTask task) {
		// TODO Auto-generated method stub
		if ("complete".equals(task.getEventName())) {
			if (task.getVariable("processInstanceID") == null) {
				task.setVariable("processInstanceID", task.getProcessInstanceId());
				task.setVariable("processName", task.getProcessDefinitionId().split(":")[0]);
			}
			IEcmSession ecmSession = null;
			String workflowSpecialUserName = env.getProperty("ecm.username");
			try {
				Map<String, Object> varMap = task.getVariables();
				String formId = varMap.get("formId").toString();
				ecmSession = authService.login("workflow", workflowSpecialUserName, env.getProperty("ecm.password"));
				distributeComponent.orderForMainDistribution(ecmSession.getToken(), formId);
			} catch (Exception e) {
				// TODO: handle exception
				e.printStackTrace();
			} finally {
				if (ecmSession != null) {
					authService.logout(workflowSpecialUserName);
				}
			}

		}
		System.out.println("taskListener_notify");

	}

}
