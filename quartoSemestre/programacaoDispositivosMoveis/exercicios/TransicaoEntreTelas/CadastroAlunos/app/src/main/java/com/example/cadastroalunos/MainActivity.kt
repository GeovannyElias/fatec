package com.example.cadastroalunos

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_main)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        val campoNome = findViewById<EditText>(R.id.edt_nome)
        findViewById<Button>(R.id.btn_continuar).setOnClickListener {
            val nome = campoNome.text.toString().trim()

            if (nome.isEmpty()) {
                campoNome.error = getString(R.string.erro_nome_obrigatorio)
                campoNome.requestFocus()
                return@setOnClickListener
            }

            startActivity(Intent(this, MatriculaActivity::class.java).apply {
                putExtra(MatriculaActivity.EXTRA_NOME_ALUNO, nome)
            })
        }
    }
}
