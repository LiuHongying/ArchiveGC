package com.ecm.portal.archivegc.workflowEvent;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

import org.apache.commons.io.FileUtils;
import org.apache.poi.openxml4j.exceptions.InvalidFormatException;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.xssf.usermodel.XSSFRow;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.flowable.engine.delegate.DelegateExecution;
import org.flowable.engine.delegate.JavaDelegate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Component;

import com.ecm.core.cache.manager.CacheManagerOper;
import com.ecm.core.entity.EcmContent;
import com.ecm.core.entity.EcmDocument;
import com.ecm.core.service.AuthService;
import com.ecm.core.service.ContentService;
import com.ecm.core.service.DocumentService;
import com.ecm.core.service.FolderPathService;
import com.ecm.core.service.FolderService;
import com.ecm.icore.service.IEcmSession;

@Component(value = "AppraisalListener")
public class AppraisalListener implements JavaDelegate {
	@Autowired
	private ContentService contentService;
	@Autowired
	private AuthService authService;
	@Autowired
	private Environment env;
	@Autowired
	private DocumentService documentService;

	private final Logger logger = LoggerFactory.getLogger(AppraisalListener.class);
	@Override
	public void execute(DelegateExecution execution) {
		
		
		String workflowSpecialUserName = env.getProperty("ecm.username");
		
		IEcmSession ecmSession = null;
		try {

		
		ecmSession = authService.login("workflow", workflowSpecialUserName, env.getProperty("ecm.password"));
		Map<String, Object> varMap = execution.getVariables();					//获取表单里传的MAP
		String formId = varMap.get("formId").toString();						//获取表单ID
		//String targetFile = srcTargetDir+"/"+srcTemplate.getName();
		//模板文件存放路径
		String templateFile = CacheManagerOper.getEcmParameters().get("UploadFolder").getValue()+File.separator+formId+"_in.xlsx";
		//修改后Excel文件路径
		String outFile = CacheManagerOper.getEcmParameters().get("UploadFolder").getValue()+File.separator+formId+"_out.xlsx";
		//读取模板
		List<Map<String, Object>>  list = documentService.getMapList(ecmSession.getToken(), "select ID from ecm_document where FOLDER_ID in(select ID from ecm_folder where FOLDER_PATH='/系统配置/文件模板/鉴定单模板' )");
		if(list.size()>0) {
			String templateId = list.get(0).get("ID").toString();
			EcmContent  content = contentService.getPrimaryContent(ecmSession.getToken(), templateId);
			InputStream instream = contentService.getContentStream(ecmSession.getToken(), content);
			int index;
			byte[] bytes = new byte[1024];
			FileOutputStream downloadFile = new FileOutputStream(templateFile);
			while ((index = instream.read(bytes)) != -1) {
				downloadFile.write(bytes, 0, index);
				downloadFile.flush();
			}
			downloadFile.close();
			instream.close();
		}
		else {
			throw new Exception("鉴定单模板不存在");
		}
		
		File operationTemplateFile = new File(templateFile);
		
		XSSFWorkbook workbook = new XSSFWorkbook(operationTemplateFile);		//先跟本地生成一个模板
		Sheet sheet = workbook.getSheetAt(0);
		if(sheet!=null) {
			String columns = "";
			Map<String,Integer> mapping = new HashMap<>();
			for (Cell cell : sheet.getRow(0)) {
				mapping.put(cell.getStringCellValue(), cell.getColumnIndex());					//获取所有key labels
				// 添加查询列
				if(cell.getStringCellValue() != null && cell.getStringCellValue().length()>0) {
					if(columns.length()>0) {
						columns += "," + cell.getStringCellValue();
					}else {
						columns +=  cell.getStringCellValue();
					}
				}
			}
			Set<String> mapkeys =  mapping.keySet();
			String formSqlStr = "select "+columns+" from ecm_document where id in (select child_id from ecm_relation where parent_id ='"+formId+"' and name = 'irel_children')";
			List<Map<String, Object>> datalist = documentService.getMapList(ecmSession.getToken(), formSqlStr);
			int rownum = 2;
			for (Map<String, Object> dataitem : datalist) {
				Row row = sheet.createRow(rownum);
				for (String key : mapkeys) {
					if(dataitem.containsKey(key)) {
						String value = dataitem.get(key).toString();
						int index = mapping.get(key);
						row.createCell(index).setCellValue(value);
					}
				}
				rownum++;
			}
		}
		OutputStream out = null;
		out = new FileOutputStream(outFile);
		workbook.write(out);
		File XL = new File(outFile);
		workbook.close();
		out.close();
		EcmDocument form = documentService.getObjectById(ecmSession.getToken(), formId);
		EcmContent excel = new EcmContent();
		long size = XL.length();
		InputStream XLS = new FileInputStream(XL);
		excel.setContentSize(size);
		excel.setName("鉴定清册.xlsx");
		excel.setInputStream(XLS);
				//关闭输入流
		documentService.updateObject(ecmSession.getToken(), form, excel);
		XLS.close();	
		// 删除临时文件
		XL.delete();
		operationTemplateFile.delete();
		
	} catch (IOException | InvalidFormatException e) {
		// TODO Auto-generated catch block
		e.printStackTrace();
	} catch (Exception e) {
		// TODO Auto-generated catch block
		e.printStackTrace();
	}
}
}