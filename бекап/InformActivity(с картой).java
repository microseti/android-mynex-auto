package example.kostya.mycar;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Handler;
import android.os.Parcelable;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.util.Log;
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

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.osmdroid.api.IMapController;
import org.osmdroid.bonuspack.overlays.Marker;
import org.osmdroid.tileprovider.tilesource.TileSourceFactory;
import org.osmdroid.util.GeoPoint;
import org.osmdroid.views.MapView;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.lang.reflect.Field;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;

public class InformActivity extends AppCompatActivity {

    String SAVED_PASSWORD = "saved_password";
    String SAVED_LOGIN = "saved_login";
    String DEV_NAME = "name_of_device";

    private static final String COMNAME = "comname";
    private static final String COMID = "comid";
    private static final String ACTIONICON = "actionicon";

    static String LOG_TAG_3 = "my_log_3";
    static String LOG_TAG_4 = "hueta";

    String ID_FROM_DEVICE_LIST;
    String LID;

    final int STATUS_DOO = 1;
    final int RUN_COMMANDS = 2;
    final int RUN_STAT_ACTION = 3;
    final int RUN_NOTHING = 0;
    final int SET_ACTION_ACTIVE = 7;
    final int SET_ACTION_INACTIVE = 8;
    final int TOAST_MAKE = 9;

    final int ZERO = 10;
    final int ONE = 11;
    final int TWO = 12;
    final int THREE = 13;
    final int FOUR = 14;
    final int FIVE = 15;
    final int SIX = 16;
    final int SEVEN = 17;

    private int ID_OBJ;// = DataActivity.idObj;
    private int idComand;//id команд
    private String FUEL = null;


    ListView listCom;

    TextView txFuel;
    TextView txSpeed;
    TextView txSatellite;
    TextView txVoltage;
    TextView tbTitle;
    TextView tbSubTitle;

    ImageView imageSignal;
    ImageView imageAction;
    ImageView fuelImg;

    Handler h;

    private String RESULT_JSON_COMM;
    private String RESULT_COM_STAT;
    private String RESULT_JSON_COM_LID;
    private String RESULT_JSON_DEVICES = "";
    private String RESULT_JSON_PLACE = "- - -";
    private String RESULT_JSON_PARAMETER;

    static Ldata mLdata;
    static Dolist doo;


    private Boolean b;
    private Boolean c;
    private Boolean INTERNET_OK;

    Toolbar toolbar;
    ImageButton btnToolbarButton = null;
    ProgressDialog pd;
    SimpleAdapter adapter;
    ProgressBar fuelProgress;

    ////////////////__Карта__//////////////
    MapView map;
    IMapController mapController;
    GeoPoint startPoint;
    Marker devMarker;
    //////////////////////////////////////

    static LComandList mLCom;

