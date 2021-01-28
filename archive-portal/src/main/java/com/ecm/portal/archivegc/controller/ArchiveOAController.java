package com.ecm.portal.archivegc.controller;

import static org.hamcrest.CoreMatchers.nullValue;

import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpSession;

import org.apache.commons.math3.analysis.function.Sin;
import org.elasticsearch.cluster.metadata.AliasAction.NewAliasValidator;
import org.flowable.engine.ManagementService;
import org.flowable.engine.TaskService;
import org.flowable.task.api.Task;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import com.alibaba.druid.util.StringUtils;
import com.ecm.common.util.JSONUtils;
import com.ecm.common.util.SecureUtils;
import com.ecm.core.ActionContext;
import com.ecm.core.AuditContext;
import com.ecm.core.entity.EcmUser;
import com.ecm.core.entity.LoginUser;
import com.ecm.core.exception.AccessDeniedException;
import com.ecm.core.service.AuditService;
import com.ecm.core.service.AuthService;
import com.ecm.core.service.UserService;
import com.ecm.icore.service.IEcmSession;
import com.ecm.portal.controller.ControllerAbstract;


/**
 * 
 * @author Matthew 档案系统登录管理
 */
@Controller
public class ArchiveOAController extends ControllerAbstract {
	@Autowired
	private ManagementService managementService;
	
	@Autowired
	private UserService userService;

	@Autowired
	private AuthService authService;
	
	@Autowired
	private AuditService auditService;
	
	@Autowired
	private TaskService taskService;
	
	
	/**
	  *  档案系统OA跳转登录
	 * 
	 * @return
	 */
	@ResponseBody
	@PostMapping(value = "/archive/userLogin")
	public Map<String, Object> ssoLogin(@RequestBody String metaData, HttpSession session) {
		Map<String, Object> mp = new HashMap<String, Object>();
		Map<String, Object> args = JSONUtils.stringToMap(metaData);
		//获取登录名
		String loginName = (String) args.get("loginName");
		//获取参数签名
		String sign = (String) args.get("sign");
		String[] signArr = sign.split("\\|");
		Date d = new Date();
		//判断OA跳转链接是否已经过期，限时一分钟
		if(Long.valueOf(d.getTime())-Long.valueOf(signArr[2])>60000) {
			mp.put("code", ActionContext.FAILURE);
			mp.put("msg", "链接超时，转至用户名密码登录");
			return mp;
		}
		//获取MD5加密后的sign
		String encrySign = (String) args.get("encrySign");
		String MD5Sign = SecureUtils.md5Encode(sign);
		if (!MD5Sign.equals(encrySign)) {
			mp.put("code", ActionContext.FAILURE);
			mp.put("msg", "MD5密文有误，转至用户名密码登录");
			return mp;
		}
			session.removeAttribute("ECMUserToken");
			try {
				// 系统登录认证
				IEcmSession s = authService.loginSSO("portal",loginName.replace("\"", ""));
				if(s.getCurrentUser().getClientPermission()>=9) {
					mp.put("code", ActionContext.FAILURE);
					mp.put("msg", "超级用户无法SSO登录，转至用户名密码登录");
					return mp;
				}
				mp.put("code", ActionContext.SUCESS);
				mp.put("token", s.getToken());
				mp.put("data", s.getCurrentUser());
				session.setAttribute("ECMUserToken", s.getToken());
			} catch (Exception e) {
				e.printStackTrace();
				mp.put("code", ActionContext.FAILURE);
				mp.put("msg", e.getMessage());
				try {
					auditService.newAudit(null,"portal",AuditContext.LOGIN_FAILED, "", null, loginName);
				} catch (AccessDeniedException e1) {
					e1.printStackTrace();
				}
			}
		return mp;
	}
	
