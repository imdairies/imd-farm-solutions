package com.imd.services.bean;

import java.util.Iterator;
import java.util.List;

public class FeedPlanBean {
	private String orgID;
	private String feedCohortCD;
	private String loginToken;
    private List<FeedItemBean> feedPlanItems;
    
    
    public void setFeedPlanItems(List<FeedItemBean> feedPlanItems) {
    	this.feedPlanItems = feedPlanItems;    	
    }
    public List<FeedItemBean> getFeedPlanItems() {
    	return feedPlanItems;    	
    }

    public String getFeedCohortCD() {
		return feedCohortCD;
	}
	public void setFeedCohortCD(String feedCohortCD) {
		this.feedCohortCD = feedCohortCD;
	}
	public String getOrgID() {
		return orgID;
	}
	public void setOrgID(String orgId) {
		this.orgID = orgId;
	}
	public String getLoginToken() {
		return loginToken;
	}
	public void setLoginToken(String loginToken) {
		this.loginToken = loginToken;
	}
	public String toString() {
		String stringifiedValue = 	"\n orgID:" + orgID + 
				"\n feedCohortCD:" + feedCohortCD + 
				"\n loginToken: " + loginToken +
				"\n feedPlanItems: [" ;
		if (feedPlanItems != null) {
			Iterator<FeedItemBean> it = feedPlanItems.iterator();
			while (it.hasNext()) {
				FeedItemBean item = it.next();
				stringifiedValue += "{" + item.toString() + "}\n";
			}
		}
		stringifiedValue += "]";
		return stringifiedValue;
	}	
	

}
