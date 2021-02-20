package com.ecm.portal.archivegc.service.impl;

import java.io.IOException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.dom4j.Document;
import org.dom4j.DocumentException;
import org.dom4j.Element;
import org.dom4j.io.SAXReader;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Primary;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Service;

import com.ecm.core.exception.EcmException;
import com.ecm.core.service.DocumentService;
import com.ecm.portal.archivegc.entity.QueryReport;
import com.ecm.portal.archivegc.entity.QueryTemplate;
import com.ecm.portal.archivegc.service.IReportService;

@Service
@Primary
public class ReportServiceImpl implements IReportService {

	@Autowired
	private DocumentService documentService;
	
	@Override
	public Map<String, String> annualReport(String selectYear, String currentYear) {
		Map<String, String> resultdata = new HashMap<String, String>();
		Element root = this.getAnnualRoot("report/annual.xml");
		if(root==null) {
			return new HashMap<String, String>();
		}
		List<Element> templateList= root.element("templates").elements("template"); 
		Map<String,QueryTemplate> tmpMap = new HashMap<>();
		for (Element element : templateList) {
			QueryTemplate tmpl = new QueryTemplate();
			tmpl.setId(element.attributeValue("id")); 
			tmpl.setTemplate(element.getTextTrim()); 
			tmpMap.put(tmpl.getId(),tmpl);
		}
		List<Element>reportList = root.element("reports").elements("report"); 
		Map<String,QueryReport> reportMap = new HashMap<>(); 
		for(Element element : reportList){
			String tmplId = element.attributeValue("tmpl"); 
			QueryTemplate tmplobj = tmpMap.get(tmplId);
			QueryReport report = new QueryReport();
			report.setNumber(element.attributeValue("index")); 
			report.setCondition(element.attributeValue("condition"));
			report.setTemplate(tmplobj);
			reportMap.put(report.getNumber(),report);
		}
		Iterator<String> keys = reportMap.keySet().iterator();
		while (keys.hasNext()) {
			String key = keys.next();
			QueryReport value = reportMap.get(key);
			String tmplString = value.getTemplate().getTemplate();
			tmplString=tmplString.replace("#{condition}",value.getCondition()).replace("#{year}", selectYear).replace("#{thisyear}", currentYear);			
			resultdata.put(key,queryResult(tmplString,"countNumber"));
		}
		
//		reportMap.forEach((key,value)->{
//			String tmplString = value.getTemplate().getTemplate();
//			tmplString=tmplString.replace("#{condition}",value.getCondition()).replace("#{year}", selectYear).replace("#{thisYear}", currentYear);
//			resultdata.put(key,queryResult(tmplString,"countNumber"));
//			
//		});
		return resultdata;
	}
	
	private String queryResult(String sql,String colname) {
		try {
			long start = System.currentTimeMillis();
			List<Map<String, Object>> list = documentService.getMapList("", sql);
			System.out.println("Runtime:"+(System.currentTimeMillis() - start));
			if(list!=null && list.size()>0 && list.get(0)!=null) {				
				return list.get(0).get(colname).toString();
			}else {
				return "0";
			}
		} catch (EcmException e) {
			e.printStackTrace();
			return "";
		}
	}
	
	
	private Element getAnnualRoot(String sourcePath) {
		ClassPathResource resource = new ClassPathResource(sourcePath);
		SAXReader reader = new SAXReader();
		Document document;
		try {
			document = reader.read(resource.getURL());
			Element root = document.getRootElement();
			return root;
		} catch (DocumentException | IOException e) {
			e.printStackTrace();
			return null;
		} 
	}

}
