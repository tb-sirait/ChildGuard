package com.example.gpstracker.server

data class PutOrtu
    (val status: String,
     val message: String,
     val `data`: List<update_ortu_item>)
