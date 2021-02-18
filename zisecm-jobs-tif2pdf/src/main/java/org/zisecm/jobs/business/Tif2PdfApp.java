package org.zisecm.jobs.business;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import com.ecm.core.cache.manager.CacheManagerOper;

@Service
public class Tif2PdfApp {
	@Autowired
	TIF2Pdf tIF2Pdf;
	@Scheduled(cron = "${corn3}")
	public void run() {
		try {
			if(CacheManagerOper.getFinishLoadCacheTag()) {
				tIF2Pdf.downFile();
			
			};
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
}
