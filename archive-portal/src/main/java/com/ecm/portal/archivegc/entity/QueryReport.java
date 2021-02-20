package com.ecm.portal.archivegc.entity;

public class QueryReport {
	private String number;
	private QueryTemplate template;
	private String year;
	private String condition;
	public String getNumber() {
		return number;
	}
	public void setNumber(String number) {
		this.number = number;
	}
	public QueryTemplate getTemplate() {
		return template;
	}
	public void setTemplate(QueryTemplate template) {
		this.template = template;
	}
	public String getYear() {
		return year;
	}
	public void setYear(String year) {
		this.year = year;
	}
	public String getCondition() {
		return condition;
	}
	public void setCondition(String condition) {
		this.condition = condition;
	}
	
	
}
