package com.ecm.portal.archivecd.controller;

import java.io.File;
import java.io.FileInputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.multipart.MultipartFile;

import com.ecm.common.util.FileUtils;
import com.ecm.common.util.JSONUtils;
import com.ecm.core.ActionContext;
import com.ecm.core.cache.manager.CacheManagerOper;
import com.ecm.core.entity.EcmContent;
import com.ecm.core.entity.EcmDocument;
import com.ecm.core.entity.EcmFolder;
import com.ecm.core.entity.EcmGridView;
import com.ecm.core.entity.EcmRelation;
import com.ecm.core.entity.LoginUser;
import com.ecm.core.entity.Pager;
import com.ecm.core.exception.AccessDeniedException;
import com.ecm.core.exception.EcmException;
import com.ecm.core.service.ContentService;
import com.ecm.core.service.DocumentService;
import com.ecm.core.service.FolderPathService;
import com.ecm.core.service.FolderService;
import com.ecm.core.service.RelationService;
import com.ecm.portal.archive.controller.ArchiveFolderController;
import com.ecm.portal.archivecd.event.Distribute;
import com.ecm.portal.controller.ControllerAbstract;

@Controller
@RequestMapping(value = "/cd")
public class DocumentController extends ControllerAbstract {
	@Autowired
	private ContentService contentService;
	@Autowired
	private FolderService folderService;
	@Autowired
	private DocumentService documentService;
	@Autowired
	private RelationService relationService;
	@Autowired
	private FolderPathService folderPathService;
	@Autowired
	Distribute distributeComponent;
	private Logger logger = LoggerFactory.getLogger(this.getClass());
	/**
	 * 获取模板树
	 * @param param
	 * @return
	 */
	@RequestMapping(value="/dc/getTemplateTreeData", method = RequestMethod.POST)
	@ResponseBody	
	public Map<String, Object> getFolderByConfigeGC(@RequestBody String parentId) {

		Map<String, Object> mp = new HashMap<String, Object>();
		try {
			List<EcmFolder> list = folderService.getFoldersByParentId(getToken(), parentId);
//			List<Map<String,Object>> docs=documentService.getMapList(getToken(), 
//					"select * from ecm_document where  folder_id='"+parentId+"'");
			List<EcmDocument> docs= documentService.getObjectsAllColumn(getToken(), " folder_id='"+parentId+"'");
			List<Object> data=new ArrayList<>();
			data.addAll(list);
			data.addAll(docs);
			mp.put("code", ActionContext.SUCESS);
			mp.put("data", data);
		}
		catch(Exception ex) {
			ex.printStackTrace();
			mp.put("code", ActionContext.FAILURE);
			mp.put("message", ex.getMessage());
		}
		return mp;
	
	}
	
