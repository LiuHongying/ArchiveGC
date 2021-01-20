package com.ecm.portal.archivegc.controller;

import static org.hamcrest.CoreMatchers.nullValue;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import com.ecm.common.util.JSONUtils;
import com.ecm.core.ActionContext;
import com.ecm.core.cache.manager.CacheManagerOper;
import com.ecm.core.entity.EcmAuditGeneral;
import com.ecm.core.entity.EcmDocument;
import com.ecm.core.entity.EcmSystemEvent;
import com.ecm.core.entity.LoginUser;
import com.ecm.core.service.AuditService;
import com.ecm.core.service.AuthService;
import com.ecm.core.service.DocumentService;
import com.ecm.core.service.RelationService;
import com.ecm.icore.service.IEcmSession;
import com.ecm.portal.controller.ControllerAbstract;

@Controller
public class ArchiveAuditController extends ControllerAbstract {
	private Logger logger = LoggerFactory.getLogger(ArchiveAuditController.class);
	
	@Autowired
	protected AuditService auidtService;
	@Autowired
	private DocumentService documentService;
	@Autowired
	private RelationService relationService;
	@Autowired
	private Environment env;
	@Autowired
	private AuthService authService;
	
	/**
	 * Matthew creates on 2020年12月10日17:21:27
	 * Matthew changes on 2021年1月19日10:04:53
	 * @param argStr
	 * @return
	 */
	@RequestMapping("/archive/addAudit")
	@ResponseBody
	public String saveAudit(@RequestBody String argStr) {
		Map<String, Object> args = JSONUtils.stringToMap(argStr);
		String docId = args.get("docId").toString();
		String actionName = args.get("actionName").toString();
		String appName = args.get("appName").toString();
		StringBuilder sqlStr = new StringBuilder("");
		List<Map<String, Object>> list = new ArrayList<Map<String, Object>>();
		EcmSystemEvent ecmSystemEvent = CacheManagerOper.getEcmSystemEvents().get(actionName);
		EcmAuditGeneral en = new EcmAuditGeneral();
		if (!ecmSystemEvent.getEnabled()) {
			logger.warn("请将日志记录打开，否则将无法控制用户借阅日志记录");
			return "fail";
		}else {
			try {
				LoginUser currentUser  = this.getSession().getCurrentUser();
				String ecmSystemName = ecmSystemEvent.getName();
				en.setActionName(ecmSystemName);
				en.setUserName(currentUser.getUserName());
				en.setUserId(currentUser.getUserId());
				en.setDocId(docId);
				en.setAppName(appName);
				en.createId();
				en.setExcuteDate(new Date());
				auidtService.newObject(getToken(),en);
				return "success";
			} catch (Exception e) {
				e.printStackTrace();
				return "fail";
			}
		}
	}
	
	/**
	 * Matthew creates on 2020年12月10日17:21:27
	 * Matthew changes on 2021年1月19日10:04:53
	 * @param argStr
	 * @return
	 */
	@RequestMapping("/archive/addAudit2")
	@ResponseBody
	public String saveAudit2(@RequestParam(value = "docId")String docId,
			@RequestParam(value = "actionName")String actionName,
			@RequestParam(value = "appName")String appName) {
		//Map<String, Object> args = JSONUtils.stringToMap(argStr);
//		String docId = args.get("docId").toString();
//		String actionName = args.get("actionName").toString();
//		String appName = args.get("appName").toString();
		StringBuilder sqlStr = new StringBuilder("");
		List<Map<String, Object>> list = new ArrayList<Map<String, Object>>();
		EcmSystemEvent ecmSystemEvent = CacheManagerOper.getEcmSystemEvents().get(actionName);
		EcmAuditGeneral en = new EcmAuditGeneral();
		if (!ecmSystemEvent.getEnabled()) {
			logger.warn("请将日志记录打开，否则将无法控制用户借阅日志记录");
			return "fail";
		}else {
			try {
				LoginUser currentUser  = this.getSession().getCurrentUser();
				String ecmSystemName = ecmSystemEvent.getName();
				en.setActionName(ecmSystemName);
				en.setUserName(currentUser.getUserName());
				en.setUserId(currentUser.getUserId());
				en.setDocId(docId);
				en.setAppName(appName);
				en.createId();
				en.setExcuteDate(new Date());
				auidtService.newObject(getToken(),en);
				return "success";
			} catch (Exception e) {
				e.printStackTrace();
				return "fail";
			}
		}
	}
	
