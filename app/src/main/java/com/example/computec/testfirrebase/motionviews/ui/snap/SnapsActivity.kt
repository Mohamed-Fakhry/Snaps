package com.example.computec.testfirrebase.motionviews.ui.snap

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.support.v7.widget.GridLayoutManager
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.Toolbar
import android.util.Log
import android.widget.LinearLayout

import com.example.computec.testfirrebase.R
import com.example.computec.testfirrebase.SportLifeFeed
import com.example.computec.testfirrebase.User
import com.example.computec.testfirrebase.motionviews.ui.MainActivity
import com.google.firebase.database.*
import com.google.firebase.storage.StorageReference
import com.sandrios.sandriosCamera.internal.SandriosCamera
import com.sandrios.sandriosCamera.internal.configuration.CameraConfiguration
import kotlinx.android.synthetic.main.activity_snaps.*
import kotlinx.android.synthetic.main.content_main2.*


class SnapsActivity : AppCompatActivity() {

    private lateinit var myRef: DatabaseReference
    private lateinit var mStorageRef: StorageReference
    private lateinit var user: User
    lateinit var snaps: MutableList<SportLifeFeed?>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_snaps)
        setSupportActionBar(toolbar as Toolbar?)
        takeSnapL.setOnClickListener { takeSnap() }
        val database = FirebaseDatabase.getInstance()
        myRef = database.reference

        this.title = "Snaps"
        snapRV.layoutManager = LinearLayoutManager(this, LinearLayoutManager.VERTICAL,false)

        myRef = FirebaseDatabase.getInstance().reference


    }

    override fun onResume() {
        super.onResume()

        var sportLifeRef = myRef.child("sport_life")
        val sportLifeChildRef = sportLifeRef.child("public").child("snap")

        sportLifeChildRef.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                snaps = mutableListOf()
                for (postSnapshot in dataSnapshot.children) {
                    var snap: SportLifeFeed? = postSnapshot.getValue(SportLifeFeed::class.java)
                    snap?.key = postSnapshot.key
                    Log.d("tag", snap.toString())
                    snaps.add(snap)
                }

                snapRV.adapter = SnapAdapter(snaps.reversed() as MutableList<SportLifeFeed?>)
            }

            override fun onCancelled(databaseError: DatabaseError) {}
        })
    }
    fun takeSnap() {
        SandriosCamera(this, CAPTURE_MEDIA)
                .setMediaAction(CameraConfiguration.MEDIA_ACTION_PHOTO)
                .enableImageCropping(true)
                .launchCamera()
    }

    public override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        try {
            if (resultCode == Activity.RESULT_OK && requestCode == CAPTURE_MEDIA) {
                val profileFilePath = data?.getStringExtra(CameraConfiguration.Arguments.FILE_PATH)
                        ?.replace("file://", "")

                startActivity(MainActivity.newInstance(this, profileFilePath))
            }
        } catch (e : Exception) {

        }
    }

    companion object {

        private val CAPTURE_MEDIA = 368

        public fun startSnapActivty(context: Context): Intent = Intent(context, SnapsActivity::class.java)
    }
}
