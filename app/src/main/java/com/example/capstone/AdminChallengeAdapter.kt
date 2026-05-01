package com.example.capstone

import android.content.res.ColorStateList
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.card.MaterialCardView

class AdminChallengeAdapter(
    private val challenges: List<AdminChallenge>,
    private val onEditClick: (AdminChallenge) -> Unit,
    private val onDeleteClick: (AdminChallenge) -> Unit,
    private val onToggleActive: (AdminChallenge) -> Unit
) : RecyclerView.Adapter<AdminChallengeAdapter.ChallengeViewHolder>() {

    class ChallengeViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val card: MaterialCardView = itemView.findViewById(R.id.cardChallenge)
        val tvTitle: TextView = itemView.findViewById(R.id.tvTitle)
        val tvDescription: TextView = itemView.findViewById(R.id.tvDescription)
        val tvPoints: TextView = itemView.findViewById(R.id.tvPoints)
        val tvType: TextView = itemView.findViewById(R.id.tvType)
        val tvStatus: TextView = itemView.findViewById(R.id.tvStatus)
        val switchActive: android.widget.Switch = itemView.findViewById(R.id.switchActive)
        val btnEdit: Button = itemView.findViewById(R.id.btnEdit)
        val btnDelete: Button = itemView.findViewById(R.id.btnDelete)
        val statusIndicator: View = itemView.findViewById(R.id.statusIndicator)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ChallengeViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_admin_challenge, parent, false)
        return ChallengeViewHolder(view)
    }

    override fun onBindViewHolder(holder: ChallengeViewHolder, position: Int) {
        val challenge = challenges[position]
        val ctx = holder.itemView.context

        holder.tvTitle.text = challenge.title
        holder.tvDescription.text = challenge.description
        holder.tvPoints.text = "${challenge.points} pts"
        holder.tvType.text = challenge.type.uppercase()

        // Status badge
        if (challenge.active) {
            holder.tvStatus.text = "● ACTIVE"
            holder.tvStatus.setTextColor(ctx.getColor(android.R.color.holo_green_dark))
            holder.statusIndicator.setBackgroundColor(ctx.getColor(android.R.color.holo_green_dark))
            // Use MaterialCardView stroke for active state
            holder.card.strokeColor = 0xFF4CAF50.toInt()
            holder.card.strokeWidth = 2
        } else {
            holder.tvStatus.text = "● INACTIVE"
            holder.tvStatus.setTextColor(ctx.getColor(android.R.color.holo_red_dark))
            holder.statusIndicator.setBackgroundColor(ctx.getColor(android.R.color.holo_red_dark))
            holder.card.strokeColor = 0xFFBDBDBD.toInt()
            holder.card.strokeWidth = 1
        }

        // Dim inactive cards slightly
        holder.card.alpha = if (challenge.active) 1.0f else 0.75f

        // Disable switch listener before setting state to avoid triggering during bind
        holder.switchActive.setOnCheckedChangeListener(null)
        holder.switchActive.isChecked = challenge.active
        holder.switchActive.setOnCheckedChangeListener { _, _ ->
            onToggleActive(challenge)
        }

        holder.btnEdit.setOnClickListener { onEditClick(challenge) }
        holder.btnDelete.setOnClickListener { onDeleteClick(challenge) }
    }

    override fun getItemCount(): Int = challenges.size
}
