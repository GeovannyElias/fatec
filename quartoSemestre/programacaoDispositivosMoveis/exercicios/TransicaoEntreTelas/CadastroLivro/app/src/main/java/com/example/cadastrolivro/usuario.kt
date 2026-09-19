package com.example.cadastrolivro

import android.os.Bundle
import android.widget.TextView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.google.android.material.floatingactionbutton.FloatingActionButton

class usuario : AppCompatActivity(R.layout.activity_usuario) {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        val txttitulo = findViewById<TextView>(R.id.txttitulo)
        val txtautor = findViewById<TextView>(R.id.txtautor)
        val fltvoltar = findViewById<FloatingActionButton>(R.id.flt_voltar)

        val tituloLivro = intent.getStringExtra("titulo_livro")
        txttitulo.setText(" Livro $tituloLivro")

        val autor = intent.getStringExtra("Autor")
        txtautor.setText(" Autor $autor")

        fltvoltar.setOnClickListener {
            finish()
        }




    }
}