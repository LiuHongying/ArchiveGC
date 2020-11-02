package com.ecm.portal.archivegc.tools.Controller;

import java.util.HashMap;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;
import com.ecm.common.util.JSONUtils;
import com.ecm.core.ActionContext;
import com.ecm.core.exception.AccessDeniedException;
import com.ecm.portal.archivegc.tools.Service.FormImportService;
import com.ecm.portal.controller.ControllerAbstract;

@RestController
public class FormImportController  extends ControllerAbstract  {

	@Autowired
	private FormImportService importService;
	
	@RequestMapping(value = "/FormImport/batchImport", method = RequestMethod.POST)
	@ResponseBody
	public Map<String, Object> batchImport(@RequestParam("excel") MultipartFile excel) throws AccessDeniedException{
		
		Map<String, Object> mp = new HashMap<String, Object>();
		String msg;
		try {
			msg = importService.importExcel(getToken(),excel);
			mp.put("code",ActionContext.SUCESS);
			mp.put("data", msg);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			mp.put("code", ActionContext.FAILURE);
			mp.put("data", e.getMessage());
		}
		
		return mp;
		
	}
	
}
