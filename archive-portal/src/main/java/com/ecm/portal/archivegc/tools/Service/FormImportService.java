package com.ecm.portal.archivegc.tools.Service;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.text.DecimalFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.DateUtil;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.usermodel.WorkbookFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import com.alibaba.druid.util.StringUtils;
import com.alibaba.fastjson.JSONArray;
import com.ecm.common.util.DateUtils;
import com.ecm.common.util.FileUtils;
import com.ecm.core.cache.manager.CacheManagerOper;
import com.ecm.core.entity.EcmContent;
import com.ecm.core.entity.EcmDocument;
import com.ecm.core.entity.EcmForm;
import com.ecm.core.entity.EcmFormItem;
import com.ecm.core.entity.EcmObject;
import com.ecm.core.service.DocumentService;
import com.ecm.core.service.EcmService;
import com.ecm.core.service.FolderPathService;
import com.ecm.core.service.FolderService;
import com.ecm.core.service.FormItemService;
import com.ecm.core.service.FormService;
import com.ecm.core.service.NumberService;

@Service
public class FormImportService extends EcmService {

	@Autowired
	private DocumentService documentService;

	@Autowired
	private NumberService numberService;

	@Autowired
	private FolderService folderService;
	
	@Autowired
	private FolderPathService folderPathService;
	
	@Autowired
	private FormService formService;
	
	@Autowired
	private FormItemService formItemService;
	

	private static String importExcelFolderId;
	
