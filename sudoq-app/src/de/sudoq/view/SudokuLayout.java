/*
 * SudoQ is a Sudoku-App for Adroid Devices with Version 2.2 at least.
 * Copyright (C) 2012  Heiko Klare, Julian Geppert, Jan-Bernhard Kordaß, Jonathan Kieling, Tim Zeitz, Timo Abele
 * This program is free software; you can redistribute it and/or modify it under the terms of the GNU General Public License as published by the Free Software Foundation; either version 3 of the License, or (at your option) any later version. 
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License for more details. 
 * You should have received a copy of the GNU General Public License along with this program; if not, see <http://www.gnu.org/licenses/>.
 */
package de.sudoq.view;

import java.util.ArrayList;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.util.Log;
import android.view.MotionEvent;
import android.widget.RelativeLayout;
import de.sudoq.controller.sudoku.FieldInteractionListener;
import de.sudoq.controller.sudoku.FieldViewPainter;
import de.sudoq.controller.sudoku.ObservableFieldInteraction;
import de.sudoq.controller.sudoku.SudokuActivity;
import de.sudoq.model.game.Assistances;
import de.sudoq.model.game.Game;
import de.sudoq.model.sudoku.Constraint;
import de.sudoq.model.sudoku.ConstraintType;
import de.sudoq.model.sudoku.Field;
import de.sudoq.model.sudoku.Position;
import de.sudoq.model.sudoku.Sudoku;
import de.sudoq.model.sudoku.sudokuTypes.SudokuType;
import de.sudoq.model.sudoku.sudokuTypes.TypeBasic;

/**
 * Eine View als RealativeLayout, die eine Sudoku-Anzeige verwaltet.
 */
public class SudokuLayout extends RelativeLayout implements ObservableFieldInteraction, ZoomableView {
	/**
	 * Das Log-Tag für den LogCat
	 */
	private static final String LOG_TAG = SudokuLayout.class.getSimpleName();

	/**
	 * Das Game, welches diese Anzeige verwaltet
	 */
	private Game game;

	/**
	 * Der Kontext dieser View
	 */
	private Context context;

	/**
	 * Die Standardgröße eines Feldes
	 */
	private int defaultFieldViewSize;

	/**
	 * Die aktuelle Größe eines Feldes
	 */
	// private int currentFieldViewSize;

	/**
	 * Die aktuell ausgewählte FieldView
	 */
	private SudokuFieldView currentFieldView;

	private float zoomFactor;

	/**
	 * Ein Array aller FieldViews
	 */
	private SudokuFieldView[][] sudokuFieldViews;

	/**
	 * Der linke Rand, verursacht durch ein zu niedriges Layout
	 */
	private int leftMargin;

	/**
	 * Der linke Rand, verursacht durch ein zu schmales Layout
	 */
	private int topMargin;

	/**
	 * Der Platz zwischen 2 Blöcken
	 */
	private static int spacing = 2;

	/**
	 * Instanziiert eine neue SudokuView in dem spezifizierten Kontext.
	 * 
	 * @param context
	 *            Der Kontext, in dem diese View angezeigt wird
	 */
	public SudokuLayout(Context context) {
		super(context);
		this.context = context;
		this.game = ((SudokuActivity) context).getGame();

		this.defaultFieldViewSize = 40;
		this.zoomFactor = 1.0f;
		// this.currentFieldViewSize = this.defaultFieldViewSize;
		this.setWillNotDraw(false);

		inflateSudoku();
	}

