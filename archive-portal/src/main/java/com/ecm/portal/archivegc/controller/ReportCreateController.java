package com.ecm.portal.archivegc.controller;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

import com.alibaba.druid.util.StringUtils;
import com.ecm.common.util.JSONUtils;
import com.ecm.core.ActionContext;
import com.ecm.core.service.DocumentService;
import com.ecm.portal.controller.ControllerAbstract;

@RestController
@RequestMapping("/dms/record")
public class ReportCreateController extends ControllerAbstract {
	private final SimpleDateFormat shortSdf = new SimpleDateFormat("yyyy-MM-dd");
	private final SimpleDateFormat longHourSdf = new SimpleDateFormat();
	private final SimpleDateFormat longSdf = new SimpleDateFormat("yyyy-MM-dd"); 
	
	@Autowired
	private DocumentService documentService;
	

	@RequestMapping(value = "workMonthStatistic", method = RequestMethod.POST)
	@ResponseBody
	public Map<String, Object> workMonthStatistic(@RequestBody String argStr) {
		// TODO 工作量统计（月度） **需要修改
		Map<String, Object> mp = new HashMap<String, Object>();
		List<Map<String, Object> > outList = new ArrayList<Map<String, Object>>();
		Map<String, Object> args = JSONUtils.stringToMap(argStr);
		
		try {
			Map<String, Object> projMap = new HashMap<String, Object>();
			
			String startDate = this.getStrValue(args, "startDate");
			String endDate = this.getStrValue(args, "endDate");
			
			String timeCheck = new String();
			
			if(!StringUtils.isEmpty(startDate) && !StringUtils.isEmpty(endDate)) {
				timeCheck = setSQLTimeSE(startDate, endDate);
			}
			if(StringUtils.isEmpty(startDate) && !StringUtils.isEmpty(endDate)) {
				timeCheck = setSQLTimeE(endDate);
			}
			if(!StringUtils.isEmpty(startDate) && StringUtils.isEmpty(endDate)) {
				timeCheck = setSQLTimeS(startDate);
			}
			if(StringUtils.isEmpty(startDate) && StringUtils.isEmpty(endDate)) {
				timeCheck = setSQLTimeEmp();
			}
			// todo 
			String sqlStatistic = "select distinct year(EXCUTE_DATE) as year1, month(EXCUTE_DATE) as month1, USER_NAME, TYPE_NAME," + 
					"(select count(*) from ecm_audit_general eag2 left join ecm_document ed2 on eag2.DOC_ID = ed2.ID where eag2.ACTION_NAME = '接收' and ed2.C_ITEM_TYPE = '案卷' and ed2.TYPE_NAME = ed.TYPE_NAME and eag2.USER_NAME = eag.USER_NAME and ed2.C_ARC_CLASSIC IS NOT NULL) as workstorcount, " + 
					"(select count(*) from ecm_audit_general eag2 left join ecm_document ed2 on eag2.DOC_ID = ed2.ID where eag2.ACTION_NAME = '接收' and ed2.C_ITEM_TYPE = '文件' and ed2.TYPE_NAME = ed.TYPE_NAME and eag2.USER_NAME = eag.USER_NAME and ed2.C_ARC_CLASSIC IS NOT NULL) as workstjorcount, " + 
					"(select count(*) from ecm_audit_general eag2 left join ecm_document ed2 on eag2.DOC_ID = ed2.ID where eag2.ACTION_NAME = '入库' and ed2.C_ITEM_TYPE = '案卷' and ed2.TYPE_NAME = ed.TYPE_NAME and eag2.USER_NAME = eag.USER_NAME and ed2.C_ARC_CLASSIC IS NOT NULL) as workgetcount, " + 
					"(select count(*) from ecm_audit_general eag2 left join ecm_document ed2 on eag2.DOC_ID = ed2.ID where eag2.ACTION_NAME = '入库' and ed2.C_ITEM_TYPE = '文件' and ed2.TYPE_NAME = ed.TYPE_NAME and eag2.USER_NAME = eag.USER_NAME and ed2.C_ARC_CLASSIC IS NOT NULL) as workgetjicount " + 
					"from ecm_audit_general eag left join ecm_document ed on eag.DOC_ID = ed.ID " + 
					"where ed.C_ARC_CLASSIC IS NOT NULL and (eag.ACTION_NAME = '入库' or eag.ACTION_NAME = '接收') " + timeCheck + " order by USER_NAME, month1, TYPE_NAME ";
			
			List<Map<String, Object>> listWorkStatistic = documentService.getMapList(getToken(), sqlStatistic);
			
			for(int i = 0; i<listWorkStatistic.size(); i++) {
				projMap = new HashMap<String, Object>();
				String wfName = (listWorkStatistic.get(i).get("USER_NAME")!=null)?(String)listWorkStatistic.get(i).get("USER_NAME"):"";
				projMap.put("wfName", wfName);
				Number wfMonth =  getSponsorFor(listWorkStatistic, "month1", i);
				projMap.put("wfMonth", wfMonth);
				String wfType =  (listWorkStatistic.get(i).get("TYPE_NAME")!=null)?(String)listWorkStatistic.get(i).get("TYPE_NAME"):"";
				projMap.put("wfType", wfType);
				Number receFiles = getSponsorFor(listWorkStatistic, "workstorcount", i);
				projMap.put("receFiles", receFiles);
				Number receDoc = getSponsorFor(listWorkStatistic, "workstjorcount", i);
				projMap.put("receDoc", receDoc);
				Number storeFiles = getSponsorFor(listWorkStatistic, "workgetcount", i);
				projMap.put("storeFiles", storeFiles);
				Number storeDoc = getSponsorFor(listWorkStatistic, "workgetjicount", i);
				projMap.put("storeDoc", storeDoc);
				outList.add(projMap);
			}
			
			mp.put("data", outList);
			mp.put("code", ActionContext.SUCESS);
		}catch(Exception ex) {
			ex.printStackTrace();
			mp.put("code", ActionContext.FAILURE);
		}
		
		return mp;
	}
	
	@RequestMapping(value = "workQuarterStatistic", method = RequestMethod.POST)
	@ResponseBody	
	public Map<String, Object> workQuarterStatistic(@RequestBody String argStr) {
		Map<String, Object> mp = new HashMap<String, Object>();
		List<Map<String, Object> > outList = new ArrayList<Map<String, Object>>();
		// TODO 工作量统计（季度）需要修改
		try {
			Map<String, Object> projMap = new HashMap<String, Object>();
			
			String sqlQuarter = "select distinct TYPE_NAME, "
					+ "(select count(*) from ecm_audit_general eag2 left join ecm_document ed2 on eag2.DOC_ID = ed2.ID where eag2.ACTION_NAME = '入库' and ed2.C_ITEM_TYPE = '案卷' and ed2.TYPE_NAME = ed.TYPE_NAME and eag2.USER_NAME = eag.USER_NAME and (eag.EXCUTE_DATE between "+ getCurrentQuarterStart(false) +" and "+ getCurrentQuarterEnd(false) +")) as workFileQT, "
					+ "(select count(*) from ecm_audit_general eag2 left join ecm_document ed2 on eag2.DOC_ID = ed2.ID where eag2.ACTION_NAME = '入库' and ed2.C_ITEM_TYPE = '文件' and ed2.TYPE_NAME = ed.TYPE_NAME and eag2.USER_NAME = eag.USER_NAME and (eag.EXCUTE_DATE between "+ getCurrentQuarterStart(false) +" and "+ getCurrentQuarterEnd(false) +")) as workDocQT, "
					+ "(select count(*) from ecm_audit_general eag2 left join ecm_document ed2 on eag2.DOC_ID = ed2.ID where eag2.ACTION_NAME = '入库' and ed2.C_ITEM_TYPE = '案卷' and ed2.TYPE_NAME = ed.TYPE_NAME and eag2.USER_NAME = eag.USER_NAME and (eag.EXCUTE_DATE between "+ getCurrentQuarterStart(true) +" and "+ getCurrentQuarterEnd(true) +")) as workFileQL, "
					+ "(select count(*) from ecm_audit_general eag2 left join ecm_document ed2 on eag2.DOC_ID = ed2.ID where eag2.ACTION_NAME = '入库' and ed2.C_ITEM_TYPE = '文件' and ed2.TYPE_NAME = ed.TYPE_NAME and eag2.USER_NAME = eag.USER_NAME and (eag.EXCUTE_DATE between "+ getCurrentQuarterStart(true) +" and "+ getCurrentQuarterEnd(true) +")) as workDocQL "
					+ "from ecm_audit_general eag left join ecm_document ed on eag.DOC_ID = ed.ID "
					+ "where ed.C_ARC_CLASSIC IS NOT NULL order by TYPE_NAME ";
			
			List<Map<String, Object>> listWorkQStatistic = documentService.getMapList(getToken(), sqlQuarter);
			
			for(int i=0; i<listWorkQStatistic.size(); i++) {
				projMap = new HashMap<String, Object>();
				String wfTypeQuarter =  (listWorkQStatistic.get(i).get("TYPE_NAME")!=null)?(String)listWorkQStatistic.get(i).get("TYPE_NAME"):"";
				projMap.put("wfTypeQuarter", wfTypeQuarter);
				Number wfFileQT =  getSponsorFor(listWorkQStatistic, "workFileQT", i);
				projMap.put("wfFileQT", wfFileQT);
				Number wfDocQT =  getSponsorFor(listWorkQStatistic, "workDocQT", i);
				projMap.put("wfDocQT", wfDocQT);
				Number wfFileQL =  getSponsorFor(listWorkQStatistic, "workFileQL", i);
				projMap.put("wfFileQL", wfFileQL);
				Number wfDocQL =  getSponsorFor(listWorkQStatistic, "workDocQL", i);
				projMap.put("wfDocQL", wfDocQL);
				Number gRateFile = wfFileQT.intValue()-wfFileQL.intValue();
				projMap.put("gRateFile", gRateFile);
				Number gRateDoc = wfDocQT.intValue() - wfDocQL.intValue();
				projMap.put("gRateDoc", gRateDoc);
				outList.add(projMap);
			}
			
			mp.put("data", outList);
			mp.put("code", ActionContext.SUCESS);
		}catch(Exception ex) {
			ex.printStackTrace();
			mp.put("code", ActionContext.FAILURE);
		}
		
		return mp;
	}
	
	//设计文件
	@RequestMapping(value = "designStatistic", method = RequestMethod.POST)
	@ResponseBody
	public Map<String, Object> designStatistic(@RequestBody String argStr) {
		Map<String, Object> mp = new HashMap<String, Object>();
		List<Map<String, Object> > outList = new ArrayList<Map<String, Object>>();
		// TODO 设计文件工作量统计  需要修改
		Map<String, Object> args = JSONUtils.stringToMap(argStr);
			
		try {
			
			
			String startDate = this.getStrValue(args, "startDate");
			String endDate = this.getStrValue(args, "endDate");
			
			String timeCheck = new String();
			
			if(!StringUtils.isEmpty(startDate) && !StringUtils.isEmpty(endDate)) {
				timeCheck = setSQLTimeSE(startDate, endDate);
			}
			if(StringUtils.isEmpty(startDate) && !StringUtils.isEmpty(endDate)) {
				timeCheck = setSQLTimeE(endDate);
			}
			if(!StringUtils.isEmpty(startDate) && StringUtils.isEmpty(endDate)) {
				timeCheck = setSQLTimeS(startDate);
			}
			if(StringUtils.isEmpty(startDate) && StringUtils.isEmpty(endDate)) {
				timeCheck = setSQLTimeEmp();
			}
			
			String sqlStatistic = "select distinct eag.EXCUTE_DATE, eag.USER_NAME," + 
					"(select count(*) from ecm_audit_general eag2 left join ecm_document ed2 on eag2.DOC_ID = ed2.ID where eag2.ACTION_NAME = '著录' and eag2.USER_NAME = eag.USER_NAME and ed2.C_ARC_CLASSIC IS NOT NULL) as workstorcount, " + 
					"(select count(*) from ecm_audit_general eag2 left join ecm_document ed2 on eag2.DOC_ID = ed2.ID where ed2.C_ITEM_TYPE = '设计文件修改单' and eag2.USER_NAME = eag.USER_NAME and ed2.C_ARC_CLASSIC IS NOT NULL) as workstjorcount, " + 
					"(select count(*) from ecm_audit_general eag2 left join ecm_document ed2 on eag2.DOC_ID = ed2.ID where eag2.ACTION_NAME = '质检' and eag2.USER_NAME = eag.USER_NAME and ed2.C_ARC_CLASSIC IS NOT NULL) as workgetcount, " + 
					"(select count(*) from ecm_audit_general eag2 left join ecm_document ed2 on eag2.DOC_ID = ed2.ID where ed2.Status = '作废' and eag2.USER_NAME = eag.USER_NAME and ed2.C_ARC_CLASSIC IS NOT NULL) as workgetjicount " + 
					"from ecm_audit_general eag left join ecm_document ed on eag.DOC_ID = ed.ID " + 
					"where ed.C_ARC_CLASSIC IS NOT NULL " + timeCheck + " order by EXCUTE_DATE, USER_NAME";
			
			List<Map<String, Object>> listWorkStatistic = documentService.getMapList(getToken(), sqlStatistic);
			List<Map<String, Object>> resultList = new ArrayList<>();
			Set<String> onlyKeys = new HashSet<String>();
			//1 type
			for(int i = 0; i<listWorkStatistic.size(); i++) {
				Map<String, Object> item = listWorkStatistic.get(i);
				String key = item.get("ymd").toString()+item.get("user_name").toString();
				if(!onlyKeys.contains(key)) {
					onlyKeys.add(key);
					resultList.add(item);
				}
			}
			
			for (Map<String, Object> item : resultList) {
				
			}
			
			//2 type
			for (int i = listWorkStatistic.size()-1; i >=0 ; i--) {
				Map<String, Object> item = listWorkStatistic.get(i);
				String key = item.get("ymd").toString()+item.get("user_name").toString();
				if(onlyKeys.contains(key)) {
					listWorkStatistic.remove(i);
				}else {
					onlyKeys.add(key);					
				}
			}
				
			for(int i = 0; i<listWorkStatistic.size(); i++) {
				Map<String, Object> item = listWorkStatistic.get(i);
				Map<String, Object> projMap = new HashMap<String, Object>();	
				String wfName = (item.get("USER_NAME")!=null)?(String)item.get("USER_NAME"):"";
				projMap.put("wfName", wfName);
				Number wfMonth =  getSponsorFor(listWorkStatistic, "month1", i);
				projMap.put("wfMonth", wfMonth);
				Number receFiles = getSponsorFor(listWorkStatistic, "workstorcount", i);
				projMap.put("receFiles", receFiles);
				Number receDoc = getSponsorFor(listWorkStatistic, "workstjorcount", i);
				projMap.put("receDoc", receDoc);
				Number storeFiles = getSponsorFor(listWorkStatistic, "workgetcount", i);
				projMap.put("storeFiles", storeFiles);
				Number storeDoc = getSponsorFor(listWorkStatistic, "workgetjicount", i);
				projMap.put("storeDoc", storeDoc);
				outList.add(projMap);
			}
			
			
			
			
			mp.put("data", outList);
			mp.put("code", ActionContext.SUCESS);
		}catch(Exception ex) {
			ex.printStackTrace();
			mp.put("code", ActionContext.FAILURE);
		}
		return mp;
	}
	
	//商务文件工作量统计表
	@RequestMapping(value = "commercialtatistic", method = RequestMethod.POST)
	@ResponseBody	
	public Map<String, Object> commercialtatistic(@RequestBody String argStr) {
		Map<String, Object> mp = new HashMap<String, Object>();
		List<Map<String, Object> > outList = new ArrayList<Map<String, Object>>();
		Map<String, Object> args = JSONUtils.stringToMap(argStr);
		
		String startDate = this.getStrValue(args, "startDate");
		String endDate = this.getStrValue(args, "endDate");
		
		try {
			Map<String, Object> projMap = new HashMap<String, Object>(); 
			
			String sqlPreArchive = "select distinct b.C_ARC_CLASSIC," + 
					"(select count(*) from ecm_audit_general a2 left join ecm_document b2 on a2.DOC_ID = b2.ID where b2.TYPE_NAME in ('合同管理案卷','合同管理文件') and (a2.EXCUTE_DATE between '"+ startDate +"' and '"+ endDate+"')) as changeCountStored " + 
					"from ecm_audit_general a left join ecm_document b on a.DOC_ID = b.ID where b.C_ARC_CLASSIC = '商务管理'";
			String sqlStored = "select distinct b.STATUS," + 
					"(select count(*) from ecm_audit_general a2 left join ecm_document b2 on a2.DOC_ID = b2.ID where b2.TYPE_NAME in ('招标文件案卷','招标文件','投标文件案卷','投标文件') and (a2.EXCUTE_DATE between '"+ startDate +"' and '"+ endDate+"')) as changeCountStored " + 
					"from ecm_audit_general a left join ecm_document b on a.DOC_ID = b.ID where b.C_ARC_CLASSIC = '商务管理'";
			String sqlOther = "select distinct b.STATUS," + 
					"(select count(*) from ecm_audit_general a2 left join ecm_document b2 on a2.DOC_ID = b2.ID where b2.TYPE_NAME not in ('合同管理案卷','合同管理文件','招标文件案卷','招标文件','投标文件案卷','投标文件') and (a2.EXCUTE_DATE between '"+ startDate +"' and '"+ endDate+"')) as changeCountStored " + 
					"from ecm_audit_general a left join ecm_document b on a.DOC_ID = b.ID where b.C_ARC_CLASSIC = '商务管理'";
			
			List<Map<String, Object>> listPreArchive = documentService.getMapList(getToken(), sqlPreArchive);
			List<Map<String, Object>> listStored = documentService.getMapList(getToken(), sqlStored);
			List<Map<String, Object>> listOther = documentService.getMapList(getToken(), sqlOther);
			
			projMap.put("fileType", "合同文件");
			Number countHetong = getSponsorTN(listPreArchive, "changeCountStored");
			projMap.put("fileCount", countHetong);
			outList.add(projMap);
			
			projMap = new HashMap<String, Object>();
			projMap.put("fileType", "招标投标文件");
			Number countZhaotoubiao = getSponsorTN(listStored, "changeCountStored");
			projMap.put("fileCount", countZhaotoubiao);
			outList.add(projMap);
			
			projMap = new HashMap<String, Object>();
			projMap.put("fileType", "其他文件");
			Number countOther = getSponsorTN(listOther, "changeCountStored");
			projMap.put("fileCount", countOther);
			outList.add(projMap);
			
			mp.put("data", outList);
			mp.put("code", ActionContext.SUCESS);
		}catch(Exception ex) {
			ex.printStackTrace();
			mp.put("code", ActionContext.FAILURE);
		}
		return mp;
	}
	
	
	//声像文件工作量统计表
	@RequestMapping(value = "AudVidQuarterStatistic", method = RequestMethod.POST)
	@ResponseBody	
	public Map<String, Object> AudVidQuarterStatistic(@RequestBody String argStr) {
		Map<String, Object> mp = new HashMap<String, Object>();
		List<Map<String, Object> > outList = new ArrayList<Map<String, Object>>();
		
		Map<String, Object> args = JSONUtils.stringToMap(argStr);
		
		String yearSelect = this.getStrValue(args, "yearSelect");
		
		String year = String.valueOf(Integer.parseInt(yearSelect.substring(0, 4)) + 1);
		
		try {
			Map<String, Object> projMap = new HashMap<String, Object>(); 
			
			String sqlQuarter = "select distinct TYPE_NAME, " + 
					"(select count(*) from ecm_audit_general eag2 left join ecm_document ed2 on eag2.DOC_ID = ed2.ID where (eag2.EXCUTE_DATE between '"+ year +"-01-01' and '"+ year +"-03-31') and ed2.TYPE_NAME = ed.TYPE_NAME) as quarter1," + 
					"(select count(*) from ecm_audit_general eag2 left join ecm_document ed2 on eag2.DOC_ID = ed2.ID where (eag2.EXCUTE_DATE between '"+ year +"-04-01' and '"+ year +"-06-30') and ed2.TYPE_NAME = ed.TYPE_NAME) as quarter2," + 
					"(select count(*) from ecm_audit_general eag2 left join ecm_document ed2 on eag2.DOC_ID = ed2.ID where (eag2.EXCUTE_DATE between '"+ year +"-07-01' and '"+ year +"-09-30') and ed2.TYPE_NAME = ed.TYPE_NAME) as quarter3," + 
					"(select count(*) from ecm_audit_general eag2 left join ecm_document ed2 on eag2.DOC_ID = ed2.ID where (eag2.EXCUTE_DATE between '"+ year +"-10-01' and '"+ year +"-12-31') and ed2.TYPE_NAME = ed.TYPE_NAME) as quarter4 " + 
					"from ecm_audit_general eag left join ecm_document ed on eag.DOC_ID = ed.ID " + 
					"where ed.TYPE_NAME in ('照片','录像','录音')";
			
			List<Map<String, Object>> listWorkStatistic = documentService.getMapList(getToken(), sqlQuarter);
			
			for(int i=0; i<listWorkStatistic.size(); i++) {
				projMap = new HashMap<String, Object>();
				String avType = (listWorkStatistic.get(i).get("TYPE_NAME")!=null)?(String)listWorkStatistic.get(i).get("TYPE_NAME"):"";
				projMap.put("avType", avType);
				Number quarterOne = getSponsorFor(listWorkStatistic, "quarter1", i);
				projMap.put("quarterOne", quarterOne);
				Number quarterTwo = getSponsorFor(listWorkStatistic, "quarter2", i);
				projMap.put("quarterTwo", quarterTwo);
				Number quarterThree = getSponsorFor(listWorkStatistic, "quarter3", i);
				projMap.put("quarterThree", quarterThree);
				Number quarterFour = getSponsorFor(listWorkStatistic, "quarter4", i);
				projMap.put("quarterFour", quarterFour);
				
				outList.add(projMap);
			}
			
			
			
			mp.put("data", outList);
			mp.put("code", ActionContext.SUCESS);
		}catch(Exception ex) {
			ex.printStackTrace();
			mp.put("code", ActionContext.FAILURE);
		}
		
		return mp;
	}
	
	//科研文件工作量统计表
	@RequestMapping(value = "scientificQuarterStatistic", method = RequestMethod.POST)
	@ResponseBody	
	public Map<String, Object> scientificQuarterStatistic(@RequestBody String argStr) {
		Map<String, Object> mp = new HashMap<String, Object>();
		List<Map<String, Object> > outList = new ArrayList<Map<String, Object>>();
		Map<String, Object> args = JSONUtils.stringToMap(argStr);
		
		String startDate = this.getStrValue(args, "startDate");
		String endDate = this.getStrValue(args, "endDate");
		
		try {
			Map<String, Object> projMap = new HashMap<String, Object>(); 
			
			String sqlPreArchive = "select distinct b.STATUS," + 
					"(select count(*) from ecm_audit_general a2 left join ecm_document b2 on a2.DOC_ID = b2.ID where b2.TYPE_NAME = '科研文件' and b2.STATUS = b.STATUS and (a2.EXCUTE_DATE between '"+ startDate +"' and '"+ endDate+"')) as fileCountPre, " + 
					"(select count(*) from ecm_audit_general a2 left join ecm_document b2 on a2.DOC_ID = b2.ID where b2.TYPE_NAME = '科研文件借阅单' and b2.STATUS = b.STATUS and (a2.EXCUTE_DATE between '"+ startDate +"' and '"+ endDate+"')) as changeCountPre " + 
					"from ecm_audit_general a left join ecm_document b on a.DOC_ID = b.ID where b.STATUS in ('预归档')";
			String sqlStored = "select distinct b.STATUS," + 
					"(select count(*) from ecm_audit_general a2 left join ecm_document b2 on a2.DOC_ID = b2.ID where b2.TYPE_NAME = '科研文件' and b2.STATUS = b.STATUS and (a2.EXCUTE_DATE between '"+ startDate +"' and '"+ endDate+"')) as fileCountStored, " + 
					"(select count(*) from ecm_audit_general a2 left join ecm_document b2 on a2.DOC_ID = b2.ID where b2.TYPE_NAME = '科研文件修改单' and b2.STATUS = b.STATUS and (a2.EXCUTE_DATE between '"+ startDate +"' and '"+ endDate+"')) as changeCountStored " + 
					"from ecm_audit_general a left join ecm_document b on a.DOC_ID = b.ID where b.STATUS in ('已入库')";
			
			List<Map<String, Object>> listPreArchive = documentService.getMapList(getToken(), sqlPreArchive);
			List<Map<String, Object>> listStored = documentService.getMapList(getToken(), sqlStored);
			
			projMap.put("fileType", "预归档文件");
			Number countPreScince = getSponsorTN(listPreArchive, "fileCountPre");
			projMap.put("fileCountPre", countPreScince);
			Number countPreChange = getSponsorTN(listPreArchive, "fileCountPre");
			projMap.put("changeCountPre", countPreChange);
			outList.add(projMap);
			
			projMap = new HashMap<String, Object>();
			projMap.put("fileType", "归档文件");
			Number countStoredScince = getSponsorTN(listStored, "fileCountPre");
			projMap.put("fileCountPre", countStoredScince);
			Number countStoredChange = getSponsorTN(listStored, "fileCountPre");
			projMap.put("changeCountPre", countStoredChange);
			
			outList.add(projMap);
			mp.put("data", outList);
			mp.put("code", ActionContext.SUCESS);
		}catch(Exception ex) {
			ex.printStackTrace();
			mp.put("code", ActionContext.FAILURE);
		}
		
		return mp;
	}
	
