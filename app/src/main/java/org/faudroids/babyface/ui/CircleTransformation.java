package org.faudroids.babyface.ui;


import android.graphics.Bitmap;
import android.graphics.BitmapShader;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffColorFilter;

import com.squareup.picasso.Transformation;

public class CircleTransformation implements Transformation {

	private final int backgroundColor, borderColor;

	public CircleTransformation(int backgroundColor, int borderColor) {
		this.backgroundColor = backgroundColor;
		this.borderColor = borderColor;
	}

	@Override
	public Bitmap transform(Bitmap source) {
		int size = Math.min(source.getWidth(), source.getHeight());

		int x = (source.getWidth() - size) / 2;
		int y = (source.getHeight() - size) / 2;

		Bitmap squaredBitmap = Bitmap.createBitmap(source, x, y, size, size);
		if (squaredBitmap != source) {
			source.recycle();
		}

		Bitmap bitmap = Bitmap.createBitmap(size, size, source.getConfig());

		Canvas canvas = new Canvas(bitmap);
		Paint paint = new Paint();
		BitmapShader shader = new BitmapShader(squaredBitmap, BitmapShader.TileMode.CLAMP, BitmapShader.TileMode.CLAMP);
		paint.setShader(shader);
		paint.setAntiAlias(true);

		// color transparent background if present
		PorterDuff.Mode mMode = PorterDuff.Mode.OVERLAY;
		paint.setColorFilter(new PorterDuffColorFilter(backgroundColor, mMode));

		// apply circle transformation
		float radius = size / 2f;
		canvas.drawCircle(radius, radius, radius, paint);

		// add border
		Paint borderPaint = new Paint();
		borderPaint.setColor(borderColor);
		borderPaint.setStyle(Paint.Style.STROKE);
		borderPaint.setAntiAlias(true);
		borderPaint.setStrokeWidth(20);
		canvas.drawCircle(radius, radius, radius - 10, borderPaint);

		squaredBitmap.recycle();
		return bitmap;
	}


	@Override
	public String key() {
		return "circle";
	}

}