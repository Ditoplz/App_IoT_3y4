package com.example.iot_ev3

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.android.volley.Request
import com.android.volley.RequestQueue
import com.android.volley.toolbox.JsonObjectRequest
import com.android.volley.toolbox.Volley
import org.json.JSONException

class login : AppCompatActivity() {

    private lateinit var usu: EditText
    private lateinit var clave: EditText
    private lateinit var btn: Button
    private lateinit var datos: RequestQueue

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_login)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        usu = findViewById(R.id.input_login_rut)
        clave = findViewById(R.id.input_login_pass)
        btn = findViewById(R.id.btn_login_ingresar)
        datos = Volley.newRequestQueue(this)

        btn.setOnClickListener()
        {
            consultarDatos(usu.getText().toString(),clave.getText().toString());
        }
    }

    fun consultarDatos(usu: String, pass: String) {
        val url = "http://34.206.51.125/apiconsultausu.php?usu=$usu&pass=$pass"
        val request = JsonObjectRequest(
            Request.Method.GET, url, null,
            { response ->
                try {
                    val estado = response.getString("estado")
                    if (estado == "0") {
                        Toast.makeText(
                            this@login, "Usuario no disponible para acceder a la plataforma",
                            Toast.LENGTH_LONG
                        ).show()
                    } else {
                        val ventana = Intent(
                            this@login,
                            admin_main::class.java
                        )
                        startActivity(ventana)
                    }
                } catch (e: JSONException) {
                    e.printStackTrace()
                }
            },
            { error ->
                error.printStackTrace()
            }
        )
        datos.add(request)
    }

}