	//其他类型文件
	@RequestMapping(value = "otherFileStatistic", method = RequestMethod.POST)
	@ResponseBody
	public Map<String, Object> otherFileStatistic(@RequestBody String argStr){
		Map<String, Object> mp = new HashMap<String, Object>();
		List<Map<String, Object> > outList = new ArrayList<Map<String, Object>>();
		
		Map<String, Object> args = JSONUtils.stringToMap(argStr);
		
		String yearSelect = this.getStrValue(args, "yearSelect");
		String year = String.valueOf(Integer.parseInt(yearSelect.substring(0, 4)) + 1);
		
		try {
			Map<String, Object> projMap = new HashMap<String, Object>(); 
			
			String sqlQuarter = "select distinct TYPE_NAME, " + 
					"(select count(*) from ecm_audit_general eag2 left join ecm_document ed2 on eag2.DOC_ID = ed2.ID where (eag2.EXCUTE_DATE between '"+ year +"-01-01' and '"+ year +"-03-31') and ed2.TYPE_NAME = ed.TYPE_NAME) as quarter1," + 
					"(select count(*) from ecm_audit_general eag2 left join ecm_document ed2 on eag2.DOC_ID = ed2.ID where (eag2.EXCUTE_DATE between '"+ year +"-04-01' and '"+ year +"-06-30') and ed2.TYPE_NAME = ed.TYPE_NAME) as quarter2," + 
					"(select count(*) from ecm_audit_general eag2 left join ecm_document ed2 on eag2.DOC_ID = ed2.ID where (eag2.EXCUTE_DATE between '"+ year +"-07-01' and '"+ year +"-09-30') and ed2.TYPE_NAME = ed.TYPE_NAME) as quarter3," + 
					"(select count(*) from ecm_audit_general eag2 left join ecm_document ed2 on eag2.DOC_ID = ed2.ID where (eag2.EXCUTE_DATE between '"+ year +"-10-01' and '"+ year +"-12-31') and ed2.TYPE_NAME = ed.TYPE_NAME) as quarter4 " + 
					"from ecm_audit_general eag left join ecm_document ed on eag.DOC_ID = ed.ID " + 
					"where ed.TYPE_NAME in ('鉴定证书','设计变更','公司刊物','设备文件','行政文件','项目归档文件','参考资料')";
			
			String sqlEngine = "select ed.C_ARC_CLASSIC, " + 
					"(select count(*) from ecm_audit_general eag2 left join ecm_document ed2 on eag2.DOC_ID = ed2.ID where (eag2.EXCUTE_DATE between '"+ year +"-01-01' and '"+ year +"-03-31') and ed2.TYPE_NAME = ed.TYPE_NAME) as quarter1," + 
					"(select count(*) from ecm_audit_general eag2 left join ecm_document ed2 on eag2.DOC_ID = ed2.ID where (eag2.EXCUTE_DATE between '"+ year +"-04-01' and '"+ year +"-06-30') and ed2.TYPE_NAME = ed.TYPE_NAME) as quarter2," + 
					"(select count(*) from ecm_audit_general eag2 left join ecm_document ed2 on eag2.DOC_ID = ed2.ID where (eag2.EXCUTE_DATE between '"+ year +"-07-01' and '"+ year +"-09-30') and ed2.TYPE_NAME = ed.TYPE_NAME) as quarter3," + 
					"(select count(*) from ecm_audit_general eag2 left join ecm_document ed2 on eag2.DOC_ID = ed2.ID where (eag2.EXCUTE_DATE between '"+ year +"-10-01' and '"+ year +"-12-31') and ed2.TYPE_NAME = ed.TYPE_NAME) as quarter4 " + 
					"from ecm_audit_general eag left join ecm_document ed on eag.DOC_ID = ed.ID " + 
					"where ed.C_ARC_CLASSIC = '工程建设' and ed.TYPE_NAME in ('工程管理案卷','工程管理文件')";
			
			String sqlProcess = "select ed.C_ARC_CLASSIC, " + 
					"(select count(*) from ecm_audit_general eag2 left join ecm_document ed2 on eag2.DOC_ID = ed2.ID where (eag2.EXCUTE_DATE between '"+ year +"-01-01' and '"+ year +"-03-31') and ed2.TYPE_NAME = ed.TYPE_NAME) as quarter1," + 
					"(select count(*) from ecm_audit_general eag2 left join ecm_document ed2 on eag2.DOC_ID = ed2.ID where (eag2.EXCUTE_DATE between '"+ year +"-04-01' and '"+ year +"-06-30') and ed2.TYPE_NAME = ed.TYPE_NAME) as quarter2," + 
					"(select count(*) from ecm_audit_general eag2 left join ecm_document ed2 on eag2.DOC_ID = ed2.ID where (eag2.EXCUTE_DATE between '"+ year +"-07-01' and '"+ year +"-09-30') and ed2.TYPE_NAME = ed.TYPE_NAME) as quarter3," + 
					"(select count(*) from ecm_audit_general eag2 left join ecm_document ed2 on eag2.DOC_ID = ed2.ID where (eag2.EXCUTE_DATE between '"+ year +"-10-01' and '"+ year +"-12-31') and ed2.TYPE_NAME = ed.TYPE_NAME) as quarter4 " + 
					"from ecm_audit_general eag left join ecm_document ed on eag.DOC_ID = ed.ID " + 
					"where ed.C_ARC_CLASSIC = '工程建设' and ed.TYPE_NAME <> '工程管理案卷' and ed.TYPE_NAME <> '工程管理文件' ";
			
			List<Map<String, Object>> listWorkStatistic = documentService.getMapList(getToken(), sqlQuarter);
			List<Map<String, Object>> listEngineStatistic = documentService.getMapList(getToken(), sqlEngine);
			List<Map<String, Object>> listProcessStatistic = documentService.getMapList(getToken(), sqlProcess);
			
			for(int i=0; i<listWorkStatistic.size(); i++) {
				projMap = new HashMap<String, Object>();
				String fileType = (listWorkStatistic.get(i).get("TYPE_NAME")!=null)?(String)listWorkStatistic.get(i).get("TYPE_NAME"):"";
				projMap.put("fileType", fileType);
				Number quarterOne = getSponsorFor(listWorkStatistic, "quarter1", i);
				projMap.put("quarterOne", quarterOne);
				Number quarterTwo = getSponsorFor(listWorkStatistic, "quarter2", i);
				projMap.put("quarterTwo", quarterTwo);
				Number quarterThree = getSponsorFor(listWorkStatistic, "quarter3", i);
				projMap.put("quarterThree", quarterThree);
				Number quarterFour = getSponsorFor(listWorkStatistic, "quarter4", i);
				projMap.put("quarterFour", quarterFour);
				
				outList.add(projMap);
			}
			
			projMap = new HashMap<String, Object>();
			projMap.put("fileType", "工程过程文件");
			projMap.put("quarterOne", getSponsorT(listProcessStatistic, "quarter1"));
			projMap.put("quarterTwo", getSponsorT(listProcessStatistic, "quarter2"));
			projMap.put("quarterThree", getSponsorT(listProcessStatistic, "quarter3"));
			projMap.put("quarterFour", getSponsorT(listProcessStatistic, "quarter4"));
			outList.add(projMap);
			
			projMap = new HashMap<String, Object>();
			projMap.put("fileType", "工程项目管理文件");
			projMap.put("quarterOne", getSponsorT(listEngineStatistic, "quarter1"));
			projMap.put("quarterTwo", getSponsorT(listEngineStatistic, "quarter2"));
			projMap.put("quarterThree", getSponsorT(listEngineStatistic, "quarter3"));
			projMap.put("quarterFour", getSponsorT(listEngineStatistic, "quarter4"));
			outList.add(projMap);
			
			mp.put("data", outList);
			mp.put("code", ActionContext.SUCESS);
		}catch(Exception ex){
			ex.printStackTrace();
			mp.put("code", ActionContext.FAILURE);
		}
		return mp;
	}
	
