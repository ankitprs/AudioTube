package tech.apps.music.adapters

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.view.isVisible
import androidx.recyclerview.widget.AsyncListDiffer
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import androidx.viewbinding.ViewBinding
import com.bumptech.glide.RequestManager
import kotlinx.android.synthetic.main.song_item_vertical.view.*
import tech.apps.music.R
import tech.apps.music.databinding.SongItemHorizontalBinding
import tech.apps.music.databinding.SongItemVerticalBinding
import tech.apps.music.model.SongModelForList
import tech.apps.music.util.TimeFunction
import tech.apps.music.util.VideoData
import javax.inject.Inject

class SongAdapter @Inject constructor(
    private val glide: RequestManager
) : RecyclerView.Adapter<SongAdapter.SongViewHolder>() {

    var isViewHorizontal: Boolean = false

    private val diffCallback = object : DiffUtil.ItemCallback<SongModelForList>() {
        override fun areItemsTheSame(
            oldItem: SongModelForList,
            newItem: SongModelForList,
        ): Boolean {
            return oldItem.videoId == newItem.videoId
        }

        override fun areContentsTheSame(
            oldItem: SongModelForList,
            newItem: SongModelForList,
        ): Boolean {
            return oldItem.hashCode() == newItem.hashCode()
        }
    }
    private val differ: AsyncListDiffer<SongModelForList> = AsyncListDiffer(this, diffCallback)

    var songs: List<SongModelForList>
        get() = differ.currentList
        set(value) = differ.submitList(value)

    class SongViewHolder(binding: ViewBinding) : RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): SongViewHolder {
        return if (isViewHorizontal) {
            SongViewHolder(
                SongItemHorizontalBinding.inflate(
                    LayoutInflater.from(parent.context),
                    parent,
                    false
                )
            )
        } else {
            SongViewHolder(
                SongItemVerticalBinding.inflate(
                    LayoutInflater.from(parent.context),
                    parent,
                    false
                )
            )
        }
    }

    private var onItemClickListener: ((SongModelForList) -> Unit)? = null

    fun setItemClickListener(listener: (SongModelForList) -> Unit) {
        onItemClickListener = listener
    }

    override fun getItemCount(): Int {
        return songs.size
    }

    override fun onBindViewHolder(holder: SongViewHolder, position: Int) {

        val song = songs[position]

        if (song.time == 0L) {

            holder.itemView.thumbnailImageTYVideo.setImageResource(R.drawable.recently_added_null)

            holder.itemView.durationTYVideo.isVisible = false
            holder.itemView.titleTYVideo.isVisible = false
            holder.itemView.channelTYVideo.isVisible = false
            return
        }

        holder.itemView.apply {
            titleTYVideo.text = song.title

            glide.load(VideoData.getThumbnailFromId(song.videoId))
                .fitCenter()
                .override(480, 270)
                .centerCrop()
                .into(thumbnailImageTYVideo)

            if(song.duration != 0L){
                durationTYVideo.text = TimeFunction.songDuration(song.duration)
            }else{
                durationTYVideo.text = song.durationText

            }
            if (song.watchedPosition == 0L) {
                determinateBar.isVisible = false
            } else {
                determinateBar.progress =
                    ((song.watchedPosition) / ((song.duration)*10)).toInt()
            }

            channelTYVideo.text = song.ChannelName


            setOnClickListener {
                onItemClickListener?.let { click ->
                    click(song)
                }
            }
        }
    }

}