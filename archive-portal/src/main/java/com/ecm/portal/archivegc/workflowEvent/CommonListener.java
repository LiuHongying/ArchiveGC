package com.ecm.portal.archivegc.workflowEvent;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.elasticsearch.common.Strings;
import org.flowable.common.engine.api.FlowableException;
import org.flowable.common.engine.api.delegate.Expression;
import org.flowable.engine.HistoryService;
import org.flowable.engine.RuntimeService;
import org.flowable.engine.TaskService;
import org.flowable.engine.delegate.DelegateExecution;
import org.flowable.engine.delegate.DelegateHelper;
import org.flowable.engine.delegate.ExecutionListener;
import org.flowable.engine.delegate.JavaDelegate;
import org.flowable.engine.delegate.TaskListener;
import org.flowable.engine.history.HistoricProcessInstance;
import org.flowable.identitylink.api.IdentityLink;
import org.flowable.task.api.Task;
import org.flowable.task.service.delegate.DelegateTask;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Component;

import com.ecm.common.util.DateUtils;
import com.ecm.core.PermissionContext;
import com.ecm.core.dao.EcmAuditWorkitemMapper;
import com.ecm.core.entity.EcmAuditWorkitem;
import com.ecm.core.entity.EcmDocument;
import com.ecm.core.entity.EcmUser;
import com.ecm.core.exception.AccessDeniedException;
import com.ecm.core.exception.EcmException;
import com.ecm.core.exception.NoPermissionException;
import com.ecm.core.service.AuthService;
import com.ecm.core.service.DocumentService;
import com.ecm.core.service.GroupService;
import com.ecm.core.service.UserService;
import com.ecm.flowable.service.CustomWorkflowService;
import com.ecm.icore.service.IEcmSession;
import com.ecm.portal.service.ServiceDocMail;

@Component(value = "commonExecutorListener")
public class CommonListener implements ExecutionListener, JavaDelegate, TaskListener {
	private Expression isSendEmail;
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	@Autowired
	private CustomWorkflowService customWorkflowService;

	@Autowired
	private HistoryService historyService;
	@Autowired
	private RuntimeService runtimeService;
	@Autowired
	private UserService userService;
	@Autowired
	private GroupService groupService;

	@Autowired
	private DocumentService documentService;

	@Autowired
	private AuthService authService;

	@Autowired
	private ServiceDocMail serviceDocMail;
	@Autowired
	private Environment env;
	@Autowired
	private EcmAuditWorkitemMapper ecmAuditWorkitemMapper;
	@Autowired
	private TaskService taskService;

	/**
	 * 监听 for executionListener
	 */
	@Override
	public void notify(DelegateExecution execution) throws FlowableException {
		if (execution.getVariable("processInstanceID") == null) {
			execution.setVariable("processInstanceID", execution.getProcessInstanceId());
			execution.setVariable("processName", execution.getProcessDefinitionId().split(":")[0]);
		}

		setPassedTaskCount(execution);
		IEcmSession ecmSession = null;
		String workflowSpecialUserName = env.getProperty("ecm.username");
		try {
			ecmSession = authService.login("workflow", workflowSpecialUserName, env.getProperty("ecm.password"));
			loadProcessBusinessLogicToVariable(ecmSession, execution);
			/********************** 发送邮件 *************************************/

//			  String isSendEmai= execution.getVariable("isSendEmail", String.class);
//			  if(isSendEmai!=null&&!"".equals(isSendEmai)) {
//			  if(Boolean.parseBoolean(isSendEmai)) {
//			  sendMailOfProcessEnd(ecmSession,execution); } }

		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			if (ecmSession != null) {
				authService.logout(workflowSpecialUserName);
			}
		}
		System.out.println("DelegateExecution_notify");
	}

	/**
	 * JavaDelegate 方法 for serviceTask
	 */
	@Override
	public void execute(DelegateExecution arg0) throws FlowableException {
		if (arg0.getVariable("processInstanceID") == null) {
			arg0.setVariable("processInstanceID", arg0.getProcessInstanceId());
			arg0.setVariable("processName", arg0.getProcessDefinitionId().split(":")[0]);
		}

		System.out.println("serviceTask_execute");
	}

