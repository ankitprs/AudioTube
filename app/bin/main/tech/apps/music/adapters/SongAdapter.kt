package tech.apps.music.adapters

import android.annotation.SuppressLint
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.view.isVisible
import androidx.recyclerview.widget.AsyncListDiffer
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.RequestManager
import tech.apps.music.R
import tech.apps.music.databinding.SongItemHorizontalBinding
import tech.apps.music.model.SongModelForList
import tech.apps.music.util.getThumbnailFromId
import tech.apps.music.util.songDuration
import javax.inject.Inject

class SongAdapter @Inject constructor(
    private val glide: RequestManager
) : RecyclerView.Adapter<SongAdapter.SongViewHolder>() {

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

    class SongViewHolder(val binding: SongItemHorizontalBinding) :
        RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): SongViewHolder {
        return SongViewHolder(
            SongItemHorizontalBinding.inflate(
                LayoutInflater.from(parent.context),
                parent,
                false
            )
        )
    }

    private var onItemClickListener: ((SongModelForList, Int) -> Unit)? = null
    private var onItemMenuClickListener: ((Pair<SongModelForList,View>) -> Unit)? = null

    fun setItemClickListener(listener: (SongModelForList, Int) -> Unit) {
        onItemClickListener = listener
    }
    fun setItemMenuClickListener(listener: (Pair<SongModelForList,View>) -> Unit) {
        onItemMenuClickListener = listener
    }

    override fun getItemCount(): Int {
        return songs.size
    }

    @SuppressLint("ResourceAsColor")
    override fun onBindViewHolder(holder: SongViewHolder, position: Int) {

        val song = songs[position]

        if (song.time == 0L) {

            holder.binding.apply {
                thumbnailImageTYVideo.setImageResource(R.drawable.recently_added_null)

                durationTYVideo.isVisible = false
                titleTYVideo.isVisible = false
                channelTYVideo.isVisible = false
            }
            return
        }

        holder.binding.apply {
            titleTYVideo.text = song.title

            glide.load(getThumbnailFromId(song.videoId))
                .fitCenter()
                .override(480, 270)
                .centerCrop()
                .into(thumbnailImageTYVideo)

            if (song.duration > 0L) {
                durationTYVideo.text = songDuration(song.duration / 1000L)
            } else {

                if (song.duration == -1L){
                    durationTYVideo.text = "LIVE"
                    durationTYVideo.background = ColorDrawable(Color.parseColor("#FF0000"))
                }else{

                    durationTYVideo.text = song.durationText
                }
            }
            if (song.watchedPosition == 0L) {
                determinateBar.isVisible = false
            } else {
                if (song.duration != 0L)
                    determinateBar.progress =
                        ((song.watchedPosition) * 100 / (song.duration)).toInt()
            }

            channelTYVideo.text = song.ChannelName

            root.setOnClickListener {
                onItemClickListener?.let { click ->
                    click(song, position)
                }
            }
        }
    }

}