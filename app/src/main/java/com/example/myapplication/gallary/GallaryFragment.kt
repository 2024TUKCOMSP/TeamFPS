package com.example.myapplication.gallary

import android.content.Intent
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Path
import android.os.Bundle
import android.os.Environment
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.ImageButton
import androidx.fragment.app.Fragment
import com.example.myapplication.NaviActivity
import com.example.myapplication.R
import com.example.myapplication.data.Paints
import com.example.myapplication.data.Pid
import com.example.myapplication.gallary.PaintView.Companion.colorList
import com.example.myapplication.gallary.PaintView.Companion.currentBrush
import com.example.myapplication.gallary.PaintView.Companion.pathList
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.google.firebase.storage.FirebaseStorage
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.text.SimpleDateFormat
import java.util.Date


class GallaryFragment : Fragment() {
    private lateinit var paintView : PaintView

    companion object{
        var path = Path()
        var paintBrush = Paint()
    }



    // TODO: Rename and change types of parameters
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        val rootView = inflater.inflate(R.layout.fragment_gallary, container, false)
        paintView = rootView.findViewById(R.id.paint_view)

        val paintEndButton: Button =rootView.findViewById(R.id.paintendbutton)
        paintEndButton.setOnClickListener{
            viewSave(paintView)
        }

