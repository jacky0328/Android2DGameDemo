package jacky.idv.a2dgamedemo;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.media.AudioAttributes;
import android.media.AudioManager;
import android.media.SoundPool;
import android.os.Build;
import android.util.Log;
import android.view.MotionEvent;
import android.view.SurfaceHolder;
import android.view.SurfaceView;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * Created by jacky on 2016/10/6.
 */
public class GameSurface extends SurfaceView implements SurfaceHolder.Callback {

    private GameThread gameThread;

   // private ChibiCharacter chibi1;

    private final List<ChibiCharacter> chibiList = new ArrayList<ChibiCharacter>();
    private final List<EnemyCharacter> emenyiList = new ArrayList<EnemyCharacter>();

    private final List<Explosion> explosionList = new ArrayList<Explosion>();

    private static final int MAX_STREAMS=100;
    private int soundIdExplosion;
    private int soundIdBackground;
    private int mWidth;
    private int mHeight;

    private boolean soundPoolLoaded;
    private SoundPool soundPool;

    public GameSurface(Context context)  {
        super(context);

        // Make Game Surface focusable so it can handle events. .
        this.setFocusable(true);

        // Sét callback.
        this.getHolder().addCallback(this);

        this.initSoundPool();



        Log.d("GameSurface","mWidth : " + String.valueOf(mWidth));
        Log.d("GameSurface","mHeight : " + String.valueOf(mHeight));

    }

    private void initSoundPool()  {
        // With Android API >= 21.
        if (Build.VERSION.SDK_INT >= 21 ) {

            AudioAttributes audioAttrib = new AudioAttributes.Builder()
                    .setUsage(AudioAttributes.USAGE_GAME)
                    .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                    .build();

            SoundPool.Builder builder= new SoundPool.Builder();
            builder.setAudioAttributes(audioAttrib).setMaxStreams(MAX_STREAMS);

            this.soundPool = builder.build();
        }
        // With Android API < 21
        else {
            // SoundPool(int maxStreams, int streamType, int srcQuality)
            this.soundPool = new SoundPool(MAX_STREAMS, AudioManager.STREAM_MUSIC, 0);
        }

        // When SoundPool load complete.
        this.soundPool.setOnLoadCompleteListener(new SoundPool.OnLoadCompleteListener() {
            @Override
            public void onLoadComplete(SoundPool soundPool, int sampleId, int status) {
                soundPoolLoaded = true;

                // Playing background sound.
                playSoundBackground();
            }
        });

        // Load the sound background.mp3 into SoundPool
        this.soundIdBackground= this.soundPool.load(this.getContext(), R.raw.background,1);

        // Load the sound explosion.wav into SoundPool
        this.soundIdExplosion = this.soundPool.load(this.getContext(), R.raw.explosion,1);


    }

    public void playSoundExplosion()  {
        if(this.soundPoolLoaded) {
            float leftVolumn = 0.8f;
            float rightVolumn =  0.8f;
            // Play sound explosion.wav
            int streamId = this.soundPool.play(this.soundIdExplosion,leftVolumn, rightVolumn, 1, 0, 1f);
        }
    }

    public void playSoundBackground()  {
        if(this.soundPoolLoaded) {
            float leftVolumn = 0.8f;
            float rightVolumn =  0.8f;
            // Play sound background.mp3
            int streamId = this.soundPool.play(this.soundIdBackground,leftVolumn, rightVolumn, 1, -1, 1f);
        }
    }


