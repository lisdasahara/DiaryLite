package com.example.diary

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.diary.databinding.ActivityMainBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*

class MainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding
    private lateinit var database: DatabaseReference
    private lateinit var adapter: NoteAdapter
    private val noteList = mutableListOf<Note>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Sembunyikan ActionBar bawaan agar hanya Toolbar ungu yang terlihat
        supportActionBar?.hide()

        val user = FirebaseAuth.getInstance().currentUser
        if (user == null) {
            startActivity(Intent(this, LoginActivity::class.java))
            finish()
            return
        }

        database = FirebaseDatabase.getInstance().getReference("Notes").child(user.uid)

        setupRecyclerView()
        fetchNotes()

        binding.fabAdd.setOnClickListener { showAddDialog() }
        binding.btnLogout.setOnClickListener {
            FirebaseAuth.getInstance().signOut()
            startActivity(Intent(this, LoginActivity::class.java))
            finish()
        }
    }

    private fun setupRecyclerView() {
        binding.rvNotes.layoutManager = LinearLayoutManager(this)
        adapter = NoteAdapter(noteList, object : NoteAdapter.NoteClickListener {
            override fun onNoteClick(note: Note) {
                // UPDATE: Klik biasa untuk mengedit
                showEditDialog(note)
            }
            override fun onNoteLongClick(note: Note): Boolean {
                // DELETE: Tekan lama untuk menghapus
                showDeleteDialog(note)
                return true
            }
        })
        binding.rvNotes.adapter = adapter
    }

    private fun fetchNotes() {
        database.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                noteList.clear()
                for (data in snapshot.children) {
                    data.getValue(Note::class.java)?.let { noteList.add(it) }
                }
                noteList.reverse()
                adapter.notifyDataSetChanged()
            }
            override fun onCancelled(error: DatabaseError) {
                Toast.makeText(this@MainActivity, "Error: ${error.message}", Toast.LENGTH_SHORT).show()
            }
        })
    }

    private fun showAddDialog() {
        val view = LayoutInflater.from(this).inflate(R.layout.dialog_add_note, null)
        val etTitle = view.findViewById<EditText>(R.id.etNoteTitle)
        val etContent = view.findViewById<EditText>(R.id.etNoteContent)

        AlertDialog.Builder(this)
            .setTitle("Tambah Catatan")
            .setView(view)
            .setPositiveButton("Simpan") { _, _ ->
                val title = etTitle.text.toString()
                val content = etContent.text.toString()
                if (title.isNotEmpty()) {
                    val id = database.push().key ?: return@setPositiveButton
                    val note = Note(id, title, content, System.currentTimeMillis())
                    database.child(id).setValue(note) // CREATE
                }
            }
            .setNegativeButton("Batal", null)
            .show()
    }

    private fun showEditDialog(note: Note) {
        val view = LayoutInflater.from(this).inflate(R.layout.dialog_add_note, null)
        val etTitle = view.findViewById<EditText>(R.id.etNoteTitle)
        val etContent = view.findViewById<EditText>(R.id.etNoteContent)

        etTitle.setText(note.title)
        etContent.setText(note.content)

        AlertDialog.Builder(this)
            .setTitle("Edit Catatan")
            .setView(view)
            .setPositiveButton("Update") { _, _ ->
                val newTitle = etTitle.text.toString()
                val newContent = etContent.text.toString()
                if (newTitle.isNotEmpty()) {
                    val updatedNote = Note(note.id, newTitle, newContent, System.currentTimeMillis())
                    note.id?.let { database.child(it).setValue(updatedNote) } // UPDATE
                }
            }
            .setNegativeButton("Batal", null)
            .show()
    }

    private fun showDeleteDialog(note: Note) {
        AlertDialog.Builder(this)
            .setTitle("Hapus Catatan")
            .setMessage("Hapus '${note.title}'?")
            .setPositiveButton("Hapus") { _, _ ->
                note.id?.let {
                    database.child(it).removeValue() // DELETE
                        .addOnSuccessListener {
                            Toast.makeText(this, "Berhasil dihapus", Toast.LENGTH_SHORT).show()
                        }
                }
            }
            .setNegativeButton("Batal", null)
            .show()
    }
}