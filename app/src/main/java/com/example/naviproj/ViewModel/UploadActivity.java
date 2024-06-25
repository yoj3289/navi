package com.example.naviproj.ViewModel;

import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.activity.result.ActivityResult;
import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.example.naviproj.R;
import com.example.naviproj.utility.FirebaseID;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.SetOptions;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

public class UploadActivity extends AppCompatActivity {

    private ImageView iv_upload_back, iv_upload_image;

    private Button btn_upload, btn_upload_gallery;

    private EditText et_post_title;
    private EditText et_upload_contents;
    private FirebaseAuth mAuth = FirebaseAuth.getInstance();
    private FirebaseFirestore mStore = FirebaseFirestore.getInstance();
    private FirebaseStorage mStorage = FirebaseStorage.getInstance();


    private String name, documentId;

    private Uri imageUri;
    private String TAG = "IMAGE";

    String uploadID = mStore.collection(FirebaseID.upload).document().getId();
    private StorageReference storageRef = mStorage.getReference();

    long now = System.currentTimeMillis();
    Date mDate = new Date(now);
    // 날짜, 시간의 형식 설정
    SimpleDateFormat simpleDateFormat1 = new SimpleDateFormat("yyyy.MM.dd hh:mm:ss");
    String current_time = simpleDateFormat1.format(mDate);

    private UploadTask uploadTask = null; // 파일 업로드하는 객체
    private String imageFileName = "IMAGE_" + documentId + "_" + uploadID + "_.png"; // 파일명
    private String url;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_upload);

        iv_upload_back = findViewById(R.id.btn_back);

        btn_upload = findViewById(R.id.btn_upload);

        iv_upload_image = findViewById(R.id.iv_upload_image);


        et_post_title = findViewById(R.id.et_post_title);
        et_upload_contents = findViewById(R.id.et_upload_contents);

        iv_upload_back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(); // 인텐트 객체 생성하고
                setResult(RESULT_OK, intent); // 응답 보내기
                finish(); // 현재 액티비티 없애기
            }
        });

        btn_upload_gallery.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent();
                intent.setType("image/*");
                intent.setAction(Intent.ACTION_GET_CONTENT);
                startActivityResult.launch(intent);
            }
        });

        iv_upload_image.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent();
                intent.setType("image/*");
                intent.setAction(Intent.ACTION_GET_CONTENT);
                startActivityResult.launch(intent);
            }
        });

        // 현재 사용자의 데이터 가져오기
        if (mAuth.getCurrentUser() != null) {
            mStore.collection(FirebaseID.user).document(mAuth.getCurrentUser().getUid())
                    .get()
                    .addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                        @Override
                        public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                            if (task.isSuccessful()) {
                                DocumentSnapshot document = task.getResult();
                                if (document.exists()) {
                                    name = (String) document.getData().get(FirebaseID.name);
                                    documentId = (String) document.getData().get(FirebaseID.documentId);
                                } else{

                                }
                            }
                        }
                    });
        }

        btn_upload.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(imageUri == null){
                    //갤러리에서 사진을 선택하지 않은 경우
                    Toast.makeText(UploadActivity.this, "갤러리에서 사진을 선택해주세요.", Toast.LENGTH_SHORT).show();
                    return;
                }
                //사진을 스토리지에 올리는 코드
                //이미지 파일 경로 지정 (/item/사용자 documentId/IAMGE_DOCUMENTID_UPLOADID.png)
                storageRef = mStorage.getReference().child("image").child(documentId).child(imageFileName);
                uploadTask = storageRef.putFile(imageUri); // 업로드할 파일과 업로드할 위치 설정
                //파일 업로드 시작
                uploadTask.addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                   @Override
                    public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                        // 업로드 성공시 동작
                        downloadUri(); //업로드 성공 시 업로드한 파일 Uri 다운받기
                        storageRef.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                            @Override
                            public void onSuccess(Uri uri) {
                                url = uri.toString();

                                //작성한 글, 닉네임 등을 파이어스토어의 upload 컬렉션에 올리는 코드
                                Map<String, Object> data = new HashMap<>();
                                data.put(FirebaseID.documentId, mAuth.getCurrentUser().getUid());
                                data.put(FirebaseID.name, name);
                                data.put(FirebaseID.title, et_post_title.getText().toString());
                                data.put(FirebaseID.contents, et_upload_contents.getText().toString());
                                data.put(FirebaseID.collectionId, uploadID);
                                data.put(FirebaseID.image, imageUri);
                                data.put(FirebaseID.time, FieldValue.serverTimestamp());
                                data.put(FirebaseID.timestamp, current_time);
                                data.put("TOTAL_SCORE", 0);
                                data.put("url", url);
                                mStore.collection(FirebaseID.upload).document(uploadID).set(data, SetOptions.merge());

                                finish();
                            }
                        }).addOnFailureListener(new OnFailureListener() {
                            @Override
                            public void onFailure(@NonNull Exception e) {
                                // 다운로드 URL 가져오기 실패시 동작
                            }
                        });
                    }
                }).addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        // 업로드 실패시 동작

                    }
                });
            }
        });
    }

    // 클릭 시 갤러리로 이동하는 코드
    ActivityResultLauncher<Intent> startActivityResult = registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), new ActivityResultCallback<ActivityResult>() {
        @Override
        public void onActivityResult(ActivityResult result) {
            if (result.getResultCode() == RESULT_OK && result.getData() != null){
                imageUri = result.getData().getData();
                try{
                    Bitmap bitmap = MediaStore.Images.Media.getBitmap(getContentResolver(), imageUri);
                    iv_upload_image.setImageBitmap(bitmap); //이미지를 띄울 이미지뷰 설정
                } catch (FileNotFoundException e) {
                    e.printStackTrace();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    });


    // 지정한 경로(reference)에 대한 uri을 다운로드하는 메서드
    // uri를 통해 이미지를 불러올 수 있음
    void downloadUri(){
        storageRef.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
            @Override
            public void onSuccess(Uri uri) {
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) { ;
            }
        });
    }




}
