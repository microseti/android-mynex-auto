package example.kostya.mycar;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Handler;
import android.os.Parcelable;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.SimpleAdapter;
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
import java.util.ArrayList;
import java.util.HashMap;

public class DeviceList extends AppCompatActivity {

    String SAVED_PASSWORD = "saved_password";
    String SAVED_LOGIN = "saved_login";
    String DEV_NAME = "name_of_device";
    String DEV_ID_SHARED = "dev_id_shared";

    static String LOG_TAG_3 = "my_log_3";
    private Ldata mLdata;
    private Dolist doo;
    private Close close;

    final int STATUS_DOO = 1;
    final int STATUS_N_DOO = 0;
    final int SET_ACTION_ACTIVE = 7;
    final int SET_ACTION_INACTIVE = 8;
    final int TOAST_MAKE = 9;
    final int REAUTH = 10;

    private static final String IDOBJ = "idobj";
    private static final String NAME = "objName"; // Верхний текст
    private static final String PLACE = "place"; // ниже главного
    private static final String SATSTEXT = "satsTextV";
    private static final String SIGNALICON = "satsImgIc";

    private String SESSION_CLOSE = "\t\t\t<script type=\"text/javascript\">\t\t\t\tlocation.replace (\"auth.php\");\t\t\t</script>\t\t";
    /*			<script type="text/javascript">				location.replace ("auth.php");			</script>		*/

    Toolbar toolbar;
    ImageButton btnToolbarButton = null;
    ProgressBar progressBar2;
    ProgressDialog pdSession;

    private int AM_OBJ;//количество елементов ListView
    ListView listView;

    ImageView imageSignal;
    ImageView imageAction;

    TextView tbTitle;

    private Boolean b;
    private Boolean INTERNET_OK;
    private Boolean RESUMED; //при активном активити - true, при свернутом приложении false


    Handler h;
    SimpleAdapter adapter;

    Prefer load;

    //ReAuthorize reauth;

    /////////////////////////////////////////////////////////////////////////////
    String RESULT_JSON_DEVICES = null;
    ////////////////////////////////////////////////////////////////////////////

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(example.kostya.mycar.R.layout.activity_device_list);
        //LeakCanary.install(getApplication());

        /*CookieManager cookieManager = new CookieManager();
        CookieHandler.setDefault(cookieManager);*/

        load = new Prefer();
        load.initt(getApplicationContext());

