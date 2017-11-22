package com.example.computec.testfirrebase.motionviews.ui.snap

import android.support.v7.widget.RecyclerView
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import com.bumptech.glide.Glide
import com.example.computec.testfirrebase.R
import com.example.computec.testfirrebase.R.id.deleteIV
import com.example.computec.testfirrebase.SportLifeFeed
import com.google.firebase.database.*
import kotlinx.android.synthetic.main.activity_main.view.*
import kotlinx.android.synthetic.main.item_snap.view.*


class SnapAdapter(var snaps: MutableList<SportLifeFeed?>) : RecyclerView.Adapter<SnapAdapter.SnapVH>() {

    override fun onCreateViewHolder(parent: ViewGroup?, viewType: Int): SnapVH =
            SnapVH(LayoutInflater.from(parent?.context).inflate(R.layout.item_snap, null))


    override fun onBindViewHolder(holder: SnapVH?, position: Int) {
        holder?.setUp(snaps[position])
        holder?.delete?.setOnClickListener{
            holder.snapRef.removeValue { _, _ ->
                snaps.removeAt(position)
                notifyItemRemoved(position)
                notifyItemRangeChanged(position, snaps.size)
            }
        }
    }

    override fun getItemCount(): Int = snaps.size

    class SnapVH(itemView: View) : RecyclerView.ViewHolder(itemView) {

        lateinit var snapRef: DatabaseReference
        lateinit var delete: ImageView

        fun setUp(snap: SportLifeFeed?) {
            delete = itemView.findViewById(R.id.deleteIV)
            snapRef = FirebaseDatabase.getInstance()
                    .reference.child("sport_life")
                    .child("public")
                    .child("snap")
                    .child(snap?.key)

            with(itemView) {
                Glide.with(context)
                        .load(snap?.imageUrl)
                        .into(snapIV)
                likeTV.text = snap?.likes.toString()
                superLikeTV.text = snap?.superLikes.toString()
                shareTV.text = snap?.shares.toString()

                likeL.setOnClickListener {
                    snapRef.child("likes").addListenerForSingleValueEvent(object : ValueEventListener {

                        override fun onDataChange(dataSnapShot: DataSnapshot?) {
                            var value = dataSnapShot?.value as Long
                            value++
                            likeTV.text = value.toString()
                            dataSnapShot.ref.setValue(value)
                        }

                        override fun onCancelled(p0: DatabaseError?) {}
                    })
                }

                superLikeL.setOnClickListener {
                    snapRef.child("superLikes").addListenerForSingleValueEvent(object : ValueEventListener {

                        override fun onDataChange(dataSnapShot: DataSnapshot?) {
                            var value = dataSnapShot?.value as Long
                            value++
                            superLikeTV.text = value.toString()
                            dataSnapShot.ref.setValue(value)
                        }

                        override fun onCancelled(p0: DatabaseError?) {}
                    })
                }

                shareL.setOnClickListener {
                    snapRef.child("shares").addListenerForSingleValueEvent(object : ValueEventListener {

                        override fun onDataChange(dataSnapShot: DataSnapshot?) {
                            var value = dataSnapShot?.value as Long
                            value++
                            shareTV.text = value.toString()
                            dataSnapShot.ref.setValue(value)
                        }

                        override fun onCancelled(p0: DatabaseError?) {}
                    })
                }
            }
        }
    }

}