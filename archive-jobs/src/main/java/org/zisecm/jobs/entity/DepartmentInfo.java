package org.zisecm.jobs.entity;

public class DepartmentInfo {

	/**
     * 部门id
     */
    private String _departmentid;

    /**
     * 简称
     */
    private String _shortname;

    /**
     * 全称
     */
    private String _fullname;

    /**
     * 所属分部id
     */
    private String _subcompanyid;

    /**
     * 上级部门id
     */
    private String _supdepartmentid;

    /**
     * 显示顺序
     */
    private String _showorder;

    /**
     * 部门编码
     */
	private String _code;
	/**
	     * 是否封存
	     */
	private String _canceled;
	public String get_departmentid() {
		return _departmentid;
	}
	public void set_departmentid(String _departmentid) {
		this._departmentid = _departmentid;
	}
	public String get_shortname() {
		return _shortname;
	}
	public void set_shortname(String _shortname) {
		this._shortname = _shortname;
	}
	public String get_fullname() {
		return _fullname;
	}
	public void set_fullname(String _fullname) {
		this._fullname = _fullname;
	}
	public String get_subcompanyid() {
		return _subcompanyid;
	}
	public void set_subcompanyid(String _subcompanyid) {
		this._subcompanyid = _subcompanyid;
	}
	public String get_supdepartmentid() {
		return _supdepartmentid;
	}
	public void set_supdepartmentid(String _supdepartmentid) {
		this._supdepartmentid = _supdepartmentid;
	}
	public String get_showorder() {
		return _showorder;
	}
	public void set_showorder(String _showorder) {
		this._showorder = _showorder;
	}
	public String get_code() {
		return _code;
	}
	public void set_code(String _code) {
		this._code = _code;
	}
	public String get_canceled() {
		return _canceled;
	}
	public void set_canceled(String _canceled) {
		this._canceled = _canceled;
	}

	
	
}
