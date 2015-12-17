package br.senac.pi.meuprimeirojogo;

import android.graphics.Bitmap;
import android.graphics.Canvas;

/**
 * Created by JoséCarlos on 16/12/2015.
 */
public class BotBorder extends JogoObjeto {
    private Bitmap image;

    public BotBorder(Bitmap res, int x, int y){
        height = 200;
        width = 20;
        this.x = x;
        this.y = y;
        dx = JogoPainel.MOVESPEED;
        image = Bitmap.createBitmap(res, 0, 0, width, height);
    }

    public void update(){
        x += dx;
    }

    public void draw(Canvas canvas){
        canvas.drawBitmap(image, x, y, null);
    }
}
