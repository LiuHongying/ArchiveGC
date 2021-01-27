package com.ecm.portal.archivegc;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.util.List;

import org.apache.poi.ss.usermodel.Workbook;
import org.junit.Test;
import org.junit.runner.RunWith;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.core.env.Environment;
import org.springframework.test.context.junit4.SpringRunner;

import com.ecm.core.service.AuthService;
import com.ecm.core.service.UserService;
import com.ecm.icore.service.IEcmSession;
import com.ecm.portal.archivegc.tool.excel.ImportExeclUtil;
import com.ecm.portal.archivegc.tool.excel.ImportUserDept;
import com.ecm.portal.archivegc.tool.excel.entity.UserInfoCD;

@RunWith(SpringRunner.class)
@SpringBootTest
@Slf4j
public class MyTests {
	@Autowired
	private ImportUserDept importUserdeptService;
	@Autowired
	private Environment env;

	@Autowired
	private AuthService authService;
	
    @Test
    public void testSyncUserDept() {
    	  String fileName="T_User.xlsx";
		  try {
			  
			  IEcmSession ecmSession = null;
			
			  ecmSession = authService.login("syncUser", "admin","Ecm.520!");
			
			  //导入用户
//			  InputStream in = new FileInputStream(new File("D:\\work\\cdtest\\userdata\\T_User.xlsx"));
//			  importUserdeptService.importUserByExcel(ecmSession.getToken(),in, fileName);
//			  
			  
			  //导入部门
//			  InputStream in = new FileInputStream(new File("D:\\work\\cdtest\\userdata\\T_Dept.xlsx"));
//			  importUserdeptService.importGroupByExcel(ecmSession.getToken(),in, fileName);
			  
			  //导入用户和部门关系
			  InputStream in = new FileInputStream(new File("D:\\work\\cdtest\\userdata\\T_USER_DEPT.xlsx"));
			  importUserdeptService.addUserToDept(ecmSession.getToken(),in, fileName);
			
			  
			  
			  
			  
//			  UserInfoCD  user = new UserInfoCD();//Bean 和EXCEL字段需要按顺序对应
//			  List<UserInfoCD> readDateListT = ImportExeclUtil.readDateListT(wb, user, 2, 0);
//			  for (int i = 0; i < readDateListT.size(); i++) {
//				  UserInfoCD  obj = readDateListT.get(i);
//				  System.out.println(obj.getId() +" " +obj.getUserId() +" " +obj.getUserName()  +" " +obj.getUserSex()  +" "+obj.getUserStatus()  +" " +obj.getUserOrder()  +" " +obj.getUserPasswd() +" " +obj.getIsMeetine() );
//			}
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
    }
}
