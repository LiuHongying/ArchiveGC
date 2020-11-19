package com.ecm.portal.archivegc.controller;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;

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
import com.ecm.core.entity.EcmRelation;
import com.ecm.core.service.DocumentService;
import com.ecm.core.service.RelationService;
import com.ecm.portal.controller.ControllerAbstract;
@Controller
public class ArchiveDcController extends ControllerAbstract{
	@Autowired
	DocumentService documentService;
	
	@Autowired
	private RelationService relationService;
	
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
	@RequestMapping(value = "/dc/Archivepending", method = RequestMethod.POST)
	@ResponseBody
	public Map<String, Object> ArchivePendingout(String metaData,String ID) throws Exception {
		Map<String, Object> mp = new HashMap<String, Object>();
		Map<String, Object> args = JSONUtils.stringToMap(metaData);
//		Map<String, Object> args1 = JSONUtils.stringToMap(ID);
//		String idsStr=args1.get("ID").toString();
		List<String> list = JSONUtils.stringToArray(ID);
		String status = args.get("status").toString();
		String parentId=args.get("parentID").toString();
		if(args.get("type").toString().equals("主表")) {
			//主表
			for (String id : list) {
				documentService.updateStatus(getToken(), id,status);
				//获取主表的文件所有子表
				String sql="ID IN (select CHILD_ID from ecm_relation where PARENT_ID='"+id+"')";
				List<Map<String, Object>> result =documentService.getObjectMap(getToken(), sql);
				for(Map<String,Object> ids:result) {
					documentService.updateStatus(getToken(), ids.get("ID").toString(), status);
				}
			}
			mp.put("al", true);
		}else {//子表
			for (String id : list) {
				documentService.updateStatus(getToken(),id, status);
			}
			//获取主表的文件所有子表
			String sql="STATUS <>'"+status+"' AND ID IN (select CHILD_ID from ecm_relation where PARENT_ID='"+parentId+"')";
			List<Map<String, Object>> result =documentService.getObjectMap(getToken(), sql);
			boolean al = true;
			if(result != null && result.size() > 0) {
				al = false;
			}
			if(al) {
				documentService.updateStatus(getToken(), parentId, status);
			}
			mp.put("al", al);
		}
		mp.put("code", ActionContext.SUCESS);
		return mp;
	}
}
