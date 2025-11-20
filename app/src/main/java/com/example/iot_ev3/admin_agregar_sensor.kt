package com.example.iot_ev3

import android.app.Activity
import android.os.Bundle
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.Spinner
import androidx.appcompat.app.AppCompatActivity
import cn.pedant.SweetAlert.SweetAlertDialog
import com.android.volley.Request
import com.android.volley.RequestQueue
import com.android.volley.toolbox.JsonArrayRequest
import com.android.volley.toolbox.StringRequest
import com.android.volley.toolbox.Volley
import org.json.JSONObject

class admin_agregar_sensor : AppCompatActivity() {

    private lateinit var requestQueue: RequestQueue
    private lateinit var adminId: String
    private val userMap = mutableMapOf<String, Int>()
    private val userList = mutableListOf<String>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_admin_agregar_sensor)

        adminId = intent.getStringExtra("id") ?: ""

        val btnVolver = findViewById<ImageView>(R.id.img_admin_agregar_sensor_volver)
        val btnRegistrar = findViewById<Button>(R.id.btn_admin_agregar_sensor_registar_sensor)

        requestQueue = Volley.newRequestQueue(this)

        btnVolver.setOnClickListener { finish() }

        btnRegistrar.setOnClickListener {
            if (validarCampos()) {
                registrarSensor()
            }
        }

        cargarUsuarios()
    }

    private fun cargarUsuarios() {
        val url = "http://34.206.51.125/api_get_usuarios_por_depto.php?admin_id=$adminId"
        val spinnerUsuarios = findViewById<Spinner>(R.id.spinner_admin_agregar_sensor_usuario)

        val jsonArrayRequest = JsonArrayRequest(Request.Method.GET, url, null,
            { response ->
                userList.add("Seleccione Usuario")
                for (i in 0 until response.length()) {
                    val userJson: JSONObject = response.getJSONObject(i)
                    val id = userJson.getInt("idUsuario")
                    val nombre = "${userJson.getString("nombres")} ${userJson.getString("apellido_p")}"
                    userList.add(nombre)
                    userMap[nombre] = id
                }
                val adapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, userList)
                adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
                spinnerUsuarios.adapter = adapter
            },
            { error ->
                mostrarError("No se pudieron cargar los usuarios: ${error.message}")
            }
        )
        requestQueue.add(jsonArrayRequest)
    }

    private fun validarCampos(): Boolean {
        val codigo = findViewById<EditText>(R.id.input_admin_agregar_sensor_codigo_acceso).text.toString().trim()
        val tipoPos = findViewById<Spinner>(R.id.spinner_admin_agregar_sensor_tipo).selectedItemPosition
        val estadoPos = findViewById<Spinner>(R.id.spinner_admin_agregar_sensor_estado).selectedItemPosition
        val usuarioPos = findViewById<Spinner>(R.id.spinner_admin_agregar_sensor_usuario).selectedItemPosition

        if (codigo.isEmpty()) {
            mostrarError("El código de acceso es obligatorio.")
            return false
        }
        if (!codigo.matches("^[a-zA-Z0-9]{16}$".toRegex())) {
            mostrarError("El código debe tener exactamente 16 caracteres alfanuméricos.")
            return false
        }
        if (tipoPos == 0) {
            mostrarError("Debe seleccionar un tipo de sensor.")
            return false
        }
        if (estadoPos == 0) {
            mostrarError("Debe seleccionar un estado para el sensor.")
            return false
        }
        if (usuarioPos == 0) {
            mostrarError("Debe seleccionar un usuario para asignar el sensor.")
            return false
        }
        return true
    }

    private fun registrarSensor() {
        val url = "http://34.206.51.125/api_agregar_sensor.php"
        val loadingDialog = SweetAlertDialog(this, SweetAlertDialog.PROGRESS_TYPE).apply {
            titleText = "Registrando..."
            setCancelable(false)
            show()
        }

        val stringRequest = object : StringRequest(Request.Method.POST, url,
            { response ->
                loadingDialog.dismiss()
                try {
                    val jsonResponse = JSONObject(response)
                    val estado = jsonResponse.getInt("estado")
                    val mensaje = jsonResponse.getString("mensaje")
                    if (estado == 1) {
                        SweetAlertDialog(this, SweetAlertDialog.SUCCESS_TYPE)
                            .setTitleText("¡Éxito!")
                            .setContentText(mensaje)
                            .setConfirmClickListener { sDialog ->
                                sDialog.dismissWithAnimation()
                                setResult(Activity.RESULT_OK) 
                                finish()
                            }
                            .show()
                    } else {
                        mostrarError(mensaje)
                    }
                } catch (e: Exception) {
                    mostrarError("Error al procesar la respuesta.")
                }
            },
            { error ->
                loadingDialog.dismiss()
                mostrarError("Error de red: ${error.message}")
            }) {
            override fun getParams(): MutableMap<String, String> {
                val params = HashMap<String, String>()
                val spinnerUsuario = findViewById<Spinner>(R.id.spinner_admin_agregar_sensor_usuario)
                val nombreUsuarioSeleccionado = spinnerUsuario.selectedItem.toString()
                
                params["id_usuario_asignado"] = userMap[nombreUsuarioSeleccionado].toString()
                params["codigo"] = findViewById<EditText>(R.id.input_admin_agregar_sensor_codigo_acceso).text.toString().trim()
                params["tipo"] = findViewById<Spinner>(R.id.spinner_admin_agregar_sensor_tipo).selectedItem.toString()
                params["estado"] = if (findViewById<Spinner>(R.id.spinner_admin_agregar_sensor_estado).selectedItem.toString() == "Activo") "1" else "0"
                return params
            }
        }
        requestQueue.add(stringRequest)
    }

    private fun mostrarError(mensaje: String) {
        SweetAlertDialog(this, SweetAlertDialog.ERROR_TYPE)
            .setTitleText("Error")
            .setContentText(mensaje)
            .show()
    }
}