package com.ecm.portal.archivegc.workflowEvent;

import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;
import javax.script.ScriptException;

import org.flowable.bpmn.model.FieldExtension;
import org.flowable.engine.delegate.DelegateExecution;
import org.flowable.engine.delegate.DelegateHelper;
import org.flowable.engine.delegate.ExecutionListener;
import org.flowable.engine.delegate.TaskListener;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Component;

import com.ecm.common.util.DateUtils;
import com.ecm.core.cache.manager.CacheManagerOper;
import com.ecm.core.db.DBFactory;
import com.ecm.core.entity.EcmAttribute;
import com.ecm.core.entity.EcmDocument;
import com.ecm.core.exception.AccessDeniedException;
import com.ecm.core.exception.EcmException;
import com.ecm.core.exception.NoPermissionException;
import com.ecm.core.service.AuthService;
import com.ecm.core.service.DocumentService;
import com.ecm.icore.service.IEcmSession;
import com.ecm.portal.archivegc.utils.EcmSessionFactory;
@Component(value = "changeColumnValueListener")
public class ChangeColumnValue implements ExecutionListener {
	private static final String JS_ENGINE_NAME = "Nashorn";
	private final ScriptEngineManager sem = new ScriptEngineManager();
	private final ScriptEngine engine = sem.getEngineByName(JS_ENGINE_NAME);
	private final Logger log = LoggerFactory.getLogger(ChangeColumnValue.class);
	
	@Autowired
	private DocumentService documentService;
	@Autowired
	private AuthService authService;
	@Autowired
	private Environment env;

	private void updateParam(DelegateExecution execution) throws ScriptException, NoPermissionException, AccessDeniedException, EcmException {
		List<FieldExtension> set = DelegateHelper.getFields(execution);
		
		List<FieldExtension> names = set.stream().filter(t -> t.getFieldName().startsWith("@")).collect(Collectors.toList());
		String formId = execution.getVariable("formId", String.class);
		IEcmSession session = EcmSessionFactory.getWorkflowSession(env, authService);
		EcmDocument form = documentService.getObjectById(session.getToken(), formId);
		for (int i = 0; names != null && i < names.size(); i++) {
			String name = names.get(i).getFieldName();
			String val = names.get(i).getStringValue();
			Object valObj = form.getAttributeValue(name.substring(1));
			if (valObj == null||"".equals(valObj.toString())) {
				EcmAttribute attr = CacheManagerOper.getEcmAttributes().get(name.substring(1));
				int type = attr.getFieldType();
				switch (type) {
				case 3:// 布尔
				{
					Object result =  engine.eval(val);
					boolean b= Boolean.parseBoolean(result.toString());
					form.addAttribute(name.substring(1), b);
					break;
				}
				case 4://
				case 5:
				case 6:
				case 7:
				case 8: {
					engine.put(name.substring(1), 0);
					Object result = engine.eval(val);
					form.addAttribute(name.substring(1), result);
					break;
				}
				default:// 字符串
				{
					engine.put(name.substring(1), "");
					Object result = engine.eval(val);
					form.addAttribute(name.substring(1), result);
					break;
				}

				}
			}else {

				EcmAttribute attr = CacheManagerOper.getEcmAttributes().get(name.substring(1));
				int type = attr.getFieldType();
				switch (type) {
				case 3:// 布尔
				{
					Object result =  engine.eval(val);
					boolean b= Boolean.parseBoolean(result.toString());
					form.addAttribute(name.substring(1), b);
					break;
				}
				case 4://
				case 5:
				case 6:
				case 7:
				case 8: {
					engine.put(name.substring(1), Integer.parseInt(valObj.toString()));
					Object result = engine.eval(val);
					form.addAttribute(name.substring(1), result);
					break;
				}
				default:// 字符串
				{
					engine.put(name.substring(1), valObj.toString());
					Object result = engine.eval(val);
					form.addAttribute(name.substring(1), result);
					break;
				}

				}
			
			}
		}
		documentService.updateObject(session.getToken(), form, null);
	}

	@Override
	public void notify(DelegateExecution execution) {
		// TODO Auto-generated method stub
		try {
			updateParam(execution);
		} catch (ScriptException | NoPermissionException | AccessDeniedException | EcmException e) {
			// TODO Auto-generated catch block
			log.error(e.getMessage());
			e.printStackTrace();
		}
	}

}
