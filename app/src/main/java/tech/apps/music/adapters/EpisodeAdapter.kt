package tech.apps.music.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.view.isVisible
import androidx.recyclerview.widget.AsyncListDiffer
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.RequestManager
import kotlinx.android.synthetic.main.episode_list_item.view.*
import tech.apps.music.R
import tech.apps.music.model.EpisodeModel
import javax.inject.Inject

class EpisodeAdapter @Inject constructor(
    private val glide: RequestManager
) :
    RecyclerView.Adapter<EpisodeAdapter.EpisodeViewHolder>() {

    private val diffCallback = object : DiffUtil.ItemCallback<EpisodeModel>() {
        override fun areItemsTheSame(oldItem: EpisodeModel, newItem: EpisodeModel): Boolean {
            return oldItem == newItem
        }

        override fun areContentsTheSame(oldItem: EpisodeModel, newItem: EpisodeModel): Boolean {
            return oldItem.hashCode() == newItem.hashCode()
        }
    }
    private val differ: AsyncListDiffer<EpisodeModel> = AsyncListDiffer(this, diffCallback)

    var songs: List<EpisodeModel>
        get() = differ.currentList
        set(value) = differ.submitList(value)

    class EpisodeViewHolder(listItem: View) : RecyclerView.ViewHolder(listItem)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): EpisodeViewHolder {
        return EpisodeViewHolder(
            LayoutInflater.from(parent.context).inflate(
                R.layout.episode_list_item,
                parent,
                false
            )
        )
    }

    var currentlyPlayingSongId: String? = null

    private var onItemClickListener: ((EpisodeModel) -> Unit)? = null

    fun setItemClickListener(listener: (EpisodeModel) -> Unit) {
        onItemClickListener = listener
    }

    override fun getItemCount(): Int {
        return songs.size
    }

    override fun onBindViewHolder(holder: EpisodeViewHolder, position: Int) {

        val song = songs[position]
        holder.itemView.apply {

//            if (currentlyPlayingSongId === song.songId) {
//                val gradientDrawable = GradientDrawable(
//                    GradientDrawable.Orientation.BL_TR,
//                    intArrayOf(
//                        Color.parseColor("#00dbff"),
//                        Color.parseColor("#0800ff")
//                    )
//                )
//                gradientDrawable.cornerRadius = 0f
//
//                //Set Gradient
//                episodesListCardView.background = gradientDrawable
//                imageViewEpisodesGoTo
//            }

            glide.load(song.songThumbnailUrl).centerCrop().into(imageViewEpisodeThumbnail)

            textViewEpisodeText.text = song.title

            if (song.watchedPosition == 0L) {
                determinateBarEpisodesIt.isVisible = false
            } else {
                determinateBarEpisodesIt.progress =
                    ((song.watchedPosition) / (song.duration * 10)).toInt()
            }

            setOnClickListener {
                onItemClickListener?.let { click ->
                    click(song)
                }
            }
        }
    }


}