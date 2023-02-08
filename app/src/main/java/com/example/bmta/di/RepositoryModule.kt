package com.example.bmta.di

import com.example.bmta.repository.NoteRepository
import org.koin.dsl.module

val repoModule = module {
    factory {
        NoteRepository(get())
    }
}