package org.hbc.absensihbc

import android.os.Bundle
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.EditText
import android.widget.Spinner
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity

class ManualActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_manual)

        val edtNim = findViewById<EditText>(R.id.edtNim)
        val spinnerKegiatan = findViewById<Spinner>(R.id.spinnerKegiatan)
        val spinnerStatus = findViewById<Spinner>(R.id.spinnerStatus)
        val lblStatus = findViewById<TextView>(R.id.lblStatusManual)

        val defaultKegiatan = listOf("Latihan HBC", "Natal", "Wisuda")
        spinnerKegiatan.adapter = ArrayAdapter(this, android.R.layout.simple_spinner_dropdown_item, defaultKegiatan)
        ApiClient.daftarKegiatan { list ->
            if (list.isNotEmpty()) runOnUiThread {
                spinnerKegiatan.adapter = ArrayAdapter(this, android.R.layout.simple_spinner_dropdown_item, list)
            }
        }

        spinnerStatus.adapter = ArrayAdapter(
            this, android.R.layout.simple_spinner_dropdown_item,
            listOf("Izin", "Sakit", "Alpa")
        )

        findViewById<Button>(R.id.btnSimpanManual).setOnClickListener {
            val nim = edtNim.text.toString().trim()
            val kegiatan = spinnerKegiatan.selectedItem?.toString() ?: ""
            val status = spinnerStatus.selectedItem?.toString() ?: "Izin"

            if (nim.isEmpty() || kegiatan.isEmpty()) {
                lblStatus.text = "NIM wajib diisi"
                return@setOnClickListener
            }

            lblStatus.text = "Menyimpan..."
            ApiClient.catatAbsensi(nim, kegiatan, status) { json ->
                runOnUiThread {
                    lblStatus.text = if (json != null && json.optBoolean("success")) {
                        "$status tercatat untuk ${json.optString("nama")}"
                    } else {
                        "Gagal: ${json?.optString("message") ?: "Tidak ada respon dari server"}"
                    }
                }
            }
        }
    }
}