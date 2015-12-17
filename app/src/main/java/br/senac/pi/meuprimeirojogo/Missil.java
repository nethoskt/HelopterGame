package br.senac.pi.meuprimeirojogo;

import android.graphics.Bitmap;
import android.graphics.Canvas;

import java.util.Random;

/**
 * Created by JoséCarlos on 15/12/2015.
 */
public class Missil extends JogoObjeto {
    private int score;
    private int speed;
    private Random rand = new Random();
    private Animacao animacao = new Animacao();
    private Bitmap spritesheet;

    public Missil(Bitmap res, int x, int y, int w, int h, int s, int numFrames){
        super.x = x;
        super.y = y;
        width = w;
        height = h;
        score = s;

        speed = 7 + (int) (rand.nextDouble()*score/30);

        //cap velocidade do missil
        if (speed>40)speed = 40;

        Bitmap[] image = new Bitmap[numFrames];

        spritesheet = res;

        for (int i=0; i<image.length; i++){
            image[i] = Bitmap.createBitmap(spritesheet, 0, i*height, width, height);
        }
        animacao.setFrames(image);
        animacao.setDelay(100-speed);
    }

    public void update(){
        x -= speed;
        animacao.update();
    }

    public void draw(Canvas canvas){
        try{
            canvas.drawBitmap(animacao.getImage(), x, y, null);
        }catch (Exception e){}
    }

    @Override
    public int getWidth(){
        //deslocado ligeiramente para a detecção de colisão mais realista - offset slightly for more realistic collision detection
        return width-10;
    }
}
