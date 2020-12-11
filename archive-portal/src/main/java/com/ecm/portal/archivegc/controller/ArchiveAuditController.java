package com.ecm.portal.archivegc.controller;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import com.ecm.common.util.JSONUtils;
import com.ecm.core.ActionContext;
import com.ecm.core.cache.manager.CacheManagerOper;
import com.ecm.core.entity.EcmAuditGeneral;
import com.ecm.core.entity.EcmSystemEvent;
import com.ecm.core.entity.LoginUser;
import com.ecm.core.service.AuditService;
import com.ecm.core.service.DocumentService;
import com.ecm.core.service.RelationService;
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
	
	/**
	 * Matthew creates on 2020年12月10日17:21:27
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
			logger.warn("请将下载日志记录打开，否则将无法控制用户借阅下载");
			return "fail";
		}else {
			try {
				LoginUser currentUser  = this.getSession().getCurrentUser();
				sqlStr.append("select id  from ecm_document where id in ");
				sqlStr.append("(select parent_id from ecm_relation where child_id = '");
				sqlStr.append(docId);
				sqlStr.append("' and name = 'irel_children') ");
				sqlStr.append("and type_name = '借阅单' and STATUS != '已完成' and sub_type = '下载' and OWNER_NAME = '");
				sqlStr.append(currentUser.getUserName());
				sqlStr.append("' order by CREATION_DATE desc limit 1");
				list = documentService.getMapList(getToken(), sqlStr.toString());
				if (list.size()>0) {
					Map<String, Object> map = list.get(0);
					String extendId = (String) map.get("id");
					en.setExtendId(extendId);
					en.setMessage("used");
				}
				en.setActionName(ecmSystemEvent.getName());
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
	 * Matthew creates on 2020年12月10日18:23:35
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
					parents.append(") and message =''");
					parents.append(" and user_name = ");
					parents.append("'"+currentUser.getUserName()+"'");
					//嗯哼，auditService没有实现getMapList方法， 尴尬，用documentService吧
					auditlist = documentService.getMapList(getToken(), parents.toString());
					//如果已经下载过则返回false，不可以显示下载
					//如果还没有下载过，则返回true，可以显示下载
					if (auditlist.size()>0) {
						mp.put("downloadPermit", true);
					}else {
						mp.put("downloadPermit", true);
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
}
