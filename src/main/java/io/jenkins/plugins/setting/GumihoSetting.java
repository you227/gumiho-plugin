package io.jenkins.plugins.setting;

import java.io.File;
import java.io.IOException;
import java.util.Collection;
import java.util.List;

import org.acegisecurity.AccessDeniedException;
import org.apache.commons.lang.StringUtils;
import org.jenkins.ui.icon.IconSpec;
import org.kohsuke.accmod.Restricted;
import org.kohsuke.accmod.restrictions.NoExternalUse;
import org.kohsuke.stapler.QueryParameter;
import org.kohsuke.stapler.StaplerRequest;

import hudson.Extension;
import hudson.Util;
import hudson.model.Item;
import hudson.model.ItemGroup;
import hudson.model.Job;
import hudson.model.TopLevelItem;
import hudson.model.AbstractProject.AbstractProjectDescriptor;
import hudson.search.Search;
import hudson.search.SearchIndex;
import hudson.security.ACL;
import hudson.security.Permission;
import hudson.tasks.Maven;
import hudson.util.FormValidation;
import jenkins.model.Jenkins;
import jenkins.mvn.GlobalSettingsProvider;
import jenkins.mvn.GlobalSettingsProviderDescriptor;
import jenkins.mvn.SettingsProvider;
import jenkins.mvn.SettingsProviderDescriptor;
import net.sf.json.JSONObject;

@SuppressWarnings("rawtypes")
@hudson.Extension
public class GumihoSetting implements TopLevelItem {
	
	private String gumihoBuildWebhook;
	
	private Integer gumihoBuildWebhookRetryCount;
	
	public static final Integer GUMIHO_BUILD_WEBHOOK_RETRY_COUNT_DEFAULT = 10;
	
	public static final Integer GUMIHO_BUILD_WEBHOOK_RETRY_COUNT_MAX = 50;
	
    public String getGumihoBuildWebhook() {
        if ((gumihoBuildWebhook!=null) && (gumihoBuildWebhook.trim().length()>0)) { 
            return getFormattedGumihoBuildWebhook(gumihoBuildWebhook);
        }
        else {
            String globalOpts = getDescriptor().getGlobalGumihoBuildWebhook();
            if (globalOpts!=null) {
                return getFormattedGumihoBuildWebhook(globalOpts);
            }
            else {
                return globalOpts;
            }
        }
    }
    
    private String getFormattedGumihoBuildWebhook(String gumihoBuildWebhook) {
        return gumihoBuildWebhook == null? null: gumihoBuildWebhook.replaceAll("[\t\r\n]+","");
    }

    public void setGumihoBuildWebhook(String gumihoBuildWebhook) {
        String globalGumihoBuildWebhook = getFormattedGumihoBuildWebhook(getDescriptor().getGlobalGumihoBuildWebhook());
        
        if (gumihoBuildWebhook != null && !getFormattedGumihoBuildWebhook(gumihoBuildWebhook).equals(globalGumihoBuildWebhook)) {
            this.gumihoBuildWebhook = gumihoBuildWebhook;
        } else {
            this.gumihoBuildWebhook = null;
        }
    }
    
    public Integer getGumihoBuildWebhookRetryCount() {
        if (gumihoBuildWebhookRetryCount!=null && gumihoBuildWebhookRetryCount>=0) { 
            return gumihoBuildWebhookRetryCount;
        } else {
            return GUMIHO_BUILD_WEBHOOK_RETRY_COUNT_DEFAULT;
        }
    }

    public void setGumihoBuildWebhookRetryCount(Integer gumihoBuildWebhookRetryCount) {
    	Integer globalGumihoBuildWebhook = getDescriptor().getGlobalGumihoBuildWebhookRetryCount();
        
        if (globalGumihoBuildWebhook != null && globalGumihoBuildWebhook>=0) {
            this.gumihoBuildWebhookRetryCount = globalGumihoBuildWebhook;
        } else {
            this.gumihoBuildWebhookRetryCount = GUMIHO_BUILD_WEBHOOK_RETRY_COUNT_DEFAULT;
        }
    }
  
	public DescriptorImpl getDescriptor() {
        return (DescriptorImpl)Jenkins.getInstance().getDescriptorOrDie(getClass());
    }

    /**
     * Descriptor is instantiated as a field purely for backward compatibility.
     * Do not do this in your code. Put @Extension on your DescriptorImpl class instead.
     */
    @Restricted(NoExternalUse.class)
    @Extension(ordinal=900)
    public static final DescriptorImpl DESCRIPTOR = new DescriptorImpl();

    public static final class DescriptorImpl extends AbstractProjectDescriptor implements IconSpec {
        
        private String globalGumihoBuildWebhook;
        
        private Integer globalGumihoBuildWebhookRetryCount;
        
        public DescriptorImpl() {
            super();
            load();
        }

        @Override
        public String getHelpFile(String fieldName) {
            String v = super.getHelpFile(fieldName);
            if (v!=null)    return v;
            if (fieldName == null) {
                return null;
            }
            return Jenkins.getInstance().getDescriptor(Maven.class).getHelpFile(fieldName);
        }

        public List<SettingsProviderDescriptor> getSettingsProviders() {
            return Jenkins.getInstance().getDescriptorList(SettingsProvider.class);
        }
        
        public List<GlobalSettingsProviderDescriptor> getGlobalSettingsProviders() {
            return Jenkins.getInstance().getDescriptorList(GlobalSettingsProvider.class);
        }

