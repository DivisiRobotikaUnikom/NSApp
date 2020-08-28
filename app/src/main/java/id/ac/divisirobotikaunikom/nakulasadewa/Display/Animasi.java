package id.ac.divisirobotikaunikom.nakulasadewa.Display;

import android.graphics.Bitmap;

public class Animasi {
    private Bitmap[] frames;
    private boolean reverse = false;
    private int currentFrame = 0;

    public void setFrames(Bitmap[] frames) {
        this.frames = frames;
        currentFrame = 0;
    }

    public Bitmap getImageMata() {
        return frames[currentFrame];
    }

    public void update() {
        if(!reverse) {
            currentFrame++;
            if (currentFrame >= frames.length) {
                reverse = true;
            }
        }
        if(reverse){
            currentFrame--;
            if(currentFrame <= 0){
                reverse = false;
                MainThread.dKedip = 2 + MainThread.r.nextInt(4);
                MainThread.elapsed = 0;
            }
        }
    }

}