	/**
	 * Erstellt die Anzeige des Sudokus.
	 */
	private void inflateSudoku() {
		FieldViewPainter.getInstance().flushMarkings();
		this.removeAllViews();

		Sudoku sudoku = this.game.getSudoku();
		SudokuType sudokuType = sudoku.getSudokuType();
		this.sudokuFieldViews = new SudokuFieldView[sudokuType.getSize().getX() + 1][sudokuType.getSize().getY() + 1];
		for (int x = 0; x <= sudokuType.getSize().getX(); x++) {
			for (int y = 0; y <= sudokuType.getSize().getY(); y++) {
				Field field = sudoku.getField(Position.get(x, y));
				if (field != null) {
					RelativeLayout.LayoutParams params = new RelativeLayout.LayoutParams((int) this.getCurrentFieldViewSize(), (int) this.defaultFieldViewSize);
					params.topMargin = (y * (int) this.getCurrentFieldViewSize()) + y;
					params.leftMargin = (x * (int) this.getCurrentFieldViewSize()) + x;
					this.sudokuFieldViews[x][y] = new SudokuFieldView(context, game, field, this.game.isAssistanceAvailable(Assistances.markWrongSymbol));
					field.registerListener(this.sudokuFieldViews[x][y]);
					this.addView(this.sudokuFieldViews[x][y], params);
				} else if (sudoku.getSudokuType().getSize().getX() == x && sudoku.getSudokuType().getSize().getY() == y) {
					RelativeLayout.LayoutParams params = new RelativeLayout.LayoutParams((int) this.getCurrentFieldViewSize(), (int) this.defaultFieldViewSize);
					params.topMargin = ((y - 1) * (int) this.getCurrentFieldViewSize()) + (y - 1) + getCurrentTopMargin();
					params.leftMargin = ((x - 1) * (int) this.getCurrentFieldViewSize()) + (x - 1) + getCurrentLeftMargin();
					this.sudokuFieldViews[x][y] = new SudokuFieldView(context, game, this.game.getSudoku().getField(Position.get(x - 1, y - 1)), this.game.isAssistanceAvailable(Assistances.markWrongSymbol));
					this.addView(this.sudokuFieldViews[x][y], params);
					this.sudokuFieldViews[x][y].setVisibility(INVISIBLE);
				}
			}
		}

		ArrayList<Constraint> allConstraints = this.game.getSudoku().getSudokuType().getConstraints();
		ArrayList<Position> positions = null;
		if (this.game.isAssistanceAvailable(Assistances.markRowColumn)) {
			for (int constrNum = 0; constrNum < allConstraints.size(); constrNum++) {
				if (allConstraints.get(constrNum).getType().equals(ConstraintType.LINE)) {
					positions = allConstraints.get(constrNum).getPositions();
					for (int i = 0; i < positions.size(); i++) {
						for (int k = 0; k < positions.size(); k++) {
							if (i != k)
								this.sudokuFieldViews[positions.get(i).getX()][positions.get(i).getY()].addConnectedField(
										this.sudokuFieldViews[positions.get(k).getX()][positions.get(k).getY()]);
						}
					}
				}
			}
		}
	}

	/**
	 * Berechnet das aktuelle Spacing (gem. dem aktuellen ZoomFaktor) und gibt
	 * es zurück.
	 * 
	 * @return Das aktuelle Spacing
	 */
	private int getCurrentSpacing() {
		return (int) (spacing * this.zoomFactor);
	}

	/**
	 * Berechnet das aktuelle obere Margin (gem. dem aktuellen ZoomFaktor) und
	 * gibt es zurück.
	 * 
	 * @return Das aktuelle obere Margin
	 */
	public int getCurrentTopMargin() {
		return (int) (this.topMargin * this.zoomFactor);
	}

	/**
	 * Berechnet das aktuelle linke Margin (gem. dem aktuellen ZoomFaktor) und
	 * gibt es zurück.
	 * 
	 * @return Das aktuelle linke Margin
	 */
	public int getCurrentLeftMargin() {
		return (int) (this.leftMargin * this.zoomFactor);
	}

