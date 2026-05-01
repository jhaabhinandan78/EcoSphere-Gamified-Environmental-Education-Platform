package com.example.capstone

import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.capstone.databinding.ItemModuleBinding

class ModuleAdapter(
    private val moduleList: List<Module>,
    private val onItemClick: (Module) -> Unit
) : RecyclerView.Adapter<ModuleAdapter.ModuleViewHolder>() {

    inner class ModuleViewHolder(val binding: ItemModuleBinding) :
        RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ModuleViewHolder {
        val binding = ItemModuleBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return ModuleViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ModuleViewHolder, position: Int) {
        val module = moduleList[position]
        val rank = position + 1

        holder.binding.tvModuleTitle.text = module.title
        holder.binding.tvModuleDescription.text = module.description
        holder.binding.tvModulePoints.text = "⭐ ${module.points} pts"

        if (module.isCompleted) {
            // Show checkmark, hide number
            holder.binding.tvModuleNumber.visibility = View.GONE
            holder.binding.ivCompleted.visibility = View.VISIBLE
            holder.binding.cardCompletedBadge.visibility = View.VISIBLE
            // Green tint on icon container
            holder.binding.root.setCardBackgroundColor(Color.parseColor("#F9FFF9"))
        } else {
            // Show number, hide checkmark
            holder.binding.tvModuleNumber.visibility = View.VISIBLE
            holder.binding.tvModuleNumber.text = rank.toString()
            holder.binding.ivCompleted.visibility = View.GONE
            holder.binding.cardCompletedBadge.visibility = View.GONE
            holder.binding.root.setCardBackgroundColor(Color.WHITE)
        }

        holder.itemView.setOnClickListener {
            onItemClick(module)
        }
    }

    override fun getItemCount(): Int = moduleList.size
}