    public void update()  {
       // this.chibi1.update();
        for(ChibiCharacter chibi: chibiList) {
            chibi.update();
        }


        //before update, check collision


        int i,j;

        for(i=0;i< emenyiList.size();i++) {
            EnemyCharacter emeny = emenyiList.get(i);
            int  x = emeny.getX();
            int  y =  emeny.getY() ;
            int  thr = emeny.getWidth();

            for(j=0;j< emenyiList.size();j++) {
                if(i!=j) {
                    EnemyCharacter emeny2 = emenyiList.get(j);
                    int  x2 = emeny2.getX();
                    int  y2 =  emeny2.getY() ;
                    double diff = Math.sqrt(Math.pow((float)(x2-x),2) + Math.pow((float)(y2-y),2));
                    if(diff <thr*2)
                    {
                        int movingVectorX = emeny.getMovingVectorX();
                        int movingVectorY = emeny.getMovingVectorY();
                        emeny.setMovingVector(movingVectorX, movingVectorY);
                        emeny2.setMovingVector(-movingVectorX, -movingVectorY);
                        break;
                    }
                }
            }
        }

        for(EnemyCharacter emeny: emenyiList) {
            emeny.update();
        }


        for(Explosion explosion: this.explosionList)  {
            explosion.update();
        }

        Iterator<ChibiCharacter> iterator_men= this.chibiList.iterator();
        Iterator<EnemyCharacter> iterator_emeny= this.emenyiList.iterator();
        while(iterator_men.hasNext()) {
            ChibiCharacter chibi = iterator_men.next();
            int x_min = chibi.getX()- chibi.getWidth()/2;
            int x_max = chibi.getX()+ chibi.getWidth()/2;
            int y_min = chibi.getY()-+ chibi.getHeight()/2;
            int y_max = chibi.getY()+ chibi.getHeight()/2;
            while(iterator_emeny.hasNext()) {
                EnemyCharacter emeny = iterator_emeny.next();
                if(((emeny.getX() >= x_min ) & (emeny.getX() <= x_max)) &
                        (emeny.getY() >= y_min ) &  (emeny.getY() <= y_max)){
                    iterator_emeny.remove();
                    // Create Explosion object.
                    Bitmap bitmap = BitmapFactory.decodeResource(this.getResources(),R.drawable.explosion);
                    Explosion explosion = new Explosion(this, bitmap,chibi.getX(),chibi.getY());

                    this.explosionList.add(explosion);
                }
            }
        }


        Iterator<Explosion> iterator= this.explosionList.iterator();
        while(iterator.hasNext())  {
            Explosion explosion = iterator.next();

            if(explosion.isFinish()) {
                // If explosion finish, Remove the current element from the iterator & list.
                iterator.remove();

                int w = (int)(Math.random()* mWidth);
                int h = (int)(Math.random()* mHeight);
                Bitmap enemyBitmap = BitmapFactory.decodeResource(this.getResources(),R.drawable.chibi1);
                EnemyCharacter emeny = new EnemyCharacter(this,enemyBitmap,w,h);
                this.emenyiList.add(emeny);
                 w = (int)(Math.random()* mWidth);
                 h = (int)(Math.random()* mHeight);
                emeny = new EnemyCharacter(this,enemyBitmap,w,h);
                this.emenyiList.add(emeny);
                continue;
            }
        }

    }



    @Override
    public void draw(Canvas canvas)  {
        super.draw(canvas);

        mHeight = canvas.getHeight();
        mWidth   = canvas.getWidth();

        Log.d("GameSurface","mWidth : " + String.valueOf(mWidth));
        Log.d("GameSurface","mHeight : " + String.valueOf(mHeight));


        Bitmap bg_img = BitmapFactory.decodeResource(this.getResources(),R.drawable.background);

        Log.d("GameSurface","bmp Width : " + String.valueOf(bg_img.getWidth()));
        Log.d("GameSurface","bmp Height : " + String.valueOf(bg_img.getHeight()));


        Matrix matrix = new Matrix();
        //matrix.setScale(1, 1.314f);
        matrix.setScale(1, (float)mHeight/bg_img.getHeight());
        canvas.drawBitmap(bg_img,matrix,null);


        for(ChibiCharacter chibi: chibiList)  {
            chibi.draw(canvas);
        }

        for(EnemyCharacter emeny: emenyiList)  {
            emeny.draw(canvas);
        }


        for(Explosion explosion: this.explosionList)  {
            explosion.draw(canvas);
        }

    }

