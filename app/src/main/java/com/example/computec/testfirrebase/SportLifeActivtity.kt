package com.example.computec.testfirrebase

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.support.v4.app.Fragment
import android.support.v4.app.FragmentManager
import android.support.v4.app.FragmentPagerAdapter
import android.support.v7.app.AppCompatActivity
import android.util.Log
import android.view.*
import com.google.firebase.database.*
import kotlinx.android.synthetic.main.activity_sport_life_activtity.*


class SportLifeActivtity : AppCompatActivity() {

    private var mSectionsPagerAdapter: SectionsPagerAdapter? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_sport_life_activtity)

        setSupportActionBar(toolbar)
        mSectionsPagerAdapter = SectionsPagerAdapter(supportFragmentManager)

        container.adapter = mSectionsPagerAdapter
        tabLayout.setupWithViewPager(container)
    }


    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.menu_sport_life_activtity, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        val id = item.itemId

        if (id == R.id.action_settings) {
            return true
        }

        return super.onOptionsItemSelected(item)
    }

    inner class SectionsPagerAdapter(fm: FragmentManager) : FragmentPagerAdapter(fm) {

        val titles: List<String> = listOf("Public Snap", "Private Snap")

        override fun getItem(position: Int): Fragment {
            return PlaceholderFragment.newInstance(titles[position]);
        }

        override fun getCount(): Int = 2

        override fun getPageTitle(position: Int): CharSequence {
            return titles[position]
        }
    }

    class PlaceholderFragment : Fragment() {

        private lateinit var myRef: DatabaseReference

        var snaps = mutableListOf<SportLifeFeed?>()
        lateinit var sportLifeRef: DatabaseReference

        override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                                  savedInstanceState: Bundle?): View? {
            val rootView = inflater.inflate(R.layout.fragment_sport_life_activtity, container, false)
            return rootView
        }

        override fun onStart() {
            super.onStart()
            myRef = FirebaseDatabase.getInstance().reference
            sportLifeRef = myRef.child("sport_life")
            val sportLifeChildRef = when (arguments.getString(ARG_SECTION_NUMBER)) {
                "Public Snap" -> sportLifeRef.child("public").child("snap")
                else -> sportLifeRef.child("private").child("snap")
            }

            sportLifeChildRef.addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(dataSnapshot: DataSnapshot) {

                    for (postSnapshot in dataSnapshot.children) {
                        var snap: SportLifeFeed? = postSnapshot.getValue(SportLifeFeed::class.java)
                        snap?.key = postSnapshot.key
                        Log.d("tag", snap.toString())
                        snaps.add(snap)
                        likeSnap()
                    }
                }

                override fun onCancelled(databaseError: DatabaseError) {}
            })
        }

        private fun likeSnap() {
            var snapsRef = sportLifeRef.child("public").child("snap")
            for (snap in snaps) {
                Log.d("test", snap.toString())
                snapsRef.child(snap?.key).child("likes")
                        .addListenerForSingleValueEvent(object : ValueEventListener {
                            override fun onDataChange(dataSnapshot: DataSnapshot?) {
                                var value = dataSnapshot?.value as Long
                                value++
                                Log.d("test", value.toString())
                                dataSnapshot.ref.setValue(value)
                            }

                            override fun onCancelled(databaseError: DatabaseError?) {

                            }
                        })
            }
        }

        companion object {

            private val ARG_SECTION_NUMBER = "section_number"

            fun newInstance(type: String): PlaceholderFragment {
                val fragment = PlaceholderFragment()
                val args = Bundle()
                args.putString(ARG_SECTION_NUMBER, type)
                fragment.arguments = args
                return fragment
            }
        }
    }

    companion object {
        fun newInstance(contect: Context): Intent = Intent(contect, SportLifeActivtity::class.java)
    }
}
