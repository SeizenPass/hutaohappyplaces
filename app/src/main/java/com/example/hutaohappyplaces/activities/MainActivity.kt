package com.example.hutaohappyplaces.activities

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.hutaohappyplaces.R
import com.example.hutaohappyplaces.adapters.HappyPlacesAdapter
import com.example.hutaohappyplaces.database.DatabaseHandler
import com.example.hutaohappyplaces.models.HappyPlaceModel
import com.example.hutaohappyplaces.utils.SwipeToDeleteCallback
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.database.*
import kotlinx.android.synthetic.main.activity_main.*
import pl.kitek.rvswipetodelete.SwipeToEditCallback

class MainActivity : AppCompatActivity() {
    private var mAuth: FirebaseAuth? = null
    private var currentUser: FirebaseUser? = null
    private var places: ArrayList<HappyPlaceModel> = ArrayList()
    private var keys: ArrayList<String> = ArrayList()
    /**
     * This function is auto created by Android when the Activity Class is created.
     */
    override fun onCreate(savedInstanceState: Bundle?) {
        //This call the parent constructor
        super.onCreate(savedInstanceState)
        var firebaseDatabase = FirebaseDatabase.getInstance()
        mAuth = FirebaseAuth.getInstance()
        val sharedPref = getSharedPreferences("auth",Context.MODE_PRIVATE)
        val email = sharedPref.getString("email", "DEFAULT")
        val password = sharedPref.getString("password", "DEFAULT")
        if (email != "DEFAULT") {
            mAuth!!.signInWithEmailAndPassword(email.toString(), password.toString())
                .addOnCompleteListener {
                    if (it.isSuccessful) {
                        currentUser = mAuth!!.currentUser
                        getHappyPlacesListFromFirebase()
                    }
                }
        }
        //val email = sharedPref.getString("email")

        // This is used to align the xml view to this class
        setContentView(R.layout.activity_main)
        progressBar1.visibility = View.VISIBLE
        if (currentUser == null && email == "DEFAULT") startAuthActivity()
        // Setting an click event for Fab Button and calling the AddHappyPlaceActivity.
        fabAddHappyPlace.setOnClickListener {
            val intent = Intent(this@MainActivity, AddHappyPlaceActivity::class.java)
            startActivityForResult(intent, ADD_PLACE_ACTIVITY_REQUEST_CODE)
        }
        fabLeave.setOnClickListener{
            currentUser = null
            mAuth!!.signOut()
            val ed = sharedPref.edit()
            ed.remove("email")
            ed.remove("password")
            ed.apply()
            startAuthActivity()
        }

        //getHappyPlacesListFromLocalDB()
        if (currentUser != null) {
            getHappyPlacesListFromFirebase()
        }
    }

    private fun startAuthActivity() {
        val authIntent = Intent(this@MainActivity, AuthActivity::class.java)
        startActivityForResult(authIntent, LOGIN)
    }

