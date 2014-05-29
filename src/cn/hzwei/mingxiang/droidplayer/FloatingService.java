package cn.hzwei.mingxiang.droidplayer;

import java.text.DecimalFormat;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnTouchListener;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.ZoomButton;

public class FloatingService extends Service {
    static boolean bLock = false;
    static final String TAG = "android.intent.action.myvideoplayer"; // 自定义的广播标签 
    static boolean bControllerViewShow = false;

    private final static int TIME = 3000; // 3000ms = 3s，快进和快退的步进都是3秒
    private boolean bPlaying = true;
    private int curPos, duration; 

    private WindowManager mWindowManager = null;
    private View mControllerView;
    private View mLockView;

    private ImageButton mPlayPauseBtn = null, mPrevBtn = null, mNextBtn = null;
    private ImageButton mStopBtn=null, mLockBtn=null;
    private ZoomButton mForwardBtn = null, mBackwardBtn = null;
    private TextView mPosTextView = null, mDurationTextView = null; 
    private SeekBar mProgressBar;
    
    @Override
    public void onCreate(){ 
        Log.d("FloatingService", "onCreate FloatingService"); 
        super.onCreate(); 
         
        createView();
        bindEvent();
        addToWin();
    }
    
    private void createView(){
        mControllerView = LayoutInflater.from(this).inflate(R.layout.controller, null);
        mLockView = LayoutInflater.from(this).inflate(R.layout.screen_lockbar, null); 
        mPlayPauseBtn = (ImageButton)mControllerView.findViewById(R.id.controller_playpause);
        mPrevBtn = (ImageButton)mControllerView.findViewById(R.id.controller_prev);
        mNextBtn = (ImageButton)mControllerView.findViewById(R.id.controller_next);
        mLockBtn = (ImageButton)mControllerView.findViewById(R.id.controller_lock);
        mStopBtn = (ImageButton)mControllerView.findViewById(R.id.controller_stop);
        mForwardBtn = (ZoomButton)mControllerView.findViewById(R.id.controller_forward); 
        mBackwardBtn = (ZoomButton)mControllerView.findViewById(R.id.controller_backward);
        mPosTextView = (TextView)mControllerView.findViewById(R.id.controller_posText);
        mDurationTextView = (TextView)mControllerView.findViewById(R.id.controller_durationText);
        mProgressBar = (SeekBar)mControllerView.findViewById(R.id.videoProgress);
    }
    
