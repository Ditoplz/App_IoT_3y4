package com.example.iot_ev3

import android.content.Intent
import android.os.Bundle
import android.widget.ImageView
import android.widget.TextView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import cn.pedant.SweetAlert.SweetAlertDialog

class operador_main : AppCompatActivity() {
    private lateinit var btnVolver: ImageView
    private lateinit var imgLogs: ImageView
    private lateinit var txtLogs: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_operador_main)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        val idUsuario = intent.getStringExtra("id")
        val tipoUsuario = intent.getStringExtra("tipo")
        btnVolver = findViewById(R.id.img_operador_main_volver)
        imgLogs = findViewById(R.id.img_operador_main_logs)
        txtLogs = findViewById(R.id.txt_operador_main_logs)


        btnVolver.setOnClickListener {
            SweetAlertDialog(this, SweetAlertDialog.WARNING_TYPE)
                .setTitleText("¿Cerrar Sesión?")
                .setContentText("Será redirigido a la pantalla de login.")
                .setConfirmText("Sí")
                .setConfirmClickListener { sDialog ->
                    sDialog.dismissWithAnimation()
                    val intent = Intent(this, login::class.java)
                    intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                    startActivity(intent)
                }
                .setCancelText("No")
                .setCancelClickListener { sDialog -> sDialog.dismissWithAnimation() }
                .show()
        }

        imgLogs.setOnClickListener {
            val intent = Intent(this, logs::class.java)
            intent.putExtra("id", idUsuario)
            intent.putExtra("tipo", tipoUsuario)
            startActivity(intent)
        }

        txtLogs.setOnClickListener {
            val intent = Intent(this, logs::class.java)
            intent.putExtra("id", idUsuario)
            intent.putExtra("tipo", tipoUsuario)
            startActivity(intent)
        }
    }
}