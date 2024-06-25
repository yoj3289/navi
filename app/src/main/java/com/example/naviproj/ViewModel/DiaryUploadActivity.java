package com.example.naviproj.ViewModel;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.text.Editable;
import android.text.InputFilter;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.activity.result.ActivityResult;
import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import com.bumptech.glide.Glide;
import com.example.naviproj.R;
import com.example.naviproj.utility.FirebaseID;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.SetOptions;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.io.ByteArrayOutputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

public class DiaryUploadActivity extends AppCompatActivity {

    private ImageView iv_upload_back, iv_upload_image, ivEmoji;
    //private Button btn_upload, btn_upload_gallery;

    private TextView btn_upload;
    private EditText et_post_title;
    private EditText et_upload_contents;
    private FirebaseAuth mAuth = FirebaseAuth.getInstance();
    private FirebaseFirestore mStore = FirebaseFirestore.getInstance();
    private FirebaseStorage mStorage = FirebaseStorage.getInstance();

    private String uploadID;
    private String imageFileName;
    private String collectionId;
    private boolean isEditMode = false;
    private String name, documentId;

    private Uri imageUri;
    private String TAG = "IMAGE";

    private StorageReference storageRef = mStorage.getReference();

    long now = System.currentTimeMillis();
    Date mDate = new Date(now);

    SimpleDateFormat simpleDateFormat1 = new SimpleDateFormat("yyyy.MM.dd hh:mm:ss");
    String current_time = simpleDateFormat1.format(mDate);

