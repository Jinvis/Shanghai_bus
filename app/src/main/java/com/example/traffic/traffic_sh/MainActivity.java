package com.example.traffic.traffic_sh;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v7.widget.CardView;
import android.support.v7.widget.PopupMenu;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.ImageButton;
import android.content.SharedPreferences;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Map;

class MyCollectButton extends Button{

    public MyCollectButton(Context i){
        super(i);
        main = (MainActivity)i;
        this.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        if (!linename.equals("")) {
                            if (!stopname.equals("")) {
                                main.buttonStartstop = startstop;
                                main.isSingle = true;
                                main.context =  main.getstopid(stopname, linename);
                                if (main.context.size() != 0) {
                                    main.mHandler.sendEmptyMessage(1);
                                }
                            }
                        }
                    }
                }).start();
            }
        });

        this.setOnLongClickListener(new OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                showPopupMenu(v);
                return true;
            }
        });
    }

    private void  showPopupMenu(final View view){
        PopupMenu popupMenu = new PopupMenu(main, view);
        popupMenu.getMenuInflater().inflate(R.menu.deletecollect, popupMenu.getMenu());
        popupMenu.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem item){
                switch (item.getItemId()){
                    case R.id.collectDelete:
                    {
                        MyCollectButton bu = (MyCollectButton)view;
                        main.buttonKey = bu.key;
                        main.mHandler.sendEmptyMessage(3);
                    }

                }
                return true;
            }

        });
        popupMenu.show();
    }

    private MainActivity main;
    public String stopname;
    public String linename;
    public String key;
    public String startstop;


};

class MyImageButton extends ImageButton {
    private String text = null;  //要显示的文字
    private int color;               //文字的颜色
    private int size;
    public MyImageButton(Context context, AttributeSet attrs) {
        super(context,attrs);
    }

    public void setText(String text){
        this.text = text;       //设置文字
    }

    public void setColor(int color){
        this.color = color;    //设置文字颜色
    }

    public void setTextSize(int size){
        this.size = size;
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        Paint paint=new Paint(Paint.FAKE_BOLD_TEXT_FLAG | Paint.ANTI_ALIAS_FLAG);
        paint.setTextAlign(Paint.Align.CENTER);
        Rect bounds = new Rect();
        paint.setColor(color);
        paint.setTextSize(size);
        paint.getTextBounds(text, 0, text.length(),bounds);
        Paint.FontMetricsInt fontMetrics = paint.getFontMetricsInt();
        int baseline = (getMeasuredHeight() - fontMetrics.bottom + fontMetrics.top) / 2 - fontMetrics.top;
        canvas.drawText(text, getMeasuredWidth()/2 -bounds.width()/2, baseline, paint);  //绘制文字
    }

}

class StopInfo{
    public StopInfo(String line_name, String startstop, String time, String dis, String cur_stop, String ends, String busnum, String start_time, String end_time)
    {
        linename = line_name;
        startStop = startstop;
        time_min = time;
        stopdis = dis;
        curStop = cur_stop;
        endStop = ends;
        busNum = busnum;
        startTime = start_time;
        endTime =end_time;
    }

    public boolean useful(){
        if (time_min.isEmpty())
            return false;
        return true;
    };

    public String linename;
    public String startStop;
    public String time_min;
    public String stopdis;
    public String curStop;
    public String endStop;
    public String busNum;
    public String startTime;
    public String endTime;

};

public class MainActivity extends AppCompatActivity {

    public  static  final  String PREFERENCE_NAME = "Data";
    public boolean isSingle = false;
    private AutoCompleteTextView mlinenameEdit;
    private AutoCompleteTextView mstopnameEdit;


    private TextView curStop;
    private TextView curLine;
    private MyImageButton bu;
    public String errorMsg;
    public String buttonKey;
    public String buttonStartstop;
    public ArrayList context = new ArrayList();
    private ArrayList stoplist = new ArrayList();

