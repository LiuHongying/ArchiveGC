package com.ecm.portal.archivegc.tool.excel.entity;

import com.ecm.portal.archivegc.tool.excel.IsNeeded;
//Bean 和EXCEL字段需要按顺序对应
public class UserInfoCD {
	@IsNeeded
	private String id;
    @IsNeeded
    private String userId;
    @IsNeeded
    private String userName;
    @IsNeeded
    private String userSex;
    @IsNeeded
    private String userStatus;
    @IsNeeded
    private String userOrder;
    @IsNeeded
    private String userPasswd;
    @IsNeeded
    private String isMeetine;
    
//    private BigDecimal grade;


	

	public String getId() {
		return id;
	}


	public void setId(String id) {
		this.id = id;
	}


	public String getUserId() {
		return userId;
	}


	public void setUserId(String userId) {
		this.userId = userId;
	}


	public String getUserName() {
		return userName;
	}


	public void setUserName(String userName) {
		this.userName = userName;
	}


	public String getUserSex() {
		return userSex;
	}


	public void setUserSex(String userSex) {
		this.userSex = userSex;
	}


	public String getUserStatus() {
		return userStatus;
	}


	public void setUserStatus(String userStatus) {
		this.userStatus = userStatus;
	}


	public String getUserOrder() {
		return userOrder;
	}


	public void setUserOrder(String userOrder) {
		this.userOrder = userOrder;
	}


	public String getUserPasswd() {
		return userPasswd;
	}


	public void setUserPasswd(String userPasswd) {
		this.userPasswd = userPasswd;
	}


	public String getIsMeetine() {
		return isMeetine;
	}


	public void setIsMeetine(String isMeetine) {
		this.isMeetine = isMeetine;
	}
    
    
   
}
