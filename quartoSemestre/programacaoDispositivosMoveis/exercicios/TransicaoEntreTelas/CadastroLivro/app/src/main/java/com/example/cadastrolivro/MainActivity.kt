package com.example.cadastrolivro

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.google.android.material.floatingactionbutton.FloatingActionButton

class MainActivity : AppCompatActivity(R.layout.activity_main) {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        var edttitulo = findViewById<EditText>(R.id.edt_titulo)
        var edtautor = findViewById<EditText>(R.id.edt_autor)
        var btnenviar = findViewById<Button>(R.id.btn_enviar)
        var btnavancar = findViewById<FloatingActionButton>(R.id.btn_resultado)

        btnenviar.setOnClickListener {
            val titulo = edttitulo.text.toString()
            val autor = edtautor.text.toString()
            Toast.makeText(this,"Salvo Com Sucesso!",Toast.LENGTH_SHORT).show()
        }

        btnavancar.setOnClickListener {
            val intent = Intent(this, usuario::class.java)
            intent.putExtra("titulo_livro", edttitulo.text.toString())
            intent.putExtra("Autor", edtautor.text.toString())
            startActivity(intent)
        }

    }
}