    // Implements method of SurfaceHolder.Callback
    @Override
    public void surfaceCreated(SurfaceHolder holder) {
        //Bitmap chibiBitmap1 = BitmapFactory.decodeResource(this.getResources(),R.drawable.chibi1);
        //Bitmap chibiBitmap1 = BitmapFactory.decodeResource(this.getResources(),R.drawable.people00);
        //ChibiCharacter chibi1 = new ChibiCharacter(this,chibiBitmap1,100,50);
        Bitmap chibiBitmap2 = BitmapFactory.decodeResource(this.getResources(),R.drawable.chibi2);
        ChibiCharacter chibi2 = new ChibiCharacter(this,chibiBitmap2,300,150);

        //this.chibiList.add(chibi1);
        this.chibiList.add(chibi2);



        Bitmap enemyBitmap = BitmapFactory.decodeResource(this.getResources(),R.drawable.chibi1);
        EnemyCharacter emeny = new EnemyCharacter(this,enemyBitmap,100,50);
        this.emenyiList.add(emeny);


        this.gameThread = new GameThread(this,holder);
        this.gameThread.setRunning(true);
        this.gameThread.start();
    }

    // Implements method of SurfaceHolder.Callback
    @Override
    public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {

    }

    // Implements method of SurfaceHolder.Callback
    @Override
    public void surfaceDestroyed(SurfaceHolder holder) {
        boolean retry= true;
        while(retry) {
            try {
                this.gameThread.setRunning(false);

                // Parent thread must wait until the end of GameThread.
                this.gameThread.join();
            }catch(InterruptedException e)  {
                e.printStackTrace();
            }
            retry= true;
        }
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        if (event.getAction() == MotionEvent.ACTION_DOWN) {
            int x=  (int)event.getX();
            int y = (int)event.getY();

           // Iterator<ChibiCharacter> iterator= this.chibiList.iterator();

           // Iterator<EnemyCharacter> iterator_emeny= this.emenyiList.iterator();

            //  check i touch to chibi
            /*
            while(iterator.hasNext()) {
                ChibiCharacter chibi = iterator.next();
                if( chibi.getX() < x && x < chibi.getX() + chibi.getWidth()
                        && chibi.getY() < y && y < chibi.getY()+ chibi.getHeight())  {
                    // Remove the current element from the iterator and the list.
                    iterator.remove();

                    // Create Explosion object.
                    Bitmap bitmap = BitmapFactory.decodeResource(this.getResources(),R.drawable.explosion);
                    Explosion explosion = new Explosion(this, bitmap,chibi.getX(),chibi.getY());

                    this.explosionList.add(explosion);
                }
            }
             */

            /*
            while(iterator.hasNext()) {
                ChibiCharacter chibi = iterator.next();
                int x_min = chibi.getX();
                int x_max = chibi.getX()+ chibi.getWidth();
                int y_min = chibi.getY();
                int y_max = chibi.getY()+ chibi.getHeight();
                while(iterator_emeny.hasNext()) {
                    EnemyCharacter emeny = iterator_emeny.next();
                    if(((emeny.getX() >= x_min ) & (emeny.getX() <= x_max)) &
                      (emeny.getY() >= y_min ) &  (emeny.getY() <= y_max)){
                        iterator_emeny.remove();
                        // Create Explosion object.
                        Bitmap bitmap = BitmapFactory.decodeResource(this.getResources(),R.drawable.explosion);
                        Explosion explosion = new Explosion(this, bitmap,chibi.getX(),chibi.getY());

                        this.explosionList.add(explosion);
                    }
                }
            }
           */

            //  moving
            for(ChibiCharacter chibi: chibiList) {
                int movingVectorX =x-  chibi.getX() ;
                int movingVectorY =y-  chibi.getY() ;
                chibi.setMovingVector(movingVectorX, movingVectorY);
            }
            return true;
        }
        return false;
    }

}
