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
@Component(value = "DocDestoryListener")
public class DocDestory implements JavaDelegate {
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
			String sql = "select * from ecm_relation where parent_id = '"+formId+"'";
			List<Map<String,Object>> mps = documentService.getMapList(ecmSession.getToken(), sql);
			EcmDocument ecmObject = documentService.getObjectById(ecmSession.getToken(), formId);
			for(Map<String,Object> mp : mps) {
				String type = new String();
				String id = mp.get("CHILD_ID").toString();		//当前遍历文件id
				EcmDocument doc = documentService.getObjectById(ecmSession.getToken(), id);		//先找到对应文件
				Map<String,Object> docMap = doc.getAttributes(); 
				docMap.put("ACL_NAME","acl_destory");
				docMap.put("STATUS", "已销毁");
				docMap.put("IS_RELEASED",0);
				documentService.updateObject(ecmSession.getToken(), docMap);		//先把当前文件的状态改了
				if(docMap.get("C_ITEM_TYPE")!=null) {			//要是当前文件是案卷，就连着案卷内的挂载文件一起删
					type = docMap.get("C_ITEM_TYPE").toString();
					if(docMap.get("C_ITEM_TYPE").toString().equals("案卷")) {
						String AttachSql = "select * from ecm_relation where parent_id = '"+id+"'";
						List<Map<String,Object>> attachMps = documentService.getMapList(ecmSession.getToken(), AttachSql);
						for(Map<String,Object> attMp : attachMps) {			//找到对应的案卷关系了
						String attachId = attMp.get("CHILD_ID").toString();
						EcmDocument AJ = documentService.getObjectById(ecmSession.getToken(), attachId);
						Map<String,Object> AJmap = AJ.getAttributes();
						AJmap.put("ACL_NAME","acl_destory");
						AJmap.put("STATUS", "已销毁");
						AJmap.put("IS_RELEASED",0);
						documentService.updateObject(ecmSession.getToken(), AJmap);		//更新案卷内关联文件的属性
						}
					}
				}
			}

			ecmObject.setStatus("已完成");
			documentService.updateObject(ecmSession.getToken(), ecmObject,null);
	}
		catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
}
}
