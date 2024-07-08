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
        }

        listSystems()

    }

    private fun listSystems() {
        val localSystems = LocalStorage.getPlants(this)

        if (localSystems.isNullOrEmpty()){
            Log.e("SystemList", "No systems found")
            return
        }

        val systemStrings = localSystems.split(";")

        val systems = mutableListOf<SystemItem>()

        for (system in systemStrings){
            val systemData = system.split("|")
            systems.add(SystemItem(systemData[0], systemData[1], systemData[2], systemData[3], systemData[4], systemData[5], systemData[6], systemData[7], systemData[8], systemData[9], systemData[10]))
        }

        val allSystems: List<SystemItem> = systems

        val recyclerView = findViewById<RecyclerView>(R.id.systemList)

        recyclerView.adapter = null

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

    override fun onResume() {
        super.onResume()
        listSystems()
    }


}