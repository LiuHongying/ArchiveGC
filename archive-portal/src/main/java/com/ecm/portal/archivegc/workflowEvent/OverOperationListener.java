package com.ecm.portal.archivegc.workflowEvent;

import org.flowable.engine.delegate.DelegateExecution;
import org.flowable.engine.delegate.JavaDelegate;
import org.springframework.stereotype.Component;
@Component(value="overOperation")
public class OverOperationListener implements JavaDelegate {

	@Override
	public void execute(DelegateExecution execution) {
		// TODO Auto-generated method stub

	}

}
