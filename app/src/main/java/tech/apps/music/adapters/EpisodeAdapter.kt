package tech.apps.music.adapters

import android.graphics.Color
import android.graphics.drawable.GradientDrawable
import android.util.Log
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.view.isVisible
import androidx.recyclerview.widget.AsyncListDiffer
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.RequestManager
import tech.apps.music.databinding.EpisodeListItemBinding
import tech.apps.music.model.YTAudioDataModel
import javax.inject.Inject

class EpisodeAdapter @Inject constructor(
    private val glide: RequestManager
) :
    RecyclerView.Adapter<EpisodeAdapter.EpisodeViewHolder>() {

    private val diffCallback = object : DiffUtil.ItemCallback<YTAudioDataModel>() {
        override fun areItemsTheSame(
            oldItem: YTAudioDataModel,
            newItem: YTAudioDataModel
        ): Boolean {
            return oldItem == newItem
        }

        override fun areContentsTheSame(
            oldItem: YTAudioDataModel,
            newItem: YTAudioDataModel
        ): Boolean {
            return oldItem.hashCode() == newItem.hashCode()
        }
    }
    private val differ: AsyncListDiffer<YTAudioDataModel> = AsyncListDiffer(this, diffCallback)

    var songs: List<YTAudioDataModel>
        get() = differ.currentList
        set(value) = differ.submitList(value)

    class EpisodeViewHolder(val binding: EpisodeListItemBinding) :
        RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): EpisodeViewHolder {
        return EpisodeViewHolder(
            EpisodeListItemBinding.inflate(
                LayoutInflater.from(parent.context),
                parent,
                false
            )
        )
    }

    var currentlyPlayingSongId: String? = null

    private var onItemClickListener: ((YTAudioDataModel) -> Unit)? = null

    fun setItemClickListener(listener: (YTAudioDataModel) -> Unit) {
        onItemClickListener = listener
    }

    override fun getItemCount(): Int {
        return songs.size
    }

    override fun onBindViewHolder(holder: EpisodeViewHolder, position: Int) {

        val song = songs[position]
        holder.binding.apply {

            if (currentlyPlayingSongId === song.mediaId) {
                Log.d("EpisodeAdapter", song.mediaId+" : song.mediaId || currentlyPlayingSongId : "+currentlyPlayingSongId.toString())
                val gradientDrawable = GradientDrawable(
                    GradientDrawable.Orientation.BL_TR,
                    intArrayOf(
                        Color.parseColor("#00dbff"),
                        Color.parseColor("#0800ff")
                    )
                )
                gradientDrawable.cornerRadius = 0f

                //Set Gradient
                episodesListCardView.background = gradientDrawable
            }

            glide.load(song.thumbnailUrl).centerCrop().into(imageViewEpisodeThumbnail)

            textViewEpisodeText.text = song.title

            determinateBarEpisodesIt.isVisible = false

            root.setOnClickListener {
                onItemClickListener?.let { click ->
                    click(song)
                }
            }
        }
    }


}