	/**
	 * 创建文档待模板
	 * @param param
	 * @return
	 */
	@RequestMapping(value="/dc/newDocumentWithTemplate", method = RequestMethod.POST)
	@ResponseBody	
	public Map<String, Object> newDocumentWithTemplate(@RequestBody String param) {

		Map<String, Object> mp = new HashMap<String, Object>();
		Map<String,Object> p= JSONUtils.stringToMap(param);
		try {
			String typeName="";
			if(p.get("typeName")!=null) {
				typeName=p.get("typeName").toString();
			}
			String templateId="";
			EcmContent en=null;
			if(p.get("templateId")!=null&&"".equals(p.get("templateId").toString())) {
				templateId=p.get("templateId").toString();
				EcmContent content = contentService.getPrimaryContent(getToken(), templateId);
				String fullPath = CacheManagerOper.getEcmStores().get(content.getStoreName()).getStorePath();
//				File f = new File(fullPath+en.getFilePath());
				FileInputStream itemStream = new FileInputStream(fullPath+content.getFilePath());
				
				en = new EcmContent();
				en.setName(content.getName());
				en.setContentSize(content.getContentSize());
				en.setFormatName(content.getFormatName());
				en.setInputStream(itemStream);
			}
			if("".equals(typeName)) {
				mp.put("code", ActionContext.FAILURE);
				mp.put("message", "类型为空");
				return mp;
			}
			
			EcmDocument document=new EcmDocument();
			document.setTypeName(typeName);
			String newObjId= documentService.newObject(getToken(), document, en);
			
			mp.put("code", ActionContext.SUCESS);
			mp.put("id", newObjId);
		}
		catch(Exception ex) {
			ex.printStackTrace();
			mp.put("code", ActionContext.FAILURE);
			mp.put("message", ex.getMessage());
		}
		return mp;
	
	}
	/**
	 * 分发
	 * @param metaData 元数据
	 * @param mainFile 主文件
	 * @param attachFiles 附件
	 * @return
	 * @throws Exception
	 */
	@RequestMapping(value = "/dc/distribute", method = RequestMethod.POST)
	@ResponseBody
	public Map<String, Object> distribute(String metaData, MultipartFile mainFile,
			MultipartFile[] attachFiles) throws Exception {
		Map<String, Object> args = JSONUtils.stringToMap(metaData);
		Map<String, Object> mp = new HashMap<String, Object>();
		try {
			
			Object parentObj= args.get("parentId");
			if(parentObj!=null) {
				args.remove("parentId");
				
			}
			String parentId=parentObj.toString();
			EcmDocument pObj= documentService.getObjectById(getToken(), parentId);
			String sql = "select b.*,a.id as RELATION_ID,a.NAME as RELATION_NAME,a.PARENT_ID,a.CHILD_ID,a.ORDER_INDEX"
					+ " from ecm_relation a, ecm_document b where  a.CHILD_ID=b.ID "
					+ " and a.PARENT_ID='"+parentId+"' and a.name='文件分发' order by a.ORDER_INDEX,b.CREATION_DATE";
			List<Map<String,Object>> list = documentService.getMapList(getToken(), sql);
			Map<String,Object> docData= list.get(0);
			EcmDocument docObj=new EcmDocument();
			docObj.setAttributes(docData);
			String[] hosts=null;
			if(args.get("C_HOST")!=null) {
				String hostStr=args.get("C_HOST").toString();
				hosts=hostStr.split(";");
			}
			String[] participations=null;
			if(args.get("C_PARTICIPATION")!=null) {
				String participationStr=args.get("C_PARTICIPATION").toString();
				participations=participationStr.split(";");
			}
			String[] copytos=null;
			if(args.get("C_COPY_TO")!=null) {
				String copyToStr=args.get("C_COPY_TO").toString();
				copytos=copyToStr.split(";");
			}
			String sender=this.getSession().getCurrentUser().getUserName();
			
			distributeComponent.distributeDataOption(getToken(), sender,docObj, pObj, hosts, participations, copytos);
			
			
			mp.put("code", ActionContext.SUCESS);
			return mp;
		}catch (Exception e) {
			// TODO: handle exception
			e.printStackTrace();
			logger.error(e.getMessage());
			mp.put("code", ActionContext.FAILURE);
			mp.put("message", "保存失败");
			return mp;
		}
		
	}
	public void execAddAttachment(Map<String, Object> args,MultipartFile[] uploadFile) throws Exception {
		String parentId = args.get("parentDocId").toString();
		
		for (MultipartFile multipartFile : uploadFile) {
			
			EcmDocument doc = new EcmDocument();
			doc.setAttributes(args);
			String fileName=multipartFile.getOriginalFilename();
			fileName=fileName.substring(0,fileName.lastIndexOf(".")<0
					?fileName.length():fileName.lastIndexOf("."));
			doc.setName(fileName);
			
			Object fid= args.get("folderId");
			String folderId="";
			if(fid==null) {
				folderId= folderPathService.getFolderId(getToken(), doc.getAttributes(), "3");
			}else {
				folderId=fid.toString();
			}
			EcmFolder folder= folderService.getObjectById(getToken(), folderId);
			doc.setFolderId(folderId);
			doc.setAclName(folder.getAclName());
			
			EcmContent en = new EcmContent();
			en.setName(multipartFile.getOriginalFilename());
			en.setContentSize(multipartFile.getSize());
			en.setInputStream(multipartFile.getInputStream());
//			documentService.addRendition(getToken(), parentId, en);
			
			String relationName="irel_children";
			relationName=args.get("relationName")!=null
					&&!"".equals(args.get("relationName").toString())
					?args.get("relationName").toString():"irel_children";
			String id = documentService.newObject(getToken(),doc,en);//创建文件和内容
			//----------------创建关系--------------
			EcmRelation relation=new EcmRelation();
			relation.setParentId(parentId);
			
			relation.setChildId(id);
			relation.setName(relationName);
			try {
				relationService.newObject(getToken(), relation);
			} catch (EcmException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
				throw e;
			}
			//----------------end创建关系-------------------
		}
	}
	/**
	 * 获取所有表单
	 * 
	 * @return
	 */
	@ResponseBody
	@RequestMapping(value="/admin/getFolder", method = RequestMethod.POST)
	public Map<String, Object> getFolder(@RequestBody String parentId) {
		Map<String, Object> mp = new HashMap<String, Object>();
		try {
			List<EcmFolder> list = folderService.getFoldersByParentId(getToken(), parentId);
			mp.put("code", ActionContext.SUCESS);
			mp.put("data", list);
		}
		catch(Exception ex) {
			ex.printStackTrace();
			mp.put("code", ActionContext.FAILURE);
			mp.put("message", ex.getMessage());
		}
		return mp;
	}
	
