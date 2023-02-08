package com.example.bmta.di

import com.example.bmta.viewmodel.NoteViewModel
import org.koin.android.viewmodel.dsl.viewModel
import org.koin.dsl.module


val viewModelModule = module {
    viewModel {
        NoteViewModel(get())
    }
}