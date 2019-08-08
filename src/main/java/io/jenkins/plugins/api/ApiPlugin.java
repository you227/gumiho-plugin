package io.jenkins.plugins.api;

import hudson.Plugin;
import hudson.model.Api;

public class ApiPlugin extends Plugin {
	
	public Api getApi() {
		return new RestApi(this);
	}

}
