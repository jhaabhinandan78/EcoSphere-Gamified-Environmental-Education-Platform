package com.example.capstone

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.capstone.databinding.ItemChallengeBinding

class ChallengeAdapter(
    private val list: List<Challenge>,
    private val onClick: (Challenge) -> Unit
) : RecyclerView.Adapter<ChallengeAdapter.VH>() {

    inner class VH(val binding: ItemChallengeBinding) : RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VH {
        val binding = ItemChallengeBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return VH(binding)
    }

    override fun onBindViewHolder(holder: VH, position: Int) {
        val c = list[position]

        holder.binding.tvChallengeTitle.text = c.title
        holder.binding.tvChallengeDesc.text = c.description
        holder.binding.tvPoints.text = "${c.points}"

        when {
            c.submissionStatus == "approved" -> {
                // Approved: show green completed badge, not clickable
                holder.binding.tvStatus.visibility = View.VISIBLE
                holder.binding.tvStatus.text = "✅ Completed"
                holder.binding.tvStatus.setTextColor(0xFF2E7D32.toInt())
                holder.itemView.alpha = 0.85f
                holder.itemView.isClickable = false
                holder.itemView.isEnabled = false
            }
            c.submissionStatus == "pending" -> {
                // Pending: show orange awaiting badge, not clickable
                holder.binding.tvStatus.visibility = View.VISIBLE
                holder.binding.tvStatus.text = "⏳ Pending Review"
                holder.binding.tvStatus.setTextColor(0xFFE65100.toInt())
                holder.itemView.alpha = 0.85f
                holder.itemView.isClickable = true // allow click to see status dialog
                holder.itemView.isEnabled = true
                holder.itemView.setOnClickListener { onClick(c) }
            }
            c.submissionStatus == "rejected" -> {
                // Rejected: show red badge, allow resubmission
                holder.binding.tvStatus.visibility = View.VISIBLE
                holder.binding.tvStatus.text = "❌ Rejected — Tap to resubmit"
                holder.binding.tvStatus.setTextColor(0xFFB71C1C.toInt())
                holder.itemView.alpha = 1f
                holder.itemView.isClickable = true
                holder.itemView.isEnabled = true
                holder.itemView.setOnClickListener { onClick(c) }
            }
            else -> {
                // Not submitted yet
                holder.binding.tvStatus.visibility = View.GONE
                holder.itemView.alpha = 1f
                holder.itemView.isClickable = true
                holder.itemView.isEnabled = true
                holder.itemView.setOnClickListener { onClick(c) }
            }
        }
    }

    override fun getItemCount(): Int = list.size
}
