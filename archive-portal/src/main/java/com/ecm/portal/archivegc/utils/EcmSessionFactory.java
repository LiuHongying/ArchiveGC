package com.ecm.portal.archivegc.utils;

import org.springframework.core.env.Environment;

import com.ecm.core.cache.manager.SessionManager;
import com.ecm.core.exception.AccessDeniedException;
import com.ecm.core.service.AuthService;
import com.ecm.icore.service.IEcmSession;

public class EcmSessionFactory {
	public static IEcmSession getWorkflowSession(Environment env,AuthService authService) {
		IEcmSession ecmSession = null;
		String workflowSpecialUserName = env.getProperty("ecm.username");
		try {
			ecmSession = authService.login("workflow", workflowSpecialUserName, env.getProperty("ecm.password"));
		}catch (Exception e) {
			// TODO: handle exception
		}
		return ecmSession;
	}
	/**
	 * 获取Session
	 * @param token
	 * @return
	 * @throws AccessDeniedException
	 */
	public static IEcmSession getSession(String token) throws AccessDeniedException {
		try {
			IEcmSession session = SessionManager.getInstance().getSession(token);
			if(session == null) {
				throw new AccessDeniedException("Invalid token: " + token);
			}
			return session;
		}catch(Exception ex)
		{
			throw new AccessDeniedException("Invalid token: " + token);
		}
		
	}
}