	/**
	 *  OA获取档案系统的待办任务数
	 * 
	 * @return
	 */
	@ResponseBody
	@PostMapping(value = "/archive/todoCount")
	public Map<String, Object> todoTaskCount(@RequestParam("loginName") String loginName,@RequestParam("sign") String sign,@RequestParam("encrySign") String encrySign) {
		//设定返回数据
		Map<String, Object> mp = new HashMap<String, Object>();
		String returnCode = "";
		String msg = "";
		Map <String,String> returnData = new HashMap<String, String>();
		String returnUserName = "";
		if (StringUtils.isEmpty(loginName)) {
			msg = "error";
			returnCode = String.valueOf(ActionContext.FAILURE);
			returnData.put("message", "登录名为空");
			mp.put("code", returnCode);
			mp.put("msg", msg);
			mp.put("data", JSONUtils.mapToJson(returnData));
			mp.put("todoCount", "");
			mp.put("userName", loginName);
			return mp;
		}
		if(StringUtils.isEmpty(sign)) {
			msg = "error";
			returnCode = String.valueOf(ActionContext.FAILURE);
			returnData.put("message", "签名为空");
			mp.put("code", returnCode);
			mp.put("msg", msg);
			mp.put("data", JSONUtils.mapToJson(returnData));
			mp.put("todoCount", "");
			mp.put("userName", loginName);
			return mp;
		}
		if(StringUtils.isEmpty(encrySign)) {
			msg = "error";
			returnCode = String.valueOf(ActionContext.FAILURE);
			returnData.put("message", "加密签名为空");
			mp.put("code", returnCode);
			mp.put("msg", msg);
			mp.put("data", JSONUtils.mapToJson(returnData));
			mp.put("todoCount", "");
			mp.put("userName", loginName);
			return mp;
		}
		EcmUser user = userService.getObjectByLoginName(null, loginName);
		if(user==null) {
			msg = "fail";
			returnCode = String.valueOf(ActionContext.FAILURE);
			returnData.put("message", "查询用户不存在");
			mp.put("code", returnCode);
			mp.put("msg", msg);
			mp.put("data", JSONUtils.mapToJson(returnData));
			mp.put("todoCount", "");
			mp.put("userName", loginName);
			return mp;
		}
		String[] signArr = sign.split("\\|");
		Date d = new Date();
		long timestamp = 0;
		//判断传入时间戳是否正常
		try {
			timestamp = Long.valueOf(signArr[2]);
		} catch (Exception e) {
			msg = "fail";
			returnCode = String.valueOf(ActionContext.FAILURE);
			returnData.put("message", "获取时间戳异常，请按照传参规范填写时间戳");
			mp.put("code", returnCode);
			mp.put("msg", msg);
			mp.put("data", JSONUtils.mapToJson(returnData));
			mp.put("todoCount", "");
			mp.put("userName", loginName);
			return mp;
		}
		//请求是否已经过期，限时12分钟
		if(Long.valueOf(d.getTime())-timestamp>60000) {
			msg = "fail";
			returnCode = String.valueOf(ActionContext.FAILURE);
			returnData.put("message", "链接超时");
			mp.put("code", returnCode);
			mp.put("msg", msg);
			mp.put("data", JSONUtils.mapToJson(returnData));
			mp.put("todoCount", "");
			mp.put("userName", loginName);
			return mp;
		}
		//判断MD5加密后的sign
		String MD5Sign = SecureUtils.md5Encode(sign);
		if (!MD5Sign.equals(encrySign)) {
			msg = "fail";
			returnCode = String.valueOf(ActionContext.FAILURE);
			returnData.put("message", "MD5密文有误");
			mp.put("code", returnCode);
			mp.put("msg", msg);
			mp.put("data", JSONUtils.mapToJson(returnData));
			mp.put("todoCount", "");
			mp.put("userName", loginName);
			return mp;
		}
		try {
			LoginUser loginUser = userService.authentication(user);
			List<String> roleList= loginUser.getRoles();
			String whereSql="";
			for(int i=0;roleList!=null&&i<roleList.size();i++) {
				if(i==0) {
					whereSql+=" T.ASSIGNEE_='"+loginUser.getUserName()+"' or T.ASSIGNEE_='"+roleList.get(i)+"'";
				}
				 
				 if(i!=0) {
					 whereSql+=" or T.ASSIGNEE_='"+roleList.get(i)+"'";
				 }
			}
			List<Task> tasks =  taskService.createNativeTaskQuery().sql("select *  from "
					+managementService.getTableName(Task.class)+" T WHERE ("+whereSql+") ").list();
			long todoCount = 0;
			if (tasks!=null) {
				todoCount = tasks.size();
			}
			msg = "success";
			returnCode = String.valueOf(ActionContext.SUCESS);
			returnData.put("message", "success");
			mp.put("todoCount", String.valueOf(todoCount));
		} catch (Exception e) {
			msg = "fail";
			returnCode = String.valueOf(ActionContext.FAILURE);
			returnData.put("message", e.getMessage());
			mp.put("todoCount", "");
		}
		mp.put("code", returnCode);
		mp.put("msg", msg);
		mp.put("data", JSONUtils.mapToJson(returnData));
		mp.put("userName", loginName);
		return mp;
	}
	
}
