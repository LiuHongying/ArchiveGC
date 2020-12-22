package com.ecm.portal.archivegc.controller;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.multipart.MultipartFile;

import com.ecm.common.util.JSONUtils;
import com.ecm.core.ActionContext;
import com.ecm.core.cache.manager.CacheManagerOper;
import com.ecm.core.entity.EcmDefType;
import com.ecm.core.entity.EcmDocument;
import com.ecm.core.entity.EcmFolder;
import com.ecm.core.entity.EcmQuery;
import com.ecm.core.entity.EcmRelation;
import com.ecm.core.exception.AccessDeniedException;
import com.ecm.core.exception.EcmException;
import com.ecm.core.exception.NoPermissionException;
import com.ecm.core.service.DocumentService;
import com.ecm.core.service.FolderPathService;
import com.ecm.core.service.FolderService;
import com.ecm.core.service.NumberService;
import com.ecm.core.entity.LoginUser;
import com.ecm.core.entity.Pager;
import com.ecm.core.exception.AccessDeniedException;
import com.ecm.core.service.DocumentService;
import com.ecm.core.service.FolderPathService;
import com.ecm.core.service.FolderService;
import com.ecm.core.service.NumberService;
import com.ecm.core.service.QueryService;
import com.ecm.core.service.RelationService;
import com.ecm.portal.archive.common.ChildrenObjAction;
import com.ecm.portal.archivegc.service.ImportServiceGc;
import com.ecm.portal.controller.ControllerAbstract;
import com.ecm.portal.entity.AttrCopyCfgEntity;
import com.ecm.portal.service.CustomCacheService;
import com.ecm.icore.service.IEcmSession;
import com.ecm.portal.controller.ControllerAbstract;
@Controller
public class ArchiveDcController extends ControllerAbstract{
	private Logger log = LoggerFactory.getLogger(ArchiveDcController.class);
	private int	AN;				//案卷
	private int WJ;				//文件
	@Autowired
	DocumentService documentService;
	@Autowired
	private NumberService numberService;
	
	@Autowired
	private FolderPathService folderPathService;
	
	@Autowired
	private FolderService folderService;
	
	@Autowired
	private RelationService relationService;
	@Autowired
	private ImportServiceGc importService;
	
