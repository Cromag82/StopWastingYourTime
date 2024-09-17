package com.neversaynever.timecounter

import android.app.AppOpsManager
import android.app.usage.UsageStatsManager
import android.content.Context
import android.content.Intent
import android.icu.util.Calendar
import android.os.Bundle
import android.os.Handler
import android.provider.Settings
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat


class MainActivity : AppCompatActivity() {

    private lateinit var mainText: TextView
    private var totalMinutesWastedToday: Long = 0
    private lateinit var handler: Handler
    private val delay: Long = 1000 // 1 segundo
    private lateinit var doSomething: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_main)

        initComponents()
        handler = Handler()

        // Verifica si el permiso de estadísticas de uso está habilitado
        if (!hasUsageStatsPermission()) {
            // Si no está habilitado, redirige al usuario a la configuración
            requestUsageStatsPermission()
        } else {
            // Si el permiso ya está habilitado, empieza a contar el tiempo en Instagram
            startCountingInstagramTime()
        }


        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
    }

    private fun hasUsageStatsPermission(): Boolean {
        val appOpsManager = getSystemService(Context.APP_OPS_SERVICE) as AppOpsManager
        val mode = appOpsManager.checkOpNoThrow(
            AppOpsManager.OPSTR_GET_USAGE_STATS,
            android.os.Process.myUid(), packageName
        )
        return mode == AppOpsManager.MODE_ALLOWED
    }

    private fun requestUsageStatsPermission() {
        val intent = Intent(Settings.ACTION_USAGE_ACCESS_SETTINGS)
        startActivity(intent)
        Toast.makeText(
            this,
            "Por favor, habilita el acceso a las estadísticas de uso.",
            Toast.LENGTH_LONG
        ).show()
    }

    private fun startCountingInstagramTime() {
        handler.postDelayed(object : Runnable {
            override fun run() {

                val minutesWasted =
                    getInstagramUsageToday() // llamamos a funcion que calcula el uso diario

                totalMinutesWastedToday = minutesWasted
                mainText.text =
                    "Has desperdiciado hoy: \n $totalMinutesWastedToday minutos en Instagram"
                doSomething.text = getApplicationContext().getString(R.string.doSomething)
                handler.postDelayed(this, delay) // actualiza cada segundo
            }
        }, delay)
    }

    private fun getInstagramUsageToday(): Long {
        val usageStatsManager = getSystemService(Context.USAGE_STATS_SERVICE) as UsageStatsManager
        val calendar = Calendar.getInstance()
        calendar.set(Calendar.HOUR_OF_DAY, 0)
        calendar.set(Calendar.MINUTE, 0)
        calendar.set(Calendar.SECOND, 0)
        val startTime = calendar.timeInMillis
        val endTime = System.currentTimeMillis()

        val stats = usageStatsManager.queryUsageStats(
            UsageStatsManager.INTERVAL_DAILY, startTime, endTime
        )

        var instagramUsageInMillis: Long = 0

        stats?.forEach { usageStats ->
            if (usageStats.packageName == "com.instagram.android") {
                instagramUsageInMillis += usageStats.totalTimeInForeground
            }
        }

        // Convertir de milisegundos a minutos
        return instagramUsageInMillis / (1000 * 60)
    }

    private fun initComponents() {
        mainText = findViewById(R.id.mainText)
        doSomething = findViewById(R.id.doSomething)
    }
}