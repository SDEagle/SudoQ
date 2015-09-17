package de.sudoq.model.sudoku;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

import de.sudoq.model.sudoku.Constraint;
import de.sudoq.model.sudoku.ConstraintType;
import de.sudoq.model.sudoku.Position;
import de.sudoq.model.sudoku.Sudoku;
import de.sudoq.model.sudoku.SudokuBuilder;
import de.sudoq.model.sudoku.SumConstraintBehavior;
import de.sudoq.model.sudoku.sudokuTypes.SudokuTypes;
import de.sudoq.model.sudoku.sudokuTypes.TypeBuilder;

public class SumConstraintBehaviorTests {

	@Test(expected = IllegalArgumentException.class)
	public void testIllegalValue() {
		@SuppressWarnings("unused")
		SumConstraintBehavior c = new SumConstraintBehavior(-1);
	}

	@Test
	public void testConstraint() {
		
		TypeBuilder.get99();//just to force initialization of fileManager
		
		Sudoku sudoku = new SudokuBuilder(SudokuTypes.standard9x9).createSudoku();

		sudoku.getField(Position.get(0, 0)).setCurrentValue(1);
		sudoku.getField(Position.get(0, 1)).setCurrentValue(2);
		sudoku.getField(Position.get(0, 2)).setCurrentValue(3);
		sudoku.getField(Position.get(1, 0)).setCurrentValue(1);
		sudoku.getField(Position.get(1, 1)).setCurrentValue(2);
		sudoku.getField(Position.get(1, 2)).setCurrentValue(3);

		Constraint constraint = new Constraint(new SumConstraintBehavior(12), ConstraintType.LINE);
		constraint.addPosition(Position.get(0, 0));
		constraint.addPosition(Position.get(0, 1));
		constraint.addPosition(Position.get(0, 2));
		constraint.addPosition(Position.get(1, 0));
		constraint.addPosition(Position.get(1, 1));
		constraint.addPosition(Position.get(1, 2));

		assertFalse("constraint has unique behavior", constraint.hasUniqueBehavior());
		assertTrue("constraint not saturated", constraint.isSaturated(sudoku));

		sudoku.getField(Position.get(1, 2)).clearCurrentValue();
		assertTrue("constraint not saturated", constraint.isSaturated(sudoku));

		sudoku.getField(Position.get(1, 1)).setCurrentValue(8);
		assertFalse("constraint not saturated", constraint.isSaturated(sudoku));

		sudoku.getField(Position.get(1, 2)).setCurrentValue(2);
		assertFalse("constraint not saturated", constraint.isSaturated(sudoku));

		sudoku.getField(Position.get(1, 2)).setCurrentValue(4);
		assertFalse("constraint not saturated", constraint.isSaturated(sudoku));
	}
}
