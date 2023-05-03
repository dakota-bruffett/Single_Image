package com.example.single_image

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.os.Environment
import android.provider.MediaStore
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.ImageButton
import android.widget.ProgressBar
import androidx.activity.result.ActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.FileProvider
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.ktx.storage
import com.squareup.picasso.Callback
import com.squareup.picasso.Picasso
import java.io.File
import java.io.IOException
import java.lang.Exception
import java.text.SimpleDateFormat
import java.util.*

const val TAG = "Main_Activity"
class MainActivity : AppCompatActivity() {
    private lateinit var PictureImage :ImageButton
    private lateinit var Content : View
    private lateinit var uploadButton: Button
    private lateinit var loadingImagebar: ProgressBar


    private var newPhotoPath : String? = null
    private var visiableImagePath : String? = null
    private var imageFilename: String?= null
    private var photoUri: Uri? = null


    private val storage = Firebase.storage
    private val NEW_PHOTO_KEY = "heres is a Photo key"
    private val VISABLE_KEY = "here is your visual key"

    private val cameraActivityLanucher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()){
        result -> handleImage(result)
    }





    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        PictureImage = findViewById(R.id.PictureButton)
        loadingImagebar=findViewById(R.id.LoadingImage)
        uploadButton = findViewById(R.id.uploadButton)
        uploadButton.setOnClickListener {
            uploadImage()
        }

        PictureImage.setOnClickListener{
            TakePicture()
        }
    }

    private fun uploadImage() {
        if(photoUri != null && imageFilename != null){

            loadingImagebar.visibility= View.VISIBLE

            val imageStorageRootReference = storage.reference
            val imageCollectionReference = imageStorageRootReference.child("image")
            val imageFileReference = imageCollectionReference.child(imageFilename!!)
            imageFileReference.putFile(photoUri!!).addOnCompleteListener{
                Snackbar.make(Content, "ImageUploaded",Snackbar.LENGTH_LONG).show()
            }.addOnFailureListener{
                error -> Snackbar.make(Content, "ImageUploaded",Snackbar.LENGTH_LONG).show()
                Log.e(TAG, "Failed to upload image $imageFilename", error)
            }

        }
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        outState.putString(NEW_PHOTO_KEY,newPhotoPath)
        outState.putString(VISABLE_KEY,visiableImagePath)
    }

   private  fun TakePicture() {
        val TakePictureIntent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
       val (photoFile,PathPhotoFile) = createImageFile()
       if (photoFile != null ){
           newPhotoPath = PathPhotoFile
           val photoUri = FileProvider.getUriForFile(
               this,
               "com.example.Single_Image.fileProvider",
               photoFile
           )
           TakePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT,photoUri)
           cameraActivityLanucher.launch(TakePictureIntent)
       }

    }
    private fun createImageFile(): Pair<File?,String?> {
        try{
            val dateTime = SimpleDateFormat("yyyyMMdd_HHmmss").format(Date())
             imageFilename = "Single Single_$dateTime"
            val StorageDir = getExternalFilesDir(Environment.DIRECTORY_PICTURES)
            val file = File.createTempFile(imageFilename!!,"jpg",StorageDir)
            val filepath = file.absolutePath
            return file to filepath
        }catch (ex:IOException){
            return null to null
        }

    }
    private fun handleImage(result: ActivityResult?) {
        if (result != null) {
            when(result.resultCode){
                RESULT_OK ->{
                    Log.d(TAG,"Result is all good and ready to be used ")
                    visiableImagePath = newPhotoPath

                }
                RESULT_CANCELED ->
                    Log.d(TAG,"You cancelled your image")
            }
        }
        }

    override fun onWindowFocusChanged(hasFocus: Boolean) {
        super.onWindowFocusChanged(hasFocus)
        Log.d(TAG,"The window changed $hasFocus visable image ar $newPhotoPath")
        if (hasFocus) {
            visiableImagePath?.let { imagepath->
                loadImage(PictureImage, imagepath)
            }
        }
    }

    private fun loadImage(pictureImage: ImageButton, imagepath: String) {
            Picasso.get()
                .load(imagepath)
                .error(android.R.drawable.stat_notify_error)
                .fit()
                .centerCrop()
                .into(PictureImage, object: Callback {
                    override fun onSuccess() {
                        Log.d(TAG, "Loaded image$pictureImage")
                    }

                    override fun onError(e: Exception?) {
                        Log.e(TAG,"Failed to load image $pictureImage",e)
                    }

                })


    }
}