package com.example.ffu.recommend

import android.annotation.SuppressLint
import android.app.Activity
import android.app.Dialog
import android.os.Build
import android.os.Bundle
import android.util.DisplayMetrics
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageButton
import android.widget.TextView
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AlertDialog
import androidx.recyclerview.widget.LinearLayoutManager
import com.bumptech.glide.Glide

import com.example.ffu.R
import com.example.ffu.UserInformation
import com.example.ffu.UserInformation.Companion.CURRENT_USERID
import com.example.ffu.UserInformation.Companion.PROFILE
import com.example.ffu.UserInformation.Companion.RECEIVEDLIKE_USER
import com.example.ffu.UserInformation.Companion.SENDLIKE_USER
import com.example.ffu.chatdetail.ChatItem
import com.example.ffu.databinding.FragmentBottomsheetBinding
import com.example.ffu.profile.HistoryModel
import com.example.ffu.profile.HistoryModel.Companion.RECEIVE_TYPE
import com.example.ffu.profile.HistoryModel.Companion.SEND_TYPE
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase
import de.hdodenhof.circleimageview.CircleImageView
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter


class RecommendList(recommendUsersUid: ArrayList<String>) : BottomSheetDialogFragment() {

    private val recommendUsers = recommendUsersUid // 거리에 매치되는 user 리스트
    // firebase
    private lateinit var userDB: DatabaseReference
    private val auth: FirebaseAuth by lazy {
        Firebase.auth
    }
    private val recommendUserList = mutableListOf<RecommendArticleModel>()

    // user View
    private var binding: FragmentBottomsheetBinding? = null
    private lateinit var recommendAdapter: RecommendAdapter

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view: View = inflater.inflate(R.layout.fragment_bottomsheet, container, false)
        view.findViewById<Button>(R.id.returnToMap).setOnClickListener {
            dismiss()
        }

        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        recommendUserList.clear()
        //userDB = Firebase.database.reference.child(DB_PROFILE)

        // bottomsheet view
        val fragmentBottomsheetBinding = FragmentBottomsheetBinding.bind(view)
        binding = fragmentBottomsheetBinding

        recommendAdapter = RecommendAdapter(onItemClicked = { RecommendArticleModel->
            showUserInformation(RecommendArticleModel)

        })


        addRecommendUserList()

        fragmentBottomsheetBinding.recommendedUsersView.layoutManager = LinearLayoutManager(context)
        fragmentBottomsheetBinding.recommendedUsersView.adapter = recommendAdapter

        //userDB.addChildEventListener(listener)

    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val dialog = super.onCreateDialog(savedInstanceState)
        dialog.setOnShowListener{ dialogInterface ->
            val bottomSheetDialog = dialogInterface as BottomSheetDialog
            setupRatio(bottomSheetDialog)
        }
        return dialog
    }

    private fun setupRatio(bottomSheetDialog: BottomSheetDialog) {
        val bottomSheet = bottomSheetDialog.findViewById<View>(com.google.android.material.R.id.design_bottom_sheet) as View
        val behavior = BottomSheetBehavior.from(bottomSheet)
        val layoutParams = bottomSheet!!.layoutParams
        layoutParams.height = getBottomSheetDialogDefaultHeight()
        bottomSheet.layoutParams = layoutParams
        behavior.state = BottomSheetBehavior.STATE_EXPANDED
    }

    private fun getBottomSheetDialogDefaultHeight(): Int {
        // 위 수치는 기기 높이 대비 80%로 다이얼로그 높이를 설정
        return getWindowHeight() * 100 / 100
    }

    private fun getWindowHeight(): Int {
        // Calculate window height for fullscreen use
        val displayMetrics = DisplayMetrics()
        (context as Activity?)!!.windowManager.defaultDisplay.getMetrics(displayMetrics)
        return displayMetrics.heightPixels
    }

    override fun onDestroy() {
        super.onDestroy()
        //userDB.removeEventListener(listener)
    }
    @SuppressLint("NotifyDataSetChanged")
    override fun onResume() {
        super.onResume()
        recommendAdapter.notifyDataSetChanged()
    }
    companion object {
        const val TAG = "RecommendList"
    }

    private fun addRecommendUserList(){
        for(userId in recommendUsers){
            if(CURRENT_USERID!=userId && SENDLIKE_USER[userId]!=true &&  RECEIVEDLIKE_USER[userId]!=true){
                val nickname = UserInformation.PROFILE[userId]?.nickname ?: ""
                val gender = UserInformation.PROFILE[userId]?.gender ?: ""
                val birth = UserInformation.PROFILE[userId]?.birth ?: ""
                val imageUri = UserInformation.URI[userId]?:""
                recommendUserList.add(RecommendArticleModel(userId,nickname,gender,birth,imageUri))
                recommendAdapter.submitList(recommendUserList)
            }

        }
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun showUserInformation(recommendArticleModel: RecommendArticleModel) {


        val userId = recommendArticleModel.Id
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

        age.text="나이 : "+PROFILE[userId]?.age
        birth.text="생일 : "+PROFILE[userId]?.birth
        drinking.text="음주여부 : "+PROFILE[userId]?.drinking
        hobby.text="취미 : "+PROFILE[userId]?.hobby
        job.text="직업 : "+PROFILE[userId]?.job
        mbti.text="mbti : "+PROFILE[userId]?.mbti
        personality.text="성격 : "+PROFILE[userId]?.personality
        smoke.text="흡연여부 : "+PROFILE[userId]?.smoke

        Glide.with(this)
            .load(recommendArticleModel.imageUrl)
            .into(image)
        //  취소 버튼 클릭 시
        cancel.setOnClickListener {
            dialog.dismiss()
            dialog.cancel()
        }
        //  완료 버튼 클릭 시
        like.setOnClickListener {
            val receivedLikeDB = Firebase.database.reference.child("likeInfo").child(userId).child("receivedLike").child(CURRENT_USERID)
            val sendLikeDB = Firebase.database.reference.child("likeInfo").child(CURRENT_USERID).child("sendLike").child(userId)
            receivedLikeDB.setValue("")
            sendLikeDB.setValue("")

            val userHistoryDB = Firebase.database.reference.child("history").child(CURRENT_USERID)
            val otherHistoryDB = Firebase.database.reference.child("history").child(userId)
            val current = LocalDateTime.now()
            val formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm")
            val formatted = current.format(formatter).toString()

            val sendHistoryItem = HistoryModel(
                name = PROFILE[userId]?.nickname!!,
                time = formatted!!,
                type = SEND_TYPE
            )

            val receiveHistoryItem = HistoryModel(
                name = PROFILE[CURRENT_USERID]?.nickname!!,
                time = formatted!!,
                type = RECEIVE_TYPE
            )

            userHistoryDB.push().setValue(sendHistoryItem)
            otherHistoryDB.push().setValue(receiveHistoryItem)


            dialog.dismiss()
            dialog.cancel()
        }
        dialog.setView(mView)
        dialog.create()
        dialog.show()
    }
}