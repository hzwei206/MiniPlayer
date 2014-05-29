package cn.hzwei.mingxiang.droidplayer.video;

import android.graphics.Bitmap;
 
public class VideoInfo {
    public String title;
    public String displayName;
    public String path;
    public long fileSize;
    public long bookMark;
    public long duration;
    public Bitmap thumbnail = null;
    public Long id;
    
    public String toString(){
        return new StringBuilder().append("{")
                .append("title=").append(title)
                .append(", displayName=").append(displayName)
                .append(", path=").append(path)
                .append(", fileSize=").append(fileSize)
                .append(", bookMark=").append(bookMark)
                .append(", duration=").append(duration)
                .append(", id=").append(id)
                .append("}").toString();
    }
}
