package com.ecm.portal.archivegc.workflowEvent;

import org.flowable.common.engine.impl.el.FixedValue;
import org.flowable.engine.delegate.DelegateExecution;
import org.flowable.engine.delegate.JavaDelegate;
import org.springframework.stereotype.Component;


/**
 * 
 * @author Matthew
 * change on 2020年11月11日14:10:51
 */
@Component(value = "DistributeGroupListener")
public class DistributeByGroupName implements JavaDelegate{
	private FixedValue value;
	@Override
	public void execute(DelegateExecution execution) {
		String groupName = value.getExpressionText();
		try {
			execution.setVariable("C_REVIEWER1", groupName);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
}