	/**
	 * Aktualisiert die Sudoku-Anzeige bzw. der enthaltenen Felder.
	 */
	private void refresh() {
		if (this.sudokuFieldViews != null) {
			Position typeSize = this.game.getSudoku().getSudokuType().getSize();
			//Iterate over all positions within the size 
			//and one more! why do we go 0 to limit, why <= ?
			for (    int x = 0; x <= typeSize.getX(); x++) {
				for (int y = 0; y <= typeSize.getY(); y++) {
					if (game.getSudoku().getField(Position.get(x, y)) != null) {
						//Position is not null. This check is important for samurai sudokus 
						RelativeLayout.LayoutParams params = (RelativeLayout.LayoutParams) this.sudokuFieldViews[x][y].getLayoutParams();
						params.width  = (int) this.getCurrentFieldViewSize();
						params.height = (int) this.getCurrentFieldViewSize();
						params.topMargin =  (int) (getCurrentTopMargin()  + (y * (this.getCurrentFieldViewSize() + getCurrentSpacing())));
						params.leftMargin = (int) (getCurrentLeftMargin() + (x * (this.getCurrentFieldViewSize() + getCurrentSpacing())));
						this.sudokuFieldViews[x][y].setLayoutParams(params);
						this.sudokuFieldViews[x][y].invalidate();
					} else if (x == typeSize.getX() && 
							   y == typeSize.getY() ) {
						//both x and y are over the limit. Why do we go there?
						RelativeLayout.LayoutParams params = new RelativeLayout.LayoutParams((int) this.getCurrentFieldViewSize(), (int) this.defaultFieldViewSize);
						params.width  = (int) this.getCurrentFieldViewSize();
						params.height = (int) this.getCurrentFieldViewSize();
						params.topMargin =  (int) (2 * getCurrentTopMargin()  + ((y - 1) * (this.getCurrentFieldViewSize() + getCurrentSpacing())));
						params.leftMargin = (int) (2 * getCurrentLeftMargin() + ((x - 1) * (this.getCurrentFieldViewSize() + getCurrentSpacing())));
						this.sudokuFieldViews[x][y].setLayoutParams(params);
						this.sudokuFieldViews[x][y].invalidate();
					}
				}
			}
		}
		invalidate();
	}

