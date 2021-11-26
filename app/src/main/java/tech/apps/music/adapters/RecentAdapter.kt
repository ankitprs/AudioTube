package tech.apps.music.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.view.isVisible
import androidx.recyclerview.widget.AsyncListDiffer
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.RequestManager
import kotlinx.android.synthetic.main.yt_video_card_item.view.*
import tech.apps.music.R
import tech.apps.music.database.Repository
import tech.apps.music.database.offline.YTVideoLink
import tech.apps.music.model.YTAudioDataModel
import tech.apps.music.util.TimeFunction
import javax.inject.Inject

class RecentAdapter @Inject constructor(
    private val glide:RequestManager,
    private val repository: Repository
) : RecyclerView.Adapter<RecentAdapter.SongViewHolder>() {

    private val diffCallback= object : DiffUtil.ItemCallback<YTVideoLink>(){
        override fun areItemsTheSame(oldItem: YTVideoLink, newItem: YTVideoLink): Boolean {
            return oldItem.videoId == newItem.videoId
        }

        override fun areContentsTheSame(oldItem: YTVideoLink, newItem: YTVideoLink): Boolean {
            return oldItem.hashCode() == newItem.hashCode()
        }
    }
    private val differ: AsyncListDiffer<YTVideoLink> = AsyncListDiffer(this,diffCallback)

    var songs: List<YTVideoLink>
        get() = differ.currentList
        set(value) = differ.submitList(value)

    class SongViewHolder(listItem: View): RecyclerView.ViewHolder(listItem)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): SongViewHolder {
        return SongViewHolder(
            LayoutInflater.from(parent.context).inflate(
                R.layout.yt_video_card_item,
                parent,
                false
            )
        )
    }

    private var onItemClickListener:((YTAudioDataModel)->Unit)? =null

    fun setItemClickListener(listener :(YTAudioDataModel)->Unit){
        onItemClickListener=listener
    }

    override fun getItemCount(): Int {
        return songs.size
    }

    override fun onBindViewHolder(holder: SongViewHolder, position: Int) {

        if (songs[position].time == 0L) {

            holder.itemView.thumbnailImageTYVideo.setImageResource(R.drawable.recently_added_null)

            holder.itemView.durationTYVideo.isVisible = false
            holder.itemView.titleTYVideo.isVisible = false
            holder.itemView.channelTYVideo.isVisible = false
            return
        }

        repository.getSongModelWithLink(songs[position].link){ song->
            if(song!=null){
                holder.itemView.apply {
                    titleTYVideo.text=song.title

                    glide.load(song.thumbnailUrl)
                        .centerCrop()
                        .into(thumbnailImageTYVideo)

                    channelTYVideo.text=song.author
                    println(song.duration)

                    durationTYVideo.text= TimeFunction.songDuration(song.duration)

                    setOnClickListener {
                        onItemClickListener?.let {click->
                            click(song)
                        }
                    }
                }
            }
        }
    }

}