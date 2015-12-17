package br.senac.pi.meuprimeirojogo;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.Typeface;
import android.media.AudioManager;
import android.media.SoundPool;
import android.view.MotionEvent;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import java.util.ArrayList;
import java.util.Random;

/**
 * Created by JoséCarlos on 15/12/2015.
 */
public class JogoPainel extends SurfaceView implements SurfaceHolder.Callback {

    public static final int WIDTH = 856;
    public static final int HEIGHT = 480;
    public static final int MOVESPEED = -5;
    private long smokeStartTime;
    private long missilStartTime;
    private MainThread thread;
    private Background bg;
    private Jogador jogador;
    private ArrayList<Smokepuff> smoke;
    private ArrayList<Missil> misseis;
    private ArrayList<TopBorder> topborder;
    private ArrayList<BotBorder> botborder;
    private Random rand = new Random();
    private int maxBorderHeight;
    private int minBorderHeight;
    private boolean topDown = true;
    private boolean botDown = true;
    private boolean newGameCreated;

    //aumentar ou diminuir a aceleração da dificuldade de progressão - increase to slow down difficulty progression, decrease to speed up difficulty progression
    private int progressDenom = 20;

    private Explosion explosion;
    private long startReset;
    private boolean reset;
    private boolean dissapear;
    private boolean started;
    private int best = 0;
    private SoundPool sounds;
    private int sExplosion;

    public JogoPainel (Context context){
        super(context);

        //adcionar o retorno de chamada para a superfície de suporte para interceptar eventos - add the callback to the surfaceholder to intercept events
        getHolder().addCallback(this);

        thread = new MainThread(getHolder(), this);

        //fazer jogoPainel focusable para que ele possa manipular eventos - make jogoPainel focusable so it can handle events
        setFocusable(true);
    }

    @Override
    public void surfaceChanged(SurfaceHolder holder, int format, int width, int height){}

