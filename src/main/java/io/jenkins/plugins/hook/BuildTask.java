package io.jenkins.plugins.hook;

import java.util.concurrent.ExecutionException;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.apache.commons.lang.StringUtils;

import hudson.model.Action;
import hudson.model.Job;
import hudson.model.Result;
import hudson.model.Run;
import hudson.model.queue.QueueTaskFuture;
import io.jenkins.plugins.BaseResult;
import io.jenkins.plugins.dto.rsp.BuildRsp;
import io.jenkins.plugins.enums.ErrorCodeEnums;
import io.jenkins.plugins.setting.GumihoSetting;
import io.jenkins.plugins.util.HttpUtil;
import jenkins.model.ParameterizedJobMixIn;
import net.sf.json.JSONObject;

public class BuildTask implements Runnable{
	
	private static final Logger logger = Logger.getLogger(BuildTask.class.getName());
	
	private Job<?, ?> job;
	
	private Action action;
	
	private Long buildId;
	
	private String webhookUrl;
	
	private Integer retryCount = GumihoSetting.GUMIHO_BUILD_WEBHOOK_RETRY_COUNT_DEFAULT;
	
	public BuildTask(Job<?, ?> job, Action action, Long buildId, String webhookUrl, Integer retryCount) {
		this.job = job;
		this.action = action;
		this.buildId = buildId;
		this.webhookUrl = webhookUrl;
		if(retryCount!=null && retryCount>=0) {
			this.retryCount = retryCount;
		}
	}
	//构建回调地址(/project-artifact-build/buildCallBack) postBody
	@Override
    public void run() {
		BaseResult<BuildRsp> result = new BaseResult<>();
		String jobName = job.getName();
		BuildRsp buildRsp = new BuildRsp();
		buildRsp.setBuildId(buildId);
		try {
			Run<?, ?> runBuild = null;
			if(job.isBuilding()) {
				//job在构建中就获取最后一次构建
				runBuild = job.getLastBuild();
				for(int i=1;i<=10000;i++){
					Thread.sleep(10000);
					if(!runBuild.isBuilding()) {
						break;
					}
				}
			}else {
				runBuild = this.scheduleBuild(job, action);
			}
	    	if(runBuild!=null) {
	    		buildRsp.setQueueId(runBuild.getQueueId());
	    		buildRsp.setBuildNumber(runBuild.getNumber());
	    		buildRsp.setIsBuilding(runBuild.isBuilding());
	    		buildRsp.setJobName(jobName);
	    		buildRsp.setUrl(runBuild.getUrl());
	    		buildRsp.setDuration(runBuild.getDuration());
	    		Result buildResult = runBuild.getResult();
	    		if(buildResult != null) {
	    			buildRsp.setBuildStatus(buildResult.toString());
	    		}else {
	    			buildRsp.setBuildStatus("build status result is null");
	    		}
	        	result.setCode(ErrorCodeEnums.ERROR_200.getCode());
				result.setMsg("job:"+jobName+"构建成功");
	    	}else {
	    		result.setCode(ErrorCodeEnums.ERROR_501.getCode());
				result.setMsg("job:"+jobName+"启动构建失败，未获取到构建信息");
	    	}
		}catch (Exception e) {
			logger.log(Level.SEVERE, "job:"+jobName+"获取构建信息失败", e);
			result.setCode(ErrorCodeEnums.ERROR_999.getCode());
			result.setMsg("job:"+jobName+"获取构建信息失败，系统异常  error:"+e.getMessage());
		}
		result.setResult(buildRsp);
		this.callbackGumihoBuild(result.toString(), jobName, 0);
    }
	
	/**
	 * @see 回调九尾狐构建
	 * @param param
	 * @param jobName
	 * @throws InterruptedException 
	 */
	protected void callbackGumihoBuild(String param, String jobName, int count) {
		try {
			String httpResult = HttpUtil.httpPostJson(webhookUrl, null, param);
			logger.log(Level.FINER, "job:{0}回调重试次数:{1}, 调用构建回调返回：{2}", new Object[]{jobName, count, httpResult});
			if(!StringUtils.isEmpty(httpResult)) {
				JSONObject httpResultJson = JSONObject.fromObject(httpResult);
				if(httpResultJson.containsKey("code") && ErrorCodeEnums.ERROR_200.getCode().equals(httpResultJson.getString("code"))) {
					logger.log(Level.FINER, "job:{0}调用构建回调返回成功", jobName);
					return;
				}
			}
			if(count < retryCount) {
				//10秒重试一次
				Thread.sleep(10000);
				callbackGumihoBuild(param, jobName, count+1);
			}else {
				logger.log(Level.SEVERE, "job:{0}回调重试：{1}次后仍未成功", new Object[]{jobName, retryCount});
			}
		}catch (Exception e) {
			logger.log(Level.SEVERE, "job:"+jobName+"回调重试次数:"+count+", 调用构建回调失败", e);
			if(count < retryCount) {
				//10秒重试一次
				try {
					Thread.sleep(10000);
				} catch (InterruptedException e1) {
					logger.log(Level.WARNING, "Interrupted!", e);
					Thread.currentThread().interrupt();
				}
				callbackGumihoBuild(param, jobName, count+1);
			}else {
				logger.log(Level.SEVERE, "job:{0}回调重试：{1}次后仍未成功", new Object[]{jobName, retryCount});
			}
		}
	}
	
	protected Run<?, ?> scheduleBuild(Job<?, ?> job, Action actions) throws InterruptedException, ExecutionException {
        int projectBuildDelay = 0;
        if (job instanceof ParameterizedJobMixIn.ParameterizedJob) {
            ParameterizedJobMixIn.ParameterizedJob abstractProject = (ParameterizedJobMixIn.ParameterizedJob) job;
            if (abstractProject.getQuietPeriod() > projectBuildDelay) {
                projectBuildDelay = abstractProject.getQuietPeriod();
            }
        }
        QueueTaskFuture<?> future = retrieveScheduleJob(job).scheduleBuild2(projectBuildDelay, actions);
        return future != null ? (Run<?, ?>)future.get() : null;
    }

	@SuppressWarnings("rawtypes")
	private ParameterizedJobMixIn<?, ?> retrieveScheduleJob(final Job<?, ?> job) {
        return new ParameterizedJobMixIn() {
            @Override
            protected Job<?, ?> asJob() {
                return job;
            }
        };
    }
	
}
