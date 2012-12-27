package com.mohaine.brewcontroller.client.display;

public interface DrawerMouseListener {

	public static class DrawerMouseEvent {

		private int x;
		private int y;

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

	}

	void mouseUp(DrawerMouseEvent e);

	void mouseDown(DrawerMouseEvent e);

	void mouseDragged(DrawerMouseEvent e);
}
