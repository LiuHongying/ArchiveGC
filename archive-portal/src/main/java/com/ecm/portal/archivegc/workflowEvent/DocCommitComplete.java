package com.ecm.portal.archivegc.workflowEvent;

import org.flowable.engine.delegate.DelegateExecution;
import org.flowable.engine.delegate.JavaDelegate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.ecm.portal.log.LogAopAction;

public class DocCommitComplete implements JavaDelegate{
	private final Logger logger = LoggerFactory.getLogger(DocCommitComplete.class);
	@Override
	public void execute(DelegateExecution execution) {
		// TODO Auto-generated method stub
		logger.info("完成");
	}
	
}
