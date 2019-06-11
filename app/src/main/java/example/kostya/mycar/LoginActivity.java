package example.kostya.mycar;

import android.content.Intent;
import android.os.AsyncTask;
import android.os.Handler;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.util.Log;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.lang.reflect.Field;
import java.net.CookieHandler;
import java.net.CookieManager;
import java.net.HttpURLConnection;
import java.net.URL;
//import java.util.logging.Handler;

//import example.kostya.mycar.R;

public class LoginActivity extends AppCompatActivity implements View.OnClickListener{

    public static String LOG_TAG = "my_log";

    String SAVED_PASSWORD = "saved_password";
    String SAVED_LOGIN = "saved_login";

    final int STATUS_DOO = 1;
    final int STATUS_N_DOO = 0;
    final int PROGRESS_ON = 5;
    final int PROGRESS_OFF = 6;

    Toolbar toolbar;

    //SharedPreferences sPref;
    ProgressBar progressBar2;
    ImageButton  btnToolbarButton = null;

    String adrUrl;
    String loginStat;
    String EDLOGIN;
    String EDPASS;

    Button btDemo;
    Button btLogin;

    EditText editLogin;
    EditText editPass;

    TextView tbTitle;

    private int AMOBJ;


    private Boolean b;//разрешение/запрет вызова второго активити(повторно)

    Handler h;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        //LeakCanary.install(getApplication());

        CookieManager cookieManager = new CookieManager();
        CookieHandler.setDefault(cookieManager);

        toolbar = (Toolbar) findViewById(R.id.tool_bar);
        toolbar.setNavigationIcon(R.drawable.icon_toolbal_arrow_white);
        //toolbar.setTitle("Авторизация");
        setSupportActionBar(toolbar);

