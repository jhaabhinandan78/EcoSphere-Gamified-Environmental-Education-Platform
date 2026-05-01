package com.example.capstone

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.capstone.models.Batch
import com.google.android.material.button.MaterialButton
import java.text.SimpleDateFormat
import java.util.Locale

/**
 * RecyclerView adapter for displaying batches in Batch Management screen
 */
class BatchAdapter(
    private var batches: List<Batch>,
    private val onViewDetailsClick: (Batch) -> Unit
) : RecyclerView.Adapter<BatchAdapter.BatchViewHolder>() {

    inner class BatchViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val tvBatchName: TextView = itemView.findViewById(R.id.tvBatchName)
        val tvAcademicYear: TextView = itemView.findViewById(R.id.tvAcademicYear)
        val tvStatus: TextView = itemView.findViewById(R.id.tvStatus)
        val tvStudentCount: TextView = itemView.findViewById(R.id.tvStudentCount)
        val tvCreatedDate: TextView = itemView.findViewById(R.id.tvCreatedDate)
        val btnViewDetails: MaterialButton = itemView.findViewById(R.id.btnViewDetails)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): BatchViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_batch, parent, false)
        return BatchViewHolder(view)
    }

    override fun onBindViewHolder(holder: BatchViewHolder, position: Int) {
        val batch = batches[position]

        holder.tvBatchName.text = batch.batchName
        holder.tvAcademicYear.text = "Academic Year: ${batch.academicYear}"
        
        // Status
        if (batch.isActive) {
            holder.tvStatus.text = "Active"
            holder.tvStatus.setTextColor(holder.itemView.context.getColor(android.R.color.holo_green_dark))
        } else {
            holder.tvStatus.text = "Inactive"
            holder.tvStatus.setTextColor(holder.itemView.context.getColor(android.R.color.darker_gray))
        }
        
        // Student count (will be updated in Phase 5)
        holder.tvStudentCount.text = "${batch.studentCount} Students"
        
        // Created date
        val createdDate = batch.createdAt?.toDate()
        if (createdDate != null) {
            val dateFormat = SimpleDateFormat("MMM dd, yyyy", Locale.getDefault())
            holder.tvCreatedDate.text = "Created ${dateFormat.format(createdDate)}"
        } else {
            holder.tvCreatedDate.text = "Created Recently"
        }
        
        // View details button
        holder.btnViewDetails.setOnClickListener {
            onViewDetailsClick(batch)
        }
    }

    override fun getItemCount(): Int = batches.size

    /**
     * Update the list of batches
     */
    fun updateBatches(newBatches: List<Batch>) {
        batches = newBatches
        notifyDataSetChanged()
    }
}
