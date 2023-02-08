package com.example.bmta.utils

import com.example.bmta.databinding.ItemBinding
import com.example.bmta.model.Note

interface INote {
    fun onClick(note : Note, position : Int, view : ItemBinding)
    fun onLongClick(note : Note, position : Int, view : ItemBinding)
}