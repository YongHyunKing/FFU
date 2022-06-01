package com.example.ffu.matching

import android.os.Build
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.widget.Button
import android.widget.ImageButton
import android.widget.TextView
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.bumptech.glide.Glide
import com.example.ffu.R
import com.example.ffu.UserInformation
import com.example.ffu.UserInformation.Companion.CURRENT_USERID
import com.example.ffu.UserInformation.Companion.MATCH_USER
import com.example.ffu.UserInformation.Companion.PROFILE
import com.example.ffu.UserInformation.Companion.RECEIVED_LIKE_USER
import com.example.ffu.UserInformation.Companion.URI
import com.example.ffu.databinding.FragmentMatchingBinding
import com.example.ffu.utils.History
import com.example.ffu.utils.LikeArticle
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase
import de.hdodenhof.circleimageview.CircleImageView
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

class MatchingFragment: Fragment(R.layout.fragment_matching) {

    private lateinit var likeArticleAdapter: LikeArticleAdapter
    private val likeArticleList = mutableListOf<LikeArticle>()
    private var binding: FragmentMatchingBinding? = null

    private val auth: FirebaseAuth by lazy {
        Firebase.auth
    }

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val fragmentMatchingBinding = FragmentMatchingBinding.bind(view)
        binding = fragmentMatchingBinding
        likeArticleList.clear()

        likeArticleAdapter = LikeArticleAdapter(onItemClicked = { likeArticleModel ->
            showUserInformation(likeArticleModel)
        })

        addReceivedLikeArticleList()

        fragmentMatchingBinding.matchingRecyclerView.layoutManager = LinearLayoutManager(context)
        fragmentMatchingBinding.matchingRecyclerView.adapter = likeArticleAdapter
    }

    private fun addReceivedLikeArticleList(){

        for(likeId in RECEIVED_LIKE_USER.keys){
            if(MATCH_USER[likeId]!=true){
                val name = PROFILE[likeId]?.nickname ?: ""
                val gender = PROFILE[likeId]?.gender ?: ""
                val birth = PROFILE[likeId]?.birth ?: ""
                val imageUri = URI[likeId]?:""
                //Log.d("name",name)
                likeArticleList.add(LikeArticle(likeId,name,gender,birth,imageUri))
                likeArticleAdapter.submitList(likeArticleList)
            }
        }

    }

    override fun onResume() {
        super.onResume()
        likeArticleAdapter.notifyDataSetChanged()
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun showUserInformation(likeArticleModel: LikeArticle) {


        val userId = likeArticleModel.Id
        val dialog = AlertDialog.Builder(requireActivity()).create()
        val edialog : LayoutInflater = LayoutInflater.from(requireActivity())
        val mView : View = edialog.inflate(R.layout.dialog_userinformation,null)
        val image : CircleImageView = mView.findViewById(R.id.dialog_userinformation_photo)
        val cancel : ImageButton = mView.findViewById(R.id.dialog_userinformation_cancel)
        val age : TextView = mView.findViewById(R.id.dialog_userinformation_age)
        val birth : TextView = mView.findViewById(R.id.dialog_userinformation_birth)
        val drinking : TextView = mView.findViewById(R.id.dialog_userinformation_drinking)
        val hobby : TextView = mView.findViewById(R.id.dialog_userinformation_hobby)
        val job : TextView = mView.findViewById(R.id.dialog_userinformation_job)
        val mbti : TextView = mView.findViewById(R.id.dialog_userinformation_mbti)
        val personality : TextView = mView.findViewById(R.id.dialog_userinformation_personality)
        val smoke: TextView = mView.findViewById(R.id.dialog_userinformation_smoke)
        val like : Button = mView.findViewById(R.id.dialog_userinformation_like)

        age.text="나이 : "+UserInformation.PROFILE[userId]?.age
        birth.text="생일 : "+UserInformation.PROFILE[userId]?.birth
        drinking.text="음주여부 : "+UserInformation.PROFILE[userId]?.drinking
        hobby.text="취미 : "+UserInformation.PROFILE[userId]?.hobby
        job.text="직업 : "+UserInformation.PROFILE[userId]?.job
        mbti.text="mbti : "+UserInformation.PROFILE[userId]?.mbti
        personality.text="성격 : "+UserInformation.PROFILE[userId]?.personality
        smoke.text="흡연여부 : "+UserInformation.PROFILE[userId]?.smoke

        Glide.with(this)
            .load(likeArticleModel.imageUrl)
            .into(image)
        //  취소 버튼 클릭 시
        cancel.setOnClickListener {
            dialog.dismiss()
            dialog.cancel()
        }
        //  완료 버튼 클릭 시
        like.setOnClickListener {
            //상대방꺼에 나를 저장
            val otherDB = Firebase.database.reference.child("likeInfo").child(userId).child("match").child(CURRENT_USERID)
            otherDB.setValue("")

            //나에 상대방꺼 저장
            val myDB = Firebase.database.reference.child("likeInfo").child(CURRENT_USERID).child("match").child(userId)
            myDB.setValue("")

            val userHistoryDB = Firebase.database.reference.child("history").child(CURRENT_USERID)
            val otherHistoryDB = Firebase.database.reference.child("history").child(userId)
            val current = LocalDateTime.now()
            val formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm")
            val formatted = current.format(formatter).toString()

            val matchUserHistoryItem = History(
                name = PROFILE[userId]?.nickname!!,
                time = formatted!!,
                type = History.MATCH_TYPE
            )

            val matchOtherUserHistoryItem = History(
                name = PROFILE[CURRENT_USERID]?.nickname!!,
                time = formatted!!,
                type = History.MATCH_TYPE
            )

            userHistoryDB.push().setValue(matchUserHistoryItem)
            otherHistoryDB.push().setValue(matchOtherUserHistoryItem)


            //val myDB = Firebase.database.reference.child("likeInfo").child(CURRENT_USERID).child("match").child(userId)

            dialog.dismiss()
            dialog.cancel()
        }
        dialog.setView(mView)
        dialog.create()
        dialog.show()
    }
}