	//档案接收
	@RequestMapping(value = "fileReceiveStatistic", method = RequestMethod.POST)
	@ResponseBody	
	public Map<String, Object> fileReceiveStatistic(@RequestBody String argStr){
		Map<String, Object> mp = new HashMap<String, Object>();
		List<Map<String, Object> > outList = new ArrayList<Map<String, Object>>();
		Map<String, Object> args = JSONUtils.stringToMap(argStr);
		
		String yearSelect = this.getStrValue(args, "yearSelect");
		String year = String.valueOf(Integer.parseInt(yearSelect.substring(0, 4)) + 1);
		String quarter = this.getStrValue(args, "quarterSelect");
		
		List<String> dateCondList = this.setQuarterCondition(year, quarter);
		
		String conditionDate1 = dateCondList.get(0);
		String conditionDate2 = dateCondList.get(1);
		String conditionDate3 = dateCondList.get(2);
		String conditionDate = dateCondList.get(3);
		
		try {
			Map<String, Object> projMap = new HashMap<String, Object>();
			
			String sqlDrawingSuit = "select distinct b.TYPE_NAME, b.SUB_TYPE, " + 
					"(select count(*) as cesetMonth1 from ecm_audit_general eag left join ecm_document ed on eag.DOC_ID = ed.ID where ed.TYPE_NAME = '设计文件' and ed.SUB_TYPE = '图纸' and ed.C_ITEM_TYPE = '案卷'"+ conditionDate1 +") as month1," + 
					"(select count(*) as cesetMonth2 from ecm_audit_general eag left join ecm_document ed on eag.DOC_ID = ed.ID where ed.TYPE_NAME = '设计文件' and ed.SUB_TYPE = '图纸' and ed.C_ITEM_TYPE = '案卷'"+ conditionDate2 +") as month2," + 
					"(select count(*) as cesetMonth3 from ecm_audit_general eag left join ecm_document ed on eag.DOC_ID = ed.ID where ed.TYPE_NAME = '设计文件' and ed.SUB_TYPE = '图纸' and ed.C_ITEM_TYPE = '案卷'"+ conditionDate3 +") as month3," + 
					"(select count(*) as cesetMonth from ecm_audit_general eag left join ecm_document ed on eag.DOC_ID = ed.ID where ed.TYPE_NAME = '设计文件' and ed.SUB_TYPE = '图纸' and ed.C_ITEM_TYPE = '案卷'"+ conditionDate +") as quarterCount  " + 
					"from ecm_audit_general a left join ecm_document b on a.DOC_ID = b.ID " + 
					"where b.TYPE_NAME = '设计文件' and b.SUB_TYPE = '图纸'";
			String sqlDrawingSheet = "select distinct b.TYPE_NAME, b.SUB_TYPE, " + 
					"(select sum(ed.C_PAGE_COUNT) as cesetMonth1 from ecm_audit_general eag left join ecm_document ed on eag.DOC_ID = ed.ID where ed.TYPE_NAME = '设计文件' and ed.SUB_TYPE = '图纸'"+ conditionDate1 +") as month1," + 
					"(select sum(ed.C_PAGE_COUNT) as cesetMonth2 from ecm_audit_general eag left join ecm_document ed on eag.DOC_ID = ed.ID where ed.TYPE_NAME = '设计文件' and ed.SUB_TYPE = '图纸'"+ conditionDate2 +") as month2," + 
					"(select sum(ed.C_PAGE_COUNT) as cesetMonth3 from ecm_audit_general eag left join ecm_document ed on eag.DOC_ID = ed.ID where ed.TYPE_NAME = '设计文件' and ed.SUB_TYPE = '图纸'"+ conditionDate3 +") as month3," + 
					"(select sum(ed.C_PAGE_COUNT) as cesetMonth from ecm_audit_general eag left join ecm_document ed on eag.DOC_ID = ed.ID where ed.TYPE_NAME = '设计文件' and ed.SUB_TYPE = '图纸'"+ conditionDate +") as quarterCount  " + 
					"from ecm_audit_general a left join ecm_document b on a.DOC_ID = b.ID " + 
					"where b.TYPE_NAME = '设计文件' and b.SUB_TYPE = '图纸'";
			String sqlFileSuit = "select distinct b.TYPE_NAME, b.SUB_TYPE, " + 
					"(select count(*) as cesetMonth1 from ecm_audit_general eag left join ecm_document ed on eag.DOC_ID = ed.ID where ed.TYPE_NAME = '设计文件' and ed.SUB_TYPE = '文件' and ed.C_ITEM_TYPE = '案卷'"+ conditionDate1 +") as month1," + 
					"(select count(*) as cesetMonth2 from ecm_audit_general eag left join ecm_document ed on eag.DOC_ID = ed.ID where ed.TYPE_NAME = '设计文件' and ed.SUB_TYPE = '文件' and ed.C_ITEM_TYPE = '案卷'"+ conditionDate2 +") as month2," + 
					"(select count(*) as cesetMonth3 from ecm_audit_general eag left join ecm_document ed on eag.DOC_ID = ed.ID where ed.TYPE_NAME = '设计文件' and ed.SUB_TYPE = '文件' and ed.C_ITEM_TYPE = '案卷'"+ conditionDate3 +") as month3," + 
					"(select count(*) as cesetMonth from ecm_audit_general eag left join ecm_document ed on eag.DOC_ID = ed.ID where ed.TYPE_NAME = '设计文件' and ed.SUB_TYPE = '文件' and ed.C_ITEM_TYPE = '案卷'"+ conditionDate +") as quarterCount  " + 
					"from ecm_audit_general a left join ecm_document b on a.DOC_ID = b.ID " + 
					"where b.TYPE_NAME = '设计文件' and b.SUB_TYPE = '文件'";
			String sqlFileVolume = "select distinct b.TYPE_NAME, b.SUB_TYPE, " + 
					"(select sum(ed.C_VOLUME_COUNT) as cesetMonth1 from ecm_audit_general eag left join ecm_document ed on eag.DOC_ID = ed.ID where ed.TYPE_NAME = '设计文件' and ed.SUB_TYPE = '文件'"+ conditionDate1 +") as month1," + 
					"(select sum(ed.C_VOLUME_COUNT) as cesetMonth2 from ecm_audit_general eag left join ecm_document ed on eag.DOC_ID = ed.ID where ed.TYPE_NAME = '设计文件' and ed.SUB_TYPE = '文件'"+ conditionDate2 +") as month2," + 
					"(select sum(ed.C_VOLUME_COUNT) as cesetMonth3 from ecm_audit_general eag left join ecm_document ed on eag.DOC_ID = ed.ID where ed.TYPE_NAME = '设计文件' and ed.SUB_TYPE = '文件'"+ conditionDate3 +") as month3," + 
					"(select sum(ed.C_VOLUME_COUNT) as cesetMonth from ecm_audit_general eag left join ecm_document ed on eag.DOC_ID = ed.ID where ed.TYPE_NAME = '设计文件' and ed.SUB_TYPE = '文件'"+ conditionDate +") as quarterCount  " + 
					"from ecm_audit_general a left join ecm_document b on a.DOC_ID = b.ID " + 
					"where b.TYPE_NAME = '设计文件' and b.SUB_TYPE = '文件'";
			String sqlElectronicItem = "select distinct b.TYPE_NAME, b.SUB_TYPE, " + 
					"(select count(*) as cesetMonth1 from ecm_audit_general eag left join ecm_document ed on eag.DOC_ID = ed.ID where ed.TYPE_NAME = '设计文件' and b.CONTENT_SIZE > 0"+ conditionDate1 +") as month1," + 
					"(select count(*) as cesetMonth2 from ecm_audit_general eag left join ecm_document ed on eag.DOC_ID = ed.ID where ed.TYPE_NAME = '设计文件' and b.CONTENT_SIZE > 0"+ conditionDate2 +") as month2," + 
					"(select count(*) as cesetMonth3 from ecm_audit_general eag left join ecm_document ed on eag.DOC_ID = ed.ID where ed.TYPE_NAME = '设计文件' and b.CONTENT_SIZE > 0"+ conditionDate3 +") as month3," + 
					"(select count(*) as cesetMonth from ecm_audit_general eag left join ecm_document ed on eag.DOC_ID = ed.ID where ed.TYPE_NAME = '设计文件' and b.CONTENT_SIZE > 0"+ conditionDate +") as quarterCount  " + 
					"from ecm_audit_general a left join ecm_document b on a.DOC_ID = b.ID " + 
					"where b.TYPE_NAME = '设计文件' and b.CONTENT_SIZE > 0";
			String sqlElectronicGM = "select distinct b.TYPE_NAME, b.SUB_TYPE, " + 
					"(select sum(ed.CONTENT_SIZE) as cesetMonth1 from ecm_audit_general eag left join ecm_document ed on eag.DOC_ID = ed.ID where ed.TYPE_NAME = '设计文件' and b.CONTENT_SIZE > 0"+ conditionDate1 +") as month1," + 
					"(select sum(ed.CONTENT_SIZE) as cesetMonth2 from ecm_audit_general eag left join ecm_document ed on eag.DOC_ID = ed.ID where ed.TYPE_NAME = '设计文件' and b.CONTENT_SIZE > 0"+ conditionDate2 +") as month2," + 
					"(select sum(ed.CONTENT_SIZE) as cesetMonth3 from ecm_audit_general eag left join ecm_document ed on eag.DOC_ID = ed.ID where ed.TYPE_NAME = '设计文件' and b.CONTENT_SIZE > 0"+ conditionDate3 +") as month3," + 
					"(select sum(ed.CONTENT_SIZE) as cesetMonth from ecm_audit_general eag left join ecm_document ed on eag.DOC_ID = ed.ID where ed.TYPE_NAME = '设计文件' and b.CONTENT_SIZE > 0"+ conditionDate +") as quarterCount  " + 
					"from ecm_audit_general a left join ecm_document b on a.DOC_ID = b.ID " + 
					"where b.TYPE_NAME = '设计文件' and b.CONTENT_SIZE > 0";
			
			List<Map<String, Object>> listDrawingSuit = documentService.getMapList(getToken(), sqlDrawingSuit);
			List<Map<String, Object>> listDrawingSheet = documentService.getMapList(getToken(), sqlDrawingSheet);
			List<Map<String, Object>> listFileSuit = documentService.getMapList(getToken(), sqlFileSuit);
			List<Map<String, Object>> listFileVolume = documentService.getMapList(getToken(), sqlFileVolume);
			List<Map<String, Object>> listElectronicItem = documentService.getMapList(getToken(), sqlElectronicItem);
			List<Map<String, Object>> listElectronicGM = documentService.getMapList(getToken(), sqlElectronicGM);
			
			projMap.put("fileType", "设计文件");
			projMap.put("typeClass", "实体-图纸");
			projMap.put("unitType", "套");
			Number drawCountSuitMonth1 = getSponsorT(listDrawingSuit, "month1");
			projMap.put("drawCountSuitMonth1", drawCountSuitMonth1);
			Number drawCountSuitMonth2 = getSponsorT(listDrawingSuit, "month2");
			projMap.put("drawCountSuitMonth2", drawCountSuitMonth2);
			Number drawCountSuitMonth3 = getSponsorT(listDrawingSuit, "month3");
			projMap.put("drawCountSuitMonth3", drawCountSuitMonth3);
			Number drawCountSuitQuarter = getSponsorT(listDrawingSuit, "quarterCount");
			projMap.put("drawCountSuitQuarter", drawCountSuitQuarter);
			
			outList.add(projMap);
			
			projMap = new HashMap<String, Object>(); 
			projMap.put("fileType", "设计文件");
			projMap.put("typeClass", "实体-图纸");
			projMap.put("unitType", "张");
			Number drawCountSheetMonth1 = getSponsorT(listDrawingSheet, "month1");
			projMap.put("drawCountSuitMonth1", drawCountSheetMonth1);
			Number drawCountSheetMonth2 = getSponsorT(listDrawingSheet, "month2");
			projMap.put("drawCountSuitMonth2", drawCountSheetMonth2);
			Number drawCountSheetMonth3 = getSponsorT(listDrawingSheet, "month3");
			projMap.put("drawCountSuitMonth3", drawCountSheetMonth3);
			Number drawCountSheetQuarter = getSponsorT(listDrawingSheet, "quarterCount");
			projMap.put("drawCountSuitQuarter", drawCountSheetQuarter);
			
			outList.add(projMap);
			
			projMap = new HashMap<String, Object>(); 
			projMap.put("fileType", "设计文件");
			projMap.put("typeClass", "实体-文件");
			projMap.put("unitType", "套");
			Number fileCountSuitMonth1 = getSponsorT(listFileSuit, "month1");
			projMap.put("drawCountSuitMonth1", fileCountSuitMonth1);
			Number fileCountSuitMonth2 = getSponsorT(listFileSuit, "month2");
			projMap.put("drawCountSuitMonth2", fileCountSuitMonth2);
			Number fileCountSuitMonth3 = getSponsorT(listFileSuit, "month3");
			projMap.put("drawCountSuitMonth3", fileCountSuitMonth3);
			Number fileCountSuitQuarter = getSponsorT(listFileSuit, "quarterCount");
			projMap.put("drawCountSuitQuarter", fileCountSuitQuarter);
			
			outList.add(projMap);
			
			projMap = new HashMap<String, Object>(); 
			projMap.put("fileType", "设计文件");
			projMap.put("typeClass", "实体-文件");
			projMap.put("unitType", "册");
			Number fileCountVolumeMonth1 = getSponsorT(listFileVolume, "month1");
			projMap.put("drawCountSuitMonth1", fileCountVolumeMonth1);
			Number fileCountVolumeMonth2 = getSponsorT(listFileVolume, "month2");
			projMap.put("drawCountSuitMonth2", fileCountVolumeMonth2);
			Number fileCountVolumeMonth3 = getSponsorT(listFileVolume, "month3");
			projMap.put("drawCountSuitMonth3", fileCountVolumeMonth3);
			Number fileCountVolumeQuarter = getSponsorT(listFileVolume, "quarterCount");
			projMap.put("drawCountSuitQuarter", fileCountVolumeQuarter);
			
			outList.add(projMap);
			
			projMap = new HashMap<String, Object>(); 
			projMap.put("fileType", "设计文件");
			projMap.put("typeClass", "电子");
			projMap.put("unitType", "件");
			Number electronicCountItemMonth1 = getSponsorT(listElectronicItem, "month1");
			projMap.put("drawCountSuitMonth1", electronicCountItemMonth1);
			Number electronicCountItemMonth2 = getSponsorT(listElectronicItem, "month2");
			projMap.put("drawCountSuitMonth2", electronicCountItemMonth2);
			Number electronicCountItemMonth3 = getSponsorT(listElectronicItem, "month3");
			projMap.put("drawCountSuitMonth3", electronicCountItemMonth3);
			Number electronicCountItemQuarter = getSponsorT(listElectronicItem, "quarterCount");
			projMap.put("drawCountSuitQuarter", electronicCountItemQuarter);
			
			outList.add(projMap);
			
			projMap = new HashMap<String, Object>(); 
			projMap.put("fileType", "设计文件");
			projMap.put("typeClass", "电子");
			projMap.put("unitType", "GB/MB");
			Number electronicCountGMMonth1 = getSponsorT(listElectronicGM, "month1");
			double electronicGMMonth1 = electronicCountGMMonth1.doubleValue()/1048576.00;
			projMap.put("drawCountSuitMonth1", electronicGMMonth1);
			Number electronicCountGMMonth2 = getSponsorT(listElectronicGM, "month2");
			double electronicGMMonth2 = electronicCountGMMonth2.doubleValue()/1048576.00;
			projMap.put("drawCountSuitMonth2", electronicGMMonth2);
			Number electronicCountGMMonth3 = getSponsorT(listElectronicGM, "month3");
			double electronicGMMonth3 = electronicCountGMMonth3.doubleValue()/1048576.00;
			projMap.put("drawCountSuitMonth3", electronicGMMonth3);
			Number electronicCountGMQuarter = getSponsorT(listElectronicGM, "quarterCount");
			double electronicGMQuarter = electronicCountGMQuarter.doubleValue()/1048576.00;
			projMap.put("drawCountSuitQuarter", electronicGMQuarter);
			
			outList.add(projMap);
			
			List<Map<String, Object> > typeMapList = new ArrayList<Map<String, Object>>();
			Map<String, Object> typeMap = new HashMap<String, Object>();
			typeMap.put("type", "'奖状等'");
			typeMap.put("name", "获奖项目");
			typeMapList.add(typeMap);
			typeMap = new HashMap<String, Object>();
			typeMap.put("type", "'科研文件','科研案卷'");
			typeMap.put("name", "科研文件");
			typeMapList.add(typeMap);
			typeMap = new HashMap<String, Object>();
			typeMap.put("type", "'系统竣工文件案卷','系统竣工文件'");
			typeMap.put("name", "竣工文件");
			typeMapList.add(typeMap);
			
			for(int i=0; i<typeMapList.size(); i++) {
				String sqlCriminalSuit = "select distinct b.C_ARC_CLASSIC, " + 
						"(select count(*) as cesetMonth1 from ecm_audit_general eag left join ecm_document ed on eag.DOC_ID = ed.ID where ed.TYPE_NAME IN ("+ typeMapList.get(i).get("type") +") and b.C_INCLUDE_PAPER = '是' and ed.C_ITEM_TYPE = '案卷'"+ conditionDate1 +") as month1," + 
						"(select count(*) as cesetMonth2 from ecm_audit_general eag left join ecm_document ed on eag.DOC_ID = ed.ID where ed.TYPE_NAME IN ("+ typeMapList.get(i).get("type") +") and b.C_INCLUDE_PAPER = '是' and ed.C_ITEM_TYPE = '案卷'"+ conditionDate2 +") as month2," + 
						"(select count(*) as cesetMonth3 from ecm_audit_general eag left join ecm_document ed on eag.DOC_ID = ed.ID where ed.TYPE_NAME IN ("+ typeMapList.get(i).get("type") +") and b.C_INCLUDE_PAPER = '是' and ed.C_ITEM_TYPE = '案卷'"+ conditionDate3 +") as month3," + 
						"(select count(*) as cesetMonth from ecm_audit_general eag left join ecm_document ed on eag.DOC_ID = ed.ID where ed.TYPE_NAME IN ("+ typeMapList.get(i).get("type") +") and b.C_INCLUDE_PAPER = '是' and ed.C_ITEM_TYPE = '案卷'"+ conditionDate +") as quarterCount " + 
						"from ecm_audit_general a left join ecm_document b on a.DOC_ID = b.ID " + 
						"where b.TYPE_NAME IN ("+ typeMapList.get(i).get("type") +") and b.C_INCLUDE_PAPER = '是'";
				String sqlCriminalVolume = "select distinct b.C_ARC_CLASSIC, " + 
						"(select sum(ed.C_VOLUME_COUNT) as cesetMonth1 from ecm_audit_general eag left join ecm_document ed on eag.DOC_ID = ed.ID where ed.TYPE_NAME IN ("+ typeMapList.get(i).get("type") +") and b.C_INCLUDE_PAPER = '是'"+ conditionDate1 +") as month1," + 
						"(select sum(ed.C_VOLUME_COUNT) as cesetMonth2 from ecm_audit_general eag left join ecm_document ed on eag.DOC_ID = ed.ID where ed.TYPE_NAME IN ("+ typeMapList.get(i).get("type") +") and b.C_INCLUDE_PAPER = '是'"+ conditionDate2 +") as month2," + 
						"(select sum(ed.C_VOLUME_COUNT) as cesetMonth3 from ecm_audit_general eag left join ecm_document ed on eag.DOC_ID = ed.ID where ed.TYPE_NAME IN ("+ typeMapList.get(i).get("type") +") and b.C_INCLUDE_PAPER = '是'"+ conditionDate3 +") as month3," + 
						"(select sum(ed.C_VOLUME_COUNT) as cesetMonth from ecm_audit_general eag left join ecm_document ed on eag.DOC_ID = ed.ID where ed.TYPE_NAME IN ("+ typeMapList.get(i).get("type") +") and b.C_INCLUDE_PAPER = '是'"+ conditionDate +") as quarterCount  " + 
						"from ecm_audit_general a left join ecm_document b on a.DOC_ID = b.ID " + 
						"where b.TYPE_NAME IN ("+ typeMapList.get(i).get("type") +") and b.C_INCLUDE_PAPER = '是'";
				String sqlCriminalItem = "select distinct b.C_ARC_CLASSIC, " + 
						"(select count(*) as cesetMonth1 from ecm_audit_general eag left join ecm_document ed on eag.DOC_ID = ed.ID where ed.TYPE_NAME IN ("+ typeMapList.get(i).get("type") +") and b.CONTENT_SIZE > 0"+ conditionDate1 +") as month1," + 
						"(select count(*) as cesetMonth2 from ecm_audit_general eag left join ecm_document ed on eag.DOC_ID = ed.ID where ed.TYPE_NAME IN ("+ typeMapList.get(i).get("type") +") and b.CONTENT_SIZE > 0"+ conditionDate2 +") as month2," + 
						"(select count(*) as cesetMonth3 from ecm_audit_general eag left join ecm_document ed on eag.DOC_ID = ed.ID where ed.TYPE_NAME IN ("+ typeMapList.get(i).get("type") +") and b.CONTENT_SIZE > 0"+ conditionDate3 +") as month3," + 
						"(select count(*) as cesetMonth from ecm_audit_general eag left join ecm_document ed on eag.DOC_ID = ed.ID where ed.TYPE_NAME IN ("+ typeMapList.get(i).get("type") +") and b.CONTENT_SIZE > 0"+ conditionDate +") as quarterCount  " + 
						"from ecm_audit_general a left join ecm_document b on a.DOC_ID = b.ID " + 
						"where b.TYPE_NAME IN ("+ typeMapList.get(i).get("type") +") and b.CONTENT_SIZE > 0";
				String sqlCriminalGM = "select distinct b.C_ARC_CLASSIC, " + 
						"(select sum(ed.CONTENT_SIZE) as cesetMonth1 from ecm_audit_general eag left join ecm_document ed on eag.DOC_ID = ed.ID where ed.TYPE_NAME IN ("+ typeMapList.get(i).get("type") +") and b.CONTENT_SIZE > 0"+ conditionDate1 +") as month1," + 
						"(select sum(ed.CONTENT_SIZE) as cesetMonth2 from ecm_audit_general eag left join ecm_document ed on eag.DOC_ID = ed.ID where ed.TYPE_NAME IN ("+ typeMapList.get(i).get("type") +") and b.CONTENT_SIZE > 0"+ conditionDate2 +") as month2," + 
						"(select sum(ed.CONTENT_SIZE) as cesetMonth3 from ecm_audit_general eag left join ecm_document ed on eag.DOC_ID = ed.ID where ed.TYPE_NAME IN ("+ typeMapList.get(i).get("type") +") and b.CONTENT_SIZE > 0"+ conditionDate3 +") as month3," + 
						"(select sum(ed.CONTENT_SIZE) as cesetMonth from ecm_audit_general eag left join ecm_document ed on eag.DOC_ID = ed.ID where ed.TYPE_NAME IN ("+ typeMapList.get(i).get("type") +") and b.CONTENT_SIZE > 0"+ conditionDate +") as quarterCount  " + 
						"from ecm_audit_general a left join ecm_document b on a.DOC_ID = b.ID " + 
						"where b.TYPE_NAME IN ("+ typeMapList.get(i).get("type") +") and b.CONTENT_SIZE > 0";
				
				List<Map<String, Object>> listCriminalSuit = documentService.getMapList(getToken(), sqlCriminalSuit);
				List<Map<String, Object>> listCriminalVolume = documentService.getMapList(getToken(), sqlCriminalVolume);
				List<Map<String, Object>> listCriminalItem = documentService.getMapList(getToken(), sqlCriminalItem);
				List<Map<String, Object>> listCriminalGM = documentService.getMapList(getToken(), sqlCriminalGM);
				
				projMap = new HashMap<String, Object>(); 
				projMap.put("fileType", typeMapList.get(i).get("name"));
				projMap.put("typeClass", "实体");
				projMap.put("unitType", "套");
				Number criminalCountSuitMonth1 = getSponsorT(listCriminalSuit, "month1");
				projMap.put("drawCountSuitMonth1", criminalCountSuitMonth1);
				Number criminalCountSuitMonth2 = getSponsorT(listCriminalSuit, "month2");
				projMap.put("drawCountSuitMonth2", criminalCountSuitMonth2);
				Number criminalCountSuitMonth3 = getSponsorT(listCriminalSuit, "month3");
				projMap.put("drawCountSuitMonth3", criminalCountSuitMonth3);
				Number criminalCountSuitQuarter = getSponsorT(listCriminalSuit, "quarterCount");
				projMap.put("drawCountSuitQuarter", criminalCountSuitQuarter);
				
				outList.add(projMap);
				
				projMap = new HashMap<String, Object>(); 
				projMap.put("fileType", typeMapList.get(i).get("name"));
				projMap.put("typeClass", "实体");
				projMap.put("unitType", "册");
				Number criminalCountVolumeMonth1 = getSponsorT(listCriminalVolume, "month1");
				projMap.put("drawCountSuitMonth1", criminalCountVolumeMonth1);
				Number criminalCountVolumeMonth2 = getSponsorT(listCriminalVolume, "month2");
				projMap.put("drawCountSuitMonth2", criminalCountVolumeMonth2);
				Number criminalCountVolumeMonth3 = getSponsorT(listCriminalVolume, "month3");
				projMap.put("drawCountSuitMonth3", criminalCountVolumeMonth3);
				Number criminalCountVolumeQuarter = getSponsorT(listCriminalVolume, "quarterCount");
				projMap.put("drawCountSuitQuarter", criminalCountVolumeQuarter);
				
				outList.add(projMap);
				
				projMap = new HashMap<String, Object>(); 
				projMap.put("fileType", typeMapList.get(i).get("name"));
				projMap.put("typeClass", "电子");
				projMap.put("unitType", "件");
				Number criminalCountItemMonth1 = getSponsorT(listCriminalItem, "month1");
				projMap.put("drawCountSuitMonth1", criminalCountItemMonth1);
				Number criminalCountItemMonth2 = getSponsorT(listCriminalItem, "month2");
				projMap.put("drawCountSuitMonth2", criminalCountItemMonth2);
				Number criminalCountItemMonth3 = getSponsorT(listCriminalItem, "month3");
				projMap.put("drawCountSuitMonth3", criminalCountItemMonth3);
				Number criminalCountItemQuarter = getSponsorT(listCriminalItem, "quarterCount");
				projMap.put("drawCountSuitQuarter", criminalCountItemQuarter);
				
				outList.add(projMap);
				
				projMap = new HashMap<String, Object>(); 
				projMap.put("fileType", typeMapList.get(i).get("name"));
				projMap.put("typeClass", "电子");
				projMap.put("unitType", "GB/MB");
				Number criminalCountGMMonth1 = getSponsorT(listCriminalGM, "month1");
				double criminalGMMonth1 = criminalCountGMMonth1.doubleValue()/1048576.00;
				projMap.put("drawCountSuitMonth1", criminalGMMonth1);
				Number criminalCountGMMonth2 = getSponsorT(listCriminalGM, "month2");
				double criminalGMMonth2 = criminalCountGMMonth2.doubleValue()/1048576.00;
				projMap.put("drawCountSuitMonth2", criminalGMMonth2);
				Number criminalCountGMMonth3 = getSponsorT(listCriminalGM, "month3");
				double criminalGMMonth3 = criminalCountGMMonth3.doubleValue()/1048576.00;
				projMap.put("drawCountSuitMonth3", criminalGMMonth3);
				Number criminalCountGMQuarter = getSponsorT(listCriminalGM, "quarterCount");
				double criminalGMQuarter = criminalCountGMQuarter.doubleValue()/1048576.00;
				projMap.put("drawCountSuitQuarter", criminalGMQuarter);
				
				outList.add(projMap);
			}
			
			String sqlChangeSuit = "select distinct b.TYPE_NAME, " + 
					"(select count(*) as cesetMonth1 from ecm_audit_general eag left join ecm_document ed on eag.DOC_ID = ed.ID where ed.TYPE_NAME = '设计变更' and b.C_INCLUDE_PAPER = '是' and ed.C_ITEM_TYPE = '案卷'"+ conditionDate1 +") as month1," + 
					"(select count(*) as cesetMonth2 from ecm_audit_general eag left join ecm_document ed on eag.DOC_ID = ed.ID where ed.TYPE_NAME = '设计变更' and b.C_INCLUDE_PAPER = '是' and ed.C_ITEM_TYPE = '案卷'"+ conditionDate2 +") as month2," + 
					"(select count(*) as cesetMonth3 from ecm_audit_general eag left join ecm_document ed on eag.DOC_ID = ed.ID where ed.TYPE_NAME = '设计变更' and b.C_INCLUDE_PAPER = '是' and ed.C_ITEM_TYPE = '案卷'"+ conditionDate3 +") as month3," + 
					"(select count(*) as cesetMonth from ecm_audit_general eag left join ecm_document ed on eag.DOC_ID = ed.ID where ed.TYPE_NAME = '设计变更' and b.C_INCLUDE_PAPER = '是' and ed.C_ITEM_TYPE = '案卷'"+ conditionDate +") as quarterCount " + 
					"from ecm_audit_general a left join ecm_document b on a.DOC_ID = b.ID " + 
					"where b.TYPE_NAME = '设计变更' and b.C_INCLUDE_PAPER = '是'";
			String sqlChangeVolume = "select distinct b.TYPE_NAME, " + 
					"(select count(*) as cesetMonth1 from ecm_audit_general eag left join ecm_document ed on eag.DOC_ID = ed.ID where ed.TYPE_NAME = '设计变更' and b.C_INCLUDE_PAPER = '是'"+ conditionDate1 +") as month1," + 
					"(select count(*) as cesetMonth2 from ecm_audit_general eag left join ecm_document ed on eag.DOC_ID = ed.ID where ed.TYPE_NAME = '设计变更' and b.C_INCLUDE_PAPER = '是'"+ conditionDate2 +") as month2," + 
					"(select count(*) as cesetMonth3 from ecm_audit_general eag left join ecm_document ed on eag.DOC_ID = ed.ID where ed.TYPE_NAME = '设计变更' and b.C_INCLUDE_PAPER = '是'"+ conditionDate3 +") as month3," + 
					"(select count(*) as cesetMonth from ecm_audit_general eag left join ecm_document ed on eag.DOC_ID = ed.ID where ed.TYPE_NAME = '设计变更' and b.C_INCLUDE_PAPER = '是'"+ conditionDate +") as quarterCount  " + 
					"from ecm_audit_general a left join ecm_document b on a.DOC_ID = b.ID " + 
					"where b.TYPE_NAME = '设计变更' and b.C_INCLUDE_PAPER = '是'";
			String sqlChangeItem = "select distinct b.TYPE_NAME, " + 
					"(select count(*) as cesetMonth1 from ecm_audit_general eag left join ecm_document ed on eag.DOC_ID = ed.ID where ed.TYPE_NAME = '设计变更' and b.CONTENT_SIZE > 0"+ conditionDate1 +") as month1," + 
					"(select count(*) as cesetMonth2 from ecm_audit_general eag left join ecm_document ed on eag.DOC_ID = ed.ID where ed.TYPE_NAME = '设计变更' and b.CONTENT_SIZE > 0"+ conditionDate2 +") as month2," + 
					"(select count(*) as cesetMonth3 from ecm_audit_general eag left join ecm_document ed on eag.DOC_ID = ed.ID where ed.TYPE_NAME = '设计变更' and b.CONTENT_SIZE > 0"+ conditionDate3 +") as month3," + 
					"(select count(*) as cesetMonth from ecm_audit_general eag left join ecm_document ed on eag.DOC_ID = ed.ID where ed.TYPE_NAME = '设计变更' and b.CONTENT_SIZE > 0"+ conditionDate +") as quarterCount  " + 
					"from ecm_audit_general a left join ecm_document b on a.DOC_ID = b.ID " + 
					"where b.TYPE_NAME = '设计变更' and b.CONTENT_SIZE > 0";
			String sqlChangeGM = "select distinct b.TYPE_NAME, " + 
					"(select sum(ed.CONTENT_SIZE) as cesetMonth1 from ecm_audit_general eag left join ecm_document ed on eag.DOC_ID = ed.ID where ed.TYPE_NAME = '设计变更' and b.CONTENT_SIZE > 0"+ conditionDate1 +") as month1," + 
					"(select sum(ed.CONTENT_SIZE) as cesetMonth2 from ecm_audit_general eag left join ecm_document ed on eag.DOC_ID = ed.ID where ed.TYPE_NAME = '设计变更' and b.CONTENT_SIZE > 0"+ conditionDate2 +") as month2," + 
					"(select sum(ed.CONTENT_SIZE) as cesetMonth3 from ecm_audit_general eag left join ecm_document ed on eag.DOC_ID = ed.ID where ed.TYPE_NAME = '设计变更' and b.CONTENT_SIZE > 0"+ conditionDate3 +") as month3," + 
					"(select sum(ed.CONTENT_SIZE) as cesetMonth from ecm_audit_general eag left join ecm_document ed on eag.DOC_ID = ed.ID where ed.TYPE_NAME = '设计变更' and b.CONTENT_SIZE > 0"+ conditionDate +") as quarterCount  " + 
					"from ecm_audit_general a left join ecm_document b on a.DOC_ID = b.ID " + 
					"where b.TYPE_NAME = '设计变更' and b.CONTENT_SIZE > 0";
			
			List<Map<String, Object>> listChangeSuit = documentService.getMapList(getToken(), sqlChangeSuit);
			List<Map<String, Object>> listChangeVolume = documentService.getMapList(getToken(), sqlChangeVolume);
			List<Map<String, Object>> listChangeItem = documentService.getMapList(getToken(), sqlChangeItem);
			List<Map<String, Object>> listChangeGM = documentService.getMapList(getToken(), sqlChangeGM);
			
			projMap = new HashMap<String, Object>(); 
			projMap.put("fileType", "工程变更文件");
			projMap.put("typeClass", "实体");
			projMap.put("unitType", "卷");
			Number changeCountSuitMonth1 = getSponsorT(listChangeSuit, "month1");
			projMap.put("drawCountSuitMonth1", changeCountSuitMonth1);
			Number changeCountSuitMonth2 = getSponsorT(listChangeSuit, "month2");
			projMap.put("drawCountSuitMonth2", changeCountSuitMonth2);
			Number changeCountSuitMonth3 = getSponsorT(listChangeSuit, "month3");
			projMap.put("drawCountSuitMonth3", changeCountSuitMonth3);
			Number changeCountSuitQuarter = getSponsorT(listChangeSuit, "quarterCount");
			projMap.put("drawCountSuitQuarter", changeCountSuitQuarter);
			
			outList.add(projMap);
			
			projMap = new HashMap<String, Object>(); 
			projMap.put("fileType", "工程变更文件");
			projMap.put("typeClass", "实体");
			projMap.put("unitType", "件");
			Number changeCountVolumeMonth1 = getSponsorT(listChangeVolume, "month1");
			projMap.put("drawCountSuitMonth1", changeCountVolumeMonth1);
			Number changeCountVolumeMonth2 = getSponsorT(listChangeVolume, "month2");
			projMap.put("drawCountSuitMonth2", changeCountVolumeMonth2);
			Number changeCountVolumeMonth3 = getSponsorT(listChangeVolume, "month3");
			projMap.put("drawCountSuitMonth3", changeCountVolumeMonth3);
			Number changeCountVolumeQuarter = getSponsorT(listChangeVolume, "quarterCount");
			projMap.put("drawCountSuitQuarter", changeCountVolumeQuarter);
			
			outList.add(projMap);
			
			projMap = new HashMap<String, Object>(); 
			projMap.put("fileType", "工程变更文件");
			projMap.put("typeClass", "电子");
			projMap.put("unitType", "件");
			Number changeCountItemMonth1 = getSponsorT(listChangeItem, "month1");
			projMap.put("drawCountSuitMonth1", changeCountItemMonth1);
			Number changeCountItemMonth2 = getSponsorT(listChangeItem, "month2");
			projMap.put("drawCountSuitMonth2", changeCountItemMonth2);
			Number changeCountItemMonth3 = getSponsorT(listChangeItem, "month3");
			projMap.put("drawCountSuitMonth3", changeCountItemMonth3);
			Number changeCountItemQuarter = getSponsorT(listChangeItem, "quarterCount");
			projMap.put("drawCountSuitQuarter", changeCountItemQuarter);
			
			outList.add(projMap);
			
			projMap = new HashMap<String, Object>(); 
			projMap.put("fileType", "工程变更文件");
			projMap.put("typeClass", "电子");
			projMap.put("unitType", "GB/MB");
			Number changeCountGMMonth1 = getSponsorT(listChangeGM, "month1");
			double changeGMMonth1 = changeCountGMMonth1.doubleValue()/1048576.00;
			projMap.put("drawCountSuitMonth1", changeGMMonth1);
			Number changeCountGMMonth2 = getSponsorT(listChangeGM, "month2");
			double changeGMMonth2 = changeCountGMMonth2.doubleValue()/1048576.00;
			projMap.put("drawCountSuitMonth2", changeGMMonth2);
			Number changeCountGMMonth3 = getSponsorT(listChangeGM, "month3");
			double changeGMMonth3 = changeCountGMMonth3.doubleValue()/1048576.00;
			projMap.put("drawCountSuitMonth3", changeGMMonth3);
			Number changeCountGMQuarter = getSponsorT(listChangeGM, "quarterCount");
			double changeGMQuarter = changeCountGMQuarter.doubleValue()/1048576.00;
			projMap.put("drawCountSuitQuarter", changeGMQuarter);
			
			outList.add(projMap);
			
			List<Map<String, Object> > classMapList = new ArrayList<Map<String, Object>>();
			Map<String, Object> classMap = new HashMap<String, Object>();
			classMap.put("type", "设备管理");
			classMap.put("name", "设备文件");
			classMapList.add(typeMap);
			typeMap = new HashMap<String, Object>();
			classMap.put("type", "商务管理");
			classMap.put("name", "商务文件");
			classMapList.add(typeMap);
			
			for(int i=0; i<classMapList.size(); i++) {
				String sqlCriminalSuit = "select distinct b.C_ARC_CLASSIC, " + 
						"(select count(*) as cesetMonth1 from ecm_audit_general eag left join ecm_document ed on eag.DOC_ID = ed.ID where ed.C_ARC_CLASSIC = '"+ classMapList.get(i).get("type") +"' and b.C_INCLUDE_PAPER = '是' and ed.C_ITEM_TYPE = '案卷'"+ conditionDate1 +") as month1," + 
						"(select count(*) as cesetMonth2 from ecm_audit_general eag left join ecm_document ed on eag.DOC_ID = ed.ID where ed.C_ARC_CLASSIC = '"+ classMapList.get(i).get("type") +"' and b.C_INCLUDE_PAPER = '是' and ed.C_ITEM_TYPE = '案卷'"+ conditionDate2 +") as month2," + 
						"(select count(*) as cesetMonth3 from ecm_audit_general eag left join ecm_document ed on eag.DOC_ID = ed.ID where ed.C_ARC_CLASSIC = '"+ classMapList.get(i).get("type") +"' and b.C_INCLUDE_PAPER = '是' and ed.C_ITEM_TYPE = '案卷'"+ conditionDate3 +") as month3," + 
						"(select count(*) as cesetMonth from ecm_audit_general eag left join ecm_document ed on eag.DOC_ID = ed.ID where ed.C_ARC_CLASSIC = '"+ classMapList.get(i).get("type") +"' and b.C_INCLUDE_PAPER = '是' and ed.C_ITEM_TYPE = '案卷'"+ conditionDate +") as quarterCount " + 
						"from ecm_audit_general a left join ecm_document b on a.DOC_ID = b.ID " + 
						"where b.C_ARC_CLASSIC = '"+ classMapList.get(i).get("type") +"' and b.C_INCLUDE_PAPER = '是'";
				String sqlCriminalVolume = "select distinct b.C_ARC_CLASSIC, " + 
						"(select sum(ed.C_VOLUME_COUNT) as cesetMonth1 from ecm_audit_general eag left join ecm_document ed on eag.DOC_ID = ed.ID where ed.C_ARC_CLASSIC = '"+ classMapList.get(i).get("type") +"' and b.C_INCLUDE_PAPER = '是'"+ conditionDate1 +") as month1," + 
						"(select sum(ed.C_VOLUME_COUNT) as cesetMonth2 from ecm_audit_general eag left join ecm_document ed on eag.DOC_ID = ed.ID where ed.C_ARC_CLASSIC = '"+ classMapList.get(i).get("type") +"' and b.C_INCLUDE_PAPER = '是'"+ conditionDate2 +") as month2," + 
						"(select sum(ed.C_VOLUME_COUNT) as cesetMonth3 from ecm_audit_general eag left join ecm_document ed on eag.DOC_ID = ed.ID where ed.C_ARC_CLASSIC = '"+ classMapList.get(i).get("type") +"' and b.C_INCLUDE_PAPER = '是'"+ conditionDate3 +") as month3," + 
						"(select sum(ed.C_VOLUME_COUNT) as cesetMonth from ecm_audit_general eag left join ecm_document ed on eag.DOC_ID = ed.ID where ed.C_ARC_CLASSIC = '"+ classMapList.get(i).get("type") +"' and b.C_INCLUDE_PAPER = '是'"+ conditionDate +") as quarterCount  " + 
						"from ecm_audit_general a left join ecm_document b on a.DOC_ID = b.ID " + 
						"where b.C_ARC_CLASSIC = '"+ classMapList.get(i).get("type") +"' and b.C_INCLUDE_PAPER = '是'";
				String sqlCriminalItem = "select distinct b.C_ARC_CLASSIC, " + 
						"(select count(*) as cesetMonth1 from ecm_audit_general eag left join ecm_document ed on eag.DOC_ID = ed.ID where ed.C_ARC_CLASSIC = '"+ classMapList.get(i).get("type") +"' and b.CONTENT_SIZE > 0"+ conditionDate1 +") as month1," + 
						"(select count(*) as cesetMonth2 from ecm_audit_general eag left join ecm_document ed on eag.DOC_ID = ed.ID where ed.C_ARC_CLASSIC = '"+ classMapList.get(i).get("type") +"' and b.CONTENT_SIZE > 0"+ conditionDate2 +") as month2," + 
						"(select count(*) as cesetMonth3 from ecm_audit_general eag left join ecm_document ed on eag.DOC_ID = ed.ID where ed.C_ARC_CLASSIC = '"+ classMapList.get(i).get("type") +"' and b.CONTENT_SIZE > 0"+ conditionDate3 +") as month3," + 
						"(select count(*) as cesetMonth from ecm_audit_general eag left join ecm_document ed on eag.DOC_ID = ed.ID where ed.C_ARC_CLASSIC = '"+ classMapList.get(i).get("type") +"' and b.CONTENT_SIZE > 0"+ conditionDate +") as quarterCount  " + 
						"from ecm_audit_general a left join ecm_document b on a.DOC_ID = b.ID " + 
						"where b.C_ARC_CLASSIC = '"+ classMapList.get(i).get("type") +"' and b.CONTENT_SIZE > 0";
				String sqlCriminalGM = "select distinct b.C_ARC_CLASSIC, " + 
						"(select sum(ed.CONTENT_SIZE) as cesetMonth1 from ecm_audit_general eag left join ecm_document ed on eag.DOC_ID = ed.ID where ed.C_ARC_CLASSIC = '"+ classMapList.get(i).get("type") +"' and b.CONTENT_SIZE > 0"+ conditionDate1 +") as month1," + 
						"(select sum(ed.CONTENT_SIZE) as cesetMonth2 from ecm_audit_general eag left join ecm_document ed on eag.DOC_ID = ed.ID where ed.C_ARC_CLASSIC = '"+ classMapList.get(i).get("type") +"' and b.CONTENT_SIZE > 0"+ conditionDate2 +") as month2," + 
						"(select sum(ed.CONTENT_SIZE) as cesetMonth3 from ecm_audit_general eag left join ecm_document ed on eag.DOC_ID = ed.ID where ed.C_ARC_CLASSIC = '"+ classMapList.get(i).get("type") +"' and b.CONTENT_SIZE > 0"+ conditionDate3 +") as month3," + 
						"(select sum(ed.CONTENT_SIZE) as cesetMonth from ecm_audit_general eag left join ecm_document ed on eag.DOC_ID = ed.ID where ed.C_ARC_CLASSIC = '"+ classMapList.get(i).get("type") +"' and b.CONTENT_SIZE > 0"+ conditionDate +") as quarterCount  " + 
						"from ecm_audit_general a left join ecm_document b on a.DOC_ID = b.ID " + 
						"where b.C_ARC_CLASSIC = '"+ classMapList.get(i).get("type") +"' and b.CONTENT_SIZE > 0";
				
				List<Map<String, Object>> listCriminalSuit = documentService.getMapList(getToken(), sqlCriminalSuit);
				List<Map<String, Object>> listCriminalVolume = documentService.getMapList(getToken(), sqlCriminalVolume);
				List<Map<String, Object>> listCriminalItem = documentService.getMapList(getToken(), sqlCriminalItem);
				List<Map<String, Object>> listCriminalGM = documentService.getMapList(getToken(), sqlCriminalGM);
				
				projMap = new HashMap<String, Object>(); 
				projMap.put("fileType", classMapList.get(i).get("name"));
				projMap.put("typeClass", "实体");
				projMap.put("unitType", "套");
				Number criminalCountSuitMonth1 = getSponsorT(listCriminalSuit, "month1");
				projMap.put("drawCountSuitMonth1", criminalCountSuitMonth1);
				Number criminalCountSuitMonth2 = getSponsorT(listCriminalSuit, "month2");
				projMap.put("drawCountSuitMonth2", criminalCountSuitMonth2);
				Number criminalCountSuitMonth3 = getSponsorT(listCriminalSuit, "month3");
				projMap.put("drawCountSuitMonth3", criminalCountSuitMonth3);
				Number criminalCountSuitQuarter = getSponsorT(listCriminalSuit, "quarterCount");
				projMap.put("drawCountSuitQuarter", criminalCountSuitQuarter);
				
				outList.add(projMap);
				
				projMap = new HashMap<String, Object>(); 
				projMap.put("fileType", classMapList.get(i).get("name"));
				projMap.put("typeClass", "实体");
				projMap.put("unitType", "册");
				Number criminalCountVolumeMonth1 = getSponsorT(listCriminalVolume, "month1");
				projMap.put("drawCountSuitMonth1", criminalCountVolumeMonth1);
				Number criminalCountVolumeMonth2 = getSponsorT(listCriminalVolume, "month2");
				projMap.put("drawCountSuitMonth2", criminalCountVolumeMonth2);
				Number criminalCountVolumeMonth3 = getSponsorT(listCriminalVolume, "month3");
				projMap.put("drawCountSuitMonth3", criminalCountVolumeMonth3);
				Number criminalCountVolumeQuarter = getSponsorT(listCriminalVolume, "quarterCount");
				projMap.put("drawCountSuitQuarter", criminalCountVolumeQuarter);
				
				outList.add(projMap);
				
				projMap = new HashMap<String, Object>(); 
				projMap.put("fileType", classMapList.get(i).get("name"));
				projMap.put("typeClass", "电子");
				projMap.put("unitType", "件");
				Number criminalCountItemMonth1 = getSponsorT(listCriminalItem, "month1");
				projMap.put("drawCountSuitMonth1", criminalCountItemMonth1);
				Number criminalCountItemMonth2 = getSponsorT(listCriminalItem, "month2");
				projMap.put("drawCountSuitMonth2", criminalCountItemMonth2);
				Number criminalCountItemMonth3 = getSponsorT(listCriminalItem, "month3");
				projMap.put("drawCountSuitMonth3", criminalCountItemMonth3);
				Number criminalCountItemQuarter = getSponsorT(listCriminalItem, "quarterCount");
				projMap.put("drawCountSuitQuarter", criminalCountItemQuarter);
				
				outList.add(projMap);
				
				projMap = new HashMap<String, Object>(); 
				projMap.put("fileType", classMapList.get(i).get("name"));
				projMap.put("typeClass", "电子");
				projMap.put("unitType", "GB/MB");
				Number criminalCountGMMonth1 = getSponsorT(listCriminalGM, "month1");
				double criminalGMMonth1 = criminalCountGMMonth1.doubleValue()/1048576.00;
				projMap.put("drawCountSuitMonth1", criminalGMMonth1);
				Number criminalCountGMMonth2 = getSponsorT(listCriminalGM, "month2");
				double criminalGMMonth2 = criminalCountGMMonth2.doubleValue()/1048576.00;
				projMap.put("drawCountSuitMonth2", criminalGMMonth2);
				Number criminalCountGMMonth3 = getSponsorT(listCriminalGM, "month3");
				double criminalGMMonth3 = criminalCountGMMonth3.doubleValue()/1048576.00;
				projMap.put("drawCountSuitMonth3", criminalGMMonth3);
				Number criminalCountGMQuarter = getSponsorT(listCriminalGM, "quarterCount");
				double criminalGMQuarter = criminalCountGMQuarter.doubleValue()/1048576.00;
				projMap.put("drawCountSuitQuarter", criminalGMQuarter);
				
				outList.add(projMap);
			}
			
			String sqlChangeSuitParty = "select distinct b.C_ARC_CLASSIC, " + 
					"(select count(*) as cesetMonth1 from ecm_audit_general eag left join ecm_document ed on eag.DOC_ID = ed.ID where ed.C_ARC_CLASSIC = '党群行政' and b.C_INCLUDE_PAPER = '是' and ed.C_ITEM_TYPE = '案卷'"+ conditionDate1 +") as month1," + 
					"(select count(*) as cesetMonth2 from ecm_audit_general eag left join ecm_document ed on eag.DOC_ID = ed.ID where ed.C_ARC_CLASSIC = '党群行政' and b.C_INCLUDE_PAPER = '是' and ed.C_ITEM_TYPE = '案卷'"+ conditionDate2 +") as month2," + 
					"(select count(*) as cesetMonth3 from ecm_audit_general eag left join ecm_document ed on eag.DOC_ID = ed.ID where ed.C_ARC_CLASSIC = '党群行政' and b.C_INCLUDE_PAPER = '是' and ed.C_ITEM_TYPE = '案卷'"+ conditionDate3 +") as month3," + 
					"(select count(*) as cesetMonth from ecm_audit_general eag left join ecm_document ed on eag.DOC_ID = ed.ID where ed.C_ARC_CLASSIC = '党群行政' and b.C_INCLUDE_PAPER = '是' and ed.C_ITEM_TYPE = '案卷'"+ conditionDate +") as quarterCount " + 
					"from ecm_audit_general a left join ecm_document b on a.DOC_ID = b.ID " + 
					"where b.C_ARC_CLASSIC = '党群行政' and b.C_INCLUDE_PAPER = '是'";
			String sqlChangeVolumeParty = "select distinct b.C_ARC_CLASSIC, " + 
					"(select count(*) as cesetMonth1 from ecm_audit_general eag left join ecm_document ed on eag.DOC_ID = ed.ID where ed.C_ARC_CLASSIC = '党群行政' and b.C_INCLUDE_PAPER = '是'"+ conditionDate1 +") as month1," + 
					"(select count(*) as cesetMonth2 from ecm_audit_general eag left join ecm_document ed on eag.DOC_ID = ed.ID where ed.C_ARC_CLASSIC = '党群行政' and b.C_INCLUDE_PAPER = '是'"+ conditionDate2 +") as month2," + 
					"(select count(*) as cesetMonth3 from ecm_audit_general eag left join ecm_document ed on eag.DOC_ID = ed.ID where ed.C_ARC_CLASSIC = '党群行政' and b.C_INCLUDE_PAPER = '是'"+ conditionDate3 +") as month3," + 
					"(select count(*) as cesetMonth from ecm_audit_general eag left join ecm_document ed on eag.DOC_ID = ed.ID where ed.C_ARC_CLASSIC = '党群行政' and b.C_INCLUDE_PAPER = '是'"+ conditionDate +") as quarterCount  " + 
					"from ecm_audit_general a left join ecm_document b on a.DOC_ID = b.ID " + 
					"where b.C_ARC_CLASSIC = '党群行政' and b.C_INCLUDE_PAPER = '是'";
			String sqlChangeItemParty = "select distinct b.C_ARC_CLASSIC, " + 
					"(select count(*) as cesetMonth1 from ecm_audit_general eag left join ecm_document ed on eag.DOC_ID = ed.ID where ed.C_ARC_CLASSIC = '党群行政' and b.CONTENT_SIZE > 0"+ conditionDate1 +") as month1," + 
					"(select count(*) as cesetMonth2 from ecm_audit_general eag left join ecm_document ed on eag.DOC_ID = ed.ID where ed.C_ARC_CLASSIC = '党群行政' and b.CONTENT_SIZE > 0"+ conditionDate2 +") as month2," + 
					"(select count(*) as cesetMonth3 from ecm_audit_general eag left join ecm_document ed on eag.DOC_ID = ed.ID where ed.C_ARC_CLASSIC = '党群行政' and b.CONTENT_SIZE > 0"+ conditionDate3 +") as month3," + 
					"(select count(*) as cesetMonth from ecm_audit_general eag left join ecm_document ed on eag.DOC_ID = ed.ID where ed.C_ARC_CLASSIC = '党群行政' and b.CONTENT_SIZE > 0"+ conditionDate +") as quarterCount  " + 
					"from ecm_audit_general a left join ecm_document b on a.DOC_ID = b.ID " + 
					"where b.C_ARC_CLASSIC = '党群行政' and b.CONTENT_SIZE > 0";
			String sqlChangeGMParty = "select distinct b.C_ARC_CLASSIC, " + 
					"(select sum(ed.CONTENT_SIZE) as cesetMonth1 from ecm_audit_general eag left join ecm_document ed on eag.DOC_ID = ed.ID where ed.C_ARC_CLASSIC = '党群行政' and b.CONTENT_SIZE > 0"+ conditionDate1 +") as month1," + 
					"(select sum(ed.CONTENT_SIZE) as cesetMonth2 from ecm_audit_general eag left join ecm_document ed on eag.DOC_ID = ed.ID where ed.C_ARC_CLASSIC = '党群行政' and b.CONTENT_SIZE > 0"+ conditionDate2 +") as month2," + 
					"(select sum(ed.CONTENT_SIZE) as cesetMonth3 from ecm_audit_general eag left join ecm_document ed on eag.DOC_ID = ed.ID where ed.C_ARC_CLASSIC = '党群行政' and b.CONTENT_SIZE > 0"+ conditionDate3 +") as month3," + 
					"(select sum(ed.CONTENT_SIZE) as cesetMonth from ecm_audit_general eag left join ecm_document ed on eag.DOC_ID = ed.ID where ed.C_ARC_CLASSIC = '党群行政' and b.CONTENT_SIZE > 0"+ conditionDate +") as quarterCount  " + 
					"from ecm_audit_general a left join ecm_document b on a.DOC_ID = b.ID " + 
					"where b.C_ARC_CLASSIC = '党群行政' and b.CONTENT_SIZE > 0";
			
			List<Map<String, Object>> listChangeSuitParty = documentService.getMapList(getToken(), sqlChangeSuitParty);
			List<Map<String, Object>> listChangeVolumeParty = documentService.getMapList(getToken(), sqlChangeVolumeParty);
			List<Map<String, Object>> listChangeItemParty = documentService.getMapList(getToken(), sqlChangeItemParty);
			List<Map<String, Object>> listChangeGMParty = documentService.getMapList(getToken(), sqlChangeGMParty);
			
			projMap = new HashMap<String, Object>(); 
			projMap.put("fileType", "党群行政文件");
			projMap.put("typeClass", "实体");
			projMap.put("unitType", "卷");
			Number changeCountSuitMonth1Party = getSponsorT(listChangeSuitParty, "month1");
			projMap.put("drawCountSuitMonth1", changeCountSuitMonth1Party);
			Number changeCountSuitMonth2Party = getSponsorT(listChangeSuitParty, "month2");
			projMap.put("drawCountSuitMonth2", changeCountSuitMonth2Party);
			Number changeCountSuitMonth3Party = getSponsorT(listChangeSuitParty, "month3");
			projMap.put("drawCountSuitMonth3", changeCountSuitMonth3Party);
			Number changeCountSuitQuarterParty = getSponsorT(listChangeSuitParty, "quarterCount");
			projMap.put("drawCountSuitQuarter", changeCountSuitQuarterParty);
			
			outList.add(projMap);
			
			projMap = new HashMap<String, Object>(); 
			projMap.put("fileType", "党群行政文件");
			projMap.put("typeClass", "实体");
			projMap.put("unitType", "件");
			Number changeCountVolumeMonth1Party = getSponsorT(listChangeVolumeParty, "month1");
			projMap.put("drawCountSuitMonth1", changeCountVolumeMonth1Party);
			Number changeCountVolumeMonth2Party = getSponsorT(listChangeVolumeParty, "month2");
			projMap.put("drawCountSuitMonth2", changeCountVolumeMonth2Party);
			Number changeCountVolumeMonth3Party = getSponsorT(listChangeVolumeParty, "month3");
			projMap.put("drawCountSuitMonth3", changeCountVolumeMonth3Party);
			Number changeCountVolumeQuarterParty = getSponsorT(listChangeVolumeParty, "quarterCount");
			projMap.put("drawCountSuitQuarter", changeCountVolumeQuarterParty);
			
			outList.add(projMap);
			
			projMap = new HashMap<String, Object>(); 
			projMap.put("fileType", "党群行政文件");
			projMap.put("typeClass", "电子");
			projMap.put("unitType", "件");
			Number changeCountItemMonth1Party = getSponsorT(listChangeItemParty, "month1");
			projMap.put("drawCountSuitMonth1", changeCountItemMonth1Party);
			Number changeCountItemMonth2Party = getSponsorT(listChangeItemParty, "month2");
			projMap.put("drawCountSuitMonth2", changeCountItemMonth2Party);
			Number changeCountItemMonth3Party = getSponsorT(listChangeItemParty, "month3");
			projMap.put("drawCountSuitMonth3", changeCountItemMonth3Party);
			Number changeCountItemQuarterParty = getSponsorT(listChangeItemParty, "quarterCount");
			projMap.put("drawCountSuitQuarter", changeCountItemQuarterParty);
			
			outList.add(projMap);
			
			projMap = new HashMap<String, Object>(); 
			projMap.put("fileType", "党群行政文件");
			projMap.put("typeClass", "电子");
			projMap.put("unitType", "GB/MB");
			Number changeCountGMMonth1Party = getSponsorT(listChangeGMParty, "month1");
			double changeGMMonth1Party = changeCountGMMonth1Party.doubleValue()/1048576.00;
			projMap.put("drawCountSuitMonth1", changeGMMonth1Party);
			Number changeCountGMMonth2Party = getSponsorT(listChangeGMParty, "month2");
			double changeGMMonth2Party = changeCountGMMonth2Party.doubleValue()/1048576.00;
			projMap.put("drawCountSuitMonth2", changeGMMonth2Party);
			Number changeCountGMMonth3Party = getSponsorT(listChangeGMParty, "month3");
			double changeGMMonth3Party = changeCountGMMonth3Party.doubleValue()/1048576.00;
			projMap.put("drawCountSuitMonth3", changeGMMonth3Party);
			Number changeCountGMQuarterParty = getSponsorT(listChangeGMParty, "quarterCount");
			double changeGMQuarterParty = changeCountGMQuarterParty.doubleValue()/1048576.00;
			projMap.put("drawCountSuitQuarter", changeGMQuarterParty);
			
			outList.add(projMap);
			
			String sqlChangeSuitPeriod = "select distinct b.C_ARC_CLASSIC, " + 
					"(select count(*) as cesetMonth1 from ecm_audit_general eag left join ecm_document ed on eag.DOC_ID = ed.ID where ed.C_ARC_CLASSIC = '科技与信息' and ed.TYPE_NAME='其他' and ed.SUB_TYPE='公司刊物' and b.C_INCLUDE_PAPER = '是' and ed.C_ITEM_TYPE = '案卷'"+ conditionDate1 +") as month1," + 
					"(select count(*) as cesetMonth2 from ecm_audit_general eag left join ecm_document ed on eag.DOC_ID = ed.ID where ed.C_ARC_CLASSIC = '科技与信息' and ed.TYPE_NAME='其他' and ed.SUB_TYPE='公司刊物' and b.C_INCLUDE_PAPER = '是' and ed.C_ITEM_TYPE = '案卷'"+ conditionDate2 +") as month2," + 
					"(select count(*) as cesetMonth3 from ecm_audit_general eag left join ecm_document ed on eag.DOC_ID = ed.ID where ed.C_ARC_CLASSIC = '科技与信息' and ed.TYPE_NAME='其他' and ed.SUB_TYPE='公司刊物' and b.C_INCLUDE_PAPER = '是' and ed.C_ITEM_TYPE = '案卷'"+ conditionDate3 +") as month3," + 
					"(select count(*) as cesetMonth from ecm_audit_general eag left join ecm_document ed on eag.DOC_ID = ed.ID where ed.C_ARC_CLASSIC = '科技与信息' and ed.TYPE_NAME='其他' and ed.SUB_TYPE='公司刊物' and b.C_INCLUDE_PAPER = '是' and ed.C_ITEM_TYPE = '案卷'"+ conditionDate +") as quarterCount " + 
					"from ecm_audit_general a left join ecm_document b on a.DOC_ID = b.ID " + 
					"where b.C_ARC_CLASSIC = '科技与信息' and b.TYPE_NAME='其他' and b.SUB_TYPE='公司刊物' and b.C_INCLUDE_PAPER = '是'";
			String sqlChangeVolumePeriod = "select distinct b.C_ARC_CLASSIC, " + 
					"(select sum(ed.C_VOLUME_COUNT) as cesetMonth1 from ecm_audit_general eag left join ecm_document ed on eag.DOC_ID = ed.ID where ed.C_ARC_CLASSIC = '科技与信息' and ed.TYPE_NAME='其他' and ed.SUB_TYPE='公司刊物' and b.C_INCLUDE_PAPER = '是'"+ conditionDate1 +") as month1," + 
					"(select sum(ed.C_VOLUME_COUNT) as cesetMonth2 from ecm_audit_general eag left join ecm_document ed on eag.DOC_ID = ed.ID wh    ere ed.C_ARC_CLASSIC = '科技与信息' and ed.TYPE_NAME='其他' and ed.SUB_TYPE='公司刊物' and b.C_INCLUDE_PAPER = '是'"+ conditionDate2 +") as month2," + 
					"(select sum(ed.C_VOLUME_COUNT) as cesetMonth3 from ecm_audit_general eag left join ecm_document ed on eag.DOC_ID = ed.ID where ed.C_ARC_CLASSIC = '科技与信息' and ed.TYPE_NAME='其他' and ed.SUB_TYPE='公司刊物' and b.C_INCLUDE_PAPER = '是'"+ conditionDate3 +") as month3," + 
					"(select sum(ed.C_VOLUME_COUNT) as cesetMonth from ecm_audit_general eag left join ecm_document ed on eag.DOC_ID = ed.ID where ed.C_ARC_CLASSIC = '科技与信息' and ed.TYPE_NAME='其他' and ed.SUB_TYPE='公司刊物' and b.C_INCLUDE_PAPER = '是'"+ conditionDate +") as quarterCount  " + 
					"from ecm_audit_general a left join ecm_document b on a.DOC_ID = b.ID " + 
					"where b.C_ARC_CLASSIC = '科技与信息' and b.TYPE_NAME='其他' and b.SUB_TYPE='公司刊物' and b.C_INCLUDE_PAPER = '是'";
			String sqlChangeItemPeriod = "select distinct b.C_ARC_CLASSIC, " + 
					"(select count(*) as cesetMonth1 from ecm_audit_general eag left join ecm_document ed on eag.DOC_ID = ed.ID where ed.C_ARC_CLASSIC = '科技与信息' and ed.TYPE_NAME='其他' and ed.SUB_TYPE='公司刊物' and b.CONTENT_SIZE > 0"+ conditionDate1 +") as month1," + 
					"(select count(*) as cesetMonth2 from ecm_audit_general eag left join ecm_document ed on eag.DOC_ID = ed.ID where ed.C_ARC_CLASSIC = '科技与信息' and ed.TYPE_NAME='其他' and ed.SUB_TYPE='公司刊物' and b.CONTENT_SIZE > 0"+ conditionDate2 +") as month2," + 
					"(select count(*) as cesetMonth3 from ecm_audit_general eag left join ecm_document ed on eag.DOC_ID = ed.ID where ed.C_ARC_CLASSIC = '科技与信息' and ed.TYPE_NAME='其他' and ed.SUB_TYPE='公司刊物' and b.CONTENT_SIZE > 0"+ conditionDate3 +") as month3," + 
					"(select count(*) as cesetMonth from ecm_audit_general eag left join ecm_document ed on eag.DOC_ID = ed.ID where ed.C_ARC_CLASSIC = '科技与信息' and ed.TYPE_NAME='其他' and ed.SUB_TYPE='公司刊物' and b.CONTENT_SIZE > 0"+ conditionDate +") as quarterCount  " + 
					"from ecm_audit_general a left join ecm_document b on a.DOC_ID = b.ID " + 
					"where b.C_ARC_CLASSIC = '科技与信息' and b.TYPE_NAME='其他' and b.SUB_TYPE='公司刊物' and b.CONTENT_SIZE > 0";
			String sqlChangeGMPeriod = "select distinct b.C_ARC_CLASSIC, " + 
					"(select sum(ed.CONTENT_SIZE) as cesetMonth1 from ecm_audit_general eag left join ecm_document ed on eag.DOC_ID = ed.ID where ed.C_ARC_CLASSIC = '科技与信息' and ed.TYPE_NAME='其他' and ed.SUB_TYPE='公司刊物' and b.CONTENT_SIZE > 0"+ conditionDate1 +") as month1," + 
					"(select sum(ed.CONTENT_SIZE) as cesetMonth2 from ecm_audit_general eag left join ecm_document ed on eag.DOC_ID = ed.ID where ed.C_ARC_CLASSIC = '科技与信息' and ed.TYPE_NAME='其他' and ed.SUB_TYPE='公司刊物' and b.CONTENT_SIZE > 0"+ conditionDate2 +") as month2," + 
					"(select sum(ed.CONTENT_SIZE) as cesetMonth3 from ecm_audit_general eag left join ecm_document ed on eag.DOC_ID = ed.ID where ed.C_ARC_CLASSIC = '科技与信息' and ed.TYPE_NAME='其他' and ed.SUB_TYPE='公司刊物' and b.CONTENT_SIZE > 0"+ conditionDate3 +") as month3," + 
					"(select sum(ed.CONTENT_SIZE) as cesetMonth from ecm_audit_general eag left join ecm_document ed on eag.DOC_ID = ed.ID where ed.C_ARC_CLASSIC = '科技与信息' and ed.TYPE_NAME='其他' and ed.SUB_TYPE='公司刊物' and b.CONTENT_SIZE > 0"+ conditionDate +") as quarterCount  " + 
					"from ecm_audit_general a left join ecm_document b on a.DOC_ID = b.ID " + 
					"where b.C_ARC_CLASSIC = '科技与信息' and b.TYPE_NAME='其他' and b.SUB_TYPE='公司刊物' and b.CONTENT_SIZE > 0";
			
			List<Map<String, Object>> listChangeSuitPeriod = documentService.getMapList(getToken(), sqlChangeSuitPeriod);
			List<Map<String, Object>> listChangeVolumePeriod = documentService.getMapList(getToken(), sqlChangeVolumePeriod);
			List<Map<String, Object>> listChangeItemPeriod = documentService.getMapList(getToken(), sqlChangeItemPeriod);
			List<Map<String, Object>> listChangeGMPeriod = documentService.getMapList(getToken(), sqlChangeGMPeriod);
			
			projMap = new HashMap<String, Object>(); 
			projMap.put("fileType", "公司刊物");
			projMap.put("typeClass", "实体");
			projMap.put("unitType", "卷");
			Number changeCountSuitMonth1Period = getSponsorT(listChangeSuitPeriod, "month1");
			projMap.put("drawCountSuitMonth1", changeCountSuitMonth1Period);
			Number changeCountSuitMonth2Period = getSponsorT(listChangeSuitPeriod, "month2");
			projMap.put("drawCountSuitMonth2", changeCountSuitMonth2Period);
			Number changeCountSuitMonth3Period = getSponsorT(listChangeSuitPeriod, "month3");
			projMap.put("drawCountSuitMonth3", changeCountSuitMonth3Period);
			Number changeCountSuitQuarterPeriod = getSponsorT(listChangeSuitPeriod, "quarterCount");
			projMap.put("drawCountSuitQuarter", changeCountSuitQuarterPeriod);
			
			outList.add(projMap);
			
			projMap = new HashMap<String, Object>(); 
			projMap.put("fileType", "公司刊物");
			projMap.put("typeClass", "实体");
			projMap.put("unitType", "册");
			Number changeCountVolumeMonth1Period = getSponsorT(listChangeVolumePeriod, "month1");
			projMap.put("drawCountSuitMonth1", changeCountVolumeMonth1Period);
			Number changeCountVolumeMonth2Period = getSponsorT(listChangeVolumePeriod, "month2");
			projMap.put("drawCountSuitMonth2", changeCountVolumeMonth2Period);
			Number changeCountVolumeMonth3Period = getSponsorT(listChangeVolumePeriod, "month3");
			projMap.put("drawCountSuitMonth3", changeCountVolumeMonth3Period);
			Number changeCountVolumeQuarterPeriod = getSponsorT(listChangeVolumePeriod, "quarterCount");
			projMap.put("drawCountSuitQuarter", changeCountVolumeQuarterPeriod);
			
			outList.add(projMap);
			
			projMap = new HashMap<String, Object>(); 
			projMap.put("fileType", "公司刊物");
			projMap.put("typeClass", "电子");
			projMap.put("unitType", "件");
			Number changeCountItemMonth1Period = getSponsorT(listChangeItemPeriod, "month1");
			projMap.put("drawCountSuitMonth1", changeCountItemMonth1Period);
			Number changeCountItemMonth2Period = getSponsorT(listChangeItemPeriod, "month2");
			projMap.put("drawCountSuitMonth2", changeCountItemMonth2Period);
			Number changeCountItemMonth3Period = getSponsorT(listChangeItemPeriod, "month3");
			projMap.put("drawCountSuitMonth3", changeCountItemMonth3Period);
			Number changeCountItemQuarterPeriod = getSponsorT(listChangeItemPeriod, "quarterCount");
			projMap.put("drawCountSuitQuarter", changeCountItemQuarterPeriod);
			
			outList.add(projMap);
			
			projMap = new HashMap<String, Object>(); 
			projMap.put("fileType", "公司刊物");
			projMap.put("typeClass", "电子");
			projMap.put("unitType", "GB/MB");
			Number changeCountGMMonth1Period = getSponsorT(listChangeGMPeriod, "month1");
			double changeGMMonth1Period = changeCountGMMonth1Period.doubleValue()/1048576.00;
			projMap.put("drawCountSuitMonth1", changeGMMonth1Period);
			Number changeCountGMMonth2Period = getSponsorT(listChangeGMPeriod, "month2");
			double changeGMMonth2Period = changeCountGMMonth2Period.doubleValue()/1048576.00;
			projMap.put("drawCountSuitMonth2", changeGMMonth2Period);
			Number changeCountGMMonth3Period = getSponsorT(listChangeGMPeriod, "month3");
			double changeGMMonth3Period = changeCountGMMonth3Period.doubleValue()/1048576.00;
			projMap.put("drawCountSuitMonth3", changeGMMonth3Period);
			Number changeCountGMQuarterPeriod = getSponsorT(listChangeGMPeriod, "quarterCount");
			double changeGMQuarterPeriod = changeCountGMQuarterPeriod.doubleValue()/1048576.00;
			projMap.put("drawCountSuitQuarter", changeGMQuarterPeriod);
			
			outList.add(projMap);
			
			String sqlChangeVolumeCemmercial = "select distinct b.C_ARC_CLASSIC, " + 
					"(select sum(ed.C_VOLUME_COUNT) as cesetMonth1 from ecm_audit_general eag left join ecm_document ed on eag.DOC_ID = ed.ID where ed.C_ARC_CLASSIC = '财务会计' and ed.TYPE_NAME='财务会计文件' and b.C_INCLUDE_PAPER = '是'"+ conditionDate1 +") as month1," + 
					"(select sum(ed.C_VOLUME_COUNT) as cesetMonth2 from ecm_audit_general eag left join ecm_document ed on eag.DOC_ID = ed.ID where ed.C_ARC_CLASSIC = '财务会计' and ed.TYPE_NAME='财务会计文件' and b.C_INCLUDE_PAPER = '是'"+ conditionDate2 +") as month2," + 
					"(select sum(ed.C_VOLUME_COUNT) as cesetMonth3 from ecm_audit_general eag left join ecm_document ed on eag.DOC_ID = ed.ID where ed.C_ARC_CLASSIC = '财务会计' and ed.TYPE_NAME='财务会计文件' and b.C_INCLUDE_PAPER = '是'"+ conditionDate3 +") as month3," + 
					"(select sum(ed.C_VOLUME_COUNT) as cesetMonth from ecm_audit_general eag left join ecm_document ed on eag.DOC_ID = ed.ID where ed.C_ARC_CLASSIC = '财务会计' and ed.TYPE_NAME='财务会计文件' and b.C_INCLUDE_PAPER = '是'"+ conditionDate +") as quarterCount  " + 
					"from ecm_audit_general a left join ecm_document b on a.DOC_ID = b.ID " + 
					"where b.C_ARC_CLASSIC = '财务会计' and b.TYPE_NAME='财务会计文件' and b.C_INCLUDE_PAPER = '是'";
			
			List<Map<String, Object>> listChangeSuitCemmercial = documentService.getMapList(getToken(), sqlChangeVolumeCemmercial);
			
			projMap = new HashMap<String, Object>(); 
			projMap.put("fileType", "会计档案");
			projMap.put("typeClass", "实体");
			projMap.put("unitType", "册");
			Number changeCountVolumeMonth1Cemmercial = getSponsorT(listChangeSuitCemmercial, "month1");
			projMap.put("drawCountSuitMonth1", changeCountVolumeMonth1Cemmercial);
			Number changeCountVolumeMonth2Cemmercial = getSponsorT(listChangeSuitCemmercial, "month2");
			projMap.put("drawCountSuitMonth2", changeCountVolumeMonth2Cemmercial);
			Number changeCountVolumeMonth3Cemmercial = getSponsorT(listChangeSuitCemmercial, "month3");
			projMap.put("drawCountSuitMonth3", changeCountVolumeMonth3Cemmercial);
			Number changeCountVolumeQuarterCemmercial = getSponsorT(listChangeSuitCemmercial, "quarterCount");
			projMap.put("drawCountSuitQuarter", changeCountVolumeQuarterCemmercial);
			
			outList.add(projMap);
			
			                  
			mp.put("data", outList);
			mp.put("code", ActionContext.SUCESS);
			
			
		}catch(Exception ex) {
			ex.printStackTrace();
			mp.put("code", ActionContext.FAILURE);
		}
		
		return mp;
	}

