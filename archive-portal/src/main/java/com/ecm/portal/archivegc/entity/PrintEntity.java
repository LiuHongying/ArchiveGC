package com.ecm.portal.archivegc.entity;

import java.util.Date;

/**
 * 打印实体
 * @author Atos
 *
 */
public class PrintEntity {
	public String getId() {
		return id;
	}
	public void setId(String id) {
		this.id = id;
	}
	public String getTypeName() {
		return typeName;
	}
	public void setTypeName(String typeName) {
		this.typeName = typeName;
	}
	public String getItemType() {
		return itemType;
	}
	public void setItemType(String itemType) {
		this.itemType = itemType;
	}
	public String getPrintType() {
		return printType;
	}
	public void setPrintType(String printType) {
		this.printType = printType;
	}
	public String getCoding() {
		return coding;
	}
	public void setCoding(String coding) {
		this.coding = coding;
	}
	public String getRevision() {
		return revision;
	}
	public void setRevision(String revision) {
		this.revision = revision;
	}
	public String getArchiveCoding() {
		return archiveCoding;
	}
	public void setArchiveCoding(String archiveCoding) {
		this.archiveCoding = archiveCoding;
	}
	public String getSecurityLevel() {
		return securityLevel;
	}
	public void setSecurityLevel(String securityLevel) {
		this.securityLevel = securityLevel;
	}
	public String getRetention() {
		return retention;
	}
	public void setRetention(String retention) {
		this.retention = retention;
	}
	public String getArchiveDate() {
		return archiveDate;
	}
	public void setArchiveDate(String archiveDate) {
		this.archiveDate = archiveDate;
	}
	public String getStoreCoding() {
		return storeCoding;
	}
	public void setStoreCoding(String storeCoding) {
		this.storeCoding = storeCoding;
	}
	public String getArchiveClassic() {
		return archiveClassic;
	}
	public void setArchiveClassic(String archiveClassic) {
		this.archiveClassic = archiveClassic;
	}
	public String getProjectCode() {
		return projectCode;
	}
	public void setProjectCode(String projectCode) {
		this.projectCode = projectCode;
	}
	private String id;
	private String typeName;
	private String itemType;
	private String printType;
	private String coding;
	private String revision;
	private String archiveCoding;
	private String archiveClassic;
	private String securityLevel;
	private String retention;
	private String archiveDate;
	private String storeCoding;
	private String projectCode;
	private String volString;
	
	public PrintEntity clone() {
		PrintEntity en = new PrintEntity();
		en.setArchiveClassic(archiveClassic);
		en.setArchiveCoding(archiveCoding);
		en.setArchiveDate(archiveDate);
		en.setCoding(coding);
		en.setId(id);
		en.setItemType(itemType);
		en.setTypeName(typeName);
		en.setPrintType(printType);
		en.setProjectCode(projectCode);
		en.setRetention(retention);
		en.setRevision(revision);
		en.setSecurityLevel(securityLevel);
		en.setStoreCoding(storeCoding);
		return en;
	}
	public String getVolString() {
		return volString;
	}
	public void setVolString(String volString) {
		this.volString = volString;
	}
}