	@RequestMapping(value="/dc/readedDistribution", method = RequestMethod.POST)
	@ResponseBody	
	public Map<String, Object> readedDistribution(@RequestBody String param){
		List<String> distributionIdList= JSONUtils.stringToArray(param);
		Map<String,Object> mp=new HashMap<String, Object>();
		try {
			for(int i=0;distributionIdList!=null&&i<distributionIdList.size();i++) {
				String id= distributionIdList.get(i);
				EcmDocument doc= documentService.getObjectById(getToken(), id);
				doc.setStatus("已阅");
				documentService.updateObject(getToken(), doc, null);
			}
			mp.put("code", ActionContext.SUCESS);
		}catch (Exception e) {
			// TODO: handle exception
			e.printStackTrace();
			mp.put("code", ActionContext.FAILURE);
			mp.put("message", "操作失败,请联系系统管理员！");
			e.printStackTrace();
			logger.error(e.getMessage());
		}
		
		return mp;
	}
	@RequestMapping(value = "/dc/getDistributeRecord", method = RequestMethod.POST) // PostMapping("/dc/getDocumentCount")
	@ResponseBody
	public Map<String, Object> getDocuments(@RequestBody String argStr) {
		Map<String, Object> mp = new HashMap<String, Object>();
		try {
			Map<String,Object> params= JSONUtils.stringToMap(argStr);
			int pageSize = Integer.parseInt(params.get("pageSize").toString());
			int pageIndex = Integer.parseInt(params.get("pageIndex").toString());
			Pager pager = new Pager();
			pager.setPageIndex(pageIndex);
			pager.setPageSize(pageSize);
			Object distributeObj=  params.get("distributeId");
			Object fildObj=params.get("fileId");
			if(distributeObj==null&&fildObj==null) {
				mp.put("code", ActionContext.FAILURE);
				mp.put("message", "未指定文件或分发单！");
				return mp;
			}
			List<Map<String,Object>> data=null;
			if(distributeObj!=null) {
				data=distributeComponent.getDistributeRecordByDistributeId(getToken(), distributeObj.toString(), pager);
			}else {
				data=distributeComponent.getDistributeRecordByFileId(getToken(), fildObj.toString(), pager);
			}
			mp.put("pager", pager);
			mp.put("code", ActionContext.SUCESS);
			mp.put("data", data);
		}catch (Exception e) {
			// TODO: handle exception
			mp.put("code", ActionContext.FAILURE);
			mp.put("message", "操作失败，请联系管理员");
			logger.error(e.getMessage());
			e.printStackTrace();
		}
		
		
		return mp;
	}
	/**
	 * 分发
	 * @param metaData 元数据
	 * @param mainFile 主文件
	 * @param attachFiles 附件
	 * @return
	 * @throws Exception
	 */
	@RequestMapping(value = "/dc/distributeApprove", method = RequestMethod.POST)
	@ResponseBody
	public Map<String, Object> distributeApprove(@RequestBody String metaData) throws Exception {
		Map<String,Object> mp=new HashMap<String, Object>();
		try {
			Map<String, Object> args = JSONUtils.stringToMap(metaData);
			EcmDocument order = new EcmDocument();
			order.setAttributes(args);
			distributeComponent.updateOrderForDistributionStatus(getToken(), order, "已生效");
		}catch (Exception e) {
			// TODO: handle exception
			mp.put("code", ActionContext.FAILURE);
			mp.put("message", "操作失败请联系管理员！"+e.getMessage());
			logger.error(e.getMessage());
			e.printStackTrace();
			return mp;
		}
		
		
		mp.put("code", ActionContext.SUCESS);
		return mp;
	}
	
}
