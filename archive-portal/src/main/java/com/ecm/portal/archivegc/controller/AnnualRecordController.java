package com.ecm.portal.archivegc.controller;

import java.io.IOException;
import java.io.InputStream;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellType;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.xssf.usermodel.XSSFRow;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbookFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import com.ecm.common.util.JSONUtils;
import com.ecm.core.ActionContext;
import com.ecm.core.entity.EcmContent;
import com.ecm.core.exception.AccessDeniedException;
import com.ecm.core.exception.EcmException;
import com.ecm.core.service.ContentService;
import com.ecm.core.service.DocumentService;
import com.ecm.core.service.FolderService;
import com.ecm.core.service.QueryService;
import com.ecm.portal.archive.controller.ImportController;
import com.ecm.portal.controller.ControllerAbstract;

@Controller
public class AnnualRecordController extends ControllerAbstract {
	
	@Autowired
	private DocumentService documentService;
	@Autowired
	private FolderService folderService;
	@Autowired
	private QueryService queryService;
	@Autowired
	private ContentService contentService;
	
	@RequestMapping(value = "/report/annual", method = RequestMethod.POST)
	@ResponseBody
	public void AnnualReportStatistic(HttpServletRequest request, HttpServletResponse response, @RequestBody String argStr) throws EcmException, AccessDeniedException, ParseException {
		List<String> outList = new ArrayList<String>();
		Map<String, Object> args = JSONUtils.stringToMap(argStr);
		
		String fileName = this.getStrValue(args, "fileName");
		String sheetName = this.getStrValue(args, "sheetName");
		String yearSelect = this.getStrValue(args, "yearSelect");
		String year = String.valueOf(Integer.parseInt(yearSelect.substring(0, 4)) + 1);
		
		SimpleDateFormat sdf = new SimpleDateFormat("yyyy");
		Date date = new Date();        
		String thisYear =  sdf.format(date);
		
		try {
			String[] condition = {"if(C_INCLUDE_PAPER = '有' and C_ITEM_TYPE = '案卷' and C_ARCHIVE_DATE between '"+ year +"-01-01 00:00:00' and '"+ year +"-12-31 23:59:59',1,0) countSign from ecm_document ed where C_ARC_CLASSIC is not null ",
								  "if(C_INCLUDE_PAPER = '有' and C_ITEM_TYPE = '文件' and C_ARCHIVE_DATE between '"+ year +"-01-01 00:00:00' and '"+ year +"-12-31 23:59:59',1,0) countSign from ecm_document ed where C_ARC_CLASSIC is not null ",
								  "if(C_RETENTION in ('永久','30年') and C_ITEM_TYPE = '案卷' and C_ARCHIVE_DATE between '"+ year +"-01-01 00:00:00' and '"+ year +"-12-31 23:59:59',1,0) countSign from ecm_document ed where C_ARC_CLASSIC is not null ",
								  "if(C_RETENTION in ('永久','30年') and C_ITEM_TYPE = '文件' and C_ARCHIVE_DATE between '"+ year +"-01-01 00:00:00' and '"+ year +"-12-31 23:59:59',1,0) countSign from ecm_document ed where C_ARC_CLASSIC is not null ",
								  "if(C_RETENTION = '永久' and C_ITEM_TYPE = '案卷' and C_ARCHIVE_DATE between '"+ year +"-01-01 00:00:00' and '"+ year +"-12-31 23:59:59',1,0) countSign from ecm_document ed where C_ARC_CLASSIC is not null ",
								  "if(C_RETENTION = '永久' and C_ITEM_TYPE = '文件' and C_ARCHIVE_DATE between '"+ year +"-01-01 00:00:00' and '"+ year +"-12-31 23:59:59',1,0) countSign from ecm_document ed where C_ARC_CLASSIC is not null ",
								  "CONTENT_SIZE as countSign from ecm_document ed where CONTENT_SIZE > 0 and C_ARCHIVE_DATE between '"+ year +"-01-01 00:00:00' and '"+ year +"-12-31 23:59:59' ",
								  "CONTENT_SIZE as countSign from ecm_document ed where CONTENT_SIZE > 0 and TYPE_NAME = '照片' and C_ARCHIVE_DATE between '"+ year +"-01-01 00:00:00' and '"+ year +"-12-31 23:59:59' ",
								  "CONTENT_SIZE as countSign from ecm_document ed where CONTENT_SIZE > 0 and TYPE_NAME = '录像录音文件' and C_ARCHIVE_DATE between '"+ year +"-01-01 00:00:00' and '"+ year +"-12-31 23:59:59' ",
								  "if(TYPE_NAME = '照片' and C_ARCHIVE_DATE between '"+ year +"-01-01 00:00:00' and '"+ year +"-12-31 23:59:59',1,0) countSign from ecm_document ed where C_ARC_CLASSIC is not null ",
								  "if(TYPE_NAME = '录像录音文件' and C_ARCHIVE_DATE between '"+ year +"-01-01 00:00:00' and '"+ year +"-12-31 23:59:59',1,0) countSign from ecm_document ed where C_ARC_CLASSIC is not null ",
								  "if(TYPE_NAME = '微缩胶片' and C_ARCHIVE_DATE between '"+ year +"-01-01 00:00:00' and '"+ year +"-12-31 23:59:59',1,0) countSign from ecm_document ed where C_ARC_CLASSIC is not null ",
								  "if(TYPE_NAME = '奖状等' and C_ARCHIVE_DATE between '"+ year +"-01-01 00:00:00' and '"+ year +"-12-31 23:59:59',1,0) countSign from ecm_document ed where C_ARC_CLASSIC is not null ",
								  "if(C_INCLUDE_PAPER = '有' and C_ITEM_TYPE = '案卷' and CONTENT_SIZE > 0 and C_ARCHIVE_DATE between '"+ year +"-01-01 00:00:00' and '"+ year +"-12-31 23:59:59',1,0) countSign from ecm_document ed where C_ARC_CLASSIC is not null ",
								  "CONTENT_SIZE as countSign from ecm_document ed where CONTENT_SIZE > 0 and C_INCLUDE_PAPER = '有' and C_ITEM_TYPE = '案卷' and C_ARCHIVE_DATE between '"+ year +"-01-01 00:00:00' and '"+ year +"-12-31 23:59:59' ",
								  "if(C_INCLUDE_PAPER = '有' and C_ITEM_TYPE = '文件' and CONTENT_SIZE > 0 and C_ARCHIVE_DATE between '"+ year +"-01-01 00:00:00' and '"+ year +"-12-31 23:59:59',1,0) countSign from ecm_document ed where C_ARC_CLASSIC is not null ",
								  "CONTENT_SIZE as countSign from ecm_document ed where CONTENT_SIZE > 0 and C_INCLUDE_PAPER = '有' and C_ITEM_TYPE = '文件' and C_ARCHIVE_DATE between '"+ year +"-01-01 00:00:00' and '"+ year +"-12-31 23:59:59' ",
								  "CONTENT_SIZE as countSign from ecm_document ed where CONTENT_SIZE > 0 and TYPE_NAME = '图片' and C_ARCHIVE_DATE between '"+ year +"-01-01 00:00:00' and '"+ year +"-12-31 23:59:59' ",
								  "CONTENT_SIZE as countSign from ecm_document ed where CONTENT_SIZE > 0 and TYPE_NAME = '录像录音文件' and C_ARCHIVE_DATE between '"+ year +"-01-01 00:00:00' and '"+ year +"-12-31 23:59:59' ",
								  "CONTENT_SIZE as countSign from ecm_document ed where CONTENT_SIZE > 0 and C_ARC_CLASSIC is not null and TYPE_NAME not in ('图片','录像录音文件') and C_ARCHIVE_DATE between '"+ year +"-01-01 00:00:00' and '"+ year +"-12-31 23:59:59' ",
								  "if(C_INCLUDE_PAPER = '有' and C_ITEM_TYPE = '案卷' and C_ARCHIVE_DATE < '1949-10-01 14:00:00',1,0) countSign from ecm_document ed where C_ARC_CLASSIC is not null ",
								  "if(C_INCLUDE_PAPER = '有' and C_ITEM_TYPE = '文件' and C_ARCHIVE_DATE < '1949-10-01 14:00:00',1,0) countSign from ecm_document ed where C_ARC_CLASSIC is not null ",
								  "if(C_INCLUDE_PAPER = '有' and C_ITEM_TYPE = '案卷' and C_ARCHIVE_DATE between '1949-10-01 14:00:00' and '"+ year +"-12-31 23:59:59',1,0) countSign from ecm_document ed where C_ARC_CLASSIC is not null ",
								  "if(C_INCLUDE_PAPER = '有' and C_ITEM_TYPE = '文件' and C_ARCHIVE_DATE between '1949-10-01 14:00:00' and '"+ year +"-12-31 23:59:59',1,0) countSign from ecm_document ed where C_ARC_CLASSIC is not null ",
								  "if(C_ITEM_TYPE = '案卷' and C_ARCHIVE_DATE between '"+ year +"-01-01 00:00:00' and '"+ year +"-12-31 23:59:59',1,0) countSign from ecm_document ed where C_ARC_CLASSIC is not null ",
								  "if(C_ITEM_TYPE = '文件' and C_ARCHIVE_DATE between '"+ year +"-01-01 00:00:00' and '"+ year +"-12-31 23:59:59',1,0) countSign from ecm_document ed where C_ARC_CLASSIC is not null ",		//档案编目
								  "if(C_INCLUDE_PAPER = '有' and C_ITEM_TYPE = '案卷' and C_ARCHIVE_DATE like '%"+ thisYear +"%' and IS_RELEASED = 1,1,0) countSign from ecm_document ed where C_ARC_CLASSIC is not null ",
								  "if(C_INCLUDE_PAPER = '有' and C_ITEM_TYPE = '文件' and C_ARCHIVE_DATE like '%"+ thisYear +"%' and IS_RELEASED = 1,1,0) countSign from ecm_document ed where C_ARC_CLASSIC is not null ",
								  "CONTENT_SIZE as countSign from ecm_document ed where CONTENT_SIZE > 0 and C_ARCHIVE_DATE like '%"+ thisYear +"%' and IS_RELEASED = 1 ",
								  "CONTENT_SIZE as countSign from ecm_document ed where CONTENT_SIZE > 0 and TYPE_NAME = '照片' and C_ARCHIVE_DATE like '%"+ thisYear +"%' and IS_RELEASED = 1 ",
								  "CONTENT_SIZE as countSign from ecm_document ed where CONTENT_SIZE > 0 and TYPE_NAME = '录像录音文件' and C_ARCHIVE_DATE like '%"+ thisYear +"%' and IS_RELEASED = 1 ",
								  "if(TYPE_NAME = '照片' and C_ARCHIVE_DATE like '%"+ thisYear +"%' and IS_RELEASED = 1,1,0) countSign from ecm_document ed where C_ARC_CLASSIC is not null ",
								  "if(TYPE_NAME = '录像录音文件' and C_ARCHIVE_DATE like '%"+ thisYear +"%' and IS_RELEASED = 1,1,0) countSign from ecm_document ed where C_ARC_CLASSIC is not null ",
								  "if(TYPE_NAME = '奖状等' and C_ARCHIVE_DATE like '%"+ thisYear +"%' and IS_RELEASED = 1,1,0) countSign from ecm_document ed where C_ARC_CLASSIC is not null ",		//本年度接收档案
								  "if(C_INCLUDE_PAPER = '有' and C_ITEM_TYPE = '案卷' and C_ARCHIVE_DATE like '%"+ thisYear +"%' and STATUS = '已销毁',1,0) countSign from ecm_document ed where C_ARC_CLASSIC is not null ",
								  "if(C_INCLUDE_PAPER = '有' and C_ITEM_TYPE = '文件' and C_ARCHIVE_DATE like '%"+ thisYear +"%' and STATUS = '已销毁',1,0) countSign from ecm_document ed where C_ARC_CLASSIC is not null ",
								  "if(TYPE_NAME = '照片' and C_ARCHIVE_DATE like '%"+ thisYear +"%' and STATUS = '已销毁',1,0) countSign from ecm_document ed where C_ARC_CLASSIC is not null ",
								  "if(TYPE_NAME = '录像录音文件' and C_ARCHIVE_DATE like '%"+ thisYear +"%' and STATUS = '已销毁',1,0) countSign from ecm_document ed where C_ARC_CLASSIC is not null ",
								  "if(C_ARCHIVE_DATE like '%"+ thisYear +"%',1,0) countSign from ecm_document ed where ID in (select CHILD_ID from ecm_relation er where PARENT_ID in (select ID from ecm_document ed2)) ",
								  "if(C_ARCHIVE_DATE like '%"+ thisYear +"%' and C_ITEM_TYPE = '案卷',1,0) countSign from ecm_document ed where ID in (select CHILD_ID from ecm_relation er where PARENT_ID in (select ID from ecm_document ed2)) ",
								  "if(C_ARCHIVE_DATE like '%"+ thisYear +"%',1,0) countSign from ecm_document ed where ID in (select CHILD_ID from ecm_relation er where PARENT_ID in (select ID from ecm_document ed2 where TYPE_NAME in ('复制单','借阅单'))) ",
								  "if(C_ARCHIVE_DATE like '%"+ thisYear +"%' and C_ITEM_TYPE = '案卷',1,0) countSign from ecm_document ed where ID in (select CHILD_ID from ecm_relation er where PARENT_ID in (select ID from ecm_document ed2 where TYPE_NAME in ('复制单','借阅单'))) ",
								  "if(C_ARCHIVE_DATE like '%"+ thisYear +"%',1,0) countSign from ecm_document ed where ID in (select CHILD_ID from ecm_relation er where PARENT_ID in (select ID from ecm_document ed2 where TYPE_NAME not in ('复制单','借阅单'))) ",
								  "if(C_ARCHIVE_DATE like '%"+ thisYear +"%' and C_ITEM_TYPE = '案卷',1,0) countSign from ecm_document ed where ID in (select CHILD_ID from ecm_relation er where PARENT_ID in (select ID from ecm_document ed2 where TYPE_NAME not in ('复制单','借阅单'))) "};
			
			for(int i=0; i<condition.length; i++) {
				StringBuffer sqlAnnual = new StringBuffer();
				sqlAnnual.append("select sum(countSign) as countNumber " + 
						"from (select TYPE_NAME , @condition ) tt "); 
				int index = sqlAnnual.indexOf("@condition");
				sqlAnnual.replace(index, 72, condition[i]);
				List<Map<String, Object>> listAnnual = documentService.getMapList(getToken(), sqlAnnual.toString());
				
				Number numAnnual = getSponsor(listAnnual, "countNumber");
				outList.add(numAnnual.toString());
			}
			
		}catch(Exception ex) {
			ex.printStackTrace();
		}
		
		try{
			OrderCoatingPortExecl(fileName, sheetName, outList, request, response);
		}catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	private void OrderCoatingPortExecl(String fileName, String sheetName, List<String> OList, HttpServletRequest request, HttpServletResponse response) throws IOException{
		ServletOutputStream os = null;
		response.reset(); // 清空输出流
		
		try {
			os = response.getOutputStream(); // 取得输出流
			response.setHeader("Content-disposition",
					"attachment; filename=" + new String(fileName.getBytes(), "ISO-8859-1")); // 设定输出文件头
			response.setContentType("application/msexcel"); // 定义输出类型
			
			XSSFWorkbook wb = null;
			EcmContent en = null;
			
			if(ImportController.imporFolderId==null) {
				ImportController.imporFolderId = folderService.getObjectByPath(getToken(), "/系统配置/文件模板/报表模板").getId();
			}
			String sql = "select * from ecm_document where FOLDER_ID='"+ImportController.imporFolderId+"' and TYPE_NAME='模板'  order by CREATION_DATE DESC";
			List<Map<String, Object>> objList = queryService.executeSQL(getToken(), sql);

			String getFolderId = new String();
			
			getFolderId = (objList.get(0).get("ID")!=null)?(String)objList.get(0).get("ID"):"";
			
			en = contentService.getPrimaryContent(getToken(), getFolderId);
			
			InputStream iStream = contentService.getContentStream(getToken(), en);
			
			wb = (XSSFWorkbook) XSSFWorkbookFactory.create(iStream);   						//读取excel模板
			iStream.close(); 
			
			//读取模板内所有sheet内容
            XSSFSheet sheet = wb.getSheetAt(0);
            wb.setSheetName(0, sheetName);
            
            int x = 0;
            for(int r=41; r<143; r++) {
            	if((r >= 41 && r <= 42)||
            	   (r >= 45 && r <= 51)||
            	   (r >= 53 && r <= 56)||
            	   (r >= 59 && r <= 62)||
            	   (r >= 64 && r <= 66)||
            	   (r >= 68 && r <= 71)||
            	   (r >= 75 && r <= 76)||
            	   (r >= 79 && r <= 80)||
            	   (r >= 82 && r <= 84)||
            	   (r >= 86 && r <= 88)||
            	   (r >= 131 && r <= 132)||
            	   (r >= 134 && r <= 135)||
            	   (r >= 137 && r <= 143)) {
            		XSSFRow row = sheet.getRow(r);
            		Cell listcell = row.getCell(3);
            		listcell.setCellValue(OList.get(x));
            		x++;
            	}
            }
            
			 os.flush();
	          //输出模板
	         wb.write(os);
	         wb.close();
		}catch(Exception e) {
			e.printStackTrace();
		}finally {
			if (os != null) {
                os.close();
            }
		}
	}
	
	private String getStrValue(Map<String, Object> args, String key) {
		return (args.containsKey(key) && args.get(key)!=null)?args.get(key).toString():"";
	}
	
	private boolean isValidateList(List<Map<String,Object>> list) {
		if(list!=null && list.size()>0) {
			return true;
		}else {
			return false;
		}
	}
	
	private Number getSponsor(List<Map<String, Object>> list, String key) {
		Number a = 0;
		if(isValidateList(list)) {
			Map<String,Object> item =list.get(0);
			if(item!=null && item.containsKey(key) && item.get(key)!=null) {
				a = (Number) item.get(key);
			}
		}

		return a;
	}
}
