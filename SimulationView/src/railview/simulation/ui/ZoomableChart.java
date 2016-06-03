package railview.simulation.ui;

import javafx.event.EventHandler;
import javafx.geometry.Point2D;
import javafx.scene.Node;
import javafx.scene.chart.Axis;
import javafx.scene.chart.LineChart;
import javafx.scene.chart.NumberAxis;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.AnchorPane;


public class ZoomableChart<X, Y> extends LineChart<X,Y>  {

	
	private final NumberAxis x_Axis;
	private final NumberAxis y_Axis;
	

	private final AnchorPane pane;
	private final SelectionRectangle selectionRectangle;


	private Point2D selectionRectangleStart;
	private Point2D selectionRectangleEnd;



	public ZoomableChart(Axis<X> xAxis, Axis<Y> yAxis, AnchorPane anchorPane) {
		super(xAxis, yAxis);
		
		x_Axis = (NumberAxis) this.getXAxis();
		y_Axis = (NumberAxis) this.getYAxis();
		
		this.pane = anchorPane;
		selectionRectangle = new SelectionRectangle();
		pane.getChildren().add(selectionRectangle);
		addDragSelectionMechanism();
	}	


	private void addDragSelectionMechanism() {
		pane.addEventHandler(MouseEvent.MOUSE_PRESSED, new MousePressedHandler());
		pane.addEventHandler(MouseEvent.MOUSE_DRAGGED, new MouseDraggedHandler());		
		pane.addEventHandler(MouseEvent.MOUSE_RELEASED, new MouseReleasedHandler());
		
	
	}

	private Point2D computeRectanglePoint(double eventX, double eventY) {
		double lowerBoundX = computeOffsetInChart(x_Axis, false);
		double upperBoundX = lowerBoundX + x_Axis.getWidth();
		double lowerBoundY = computeOffsetInChart(y_Axis, true);
		double upperBoundY = lowerBoundY + y_Axis.getHeight();
		double x = Math.max(lowerBoundX, Math.min(eventX, upperBoundX));
		double y = Math.max(lowerBoundY, Math.min(eventY, upperBoundY));
		return new Point2D(x, y);
	}

	private double computeOffsetInChart(Node node, boolean vertical) {
		double offset = 0;
		do {
			if (vertical) {
				offset += node.getLayoutY();
			} else {
				offset += node.getLayoutX();
			}
			node = node.getParent();
		} while (node != this);
		return offset;
	}

	private final class MousePressedHandler implements EventHandler<MouseEvent> {
		@Override
		public void handle(final MouseEvent event) {

			if (event.isPrimaryButtonDown()) {
				return;
			}

			selectionRectangleStart = computeRectanglePoint(event.getX(), event.getY());
			event.consume();
		}
	}

	private final class MouseDraggedHandler implements EventHandler<MouseEvent> {
		@Override
		public void handle(final MouseEvent event) {

			if (event.isPrimaryButtonDown()) {
				return;
			}

			selectionRectangleEnd = computeRectanglePoint(event.getX(), event.getY());

			double x = Math.min(selectionRectangleStart.getX(), selectionRectangleEnd.getX());
			double y = Math.min(selectionRectangleStart.getY(), selectionRectangleEnd.getY());
			double width = Math.abs(selectionRectangleStart.getX() - selectionRectangleEnd.getX());
			double height = Math.abs(selectionRectangleStart.getY() - selectionRectangleEnd.getY());

			drawSelectionRectangle(x, y, width, height);
			event.consume();
		}

		private void drawSelectionRectangle(final double x, final double y, final double width, final double height) {
			selectionRectangle.setVisible(true);
			selectionRectangle.setX(x);
			selectionRectangle.setY(y);
			selectionRectangle.setWidth(width);
			selectionRectangle.setHeight(height);
		}
	}


	private final class MouseReleasedHandler implements EventHandler<MouseEvent> {

		private static final double MIN_RECTANGE_WIDTH = 10;

		private static final double MIN_RECTANGLE_HEIGHT = 10;

		@Override
		public void handle(final MouseEvent event) {
			hideSelectionRectangle();

			if (selectionRectangleStart == null || selectionRectangleEnd == null) {
				return;
			}

			if (isRectangleSizeTooSmall()) {
				return;
			}

			setAxisBounds();
			selectionRectangleStart = null;
			selectionRectangleEnd = null;

			pane.requestFocus();
			event.consume();
		}

