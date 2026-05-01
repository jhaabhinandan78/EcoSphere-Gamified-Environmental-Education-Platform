package com.example.capstone

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.button.MaterialButton
import com.google.firebase.Timestamp
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.concurrent.TimeUnit

/**
 * Data class for pending teacher information
 */
data class PendingTeacher(
    val uid: String = "",
    val name: String = "",
    val email: String = "",
    val phone: String = "",
    val dob: String = "",
    val gender: String = "",
    val schoolId: String = "",
    val schoolCode: String = "",
    val createdAt: Timestamp? = null
)

/**
 * RecyclerView adapter for displaying pending teachers
 */
class PendingTeacherAdapter(
    private var teachers: List<PendingTeacher>,
    private val onApprove: (PendingTeacher) -> Unit,
    private val onReject: (PendingTeacher) -> Unit
) : RecyclerView.Adapter<PendingTeacherAdapter.PendingTeacherViewHolder>() {

    inner class PendingTeacherViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val tvTeacherName: TextView = itemView.findViewById(R.id.tvTeacherName)
        val tvTeacherEmail: TextView = itemView.findViewById(R.id.tvTeacherEmail)
        val tvTeacherPhone: TextView = itemView.findViewById(R.id.tvTeacherPhone)
        val tvTeacherDob: TextView = itemView.findViewById(R.id.tvTeacherDob)
        val tvRegistrationDate: TextView = itemView.findViewById(R.id.tvRegistrationDate)
        val btnApprove: MaterialButton = itemView.findViewById(R.id.btnApprove)
        val btnReject: MaterialButton = itemView.findViewById(R.id.btnReject)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PendingTeacherViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_pending_teacher, parent, false)
        return PendingTeacherViewHolder(view)
    }

    override fun onBindViewHolder(holder: PendingTeacherViewHolder, position: Int) {
        val teacher = teachers[position]

        holder.tvTeacherName.text = teacher.name
        holder.tvTeacherEmail.text = teacher.email
        holder.tvTeacherPhone.text = teacher.phone.ifEmpty { "Not provided" }
        holder.tvTeacherDob.text = "DOB: ${teacher.dob}"
        
        // Format registration date
        val registrationDate = teacher.createdAt?.toDate()
        if (registrationDate != null) {
            holder.tvRegistrationDate.text = getTimeAgo(registrationDate)
        } else {
            holder.tvRegistrationDate.text = "Registered recently"
        }
        
        // Approve button
        holder.btnApprove.setOnClickListener {
            onApprove(teacher)
        }
        
        // Reject button
        holder.btnReject.setOnClickListener {
            onReject(teacher)
        }
    }

    override fun getItemCount(): Int = teachers.size

    /**
     * Update the list of pending teachers
     */
    fun updateTeachers(newTeachers: List<PendingTeacher>) {
        teachers = newTeachers
        notifyDataSetChanged()
    }
    
    /**
     * Remove a teacher from the list (after approval/rejection)
     */
    fun removeTeacher(teacher: PendingTeacher) {
        val index = teachers.indexOf(teacher)
        if (index != -1) {
            teachers = teachers.toMutableList().apply { removeAt(index) }
            notifyItemRemoved(index)
        }
    }
    
    /**
     * Format timestamp to "X days ago" format
     */
    private fun getTimeAgo(date: Date): String {
        val now = System.currentTimeMillis()
        val diff = now - date.time
        
        return when {
            diff < TimeUnit.MINUTES.toMillis(1) -> "Registered just now"
            diff < TimeUnit.HOURS.toMillis(1) -> {
                val minutes = TimeUnit.MILLISECONDS.toMinutes(diff)
                "Registered $minutes ${if (minutes == 1L) "minute" else "minutes"} ago"
            }
            diff < TimeUnit.DAYS.toMillis(1) -> {
                val hours = TimeUnit.MILLISECONDS.toHours(diff)
                "Registered $hours ${if (hours == 1L) "hour" else "hours"} ago"
            }
            diff < TimeUnit.DAYS.toMillis(7) -> {
                val days = TimeUnit.MILLISECONDS.toDays(diff)
                "Registered $days ${if (days == 1L) "day" else "days"} ago"
            }
            else -> {
                val dateFormat = SimpleDateFormat("MMM dd, yyyy", Locale.getDefault())
                "Registered ${dateFormat.format(date)}"
            }
        }
    }
}
