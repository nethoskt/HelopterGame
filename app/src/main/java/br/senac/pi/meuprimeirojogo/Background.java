package br.senac.pi.meuprimeirojogo;

import android.graphics.Bitmap;
import android.graphics.Canvas;

/**
 * Created by JoséCarlos on 15/12/2015.
 */
public class Background {

    private Bitmap image;
    private int x, y, dx;

    public Background(Bitmap res){
        image = res;
        dx = JogoPainel.MOVESPEED;
    }

    public void update(){
        x+=dx;
        if (x<-JogoPainel.WIDTH){
            x=0;
        }
    }

    public void draw(Canvas canvas){
        canvas.drawBitmap(image, x, y, null);
        if (x<0){
            canvas.drawBitmap(image, x+JogoPainel.WIDTH, y, null);
        }
    }

    public void setVector(int dx){
        this.dx = dx;
    }
}
