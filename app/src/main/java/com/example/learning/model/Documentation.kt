package com.example.learning.model

data class DocSection(
    val id: String,
    val title: String,
    val category: String,
    val summary: String,
    val content: String,
    val tags: List<String>,
    val codeSnippet: String? = null
)
