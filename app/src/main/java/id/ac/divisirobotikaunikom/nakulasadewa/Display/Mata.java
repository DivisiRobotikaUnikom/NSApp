package id.ac.divisirobotikaunikom.nakulasadewa.Display;

import android.graphics.Bitmap;
import android.graphics.Canvas;

import id.ac.divisirobotikaunikom.nakulasadewa.MainActivity;

public class Mata extends WajahObject {
    private Bitmap spriteSheet;
    private Animasi animation = new Animasi();
    public static int heightMata;

    public Mata(Bitmap res, int w, int h, int numFrames) {
        x = 0;
        y = 0;
        height = h;
        width = w;
        heightMata = h;

        Bitmap[] image = new Bitmap[numFrames];
        spriteSheet = res;

        for (int i = 0; i < image.length; i++) {
            image[i] = Bitmap.createBitmap(spriteSheet, 0, i * height, width, height);
        }

        animation.setFrames(image);
    }

    public void update(){
        animation.update();
    }

    public void draw(Canvas canvas){
        int panjang = MainActivity.WIDTH;
        int lebar = MainActivity.HEIGHT;
        x = (panjang - width)/2;
        y = (lebar - (height / 2)) / 4;
        canvas.drawBitmap(animation.getImageMata(), x, y, null);
    }
}
