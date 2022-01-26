package tech.apps.music.adapters

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.AsyncListDiffer
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.RequestManager
import tech.apps.music.databinding.PremiumItemListBinding
import tech.apps.music.model.EpisodesListModel
import javax.inject.Inject

class PremiumListAdapter @Inject constructor(
    private val glide: RequestManager
) : RecyclerView.Adapter<PremiumListAdapter.SongViewHolder>() {

    private val diffCallback = object : DiffUtil.ItemCallback<EpisodesListModel>() {
        override fun areItemsTheSame(
            oldItem: EpisodesListModel,
            newItem: EpisodesListModel
        ): Boolean {
            return oldItem.id == newItem.id
        }

        override fun areContentsTheSame(
            oldItem: EpisodesListModel,
            newItem: EpisodesListModel
        ): Boolean {
            return oldItem.hashCode() == newItem.hashCode()
        }
    }
    private val differ: AsyncListDiffer<EpisodesListModel> = AsyncListDiffer(this, diffCallback)

    var songs: List<EpisodesListModel>
        get() = differ.currentList
        set(value) = differ.submitList(value)

    class SongViewHolder(val binding: PremiumItemListBinding) : RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): SongViewHolder {
        return SongViewHolder(
            PremiumItemListBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        )

    }

    private var onItemClickListener: ((EpisodesListModel) -> Unit)? = null

    fun setItemClickListener(listener: (EpisodesListModel) -> Unit) {
        onItemClickListener = listener
    }

    override fun getItemCount(): Int {
        return songs.size
    }

    override fun onBindViewHolder(holder: SongViewHolder, position: Int) {
        val song = songs[position]
        holder.binding.apply {
            titleAudioBook.text = song.title
            authorAudioBook.text = song.author
            durationAudioBook.text = song.duration

            glide.load(song.thumbnailUrl)
                .centerCrop()
                .into(thumbnailImageAudioBook)


            root.setOnClickListener {
                onItemClickListener?.let { click ->
                    click(song)
                }
            }
        }
    }

}