    @Override
    public void surfaceDestroyed(SurfaceHolder holder){
        boolean retry = true;
        int counter = 0;
        while (retry && counter<1000){
            counter++;
            try {
                thread.setRunning(false);
                thread.join();
                retry = false;
                thread = null;
            }catch(InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

    @Override
    public void surfaceCreated(SurfaceHolder holder){
        bg = new Background(BitmapFactory.decodeResource(getResources(), R.drawable.grassbg1));
        jogador = new Jogador(BitmapFactory.decodeResource(getResources(), R.drawable.helicopter), 65, 25, 3);
        smoke = new ArrayList<Smokepuff>();
        misseis = new ArrayList<Missil>();
        topborder = new ArrayList<TopBorder>();
        botborder = new ArrayList<BotBorder>();
        smokeStartTime = System.nanoTime();
        missilStartTime = System.nanoTime();

        thread = new MainThread(getHolder(), this);
        //podemos começar seguramente o loop do jogo - we can safely start the game loop
        thread.setRunning(true);
        thread.start();
    }

    @Override
    public boolean onTouchEvent(MotionEvent event){
        if (event.getAction()==MotionEvent.ACTION_DOWN){
            if (!jogador.getPlaying() && newGameCreated && reset){
                jogador.setPlaying(true);
                jogador.setUp(true);
            }
            if (jogador.getPlaying()){
                if (!started)started = true;
                reset = false;
                jogador.setUp(true);
            }
            return true;
        }
        if (event.getAction()==MotionEvent.ACTION_UP){
            jogador.setUp(false);
            return true;
        }
        return super.onTouchEvent(event);
    }

    public void update(){
        if (jogador.getPlaying()) {
            if (botborder.isEmpty()){
                jogador.setPlaying(false);
                return;
            }
            if (topborder.isEmpty()){
                jogador.setPlaying(false);
                return;
            }

            bg.update();
            jogador.update();

            //o cálculo do piso da altura da fronteira pode ter com base na pontuação - calculate the threshold of height the border can have based on the score
            //maximo e o minimo da borda são atualizados, a borda troca de direção quando quer - max and min border heart are update, and the border
            //switched direction when either mas or min is net

            maxBorderHeight = 30+jogador.getScore()/progressDenom;
            //a altura da máxima borda, para que elas só possa se expandir até 1/2 da tela - cap max border height so that borders can only take up a total of 1/2 the screen
            if(maxBorderHeight > HEIGHT/4)maxBorderHeight = HEIGHT/4;
            minBorderHeight = 5+jogador.getScore()/progressDenom;

            //checar colisão com o inferior da borda - check bottom border collision
            for(int i = 0; i < botborder.size(); i++){
                if (colisao(botborder.get(i), jogador)) jogador.setPlaying(false);
            }

            //checar colisão com o topo da borda - check top border collision
            for (int i = 0; i < topborder.size(); i++){
                if (colisao(topborder.get(i), jogador)) jogador.setPlaying(false);
            }

            //atualizar o topo da borda - update top border
            this.updateTopBorder();

            //atualizar o inferior da borda - update bottom border
            this.updateBottomBorder();

            //adicionar mísseis no tempo - add missiles on time
            long missileElapsed = (System.nanoTime()-missilStartTime)/1000000;
            if (missileElapsed >(2000 - jogador.getScore()/4)){

                //primeiro míssil sempre vim do meio para baixo - first missile always goes down the midlle
                if(misseis.size()==0){
                    misseis.add(new Missil(BitmapFactory.decodeResource(getResources(),R.drawable.missile),WIDTH + 10, HEIGHT/2, 45, 15, jogador.getScore(), 13));
                }else{
                    misseis.add(new Missil(BitmapFactory.decodeResource(getResources(),R.drawable.missile), WIDTH+10, (int)(rand.nextDouble()*(HEIGHT - (maxBorderHeight * 2))+maxBorderHeight),45,15, jogador.getScore(),13));
                }

                //reiniciar o tempo - reset timer
                missilStartTime = System.nanoTime();
            }

            //laço para gerar todos mísseis e verificar colisão e remover - loop through every missile and check collision and remove
            for(int i = 0; i<misseis.size();i++){

                //update missile
                misseis.get(i).update();
                if(colisao(misseis.get(i),jogador)) {
                    misseis.remove(i);
                    jogador.setPlaying(false);
                    break;
                }

                //remover mísseis se for fora da tela - remove missile if it is way off the screen
                if(misseis.get(i).getX()<-100)
                {
                    misseis.remove(i);
                    break;
                }
            }

            //adicionar fumaça no tempo - add smoke puffs on time
            long elapsed = (System.nanoTime() - smokeStartTime)/1000000;
            if (elapsed > 120){
                smoke.add(new Smokepuff(jogador.getX(), jogador.getY()+10));
                smokeStartTime = System.nanoTime();
            }

            for (int i = 0; i<smoke.size(); i++){
                smoke.get(i).update();
                if(smoke.get(i).getX()<-10){
                    smoke.remove(i);
                }
            }
        }else {
            jogador.resetDY();
            if (!reset){
                newGameCreated = false;
                startReset = System.nanoTime();
                reset = true;
                dissapear = true;
                explosion = new Explosion(BitmapFactory.decodeResource(getResources(),R.drawable.explosion),jogador.getX(),jogador.getY()-30, 100, 100, 25);

                sounds = new SoundPool(10, AudioManager.STREAM_MUSIC,0);
                sExplosion = sounds.load(getContext(), R.raw.over, 1);
                sounds.play(sExplosion, 1.0f, 1.0f, 0, 0, 1.5f);
            }
            explosion.update();
            long resetElapsed = (System.nanoTime()-startReset)/1000000;
            if (resetElapsed > 2500 && !newGameCreated){
                newGame();
            }
        }
    }

    public boolean colisao(JogoObjeto a, JogoObjeto b){
        if(Rect.intersects(a.getRectangle(), b.getRectangle())){
            return true;

        }
        return false;
    }

    @Override
    public void draw(Canvas canvas){
        final float scaleFactorX = getWidth()/(WIDTH*1.f);
        final float scaleFactorY = getHeight()/(HEIGHT*1.f);
        if (canvas != null){
            final int savedState = canvas.save();
            canvas.scale(scaleFactorX, scaleFactorY);
            bg.draw(canvas);
            if (!dissapear) {
                jogador.draw(canvas);
            }

            // adiciona fumaça - add smokepuffs
            for(Smokepuff sp: smoke){
                sp.draw(canvas);
            }

            //desenhar mísseis - draw missiles
            for (Missil m: misseis){
                m.draw(canvas);
            }

            //desenhar a borda superior - draw topborder
            for (TopBorder tb: topborder){
                tb.draw(canvas);
            }

            //desenhar a borda inferior - draw botborder
            for (BotBorder bb: botborder){
                bb.draw(canvas);
            }

            //desenhar explosão- draw explosion
            if (started){
                explosion.draw(canvas);
            }
            drawText(canvas);
            canvas.restoreToCount(savedState);
        }
    }

    public void updateTopBorder(){
        //sempre 50 pontos, inseridos a cada bloco no top da parede - ever 50 points, insert randomly placed top blocks that break the pattern
        if (jogador.getScore()%50 == 0){
            topborder.add(new TopBorder(BitmapFactory.decodeResource(getResources(), R.drawable.brick),topborder.get(topborder.size()-1).getX()+20,0,(int)((rand.nextDouble()*(maxBorderHeight))+1)));
        }
        for (int i = 0; i < topborder.size(); i++){
            topborder.get(i).update();
            if (topborder.get(i).getX()<-20){
                topborder.remove(i);
                //remove o elemento do arraylist, substituindo-o e adicionando um novo - remove element of arraylist, replace it by adding a new one

                //calculate topdown which determines the direction the border is moving (up or down)
                if (topborder.get(topborder.size()-1).getHeight()>=maxBorderHeight){
                    topDown = false;
                }
                if (topborder.get(topborder.size()-1).getHeight()<=minBorderHeight){
                    topDown = true;
                }

                //nova borda adicionada terá grande largura - new border added will have large height
                if (topDown){
                    topborder.add(new TopBorder(BitmapFactory.decodeResource(getResources(), R.drawable.brick), topborder.get(topborder.size()-1).getX()+20,0,topborder.get(topborder.size()-1).getHeight()+1));
                }else {
                    //nova borda adicionada terá pequena largura - new border added wil have smaller height
                    topborder.add(new TopBorder(BitmapFactory.decodeResource(getResources(), R.drawable.brick), topborder.get(topborder.size()-1).getX()+20,0,topborder.get(topborder.size()-1).getHeight()-1));
                }
            }
        }
    }

    public void updateBottomBorder(){
        //sempre 40 pontos, inseridos a cada bloco no inferior da parede - ever 40 points, insert randomly placed top blocks that break the pattern
        if (jogador.getScore()%40 == 0){
            botborder.add(new BotBorder(BitmapFactory.decodeResource(getResources(), R.drawable.brick),botborder.get(botborder.size()-1).getX()+20,(int)((rand.nextDouble() * maxBorderHeight)+(HEIGHT-maxBorderHeight))));
        }

        //atualiza o inferior da borda - update bottom border
        for (int i = 0; i < botborder.size(); i++){
            botborder.get(i).update();

            //remove o elemento do arraylist, substituindo-o e adicionando um novo - remove element of arraylist, replace it by adding a new one
            if (botborder.get(i).getX()<-20){
                botborder.remove(i);

                //determina se a borda irá se mover para cima ou para baixo - determine if border will be moving up or down
                if (botborder.get(botborder.size()-1).getY() <= HEIGHT-maxBorderHeight){
                    botDown = true;
                }
                if (botborder.get(botborder.size()-1).getY() >= HEIGHT-minBorderHeight){
                    botDown = false;
                }

                //nova borda adicionada terá grande largura - new border added will have large height
                if (botDown){
                    botborder.add(new BotBorder(BitmapFactory.decodeResource(getResources(), R.drawable.brick), botborder.get(botborder.size()-1).getX()+20,botborder.get(botborder.size()-1).getY()+1));
                }else {
                    //nova borda adicionada terá pequena largura - new border added wil have smaller height
                    botborder.add(new BotBorder(BitmapFactory.decodeResource(getResources(), R.drawable.brick), botborder.get(botborder.size()-1).getX()+20,botborder.get(botborder.size()-1).getY()-1));
                }
            }
        }
    }

    public void newGame() {
        dissapear = false;
        botborder.clear();
        topborder.clear();
        misseis.clear();
        smoke.clear();

        minBorderHeight = 5;
        maxBorderHeight = 30;

        jogador.resetDY();
        jogador.setY(HEIGHT/2);

        if (jogador.getScore()>best){
            best = (jogador.getScore());
        }

        jogador.resetScore();

        //criação inicial das bordas - create initial borders
        //inicial no topo da borda - initial top border
        for(int i = 0; i*20<WIDTH+40;i++) {

            // criação do primeiro topo da borda - first top border create
            if (i==0) {
                topborder.add(new TopBorder(BitmapFactory.decodeResource(getResources(),R.drawable.brick),i*20,0, 10));
            } else {
                topborder.add(new TopBorder(BitmapFactory.decodeResource(getResources(),R.drawable.brick),i*20,0, topborder.get(i-1).getHeight()+1));
            }
        }

        //iniciação da borda inferior - initial bottom border
        for(int i = 0; i*20<WIDTH+40; i++) {

            //primeira borda sempre criada - first border ever created
            if(i==0) {
                botborder.add(new BotBorder(BitmapFactory.decodeResource(getResources(),R.drawable.brick),i*20,HEIGHT - minBorderHeight));
            } else {

                //adicionando bordas até o inicio da tela - adding borders until the initial screen is filed
                botborder.add(new BotBorder(BitmapFactory.decodeResource(getResources(), R.drawable.brick),i*20,botborder.get(i - 1).getY() - 1));
            }
        }
        newGameCreated = true;
    }

    public void drawText(Canvas canvas) {
        Paint paint = new Paint();
        paint.setColor(Color.WHITE);
        paint.setTextSize(30);
        paint.setTypeface(Typeface.create(Typeface.DEFAULT, Typeface.BOLD));
        canvas.drawText("DISTÂNCIA: " + (jogador.getScore()), 10, HEIGHT - 10, paint);
        canvas.drawText("MELHOR: " + best, WIDTH - 215, HEIGHT - 10, paint);

        if(!jogador.getPlaying()&&newGameCreated&&reset) {
            Paint paint1 = new Paint();
            paint1.setTextSize(40);
            paint1.setTypeface(Typeface.create(Typeface.DEFAULT, Typeface.BOLD));
            canvas.drawText("PRECIONE PARA INICIAR", WIDTH/2-50, HEIGHT/2, paint1);

            paint1.setTextSize(20);
            canvas.drawText("PRECIONE E SEGURE PARA IR PARA CIMA", WIDTH/2-50, HEIGHT/2 + 20, paint1);
            canvas.drawText("LIBERE DE IR PARA BAIXO", WIDTH/2-50, HEIGHT/2 + 40, paint1);
        }
    }
}