	@Override
	/**
	 * Draws all black borders for the sudoku, nothing else
	 * Fields have to be drawn after this method
	 * No insight on the coordinate-wise workings, unsure about the 'i's.
	 */
	protected void onDraw(Canvas canvas) {
		super.onDraw(canvas);
		float edgeRadius = getCurrentFieldViewSize() / 20.0f;
		Paint paint = new Paint();
		paint.setColor(Color.BLACK);
		for (Constraint c: this.game.getSudoku().getSudokuType()) {
			if (c.getType().equals(ConstraintType.BLOCK)) {
				for (Position p: c) {
					/* determine whether the position p is in the (right|left|top|bottom) border of its block constraint.
					 * test for 0 to avoid illegalArgExc for neg. vals
					 * careful when trying to optimize this definition: blocks can be squiggly (every additional compound to row/col but extra as in hypersudoku is s.th. different)
					 * */
					boolean isLeft   = p.getX() == 0 || !c.includes(Position.get(p.getX() - 1, p.getY()    ));
					boolean isRight  =                  !c.includes(Position.get(p.getX() + 1, p.getY()    ));
					boolean isTop    = p.getY() == 0 || !c.includes(Position.get(p.getX(),     p.getY() - 1));
					boolean isBottom =                  !c.includes(Position.get(p.getX(),     p.getY() + 1));
					/* apparently:
					 *   00 10 20 30 ...  
					 *   01 11
					 *   02    xy
					 *   .
					 *   .
					 * */
					
					
					for (int i = 1; i <= getCurrentSpacing(); i++) {//?
						//deklariert hier, weil wir es nicht früher brauchen, effizienter wäre weiter oben
						int fieldSizeAndSpacing = this.getCurrentFieldViewSize() + getCurrentSpacing();
						/* these first 4 seem similar. drawing the black line around?*/
						/* fields that touch the edge: Paint your edge but leave space at the corners*/
						//paint.setColor(Color.TRANSPARENT);

						if (isLeft) {
							float startX = getCurrentLeftMargin() + p.getX() * (int) (fieldSizeAndSpacing) - i;
							float stopX = startX;
							
							float startY = getCurrentTopMargin()  +  p.getY()      * (int) (fieldSizeAndSpacing) + edgeRadius;
							float stopY  = getCurrentTopMargin()  + (p.getY() + 1) * (int) (fieldSizeAndSpacing) - edgeRadius - getCurrentSpacing();
							canvas.drawLine(startX, startY,	stopX, stopY, paint);
						}
						if (isRight) {
							float startX = getCurrentLeftMargin() + (p.getX() + 1) * (int) (fieldSizeAndSpacing) - getCurrentSpacing() - 1 + i;
							float stopX  = startX;
							
							float startY = getCurrentTopMargin()  +  p.getY()      * (int) (fieldSizeAndSpacing) + edgeRadius;
							float stopY  = getCurrentTopMargin()  + (p.getY() + 1) * (int) (fieldSizeAndSpacing) - edgeRadius - getCurrentSpacing();
							canvas.drawLine(startX, startY,	stopX, stopY, paint);
						}
						if (isTop) {
							float startX = getCurrentLeftMargin() +  p.getX()      * (int) (fieldSizeAndSpacing) + edgeRadius;
							float stopX  = getCurrentLeftMargin() + (p.getX() + 1) * (int) (fieldSizeAndSpacing) - edgeRadius - getCurrentSpacing();
							
							float startY = getCurrentTopMargin()  + p.getY() * (int) (fieldSizeAndSpacing) - i;
							float stopY  = startY;
							canvas.drawLine(startX, startY,	stopX, stopY, paint);
						}
						if (isBottom) {
							float startX = getCurrentLeftMargin() +  p.getX()      * (int) (fieldSizeAndSpacing) + edgeRadius;
							float stopX  = getCurrentLeftMargin() + (p.getX() + 1) * (int) (fieldSizeAndSpacing) - edgeRadius - getCurrentSpacing();
							float startY = getCurrentTopMargin()  + (p.getY() + 1) * (int) (fieldSizeAndSpacing) - getCurrentSpacing() - 1 + i;
							float stopY  = startY;
							canvas.drawLine(startX, startY,	stopX, stopY, paint);
						}

						/* Fields at corners of their block draw a circle for a round circumference*/
						/*TopLeft*/
						if (isLeft && isTop) {
							//paint.setColor(Color.MAGENTA);
							canvas.drawCircle(
									getCurrentLeftMargin() + p.getX() * (int) (this.getCurrentFieldViewSize() + getCurrentSpacing()) + edgeRadius,
									getCurrentTopMargin()  + p.getY() * (int) (this.getCurrentFieldViewSize() + getCurrentSpacing()) + edgeRadius,
									edgeRadius + i, 
									paint);
						}	
						
						/* Top Right*/
						if (isRight && isTop) {
							//paint.setColor(Color.BLUE);
							canvas.drawCircle(
									getCurrentLeftMargin() + (p.getX() + 1) * (int) (this.getCurrentFieldViewSize() + getCurrentSpacing()) - getCurrentSpacing() - edgeRadius,
									getCurrentTopMargin()  + p.getY()       * (int) (this.getCurrentFieldViewSize() + getCurrentSpacing()) + edgeRadius,
									edgeRadius + i, 
									paint);
						}

						/*Bottom Left*/
						if (isLeft && isBottom) {
							//paint.setColor(Color.CYAN);
							canvas.drawCircle(
									getCurrentLeftMargin() + p.getX() *       (int) (this.getCurrentFieldViewSize() + getCurrentSpacing()) + edgeRadius,
									getCurrentTopMargin()  + (p.getY() + 1) * (int) (this.getCurrentFieldViewSize() + getCurrentSpacing()) - edgeRadius - getCurrentSpacing(),
									edgeRadius + i, 
									paint);
						}
						
						/*BottomRight*/
						if (isRight && isBottom) {
							//paint.setColor(Color.RED);
							canvas.drawCircle(
									getCurrentLeftMargin() + (p.getX() + 1) * (int) (this.getCurrentFieldViewSize() + getCurrentSpacing()) - edgeRadius - getCurrentSpacing(),
									getCurrentTopMargin()  + (p.getY() + 1) * (int) (this.getCurrentFieldViewSize() + getCurrentSpacing()) - edgeRadius - getCurrentSpacing(),
									edgeRadius + i, 
									paint);
						}
						
						
						
						/*Now filling the edges*/
						boolean belowRightMember = c.includes(Position.get(p.getX() + 1, p.getY() + 1));
						/*For a field on the right border, fill edge to neighbour below 
						 * 
						 * !isBottom excludes:      corner to the left -> no neighbour directly below i.e. unwanted filling
						 *  3rd condition excludes: corner to the right-> member below right          i.e. unwanted filling
						 * 
						 * */
						if (isRight && !isBottom && !belowRightMember) {
							canvas.drawLine(
									getCurrentLeftMargin() + (p.getX() + 1) * (int) (this.getCurrentFieldViewSize() + getCurrentSpacing()) - getCurrentSpacing() - 1 + i,
									getCurrentTopMargin()  + (p.getY() + 1) * (int) (this.getCurrentFieldViewSize() + getCurrentSpacing()) - getCurrentSpacing() - edgeRadius,
									getCurrentLeftMargin() + (p.getX() + 1) * (int) (this.getCurrentFieldViewSize() + getCurrentSpacing()) - getCurrentSpacing() - 1 + i,
									getCurrentTopMargin()  + (p.getY() + 1) * (int) (this.getCurrentFieldViewSize() + getCurrentSpacing()) + edgeRadius,
									paint);
						}
						/*For a field at the bottom, fill edge to right neighbour */
						if (isBottom && !isRight && !belowRightMember) {
							canvas.drawLine(
									getCurrentLeftMargin() + (p.getX() + 1) * (int) (this.getCurrentFieldViewSize() + getCurrentSpacing()) - edgeRadius - getCurrentSpacing(),
									getCurrentTopMargin()  + (p.getY() + 1) * (int) (this.getCurrentFieldViewSize() + getCurrentSpacing()) - getCurrentSpacing() - 1 + i,
									getCurrentLeftMargin() + (p.getX() + 1) * (int) (this.getCurrentFieldViewSize() + getCurrentSpacing()) + edgeRadius,
									getCurrentTopMargin()  + (p.getY() + 1) * (int) (this.getCurrentFieldViewSize() + getCurrentSpacing()) - getCurrentSpacing() - 1 + i,
									paint);
						}
						
						/*For a field on the left border, fill edge to upper neighbour*/
						if (isLeft && !isTop && (p.getX() == 0 || !c.includes(Position.get(p.getX() - 1, p.getY() - 1)))) {
							canvas.drawLine(
									getCurrentLeftMargin() + p.getX() * (int) (this.getCurrentFieldViewSize() + getCurrentSpacing()) - i,
									getCurrentTopMargin()  + p.getY() * (int) (this.getCurrentFieldViewSize() + getCurrentSpacing()) - getCurrentSpacing() - edgeRadius,
									getCurrentLeftMargin() + p.getX() * (int) (this.getCurrentFieldViewSize() + getCurrentSpacing()) - i,
									getCurrentTopMargin()  + p.getY() * (int) (this.getCurrentFieldViewSize() + getCurrentSpacing()) + edgeRadius,
									paint);
						}
						/*For a field at the top fill to the left*/
						if (isTop && !isLeft && (p.getY() == 0 || !c.includes(Position.get(p.getX() - 1, p.getY() - 1)))) {
							canvas.drawLine(
									getCurrentLeftMargin() + p.getX() * (int) (this.getCurrentFieldViewSize() + getCurrentSpacing()) - edgeRadius - getCurrentSpacing(),
									getCurrentTopMargin()  + p.getY() * (int) (this.getCurrentFieldViewSize() + getCurrentSpacing()) - i,
									getCurrentLeftMargin() + p.getX() * (int) (this.getCurrentFieldViewSize() + getCurrentSpacing()) + edgeRadius,
									getCurrentTopMargin()  + p.getY() * (int) (this.getCurrentFieldViewSize() + getCurrentSpacing()) - i,
									paint);
						}
					}
				}
			}
		}
	}

