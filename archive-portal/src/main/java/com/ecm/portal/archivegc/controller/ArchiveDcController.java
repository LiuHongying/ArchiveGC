package com.ecm.portal.archivegc.controller;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.multipart.MultipartFile;

import com.ecm.common.util.JSONUtils;
import com.ecm.core.ActionContext;
import com.ecm.core.cache.manager.CacheManagerOper;
import com.ecm.core.entity.EcmDefType;
import com.ecm.core.entity.EcmDocument;
import com.ecm.core.service.DocumentService;
import com.ecm.portal.controller.ControllerAbstract;
@Controller
public class ArchiveDcController extends ControllerAbstract{
	private Logger log = LoggerFactory.getLogger(ArchiveDcController.class);
	@Autowired
	DocumentService documentService;
	
	@RequestMapping(value = "/dc/getEcmDefTypes", method = RequestMethod.POST)
	@ResponseBody
	public Map<String, Object> getEcmDefTypes(@RequestBody String argStr) throws Exception {
//		Map<String, Object> args = JSONUtils.stringToMap(argStr);
		Map<String, Object> mp = new HashMap<String, Object>();
//		Object classicName= args.get("classicName");
		
		if(argStr!=null) {
			String classicNameStr=argStr.toString();
			List<EcmDefType> types= CacheManagerOper.getEcmDefTypes().values().stream()
					.filter(t -> classicNameStr.equals(t.getTypeTag())).collect(Collectors.toList());
			mp.put("code", ActionContext.SUCESS);
			mp.put("data", types);
			return mp;
		}
		mp.put("code", ActionContext.SUCESS);
		mp.put("data", null);
		return mp;
	}
	@RequestMapping(value = "/dc/getArchiveFileConfig", method = RequestMethod.POST)
	@ResponseBody
	public Map<String, Object> getArchiveFileConfig(@RequestBody String argStr) throws Exception {
		Map<String, Object> mp = new HashMap<String, Object>();
		
		if(argStr!=null) {
			String archiveType=argStr.toString();
			String condition=" TYPE_NAME='案卷文件配置' and C_FROM='"+argStr.toString()+"'";
			List<Map<String,Object>> docList= documentService.getObjectMap(this.getToken(), condition);
			if(docList==null||docList.size()==0) {
				mp.put("code", ActionContext.FAILURE);
				mp.put("msg", "此类型（“"+argStr.toString()+"”）不允许创建子文件");
				return mp;
			}
			mp.put("code", ActionContext.SUCESS);
			mp.put("data", docList);
		}
		
		return mp;
		
	}
	/**
	 * 保存驳回原因
	 * @param argStr
	 * @return
	 * @throws Exception
	 */
	@RequestMapping(value = "/dc/savePenNot", method = RequestMethod.POST)
	@ResponseBody
	public Map<String, Object> savePenNot(@RequestBody String argStr) throws Exception {
		Map<String, Object> mp = new HashMap<String, Object>();
		try {
			
			Map<String, Object> args= JSONUtils.stringToMap(argStr);
			String idsStr=args.get("ids").toString();
			String comment=args.get("comment").toString();
			List<String> idsList=JSONUtils.stringToArray(idsStr);
			String userName= this.getSession().getCurrentUser().getUserName();
			for (String id : idsList) {
				EcmDocument doc= documentService.getObjectById(getToken(), id);
				doc.addAttribute("C_REJECT_COMMENT", comment);
				doc.addAttribute("C_REJECTOR", userName);
				documentService.updateObject(getToken(), doc,null);
			}
			mp.put("code", ActionContext.SUCESS);
			mp.put("msg", "保存成功");
			return mp;
		}catch (Exception e) {
			// TODO: handle exception
			log.error(e.getMessage());
			e.printStackTrace();
			mp.put("code", ActionContext.SUCESS);
			mp.put("msg", "保存失败");
			return mp;
		}
		
	}
}
