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
    private FolderPathService folderPathService;
	@Autowired
	private DocumentService documentService;
	@Autowired
	private FolderService folderService;
	private final Logger logger = LoggerFactory.getLogger(DocCommitComplete.class);
	@Override
	public void execute(DelegateExecution execution) {
		String mkdir = UUID.randomUUID().toString().replace("-", "");
		File srcTemplate = new File("E:/temps/template.xlsx");
		File srcTargetDir = new File("e:/temps/"+mkdir);
		String workflowSpecialUserName = env.getProperty("ecm.username");
		
		IEcmSession ecmSession = null;
		try {
		FileUtils.copyFileToDirectory(srcTemplate, srcTargetDir);
		
		ecmSession = authService.login("workflow", workflowSpecialUserName, env.getProperty("ecm.password"));
		Map<String, Object> varMap = execution.getVariables();					//获取表单里传的MAP
		String formId = varMap.get("formId").toString();						//获取表单ID
		String targetFile = srcTargetDir+"/"+srcTemplate.getName();
		
		File operationTemplateFile = new File(targetFile);
		
		XSSFWorkbook workbook = new XSSFWorkbook(operationTemplateFile);		//先跟本地生成一个模板
		Sheet sheet = workbook.getSheet("Data");
		if(sheet!=null) {
			Map<String,Integer> mapping = new HashMap<>();
			for (Cell cell : sheet.getRow(0)) {
				mapping.put(cell.getStringCellValue(), cell.getColumnIndex());					//获取所有key labels
			}
			
			Set<String> mapkeys =  mapping.keySet();
			
			
			String formSqlStr = "select * from ecm_document where id in (select child_id from ecm_relation where parent_id ='"+formId+"' and name = 'irel_children')";
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
		out = new FileOutputStream(srcTargetDir.getAbsolutePath()+"/"+"results.xlsx");
		workbook.write(out);
		File XL = new File(srcTargetDir.getAbsolutePath()+"/"+"results.xlsx");
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
		/**
		 * upload file
		 */
		
		FileUtils.deleteDirectory(srcTargetDir);
		
	} catch (IOException | InvalidFormatException e) {
		// TODO Auto-generated catch block
		e.printStackTrace();
	} catch (Exception e) {
		// TODO Auto-generated catch block
		e.printStackTrace();
	}
}
}