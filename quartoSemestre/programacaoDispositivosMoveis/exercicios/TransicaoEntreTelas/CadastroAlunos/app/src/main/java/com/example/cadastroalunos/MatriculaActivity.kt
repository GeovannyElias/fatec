package com.example.cadastroalunos

import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import kotlin.random.Random

class MatriculaActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_matricula)

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { view, insets ->
            val barrasSistema = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            view.setPadding(barrasSistema.left, barrasSistema.top, barrasSistema.right, barrasSistema.bottom)
            insets
        }

        val nome = intent.getStringExtra(EXTRA_NOME_ALUNO).orEmpty()
        findViewById<TextView>(R.id.txt_nome_aluno).text = getString(R.string.nome_aluno, nome)

        val textoMatricula = findViewById<TextView>(R.id.txt_matricula)
        findViewById<Button>(R.id.btn_gerar_matricula).setOnClickListener {
            val matricula = Random.nextInt(100_000, 1_000_000)
            textoMatricula.text = getString(R.string.matricula_gerada, matricula)
        }
    }

    companion object {
        const val EXTRA_NOME_ALUNO = "nome_aluno"
    }
}
