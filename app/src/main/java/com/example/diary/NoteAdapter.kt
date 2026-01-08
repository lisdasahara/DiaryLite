package com.example.diary

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

class NoteAdapter(
    private val noteList: List<Note>,
    private val listener: NoteClickListener
) : RecyclerView.Adapter<NoteAdapter.NoteViewHolder>() {

    interface NoteClickListener {
        fun onNoteClick(note: Note)
        fun onNoteLongClick(note: Note): Boolean
    }

    class NoteViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val tvTitle: TextView = view.findViewById(R.id.tvItemTitle)
        val tvContent: TextView = view.findViewById(R.id.tvItemContent)
        val tvDate: TextView = view.findViewById(R.id.tvItemDate)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): NoteViewHolder {
        // Menggunakan layout activity_note sebagai tampilan per baris (item)
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.activity_note, parent, false)
        return NoteViewHolder(view)
    }

    override fun onBindViewHolder(holder: NoteViewHolder, position: Int) {
        val note = noteList[position]
        holder.tvTitle.text = note.title ?: "Tanpa Judul"
        holder.tvContent.text = note.content ?: ""
        holder.tvDate.text = note.getFormattedDate()

        holder.itemView.setOnClickListener {
            listener.onNoteClick(note)
        }

        holder.itemView.setOnLongClickListener {
            listener.onNoteLongClick(note)
        }
    }

    override fun getItemCount(): Int = noteList.size
}