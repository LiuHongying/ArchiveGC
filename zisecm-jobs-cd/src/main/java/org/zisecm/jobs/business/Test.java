package org.zisecm.jobs.business;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.List;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.zisecm.jobs.entity.SyncUserIpBean;

import com.alibaba.fastjson.JSONObject;

public class Test {
public static void main(String[] args) {
	 SyncPublicNet sync = new SyncPublicNet();
//	 ArrayList<SyncUserBean> list = new  ArrayList<SyncUserBean>() ;
//
//	 for (int i = 0; i < 3; i++) {
//		 SyncUserBean synObj = new SyncUserBean();
//		 synObj.setKeyid(null);
//		 synObj.setIp("139.8.10."+i);
//		 synObj.setSecretlevel(String.valueOf(i));
//		 list.add(synObj);
//	}
//  
//    String fullPath = "D:\\work\\getip1.json";
//    try {
//    	sync.writeJsonFile(list,fullPath);
//	} catch (IOException e) {
//		// TODO Auto-generated catch block
//		e.printStackTrace();
//	}
//	 List<SyncUserBean> objList = JSON.parseArray(sync.readJsonFile("D:\\work\\getip.json"), SyncUserBean.class);
//	 System.out.println();
	 
	 
		 JSONObject jsonObject=new JSONObject();
		 
		// String str = "[{\"keyid\":null,\"ip\":\"139.8.10.1\",\"secretlevel\":\"1\"},{\"keyid\":null,\"ip\":\"139.8.10.2\",\"secretlevel\":\"2\"}]";
		 String str = "[{\"code\":\"1\",\"secretlevel\":\"1\",\"message\":\"获取成功\"}]";
			
		 List<SyncUserIpBean> objList =jsonObject.parseArray(str, SyncUserIpBean.class);
		 String a= objList.get(0).getSecretlevel();
		 System.out.println(a);
	
}

}
