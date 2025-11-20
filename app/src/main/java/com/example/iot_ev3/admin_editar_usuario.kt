package com.example.iot_ev3

import android.app.Activity
import android.os.Bundle
import android.view.View
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.Spinner
import androidx.appcompat.app.AppCompatActivity
import cn.pedant.SweetAlert.SweetAlertDialog
import com.android.volley.Request
import com.android.volley.RequestQueue
import com.android.volley.toolbox.JsonObjectRequest
import com.android.volley.toolbox.StringRequest
import com.android.volley.toolbox.Volley
import org.json.JSONObject

class admin_editar_usuario : AppCompatActivity() {

    private lateinit var requestQueue: RequestQueue
    private lateinit var userIdToEdit: String

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_admin_editar_usuario)

        userIdToEdit = intent.getStringExtra("USER_ID_TO_EDIT") ?: ""

        val btnVolver = findViewById<ImageView>(R.id.img_admin_editar_usuario_volver)
        val btnGuardar = findViewById<Button>(R.id.btn_admin_editar_usuario_guardar_cambios)
        val btnEliminar = findViewById<Button>(R.id.btn_admin_editar_usuario_eliminar)
        val spinnerEstado = findViewById<Spinner>(R.id.spinner_admin_editar_usuario_estado)

        requestQueue = Volley.newRequestQueue(this)

        ArrayAdapter.createFromResource(
            this,
            R.array.opciones_estado_usuario,
            android.R.layout.simple_spinner_item
        ).also { adapter ->
            adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
            spinnerEstado.adapter = adapter
        }

        btnVolver.setOnClickListener {
            finish()
        }

        btnGuardar.setOnClickListener {
            if (validarCampos()) {
                actualizarUsuario()
            }
        }

        btnEliminar.setOnClickListener {
            mostrarDialogoEliminar()
        }

        cargarDatosUsuario()
    }

    private fun cargarDatosUsuario() {
        val url = "http://34.206.51.125/api_get_datos_usuario.php?id=$userIdToEdit"
        val jsonObjectRequest = JsonObjectRequest(Request.Method.GET, url, null,
            { response ->
                findViewById<EditText>(R.id.input_admin_editar_usuario_nombres).setText(response.getString("nombres"))
                findViewById<EditText>(R.id.input_admin_editar_usuario_apellido_paterno).setText(response.getString("apellido_p"))
                findViewById<EditText>(R.id.input_admin_editar_usuario_apellido_materno).setText(response.getString("apellido_m"))
                findViewById<EditText>(R.id.input_admin_editar_usuario_rut).setText(response.getString("rut"))

                val estado = response.getString("estado")
                val spinner = findViewById<Spinner>(R.id.spinner_admin_editar_usuario_estado)
                val adapter = spinner.adapter as ArrayAdapter<String>
                val position = if (estado == "1") adapter.getPosition("Activo") else adapter.getPosition("Inactivo")
                spinner.setSelection(position)

                val userType = response.getString("tipo")
                if (userType.equals("admin", ignoreCase = true)) {
                    findViewById<Button>(R.id.btn_admin_editar_usuario_eliminar).visibility = View.GONE
                }
            },
            { error ->
                mostrarError("No se pudieron cargar los datos del usuario. Error: ${error.message}")
            }
        )
        requestQueue.add(jsonObjectRequest)
    }

    private fun validarCampos(): Boolean {
        val nombres = findViewById<EditText>(R.id.input_admin_editar_usuario_nombres).text.toString().trim()
        val apellidoP = findViewById<EditText>(R.id.input_admin_editar_usuario_apellido_paterno).text.toString().trim()
        val apellidoM = findViewById<EditText>(R.id.input_admin_editar_usuario_apellido_materno).text.toString().trim()
        val rut = findViewById<EditText>(R.id.input_admin_editar_usuario_rut).text.toString().trim()
        val pass1 = findViewById<EditText>(R.id.input_admin_editar_usuario_clave).text.toString()
        val pass2 = findViewById<EditText>(R.id.input_admin_editar_usuario_repetir_clave).text.toString()
        val estadoPos = findViewById<Spinner>(R.id.spinner_admin_editar_usuario_estado).selectedItemPosition

        if (nombres.isEmpty() || apellidoP.isEmpty() || apellidoM.isEmpty() || rut.isEmpty()) {
            mostrarError("Los campos de nombre, apellidos y RUT son obligatorios.")
            return false
        }
        if (!nombres.matches("^[a-zA-ZáéíóúÁÉÍÓÚñÑ\\s]+$".toRegex()) || nombres.length > 45 ||
            !apellidoP.matches("^[a-zA-ZáéíóúÁÉÍÓÚñÑ\\s]+$".toRegex()) || apellidoP.length > 45 ||
            !apellidoM.matches("^[a-zA-ZáéíóúÁÉÍÓÚñÑ\\s]+$".toRegex()) || apellidoM.length > 45) {
            mostrarError("Nombres y apellidos solo deben contener letras (máx 45 caracteres).")
            return false
        }
        if (!rut.matches("^\\d{7,8}-[\\dkK]$".toRegex())) {
            mostrarError("El formato del RUT no es válido (Ej: 12345678-9).")
            return false
        }
        if (pass1 != pass2) {
            mostrarError("Las contraseñas no coinciden.")
            return false
        }
        if (estadoPos == 0) {
            mostrarError("Debe seleccionar un estado para el usuario.")
            return false
        }

        return true
    }

    private fun actualizarUsuario() {
        val url = "http://34.206.51.125/api_editar_usuario.php"
        val loadingDialog = SweetAlertDialog(this, SweetAlertDialog.PROGRESS_TYPE).apply {
            titleText = "Actualizando..."
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
                params["id_usuario"] = userIdToEdit
                params["nombres"] = findViewById<EditText>(R.id.input_admin_editar_usuario_nombres).text.toString().trim()
                params["apellido_p"] = findViewById<EditText>(R.id.input_admin_editar_usuario_apellido_paterno).text.toString().trim()
                params["apellido_m"] = findViewById<EditText>(R.id.input_admin_editar_usuario_apellido_materno).text.toString().trim()
                params["rut"] = findViewById<EditText>(R.id.input_admin_editar_usuario_rut).text.toString().trim()
                params["clave"] = findViewById<EditText>(R.id.input_admin_editar_usuario_clave).text.toString()
                params["estado"] = if (findViewById<Spinner>(R.id.spinner_admin_editar_usuario_estado).selectedItem.toString() == "Activo") "1" else "0"
                return params
            }
        }
        requestQueue.add(stringRequest)
    }

    private fun mostrarDialogoEliminar() {
        SweetAlertDialog(this, SweetAlertDialog.WARNING_TYPE)
            .setTitleText("¿Estás seguro?")
            .setContentText("Esto eliminará al usuario de forma permanente. Si quiere quitarle permisos de acceso, debe cambiar su estado a Inactivo.")
            .setConfirmText("Sí, eliminar")
            .setCancelText("No, cancelar")
            .setConfirmClickListener { sDialog ->
                sDialog.dismissWithAnimation()
                eliminarUsuario()
            }
            .show()
    }

    private fun eliminarUsuario() {
        val url = "http://34.206.51.125/api_eliminar_usuario.php"
        val loadingDialog = SweetAlertDialog(this, SweetAlertDialog.PROGRESS_TYPE).apply {
            titleText = "Eliminando..."
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
                            .setTitleText("¡Eliminado!")
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
                params["id_usuario"] = userIdToEdit
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