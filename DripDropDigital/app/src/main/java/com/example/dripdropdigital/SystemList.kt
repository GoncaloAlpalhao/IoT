package com.example.dripdropdigital

import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.StaggeredGridLayoutManager
import com.google.android.material.floatingactionbutton.FloatingActionButton

class SystemList : AppCompatActivity() {

    // Declare the floating action button
    private lateinit var fab: FloatingActionButton
    private var test: Int = 0
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_system_list)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        supportActionBar?.hide()

        // Initialize the floating action button
        fab = findViewById(R.id.fabAddSystem)

        // Set the click listener for the floating action button
        fab.setOnClickListener {
            // Call the activity to add a new system
            val intent = Intent(this, NewSystem::class.java)
            startActivity(intent)
            test = 2
        }

        // Call the function to add a new system
        exampleLocation()

    }

    // Função para adicionar uma localização no layout da lista
    private fun exampleLocation(callback: (() -> Unit)? = null) {
        // Obter a recyclerview
        val recyclerView = findViewById<RecyclerView>(R.id.systemList)

        // Esvaziar a lista
        recyclerView.adapter = null

        // Criar uma lista de localizações de exemplo
        val exampleLocation = mutableListOf<SystemID>()
        exampleLocation.add(SystemID("noimage", "Horta do Mário", "Atrás Do Armário", 0))

        if (test == 1) {
            exampleLocation.add(SystemID("noimage", "Epa Ya", "Foi Mesmo", 1))
        }

        // Criar um novo adapter
        val adapter = SystemListAdapter(exampleLocation, this, object: SystemListAdapter.OnItemClickListener {
            override fun onItemClick(position: Int, location: SystemID) {
                // Chama o maindashboard
                val intent = Intent(this@SystemList, MainDashboard::class.java)
                startActivity(intent)
            }
        })

        // Definir o novo adapter
        recyclerView.adapter = adapter

        // Definir o novo layout manager
        val layoutManager = StaggeredGridLayoutManager(2, StaggeredGridLayoutManager.VERTICAL)
        recyclerView.layoutManager = layoutManager

        // Mostrar o layout
        recyclerView.visibility = RecyclerView.VISIBLE

        // After setting the adapter and layout manager for the RecyclerView, add this line:
        Log.d("SystemList", "exampleLocation: $exampleLocation")
        Log.d("SystemList", "exampleLocation: ${recyclerView.childCount}")

    }

    override fun onResume() {
        super.onResume()

        // ignora isto, trust me
        if (test == 0)
            exampleLocation()
        else if (test == 2)
            test = 1
            exampleLocation()
    }


}