        try {
            Field fNavBtn = toolbar.getClass().getDeclaredField("mNavButtonView");
            fNavBtn.setAccessible(true);
            btnToolbarButton = (ImageButton) fNavBtn.get(toolbar);
            btnToolbarButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    finish();
                }
            });
        }catch (NoSuchFieldException e) {
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        }
        //getSupportActionBar().setTitle("Авторизация");

        //toolbar.setLogo(R.drawable.bt_logo_small);

        progressBar2 = (ProgressBar) findViewById(R.id.progressBar2);


        Log.d(LOG_TAG, "OnCreate");

        b = true;

        btDemo = (Button) findViewById(R.id.btDemo);
        btLogin = (Button) findViewById(R.id.btLogin);

        tbTitle = (TextView) findViewById(R.id.tbTitle);
        tbTitle.setText("Авторизация");

        btDemo.setOnClickListener(this);
        btLogin.setOnClickListener(this);

        //authStat = (TextView) findViewById(R.id.authStat);

        editLogin = (EditText) findViewById(R.id.editLogin);
        editPass = (EditText) findViewById(R.id.editPass);

        h = new Handler(){
            public void handleMessage(android.os.Message msg){
                switch(msg.what){
                    case PROGRESS_ON:
                        progressBar2.setVisibility(ProgressBar.VISIBLE);
                        break;
                    case PROGRESS_OFF:
                        progressBar2.setVisibility(ProgressBar.INVISIBLE);
                        break;
                }
            }
        };
        h.sendEmptyMessage(STATUS_N_DOO);

    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        Log.d(LOG_TAG, "onDestroy");
        h.removeCallbacksAndMessages(null);
        //Mycar.instance.mustDie(this);
    }

    @Override
    protected void onPause() {
        super.onPause();
        Log.d(LOG_TAG, "onPause");
        h.removeCallbacksAndMessages(null);
    }

    @Override
    protected void onResume() {
        super.onResume();
        Log.d(LOG_TAG, "onResume");
        b = true;
    }

    @Override
    protected void onStop() {
        super.onStop();
        Log.d(LOG_TAG, "onStop");
    }

    @Override
    protected void onStart() {
        super.onStart();
        Log.d(LOG_TAG, "onStart");
    }

    /*@Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_login, menu);
        return true;
    }*/

    /*@Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        *//*if (id == R.id.action_settings) {
            return true;
        }*//*

        return super.onOptionsItemSelected(item);
    }*/


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_login, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        int id = item.getItemId();
        if (id == R.id.action_settings) {
            finish();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onClick(View v) {
        if(b == false){
            return;
        }
        switch (v.getId()){
            case R.id.btLogin:
                b = false;
                adrUrl = null;

                String username = editLogin.getText().toString();
                String password = editPass.getText().toString();

                EDLOGIN = editLogin.getText().toString();
                EDPASS = editPass.getText().toString();

                if (TextUtils.isEmpty(editLogin.getText().toString()) || TextUtils.isEmpty(editPass.getText().toString())){
                    Toast toast = Toast.makeText(LoginActivity.this, "Введите логин и пароль", Toast.LENGTH_SHORT);
                    toast.setGravity(Gravity.CENTER, 0, 0);
                    toast.show();
                    b = true;
                    return;
                }
                adrUrl = "https://mynex.ru/json.login.php?name="+username+"&pass="+password;
                new Authorize().execute();
                //authStat.setText("user");

                break;

            case R.id.btDemo:
                b = false;
                adrUrl = "https://mynex.ru/json.login.php?name=demo&pass=demo123";

                EDLOGIN = "demo";
                EDPASS = "demo123";

                new Authorize().execute();
                //authStat.setText("demo");
                break;
        }
    }

    private class Authorize extends AsyncTask <Void, Void, String> {

        HttpURLConnection urlAuth = null;
        String result;
        String cookie;
        BufferedReader reader1 = null;
        String resultAuth = "";
        Boolean statConnection;

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            progressBar2.setVisibility(ProgressBar.VISIBLE);
        }

        @Override
        protected String doInBackground(Void... params) {

            Log.d(LOG_TAG, "начало");

            // получаем cookie с внешнего ресурса
            try {
                URL url = new URL(adrUrl);

                urlAuth = (HttpURLConnection) url.openConnection();
                urlAuth.setRequestMethod("GET");
                urlAuth.connect();
                //***************************************_begin_****************************************************************
                String headerName = null;
                for (int i=1; (headerName = urlAuth.getHeaderFieldKey(i))!=null; i++) {
                    if (headerName.equals("Set-Cookie")) {
                        cookie = urlAuth.getHeaderField(i);
                        //cookie = cookie.substring(0, cookie.indexOf(";"));
                    }
                }
                //***************************************_end_******************************************************************
                //***************************************_begin_****************************************************************
                InputStream inputStream = urlAuth.getInputStream();
                StringBuffer buffer = new StringBuffer();
                reader1 = new BufferedReader(new InputStreamReader(inputStream));
                String line;
                while ((line = reader1.readLine()) != null) {
                    buffer.append(line);
                }
                resultAuth = buffer.toString();
                //***************************************_end_******************************************************************
                statConnection = true;

            } catch (Exception e) {
                e.printStackTrace();
                result = "fail";
                b = true;
                statConnection = false;
            }
            return resultAuth;
        }

        @Override
        protected void onPostExecute(String rez) {
            super.onPostExecute(rez);

            Log.d(LOG_TAG, rez);

            if (statConnection == true) {
                JSONObject jsonLogin = null;
                try {
                    jsonLogin = new JSONObject(rez);
                    JSONObject output = jsonLogin.getJSONObject("output");
                    loginStat = output.getString("login");
                } catch (Exception e){
                    e.printStackTrace();
                    //authStat.setText("FailGetLoginStat");
                }
                String okneok = String.valueOf(loginStat);//не знал как назвать, назвал okneok(результат авторизации)

                switch (okneok){
                    case "ok":
                        //*************************_Временные меры_************************************************
                        //Intent intent = new Intent(LoginActivity.this, DeviceListActivity.class);
                        //startActivity(intent);

                        Prefer save = new Prefer();
                        save.addProperty(SAVED_LOGIN, EDLOGIN);
                        save.addProperty(SAVED_PASSWORD, EDPASS);

                        new Amount().execute();
                        break;
                    case "fail":
                        progressBar2.setVisibility(ProgressBar.INVISIBLE);

                        b = true;
                        Toast toast = Toast.makeText(LoginActivity.this, "Неверный логин или пароль", Toast.LENGTH_SHORT);
                        toast.setGravity(Gravity.CENTER, 0, 0);
                        toast.show();
                        break;
                    default:
                        progressBar2.setVisibility(ProgressBar.INVISIBLE);
                        Toast toast1 = Toast.makeText(LoginActivity.this, "неверный ответ от сервера", Toast.LENGTH_SHORT);
                        toast1.setGravity(Gravity.CENTER, 0, 0);
                        toast1.show();
                }

                /*if (okneok.equals("ok")){
/*//*************************_Временные меры_************************************************
                    //Intent intent = new Intent(LoginActivity.this, DeviceListActivity.class);
                    //startActivity(intent);

                    Prefer save = new Prefer();
                    save.addProperty(SAVED_LOGIN, EDLOGIN);
                    save.addProperty(SAVED_PASSWORD, EDPASS);

                    new Amount().execute();
                } else {

                    progressBar2.setVisibility(ProgressBar.INVISIBLE);

                    b = true;
                    Toast toast = Toast.makeText(LoginActivity.this, "Неверный логин или пароль", Toast.LENGTH_SHORT);
                    toast.setGravity(Gravity.CENTER, 0, 0);
                    toast.show();
                }
                *//*Intent intent = new Intent(LoginActivity.this, DataActivity.class);
                startActivity(intent);*//*
                //authStat.setText("Auth OK");*/
            } else {
                //authStat.setText("FailUrlConnection");
                progressBar2.setVisibility(ProgressBar.INVISIBLE);
                Toast toast = Toast.makeText(LoginActivity.this, "Нет подключения к internet", Toast.LENGTH_SHORT);
                toast.setGravity(Gravity.CENTER, 0, 0);
                toast.show();
            }
        }
    }

    private class Amount extends AsyncTask <Void, Void, String> {

    HttpURLConnection URLAMOUNT = null;
    String result1;
    BufferedReader reader1 = null;


    @Override
    protected String doInBackground(Void... params) {

        try {
            URL url = new URL("https://mynex.ru/json.device.php");

            URLAMOUNT = (HttpURLConnection) url.openConnection();
            URLAMOUNT.setRequestMethod("GET");
            URLAMOUNT.connect();
            //***************************************_begin_****************************************************************
            InputStream inputStream = URLAMOUNT.getInputStream();
            StringBuffer buffer = new StringBuffer();
            reader1 = new BufferedReader(new InputStreamReader(inputStream));
            String line;
            while ((line = reader1.readLine()) != null) {
                buffer.append(line);
            }
            result1 = buffer.toString();
            //***************************************_end_******************************************************************
            //statConnection = true;

        } catch (Exception e) {
            e.printStackTrace();
        }
        return result1;
    }

    @Override
    protected void onPostExecute(String rez) {
        super.onPostExecute(rez);

        JSONObject someObj;

        try {
            someObj = new JSONObject(rez);
            JSONObject output = someObj.getJSONObject("output");
            JSONObject get = output.getJSONObject("get");
            AMOBJ = Integer.valueOf(get.getString("count"));
        } catch (JSONException e) {
            e.printStackTrace();
        }

        //________________ПРОГРЕСС БАР КОНЕЦ______________
        progressBar2.setVisibility(ProgressBar.INVISIBLE);

        if(AMOBJ == 1){
            Intent intent = new Intent(LoginActivity.this, DeviceList.class)//вылетает когда нет устройств в списке
                    .addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK)
                    .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(intent);
        }else {
            Intent intent = new Intent(LoginActivity.this, DeviceList.class)
                    .addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK)
                    .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(intent);
        }
    }
}

}
