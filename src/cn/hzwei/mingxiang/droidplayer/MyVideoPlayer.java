/**
 * 
 */
package cn.hzwei.mingxiang.droidplayer;
 
import cn.hzwei.mingxiang.droidplayer.video.VideoCached;
import cn.hzwei.mingxiang.droidplayer.video.VideoInfo;
import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.ActivityInfo;
import android.graphics.PixelFormat;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Toast;

public class MyVideoPlayer extends Activity implements SurfaceHolder.Callback {
    private String videoIdxTag = "videoIdx";
    private int videoIdx;

    private Context mContext = null; 
    private SurfaceHolder mSurfaceHolder; 
    private MediaPlayer mMediaPlayer; 
    private SurfaceView mSurfaceView; 
    private Intent mFloatingWindow = null; 
    private Handler mHandler = null; 
    private MyBroadcastReceiver mControllerReceiver; 
    private MyThread updateProgressBarThread =  null;

    public void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);
        this.requestWindowFeature(Window.FEATURE_NO_TITLE); 
        this.getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        this.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE); // 设置为竖屏

        setContentView(R.layout.player);
        getWindow().setFormat(PixelFormat.UNKNOWN);
        
        Intent intent = getIntent();
        videoIdx = intent.getIntExtra(videoIdxTag, 0);
        if (savedInstanceState != null && savedInstanceState.containsKey(videoIdxTag)){
            videoIdx = savedInstanceState.getInt(videoIdxTag);
        }
        
        createPlayerView();
        createFloatingWindow();
        
        mContext = this;
        mHandler = new MyHandler(this);
    }

    private void createPlayerView(){ 
        mSurfaceView = (SurfaceView)findViewById(R.id.surfaceView); 
        mSurfaceHolder = mSurfaceView.getHolder(); 
        mSurfaceHolder.addCallback(this); 
        mSurfaceHolder.setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);

        mMediaPlayer = new MediaPlayer();
        mMediaPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC); 
        mMediaPlayer.setOnPreparedListener(new MediaPlayer.OnPreparedListener(){ 
            @Override
            public void onPrepared(MediaPlayer mp) { 
                startPlaying(); 
            }
        });
        
        mMediaPlayer.setOnCompletionListener(new MediaPlayer.OnCompletionListener(){ 
            @Override
            public void onCompletion(MediaPlayer mp){ 
                //自动播放下一个
                mMediaPlayer.reset();
                playNext();
            }
        });
    }
    
    private void createFloatingWindow(){
        mFloatingWindow = new Intent();
        mFloatingWindow.setClass(MyVideoPlayer.this, FloatingService.class);
        mControllerReceiver = new MyBroadcastReceiver();
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(FloatingService.TAG);
        this.registerReceiver(mControllerReceiver, intentFilter);
    }
    
    @Override  
    protected void onSaveInstanceState(Bundle outState) {  
        super.onSaveInstanceState(outState);  
        outState.putInt(videoIdxTag, videoIdx);
        if (mMediaPlayer != null){
            outState.putInt("curPos", mMediaPlayer.getCurrentPosition());
        }
    }
     
    @Override
    protected void onPause(){
        Log.d("MyVideoPlayer", "onPause");
        super.onPause();
    }

    @Override
    protected void onRestart(){
        Log.d("MyVideoPlayer", "onRestart");
        super.onRestart();
    }

    @Override
    protected void onResume(){
        Log.d("MyVideoPlayer", "onResume");
        super.onResume();
    }

    @Override
    protected void onStart(){
        Log.d("MyVideoPlayer", "onStart");
        super.onStart();
    }

    @Override
    protected void onStop(){
        Log.d("MyVideoPlayer", "onStop");
        super.onStop();
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (FloatingService.bLock){
            return false;
        }
        
        updateProgressBarThread.working = false;
        if(keyCode == KeyEvent.KEYCODE_BACK) { 
            this.finish();
            return true;
        } else if(keyCode == KeyEvent.KEYCODE_MENU) {
            //监控/拦截菜单键
        } else if(keyCode == KeyEvent.KEYCODE_HOME) {
            //由于Home键为系统键，此处不能捕获，需要重写onAttachedToWindow()
            
        }
        return super.onKeyDown(keyCode, event);
    }
    
    public void onAttachedToWindow() {
        int sysVersion = Integer.parseInt(android.os.Build.VERSION.SDK);
        if (sysVersion <= 8){  // 只对2.2以下的版本有效
            this.getWindow().setType(WindowManager.LayoutParams.TYPE_KEYGUARD); 
        }
            
        super.onAttachedToWindow();    
    }

    @Override
    public boolean onTouchEvent(MotionEvent event){
        if (!FloatingService.bLock){
            noticeShowControllerView(mMediaPlayer.getCurrentPosition(), 
                                     mMediaPlayer.getDuration(), true);
        }
        
        return super.onTouchEvent(event); 
    }

    @Override
    protected void onDestroy(){
        updateProgressBarThread.working = false;
        stopService(mFloatingWindow);
        this.unregisterReceiver(mControllerReceiver);
        if(this.mMediaPlayer != null){
            this.mMediaPlayer.release();
            mMediaPlayer = null;
        }
        mHandler.removeMessages(MyHandler.UPD_VIDEO_PROCESSBAR);
        mFloatingWindow = null;
        super.onDestroy();
    }
    
    public void surfaceChanged(SurfaceHolder arg0, int arg1, int arg2, int arg3){
        Log.d("MyVideoPlayer", "surfaceChanged");
    }

    public void surfaceCreated(SurfaceHolder holder){
        mMediaPlayer.setDisplay(holder);
        preparePlay();
    }

    public void surfaceDestroyed(SurfaceHolder arg0){
        Log.d("MyVideoPlayer", "surfaceDestroyed");
    }
 
    private void playNext(){
        if (videoIdx + 1 == VideoCached.getVideoCount()){
            Toast.makeText(mContext, "已经是最后一个视频文件了", Toast.LENGTH_LONG).show();
            mMediaPlayer.stop();
        }else{
            videoIdx = videoIdx + 1;
            preparePlay();
        }
    }
    
    private void playPrev(){
        if (videoIdx == 0){
            Toast.makeText(mContext, "已经是第一个视频文件了", Toast.LENGTH_LONG).show();
            mMediaPlayer.stop();
        }else{
            videoIdx = videoIdx - 1;
            preparePlay();
        }
    }

    private void preparePlay(){
        VideoInfo video = VideoCached.getVideo(videoIdx);
        if (video == null){
            Toast.makeText(this, "没有视频文件", Toast.LENGTH_LONG).show();
            return;
        }
        
        if(mMediaPlayer.isPlaying() == true){ 
            mMediaPlayer.reset(); 
        } 
        
        try{
            
            mMediaPlayer.setDataSource(this, Uri.parse(video.path)); // 设置MediaPlayer的数据源 
            mMediaPlayer.prepare(); 
        }catch(Exception e){ 
            Log.e("surfaceCreated", "surfaceCreated exception", e); 
        } 
    }
    
    private void startPlaying(){
        mMediaPlayer.start();  //播放视频
        
        if (updateProgressBarThread != null){
            updateProgressBarThread.working = false;
            updateProgressBarThread = null;
        }
        updateProgressBarThread = new MyThread();
        updateProgressBarThread.start();
    }

    private void noticeShowControllerView(int position, int duration, boolean touch){ 
        mFloatingWindow.putExtra("position", position); 
        mFloatingWindow.putExtra("max", duration);
        if (touch){ 
            mFloatingWindow.putExtra("visable", true);
        }

        mFloatingWindow.putExtra("touch", touch);
        startService(mFloatingWindow);
    }
     
    public class MyBroadcastReceiver extends BroadcastReceiver { // 自定义广播接受者
        @Override
        public void onReceive(Context arg0, Intent intent){
            String action = intent.getAction();
            if(action.equals(FloatingService.TAG)){
                String flag = intent.getStringExtra("flag");
                if(flag.equals("play")){ 
                    if(!mMediaPlayer.isPlaying()){ 
                        startPlaying();
                    }
                }else if(flag.equals("pause")){
                    if(mMediaPlayer.isPlaying()){ 
                        mMediaPlayer.pause(); 
                    }
                }else if(flag.equals("change")){
                    int pos = intent.getIntExtra("newpos", 0);
                    if(mMediaPlayer.isPlaying()){ 
                        mMediaPlayer.pause(); // 暂停
                    }
                    
                    mMediaPlayer.seekTo(pos);
                    startPlaying();
                }else if(flag.equals("forward")){
                    int pos = intent.getIntExtra("newpos", 0);
                    if(mMediaPlayer.isPlaying()){ 
                        mMediaPlayer.pause(); 
                    }
                    mMediaPlayer.seekTo(pos);
                    startPlaying();
                }else if(flag.equals("backward")){
                    int pos = intent.getIntExtra("newpos", 0);
                    if(mMediaPlayer.isPlaying()){ 
                        mMediaPlayer.pause(); // 暂停
                    }
                    mMediaPlayer.seekTo(pos);
                    startPlaying();
                }else if(flag.equals("prev")){ 
                    playPrev();
                }else if(flag.equals("next")){ 
                    playNext();
                }else{
                    MyVideoPlayer.this.finish();
                }
            }
        }
    }  //广播接收方法结束
    
    
   private static class MyHandler extends Handler{
       static final int UPD_VIDEO_PROCESSBAR = 1;
       
       MyVideoPlayer mContext;
       MyHandler(MyVideoPlayer mContext){
           this.mContext = mContext;
       }
       
       @Override
       public void handleMessage(Message msg){
           switch(msg.what){
               case UPD_VIDEO_PROCESSBAR:
                   updVideoProcessbar();
                   break;
           }
           
       }
       
       private void updVideoProcessbar(){
           if (mContext != null && mContext.mMediaPlayer != null && FloatingService.bControllerViewShow){
               if (mContext.mMediaPlayer.isPlaying()){ 
                   mContext.noticeShowControllerView(mContext.mMediaPlayer.getCurrentPosition(), 
                           mContext.mMediaPlayer.getDuration(), false);
               }
           }
       }
   }
   
   private final class MyThread extends Thread{
       boolean working = true;
       
       @Override
       public void run(){
           while (working && mMediaPlayer != null && mMediaPlayer.isPlaying()){
               if (mHandler != null) mHandler.sendEmptyMessage(MyHandler.UPD_VIDEO_PROCESSBAR);
               
               try{ 
                   Thread.sleep(1000);
               }catch(InterruptedException e){  
                   e.printStackTrace();
               }
           }
       }
   }
   
}