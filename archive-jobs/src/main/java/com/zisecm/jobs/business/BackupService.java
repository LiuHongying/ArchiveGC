package com.zisecm.jobs.business;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import com.alibaba.druid.util.StringUtils;
import com.ecm.common.util.DateUtils;
import com.ecm.common.util.ExcelUtil;
import com.ecm.common.util.JSONUtils;
import com.ecm.core.cache.manager.CacheManagerOper;
import com.ecm.core.entity.EcmDocument;
import com.ecm.core.entity.EcmForm;
import com.ecm.core.entity.EcmFormItem;
import com.ecm.core.entity.EcmGridView;
import com.ecm.core.entity.EcmGridViewItem;
import com.ecm.core.exception.AccessDeniedException;
import com.ecm.core.exception.EcmException;
import com.ecm.core.service.AuthService;
import com.ecm.core.service.ContentService;
import com.ecm.core.service.DocumentService;
import com.ecm.core.service.UserService;
import com.ecm.icore.service.IEcmSession;
@Service
public class BackupService {
	 	@Autowired
	    private DocumentService documentService;
	 	@Autowired
	    private ContentService contentService;
	 	
	    @Autowired
		private AuthService authService;
	    @Autowired
		private Environment env;
	    @Autowired
	    private UserService userService;
	    private Logger logger = LoggerFactory.getLogger(this.getClass());
	   	    
