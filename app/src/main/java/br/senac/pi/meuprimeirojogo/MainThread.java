package br.senac.pi.meuprimeirojogo;

import android.graphics.Canvas;
import android.view.SurfaceHolder;

/**
 * Created by JoséCarlos on 15/12/2015.
 */
public class MainThread extends Thread {
    private int FPS = 30;
    private double averageFPS;
    private SurfaceHolder surfaceHolder;
    private JogoPainel jogoPainel;
    private boolean running;
    public static Canvas canvas;

    public MainThread(SurfaceHolder surfaceHolder, JogoPainel jogoPainel){
        super();
        this.surfaceHolder = surfaceHolder;
        this.jogoPainel = jogoPainel;
    }

    @Override
    public void run(){
        long startTime;
        long timeMillis;
        long waitTime;
        long totalTime = 0;
        int frameCount = 0;
        long targetTime = 1000/FPS;

        while (running){
            startTime = System.nanoTime();
            canvas = null;

            //try bloquear a tela para a edição de pixels - try locking the canvas for pixel editing
            try {
                canvas = this.surfaceHolder.lockCanvas();
                synchronized (surfaceHolder){
                    this.jogoPainel.update();
                    this.jogoPainel.draw(canvas);
                }
            }catch (Exception e ){}

            finally {
                if (canvas!=null){
                    try{
                        surfaceHolder.unlockCanvasAndPost(canvas);
                    }catch(Exception e){
                        e.printStackTrace();
                    }
                }
            }

            timeMillis = (System.nanoTime() - startTime) /  1000000;
            waitTime = targetTime-timeMillis;

            try{
                this.sleep(waitTime);
            }catch (Exception e){}

            totalTime += System.nanoTime()-startTime;
            frameCount++;
            if (frameCount == FPS){
                averageFPS = 1000/((totalTime/frameCount)/1000000);
                frameCount = 0;
                totalTime = 0;
                System.out.println(averageFPS);
            }
        }
    }

    public  void setRunning(boolean b){
        running = b;
    }
}
