package com.imd.dto;

import java.time.LocalDateTime;

public class ParturationDetail {
	private LocalDateTime heatDateTime;
	private LocalDateTime standingHeatDateTime;
	private LocalDateTime inseminationDateTime;
	private boolean isDoubleDose;
	/**
	 * How many times did we perform the AI including the one that resulted in the successful conception.
	 */
	private Integer inseminationCount;
	private LocalDateTime parturationDateTime;
	private Sire semenSireDetail; 
	private Animal calf;
	private Note parturationComments;
	public LocalDateTime getHeatDateTime() {
		return heatDateTime;
	}
	public void setHeatDateTime(LocalDateTime heatDateTime) {
		this.heatDateTime = heatDateTime;
	}
	public Note getParturationComments() {
		return parturationComments;
	}
	public void setParturationComments(Note parturationComments) {
		this.parturationComments = parturationComments;
	}
	public Animal getCalf() {
		return calf;
	}
	public void setCalf(Animal calf) {
		this.calf = calf;
	}
	public Sire getSemenSireDetail() {
		return semenSireDetail;
	}
	public void setSemenSireDetail(Sire semenSireDetail) {
		this.semenSireDetail = semenSireDetail;
	}
	public LocalDateTime getParturationDateTime() {
		return parturationDateTime;
	}
	public void setParturationDateTime(LocalDateTime parturationDateTime) {
		this.parturationDateTime = parturationDateTime;
	}
	public Integer getInseminationCount() {
		return inseminationCount;
	}
	public void setInseminationCount(Integer inseminationCount) {
		this.inseminationCount = inseminationCount;
	}
	public boolean isDoubleDose() {
		return isDoubleDose;
	}
	public void setDoubleDose(boolean isDoubleDose) {
		this.isDoubleDose = isDoubleDose;
	}
	public LocalDateTime getInseminationDateTime() {
		return inseminationDateTime;
	}
	public void setInseminationDateTime(LocalDateTime inseminationDateTime) {
		this.inseminationDateTime = inseminationDateTime;
	}
	public LocalDateTime getStandingHeatDateTime() {
		return standingHeatDateTime;
	}
	public void setStandingHeatDateTime(LocalDateTime standingHeatDateTime) {
		this.standingHeatDateTime = standingHeatDateTime;
	}
}
