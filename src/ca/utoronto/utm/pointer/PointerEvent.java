package ca.utoronto.utm.pointer;

import java.awt.Component;
import java.awt.Point;
import java.awt.event.InputEvent;
import java.awt.event.MouseEvent;

public class PointerEvent {

    private int pointerId,pressure;
    private final MouseEvent event;


    /**
     * Constructs a prototypical Event.
     *
     * @param source The object on which the Event initially occurred.
     * @throws IllegalArgumentException if source is null.
     */
    public PointerEvent(Component source, int eventId, long when, int modifiers,int x,int y,int xAbs, int yAbs, int clickCount,int button, int pointerId, int pressure) {
        event = new MouseEvent(source,eventId,when,modifiers,x,y,xAbs,yAbs,clickCount,false,button);
        this.pointerId = pointerId;
        this.pressure = pressure;
    }

	public PointerEvent(MouseEvent event) {
		this.event = event;
		this.pointerId = 0;
		this.pressure = 0;
	}

	public int getPointerId() {
        return pointerId;
    }

    public int getPressure() {
        return pressure;
    }

	public MouseEvent getEvent(){
    	return event;
	}

	public Point getLocationOnScreen() {
		return event.getLocationOnScreen();
	}

	public int getXOnScreen() {
		return event.getXOnScreen();
	}

	public int getYOnScreen() {
		return event.getYOnScreen();
	}

	public int getModifiersEx() {
		return event.getModifiersEx();
	}

	public int getX() {
		return event.getX();
	}

	public int getY() {
		return event.getY();
	}

	public Point getPoint() {
		return event.getPoint();
	}

	public void translatePoint(int x, int y) {
		event.translatePoint(x, y);
	}

	public int getClickCount() {
		return event.getClickCount();
	}

	public int getButton() {
		return event.getButton();
	}

	public boolean isPopupTrigger() {
		return event.isPopupTrigger();
	}

	public static String getMouseModifiersText(int modifiers) {
		return MouseEvent.getMouseModifiersText(modifiers);
	}

	public String paramString() {
		return event.paramString();
	}

	public static int getMaskForButton(int button) {
		return InputEvent.getMaskForButton(button);
	}

	public boolean isShiftDown() {
		return event.isShiftDown();
	}

	public boolean isControlDown() {
		return event.isControlDown();
	}

	public boolean isMetaDown() {
		return event.isMetaDown();
	}

	public boolean isAltDown() {
		return event.isAltDown();
	}

	public boolean isAltGraphDown() {
		return event.isAltGraphDown();
	}

	public long getWhen() {
		return event.getWhen();
	}

	public int getModifiers() {
		return event.getModifiers();
	}

	public void consume() {
		event.consume();
	}

	public boolean isConsumed() {
		return event.isConsumed();
	}

	public static String getModifiersExText(int modifiers) {
		return InputEvent.getModifiersExText(modifiers);
	}

	public Component getComponent() {
		return event.getComponent();
	}

	public void setSource(Object newSource) {
		event.setSource(newSource);
	}

	public int getID() {
		return event.getID();
	}

	public Object getSource() {
		return event.getSource();
	}
}