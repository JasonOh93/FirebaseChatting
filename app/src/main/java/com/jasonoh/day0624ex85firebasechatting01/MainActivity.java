package com.jasonoh.day0624ex85firebasechatting01;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.Manifest;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.text.SimpleDateFormat;
import java.util.Date;

import de.hdodenhof.circleimageview.CircleImageView;

public class MainActivity extends AppCompatActivity {

    CircleImageView ivProfile;
    EditText etName;

    //프로필 이미지 uri 참조변수
    Uri imgUri;

    //데이터의 변경이 있었는지 여부
    boolean isChanged = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        getSupportActionBar().setTitle( "Chatting Title" );

        ivProfile = findViewById(R.id.iv_profile);
        etName = findViewById(R.id.et);

        //이미 저장되어있는 정보들 읽어오기
        loadData();
        if(Global.nickName != null) {
            //저장된 것이 있다면
            etName.setText(Global.nickName);
            Glide.with(this).load(Global.profileUri).into(ivProfile);
        }

    }

    //데이터를 저장하는 메소드
    public void saveData(){

        //프로필 이미지와 채팅명을 Firebase DB에 저장
        Global.nickName = etName.getText().toString();
        //Global.profileUri = imgUri.toString(); //Uri의 정보(Full 경로)를 저장

        //먼저 이미지 파일부터 Firebase Storage 에 업로드

        //업로드할 파일 명이 같으면 안되므로 날짜를 이용해서 파일명 지정
        SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMddhhmmss");
        String fileName = sdf.format( new Date()) + ".png";

        FirebaseStorage firebaseStorage = FirebaseStorage.getInstance();
        final StorageReference imgRef = firebaseStorage.getReference("profileImages/" + fileName);

        //이미지 업로드
        UploadTask task = imgRef.putFile( imgUri );
        task.addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
            @Override
            public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                //firebase 실시간 DB에 저장할 저장소에 업로드된 실제 인터넷 경로(URL)을 알아내기
                imgRef.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                    @Override
                    public void onSuccess(Uri uri) {
                        //다운로드 URL을 Global.profileUri에 저장
                        Global.profileUri = uri.toString();
                        //firebase 에 저장된 이미지 파일의 경로를 fire DB에 저장하고 내 디바이스에도 저장

                        //1. FirebaseDB 저장 [Global.nickName, imgUri의 다운로드 URL]
                        FirebaseDatabase firebaseDatabase = FirebaseDatabase.getInstance();
                        //"profiles" 이라는 이름의 자식 노드 참조 객체
                        DatabaseReference profileRef = firebaseDatabase.getReference("profiles");
                        //nickName을 키값으로 지정한 노드에 이미지 경로를 값으로 지정
                        //profileRef.child( Global.nicName ).setValue( Global.profileUri );
                        profileRef.child( Global.nickName ).setValue( Global.profileUri );

                        //2. SharedPreferences 이용하여 저장 [Global.nickName, Global.profileUri]
                        SharedPreferences pref = getSharedPreferences( "account", MODE_PRIVATE );
                        SharedPreferences.Editor editor = pref.edit();
                        editor.putString( "nickName", Global.nickName );
                        editor.putString("profileUrl", Global.profileUri);

                        editor.commit();

                        Toast.makeText(MainActivity.this, "저장 완료", Toast.LENGTH_SHORT).show();

                        //모든 저장이 끝났으므로
                        //채팅화면으로 이동

                        //채팅방으로 이동
                        //Intent intent = new Intent(this, ChattingActivity.class);
                        startActivity( new Intent(MainActivity.this, ChattingActivity.class) );

                        finish();

                    }
                });
            }
        });

    }

    public void clickBtn(View view) {

        //데이터의 변경이 있었는지
        if(isChanged) saveData();
        else if((!isChanged) && !Global.nickName.equals(etName.getText().toString())) {
            Global.nickName = etName.getText().toString();
            //firebase 에 저장된 이미지 파일의 경로를 fire DB에 저장하고 내 디바이스에도 저장

            //1. FirebaseDB 저장 [Global.nickName, imgUri의 다운로드 URL]
            FirebaseDatabase firebaseDatabase = FirebaseDatabase.getInstance();
            //"profiles" 이라는 이름의 자식 노드 참조 객체
            DatabaseReference profileRef = firebaseDatabase.getReference("profiles");
            //nickName을 키값으로 지정한 노드에 이미지 경로를 값으로 지정
            profileRef.child( Global.nickName ).setValue( Global.profileUri );

            //2. SharedPreferences 이용하여 저장 [Global.nickName, Global.profileUri]
            SharedPreferences pref = getSharedPreferences( "account", MODE_PRIVATE );
            SharedPreferences.Editor editor = pref.edit();
            editor.putString( "nickName", Global.nickName );
            editor.putString("profileUrl", Global.profileUri);
            editor.commit();

            startActivity( new Intent(this, ChattingActivity.class) );
            finish();

        } else {
            startActivity( new Intent(this, ChattingActivity.class) );
            finish();
        }

    }

    //디바이스에 저장된 정보 읽어오기
    void loadData(){
        SharedPreferences pref = getSharedPreferences("account", MODE_PRIVATE);
        Global.nickName = pref.getString( "nickName", null );
        Global.profileUri = pref.getString( "profileUrl", null );
    }

    public void clickImage(View view) {
        Intent intent = new Intent(Intent.ACTION_PICK);
        intent.setType( "image/*" );
        startActivityForResult( intent, 100 );
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if( requestCode == 100 && resultCode == RESULT_OK ) {
            imgUri = data.getData();
            if(imgUri != null) {
                Glide.with(this).load(imgUri).into(ivProfile);

                //프로필 이미지 바뀌었다고 표시
                isChanged = true;
            }
        }

    }
}
