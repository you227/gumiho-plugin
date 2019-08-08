package io.jenkins.plugins.api;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.jws.WebMethod;

import org.apache.commons.lang.StringUtils;
import org.kohsuke.stapler.StaplerRequest;
import org.kohsuke.stapler.StaplerResponse;

import hudson.model.Api;
import hudson.model.Item;
import hudson.model.ItemGroup;
import hudson.model.Job;
import hudson.model.ParameterValue;
import hudson.model.ParametersAction;
import hudson.model.StringParameterValue;
import io.jenkins.plugins.BaseResult;
import io.jenkins.plugins.dto.rsp.BuildRsp;
import io.jenkins.plugins.enums.ErrorCodeEnums;
import io.jenkins.plugins.hook.BuildTask;
import io.jenkins.plugins.hook.ThreadPoolManager;
import io.jenkins.plugins.setting.GumihoSetting;
import io.jenkins.plugins.util.Constant;
import jenkins.model.Jenkins;

public class RestApi extends Api {
	
	private static final Logger logger = Logger.getLogger(RestApi.class.getName());

	public RestApi(Object bean) {
		super(bean);
	}
	
	//{jenkins_host}/plugin/{plugin-name}/api/build
	@WebMethod(operationName = "build")
	public void doBuild(StaplerRequest req, StaplerResponse rsp) throws IOException {
		BaseResult<BuildRsp> result = new BaseResult<>();
		try {
			GumihoSetting globalSetting = new GumihoSetting();
			String webhookUrl = globalSetting.getGumihoBuildWebhook();
			if(StringUtils.isEmpty(webhookUrl)) {
				result.setCode(ErrorCodeEnums.ERROR_401.getCode());
    			result.setMsg("请先在jenkins全局配置中配置九尾狐回调地址");
				this.result(rsp, result);
				return;
			}
			if(!webhookUrl.matches("(?:https?)://[-A-Za-z0-9+&@#/%?=~_|!:,.;]+[-A-Za-z0-9+&@#/%=~_|]")) {
				result.setCode(ErrorCodeEnums.ERROR_401.getCode());
    			result.setMsg("jenkins全局配置中九尾狐回调地址不合法");
				this.result(rsp, result);
				return;
			}
			Integer retryCount = globalSetting.getGumihoBuildWebhookRetryCount();
			if(retryCount == null || retryCount<0) {
				retryCount = GumihoSetting.GUMIHO_BUILD_WEBHOOK_RETRY_COUNT_DEFAULT;
				logger.log(Level.FINER, "未获取到九尾狐回调重试全局配置使用默认值:{0}", retryCount);
			}
			logger.log(Level.FINER, "Gumiho-eco全局配置 url:{0}", webhookUrl);
			
			String projectName = req.getParameter("projectName");
			if(StringUtils.isEmpty(projectName)) {
				result.setCode(ErrorCodeEnums.ERROR_401.getCode());
    			result.setMsg("工程名不能为空");
				this.result(rsp, result);
				return;
			}
			String buildIdStr = req.getParameter("buildId");
			if(StringUtils.isEmpty(buildIdStr)) {
				result.setCode(ErrorCodeEnums.ERROR_401.getCode());
    			result.setMsg("构建id不能为空");
				this.result(rsp, result);
				return;
			}
			if(!buildIdStr.matches("^[1-9]\\d*$")) {
				result.setCode(ErrorCodeEnums.ERROR_400.getCode());
    			result.setMsg("构建id格式不正确只能是数字");
				this.result(rsp, result);
				return;
			}
			String branch = req.getParameter("branch");
			if(!StringUtils.isEmpty(branch)) {
				//参数验证
				String env = req.getParameter("env");
				if(StringUtils.isEmpty(env)) {
					result.setCode(ErrorCodeEnums.ERROR_401.getCode());
	    			result.setMsg("环境不能为空");
					this.result(rsp, result);
					return;
				}
				String imageName = req.getParameter("imageName");
				if(StringUtils.isEmpty(imageName)) {
					result.setCode(ErrorCodeEnums.ERROR_401.getCode());
	    			result.setMsg("镜像名称不能为空");
					this.result(rsp, result);
					return;
				}
				String imageTag = req.getParameter("imageTag");
				if(StringUtils.isEmpty(imageTag)) {
					result.setCode(ErrorCodeEnums.ERROR_401.getCode());
	    			result.setMsg("镜像标签不能为空");
					this.result(rsp, result);
					return;
				}
			}
			
			Long buildId = Long.valueOf(buildIdStr);
			final Jenkins jenkins = Jenkins.getInstance();
	        if (jenkins != null) {
	            Item item = jenkins.getItemByFullName(projectName);
	            if(item == null) {
	            	result.setCode(ErrorCodeEnums.ERROR_404.getCode());
	    			result.setMsg(String.format("找不到job:%s", projectName));
	            }else {
	            	if(!StringUtils.isEmpty(branch)) {
						while (item instanceof ItemGroup<?>) {
							item = jenkins.getItem(projectName+"/"+branch, (ItemGroup<?>) item);
						}
		            }
	            	if(item == null && !StringUtils.isEmpty(branch)) {
	        			result.setCode(ErrorCodeEnums.ERROR_404.getCode());
		            	result.setMsg(String.format("job:%s 找不到 branch:%s", projectName, branch));
	            	}else if (item instanceof Job<?, ?>) {
		            	Job<?, ?> job = (Job<?, ?>) item;
		            	logger.log(Level.INFO, "Gumiho plugin run build job:{0}, branch:{1}", new Object[]{projectName, branch});
		            	List<ParameterValue> params = new ArrayList<>();
		            	Enumeration<String> names = req.getParameterNames();
		            	while(names.hasMoreElements()) {
		            		String name = names.nextElement();
		            		params.add(new StringParameterValue(name, req.getParameter(name)));
		            	}
		            	ParametersAction action = new ParametersAction(params);
		            	ThreadPoolExecutor executor = ThreadPoolManager.getInstance();
		            	BuildTask buildTask = new BuildTask(job, action, buildId, webhookUrl, retryCount);
		                executor.execute(buildTask);
		                result.setCode(ErrorCodeEnums.ERROR_200.getCode());
		                if(StringUtils.isEmpty(branch)) {
		                	result.setMsg(String.format("job:%s 构建中", projectName));
		                }else {
		                	result.setMsg(String.format("job:%s, branch:%s 构建中", projectName, branch));
		                }
		            }else {
		            	result.setCode(ErrorCodeEnums.ERROR_404.getCode());
		            	if(StringUtils.isEmpty(branch)) {
		            		result.setMsg(String.format("找不到job:%s, 类型不是job", projectName));
		                }else {
		                	result.setMsg(String.format("job:%s 找不到 branch:%s, 类型不是job", projectName, branch));
		                }
		            }
	            }
	        }
		}catch (Exception e) {
			logger.log(Level.SEVERE, "构建失败系统异常",e);
			result.setCode(ErrorCodeEnums.ERROR_999.getCode());
			result.setMsg("构建失败系统异常，error:"+e.getMessage());
		}
		this.result(rsp, result);
	}
	
	public void result(StaplerResponse rsp, BaseResult<?> result) throws IOException {
		rsp.setContentType("application/json;charset="+Constant.CHARSET);
		rsp.setCharacterEncoding(Constant.CHARSET);
		PrintWriter pr = rsp.getWriter();
		pr.write(result.toString());
		pr.close();
	}
	
}
