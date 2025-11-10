package com.doublez.backend.request;

import com.fasterxml.jackson.annotation.JsonProperty;

public class PromoteAgencyAdminRequest {
	
	@JsonProperty("userId")
	private Long userId;
	@JsonProperty("name")
    private String name;
	@JsonProperty("description")
    private String description;
	
    public PromoteAgencyAdminRequest() {
	}
    
	public Long getUserId() {
		return userId;
	}
	public void setUserId(Long userId) {
		this.userId = userId;
	}
	
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
	
	public String getDescription() {
		return description;
	}
	public void setDescription(String description) {
		this.description = description;
	}
    
	 @Override
	    public String toString() {
	        return String.format("PromoteAgencyAdminRequest{userId=%d, name='%s', description='%s'}", 
	            userId, name, description);
	    }

}
