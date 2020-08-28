package id.ac.divisirobotikaunikom.nakulasadewa.Display;

import android.graphics.Canvas;
import android.view.SurfaceHolder;

import java.util.Random;

import id.ac.divisirobotikaunikom.nakulasadewa.MainActivity;

public class MainThread extends Thread{
    //SurfaceHolder
    private SurfaceHolder surfaceHolder;

    //Main Class
    private MainActivity wajahPanel;

    //Canvas
    private Canvas canvas;

    //Random
    public static Random r = new Random();

    //boolean
    private boolean running;

    //int
    public static int dKedip = 2 + r.nextInt(4);
    public static long elapsed = 0;

    public MainThread(SurfaceHolder surfaceHolder, MainActivity wajahPanel) {
        //super();
        this.surfaceHolder = surfaceHolder;
        this.wajahPanel = wajahPanel;
    }

    @Override
    public void run() {
        //super.run();
        long startTime;
        while (running) {
            startTime = System.nanoTime();
            //try locking canvas for pixel editing
            try {
                canvas = this.surfaceHolder.lockCanvas();
                synchronized (surfaceHolder) {
                    this.wajahPanel.update();
                    this.wajahPanel.draw(canvas);
                }
            } catch (Exception e) {

            }finally {
                if (canvas != null) {
                    try {
                        surfaceHolder.unlockCanvasAndPost(canvas);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            }

            //Timer kedip
            elapsed += System.nanoTime() - startTime;
            if (elapsed / 1000000 > dKedip * 1000) {
                MainActivity.mata.update();
            }
        }
    }

    public void setRunning(boolean b) {
        running = b;
    }
}
