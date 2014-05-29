/**
 * 
 */
package cn.hzwei.mingxiang.droidplayer;
 
import java.text.DecimalFormat;

import cn.hzwei.mingxiang.droidplayer.video.VideoCached;
import cn.hzwei.mingxiang.droidplayer.video.VideoInfo;
import android.content.Context;
import android.graphics.Bitmap;
import android.os.AsyncTask;
import android.provider.MediaStore;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

/**
 *  
 * @author huangzw
 * @date 2014年5月25日 上午12:51:23
 * @version 1.0
 */
public class VideoAdapter extends BaseAdapter { 
    private Context mContext; 
    private LayoutInflater mInfalter; 
    public VideoAdapter(Context mContext){
        this.mContext = mContext;
        mInfalter = (LayoutInflater) mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
    }

    /* (non-Javadoc)
     * @see android.widget.Adapter#getCount()
     */
    @Override
    public final int getCount(){ 
        return VideoCached.getVideoCount();
    }

    /* (non-Javadoc)
     * @see android.widget.Adapter#getItem(int)
     */
    @Override
    public final VideoInfo getItem(int position){ 
         return VideoCached.getVideo(position);
    }

    /* (non-Javadoc)
     * @see android.widget.Adapter#getItemId(int)
     */
    @Override
    public long getItemId(int position){ 
        if (getCount() <= position){
            return 0; 
        }
        
        return position;
    }

    /* (non-Javadoc)
     * @see android.widget.Adapter#getView(int, android.view.View, android.view.ViewGroup)
     */
    @Override
    public View getView(int position, View convertView, ViewGroup parent){
        ViewHolder holder ;  
        if(convertView == null){  
            holder = new ViewHolder();  
            convertView = mInfalter.inflate(R.layout.movie_item, null);  
            holder.thumbImage = (ImageView)convertView.findViewById(R.id.movieThumbnail);  
            holder.titleText = (TextView)convertView.findViewById(R.id.movieName);
            holder.pathText = (TextView)convertView.findViewById(R.id.moviePath);
            convertView.setTag(holder);  
        }else{  
            holder = (ViewHolder)convertView.getTag();  
        }  
          
        //显示信息  
        VideoInfo video = getItem(position);
        holder.titleText.setText(video.title);
        holder.pathText.setText(convert(video.duration/1000)); 
        holder.thumbImage.setImageResource(R.drawable.son);
        
        if(video.id != null && video.thumbnail == null){ 
            AsyncImageLoadTask task = new AsyncImageLoadTask(position);
            try{
                task.execute(video.id);
            }catch(Exception e){
                Log.e("VideoAdapter", "ListView异步加载图片异常：" + e.toString());
            }
            
        }else if(video.thumbnail != null){
            holder.thumbImage.setImageBitmap(video.thumbnail);
        }

        return convertView;  
    }
     
    private class ViewHolder{  
        ImageView thumbImage;  
        TextView titleText;
        TextView pathText;
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
    
    private final class AsyncImageLoadTask extends AsyncTask<Long, Integer, Bitmap>{
        private int position;
        private AsyncImageLoadTask(int position){
            this.position = position;
        }
        
        @Override
        protected Bitmap doInBackground(Long... params){ 
            return MediaStore.Video.Thumbnails.getThumbnail(mContext.getContentResolver(), 
                    params[0], MediaStore.Video.Thumbnails.MICRO_KIND, null);
        }

        @Override
        protected void onPostExecute(Bitmap result){
            VideoInfo v =  VideoCached.getVideo(position);
            if(v!=null && result!=null){
                v.thumbnail = result;
                VideoAdapter.this.notifyDataSetChanged();
            }
            super.onPostExecute(result);
        } 
    }
}
