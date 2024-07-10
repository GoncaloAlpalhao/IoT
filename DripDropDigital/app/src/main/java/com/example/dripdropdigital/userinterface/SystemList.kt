package com.example.dripdropdigital.userinterface

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.StaggeredGridLayoutManager
import com.example.dripdropdigital.backend.LocalStorage
import com.example.dripdropdigital.R
import com.example.dripdropdigital.systems.SystemItem
import com.example.dripdropdigital.systems.SystemListAdapter
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.floatingactionbutton.FloatingActionButton

/**
 * Displays the list of systems
 */
class SystemList : AppCompatActivity() {

    // Declare the floating action button
    private lateinit var fab: FloatingActionButton
    private lateinit var fabDel: FloatingActionButton
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
        fabDel = findViewById(R.id.fabDeleteSystem)

        // Set the click listener for the floating action button
        fab.setOnClickListener {
            // Call the activity to add a new system
            val intent = Intent(this, NewSystem::class.java)
            startActivity(intent)
        }

        fabDel.setOnClickListener {
            onDeleteConfirmation()
            listSystems()
        }

        listSystems()

    }

    /**
     * This function is responsible for displaying the list of systems in the system list page
     */
    private fun listSystems() {
        val localSystems = LocalStorage.getPlants(this)

        val recyclerView = findViewById<RecyclerView>(R.id.systemList)

        recyclerView.adapter = null

        if (localSystems.isNullOrEmpty()){
            Log.e("SystemList", "No systems found")
            Toast.makeText(this, "Não foram encontrados sistemas", Toast.LENGTH_SHORT).show()
            return
        }

        val systemStrings = localSystems.split(";")

        val systems = mutableListOf<SystemItem>()

        for (system in systemStrings){
            val systemData = system.split("|")
            systems.add(SystemItem(systemData[0], systemData[1], systemData[2], systemData[3], systemData[4], systemData[5], systemData[6], systemData[7], systemData[8], systemData[9], systemData[10]))
        }

        val allSystems: List<SystemItem> = systems

        val adapter = SystemListAdapter(allSystems, this, object: SystemListAdapter.OnItemClickListener {
            override fun onItemClick(position: Int, system: SystemItem) {
                val intent = Intent(this@SystemList, MainDashboard::class.java)
                intent.putExtra("system", system)
                startActivity(intent)
            }
        })

        recyclerView.adapter = adapter

        val layoutManager = StaggeredGridLayoutManager(2, StaggeredGridLayoutManager.VERTICAL)
        recyclerView.layoutManager = layoutManager

    }

    /**
     * This function is responsible for displaying a confirmation dialog before deleting all systems
     */
    private fun onDeleteConfirmation(){
        val builder = MaterialAlertDialogBuilder(this)
        builder.setTitle("Apagar todos os sistemas")
        builder.setMessage("Tem a certeza que deseja apagar todos os sistemas?")
        builder.setPositiveButton("Sim") { _, _ ->
            LocalStorage.deleteLocations(this)
            listSystems()
        }
        builder.setNegativeButton("Não") { dialog, _ -> dialog.dismiss() }
        builder.show()
    }

    /**
     * This function is called when the activity is resumed
     */
    override fun onResume() {
        super.onResume()
        listSystems()
    }


}