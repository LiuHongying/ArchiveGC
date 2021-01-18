package com.ecm.portal.archivecd.event;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import com.ecm.core.PermissionContext.ObjectPermission;
import com.ecm.core.entity.EcmDocument;
import com.ecm.core.entity.EcmRelation;
import com.ecm.core.exception.AccessDeniedException;
import com.ecm.core.exception.EcmException;
import com.ecm.core.exception.NoPermissionException;
import com.ecm.core.service.DocumentService;
import com.ecm.core.service.RelationService;

@Component
public class Distribute {
	@Autowired
	DocumentService documentService;
	@Autowired
	RelationService relationService;
	/**
	 * 母单
	 * @param token 
	 * @param docId 文件Id
	 * @param sender 发送人
	 * @param mainReceiver 主送
	 * @param subReceiver 抄送
	 * @param reader 阅知
	 * @return
	 * @throws Exception
	 */
	public EcmDocument orderForMainDistribution(String token,String docId,String sender,
			String mainReceiver,String subReceiver,String reader) throws Exception {
		EcmDocument doc= documentService.getObjectById(token, docId);
		return orderForMainDistribution(token,doc,sender,mainReceiver,subReceiver,reader);
	}
	/**
	 * 母单
	 * @param token
	 * @param doc 文档对象
	 * @param sender 发送人
	 * @param mainReceiver 主送
	 * @param subReceiver 抄送
	 * @param reader 阅知
	 * @return
	 * @throws Exception
	 */
	public EcmDocument orderForMainDistribution(String token,EcmDocument doc,Object sender,Object mainReceiver,
			Object subReceiver,Object reader) throws Exception {
		EcmDocument order = new EcmDocument();
		SimpleDateFormat df=new SimpleDateFormat("yyyy-MM-dd");
		String dt= df.format(new Date());
		order.setName(doc.getName()+"的分发单，分发日期"+dt);
		order.addAttribute("C_HOST", mainReceiver);//主送
		order.addAttribute("C_PARTICIPATION", subReceiver);//抄送
		order.addAttribute("C_COPY_TO", reader);//阅知
		order.addAttribute("C_FROM_CODING", sender);//发送人
		order.setTypeName("分发单");
		order.setStatus("新建");
		documentService.newObject(token, order, null);
		EcmRelation relation=new EcmRelation();
		relation.setParentId(order.getId());
		relation.setChildId(doc.getId());
		relation.setName("irel_children");
		relationService.newObject(token, relation);
		documentService.newAudit(token, "Portal", "新建", order.getId(), null, null);
		return order;
	}
	/**
	 * 修改分发单状态
	 * @param token
	 * @param orderId
	 * @param status
	 * @return
	 * @throws Exception 
	 */
	public EcmDocument updateOrderForDistributionStatus(String token,String orderId,String status)
			throws Exception {
		EcmDocument order= documentService.getObjectById(token, orderId);
		return updateOrderForDistributionStatus(token, order, status);
	}
	/**
	 * 修改分发单状态
	 * @param token 
	 * @param order 分发单对象
	 * @param status 目标状态
	 * @return
	 * @throws Exception 
	 */
	@Transactional(rollbackFor = Exception.class)
	public EcmDocument updateOrderForDistributionStatus(String token,EcmDocument order,String status)
			throws Exception {
		
		if(status!=null&&"已生效".equals(status)) {
			order.setStatus(status);
			order.addAttribute("IS_RELEASED", "1");
			documentService.updateObject(token, order, null);
			String host= order.getAttributeValue("C_HOST")!=null?order.getAttributeValue("C_HOST").toString():"";
			String participation= order.getAttributeValue("C_PARTICIPATION")!=null?order.getAttributeValue("C_PARTICIPATION").toString():"";
			String copyTo= order.getAttributeValue("C_COPY_TO")!=null?order.getAttributeValue("C_COPY_TO").toString():"";
			if(!"".equals(host)||!"".equals(participation)||!"".equals(copyTo)) {
				
				List<String> users=new ArrayList<>();
				String[] hosts= host.split(";");
				String[] participations=participation.split(";");
				String[] copyTos=copyTo.split(";");
				if(hosts.length>0) {
					users.addAll(Arrays.asList(host));
				}
				if(participations.length>0) {
					users.addAll(Arrays.asList(participations));
				}
				if(copyTos.length>0) {
					users.addAll(Arrays.asList(copyTos));
				}
				List<Map<String,Object>> relations= relationService.getObjectMap(token,
						" parent_id='"+order.getId()+"' and name='irel_children'");
				if(relations!=null&&relations.size()>0) {
					String childId= relations.get(0).get("CHILD_ID").toString();
					EcmDocument docObj= documentService.getObjectById(token, childId);
					
					if(users.size()>1) {
						distributeDataOption(token, docObj,order, hosts, participations, copyTos);
					}else if(users.size()==0){
						documentService.grantUser(token, childId, users.get(0),
								ObjectPermission.BROWSER, new Date());
					}
				}
				
			}
		}else {
			order.setStatus(status);
			documentService.updateObject(token, order, null);
		}
		documentService.newAudit(token, "Portal", status, order.getId(), null, null);
		return order;
		
	}
	/**
	 * 二次分发并授权
	 * @param token
	 * @param docObj
	 * @param hosts
	 * @param participations
	 * @param copytos
	 * @return
	 * @throws Exception 
	 */
	@Transactional(rollbackFor = Exception.class)
	public EcmDocument distributeDataOption(String token,EcmDocument docObj,EcmDocument parentOrder,
			String[] hosts,String[] participations,String[] copytos) throws Exception {
		for(int i=0;hosts!=null&&i<hosts.length;i++) {
			EcmDocument order = new EcmDocument();
			SimpleDateFormat df=new SimpleDateFormat("yyyy-MM-dd");
			String dt= df.format(new Date());
			order.setName(docObj.getName()+"的分发单，分发日期"+dt);
			order.addAttribute("C_HOST", hosts[i]);//主送
			order.addAttribute("C_FROM_CODING", parentOrder.getAttributeValue("C_FROM_CODING"));//发送人
			order.setTypeName("分发单");
			order.setStatus("新建");
			documentService.grantUser(token, docObj.getId(), hosts[i],
					ObjectPermission.BROWSER, new Date());
			distributeOrder(token, order, parentOrder.getId(), docObj.getId());
		}
		
		for(int i=0;participations!=null&&i<participations.length;i++) {

			EcmDocument order = new EcmDocument();
			SimpleDateFormat df=new SimpleDateFormat("yyyy-MM-dd");
			String dt= df.format(new Date());
			order.setName(docObj.getName()+"的分发单，分发日期"+dt);
			order.addAttribute("C_PARTICIPATION", participations[i]);//抄送
			order.addAttribute("C_FROM_CODING", parentOrder.getAttributeValue("C_FROM_CODING"));//发送人
			order.setTypeName("分发单");
			order.setStatus("新建");
			documentService.grantUser(token, docObj.getId(), participations[i],
					ObjectPermission.BROWSER, new Date());
			distributeOrder(token, order,  parentOrder.getId(), docObj.getId());
		
		}
		for(int i=0;copytos!=null&&i<copytos.length;i++) {
			EcmDocument order = new EcmDocument();
			SimpleDateFormat df=new SimpleDateFormat("yyyy-MM-dd");
			String dt= df.format(new Date());
			order.setName(docObj.getName()+"的分发单，分发日期"+dt);
			order.addAttribute("C_COPY_TO", copytos[i]);//阅知
			order.addAttribute("C_FROM_CODING", parentOrder.getAttributeValue("C_FROM_CODING"));//发送人
			order.setTypeName("分发单");
			order.setStatus("新建");
			documentService.grantUser(token, docObj.getId(), copytos[i],
					ObjectPermission.BROWSER, new Date());
			distributeOrder(token, order,  parentOrder.getId(), docObj.getId());
		}
		return null;
		
	}
	
//	@Transactional(rollbackFor = Exception.class)
	private EcmDocument distributeOrder(String token,EcmDocument childOrder,String parentId,String docId) throws Exception {
		String orderId= documentService.newObject(token, childOrder, null);
		EcmRelation relation=new EcmRelation("分发",parentId,orderId);
		relationService.newObject(token, relation);
		EcmRelation relation2=new EcmRelation("irel_children",orderId,docId);
		relationService.newObject(token, relation2);
		return childOrder;
		
	}
	

	
	/**
	 * 母单
	 * @param token
	 * @param doc 文件对象
	 * @return
	 * @throws Exception
	 */
	public EcmDocument orderForMainDistribution(String token,EcmDocument doc) throws Exception {
		return orderForMainDistribution(token,doc,
				doc.getAttributeValue("C_FROM_CODING"),
				doc.getAttributeValue("C_COMMENT"),
				doc.getAttributeValue("C_COMMENT1"),
				doc.getAttributeValue("C_COPY_TO")
				);
	}
	/**
	 * 创建子单
	 * @param docId
	 * @param sender
	 * @param receiver
	 */
	public void orderForSubDistribution(String token,EcmDocument childOrder,EcmDocument parentOrder,String docId) {
		
	}
}
