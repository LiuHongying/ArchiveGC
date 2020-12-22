package org.zisecm.jobs.business;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.thymeleaf.context.Context;
import org.thymeleaf.spring5.SpringTemplateEngine;

import com.ecm.common.util.PasswordUtils;
import com.ecm.core.ActionContext;
import com.ecm.core.cache.manager.CacheManagerOper;
import com.ecm.core.entity.EcmAttribute;
import com.ecm.core.entity.EcmUser;
import com.ecm.core.service.AuthService;
import com.ecm.core.service.DocumentService;
import com.ecm.core.service.MailService;
import com.ecm.core.service.UserService;
import com.ecm.icore.service.IEcmSession;

@Service
public class EmailService {
	private final Logger logger = LoggerFactory.getLogger(EmailService.class);

	private String token;
	/**
	 * 邮件发送测试类
	 */
	@Autowired
	private MailService mailService;
	@Autowired
	private DocumentService documentService;

	@Autowired
	private AuthService authService;

	@Autowired
	private SpringTemplateEngine templateEngine;
	@Autowired
	private Environment env;
	@Autowired
	private UserService userService;

	public void sendMail(List<Map<String, Object>> row, String email) throws Exception {
		try {
			// 创建邮件正文
			Map<String, Object> map = new HashMap<String, Object>();
			map.put("docList", row);
			Context context = new Context();
			context.setVariables(map);
			String emailContent = templateEngine.process("electronicLending", context);
			mailService.sendHtmlMail(email, "主题：邮件催还", emailContent);/**/
		} catch (Exception e) {
			e.printStackTrace();
			throw new Exception("发送邮件测试发生异常！");
		}
	}

	/**
	 * 启动服务
	 * 
	 * @throws Exception
	 * properties文件配置  cron.sendCollectionMail = 0 0 14 * * ?
	 */
	@Scheduled(cron = "${cron.sendCollectionMail}")
	public synchronized void runJobs() {
		IEcmSession ecmSession = null;
		try {
			StringBuilder borrowsql = new StringBuilder("");
			borrowsql.append("select ");
			borrowsql.append(getDocumentAllColumns());
			borrowsql.append(" from ecm_document where ");
			borrowsql.append("TYPE_NAME='借阅单' ");
			borrowsql.append("and SUB_TYPE = '纸质借阅' ");
			borrowsql.append("and STATUS='待入库' ");
			// 纸质文档借阅期限：内部公开90天；受限、普通商密30天；
			// 此处设置到期前五天提醒
			StringBuilder fiveDaysLimitSql = new StringBuilder(borrowsql.toString());
			StringBuilder overTimeSql = new StringBuilder(borrowsql.toString());
			fiveDaysLimitSql.append(
					"and ((date_format(now(),'%Y-%m-%d')=date_format(date_add(C_APPROVE_DATE,interval 85 day),'%Y-%m-%d') and C_SECURITY_LEVEL='内部公开') ");
			fiveDaysLimitSql.append(
					"or (date_format(now(),'%Y-%m-%d')=date_format(date_add(C_APPROVE_DATE,interval 25 day),'%Y-%m-%d') and C_SECURITY_LEVEL='受限') ");
			fiveDaysLimitSql.append(
					"or (date_format(now(),'%Y-%m-%d')=date_format(date_add(C_APPROVE_DATE,interval 25 day),'%Y-%m-%d') and C_SECURITY_LEVEL='普通商密'))");
			// 此处设置超期提醒，每天催收过期文件
			overTimeSql.append(
					"and ((date_format(now(),'%Y-%m-%d')>=date_add(C_APPROVE_DATE,interval 90 day) and C_SECURITY_LEVEL='内部公开') ");
			overTimeSql.append(
					"or (date_format(now(),'%Y-%m-%d')>=date_add(C_APPROVE_DATE,interval 30 day) and C_SECURITY_LEVEL='受限') ");
			overTimeSql.append(
					"or (date_format(now(),'%Y-%m-%d')>=date_add(C_APPROVE_DATE,interval 30 day) and C_SECURITY_LEVEL='普通商密'))");
			List<Map<String, Object>> limitFiveDaysResult = documentService.getMapList(getToken(),
					fiveDaysLimitSql.toString());
			List<Map<String, Object>> overTimeResult = documentService.getMapList(getToken(), overTimeSql.toString());
			limitFiveDaysResult.addAll(overTimeResult);
			if (limitFiveDaysResult == null || limitFiveDaysResult.size() < 1) {
				return;
			}
			String workflowSpecialUserName = env.getProperty("ecm.username");
			ecmSession = authService.login("workflow", workflowSpecialUserName, env.getProperty("ecm.password"));
			for (Map<String, Object> row : limitFiveDaysResult) {
				String borrowDocsSql = "select a.* from ecm_document a,"
						+ "ecm_relation b where a.id=b.child_id and b.name='irel_children' and" + " b.parent_id='"
						+ row.get("ID").toString() + "' ";
				try {
					EcmUser user = userService.getObjectByName(ecmSession.getToken(), row.get("C_DRAFTER").toString());
					String email = user.getEmail();
					List<Map<String, Object>> docs = documentService.getMapList(getToken(), borrowDocsSql);
					sendMail(docs, email);
				} catch (Exception e) {
					logger.error("发送催收文档邮件异常", e);
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			if (ecmSession != null) {
				authService.logout(ecmSession.getToken());
			}
		}
	}

	/**
	 * 获取文档所有列
	 * 
	 * @return
	 */
	private String getDocumentAllColumns() {
		String cols = "";
		for (String attr : CacheManagerOper.getEcmAttributes().keySet()) {
			if (cols.length() == 0) {
				cols = attr;
			} else {
				cols += "," + attr;
			}
		}
		if ("".equals(cols)) {
			cols = " * ";
		}
		return cols;
	}

	public String getToken() {
		if (token == null || authService.login(token) == null) {
			String user = null;
			try {
				user = CacheManagerOper.getEcmParameters().get("IndexUser").getValue();

			} catch (Exception e) {
				// TODO Auto-generated catch block
				// e.printStackTrace();
				return null;
			}

			String password = CacheManagerOper.getEcmParameters().get("IndexPassword").getValue();
			if (!StringUtils.isEmpty(password)) {
				try {
					password = PasswordUtils.decodePassword(password);
				} catch (Exception ex) {

				}
			}
			try {
				token = authService.login(ActionContext.APP_INDEX_AGENT, user, password).getToken();
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
				return null;
			}
		}
		return token;
	}
}
