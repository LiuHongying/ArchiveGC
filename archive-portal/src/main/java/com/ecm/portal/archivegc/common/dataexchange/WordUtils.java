package com.ecm.portal.archivegc.common.dataexchange;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.log4j.Logger;
import org.docx4j.Docx4J;
import org.docx4j.TraversalUtil;
import org.docx4j.convert.out.FOSettings;
import org.docx4j.finders.RangeFinder;
import org.docx4j.fonts.IdentityPlusMapper;
import org.docx4j.fonts.Mapper;
import org.docx4j.fonts.PhysicalFont;
import org.docx4j.fonts.PhysicalFonts;
import org.docx4j.jaxb.Context;
import org.docx4j.model.structure.HeaderFooterPolicy;
import org.docx4j.model.structure.SectionWrapper;
import org.docx4j.openpackaging.exceptions.Docx4JException;
import org.docx4j.openpackaging.packages.WordprocessingMLPackage;
import org.docx4j.openpackaging.parts.WordprocessingML.HeaderPart;
import org.docx4j.openpackaging.parts.WordprocessingML.MainDocumentPart;
import org.docx4j.org.apache.poi.util.IOUtils;
import org.docx4j.wml.Body;
import org.docx4j.wml.CTBookmark;
import org.docx4j.wml.P;
import org.docx4j.wml.R;
import org.docx4j.wml.RFonts;
import org.docx4j.wml.Tbl;
import org.docx4j.wml.Text;

import com.ecm.core.entity.EcmDocument;

public class WordUtils {
	/** 交换属性和签名 **/
	public static final int CHANGE_ATTR_AND_SIGN = 0;
	/** 只交换属性 **/
	public static final int CHANGE_ATTR = 1;
	/** 只交换签名 **/
	public static final int CHANGE_SIGN = 2;

	/** 模板路径 **/
	public String templatePath = System.getProperty("user.dir");
	/** 生成word文件存放路径 **/
	private String savePath = System.getProperty("user.dir");
	
	
	
