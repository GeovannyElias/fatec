package br.edu.fatecpg.appkotlin

import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.google.android.material.button.MaterialButton
import com.google.android.material.textfield.TextInputEditText
import com.google.android.material.textfield.TextInputLayout
import com.google.firebase.Firebase
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.firestore

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

        val db = Firebase.firestore

        val layoutNome = findViewById<TextInputLayout>(R.id.layoutNome)
        val layoutCpf = findViewById<TextInputLayout>(R.id.layoutCpf)
        val edtNome = findViewById<TextInputEditText>(R.id.edtNome)
        val edtCpf = findViewById<TextInputEditText>(R.id.edtCpf)
        val btnCadastrar = findViewById<MaterialButton>(R.id.btnCadastrar)
        val btnLimpar = findViewById<MaterialButton>(R.id.btnLimpar)

        fun limparCampos() {
            edtNome.text?.clear()
            edtCpf.text?.clear()
            layoutNome.error = null
            layoutCpf.error = null
            edtNome.requestFocus()
        }

        btnLimpar.setOnClickListener { limparCampos() }

        btnCadastrar.setOnClickListener {
            val nome = edtNome.text.toString().trim()
            val cpf = edtCpf.text.toString().filter { it.isDigit() }

            layoutNome.error = if (nome.isEmpty()) getString(R.string.erro_nome) else null
            layoutCpf.error = if (cpf.length != 11) getString(R.string.erro_cpf) else null
            if (nome.isEmpty() || cpf.length != 11) return@setOnClickListener

            val cliente = hashMapOf(
                "nome" to nome,
                "cpf" to cpf,
                "dt_compra" to FieldValue.serverTimestamp()
            )

            btnCadastrar.isEnabled = false

            // Usa o CPF como nome do document, evitando clientes duplicados
            db.collection("colecao_cliente")
                .document(cpf)
                .set(cliente)
                .addOnCompleteListener { task ->
                    btnCadastrar.isEnabled = true
                    if (task.isSuccessful) {
                        Log.d("FIRESTORE", "SUCESSO!")
                        Toast.makeText(this, R.string.msg_sucesso, Toast.LENGTH_SHORT).show()
                        limparCampos()
                    } else {
                        Log.d("FIRESTORE", "ERROR ${task.exception}")
                        Toast.makeText(
                            this,
                            getString(R.string.msg_erro, task.exception?.message),
                            Toast.LENGTH_LONG
                        ).show()
                    }
                }
        }
    }
}
