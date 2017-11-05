package ca.utoronto.utm.paint;

import javax.swing.JButton;
import javax.swing.JPanel;
import java.awt.Graphics;

public class ShapeButton extends JButton {
    private Shape shape;
    private int shapeNum;
    private JPanel canvas;

    public ShapeButton(int shapeNum) {
        this.shapeNum = shapeNum;
        this.canvas = new JPanel(){
            @Override
            public void paint(Graphics g) {
                shape.setX(canvas.getX());
                shape.setY(canvas.getY());
                shape.setXEnd(canvas.getWidth());
                shape.setYEnd(canvas.getHeight());
                shape.print(g);
                g.dispose();
            }
        };
        shape = new ShapeBuilder(shapeNum==0?5:shapeNum, 0, 0).build();
        this.add(canvas);
    }


}
