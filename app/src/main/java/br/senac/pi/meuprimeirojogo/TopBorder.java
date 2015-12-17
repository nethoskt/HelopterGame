package br.senac.pi.meuprimeirojogo;

import android.graphics.Bitmap;
import android.graphics.Canvas;

/**
 * Created by JoséCarlos on 16/12/2015.
 */
public class TopBorder extends JogoObjeto {
    private Bitmap image;

    public TopBorder(Bitmap res, int x, int y, int h){
        height = h;
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
        try{canvas.drawBitmap(image, x, y, null);}catch (Exception e ){}
    }
}