	//档案升版
	@RequestMapping(value = "fileUpVersionWorkStatistic", method = RequestMethod.POST)
	@ResponseBody	
	public Map<String, Object> fileUpVersionWorkStatistic(@RequestBody String argStr){
		Map<String, Object> mp = new HashMap<String, Object>();
		List<Map<String, Object> > outList = new ArrayList<Map<String, Object>>();
		Map<String, Object> args = JSONUtils.stringToMap(argStr);
		
		String yearSelect = this.getStrValue(args, "yearSelect");
		String year = String.valueOf(Integer.parseInt(yearSelect.substring(0, 4)) + 1);
		String quarter = this.getStrValue(args, "quarterSelect");
		
		List<String> dateCondList = this.setQuarterCondition(year, quarter);
		
		String conditionDate1 = dateCondList.get(0);
		String conditionDate2 = dateCondList.get(1);
		String conditionDate3 = dateCondList.get(2);
		String conditionDate = dateCondList.get(3);
		
		try {
			Map<String, Object> projMap = new HashMap<String, Object>();
			
			String sqlDrawingSuit = "select distinct b.TYPE_NAME, b.SUB_TYPE, " + 
					"(select count(*) as cesetMonth1 from ecm_audit_general eag left join ecm_document ed on eag.DOC_ID = ed.ID where ed.TYPE_NAME = '设计文件' and ed.SUB_TYPE = '图纸' and ed.REVISION <> 'A' and ed.C_ITEM_TYPE = '案卷'"+ conditionDate1 +") as month1," + 
					"(select count(*) as cesetMonth2 from ecm_audit_general eag left join ecm_document ed on eag.DOC_ID = ed.ID where ed.TYPE_NAME = '设计文件' and ed.SUB_TYPE = '图纸' and ed.REVISION <> 'A' and ed.C_ITEM_TYPE = '案卷'"+ conditionDate2 +") as month2," + 
					"(select count(*) as cesetMonth3 from ecm_audit_general eag left join ecm_document ed on eag.DOC_ID = ed.ID where ed.TYPE_NAME = '设计文件' and ed.SUB_TYPE = '图纸' and ed.REVISION <> 'A' and ed.C_ITEM_TYPE = '案卷'"+ conditionDate3 +") as month3," + 
					"(select count(*) as cesetMonth from ecm_audit_general eag left join ecm_document ed on eag.DOC_ID = ed.ID where ed.TYPE_NAME = '设计文件' and ed.SUB_TYPE = '图纸' and ed.REVISION <> 'A' and ed.C_ITEM_TYPE = '案卷'"+ conditionDate +") as quarterCount  " + 
					"from ecm_audit_general a left join ecm_document b on a.DOC_ID = b.ID " + 
					"where b.TYPE_NAME = '设计文件' and b.SUB_TYPE = '图纸'";
			String sqlDrawingSheet = "select distinct b.TYPE_NAME, b.SUB_TYPE, " + 
					"(select sum(ed.C_PAGE_COUNT) as cesetMonth1 from ecm_audit_general eag left join ecm_document ed on eag.DOC_ID = ed.ID where ed.TYPE_NAME = '设计文件' and ed.SUB_TYPE = '图纸' and ed.REVISION <> 'A'"+ conditionDate1 +") as month1," + 
					"(select sum(ed.C_PAGE_COUNT) as cesetMonth2 from ecm_audit_general eag left join ecm_document ed on eag.DOC_ID = ed.ID where ed.TYPE_NAME = '设计文件' and ed.SUB_TYPE = '图纸' and ed.REVISION <> 'A'"+ conditionDate2 +") as month2," + 
					"(select sum(ed.C_PAGE_COUNT) as cesetMonth3 from ecm_audit_general eag left join ecm_document ed on eag.DOC_ID = ed.ID where ed.TYPE_NAME = '设计文件' and ed.SUB_TYPE = '图纸' and ed.REVISION <> 'A'"+ conditionDate3 +") as month3," + 
					"(select sum(ed.C_PAGE_COUNT) as cesetMonth from ecm_audit_general eag left join ecm_document ed on eag.DOC_ID = ed.ID where ed.TYPE_NAME = '设计文件' and ed.SUB_TYPE = '图纸' and ed.REVISION <> 'A'"+ conditionDate +") as quarterCount  " + 
					"from ecm_audit_general a left join ecm_document b on a.DOC_ID = b.ID " + 
					"where b.TYPE_NAME = '设计文件' and b.SUB_TYPE = '图纸'";
			String sqlFileSuit = "select distinct b.TYPE_NAME, b.SUB_TYPE, " + 
					"(select count(*) as cesetMonth1 from ecm_audit_general eag left join ecm_document ed on eag.DOC_ID = ed.ID where ed.TYPE_NAME = '设计文件' and ed.SUB_TYPE = '文件' and ed.REVISION <> 'A' and ed.C_ITEM_TYPE = '案卷'"+ conditionDate1 +") as month1," + 
					"(select count(*) as cesetMonth2 from ecm_audit_general eag left join ecm_document ed on eag.DOC_ID = ed.ID where ed.TYPE_NAME = '设计文件' and ed.SUB_TYPE = '文件' and ed.REVISION <> 'A' and ed.C_ITEM_TYPE = '案卷'"+ conditionDate2 +") as month2," + 
					"(select count(*) as cesetMonth3 from ecm_audit_general eag left join ecm_document ed on eag.DOC_ID = ed.ID where ed.TYPE_NAME = '设计文件' and ed.SUB_TYPE = '文件' and ed.REVISION <> 'A' and ed.C_ITEM_TYPE = '案卷'"+ conditionDate3 +") as month3," + 
					"(select count(*) as cesetMonth from ecm_audit_general eag left join ecm_document ed on eag.DOC_ID = ed.ID where ed.TYPE_NAME = '设计文件' and ed.SUB_TYPE = '文件' and ed.REVISION <> 'A' and ed.C_ITEM_TYPE = '案卷'"+ conditionDate +") as quarterCount  " + 
					"from ecm_audit_general a left join ecm_document b on a.DOC_ID = b.ID " + 
					"where b.TYPE_NAME = '设计文件' and b.SUB_TYPE = '文件'";
			String sqlFileVolume = "select distinct b.TYPE_NAME, b.SUB_TYPE, " + 
					"(select sum(ed.C_VOLUME_COUNT) as cesetMonth1 from ecm_audit_general eag left join ecm_document ed on eag.DOC_ID = ed.ID where ed.TYPE_NAME = '设计文件' and ed.SUB_TYPE = '文件' and ed.REVISION <> 'A'"+ conditionDate1 +") as month1," + 
					"(select sum(ed.C_VOLUME_COUNT) as cesetMonth2 from ecm_audit_general eag left join ecm_document ed on eag.DOC_ID = ed.ID where ed.TYPE_NAME = '设计文件' and ed.SUB_TYPE = '文件' and ed.REVISION <> 'A'"+ conditionDate2 +") as month2," + 
					"(select sum(ed.C_VOLUME_COUNT) as cesetMonth3 from ecm_audit_general eag left join ecm_document ed on eag.DOC_ID = ed.ID where ed.TYPE_NAME = '设计文件' and ed.SUB_TYPE = '文件' and ed.REVISION <> 'A'"+ conditionDate3 +") as month3," + 
					"(select sum(ed.C_VOLUME_COUNT) as cesetMonth from ecm_audit_general eag left join ecm_document ed on eag.DOC_ID = ed.ID where ed.TYPE_NAME = '设计文件' and ed.SUB_TYPE = '文件' and ed.REVISION <> 'A'"+ conditionDate +") as quarterCount  " + 
					"from ecm_audit_general a left join ecm_document b on a.DOC_ID = b.ID " + 
					"where b.TYPE_NAME = '设计文件' and b.SUB_TYPE = '文件'";
			
			List<Map<String, Object>> listDrawingSuit = documentService.getMapList(getToken(), sqlDrawingSuit);
			List<Map<String, Object>> listDrawingSheet = documentService.getMapList(getToken(), sqlDrawingSheet);
			List<Map<String, Object>> listFileSuit = documentService.getMapList(getToken(), sqlFileSuit);
			List<Map<String, Object>> listFileVolume = documentService.getMapList(getToken(), sqlFileVolume);
			
			Number drawCountSuitMonth1 = getSponsorT(listDrawingSuit, "month1");
			projMap.put("drawCountSuitMonth1", drawCountSuitMonth1);
			Number drawCountSuitMonth2 = getSponsorT(listDrawingSuit, "month2");
			projMap.put("drawCountSuitMonth2", drawCountSuitMonth2);
			Number drawCountSuitMonth3 = getSponsorT(listDrawingSuit, "month3");
			projMap.put("drawCountSuitMonth3", drawCountSuitMonth3);
			Number drawCountSuitQuarter = getSponsorT(listDrawingSuit, "quarterCount");
			projMap.put("drawCountSuitQuarter", drawCountSuitQuarter);
			
			outList.add(projMap);
			
			projMap = new HashMap<String, Object>(); 
			Number drawCountSheetMonth1 = getSponsorT(listDrawingSheet, "month1");
			projMap.put("drawCountSheetMonth1", drawCountSheetMonth1);
			Number drawCountSheetMonth2 = getSponsorT(listDrawingSheet, "month2");
			projMap.put("drawCountSheetMonth2", drawCountSheetMonth2);
			Number drawCountSheetMonth3 = getSponsorT(listDrawingSheet, "month3");
			projMap.put("drawCountSheetMonth3", drawCountSheetMonth3);
			Number drawCountSheetQuarter = getSponsorT(listDrawingSheet, "quarterCount");
			projMap.put("drawCountSheetQuarter", drawCountSheetQuarter);
			
			outList.add(projMap);
			
			projMap = new HashMap<String, Object>(); 
			Number fileCountSuitMonth1 = getSponsorT(listFileSuit, "month1");
			projMap.put("fileCountSuitMonth1", fileCountSuitMonth1);
			Number fileCountSuitMonth2 = getSponsorT(listFileSuit, "month2");
			projMap.put("fileCountSuitMonth2", fileCountSuitMonth2);
			Number fileCountSuitMonth3 = getSponsorT(listFileSuit, "month3");
			projMap.put("fileCountSuitMonth3", fileCountSuitMonth3);
			Number fileCountSuitQuarter = getSponsorT(listFileSuit, "quarterCount");
			projMap.put("fileCountSuitQuarter", fileCountSuitQuarter);
			
			outList.add(projMap);
			
			projMap = new HashMap<String, Object>(); 
			Number fileCountVolumeMonth1 = getSponsorT(listFileVolume, "month1");
			projMap.put("fileCountVolumeMonth1", fileCountVolumeMonth1);
			Number fileCountVolumeMonth2 = getSponsorT(listFileVolume, "month2");
			projMap.put("fileCountVolumeMonth2", fileCountVolumeMonth2);
			Number fileCountVolumeMonth3 = getSponsorT(listFileVolume, "month3");
			projMap.put("fileCountVolumeMonth3", fileCountVolumeMonth3);
			Number fileCountVolumeQuarter = getSponsorT(listFileVolume, "quarterCount");
			projMap.put("fileCountVolumeQuarter", fileCountVolumeQuarter);
			
			outList.add(projMap);
			                    
			mp.put("data", outList);
			mp.put("code", ActionContext.SUCESS);
			
		}catch(Exception ex) {
			ex.printStackTrace();
			mp.put("code", ActionContext.FAILURE);
		}
		
		return mp;
	}
	