        public String getGlobalGumihoBuildWebhook() {
            return globalGumihoBuildWebhook;
        }

        public void setGlobalGumihoBuildWebhook(String globalGumihoBuildWebhook) {
            this.globalGumihoBuildWebhook = globalGumihoBuildWebhook;
            save();
        }
        
        public Integer getGlobalGumihoBuildWebhookRetryCount() {
			if (globalGumihoBuildWebhookRetryCount!=null && globalGumihoBuildWebhookRetryCount>=0) { 
	            return globalGumihoBuildWebhookRetryCount;
	        } else {
	            return GumihoSetting.GUMIHO_BUILD_WEBHOOK_RETRY_COUNT_DEFAULT;
	        }
		}

		public void setGlobalGumihoBuildWebhookRetryCount(Integer globalGumihoBuildWebhookRetryCount) {
			if (globalGumihoBuildWebhookRetryCount != null && globalGumihoBuildWebhookRetryCount>=0) {
	            this.globalGumihoBuildWebhookRetryCount = globalGumihoBuildWebhookRetryCount;
	        } else {
	            this.globalGumihoBuildWebhookRetryCount = GumihoSetting.GUMIHO_BUILD_WEBHOOK_RETRY_COUNT_DEFAULT;
	        }
			save();
		}

		public String getDisplayName() {
            return "gumiho-eco-display";
        }

        public String getCategoryId() {
            return "gumiho-eco-id";
        }

        public String getDescription() {
            return "gumiho-eco";
        }
        
        public FormValidation doCheckGlobalGumihoBuildWebhook(@QueryParameter String value)  {
	        if (StringUtils.isEmpty(value)) {
	            return FormValidation.error("请填写九尾狐构建回调地址");
	        }else if(!value.matches("(?:https?)://[-A-Za-z0-9+&@#/%?=~_|!:,.;]+[-A-Za-z0-9+&@#/%=~_|]")) {
	            return FormValidation.error("九尾狐构建回调地址不合法，必须是http或https地址");
	        }
	        return FormValidation.ok();
	    }
        
        public FormValidation doCheckGlobalGumihoBuildWebhookRetryCount(@QueryParameter String value)  {
	        if (StringUtils.isEmpty(value)) {
	            return FormValidation.error("请填写回调失败重试次数");
	        }else if(!value.matches("^[1-9]\\d*$")) {
	            return FormValidation.error("回调失败重试次数不合法，必须是正整数");
	        }
	        int count = Integer.parseInt(value);
	        if(count < 0) {
	            return FormValidation.error("回调失败重试次数必须大于等于0");
	        }else if(count > GumihoSetting.GUMIHO_BUILD_WEBHOOK_RETRY_COUNT_MAX) {
	            return FormValidation.error("回调失败重试次数不能大于"+GumihoSetting.GUMIHO_BUILD_WEBHOOK_RETRY_COUNT_MAX);
	        }
	        return FormValidation.ok();
	    }

        public GumihoSetting newInstance(ItemGroup parent, String name) {
			
        	return new GumihoSetting();
        }

        public Maven.DescriptorImpl getMavenDescriptor() {
            return Jenkins.getInstance().getDescriptorByType(Maven.DescriptorImpl.class);
        }
        
        @Override
        public boolean configure( StaplerRequest req, JSONObject o ) {
        	globalGumihoBuildWebhook = Util.fixEmptyAndTrim(o.getString("globalGumihoBuildWebhook"));
        	globalGumihoBuildWebhookRetryCount = o.getInt("globalGumihoBuildWebhookRetryCount");
            save();

            return true;
        }

		@Override
		public String getIconClassName() {
			
			return null;
		}

    }

	@Override
	public ItemGroup<? extends Item> getParent() {
		return null;
	}

	@Override
	public Collection<? extends Job> getAllJobs() {
		return null;
	}

	@Override
	public String getName() {
		return null;
	}

	@Override
	public String getFullName() {
		return null;
	}

	@Override
	public String getDisplayName() {
		return null;
	}

	@Override
	public String getFullDisplayName() {
		return null;
	}

	@Override
	public String getRelativeNameFrom(ItemGroup g) {
		return null;
	}

	@Override
	public String getRelativeNameFrom(Item item) {
		return null;
	}

	@Override
	public String getUrl() {
		return null;
	}

	@Override
	public String getShortUrl() {
		return null;
	}

	@Override
	public String getAbsoluteUrl() {
		return null;
	}

	@Override
	public void onLoad(ItemGroup<? extends Item> parent, String name) throws IOException {
		
	}

	@Override
	public void onCopiedFrom(Item src) {
		
	}

	@Override
	public void onCreatedFromScratch() {
		
	}

	@Override
	public void save() throws IOException {
		
	}

	@Override
	public void delete() throws IOException, InterruptedException {
		
	}

	@Override
	public File getRootDir() {
		return null;
	}

	@Override
	public Search getSearch() {
		return null;
	}

	@Override
	public String getSearchName() {
		return null;
	}

	@Override
	public String getSearchUrl() {
		return null;
	}

	@Override
	public SearchIndex getSearchIndex() {
		return null;
	}

	@Override
	public ACL getACL() {
		return null;
	}

	@Override
	public void checkPermission(Permission permission) throws AccessDeniedException {
		
	}

	@Override
	public boolean hasPermission(Permission permission) {
		return false;
	}

}
