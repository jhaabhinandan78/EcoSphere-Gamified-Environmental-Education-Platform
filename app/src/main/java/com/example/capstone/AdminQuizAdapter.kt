package com.example.capstone

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

class AdminQuizAdapter(
    private val questions: List<AdminQuizQuestion>,
    private val onEditClick: (AdminQuizQuestion) -> Unit,
    private val onDeleteClick: (AdminQuizQuestion) -> Unit
) : RecyclerView.Adapter<AdminQuizAdapter.QuizViewHolder>() {

    class QuizViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val tvModuleTitle: TextView = itemView.findViewById(R.id.tvModuleTitle)
        val tvQuestion: TextView = itemView.findViewById(R.id.tvQuestion)
        val tvOption1: TextView = itemView.findViewById(R.id.tvOption1)
        val tvOption2: TextView = itemView.findViewById(R.id.tvOption2)
        val tvOption3: TextView = itemView.findViewById(R.id.tvOption3)
        val tvOption4: TextView = itemView.findViewById(R.id.tvOption4)
        val tvCorrectAnswer: TextView = itemView.findViewById(R.id.tvCorrectAnswer)
        val btnEdit: Button = itemView.findViewById(R.id.btnEdit)
        val btnDelete: Button = itemView.findViewById(R.id.btnDelete)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): QuizViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_admin_quiz_question, parent, false)
        return QuizViewHolder(view)
    }

    override fun onBindViewHolder(holder: QuizViewHolder, position: Int) {
        val question = questions[position]
        val defaultColor = holder.itemView.context.getColor(android.R.color.darker_gray)
        val correctColor = holder.itemView.context.getColor(android.R.color.holo_green_dark)

        holder.tvModuleTitle.text = "📚 ${question.moduleTitle}"
        holder.tvQuestion.text = question.question
        holder.tvOption1.text = "A) ${question.option1}"
        holder.tvOption2.text = "B) ${question.option2}"
        holder.tvOption3.text = "C) ${question.option3}"
        holder.tvOption4.text = "D) ${question.option4}"
        holder.tvCorrectAnswer.text = "✓ Correct: ${question.correctAnswer}"

        // ✅ Always reset all option colors first to prevent RecyclerView color bleed
        holder.tvOption1.setTextColor(defaultColor)
        holder.tvOption2.setTextColor(defaultColor)
        holder.tvOption3.setTextColor(defaultColor)
        holder.tvOption4.setTextColor(defaultColor)

        // Then highlight only the correct one
        when (question.correctAnswer) {
            question.option1 -> holder.tvOption1.setTextColor(correctColor)
            question.option2 -> holder.tvOption2.setTextColor(correctColor)
            question.option3 -> holder.tvOption3.setTextColor(correctColor)
            question.option4 -> holder.tvOption4.setTextColor(correctColor)
        }

        holder.btnEdit.setOnClickListener { onEditClick(question) }
        holder.btnDelete.setOnClickListener { onDeleteClick(question) }
    }

    override fun getItemCount(): Int = questions.size
}