	/**
	 * 收回文件权限
	 * @param argStr
	 * @return
	 */
	@RequestMapping("/archive/revokeAcl")
	@ResponseBody
	public String revokeAcl(@RequestBody String argStr) {
		Map<String, Object> args = JSONUtils.stringToMap(argStr);
		String docId = args.get("docId").toString();
		IEcmSession ecmSession = null;
		String workflowSpecialUserName = env.getProperty("ecm.username");
		try {
			LoginUser currentUser  = this.getSession().getCurrentUser();
			ecmSession = authService.login("workflow", workflowSpecialUserName, env.getProperty("ecm.password"));
			EcmDocument docObj = documentService.getObjectById(ecmSession.getToken(),docId);
			documentService.revokeUser(ecmSession.getToken(), docObj, currentUser.getUserName(), false);
		} catch (Exception e) {
			e.printStackTrace();
		}finally {
			if (ecmSession != null) {
				authService.logout(workflowSpecialUserName);
			}
		}
		return "success";
	}
	
	/**
	 * 收回文件权限
	 * @param argStr
	 * @return
	 */
	@RequestMapping("/archive/revokeAcl2")
	@ResponseBody
	public String revokeAcl2(@RequestParam(value = "docId")String docId) {
//		Map<String, Object> args = JSONUtils.stringToMap(argStr);
//		String docId = args.get("docId").toString();
		IEcmSession ecmSession = null;
		String workflowSpecialUserName = env.getProperty("ecm.username");
		try {
			LoginUser currentUser  = this.getSession().getCurrentUser();
			ecmSession = authService.login("workflow", workflowSpecialUserName, env.getProperty("ecm.password"));
			EcmDocument docObj = documentService.getObjectById(ecmSession.getToken(),docId);
			documentService.revokeUser(ecmSession.getToken(), docObj, currentUser.getUserName(), false);
		} catch (Exception e) {
			e.printStackTrace();
		}finally {
			if (ecmSession != null) {
				authService.logout(workflowSpecialUserName);
			}
		}
		return "success";
	}
	
	/**
	 * Matthew creates on 2020年12月10日18:23:35
	 * 方法已经弃用，但是考虑到前端还有用到此方法，就没有删除
	 * @param argStr
	 * @return
	 */
	@RequestMapping("/archive/judgeDownloadByAudit")
	@ResponseBody
	public Map<String, Object> judgeDownloadByAudit(@RequestBody String argStr) {
		Map<String, Object> args = JSONUtils.stringToMap(argStr);
		String docId = args.get("docId").toString();
		Map<String, Object> mp = new HashMap<String, Object>();
		StringBuilder relationSql = new StringBuilder("");
		List<Map<String, Object>> relationlist = new ArrayList<Map<String, Object>>();
		List<Map<String, Object>> auditlist = new ArrayList<Map<String, Object>>();
			try {
				LoginUser currentUser  = this.getSession().getCurrentUser();
				relationSql.append("select parent_id from ecm_relation where parent_id in ");
				relationSql.append("(select id from ecm_document where type_name = '借阅单' and sub_type = '下载' and OWNER_NAME = '");
				relationSql.append(currentUser.getUserName());
				relationSql.append("' ) and child_id = '");
				relationSql.append(docId);
				relationSql.append("'");
				relationlist = relationService.getMapList(getToken(), relationSql.toString());
				//如果list.size=0，说明此人从未借阅过此文件，只要正常显示权限下载按钮即可。
				//如果list.size>0，说明此人借阅过此文件，需要判断是否已经做过下载此文件。
				if (relationlist.size()>0) {
					mp.put("showRelyPermit", false);
					StringBuilder parents = new StringBuilder("");
					parents.append("select extend_id from ecm_audit_general where doc_id = '");
					parents.append(docId);
					parents.append("' and extend_id in (");
					for (Map<String, Object> map : relationlist) {
						parents.append("'"+(String)map.get("parent_id")+"',");
					}
					parents.deleteCharAt(parents.length()-1);
					parents.append(") ");
					parents.append(" and user_name = ");
					parents.append("'"+currentUser.getUserName()+"'");
					parents.append(" and action_name = 'ecm_download'");
					//嗯哼，auditService没有实现getMapList方法， 尴尬，用documentService吧
					auditlist = documentService.getMapList(getToken(), parents.toString());
					//如果已经下载过则返回false，不可以显示下载
					//如果还没有下载过，则返回true，可以显示下载
					if (auditlist.size()<relationlist.size()) {
						mp.put("downloadPermit", true);
					}else {
						mp.put("downloadPermit", false);
					}
					mp.put("code", ActionContext.SUCESS);
				}else {
					mp.put("showRelyPermit", true);
					mp.put("downloadPermit", false);
					mp.put("code", ActionContext.SUCESS);
				}
			} catch (Exception e) {
				e.printStackTrace();
				mp.put("code", ActionContext.FAILURE);
			}
			return mp;
	}
	
