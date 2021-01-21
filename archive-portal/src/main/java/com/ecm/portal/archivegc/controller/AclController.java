package com.ecm.portal.archivegc.controller;

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
import com.ecm.core.entity.EcmGroup;
import com.ecm.core.entity.LoginUser;
import com.ecm.core.entity.Pager;
import com.ecm.core.exception.AccessDeniedException;
import com.ecm.core.service.GroupService;
import com.ecm.portal.controller.ControllerAbstract;

@Controller
public class AclController extends ControllerAbstract{
	@Autowired
	private GroupService groupService;
  

	@ResponseBody
	@RequestMapping(value = "/admin/getScopeKnowledge", method = RequestMethod.POST)
	public Map<String, Object> getScopeKnowledge(@RequestBody String argStr) {
		Map<String, Object> mp = new HashMap<String, Object>();
		try {
			LoginUser currentUser  = this.getSession().getCurrentUser();
			String userName = currentUser.getUserName();
			
			String condition = "MANAGER like '%" + userName+ "%'  or creator  like  '%" + userName+ "%' "  ;
			
			Map<String, Object> args = JSONUtils.stringToMap(argStr);
			List<EcmGroup> list = null;
			Pager pager = null;
			if (args.get("pageSize") != null) {
				pager = new Pager();
				pager.setPageIndex(Integer.parseInt(args.get("pageIndex").toString()));
				pager.setPageSize(Integer.parseInt(args.get("pageSize").toString()));
				list = groupService.getGroups(getToken(), args.get("id").toString(), args.get("groupType").toString(),
						pager, condition);
			} else {
//				list = groupService.getAllGroups(getToken(), args.get("id").toString(),
//						args.get("groupType").toString());
			}
			mp.put("data", list);
			mp.put("pager", pager);
			mp.put("code", ActionContext.SUCESS);
		} catch (AccessDeniedException e) {
			mp.put("code", ActionContext.TIME_OUT);
		}
		return mp;
	}
}
