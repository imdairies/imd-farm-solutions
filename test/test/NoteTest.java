package com.imd.test;

import static org.junit.jupiter.api.Assertions.*;

import java.time.LocalDate;
import java.time.LocalDateTime;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.imd.dto.Note;

class NoteTest {

	@BeforeAll
	static void setUpBeforeClass() throws Exception {
	}

	@AfterAll
	static void tearDownAfterClass() throws Exception {
	}

	@BeforeEach
	void setUp() throws Exception {
	}

	@AfterEach
	void tearDown() throws Exception {
	}

	@Test
	void testNoteCreation() {
		Note note = new Note(0,"Hello World", LocalDateTime.now()); 
		assertTrue(note.getNoteID()==0," Note ID mismatch, was expecting 0 but got: " + note.getNoteID());
		assertTrue(note.getNoteText().equals("Hello World")," Note Text mismatch, was expecting [Hello World] but got: " + note.getNoteText());
		assertTrue(note.getNoteDate().isEqual(LocalDate.now())," Note Date mismatch, was expecting" + (LocalDate.now() +"  but got: " + note.getNoteDate()));
	}
}
