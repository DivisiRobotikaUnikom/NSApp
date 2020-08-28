package id.ac.divisirobotikaunikom.nakulasadewa.Display;

import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;

public class Background {
    private Paint pBg1 = new Paint();
    public Background() {
        pBg1.setColor(Color.BLACK);
    }
    public void draw(Canvas canvas) {
        canvas.drawRect(0, 0, canvas.getWidth() * 2, canvas.getHeight() * 2, pBg1);
    }
}
