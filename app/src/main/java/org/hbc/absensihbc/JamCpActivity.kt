package org.hbc.absensihbc

import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity

class JamCpActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_jamcp)

        val edtNim = findViewById<EditText>(R.id.edtNim)
        val edtKegiatan = findViewById<EditText>(R.id.edtKegiatan)
        val edtJam = findViewById<EditText>(R.id.edtJam)
        val edtCp = findViewById<EditText>(R.id.edtCp)
        val lblStatus = findViewById<TextView>(R.id.lblStatusJamCp)

        findViewById<Button>(R.id.btnSimpanJamCp).setOnClickListener {
            val nim = edtNim.text.toString().trim()
            val kegiatan = edtKegiatan.text.toString().trim()
            val jam = edtJam.text.toString().toDoubleOrNull() ?: 0.0
            val cp = edtCp.text.toString().toIntOrNull() ?: 0

            if (nim.isEmpty() || kegiatan.isEmpty()) {
                lblStatus.text = "NIM dan Jenis Kegiatan wajib diisi"
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
