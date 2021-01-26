package com.ecm.portal.archivegc.tool.excel;


import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import org.apache.poi.ss.usermodel.Workbook;
import org.elasticsearch.common.Strings;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.ecm.core.entity.EcmGroup;
import com.ecm.core.entity.EcmUser;
import com.ecm.core.exception.AccessDeniedException;
import com.ecm.core.service.GroupService;
import com.ecm.core.service.UserService;
import com.ecm.portal.archivegc.tool.excel.entity.DeptInfoCD;
import com.ecm.portal.archivegc.tool.excel.entity.UserDeptInfoCD;
import com.ecm.portal.archivegc.tool.excel.entity.UserInfoCD;  
@Service
public class ImportUserDept {
	@Autowired
	private UserService userService;
	
	@Autowired
	private GroupService groupService;
	
	/**
	 *      通过EXCEL导入用户
	 * @param in
	 * @param fileName
	 * @throws Exception 
	 */
	@Transactional(rollbackFor = Exception.class)
	public void importUserByExcel(String token, InputStream in, String fileName) throws Exception  {
		  Workbook wb = ImportExeclUtil.chooseWorkbook(fileName, in);
		  UserInfoCD  obj = new UserInfoCD();//Bean 和EXCEL字段需要按顺序对应
		  List<UserInfoCD> readDateListT = ImportExeclUtil.readDateListT(wb, obj, 2, 0);
		  for (int i = 0; i < readDateListT.size(); i++) {
			  UserInfoCD  syncUser = readDateListT.get(i);
			  System.out.println(syncUser.getId() +" " +syncUser.getUserId() +" " +syncUser.getUserName()  +" " +syncUser.getUserSex()  +" "+syncUser.getUserStatus()  +" " +syncUser.getUserOrder()  +" " +syncUser.getUserPasswd() +" " +syncUser.getIsMeetine() );
			  EcmUser ecmUser = userService.getObjectByLoginName(token, syncUser.getUserId());
			  if (ecmUser == null) {
				  EcmUser newUser =  new  EcmUser();
				  newUser.setLoginName(syncUser.getUserId());
				  newUser.setName(syncUser.getUserName());
				  if(syncUser.getUserStatus()!= null && syncUser.getUserStatus().equals("1")) {
					  newUser.setIsActived(true);
				  }else {
					  newUser.setIsActived(false);
				  }
				  newUser.setPassword(syncUser.getUserPasswd());
				  newUser.setClientPermission(0);
				  newUser.setSystemPermission(0);
				  userService.newObject(token, newUser);
			  }
		  }
	}
	
	/**
	 *      通过EXCEL导入部门
	 * @param in
	 * @param fileName
	 * @throws Exception 
	 */

	@Transactional(rollbackFor = Exception.class)
	public void importGroupByExcel(String token, InputStream in, String fileName) throws Exception  {
		  Workbook wb = ImportExeclUtil.chooseWorkbook(fileName, in);
		  DeptInfoCD  obj = new DeptInfoCD();//Bean 和EXCEL字段需要按顺序对应
		  List<DeptInfoCD> readDateListT = ImportExeclUtil.readDateListT(wb, obj, 2, 0);
		  for (int i = 0; i < readDateListT.size(); i++) {
			  DeptInfoCD  syncDept = readDateListT.get(i);
			  System.out.println(syncDept.getId() +" " +syncDept.getDeptId() +" " +syncDept.getDeptName()  +" " +syncDept.getDeptParentId()  +" "+syncDept.getDeptStatus()  +" " +syncDept.getDeptOrder()  );
			  EcmGroup ecmGroup = groupService.getGroupByName(token, syncDept.getDeptName());
			  if (ecmGroup == null) {
				  EcmGroup newDept=  new  EcmGroup();
				  newDept.setName(syncDept.getDeptName());
				  newDept.setExtendId(syncDept.getDeptId());
				  newDept.setGroupType("1"); 
				  groupService.newGroup(token, newDept);
			  }
		  }
		  
		  //建立部门之间关系
		  for (int i = 0; i < readDateListT.size(); i++) {
			  DeptInfoCD  syncChildDept = readDateListT.get(i);
			  String childDeptParentId = syncChildDept.getDeptParentId();
			  if(!Strings.isEmpty(childDeptParentId)) {
				  //通过部门名称获取已经创建的部门信息
				  EcmGroup childGroup = groupService.getGroupByName(token, syncChildDept.getDeptName());
				  EcmGroup parentGroup = null;
				  for (int j = 0; j < readDateListT.size(); j++) {
					  DeptInfoCD  syncParentDept = readDateListT.get(j);
					  if(syncParentDept.getDeptId().equals(childDeptParentId)) {
						   parentGroup = groupService.getGroupByName(token, syncParentDept.getDeptName());
						   break;
					  }
				  } 
				  if (childGroup != null && parentGroup !=null) {
					 groupService.addRole(null, parentGroup.getId(), childGroup.getId());
				  }
			  }
		}
	}
	
	/**
	 *      用户加入部门
	 * @param in
	 * @param fileName
	 * @throws Exception 
	 */
	@Transactional(rollbackFor = Exception.class)
	public void addUserToDept(String token, InputStream in, String fileName) throws Exception  {
		 Workbook wb = ImportExeclUtil.chooseWorkbook(fileName, in);
		 UserDeptInfoCD  obj = new UserDeptInfoCD();//Bean 和EXCEL字段需要按顺序对应
		  List<UserDeptInfoCD> readDateListT = ImportExeclUtil.readDateListT(wb, obj, 2, 0);
		  for (int i = 0; i < readDateListT.size(); i++) {
			  UserDeptInfoCD  syncUserDept = readDateListT.get(i);
			  System.out.println(syncUserDept.getId() +" " +syncUserDept.getUserId() +" " +syncUserDept.getDeptId()  );
			  
			  EcmUser ecmUser = userService.getObjectByLoginName(token, syncUserDept.getUserId());
			  String sql = "EXTEND_ID = '" +syncUserDept.getDeptId()+ "'";
			  List<EcmGroup> ecmGroupList =  groupService.getGroupObjects(token,sql);
			   if(ecmGroupList.size()>0 && ecmUser!=null) {
				   EcmGroup ecmDept =  ecmGroupList.get(0);
				   groupService.addUserToGroup(token, ecmUser.getId(),ecmDept.getId());
				}
			  
		  }
	}
	
	
	
	public static void main(String[] args) {

		  String fileName="T_User.xlsx";
		  try {
			  InputStream in = new FileInputStream(new File("D:\\\\work\\\\cdtest\\\\userdata\\\\T_User.xlsx"));
			  Workbook wb = ImportExeclUtil.chooseWorkbook(fileName, in);
			  UserInfoCD  user = new UserInfoCD();//Bean 和EXCEL字段需要按顺序对应
			  List<UserInfoCD> readDateListT = ImportExeclUtil.readDateListT(wb, user, 2, 0);

			  for (int i = 0; i < readDateListT.size(); i++) {
				  UserInfoCD  obj = readDateListT.get(i);
				  System.out.println(obj.getId() +" " +obj.getUserId() +" " +obj.getUserName()  +" " +obj.getUserSex()  +" "+obj.getUserStatus()  +" " +obj.getUserOrder()  +" " +obj.getUserPasswd() +" " +obj.getIsMeetine() );
			}
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	
	
	
}
