package br.senac.pi.meuprimeirojogo;

import android.graphics.Bitmap;
import android.graphics.Canvas;

/**
 * Created by JoséCarlos on 17/12/2015.
 */
public class Explosion {
    private int x;
    private int y;
    private int width;
    private int height;
    private int row;
    private Animacao animacao = new Animacao();
    private Bitmap spritesheet;

    public Explosion(Bitmap res, int x, int y, int w, int h, int numFrames) {
        this.x = x;
        this.y = y;
        this.width = w;
        this.height = h;

        Bitmap[] image = new Bitmap[numFrames];

        spritesheet = res;

        for(int i = 0; i<image.length; i++) {
            if(i%5==0&&i>0)row++;
            image[i] = Bitmap.createBitmap(spritesheet, (i-(5*row))*width, row*height, width, height);
        }
        animacao.setFrames(image);
        animacao.setDelay(10);
    }

    public void draw(Canvas canvas) {
        if(!animacao.playedOne()) {
            canvas.drawBitmap(animacao.getImage(),x,y,null);
        }
    }
    public void update(){
        if(!animacao.playedOne()) {
            animacao.update();
        }
    }
    public int getHeight(){return height;}
}
