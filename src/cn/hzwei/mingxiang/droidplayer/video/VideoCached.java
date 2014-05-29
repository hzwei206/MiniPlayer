/**
 * 
 */
package cn.hzwei.mingxiang.droidplayer.video;

import java.util.ArrayList;
import java.util.List;

/**
 * 缓存类
 *
 * @author huangzw
 * @date 2014年5月25日 下午2:02:31
 * @version 1.0
 */
public class VideoCached {
    private static List<Integer> playList = new ArrayList<Integer>(0);  //播放列表，存放的是视频索引
    private static List<VideoInfo> videoList = new ArrayList<VideoInfo>();
    public static void clearAll(){
        videoList.clear();
        playList.clear();
    }
    
    public static void clearAllThumbnails(){
        for (VideoInfo video: videoList){
            video.thumbnail = null;
        }
    }
    
    public static void addVideo(VideoInfo video){
        videoList.add(video);
    }
    
    public static int getVideoCount(){
        return videoList.size();
    }
    
    public static VideoInfo getVideo(int position){
        if (videoList.isEmpty()){
            return null;
        }
        
        if (position < 0){
            return videoList.get(0);
        }
        
        if (position >= videoList.size()){
            return videoList.get(videoList.size() - 1);
        }
        
        return videoList.get(position);
    }
    
    public static VideoInfo getPlayListVideo(int position){
        int idx = 0;
        if (position>=0 && position < playList.size()){
            idx = playList.get(position);
        }
        return getVideo(idx);
    }
}
