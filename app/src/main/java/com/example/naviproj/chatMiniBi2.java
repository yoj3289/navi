//공공데이터 포털 api사전준비 필요
//4-2는 식품의약품안전처_의약품 낱알식별 정보->일반 인증키
//4-3은 식품의약품안전처_의약품개요정보(e약은요)->일반 인증키
//4-4는 식품의약품안전처_의약품 제품 허가정보->일반 인증키
package com.example.naviproj;

import android.content.Context;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.ImageView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserFactory;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.net.URL;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.AbstractMap;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class chatMiniBi2 extends AppCompatActivity {

    private Context context;

    private DataBaseHelper dbHelper;

    private TextInputEditText inputChat;

    private LinearLayoutManager layoutManager;
    private RecyclerView showRecyclerView;

    private String userChat;

    private List<ChatMediItem> ChatMediList;

    private ChatMediAdapter chatMediAdapter;

    private String group="";//그룹 정보를 저자하기 위함

    private String speaker = ""; //채팅을 올리는게 컴퓨터인지 사람인지 구분위함

    private ArrayList<String> mediNameSave = new ArrayList<>(); //약 이름 저장을 위한 리스트

    private ArrayList<String> mediCodeSave = new ArrayList<>(); //약 코드 저장을 위한 리스트

    private ArrayList<String> mediInfoSave = new ArrayList<>(); //약 정보 저장을 위한 리스트(정보는 약의 효능과 복용법 보관법을 알려줌)
    private String mediSideEffectSave=""; //부작용 저장을 위한 리스트
    private int mediCount = 0; //검색개수 저장

    private boolean constraint=false; //사용자에게 특정 응답만 받아야 할 때 true로 전환

    //약 정보, 부작용 2가지에 대한 대화 10개를 넣어놓음

    private String[] conversationData = {"타이레놀 정보 알려줘", "타이레놀에 대해서","타이레놀에 대해서 알려줘","타이레놀 무슨약이야?","타이레놀이 무슨 약인지 알려줘",
            "타이레놀 부작용 알려줘","타이레놀 문제점","타이레놀 먹을 때 주의할 점","타이레놀 부작용이 뭐야?","타이레놀의 부작용"};

    Map<String, String> groupMap = createGroupMap(conversationData);

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        dbHelper = new DataBaseHelper(this);
        setContentView(R.layout.chat_minibi_medi); // XML 파일과 연결


        Toolbar toolbar = findViewById(R.id.custom_toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // 뒤로가기 버튼 클릭 시 수행할 동작 추가
                finish();
            }
        });

        showRecyclerView = findViewById(R.id.chatRecyclerView);

        // LinearLayoutManager를 가로 방향으로 설정
        layoutManager = new LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false);
        showRecyclerView.setLayoutManager(layoutManager);

        // 체중정보 리사이클러뷰 어댑터 설정 (가상의 데이터 사용)
        chatMediAdapter = new ChatMediAdapter(getchatMedi()); //기본 내용
        showRecyclerView.setAdapter(chatMediAdapter);

        // 다이얼로그 안의 위젯들을 참조
        TextInputLayout chatInputLayout = findViewById(R.id.inputChat);//체중참조
        inputChat = chatInputLayout.findViewById(R.id.editChatInput);


        ImageView btnSend = findViewById(R.id.btnSend);


        // 채팅 알고리즘
        btnSend.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                inputChat.clearFocus();

                layoutManager.setStackFromEnd(true);
                showRecyclerView.scrollToPosition(chatMediAdapter.getItemCount() - 1);

                // InputMethodManager를 가져옴
                InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);

                // 현재 활성화된 뷰에서 키보드를 숨김
                imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
                userChat = inputChat.getText().toString();
                setUserChat(userChat);

                if(constraint==false){
                    basicChatLoop();
                }

                //몇번 약인지 입력받을 때 사용..?
                else{
                    Pattern pattern = Pattern.compile("\\d+"); // 숫자 찾기 위한 정규표현식 패턴
                    Matcher matcher = pattern.matcher(userChat);
                    if (matcher.find()) {
                        findSpecific(group, userChat);
                    }
                    else{
                        basicChatLoop();
                    }
                }
                inputChat.setText("");
            }

        });

    }

    private void basicChatLoop(){//기본적인 대화
        while(true){
            AbstractMap.SimpleEntry<String, Double> result = findMostSimilarString(userChat, conversationData);
            double value = result.getValue();
            double roundedValue = Math.round(value * 1000.0) / 1000.0; // 소수 셋째 자리에서 반올림
            if(roundedValue>=0.5){ //유사도가 0.5 이상일 때만 작업 수행
                group = groupMap.getOrDefault(result.getKey(), "null");
                String newStr="";//약의 이름만 추출해서 새롭게 저장하려는 용도
                if(group=="그룹1"||group=="그룹2"){
                    newStr=sliceName(userChat);//문장에서 조사를 제거하고 약의 이름만 추출하기
                    findGeneral(newStr); //사용자가 입력한 키워드가 들어간 약의 이름부터 구함
                    break;
                }
            }
            else{
                setComChat("질문을 파악하지 못했어요.\n1. (약이름) 정보 알려줘\n" +
                        "2. (약이름) 부작용 알려줘\n와 같이 입력 해 주세요");
                return;

            }
        }
    }
    private List<ChatMediItem> getchatMedi() {
        // 가상의 데이터를 생성하거나 실제 데이터를 가져오는 로직을 추가
        ChatMediList = new ArrayList<>();

        ChatMediList.add(new ChatMediItem("안녕하세요!\n알약에 대한 정보를 알려줄 MiniBi에요!\n\n아래 형식으로 말하면 도움을 줄 수 있어요\n" +
                "1. (약이름) 정보 알려줘\n2. (약이름) 부작용 알려줘\n와 같이 입력 해 주세요", "MiniBi"));

        return ChatMediList;
    }
    private List<ChatMediItem> setUserChat(String message) {//사용자가 입력한 내용을 화면에 띄워줌
        // 가상의 데이터를 생성하거나 실제 데이터를 가져오는 로직을 추가
        //ChatMediList.clear();

        ChatMediList.add(new ChatMediItem(message,"You"));

        chatMediAdapter.notifyItemInserted(1);
        chatMediAdapter.notifyDataSetChanged();


        return ChatMediList;
    }

    private List<ChatMediItem> setComChat(String message) {//응답을 화면에 띄워줌
        // 가상의 데이터를 생성하거나 실제 데이터를 가져오는 로직을 추가
        //ChatMediList.clear();

        ChatMediList.add(new ChatMediItem(message,"MiniBi"));

        chatMediAdapter.notifyItemInserted(1);
        chatMediAdapter.notifyDataSetChanged();

        return ChatMediList;
    }

    private String sliceName(String name){//첫번째 조사 이후는 모두 잘라내고 약이름 키워드만 남겨줌
        String newName=name;
        String[] sliceParticle=new String[]{"은 ","는 ","이 ","가 ","에 ","게 ","서 ","로 "," "}; //조사 제거, 조사 없이 정보 알려줘, 무슨약이야 등의 질문에 대처 위해 첫공백 제거, 단 조사제거를 우선시로 해서 조사가 없는 경우에만 하기 위해 맨 뒤로 배치
        for(int i=0;i<sliceParticle.length;i++){
            newName = name.replaceAll(sliceParticle[i]+".*", "");
            if(newName!=name) {
                break;
            }
        }
        return newName;
    }

    private void findGeneral(String keyWord){//사용자가 입력한 키워드가 들어간 약의 목록 일부를 찾아옴
        String getKeyWord=keyWord;
        //String getGroup=group;
        new Thread(new Runnable() {
            @Override
            public void run() {
                findNameList(getKeyWord); //키워드가 포함된 약의 이름 목록을 가져오기, 새로운 스레드로 실행해야 오류 없음
                if(mediNameSave.size()==0){
                    findNameList2(getKeyWord);
                }

                // UI 스레드로 작업 전환
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        // 결과 처리 및 UI 업데이트
                        String res = "";
                        if (mediNameSave.size() == 0) {
                            res += "검색 결과가 없어요! 약의 이름을 다시 입력 하거나\n구문에 맞춰 질문을 다시 해 보세요";
                            constraint=false;
                            ChatMediList.add(new ChatMediItem(res,"MiniBi")); //추가 다르게 할 수 있는지 test
                        } else {
                            if(mediNameSave.size()==1){
                                setInfo(mediCodeSave.get(0));
                            }
                            else{//약의 리스트가 1 이상일 때
                                for (int i = 0; i < mediNameSave.size(); i++) {
                                    if (i == 0) {
                                        res += "'"+getKeyWord+"'" + "에 대해 총" + mediNameSave.size() + "개의 결과가 나왔어요\n" + 1 + "번: " + mediNameSave.get(0) + "\n";
                                    } else {
                                        res+=i+1 + "번: " + mediNameSave.get(i)+ "\n";
                                    }
                                }
                                if(group=="그룹1"){
                                    res+="어떤 약에 대한 정보를 찾을까요? 숫자만 입력 해 주세요\n목록에 원하는 약이 없다면 조금 더 구체적으로 약의 이름을 입력 해 보세요!";
                                }
                                else{
                                    res+="어떤 약에 대한 부작용을 찾을까요? 숫자만 입력 해 주세요\n목록에 원하는 약이 없다면 조금 더 구체적으로 약의 이름을 입력 해 보세요!";
                                }
                                constraint=true;
                                ChatMediList.add(new ChatMediItem(res,"MiniBi"));
                            }
                        }

                        chatMediAdapter.notifyDataSetChanged();
                        layoutManager.setStackFromEnd(true);
                        showRecyclerView.scrollToPosition(chatMediAdapter.getItemCount() - 1);
                    }
                });
            }
        }).start();

        //findSpecific(getGroup); //구체적인 이름을 알아내고 결과를 도출
    }

    private void findSpecific(String mode,String mediName){ //구체적 이름으로 실제 결과 도출
        Pattern pattern = Pattern.compile("\\d+"); // 숫자 찾기 위한 정규표현식 패턴
        Matcher matcher = pattern.matcher(mediName);
        if(mode=="그룹1" || mode=="그룹2"){
            /*Pattern pattern = Pattern.compile("\\d+"); // 숫자 찾기 위한 정규표현식 패턴
            Matcher matcher = pattern.matcher(mediName);*/
            if (matcher.find()) {
                // 사용자 입력이 int 타입인 경우의 처리
                String foundNumber = matcher.group(); // 일치하는 부분(숫자) 추출
                String testName=mediCodeSave.get(Integer.parseInt(foundNumber)-1);
                setInfo(testName);

            } else {// 사용자 입력이 int 타입이 아닌 경우의 처리
                constraint=false;
            }
        }
    }

    private void setInfo(String mediCode){//약품 코드를 인자로 받고 api검색으로 보냄, e약은요 api연결하고 약 코드로 검색해서 답 가져오기
        String getKeyWord=mediCode;
        //String getGroup=group;
        new Thread(new Runnable() {
            @Override
            public void run() {
                if(group=="그룹1"){
                    getInfo(getKeyWord); //코드로 약의 정보 추출, e약은요 api우선 사용
                    if(mediInfoSave.size()==0){
                        getInfo2(getKeyWord); //e약은요에 없으면 다른 api 사용
                    }
                }
                else{
                    getSideEffect(getKeyWord);
                    if(mediInfoSave.size()==0){
                        getSideEffect2(getKeyWord);//e약은요에 없으면 다른 api 사용
                    }
                }

                // UI 스레드로 작업 전환
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        // 결과 처리 및 UI 업데이트
                        String res = "";
                        if (mediInfoSave.size() == 0) {
                            res = "검색 결과가 없어요! 약의 이름을 다시 입력 하거나\n구문에 맞춰 질문을 다시 해 보세요";
                        } else {
                            for(int i=0;i<mediInfoSave.size();i++){
                                if(i!=0&&(!(mediInfoSave.get(i).equals("&nbsp;")))){
                                    res+="\n";
                                }
                                //res += mediInfoSave.get(i);
                                if((!(mediInfoSave.get(i).equals("&nbsp;")))){
                                    res += mediInfoSave.get(i);
                                }
                            }
                            //res += mediInfoSave.get(0)+"\n"+mediInfoSave.get(1)+"\n"+mediInfoSave.get(2);
                            //res+="어떤 약에 대한 정보를 찾을까요? 숫자만 입력 해 주세요\n목록에 원하는 약이 없다면 조금 더 구체적으로 약의 이름을 입력 해 보세요!";
                            constraint=true;
                        }
                        ChatMediList.add(new ChatMediItem(res,"MiniBi")); //추가 다르게 할 수 있는지 test
                        chatMediAdapter.notifyDataSetChanged();

                        layoutManager.setStackFromEnd(true);
                        showRecyclerView.scrollToPosition(chatMediAdapter.getItemCount() - 1);
                    }
                });
            }
        }).start();
    }

    public static String encodeParameter(String parameter) { //한글을 url에 넣어도 잘 되도록 UTF_8로 URL인코딩을 해줌
        try {
            return URLEncoder.encode(parameter, StandardCharsets.UTF_8.toString());
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
            // 예외 처리 코드
            return "";
        }
    }

    public void findNameList(String mediName) {//사용자가 입력한 키워드가 포함된 약의 이름을 가져옴
        /*트리멘정, 레스파정, 유니콘연질캡슐,락스타더블캡슐*/
        mediNameSave.clear();
        mediCodeSave.clear();
        StringBuffer buffer = new StringBuffer();
        String str = mediName;//EditText에 작성된 Text얻어오기
        String encodedInput = encodeParameter(str);

        //"+"(따옴표도 지우는 것)를 지우고 공지사항 파일의 4-2번 수행한 것 붙여넣기
        String queryUrl = "http://apis.data.go.kr/1471000/MdcinGrnIdntfcInfoService01/getMdcinGrnIdntfcInfoList01?serviceKey="+"&&item_name=" + encodedInput + "&type=xml";
        String firstimage = " ";   // 이미지
        String title = " ";       // 제목
        String com = " ";      // 주소
        String code=" ";

        try {
            URL url = new URL(queryUrl);//문자열로 된 요청 url을 URL 객체로 생성.
            InputStream is = url.openStream(); //url위치로 입력스트림 연결

            XmlPullParserFactory factory = XmlPullParserFactory.newInstance();//xml파싱을 위한
            XmlPullParser xpp = factory.newPullParser();
            xpp.setInput(new InputStreamReader(is, "UTF-8")); //inputstream 으로부터 xml 입력받기

            String tag;

            xpp.next();
            int eventType = xpp.getEventType();
            while (eventType != XmlPullParser.END_DOCUMENT) {
                switch (eventType) {
                    case XmlPullParser.START_DOCUMENT:
                        buffer.append("파싱 시작...\n\n");
                        break;

                    case XmlPullParser.START_TAG:
                        tag = xpp.getName();//테그 이름 얻어오기

                        if (tag.equals("item")) ;// 첫번째 검색결과
                        else if (tag.equals("ITEM_SEQ")) {
                            xpp.next();
                            code = xpp.getText();
                        }

                        else if (tag.equals("ITEM_NAME")) {
                            xpp.next();
                            title = xpp.getText();

                        } else if (tag.equals("ENTP_NAME")) {
                            xpp.next();
                            com = xpp.getText();
                        }
                        break;

                    case XmlPullParser.TEXT:
                        break;

                    case XmlPullParser.END_TAG:
                        tag = xpp.getName(); //테그 이름 얻어오기

                        if (tag.equals("item")) {
                            buffer.append("\n");// 첫번째 검색결과종료..줄바꿈
                            mediNameSave.add(title); //리스트에 이름 저장
                            mediCodeSave.add(code); //리스트에 코드 저장
                        }
                        break;
                }

                eventType = xpp.next();
            }

        } catch (Exception e) {
            e.printStackTrace();
        }

        buffer.append("파싱 끝\n");

    }//getXmlData method....

    //낱알식별에 없으면 사용, 뇌선 검색 테스트로
    public void findNameList2(String mediName) {//사용자가 입력한 키워드가 포함된 약의 이름을 가져옴
        /*트리멘정, 레스파정, 유니콘연질캡슐,락스타더블캡슐*/
        mediNameSave.clear();
        mediCodeSave.clear();
        StringBuffer buffer = new StringBuffer();
        String str = mediName;//EditText에 작성된 Text얻어오기
        String encodedInput = encodeParameter(str);

        //"+"(따옴표도 지우는 것)를 지우고 공지사항 파일의 4-4번 수행한 것 붙여넣기
        String queryUrl = "https://apis.data.go.kr/1471000/DrugPrdtPrmsnInfoService05/getDrugPrdtPrmsnDtlInq04?serviceKey="+"&pageNo=1&numOfRows=3&type=xml&item_name="+encodedInput;
        String firstimage = " ";   // 이미지
        String title = " ";       // 제목
        String com = " ";      // 주소
        String code=" ";

        try {
            URL url = new URL(queryUrl);//문자열로 된 요청 url을 URL 객체로 생성.
            InputStream is = url.openStream(); //url위치로 입력스트림 연결

            XmlPullParserFactory factory = XmlPullParserFactory.newInstance();//xml파싱을 위한
            XmlPullParser xpp = factory.newPullParser();
            xpp.setInput(new InputStreamReader(is, "UTF-8")); //inputstream 으로부터 xml 입력받기

            String tag;

            xpp.next();
            int eventType = xpp.getEventType();
            while (eventType != XmlPullParser.END_DOCUMENT) {
                switch (eventType) {
                    case XmlPullParser.START_DOCUMENT:
                        buffer.append("파싱 시작...\n\n");
                        break;

                    case XmlPullParser.START_TAG:
                        tag = xpp.getName();//테그 이름 얻어오기

                        if (tag.equals("item")) ;// 첫번째 검색결과
                        else if (tag.equals("ITEM_SEQ")) {
                            xpp.next();
                            code = xpp.getText();
                        }

                        else if (tag.equals("ITEM_NAME")) {
                            xpp.next();
                            title = xpp.getText();

                        }
                        break;

                    case XmlPullParser.TEXT:
                        break;

                    case XmlPullParser.END_TAG:
                        tag = xpp.getName(); //테그 이름 얻어오기

                        if (tag.equals("item")) {
                            buffer.append("\n");// 첫번째 검색결과종료..줄바꿈
                            mediNameSave.add(title); //리스트에 이름 저장
                            mediCodeSave.add(code); //리스트에 코드 저장
                        }
                        break;
                }

                eventType = xpp.next();
            }

        } catch (Exception e) {
            e.printStackTrace();
        }

        buffer.append("파싱 끝\n");

    }//getXmlData method....

    public void getInfo(String code){
        /*트리멘정, 레스파정, 유니콘연질캡슐,락스타더블캡슐*/
        mediInfoSave.clear();
        StringBuffer buffer = new StringBuffer();
        String str = code;//EditText에 작성된 Text얻어오기
        String encodedInput = encodeParameter(code);

        //"+"(따옴표도 지우는 것)를 지우고 공지사항 파일의 4-3번 수행한 것 붙여넣기
        String queryUrl = "http://apis.data.go.kr/1471000/DrbEasyDrugInfoService/getDrbEasyDrugList?serviceKey="+"&itemSeq=" + encodedInput + "&pageNo=1&startPage=1&numOfRows=3";
        String info = " ";       // 정보들 저장

        try {
            URL url = new URL(queryUrl);//문자열로 된 요청 url을 URL 객체로 생성.
            InputStream is = url.openStream(); //url위치로 입력스트림 연결

            XmlPullParserFactory factory = XmlPullParserFactory.newInstance();//xml파싱을 위한
            XmlPullParser xpp = factory.newPullParser();
            xpp.setInput(new InputStreamReader(is, "UTF-8")); //inputstream 으로부터 xml 입력받기

            String tag;

            xpp.next();
            int eventType = xpp.getEventType();
            while (eventType != XmlPullParser.END_DOCUMENT) {
                switch (eventType) {
                    case XmlPullParser.START_DOCUMENT:
                        buffer.append("파싱 시작...\n\n");
                        break;

                    case XmlPullParser.START_TAG:
                        tag = xpp.getName();//테그 이름 얻어오기

                        if (tag.equals("item")) ;// 첫번째 검색결과
                        else if (tag.equals("efcyQesitm")) { //효능저장
                            xpp.next();
                            //info += xpp.getText();
                            if(!(xpp.getText().isEmpty())){
                                mediInfoSave.add(xpp.getText());
                            }
                            //mediInfoSave.add(xpp.getText());
                        }

                        else if (tag.equals("useMethodQesitm")) {//복용법 저장
                            xpp.next();
                            //info += xpp.getText();
                            if(!(xpp.getText().isEmpty())){
                                mediInfoSave.add(xpp.getText());
                            }
                            //mediInfoSave.add(xpp.getText());

                        } else if (tag.equals("depositMethodQesitm")) { //보관법 저장
                            xpp.next();
                            //info += xpp.getText();
                            if(!(xpp.getText().isEmpty())){
                                mediInfoSave.add(xpp.getText());
                            }
                            //mediInfoSave.add(xpp.getText());
                        }
                        break;

                    case XmlPullParser.TEXT:
                        break;

                    case XmlPullParser.END_TAG:
                        tag = xpp.getName(); //테그 이름 얻어오기

                        if (tag.equals("item")) {
                            buffer.append("\n");// 첫번째 검색결과종료..줄바꿈
                            //mediInfoSave.add(info); //리스트에 이름 저장
                        }
                        break;
                }

                eventType = xpp.next();
            }

        } catch (Exception e) {
            e.printStackTrace();
        }

        buffer.append("파싱 끝\n");
    }

    public void getSideEffect(String code){
        /*트리멘정, 레스파정, 유니콘연질캡슐,락스타더블캡슐*/
        mediInfoSave.clear();
        StringBuffer buffer = new StringBuffer();
        String str = code;//EditText에 작성된 Text얻어오기
        String encodedInput = encodeParameter(code);

        //"+"(따옴표도 지우는 것)를 지우고 공지사항 파일의 4-3번 수행한 것 붙여넣기
        String queryUrl = "http://apis.data.go.kr/1471000/DrbEasyDrugInfoService/getDrbEasyDrugList?serviceKey="+"&itemSeq=" + encodedInput + "&pageNo=1&startPage=1&numOfRows=3";
        String info = " ";       // 정보들 저장

        try {
            URL url = new URL(queryUrl);//문자열로 된 요청 url을 URL 객체로 생성.
            InputStream is = url.openStream(); //url위치로 입력스트림 연결

            XmlPullParserFactory factory = XmlPullParserFactory.newInstance();//xml파싱을 위한
            XmlPullParser xpp = factory.newPullParser();
            xpp.setInput(new InputStreamReader(is, "UTF-8")); //inputstream 으로부터 xml 입력받기

            String tag;

            xpp.next();
            int eventType = xpp.getEventType();
            while (eventType != XmlPullParser.END_DOCUMENT) {
                switch (eventType) {
                    case XmlPullParser.START_DOCUMENT:
                        buffer.append("파싱 시작...\n\n");
                        break;

                    case XmlPullParser.START_TAG:
                        tag = xpp.getName();//테그 이름 얻어오기

                        if (tag.equals("item")) ;// 첫번째 검색결과
                        else if (tag.equals("seQesitm")) { //효능저장
                            xpp.next();
                            mediInfoSave.add(xpp.getText());
                        }
                        break;

                    case XmlPullParser.TEXT:
                        break;

                    case XmlPullParser.END_TAG:
                        tag = xpp.getName(); //테그 이름 얻어오기

                        if (tag.equals("item")) {
                            buffer.append("\n");// 첫번째 검색결과종료..줄바꿈
                            //mediInfoSave.add(info); //리스트에 이름 저장
                        }
                        break;
                }

                eventType = xpp.next();
            }

        } catch (Exception e) {
            e.printStackTrace();
        }

        buffer.append("파싱 끝\n");
    }

    //의약품 제품 허가정보 api활용(코드는 부작용이 아닌 효과에 관한 코드임 함수 이름 바꾸기)
    public void getInfo2(String mediName){
        mediInfoSave.clear();
        StringBuffer buffer = new StringBuffer();
        String str = mediName;//EditText에 작성된 Text얻어오기
        String encodedInput = encodeParameter(str);

        //"+"(따옴표도 지우는 것)를 지우고 공지사항 파일의 4-4번 수행한 것 붙여넣기
        String queryUrl = "http://apis.data.go.kr/1471000/DrugPrdtPrmsnInfoService05/getDrugPrdtPrmsnDtlInq04?serviceKey="+"&pageNo=1&numOfRows=3&type=xml&item_seq="+encodedInput;
        String firstimage = " ";   // 이미지
        String title = " ";       // 제목
        String com = " ";      // 주소3
        String tagChecker="";//태그 확인하여 더 진입할지 말지 결정

        try {
            URL url = new URL(queryUrl);
            InputStream is = url.openStream();
            XmlPullParserFactory factory = XmlPullParserFactory.newInstance();
            XmlPullParser xpp = factory.newPullParser();
            xpp.setInput(new InputStreamReader(is, "UTF-8"));
            String tag;
            boolean insideEEDocData = false; // EE_DOC_DATA 태그 안에 있는지 여부를 추적
            boolean insideUDDocData = false; // UD_DOC_DATA 태그 안에 있는지 여부를 추적
            xpp.next();
            int eventType = xpp.getEventType();
            while (eventType != XmlPullParser.END_DOCUMENT) {
                switch (eventType) {
                    case XmlPullParser.START_DOCUMENT:
                        buffer.append("파싱 시작...\n\n");
                        break;
                    case XmlPullParser.START_TAG:
                        tag = xpp.getName();
                        if (tag.equals("EE_DOC_DATA")) {
                            insideEEDocData = true; // EE_DOC_DATA 태그 안으로 진입


                        }else if(tag.equals("ARTICLE")&& insideEEDocData){
                            if(xpp.getAttributeValue(null,"title")!=""){
                                mediInfoSave.add((xpp.getAttributeValue(null,"title")));
                                break;
                            }
                        }
                        else if (tag.equals("PARAGRAPH") && insideEEDocData) {
                            xpp.next(); // PARAGRAPH 태그 내부로 진입하여 텍스트를 읽기
                            mediInfoSave.add(xpp.getText());
                        }
                        else if(tag.equals("UD_DOC_DATA") && insideEEDocData){
                            insideUDDocData=true;
                            insideEEDocData=false;
                        }
                        else if(tag.equals("ARTICLE")&& insideUDDocData){
                            if(xpp.getAttributeValue(null,"title")!=""){
                                mediInfoSave.add((xpp.getAttributeValue(null,"title")));
                                break;
                            }
                        }
                        else if (tag.equals("PARAGRAPH") && insideUDDocData) {
                            xpp.next(); // PARAGRAPH 태그 내부로 진입하여 텍스트를 읽기
                            mediInfoSave.add(xpp.getText());
                        }
                        break;
                    case XmlPullParser.END_TAG:
                        tag = xpp.getName();
                        if (tag.equals("UD_DOC_DATA")) {
                            insideEEDocData = false; // EE_DOC_DATA 태그를 벗어남
                            insideUDDocData=false;
                        }
                        break;
                }
                eventType = xpp.next();
            }
            buffer.append("\n파싱 끝...\n");
            buffer.append("\n");
        } catch (Exception e) {
            e.printStackTrace();
        }
        buffer.append("파싱 끝\n");
    }

    //의약품 제품 허가정보 api활용
    public void getSideEffect2(String mediName){
        mediInfoSave.clear();
        StringBuffer buffer = new StringBuffer();
        String str = mediName;//EditText에 작성된 Text얻어오기
        String encodedInput = encodeParameter(str);

        //"+"(따옴표도 지우는 것)를 지우고 공지사항 파일의 4-4번 수행한 것 붙여넣기
        String queryUrl = "http://apis.data.go.kr/1471000/DrugPrdtPrmsnInfoService05/getDrugPrdtPrmsnDtlInq04?serviceKey="+"&pageNo=1&numOfRows=3&type=xml&item_seq="+encodedInput;
        String firstimage = " ";   // 이미지
        String title = " ";       // 제목
        String com = " ";      // 주소3
        String tagChecker="";//태그 확인하여 더 진입할지 말지 결정

        try {
            URL url = new URL(queryUrl);
            InputStream is = url.openStream();
            XmlPullParserFactory factory = XmlPullParserFactory.newInstance();
            XmlPullParser xpp = factory.newPullParser();
            xpp.setInput(new InputStreamReader(is, "UTF-8"));
            String tag;
            boolean insideNBDocData = false; // EE_DOC_DATA 태그 안에 있는지 여부를 추적
            boolean insideSideEffect = false; //부작용 태그 안에 있는지 여부를 추적

            xpp.next();
            int eventType = xpp.getEventType();
            while (eventType != XmlPullParser.END_DOCUMENT) {
                switch (eventType) {
                    case XmlPullParser.START_DOCUMENT:
                        buffer.append("파싱 시작...\n\n");
                        break;
                    case XmlPullParser.START_TAG:
                        tag = xpp.getName();
                        if (tag.equals("NB_DOC_DATA")) {
                            insideNBDocData = true; // EE_DOC_DATA 태그 안으로 진입


                        }else if(tag.equals("ARTICLE")&& insideNBDocData){
                            if(xpp.getAttributeValue(null,"title").contains("이상반응")||xpp.getAttributeValue(null,"title").contains("부작용")){
                                insideSideEffect=true;
                            }
                            else{
                                insideSideEffect=false;
                            }
                        }
                        else if (tag.equals("PARAGRAPH") && insideNBDocData&&insideSideEffect) {
                            xpp.next(); // PARAGRAPH 태그 내부로 진입하여 텍스트를 읽기
                            mediInfoSave.add(xpp.getText());
                        }
                        break;
                    case XmlPullParser.END_TAG:
                        tag = xpp.getName();
                        if (tag.equals("NB_DOC_DATA")) {
                            insideNBDocData = false; // EE_DOC_DATA 태그를 벗어남
                            insideSideEffect=false;
                        }
                        break;
                }
                eventType = xpp.next();
            }
            buffer.append("\n파싱 끝...\n");
            buffer.append("\n");
        } catch (Exception e) {
            e.printStackTrace();
        }
        buffer.append("파싱 끝\n");
    }

    public static AbstractMap.SimpleEntry<String, Double> findMostSimilarString(String userInput, String[] texts) {//미리 준비한 문장 데이터셋과의 유사도검사
        double maxSimilarity = -1.0;
        String mostSimilarText = "";
        Map<String, Integer> tfUserInput = getTermFrequency(userInput);

        for (String text : texts) {
            Map<String, Integer> tfText = getTermFrequency(text);

            Set<String> vocab = new HashSet<>(tfUserInput.keySet());
            vocab.addAll(tfText.keySet());

            double dotProduct = 0.0;
            double normUserInput = 0.0;
            double normText = 0.0;

            for (String term : vocab) {
                int freqUserInput = tfUserInput.getOrDefault(term, 0);
                int freqText = tfText.getOrDefault(term, 0);

                dotProduct += freqUserInput * freqText;
                normUserInput += Math.pow(freqUserInput, 2);
                normText += Math.pow(freqText, 2);
            }

            normUserInput = Math.sqrt(normUserInput);
            normText = Math.sqrt(normText);

            if (normUserInput != 0 && normText != 0) {
                double similarity = dotProduct / (normUserInput * normText);
                if (similarity > maxSimilarity) {
                    maxSimilarity = similarity;
                    mostSimilarText = text;
                }
            }
        }

        return new AbstractMap.SimpleEntry<>(mostSimilarText, maxSimilarity);
    }

    public static Map<String, Integer> getTermFrequency(String text) {
        Map<String, Integer> frequencyMap = new HashMap<>();
        String[] words = text.toLowerCase().split("\\s+");
        for (String word : words) {
            frequencyMap.put(word, frequencyMap.getOrDefault(word, 0) + 1);
        }
        return frequencyMap;
    }

    public static Map<String, String> createGroupMap(String[] strings) {//어느 그룹에 속한 내용인지 저장
        Map<String, String> groupMap = new HashMap<>();

        // 그룹 1
        for (int i = 0; i < 5; i++) {
            groupMap.put(strings[i], "그룹1");
        }

        // 그룹 2
        for (int i = 5; i < 9; i++) {
            groupMap.put(strings[i], "그룹2");
        }


        return groupMap;
    }
}
