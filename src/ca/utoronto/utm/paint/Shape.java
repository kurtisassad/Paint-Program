package ca.utoronto.utm.paint;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Stroke;

public abstract class Shape {
    protected int x,y;
    protected int xEnd,yEnd;
    protected Color colour;
    protected float lineThickness;
    protected Stroke stroke;

	public int getX() {
		return x;
	}

	public void setX(int x) {
		this.x = x;
	}

	public int getY() {
		return y;
	}

	public void setY(int y) {
		this.y = y;
	}

	public int getxEnd() {
		return xEnd;
	}

	public void setxEnd(int xEnd) {
		this.xEnd = xEnd;
	}

	public int getyEnd() {
		return yEnd;
	}

	public void setyEnd(int yEnd) {
		this.yEnd = yEnd;
	}

	public Color getColour() { return colour; }

	public void setColour(Color colour) { this.colour = colour; }

	public void setLineThickness(float lineThickness) {
		this.lineThickness = lineThickness;
	}

	public void setStroke(Stroke stroke) {
		this.stroke = stroke;
	}

	public void translateOrigin(int x, int y) {
		this.x+=x;
		this.y+=y;
	}

	public void translateEndPoint(int x, int y) {
		this.xEnd+=x;
		this.yEnd+=y;
	}

	protected void prepare(Graphics2D g2) {
		g2.setColor(colour);
		g2.setStroke(stroke);
	}

    public abstract void print(Graphics2D g2);
}
