package com.imd.dto;


import org.joda.time.DateTime;
import org.joda.time.LocalDate;
import org.joda.time.LocalTime;

import com.imd.util.IMDProperties;

public class Note {
	private int noteID;
	private String noteText;
	private DateTime noteDateTimeStamp;
	
	public Note(int id, String text, DateTime noteDTTM) {
		this.noteID = id;
		this.noteText = text;
		this.noteDateTimeStamp = noteDTTM;

	}
	
	public Note(int id, String text) {
		this.noteID = id;
		this.noteText = text;
		this.noteDateTimeStamp = DateTime.now(IMDProperties.getServerTimeZone());

	}
	public int getNoteID() {
		return noteID;
	}
	public void setNoteID(int noteID) {
		this.noteID = noteID;
	}
	public DateTime getNoteTimeStamp() {
		return noteDateTimeStamp;
	}
	public void setNoteTimeStamp(DateTime noteTimeStamp) {
		this.noteDateTimeStamp = noteTimeStamp;
	}
	public String getNoteText() {
		return noteText;
	}
	public void setNoteText(String text) {
		this.noteText = text;
	}
	public void setNoteDateTimeStampToNow() {
		noteDateTimeStamp = DateTime.now(IMDProperties.getServerTimeZone());
	}
	public LocalDate getNoteDate() {
		return noteDateTimeStamp.toLocalDate();
	}
	public LocalTime getNoteTime() {
		return noteDateTimeStamp.toLocalTime();
	}	

}
