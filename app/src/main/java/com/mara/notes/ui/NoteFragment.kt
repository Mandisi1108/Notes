package com.mara.notes.ui

import android.os.Bundle
import android.view.*
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.navigation.Navigation
import com.mara.notes.R
import com.mara.notes.room.Note
import com.mara.notes.room.NoteDatabase
import kotlinx.android.synthetic.main.fragment_note.*
import kotlinx.coroutines.launch

class NoteFragment : BaseFragment() {
    private var note: Note? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        setHasOptionsMenu(true)
        return inflater.inflate(R.layout.fragment_note, container, false)
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        arguments?.let {
            note = NoteFragmentArgs.fromBundle(it).note
            titleEditText.setText(note?.title)
            bodyEditText.setText((note?.body))
        }
        saveButton.setOnClickListener{
            val noteTitle = titleEditText.text.toString().trim()
            val noteBody = bodyEditText.text.toString().trim()

            if (noteTitle.isNullOrEmpty()) {
                titleEditText.error = "Title required"
                return@setOnClickListener
            }

            if (noteBody.isNullOrEmpty()) {
                bodyEditText.error = "Body required"
                return@setOnClickListener
            }

            val newNote = Note(noteTitle, noteBody)

            launch {
                if (note == null) {
                    saveNote(newNote)
                } else {
                    updateNote(newNote)
                }
            }

            navigateBack()
        }
    }

    private suspend fun saveNote(note: Note) {
        context?.let {
            NoteDatabase(it).getNoteDao().addNote(note)
            Toast.makeText(it, "Note created", Toast.LENGTH_SHORT).show()
        }
    }

    private suspend fun updateNote(newNote: Note) {
        newNote.id = note!!.id
        context?.let {
            NoteDatabase(it).getNoteDao().updateNote(newNote)
            Toast.makeText(it, "Note updated", Toast.LENGTH_SHORT).show()
        }
    }

    private fun navigateBack() {
        val action = NoteFragmentDirections.actionSave()
        Navigation.findNavController(saveButton).navigate(action)
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        super.onCreateOptionsMenu(menu, inflater)
        inflater.inflate(R.menu.note_menu, menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when(item.itemId) {
            R.id.deleteNote -> {
                note?.let {
                    deleteNote()
                }
            }
        }
        return super.onOptionsItemSelected(item)
    }

    private fun deleteNote() {
        context?.let {
            AlertDialog.Builder(it).apply {
                setTitle("Are you sure")
                setMessage("You cannot undo this operation.")
                setPositiveButton("OK") {dialog, which ->
                    launch {
                        NoteDatabase(it).getNoteDao().deleteNote(note!!)
                        navigateBack()
                    }
                }
                setNegativeButton("Cancel") {dialog, which ->  }
                show()
            }
        }
    }
}
