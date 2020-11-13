package com.ecm.portal.archivegc.controller;

import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import com.ecm.common.util.JSONUtils;
import com.ecm.core.ActionContext;
import com.ecm.core.db.DBFactory;
import com.ecm.core.entity.EcmDocument;
import com.ecm.core.service.DocumentService;
import com.ecm.portal.controller.ControllerAbstract;
@Controller
public class Receiving4TC extends ControllerAbstract {
	@Autowired
	private DocumentService documentService;
    /**
	 *接收文档
	 */
	@RequestMapping(value = "/TC/Receivingdc", method = RequestMethod.POST)
	@ResponseBody
	public Map<String, Object> Receivingdc(@RequestBody String argStr) throws Exception {
		Map<String, Object> mp = new HashMap<String, Object>();
		try {
			Map<String, Object> args = JSONUtils.stringToMap(argStr);
			String idsStr=args.get("ids").toString();
			List<String> list = JSONUtils.stringToArray(idsStr);
			//
			for(String childId : list) {
				EcmDocument doc= documentService.getObjectById(getToken(), childId);
				doc.setStatus("已接收");
				documentService.updateObject(getToken(), doc, null);
			}
			mp.put("code", ActionContext.SUCESS);
			
		}catch (Exception e) {
			// TODO: handle exception
			mp.put("code", ActionContext.FAILURE);
			mp.put("message", e.getMessage());
			e.printStackTrace();
		}
		return mp;
	}
	/**
	 *驳回文档
	 */
	@RequestMapping(value = "/TC/Rejectdc", method = RequestMethod.POST)
	@ResponseBody
	public Map<String, Object> Rejectdc(@RequestBody String argStr) throws Exception {
		Map<String, Object> mp = new HashMap<String, Object>();
		try {
			Map<String, Object> args = JSONUtils.stringToMap(argStr);
			String idsStr=args.get("ids").toString();
			List<String> list = JSONUtils.stringToArray(idsStr);
			String rejectCommon=args.get("rejectCommon")!=null?args.get("rejectCommon").toString():"";
			for(String childId : list) {
				EcmDocument doc= documentService.getObjectById(getToken(), childId);
				doc.setStatus("已驳回");
				doc.addAttribute("C_REJECT_COMMENT", rejectCommon);
				doc.addAttribute("C_REJECTOR", this.getSession().getCurrentUser().getUserName());
				doc.addAttribute("C_REJECT_DATE", DBFactory.getDBConn().getDBUtils().getDBDateNow());
				documentService.updateObject(getToken(), doc, null);
			}
			mp.put("code", ActionContext.SUCESS);
			
		}catch (Exception e) {
			// TODO: handle exception
			mp.put("code", ActionContext.FAILURE);
			mp.put("message", e.getMessage());
			e.printStackTrace();
		}
		return mp;
	}
}
