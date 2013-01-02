package com.mohaine.brewcontroller.client.display;

public interface DrawerMouseListener {

	public static class DrawerMouseEvent {

		private int x;
		private int y;

		public DrawerMouseEvent(int x, int y) {
			super();
			this.x = x;
			this.y = y;
		}

		public int getX() {
			return x;
		}

		public int getY() {
			return y;
		}

	}

	void mouseUp(DrawerMouseEvent e);

	void mouseDown(DrawerMouseEvent e);

	void mouseDragged(DrawerMouseEvent e);
}
