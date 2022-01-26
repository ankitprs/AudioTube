package tech.apps.music.adapters

import android.graphics.Color
import android.graphics.drawable.GradientDrawable
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.AsyncListDiffer
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import tech.apps.music.databinding.ExploreListItemBinding
import tech.apps.music.model.ExploreModel

class ExploreAdapter : RecyclerView.Adapter<ExploreAdapter.ExploreViewHolder>() {

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

    class ExploreViewHolder(val binding: ExploreListItemBinding) :
        RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ExploreViewHolder {
        return ExploreViewHolder(
            ExploreListItemBinding.inflate(
                LayoutInflater.from(parent.context),
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


        holder.binding.apply {
            val gradientDrawable = GradientDrawable(
                GradientDrawable.Orientation.BL_TR,
                intArrayOf(
                    Color.parseColor(song.color1),
                    Color.parseColor(song.color2)
                )
            )
            gradientDrawable.cornerRadius = 0f

            //Set Gradient
            backgroundForExploreCoordinateView.background = gradientDrawable

            iconForExploreTopicTextView.text = song.text
            iconForExploreTopic.setImageResource(song.icon)

            root.setOnClickListener {
                onItemClickListener?.let { click ->
                    click(song)
                }
            }
        }
    }

}