    public Handler mHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            if (msg.what == 1) {
                LinearLayout cardlayout = (LinearLayout) findViewById(R.id.cardlayout);
                cardlayout.removeAllViews();
                for(int i = 0; i <context.size(); i++)
                {
                    StopInfo stop =  (StopInfo) context.get(i);
                    CardView stopcard = (CardView) View.inflate(getApplicationContext(), R.layout.cardviewlayout, null);
                    stopcard.setCardElevation(5);
                    stopcard.setRadius(18);
                    CardView.LayoutParams lp = new CardView.LayoutParams(CardView.LayoutParams.MATCH_PARENT, CardView.LayoutParams.WRAP_CONTENT);
                    lp.setMargins(0, 0, 0, 30);
                    stopcard.setLayoutParams(lp);
                    stopcard.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            showPopupMenu(v);
                        }
                    });
                    TextView startstop = (TextView) stopcard.findViewById(R.id.startStop);
                    TextView time = (TextView) stopcard.findViewById(R.id.timeView);
                    TextView endstop = (TextView) stopcard.findViewById(R.id.endStop);
                    TextView startTime = (TextView) stopcard.findViewById(R.id.firstBus);
                    TextView endTime = (TextView) stopcard.findViewById(R.id.lastBus);

                    startstop.setText(stop.startStop);
                    startTime.setText(stop.startTime);
                    endTime.setText(stop.endTime);
                    if (!stop.busNum.equals("") && !stop.stopdis.equals("") && !stop.time_min.equals(""))
                        time.setText(String.format("%s还有%s站，约%s分钟.", stop.busNum, stop.stopdis, stop.time_min));
                    else time.setText("等待发车");
                    endstop.setText(stop.endStop);
                    if(isSingle) {
                        if(stop.startStop.equals(buttonStartstop)) {
                            cardlayout.addView(stopcard);
                            curStop.setText(stop.curStop);
                            curLine.setText(stop.linename);
                        }
                    }else {
                        cardlayout.addView(stopcard);
                        curStop.setText(stop.curStop);
                        curLine.setText(stop.linename);
                    }

                }
                isSingle = false;
            }
            if (msg.what == 4) {
                String[] COUNTRIES = (String[])stoplist.toArray(new String[stoplist.size()]);
                ArrayAdapter<String> adapter = new ArrayAdapter<String>(getBaseContext(), android.R.layout.select_dialog_singlechoice, COUNTRIES);
                mstopnameEdit.setAdapter(adapter);
            }
            if(msg.what == 2)
            {
                Toast ts =Toast.makeText(getBaseContext(),errorMsg, Toast.LENGTH_LONG);
                ts.show();
            }
            if(msg.what == 3)
            {
                removeData(buttonKey);
            }
            //mTvTest.setText(context);
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        initView();
        mstopnameEdit.setOnFocusChangeListener(new View.OnFocusChangeListener(){
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                if (hasFocus) {
                    new Thread(new Runnable() {
                        @Override
                        public void run() {
                            String linename = mlinenameEdit.getText().toString();
                            if (!linename.equals("")) {
                                stoplist.clear();
                                stoplist = getstoplist(linename);
                                if (stoplist.size() != 0) {
                                    mHandler.sendEmptyMessage(4);
                                }
                            }
                        }
                    }).start();
                }
            }
        });
        bu.setOnTouchListener(new View.OnTouchListener(){
            public boolean onTouch(View v, MotionEvent event) {
                if(event.getAction() == MotionEvent.ACTION_DOWN){
                    bu.setBackgroundColor(Color.parseColor("#ffe4c4"));
                }else  if(event.getAction() == MotionEvent.ACTION_UP)
                {
                    bu.setBackgroundColor(Color.parseColor("#ffffff"));
                }
                return false;
            }
            });
        bu.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v){
                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        String linename = mlinenameEdit.getText().toString();
                        if (!linename.equals("")) {
                            String stopname = mstopnameEdit.getText().toString();
                            if (!stopname.equals("")) {
                                context = getstopid(stopname, linename);
                            }
                            if (context.size() != 0) {
                                mHandler.sendEmptyMessage(1);
                            }
                        }
                    }
                }).start();
            }
        });

    }

    private  boolean isNetworkConnected(Context context) {
        if (context != null) {
            ConnectivityManager mConnectivityManager = (ConnectivityManager) context
                    .getSystemService(Context.CONNECTIVITY_SERVICE);
            NetworkInfo mNetworkInfo = null;
            if (mConnectivityManager != null) {
                mNetworkInfo = mConnectivityManager
                        .getActiveNetworkInfo();
            }
            if (mNetworkInfo != null) {
                return mNetworkInfo.isAvailable();
            }
        }
        return false;
    }

    private void  showPopupMenu(final View view){
        PopupMenu popupMenu = new PopupMenu(MainActivity.this, view);
        popupMenu.getMenuInflater().inflate(R.menu.collectmenu, popupMenu.getMenu());
        popupMenu.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem item){
                switch (item.getItemId()){
                    case R.id.collectAdd:
                    {
                        TextView startview = (TextView)(view.findViewById(R.id.startStop));
                        String endStopName = startview.getText().toString();
                        String curstopName = curStop.getText().toString();
                        String lineName = curLine.getText().toString();
                        String key = String.format("%s_%s",lineName,curstopName);
                        String insertContent = String.format("%s,%s,%s,%s", key,lineName,curstopName,endStopName);
                        addData(insertContent);
                    }

                }
                return true;
            }

        });
        popupMenu.show();
    }

    private void removeData(String ButtonName) {
        SharedPreferences.Editor  editor = getSharedPreferences(PREFERENCE_NAME,Context.MODE_PRIVATE).edit();
        editor.remove(ButtonName);
        editor.commit();
        updateCollect();
    }

    private void addData(String insertContent){
        SharedPreferences sharedPreferences = getSharedPreferences(PREFERENCE_NAME,Context.MODE_PRIVATE);
        Map<String, String> key_Value = ( Map<String, String>)sharedPreferences.getAll();
        String name = String.format("insert%d",key_Value.size());

        SharedPreferences.Editor  editor = getSharedPreferences(PREFERENCE_NAME,Context.MODE_PRIVATE).edit();
        editor.putString(name,insertContent);
        editor.commit();
        updateCollect();
    }

    private void updateCollect(){

        SharedPreferences sharedPreferences = getSharedPreferences(PREFERENCE_NAME,Context.MODE_PRIVATE);
        LinearLayout right = (LinearLayout)findViewById(R.id.collectright);
        LinearLayout left = (LinearLayout)findViewById(R.id.collectleft);
        right.removeAllViews();
        left.removeAllViews();
        Map<String, String> key_Value = ( Map<String, String>)sharedPreferences.getAll();

        for (Map.Entry<String,String> entry:key_Value.entrySet())
        {
            String key = entry.getKey();
            String buttonInfo = entry.getValue();
            String[] info = buttonInfo.split(",");
            String temp = key.replace("insert","");
            int isleft = 0;
            if(!temp.isEmpty())
                isleft = Integer.parseInt(temp)%2;

            MyCollectButton insert = new MyCollectButton(this);
            insert.setText(info[0]);
            insert.key = key;
            insert.linename = info[1];
            insert.stopname = info[2];
            insert.startstop = info[3];

            if( isleft == 1)
            {
                right.addView(insert);
            }
            else {
                left.addView(insert);
            }

        }



    }
    private void initView() {

        bu = (MyImageButton)findViewById(R.id.button2);
        curStop =(TextView) findViewById(R.id.curStop);
        curLine =(TextView) findViewById(R.id.curLine);
        mlinenameEdit = (AutoCompleteTextView) findViewById(R.id.LinenameEditText);
        mstopnameEdit = (AutoCompleteTextView) findViewById(R.id.StopEditText);
        updateCollect();

        bu.setText("Search");
        bu.setColor(Color.BLACK);
        bu.setTextSize(50);

        ArrayAdapter<String> adapter = new ArrayAdapter<String>(this, android.R.layout.select_dialog_singlechoice, getResources().getStringArray(R.array.stoplist));
        mlinenameEdit.setAdapter(adapter);
    }

    private ArrayList getstoplist(String linename){
        if(linename.equals(""))
            return  null;
        ArrayList list = new ArrayList();
        if(isNetworkConnected(getBaseContext())) {

            try {
                ArrayList lineInfo = getlineid(linename);
                String lineid = lineInfo.get(0).toString();
                if(!lineid.isEmpty()) {
                    Document doc = Jsoup.connect("http://180.168.57.114:8380/bsth_kxbus/GetDateHttpUtils/Getlinexx.do")
                            .ignoreContentType(true)
                            .data("linename", linename)
                            .data("lineid", lineid)
                            .post();
                    JSONObject jsonObject = new JSONObject();
                    try {
                        jsonObject = new JSONObject(doc.text());
                        jsonObject = new JSONObject(jsonObject.getString("linelist"));
                        jsonObject = jsonObject.getJSONObject("lineInfoDetails");
                    } catch (JSONException e) {
                        Log.e("get linlist error", e.getMessage());
                    }
                    try {
                        JSONArray start_list0 = jsonObject.getJSONObject("lineResults0").getJSONArray("stop");
                        for (int i = 0; i < start_list0.length(); i++) {
                            JSONObject stops = start_list0.getJSONObject(i);
                            list.add(stops.getString("zdmc"));
                        }
                    } catch (JSONException e) {
                        Log.e("lineResult0", e.getMessage());
                    }
                }
            } catch (IOException e) {
                Log.e("getstoplist error", e.getMessage());
            }

        }else {
            errorMsg =  "请检查网络";
            mHandler.sendEmptyMessage(2);
        }
        //return (String[])list.toArray(new String[list.size()]);
        return list;
    }
    private ArrayList getlineid(String linename){
        ArrayList list = new ArrayList();
        if(isNetworkConnected(getBaseContext())) {
            try {
                Document doc = Jsoup.connect("http://180.168.57.114:8380/bsth_kxbus/GetDateHttpUtils/Getlinename.do")
                        .ignoreContentType(true)
                        .data("linename", linename).post();

                try {
                    JSONObject jsonObject = new JSONObject(doc.text());
                    if(jsonObject.has("msg")) {
                        errorMsg = jsonObject.getString("msg");
                        mHandler.sendEmptyMessage(2);
                    }
                    else {
                        if(jsonObject.has("line")) {
                            jsonObject = jsonObject.getJSONObject("line");
                            list.add(jsonObject.getString("line_id"));
                            list.add(jsonObject.getString("start_earlytime"));
                            list.add(jsonObject.getString("start_latetime"));
                            list.add(jsonObject.getString("end_earlytime"));
                            list.add(jsonObject.getString("end_latetime"));
                        }
                    }
                } catch (JSONException e) {
                    Log.e("line 470 post failed", e.getMessage());
                }
            } catch (IOException e) {

                Log.e("line 214 post failed", e.getMessage());
            }
        }
        else {
            errorMsg = "请检查网络";
            mHandler.sendEmptyMessage(2);
        }
        return list;
    }
    public ArrayList getstopid(String stopname, String linename){
        ArrayList lineInfo = getlineid(linename);
        String lineid = lineInfo.get(0).toString();
        //String start_earlytime = lineInfo.get(1).toString();
        //String start_latetime = lineInfo.get(2).toString();
        //String end_earlytime = lineInfo.get(3).toString();
        //String end_latetime = lineInfo.get(4).toString();

        ArrayList stopStatus = new ArrayList();
        if(isNetworkConnected(getBaseContext())) {
            try {
                Document doc = Jsoup.connect("http://180.168.57.114:8380/bsth_kxbus/GetDateHttpUtils/Getlinexx.do")
                        .ignoreContentType(true)
                        .data("linename", linename)
                        .data("lineid", lineid)
                        .post();
                JSONObject jsonObject = new JSONObject();
                try {
                    jsonObject = new JSONObject(doc.text());
                    if(jsonObject.has("linelist")) {
                        jsonObject = new JSONObject(jsonObject.getString("linelist"));
                        jsonObject = jsonObject.getJSONObject("lineInfoDetails");
                    }
                    else
                        return null;
                } catch (JSONException e) {
                    Log.e("get linlist error", e.getMessage());
                }

                String[] lineResults = new String[] {
                     "lineResults0", "lineResults1"
                };
                String startstop = "";
                String endstop = "";
                for (int j = 0; j<lineResults.length; j++) {
                    try {
                        JSONArray start_list = jsonObject.getJSONObject(lineResults[j]).getJSONArray("stop");
                        String dir = jsonObject.getJSONObject(lineResults[j]).getString("direction");
                        if (dir.equals("true"))
                            dir = "0";
                        else if (dir.equals("false"))
                            dir = "1";
                        String stop_id = "";
                        for (int i = 0; i < start_list.length(); i++) {
                            JSONObject stops = start_list.getJSONObject(i);
                            String name = stops.getString("zdmc");
                            if (stopname.equals(name)) {
                                stop_id = stops.getString("id");
                                break;
                            }
                        }
                        startstop = start_list.getJSONObject(0).getString("zdmc");
                        endstop = start_list.getJSONObject(start_list.length() - 1).getString("zdmc");

                        if (!stop_id.equals("")) {
                            try {
                                Document status = Jsoup.connect("http://180.168.57.114:8380/bsth_kxbus/GetDateHttpUtils/Realtimesite.do")
                                        .ignoreContentType(true)
                                        .data("linename", linename)
                                        .data("lineid", lineid)
                                        .data("stopid", stop_id)
                                        .data("direction", dir)
                                        .post();
                                JSONObject stopInfo = new JSONObject(status.text());
                                if(stopInfo.has("msg"))
                                {
                                    errorMsg = stopInfo.getString("msg");
                                    mHandler.sendEmptyMessage(2);
                                    return null;
                                }
                                stopInfo = new JSONObject(stopInfo.getString("linelist"));
                                stopInfo = stopInfo.getJSONObject("result");
                                JSONObject car = stopInfo.getJSONObject("cars").getJSONObject("car");
                                String time_sec = "";

                                String stopdis = car.getString("stopdis");
                                time_sec = String.valueOf(Integer.parseInt(car.getString("time")) / 60);
                                String busnum = car.getString("terminal");
                                int start= (j+1)*2-1;
                                int end = (j+1)*2;
                                StopInfo info0 = new StopInfo(linename, startstop, time_sec, stopdis, stopname, endstop, busnum, lineInfo.get(start).toString(), lineInfo.get(end).toString());
                                stopStatus.add(info0);
                            } catch (IOException e) {
                                Log.e("stop_list has no info", e.getMessage());
                            }
                        }
                    } catch (JSONException e) {
                        StopInfo info = new StopInfo(linename, startstop, "", "", stopname, endstop, "", lineInfo.get(j*2-1).toString(), lineInfo.get(j*2).toString());
                        stopStatus.add(info);
                        Log.e("stop_list error", e.getMessage());
                    }
                }

            } catch (IOException e) {

                Log.e("line 331 post failed", e.getMessage());
            }
        }
        else {
            errorMsg =  "请检查网络";
            mHandler.sendEmptyMessage(2);
        }
        return stopStatus;
    }
}

