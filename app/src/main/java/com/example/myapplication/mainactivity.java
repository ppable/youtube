package com.example.youtubesearch;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.os.AsyncTask;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import com.example.myapplication.R;

public class mainactivity extends AppCompatActivity {

    EditText search;
    Button button;
    RecyclerView recyclerview;
    UtubeAdapter utubeAdapter;
    AsyncTask<?, ?, ?> searchTask;

    ArrayList<SearchData> sdata = new ArrayList<SearchData>();

    final String serverKey="AIzaSyD51I7LuD6KhFpSkEyYjlCzZJBDBPI0Wzo";




    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.mainactivity);

        search = findViewById(R.id.search);
        button = findViewById(R.id.button);
        recyclerview= findViewById(R.id.recyclerview);
        LinearLayoutManager mLinearLayoutManager = new LinearLayoutManager(this);
        recyclerview.setLayoutManager(mLinearLayoutManager);

        //버튼 클릭시 검색한 정보를 가져옴.
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                searchTask = new searchTask().execute();

            }
        });
    }

    private class searchTask extends AsyncTask<Void, Void, Void> {
        @Override
        protected void onPreExecute() {
            super.onPreExecute();
        }

        @Override
        protected Void doInBackground(Void... params) {
            try {
                paringJsonData(getUtube()); //비동기로 가져온 데이터를 파싱

            } catch (JSONException | IOException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }

            return null;
        }

        @Override
        protected void onPostExecute(Void result) {
            utubeAdapter = new UtubeAdapter(com.example.youtubesearch.mainactivity.this, sdata);
            recyclerview.setAdapter(utubeAdapter);
            utubeAdapter.notifyDataSetChanged(); //가져온 데이터 리싸이클러뷰에 업데이트
        }
    }

    //검색한 결과들을 json 객체로 생성
    public JSONObject getUtube() throws IOException {


        String originUrl = "https://www.googleapis.com/youtube/v3/search?"
                + "part=snippet&q=" + search.getText().toString()
                + "&key="+ serverKey+"&maxResults=50";

        String myUrl = String.format(originUrl);

        URL url = new URL(myUrl);

        HttpURLConnection connection =(HttpURLConnection)url.openConnection();
        connection.setRequestMethod("GET");
        connection.setReadTimeout(10000);
        connection.setConnectTimeout(15000);
        connection.connect();

        String line;
        String result="";
        InputStream inputStream=connection.getInputStream();
        BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream));
        StringBuffer response = new StringBuffer();

        while ((line = reader.readLine())!=null){
            response.append(line);
        }
        System.out.println("검색결과"+ response);
        result=response.toString();


        JSONObject jsonObject = new JSONObject();
        try {
            jsonObject = new JSONObject(result);
        } catch (JSONException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }

        return jsonObject;
    }

    //json 객체 파싱
    private void paringJsonData(JSONObject jsonObject) throws JSONException {
        //재검색할때 데이터들이 쌓이는걸 방지하기 위해 리스트를 초기화 시켜준다.
        sdata.clear();

        JSONArray contacts = jsonObject.getJSONArray("items");
        for (int i = 0; i < contacts.length(); i++) {
            JSONObject c = contacts.getJSONObject(i);
            String kind =  c.getJSONObject("id").getString("kind"); // 종류를 체크하여 playlist도 저장
            if(kind.equals("youtube#video")){
                // 유튜브 비디오 검색
                vodid = c.getJSONObject("id").getString("videoId");
            }else{
                // 유튜브 채널검색
                vodid = c.getJSONObject("id").getString("playlistId");
            }

            String title = c.getJSONObject("snippet").getString("title"); //유튜브 제목

            String changString = stringToHtmlSign(title);

            String date = c.getJSONObject("snippet").getString("publishedAt") //등록날짜
                    .substring(0, 10);
            String imgUrl = c.getJSONObject("snippet").getJSONObject("thumbnails")
                    .getJSONObject("default").getString("url");  //썸네일 이미지 URL값

            String channel = c.getJSONObject("snippet").getString("channelTitle");

            //JSON으로 파싱한 정보들을 객체화 시켜서 리스트에 담아준다.
            sdata.add(new SearchData(vodid, changString, imgUrl, date));
        }
    }

    String vodid = "";


    //영상 제목을 받아올때 &quot; &#39; 문자가 그대로 출력되기 때문에 다른 문자로 대체
    private String stringToHtmlSign(String str) {

        return str.replaceAll("&amp;", "[&]")

                .replaceAll("[<]", "&lt;")

                .replaceAll("[>]", "&gt;")

                .replaceAll("&quot;", "'")

                .replaceAll("&#39;", "'");
    }


}