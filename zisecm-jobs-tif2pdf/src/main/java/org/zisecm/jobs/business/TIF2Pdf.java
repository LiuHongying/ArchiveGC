package org.zisecm.jobs.business;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletResponse;

import org.apache.commons.io.FileUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Service;

import com.ecm.core.cache.manager.CacheManagerOper;
import com.ecm.core.dao.EcmDocumentMapper;
import com.ecm.core.entity.EcmContent;
import com.ecm.core.service.AuthService;
import com.ecm.core.service.ContentService;
import com.ecm.core.service.DocumentService;
import com.ecm.icore.service.IEcmSession;
import com.lowagie.text.Document;
import com.lowagie.text.DocumentException;
import com.lowagie.text.Image;
import com.lowagie.text.Utilities;
import com.lowagie.text.pdf.PdfWriter;
import com.lowagie.text.pdf.RandomAccessFileOrArray;
import com.lowagie.text.pdf.codec.TiffImage;
 
/**
 * 
 * <pre>
 * 普通图片格式转成PDF格式
 * </pre>
 * 
 */
@Service
public class TIF2Pdf {
	@Autowired
	private DocumentService documentService;
	@Autowired
	private ContentService contentService;
	@Autowired
	private Environment env;
	@Autowired
	private AuthService authService;
	@SuppressWarnings("null")
	public void downFile() {
		IEcmSession ecmSession = null;
		EcmContent content = new EcmContent();
		content.getFormatName();
		String userName = env.getProperty("ecm.username");
		try {
		ecmSession = authService.login("tif转pdf", userName, env.getProperty("ecm.password"));
		String token = ecmSession.getToken();
		
		String sql="select * from ecm_document where FORMAT_NAME='tif'";
		List<Map<String, Object>> List = documentService.getMapList(token, sql);
		for(Map<String,Object> map:List) {
			String id = map.get("ID").toString();
			try {
			EcmContent en = contentService.getPrimaryContent(token,id);
			InputStream iStream = contentService.getContentStream(token,en);
			String folderBase=CacheManagerOper.getEcmParameters().get("Tiff2PdfFolder").getValue();
			String fileName = folderBase + File.separatorChar+ en.getId()+".tif";
			String pdfName = folderBase + File.separatorChar+ en.getId()+".pdf";
			FileUtils.copyInputStreamToFile(iStream, new File(fileName));
			convert(fileName,pdfName);
			iStream.close();
			EcmContent newDoc = new EcmContent();
			FileInputStream fis = new FileInputStream(pdfName);
			newDoc.setName(pdfName);
			newDoc.setInputStream(fis);
			documentService.addRendition(token, id, newDoc);
			fis.close();
			File file = new File(fileName);
			file.delete();
			File pdfFile = new File(pdfName);
			pdfFile.delete();
			
			}
			catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		authService.logout(token);
		}catch (Exception e) {
			e.printStackTrace();
		}
		
	}
	public void convert(String fileName, String pdfName) {
		// 创建一个文档对象
		Document doc = new Document();
		try {
			// 定义输出文件的位置
			PdfWriter.getInstance(doc, new FileOutputStream(pdfName));
			// 开启文档
			doc.open();
			Object localObject1 = null;
			Object localObject2 = null;
			Image localImage1 = null;
			URL paramURL = Utilities.toURL(fileName);
			try {
				if (paramURL.getProtocol().equals("file")) {
					localObject2 = paramURL.getFile();
					localObject2 = Utilities
							.unEscapeURL((String) localObject2);
					localObject1 = new RandomAccessFileOrArray(
							(String) localObject2);
				} else {
					localObject1 = new RandomAccessFileOrArray(paramURL);
				}

				int pageNums = TiffImage
						.getNumberOfPages((RandomAccessFileOrArray) localObject1);
				if (pageNums > 0) {
					for (int i = 1; i <= pageNums; i++) {
						localObject2 = TiffImage.getTiffImage(
								(RandomAccessFileOrArray) localObject1, i);
						Image jpg = (Image) localObject2;
						// 获得图片的高度
						float heigth = jpg.getHeight();
						float width = jpg.getWidth();
						// 合理压缩，h>w，按w压缩，否则按w压缩
						int percent = getPercent(heigth, width);

						// 设置图片居中显示
						jpg.setAlignment(Image.MIDDLE);
						// 按百分比显示图片的比例
						if (width > 1024 || heigth > 786) {
							jpg.scalePercent(percent);
						}
						doc.add(jpg);
					}
				}
				if (localObject1 != null)
					((RandomAccessFileOrArray) localObject1).close();

			} finally {
				if (localObject1 != null)
					((RandomAccessFileOrArray) localObject1).close();
			}
		
//					}
		} catch (FileNotFoundException e) {
			
			e.printStackTrace();
		} catch (DocumentException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			if (doc != null) {
				doc.close();
			}
		}
	}
 
	/**
	 * 第一种解决方案 在不改变图片形状的同时，判断，如果h>w，则按h压缩，否则在w>h或w=h的情况下，按宽度压缩
	 * 
	 * @param h
	 * @param w
	 * @return
	 */
 
	public static int getPercent(float h, float w) {
		int p = 0;
		float p2 = 0.0f;
		if (h > w) {
			p2 = 210 / h * 279;
		} else {
			p2 = 210 / w * 279;
		}
		p = Math.round(p2);
		return p;
	}
}