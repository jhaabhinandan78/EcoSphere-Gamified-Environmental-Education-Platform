package com.example.capstone

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.capstone.databinding.ItemActivityFeedBinding
import com.example.capstone.models.ActivityFeed

class ActivityFeedAdapter(
    private val activities: List<ActivityFeed>
) : RecyclerView.Adapter<ActivityFeedAdapter.ActivityViewHolder>() {

    inner class ActivityViewHolder(private val binding: ItemActivityFeedBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(activity: ActivityFeed) {
            binding.tvActivityIcon.text = activity.getActivityIcon()
            binding.tvActivityDescription.text = activity.getActivityDescription()
            binding.tvActivityTime.text = LiveActivityFeedActivity.getRelativeTime(activity.timestamp)
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ActivityViewHolder {
        val binding = ItemActivityFeedBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return ActivityViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ActivityViewHolder, position: Int) {
        holder.bind(activities[position])
    }

    override fun getItemCount() = activities.size
}
