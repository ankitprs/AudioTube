package tech.apps.music.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.AsyncListDiffer
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.RequestManager
import kotlinx.android.synthetic.main.explore_list_item.view.*
import tech.apps.music.R
import tech.apps.music.model.ExploreModel
import javax.inject.Inject

class ExploreAdapter @Inject constructor(
    private val glide: RequestManager
) : RecyclerView.Adapter<ExploreAdapter.ExploreViewHolder>() {

    private val diffCallback = object : DiffUtil.ItemCallback<ExploreModel>() {
        override fun areItemsTheSame(oldItem: ExploreModel, newItem: ExploreModel): Boolean {
            return oldItem.keyword == newItem.keyword
        }

        override fun areContentsTheSame(oldItem: ExploreModel, newItem: ExploreModel): Boolean {
            return oldItem.hashCode() == newItem.hashCode()
        }
    }
    private val differ: AsyncListDiffer<ExploreModel> = AsyncListDiffer(this, diffCallback)

    var songs: List<ExploreModel>
        get() = differ.currentList
        set(value) = differ.submitList(value)

    class ExploreViewHolder(listItem: View) : RecyclerView.ViewHolder(listItem)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ExploreViewHolder {
        return ExploreViewHolder(
            LayoutInflater.from(parent.context).inflate(
                R.layout.explore_list_item,
                parent,
                false
            )
        )
    }

    private var onItemClickListener: ((ExploreModel) -> Unit)? = null

    fun setItemClickListener(listener: (ExploreModel) -> Unit) {
        onItemClickListener = listener
    }

    override fun getItemCount(): Int {
        return songs.size
    }

    override fun onBindViewHolder(holder: ExploreViewHolder, position: Int) {
        val song = songs[position]

        holder.itemView.apply {
            iconForExploreTopicTextView.text = song.text
            iconForExploreTopic.setImageResource(song.icon)
            setOnClickListener {
                onItemClickListener?.let { click ->
                    click(song)
                }
            }
        }
    }

}