	/**
	 * TaskListener 方法 for taskListener
	 */
	@Override
	public void notify(DelegateTask task) throws FlowableException {
		if ("create".equals(task.getEventName())) {
			/////////////////////// 任务到达发送邮件//////////////

			String assignee = task.getAssignee();// ecm_user.Name

			IEcmSession ecmSession = null;
			String workflowSpecialUserName = env.getProperty("ecm.username");
			try {
				ecmSession = authService.login("workflow", workflowSpecialUserName, env.getProperty("ecm.password"));
				String taskUserIds = task.getAssignee();

				/******************************* 发送邮件 ****************************/

				String sendMail = isSendEmail != null ? isSendEmail.getValue(task).toString() : null;
				if (sendMail != null && !"".equals(sendMail)) {
					if (Boolean.parseBoolean(sendMail)) {
						if (task.getAssignee() != null) {// 普通任务
							List<String> userMails = new ArrayList<>();
							if (task.getAssignee().contains(";")) {
								List<String> userNames = Arrays.asList(task.getAssignee().split(";"));
								for (String userName : userNames) {
									EcmUser user = userService.getObjectByName(ecmSession.getToken(), userName);
									if (user.getEmail() != null && !"".equals(user.getEmail())) {
										userMails.add(user.getEmail());
									}
								}
								Task tsk = taskService.createTaskQuery().taskId(task.getId()).singleResult();
								if(userMails!=null&&userMails.size()>0) {
									serviceDocMail.sendTaskMailMultipleUsers(userMails, tsk);
								}
								
							} else {
								EcmUser user = userService.getObjectByName(ecmSession.getToken(), assignee);
								if (user == null) {
									List<EcmUser> users = groupService.getAllUserByGroupName(ecmSession.getToken(),assignee);
									for (int n = 0; users != null && n < users.size(); n++) {
										EcmUser u = users.get(n);
										String email = u.getEmail();
										if (email != null && !"".equals(email)) {
											userMails.add(email);
										}
									}
									Task tsk = taskService.createTaskQuery().taskId(task.getId()).singleResult();
									if(userMails!=null&&userMails.size()>0) {
										serviceDocMail.sendTaskMailMultipleUsers(userMails, tsk);
									}
									
								} else {
									// TODO 如果代理人不为空，就执行代理
									if (!Strings.isEmpty(user.getDelegateUser())
											&& DateUtils.compareDate(new Date(), user.getDelegateStart()) >= 0
											&& DateUtils.compareDate(new Date(), user.getDelegateEnd()) < 0) {
										customWorkflowService.delegateTask(task.getId(), user.getDelegateUser());
										;
									}
									String email = user.getEmail();
									if (email != null && !"".equals(email)) {
										Task tsk = taskService.createTaskQuery().taskId(task.getId()).singleResult();
										serviceDocMail.sendTaskMail(email, tsk);
									}
								}
							}
						} else {// 候选用户
							Set<IdentityLink> candidates = task.getCandidates();
							taskUserIds = "";
							for (Iterator iterator = candidates.iterator(); iterator.hasNext();) {
								IdentityLink identityLink = (IdentityLink) iterator.next();
								EcmUser user = userService.getObjectByName(ecmSession.getToken(),
										identityLink.getUserId());
								String email = user.getEmail();
								if (email != null && !"".equals(email)) {
									// serviceDocMail.sendTaskMail(email);
									Task tsk = taskService.createTaskQuery().taskId(task.getId()).singleResult();
									serviceDocMail.sendTaskMail(email, tsk);
								}
								taskUserIds = identityLink.getUserId() + ";" + taskUserIds;
							}

						}
					}
				}
				
				/*****************************发送邮件END*****************************/
				
				// 创建流程日志
				EcmAuditWorkitem audit = new EcmAuditWorkitem();
				audit.createId();
				audit.setCreateTime(task.getCreateTime());
				audit.setDocId("");
				audit.setFormId("");
				audit.setTaskName(task.getName());
				audit.setAssignee(taskUserIds);
				audit.setProcessInstanceId(task.getProcessInstanceId());
				audit.setTaskId(task.getId());
				
				ecmAuditWorkitemMapper.insert(audit);

			} catch (Exception e) {
				e.printStackTrace();
			} finally {
				if (ecmSession != null) {
					authService.logout(workflowSpecialUserName);
				}
			}
		} else if ("complete".equals(task.getEventName())) {
			if (task.getVariable("processInstanceID") == null) {
				task.setVariable("processInstanceID", task.getProcessInstanceId());
				task.setVariable("processName", task.getProcessDefinitionId().split(":")[0]);
			}
			IEcmSession ecmSession = null;
			String workflowSpecialUserName = env.getProperty("ecm.username");
			try {
				ecmSession = authService.login("workflow", workflowSpecialUserName, env.getProperty("ecm.password"));
				loadProcessBusinessLogicToVariable(ecmSession, task);
			} catch (Exception e) {
				// TODO: handle exception
			} finally {
				if (ecmSession != null) {
					authService.logout(workflowSpecialUserName);
				}
			}

		}
		System.out.println("taskListener_notify");

	}

