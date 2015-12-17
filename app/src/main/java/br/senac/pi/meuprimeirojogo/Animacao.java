package br.senac.pi.meuprimeirojogo;

import android.graphics.Bitmap;

/**
 * Created by JoséCarlos on 15/12/2015.
 */
public class Animacao {
    private Bitmap[] frames;
    private int currentFrame;
    private long startTime;
    private long delay;
    private boolean playedOne;

    public void setFrames(Bitmap[] frames){
        this.frames = frames;
        currentFrame = 0;
        startTime = System.nanoTime();
    }

    public void setDelay(long d){
        delay = d;
    }

    public void setFrame(int i){
        currentFrame = i;
    }

    public void update(){
        long elapsed = (System.nanoTime()-startTime)/1000000;
        if (elapsed>delay){
            currentFrame++;
            startTime = System.nanoTime();
        }
        if (currentFrame == frames.length){
            currentFrame = 0;
            playedOne = true;
        }
    }

    public Bitmap getImage(){
        return frames[currentFrame];
    }

    public int getFrame(){
        return currentFrame;
    }

    public boolean playedOne(){
        return playedOne;
    }
}
