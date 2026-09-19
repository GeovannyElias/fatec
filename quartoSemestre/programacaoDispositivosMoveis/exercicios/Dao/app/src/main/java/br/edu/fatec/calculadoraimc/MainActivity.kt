package br.edu.fatec.calculadoraimc

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import br.edu.fatec.calculadoraimc.databinding.ActivityMainBinding

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.btnCalcular.setOnClickListener { calcularImc() }
    }

    private fun calcularImc() {
        val peso = binding.edtPeso.text.toString().trim().replace(',', '.').toDoubleOrNull()
        val altura = binding.edtAltura.text.toString().trim().replace(',', '.').toDoubleOrNull()

        if (peso == null || peso <= 0) {
            binding.edtPeso.error = getString(R.string.erro_peso)
            binding.edtPeso.requestFocus()
            return
        }

        if (altura == null || altura <= 0 || altura > 3) {
            binding.edtAltura.error = getString(R.string.erro_altura)
            binding.edtAltura.requestFocus()
            return
        }

        val imc = peso / (altura * altura)
        startActivity(Intent(this, ResultadoActivity::class.java).apply {
            putExtra(ResultadoActivity.EXTRA_IMC, imc)
        })
    }
}
