package com.example.capstone

import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.card.MaterialCardView
import java.text.SimpleDateFormat
import java.util.*

class InAppNotificationAdapter(
    private val onNotificationClick: (InAppNotification) -> Unit
) : ListAdapter<InAppNotification, InAppNotificationAdapter.NotificationViewHolder>(NotificationDiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): NotificationViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_in_app_notification, parent, false)
        return NotificationViewHolder(view)
    }

    override fun onBindViewHolder(holder: NotificationViewHolder, position: Int) {
        holder.bind(getItem(position), onNotificationClick)
    }

    class NotificationViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val tvTitle: TextView = itemView.findViewById(R.id.tvNotificationTitle)
        private val tvMessage: TextView = itemView.findViewById(R.id.tvNotificationMessage)
        private val tvTime: TextView = itemView.findViewById(R.id.tvNotificationTime)
        private val viewUnreadIndicator: View = itemView.findViewById(R.id.viewUnreadIndicator)
        private val card = itemView as MaterialCardView

        fun bind(notification: InAppNotification, onClick: (InAppNotification) -> Unit) {
            tvTitle.text = notification.title
            tvMessage.text = notification.message
            tvTime.text = formatTime(notification.sentAt.toDate())

            if (notification.isRead) {
                // Read: no indicator, white background
                viewUnreadIndicator.visibility = View.GONE
                card.setCardBackgroundColor(Color.WHITE)
                tvTitle.setTextColor(Color.parseColor("#757575"))
            } else {
                // Unread: green dot, light blue tint background
                viewUnreadIndicator.visibility = View.VISIBLE
                card.setCardBackgroundColor(Color.parseColor("#F1F8FF"))
                tvTitle.setTextColor(Color.parseColor("#212121"))
            }

            itemView.setOnClickListener {
                onClick(notification)
            }
        }

        private fun formatTime(date: Date): String {
            val now = Date()
            val diff = now.time - date.time
            val seconds = diff / 1000
            val minutes = seconds / 60
            val hours = minutes / 60
            val days = hours / 24

            return when {
                seconds < 60 -> "Just now"
                minutes < 60 -> "$minutes min ago"
                hours < 24 -> "$hours hour${if (hours > 1) "s" else ""} ago"
                days < 7 -> "$days day${if (days > 1) "s" else ""} ago"
                else -> SimpleDateFormat("MMM dd, yyyy", Locale.getDefault()).format(date)
            }
        }
    }

    class NotificationDiffCallback : DiffUtil.ItemCallback<InAppNotification>() {
        override fun areItemsTheSame(oldItem: InAppNotification, newItem: InAppNotification) =
            oldItem.id == newItem.id

        override fun areContentsTheSame(oldItem: InAppNotification, newItem: InAppNotification) =
            oldItem == newItem
    }
}
