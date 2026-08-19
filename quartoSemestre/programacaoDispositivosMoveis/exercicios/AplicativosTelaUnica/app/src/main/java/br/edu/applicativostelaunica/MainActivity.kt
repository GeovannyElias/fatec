package br.edu.applicativostelaunica

import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import br.edu.applicativostelaunica.databinding.ActivityMainBinding
import java.util.Locale

class MainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.btnSomar.setOnClickListener { calcularOperacao('+') }
        binding.btnSubtrair.setOnClickListener { calcularOperacao('-') }
        binding.btnMultiplicar.setOnClickListener { calcularOperacao('*') }
        binding.btnDividir.setOnClickListener { calcularOperacao('/') }
        binding.btnConverter.setOnClickListener { converterTemperatura() }
        binding.btnCalcularOhm.setOnClickListener { calcularOhm() }
    }

    private fun calcularOperacao(operacao: Char) {
        val primeiro = binding.etNumero1.text.toString().replace(',', '.').toDoubleOrNull()
        val segundo = binding.etNumero2.text.toString().replace(',', '.').toDoubleOrNull()
        if (primeiro == null || segundo == null) {
            mensagem("Digite os dois números para calcular.")
            return
        }
        if (operacao == '/' && segundo == 0.0) {
            mensagem("Não é possível dividir por zero.")
            return
        }
        val resultado = when (operacao) {
            '+' -> primeiro + segundo
            '-' -> primeiro - segundo
            '*' -> primeiro * segundo
            else -> primeiro / segundo
        }
        binding.tvResultadoOperacoes.text = "Resultado: ${formatar(resultado)}"
    }

    private fun converterTemperatura() {
        val celsius = binding.etCelsius.text.toString().replace(',', '.').toDoubleOrNull()
        if (celsius == null) {
            mensagem("Digite uma temperatura em Celsius.")
            return
        }
        val fahrenheit = celsius * 9 / 5 + 32
        binding.tvResultadoTemperatura.text = "${formatar(celsius)} °C = ${formatar(fahrenheit)} °F"
    }

    private fun calcularOhm() {
        val tensao = binding.etTensao.text.toString().replace(',', '.').toDoubleOrNull()
        val resistencia = binding.etResistencia.text.toString().replace(',', '.').toDoubleOrNull()
        val corrente = binding.etCorrente.text.toString().replace(',', '.').toDoubleOrNull()
        val preenchidos = listOf(tensao, resistencia, corrente).count { it != null }

        if (preenchidos != 2) {
            mensagem("Preencha exatamente dois campos para calcular o terceiro.")
            return
        }

        when {
            tensao != null && resistencia != null -> {
                if (resistencia == 0.0) return mensagem("A resistência não pode ser zero.")
                binding.tvResultadoOhm.text = "Corrente (I): ${formatar(tensao / resistencia)} A"
            }
            tensao != null && corrente != null -> {
                if (corrente == 0.0) return mensagem("A corrente não pode ser zero.")
                binding.tvResultadoOhm.text = "Resistência (R): ${formatar(tensao / corrente)} Ω"
            }
            resistencia != null && corrente != null ->
                binding.tvResultadoOhm.text = "Tensão (V): ${formatar(resistencia * corrente)} V"
        }
    }

    private fun formatar(valor: Double): String = String.format(Locale("pt", "BR"), "%.2f", valor)

    private fun mensagem(texto: String) = Toast.makeText(this, texto, Toast.LENGTH_SHORT).show()
}