	/**
	 * 是否数据交换支持类型
	 * @param obj
	 * @return
	 */
	public static boolean isSupportFormat(String formatName)
	{
		try {
			//String formatName = obj.getFormat()==null?"":obj.getFormat().getName();
			if("msw12".equals(formatName)||"msw12".equals(formatName)||"msw14".equals(formatName)||"msw15".equals(formatName)) {
				return true;
			}
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return false;
	}
	
	/**
	 * 交换数据
	 * 1:加载docx模板
	 * 2：读取模板中所有的标签 
	 * 3：根据标签获取相应的数据 
	 * 4：将数据替换到word文档中的相应位置
	 * @param doc 不是docx或者字段大小为0直接返回false
	 * @param flag 0：交换属性和签名；1：只交换属性；2：交换签名
	 * @return 
	 * ,String str1,String str2,String str3,String str4,String str5,String str6
	 */
	public boolean exchangeData(List<ArrayList<String>> list ,EcmDocument doc, int flag) throws Exception {
//		if(doc.getContentSize()==0){
//			return false;
//		}
		
		//log.debug("exchangeData_" + doc.getObjectId() + "_flag_" + flag);
		if(!templatePath.toLowerCase().endsWith(".docx")){
			return false;
		}
	//	List<ArrayList<String>> list = AttrUtils.getWorkitemAudits("");
		Map<String, String> textMap = new HashMap<String, String>();
		Map<String, List<ArrayList<String>>> tableMap = new HashMap<String, List<ArrayList<String>>>();
		WordprocessingMLPackage wordMLPackage = WordprocessingMLPackage.load(new java.io.File(templatePath));
//		if(doc.hasAttr("c_page_count")) {
//			org.docx4j.openpackaging.parts.DocPropsExtendedPart docPropsExtendedPart = wordMLPackage.getDocPropsExtendedPart();
//			org.docx4j.docProps.extended.Properties extendedProps = (org.docx4j.docProps.extended.Properties)docPropsExtendedPart.getJaxbElement(); 
//			if(extendedProps.getPages()>1) {
//				doc.setInt("c_page_count", extendedProps.getPages());
//			}
//		}
		// 正文内容处理
		MainDocumentPart documentPart = wordMLPackage.getMainDocumentPart();
		
		org.docx4j.wml.Document wmlDocumentEl = (org.docx4j.wml.Document) documentPart.getJaxbElement();
		
		Body body = wmlDocumentEl.getBody();
		List<Object> paragraphs = body.getContent();
		RangeFinder rt = new RangeFinder("CTBookmark", "CTMarkupRange");
		new TraversalUtil(paragraphs, rt);
		for (CTBookmark bm : rt.getStarts()) {
			if (bm.getName() == null) {
				continue;
			}
			if (bm.getName().startsWith("TBL")) { 
				tableMap.put(bm.getName(), AttrUtils.getDataByBookMarkNameForTable(list,doc, bm.getName()));
			} else {
				if(flag==CHANGE_ATTR){//只交换属性
					if(!bm.getName().startsWith("IMG")){
						textMap.put(bm.getName(),  AttrUtils.getDataByBookMarkName(list,doc, bm.getName()));
					}
				}else if(flag==CHANGE_SIGN){//只交换签名
					if(bm.getName().startsWith("IMG")){
						textMap.put(bm.getName(),  AttrUtils.getDataByBookMarkName(list,doc, bm.getName()));
					}
				}else{
					textMap.put(bm.getName(),  AttrUtils.getDataByBookMarkName(list,doc, bm.getName()));
				}
				if(bm.getName().startsWith("IMG")){
					textMap.put(bm.getName()+"_NAME",  AttrUtils.getDataByBookMarkName(list,doc, bm.getName()));
				}
				
			}
		}

		
		BMReplaceWithText.replaceBookmarkContents(body.getContent(), textMap, wordMLPackage, flag);
		
		//BMReplaceForTable.replaceBookmarkContents(body.getContent(), tableMap, wordMLPackage,flag);
		if(flag!=CHANGE_SIGN)
		{
			//页眉，定制 表格 处理 
			if(!BMReplaceForFooter.headerAddition(wordMLPackage, doc,textMap,list))
			{
				//非定制 表格 处理 
				List<SectionWrapper> sectionWrappers = wordMLPackage.getDocumentModel().getSections();
				//遍历所有章节
				for (SectionWrapper sw : sectionWrappers) {
	
					HeaderFooterPolicy hfp = sw.getHeaderFooterPolicy();
					if (hfp.getDefaultHeader()!=null) {
						HeaderPart dhp = hfp.getDefaultHeader();
						if(dhp!=null)
						{
							List<Object> paragraphs2 = dhp.getContent();
							RangeFinder rt2 = new RangeFinder("CTBookmark", "CTMarkupRange");
							new TraversalUtil(paragraphs2, rt2);
							for (CTBookmark bm : rt2.getStarts()) {
								textMap.put(bm.getName(),  AttrUtils.getDataByBookMarkName(list,doc, bm.getName()));
							}
							BMReplaceWithText.replaceBookmarkContents(paragraphs2, textMap, wordMLPackage,flag);
						}
					}
				}
			}
		}

		wordMLPackage.save(new java.io.File(templatePath));
	
		return true;
	}
	


	
	private static void setTextValue(P p,String key,String val)
	{
		p.getContent().clear();
		R run = BMReplaceWithText.factory.createR();
		org.docx4j.wml.Text t = BMReplaceWithText.factory.createText();
		run.getContent().add(t);
		t.setValue(val);
		t.setSpace(key);
		p.getContent().add(run);
	}
	
	
	
	public String getSavePath()
	{
		return savePath;
	}

	public void setSavePath(String savePath)
	{
		this.savePath = savePath;
	}

	
	
	public String getTemplatePath()
	{
		return templatePath;
	}

	public void setTemplatePath(String templatePath)
	{
		this.templatePath = templatePath;
	}
	
	
	public void convertDocxToPDF(String docxPath, String pdfPath) throws Exception {  
        OutputStream os = null;  
        try {  
            WordprocessingMLPackage mlPackage = WordprocessingMLPackage.load(new File(docxPath));  
            configSimSunFont(mlPackage);
            Mapper fontMapper = new IdentityPlusMapper();  
            fontMapper.put("华文行楷", PhysicalFonts.get("STXingkai"));  
            fontMapper.put("华文仿宋", PhysicalFonts.get("STFangsong"));  
            fontMapper.put("隶书", PhysicalFonts.get("LiSu"));  
            mlPackage.setFontMapper(fontMapper);  
            os = new java.io.FileOutputStream(pdfPath);  
  
            FOSettings foSettings = Docx4J.createFOSettings();  
            foSettings.setWmlPackage(mlPackage);  
            Docx4J.toFO(foSettings, os, Docx4J.FLAG_EXPORT_PREFER_XSL);  
  
        }catch(Exception ex){  
            ex.printStackTrace();  
        }finally {  
            IOUtils.closeQuietly(os);  
        }  
    }  
	
	protected void configSimSunFont(WordprocessingMLPackage wordMLPackage) throws Exception {
        Mapper fontMapper = new IdentityPlusMapper();
        wordMLPackage.setFontMapper(fontMapper);

        String fontFamily = "SimSun";

        URL simsunUrl = this.getClass().getResource("/SIMSUN.TTC"); //加载字体文件（解决linux环境下无中文字体问题）
        PhysicalFonts.addPhysicalFonts(fontFamily, simsunUrl);
        PhysicalFont simsunFont = PhysicalFonts.get(fontFamily);
        fontMapper.put(fontFamily, simsunFont);

        RFonts rfonts = Context.getWmlObjectFactory().createRFonts(); //设置文件默认字体
        rfonts.setAsciiTheme(null);
        rfonts.setAscii(fontFamily);
        wordMLPackage.getMainDocumentPart().getPropertyResolver()
                .getDocumentDefaultRPr().setRFonts(rfonts);
    }

	
}
