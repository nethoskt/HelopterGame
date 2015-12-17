package br.senac.pi.meuprimeirojogo;

import android.graphics.Bitmap;
import android.graphics.Canvas;

/**
 * Created by JoséCarlos on 15/12/2015.
 */
public class Jogador extends JogoObjeto{
    private Bitmap spritesheet;
    private int score;
    private boolean up;
    private boolean playing;
    private Animacao animacao = new Animacao();
    private long startTime;

    public Jogador(Bitmap res, int w, int h, int numFrames){
        x = 100;
        y = JogoPainel.HEIGHT/2;
        dy = 0;
        score = 0;
        height = h;
        width = w;

        Bitmap[] image = new Bitmap[numFrames];
        spritesheet = res;

        for (int i = 0; i < image.length; i++){
            image[i] = Bitmap.createBitmap(spritesheet, i*width, 0, width, height);
        }
        animacao.setFrames(image);
        animacao.setDelay(10);
        startTime = System.nanoTime();
    }

    public void setUp(boolean b){
        up = b;
    }

    public void update(){
        long elapsed = (System.nanoTime()-startTime)/1000000;
        if (elapsed>100){
            score++;
            startTime = System.nanoTime();
        }
        animacao.update();
        if (up){
            dy -= 1;
        }else{
            dy += 1;
        }
        if (dy>14)dy = 14;
        if (dy<-14)dy = -14;

        y += dy*2;
    }

    public void draw(Canvas canvas){
        canvas.drawBitmap(animacao.getImage(), x, y, null);
    }

    public int getScore(){
        return score;
    }

    public boolean getPlaying(){
        return playing;
    }

    public void setPlaying(boolean b){
        playing = b;
    }

    public void resetDY(){
        dy = 0;
    }

    public void resetScore(){
        score = 0;
    }
}
