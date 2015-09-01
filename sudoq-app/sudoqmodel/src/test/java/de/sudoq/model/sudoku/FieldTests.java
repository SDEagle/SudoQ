package de.sudoq.model.sudoku;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import org.junit.Test;

import de.sudoq.model.sudoku.Field;

public class FieldTests {

	@Test
	public void testNewFieldWithSolution() {
		assertTrue(new Field(false, 5, -1, 5).getCurrentValue() == 5);
	}

	@Test
	public void testSolution() {
		Field f;

		try {
			f = new Field(false, -2, -1, 3);
			fail("Initialisation with negative solution possible.");
		} catch (IllegalArgumentException e) {

		}

		for (int i = 0; i < 20; i += 5) {
			f = new Field(false, i, -1, i);
			assertTrue("solutionFail", f.getSolution() == i);
		}
	}

	@Test
	public void testCurrent() {
		Field f = new Field(true, 8, -1, 20);

		try {
			f.setCurrentValue(Field.EMPTYVAL - 1);
			fail("Initialisation with negative solution possible.");
		} catch (IllegalArgumentException e) {

		}

		try {
			f.setCurrentValue(Field.EMPTYVAL - 1, false);
			fail("Initialisation with negative solution possible.");
		} catch (IllegalArgumentException e) {

		}

		for (int i = 0; i < 20; i += 5) {
			f.setCurrentValue(i);
			assertTrue("currentFail", f.getCurrentValue() == i);
		}
	}

	@Test
	public void testToggleNote() {
		Field f = new Field(-1, 1);

		for (int i = 0; i < 20; i++) {
			f.toggleNote(i);
			assertTrue("toggleN1 doesn't work", f.isNoteSet(i));
		}

		for (int i = 0; i < 20; i++) {
			f.toggleNote(i);
			assertTrue("toggleN2 doesn't work", !f.isNoteSet(i));
		}

		assertFalse("toggleN2 doesn't work", f.isNoteSet(-4));

	}

	@Test
	public void testEditable() {
		Field f = new Field(true, 6, -1, 9);
		assertTrue("editableFail", f.isEditable());
		assertTrue(f.getNumberOfValues() == 9);
		assertTrue(f.getId() == -1);
		f = new Field(false, 6, -1, 9);
		assertFalse("editableFail", f.isEditable());

		f.setCurrentValue(3);
		assertTrue("editable doesn't lock", f.isNotWrong());
		f.setCurrentValue(-1);// darf nichts bewirken
		assertTrue("editable doesn't lock", f.isNotWrong());
	}

	@Test
	public void testIsSolvedCorrect() {
		Field f = new Field(true, 5, -1, 9);
		assertFalse("correctFail", f.isSolvedCorrect());
		f.setCurrentValue(5);
		assertTrue("correctFail", f.isSolvedCorrect());
		f.setCurrentValue(4);
		assertFalse("correctFail", f.isSolvedCorrect());
		f.setCurrentValue(5);
		assertTrue("correctFail", f.isSolvedCorrect());
	}

	@Test
	public void testIsNotWrong() {
		Field f = new Field(true, 4, -1, 9);
		f.setCurrentValue(0);
		assertFalse(f.isNotWrong());
	}

	@Test
	public void testEqual() {
		Field f = new Field(true, 3, -1, 9);
		Field g = new Field(true, 3, -1, 9);
		assertTrue(f.equals(g));

		g.toggleNote(2);

		assertFalse(f.equals(g));
		g.toggleNote(2);
		g.setCurrentValue(4);
		assertFalse(f.equals(g));

		assertFalse(f.equals(null));

		Field h = new Field(false, 2, -1, 9);
		assertFalse(f.equals(h));
	}

	@Test
	public void testClearIsEmpty() {
		Field f = new Field(true, 3, -1, 9);
		f.setCurrentValue(5);
		assertEquals(f.getCurrentValue(), 5);
		assertTrue(!f.isEmpty());
		f.clearCurrentValue();
		assertEquals(f.getCurrentValue(), Field.EMPTYVAL);
		assertTrue(f.isEmpty());
	}

	@Test(expected = IllegalArgumentException.class)
	public void testSetCurrent() {
		Field field = new Field(true, 0, 0, 9);
		field.setCurrentValue(2, true);
		field.setCurrentValue(-2, true);
	}

	@Test
	public void testToString() {
		Field field = new Field(true, 0, 0, 21);
		field.setCurrentValue(20);
		assertEquals("20", field.toString());
	}

	@Test(expected = IllegalArgumentException.class)
	public void testValueTooHigh() {
		Field field = new Field(true, 2, 0, 4);
		field.setCurrentValue(4);
	}
}