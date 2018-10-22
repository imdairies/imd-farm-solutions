package com.imd.dto;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;

public class Note {
	private int noteID;
	private String noteText;
	private LocalDateTime noteDateTimeStamp;
	
	public Note(int id, String text, LocalDateTime noteDTTM) {
		this.noteID = id;
		this.noteText = text;
		this.noteDateTimeStamp = noteDTTM;

	}
	
	public Note(int id, String text) {
		this.noteID = id;
		this.noteText = text;
		this.noteDateTimeStamp = LocalDateTime.now();

	}
	public int getNoteID() {
		return noteID;
	}
	public void setNoteID(int noteID) {
		this.noteID = noteID;
	}
	public LocalDateTime getNoteTimeStamp() {
		return noteDateTimeStamp;
	}
	public void setNoteTimeStamp(LocalDateTime noteTimeStamp) {
		this.noteDateTimeStamp = noteTimeStamp;
	}
	public String getNoteText() {
		return noteText;
	}
	public void setNoteText(String text) {
		this.noteText = text;
	}
	public void setNoteDateTimeStampToNow() {
		noteDateTimeStamp = LocalDateTime.now();
	}
	public LocalDate getNoteDate() {
		return noteDateTimeStamp.toLocalDate();
	}
	public LocalTime getNoteTime() {
		return noteDateTimeStamp.toLocalTime();
	}	

}