    private void bindEvent(){
        mControllerView.setOnTouchListener(new OnTouchListener(){ 
            public boolean onTouch(View v, MotionEvent event){ 
                // 获取相对屏幕的坐标，即以屏幕左上角为原点 
                hideControllerView();
                return true; 
            }
        });
        
        mLockView.setOnClickListener(new Button.OnClickListener(){ 
            @Override
            public void onClick(View v){ 
                unlock();
            }
        });
        
        mProgressBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener(){ 
            @Override
            public void onStopTrackingTouch(SeekBar seekBar){ 
                action("change", curPos);
            }

            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser){ 
                if (fromUser) {
                    curPos = progress;
                }
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar){ }

        });

        mForwardBtn.setOnClickListener(new Button.OnClickListener(){ 
            @Override
            public void onClick(View v){  
                if(curPos <= duration - TIME){
                    curPos += TIME;
                } 
                mProgressBar.setProgress(curPos);
                action("forward", curPos);
            }

        });

        mBackwardBtn.setOnClickListener(new Button.OnClickListener(){ 
            @Override
            public void onClick(View v){
                if(curPos >= TIME){
                    curPos -= TIME; 
                }
                mProgressBar.setProgress(curPos); 
                action("backward", curPos);
            }
        });

        mPlayPauseBtn.setOnClickListener(new Button.OnClickListener(){ 
            @Override
            public void onClick(View v){
                String action = null;
                if (bPlaying){
                    bPlaying = false;
                    mPlayPauseBtn.getDrawable().setLevel(2);
                    action = "pause"; // 暂停 
                }else{
                    bPlaying = true;
                    mPlayPauseBtn.getDrawable().setLevel(1);
                    action = "play"; // 播放 
                }

                action(action, null);
            }
        });

        mPrevBtn.setOnClickListener(new Button.OnClickListener(){ 
            @Override
            public void onClick(View v){ 
                action("prev", null);
            } 
        });

        mNextBtn.setOnClickListener(new Button.OnClickListener(){
            @Override
            public void onClick(View v){ 
                action("next", null);
            } 
        });
        
        mStopBtn.setOnClickListener(new Button.OnClickListener(){
            @Override
            public void onClick(View v){
                hideControllerView();
            } 
        });
        
        mLockBtn.setOnClickListener(new Button.OnClickListener(){
            @Override
            public void onClick(View v){
                lock();
            } 
        });
    }
    
    private void action(String action, Integer position){
        Intent intent = new Intent(); 
        intent.setAction(TAG); 
        intent.putExtra("flag", action);
        if (position != null){
            intent.putExtra("newpos", position); 
        }
        
        this.sendBroadcast(intent); // 发送广播 
    }

    private void addToWin(){ 
        // 获取WindowManager 
        mWindowManager = (WindowManager)getApplicationContext().getSystemService("window");
        
        // 设置LayoutParams(全局变量）相关参数 
        WindowManager.LayoutParams controllerWmParams = new WindowManager.LayoutParams(); 
        controllerWmParams.type = WindowManager.LayoutParams.TYPE_PHONE;// 2002; 
        controllerWmParams.flags |= WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE;// 8; 
        controllerWmParams.gravity = Gravity.CENTER | Gravity.BOTTOM; 
        // 以屏幕左上角为原点，设置x、y初始值 
        controllerWmParams.x = 0; 
        controllerWmParams.y = 0; 
        // 设置悬浮窗口长宽数据 
        controllerWmParams.width = WindowManager.LayoutParams.FILL_PARENT; 
        controllerWmParams.height = WindowManager.LayoutParams.WRAP_CONTENT; 
        controllerWmParams.format = 1; 
        mWindowManager.addView(mControllerView, controllerWmParams);
        
        WindowManager.LayoutParams lockWmParams = new WindowManager.LayoutParams(); 
        lockWmParams.type = WindowManager.LayoutParams.TYPE_PHONE;// 2002; 
        lockWmParams.flags |= WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE;// 8; 
        lockWmParams.gravity = Gravity.TOP | Gravity.LEFT; 
        // 以屏幕左上角为原点，设置x、y初始值 
        lockWmParams.x = 0; 
        lockWmParams.y = 0; 
        // 设置悬浮窗口长宽数据 
        lockWmParams.width = WindowManager.LayoutParams.WRAP_CONTENT; 
        lockWmParams.height = WindowManager.LayoutParams.WRAP_CONTENT; 
        lockWmParams.format = 1;
        mWindowManager.addView(mLockView, lockWmParams); 
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId){
        Log.d("FloatingService", "onStartCommand FloatingService"); 
        
        curPos = intent.getIntExtra("position", 0);
        duration = intent.getIntExtra("max", 0);
        mPosTextView.setText(convert(curPos/1000));
        mDurationTextView.setText(convert(duration/1000));
        
        mProgressBar.setMax(duration);
        mProgressBar.setProgress(curPos);
         
        if (bLock){
            lock();
        }else{
            unlock();
            boolean touch = intent.getBooleanExtra("touch", false);
            if (touch){ 
                boolean visable = intent.getBooleanExtra("visable", false);
                if(visable){
                    showControllerView();
                }else{
                    hideControllerView();
                }
            }
        }
         
        return super.onStartCommand(intent, flags, startId);
    }
    
    private void showControllerView(){
        mControllerView.setVisibility(View.VISIBLE);
        bControllerViewShow = true;
    }
    
    private void hideControllerView(){
        mControllerView.setVisibility(View.GONE);
        bControllerViewShow = false;
    }
    
    private void lock(){
        hideControllerView();
        mLockView.setVisibility(View.VISIBLE); 
        bLock = true;
    }
    
    private void unlock(){
        mLockView.setVisibility(View.GONE);
        bLock = false;
    }

    @Override
    public void onDestroy(){
        Log.d("FloatingService", "onDestroy FloatingService");
        mWindowManager.removeView(mControllerView);
        mWindowManager.removeView(mLockView);
        super.onDestroy();
    }

    @Override
    public IBinder onBind(Intent intent){
        return null;
    }
    
    private String convert(long second){
        DecimalFormat df = new DecimalFormat("00");
        if (second < 60){
            return "00:00:"+df.format(second);
        }
        
        if (second < 3600){
            return "00:"+df.format(second/60)+":"+df.format(second%60);
        }
        
        long temp = second%3600;
        
        return df.format(second/3600)+":"+df.format(temp/60)+":"+df.format(temp%60);
    }
}
