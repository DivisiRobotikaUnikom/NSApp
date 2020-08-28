package id.ac.divisirobotikaunikom.nakulasadewa.Display;

import android.graphics.Bitmap;
import android.graphics.Canvas;

import id.ac.divisirobotikaunikom.nakulasadewa.MainActivity;

public class Mulut extends WajahObject {
    private Bitmap spriteSheet;
    public Mulut(Bitmap res, int w, int h){
        x = 0;
        y = 0;
        height = h;
        width = w;
        spriteSheet = res;
    }

    public void draw(Canvas canvas){
        int panjang = MainActivity.WIDTH;
        int lebar = MainActivity.HEIGHT;
        x = (panjang - width)/2;
        y = ((lebar - (Mata.heightMata / 2)) / 4) + Mata.heightMata + 20;
        canvas.drawBitmap(spriteSheet, x, y, null);
    }
}
