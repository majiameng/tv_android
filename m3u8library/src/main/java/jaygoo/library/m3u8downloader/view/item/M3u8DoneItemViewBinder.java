package jaygoo.library.m3u8downloader.view.item;

import android.content.Intent;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;

import java.io.File;

import jaygoo.library.m3u8downloader.M3U8Downloader;
import jaygoo.library.m3u8downloader.M3U8Library;
import jaygoo.library.m3u8downloader.R;
import me.drakeet.multitype.ItemViewBinder;

/**
 * @author huangyong
 * createTime 2019-09-28
 */
public class M3u8DoneItemViewBinder extends ItemViewBinder<M3u8DoneItem, M3u8DoneItemViewBinder.ViewHolder> {

    @NonNull
    @Override
    protected ViewHolder onCreateViewHolder(@NonNull LayoutInflater inflater, @NonNull ViewGroup parent) {
        View root = inflater.inflate(R.layout.item_m3u8_done_item, parent, false);
        return new ViewHolder(root);
    }

    @Override
    protected void onBindViewHolder(@NonNull final ViewHolder holder, @NonNull final M3u8DoneItem m3u8DoneItem) {
        holder.itemTitle.setText(m3u8DoneItem.getM3u8DoneInfo().getTaskName());
        holder.itemState.setText("已完成");
        Glide.with(holder.itemView.getContext()).load(m3u8DoneItem.getM3u8DoneInfo().getTaskPoster()).into(holder.icon);

        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String  path=M3U8Downloader.getInstance().getM3U8Path(m3u8DoneItem.getM3u8DoneInfo().getTaskData());
                Log.d("M3u8DoneItemViewBinder", path+"111111111");
                Log.d("M3u8DoneItemViewBinder", m3u8DoneItem.getM3u8DoneInfo().getTaskData());
                Log.d("M3u8DoneItemViewBinder", m3u8DoneItem.getM3u8DoneInfo()+"");
                path=path.substring(0, path.lastIndexOf(File.separator));
//                if (M3U8Downloader.getInstance().checkM3U8IsExist(path)) {

                    Log.d("M3u8DoneItemViewBinder", path+"__");


//                    Intent intent = new Intent(holder.itemView.getContext(), StorePlayActivity.class);
                    Intent intent = new Intent();
                    intent.setClassName(holder.itemView.getContext(), "cn.mahua.vod.ui.play.StorePlayActivity");
                    intent.putExtra("play_url",path);
                    intent.putExtra("play_name",m3u8DoneItem.getM3u8DoneInfo().getTaskName());
                    intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                    holder.itemView.getContext().startActivity(intent);
////                    intent.putExtra(DataInter.Key.KEY_CURRENTPLAY_URL, M3U8Downloader.getInstance().getM3U8Path(m3u8DoneItem.getM3u8DoneInfo().getTaskData()));
////                    intent.putExtra(DataInter.Key.KEY_CURRENTPLAY_TITLE, m3u8DoneItem.getM3u8DoneInfo().getTaskName());
//                    holder.itemView.getContext().startActivity(intent);
//                    Toast.makeText( holder.itemView.getContext(), "播放本地文件", Toast.LENGTH_SHORT).show();
//                    TODO  播放
//                } else {
//                    Toast.makeText(M3U8Library.getContext(), "未发现播放文件，删除了？", Toast.LENGTH_SHORT).show();
//                }
            }
        });
        holder.itemView.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                if (m3u8DoneItem.clickListener != null) {
                    m3u8DoneItem.clickListener.onLongClick(m3u8DoneItem, getAdapter().getItems().indexOf(m3u8DoneItem));
                }
                return true;
            }
        });
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        TextView itemState;
        TextView itemTitle;
        ImageView icon;

        ViewHolder(View itemView) {
            super(itemView);
            icon = itemView.findViewById(R.id.m3u8_item_icon);
            itemTitle = itemView.findViewById(R.id.m3u8_title);
            itemState = itemView.findViewById(R.id.m3u8_state);
        }
    }

    public interface OnItemListener {
        void onLongClick(M3u8DoneItem m3u8DoneItem, int doneItem);
    }
}
