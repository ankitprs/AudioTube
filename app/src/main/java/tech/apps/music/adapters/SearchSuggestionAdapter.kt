package tech.apps.music.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.AsyncListDiffer
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import kotlinx.android.synthetic.main.search_result_recom_list_item.view.*
import tech.apps.music.R

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

    class SongSearchViewHolder(listItem: View) : RecyclerView.ViewHolder(listItem)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): SongSearchViewHolder {
        return SongSearchViewHolder(
            LayoutInflater.from(parent.context).inflate(
                R.layout.search_result_recom_list_item,
                parent,
                false
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

        holder.itemView.apply {
            textViewSearchSuggest.text = song

            setOnClickListener {
                onItemClickListener?.let { click ->
                    click(song)
                }
            }
        }
    }


}