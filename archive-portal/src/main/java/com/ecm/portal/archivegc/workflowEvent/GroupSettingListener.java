package com.ecm.portal.archivegc.workflowEvent;

import java.util.List;
import java.util.Map;

import org.flowable.common.engine.api.delegate.Expression;
import org.flowable.engine.delegate.DelegateExecution;
import org.flowable.engine.delegate.ExecutionListener;
import org.flowable.engine.delegate.TaskListener;
import org.flowable.task.service.delegate.DelegateTask;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Component;

import com.ecm.core.entity.EcmDocument;
import com.ecm.core.exception.AccessDeniedException;
import com.ecm.core.exception.EcmException;
import com.ecm.core.exception.NoPermissionException;
import com.ecm.core.service.AuthService;
import com.ecm.core.service.DocumentService;
import com.ecm.icore.service.IEcmSession;
import com.ecm.portal.archivegc.utils.EcmSessionFactory;
@Component(value="groupSettingListener")
public class GroupSettingListener implements ExecutionListener {
	private Expression assignName;
	
	@Autowired
	DocumentService documentService;
	@Autowired
	private AuthService authService;
	@Autowired
	private Environment env;
	

	@Override
	public void notify(DelegateExecution execution) {
		String columnName= assignName.getValue(execution).toString();
		String formId= execution.getVariable("formId").toString();
		IEcmSession session=EcmSessionFactory.getWorkflowSession(env, authService);
		String sql="select b.* from ecm_relation a,ecm_document b where a.child_id=b.id and a.parent_id='"+formId+"'";
		try {
			List<Map<String,Object>> childrenDoc= documentService.getMapList(session.getToken(), sql);
			if(childrenDoc!=null&&childrenDoc.size()>0) {
				Map<String,Object> docAttrs=childrenDoc.get(0);
				String attrVal= docAttrs.get("C_ARC_CLASSIC").toString();
				switch(attrVal) {
				case"工程设计":
					//设计档案管理员
					execution.setVariable(columnName,"设计档案管理员");
					break;
				case"商务管理":
					//商务档案管理员
					execution.setVariable(columnName,"商务档案管理员");
					break;
				case"科技与信息":
					//科研档案管理员
					execution.setVariable(columnName,"科研档案管理员");
					break;
				case"工程建设":
					//项目档案管理员
					execution.setVariable(columnName,"项目档案管理员");
					break;
				
				default:
					//其他档案管理员
					execution.setVariable(columnName,"其他档案管理员");
					break;
				}
				EcmDocument ecmObject = documentService.getObjectById(session.getToken(), formId);
				ecmObject.addAttribute(columnName, execution.getVariable(columnName));
				try {
					documentService.updateObject(session.getToken(), ecmObject,null);
				} catch (AccessDeniedException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				} catch (NoPermissionException e) {
					e.printStackTrace();
				}
			}
		} catch (EcmException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	
	
		
	}

}
