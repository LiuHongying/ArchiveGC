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
import com.ecm.core.entity.EcmRelation;
import com.ecm.core.service.AuthService;
import com.ecm.core.service.ContentService;
import com.ecm.core.service.DocumentService;
import com.ecm.core.service.FolderPathService;
import com.ecm.core.service.FolderService;
import com.ecm.icore.service.IEcmSession;

@Component(value = "AppraisalCompleteListener")
public class AppraisalComplete implements JavaDelegate {
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
		boolean isCancel = false;			//用来判断是否需要生成销毁单
		String CancelId = new String();					//销毁单ID
		String workflowSpecialUserName = env.getProperty("ecm.username");
		IEcmSession ecmSession = null;
		try {
			ecmSession = authService.login("workflow", workflowSpecialUserName, env.getProperty("ecm.password"));
			Map<String, Object> varMap = execution.getVariables();
			String formId = varMap.get("formId").toString();
			EcmDocument ecmObject = documentService.getObjectById(ecmSession.getToken(), formId);
			EcmContent content = new EcmContent();
			content = contentService.getPrimaryContent(ecmSession.getToken(), formId);
			InputStream is = content.getInputStream();
			XSSFWorkbook wb = new XSSFWorkbook(is);
			//获取到EXCEL了，现在开始操作//
			Sheet sheet = wb.getSheet("Data");					
			Map<String,Integer> mapping = new HashMap<>();
			for (Cell cell : sheet.getRow(0)) {
				mapping.put(cell.getStringCellValue(), cell.getColumnIndex());		//获取所有key labels
			}
			int idIndex = mapping.get("ID");
			int resIndex = mapping.get("鉴定结果");
			int newDateIndex = mapping.get("新保管期限");//获取相关列的index
			for(int i = 2 ;i <= sheet.getLastRowNum();i++) {
				Row temp = sheet.getRow(i);
				Cell ress = temp.getCell(i);
				if(ress!=null && ress.getStringCellValue().equals("销毁")) {
					isCancel = true;		//有状态为销毁的记录，这个时候说明需要创建销毁单
				}
			}
			if(isCancel == true) {
				Map<String,Object> cancelMap = new HashMap<String,Object>();
				cancelMap.put("status","新建");
				cancelMap.put("TYPE_NAME", "档案销毁单");
				CancelId = documentService.newObject(ecmSession.getToken(), cancelMap);
			}
			for(int rownum = 2;rownum <= sheet.getLastRowNum();rownum++) {		//默认第三行是数据
		    Row rows = sheet.getRow(rownum);
		    Cell res = rows.getCell(resIndex);
		    Cell idres = rows.getCell(idIndex);
		    Cell newDate = rows.getCell(newDateIndex);		   
		    if(res!=null) {
		    	if(res.getStringCellValue().equals("销毁")) {	//把表单文件加到销毁单上
		    	String id = idres.getStringCellValue();
		    	EcmRelation relation = new EcmRelation();
		    	relation.setParentId(CancelId);
		    	relation.setChildId(id);
		    	relation.setName("irel_children");
		    	}
		    	//自动延期更新属性
		    	if(res.getStringCellValue().equals("自动延期")&&newDate.getStringCellValue()!=null) {
		    		String id = idres.getStringCellValue();
		    		EcmDocument ecs = documentService.getObjectById(ecmSession.getToken(), id);
		    		String date = newDate.getStringCellValue();
		    		Map<String,Object> temps = ecs.getAttributes();
		    		temps.put("C_RETENTION",date);
		    		documentService.updateObject(ecmSession.getToken(), temps);         //更新
		    	}
		    }
		    }
			ecmObject.setStatus("已完成");
			documentService.updateObject(ecmSession.getToken(), ecmObject,null);
			is.close();
			wb.close();
	}
		catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

}
}