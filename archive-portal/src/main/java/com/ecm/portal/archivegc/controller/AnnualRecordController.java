package com.ecm.portal.archivegc.controller;

import java.io.IOException;
import java.io.InputStream;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Map;

import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.DataFormatter;
import org.apache.poi.xssf.usermodel.XSSFRow;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbookFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import com.ecm.common.util.JSONUtils;
import com.ecm.core.entity.EcmContent;
import com.ecm.core.exception.AccessDeniedException;
import com.ecm.core.exception.EcmException;
import com.ecm.core.service.ContentService;
import com.ecm.core.service.FolderService;
import com.ecm.core.service.QueryService;
import com.ecm.portal.archive.controller.ImportController;
import com.ecm.portal.archivegc.service.IReportService;
import com.ecm.portal.controller.ControllerAbstract;

@Controller
public class AnnualRecordController extends ControllerAbstract {
	
	@Autowired
	private FolderService folderService;
	@Autowired
	private QueryService queryService;
	@Autowired
	private ContentService contentService;
	
	@Autowired
	private IReportService iReportService;
	
	@RequestMapping(value = "/report/annual", method = RequestMethod.POST)
	@ResponseBody
	public void AnnualReportStatistic(HttpServletRequest request, HttpServletResponse response, @RequestBody String argStr) throws EcmException, AccessDeniedException, ParseException, org.dom4j.DocumentException {		
		Map<String, Object> args = JSONUtils.stringToMap(argStr);
		
		String fileName = this.getStrValue(args, "fileName");
		String sheetName = this.getStrValue(args, "sheetName");
		String yearSelect = this.getStrValue(args, "yearSelect");
		String year = String.valueOf(Integer.parseInt(yearSelect.substring(0, 4)) + 1);
		
		SimpleDateFormat sdf = new SimpleDateFormat("yyyy");
		Date date = new Date();        
		String thisYear =  sdf.format(date);
		
		try {
			Map<String,String> queryResultMap = iReportService.annualReport(year, thisYear);
			OrderCoatingPortExecl(fileName, sheetName, queryResultMap, request, response);
		}catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	private void OrderCoatingPortExecl(String fileName, String sheetName, Map<String, String> OMap, HttpServletRequest request, HttpServletResponse response) throws IOException{
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
            
            for(int r=41; r<143; r++) {
            	XSSFRow row = sheet.getRow(r);
            	Cell keyCell = row.getCell(2);
            	Cell listcell = row.getCell(3);
            	String currentNumber = getCellValue(keyCell);
            	if( !currentNumber.equals("—") && OMap.containsKey(currentNumber)){
            		listcell.setCellValue(OMap.get(currentNumber));
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
	
	public static String getCellValue(Cell cell) {
        String cellValue = "";
        // 以下是判断数据的类型
        switch (cell.getCellType()) {
            case NUMERIC: // 数字
                if (org.apache.poi.ss.usermodel.DateUtil.isCellDateFormatted(cell)) {
                    SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
                    cellValue = sdf.format(org.apache.poi.ss.usermodel.DateUtil.getJavaDate(cell.getNumericCellValue())).toString();
                } else {
                    DataFormatter dataFormatter = new DataFormatter();
                    cellValue = dataFormatter.formatCellValue(cell);
                }
                break;
            case STRING: // 字符串
                cellValue = cell.getStringCellValue();
                break;
            case BOOLEAN: // Boolean
                cellValue = cell.getBooleanCellValue() + "";
                break;
            case FORMULA: // 公式
                cellValue = cell.getCellFormula() + "";
                break;
            case BLANK: // 空值
                cellValue = "";
                break;
            case ERROR: // 故障
                cellValue = "非法字符";
                break;
            default:
                cellValue = "未知类型";
                break;
        }
        return cellValue;
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
