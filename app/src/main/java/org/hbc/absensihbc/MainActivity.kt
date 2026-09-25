package org.hbc.absensihbc

import android.content.Intent
import android.os.Bundle
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

        loadMingguIni()
    }

    private fun loadMingguIni() {
        val lbl = findViewById<TextView>(R.id.lblWeekly)
        lbl.text = "Memuat..."
        ApiClient.hitungMingguIni { json ->
            runOnUiThread {
                lbl.text = if (json != null && json.optBoolean("success")) {
                    "Minggu ini: ${json.optInt("hadir")}/${json.optInt("total")} hadir"
                } else {
                    "Minggu ini: -/- hadir"
                }
            }
        }
    }

    override fun onResume() {
        super.onResume()
        loadMingguIni()
    }
}