	/**
	 * @param ecmSession
	 * @param userName
	 * @return
	 */
	private Object getApprover(IEcmSession ecmSession, String userName) {
		String[] user = userName.split(";");
		Object result = null;
		if (user.length == 1) {// 只有一个用户或一个用户组
			if (userService.getObjectByName(ecmSession.getToken(), userName) != null) {
				result = userName;
			} else {// 如果是用户组
				String groupId = groupService.getGroupByName(ecmSession.getToken(), userName).getId();
				List<EcmUser> userList = groupService.getUsers(ecmSession.getToken(), groupId);
				List<String> userNameList = new ArrayList<String>();
				for (int i = 0; i < userList.size(); i++) {
					userNameList.add(userList.get(i).getName());
				}
				result = userNameList;
			}
		} else {// 多个用户
			result = Arrays.asList(user);
		}
		return result;
	}

	private Integer geVariable(DelegateExecution execution, String variableName) {
		Object value = execution.getVariableLocal(variableName);
		return (Integer) (value != null ? value : 0);
	}

	private DelegateExecution getMultiInstanceRootExecution(DelegateExecution executionEntity) {
		DelegateExecution multiInstanceRootExecution = null;
		DelegateExecution currentExecution = executionEntity;
		while (currentExecution != null && multiInstanceRootExecution == null && currentExecution.getParent() != null) {
			if (currentExecution.isMultiInstanceRoot()) {
				multiInstanceRootExecution = currentExecution;
			} else {
				currentExecution = currentExecution.getParent();
			}
		}
		return multiInstanceRootExecution;
	}

	private Map<String, Object> loadProcessBusinessLogicToVariable(IEcmSession ecmSession,
			org.flowable.variable.api.delegate.VariableScope arg0) {
		Map<String, Object> varMap = arg0.getVariables();
		String formId = varMap.get("formId").toString();
		try {
			EcmDocument ecmObject = documentService.getObjectById(ecmSession.getToken(), formId);
			ecmObject.setStatus("流程中");
			documentService.updateObject(ecmSession.getToken(), ecmObject, null);
		} catch (Exception e) {
			e.printStackTrace();
		}
		return varMap;
	}

	/**
	 * @param execution
	 */
	private void setPassedTaskCount(DelegateExecution execution) {
		// 设置通过拒绝参数
		DelegateExecution miRootExecution = getMultiInstanceRootExecution(execution);
		if (miRootExecution != null) {
			if ("通过".equals(execution.getVariable("outcome"))) {
				int nrOfPassedInstances = geVariable(miRootExecution, "nrOfPassedInstances");
				miRootExecution.setVariableLocal("nrOfPassedInstances", ++nrOfPassedInstances);
			} else {
				int nrOfRejectedInstances = geVariable(miRootExecution, "nrOfRejectedInstances");
				miRootExecution.setVariableLocal("nrOfRejectedInstances", ++nrOfRejectedInstances);
			}
		}
	}

	/**
	 * @param execution
	 * @param ecmSession
	 */
	private void sendMailOfProcessEnd(IEcmSession ecmSession, DelegateExecution execution) {
		// 流程结束发送邮件
		if ("endevent1".equals(execution.getCurrentActivityId())) {
			HistoricProcessInstance hi = historyService.createHistoricProcessInstanceQuery()
					.processInstanceId(execution.getProcessInstanceId()).singleResult();
			String startUserId = hi.getStartUserId();
			EcmUser user = userService.getObjectByName(ecmSession.getToken(), startUserId);
			String email = user.getEmail();
			if (email != null && !"".equals(email)) {
				// erviceDocMail.sendEndMail(email);
			}
		}
	}

}