	    private void exportExcel(String path,String typeName,List<Map<String,Object>> data) {

			ExcelUtil excelUtil = new ExcelUtil();
			List<Object[]> datalist = new ArrayList<Object[]>();
			
//			String typeName= conditions.substring(conditions.indexOf("='")+2,conditions.indexOf("' "));
			EcmForm frm = CacheManagerOper.getEcmForms().get(typeName + "_1");
			
			List<EcmFormItem> formItems= frm.getEcmFormItems();
			List<String> fieldNames=new ArrayList<String>();
			List<String> attrs=new ArrayList<String>();
			for(int i=0;i<formItems.size();i++) {
				EcmFormItem item=formItems.get(i);
				if(item.getIsHide()) {
					continue;
				}
				fieldNames.add(item.getLabel());
				attrs.add(item.getAttrName());
			}
			String []res = fieldNames.toArray(new String[fieldNames.size()]);
			try {
				List<Object[]> grid=new ArrayList<Object[]>();
				for (int i=0;data!=null&&i<data.size();i++) {
					Map<String, Object> map = data.get(i);
					List<Object> row=new ArrayList<>();
					for(String attr:attrs) {
						Object o=map.get(attr);
						row.add(o);
					}
					grid.add(row.toArray());
				}
				excelUtil.makeExcel(path,"Export", res, grid);
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			
		
	    }
	    
	    @Scheduled(cron = "0/5 * * * * ?")
	    public void backup() {
	    	System.out.println("-----"+new Date()+"-------job开始---------");
	    	IEcmSession ecmSession = null;
			String workflowSpecialUserName = env.getProperty("ecm.username");
			try {
				ecmSession = authService.login("jobs", workflowSpecialUserName, env.getProperty("ecm.password"));
				List<Map<String,Object>> objs= documentService.getObjectMap(ecmSession.getToken(), "TYPE_NAME='备份批次'  and STATUS='制作中'");
				for(int i=0;i<objs.size();i++) {
					Map<String,Object> obj=objs.get(i);
					float totalSize=Float.parseFloat(obj.get("C_PAGE_COUNT").toString()) ;
					float sumsize=0;
					String coding=obj.get("CODING").toString();
					//String condition= obj.get("C_DESC1").toString();//查询条件
					String condition = "TYPE_NAME='供应商管理案卷' and coding = 'TEST-01211018'";
					String path= obj.get("TITLE").toString();//路径
					List<Map<String, Object>> boxes= 
							documentService.getObjectMap(ecmSession.getToken(), " "+condition+" order by coding");
					String typeName=condition.substring(condition.indexOf("='")+2,condition.indexOf("' "));
					
					int recordNum=1;
					String recordNumStr=String.format("%04d", recordNum);
					String recordCoding=coding+"-"+recordNumStr;
					EcmDocument ecmDoc=new EcmDocument();
					ecmDoc.setCoding(recordCoding);
					ecmDoc.addAttribute("C_BATCH_CODING", coding);
					
					ecmDoc.setName(obj.get("NAME").toString());
					ecmDoc.setTitle(obj.get("TITLE").toString()+"/"+obj.get("CODING")+"/"+recordNumStr);
					ecmDoc.setStatus("制作中");
					ecmDoc.setTypeName("备份记录");
					String ecmDocId= documentService.newObject(ecmSession.getToken(), ecmDoc);
					exportExcel(obj.get("TITLE").toString()+"/"+obj.get("CODING")+"\\"+obj.get("CODING")+".xlsx",
							typeName, boxes);			//第一层EXCEL，会把所有文件都导出来
					if(typeName.contains("案卷")) {
						List<Map<String,Object>> expboxes=new ArrayList<>();
						for(int j=0;j<boxes.size();j++) {

							try {
								Map<String, Object> box= boxes.get(j);
								float f= getBoxsize(box.get("ID").toString(),ecmSession.getToken());
								float contentsize = getContentSize4Export(box.get("ID").toString(),ecmSession.getToken());
								float total = f + contentsize;
								if(totalSize-total>f) {
									sumsize+=f;
									//查询盒中所有的content
									expboxes.add(box);
									/*******************************盒内文件*************************************************/
									String sqlFile="select b.*,a.STORE_NAME,a.FILE_PATH,a.FORMAT_NAME from ecm_content a,ecm_document b,ecm_relation c "
											+ "where a.parent_id=b.id and b.id=c.child_id and b.type_name!='图册'"
											+ " and c.parent_id='"+box.get("ID").toString()+"'";
									String sql = "select b.* from ecm_document b,ecm_relation c "
											+ "where b.id=c.child_id and b.type_name!='图册'"
											+ " and b.id='"+box.get("ID").toString()+"'";//查询总map
																											
									List<Map<String, Object>> contents= documentService.getMapList(ecmSession.getToken(), sqlFile);
									List<Map<String,Object>> res = documentService.getMapList(ecmSession.getToken(), sql);
									String childTypeName="";
									childTypeName=contents.get(0).get("TYPE_NAME").toString();
									exportExcel(obj.get("TITLE").toString()+"/"+obj.get("CODING")+"/"+recordNumStr+"/"+box.get("CODING").toString()
											+"\\"+recordCoding+".xlsx",
											childTypeName, res);
//									if(contents!=null&&contents.size()>0) {
////										String childTypeName="";
////										childTypeName=contents.get(0).get("TYPE_NAME").toString();
////										exportExcel(obj.get("TITLE").toString()+"/"+obj.get("CODING")+"/"+recordNumStr+"/"+box.get("CODING").toString()
////												+"\\"+recordCoding+".xlsx",
////												childTypeName, res);
//									}
									//以上操作，不管有没有电子文件都会生成全部excel
									//开始导出文件
									for(Map<String,Object> c:contents) {
										//InputStream fis = null;
										String fullPath = CacheManagerOper.getEcmStores().get(c.get("STORE_NAME").toString()).getStorePath();
										File fil = new File(fullPath+c.get("FILE_PATH").toString());
										//fis = new BufferedInputStream(new FileInputStream(fil));
										byte[] buffer = new byte[1024];
										FileInputStream fis = null; //文件输入流
										BufferedInputStream bis = null;
										fis = new FileInputStream(fil); 
										bis = new BufferedInputStream(fis);
										
										File outFile=new File(obj.get("TITLE").toString()
												+"/"+obj.get("CODING")+"/"+recordNumStr+"/"+box.get("CODING").toString()+"\\"+c.get("CODING")+"."+c.get("FORMAT_NAME"));

										if (!outFile.exists()) {
											try {
												boolean fc= outFile.createNewFile();
												
										    } catch (IOException e) {
										        // TODO Auto-generated catch block
										    	File dir=new File(obj.get("TITLE").toString()
														+"/"+obj.get("CODING")+"/"+recordNumStr+"/"+box.get("CODING").toString());
										    	logger.error("请注意标题为："+obj.get("TITLE").toString()+" 的文件不存在!");
												dir.mkdirs();
												outFile.createNewFile();
//										        e.printStackTrace();
										    }

								        }
										OutputStream out=new BufferedOutputStream(new FileOutputStream(fil));
							            int x = bis.read(buffer);
							            while(x != -1){
							                out.write(buffer);
							                x = bis.read(buffer);
							            }
							            bis.close();
							            fis.close();
										out.flush();
										out.close();
										fis.close();
										
									}
									/******************************end 盒内文件**********************************************************/
									
									
//									/**888888888888888888888888盒内图册及文件888888888888888888888888888888888888888888*/
//									String sqlVolumns="select b.*  from ecm_document b,ecm_relation c "
//											+ "where b.id=c.child_id and b.type_name='图册'"
//											+ " and c.parent_id='"+box.get("ID").toString()+"'";
//									
//									List<Map<String, Object>> volumns= documentService.getMapList(ecmSession.getToken(), sqlVolumns);
//									
//									if(volumns!=null&&volumns.size()>0) {
//										exportExcel(obj.get("TITLE").toString()+"/"+obj.get("CODING")+"/"+recordNumStr
//												+"/"+box.get("CODING").toString()
//												+"\\"+recordCoding+"图册.xlsx",
//												"图册", contents);
//									}
//									
//									//开始导出文件
//									for(Map<String,Object> c:volumns) {
//										
//										File dir=new File(obj.get("TITLE").toString()
//												+"/"+obj.get("CODING")+"/"+recordNumStr+"/"+box.get("CODING").toString()+"/"+c.get("CODING"));
//
//										if (!dir.exists()) {
//											dir.mkdirs();
//										}
//										String sqlVolumnsFile="select b.*,a.STORE_NAME,a.FILE_PATH,FORMAT_NAME from ecm_content a,ecm_document b,ecm_relation c "
//												+ "where a.parent_id=b.id and b.id=c.child_id and b.type_name='图册'"
//												+ " and c.parent_id='"+c.get("ID").toString()+"'";
//										
//										List<Map<String,Object>> volumnsFiles=documentService.getMapList(ecmSession.getToken(), sqlVolumnsFile);
//										
//										for(Map<String,Object> file:volumnsFiles) {
//											InputStream fis = null;
//											String fullPath = CacheManagerOper.getEcmStores().get(c.get("STORE_NAME").toString()).getStorePath();
//											File fil = new File(fullPath+c.get("FILE_PATH").toString());
//											fis = new BufferedInputStream(new FileInputStream(fil));
//											byte[] data=new byte[ fis.available()];
//											
//											
//											File outFile=new File(obj.get("TITLE").toString()
//													+"/"+obj.get("CODING")+"/"+recordNumStr+"/"+box.get("CODING").toString()+"/"+c.get("CODING").toString()
//													+"\\"+file.get("CODING").toString()+"."+file.get("FORMAT_NAME"));
//
//											if (!outFile.exists()) {
//												try {
//													boolean fc= outFile.createNewFile();
//													
//											    } catch (IOException e) {
//											        // TODO Auto-generated catch block
//											    	File dirs=new File(obj.get("TITLE").toString()
//															+"/"+obj.get("CODING")+"/"+recordNumStr+"/"+box.get("CODING").toString()+"/"+c.get("CODING").toString());
//													dirs.mkdirs();
//													outFile.createNewFile();
////											        e.printStackTrace();
//											    }
//
//									        }
//											OutputStream out=new BufferedOutputStream(new FileOutputStream(outFile));
//											out.write(data);
//											out.flush();
//											out.close();
//											fis.close();
//										}
//										
//										
//									}
									/*******************************附件*************************************************/
									String sqlContent="select b.*,a.STORE_NAME,a.FILE_PATH,a.FORMAT_NAME from ecm_content a,ecm_document b"
											+ " where a.parent_id in (select child_id as parent_id from ecm_relation where parent_id ='"+box.get("ID").toString()+"') and a.parent_id = b.id";
									
									//System.out.println(sqlFile);
									List<Map<String, Object>> fj= documentService.getMapList(ecmSession.getToken(), sqlContent);
									exportExcel(obj.get("TITLE").toString()+"/"+obj.get("CODING")+"/"+recordNumStr
											+"/"+box.get("CODING").toString()
											+"\\"+recordCoding+"附件.xlsx",
											"设计文件", fj);					//这个是测试阶段的gridname，对应附件可单独调typename

									
									int k = 1;
									//开始导出文件
									for(Map<String,Object> c:fj) {
										//InputStream fis = null;
										String fullPath = CacheManagerOper.getEcmStores().get(c.get("STORE_NAME").toString()).getStorePath();
										File fil = new File(fullPath+c.get("FILE_PATH").toString());
										//fis = new BufferedInputStream(new FileInputStream(fil));
										byte[] buffer = new byte[1024];
										FileInputStream fis = null; //文件输入流
										BufferedInputStream bis = null;
										fis = new FileInputStream(fil); 
										bis = new BufferedInputStream(fis);
										
										
										File outFile=new File(obj.get("TITLE").toString()
												+"/"+obj.get("CODING")+"/"+recordNumStr+"\\"+box.get("CODING").toString()+"/"+c.get("CODING")+"_附件"+k+"."+c.get("FORMAT_NAME"));
										k++;
										if (!outFile.exists()) {
											try {
												boolean fc= outFile.createNewFile();
												
										    } catch (IOException e) {
										        // TODO Auto-generated catch block
										    	File dir=new File(obj.get("TITLE").toString()
														+"/"+obj.get("CODING")+"/"+recordNumStr+"/"+box.get("CODING").toString());
										    	logger.error("请注意标题为："+obj.get("TITLE").toString()+" 的文件不存在!");
												dir.mkdirs();
												outFile.createNewFile();
//										        e.printStackTrace();
										    }

								        }
										OutputStream out=new BufferedOutputStream(new FileOutputStream(fil));
							            int x = bis.read(buffer);
							            while(x != -1){
							                out.write(buffer);
							                x = bis.read(buffer);
							            }
							            bis.close();
							            fis.close();
										out.flush();
										out.close();
										fis.close();
										
									} 
									
									/*******************************附件*************************************************/
									
									/***************************end 盒内图册及文件***************************************************/
									
								}else if(j==boxes.size()-1) {
									recordNumStr=String.format("%04d", recordNum);
									recordCoding=coding+"-"+recordNumStr;
									exportExcel(obj.get("TITLE").toString()+"/"+obj.get("CODING")+"/"+recordNumStr+"\\"+recordCoding+".xlsx",
											typeName, expboxes);
								}else {
									recordNumStr=String.format("%04d", recordNum);
									recordCoding=coding+"-"+recordNumStr;
									exportExcel(obj.get("TITLE").toString()+"/"+obj.get("CODING")+"/"+recordNumStr+"\\"+recordCoding+".xlsx",
											typeName, expboxes);
									//创建一条新的记录
									recordNum++;
									sumsize=0;
									EcmDocument backupBach= documentService.getObjectById(ecmSession.getToken(), ecmDocId);
									backupBach.setStatus("已完成");
									documentService.updateObject(ecmSession.getToken(), backupBach,null);
									recordNumStr=String.format("%04d", recordNum);
									recordCoding=coding+"-"+recordNumStr;
									EcmDocument ecmDocNew=new EcmDocument();
									ecmDocNew.setCoding(recordCoding);
									ecmDocNew.addAttribute("C_BATCH_CODING", coding);
									
									ecmDocNew.setName(obj.get("NAME").toString());
									ecmDocNew.setTitle(obj.get("TITLE").toString()+"/"+obj.get("CODING")+"/"+recordNumStr);
									ecmDocNew.setStatus("制作中");
									ecmDocNew.setTypeName("备份记录");
									ecmDocId= documentService.newObject(ecmSession.getToken(), ecmDocNew);
									j--;
								}
							}catch (Exception e) {
								// TODO: handle exception
								e.printStackTrace();
								continue;
							}
						
						}
						
					}
					
//					else if("图册".equals(typeName)) {
//
//						List<Map<String,Object>> expboxes=new ArrayList<>();
//						for(int j=0;j<boxes.size();j++) {
//
//							try {
//								Map<String, Object> box= boxes.get(j);
//								float f= getBoxsize(box.get("ID").toString(),ecmSession.getToken());
//								if(totalSize-sumsize>f) {
//									sumsize+=f;
//									//查询盒中所有的content
//									expboxes.add(box);
//									/*******************************卷内文件*************************************************/
//									String sqlFile="select b.*,a.STORE_NAME,a.FILE_PATH,FORMAT_NAME from ecm_content a,ecm_document b,ecm_relation c "
//											+ "where a.parent_id=b.id and b.id=c.child_id "
//											+ " and c.parent_id='"+box.get("ID").toString()+"'";
//									
//									List<Map<String, Object>> contents= documentService.getMapList(ecmSession.getToken(), sqlFile);
//									
//									if(contents!=null&&contents.size()>0) {
//										String childTypeName="";
//										childTypeName=contents.get(0).get("TYPE_NAME").toString();
//										exportExcel(obj.get("TITLE").toString()+"/"+obj.get("CODING")+"/"+recordNumStr+"/"+box.get("CODING").toString()
//												+"\\"+recordCoding+".xlsx",
//												childTypeName, contents);
//									}
//									
//									//开始导出文件
//									for(Map<String,Object> c:contents) {
//										InputStream fis = null;
//										String fullPath = CacheManagerOper.getEcmStores().get(c.get("STORE_NAME").toString()).getStorePath();
//										File fil = new File(fullPath+c.get("FILE_PATH").toString());
//										fis = new BufferedInputStream(new FileInputStream(fil));
//										byte[] data=new byte[ fis.available()];
//										
//										
//										File outFile=new File(obj.get("TITLE").toString()
//												+"/"+obj.get("CODING")+"/"+recordNumStr+"/"+box.get("CODING").toString()+"\\"+c.get("CODING")+"."+c.get("FORMAT_NAME"));
//
//										if (!outFile.exists()) {
//											try {
//												boolean fc= outFile.createNewFile();
//												
//										    } catch (IOException e) {
//										        // TODO Auto-generated catch block
//										    	File dir=new File(obj.get("TITLE").toString()
//														+"/"+obj.get("CODING")+"/"+recordNumStr+"/"+box.get("CODING").toString());
//												dir.mkdirs();
//												outFile.createNewFile();
////										        e.printStackTrace();
//										    }
//
//								        }
//										OutputStream out=new BufferedOutputStream(new FileOutputStream(outFile));
//										out.write(data);
//										out.flush();
//										out.close();
//										fis.close();
//										
//									}
//									/******************************end 卷内文件**********************************************************/
//									
//									
//								}else if(j==boxes.size()-1) {
//									recordNumStr=String.format("%04d", recordNum);
//									recordCoding=coding+"-"+recordNumStr;
//									exportExcel(obj.get("TITLE").toString()+"/"+obj.get("CODING")+"/"+recordNumStr+"\\"+recordCoding+".xlsx",
//											typeName, expboxes);
//								}else {
//									recordNumStr=String.format("%04d", recordNum);
//									recordCoding=coding+"-"+recordNumStr;
//									exportExcel(obj.get("TITLE").toString()+"/"+obj.get("CODING")+"/"+recordNumStr+"\\"+recordCoding+".xlsx",
//											typeName, expboxes);
//									//创建一条新的记录
//									recordNum++;
//									sumsize=0;
//									EcmDocument backupBach= documentService.getObjectById(ecmSession.getToken(), ecmDocId);
//									backupBach.setStatus("已完成");
//									documentService.updateObject(ecmSession.getToken(), backupBach,null);
//									recordNumStr=String.format("%04d", recordNum);
//									recordCoding=coding+"-"+recordNumStr;
//									EcmDocument ecmDocNew=new EcmDocument();
//									ecmDocNew.setCoding(recordCoding);
//									ecmDocNew.addAttribute("C_BATCH_CODE", coding);
//									
//									ecmDocNew.setName(obj.get("NAME").toString());
//									ecmDocNew.setTitle(obj.get("TITLE").toString()+"/"+obj.get("CODING")+"/"+recordNumStr);
//									ecmDocNew.setStatus("制作中");
//									ecmDocNew.setTypeName("备份记录");
//									ecmDocId= documentService.newObject(ecmSession.getToken(), ecmDocNew);
//									j--;
//								}
//							}catch (Exception e) {
//								// TODO: handle exception
//								continue;
//							}
//						
//						}
//						
//					
//					}
					
					else {


						List<Map<String,Object>> expboxes=new ArrayList<>();
						for(int j=0;j<boxes.size();j++) {

							try {
								Map<String, Object> box= boxes.get(j);
								float f= getContentSize(box.get("ID").toString(),ecmSession.getToken());
								float contentsize = getSingelContent(box.get("ID").toString(),ecmSession.getToken());	//算附件大小
								float total = f + contentsize;
								if(totalSize-total>f) {			//这个时候确认文件夹够大
									sumsize+=f;
									//查询盒中所有的content
									expboxes.add(box);
									/*******************************附件*************************************************/
									String sqlContent="select b.*,a.STORE_NAME,a.FILE_PATH,a.FORMAT_NAME from ecm_content a,ecm_document b"
											+ " where a.parent_id in (select child_id as parent_id from ecm_relation where parent_id ='"+box.get("ID").toString()+"') and a.parent_id = b.id";
									
									//System.out.println(sqlFile);
									List<Map<String, Object>> fj= documentService.getMapList(ecmSession.getToken(), sqlContent);
									exportExcel(obj.get("TITLE").toString()+"/"+obj.get("CODING")+"/"+recordNumStr
											+"/"+box.get("CODING").toString()
											+"\\"+recordCoding+"附件.xlsx",
											"设计文件", fj);					//这个是测试阶段的gridname，对应附件可单独调typename

									
									int k = 1;
									//开始导出文件
									for(Map<String,Object> c:fj) {
										InputStream fis = null;
										String fullPath = CacheManagerOper.getEcmStores().get(c.get("STORE_NAME").toString()).getStorePath();
										File fil = new File(fullPath+c.get("FILE_PATH").toString());
										fis = new BufferedInputStream(new FileInputStream(fil));
										byte[] data=new byte[ fis.available()];
										
										
										File outFile=new File(obj.get("TITLE").toString()
												+"/"+obj.get("CODING")+"/"+recordNumStr+"\\"+box.get("CODING").toString()+"/"+c.get("CODING")+"_附件"+k+"."+c.get("FORMAT_NAME"));
										k++;
										if (!outFile.exists()) {
											try {
												boolean fc= outFile.createNewFile();
												
										    } catch (IOException e) {
										        // TODO Auto-generated catch block
										    	File dir=new File(obj.get("TITLE").toString()
														+"/"+obj.get("CODING")+"/"+recordNumStr+"\\"+box.get("CODING").toString());
										    	//logger.error("请注意标题为："+obj.get("TITLE").toString()+" 的文件不存在!");
												dir.mkdirs();
												outFile.createNewFile();
										        e.printStackTrace();
										    }

								        }
										FileOutputStream downloadFile = new FileOutputStream(outFile);
										downloadFile.write(data);
										downloadFile.flush();
										downloadFile.close();
										fis.close();
										
									} 
									/*******************************附件*************************************************/
									/*******************************文件*************************************************/
									String sqlFile="select b.*,a.STORE_NAME,a.FILE_PATH,a.FORMAT_NAME from ecm_content a,ecm_document b  "
											+ "where a.parent_id=b.id and b.id='"+box.get("ID").toString()+"'";
									
									//System.out.println(sqlFile);
									List<Map<String, Object>> contents= documentService.getMapList(ecmSession.getToken(), sqlFile);
									
									
									
									//开始导出文件
									for(Map<String,Object> c:contents) {
										InputStream fis = null;
										String fullPath = CacheManagerOper.getEcmStores().get(c.get("STORE_NAME").toString()).getStorePath();
										File fil = new File(fullPath+c.get("FILE_PATH").toString());
										fis = new BufferedInputStream(new FileInputStream(fil));
										byte[] data=new byte[ fis.available()];
										
										
										File outFile=new File(obj.get("TITLE").toString()
												+"/"+obj.get("CODING")+"/"+recordNumStr+"\\"+c.get("CODING")+"."+c.get("FORMAT_NAME"));

										if (!outFile.exists()) {
											try {
												boolean fc= outFile.createNewFile();
												
										    } catch (IOException e) {
										        // TODO Auto-generated catch block
										    	File dir=new File(obj.get("TITLE").toString()
														+"/"+obj.get("CODING")+"/"+recordNumStr);
										    	logger.error("请注意标题为："+obj.get("TITLE").toString()+" 的文件不存在!");
												dir.mkdirs();
												outFile.createNewFile();
												continue;
//										        e.printStackTrace();
										    }

								        }
										OutputStream out=new BufferedOutputStream(new FileOutputStream(outFile));
										out.write(data);
										out.flush();
										out.close();
										fis.close();
										
									}
									/******************************end 卷内文件**********************************************************/
									
									//导出文件
								}
								else if(j==boxes.size()-1) {
									recordNumStr=String.format("%04d", recordNum);
									recordCoding=coding+"-"+recordNumStr;
									exportExcel(obj.get("TITLE").toString()+"/"+obj.get("CODING")+"/"+recordNumStr+"\\"+recordCoding+".xlsx",
											typeName, expboxes);
								}else {
									recordNumStr=String.format("%04d", recordNum);
									recordCoding=coding+"-"+recordNumStr;
									exportExcel(obj.get("TITLE").toString()+"/"+obj.get("CODING")+"/"+recordNumStr+"\\"+recordCoding+".xlsx",
											typeName, expboxes);
									//创建一条新的记录
									recordNum++;
									total=0;
									EcmDocument backupBach= documentService.getObjectById(ecmSession.getToken(), ecmDocId);
									backupBach.setStatus("已完成");
									documentService.updateObject(ecmSession.getToken(), backupBach,null);
									recordNumStr=String.format("%04d", recordNum);
									recordCoding=coding+"-"+recordNumStr;
									EcmDocument ecmDocNew=new EcmDocument();
									ecmDocNew.setCoding(recordCoding);
									ecmDocNew.addAttribute("C_BATCH_CODE", coding);
									
									ecmDocNew.setName(obj.get("NAME").toString());
									ecmDocNew.setTitle(obj.get("TITLE").toString()+"/"+obj.get("CODING")+"/"+recordNumStr);
									ecmDocNew.setStatus("制作中");
									ecmDocNew.setTypeName("备份记录");
									ecmDocId= documentService.newObject(ecmSession.getToken(), ecmDocNew);
									j--;
								}
							}catch (Exception e) {
								// TODO: handle exception
								e.printStackTrace();
								continue;
							}
						
						}
						
					
					
					}
					
					
				//	documentService.updateStatus(ecmSession.getToken(), obj.get("ID").toString(), "已完成"); //该记录已完成
					
				}
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}finally {
				if(ecmSession!=null) {
					authService.logout(ecmSession.getToken());
				}
			}
			System.out.println("-----------"+new Date()+"--------job end-----------------");
	    }
	    public float getSingelContent(String id,String token) {			//导出单个文件的时候计算总大小
	    	String sql = "select child_id from ecm_relation where parent_id = '"+id+"'";
	    	float ress = 0;
			try {
				List<Map<String,Object>> objs= documentService.getMapList(token, sql);
				if(objs!=null&&objs.size()>0&&objs.get(0)!=null) {
					for(int i=0;i<objs.size();i++) {
					Map<String,Object> res= objs.get(i);
					String objId = res.get("child_id").toString();
					String sql4content = "select sum(content_size) as boxsize from ecm_content where PARENT_ID in (select ID as PARENT_ID from ecm_document ed where ID='"+objId+"')";
					List<Map<String,Object>> result= documentService.getMapList(token, sql4content);
					Map<String,Object> boxObj= result.get(0);
					float f = Float.parseFloat(boxObj.get("boxsize").toString());
					f=f/1024/1024;
					ress = ress + f;}
					return ress;
				}
			}catch (Exception e) {
				// TODO: handle exception
				return 0;
			}
		return 0;
	    }
	    
	    public float getContentSize(String id,String token) {
	    	String sql = "select content_size from ecm_content where PARENT_ID in (select ID as PARENT_ID from ecm_document ed where ID='"+id+"')";
			try {
				List<Map<String,Object>> objs= documentService.getMapList(token, sql);
				if(objs!=null&&objs.size()>0&&objs.get(0)!=null) {
					Map<String,Object> boxObj= objs.get(0);
					
					float f= Float.parseFloat(boxObj.get("content_size").toString());
					f=f/1024/1024;
					return f;
				}
			}catch (Exception e) {
				// TODO: handle exception
				return 0;
			}
		return 0;
    }
	    public float getBoxsize(String boxId,String token) {
	    	String sql="select sum(CONTENT_SIZE) as boxsize from ecm_content where PARENT_ID in"
	    			+ "(select child_id as PARENT_ID from ecm_relation where PARENT_ID ='"+boxId+"')";
			try {
					List<Map<String,Object>> objs= documentService.getMapList(token, sql);
					if(objs!=null&&objs.size()>0&&objs.get(0)!=null) {
						Map<String,Object> boxObj= objs.get(0);
						
						float f= Float.parseFloat(boxObj.get("boxsize").toString());
						f=f/1024/1024;
						return f;
					}
				}catch (Exception e) {
					// TODO: handle exception
					return 0;
				}
			return 0;
	    }
	    public float getContentSize4Export(String boxId,String token) {		//算一下附件总大小
	    	String sql="select child_id from ecm_relation where PARENT_ID = '"+boxId+"'";
			float res = 0;
	    	try {
				List<Map<String,Object>> objs= documentService.getMapList(token, sql);
				if(objs!=null&&objs.size()>0&&objs.get(0)!=null) {
					for(int i = 0;i < objs.size();i++) {
						Map<String,Object> mp = objs.get(i);
						String id = mp.get("child_id").toString();
						String sql4content = "select sum(CONTENT_SIZE) as boxsize from ecm_content where PARENT_ID in"
				    			+ "(select child_id as PARENT_ID from ecm_relation where PARENT_ID ='"+id+"')";
						List<Map<String,Object>> result= documentService.getMapList(token, sql4content);
						Map<String,Object> boxObj= result.get(0);
						float f = Float.parseFloat(boxObj.get("boxsize").toString());
						f=f/1024/1024;
						res = res + f;
						}
					return res;
				}
			}catch (Exception e) {
				// TODO: handle exception
				return 0;
			}
		return 0;
	    }
}