	//档案作废
		@RequestMapping(value = "fileCancelWorkStatistic", method = RequestMethod.POST)
		@ResponseBody	
		public Map<String, Object> fileCancelWorkStatistic(@RequestBody String argStr){
			Map<String, Object> mp = new HashMap<String, Object>();
			List<Map<String, Object> > outList = new ArrayList<Map<String, Object>>();
			Map<String, Object> args = JSONUtils.stringToMap(argStr);
			
			String yearSelect = this.getStrValue(args, "yearSelect");
			String year = String.valueOf(Integer.parseInt(yearSelect.substring(0, 4)) + 1);
			String quarter = this.getStrValue(args, "quarterSelect");
			
			List<String> dateCondList = this.setQuarterCondition(year, quarter);
			
			String conditionDate1 = dateCondList.get(0);
			String conditionDate2 = dateCondList.get(1);
			String conditionDate3 = dateCondList.get(2);
			String conditionDate = dateCondList.get(3);
			
			try {
				Map<String, Object> projMap = new HashMap<String, Object>();
				
				String sqlDrawingSuit = "select distinct b.TYPE_NAME, b.SUB_TYPE, " + 
						"(select count(*) as cesetMonth1 from ecm_audit_general eag left join ecm_document ed on eag.DOC_ID = ed.ID where ed.TYPE_NAME = '设计文件' and ed.SUB_TYPE = '图纸' and ed.STATUS = '已作废' and ed.C_ITEM_TYPE = '案卷'"+ conditionDate1 +") as month1," + 
						"(select count(*) as cesetMonth2 from ecm_audit_general eag left join ecm_document ed on eag.DOC_ID = ed.ID where ed.TYPE_NAME = '设计文件' and ed.SUB_TYPE = '图纸' and ed.STATUS = '已作废' and ed.C_ITEM_TYPE = '案卷'"+ conditionDate2 +") as month2," + 
						"(select count(*) as cesetMonth3 from ecm_audit_general eag left join ecm_document ed on eag.DOC_ID = ed.ID where ed.TYPE_NAME = '设计文件' and ed.SUB_TYPE = '图纸' and ed.STATUS = '已作废' and ed.C_ITEM_TYPE = '案卷'"+ conditionDate3 +") as month3," + 
						"(select count(*) as cesetMonth from ecm_audit_general eag left join ecm_document ed on eag.DOC_ID = ed.ID where ed.TYPE_NAME = '设计文件' and ed.SUB_TYPE = '图纸' and ed.STATUS = '已作废' and ed.C_ITEM_TYPE = '案卷'"+ conditionDate +") as quarterCount  " + 
						"from ecm_audit_general a left join ecm_document b on a.DOC_ID = b.ID " + 
						"where b.TYPE_NAME = '设计文件' and b.SUB_TYPE = '图纸'";
				String sqlDrawingSheet = "select distinct b.TYPE_NAME, b.SUB_TYPE, " + 
						"(select sum(ed.C_PAGE_COUNT) as cesetMonth1 from ecm_audit_general eag left join ecm_document ed on eag.DOC_ID = ed.ID where ed.TYPE_NAME = '设计文件' and ed.SUB_TYPE = '图纸' and ed.STATUS = '已作废'"+ conditionDate1 +") as month1," + 
						"(select sum(ed.C_PAGE_COUNT) as cesetMonth2 from ecm_audit_general eag left join ecm_document ed on eag.DOC_ID = ed.ID where ed.TYPE_NAME = '设计文件' and ed.SUB_TYPE = '图纸' and ed.STATUS = '已作废'"+ conditionDate2 +") as month2," + 
						"(select sum(ed.C_PAGE_COUNT) as cesetMonth3 from ecm_audit_general eag left join ecm_document ed on eag.DOC_ID = ed.ID where ed.TYPE_NAME = '设计文件' and ed.SUB_TYPE = '图纸' and ed.STATUS = '已作废'"+ conditionDate3 +") as month3," + 
						"(select sum(ed.C_PAGE_COUNT) as cesetMonth from ecm_audit_general eag left join ecm_document ed on eag.DOC_ID = ed.ID where ed.TYPE_NAME = '设计文件' and ed.SUB_TYPE = '图纸' and ed.STATUS = '已作废'"+ conditionDate +") as quarterCount  " + 
						"from ecm_audit_general a left join ecm_document b on a.DOC_ID = b.ID " + 
						"where b.TYPE_NAME = '设计文件' and b.SUB_TYPE = '图纸'";
				String sqlFileSuit = "select distinct b.TYPE_NAME, b.SUB_TYPE, " + 
						"(select count(*) as cesetMonth1 from ecm_audit_general eag left join ecm_document ed on eag.DOC_ID = ed.ID where ed.TYPE_NAME = '设计文件' and ed.SUB_TYPE = '文件' and ed.STATUS = '已作废' and ed.C_ITEM_TYPE = '案卷'"+ conditionDate1 +") as month1," + 
						"(select count(*) as cesetMonth2 from ecm_audit_general eag left join ecm_document ed on eag.DOC_ID = ed.ID where ed.TYPE_NAME = '设计文件' and ed.SUB_TYPE = '文件' and ed.STATUS = '已作废' and ed.C_ITEM_TYPE = '案卷'"+ conditionDate2 +") as month2," + 
						"(select count(*) as cesetMonth3 from ecm_audit_general eag left join ecm_document ed on eag.DOC_ID = ed.ID where ed.TYPE_NAME = '设计文件' and ed.SUB_TYPE = '文件' and ed.STATUS = '已作废' and ed.C_ITEM_TYPE = '案卷'"+ conditionDate3 +") as month3," + 
						"(select count(*) as cesetMonth from ecm_audit_general eag left join ecm_document ed on eag.DOC_ID = ed.ID where ed.TYPE_NAME = '设计文件' and ed.SUB_TYPE = '文件' and ed.STATUS = '已作废' and ed.C_ITEM_TYPE = '案卷'"+ conditionDate +") as quarterCount  " + 
						"from ecm_audit_general a left join ecm_document b on a.DOC_ID = b.ID " + 
						"where b.TYPE_NAME = '设计文件' and b.SUB_TYPE = '文件'";
				String sqlFileVolume = "select distinct b.TYPE_NAME, b.SUB_TYPE, " + 
						"(select sum(ed.C_VOLUME_COUNT) as cesetMonth1 from ecm_audit_general eag left join ecm_document ed on eag.DOC_ID = ed.ID where ed.TYPE_NAME = '设计文件' and ed.SUB_TYPE = '文件' and ed.STATUS = '已作废'"+ conditionDate1 +") as month1," + 
						"(select sum(ed.C_VOLUME_COUNT) as cesetMonth2 from ecm_audit_general eag left join ecm_document ed on eag.DOC_ID = ed.ID where ed.TYPE_NAME = '设计文件' and ed.SUB_TYPE = '文件' and ed.STATUS = '已作废'"+ conditionDate2 +") as month2," + 
						"(select sum(ed.C_VOLUME_COUNT) as cesetMonth3 from ecm_audit_general eag left join ecm_document ed on eag.DOC_ID = ed.ID where ed.TYPE_NAME = '设计文件' and ed.SUB_TYPE = '文件' and ed.STATUS = '已作废'"+ conditionDate3 +") as month3," + 
						"(select sum(ed.C_VOLUME_COUNT) as cesetMonth from ecm_audit_general eag left join ecm_document ed on eag.DOC_ID = ed.ID where ed.TYPE_NAME = '设计文件' and ed.SUB_TYPE = '文件' and ed.STATUS = '已作废'"+ conditionDate +") as quarterCount  " + 
						"from ecm_audit_general a left join ecm_document b on a.DOC_ID = b.ID " + 
						"where b.TYPE_NAME = '设计文件' and b.SUB_TYPE = '文件'";
				
				List<Map<String, Object>> listDrawingSuit = documentService.getMapList(getToken(), sqlDrawingSuit);
				List<Map<String, Object>> listDrawingSheet = documentService.getMapList(getToken(), sqlDrawingSheet);
				List<Map<String, Object>> listFileSuit = documentService.getMapList(getToken(), sqlFileSuit);
				List<Map<String, Object>> listFileVolume = documentService.getMapList(getToken(), sqlFileVolume);
				
				Number drawCountSuitMonth1 = getSponsorT(listDrawingSuit, "month1");
				projMap.put("drawCountSuitMonth1", drawCountSuitMonth1);
				Number drawCountSuitMonth2 = getSponsorT(listDrawingSuit, "month2");
				projMap.put("drawCountSuitMonth2", drawCountSuitMonth2);
				Number drawCountSuitMonth3 = getSponsorT(listDrawingSuit, "month3");
				projMap.put("drawCountSuitMonth3", drawCountSuitMonth3);
				Number drawCountSuitQuarter = getSponsorT(listDrawingSuit, "quarterCount");
				projMap.put("drawCountSuitQuarter", drawCountSuitQuarter);
				
				outList.add(projMap);
				
				projMap = new HashMap<String, Object>(); 
				Number drawCountSheetMonth1 = getSponsorT(listDrawingSheet, "month1");
				projMap.put("drawCountSheetMonth1", drawCountSheetMonth1);
				Number drawCountSheetMonth2 = getSponsorT(listDrawingSheet, "month2");
				projMap.put("drawCountSheetMonth2", drawCountSheetMonth2);
				Number drawCountSheetMonth3 = getSponsorT(listDrawingSheet, "month3");
				projMap.put("drawCountSheetMonth3", drawCountSheetMonth3);
				Number drawCountSheetQuarter = getSponsorT(listDrawingSheet, "quarterCount");
				projMap.put("drawCountSheetQuarter", drawCountSheetQuarter);
				
				outList.add(projMap);
				
				projMap = new HashMap<String, Object>(); 
				Number fileCountSuitMonth1 = getSponsorT(listFileSuit, "month1");
				projMap.put("fileCountSuitMonth1", fileCountSuitMonth1);
				Number fileCountSuitMonth2 = getSponsorT(listFileSuit, "month2");
				projMap.put("fileCountSuitMonth2", fileCountSuitMonth2);
				Number fileCountSuitMonth3 = getSponsorT(listFileSuit, "month3");
				projMap.put("fileCountSuitMonth3", fileCountSuitMonth3);
				Number fileCountSuitQuarter = getSponsorT(listFileSuit, "quarterCount");
				projMap.put("fileCountSuitQuarter", fileCountSuitQuarter);
				
				outList.add(projMap);
				
				projMap = new HashMap<String, Object>(); 
				Number fileCountVolumeMonth1 = getSponsorT(listFileVolume, "month1");
				projMap.put("fileCountVolumeMonth1", fileCountVolumeMonth1);
				Number fileCountVolumeMonth2 = getSponsorT(listFileVolume, "month2");
				projMap.put("fileCountVolumeMonth2", fileCountVolumeMonth2);
				Number fileCountVolumeMonth3 = getSponsorT(listFileVolume, "month3");
				projMap.put("fileCountVolumeMonth3", fileCountVolumeMonth3);
				Number fileCountVolumeQuarter = getSponsorT(listFileVolume, "quarterCount");
				projMap.put("fileCountVolumeQuarter", fileCountVolumeQuarter);
				
				outList.add(projMap);
				                    
				mp.put("data", outList);
				mp.put("code", ActionContext.SUCESS);
				
			}catch(Exception ex) {
				ex.printStackTrace();
				mp.put("code", ActionContext.FAILURE);
			}
			
			return mp;
		}
	