    private UploadTask uploadTask = null;
    private String url;
    private String selectedEmoji = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_upload);

        iv_upload_back = findViewById(R.id.btn_back);
        btn_upload = findViewById(R.id.btn_upload);
        iv_upload_image = findViewById(R.id.iv_upload_image);
        et_post_title = findViewById(R.id.et_post_title);
        et_upload_contents = findViewById(R.id.et_upload_contents);
        TextView charCountTextView = findViewById(R.id.countText);

        InputFilter[] filterArray = new InputFilter[1];
        filterArray[0] = new InputFilter.LengthFilter(70); // 최대 글자 수를 70자로 제한
        et_upload_contents.setFilters(filterArray);
        et_upload_contents.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                // Do nothing
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                // 현재 글자 수를 업데이트
                int currentLength = s.length();
                charCountTextView.setText(currentLength + "/70");
            }

            @Override
            public void afterTextChanged(Editable s) {

            }

        });


        ivEmoji = findViewById(R.id.iv_emoji);

        documentId = mAuth.getCurrentUser().getUid();
        uploadID = mStore.collection(FirebaseID.diary).document().getId();

        imageFileName = "IMAGE_" + documentId + "_" + uploadID + "_.png";

        collectionId = getIntent().getStringExtra("diary_collectionid");
        if (collectionId != null) {
            isEditMode = true;
            loadDiaryData(collectionId);
        }

        iv_upload_back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent();
                setResult(RESULT_OK, intent);
                finish();
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

        ivEmoji.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showEmojiBottomSheet();
            }
        });

        if (mAuth.getCurrentUser() != null) {
            mStore.collection(FirebaseID.user).document(mAuth.getCurrentUser().getUid())
                    .get()
                    .addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                        @Override
                        public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                            if (task.isSuccessful()) {
                                DocumentSnapshot document = task.getResult();
                                if (document.exists()) {
                                    Log.d("TAG", "Document is exists");
                                    name = (String) document.getData().get(FirebaseID.name);
                                    documentId = (String) document.getData().get(FirebaseID.documentId);
                                } else
                                    Log.d("TAG", "Document is not exists");
                            }
                        }
                    });
        }

        btn_upload.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String title = et_post_title.getText().toString().trim();
                String contents = et_upload_contents.getText().toString().trim();

                if (!isEditMode) {
                    uploadNewDiaryData(title, contents);
                } else {
                    updateDiaryData(collectionId, title, contents);
                }
            }
        });
    }

    private void showEmojiBottomSheet() {
        BottomSheetDialog bottomSheetDialog = new BottomSheetDialog(this);
        View bottomSheetView = LayoutInflater.from(getApplicationContext())
                .inflate(R.layout.bottom_sheet_emoji, null);

        int[] emojiResources = {R.drawable.ic_pulsar, R.drawable.ic_smile, R.drawable.ic_anger, R.drawable.ic_boring, R.drawable.ic_sad, R.drawable.ic_joy,
                R.drawable.ic_surprise, R.drawable.ic_sick, R.drawable.ic_funny, R.drawable.ic_mid, R.drawable.ic_happy, R.drawable.ic_burnout,
                R.drawable.ic_tired, R.drawable.ic_love, R.drawable.ic_awkward, R.drawable.ic_none};

        ImageView[] emojiImageViews = {
                bottomSheetView.findViewById(R.id.iv_pulsar),
                bottomSheetView.findViewById(R.id.iv_smile),
                bottomSheetView.findViewById(R.id.iv_anger),
                bottomSheetView.findViewById(R.id.iv_boring),
                bottomSheetView.findViewById(R.id.iv_sad),
                bottomSheetView.findViewById(R.id.iv_joy),
                bottomSheetView.findViewById(R.id.iv_surprise),
                bottomSheetView.findViewById(R.id.iv_sick),
                bottomSheetView.findViewById(R.id.iv_funny),
                bottomSheetView.findViewById(R.id.iv_mid),
                bottomSheetView.findViewById(R.id.iv_happy),
                bottomSheetView.findViewById(R.id.iv_burnout),
                bottomSheetView.findViewById(R.id.iv_tired),
                bottomSheetView.findViewById(R.id.iv_love),
                bottomSheetView.findViewById(R.id.iv_awkward),
                bottomSheetView.findViewById(R.id.iv_none)
        };

        for (int i = 0; i < emojiImageViews.length; i++) {
            final int index = i;
            emojiImageViews[i].setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (index == emojiImageViews.length - 1) {
                        selectedEmoji = ""; // 선택 없음 클릭 시 빈 문자열 할당
                        ivEmoji.setImageResource(emojiResources[index]); // 선택한 이미지로 설정
                    } else {
                        selectedEmoji = "emoji" + (index + 1);
                        ivEmoji.setImageResource(emojiResources[index]);
                    }
                    bottomSheetDialog.dismiss();
                }
            });
        }

        ImageView ivClose = bottomSheetView.findViewById(R.id.iv_close);
        ivClose.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                bottomSheetDialog.dismiss();
            }
        });

        bottomSheetDialog.setContentView(bottomSheetView);
        bottomSheetDialog.show();
    }

    private Bitmap resizeImage(Uri imageUri) {
        try {
            BitmapFactory.Options options = new BitmapFactory.Options();
            options.inJustDecodeBounds = true;
            BitmapFactory.decodeStream(getContentResolver().openInputStream(imageUri), null, options);

            int width = options.outWidth;
            int height = options.outHeight;
            int scaleFactor = 1;

            if (width > 1024 || height > 1024) {
                scaleFactor = Math.min(width / 1024, height / 1024);
            }

            options.inJustDecodeBounds = false;
            options.inSampleSize = scaleFactor;

            Bitmap resizedBitmap = BitmapFactory.decodeStream(getContentResolver().openInputStream(imageUri), null, options);
            return resizedBitmap;
        } catch (FileNotFoundException e) {
            e.printStackTrace();
            return null;
        }
    }

    private void compressImage(Bitmap bitmap, Map<String, Object> data) {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.JPEG, 80, baos);
        byte[] imageData = baos.toByteArray();

        StorageReference storageRef = mStorage.getReference().child("image").child(documentId).child(imageFileName);
        UploadTask uploadTask = storageRef.putBytes(imageData);
        uploadTask.addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
            @Override
            public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                storageRef.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                    @Override
                    public void onSuccess(Uri uri) {
                        url = uri.toString();
                        Log.d("uri : ", uri.toString());
                        data.put("url", url);
                        saveDiaryData(data);
                    }
                }).addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        // Handle failure
                    }
                });
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Log.d(TAG, "onFailure: upload");
            }
        });
    }

    private void uploadNewDiaryData(String title, String contents) {
        Map<String, Object> data = new HashMap<>();
        data.put(FirebaseID.documentId, mAuth.getCurrentUser().getUid());
        data.put(FirebaseID.name, name);
        data.put(FirebaseID.title, title);
        data.put(FirebaseID.contents, contents);
        data.put(FirebaseID.collectionId, uploadID);
        data.put(FirebaseID.time, FieldValue.serverTimestamp());
        data.put(FirebaseID.timestamp, current_time);

        if (!selectedEmoji.isEmpty()) {
            data.put("emoji", selectedEmoji);
        }

        if (imageUri != null) {
            Bitmap resizedBitmap = resizeImage(imageUri);
            if (resizedBitmap != null) {
                compressImage(resizedBitmap, data);
            }
        } else {
            saveDiaryData(data);
        }
    }

    private void saveDiaryData(Map<String, Object> data) {
        mStore.collection("diary")
                .document("diarypage")
                .collection("pages")
                .document(uploadID)
                .set(data, SetOptions.merge())
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        finish();
                    }
                }).addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Log.d(TAG, "Failed to save diary data: " + e.getMessage());
                    }
                });
    }

    private void loadDiaryData(String collectionId) {
        mStore.collection(FirebaseID.diary)
                .document("diarypage")
                .collection("pages")
                .document(collectionId)
                .get()
                .addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                        if (task.isSuccessful()) {
                            DocumentSnapshot document = task.getResult();
                            if (document.exists()) {
                                String title = document.getString(FirebaseID.title);
                                String contents = document.getString(FirebaseID.contents);
                                String imageUrl = document.getString("url");
                                String emojiString = document.getString("emoji");

                                et_post_title.setText(title);
                                et_upload_contents.setText(contents);

                                if (imageUrl != null && !imageUrl.isEmpty()) {
                                    Glide.with(DiaryUploadActivity.this)
                                            .load(imageUrl)
                                            .into(iv_upload_image);
                                }

                                if (emojiString != null && !emojiString.isEmpty()) {
                                    selectedEmoji = emojiString;
                                    int emojiResource = getEmojiResource(emojiString);
                                    if (emojiResource != 0) {
                                        ivEmoji.setImageResource(emojiResource);
                                    }
                                }
                            }
                        }
                    }
                });
    }

    private int getEmojiResource(String emojiString) {
        switch (emojiString) {
            case "emoji1":
                return R.drawable.ic_pulsar;
            case "emoji2":
                return R.drawable.ic_smile;
            case "emoji3":
                return R.drawable.ic_anger;
            case "emoji4":
                return R.drawable.ic_boring;
            case "emoji5":
                return R.drawable.ic_sad;
            case "emoji6":
                return R.drawable.ic_joy;
            case "emoji7":
                return R.drawable.ic_surprise;
            case "emoji8":
                return R.drawable.ic_sick;
            case "emoji9":
                return R.drawable.ic_funny;
            case "emoji10":
                return R.drawable.ic_mid;
            case "emoji11":
                return R.drawable.ic_happy;
            case "emoji12":
                return R.drawable.ic_burnout;
            case "emoji13":
                return R.drawable.ic_tired;
            case "emoji14":
                return R.drawable.ic_love;
            case "emoji15":
                return R.drawable.ic_awkward;
            case "emoji16":
                return R.drawable.ic_none;
            default:
                return 0;
        }
    }


    private void updateDiaryData(String collectionId, String title, String contents) {
        Map<String, Object> data = new HashMap<>();
        data.put(FirebaseID.title, title);
        data.put(FirebaseID.contents, contents);
        data.put(FirebaseID.timestamp, current_time);

        if (selectedEmoji.isEmpty()) {
            // 선택없음 이모지를 선택한 경우, "emoji" 키를 제거
            data.put("emoji", FieldValue.delete());
        } else {
            data.put("emoji", selectedEmoji);
        }

        if (imageUri != null) {
            Bitmap resizedBitmap = resizeImage(imageUri);
            if (resizedBitmap != null) {
                compressImage(resizedBitmap, data, collectionId);
            }
        } else {
            saveUpdatedDiaryData(collectionId, data);
        }
    }

    private void compressImage(Bitmap bitmap, Map<String, Object> data, String collectionId) {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.JPEG, 80, baos);
        byte[] imageData = baos.toByteArray();

        StorageReference storageRef = mStorage.getReference().child("image").child(documentId).child(imageFileName);
        UploadTask uploadTask = storageRef.putBytes(imageData);
        uploadTask.addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
            @Override
            public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                storageRef.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                    @Override
                    public void onSuccess(Uri uri) {
                        url = uri.toString();
                        Log.d("uri : ", uri.toString());
                        data.put("url", url);
                        saveUpdatedDiaryData(collectionId, data);
                    }
                }).addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        // Handle failure
                    }
                });
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Log.d(TAG, "onFailure: upload");
            }
        });
    }

    // 수정된 일기 데이터 저장 메서드
    private void saveUpdatedDiaryData(String collectionId, Map<String, Object> data) {
        mStore.collection(FirebaseID.diary)
                .document("diarypage")
                .collection("pages")
                .document(collectionId)
                .set(data, SetOptions.merge())
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        Intent intent = new Intent();
                        intent.putExtra("updated_position", getIntent().getIntExtra("selectedPosition", 0));
                        setResult(RESULT_OK, intent);
                        finish();
                    }
                }).addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Log.d(TAG, "Failed to update diary data: " + e.getMessage());
                    }
                });
    }

    // 클릭 시 갤러리 이동
    ActivityResultLauncher<Intent> startActivityResult = registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), new ActivityResultCallback<ActivityResult>() {
        @Override
        public void onActivityResult(ActivityResult result) {
            if (result.getResultCode() == RESULT_OK && result.getData() != null) {
                imageUri = result.getData().getData();
                try {
                    Bitmap bitmap = MediaStore.Images.Media.getBitmap(getContentResolver(), imageUri);
                    iv_upload_image.setImageBitmap(bitmap);
                } catch (FileNotFoundException e) {
                    e.printStackTrace();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    });

    void downloadUri() {
        storageRef.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
            @Override
            public void onSuccess(Uri uri) {
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Log.d(TAG, "onFailure: download");
            }
        });
    }

    public void replaceFragment(Fragment fragment) {
        FragmentManager fragmentManager = getSupportFragmentManager();
        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
        fragmentTransaction.replace(R.id.btn_back, fragment);
        fragmentTransaction.commit();
    }
}