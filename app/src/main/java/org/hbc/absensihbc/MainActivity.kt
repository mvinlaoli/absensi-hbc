package org.hbc.absensihbc

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.widget.ImageButton
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.cardview.widget.CardView

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        findViewById<CardView>(R.id.cardScan).setOnClickListener {
            startActivity(Intent(this, ScanActivity::class.java))
        }
        findViewById<CardView>(R.id.cardManual).setOnClickListener {
            startActivity(Intent(this, ManualActivity::class.java))
        }
        findViewById<CardView>(R.id.cardJamCp).setOnClickListener {
            startActivity(Intent(this, JamCpActivity::class.java))
        }

        // Tombol Rekap → buka Google Sheets
        findViewById<ImageButton>(R.id.btnRekap).setOnClickListener {
            val url = "https://docs.google.com/spreadsheets/d/1nYchu61_ZknF8Ve3RvLRaNf1kMCi5x9GWUc8HX2ZvwE/edit"
            startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(url)))
        }

        loadInfoMinggu()
    }

    override fun onResume() {
        super.onResume()
        loadInfoMinggu()
    }

    private fun loadInfoMinggu() {
        val lblWeekly = findViewById<TextView>(R.id.lblWeekly)
        ApiClient.infoMinggu { json ->
            runOnUiThread {
                if (json != null && json.optBoolean("success")) {
                    val hadir = json.optInt("hadir", 0)
                    val izin = json.optInt("izin", 0)
                    val sakit = json.optInt("sakit", 0)
                    val alpa = json.optInt("alpa", 0)
                    lblWeekly.text = "Minggu ini — Hadir: $hadir  Izin: $izin  Sakit: $sakit  Alpa: $alpa"
                } else {
                    lblWeekly.text = "Minggu ini: —"
                }
            }
        }
    }
}