	/**
	 * Matthew changes on 2021年1月15日16:42:30
	 * @param argStr
	 * @return
	 */
	@RequestMapping("/archive/judgeDownloadByPermit")
	@ResponseBody
	public Map<String, Object> judgeDownloadByPermit(@RequestBody String argStr){
		Map<String, Object> args = JSONUtils.stringToMap(argStr);
		Boolean borrowHistory = false;
		String docId = args.get("docId").toString();
		Map<String, Object> mp = new HashMap<String, Object>();
		StringBuilder relationSql = new StringBuilder("");
		List<Map<String, Object>> relationlist = new ArrayList<Map<String, Object>>();
		List<Map<String, Object>> auditlist = new ArrayList<Map<String, Object>>();
			try {
				LoginUser currentUser  = this.getSession().getCurrentUser();
				relationSql.append("select parent_id from ecm_relation where parent_id in ");
				relationSql.append("(select id from ecm_document where type_name = '借阅单' and sub_type = '下载' and OWNER_NAME = '");
				relationSql.append(currentUser.getUserName());
				relationSql.append("' ) and child_id = '");
				relationSql.append(docId);
				relationSql.append("'");
				relationlist = relationService.getMapList(getToken(), relationSql.toString());
				//如果list.size=0，说明此人从未借阅过此文件，只要正常显示权限下载按钮即可。
				//如果list.size>0，说明此人借阅过此文件，需要判断是否已经做过下载此文件。
				if (relationlist.size()>0) {
					borrowHistory = true;
					mp.put("borrowHistory", borrowHistory);
					StringBuilder parents = new StringBuilder("");
					parents.append("select extend_id from ecm_audit_general where doc_id = '");
					parents.append(docId);
					parents.append("' and extend_id in (");
					for (Map<String, Object> map : relationlist) {
						parents.append("'"+(String)map.get("parent_id")+"',");
					}
					parents.deleteCharAt(parents.length()-1);
					parents.append(") ");
					parents.append(" and user_name = ");
					parents.append("'"+currentUser.getUserName()+"'");
					parents.append(" and action_name = 'ecm_download'");
					//嗯哼，auditService没有实现getMapList方法， 尴尬，用documentService吧
					auditlist = documentService.getMapList(getToken(), parents.toString());
					//如果已经下载过则返回false，不可以显示下载
					//如果还没有下载过，则返回true，可以显示下载
					if (auditlist.size()<relationlist.size()) {
						mp.put("downloadPermit", true);
						mp.put("code", ActionContext.SUCESS);
					}else {
						mp.put("downloadPermit", false);
						mp.put("code", ActionContext.SUCESS);
					}
				}else {
					borrowHistory = false;
					mp.put("borrowHistory", borrowHistory);
					mp.put("downloadPermit", true);
					mp.put("code", ActionContext.SUCESS);
					return mp;
				}
			} catch (Exception e) {
				e.printStackTrace();
				mp.put("code", ActionContext.FAILURE);
			}
			return mp;
	}
	
	/**
	 * Matthew changes on 2021年1月19日10:24:42
	 * 十分尴尬，一个方法写三遍
	 * 判断是否可以打印的逻辑
	 * @param argStr
	 * @return
	 */
	@RequestMapping("/archive/judgePrintByPermit")
	@ResponseBody
	public Map<String, Object> judgePrintByPermit(@RequestBody String argStr){
		Map<String, Object> args = JSONUtils.stringToMap(argStr);
		Boolean borrowHistory = false;
		String docId = args.get("docId").toString();
		Map<String, Object> mp = new HashMap<String, Object>();
		StringBuilder relationSql = new StringBuilder("");
		List<Map<String, Object>> relationlist = new ArrayList<Map<String, Object>>();
			try {
				LoginUser currentUser  = this.getSession().getCurrentUser();
				relationSql.append("select parent_id from ecm_relation where parent_id in ");
				relationSql.append("(select id from ecm_document where type_name = '借阅单' and sub_type = '打印' and OWNER_NAME = '");
				relationSql.append(currentUser.getUserName());
				relationSql.append("' ) and child_id = '");
				relationSql.append(docId);
				relationSql.append("'");
				relationlist = relationService.getMapList(getToken(), relationSql.toString());
				//如果list.size=0，说明此人从未请求打印过此文件，只要正常显示权限打印按钮即可。
				//如果list.size>0，说明此人请求打印过此文件，需要判断是否已经做过打印此文件的操作。
				if (relationlist.size()>0) {
					borrowHistory = true;
					mp.put("borrowHistory", borrowHistory);
					mp.put("code", ActionContext.SUCESS);
				}else {
					borrowHistory = false;
					mp.put("borrowHistory", borrowHistory);
					mp.put("code", ActionContext.SUCESS);
					return mp;
				}
			} catch (Exception e) {
				e.printStackTrace();
				mp.put("code", ActionContext.FAILURE);
			}
			return mp;
	}
	
}
