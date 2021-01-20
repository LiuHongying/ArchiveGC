package com.ecm.portal.archivegc.service;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.Image;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.imageio.ImageIO;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import com.ecm.common.util.DateUtils;
import com.ecm.common.util.FileUtils;
import com.ecm.core.cache.manager.CacheManagerOper;
import com.ecm.core.entity.EcmContent;
import com.ecm.core.exception.AccessDeniedException;
import com.ecm.core.exception.NoTakeNumberRuleException;
import com.ecm.core.service.DocumentService;
import com.ecm.core.service.EcmService;
import com.ecm.icore.service.IEcmSession;
import com.ecm.portal.archivegc.entity.PrintEntity;
import com.itextpdf.text.pdf.BarcodePDF417;

@Service
public class MountFileService extends EcmService{
	@Autowired
	private DocumentService documentService;

	private static final Logger logger = LoggerFactory.getLogger(MountFileService.class);
	
	public String BatchMountFile(String token, List<String> ids, MultipartFile[] files) throws Exception {
		StringBuilder sb = new StringBuilder();
		int sucessCount = 0;
		int failedCount = 0;
		String relationName = "irel_children";
		sb.append("开始挂载:").append(DateUtils.currentDate("yyyy-MM-dd HH:mm:ss")).append("\r\n");
		if(files == null || files.length ==0 ) {
			sb.append("未选择电子文件！\r\n");
			sb.append("完成挂载导入:").append(DateUtils.currentDate("yyyy-MM-dd HH:mm:ss")).append("\r\n");
			return sb.toString();
		}
		if(ids == null || ids.size() ==0 ) {
			sb.append("未选择文件或案卷！");
			sb.append("完成挂载导入:").append(DateUtils.currentDate("yyyy-MM-dd HH:mm:ss")).append("\r\n");
			return sb.toString();
		}
		if(ids.size()==1 && files.length ==1) {
			MultipartFile uploadFile = files[0];
			EcmContent en = new EcmContent();
			en.setName(uploadFile.getOriginalFilename());
			en.setContentSize(uploadFile.getSize());
			en.setInputStream(uploadFile.getInputStream());
			en.setFormatName(FileUtils.getExtention(uploadFile.getOriginalFilename()));
			en.setContentType(1);
			documentService.mountFile(token, ids.get(0), en);
			sucessCount++;
		}else {
//			IEcmSession session = documentService.getSession(token);
//			String userLoginName = session.getCurrentUser().getLoginName();
//			String uploadFolder = CacheManagerOper.getEcmParameters().get("UploadFolder").getValue();
//			if (!uploadFolder.endsWith(File.separator)) {
//				uploadFolder += File.separator;
//			}
//			uploadFolder +=  userLoginName + File.separator;
//			File f = new File(uploadFolder);
//			f.mkdirs();
			
			//Map<String, Long> fileList = new HashMap<String, Long>();
			String idSql = "";
			for(String id: ids) {
				if(idSql.length()>0) {
					idSql +=",'"+id+"'";
				}else {
					idSql +="'"+id+"'";
				}
			}
			for (MultipartFile file : files) {
				String name = file.getOriginalFilename();
				name = name .replace("（", "(").replace("）", ")");
				String coding = "";
				String revision = "";
				if(name.indexOf("(")>0) {
					String[] temp = name.split("\\(");
					coding = temp[0];
					if(name.indexOf(")")>0) {
						revision = temp[1].split("\\)")[0];
					}else {
						revision = temp[1].split(".")[0];
					}
				}else {
					coding = name.split(".")[0];
				}
				String sql = "select ID from ecm_document where C_ITEM_TYPE='文件' AND ID in("+idSql +") AND CODING='"+coding+"'";
				if(revision.length()>0) {
					sql += " AND REVISION='"+revision+"'";
				}
				List<Map<String,Object>> docList = documentService.getMapList(token, sql);
				if(docList == null || docList.size()==0) {
					sql = "select a.ID from ecm_document a,ecm_relation b where a.C_ITEM_TYPE='文件' AND a.ID=b.CHILD_ID and b.PARENT_ID in("+idSql +")  AND a.CODING='"+coding+"'";
					if(revision.length()>0) {
						sql += " AND a.REVISION='"+revision+"'";
					}
					docList = documentService.getMapList(token, sql);
					if(docList == null || docList.size()==0) {
						sb.append("文件：").append(name).append("，未找到对应文件。\r\n");
						failedCount++;
					}else if(docList.size() ==1) {
						MultipartFile uploadFile = file;
						EcmContent en = new EcmContent();
						en.setName(uploadFile.getOriginalFilename());
						en.setContentSize(uploadFile.getSize());
						en.setInputStream(uploadFile.getInputStream());
						en.setFormatName(FileUtils.getExtention(uploadFile.getOriginalFilename()));
						en.setContentType(1);
						documentService.mountFile(token, docList.get(0).get("ID").toString(), en);
						sucessCount++;
					}else {
						sb.append("文件：").append(name).append("，找到对应文件多于一条。\r\n");
						failedCount++;
					}
				}else if(docList.size() ==1) {
					MultipartFile uploadFile = file;
					EcmContent en = new EcmContent();
					en.setName(uploadFile.getOriginalFilename());
					en.setContentSize(uploadFile.getSize());
					en.setInputStream(uploadFile.getInputStream());
					en.setFormatName(FileUtils.getExtention(uploadFile.getOriginalFilename()));
					en.setContentType(1);
					documentService.mountFile(token, docList.get(0).get("ID").toString(), en);
					sucessCount++;
				}else {
					sb.append("文件：").append(name).append("，找到对应文件多于一条。\r\n");
					failedCount++;
				}
			}
		}
		sb.append("完成挂载导入:").append(DateUtils.currentDate("yyyy-MM-dd HH:mm:ss")).append("\r\n");
		sb.append("成功行数:").append(sucessCount).append("\r\n");
		sb.append("错误行数:").append(failedCount).append("\r\n");
		return sb.toString();
	}
	
	/**
	 * 将InputStream写入本地文件
	 * 
	 * @param destination 写入本地目录
	 * @param input       输入流
	 * @throws IOException
	 */
	private void writeToLocal(String destination, InputStream input) throws IOException {
		int index;
		byte[] bytes = new byte[1024];
		FileOutputStream downloadFile = new FileOutputStream(destination);
		while ((index = input.read(bytes)) != -1) {
			downloadFile.write(bytes, 0, index);
			downloadFile.flush();
		}
		downloadFile.close();
		input.close();
	}
}
