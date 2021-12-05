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
import tech.apps.music.database.network.VideoObject
import javax.inject.Inject

class SearchAdapter @Inject constructor(
    private val glide: RequestManager
) : RecyclerView.Adapter<SearchAdapter.SongSearchViewHolder>() {

    private val diffCallback = object : DiffUtil.ItemCallback<VideoObject>() {
        override fun areItemsTheSame(oldItem: VideoObject, newItem: VideoObject): Boolean {
            return oldItem.videoId == newItem.videoId
        }

        override fun areContentsTheSame(oldItem: VideoObject, newItem: VideoObject): Boolean {
            return oldItem.hashCode() == newItem.hashCode()
        }
    }
    private val differ: AsyncListDiffer<VideoObject> = AsyncListDiffer(this, diffCallback)

    var songs: List<VideoObject>
        get() = differ.currentList
        set(value) = differ.submitList(value)

    class SongSearchViewHolder(listItem: View) : RecyclerView.ViewHolder(listItem)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): SongSearchViewHolder {
        return SongSearchViewHolder(
            LayoutInflater.from(parent.context).inflate(
                R.layout.yt_video_card_view_full,
                parent,
                false
            )
        )
    }

    private var onItemClickListener: ((VideoObject) -> Unit)? = null

    fun setItemClickListener(listener: (VideoObject) -> Unit) {
        onItemClickListener = listener
    }

    override fun getItemCount(): Int {
        return songs.size
    }

    override fun onBindViewHolder(holder: SongSearchViewHolder, position: Int) {
        val song = songs[position]

        if (song.duration.isEmpty()) {
        }

        holder.itemView.apply {
            titleTYVideo.text = song.title

            glide.load(song.thumbnails)
                .override(1280, 720)
                .centerCrop()
                .into(thumbnailImageTYVideo)

            channelTYVideo.text = song.channelName
            println(song.duration)

            if (song.duration.isEmpty()) {
                durationTYVideo.isVisible = false
            } else {
                durationTYVideo.text = song.duration
            }

            setOnClickListener {
                onItemClickListener?.let { click ->
                    click(song)
                }
            }
        }
    }

}