package com.ecm.portal.archivegc.workflowEvent;

import org.flowable.engine.delegate.ExecutionListener;
import org.flowable.task.api.history.HistoricTaskInstance;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;

import org.flowable.engine.HistoryService;
import org.flowable.engine.delegate.DelegateExecution;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Component;

import com.alibaba.druid.util.StringUtils;
import com.ecm.core.cache.manager.CacheManagerOper;
import com.ecm.core.entity.EcmContent;
import com.ecm.core.entity.EcmDocument;
import com.ecm.core.entity.EcmUser;
import com.ecm.core.service.AuthService;
import com.ecm.core.service.ContentService;
import com.ecm.core.service.DocumentService;
import com.ecm.core.service.UserService;
import com.ecm.icore.service.IEcmSession;
import com.ecm.portal.archivegc.common.dataexchange.WordUtils;
import com.ecm.portal.archivegc.utils.EcmSessionFactory;
@Component(value="dataExchangeListener")
public class DataExchangeListener implements ExecutionListener {
	private static final long serialVersionUID = 1L;
	private Logger log=LoggerFactory.getLogger(this.getClass());
	@Autowired
	DocumentService documentService;
	@Autowired
	private Environment env;
	@Autowired
	private AuthService authService;
	@Autowired
	private ContentService contentService;
	@Autowired
	private UserService userService;
	@Autowired
	private HistoryService historyService;
	
	@Override
	public void notify(DelegateExecution execution) {

		IEcmSession session=EcmSessionFactory.getWorkflowSession(env, authService);
		String token=session.getToken();
		String formId= execution.getVariable("formId").toString();
		EcmDocument document= documentService.getObjectById(token, formId);
		try {
			String processInstanceId = 	execution.getProcessInstanceId();
		
			List<ArrayList<String>> resultList = new ArrayList<ArrayList<String>>();
			List<HistoricTaskInstance> tasks = historyService.createHistoricTaskInstanceQuery()
					.processInstanceId(processInstanceId).orderByTaskCreateTime().asc().list();
			for (HistoricTaskInstance task : tasks) {
				ArrayList<String> array = new ArrayList<String>();
				array.add(task.getName());
				array.add(task.getAssignee());
				EcmUser assigneeName = userService.getObjectByName(token, task.getAssignee());
				array.add(assigneeName.getSignImage());
				
				SimpleDateFormat f = new SimpleDateFormat("yyyy-MM-dd");
				array.add(task.getEndTime()==null?"":f.format(task.getEndTime()));
				resultList.add(array);
			}		
			String id=document.getId();
			
//			String reviewerName = "testlgx";		
//			String reviewerTime = "2021-01-08";
//			EcmUser userObj1 = userService.getObjectByName(token, reviewerName);
//			String reviewerNameImg = userObj1.getSignImage();
//		
//			String approverName="testlgx";		
//			String approverTime="2021-01-09";
//			EcmUser userObj2 = userService.getObjectByName(token, approverName);
//			String approverNameImg = userObj2.getSignImage();
			
			EcmContent en = null;
			if (!StringUtils.isEmpty(document.getFormatName()==null?"":document.getFormatName())) {
					en = contentService.getObject(token, id, 0, document.getFormatName());
			} else {
				en = contentService.getPrimaryContent(token, id);
			}
			String fullPath = CacheManagerOper.getEcmStores().get(en.getStoreName()).getStorePath();
			String TemplatePath = fullPath+en.getFilePath();
			
			WordUtils util = new WordUtils();
			util.setTemplatePath(TemplatePath);
			util.exchangeData(resultList,document,0); //,reviewerName,reviewerTime,reviewerNameImg,approverName,approverTime,approverNameImg
			
				
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			log.error(e.getMessage());
			
		}finally {
			if(session!=null) {
				EcmSessionFactory.releaseSession(authService, session);
			}
		}
	
	
	
	}

}
