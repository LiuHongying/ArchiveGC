package com.ecm.portal.archivegc.controller;

import java.util.ArrayList;
import java.util.Date;
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
import com.ecm.core.cache.manager.CacheManagerOper;
import com.ecm.core.entity.EcmAuditGeneral;
import com.ecm.core.entity.EcmSystemEvent;
import com.ecm.core.entity.LoginUser;
import com.ecm.core.service.AuditService;
import com.ecm.core.service.DocumentService;
import com.ecm.portal.controller.ControllerAbstract;

@Controller
public class ArchiveAuditController extends ControllerAbstract {
	private Logger logger = LoggerFactory.getLogger(ArchiveAuditController.class);
	
	@Autowired
	protected AuditService auidtService;
	@Autowired
	private DocumentService documentService;
	
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
}