        return rootView

    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)



        val redBtn = view.findViewById<ImageButton>(R.id.redColor)
        val blueBtn = view.findViewById<ImageButton>(R.id.blueColor)
        val blackBtn = view.findViewById<ImageButton>(R.id.blackColor)
        val eraser = view.findViewById<ImageButton>(R.id.whiteColor)

        redBtn.setOnClickListener{
            paintBrush.color = Color.RED
            currentColor(paintBrush.color)
        }
        blueBtn.setOnClickListener{
            paintBrush.color = Color.BLUE
            currentColor(paintBrush.color)
        }
        blackBtn.setOnClickListener{
            paintBrush.color = Color.BLACK
            currentColor(paintBrush.color)
        }
        eraser.setOnClickListener{
            pathList.clear()
            colorList.clear()
            path.reset()
        }

    }
    private fun getViewBitmap(view: View): Bitmap {
        //뷰의 크기('measuredWidth','measuredHeight')에 맞는 빈 비트맵 이미지를 생성한다. ARGB_8888은 32비트맵을 의미
        Log.d("hyun", "bug 위치 : 비트맵 생성 전 ")
        val bitmap = Bitmap.createBitmap(
            view.measuredWidth, view.measuredHeight, Bitmap.Config.ARGB_8888
        )
        Log.d("hyun", "bug 위치 : 비트맵 생성 ")
        //비트맵을 캔버스에 연결합니다.
        val canvas = Canvas(bitmap)
        Log.d("hyun", "bug 위치 : 캔버스에 비트맵 연결")
        //뷰의 내용을 캔버스에 그립니다.
        view.draw(canvas)
        Log.d("hyun", "bug 위치 : 캔버스에 뷰 내용 그리기")
        //캡처된 비트맵을 반환합니다.
        return bitmap
    }
    private fun getSaveFilePathName(): String {
        //기기의 공용 다운로드 디렉터리의 경로를 가져옵니다.
        val folder = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS).toString()
        Log.d("hyun", "bug 위치 : 다운로드 디렉터리 경로 가져오기")
        //현재 날짜와 시간을 기반으로 파일이름을 생성합니다.
        val fileName = SimpleDateFormat("yyyyMMddHHmmss").format(Date())
        Log.d("hyun", "bug 위치 : 파일 이름 생성")
        //다운로드 디렉토리 경로와 파일 이름을 결합하여 전체 파일 경로를 반환합니다.
        return "$folder/$fileName.jpg"
    }

    private fun bitmapFileSave(bitmap: Bitmap, path: String) {
        //파일 출력 스트림을 선언합니다.
        val fos: FileOutputStream
        Log.d("hyun", "bug 위치 : 비트맵 파일저장 시작")
        try{
            //주어진 경로에 파일 출력 스트림을 엽니다.
            fos = FileOutputStream(File(path))
            Log.d("hyun", "bug 위치 : 비트맵 파일저장 출력스트림열기")
            //비트맵을 JPEG 형식으로 압축하여 파일 출력 스트림에 저장합니다. 압축 품질은 100(최고 품질)으로 설정됩니다.
            bitmap.compress(Bitmap.CompressFormat.JPEG,100,fos)
            Log.d("hyun", bitmap.toString())
            //파일 출력 스트림을 닫습니다.

            //*****************파베에 사진 저장 코드
            val storageRef = FirebaseStorage.getInstance().reference
            //데이터 베이스에 사진 저장 하는 코드
            val fileRef = storageRef.child("images/${System.currentTimeMillis()}.jpg")
            //baos로 바이트 배열로 변환
            val baos = ByteArrayOutputStream()
            //JPEG 형태로 압축
            bitmap.compress(Bitmap.CompressFormat.JPEG, 100, baos)
            //data에 바이트 배열형태로 변환 후 저장
            val data = baos.toByteArray()
            //배열형태로 저장된 것을 fileRef에 저장(파베 저장 시스템)
            val uploadTask = fileRef.putBytes(data)

            uploadTask.addOnFailureListener { exception ->
                Log.e("hyun", "Image upload failed", exception)
            }.addOnSuccessListener {
                // 업로드 성공 후 다운로드 URL을 가져옴
                fileRef.downloadUrl.addOnSuccessListener { uri ->
                    val imageUrl = uri.toString()
                    Log.d("hyun", "Uploaded image URL: $imageUrl")

                    val customDialog = SelectDrawingCostName(requireContext())
                    customDialog.show()
                    val token = (requireActivity() as? NaviActivity)?.getToken()
                    val uid = token.hashCode().toString()

                    val database = FirebaseDatabase.getInstance()
                    val paintsRef = database.getReference("paints")
                    val pidRef = database.getReference("pid")

                    pidRef.child("1").addListenerForSingleValueEvent(object : ValueEventListener {
                        override fun onDataChange(snapshot: DataSnapshot) {
                            val retrievedPid = snapshot.getValue(Pid::class.java)
                            if (snapshot.exists()) {
                                var Intpid = retrievedPid?.pid?.plus(1)
                                val setpid = Pid(Intpid)
                                pidRef.child("1").setValue(setpid)

                                val paint = Paints(
                                    Intpid.toString(),
                                    uid,
                                    "",  // 이미지 URL 설정
                                    "",
                                    0,
                                    imageUrl,
                                    ""
                                )
                                paintsRef.child(paint.pid!!).setValue(paint)
                            }
                            pathList.clear()
                            colorList.clear()
                        }

                        override fun onCancelled(e: DatabaseError) {
                            Log.d("yang", "데이터 호출 실패: $e")
                        }
                    })
                }
            }




            fos.close()
            Log.d("hyun", "bug 위치 : 비트맵 파일저장 파일닫기")
        }catch (e: IOException) {
            //오류시 처리 코드
            e.printStackTrace()
        }
    }
    private fun viewSave(view: PaintView) {
        //뷰가 측정된 후에 비트맵을 생성하도록 수정
        Log.d("hyun", "bug 위치 : view.post 실행 전")
        view.post{
            //뷰를 비트맵 이미지로 변환합니다.
            Log.d("hyun", "bug 위치 : 뷰 세이브로 가짐")
            val bitmap = getViewBitmap(view)
            Log.d("hyun", "bug 위치 : 뷰를 비트맵 이미지로 변환 성공")
            //저장할 파일 경로와 이름을 생성합니다.
            val filePath = getSaveFilePathName()
            Log.d("hyun", filePath)
            Log.d("hyun", "bug 위치 : 파일 경로와 이름 생성 성공")
            //비트맵 이미지를 파일로 저장합니다
            bitmapFileSave(bitmap, filePath)
            Log.d("hyun", "bug 위치 : 파일 저장 성공")

        }
    }

    private fun currentColor(color: Int){
        currentBrush = color
        path = Path()
    }

}