package ca.utoronto.utm.paint;

import java.awt.*;
import java.util.ArrayList;
import java.util.Observable;

public class PaintModel extends Observable {
	private ArrayList<Point> points=new ArrayList<Point>();
	private ArrayList<Shape> shapes =new ArrayList<Shape>();

	
	public void addPoint(Point p){
		this.points.add(p);
		this.setChanged();
		this.notifyObservers();
	}
	public ArrayList<Point> getPoints(){
		return points;
	}
	
	public void addShape(Shape c){
		synchronized(this) {
			this.shapes.add(c);
		}
		this.setChanged();
		this.notifyObservers();
	}
	public  ArrayList<Shape> getShapes(){
		return shapes;
	}
	public void paint(Graphics g) {
		 Graphics2D g2 = (Graphics2D) g; // lets use the advanced api
			// setBackground (Color.blue); 
			// Origin is at the top left of the window 50 over, 75 down
			g2.setColor(Color.black);
			synchronized(this) {
				for(Shape s : shapes) {
					s.print(g2);
				}
			}
			
			g2.dispose();
	}
}
