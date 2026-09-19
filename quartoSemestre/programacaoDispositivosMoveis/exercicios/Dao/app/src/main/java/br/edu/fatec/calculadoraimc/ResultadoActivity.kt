package br.edu.fatec.calculadoraimc

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import br.edu.fatec.calculadoraimc.databinding.ActivityResultadoBinding
import java.util.Locale

class ResultadoActivity : AppCompatActivity() {

    private lateinit var binding: ActivityResultadoBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityResultadoBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val imc = intent.getDoubleExtra(EXTRA_IMC, 0.0)
        exibirResultado(imc)
        binding.btnNovaConsulta.setOnClickListener { finish() }
    }

    private fun exibirResultado(imc: Double) {
        val imcFormatado = String.format(Locale("pt", "BR"), "%.2f", imc)
        val classificacao = when {
            imc < 18.5 -> getString(R.string.abaixo_do_peso)
            imc < 25.0 -> getString(R.string.peso_ideal)
            imc < 30.0 -> getString(R.string.sobrepeso)
            else -> getString(R.string.obesidade)
        }

        binding.txtImc.text = getString(R.string.imc_resultado, imcFormatado)
        binding.txtClassificacao.text = getString(R.string.classificacao, classificacao)
    }

    companion object {
        const val EXTRA_IMC = "imc"
    }
}