	//非密档案利用情况
	@RequestMapping(value = "openFileWorkStatistic", method = RequestMethod.POST)
	@ResponseBody	
	public Map<String, Object> openFileWorkStatistic(@RequestBody String argStr){
		Map<String, Object> mp = new HashMap<String, Object>();
		List<Map<String, Object> > outList = new ArrayList<Map<String, Object>>();
		Map<String, Object> args = JSONUtils.stringToMap(argStr);
		
		String yearSelect = this.getStrValue(args, "yearSelect");
		String year = String.valueOf(Integer.parseInt(yearSelect.substring(0, 4)) + 1);
		String quarter = this.getStrValue(args, "quarterSelect");
		
		List<String> dateCondList = this.setQuarterCondition(year, quarter);
		
		String conditionDate1 = dateCondList.get(0);
		String conditionDate2 = dateCondList.get(1);
		String conditionDate3 = dateCondList.get(2);
		String conditionDate = dateCondList.get(3);
			
		try {
			Map<String, Object> projMap = new HashMap<String, Object>();
			
			String borrowSuitCount = "select distinct b.TYPE_NAME, " + 
					"(select count(*) as cesetMonth1 from ecm_audit_general eag left join ecm_document ed on eag.DOC_ID = ed.ID where ed.TYPE_NAME = '借阅单' and ed.C_ITEM_TYPE = '案卷'"+ conditionDate1 +") as month1," + 
					"(select count(*) as cesetMonth1 from ecm_audit_general eag left join ecm_document ed on eag.DOC_ID = ed.ID where ed.TYPE_NAME = '借阅单' and ed.C_ITEM_TYPE = '案卷'"+ conditionDate2 +") as month2," + 
					"(select count(*) as cesetMonth1 from ecm_audit_general eag left join ecm_document ed on eag.DOC_ID = ed.ID where ed.TYPE_NAME = '借阅单' and ed.C_ITEM_TYPE = '案卷'"+ conditionDate3 +") as month3," + 
					"(select count(*) as cesetMonth1 from ecm_audit_general eag left join ecm_document ed on eag.DOC_ID = ed.ID where ed.TYPE_NAME = '借阅单' and ed.C_ITEM_TYPE = '案卷'"+ conditionDate +") as quarterCount  " + 
					"from ecm_audit_general a left join ecm_document b on a.DOC_ID = b.ID " + 
					"where b.TYPE_NAME = '借阅单'";
			
			List<Map<String, Object>> borrowSuitCountStatistic = documentService.getMapList(getToken(), borrowSuitCount);
			
			Number borrowSuitMonth1 = getSponsorT(borrowSuitCountStatistic, "month1");
			projMap.put("borrowSuitMonth1", borrowSuitMonth1);
			Number borrowSuitMonth2 = getSponsorT(borrowSuitCountStatistic, "month2");
			projMap.put("borrowSuitMonth2", borrowSuitMonth2);
			Number borrowSuitMonth3 = getSponsorT(borrowSuitCountStatistic, "month3");
			projMap.put("borrowSuitMonth3", borrowSuitMonth3);
			Number borrowSuitQuarter = getSponsorT(borrowSuitCountStatistic, "quarterCount");
			projMap.put("borrowSuitQuarter", borrowSuitQuarter);
			
			outList.add(projMap);
			
			projMap = new HashMap<String, Object>();
			String borrowSheetCount = "select distinct b.TYPE_NAME, " + 
					"(select sum(ed.C_VOLUME_COUNT) as cesetMonth1 from ecm_audit_general eag left join ecm_document ed on eag.DOC_ID = ed.ID where ed.TYPE_NAME = '借阅单'"+ conditionDate1 +") as month1," + 
					"(select sum(ed.C_VOLUME_COUNT) as cesetMonth1 from ecm_audit_general eag left join ecm_document ed on eag.DOC_ID = ed.ID where ed.TYPE_NAME = '借阅单'"+ conditionDate2 +") as month2," + 
					"(select sum(ed.C_VOLUME_COUNT) as cesetMonth1 from ecm_audit_general eag left join ecm_document ed on eag.DOC_ID = ed.ID where ed.TYPE_NAME = '借阅单'"+ conditionDate3 +") as month3," + 
					"(select sum(ed.C_VOLUME_COUNT) as cesetMonth1 from ecm_audit_general eag left join ecm_document ed on eag.DOC_ID = ed.ID where ed.TYPE_NAME = '借阅单'"+ conditionDate +") as quarterCount  " + 
					"from ecm_audit_general a left join ecm_document b on a.DOC_ID = b.ID " + 
					"where b.TYPE_NAME = '借阅单'";
			
			List<Map<String, Object>> borrowSheetCountStatistic = documentService.getMapList(getToken(), borrowSheetCount);
			
			Number borrowSheetMonth1 = getSponsorT(borrowSheetCountStatistic, "month1");
			projMap.put("borrowSheetMonth1", borrowSheetMonth1);
			Number borrowSheetMonth2 = getSponsorT(borrowSheetCountStatistic, "month2");
			projMap.put("borrowSheetMonth2", borrowSheetMonth2);
			Number borrowSheetMonth3 = getSponsorT(borrowSheetCountStatistic, "month3");
			projMap.put("borrowSheetMonth3", borrowSheetMonth3);
			Number borrowSheetQuarter = getSponsorT(borrowSheetCountStatistic, "quarterCount");
			projMap.put("borrowSheetQuarter", borrowSheetQuarter);
			
			outList.add(projMap);
			
			projMap = new HashMap<String, Object>();
			String borrowPersonCount = "select distinct b.TYPE_NAME, " + 
					"(select count(*) as cesetMonth1 from ecm_audit_general eag left join ecm_document ed on eag.DOC_ID = ed.ID where ed.TYPE_NAME = '借阅单' and ed.C_ITEM_TYPE = '案卷'"+ conditionDate1 +") as month1," + 
					"(select count(*) as cesetMonth1 from ecm_audit_general eag left join ecm_document ed on eag.DOC_ID = ed.ID where ed.TYPE_NAME = '借阅单' and ed.C_ITEM_TYPE = '案卷'"+ conditionDate2 +") as month2," + 
					"(select count(*) as cesetMonth1 from ecm_audit_general eag left join ecm_document ed on eag.DOC_ID = ed.ID where ed.TYPE_NAME = '借阅单' and ed.C_ITEM_TYPE = '案卷'"+ conditionDate3 +") as month3," + 
					"(select count(*) as cesetMonth1 from ecm_audit_general eag left join ecm_document ed on eag.DOC_ID = ed.ID where ed.TYPE_NAME = '借阅单' and ed.C_ITEM_TYPE = '案卷'"+ conditionDate +") as quarterCount  " + 
					"from ecm_audit_general a left join ecm_document b on a.DOC_ID = b.ID " + 
					"where b.TYPE_NAME = '借阅单'";
			
			List<Map<String, Object>> borrowPersonCountStatistic = documentService.getMapList(getToken(), borrowPersonCount);
			
			Number borrowPersonMonth1 = getSponsorT(borrowPersonCountStatistic, "month1");
			projMap.put("borrowPersonMonth1", borrowPersonMonth1);
			Number borrowPersonMonth2 = getSponsorT(borrowPersonCountStatistic, "month2");
			projMap.put("borrowPersonMonth2", borrowPersonMonth2);
			Number borrowPersonMonth3 = getSponsorT(borrowPersonCountStatistic, "month3");
			projMap.put("borrowPersonMonth3", borrowPersonMonth3);
			Number borrowPersonQuarter = getSponsorT(borrowPersonCountStatistic, "quarterCount");
			projMap.put("borrowPersonQuarter", borrowPersonQuarter);
			
			outList.add(projMap);
			
			projMap = new HashMap<String, Object>();
			String borrowBackSuitCount = "select distinct b.TYPE_NAME, b.C_STORE_STATUS , " + 
					"(select count(*) as cesetMonth1 from ecm_audit_general eag left join ecm_document ed on eag.DOC_ID = ed.ID where ed.TYPE_NAME = '借阅单' and ed.C_STORE_STATUS = '在库' and ed.C_ITEM_TYPE = '案卷'"+ conditionDate1 +") as month1," + 
					"(select count(*) as cesetMonth1 from ecm_audit_general eag left join ecm_document ed on eag.DOC_ID = ed.ID where ed.TYPE_NAME = '借阅单' and ed.C_STORE_STATUS = '在库' and ed.C_ITEM_TYPE = '案卷'"+ conditionDate2 +") as month2," + 
					"(select count(*) as cesetMonth1 from ecm_audit_general eag left join ecm_document ed on eag.DOC_ID = ed.ID where ed.TYPE_NAME = '借阅单' and ed.C_STORE_STATUS = '在库' and ed.C_ITEM_TYPE = '案卷'"+ conditionDate3 +") as month3," + 
					"(select count(*) as cesetMonth1 from ecm_audit_general eag left join ecm_document ed on eag.DOC_ID = ed.ID where ed.TYPE_NAME = '借阅单' and ed.C_STORE_STATUS = '在库' and ed.C_ITEM_TYPE = '案卷'"+ conditionDate +") as quarterCount  " + 
					"from ecm_audit_general a left join ecm_document b on a.DOC_ID = b.ID " + 
					"where b.TYPE_NAME = '借阅单' and b.C_STORE_STATUS = '在库'";
			
			List<Map<String, Object>> borrowBackSuitCountStatistic = documentService.getMapList(getToken(), borrowBackSuitCount);
			
			Number borrowBackSuitMonth1 = getSponsorT(borrowBackSuitCountStatistic, "month1");
			projMap.put("borrowBackSuitMonth1", borrowBackSuitMonth1);
			Number borrowBackSuitMonth2 = getSponsorT(borrowBackSuitCountStatistic, "month2");
			projMap.put("borrowBackSuitMonth2", borrowBackSuitMonth2);
			Number borrowBackSuitMonth3 = getSponsorT(borrowBackSuitCountStatistic, "month3");
			projMap.put("borrowBackSuitMonth3", borrowBackSuitMonth3);
			Number borrowBackSuitQuarter = getSponsorT(borrowBackSuitCountStatistic, "quarterCount");
			projMap.put("borrowBackSuitQuarter", borrowBackSuitQuarter);
			
			outList.add(projMap);
			
			projMap = new HashMap<String, Object>();
			String borrowBackSheetCount = "select distinct b.TYPE_NAME, b.C_STORE_STATUS , " + 
					"(select sum(ed.C_VOLUME_COUNT) as cesetMonth1 from ecm_audit_general eag left join ecm_document ed on eag.DOC_ID = ed.ID where ed.TYPE_NAME = '借阅单' and ed.C_STORE_STATUS = '在库'"+ conditionDate1 +") as month1," + 
					"(select sum(ed.C_VOLUME_COUNT) as cesetMonth1 from ecm_audit_general eag left join ecm_document ed on eag.DOC_ID = ed.ID where ed.TYPE_NAME = '借阅单' and ed.C_STORE_STATUS = '在库'"+ conditionDate2 +") as month2," + 
					"(select sum(ed.C_VOLUME_COUNT) as cesetMonth1 from ecm_audit_general eag left join ecm_document ed on eag.DOC_ID = ed.ID where ed.TYPE_NAME = '借阅单' and ed.C_STORE_STATUS = '在库'"+ conditionDate3 +") as month3," + 
					"(select sum(ed.C_VOLUME_COUNT) as cesetMonth1 from ecm_audit_general eag left join ecm_document ed on eag.DOC_ID = ed.ID where ed.TYPE_NAME = '借阅单' and ed.C_STORE_STATUS = '在库'"+ conditionDate +") as quarterCount  " + 
					"from ecm_audit_general a left join ecm_document b on a.DOC_ID = b.ID " + 
					"where b.TYPE_NAME = '借阅单' and b.C_STORE_STATUS = '在库'";
			
			List<Map<String, Object>> borrowBackSheetCountStatistic = documentService.getMapList(getToken(), borrowBackSheetCount);
			
			Number borrowBackSheetMonth1 = getSponsorT(borrowBackSheetCountStatistic, "month1");
			projMap.put("borrowBackSheetMonth1", borrowBackSheetMonth1);
			Number borrowBackSheetMonth2 = getSponsorT(borrowBackSheetCountStatistic, "month2");
			projMap.put("borrowBackSheetMonth2", borrowBackSheetMonth2);
			Number borrowBackSheetMonth3 = getSponsorT(borrowBackSheetCountStatistic, "month3");
			projMap.put("borrowBackSheetMonth3", borrowBackSheetMonth3);
			Number borrowBackSheetQuarter = getSponsorT(borrowBackSheetCountStatistic, "quarterCount");
			projMap.put("borrowBackSheetQuarter", borrowBackSheetQuarter);
			
			outList.add(projMap);
			
			projMap = new HashMap<String, Object>();
			String borrowBackPersonCount = "select distinct b.TYPE_NAME, b.C_STORE_STATUS , " + 
					"(select count(*) as cesetMonth1 from ecm_audit_general eag left join ecm_document ed on eag.DOC_ID = ed.ID where ed.TYPE_NAME = '借阅单' and ed.C_STORE_STATUS = '在库'"+ conditionDate1 +") as month1," + 
					"(select count(*) as cesetMonth1 from ecm_audit_general eag left join ecm_document ed on eag.DOC_ID = ed.ID where ed.TYPE_NAME = '借阅单' and ed.C_STORE_STATUS = '在库'"+ conditionDate2 +") as month2," + 
					"(select count(*) as cesetMonth1 from ecm_audit_general eag left join ecm_document ed on eag.DOC_ID = ed.ID where ed.TYPE_NAME = '借阅单' and ed.C_STORE_STATUS = '在库'"+ conditionDate3 +") as month3," + 
					"(select count(*) as cesetMonth1 from ecm_audit_general eag left join ecm_document ed on eag.DOC_ID = ed.ID where ed.TYPE_NAME = '借阅单' and ed.C_STORE_STATUS = '在库'"+ conditionDate +") as quarterCount  " + 
					"from ecm_audit_general a left join ecm_document b on a.DOC_ID = b.ID " + 
					"where b.TYPE_NAME = '借阅单' and b.C_STORE_STATUS = '在库'";
			
			List<Map<String, Object>> borrowBackPersonCountStatistic = documentService.getMapList(getToken(), borrowBackPersonCount);
			
			Number borrowBackPersonMonth1 = getSponsorT(borrowBackPersonCountStatistic, "month1");
			projMap.put("borrowBackPersonMonth1", borrowBackPersonMonth1);
			Number borrowBackPersonMonth2 = getSponsorT(borrowBackPersonCountStatistic, "month2");
			projMap.put("borrowBackPersonMonth2", borrowBackPersonMonth2);
			Number borrowBackPersonMonth3 = getSponsorT(borrowBackPersonCountStatistic, "month3");
			projMap.put("borrowBackPersonMonth3", borrowBackPersonMonth3);
			Number borrowBackPersonQuarter = getSponsorT(borrowBackPersonCountStatistic, "quarterCount");
			projMap.put("borrowPersonQuarter", borrowBackPersonQuarter);
			
			outList.add(projMap);
			
			projMap = new HashMap<String, Object>();
			String copySuitCount = "select distinct b.TYPE_NAME, b.C_STORE_STATUS , " + 
					"(select count(*) as cesetMonth1 from ecm_audit_general eag left join ecm_document ed on eag.DOC_ID = ed.ID where ed.TYPE_NAME = '复制单' and ed.C_ITEM_TYPE = '案卷'"+ conditionDate1 +") as month1," + 
					"(select count(*) as cesetMonth1 from ecm_audit_general eag left join ecm_document ed on eag.DOC_ID = ed.ID where ed.TYPE_NAME = '复制单' and ed.C_ITEM_TYPE = '案卷'"+ conditionDate2 +") as month2," + 
					"(select count(*) as cesetMonth1 from ecm_audit_general eag left join ecm_document ed on eag.DOC_ID = ed.ID where ed.TYPE_NAME = '复制单' and ed.C_ITEM_TYPE = '案卷'"+ conditionDate3 +") as month3," + 
					"(select count(*) as cesetMonth1 from ecm_audit_general eag left join ecm_document ed on eag.DOC_ID = ed.ID where ed.TYPE_NAME = '复制单' and ed.C_ITEM_TYPE = '案卷'"+ conditionDate +") as quarterCount  " + 
					"from ecm_audit_general a left join ecm_document b on a.DOC_ID = b.ID " + 
					"where b.TYPE_NAME = '复制单'";
			
			List<Map<String, Object>> copySuitCountStatistic = documentService.getMapList(getToken(), copySuitCount);
			
			Number copySuitMonth1 = getSponsorT(copySuitCountStatistic, "month1");
			projMap.put("copySuitMonth1", copySuitMonth1);
			Number copySuitMonth2 = getSponsorT(copySuitCountStatistic, "month2");
			projMap.put("copySuitMonth2", copySuitMonth2);
			Number copySuitMonth3 = getSponsorT(copySuitCountStatistic, "month3");
			projMap.put("copySuitMonth3", copySuitMonth3);
			Number copySuitQuarter = getSponsorT(copySuitCountStatistic, "quarterCount");
			projMap.put("copySuitQuarter", copySuitQuarter);
			
			outList.add(projMap);
			
			projMap = new HashMap<String, Object>();
			String copySheetCount = "select distinct b.TYPE_NAME, b.C_STORE_STATUS , " + 
					"(select sum(ed.C_VOLUME_COUNT) as cesetMonth1 from ecm_audit_general eag left join ecm_document ed on eag.DOC_ID = ed.ID where ed.TYPE_NAME = '复制单'"+ conditionDate1 +") as month1," + 
					"(select sum(ed.C_VOLUME_COUNT) as cesetMonth1 from ecm_audit_general eag left join ecm_document ed on eag.DOC_ID = ed.ID where ed.TYPE_NAME = '复制单'"+ conditionDate2 +") as month2," + 
					"(select sum(ed.C_VOLUME_COUNT) as cesetMonth1 from ecm_audit_general eag left join ecm_document ed on eag.DOC_ID = ed.ID where ed.TYPE_NAME = '复制单'"+ conditionDate3 +") as month3," + 
					"(select sum(ed.C_VOLUME_COUNT) as cesetMonth1 from ecm_audit_general eag left join ecm_document ed on eag.DOC_ID = ed.ID where ed.TYPE_NAME = '复制单'"+ conditionDate +") as quarterCount  " + 
					"from ecm_audit_general a left join ecm_document b on a.DOC_ID = b.ID " + 
					"where b.TYPE_NAME = '复制单'";
			
			List<Map<String, Object>> copySheetCountStatistic = documentService.getMapList(getToken(), copySheetCount);
			
			Number copySheetMonth1 = getSponsorT(copySheetCountStatistic, "month1");
			projMap.put("copySheetMonth1", copySheetMonth1);
			Number copySheetMonth2 = getSponsorT(copySheetCountStatistic, "month2");
			projMap.put("copySheetMonth2", copySheetMonth2);
			Number copySheetMonth3 = getSponsorT(copySheetCountStatistic, "month3");
			projMap.put("copySheetMonth3", copySheetMonth3);
			Number copySheetQuarter = getSponsorT(copySheetCountStatistic, "quarterCount");
			projMap.put("copySheetQuarter", copySheetQuarter);
			
			outList.add(projMap);
			
			projMap = new HashMap<String, Object>();
			String copyPageCount = "select distinct b.TYPE_NAME, b.C_STORE_STATUS , " + 
					"(select sum(ed.C_PAGE_COUNT) as cesetMonth1 from ecm_audit_general eag left join ecm_document ed on eag.DOC_ID = ed.ID where ed.TYPE_NAME = '复制单'"+ conditionDate1 +") as month1," + 
					"(select sum(ed.C_PAGE_COUNT) as cesetMonth1 from ecm_audit_general eag left join ecm_document ed on eag.DOC_ID = ed.ID where ed.TYPE_NAME = '复制单'"+ conditionDate2 +") as month2," + 
					"(select sum(ed.C_PAGE_COUNT) as cesetMonth1 from ecm_audit_general eag left join ecm_document ed on eag.DOC_ID = ed.ID where ed.TYPE_NAME = '复制单'"+ conditionDate3 +") as month3," + 
					"(select sum(ed.C_PAGE_COUNT) as cesetMonth1 from ecm_audit_general eag left join ecm_document ed on eag.DOC_ID = ed.ID where ed.TYPE_NAME = '复制单'"+ conditionDate +") as quarterCount  " + 
					"from ecm_audit_general a left join ecm_document b on a.DOC_ID = b.ID " + 
					"where b.TYPE_NAME = '复制单'";
			
			List<Map<String, Object>> copyPageCountStatistic = documentService.getMapList(getToken(), copyPageCount);
			
			Number copyPageMonth1 = getSponsorT(copyPageCountStatistic, "month1");
			projMap.put("copyPageMonth1", copyPageMonth1);
			Number copyPageMonth2 = getSponsorT(copyPageCountStatistic, "month2");
			projMap.put("copyPageMonth2", copyPageMonth2);
			Number copyPageMonth3 = getSponsorT(copyPageCountStatistic, "month3");
			projMap.put("copyPageMonth3", copyPageMonth3);
			Number copyPageQuarter = getSponsorT(copyPageCountStatistic, "quarterCount");
			projMap.put("copyPageQuarter", copyPageQuarter);
			
			outList.add(projMap);
			
			projMap = new HashMap<String, Object>();
			String copyPersonCount = "select distinct b.TYPE_NAME, b.C_STORE_STATUS , " + 
					"(select count(*) as cesetMonth1 from ecm_audit_general eag left join ecm_document ed on eag.DOC_ID = ed.ID where ed.TYPE_NAME = '复制单'"+ conditionDate1 +") as month1," + 
					"(select count(*) as cesetMonth1 from ecm_audit_general eag left join ecm_document ed on eag.DOC_ID = ed.ID where ed.TYPE_NAME = '复制单'"+ conditionDate2 +") as month2," + 
					"(select count(*) as cesetMonth1 from ecm_audit_general eag left join ecm_document ed on eag.DOC_ID = ed.ID where ed.TYPE_NAME = '复制单'"+ conditionDate3 +") as month3," + 
					"(select count(*) as cesetMonth1 from ecm_audit_general eag left join ecm_document ed on eag.DOC_ID = ed.ID where ed.TYPE_NAME = '复制单'"+ conditionDate +") as quarterCount  " + 
					"from ecm_audit_general a left join ecm_document b on a.DOC_ID = b.ID " + 
					"where b.TYPE_NAME = '复制单'";
			
			List<Map<String, Object>> copyPersonCountStatistic = documentService.getMapList(getToken(), copyPersonCount);
			
			Number copyPersonMonth1 = getSponsorT(copyPersonCountStatistic, "month1");
			projMap.put("copyPersonMonth1", copyPersonMonth1);
			Number copyPersonMonth2 = getSponsorT(copyPersonCountStatistic, "month2");
			projMap.put("copyPersonMonth2", copyPersonMonth2);
			Number copyPersonMonth3 = getSponsorT(copyPersonCountStatistic, "month3");
			projMap.put("copyPersonMonth3", copyPersonMonth3);
			Number copyPersonQuarter = getSponsorT(copyPersonCountStatistic, "quarterCount");
			projMap.put("copyPersonQuarter", copyPersonQuarter);
			
			outList.add(projMap);
			
			projMap = new HashMap<String, Object>();
			String copyBackSuitCount = "select distinct b.TYPE_NAME, b.C_STORE_STATUS , " + 
					"(select count(*) as cesetMonth1 from ecm_audit_general eag left join ecm_document ed on eag.DOC_ID = ed.ID where ed.TYPE_NAME = '复制单' and ed.C_STORE_STATUS = '在库' and ed.C_ITEM_TYPE = '案卷'"+ conditionDate1 +") as month1," + 
					"(select count(*) as cesetMonth1 from ecm_audit_general eag left join ecm_document ed on eag.DOC_ID = ed.ID where ed.TYPE_NAME = '复制单' and ed.C_STORE_STATUS = '在库' and ed.C_ITEM_TYPE = '案卷'"+ conditionDate2 +") as month2," + 
					"(select count(*) as cesetMonth1 from ecm_audit_general eag left join ecm_document ed on eag.DOC_ID = ed.ID where ed.TYPE_NAME = '复制单' and ed.C_STORE_STATUS = '在库' and ed.C_ITEM_TYPE = '案卷'"+ conditionDate3 +") as month3," + 
					"(select count(*) as cesetMonth1 from ecm_audit_general eag left join ecm_document ed on eag.DOC_ID = ed.ID where ed.TYPE_NAME = '复制单' and ed.C_STORE_STATUS = '在库' and ed.C_ITEM_TYPE = '案卷'"+ conditionDate +") as quarterCount  " + 
					"from ecm_audit_general a left join ecm_document b on a.DOC_ID = b.ID " + 
					"where b.TYPE_NAME = '复制单' and b.C_STORE_STATUS = '在库'";
			
			List<Map<String, Object>> copyBackSuitCountStatistic = documentService.getMapList(getToken(), copyBackSuitCount);
			
			Number copyBackSuitMonth1 = getSponsorT(copyBackSuitCountStatistic, "month1");
			projMap.put("copyBackSuitMonth1", copyBackSuitMonth1);
			Number copyBackSuitMonth2 = getSponsorT(copyBackSuitCountStatistic, "month2");
			projMap.put("copyBackSuitMonth2", copyBackSuitMonth2);
			Number copyBackSuitMonth3 = getSponsorT(copyBackSuitCountStatistic, "month3");
			projMap.put("copyBackSuitMonth3", copyBackSuitMonth3);
			Number copyBackSuitQuarter = getSponsorT(copyBackSuitCountStatistic, "quarterCount");
			projMap.put("copyBackSuitQuarter", copyBackSuitQuarter);
			
			outList.add(projMap);
			
			projMap = new HashMap<String, Object>();
			String copyBackSheetCount = "select distinct b.TYPE_NAME, b.C_STORE_STATUS , " + 
					"(select sum(ed.C_VOLUME_COUNT) as cesetMonth1 from ecm_audit_general eag left join ecm_document ed on eag.DOC_ID = ed.ID where ed.TYPE_NAME = '复制单' and ed.C_STORE_STATUS = '在库'"+ conditionDate1 +") as month1," + 
					"(select sum(ed.C_VOLUME_COUNT) as cesetMonth1 from ecm_audit_general eag left join ecm_document ed on eag.DOC_ID = ed.ID where ed.TYPE_NAME = '复制单' and ed.C_STORE_STATUS = '在库'"+ conditionDate2 +") as month2," + 
					"(select sum(ed.C_VOLUME_COUNT) as cesetMonth1 from ecm_audit_general eag left join ecm_document ed on eag.DOC_ID = ed.ID where ed.TYPE_NAME = '复制单' and ed.C_STORE_STATUS = '在库'"+ conditionDate3 +") as month3," + 
					"(select sum(ed.C_VOLUME_COUNT) as cesetMonth1 from ecm_audit_general eag left join ecm_document ed on eag.DOC_ID = ed.ID where ed.TYPE_NAME = '复制单' and ed.C_STORE_STATUS = '在库'"+ conditionDate +") as quarterCount  " + 
					"from ecm_audit_general a left join ecm_document b on a.DOC_ID = b.ID " + 
					"where b.TYPE_NAME = '复制单' and b.C_STORE_STATUS = '在库'";
			
			List<Map<String, Object>> copyBackSheetCountStatistic = documentService.getMapList(getToken(), copyBackSheetCount);
			
			Number copyBackSheetMonth1 = getSponsorT(copyBackSheetCountStatistic, "month1");
			projMap.put("copyBackSheetMonth1", copyBackSheetMonth1);
			Number copyBackSheetMonth2 = getSponsorT(copyBackSheetCountStatistic, "month2");
			projMap.put("copyBackSheetMonth2", copyBackSheetMonth2);
			Number copyBackSheetMonth3 = getSponsorT(copyBackSheetCountStatistic, "month3");
			projMap.put("copyBackSheetMonth3", copyBackSheetMonth3);
			Number copyBackSheetQuarter = getSponsorT(copyBackSheetCountStatistic, "quarterCount");
			projMap.put("copyBackSheetQuarter", copyBackSheetQuarter);
			
			outList.add(projMap);
			
			projMap = new HashMap<String, Object>();
			String copyBackPageCount = "select distinct b.TYPE_NAME, b.C_STORE_STATUS , " + 
					"(select sum(ed.C_PAGE_COUNT) as cesetMonth1 from ecm_audit_general eag left join ecm_document ed on eag.DOC_ID = ed.ID where ed.TYPE_NAME = '复制单' and ed.C_STORE_STATUS = '在库'"+ conditionDate1 +") as month1," + 
					"(select sum(ed.C_PAGE_COUNT) as cesetMonth1 from ecm_audit_general eag left join ecm_document ed on eag.DOC_ID = ed.ID where ed.TYPE_NAME = '复制单' and ed.C_STORE_STATUS = '在库'"+ conditionDate2 +") as month2," + 
					"(select sum(ed.C_PAGE_COUNT) as cesetMonth1 from ecm_audit_general eag left join ecm_document ed on eag.DOC_ID = ed.ID where ed.TYPE_NAME = '复制单' and ed.C_STORE_STATUS = '在库'"+ conditionDate3 +") as month3," + 
					"(select sum(ed.C_PAGE_COUNT) as cesetMonth1 from ecm_audit_general eag left join ecm_document ed on eag.DOC_ID = ed.ID where ed.TYPE_NAME = '复制单' and ed.C_STORE_STATUS = '在库'"+ conditionDate +") as quarterCount  " + 
					"from ecm_audit_general a left join ecm_document b on a.DOC_ID = b.ID " + 
					"where b.TYPE_NAME = '复制单' and b.C_STORE_STATUS = '在库'";
			
			List<Map<String, Object>> copyBackPageCountStatistic = documentService.getMapList(getToken(), copyBackPageCount);
			
			Number copyBackPageMonth1 = getSponsorT(copyBackPageCountStatistic, "month1");
			projMap.put("copyBackPageMonth1", copyBackPageMonth1);
			Number copyBackPageMonth2 = getSponsorT(copyBackPageCountStatistic, "month2");
			projMap.put("copyBackPageMonth2", copyBackPageMonth2);
			Number copyBackPageMonth3 = getSponsorT(copyBackPageCountStatistic, "month3");
			projMap.put("copyBackPageMonth3", copyBackPageMonth3);
			Number copyBackPageQuarter = getSponsorT(copyBackPageCountStatistic, "quarterCount");
			projMap.put("copyBackPageQuarter", copyBackPageQuarter);
			
			outList.add(projMap);
			
			projMap = new HashMap<String, Object>();
			String copyBackPersonCount = "select distinct b.TYPE_NAME, b.C_STORE_STATUS , " + 
					"(select count(*) as cesetMonth1 from ecm_audit_general eag left join ecm_document ed on eag.DOC_ID = ed.ID where ed.TYPE_NAME = '复制单' and ed.C_STORE_STATUS = '在库'"+ conditionDate1 +") as month1," + 
					"(select count(*) as cesetMonth1 from ecm_audit_general eag left join ecm_document ed on eag.DOC_ID = ed.ID where ed.TYPE_NAME = '复制单' and ed.C_STORE_STATUS = '在库'"+ conditionDate2 +") as month2," + 
					"(select count(*) as cesetMonth1 from ecm_audit_general eag left join ecm_document ed on eag.DOC_ID = ed.ID where ed.TYPE_NAME = '复制单' and ed.C_STORE_STATUS = '在库'"+ conditionDate3 +") as month3," + 
					"(select count(*) as cesetMonth1 from ecm_audit_general eag left join ecm_document ed on eag.DOC_ID = ed.ID where ed.TYPE_NAME = '复制单' and ed.C_STORE_STATUS = '在库'"+ conditionDate +") as quarterCount  " + 
					"from ecm_audit_general a left join ecm_document b on a.DOC_ID = b.ID " + 
					"where b.TYPE_NAME = '复制单' and b.C_STORE_STATUS = '在库'";
			
			List<Map<String, Object>> copyBackPersonCountStatistic = documentService.getMapList(getToken(), copyBackPersonCount);
			
			Number copyBackPersonMonth1 = getSponsorT(copyBackPersonCountStatistic, "month1");
			projMap.put("copyBackPersonMonth1", copyBackPersonMonth1);
			Number copyBackPersonMonth2 = getSponsorT(copyBackPersonCountStatistic, "month2");
			projMap.put("copyBackPersonMonth2", copyBackPersonMonth2);
			Number copyBackPersonMonth3 = getSponsorT(copyBackPersonCountStatistic, "month3");
			projMap.put("copyBackPersonMonth3", copyBackPersonMonth3);
			Number copyBackPersonQuarter = getSponsorT(copyBackPersonCountStatistic, "quarterCount");
			projMap.put("copyBackPersonQuarter", copyBackPersonQuarter);
			
			outList.add(projMap);
			
			projMap = new HashMap<String, Object>();
			String changeSuitCount = "select distinct b.TYPE_NAME, b.C_STORE_STATUS , " + 
					"(select count(*) as cesetMonth1 from ecm_audit_general eag left join ecm_document ed on eag.DOC_ID = ed.ID where ed.TYPE_NAME = '科研文件修改单' and ed.C_ITEM_TYPE = '案卷'"+ conditionDate1 +") as month1," + 
					"(select count(*) as cesetMonth1 from ecm_audit_general eag left join ecm_document ed on eag.DOC_ID = ed.ID where ed.TYPE_NAME = '科研文件修改单' and ed.C_ITEM_TYPE = '案卷'"+ conditionDate2 +") as month2," + 
					"(select count(*) as cesetMonth1 from ecm_audit_general eag left join ecm_document ed on eag.DOC_ID = ed.ID where ed.TYPE_NAME = '科研文件修改单' and ed.C_ITEM_TYPE = '案卷'"+ conditionDate3 +") as month3," + 
					"(select count(*) as cesetMonth1 from ecm_audit_general eag left join ecm_document ed on eag.DOC_ID = ed.ID where ed.TYPE_NAME = '科研文件修改单' and ed.C_ITEM_TYPE = '案卷'"+ conditionDate +") as quarterCount  " + 
					"from ecm_audit_general a left join ecm_document b on a.DOC_ID = b.ID " + 
					"where b.TYPE_NAME = '科研文件修改单'";
			
			List<Map<String, Object>> changeSuitCountStatistic = documentService.getMapList(getToken(), changeSuitCount);
			
			Number changeSuitMonth1 = getSponsorT(changeSuitCountStatistic, "month1");
			projMap.put("changeSuitMonth1", changeSuitMonth1);
			Number changeSuitMonth2 = getSponsorT(changeSuitCountStatistic, "month2");
			projMap.put("changeSuitMonth2", changeSuitMonth2);
			Number changeSuitMonth3 = getSponsorT(changeSuitCountStatistic, "month3");
			projMap.put("changeSuitMonth3", changeSuitMonth3);
			Number changeSuitQuarter = getSponsorT(changeSuitCountStatistic, "quarterCount");
			projMap.put("changeSuitQuarter", changeSuitQuarter);
			
			outList.add(projMap);
			
			projMap = new HashMap<String, Object>();
			String changeSheetCount = "select distinct b.TYPE_NAME, b.C_STORE_STATUS , " + 
					"(select sum(ed.C_VOLUME_COUNT) as cesetMonth1 from ecm_audit_general eag left join ecm_document ed on eag.DOC_ID = ed.ID where ed.TYPE_NAME = '科研文件修改单'"+ conditionDate1 +") as month1," + 
					"(select sum(ed.C_VOLUME_COUNT) as cesetMonth1 from ecm_audit_general eag left join ecm_document ed on eag.DOC_ID = ed.ID where ed.TYPE_NAME = '科研文件修改单'"+ conditionDate2 +") as month2," + 
					"(select sum(ed.C_VOLUME_COUNT) as cesetMonth1 from ecm_audit_general eag left join ecm_document ed on eag.DOC_ID = ed.ID where ed.TYPE_NAME = '科研文件修改单'"+ conditionDate3 +") as month3," + 
					"(select sum(ed.C_VOLUME_COUNT) as cesetMonth1 from ecm_audit_general eag left join ecm_document ed on eag.DOC_ID = ed.ID where ed.TYPE_NAME = '科研文件修改单'"+ conditionDate +") as quarterCount  " + 
					"from ecm_audit_general a left join ecm_document b on a.DOC_ID = b.ID " + 
					"where b.TYPE_NAME = '科研文件修改单'";
			
			List<Map<String, Object>> changeSheetCountStatistic = documentService.getMapList(getToken(), changeSheetCount);
			
			Number changeSheetMonth1 = getSponsorT(changeSheetCountStatistic, "month1");
			projMap.put("changeSheetMonth1", changeSheetMonth1);
			Number changeSheetMonth2 = getSponsorT(changeSheetCountStatistic, "month2");
			projMap.put("changeSheetMonth2", changeSheetMonth2);
			Number changeSheetMonth3 = getSponsorT(changeSheetCountStatistic, "month3");
			projMap.put("changeSheetMonth3", changeSheetMonth3);
			Number changeSheetQuarter = getSponsorT(changeSheetCountStatistic, "quarterCount");
			projMap.put("changeSheetQuarter", changeSheetQuarter);
			
			outList.add(projMap);
			
			projMap = new HashMap<String, Object>();
			String changePageCount = "select distinct b.TYPE_NAME, b.C_STORE_STATUS , " + 
					"(select sum(ed.C_PAGE_COUNT) as cesetMonth1 from ecm_audit_general eag left join ecm_document ed on eag.DOC_ID = ed.ID where ed.TYPE_NAME = '科研文件修改单'"+ conditionDate1 +") as month1," + 
					"(select sum(ed.C_PAGE_COUNT) as cesetMonth1 from ecm_audit_general eag left join ecm_document ed on eag.DOC_ID = ed.ID where ed.TYPE_NAME = '科研文件修改单'"+ conditionDate2 +") as month2," + 
					"(select sum(ed.C_PAGE_COUNT) as cesetMonth1 from ecm_audit_general eag left join ecm_document ed on eag.DOC_ID = ed.ID where ed.TYPE_NAME = '科研文件修改单'"+ conditionDate3 +") as month3," + 
					"(select sum(ed.C_PAGE_COUNT) as cesetMonth1 from ecm_audit_general eag left join ecm_document ed on eag.DOC_ID = ed.ID where ed.TYPE_NAME = '科研文件修改单'"+ conditionDate +") as quarterCount  " + 
					"from ecm_audit_general a left join ecm_document b on a.DOC_ID = b.ID " + 
					"where b.TYPE_NAME = '科研文件修改单'";
			
			List<Map<String, Object>> changePageCountStatistic = documentService.getMapList(getToken(), changePageCount);
			
			Number changePageMonth1 = getSponsorT(changePageCountStatistic, "month1");
			projMap.put("changePageMonth1", changePageMonth1);
			Number changePageMonth2 = getSponsorT(changePageCountStatistic, "month2");
			projMap.put("changePageMonth2", changePageMonth2);
			Number changePageMonth3 = getSponsorT(changePageCountStatistic, "month3");
			projMap.put("changePageMonth3", changePageMonth3);
			Number changePageQuarter = getSponsorT(changePageCountStatistic, "quarterCount");
			projMap.put("changePageQuarter", changePageQuarter);
			
			outList.add(projMap);
			
			projMap = new HashMap<String, Object>();
			String changePersonCount = "select distinct b.TYPE_NAME, b.C_STORE_STATUS , " + 
					"(select count(*) as cesetMonth1 from ecm_audit_general eag left join ecm_document ed on eag.DOC_ID = ed.ID where ed.TYPE_NAME = '科研文件修改单'"+ conditionDate1 +") as month1," + 
					"(select count(*) as cesetMonth1 from ecm_audit_general eag left join ecm_document ed on eag.DOC_ID = ed.ID where ed.TYPE_NAME = '科研文件修改单'"+ conditionDate2 +") as month2," + 
					"(select count(*) as cesetMonth1 from ecm_audit_general eag left join ecm_document ed on eag.DOC_ID = ed.ID where ed.TYPE_NAME = '科研文件修改单'"+ conditionDate3 +") as month3," + 
					"(select count(*) as cesetMonth1 from ecm_audit_general eag left join ecm_document ed on eag.DOC_ID = ed.ID where ed.TYPE_NAME = '科研文件修改单'"+ conditionDate +") as quarterCount  " + 
					"from ecm_audit_general a left join ecm_document b on a.DOC_ID = b.ID " + 
					"where b.TYPE_NAME = '科研文件修改单'";
			
			List<Map<String, Object>> changePersonCountStatistic = documentService.getMapList(getToken(), changePersonCount);
			
			Number changePersonMonth1 = getSponsorT(changePersonCountStatistic, "month1");
			projMap.put("changePersonMonth1", changePersonMonth1);
			Number changePersonMonth2 = getSponsorT(changePersonCountStatistic, "month2");
			projMap.put("changePersonMonth2", changePersonMonth2);
			Number changePersonMonth3 = getSponsorT(changePersonCountStatistic, "month3");
			projMap.put("changePersonMonth3", changePersonMonth3);
			Number changePersonQuarter = getSponsorT(changePersonCountStatistic, "quarterCount");
			projMap.put("changePersonQuarter", changePersonQuarter);
			
			outList.add(projMap);
			
			projMap = new HashMap<String, Object>();
			String changeBackSuitCount = "select distinct b.TYPE_NAME, b.C_STORE_STATUS , " + 
					"(select count(*) as cesetMonth1 from ecm_audit_general eag left join ecm_document ed on eag.DOC_ID = ed.ID where ed.TYPE_NAME = '科研文件修改单' and ed.C_STORE_STATUS = '在库' and ed.C_ITEM_TYPE = '案卷'"+ conditionDate1 +") as month1," + 
					"(select count(*) as cesetMonth1 from ecm_audit_general eag left join ecm_document ed on eag.DOC_ID = ed.ID where ed.TYPE_NAME = '科研文件修改单' and ed.C_STORE_STATUS = '在库' and ed.C_ITEM_TYPE = '案卷'"+ conditionDate2 +") as month2," + 
					"(select count(*) as cesetMonth1 from ecm_audit_general eag left join ecm_document ed on eag.DOC_ID = ed.ID where ed.TYPE_NAME = '科研文件修改单' and ed.C_STORE_STATUS = '在库' and ed.C_ITEM_TYPE = '案卷'"+ conditionDate3 +") as month3," + 
					"(select count(*) as cesetMonth1 from ecm_audit_general eag left join ecm_document ed on eag.DOC_ID = ed.ID where ed.TYPE_NAME = '科研文件修改单' and ed.C_STORE_STATUS = '在库' and ed.C_ITEM_TYPE = '案卷'"+ conditionDate +") as quarterCount  " + 
					"from ecm_audit_general a left join ecm_document b on a.DOC_ID = b.ID " + 
					"where b.TYPE_NAME = '科研文件修改单' and b.C_STORE_STATUS = '在库'";
			
			List<Map<String, Object>> changeBackSuitCountStatistic = documentService.getMapList(getToken(), changeBackSuitCount);
			
			Number changeBackSuitMonth1 = getSponsorT(changeBackSuitCountStatistic, "month1");
			projMap.put("changeBackSuitMonth1", changeBackSuitMonth1);
			Number changeBackSuitMonth2 = getSponsorT(changeBackSuitCountStatistic, "month2");
			projMap.put("changeBackSuitMonth2", changeBackSuitMonth2);
			Number changeBackSuitMonth3 = getSponsorT(changeBackSuitCountStatistic, "month3");
			projMap.put("changeBackSuitMonth3", changeBackSuitMonth3);
			Number changeBackSuitQuarter = getSponsorT(changeBackSuitCountStatistic, "quarterCount");
			projMap.put("changeBackSuitQuarter", changeBackSuitQuarter);
			
			outList.add(projMap);
			
			projMap = new HashMap<String, Object>();
			String changeBackSheetCount = "select distinct b.TYPE_NAME, b.C_STORE_STATUS , " + 
					"(select sum(ed.C_VOLUME_COUNT) as cesetMonth1 from ecm_audit_general eag left join ecm_document ed on eag.DOC_ID = ed.ID where ed.TYPE_NAME = '科研文件修改单' and ed.C_STORE_STATUS = '在库'"+ conditionDate1 +") as month1," + 
					"(select sum(ed.C_VOLUME_COUNT) as cesetMonth1 from ecm_audit_general eag left join ecm_document ed on eag.DOC_ID = ed.ID where ed.TYPE_NAME = '科研文件修改单' and ed.C_STORE_STATUS = '在库'"+ conditionDate2 +") as month2," + 
					"(select sum(ed.C_VOLUME_COUNT) as cesetMonth1 from ecm_audit_general eag left join ecm_document ed on eag.DOC_ID = ed.ID where ed.TYPE_NAME = '科研文件修改单' and ed.C_STORE_STATUS = '在库'"+ conditionDate3 +") as month3," + 
					"(select sum(ed.C_VOLUME_COUNT) as cesetMonth1 from ecm_audit_general eag left join ecm_document ed on eag.DOC_ID = ed.ID where ed.TYPE_NAME = '科研文件修改单' and ed.C_STORE_STATUS = '在库'"+ conditionDate +") as quarterCount  " + 
					"from ecm_audit_general a left join ecm_document b on a.DOC_ID = b.ID " + 
					"where b.TYPE_NAME = '科研文件修改单' and b.C_STORE_STATUS = '在库'";
			
			List<Map<String, Object>> changeBackSheetCountStatistic = documentService.getMapList(getToken(), changeBackSheetCount);
			
			Number changeBackSheetMonth1 = getSponsorT(changeBackSheetCountStatistic, "month1");
			projMap.put("changeBackSheetMonth1", changeBackSheetMonth1);
			Number changeBackSheetMonth2 = getSponsorT(changeBackSheetCountStatistic, "month2");
			projMap.put("changeBackSheetMonth2", changeBackSheetMonth2);
			Number changeBackSheetMonth3 = getSponsorT(changeBackSheetCountStatistic, "month3");
			projMap.put("changeBackSheetMonth3", changeBackSheetMonth3);
			Number changeBackSheetQuarter = getSponsorT(changeBackSheetCountStatistic, "quarterCount");
			projMap.put("changeBackSheetQuarter", changeBackSheetQuarter);
			
			outList.add(projMap);
			
			projMap = new HashMap<String, Object>();
			String changeBackPageCount = "select distinct b.TYPE_NAME, b.C_STORE_STATUS , " + 
					"(select sum(ed.C_PAGE_COUNT) as cesetMonth1 from ecm_audit_general eag left join ecm_document ed on eag.DOC_ID = ed.ID where ed.TYPE_NAME = '科研文件修改单' and ed.C_STORE_STATUS = '在库'"+ conditionDate1 +") as month1," + 
					"(select sum(ed.C_PAGE_COUNT) as cesetMonth1 from ecm_audit_general eag left join ecm_document ed on eag.DOC_ID = ed.ID where ed.TYPE_NAME = '科研文件修改单' and ed.C_STORE_STATUS = '在库'"+ conditionDate2 +") as month2," + 
					"(select sum(ed.C_PAGE_COUNT) as cesetMonth1 from ecm_audit_general eag left join ecm_document ed on eag.DOC_ID = ed.ID where ed.TYPE_NAME = '科研文件修改单' and ed.C_STORE_STATUS = '在库'"+ conditionDate3 +") as month3," + 
					"(select sum(ed.C_PAGE_COUNT) as cesetMonth1 from ecm_audit_general eag left join ecm_document ed on eag.DOC_ID = ed.ID where ed.TYPE_NAME = '科研文件修改单' and ed.C_STORE_STATUS = '在库'"+ conditionDate +") as quarterCount  " + 
					"from ecm_audit_general a left join ecm_document b on a.DOC_ID = b.ID " + 
					"where b.TYPE_NAME = '科研文件修改单' and b.C_STORE_STATUS = '在库'";
			
			List<Map<String, Object>> changeBackPageCountStatistic = documentService.getMapList(getToken(), changeBackPageCount);
			
			Number changeBackPageMonth1 = getSponsorT(changeBackPageCountStatistic, "month1");
			projMap.put("changeBackPageMonth1", changeBackPageMonth1);
			Number changeBackPageMonth2 = getSponsorT(changeBackPageCountStatistic, "month2");
			projMap.put("changeBackPageMonth2", changeBackPageMonth2);
			Number changeBackPageMonth3 = getSponsorT(changeBackPageCountStatistic, "month3");
			projMap.put("changeBackPageMonth3", changeBackPageMonth3);
			Number changeBackPageQuarter = getSponsorT(changeBackPageCountStatistic, "quarterCount");
			projMap.put("changeBackPageQuarter", changeBackPageQuarter);
			
			outList.add(projMap);
			
			projMap = new HashMap<String, Object>();
			String changeBackPersonCount = "select distinct b.TYPE_NAME, b.C_STORE_STATUS , " + 
					"(select count(*) as cesetMonth1 from ecm_audit_general eag left join ecm_document ed on eag.DOC_ID = ed.ID where ed.TYPE_NAME = '科研文件修改单' and ed.C_STORE_STATUS = '在库'"+ conditionDate1 +") as month1," + 
					"(select count(*) as cesetMonth1 from ecm_audit_general eag left join ecm_document ed on eag.DOC_ID = ed.ID where ed.TYPE_NAME = '科研文件修改单' and ed.C_STORE_STATUS = '在库'"+ conditionDate2 +") as month2," + 
					"(select count(*) as cesetMonth1 from ecm_audit_general eag left join ecm_document ed on eag.DOC_ID = ed.ID where ed.TYPE_NAME = '科研文件修改单' and ed.C_STORE_STATUS = '在库'"+ conditionDate3 +") as month3," + 
					"(select count(*) as cesetMonth1 from ecm_audit_general eag left join ecm_document ed on eag.DOC_ID = ed.ID where ed.TYPE_NAME = '科研文件修改单' and ed.C_STORE_STATUS = '在库'"+ conditionDate +") as quarterCount  " + 
					"from ecm_audit_general a left join ecm_document b on a.DOC_ID = b.ID " + 
					"where b.TYPE_NAME = '科研文件修改单' and b.C_STORE_STATUS = '在库'";
			
			List<Map<String, Object>> changeBackPersonCountStatistic = documentService.getMapList(getToken(), changeBackPersonCount);
			
			Number changeBackPersonMonth1 = getSponsorT(changeBackPersonCountStatistic, "month1");
			projMap.put("changeBackPersonMonth1", changeBackPersonMonth1);
			Number changeBackPersonMonth2 = getSponsorT(changeBackPersonCountStatistic, "month2");
			projMap.put("changeBackPersonMonth2", changeBackPersonMonth2);
			Number changeBackPersonMonth3 = getSponsorT(changeBackPersonCountStatistic, "month3");
			projMap.put("changeBackPersonMonth3", changeBackPersonMonth3);
			Number changeBackPersonQuarter = getSponsorT(changeBackPersonCountStatistic, "quarterCount");
			projMap.put("changeBackPersonQuarter", changeBackPersonQuarter);
			
			outList.add(projMap);
			
			mp.put("data", outList);
			mp.put("code", ActionContext.SUCESS);
		}catch(Exception ex) {
			ex.printStackTrace();
			mp.put("code", ActionContext.FAILURE);
		}
		
		return mp;
	}
	