    private Float prun;
    private Float pext;
    private double LAT = 0;
    private double LNG = 0;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_inform);

        pd = new ProgressDialog(this);

        Intent intent = getIntent();
        ID_FROM_DEVICE_LIST = intent.getStringExtra("idd");

        ID_OBJ = Integer.valueOf(ID_FROM_DEVICE_LIST);


        Prefer load = new Prefer();

        toolbar = (Toolbar) findViewById(R.id.tool_bar);
        //toolbar.setTitle(load.getProperty(DEV_NAME));
        toolbar.setNavigationIcon(R.drawable.icon_toolbal_arrow_white);
        setSupportActionBar(toolbar);

        txFuel = (TextView) findViewById(R.id.txFuel);
        txSpeed = (TextView) findViewById(R.id.txSpeed);
        txSatellite = (TextView) findViewById(R.id.txSatellite);
        txVoltage = (TextView) findViewById(R.id.txVoltage);
        tbSubTitle = (TextView) findViewById(R.id.tbSubTitle);
        tbTitle = (TextView) findViewById(R.id.tbTitle);
        tbTitle.setText(load.getProperty(DEV_NAME));

        imageSignal = (ImageView) findViewById(R.id.imageSignal);
        imageAction = (ImageView) findViewById(R.id.imageAction);
        fuelImg = (ImageView) findViewById(R.id.fuelImg);

        fuelProgress = (ProgressBar) findViewById(R.id.fuelProgress);

        listCom = (ListView) findViewById(R.id.listCom);

        listCom.setOnItemClickListener(itemClickListener);

        try {
            Field fNavBtn = toolbar.getClass().getDeclaredField("mNavButtonView");
            fNavBtn.setAccessible(true);
            btnToolbarButton = (ImageButton) fNavBtn.get(toolbar);
            btnToolbarButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    c = false;
                    finish();
                }
            });
        } catch (NoSuchFieldException e) {
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        }

        c = true;


        Log.d(LOG_TAG_3, "onCreate INFORM");

        ////////////////__MAP__///////////////
        map = (MapView) findViewById(R.id.map);
        map.setTileSource(TileSourceFactory.MAPNIK);

        map.setBuiltInZoomControls(true);
        map.setMultiTouchControls(true);

        //map.

        mapController = map.getController();
        mapController.setZoom(15);
        startPoint = new GeoPoint(43.116667, 131.9);
        mapController.setCenter(startPoint);

        devMarker = new Marker(map);
        Drawable devIcon = getResources().getDrawable(R.drawable.dev_marker);
        //devIcon.
        devMarker.setIcon(devIcon);
        map.getOverlays().add(devMarker);
        //////////////////////////////////////

        h = new Handler() {
            public void handleMessage(android.os.Message msg) {
                switch (msg.what) {
                    case RUN_COMMANDS:
                        CommandsList dooC = new CommandsList();
                        dooC.doComandList();
                        break;
                    case STATUS_DOO:
                        if (c = true) {
                            doo = new Dolist();
                            doo.doList();
                        }
                        break;
                    case SET_ACTION_ACTIVE:
                        imageAction.setImageResource(R.drawable.in_action);
                        break;
                    case SET_ACTION_INACTIVE:
                        imageAction.setImageResource(R.drawable.in_action_trnspr);
                        break;
                    case TOAST_MAKE:
                        Toast.makeText(getApplicationContext(), "Нет соединения с интернет", Toast.LENGTH_SHORT).show();
                        break;

                    //case START_PROGRESS_DIALOG:
                }
            }
        };
        h.sendEmptyMessage(RUN_NOTHING);

        INTERNET_OK = true;

        mLdata = new Ldata();
        mLdata.start();

        mLCom = new LComandList();
        mLCom.start();

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(example.kostya.mycar.R.menu.menu_inform, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        int id = item.getItemId();
        if (id == example.kostya.mycar.R.id.action_settings) {

            Prefer save = new Prefer();
            save.addProperty(SAVED_LOGIN, "");
            save.addProperty(SAVED_PASSWORD, "");
            save.addProperty(DEV_NAME, "");

            Intent intent = new Intent(InformActivity.this, LoginActivity.class)//вылетает когда нет устройств в списке
                    .addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK)
                    .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(intent);

            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onPause() {
        super.onPause();
        mLCom.interrupt();
        Log.d(LOG_TAG_4, "onPause");
        c = false;
        mLdata.interrupt();
    }

    @Override
    public void onResume() {
        super.onResume();

        c = true;
        b = true;

        if (!mLCom.isAlive()) {
            mLCom = new LComandList();
            mLCom.start();
        }

        if (!mLdata.isAlive()) {
            mLdata = new Ldata();
            mLdata.start();
        }
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        c = false;
        Log.d(LOG_TAG_3, "нажато назад");
    }

    @Override
    protected void onStop() {
        super.onStop();
        Log.d(LOG_TAG_4, "onStop");
        //c = false;
        mLdata.interrupt();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        Log.d(LOG_TAG_4, "onDestroy");
    }

    private class LComandList extends Thread {

        HttpURLConnection urlCommands = null;
        BufferedReader readerCom = null;

        @Override
        public void run() {    //Этот метод будет выполнен в побочном потоке
            try {
                if (!Thread.interrupted()) {
                    URL url = new URL("http://a.kostya.example/json.command.php?ev=get");

                    urlCommands = (HttpURLConnection) url.openConnection();
                    urlCommands.setRequestMethod("GET");
                    urlCommands.connect();

                    InputStream inputStream = urlCommands.getInputStream();
                    StringBuffer buffer = new StringBuffer();

                    readerCom = new BufferedReader(new InputStreamReader(inputStream));

                    String line;
                    while ((line = readerCom.readLine()) != null) {
                        buffer.append(line);
                    }
                    RESULT_JSON_COMM = buffer.toString();
                    //h.sendEmptyMessage(RUN_COMMANDS);
                } else {
                    return;
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
            //return;
        }
    }

    private class CommandsList {
        private ArrayList<HashMap<String, Object>> mComands;
        JSONArray selectedComm = null; // массив из выбранных комманд для конкретного объекта
        JSONObject jsonCommand = null;
        Boolean ST_ST = false; //присутствует команда стоп и старт
        String STOP_ID;


        public void doComandList() {
            mComands = new ArrayList<HashMap<String, Object>>();
            HashMap<String, Object> hm;

            try {
                jsonCommand = new JSONObject(RESULT_JSON_COMM);
                JSONObject output = jsonCommand.getJSONObject("output");
                JSONArray get = output.getJSONArray("get");
                selectedComm = new JSONArray();
                int j = 0;
                for (int i = 0; i < get.length(); i++) {

                    JSONObject command = get.getJSONObject(i);
                    String adrCom = command.getString("addr");
                    String typeCom = command.getString("type");
                    String nameCom = command.getString("name");

                    if (adrCom.equals(String.valueOf(ID_OBJ)) && typeCom.equals("0")) {

                        if (nameCom.equals("stop")){
                            ST_ST = true;
                            STOP_ID = command.getString("id");
                        } else{
                            selectedComm.put(j, command);
                            j = j + 1;
                        }
                    }
                }
                //Log.d(LOG_TAG_2, "JSONArray: "+String.valueOf(selectedComm));

            } catch (JSONException e) {
                e.printStackTrace();
                return;
            }

            try {

                for (int i = 0; i < selectedComm.length(); i++) {
                    JSONObject some = selectedComm.getJSONObject(i);
                    String comTitle = some.getString("name");
                    String comId = some.getString("id");

                    hm = new HashMap<>();
                    if (comTitle.equals("start")) {
                        hm.put(ACTIONICON, R.drawable.key_run);
                    }
                    ;
                    if (comTitle.equals("lock")) {
                        hm.put(ACTIONICON, R.drawable.locked59);
                    }
                    ;
                    if (comTitle.equals("unlock")) {
                        hm.put(ACTIONICON, R.drawable.unlock);
                    }
                    ;
                    hm.put(COMNAME, comTitle);

                    //условия выбора id для стопа старта
                    if (ST_ST && pext > prun && comTitle.equals("start")){
                        comId = STOP_ID;
                    }

                    hm.put(COMID, comId); // ключ/значение = comid/comId

                    mComands.add(hm);
                }

            } catch (JSONException e) {
                e.printStackTrace();
            }

            try {
/*
 SimpleAdapter adapter = new SimpleAdapter(getActivity(), mComands, R.layout.comandlist, new String[]{COMNAME}, new int[]{R.id.comName});
*/
                adapter = new SimpleAdapter(InformActivity.this, mComands, R.layout.buttons_list,
                        new String[]{ACTIONICON, COMNAME},
                        new int[]{R.id.actionImg, R.id.ll_buttons});
                adapter.setViewBinder(new MyViewBinder());

                listCom.setScrollContainer(false);
                Parcelable state = listCom.onSaveInstanceState();
                listCom.setAdapter(adapter);
                listCom.onRestoreInstanceState(state);


            } catch (Exception e) {
                return;
            }

            //listCom.getScrollBarSize();
        }
    }

    private class Action extends AsyncTask<Void, Void, String> {

        HttpURLConnection urlAction = null;
        BufferedReader readerCom = null;

        @Override
        protected void onPreExecute() {
            super.onPreExecute();

            //h.sendEmptyMessage(START_PROGRESS_DIALOG)

            //pd.setTitle("Выполняется...");
            pd.setMessage("Выполняется...");
            pd.setIndeterminate(true);
            pd.setCancelable(false);
            pd.show();
        }

        @Override
        protected String doInBackground(Void... params) {

            try {
                URL url = new URL("http://a.kostya.example/json.command.php?ev=does&id=" + idComand);

                urlAction = (HttpURLConnection) url.openConnection();
                urlAction.setRequestMethod("POST");
                urlAction.connect();

                InputStream inputStream = urlAction.getInputStream();
                StringBuffer buffer = new StringBuffer();

                readerCom = new BufferedReader(new InputStreamReader(inputStream));

                String line;
                while ((line = readerCom.readLine()) != null) {
                    buffer.append(line);
                }

                RESULT_JSON_COM_LID = buffer.toString();

            } catch (Exception e) {
                e.printStackTrace();
            }
            return RESULT_JSON_COM_LID;
        }

        @Override
        protected void onPostExecute(String jCom) {
            super.onPostExecute(jCom);

            if (jCom == null) {
                pd.setIndeterminate(false);
                pd.cancel();

                AlertDialog.Builder builder = new AlertDialog.Builder(InformActivity.this);
                builder.setTitle("Ошибка подключения");
                builder.setMessage("Проверьте подключение к internet и попробуйте снова");
                builder.setCancelable(false);
                builder.setPositiveButton("Закрыть", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        b = true;
                        dialog.cancel();
                    }
                });
                AlertDialog alert = builder.create();
                alert.show();

                b = true;

            } else {

                JSONObject jsonLid;
                try {
                    jsonLid = new JSONObject(RESULT_JSON_COM_LID);
                    JSONObject output = jsonLid.getJSONObject("output");
                    LID = output.getString("does");
                } catch (JSONException e) {
                    e.printStackTrace();
                }

                new StatAction().execute();

                //b = true;
            }
        }
    }

    private class StatAction extends AsyncTask<Void, Integer, Integer> {

        HttpURLConnection urlStat = null;
        BufferedReader readerStat = null;
        int COM_ST = 7;

        int count = 0;
        private String RESPONSE_LID;

        @Override
        protected void onPreExecute() {
            super.onPreExecute();

            //pd.setMessage("Ожидание ответа");
        }

        @Override
        protected Integer doInBackground(Void... params) {

            do {
                try {
                    URL url = new URL("http://a.kostya.example/json.command.php?ev=sta&lid=" + LID);

                    urlStat = (HttpURLConnection) url.openConnection();
                    urlStat.setRequestMethod("GET");
                    urlStat.connect();

                    InputStream inputStream = urlStat.getInputStream();
                    StringBuffer buffer = new StringBuffer();

                    readerStat = new BufferedReader(new InputStreamReader(inputStream));

                    String line;
                    while ((line = readerStat.readLine()) != null) {
                        buffer.append(line);
                    }

                    RESULT_COM_STAT = buffer.toString();

                    JSONObject jsonLidRez;

                    try {
                        jsonLidRez = new JSONObject(RESULT_COM_STAT);
                        //RESULT_COM_STAT = "";
                        JSONObject output = jsonLidRez.getJSONObject("output");
                        RESPONSE_LID = output.getString("sta");
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }

                    COM_ST = Integer.valueOf(RESPONSE_LID);
                    //publishProgress(COM_ST);

                } catch (Exception e) {
                    e.printStackTrace();
                    Toast.makeText(InformActivity.this, "хрень", Toast.LENGTH_SHORT).show();
                }

                //++count;

            } while (COM_ST == 0 || COM_ST == 1);
            //return RESULT_COM_STAT;
            return COM_ST;
        }

        /* @Override
         protected void onProgressUpdate(Integer... val) {
             super.onProgressUpdate(val);


             switch (val[0]){
                 case 0:
                     //pd.setMessage("Команда обрабатывается" + count);
                     //pd.setTitle("Выполняется");
                     break;
                 case 1:
                     //pd.setMessage("Ожидание выполнения");
                     //pd.setTitle("Выполняется. "+count+" "+ val[0]);
                     break;
                 case 2:
                     pd.setMessage("Команда отменена");
                     pd.setTitle("Выполнено");
                     break;
                 case 3:
                     //pd.setMessage("Выполнено!");
                     pd.setTitle("Выполнено");
                     break;
                 case 4:
                     pd.setMessage("Ошибка выполнения");
                     pd.setTitle("Выполнено");
                     break;
                 case 5:
                     pd.setMessage("Вышло время ожидания");
                     pd.setTitle("Выполнено");
                     break;
                 case 6:
                     pd.setMessage("Объект не на связи");
                     pd.setTitle("Выполнено");
                     break;
                 case 7:
                     pd.setMessage("Связь с сервером прервана");
                     pd.setTitle("Выполнено");
                     break;
             }
         }
 */
        @Override
        protected void onPostExecute(Integer result) {
            super.onPostExecute(result);

            pd.setIndeterminate(false);
            pd.cancel();

            AlertDialog.Builder builder = new AlertDialog.Builder(InformActivity.this);
            //builder.setTitle("Готово!");
            //builder.setMessage("Выполнить команду?");
            builder.setCancelable(false);
            builder.setPositiveButton("Закрыть", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    b = true;
                    dialog.cancel();
                }
            });

            switch (result) {
                case 2:
                    builder.setMessage("Команда отменена");
                    break;
                case 3:
                    builder.setMessage("Выполнено");
                    break;
                case 4:
                    builder.setMessage("Ошибка выполнения");
                    break;
                case 5:
                    builder.setMessage("Вышло время ожидания");
                    break;
                case 6:
                    builder.setMessage("Объект не на связи");
                    break;
                case 7:
                    builder.setMessage("Связь с сервером прервана");
                    break;
            }
            AlertDialog alert = builder.create();
            alert.show();

            b = true;

        }
    }

    public class Dolist {

        private String listDev = null;
        private JSONObject someObj;
        protected JSONObject someObjDev;
        private JSONObject forOneDev;
        private int AM_OBJ;

        public void doList() {

            Place place = new Place();
            place.start();

            Fuel fuel = new Fuel();
            fuel.start();

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

                try {
                    someObjDev = new JSONObject(listDev);
                } catch (Exception e) {
                    Log.d(LOG_TAG_4, "vot ona");
                    return;
                }

                for (int i = 0; i < AM_OBJ; i++) {
                    JSONObject devi = someObjDev.getJSONObject(String.valueOf(i));
                    int idobj = Integer.valueOf(devi.getString("id"));

                    if (idobj == ID_OBJ) {
                        forOneDev = someObjDev.getJSONObject(String.valueOf(i));
                    }
                }

                String nameObj = forOneDev.getString("name");
                String speed = forOneDev.getString("speed");
                String signal = forOneDev.getString("mqua");
                String sats = forOneDev.getString("sats");


                prun = Float.valueOf(forOneDev.getString("prun"));
                pext = Float.valueOf(forOneDev.getString("pext"));

                LAT = Float.valueOf(forOneDev.getString("lat"));
                LNG = Float.valueOf(forOneDev.getString("lng"));

                float course = Float.valueOf(forOneDev.getString("course"));

                GeoPoint markerPoint = new GeoPoint(LAT, LNG);


                devMarker.setPosition(markerPoint);
                devMarker.setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_CENTER);
                devMarker.setTitle(nameObj);
                devMarker.setRotation(course);
                //devMarker.setAlpha(0);
                //mapController.setCenter(markerPoint);

                mapController.animateTo(markerPoint);

                map.invalidate();

                try{
                    double tank = Double.valueOf(forOneDev.getString("tank_max"));
                    int tank_max = (int) tank;
                    fuelProgress.setMax(tank_max);
                }catch (Exception e){

                }
                double NET = Double.valueOf(forOneDev.getString("net"));

                txVoltage.setText(pext + " " + "В");
                txSpeed.setText(speed + " км/ч");
                txSatellite.setText(sats);

                if (RESULT_JSON_PLACE.equals("failure")) {
                    tbSubTitle.setText("Адрес не доступен");
                } else {
                    tbSubTitle.setText(RESULT_JSON_PLACE + "  ");
                }

                if (!(FUEL == null)){
                    txFuel.setText(FUEL + " Л");
                    txFuel.setTextColor(getResources().getColor(R.color.black));

                    fuelImg.setImageResource(R.drawable.fuel4_dark);

                    double d = Double.valueOf(FUEL);
                    int prgs = (int) d;

                    fuelProgress.setProgress(prgs);
                }

                //adapter.setViewBinder(new MyViewBinder());

                if (NET == 0) {
                    imageSignal.setImageResource(example.kostya.mycar.R.drawable.signal_0_wh);
                } else {
                    switch (Integer.valueOf(signal)) {
                        case 0:
                            imageSignal.setImageResource(example.kostya.mycar.R.drawable.signal_0_wh); // Картинка
                            break;
                        case 1:
                            imageSignal.setImageResource(example.kostya.mycar.R.drawable.signal_2_wh); // Картинка
                            break;
                        case 2:
                            imageSignal.setImageResource(example.kostya.mycar.R.drawable.signal_2_wh); // Картинка
                            break;
                        case 3:
                            imageSignal.setImageResource(example.kostya.mycar.R.drawable.signal_3_wh); // Картинка
                            break;
                        case 4:
                            imageSignal.setImageResource(example.kostya.mycar.R.drawable.signal_4_wh); // Картинка
                            break;
                        case 5:
                            imageSignal.setImageResource(example.kostya.mycar.R.drawable.signal_5_wh); // Картинка
                            break;
                    }
                }

                if (!(RESULT_JSON_COMM == null)) {
                    h.sendEmptyMessage(RUN_COMMANDS);
                }

            } catch (Exception e) {
                e.printStackTrace();
                Log.d(LOG_TAG_4, "ОШИБКА DOLIST");
                return;
            }
        }
    }

    public class Place extends Thread {

        HttpURLConnection urlJsonPlace = null;
        BufferedReader readerPlace = null;
        private JSONObject someObj;

        @Override
        public void run() {    //Этот метод будет выполнен в побочном потоке
            try {
                if (!Thread.interrupted()) {
                    //h.sendEmptyMessage(SET_ACTION_ACTIVE);
                    URL url = new URL("http://a.kostya.example/json.device.php?ev=geo&id=" + ID_OBJ);

                    urlJsonPlace = (HttpURLConnection) url.openConnection();
                    urlJsonPlace.setRequestMethod("GET");
                    urlJsonPlace.connect();

                    InputStream inputStream = urlJsonPlace.getInputStream();
                    StringBuffer buffer = new StringBuffer();

                    readerPlace = new BufferedReader(new InputStreamReader(inputStream));

                    String line;
                    while ((line = readerPlace.readLine()) != null) {
                        buffer.append(line);
                    }
                    //h.sendEmptyMessage(SET_ACTION_INACTIVE);

                    try {
                        someObj = new JSONObject(buffer.toString());
                        JSONObject output = someObj.getJSONObject("output");
                        RESULT_JSON_PLACE = output.getString("geo");

                    } catch (JSONException e) {
                        e.printStackTrace();
                        return;
                    }


                    //h.sendEmptyMessage(STATUS_DOO);


                } else {
                    return;
                }
                try {
                    Thread.sleep(2000);
                } catch (Exception e) {
                    e.printStackTrace();
                    return;
                }

            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    class MyViewBinder implements SimpleAdapter.ViewBinder {

        @Override
        public boolean setViewValue(View view, Object data, String textRepresentation) {
            String i;
            try {
                switch (view.getId()) {
                    // LinearLayout
                    case R.id.ll_buttons:
                        i = ((String) data);
                        if (i.equals("start")) {

                            if (pext > prun) {
                                view.setBackgroundColor(getResources().getColor(R.color.activClr));
                            } else {
                                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
                                    view.setBackground(getResources().getDrawable(R.drawable.button_background));
                                }
                            }
                        }else{
                            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
                                view.setBackground(getResources().getDrawable(R.drawable.button_background));
                            }
                        }
                        return true;
                }
            } catch (Exception e) {
                return false;
            }
            return false;
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
            String idd = itemHashMap.get(COMID).toString();
            //Toast.makeText(InformActivity.this, idd, Toast.LENGTH_SHORT).show();
            idComand = Integer.valueOf(idd);

            AlertDialog.Builder builder = new AlertDialog.Builder(InformActivity.this);
            //builder.setTitle("Подтверждение")
            builder.setMessage("Выполнить команду?")
                    .setCancelable(false)
                    .setPositiveButton("Да", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            new Action().execute();
                            dialog.cancel();
                        }
                    })
                    .setNegativeButton("Нет", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            b = true;
                            dialog.cancel();
                        }
                    });
            AlertDialog alert = builder.create();
            alert.show();
        }
    };

    public class Ldata extends Thread {

        HttpURLConnection urlJson = null;
        BufferedReader reader = null;

        @Override
        public void run() {    //Этот метод будет выполнен в побочном потоке
            do {
                try {
                    if (!Thread.interrupted()) {
                        // Log.d(LOG_TAG_3, "..............backGround3...............");

                        if (INTERNET_OK) {
                            h.sendEmptyMessage(SET_ACTION_ACTIVE);
                        }

                        URL url = new URL("http://a.kostya.example/json.device.php");

                        urlJson = (HttpURLConnection) url.openConnection();
                        urlJson.setRequestMethod("GET");
                        urlJson.connect();

                        InputStream inputStream = urlJson.getInputStream();
                        StringBuffer buffer = new StringBuffer();

                        reader = new BufferedReader(new InputStreamReader(inputStream));

                        String line;
                        while ((line = reader.readLine()) != null) {
                            buffer.append(line);
                        }
                        h.sendEmptyMessage(SET_ACTION_INACTIVE);

                        RESULT_JSON_DEVICES = buffer.toString();
                        INTERNET_OK = true;

                        if (c) {
                            h.sendEmptyMessage(STATUS_DOO);
                        }
                    } else {
                        return;
                    }

                } catch (Exception e) {
                    e.printStackTrace();
                    Log.d(LOG_TAG_4, "НЕТ ИНТЕРНЕТА в LDATA ");

                    INTERNET_OK = false;
                    h.sendEmptyMessage(SET_ACTION_INACTIVE);
                }

                Log.d(LOG_TAG_4, "ЦИКЛ ИДЕТ... ");

                try {
                    Thread.sleep(2000);
                } catch (Exception e) {
                    e.printStackTrace();
                    return;
                }

            } while (true);
        }
    }

    public class Fuel extends Thread {

        HttpURLConnection urlJsonFuel = null;
        BufferedReader readerFuel = null;
        private JSONObject someObj;
        private int parAmount;

        @Override
        public void run() {    //Этот метод будет выполнен в побочном потоке
            try {
                if (!Thread.interrupted()) {
                    //h.sendEmptyMessage(SET_ACTION_ACTIVE);
                    URL url = new URL("http://a.kostya.example/json.parameter.php?did=" + ID_OBJ);

                    urlJsonFuel = (HttpURLConnection) url.openConnection();
                    urlJsonFuel.setRequestMethod("GET");
                    urlJsonFuel.connect();

                    InputStream inputStream = urlJsonFuel.getInputStream();
                    StringBuffer buffer = new StringBuffer();

                    readerFuel = new BufferedReader(new InputStreamReader(inputStream));

                    String line;
                    while ((line = readerFuel.readLine()) != null) {
                        buffer.append(line);
                    }
                    //h.sendEmptyMessage(SET_ACTION_INACTIVE);

                    try {
                        RESULT_JSON_PARAMETER = buffer.toString();//на всякий случай

                        someObj = new JSONObject(buffer.toString());
                        JSONObject output = someObj.getJSONObject("output");
                        JSONObject get = output.getJSONObject("get");
                        parAmount = Integer.valueOf(get.getString("count"));

                        for (int i = 0; i < parAmount; i++) {
                            JSONObject fuel = get.getJSONObject(String.valueOf(i));
                            int view = Integer.valueOf(fuel.getString("view"));
                            String parName = fuel.getString("name");

                            if (view == 1 && parName.equals("Уровень топлива")) {
                                FUEL = fuel.getString("val");
                            }
                        }

                    } catch (JSONException e) {
                        e.printStackTrace();
                        return;
                    }

                } else {
                    return;
                }
                try {
                    Thread.sleep(2000);
                } catch (Exception e) {
                    e.printStackTrace();
                    return;
                }

            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

}
