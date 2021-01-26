package org.zisecm.jobs.business;

import java.io.File;
import java.io.IOException;

import org.apache.tomcat.util.http.fileupload.FileUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import com.ecm.core.cache.manager.CacheManagerOper;
import com.ecm.core.util.SysConfig;

@Service
public class SyncPublicNetApp {
	@Autowired
	SyncPublicNet syncPublicNet;

	
	@Scheduled(cron = "${corn.syncjob}")
	public void run() {
		try {
			syncPublicNet.saveAllUserIp("http://139.10.18.121:6666/Enterprise.asmx/GetAllComputerSecretLevel");
			
			syncPublicNet.saveAllUserId("http://139.10.18.121:6666/Enterprise.asmx/GetAllUserSecretLevel");
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
}