	private String getStrValue(Map<String, Object> args, String key) {
		return (args.containsKey(key) && args.get(key)!=null)?args.get(key).toString():"";
	}
	
	private Number getSponsorT(List<Map<String, Object>> list, String key) {
		return (list.get(0).get(key)!=null)?(Number)list.get(0).get(key):0;
	}
	
	private Number getSponsorTN(List<Map<String, Object>> list, String key) {
		return (list.get(0).get(key)!=null)?(Number)list.get(0).get(key):0;
	}
	
	private Number getSponsorFor(List<Map<String, Object>> list, String key, int i) {
		return (list.get(i).get(key)!=null)?(Number)list.get(i).get(key):0;
	}
	
	private String setSQLTimeSE(String key1, String key2) {
		return " AND (eag.EXCUTE_DATE BETWEEN '" + key1 + "' AND '" + key2 + "')";
	}
	
	private String setSQLTimeE(String key) {
		return " AND (eag.EXCUTE_DATE < '" + key + "')";
	}
	
	private String setSQLTimeS(String key) {
		return " AND (eag.EXCUTE_DATE > '" + key + "')";
	}
	
	private String setSQLTimeEmp() {
		return "";
	}
	
	private String getCurrentQuarterStart(Boolean judge) throws ParseException {
		Calendar c = Calendar.getInstance();
		int currentMonth = c.get(Calendar.MONTH) + 1;
		int yearC = c.get(Calendar.YEAR);
		
		if(judge) {
			yearC = yearC - 1;
		}
		
		String now = new String(); 
		
		c.set(Calendar.YEAR, yearC);
		c.set(Calendar.DATE, 1);
		
		if (currentMonth >= 1 && currentMonth <= 3) {
			c.set(Calendar.MONTH, 0);
		}else if (currentMonth >= 4 && currentMonth <= 6) {
			c.set(Calendar.MONTH, 3);
		}else if (currentMonth >= 7 && currentMonth <= 9) {
			c.set(Calendar.MONTH, 4);
		}else if (currentMonth >= 10 && currentMonth <= 12) {
			c.set(Calendar.MONTH, 9);
		}
		
		now = shortSdf.format(c.getTime()); 
		
		return now;
	}
	
