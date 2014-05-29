/**
 * 
 */
package cn.hzwei.mingxiang.droidplayer.video;
 
import android.content.ContentResolver;
import android.content.Context;
import android.database.Cursor;
import android.provider.MediaStore;
import android.util.Log;

/**
 * 通过媒体数据中心获取SDK的视频信息，放入应用视频库
 *
 * @author huangzw
 * @date 2014年5月25日 下午2:20:11
 * @version 1.0
 */
public class MediaVideoProvider implements IAbstructProvider {
    private Context mContext;
    public MediaVideoProvider(Context mContext){
        this.mContext = mContext;
    }

    /* (non-Javadoc)
     * @see cn.hzwei.mingxiang.droidplayer.video.IAbstructProvider#fillInVideo(java.util.List)
     */
    @Override
    public void scanVedio(){
        Cursor cursor = null;
        try{
            VideoCached.clearAll();
            
            ContentResolver mResolver = mContext.getContentResolver();
            cursor = mResolver.query(MediaStore.Video.Media.EXTERNAL_CONTENT_URI, null, null, null, null);
            if (cursor == null){
                Log.e("QueryMediaVideo", "queryMediaVideo: "+MediaStore.Video.Media.EXTERNAL_CONTENT_URI+", the cursor is null");
                cursor = mResolver.query(MediaStore.Video.Media.INTERNAL_CONTENT_URI, null, null, null, null);
                if (cursor == null){
                    Log.e("QueryMediaVideo", "queryMediaVideo: "+MediaStore.Video.Media.INTERNAL_CONTENT_URI+", the cursor is null");
                    
                    return;
                }
            }
            
            while(cursor.moveToNext()){
                VideoInfo video = createVideoInfo(mResolver, cursor);
                if (video != null){
                    VideoCached.addVideo(video);
                }
            } 
        }finally{
            if (cursor != null){
                cursor.close();
            }
        }
    }

    private VideoInfo createVideoInfo(ContentResolver mResolver, Cursor cursor){
        try{
            long duration = cursor.getLong(cursor.getColumnIndexOrThrow(MediaStore.Video.Media.DURATION));
            if (duration <= 3000){
                return null;
            }
            
            VideoInfo video = new VideoInfo();
            video.id = cursor.getLong(cursor.getColumnIndexOrThrow(MediaStore.Video.Media._ID));
            video.title = cursor.getString(cursor.getColumnIndexOrThrow(MediaStore.Video.Media.TITLE));
            video.displayName = cursor.getString(cursor.getColumnIndexOrThrow(MediaStore.Video.Media.DISPLAY_NAME));
            video.path = cursor.getString(cursor.getColumnIndexOrThrow(MediaStore.Video.Media.DATA));
            video.fileSize = cursor.getLong(cursor.getColumnIndexOrThrow(MediaStore.Video.Media.SIZE));
            video.bookMark = cursor.getLong(cursor.getColumnIndexOrThrow(MediaStore.Video.Media.BOOKMARK));
            video.duration = duration;
            return video;
        }catch(Exception e){
            Log.e("QueryMediaVideo", "createVideoInfo Exception", e);
            return null;
        }
    }
}
