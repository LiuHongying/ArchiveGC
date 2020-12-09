package com.ecm.portal.archivegc.controller;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

import com.ecm.common.util.JSONUtils;
import com.ecm.core.ActionContext;
import com.ecm.core.entity.EcmDocument;
import com.ecm.core.service.DocumentService;
import com.ecm.portal.controller.ControllerAbstract;

@RestController
@RequestMapping("/condition")
public class ConditionController extends ControllerAbstract {
	@Autowired
	private DocumentService documentService;
	
	@PostMapping("save")
	@ResponseBody
	public void saveCondition(@RequestBody String argStr) throws Exception{
		Map<String, Object> conditionMap = JSONUtils.stringToMap(argStr);
		
		EcmDocument doc = documentService.getObjectById(getToken(), conditionMap.get("Id").toString());
		doc.addAttribute("CREATOR", conditionMap.get("User").toString());
		doc.addAttribute("NAME", conditionMap.get("Name").toString());
		doc.addAttribute("ITEM_CONTENT", conditionMap.get("Condition").toString());
		documentService.updateObject(getToken(), doc);
	}
	
	@PostMapping("load")
	@ResponseBody
	public Map<String, Object> loadCondition(HttpServletRequest request, HttpServletResponse response, @RequestBody String argStr) throws Exception{
		Map<String, Object> mp = new HashMap<String, Object>();
		Map<String, Object> args = JSONUtils.stringToMap(argStr);
		List<Map<String, Object> > outList = new ArrayList<Map<String, Object>>();
		Map<String, Object> projMap = new HashMap<String, Object>();
		
		String loginName= args.get("User").toString();
				
		String searchCondition = " select * from ecm_document ed where CREATOR = '" + loginName + "' and IS_RELEASED = 1 and STATUS='已入库' and IS_HIDDEN=0 ";
		
		try {
			List<Map<String,Object>> conditionList =documentService.getMapList(getToken(), searchCondition);
			for(int i = 0; i < conditionList.size(); i++) {
				projMap = new HashMap<String, Object>();
				projMap.put("ID", conditionList.get(i).get("ID"));
				projMap.put("Name", conditionList.get(i).get("NAME"));
				projMap.put("Condition", conditionList.get(i).get("ITEM_CONTENT"));
				outList.add(projMap);
			}
			mp.put("data", outList);
		}catch (Exception ex) {
			ex.printStackTrace();
			mp.put("code", ActionContext.FAILURE);
		}
		
		return mp;
	}
	
}
