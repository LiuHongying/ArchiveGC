package com.ecm.portal.archivegc.controller;

import java.io.BufferedOutputStream;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;
import com.ecm.common.util.JSONUtils;
import com.ecm.core.ActionContext;
import com.ecm.core.exception.AccessDeniedException;
import com.ecm.portal.archivegc.service.PrintService;
import com.ecm.portal.controller.ControllerAbstract;

@RestController
@RequestMapping("/record/print")
public class PrintController  extends ControllerAbstract  {
	
	
	@Autowired
	private PrintService printService;
	
	@PostMapping("getPrintData")
	@ResponseBody
	public Map<String, Object>  getPrintData(@RequestBody String argStr) {
		Map<String, Object> mp = new HashMap<String, Object>();
		Map<String, Object> args = JSONUtils.stringToMap(argStr);
		String ids = (String)args.get("ids");
		String printType = (String)args.get("printType");
		List<String> idList = JSONUtils.stringToArray(ids);
		try {
			mp.put("data", printService.getPrintEntity(getToken(),idList, printType));
			mp.put("code", ActionContext.SUCESS);
			return mp;
		} catch (AccessDeniedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		mp.put("code", ActionContext.FAILURE);
		return mp;
	}
	
	@GetMapping("getContentBarcode")
	@ResponseBody
	public void  getContentBarcode(HttpServletRequest request,HttpServletResponse response) throws Exception {
		String str= "";
		if(request.getAttribute("str")!=null){
			str = request.getAttribute("str").toString();
		}
		else{
			str = request.getParameter("str");
		}
		byte[] buffer = printService.generatePdf417Image(str, null, "png");
		String id = UUID.randomUUID().toString().replace("-", "");
		// 清空response
        response.reset();
        // 设置response的Header
        response.setCharacterEncoding("UTF-8");
        response.addHeader("Content-Disposition", "attachment;filename=" + id + ".png" );
        response.addHeader("Content-Length", "" + buffer.length);
        BufferedOutputStream toClient = new BufferedOutputStream(response.getOutputStream());
        response.setContentType("application/octet-stream");
        toClient.write(buffer, 0, buffer.length);
        if(toClient!=null) {
	        try {
				toClient.flush();
				toClient.close();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}
	
	
}
