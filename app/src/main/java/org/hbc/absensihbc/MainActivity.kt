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

        findViewById<ImageButton>(R.id.btnRekap).setOnClickListener {
            val url = "https://docs.google.com/spreadsheets/d/1nYchu61_ZknF8Ve3RvLRaNf1kMCi5x9GWUc8HX2ZvwE/edit"
            startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(url)))
        }

        loadInfoMinggu()
        loadStatistik()
    }

    override fun onResume() {
        super.onResume()
        loadInfoMinggu()
        loadStatistik()
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

    private fun loadStatistik() {
        val lblStat = findViewById<TextView>(R.id.lblStatistik)
        ApiClient.statistikBulanan { json ->
            runOnUiThread {
                if (json != null && json.optBoolean("success")) {
                    val hadir = json.optInt("hadir", 0)
                    val izin = json.optInt("izin", 0)
                    val sakit = json.optInt("sakit", 0)
                    val alpa = json.optInt("alpa", 0)
                    val persen = json.optInt("persen", 0)

                    var text = "📊 STATISTIK BULAN INI\n\n"
                    text += "✅ Hadir: $hadir\n"
                    text += "⚠️ Izin: $izin\n"
                    text += "💊 Sakit: $sakit\n"
                    text += "❌ Alpa: $alpa\n"
                    text += "📈 Persentase: $persen%\n\n"

                    val top3 = json.optJSONArray("top3")
                    if (top3 != null && top3.length() > 0) {
                        text += "🏆 TOP 3 RAJIN:\n"
                        for (i in 0 until top3.length()) {
                            val item = top3.getJSONObject(i)
                            text += "${i + 1}. ${item.optString("nama")} (${item.optInt("hadir")}x)\n"
                        }
                    }

                    lblStat.text = text.trim()
                } else {
                    lblStat.text = "Statistik: —"
                }
            }
        }
    }
}