        toolbar = (Toolbar) findViewById(example.kostya.mycar.R.id.tool_bar);
        toolbar.setTitle("Объекты");
        toolbar.setNavigationIcon(example.kostya.mycar.R.drawable.icon_toolbal_arrow_white);
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
        } catch (NoSuchFieldException e) {
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        }

        progressBar2 = (ProgressBar) findViewById(example.kostya.mycar.R.id.progressBar2);

        pdSession = new ProgressDialog(this);

        tbTitle = (TextView) findViewById(example.kostya.mycar.R.id.tbTitle);
        tbTitle.setText("Доступные объекты");

        imageSignal = (ImageView) findViewById(example.kostya.mycar.R.id.imageSignal);
        imageAction = (ImageView) findViewById(example.kostya.mycar.R.id.imageAction);

        imageSignal.setVisibility(View.INVISIBLE);

        listView = (ListView) findViewById(example.kostya.mycar.R.id.listView);
        listView.setOnItemClickListener(itemClickListener);



        Log.d(LOG_TAG_3, "onCreate DEVICE_LIST");

        //reauth = new ReAuthorize();


        h = new Handler() {
            public void handleMessage(android.os.Message msg) {

                switch (msg.what) {
                    case STATUS_DOO:
                        doo = new Dolist();
                        doo.doList();
                        break;
                    case SET_ACTION_ACTIVE:
                        imageAction.setImageResource(R.drawable.ic_data_24dp);
                        break;
                    case SET_ACTION_INACTIVE:
                        imageAction.setImageResource(R.drawable.ic_data_trnsp_24dp);
                        break;
                    case TOAST_MAKE:
                        Toast.makeText(DeviceList.this, "Сессия закрыта", Toast.LENGTH_LONG).show();
                        break;
                    case REAUTH:
                        new ReAuthorize().execute();
                        break;
                }
            }
        };
        h.sendEmptyMessage(STATUS_N_DOO);

        INTERNET_OK = true;
        mLdata = new Ldata();
        mLdata.start();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(example.kostya.mycar.R.menu.menu_device_list, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        int id = item.getItemId();

        switch (id){
            case R.id.action_settings:
                RESUMED = false;
                Prefer save = new Prefer();
                save.addProperty(SAVED_LOGIN, "");
                save.addProperty(SAVED_PASSWORD, "");

                Intent intent = new Intent(DeviceList.this, LoginActivity.class)//вылетает когда нет устройств в списке
                        .addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK)
                        .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                startActivity(intent);
                break;

            /*case R.id.action_close_session:
                Close close = new Close();
                close.start();*/

        }

        /*if (id == example.kostya.mycar.R.id.action_settings) {

            Prefer save = new Prefer();
            save.addProperty(SAVED_LOGIN, "");
            save.addProperty(SAVED_PASSWORD, "");

            Intent intent = new Intent(DeviceList.this, LoginActivity.class)//вылетает когда нет устройств в списке
                    .addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK)
                    .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(intent);

            return true;
        }*/
        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onResume() {
        super.onResume();
        Log.d(LOG_TAG_3, "onResume");

        RESUMED = true;

        //load = new Prefer();
        load.initt(getApplicationContext());

        if (!mLdata.isAlive()) {
            mLdata = new Ldata();
            mLdata.start();
        }
        b = true;
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        //Mycar.instance.mustDie(this);


    }

    @Override
    protected void onStop() {
        super.onStop();
        Log.d(LOG_TAG_3, "onStop");

        RESUMED = false;

        mLdata.interrupt();

        h.removeCallbacksAndMessages(null);



        //reauth.cancel(true);
    }

    @Override
    public void onBackPressed() {
        RESUMED = false;
        super.onBackPressed();
    }

    public class Dolist {

        private String listDev;
        private JSONObject someObj;
        protected JSONObject someObjDev;

        private ArrayList<HashMap<String, Object>> mCatList;

        public void doList() {

            mCatList = new ArrayList<HashMap<String, Object>>();
            HashMap<String, Object> hm;

            try {
                someObj = new JSONObject(RESULT_JSON_DEVICES);
                JSONObject output = someObj.getJSONObject("output");
                JSONObject get = output.getJSONObject("get");
                AM_OBJ = Integer.valueOf(get.getString("count"));
                listDev = String.valueOf(get);
            } catch (JSONException e) {
                e.printStackTrace();
            }

            try {
                someObjDev = new JSONObject(listDev);

                for (int i = 0; i < AM_OBJ; i++) {
                    JSONObject devi = someObjDev.getJSONObject(String.valueOf(i));
                    String idobj = devi.getString("id");
                    String nameObj = devi.getString("name");
                    String signal = devi.getString("mqua");
                    String sats = devi.getString("sats");
                    double NET = Double.valueOf(devi.getString("net"));

                    int mode = Integer.valueOf(devi.getString("mode"));

                    if ((mode >= 1 && mode <= 6) || mode == 99 || mode == 100 || mode == 150) {
                        hm = new HashMap<>();
                        hm.put(IDOBJ, idobj);
                        hm.put(NAME, nameObj); // Название
                        hm.put(PLACE, "...");
                        hm.put(SATSTEXT, sats); // Описание

                        if (NET == 0) {
                            hm.put(SIGNALICON, R.drawable.ic_signal_lost_24dp);
                        } else {
                            switch (Integer.valueOf(signal)) {
                                case 0:
                                    hm.put(SIGNALICON, R.drawable.ic_signal_0_24dp); // Картинка
                                    break;
                                case 1:
                                    hm.put(SIGNALICON, R.drawable.ic_signal_1_24dp); // Картинка
                                    break;
                                case 2:
                                    hm.put(SIGNALICON, R.drawable.ic_signal_2_24dp); // Картинка
                                    break;
                                case 3:
                                    hm.put(SIGNALICON, R.drawable.ic_signal_3_24dp); // Картинка
                                    break;
                                case 4:
                                    hm.put(SIGNALICON, R.drawable.ic_signal_4_24dp); // Картинка
                                    break;
                                case 5:
                                    hm.put(SIGNALICON, R.drawable.ic_signal_5_24dp); // Картинка
                                    break;
                            }
                        }
                        mCatList.add(hm);

                            //////////возможно утечка в hm
                            // hm.clear();
                    }

                }
            } catch (Exception e) {
                e.printStackTrace();
                Log.d(LOG_TAG_3, "ОШИБКА ДЕВАЙС ЛИСТ");
                return;
            }

            adapter = new SimpleAdapter(DeviceList.this, mCatList, example.kostya.mycar.R.layout.listcomponent, new String[]{NAME, PLACE, SATSTEXT, SIGNALICON}, new int[]{example.kostya.mycar.R.id.name, example.kostya.mycar.R.id.place, example.kostya.mycar.R.id.satsText, example.kostya.mycar.R.id.signalImg});
            Log.d(LOG_TAG_3, "..............backGround...............");
            //mCatList.clear();

            listView.setScrollContainer(false);
            Parcelable state = listView.onSaveInstanceState();
            listView.setAdapter(adapter);
            listView.onRestoreInstanceState(state);
            adapter = null;
            //hm.clear();

        }
    }

    AdapterView.OnItemClickListener itemClickListener = new AdapterView.OnItemClickListener() {
        @Override
        public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
            if (b == false) {
                return;
            }
            b = false;
            HashMap<String, Object> itemHashMap = (HashMap<String, Object>) parent.getItemAtPosition(position);
            String idd = itemHashMap.get(IDOBJ).toString();
            String nameD = itemHashMap.get(NAME).toString();

            Prefer save = new Prefer();
            save.addProperty(DEV_NAME, nameD);
            save.addProperty(DEV_ID_SHARED, idd);

            /*Toast.makeText(getApplicationContext(), idd, Toast.LENGTH_SHORT).show();*/
            Intent intent = new Intent(DeviceList.this, InformActivity.class);
            intent.putExtra("idd", idd);
            startActivity(intent);//вызов активити
        }
    };

    private class Ldata extends Thread {

        HttpURLConnection urlJson = null;
        BufferedReader reader = null;
        InputStream inputStream = null;
        StringBuffer buffer = null;
        String line = null;
        URL url = null;

        @Override
        public void run() {    //Этот метод будет выполнен в побочном потоке
            do {
                try {
                    if (!Thread.interrupted()) {
                        Log.d(LOG_TAG_3, "..............backGround_devlist...............");
                        if(INTERNET_OK == true){
                            h.sendEmptyMessage(SET_ACTION_ACTIVE);
                        }
                        url = new URL("https://mynex.ru/json.device.php");

                        urlJson = (HttpURLConnection) url.openConnection();
                        urlJson.setRequestMethod("GET");
                        urlJson.connect();

                        Log.d(LOG_TAG_3, "connected");

                        inputStream = urlJson.getInputStream();
                        buffer = new StringBuffer();

                        reader = new BufferedReader(new InputStreamReader(inputStream));

                        while ((line = reader.readLine()) != null) {
                            buffer.append(line);
                        }

                        h.sendEmptyMessage(SET_ACTION_INACTIVE);

                        RESULT_JSON_DEVICES = buffer.toString();

                        buffer.delete(0, buffer.length());
                        reader.close();

                        Log.d(LOG_TAG_3, RESULT_JSON_DEVICES);

                        if(RESULT_JSON_DEVICES.equals(SESSION_CLOSE)){
                            //h.sendEmptyMessage(TOAST_MAKE);

                            //mLdata.interrupt();
                            if(RESUMED){
                                h.sendEmptyMessage(REAUTH);
                            }

                            return;

                        }else{
                            h.sendEmptyMessage(STATUS_DOO);
                        }

                        INTERNET_OK = true;

                    } else {
                        return;
                    }

                } catch (Exception e) {
                    e.printStackTrace();
                    Log.d(LOG_TAG_3, "НЕТ ИНТЕРНЕТА в LDATA DEVICE_LIST");

                    INTERNET_OK = false;

                    h.sendEmptyMessage(SET_ACTION_INACTIVE);

                }

                try {
                    Thread.sleep(2000);
                } catch (Exception e) {
                    e.printStackTrace();
                    return;
                }

            } while (true);
        }
    }

    private class ReAuthorize extends AsyncTask<Void, Void, String> {

        HttpURLConnection urlAuth = null;
        String result;
        String cookie;
        BufferedReader reader1 = null;
        String resultAuth = "";
        Boolean statConnection;

        String username;
        String password;

        String loginStat;

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            //progressBar2.setVisibility(ProgressBar.VISIBLE);
            if (mLdata.isAlive()) {
                mLdata.interrupt();
            }
            if(RESUMED == false){
                return;
            }

            CookieManager cookieManager = new CookieManager();
            CookieHandler.setDefault(cookieManager);

            //load = new Prefer();
            //load.initt(DeviceList.this);

            username = load.getProperty(SAVED_LOGIN);
            password = load.getProperty(SAVED_PASSWORD);

            pdSession.setMessage("Рестарт сессии...");
            //pdSession.setIndeterminate(true);
            pdSession.setCancelable(false);
            pdSession.show();
        }

        @Override
        protected String doInBackground(Void... params) {

            //Log.d(LOG_TAG, "начало");

            // получаем cookie с внешнего ресурса
                try {
                    URL url = new URL("https://mynex.ru/json.login.php?name="+username+"&pass="+password);

                    urlAuth = (HttpURLConnection) url.openConnection();
                    urlAuth.setRequestMethod("GET");
                    urlAuth.connect();

                    //***************************************_cookie_****************************************************************
                    String headerName = null;
                    for (int i=1; (headerName = urlAuth.getHeaderFieldKey(i))!=null; i++) {
                        if (headerName.equals("Set-Cookie")) {
                            cookie = urlAuth.getHeaderField(i);
                            //cookie = cookie.substring(0, cookie.indexOf(";"));
                        }
                    }
                    //***************************************_end_******************************************************************
                    //***************************************_json_****************************************************************
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
                    //result = "fail";
                    //b = true;
                    statConnection = false;
                }
            return resultAuth;
        }

        @Override
        protected void onPostExecute(String rez) {
            super.onPostExecute(rez);

            Log.d(LOG_TAG_3, "авторизация "+rez);

            pdSession.cancel();

            if (rez == null){

                if (!mLdata.isAlive() && RESUMED) {
                    mLdata = new Ldata();
                    mLdata.start();
                }

                return;
            }

            if (statConnection) {
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

                    if (!mLdata.isAlive() && RESUMED) {
                        mLdata = new Ldata();
                        mLdata.start();
                    }

                } else {

                    //progressBar2.setVisibility(ProgressBar.INVISIBLE);

                    b = true;
                    Toast toast = Toast.makeText(getApplicationContext(), "Неверный логин или пароль", Toast.LENGTH_SHORT);
                    toast.setGravity(Gravity.CENTER, 0, 0);
                    toast.show();
                    finish();
                }

            } else {
                Toast toast = Toast.makeText(getApplicationContext(), "Нет подключения к internet", Toast.LENGTH_SHORT);
                toast.setGravity(Gravity.CENTER, 0, 0);
                toast.show();

                if (!mLdata.isAlive() && RESUMED) {
                    mLdata = new Ldata();
                    mLdata.start();
                }
            }
        }
    }

    public class Close extends Thread {

        HttpURLConnection urlClose = null;
        BufferedReader readerCls = null;
        String resultClose = "";

        @Override
        public void run() {    //Этот метод будет выполнен в побочном потоке
            try {
                if (!Thread.interrupted()) {
                    //h.sendEmptyMessage(SET_ACTION_ACTIVE);
                    URL url = new URL("https://mynex.ru/auth_close.php");

                    urlClose = (HttpURLConnection) url.openConnection();
                    urlClose.setRequestMethod("GET");
                    urlClose.connect();

                    Log.d(LOG_TAG_3, "closing");

                    InputStream inputStream = urlClose.getInputStream();
                    StringBuffer buffer = new StringBuffer();
                    readerCls = new BufferedReader(new InputStreamReader(inputStream));
                    String line;
                    while ((line = readerCls.readLine()) != null) {
                        buffer.append(line);
                    }
                    resultClose = buffer.toString();
                    Log.d(LOG_TAG_3, resultClose);

                } else {
                    return;
                }

            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }


}
