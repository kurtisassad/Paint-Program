package ca.utoronto.utm.pointer;

import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseWheelEvent;

/**
 * Proxy Java MouseEvent to PointerEvent
 */
public class MouseEventProxy extends MouseAdapter{

	private PointerListener listener;

	public MouseEventProxy(PointerListener listener) {
		this.listener = listener;
	}

	@Override
	public void mouseWheelMoved(MouseWheelEvent e) {
		listener.pointerUpdated(new PointerEvent(e));
	}

	@Override
	public void mousePressed(MouseEvent e) {
		listener.pointerUpdated(new PointerEvent(e));
	}

	@Override
	public void mouseReleased(MouseEvent e) {
		listener.pointerUpdated(new PointerEvent(e));
	}

	@Override
	public void mouseDragged(MouseEvent e) {
		PointerEvent p = new PointerEvent(e);
		p.eventOverride(MouseEvent.MOUSE_MOVED);
		listener.pointerUpdated(p);
	}

	@Override
	public void mouseMoved(MouseEvent e) {
		listener.pointerUpdated(new PointerEvent(e));
	}
}
