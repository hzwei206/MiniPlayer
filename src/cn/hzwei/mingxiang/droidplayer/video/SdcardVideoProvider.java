/**
 * 
 */
package cn.hzwei.mingxiang.droidplayer.video;

import java.io.File;
import java.io.FileFilter;

import android.os.Environment;

/**
 * 从SD卡路径获得视频资源
 * 
 * @deprecated instead by {@link MediaVideoProvider}
 */
public class SdcardVideoProvider implements IAbstructProvider {

    /* (non-Javadoc)
     * @see cn.hzwei.mingxiang.droidplayer.video.IAbstructProvider#fillInVideo(java.util.List)
     */
    @Override
    public void scanVedio(){
        if (Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED)) {
            VideoCached.clearAll();
            fileList(Environment.getExternalStorageDirectory());
        } 
    }
 
    private void fileList(File file){
        file.listFiles(new FileFilter(){
            @Override
            public boolean accept(File file){
                // sdCard找到视频名称
                String name = file.getName();
                int i = name.indexOf('.');
                if(i != -1){
                    name = name.substring(i);
                    if(name.equalsIgnoreCase(".mp4") || name.equalsIgnoreCase(".3gp") || name.equalsIgnoreCase(".wmv")
                            || name.equalsIgnoreCase(".ts") || name.equalsIgnoreCase(".rmvb") || name.equalsIgnoreCase(".mov")
                            || name.equalsIgnoreCase(".m4v") || name.equalsIgnoreCase(".avi") || name.equalsIgnoreCase(".m3u8")
                            || name.equalsIgnoreCase(".3gpp") || name.equalsIgnoreCase(".3gpp2") || name.equalsIgnoreCase(".mkv")
                            || name.equalsIgnoreCase(".flv") || name.equalsIgnoreCase(".divx") || name.equalsIgnoreCase(".f4v")
                            || name.equalsIgnoreCase(".rm") || name.equalsIgnoreCase(".asf") || name.equalsIgnoreCase(".ram")
                            || name.equalsIgnoreCase(".mpg") || name.equalsIgnoreCase(".v8") || name.equalsIgnoreCase(".swf")
                            || name.equalsIgnoreCase(".m2v") || name.equalsIgnoreCase(".asx") || name.equalsIgnoreCase(".ra")
                            || name.equalsIgnoreCase(".ndivx") || name.equalsIgnoreCase(".xvid")){
                        VideoInfo video = new VideoInfo();
                        video.title = file.getName();
                        video.path = file.getAbsolutePath();
                        VideoCached.addVideo(video);
                        return true;
                    }
                }else if(file.isDirectory()){
                    fileList(file);
                }
                return false;
            }
        });
    }
}
