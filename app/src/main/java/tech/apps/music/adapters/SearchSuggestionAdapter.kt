package tech.apps.music.adapters

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.AsyncListDiffer
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import tech.apps.music.databinding.SearchResultRecomListItemBinding

class SearchSuggestionAdapter :
    RecyclerView.Adapter<SearchSuggestionAdapter.SongSearchViewHolder>() {

    private val diffCallback = object : DiffUtil.ItemCallback<String>() {
        override fun areItemsTheSame(oldItem: String, newItem: String): Boolean {
            return oldItem == newItem
        }

        override fun areContentsTheSame(oldItem: String, newItem: String): Boolean {
            return oldItem.hashCode() == newItem.hashCode()
        }
    }
    private val differ: AsyncListDiffer<String> = AsyncListDiffer(this, diffCallback)

    var songs: List<String>
        get() = differ.currentList
        set(value) = differ.submitList(value)

    class SongSearchViewHolder(val binding: SearchResultRecomListItemBinding) :
        RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): SongSearchViewHolder {
        return SongSearchViewHolder(
            SearchResultRecomListItemBinding.inflate(
                LayoutInflater.from(
                    parent.context
                ), parent, false
            )
        )
    }

    private var onItemClickListener: ((String) -> Unit)? = null

    fun setItemClickListener(listener: (String) -> Unit) {
        onItemClickListener = listener
    }

    override fun getItemCount(): Int {
        return songs.size
    }

    override fun onBindViewHolder(holder: SongSearchViewHolder, position: Int) {
        val song = songs[position]

        if (song.isEmpty()) {
            return
        }

        holder.binding.apply {
            textViewSearchSuggest.text = song

            root.setOnClickListener {
                onItemClickListener?.let { click ->
                    click(song)
                }
            }
        }
    }


}