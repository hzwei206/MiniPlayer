/**
 * 
 */
package cn.hzwei.mingxiang.droidplayer;

import cn.hzwei.mingxiang.droidplayer.video.IAbstructProvider;
import cn.hzwei.mingxiang.droidplayer.video.MediaVideoProvider;
import cn.hzwei.mingxiang.droidplayer.video.VideoCached;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.res.Configuration;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;

public class MovieList extends Activity {
    final static int UPDATE = Menu.FIRST;
    final static int QUIT = Menu.FIRST + 1;
    private ListView listView;
    private IAbstructProvider videoProvider = null;
    boolean registReceiver = false;
    
    @Override
    protected void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);
        setTitle(R.string.app_title); 
        setContentView(R.layout.list);

        videoProvider = new MediaVideoProvider(this);
       
        videoProvider.scanVedio();

        VideoAdapter adapter = new VideoAdapter(this);
        listView = (ListView)findViewById(R.id.lv);
        listView.setAdapter(adapter);

        listView.setOnItemClickListener(new ListView.OnItemClickListener(){
            @Override
            public void onItemClick(AdapterView<?> arg0, View view, int position, long id){
                Intent intent = new Intent(MovieList.this, MyVideoPlayer.class);
                intent.putExtra("videoIdx", position);
                startActivity(intent);
            }
        });
    }
     
    @Override
    public void onConfigurationChanged(Configuration newConfig){ 
        super.onConfigurationChanged(newConfig);
        VideoCached.clearAllThumbnails();
    }
 
    @Override
    protected void onDestroy(){
        if (registReceiver){
            unregisterReceiver(broadcastReceiver);
        }
        
        super.onDestroy();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu){
        int i = Menu.FIRST;
        menu.add(i, UPDATE, i, "更新列表");
        menu.add(i, QUIT, i, "退出");
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item){
        switch(item.getItemId()){
            case UPDATE:
                scanSdCard();
                break;
            case QUIT: // 退出
                this.finish();
                break;

        }

        return super.onOptionsItemSelected(item);

    }
    
    AlertDialog builder = null;
    
    BroadcastReceiver broadcastReceiver = new BroadcastReceiver(){
        @Override
        public void onReceive(Context context, Intent intent){
            String action = intent.getAction();
            if(Intent.ACTION_MEDIA_SCANNER_STARTED.equals(action)){
                builder=new AlertDialog.Builder(MovieList.this).setMessage("正在扫描存储卡...").create();
                builder.show();
            }else if(Intent.ACTION_MEDIA_SCANNER_FINISHED.equals(action)){
                if (builder != null){
                    builder.dismiss();
                }
                videoProvider.scanVedio();
            }
        }
    };

    private void scanSdCard(){
        IntentFilter intentfilter = new IntentFilter(Intent.ACTION_MEDIA_SCANNER_STARTED);
        intentfilter.addAction(Intent.ACTION_MEDIA_SCANNER_FINISHED);
        intentfilter.addDataScheme("file");

        registerReceiver(broadcastReceiver, intentfilter);
        registReceiver = true;
        sendBroadcast(new Intent(Intent.ACTION_MEDIA_MOUNTED, Uri.parse("file://"
                + Environment.getExternalStorageDirectory().getAbsolutePath())));
    }
}