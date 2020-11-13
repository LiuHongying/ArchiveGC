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

@Component(value = "documentListener")
public class countDocuments implements JavaDelegate{
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
	private int	AN;				//案卷
	private int WJ;				//文件
	@Override
	public void execute(DelegateExecution execution) {
		// TODO Auto-generated method stub
		AN = 0;
		WJ = 0;								//初始化计数器
		String workflowSpecialUserName = env.getProperty("ecm.username");
		IEcmSession ecmSession = null;
		try {
			ecmSession = authService.login("workflow", workflowSpecialUserName, env.getProperty("ecm.password"));
			Map<String, Object> varMap = execution.getVariables();
			String formId = varMap.get("formId").toString();
			EcmDocument form = documentService.getObjectById(ecmSession.getToken(), formId);	//表单对象
			String sql = "select  C_ITEM_TYPE from ecm_document where id in(select CHILD_ID from ecm_relation where parent_id = '"+formId+"')";
			List<Map<String,Object>> Res = documentService.getMapList(ecmSession.getToken(), sql);
			for(Map<String,Object> mp : Res) {
				String type = mp.get("C_ITEM_TYPE").toString();
				if(type.equals("案卷")) {
					AN++;
				}
				if(type.equals("文件")) {
					WJ++;
				}
			}
			if(AN>10||WJ>50) {
				execution.setVariable("MoreThan50", "是");
			}
			else {
				execution.setVariable("MoreThan50", "否");
			}
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		logger.info("完成");
	}
}
