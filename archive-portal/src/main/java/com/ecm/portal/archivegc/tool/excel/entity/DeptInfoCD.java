package com.ecm.portal.archivegc.tool.excel.entity;

import com.ecm.portal.archivegc.tool.excel.IsNeeded;

public class DeptInfoCD {
	@IsNeeded
	private String id;
    @IsNeeded
    private String deptId;
    @IsNeeded
    private String deptName;
    @IsNeeded
    private String deptParentId;
    @IsNeeded
    private String deptStatus;
    @IsNeeded
    private String deptOrder;
	public String getId() {
		return id;
	}
	public void setId(String id) {
		this.id = id;
	}
	public String getDeptId() {
		return deptId;
	}
	public void setDeptId(String deptId) {
		this.deptId = deptId;
	}
	public String getDeptName() {
		return deptName;
	}
	public void setDeptName(String deptName) {
		this.deptName = deptName;
	}
	public String getDeptParentId() {
		return deptParentId;
	}
	public void setDeptParentId(String deptParentId) {
		this.deptParentId = deptParentId;
	}
	public String getDeptStatus() {
		return deptStatus;
	}
	public void setDeptStatus(String deptStatus) {
		this.deptStatus = deptStatus;
	}
	public String getDeptOrder() {
		return deptOrder;
	}
	public void setDeptOrder(String deptOrder) {
		this.deptOrder = deptOrder;
	} 
    
    
    
}
