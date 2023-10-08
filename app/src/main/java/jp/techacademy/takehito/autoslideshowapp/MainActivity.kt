package jp.techacademy.takehito.autoslideshowapp

import android.content.ContentUris
import android.content.pm.PackageManager
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.provider.MediaStore
import android.util.Log
import jp.techacademy.takehito.autoslideshowapp.databinding.ActivityMainBinding
import androidx.activity.result.contract.ActivityResultContracts
import java.util.*


class MainActivity : AppCompatActivity() {

    //パーミッションの確認処理
    private lateinit var binding: ActivityMainBinding
    private val PERMISSIONS_REQUEST_CODE = 100
    private var timer: Timer? = null

    private val requestPermissionLauncher =
        registerForActivityResult(ActivityResultContracts.RequestPermission()) { isGranted ->
            if (isGranted) {
                Log.d("ANDROID", "許可された")
            } else {
                Log.d("ANDROID", "許可されなかった")
            }
        }

    // APIレベルによって許可が必要なパーミッションを切り替える
    private val readImagesPermission =
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) android.Manifest.permission.READ_MEDIA_IMAGES
        else android.Manifest.permission.READ_EXTERNAL_STORAGE

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        val view = binding.root
        setContentView(view)

        // パーミッションの許可状態を確認する
        if (checkSelfPermission(readImagesPermission) == PackageManager.PERMISSION_GRANTED) {
            // 許可されている
            buttonAction(3)
            //getContentsInfo()
        } else {
            // 許可されていないので許可ダイアログを表示する
            requestPermissions(
                arrayOf(readImagesPermission),
                PERMISSIONS_REQUEST_CODE
            )
        }

        //各ボタン　下記buttonActionの中のwhenで分岐
        binding.nextButton.setOnClickListener {
            buttonAction(0)
        }  //進むボタン
        binding.playButton.setOnClickListener {
            buttonAction(2)
        }  //再生・停止ボタン
        binding.backButton.setOnClickListener {
            buttonAction(1)
        }  //戻るボタン
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<String>,
        grantResults: IntArray,
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        when (requestCode) {
            PERMISSIONS_REQUEST_CODE ->
                if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    buttonAction(3)
                }
        }
    }

    private var currentPosition: Int = 0

    private fun buttonAction(selectNo: Int) {
        val resolver = contentResolver
        val cursor = resolver.query(
            MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
            null,
            null,
            null,
            null
        )

        //画像取得のためのURIを作成する
        fun uriParameters() {
            val fieldIndex = cursor!!.getColumnIndex(MediaStore.Images.Media._ID)
            val id = cursor.getLong(fieldIndex)
            val imageUri =
                ContentUris.withAppendedId(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, id)
            binding.imageView.setImageURI(imageUri)
        }
        //selectNoをLogで確認
        Log.d("ANDROID", "selectNo : $selectNo")

        //アプリ起動の画面、進む、再生・停止、戻るボタンの分岐処理
        when (selectNo) {
            0 -> {
                if (cursor!!.moveToPosition(currentPosition + 1)) {
                    uriParameters()
                    currentPosition++
                } else {

                    cursor.moveToFirst()
                    uriParameters()
                    currentPosition = 0
                }
            }   //進むボタン
            1 -> {
                if (cursor!!.moveToPosition(currentPosition - 1)) {
                    uriParameters()
                    currentPosition--
                } else {
                    cursor.moveToLast()
                    uriParameters()
                    currentPosition = cursor.count - 1
                }
            }  //戻るボタン
            2 -> {
                if (binding.playButton.text == "再生") {
                    binding.nextButton.isEnabled = false
                    binding.backButton.isEnabled = false
                    binding.playButton.text = "停止"
                    timer = Timer()
                    timer?.scheduleAtFixedRate(object : TimerTask() {
                        override fun run() {
                            runOnUiThread {
                                buttonAction(0)
                            }
                        }
                    }, 2000, 2000)
                } else {
                    binding.playButton.text = "再生"
                    binding.nextButton.isEnabled = true
                    binding.backButton.isEnabled = true
                    timer?.cancel()
                }
            }   //再生・停止ボタン（画像自動送り及びボタン表示変更）
            3 -> {
                if (cursor!!.moveToFirst()) {
                    uriParameters()
                }
            }  //アプリ起動画面
        }
        cursor?.close()
    }
}



