package com.example.iot_ev3

import android.app.Activity
import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.SearchView
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import cn.pedant.SweetAlert.SweetAlertDialog
import com.android.volley.Request
import com.android.volley.RequestQueue
import com.android.volley.toolbox.JsonArrayRequest
import com.android.volley.toolbox.Volley
import org.json.JSONObject

data class Usuario(val id: Int, val nombreCompleto: String, val rut: String, val estado: String)

class UsuariosAdapter(
    private val usuarios: List<Usuario>,
    private val onUserClicked: (Usuario) -> Unit
) : RecyclerView.Adapter<UsuariosAdapter.ViewHolder>() {

    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val nombre: TextView = view.findViewById(R.id.txt_item_user_nombre)
        val rut: TextView = view.findViewById(R.id.txt_item_user_rut)
        val estado: TextView = view.findViewById(R.id.txt_item_user_estado)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_usuario, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val usuario = usuarios[position]
        holder.nombre.text = usuario.nombreCompleto
        holder.rut.text = usuario.rut
        holder.estado.text = if (usuario.estado == "1") "Activo" else "Inactivo"

        val color = if (usuario.estado == "1") Color.parseColor("#4CAF50") else Color.parseColor("#F44336")
        holder.estado.setBackgroundColor(color)

        holder.itemView.setOnClickListener { onUserClicked(usuario) }
    }

    override fun getItemCount() = usuarios.size
}

class admin_crud_usuarios : AppCompatActivity() {

    private lateinit var requestQueue: RequestQueue
    private lateinit var recyclerView: RecyclerView
    private lateinit var adapter: UsuariosAdapter
    private var usuariosList = mutableListOf<Usuario>()
    private lateinit var adminId: String

    private val editUserLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            consultarUsuarios("")
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_admin_crud_usuarios)

        adminId = intent.getStringExtra("id") ?: ""

        val btnVolver = findViewById<ImageView>(R.id.img_admin_crud_usuarios_volver)
        val btnAgregar = findViewById<Button>(R.id.btn_admin_crud_usuarios_agregar_usuario)
        val searchView = findViewById<SearchView>(R.id.input_admin_crud_sensores_buscar_usuario)

        requestQueue = Volley.newRequestQueue(this)

        recyclerView = findViewById(R.id.view_admin_crud_personas_lista)
        recyclerView.layoutManager = LinearLayoutManager(this)
        adapter = UsuariosAdapter(usuariosList) { usuario ->
            val intent = Intent(this, admin_editar_usuario::class.java)
            intent.putExtra("USER_ID_TO_EDIT", usuario.id.toString())
            editUserLauncher.launch(intent)
        }
        recyclerView.adapter = adapter

        btnVolver.setOnClickListener {
            finish()
        }

        btnAgregar.setOnClickListener {
            val intent = Intent(this, admin_agregar_usuario::class.java)
            intent.putExtra("id", adminId)
            startActivity(intent)
        }

        searchView.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String?): Boolean {
                consultarUsuarios(query ?: "")
                return true
            }

            override fun onQueryTextChange(newText: String?): Boolean {
                consultarUsuarios(newText ?: "")
                return true
            }
        })

        consultarUsuarios("")
    }

    private fun consultarUsuarios(query: String) {
        val url = "http://34.206.51.125/api_crud_usuarios.php?admin_id=$adminId&query=$query"

        val jsonArrayRequest = JsonArrayRequest(
            Request.Method.GET, url, null,
            { response ->
                usuariosList.clear()
                for (i in 0 until response.length()) {
                    val userJson: JSONObject = response.getJSONObject(i)
                    val usuario = Usuario(
                        id = userJson.getInt("idUsuario"),
                        nombreCompleto = "${userJson.getString("nombres")} ${userJson.getString("apellido_p")} ${userJson.getString("apellido_m")}",
                        rut = userJson.getString("rut"),
                        estado = userJson.getString("estado")
                    )
                    usuariosList.add(usuario)
                }
                adapter.notifyDataSetChanged()
            },
            { error ->
                error.printStackTrace()
                SweetAlertDialog(this, SweetAlertDialog.ERROR_TYPE)
                    .setTitleText("Error")
                    .setContentText("No se pudieron cargar los usuarios. Error: ${error.message}")
                    .show()
            }
        )
        requestQueue.add(jsonArrayRequest)
    }
}