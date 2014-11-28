/*
 * SudoQ is a Sudoku-App for Adroid Devices with Version 2.2 at least.
 * Copyright (C) 2012  Heiko Klare, Julian Geppert, Jan-Bernhard Kordaß, Jonathan Kieling, Tim Zeitz, Timo Abele
 * This program is free software; you can redistribute it and/or modify it under the terms of the GNU General Public License as published by the Free Software Foundation; either version 3 of the License, or (at your option) any later version. 
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License for more details. 
 * You should have received a copy of the GNU General Public License along with this program; if not, see <http://www.gnu.org/licenses/>.
 */
package de.sudoq.model.sudoku.sudokuTypes;

import de.sudoq.model.sudoku.complexity.Complexity;
import de.sudoq.model.sudoku.complexity.ComplexityConstraint;

public class SquigglyBSudokuType9x9 extends TypeSquiggly {

	protected final static String PATTERN = "AAAAABBBB"
			+ "AACCCCDBB"
			+ "ACCEFCDDB"
			+ "AEEEFCCDB"
			+ "GEFFFFFDB"
			+ "GEHHFDDDI"
			+ "GEEHFDHHI"
			+ "GGEHHHHII"
			+ "GGGGIIIII";

	/**
	 * Erzeugt einen neuen SquigglyB-Sudokutyp
	 */
	public SquigglyBSudokuType9x9() {
		super(PATTERN);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public SudokuTypes getEnumType() {
		return SudokuTypes.squigglyb;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public ComplexityConstraint buildComplexityConstraint(Complexity complexity) {
		ComplexityConstraint ret = null;

		if (complexity != null) {

			switch (complexity) {
			case easy:
				ret = new ComplexityConstraint(Complexity.easy, 40, 500, 800, 2);
				break;
			case medium:
				ret = new ComplexityConstraint(Complexity.medium, 32, 750, 1050, 3);
				break;
			case difficult:
				ret = new ComplexityConstraint(Complexity.difficult, 28, 1000, 2500, Integer.MAX_VALUE);
				break;
			case infernal:
				ret = new ComplexityConstraint(Complexity.infernal, 27, 2500, 25000, Integer.MAX_VALUE);
				break;
			case arbitrary:
				ret = new ComplexityConstraint(Complexity.arbitrary, 32, 1, Integer.MAX_VALUE, Integer.MAX_VALUE);
				break;
			}
		}
		return ret;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public float getStandardAllocationFactor() {
		return 0.20f;
	}
}
