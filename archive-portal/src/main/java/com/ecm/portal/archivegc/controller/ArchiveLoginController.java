package com.ecm.portal.archivegc.controller;

import java.util.HashMap;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import com.ecm.core.ActionContext;
import com.ecm.core.AuditContext;
import com.ecm.core.cache.manager.CacheManagerOper;
import com.ecm.core.entity.EcmUser;
import com.ecm.core.entity.UserEntity;
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
public class ArchiveLoginController extends ControllerAbstract {

	@Autowired
	private AuthService authService;
	
	@Autowired
	private AuditService auditService;
	
	
	/**
	  *  档案系统登录
	 * 
	 * @return
	 */
	@ResponseBody
	@RequestMapping(value = "/archive/userLogin", method = RequestMethod.POST)
	public Map<String, Object> ssoLogin(@RequestBody String loginName, HttpSession session) {
		
		Map<String, Object> mp = new HashMap<String, Object>();
			session.removeAttribute("ECMUserToken");
			try {
				// 系统登录认证
				IEcmSession s = authService.loginSSO("portal",loginName.replace("\"", ""));
				mp.put("code", ActionContext.SUCESS);
				mp.put("token", s.getToken());
				mp.put("department", s.getCurrentUser().getDepartment());
				mp.put("clientPermission", s.getCurrentUser().getClientPermission());
				mp.put("systemPermission", s.getCurrentUser().getSystemPermission());
				mp.put("loginName", s.getCurrentUser().getLoginName());
				mp.put("userName", s.getCurrentUser().getUserName());
				session.setAttribute("ECMUserToken", s.getToken());
			} catch (Exception e) {
				e.printStackTrace();
				mp.put("code", 0);
				mp.put("msg", e.getMessage());
				try {
					auditService.newAudit(null,"portal",AuditContext.LOGIN_FAILED, "", null, loginName);
				} catch (AccessDeniedException e1) {
					e1.printStackTrace();
				}
			}
		return mp;
	}
	
}