		private boolean isRectangleSizeTooSmall() {
			double width = Math.abs(selectionRectangleEnd.getX() - selectionRectangleStart.getX());
			double height = Math.abs(selectionRectangleEnd.getY() - selectionRectangleStart.getY());
			return width < MIN_RECTANGE_WIDTH || height < MIN_RECTANGLE_HEIGHT;
		}


		private void hideSelectionRectangle() {
			selectionRectangle.setVisible(false);
		}

		private void setAxisBounds() {
			disableAutoRanging();

			double selectionMinX = Math.min(selectionRectangleStart.getX(), selectionRectangleEnd.getX());
			double selectionMaxX = Math.max(selectionRectangleStart.getX(), selectionRectangleEnd.getX());
			double selectionMinY = Math.min(selectionRectangleStart.getY(), selectionRectangleEnd.getY());
			double selectionMaxY = Math.max(selectionRectangleStart.getY(), selectionRectangleEnd.getY());

			setHorizontalBounds(selectionMinX, selectionMaxX);
			setVerticalBounds(selectionMinY, selectionMaxY);
		}

		private void disableAutoRanging() {
			
			x_Axis.setAutoRanging(false);
			y_Axis.setAutoRanging(false);
		}

		private void setHorizontalBounds(double minPixelPosition, double maxPixelPosition) {
			double currentLowerBound = x_Axis.getLowerBound();
			double currentUpperBound = x_Axis.getUpperBound();
			double offset = computeOffsetInChart(x_Axis, false);
			setLowerBoundX(minPixelPosition, currentLowerBound, currentUpperBound, offset);
			setUpperBoundX(maxPixelPosition, currentLowerBound, currentUpperBound, offset);
		}

		private void setVerticalBounds(double minPixelPosition, double maxPixelPosition) {
			double currentLowerBound = y_Axis.getLowerBound();
			double currentUpperBound = y_Axis.getUpperBound();
			double offset = computeOffsetInChart(y_Axis, true);
			setLowerBoundY(maxPixelPosition, currentLowerBound, currentUpperBound, offset);
			setUpperBoundY(minPixelPosition, currentLowerBound, currentUpperBound, offset);
		}

		private void setLowerBoundX(double pixelPosition, double currentLowerBound, double currentUpperBound,
				double offset) {
			double newLowerBound = computeBound(pixelPosition, offset, x_Axis.getWidth(), currentLowerBound,
					currentUpperBound, false);
			x_Axis.setLowerBound(newLowerBound);
		}

		private void setUpperBoundX(double pixelPosition, double currentLowerBound, double currentUpperBound,
				double offset) {
			double newUpperBound = computeBound(pixelPosition, offset, x_Axis.getWidth(), currentLowerBound,
					currentUpperBound, false);
			x_Axis.setUpperBound(newUpperBound);
		}

		private void setLowerBoundY(double pixelPosition, double currentLowerBound, double currentUpperBound,
				double offset) {
			double newLowerBound = computeBound(pixelPosition, offset, y_Axis.getHeight(), currentLowerBound,
					currentUpperBound, true);
			y_Axis.setLowerBound(newLowerBound);
		}

		private void setUpperBoundY(double pixelPosition, double currentLowerBound, double currentUpperBound,
				double offset) {
			double newUpperBound = computeBound(pixelPosition, offset, y_Axis.getHeight(), currentLowerBound,
					currentUpperBound, true);
			y_Axis.setUpperBound(newUpperBound);
		}

		private double computeBound(double pixelPosition, double pixelOffset, double pixelLength, double lowerBound,
				double upperBound, boolean axisInverted) {
			double pixelPositionWithoutOffset = pixelPosition - pixelOffset;
			double relativePosition = pixelPositionWithoutOffset / pixelLength;
			double axisLength = upperBound - lowerBound;

			double offset = 0;
			int sign = 0;
			if (axisInverted) {
				offset = upperBound;
				sign = -1;
			} else {
				offset = lowerBound;
				sign = 1;
			}

			double newBound = offset + sign * relativePosition * axisLength;
			return newBound;
		}
	}


}