	@Autowired
	private CustomCacheService customCacheService;
	
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
			String obj=argStr.toString();
			String condition=" TYPE_NAME='案卷文件配置' and C_FROM='"+obj+"'";
			if(obj.length()>30)
			{
				EcmDocument doc = documentService.getObjectById(getToken(), obj);
				if(doc != null) {
					condition=" TYPE_NAME='案卷文件配置' and C_FROM='"+doc.getTypeName()+"'";
					AttrCopyCfgEntity en = customCacheService.getAttrCopyCfg(getToken(), doc.getTypeName());
					if(en != null) {
						Map<String, Object> valmp = new HashMap<String, Object>();
						for(String attr: en.getAttrNames().keySet()) {
							valmp.put(attr, doc.getAttributeValue(en.getAttrNames().get(attr)));
						}
						mp.put("copyInfo", valmp);
					}
					
				}
			}
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
	/**
	 * 出入库操作
	 * @param metaData
	 * @param ID
	 * @return
	 * @throws Exception
	 */
	@RequestMapping(value = "/dc/Archivepending", method = RequestMethod.POST)
	@ResponseBody
	public Map<String, Object> ArchivePendingout(String metaData,String ID) throws Exception {
		Map<String, Object> mp = new HashMap<String, Object>();
		Map<String, Object> args = JSONUtils.stringToMap(metaData);
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
	
	@RequestMapping(value = "/dc/countDocuments", method = RequestMethod.POST)
	@ResponseBody
	public Map<String, Object> countDocuments(@RequestBody String metadata) throws Exception {
		AN = 0;
		WJ = 0;								//初始化计数器
		String type = "未识别";
		Map<String, Object> mp = new HashMap<String, Object>();
		Map<String, Object> args = JSONUtils.stringToMap(metadata);
		Object childObj=args.get("childFileId");
		if(childObj!=null&&!"".equals(childObj.toString())) {
			String childFileIds=childObj.toString();
			List<String> childIdList= JSONUtils.stringToArray(childFileIds);
			if(childIdList.size()<10) {
				mp.put("code", "0");			//数量处于绝对少数，无需向领导请示
				return mp;
			}
			if(childIdList.size()>50) {
				mp.put("code", "1");            //数量处于绝对多数，无需判断，必须向领导请示
				return mp;
			}
			for(int i=0;childIdList!=null&&i<childIdList.size();i++) {
				
				String id = childIdList.get(i);
				Map<String,Object> temp = documentService.getObjectMapById(getToken(), id);
				if(temp.get("C_ITEM_TYPE")!=null) {
					type = temp.get("C_ITEM_TYPE").toString();
				}
				if(type.equals("案卷")) {
					AN++;
				}
				if(type.equals("文件")) {
					WJ++;
				}
			}
			if(AN>10||WJ>50) {
				mp.put("code",1);
			}
			else {
				mp.put("code", 0);
			}
		}
		return mp;
	}
	/**
	 * 逻辑删除
	 * @param argStr
	 * @return
	 * @throws Exception
	 */
	@RequestMapping(value = "/dc/logicallyDel", method = RequestMethod.POST)
	@ResponseBody
	public Map<String, Object> logicallyDel(@RequestBody String argStr) throws Exception{

		Map<String, Object> mp = new HashMap<String, Object>();
		try {
			
			Map<String, Object> args= JSONUtils.stringToMap(argStr);
			if(args.get("ids")==null) {
				mp.put("code", ActionContext.FAILURE);
				mp.put("msg", "没有选择要删除的数据！");
				return mp;
			}
			String idsStr=args.get("ids").toString();
			List<String> idList= JSONUtils.stringToArray(idsStr);
			String whereSql= String.join("','", idList.toArray(new String[idList.size()]));
			whereSql=" PARENT_ID in( '"+whereSql+"')";
			
			String sql="select ID,CHILD_ID from ecm_relation where "+whereSql;
			List<Map<String,Object>> result= documentService.getMapList(getToken(), sql);
			for (Map<String, Object> map : result) {
				String childId= map.get("CHILD_ID").toString();
				EcmDocument childDoc= documentService.getObjectById(getToken(), childId);
				childDoc.addAttribute("IS_HIDDEN", 1);
				documentService.updateObject(getToken(), childDoc, null);
				relationService.deleteObjectById(getToken(), map.get("ID").toString());
			}
			for (String id:idList) {
				EcmDocument pDoc= documentService.getObjectById(getToken(), id);
				pDoc.addAttribute("IS_HIDDEN", 1);
				documentService.updateObject(getToken(), pDoc, null);
			}
			mp.put("code", ActionContext.SUCESS);
			mp.put("msg", "删除成功");
			return mp;
		}catch (Exception e) {
			// TODO: handle exception
			log.error(e.getMessage());
			e.printStackTrace();
			mp.put("code", ActionContext.FAILURE);
			mp.put("msg", "删除失败");
			return mp;
		}
	
	}
	@RequestMapping(value = "/dc/updateData", method = RequestMethod.POST)
	@ResponseBody
	public Map<String, Object> updateData(@RequestBody String argStr) throws Exception{
		Map<String, Object> mp = new HashMap<String, Object>();
		try {
			
			List<String> paramStrs=JSONUtils.stringToArray(argStr);
			if(paramStrs==null) {
				mp.put("code", ActionContext.FAILURE);
				mp.put("msg", "没有选择要修改的数据！");
				return mp;
			}
			for (String paramStr : paramStrs) {
				Map<String, Object> args= JSONUtils.stringToMap(paramStr);
				EcmDocument doc=new EcmDocument();
				doc.setAttributes(args);
				documentService.updateObject(getToken(), doc,null);
			}
			mp.put("code", ActionContext.SUCESS);
			mp.put("msg", "修改成功");
			return mp;
			
		}catch (Exception e) {
			// TODO: handle exception
			mp.put("code", ActionContext.FAILURE);
			mp.put("msg", "修改失败");
			return mp;
		}
		
	}
	/**
	 * 提取信息
	 * @param argStr
	 * @return
	 */
	@RequestMapping(value = "/dc/fetchInformationGc", method = RequestMethod.POST) // PostMapping("/dc/getDocumentCount")
	@ResponseBody
	public Map<String,Object> fetchInformation(@RequestBody String argStr){

		List<String> list = JSONUtils.stringToArray(argStr);
		Map<String, Object> mp = new HashMap<String, Object>();
		try {
			for(int i=0;list!=null&&i<list.size();i++) {
				String boxId=list.get(i);
				EcmDocument doc = null;
				doc = documentService.getObjectById(getToken(), boxId);
				
				if(doc.getCoding()==null||"".equals(doc.getCoding())) {
					mp.put("code", ActionContext.FAILURE);
					mp.put("message", "请先取号！");
					return mp;
				}else {

					String sqlSumPage="select sum(C_PAGE_COUNT) as pageCount from ecm_document "
							+ "where id in(select child_id from ecm_relation where parent_id='"+boxId+"' "
									+ " and name='irel_children' and (DESCRIPTION!='复用' or DESCRIPTION is null))";
					List<Map<String, Object>> pages= documentService.getMapList(getToken(),sqlSumPage);
					if(pages!=null&&pages.size()>0&&pages.get(0)!=null) {
						doc.addAttribute("C_PAGE_COUNT", pages.get(0).get("pageCount"));
					}else {
						doc.addAttribute("C_PAGE_COUNT", "0");
					}
					
//					String minDocDate=ChildrenObjAction.getMinDocDate(getToken(), boxId, "C_DRAFT_DATE",documentService);
//					if(minDocDate!=null) {
//						doc.addAttribute("C_DRAFT_DATE", minDocDate);
//					}
//					String maxDocDate=ChildrenObjAction.getMaxDocDate(getToken(), boxId, "C_DRAFT_DATE",documentService);
//					if(maxDocDate!=null) {
//						doc.addAttribute("C_END_DATE", maxDocDate);
//					}
					
					String maxRetention= ChildrenObjAction.getRetention(getToken(), boxId, documentService);
					if(maxRetention!=null) {
						doc.addAttribute("C_RETENTION", maxRetention);
					}else {
						doc.addAttribute("C_RETENTION", "10年");
					}
					String maxSecurity=ChildrenObjAction.getVolumeMaxSecurity(getToken(), boxId, documentService);
					if(maxSecurity==null) {
						doc.addAttribute("C_SECURITY_LEVEL", "内部公开");
					}else {
						doc.addAttribute("C_SECURITY_LEVEL", maxSecurity);
					}
					documentService.updateObject(getToken(), doc, null);
				}
				
				//////////////////////////////////////////////
			}
		} catch (AccessDeniedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			mp.put("code", ActionContext.FAILURE);
			mp.put("message", e.getMessage());
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			mp.put("code", ActionContext.FAILURE);
			mp.put("message", e.getMessage());
		}
		mp.put("code", ActionContext.SUCESS);
		return mp;
		
	
		
	}
	@RequestMapping(value = "/import/batchImport", method = RequestMethod.POST)
	@ResponseBody
	public Map<String, Object> batchImport(@RequestParam("metaData")String metaData,@RequestParam("excel") MultipartFile excel, @RequestParam("files") MultipartFile[] files) throws AccessDeniedException{
		Map<String, Object> mp = new HashMap<String, Object>();
		Map<String, Object> args = JSONUtils.stringToMap(metaData);
		String msg;
		try {
			String relationName=args.get("relationName")==null?"":args.get("relationName").toString();
			msg = importService.importExcel(getToken(),args.get("id").toString(),relationName,excel, files);
			mp.put("code", ActionContext.SUCESS);
			mp.put("data", msg);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			mp.put("code", ActionContext.FAILURE);
			mp.put("data", e.getMessage());
		}
		
		return mp;
	}
	
	
	
	/**
	 * 预归档库
	 * @param argStr
	 * @return
	 */
	@RequestMapping(value = "/dc/penddingStorage", method = RequestMethod.POST) // PostMapping("/dc/getDocumentCount")
	@ResponseBody
	public Map<String,Object> penddingStorage(@RequestBody String argStr){
		List<String> list = JSONUtils.stringToArray(argStr);
		Map<String, Object> mp = new HashMap<String, Object>();
		for (String id : list) {
//			Map<String,Object> obj= JSONUtils.stringToMap(mpstr);
			
			try {
//				String id=obj.get("ID").toString();
				EcmDocument doc= documentService.getObjectById(getToken(), id);
				doc.setStatus("待入库");
				
				String folderId= folderPathService.getFolderId(getToken(), doc.getAttributes(), "3");
				EcmFolder folder= folderService.getObjectById(getToken(), folderId);
				doc.setFolderId(folderId);
				doc.setAclName(folder.getAclName());
				documentService.updateObject(getToken(), doc,null);
			} catch (NoPermissionException | AccessDeniedException | EcmException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
				mp.put("code", ActionContext.FAILURE);
				mp.put("message", "操作失败");
				return mp;
			}
		}
		mp.put("code", ActionContext.SUCESS);
		mp.put("message", "操作成功");
		return mp;
	}
	/**
	 * 预归档库
	 * @param argStr
	 * @return
	 */
	@RequestMapping(value = "/dc/moveToPreFiling", method = RequestMethod.POST) // PostMapping("/dc/getDocumentCount")
	@ResponseBody
	public Map<String,Object> moveToPreFiling(@RequestBody String argStr){
		List<String> list = JSONUtils.stringToArray(argStr);
		Map<String, Object> mp = new HashMap<String, Object>();
		for (String id : list) {
//			Map<String,Object> obj= JSONUtils.stringToMap(mpstr);
			
			try {
//				String id=obj.get("ID").toString();
				EcmDocument doc= documentService.getObjectById(getToken(), id);
				doc.setStatus("预归档");
				
				String folderId= folderPathService.getFolderId(getToken(), doc.getAttributes(), "4");
				doc.setFolderId(folderId);
				doc.setAclName("acl_pre_archive");
				documentService.updateObject(getToken(), doc,null);
			} catch (NoPermissionException | AccessDeniedException | EcmException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
				mp.put("code", ActionContext.FAILURE);
				mp.put("message", "操作失败");
				return mp;
			}
		}
		mp.put("code", ActionContext.SUCESS);
		mp.put("message", "操作成功");
		return mp;
	}
	/**
	 * 取号
	 * @param argStr
	 * @return
	 */
	@RequestMapping(value = "/dc/takeNumbersArchiveGc", method = RequestMethod.POST) // PostMapping("/dc/getDocumentCount")
	@ResponseBody
	public Map<String,Object> takeNumbersArchiveGc(@RequestBody String argStr){
		List<String> list = JSONUtils.stringToArray(argStr);
		Map<String, Object> mp = new HashMap<String, Object>();
		try {
			for(int i=0;list!=null&&i<list.size();i++) {
				String boxId=list.get(i);
//				String boxId=argStr;
				EcmDocument doc = null;
				doc = documentService.getObjectById(getToken(), boxId);
				String coding="";
				if(doc.getCoding()!=null&&!"".equals(doc.getCoding())) {
					coding=doc.getCoding();
				}else {
					while(true) {
						coding= numberService.getNumber(getToken(), doc.getAttributes());
						String validataSql="select coding from ecm_document where coding='"+coding+"'";
						List<Map<String,Object>> result= documentService.getMapList(getToken(),validataSql);
						if(result==null||result.size()==0) {
							doc.setCoding(coding);
							documentService.updateObject(getToken(), doc,null);
							break;
						}
					}
				}
				String sql="select child_id from ecm_relation where parent_id='"+boxId+"' and name='irel_children'";
				List<Map<String,Object>> childrenIds=documentService.getMapList(getToken(), sql);// ChildrenObjAction.getChildrenObjById(getToken(), boxId, documentService);
				for(Map<String,Object> childId : childrenIds) {
					String childidStr=(String) childId.get("child_id");
					EcmDocument childDoc= documentService.getObjectById(getToken(), childidStr);
					childDoc.addAttribute("C_ARCHIVE_CODING", coding);
					try {
						documentService.updateObject(getToken(), childDoc,null);
					}catch(NullPointerException nu) {
						continue;
					}catch (Exception e) {
						// TODO: handle exception
						e.printStackTrace();
						throw e;
					}
					
				}
				
			
			}
		} catch (AccessDeniedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			mp.put("code", ActionContext.FAILURE);
			mp.put("message", e.getMessage());
			return mp;
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			mp.put("code", ActionContext.FAILURE);
			mp.put("message", "取号失败,详细信息："+e.getMessage());
			return mp;
		}
		mp.put("code", ActionContext.SUCESS);
		return mp;
		
	}
	
	@RequestMapping(value = "/dc/newArchiveAppraisal", method = RequestMethod.POST)
	@ResponseBody
	public Map<String, Object> newArchiveAppraisal(String metaData) throws Exception {
		Map<String, Object> mp = new HashMap<String, Object>();
		try {
			Map<String, Object> args = JSONUtils.stringToMap(metaData);
			EcmDocument doc = new EcmDocument();
			doc.setAttributes(args);
			String folderId="";
			folderId = folderPathService.getFolderId(getToken(),args, "3");
			EcmFolder folder = folderService.getObjectById(getToken(), folderId);
			LoginUser userObj=null;
			userObj=getSession().getCurrentUser();
			String username = userObj.getUserName();
			doc.addAttribute("C_DRAFTER", username);
			doc.addAttribute("IS_RELEASED", "1");
			doc.setFolderId(folderId);
			String coding = numberService.getNumber(getToken(), args);
			doc.setCoding(coding);
			doc.setAclName(folder.getAclName());
			doc.setStatus("新建");
			String id = documentService.newObject(getToken(), doc, null);
			mp.put("code", ActionContext.SUCESS);
			mp.put("id", id);
		}catch (AccessDeniedException e) {
			mp.put("code", ActionContext.TIME_OUT);
		}
		return mp;
	}
	
	@RequestMapping(value = "/dc/getDocuments4DC", method = RequestMethod.POST) // PostMapping("/dc/getDocumentCount")
	@ResponseBody
	public Map<String, Object> getDocuments(@RequestBody String argStr) throws ParseException {
		Map<String, Object> mp = new HashMap<String, Object>();
		try {
			Map<String, Object> args = JSONUtils.stringToMap(argStr);
			int pageSize = Integer.parseInt(args.get("pageSize").toString());
			int pageIndex = Integer.parseInt(args.get("pageIndex").toString());
			Pager pager = new Pager();
			pager.setPageIndex(pageIndex);
			pager.setPageSize(pageSize);
			List<Map<String, Object>> res = new ArrayList<Map<String, Object>>();
			List<Map<String, Object>> list = documentService.getObjects(getToken(), args.get("gridName").toString(),
					args.get("folderId")==null?"":args.get("folderId").toString(), pager, args.get("condition").toString(),
					args.get("orderBy").toString());
			int num=0;
			for(Map<String,Object> obj:list) {
				SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
				long nowData=new Date().getTime();
				Date date = sdf.parse(obj.get("C_DRAFT_DATE").toString());
				long a = date.getTime();
				if(obj.get("C_RETENTION").toString().equals("10年")) {
					if(nowData-(10*12*24*60*60*1000)>a) {
						res.add(obj);
//						res.add(obj);
					}
				}else if(obj.get("C_RETENTION").toString().equals("30年")) {
					if(nowData-(30*12*24*60*60*1000)>a) {
						res.add(obj);
//						res.add(obj);
					}
				}
			}
			mp.put("data", res);
			mp.put("pager", pager);
			mp.put("code", ActionContext.SUCESS);
		} catch (AccessDeniedException e) {
			mp.put("code", ActionContext.TIME_OUT);
		}
		return mp;
	}
	
	@RequestMapping(value = "/dc/checkArchiveFile", method = RequestMethod.GET)
	@ResponseBody
	public Map<String, Object> checkArchiveFile() throws Exception {
		Map<String, Object> mp = new HashMap<String, Object>();
		try {
			String cond="TYPE_NAME='档案鉴定单' and IS_RELEASED=1 and(STATUS='新建' or STATUS='流程中')";
			List<Map<String, Object>>  relist = documentService.getObjectMap(getToken(), cond);
			String id="";
			if(relist != null && relist.size() > 0) {
				for(Map<String,Object> map:relist) {
					String condition = "select * from ecm_relation where PARENT_ID='"+map.get("ID").toString()+"'";
					List<Map<String, Object>>  list = relationService.getMapList(getToken(), condition);
//							getObjectMap(getToken(), condition);
//					List<Map<String,Object>> list =documentService.getObjectMap(getToken(), cond);
					if(list != null && list.size() > 0) {
						for(Map<String,Object> re:list) {
							id += "'";
							id+=re.get("CHILD_ID").toString();
							id+="',";
						}
					}else {
						id+="";
						
					}
				}
				if(id.length()>0) {
					id = id.substring(0,id.length() - 1);
				}
				
			}
			mp.put("code", ActionContext.SUCESS);
			mp.put("ID",id);
			return mp;
		} catch (Exception ex) {
			mp.put("code", ActionContext.FAILURE);
			mp.put("message", ex.getMessage());
		}
		return mp;
	}
	
	/**
	 * 档案鉴定子表删除文档
	 * @param argStr
	 * @return
	 * @throws Exception
	 */
	@RequestMapping(value = "/dc/Archive/DelDcRelation", method = RequestMethod.POST)
	@ResponseBody
	public Map<String, Object> DelDcRelation(@RequestBody String argStr) throws Exception {
		Map<String, Object> mp = new HashMap<String, Object>();
		System.out.print(argStr);
		List<String> list = JSONUtils.stringToArray(argStr);
		for (String id : list) {
			System.out.print(id);
			relationService.deleteByChildIdAndRelationName(getToken(), id, "irel_children");
		}
		mp.put("code", ActionContext.SUCESS);
		return mp;
	}
	
	@Autowired
	private QueryService queryService;
	
	@ResponseBody
	@PostMapping("/query/getquery")
	public Map<String,Object> getQuery(@RequestBody String argStr){
		Map<String, Object> args = JSONUtils.stringToMap(argStr);
		Map<String, Object> map = new HashMap<String, Object>();
		EcmQuery queryObject = queryService.getObjectByName("", args.get("queryName").toString());
		
		map.put("data",queryService.executeSQL("", queryObject.getSqlString()));
		map.put("valueField", queryObject.getValueColumn());
		map.put("labelField", queryObject.getLabelColumn());
		map.put("code", ActionContext.SUCESS);
		return map;
	}
	
	@RequestMapping(value = "/dc/Archive/newDcRelation", method = RequestMethod.POST)
	@ResponseBody
	public Map<String, Object> NewDcRelation(String metaData,String ID) throws Exception {
		Map<String, Object> mp = new HashMap<String, Object>();
		Map<String, Object> args = JSONUtils.stringToMap(metaData);
		List<String> list = JSONUtils.stringToArray(ID);
		String parentId=args.get("parentID").toString();
		for (String id : list) {
			EcmRelation relation=new EcmRelation();
			relation.setParentId(parentId);
			relation.setChildId(id);
			relation.setName("irel_children");
			String ids=relationService.newObject(getToken(), relation);
		}
		mp.put("code", ActionContext.SUCESS);
		return mp;
	}
	@RequestMapping(value = "/dc/recyclebin/resetDC", method = RequestMethod.POST)
	@ResponseBody
	public Map<String, Object> resetDC(@RequestBody String argStr) throws Exception {
		Map<String, Object> mp = new HashMap<String, Object>();
		List<String> list = JSONUtils.stringToArray(argStr);
		EcmDocument temp = new EcmDocument();
		for (String id : list) {
			temp = documentService.getObjectById(getToken(), id);
			temp.addAttribute("IS_HIDDEN", 0);
			documentService.updateObject(getToken(), temp, null);
		}
		mp.put("code", ActionContext.SUCESS);
		return mp;
	}
}