	public String importExcel(String token,MultipartFile  excelSrcFile) throws Exception {
		StringBuilder sb = new StringBuilder();
		sb.append("开始导入:").append(DateUtils.currentDate("yyyy-MM-dd HH:mm:ss")).append("\r\n");
		Map<String,Object> map = new HashMap<String, Object>();
		map.put("TYPE_NAME", "导入批次");
		String number = DateUtils.currentDate("yyyy-MM-dd");
		String uploadFolder = CacheManagerOper.getEcmParameters().get("UploadFolder").getValue();
		if(!uploadFolder.endsWith(File.separator))
		{
			uploadFolder += File.separator;
		}
		uploadFolder += getSession(token).getCurrentUser().getLoginName()+File.separator+number+File.separator;
		File f = new File(uploadFolder);
		f.mkdirs();
		
		if(FormImportService.importExcelFolderId==null) {
			FormImportService.importExcelFolderId = folderService.getObjectByPath(token, "/表单/批量导入单").getId();
//			ImportService.importDocFolderId = folderService.getObjectByPath(token, "/移交文档").getId();
		}
		
		EcmDocument doc = new EcmDocument();
		doc.getAttributes().put("NAME", excelSrcFile.getOriginalFilename());
		doc.getAttributes().put("CODING", number);
		doc.setTypeName("导入批次");
		doc.setFolderId(importExcelFolderId);
		EcmContent content = new EcmContent();
		content.setName(excelSrcFile.getOriginalFilename());
		content.setContentSize(excelSrcFile.getSize());
		content.setFormatName(FileUtils.getExtention(excelSrcFile.getOriginalFilename()).toLowerCase());
		content.setInputStream(excelSrcFile.getInputStream());
		String folderId="";
		folderId=folderPathService.getFolderId(token, doc.getAttributes(), "3");
		content.setFields(folderId);
		doc.getAttributes().put("FORMAT_NAME", content.getFormatName());
		doc.getAttributes().put("CONTENT_SIZE", content.getContentSize());
		documentService.newObject(token, doc, content);
		excelSrcFile.getInputStream().close();
		String excelFile = content.getFilePath();
		String storePath = CacheManagerOper.getEcmStores().get(content.getStoreName()).getStorePath();
		FileInputStream fis = new FileInputStream(storePath+excelFile);
		Workbook workbook = WorkbookFactory.create(fis);
		if (fis != null) {
			fis.close();
		}
		int sheetCount = workbook.getNumberOfSheets();  //Sheet的数量  
		//遍历每个Sheet  
		for (int s = 0; s < sheetCount; s++) {
			String ParentId="";
			Sheet sheet = workbook.getSheetAt(s);
			EcmForm TypeName = new EcmForm();
			TypeName.setTypeName(sheet.getSheetName());
			TypeName.setIsDefault(true);
			TypeName.setAction("NEW");
			List<EcmForm> a = formService.getAllObject(token);
			boolean isTypeName=false;
			for (int i = 0; i < a.size(); i++) {
				if(a.get(i).getTypeName().equals(sheet.getSheetName())) {
					isTypeName=true;
					break;
				}
			}
			if(isTypeName) {
				sb.append("表"+sheet.getSheetName()+"已存在").append("\r\n");
				continue;
			}else{
				ParentId = formService.newObject(token,TypeName);
			}
		    int rowCount = sheet.getPhysicalNumberOfRows(); //获取总行数  
		    Map<Integer,String> attrNames = new HashMap<Integer,String>();
		    Map<String,Object> docAttrs = new HashMap<>();
		    for(int i=0;i<=sheet.getRow(1).getLastCellNum();i++) {
				if(sheet.getRow(0).getCell(i)!=null && !StringUtils.isEmpty(sheet.getRow(0).getCell(i).getStringCellValue())) {
					attrNames.put(i, sheet.getRow(0).getCell(i).getStringCellValue());
				}
			}
		     //遍历每一行  
		    for (int r = 2; r < rowCount; r++) {
		    	Row row = sheet.getRow(r); 
		    	if(row==null) {
		    		continue;
		    	}
		        int cellCount = row.getPhysicalNumberOfCells(); //获取总列数  
		         //遍历每一个单元格  
		        for (int c = 0; c < cellCount; c++) {
		            String val=getCellValue(row.getCell(c));
		             //按照字符串类型读取单元格内数据
		              /*在这里可以对每个单元格中的值进行二次操作转化*/
		            String a1 = attrNames.get(c);
		            if(a1==null) {
		            	continue;
		            }
		            docAttrs.put(a1, val);
		            
		        }
		        if(docAttrs.get("ATTR_NAME")==null) {
	            	continue;
	            }
		        List<EcmFormItem> formItem=formItemService.getFormItems(token, ParentId);
	            boolean isformItem=false;
				for (int i = 0; i < formItem.size(); i++) {
					if(formItem.get(i).getAttrName().equals(docAttrs.get("ATTR_NAME"))) {
						isformItem=true;
						break;
					}
				}
				if(isformItem) {
					sb.append("表"+sheet.getSheetName()+"中第"+r+"行字段"+docAttrs.get("ATTR_NAME")+"已存在").append("\r\n");
					continue;
				}else{
					EcmFormItem obj=new EcmFormItem();
			        obj.setAttributes(docAttrs);
			        obj.createId();
			        obj.setParentId(ParentId);
					formItemService.newObject(token,obj);
				}
		    }  
		}
		sb.append("完成导入:").append(DateUtils.currentDate("yyyy-MM-dd HH:mm:ss")).append("\r\n");
		return sb.toString();
		
	}
	/**
	 * 将InputStream写入本地文件
	 * @param destination 写入本地目录
	 * @param input	输入流
	 * @throws IOException
	 */
	private void writeToLocal(String destination, InputStream input)
			throws IOException {
		int index;
		byte[] bytes = new byte[1024];
		FileOutputStream downloadFile = new FileOutputStream(destination);
		while ((index = input.read(bytes)) != -1) {
			downloadFile.write(bytes, 0, index);
			downloadFile.flush();
		}
		downloadFile.close();
		input.close();
	}
	/**
	 * 读取Cell值
	 * @param cell
	 * @return
	 */
	private static String getCellValue(Cell cell) {
		String retVal = null;
		if(cell==null) {
			return null;
		}
		switch (cell.getCellType()) {
		case BOOLEAN:
			// 得到Boolean对象的方法
			retVal = cell.getBooleanCellValue()+"";
			break;
		case NUMERIC:
			// 先看是否是日期格式
			if (DateUtil.isCellDateFormatted(cell)) {
				// 读取日期格式
				Date dt= cell.getDateCellValue();
			} else {
				DecimalFormat df = new DecimalFormat();
				// 单元格的值,替换掉,
				retVal= df.format(cell.getNumericCellValue()).replace(",", "");

			}
			break;
		case FORMULA:
			// 读取公式
			retVal = cell.getCellFormula();
			break;
		case STRING:
			// 读取String
			retVal = cell.getRichStringCellValue().toString();
			break;
		default:
			break;
		}
		return retVal;
	}

}
