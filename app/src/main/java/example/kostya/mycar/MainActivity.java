package example.kostya.mycar;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.Gravity;
import android.widget.ProgressBar;
import android.widget.Toast;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.CookieHandler;
import java.net.CookieManager;
import java.net.HttpURLConnection;
import java.net.URL;

public class MainActivity extends AppCompatActivity {

    String SAVED_PASSWORD = "saved_password";
    String SAVED_LOGIN = "saved_login";
    String DEV_NAME = "name_of_device";
    String DEV_ID_SHARED = "dev_id_shared";

    private int AMOBJ;
    private String idd;

    final int STATUS_DOO = 1;
    final int STATUS_N_DOO = 0;
    final int PROGRESS_ON = 5;
    final int PROGRESS_OFF = 6;

    String loginStat;
    Boolean STAT_CONNECTION;

    String login;
    String parol;

    Prefer load;

    ProgressBar progressBar;

    Handler h;
    private String nameD;
    private long TIME_DELAY = 1000;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        //LeakCanary.install(getApplication());

        StateConnect sc = new StateConnect();

        if(!sc.isOnLine(this)){

            AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
            builder.setTitle("Ошибка подключения")
                    .setMessage("Проверьте подключение к сети internet")
                    .setCancelable(false)
                    .setNegativeButton("Выход", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            dialog.cancel();
                            MainActivity.this.finish();
                        }
                    });
            AlertDialog alert = builder.create();
            alert.show();

            /*Toast toast = Toast.makeText(MainActivity.this, "нет соединения с интернет", Toast.LENGTH_SHORT);
            toast.setGravity(Gravity.TOP, 0, 200);
            toast.show();
            finish();*/
        } else {

            CookieManager cookieManager = new CookieManager();
            CookieHandler.setDefault(cookieManager);

            load = new Prefer();
            load.initt(getApplicationContext());

            progressBar = (ProgressBar) findViewById(example.kostya.mycar.R.id.progressBar);

            //progressBar.setVisibility(ProgressBar.INVISIBLE);

            VerifyLoginData proverka = new VerifyLoginData();
            proverka.start();

            h = new Handler() {
                public void handleMessage(android.os.Message msg) {
                    switch (msg.what) {
                        case PROGRESS_ON:
                            progressBar.setVisibility(ProgressBar.VISIBLE);
                            break;
                        case PROGRESS_OFF:
                            progressBar.setVisibility(ProgressBar.INVISIBLE);
                            break;
                    }
                }
            };
            h.sendEmptyMessage(STATUS_N_DOO);
        }

    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        //Mycar.instance.mustDie(this);

        load.initt(null);

    }

    @Override
    protected void onPause() {
        super.onPause();
        if(h != null){
            h.removeCallbacksAndMessages(null);
        }

    }

    public class VerifyLoginData extends Thread {

        @Override
        public void run() {

            //load = new Prefer();
            login = load.getProperty(SAVED_LOGIN);
            parol = load.getProperty(SAVED_PASSWORD);

            if(login == null || login == ""){
                //вызываем Login
                h.sendEmptyMessage(PROGRESS_ON);

                try {
                    Thread.sleep(TIME_DELAY);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                    return;
                }
                h.sendEmptyMessage(PROGRESS_OFF);

                Intent intent = new Intent(MainActivity.this, LoginActivity.class)//вернуть логин активити
                        .addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK)
                        .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                startActivity(intent);
            } else{
                //читаем данные из SharedPreferenses и логинимся
                new Authorize().execute();
            }
        }
    }

    private class Authorize extends AsyncTask <Void, Void, String> {

        HttpURLConnection urlAuth = null;
        String cookie;
        BufferedReader reader = null;
        String resultAuth = "";
        Boolean statConnection;



        String USERNAME = load.getProperty(SAVED_LOGIN);
        String PASSWORD = load.getProperty(SAVED_PASSWORD);

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            //________________ПРОГРЕСС БАР НАЧАЛО__________________________________________________________________________________
            progressBar.setVisibility(ProgressBar.VISIBLE);
        }

        @Override
        protected String doInBackground(Void... params) {

            // получаем cookie с внешнего ресурса
            try {
                URL url = new URL("https://mynex.ru/json.login.php?name="+USERNAME+"&pass="+PASSWORD);

                urlAuth = (HttpURLConnection) url.openConnection();
                urlAuth.setRequestMethod("GET");
                urlAuth.connect();
                //***************************************_begin_****************************************************************
                String headerName = null;
                for (int i=1; (headerName = urlAuth.getHeaderFieldKey(i))!=null; i++) {
                    if (headerName.equals("Set-Cookie")) {
                        cookie = urlAuth.getHeaderField(i);
                    }
                }
                //***************************************_end_******************************************************************
                //***************************************_begin_****************************************************************
                InputStream inputStream = urlAuth.getInputStream();
                StringBuffer buffer = new StringBuffer();
                reader = new BufferedReader(new InputStreamReader(inputStream));
                String line;
                while ((line = reader.readLine()) != null) {
                    buffer.append(line);
                }
                resultAuth = buffer.toString();
                //***************************************_end_******************************************************************
                STAT_CONNECTION = true;

            } catch (Exception e) {
                e.printStackTrace();
                STAT_CONNECTION = false;
            }
            return resultAuth;
        }

        @Override
        protected void onPostExecute(String rez) {
            super.onPostExecute(rez);

            if (STAT_CONNECTION == true) {
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
                if (okneok.equals("ok")){
                    new Amount().execute();
                } else {
                    //b = true;
                    progressBar.setVisibility(ProgressBar.INVISIBLE);
                    Toast toast = Toast.makeText(MainActivity.this, "Неверный логин или пароль", Toast.LENGTH_SHORT);
                    toast.setGravity(Gravity.TOP, 0, 0);
                    toast.show();

                    Intent intent = new Intent(MainActivity.this, LoginActivity.class)//вернуть логин активити
                            .addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK)
                            .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                    startActivity(intent);
                }
                /*Intent intent = new Intent(LoginActivity.this, DataActivity.class);
                startActivity(intent);*/
                //authStat.setText("Auth OK");
            } else {
                //Включаем аллерт диалог



                Toast toast = Toast.makeText(MainActivity.this, "сервер недоступен", Toast.LENGTH_SHORT);
                toast.setGravity(Gravity.TOP, 0, 200);
                toast.show();
                finish();
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
                JSONObject zer  = get.getJSONObject("0");
                idd = zer.getString("id");
                nameD = zer.getString("name");
            } catch (JSONException e) {
                e.printStackTrace();
            }

            //________________ПРОГРЕСС БАР КОНЕЦ_______________________________________________________________________________________
            progressBar.setVisibility(ProgressBar.INVISIBLE);

            //НЕ ЗАБЫТЬ ПОМЕНЯТЬ AMOBJ == 0 на AMOBJ == 1
            if(AMOBJ == 1){
                Prefer save = new Prefer();
                save.addProperty(DEV_NAME, nameD);
                save.addProperty(DEV_ID_SHARED, idd);
                Intent intent = new Intent(MainActivity.this, InformActivity.class)
                        .addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK)
                        .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                intent.putExtra("idd", idd);
                startActivity(intent);
                finish();
            }else {
                Intent intent = new Intent(MainActivity.this, DeviceList.class)
                        .addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK)
                        .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                startActivity(intent);
            }
        }
    }
}