    // Call Back method  to get the Message form other Activity
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        // check if the request code is same as what is passed  here it is 'ADD_PLACE_ACTIVITY_REQUEST_CODE'
        if (requestCode == ADD_PLACE_ACTIVITY_REQUEST_CODE) {
            if (resultCode == Activity.RESULT_OK) {

            } else {
                Log.e("Activity", "Cancelled or Back Pressed")
            }
        }
        if (requestCode == LOGIN) {
            if (resultCode == RESULT_OK) {
                currentUser = mAuth!!.currentUser
                Log.d("INFO", mAuth!!.currentUser!!.email!!)
                getHappyPlacesListFromFirebase()
            }
        }
    }

    /**
     * A function to get the list of happy place from local database.
     */
    private fun getHappyPlacesListFromLocalDB() {

        val dbHandler = DatabaseHandler(this)

        val getHappyPlacesList = dbHandler.getHappyPlacesList()

        if (getHappyPlacesList.size > 0) {
            rv_happy_places_list.visibility = View.VISIBLE
            tv_no_records_available.visibility = View.GONE
            setupHappyPlacesRecyclerView(getHappyPlacesList)
        } else {
            rv_happy_places_list.visibility = View.GONE
            tv_no_records_available.visibility = View.VISIBLE
        }
    }

    private fun getHappyPlacesListFromFirebase() {
        var firebaseDatabase = FirebaseDatabase.getInstance()
        var databaseRef = firebaseDatabase.getReference("users")
            .child(currentUser!!.email!!.toString().replace('.', ',')).child("places")
        val childEventListener = object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                places.clear()
                keys = ArrayList()
                for (node in snapshot.children) {
                    keys.add(node.key!!)
                    //TODO OPTIMIZE
                    val place = HappyPlaceModel(
                        node.child("title").value.toString(),
                        node.child("image").value.toString(),
                        node.child("description").value.toString(),
                        node.child("date").value.toString(),
                        node.child("location").value.toString(),
                        node.child("latitude").value.toString().toDouble(),
                        node.child("longitude").value.toString().toDouble()
                    )
                    places.add(place)
                }
                progressBar1.visibility = View.GONE
                if (places.size > 0) {
                    rv_happy_places_list.visibility = View.VISIBLE
                    tv_no_records_available.visibility = View.GONE
                    setupHappyPlacesRecyclerView(places)
                } else {
                    rv_happy_places_list.visibility = View.GONE
                    tv_no_records_available.visibility = View.VISIBLE
                }
            }
            override fun onCancelled(databaseError: DatabaseError) {
            }
        }
        databaseRef.addValueEventListener(childEventListener)

    }

    /**
     * A function to populate the recyclerview to the UI.
     */
    private fun setupHappyPlacesRecyclerView(happyPlacesList: ArrayList<HappyPlaceModel>) {

        rv_happy_places_list.layoutManager = LinearLayoutManager(this)
        rv_happy_places_list.setHasFixedSize(true)

        val placesAdapter = HappyPlacesAdapter(this, happyPlacesList)
        rv_happy_places_list.adapter = placesAdapter

        placesAdapter.setOnClickListener(object :
                HappyPlacesAdapter.OnClickListener {
            override fun onClick(position: Int, model: HappyPlaceModel) {
                val intent = Intent(this@MainActivity, HappyPlaceDetailActivity::class.java)
                intent.putExtra(EXTRA_PLACE_DETAILS, model) // Passing the complete serializable data class to the detail activity using intent.
                startActivity(intent)
            }
        })

        val editSwipeHandler = object : SwipeToEditCallback(this) {
            override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {
                val adapter = rv_happy_places_list.adapter as HappyPlacesAdapter
                adapter.notifyEditItem(
                        this@MainActivity,
                        viewHolder.adapterPosition,
                        ADD_PLACE_ACTIVITY_REQUEST_CODE,
                    keys
                )
            }
        }
        val editItemTouchHelper = ItemTouchHelper(editSwipeHandler)
        editItemTouchHelper.attachToRecyclerView(rv_happy_places_list)

        val deleteSwipeHandler = object : SwipeToDeleteCallback(this) {
            override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {
                val adapter = rv_happy_places_list.adapter as HappyPlacesAdapter
                adapter.removeAt(viewHolder.adapterPosition, keys)

                //getHappyPlacesListFromLocalDB() // Gets the latest list from the local database after item being delete from it.
            }
        }
        val deleteItemTouchHelper = ItemTouchHelper(deleteSwipeHandler)
        deleteItemTouchHelper.attachToRecyclerView(rv_happy_places_list)
    }

    companion object {
        private const val ADD_PLACE_ACTIVITY_REQUEST_CODE = 1
        internal const val EXTRA_PLACE_DETAILS = "extra_place_details"
        private const val LOGIN = 2
        internal const val KEY = "KEY"
    }
}