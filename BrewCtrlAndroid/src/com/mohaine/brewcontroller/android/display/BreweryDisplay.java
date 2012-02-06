package com.mohaine.brewcontroller.android.display;

import java.util.ArrayList;
import java.util.List;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Rect;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;

import com.mohaine.brewcontroller.android.widget.ZoneView;
import com.mohaine.brewcontroller.layout.BrewLayout;
import com.mohaine.brewcontroller.layout.Zone;

public class BreweryDisplay extends ViewGroup {
	private static final String TAG = "BreweryDisplay";
	private List<ZoneView> zoneViews = new ArrayList<ZoneView>();

	public BreweryDisplay(Context context) {
		super(context);
	}

	@Override
	protected boolean drawChild(Canvas canvas, View child, long drawingTime) {

		// Log.v(TAG, "drawChild: " + child.getLeft() + "," + child.getTop() +
		// "," + child.getRight() + "," + child.getBottom());
		//
		// float l = child.getLeft();
		// float t = child.getTop();
		// float r = child.getRight();
		// float b = child.getBottom();
		boolean more = super.drawChild(canvas, child, drawingTime);

		// Rect clipBounds = canvas.getClipBounds();
		// canvas.clipRect(l, t, r, b);
		// child.draw(canvas);
		// canvas.clipRect(clipBounds);

		// Paint paint = new Paint();
		// paint.setAntiAlias(true);
		// paint.setColor(0xffff0000);
		// paint.setStrokeWidth(2.0f);

		// canvas.drawLine(l, t, l, b, paint);
		// canvas.drawLine(l, b, r, b, paint);
		// canvas.drawLine(r, t, r, b, paint);
		// canvas.drawLine(l, t, r, t, paint);

		// LayoutParams lp = (LayoutParams) child.getLayoutParams();
		// if (lp.horizontalSpacing > 0) {
		// float x = child.getRight();
		// float y = child.getTop() + child.getHeight() / 2.0f;
		// canvas.drawLine(x, y - 4.0f, x, y + 4.0f, mPaint);
		// canvas.drawLine(x, y, x + lp.horizontalSpacing, y, mPaint);
		// canvas.drawLine(x + lp.horizontalSpacing, y - 4.0f, x +
		// lp.horizontalSpacing, y + 4.0f, mPaint);
		// }
		// if (lp.breakLine) {
		// float x = child.getRight();
		// float y = child.getTop() + child.getHeight() / 2.0f;
		// canvas.drawLine(x, y, x, y + 6.0f, mPaint);
		// canvas.drawLine(x, y + 6.0f, x + 6.0f, y + 6.0f, mPaint);
		// }
		return more;
	}

	@Override
	protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
		super.onMeasure(widthMeasureSpec, heightMeasureSpec);

		Log.v(TAG, "onMeasure: " + widthMeasureSpec + "," + heightMeasureSpec);

		int widthSize = MeasureSpec.getSize(widthMeasureSpec);
		int heightSize = MeasureSpec.getSize(heightMeasureSpec);
		Log.v(TAG, "  onMeasure:  " + widthSize + "," + heightSize);

		int zoneHeight = heightSize / zoneViews.size();
		int padding = 30;
		zoneHeight -= padding;
		int zoneWidth = 200;

		Log.v(TAG, "  Zones:  " + zoneWidth + "," + zoneHeight);

		int yOffset = 5;
		Rect rect = new Rect(5, yOffset, 5 + zoneWidth, yOffset + zoneHeight);
		for (ZoneView zoneView : zoneViews) {
			measureChild(zoneView, widthMeasureSpec, heightMeasureSpec);

			LayoutParams lp = (LayoutParams) zoneView.getLayoutParams();

			lp.x = rect.left;
			lp.y = rect.top;
			lp.width = zoneWidth;
			lp.height = zoneHeight;
			Log.v(TAG, "   set lp: " + lp.x + "," + lp.y + " => " + (lp.x + lp.width) + "," + (lp.y + lp.height));

			rect.top += zoneHeight + padding;
		}

		setMeasuredDimension(resolveSize(widthSize, widthMeasureSpec), resolveSize(heightSize, heightMeasureSpec));
	}

	@Override
	protected void onLayout(boolean changed, int l, int t, int r, int b) {
		Log.v(TAG, "onLayout: ");
		for (ZoneView zoneView : zoneViews) {
			LayoutParams lp = (LayoutParams) zoneView.getLayoutParams();
			int right = lp.x + zoneView.getMeasuredWidth();
			int bottom = lp.y + zoneView.getMeasuredHeight();
			zoneView.layout(lp.x, lp.y, right, bottom);
			Log.v(TAG, "onLayout: " + lp.x + "," + lp.y + "  => " + right + "," + bottom);
		}
	}

	public void setBreweryLayout(BrewLayout brewLayout) {
		Log.v(TAG, "OverviewDisplayAndroid.setBreweryLayout()");
		List<Zone> zones = brewLayout.getZones();
		for (Zone zone : zones) {
			ZoneView zoneView = new ZoneView(getContext());
			zoneViews.add(zoneView);
			zoneView.setZone(zone);
			zoneView.setLayoutParams(new LayoutParams(200, 200));
			addView(zoneView);
		}
	}

	@Override
	protected boolean checkLayoutParams(ViewGroup.LayoutParams p) {
		return p instanceof LayoutParams;
	}

	@Override
	protected LayoutParams generateDefaultLayoutParams() {
		return new LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);
	}

	@Override
	public LayoutParams generateLayoutParams(AttributeSet attrs) {
		return new LayoutParams(getContext(), attrs);
	}

	@Override
	protected LayoutParams generateLayoutParams(ViewGroup.LayoutParams p) {
		return new LayoutParams(p.width, p.height);
	}

	public static class LayoutParams extends ViewGroup.LayoutParams {
		int x;
		int y;

		public LayoutParams(int w, int h) {
			super(w, h);
		}

		public LayoutParams(Context context, AttributeSet attrs) {
			super(context, attrs);
		}
	}

}