package tech.apps.music.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
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

    var iconUri: String? = null

    private var onItemClickListener: ((Int) -> Unit)? = null

    fun setItemClickListener(listener: (Int) -> Unit) {
        onItemClickListener = listener
    }

    override fun getItemCount(): Int {
        return songs.size
    }

    override fun onBindViewHolder(holder: EpisodeViewHolder, position: Int) {
        holder.itemView.apply {

            if (!iconUri.isNullOrEmpty()) {
                glide.load(iconUri).centerCrop().into(imageViewEpisodeThumbnail)
            }
            textViewEpisodeText.text = songs[position].title

            determinateBarEpisodesIt.progress = ((songs[position].watchedPosition) / (songs[position].duration*10)).toInt()

            setOnClickListener {
                onItemClickListener?.let { click ->
                    click(position)
                }
            }
        }
    }


}