package com.jasonoh.day0624ex85firebasechatting01;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.DisplayMetrics;
import android.util.TypedValue;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewTreeObserver;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.Toast;

import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.ArrayList;
import java.util.Calendar;

public class ChattingActivity extends AppCompatActivity {

    ListView listView;
    ChatAdapter adapter;
    ArrayList<MessageItem> messageItems = new ArrayList<>();
    EditText etMsg;

    Button btnSend;
    LinearLayout chattingRoom;

    FirebaseDatabase firebaseDatabase;
    DatabaseReference chatRef;

    @Override
    protected void onResume() {
        super.onResume();
        listView.requestFocus();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chatting);

        //제목줄의 글씨는 보통 채팅방이름인데 본인 닉네임으로 할수 있음
        getSupportActionBar().setTitle( "Chatting Room ( " + Global.nickName + " )" );

        //getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE);

        etMsg = findViewById(R.id.et_msg);
        btnSend = findViewById(R.id.btn_send);
        listView = findViewById(R.id.list_view);
        adapter = new ChatAdapter( this, messageItems );
        listView.setAdapter(adapter);

        listView.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                //소프트 키패드를 안보이도록
                InputMethodManager imm =  (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                imm.hideSoftInputFromWindow(getCurrentFocus().getWindowToken(), 0); //flags 는 즉시하려면 0
                //imm.showSoftInput() //화면에 보일때는 show
                return false;
            }
        });

        //btnSend.setVisibility( View.INVISIBLE );
        //btnSend.setClickable( false );
        btnSend.setEnabled( false );
        btnSend.setTextColor( getResources().getColor( android.R.color.darker_gray ) );

        //Firebase DB 의 "chat" 이라는 이름의 자식노드에 채팅데이터들 저장
        // "chat" 이름을 변경하면 여러 채팅방이 가능
        firebaseDatabase = FirebaseDatabase.getInstance();
        chatRef = firebaseDatabase.getReference( "chat" );

        //채팅 메세지의 변경 내역에 관여하는 리스너 등록
        // ValueEventListener 는 값 변경시 마다 전체 데이터를 다시 줌
        //chatRef.addValueEventListener(); // 모든 값을 다시 가져오는 식으로 된다.
        // addChildEventListener() : 값이 하나가 바뀌었을때 바뀐 값을 가져오는 식
        chatRef.addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {
                //추가된 메세지 데이터 하나의 스냅샷을 줌
                MessageItem messageItem = dataSnapshot.getValue( MessageItem.class );

                //새로 추가된 아이템을 리스트에 추가
                messageItems.add(messageItem);
                adapter.notifyDataSetChanged();
                //리스트뷰의 커서위치를 가장 마지막 아이템 포지션으로
                listView.setSelection( messageItems.size() - 1 );
            }

            @Override
            public void onChildChanged(@NonNull DataSnapshot dataSnapshot, @Nullable String s) { }

            @Override
            public void onChildRemoved(@NonNull DataSnapshot dataSnapshot) { }

            @Override
            public void onChildMoved(@NonNull DataSnapshot dataSnapshot, @Nullable String s) { }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) { }
        });

        etMsg.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) { }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
//                if (etMsg.getText().toString().equals("")) btnSend.setVisibility(View.INVISIBLE);
//                else btnSend.setVisibility( View.VISIBLE );
                if (etMsg.getText().toString().equals("")) {
                    //btnSend.setClickable(false);
                    btnSend.setEnabled( false );
                    btnSend.setTextColor( getResources().getColor( android.R.color.darker_gray ) );
                }
                else {
                    //btnSend.setClickable(true);
                    btnSend.setEnabled( true );
                    btnSend.setTextColor( getResources().getColor( android.R.color.white ) );
                }

                //listView.setSelection( messageItems.size() - 1 );
            }

            @Override
            public void afterTextChanged(Editable s) { }
        });

//        listView.addOnLayoutChangeListener(new View.OnLayoutChangeListener() {
//            @Override
//            public void onLayoutChange(View v, int left, int top, int right, int bottom, int oldLeft, int oldTop, int oldRight, int oldBottom) {
//                Toast.makeText(ChattingActivity.this, "asdf", Toast.LENGTH_SHORT).show();
//                listView.setSelection( messageItems.size() - 1 );
//            }
//        });

    }

//    public void showFromKeyboard(){
//
//        //전체 액티비티의 화면중에 키보드가 올라왔다는 것을 알려주는 느낌!!
//        //https://developer88.tistory.com/109
//        //참고 사이트
//        chattingRoom = findViewById(R.id.chatting_room);
//        chattingRoom.getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
//            @Override
//            public void onGlobalLayout() {
//                int mRootViewHeight = chattingRoom.getRootView().getHeight();
//                int mChattingRoomHeight = chattingRoom.getHeight();
//                int mDiff = mRootViewHeight - mChattingRoomHeight;
//                if(mDiff > dpToPx(200)) listView.setSelection( messageItems.size() - 1 );
//            }
//        });
//    }
//
//    public float dpToPx(float valueInDp) {
//        DisplayMetrics metrics = getResources().getDisplayMetrics();
//        return TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, valueInDp, metrics);
//    }

    public void clickSend(View view) {

        //Firebase DB에 저장할 데이터들( 닉네임, 메세지, 시간, 이미지URL )
        String name = Global.nickName;
        String message = etMsg.getText().toString();
        String profileUrl = Global.profileUri;

        //메세지 작성 시간을 문자열로
        Calendar calendar = Calendar.getInstance();
        String time = calendar.get( Calendar.HOUR_OF_DAY ) + ":" + calendar.get(Calendar.MINUTE); //24시간 분량

        //객체를 한번에 저장하기 위해
        MessageItem messageItem = new MessageItem( name, message, time, profileUrl );

        chatRef.push().setValue( messageItem ); //객체를 한번에 저장

        etMsg.setText("");

        //소프트 키패드를 안보이도록
        InputMethodManager imm =  (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
        imm.hideSoftInputFromWindow(getCurrentFocus().getWindowToken(), 0); //flags 는 즉시하려면 0
        //imm.showSoftInput() //화면에 보일때는 show

    }
}
