package com.ecm.portal.archivegc.service;

import java.util.Map;

public interface IReportService {
	/**
	 * 年报
	 * @param selectYear 选择年度
	 * @param currentYear 当前年度
	 * @return
	 */
	public Map<String,String> annualReport(String selectYear,String currentYear);
}
