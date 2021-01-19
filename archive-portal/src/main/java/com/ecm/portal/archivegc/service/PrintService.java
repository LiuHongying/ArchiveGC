package com.ecm.portal.archivegc.service;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.Image;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;

import javax.imageio.ImageIO;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.ecm.common.util.DateUtils;
import com.ecm.core.service.DocumentService;
import com.ecm.core.service.EcmService;
import com.ecm.portal.archivegc.entity.PrintEntity;
import com.itextpdf.text.pdf.BarcodePDF417;

@Service
public class PrintService extends EcmService{
	@Autowired
	private DocumentService documentService;

	private static final Logger logger = LoggerFactory.getLogger(PrintService.class);
	
	public List<PrintEntity> getPrintEntity(String token,List<String> idList, String printType) throws Exception{
		List<PrintEntity> list = new ArrayList<PrintEntity>();
		String ids = "";
		for(String id:idList) {
			if(ids.length()>0) {
				ids += ",'"+id+"'";
			}else {
				ids = "'"+id+"'";
			}
		}
		String sql = "select ID,TYPE_NAME,C_ITEM_TYPE,C_ARC_CLASSIC,C_ARCHIVE_CODING,C_FROM_CODING,C_STORE_CODING,CODING,REVISION,C_PROJECT_CODE,C_PROJECT_NUM,C_SECURITY_LEVEL,C_RETENTION,C_SET_COUNT,C_VOLUME_COUNT,C_COUNT2,C_COPY_COUNT,C_COUNT1,C_ARCHIVE_DATE " + 
				" from ecm_document where id in("+ids+") order by TYPE_NAME,CODING,C_ARCHIVE_DATE";
		List<Map<String,Object>> dataList = documentService.getMapList(token, sql);
		for(Map<String,Object> data: dataList) {
			String archiveClassic = (String)data.get("C_ARC_CLASSIC");
			PrintEntity en = new PrintEntity();
			en.setId((String)data.get("ID"));
			en.setPrintType(printType);
			en.setArchiveClassic(archiveClassic);
			en.setTypeName((String)data.get("TYPE_NAME"));
			en.setItemType((String)data.get("C_ITEM_TYPE"));
			en.setArchiveCoding((String)data.get("C_ARCHIVE_CODING"));
			//工程建设、工程设计 显示图册号
			if("工程设计".equals(archiveClassic) || "工程建设".equals(archiveClassic)) {
				en.setCoding((String)data.get("C_FROM_CODING"));
			}
			//其次设置编码
			if(StringUtils.isEmpty(en.getCoding())){
				en.setCoding((String)data.get("CODING"));
			}
			//编码为空再设置档号
			if(StringUtils.isEmpty(en.getCoding())){
				en.setCoding(en.getArchiveCoding());
			}
			en.setRevision((String)data.get("REVISION"));
			en.setProjectCode((String)data.get("C_PROJECT_NUM"));
			if(StringUtils.isEmpty(en.getProjectCode())) {
				en.setProjectCode((String)data.get("C_PROJECT_CODE"));
			}
			en.setSecurityLevel((String)data.get("C_SECURITY_LEVEL"));
			en.setRetention((String)data.get("C_RETENTION"));
			en.setArchiveDate(getDateString((Date)data.get("C_ARCHIVE_DATE")));
			en.setStoreCoding((String)data.get("C_STORE_CODING"));
			//套数
			int setCount = 0;
			//册数
			int volCount = 0;

			if("商务管理".equals(archiveClassic)) {
				if("正本".equals(printType) || "复制件".equals(printType)) {
//					正本套数	C_SET_COUNT
//					正本册数	C_COUNT2
					setCount = getCount(data,"C_SET_COUNT");
					volCount = getCount(data,"C_COUNT2");
				}
				//副本
				else {
//					副本套数	C_COPY_COUNT
//					副本册数	C_COUNT1
					setCount = getCount(data,"C_COPY_COUNT");
					volCount = getCount(data,"C_COUNT1");
				}
			}else {
				//归档套数	C_SET_COUNT
				//册数	C_VOLUME_COUNT
				setCount = getCount(data,"C_SET_COUNT");
				volCount = getCount(data,"C_VOLUME_COUNT");
				
			}
			if(volCount>1) {
				for(int i=0; i<volCount; i++) {
					if(setCount>1) {
						for(int j=0; j<setCount; j++) {
							String tempStr = "第"+volCount+"_"+(i+1)+"册 第"+setCount+"_"+(j+1)+"套";
							PrintEntity tempEn = en.clone();
							tempEn.setVolString(tempStr);
							list.add(tempEn);
						}
					}else {
						String tempStr = "第"+volCount+"_"+(i+1)+"册";
						PrintEntity tempEn = en.clone();
						tempEn.setVolString(tempStr);
						list.add(tempEn);
					}
				}
			}else {
				if(setCount>1) {
					for(int j=0; j<setCount; j++) {
						String tempStr = "第"+setCount+"_"+(j+1)+"套";
						PrintEntity tempEn = en.clone();
						tempEn.setVolString(tempStr);
						list.add(tempEn);
					}
				}else {
					list.add(en);
				}
			}
		}
		return list;
	}
	/**
	 * 生成PDf417条码
	 * @param strInfo 二维码信息
	 * @param encode 字符编码（UTF-8、GBK)，默认UTF-8，
	 * @param format 图片格式，jpg，png等
	 * @return 二进制图片数据
	 * @throws Exception
	 */
	public byte[] generatePdf417Image(String strInfo, String encode,
			String format) throws Exception {
		if (StringUtils.isEmpty(strInfo)) {
			throw new Exception("二维条码的文本信息参数不能为空！");
		}
		if (StringUtils.isEmpty(encode)) {
			encode = "UTF-8";
		}

		BarcodePDF417 barcodePDF417 = new BarcodePDF417();

		barcodePDF417.setText(strInfo.getBytes(encode));

		Image pdfImg = barcodePDF417.createAwtImage(Color.black, Color.white);

		BufferedImage img = new BufferedImage((int) pdfImg.getWidth(null),
				(int) pdfImg.getHeight(null), BufferedImage.TYPE_INT_RGB);
		Graphics g = img.getGraphics();

		g.drawImage(pdfImg, 0, 0, Color.white, null);

		ByteArrayOutputStream os = new ByteArrayOutputStream();

		ImageIO.write(img, format, os);

		byte[] buffs = os.toByteArray();

		os.close();

		return buffs;
	}
	
	private String getDateString(Date dt) {
		if(dt != null) {
			return DateUtils.DateToStr(dt,"yyyy-MM-dd");
		}
		return null;
	}
	
	private int getCount(Map<String,Object> data,String attrName) {
		Object obj = data.get(attrName);
		if(obj != null && obj instanceof Integer) {
			return (int)obj;
		}
		String i = (String)data.get(attrName);
		if(!StringUtils.isEmpty(i)) {
			try {
				return Integer.parseInt(i);
			}catch(Exception ex) {
				
			}
		}
		return 0;
	}
}
