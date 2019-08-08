package io.jenkins.plugins.dto.rsp;

import java.io.Serializable;

public class BuildRsp implements Serializable {
	
	private static final long serialVersionUID = 7120542821129406669L;
	
	private Long buildId;

	private Long queueId;
	
	private Integer buildNumber;
	
	private Boolean isBuilding;
	
	private String buildStatus;
	
	private String jobName;
	
	private Long duration;
	
	private String url;

	public Long getBuildId() {
		return buildId;
	}

	public void setBuildId(Long buildId) {
		this.buildId = buildId;
	}

	public Long getQueueId() {
		return queueId;
	}

	public void setQueueId(Long queueId) {
		this.queueId = queueId;
	}

	public Integer getBuildNumber() {
		return buildNumber;
	}

	public void setBuildNumber(Integer buildNumber) {
		this.buildNumber = buildNumber;
	}

	public Boolean getIsBuilding() {
		return isBuilding;
	}

	public void setIsBuilding(Boolean isBuilding) {
		this.isBuilding = isBuilding;
	}

	public String getBuildStatus() {
		return buildStatus;
	}

	public void setBuildStatus(String buildStatus) {
		this.buildStatus = buildStatus;
	}

	public String getJobName() {
		return jobName;
	}

	public void setJobName(String jobName) {
		this.jobName = jobName;
	}

	public Long getDuration() {
		return duration;
	}

	public void setDuration(Long duration) {
		this.duration = duration;
	}

	public String getUrl() {
		return url;
	}

	public void setUrl(String url) {
		this.url = url;
	}

}
