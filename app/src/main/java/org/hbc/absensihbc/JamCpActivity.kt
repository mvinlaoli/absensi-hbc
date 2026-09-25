package org.hbc.absensihbc

import android.app.AlertDialog
import android.os.Bundle
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import org.json.JSONObject

class JamCpActivity : AppCompatActivity() {
    private lateinit var spinnerNama: Spinner
    private lateinit var spinnerKegiatan: Spinner
    private lateinit var edtJam: EditText
    private lateinit var edtCp: EditText
    private lateinit var lblStatus: TextView
    private var listAnggota: List<JSONObject> = emptyList()
    private var listKegiatan: MutableList<String> = mutableListOf()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_jamcp)

        spinnerNama = findViewById(R.id.spinnerNama)
        spinnerKegiatan = findViewById(R.id.spinnerKegiatan)
        edtJam = findViewById(R.id.edtJam)
        edtCp = findViewById(R.id.edtCp)
        lblStatus = findViewById(R.id.lblStatusJamCp)

        ApiClient.listAnggota { list ->
            runOnUiThread {
                listAnggota = list
                val namaList = list.map { "${it.optString("nama")} - ${it.optString("nim")}" }
                spinnerNama.adapter = ArrayAdapter(this, android.R.layout.simple_spinner_dropdown_item, namaList)
            }
        }

        loadKegiatan()

        findViewById<Button>(R.id.btnSimpanJamCp).setOnClickListener {
            val pos = spinnerNama.selectedItemPosition
            if (pos < 0 || pos >= listAnggota.size) {
                lblStatus.text = "Pilih nama dulu"
                return@setOnClickListener
            }
            val nim = listAnggota[pos].optString("nim")
            val kegiatan = spinnerKegiatan.selectedItem?.toString() ?: ""
            val jam = edtJam.text.toString().toDoubleOrNull() ?: 0.0
            val cp = edtCp.text.toString().toIntOrNull() ?: 0

            if (kegiatan.startsWith("+")) {
                lblStatus.text = "Pilih kegiatan dulu"
                return@setOnClickListener
            }

            lblStatus.text = "Menyimpan..."
            ApiClient.catatJamCp(nim, kegiatan, jam, cp) { json ->
                runOnUiThread {
                    lblStatus.text = if (json != null && json.optBoolean("success")) {
                        "✅ ${json.optString("nama")} (+$jam jam, +$cp CP)"
                    } else {
                        "❌ Gagal: ${json?.optString("message") ?: "Tidak ada respon"}"
                    }
                }
            }
        }
    }

    private fun loadKegiatan() {
        listKegiatan = mutableListOf("Latihan HBC", "Natal", "Wisuda", "+ Tambah Kegiatan Baru")
        spinnerKegiatan.adapter = ArrayAdapter(this, android.R.layout.simple_spinner_dropdown_item, listKegiatan)
        spinnerKegiatan.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>?, view: android.view.View?, position: Int, id: Long) {
                if (listKegiatan[position] == "+ Tambah Kegiatan Baru") showTambahKegiatanDialog()
            }
            override fun onNothingSelected(parent: AdapterView<*>?) {}
        }
        ApiClient.daftarKegiatan { list ->
            if (list.isNotEmpty()) runOnUiThread {
                listKegiatan = (list + "+ Tambah Kegiatan Baru").toMutableList()
                spinnerKegiatan.adapter = ArrayAdapter(this, android.R.layout.simple_spinner_dropdown_item, listKegiatan)
            }
        }
    }

    private fun showTambahKegiatanDialog() {
        val input = EditText(this)
        input.hint = "Nama kegiatan baru"
        AlertDialog.Builder(this)
            .setTitle("Tambah Kegiatan")
            .setView(input)
            .setPositiveButton("Simpan") { _, _ ->
                val nama = input.text.toString().trim()
                if (nama.isNotEmpty()) {
                    ApiClient.tambahKegiatan(nama, "Acara") { json ->
                        runOnUiThread {
                            if (json != null && json.optBoolean("success")) {
                                Toast.makeText(this, "Kegiatan ditambahkan", Toast.LENGTH_SHORT).show()
                                loadKegiatan()
                            }
                        }
                    }
                }
            }
            .setNegativeButton("Batal", null)
            .show()
    }
}