	private String getCurrentQuarterEnd(Boolean judge) throws ParseException {
		Calendar c = Calendar.getInstance();
		int currentMonth = c.get(Calendar.MONTH) + 1;
		int yearC = c.get(Calendar.YEAR);
		
		if(judge) {
			yearC = yearC - 1;
		}
		
		String now = new String(); 
		
		c.set(Calendar.YEAR, yearC);
		
		if (currentMonth >= 1 && currentMonth <= 3) {
			c.set(Calendar.MONTH, 2);
			c.set(Calendar.DATE, 31);
		} else if (currentMonth >= 4 && currentMonth <= 6) {
			c.set(Calendar.MONTH, 5);
			c.set(Calendar.DATE, 30);
		} else if (currentMonth >= 7 && currentMonth <= 9) {
			c.set(Calendar.MONTH,8);
			c.set(Calendar.DATE, 30);
		} else if (currentMonth >= 10 && currentMonth <= 12) {
			c.set(Calendar.MONTH, 11);
			c.set(Calendar.DATE, 31);
		}
		
		now = shortSdf.format(c.getTime()); 
		
		return now;
	}
	
	private List<String> setQuarterCondition(String year, String quarter){
		List<String> conditionList = new ArrayList<String>();
		
		String conditionDate1 = new String();
		String conditionDate2 = new String();
		String conditionDate3 = new String();
		String conditionDate = new String();
		
		if(Integer.parseInt(year)%4==0&&Integer.parseInt(year)%100!=0||Integer.parseInt(year)%400==0){
			if(quarter.equals("1")) {
				conditionDate1 = " and (eag.EXCUTE_DATE between '"+ year +"-01-01' and '"+ year +"-01-31') ";
				conditionDate2= " and (eag.EXCUTE_DATE between '"+ year +"-02-01' and '"+ year +"-02-29') ";
				conditionDate3 = " and (eag.EXCUTE_DATE between '"+ year +"-03-01' and '"+ year +"-03-31') ";
				conditionDate = " and (eag.EXCUTE_DATE between '"+ year +"-01-01' and '"+ year +"-03-31') ";
			}else if(quarter.equals("2")) {
				conditionDate1 = " and (eag.EXCUTE_DATE between '"+ year +"-04-01' and '"+ year +"-04-30') ";
				conditionDate2= " and (eag.EXCUTE_DATE between '"+ year +"-05-01' and '"+ year +"-05-31') ";
				conditionDate3 = " and (eag.EXCUTE_DATE between '"+ year +"-06-01' and '"+ year +"-06-30') ";
				conditionDate = " and (eag.EXCUTE_DATE between '"+ year +"-04-01' and '"+ year +"-06-30') ";
			}else if(quarter.equals("3")) {
				conditionDate1 = " and (eag.EXCUTE_DATE between '"+ year +"-07-01' and '"+ year +"-07-31') ";
				conditionDate2= " and (eag.EXCUTE_DATE between '"+ year +"-08-01' and '"+ year +"-08-31') ";
				conditionDate3 = " and (eag.EXCUTE_DATE between '"+ year +"-09-01' and '"+ year +"-09-30') ";
				conditionDate = " and (eag.EXCUTE_DATE between '"+ year +"-07-01' and '"+ year +"-09-30') ";
			}else {
				conditionDate1 = " and (eag.EXCUTE_DATE between '"+ year +"-10-01' and '"+ year +"-10-31') ";
				conditionDate2= " and (eag.EXCUTE_DATE between '"+ year +"-11-01' and '"+ year +"-11-30') ";
				conditionDate3 = " and (eag.EXCUTE_DATE between '"+ year +"-12-01' and '"+ year +"-12-31') ";
				conditionDate = " and (eag.EXCUTE_DATE between '"+ year +"-10-01' and '"+ year +"-12-31') ";
			}
		}else {
			if(quarter.equals("1")) {
				conditionDate1 = " and (eag.EXCUTE_DATE between '"+ year +"-01-01' and '"+ year +"-01-31') ";
				conditionDate2= " and (eag.EXCUTE_DATE between '"+ year +"-02-01' and '"+ year +"-02-28') ";
				conditionDate3 = " and (eag.EXCUTE_DATE between '"+ year +"-03-01' and '"+ year +"-03-31') ";
				conditionDate = " and (eag.EXCUTE_DATE between '"+ year +"-01-01' and '"+ year +"-03-31') ";
			}else if(quarter.equals("2")) {
				conditionDate1 = " and (eag.EXCUTE_DATE between '"+ year +"-04-01' and '"+ year +"-04-30') ";
				conditionDate2= " and (eag.EXCUTE_DATE between '"+ year +"-05-01' and '"+ year +"-05-31') ";
				conditionDate3 = " and (eag.EXCUTE_DATE between '"+ year +"-06-01' and '"+ year +"-06-30') ";
				conditionDate = " and (eag.EXCUTE_DATE between '"+ year +"-04-01' and '"+ year +"-06-30') ";
			}else if(quarter.equals("3")) {
				conditionDate1 = " and (eag.EXCUTE_DATE between '"+ year +"-07-01' and '"+ year +"-07-31') ";
				conditionDate2= " and (eag.EXCUTE_DATE between '"+ year +"-08-01' and '"+ year +"-08-31') ";
				conditionDate3 = " and (eag.EXCUTE_DATE between '"+ year +"-09-01' and '"+ year +"-09-30') ";
				conditionDate = " and (eag.EXCUTE_DATE between '"+ year +"-07-01' and '"+ year +"-09-30') ";
			}else {
				conditionDate1 = " and (eag.EXCUTE_DATE between '"+ year +"-10-01' and '"+ year +"-10-31') ";
				conditionDate2= " and (eag.EXCUTE_DATE between '"+ year +"-11-01' and '"+ year +"-11-30') ";
				conditionDate3 = " and (eag.EXCUTE_DATE between '"+ year +"-12-01' and '"+ year +"-12-31') ";
				conditionDate = " and (eag.EXCUTE_DATE between '"+ year +"-10-01' and '"+ year +"-12-31') ";
			}
		}
		
		conditionList.add(conditionDate1);
		conditionList.add(conditionDate2);
		conditionList.add(conditionDate3);
		conditionList.add(conditionDate);
		
		return conditionList;
	}
}