	/**
	 * Zoom so heraus, dass ein diese View optimal in ein Layout der
	 * spezifizierte Größe passt
	 * 
	 * @param width
	 *            Die Breite auf die optimiert werden soll
	 * @param height
	 *            Die Höhe auf die optimiert werden soll
	 */
	public void optiZoom(int width, int height) {
		Log.d(LOG_TAG, "SudokuView height intern: " + this.getMeasuredHeight());
		SudokuType sudokuType = this.game.getSudoku().getSudokuType();
		int size = width < height ? width : height;
		int numberOfFields = width < height ? sudokuType.getSize().getX() : sudokuType.getSize().getY();
		this.defaultFieldViewSize = (size - (numberOfFields + 1) * spacing) / numberOfFields;
		// this.currentFieldViewSize = this.defaultFieldViewSize;

		this.leftMargin = (int) (width - sudokuType.getSize().getX() *
				(this.getCurrentFieldViewSize() + spacing) + spacing)
				/ 2;
		this.topMargin = (int) (height - sudokuType.getSize().getY() *
				(this.getCurrentFieldViewSize() + spacing) + spacing)
				/ 2;
		Log.d(LOG_TAG, "Sudoku width: " + width);
		Log.d(LOG_TAG, "Sudoku height: " + height);
		this.refresh();
	}

