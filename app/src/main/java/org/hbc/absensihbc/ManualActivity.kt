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
        val edtKegiatan = findViewById<EditText>(R.id.edtKegiatan)
        val spinnerStatus = findViewById<Spinner>(R.id.spinnerStatus)
        val lblStatus = findViewById<TextView>(R.id.lblStatusManual)

        spinnerStatus.adapter = ArrayAdapter(
            this, android.R.layout.simple_spinner_dropdown_item,
            listOf("Izin", "Sakit", "Alpa")
        )

        findViewById<Button>(R.id.btnSimpanManual).setOnClickListener {
            val nim = edtNim.text.toString().trim()
            val kegiatan = edtKegiatan.text.toString().trim()
            val status = spinnerStatus.selectedItem?.toString() ?: "Izin"

            if (nim.isEmpty() || kegiatan.isEmpty()) {
                lblStatus.text = "NIM dan Jenis Kegiatan wajib diisi"
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
