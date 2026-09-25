package org.hbc.absensihbc

import android.os.Bundle
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.EditText
import android.widget.Spinner
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity

class JamCpActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_jamcp)

        val edtNim = findViewById<EditText>(R.id.edtNim)
        val spinnerKegiatan = findViewById<Spinner>(R.id.edtKegiatan)
        val edtJam = findViewById<EditText>(R.id.edtJam)
        val edtCp = findViewById<EditText>(R.id.edtCp)
        val lblStatus = findViewById<TextView>(R.id.lblStatusJamCp)

        val defaultKegiatan = listOf("Latihan HBC", "Natal", "Wisuda")
        spinnerKegiatan.adapter = ArrayAdapter(this, android.R.layout.simple_spinner_dropdown_item, defaultKegiatan)
        ApiClient.daftarKegiatan { list ->
            if (list.isNotEmpty()) runOnUiThread {
                spinnerKegiatan.adapter = ArrayAdapter(this, android.R.layout.simple_spinner_dropdown_item, list)
            }
        }

        findViewById<Button>(R.id.btnSimpanJamCp).setOnClickListener {
            val nim = edtNim.text.toString().trim()
            val kegiatan = spinnerKegiatan.selectedItem?.toString() ?: ""
            val jam = edtJam.text.toString().toDoubleOrNull() ?: 0.0
            val cp = edtCp.text.toString().toIntOrNull() ?: 0

            if (nim.isEmpty() || kegiatan.isEmpty()) {
                lblStatus.text = "NIM wajib diisi"
                return@setOnClickListener
            }

            lblStatus.text = "Menyimpan..."
            ApiClient.catatJamCp(nim, kegiatan, jam, cp) { json ->
                runOnUiThread {
                    lblStatus.text = if (json != null && json.optBoolean("success")) {
                        "Tersimpan: ${json.optString("nama")} (+$jam jam, +$cp CP)"
                    } else {
                        "Gagal: ${json?.optString("message") ?: "Tidak ada respon dari server"}"
                    }
                }
            }
        }
    }
}