	/**
	 * Touch-Events werden nicht verarbeitet.
	 */
	@Override
	public boolean onTouchEvent(MotionEvent event) {
		return false;

	}

	/**
	 * Gibt das Array der SudokuFieldViews zurück.
	 * 
	 * @return Das Array der SudokuFieldViews
	 */
	public SudokuFieldView[][] getSudokuFieldViews() {
		return this.sudokuFieldViews;
	}

	/**
	 * Setzt den aktuellen Zoom-Faktor für diese View und refresh sie.
	 * 
	 * @param factor
	 *            Der Zoom-Faktor
	 */
	public boolean zoom(float factor) {
		this.zoomFactor = factor;
		refresh();
		return true;
	}

	/**
	 * Gibt die aktuell aktive SudokuFieldView dieser View zurück.
	 * 
	 * @return Die aktive SudokuFieldView
	 */
	public SudokuFieldView getCurrentFieldView() {
		return this.currentFieldView;
	}

	/**
	 * Setzt die aktuelle SudokuFieldView
	 * 
	 * @param currentFieldView
	 *            die zu setzende SudokuFieldView
	 */
	public void setCurrentFieldView(SudokuFieldView currentFieldView) {
		this.currentFieldView = currentFieldView;
	}

	/**
	 * Gibt die aktuelle Größe einer FieldView zurück.
	 * 
	 * @return die aktuelle Größe einer FieldView
	 */
	private int getCurrentFieldViewSize() {
		return (int) (this.defaultFieldViewSize * zoomFactor);
	}

	/**
	 * Unbenutzt.
	 * 
	 * @throws UnsupportedOperationException
	 *             Wirft immer eine UnsupportedOperationException
	 */
	public void notifyListener() {
		throw new UnsupportedOperationException();
	}

	/**
	 * {@inheritDoc}
	 */
	public void registerListener(FieldInteractionListener listener) {
		SudokuType sudokuType = this.game.getSudoku().getSudokuType();
		for (Position p: sudokuType.getValidPositions())
			this.sudokuFieldViews[p.getX()][p.getY()].registerListener(listener);
	}

	/**
	 * {@inheritDoc}
	 */
	public void removeListener(FieldInteractionListener listener) {
		SudokuType sudokuType = this.game.getSudoku().getSudokuType();
		for (Position p: sudokuType.getValidPositions()){
			this.sudokuFieldViews[p.getX()][p.getY()].removeListener(listener);
		}
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public float getMinZoomFactor() {
		return 1.0f;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public float getMaxZoomFactor() {
		return this.game.getSudoku().getSudokuType().getSize().getX() / 2.0f;
	}

}
