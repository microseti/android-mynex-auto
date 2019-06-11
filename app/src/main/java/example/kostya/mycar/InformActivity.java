package example.kostya.mycar;

import android.Manifest;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.drawable.Drawable;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.AsyncTask;
import android.os.Handler;
import android.os.Message;
import android.os.Parcelable;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
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
import android.widget.RelativeLayout;
import android.widget.SimpleAdapter;
import android.widget.TextView;
import android.widget.Toast;

/*import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationServices;*/

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.osmdroid.api.IMapController;
//import org.osmdroid.bonuspack.overlays.Marker;
import org.osmdroid.tileprovider.tilesource.TileSourceFactory;
import org.osmdroid.util.GeoPoint;
import org.osmdroid.views.MapView;
import org.osmdroid.views.overlay.Marker;
import org.osmdroid.views.overlay.mylocation.GpsMyLocationProvider;
import org.osmdroid.views.overlay.mylocation.MyLocationNewOverlay;

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

public class InformActivity extends AppCompatActivity {

    String SAVED_PASSWORD = "saved_password";
    String SAVED_LOGIN = "saved_login";
    String DEV_NAME = "name_of_device";
    String DEV_ID_SHARED = "dev_id_shared";

    private String SESSION_CLOSE = "\t\t\t<script type=\"text/javascript\">\t\t\t\tlocation.replace (\"auth.php\");\t\t\t</script>\t\t";

    private static final String COMNAME = "comname";
    private static final String COMID = "comid";
    private static final String ACTIONICON = "actionicon";

    static String LOG_TAG_3 = "my_log_3";
    static String LOG_TAG_4 = "sessres";

    String ID_FROM_DEVICE_LIST;
    String LID;

    /*final int GPS_EVENT_STARTED = 1;
    final int GPS_EVENT_STOPPED = 2;*/

    final int STATUS_DOO = 1;
    final int RUN_COMMANDS = 2;
    final int RUN_STAT_ACTION = 3;
    final int RUN_NOTHING = 0;
    final int SET_ACTION_ACTIVE = 7;
    final int SET_ACTION_INACTIVE = 8;
    final int TOAST_MAKE = 9;
    final int REQUEST_ACCESS_FINE_LOCATION = 1;
    final int REQUEST_WRITE_EXTERNAL_STORAGE = 2;
    final int ON_MYLOC = 1;
    final int OFF_MYLOC = 2;

    final int REAUTH = 10;
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
    TextView label_name;
    TextView tbSubTitle;

    ImageView imageSignal;
    ImageView imageAction;


    ImageView fuelImg;

    ImageButton devBut;
    ImageButton meBut;

    ////////////_images on toolbar_//////////////
    ImageView imageEngine;
    ImageView imageMove;
    ImageView imageLock;
    ImageView imageGuard;
    ImageView imageAdbat;
    /////////////////////////////////////////////

    Handler h;

    private String RESULT_JSON_COMM = "";
    private String RESULT_COM_STAT;
    private String RESULT_JSON_COM_LID;
    private String RESULT_JSON_DEVICES = "";
    private String RESULT_JSON_PLACE = "- - -";
    private String RESULT_JSON_PARAMETER;

    private LdataInform mLdata;
    private Dolist doo;


    private Boolean b;
    private Boolean c;
    private Boolean INTERNET_OK;
    private Boolean RESUMED; //при активном активити - true, при свернутом приложении false
    private Boolean IN_POSITION;

    Toolbar toolbar;
    ImageButton btnToolbarButton = null;
    ProgressDialog pd;
    ProgressDialog pdSession;
    SimpleAdapter adapter;
    ProgressBar fuelProgress;

    Prefer load;


    ////////////////__Карта__//////////////
    RelativeLayout mapLay;

    MapView map;
    IMapController mapController;
    GeoPoint startPoint;
    Marker devMarker;
    Marker meMarker;
    GeoPoint markerPoint;
    MyLocationNewOverlay mLocationOverlay;
    LocationManager locationManager;
    //GoogleApiClient mGoogleApiClient;
    //////////////////////////////////////

    private LComandList mLCom;
    private Close close;

    private Float prun;
    private Float pext;
    private int guard;
    private double LAT = 0;
    private double LNG = 0;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_inform);
        //LeakCanary.install(getApplication());

        load = new Prefer();
        load.initt(getApplicationContext());

        pd = new ProgressDialog(this);
        pdSession = new ProgressDialog(this);//рестарт сессии

        IN_POSITION = false;

        try {
            Intent intent = getIntent();
            ID_FROM_DEVICE_LIST = intent.getStringExtra("idd");
        } catch (Exception e) {
            e.printStackTrace();
            ID_FROM_DEVICE_LIST = load.getProperty(DEV_ID_SHARED);
        }


        ID_OBJ = Integer.valueOf(ID_FROM_DEVICE_LIST);


        //Prefer load = new Prefer();

        toolbar = (Toolbar) findViewById(R.id.tool_bar_inform);
        toolbar.setTitle("");
        toolbar.setNavigationIcon(R.drawable.icon_toolbal_arrow_white);
        setSupportActionBar(toolbar);

        txFuel = (TextView) findViewById(R.id.txFuel);
        txSpeed = (TextView) findViewById(R.id.txSpeed);
        txSatellite = (TextView) findViewById(R.id.txSatellite);
        txVoltage = (TextView) findViewById(R.id.txVoltage);
        tbSubTitle = (TextView) findViewById(R.id.tbSubTitle);
        label_name = (TextView) findViewById(R.id.label_name);
        label_name.setText(load.getProperty(DEV_NAME));

        imageSignal = (ImageView) findViewById(R.id.imageSignal);
        imageAction = (ImageView) findViewById(R.id.imageAction);
        fuelImg = (ImageView) findViewById(R.id.fuelImg);


        imageAdbat = (ImageView) findViewById(R.id.imageAdBat);
        imageGuard = (ImageView) findViewById(R.id.imageGuard);
        imageLock = (ImageView) findViewById(R.id.imageLock);
        imageMove = (ImageView) findViewById(R.id.imageMove);
        imageEngine = (ImageView) findViewById(R.id.imageEngine);

        //imageMove.getDrawingCache().recycle();

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
        mapLay = (RelativeLayout) findViewById(R.id.mapLay);
        map = new MapView(InformActivity.this);
        map.setTileSource(TileSourceFactory.MAPNIK);

        RelativeLayout.LayoutParams layPar = new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.MATCH_PARENT,
                RelativeLayout.LayoutParams.MATCH_PARENT);

        map.setLayoutParams(layPar);

        mapLay.addView(map);


        //map = (MapView) findViewById(R.id.map);
        //map.setDrawingCacheEnabled(true);


        map.setBuiltInZoomControls(true);
        map.setMultiTouchControls(true);

        //map.

        mapController = map.getController();
        mapController.setZoom(15);
        startPoint = new GeoPoint(43.116667, 131.9);
        mapController.setCenter(startPoint);


        devBut = (ImageButton) findViewById(R.id.devBut);
        devBut.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                try {
                    mapController.animateTo(markerPoint);
                    map.invalidate();
                } catch (Exception e) {
                    return;
                }
            }
        });

        meBut = (ImageButton) findViewById(R.id.meBut);
        meBut.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                /*try {
                    myPlace();
                } catch (Exception e) {
                    Toast toast = Toast.makeText(InformActivity.this, "ошибка..", Toast.LENGTH_SHORT);
                    toast.setGravity(Gravity.CENTER, 0, 0);
                    toast.show();
                }*/

                try {
                    mapController.animateTo(mLocationOverlay.getMyLocation());
                    map.invalidate();
                } catch (Exception e) {
                    return;
                }
            }
        });


        devMarker = new Marker(map);
        Drawable devIcon = getResources().getDrawable(R.drawable.dev_marker);
        //devIcon.
        devMarker.setIcon(devIcon);

        /*meMarker = new Marker(map);
        meMarker.setIcon(getResources().getDrawable(R.drawable.me_marker));*/

        /*myPlace();

        this.mLocationOverlay = new MyLocationNewOverlay(getApplicationContext(), new GpsMyLocationProvider(getApplicationContext()),map);
        map.getOverlays().add(this.mLocationOverlay);
        mLocationOverlay.enableMyLocation();*/


        map.getOverlays().add(devMarker);
        //map.getOverlays().add(meMarker);*/
        //////////////////////////////////////

        h = new Handler() {
            public void handleMessage(Message msg) {
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
                        imageAction.setImageResource(R.drawable.ic_data_24dp);
                        break;
                    case SET_ACTION_INACTIVE:
                        imageAction.setImageResource(R.drawable.ic_data_trnsp_24dp);
                        break;
                    case TOAST_MAKE:
                        Toast.makeText(getApplicationContext(), "Нет соединения с интернет", Toast.LENGTH_SHORT).show();
                        break;
                    case REAUTH:

                        /*Boolean interr = mLdata.isAlive();

                        if (interr) {
                            mLdata.interrupt();
                        }*/

                        new ReAuthorize().execute();
                        break;

                    //case START_PROGRESS_DIALOG:
                }
            }
        };
        h.sendEmptyMessage(RUN_NOTHING);

        INTERNET_OK = true;

        mLdata = new LdataInform();
        mLdata.start();

        mLCom = new LComandList();
        mLCom.start();

        try {
            myPlace(ON_MYLOC);
        } catch (Exception e) {
            Log.d(LOG_TAG_4, "та да");
        }

        locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);

        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED
                && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION)
                != PackageManager.PERMISSION_GRANTED
                && ActivityCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE)
                != PackageManager.PERMISSION_GRANTED) {

            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            //return;

            ActivityCompat.requestPermissions(InformActivity.this,
                    new String[]{Manifest.permission.ACCESS_FINE_LOCATION,
                            Manifest.permission.ACCESS_COARSE_LOCATION},
                    REQUEST_ACCESS_FINE_LOCATION);
            ActivityCompat.requestPermissions(InformActivity.this,
                    new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE},
                    REQUEST_WRITE_EXTERNAL_STORAGE);
        } else{
            locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, //обнаружена проблема
                    0, 10, locationListener);
        }

        /*if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            locationManager.registerGnssStatusCallback(new GnssStatus.Callback() {
                @Override
                public void onStarted() {
                    super.onStarted();
                    //
                    Toast.makeText(getApplicationContext(), "GNSS STARTED", Toast.LENGTH_SHORT).show();
                }

                @Override
                public void onStopped() {
                    super.onStopped();
                    //
                    Toast.makeText(getApplicationContext(), "GNSS STOPED", Toast.LENGTH_SHORT).show();
                }
            });
        } else{
            locationManager.addGpsStatusListener(new GpsStatus.Listener()
            {
                public void onGpsStatusChanged(int event)
                {
                    switch(event)
                    {
                        case GpsStatus.GPS_EVENT_STARTED:
                            Log.d(LOG_TAG_4, "GPS ENABLED");
                            Toast.makeText(getApplicationContext(), "GPS ENABLED", Toast.LENGTH_SHORT).show();
                            break;
                        case GpsStatus.GPS_EVENT_STOPPED:
                            Log.d(LOG_TAG_4, "GPS DISABLED");
                            Toast.makeText(getApplicationContext(), "GPS DISABLED", Toast.LENGTH_SHORT).show();
                            break;
                    }
                }
            });
        }*/

        /*if (mGoogleApiClient == null) {
            mGoogleApiClient = new GoogleApiClient.Builder(this)
                    .addConnectionCallbacks(this)
                    .addOnConnectionFailedListener(this)
                    .addApi(LocationServices.API)
                    .build();
        }*/


    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(example.kostya.mycar.R.menu.menu_inform, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        int id = item.getItemId();

        switch (id) {
            case R.id.action_settings:
                RESUMED = false;
                Prefer save = new Prefer();
                save.addProperty(SAVED_LOGIN, "");
                save.addProperty(SAVED_PASSWORD, "");
                save.addProperty(DEV_NAME, "");

                Intent intent = new Intent(InformActivity.this, LoginActivity.class)//вылетает когда нет устройств в списке
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
            save.addProperty(DEV_NAME, "");

            Intent intent = new Intent(InformActivity.this, LoginActivity.class)//вылетает когда нет устройств в списке
                    .addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK)
                    .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(intent);

            return true;
        }*/
        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onStart() {
        //mGoogleApiClient.connect();
        super.onStart();
    }

    @Override
    public void onPause() {
        super.onPause();
        mLCom.interrupt();
        Log.d(LOG_TAG_4, "onPause");
        c = false;
        mLdata.interrupt();

        if (locationManager != null) {
            if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                // TODO: Consider calling
                //    ActivityCompat#requestPermissions
                // here to request the missing permissions, and then overriding
                //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
                //                                          int[] grantResults)
                // to handle the case where the user grants the permission. See the documentation
                // for ActivityCompat#requestPermissions for more details.
                return;
            }
            locationManager.removeUpdates(locationListener);
        }
        locationManager = null;

        map.getTileProvider().clearTileCache();

        h.removeCallbacksAndMessages(null);


    }

    @Override
    public void onResume() {
        super.onResume();


        Log.d(LOG_TAG_4, "onResume");

        //load = new Prefer();
        load.initt(getApplicationContext());

        c = true;
        b = true;
        RESUMED = true;

        if (!mLCom.isAlive()) {
            mLCom = new LComandList();
            mLCom.start();
        }

        if (!mLdata.isAlive()) {
            mLdata = new LdataInform();
            Log.d(LOG_TAG_4, "new data");
            mLdata.start();
        }
    }

    @Override
    public void onBackPressed() {
        RESUMED = false;
        super.onBackPressed();
        c = false;
        Log.d(LOG_TAG_3, "нажато назад");
    }

    @Override
    protected void onStop() {
        //mGoogleApiClient.disconnect();
        super.onStop();
        Log.d(LOG_TAG_4, "onStop");
        //c = false;
        mLdata.interrupt();
        RESUMED = false;

       /*map.getDrawingCache().recycle();*/
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        Log.d(LOG_TAG_4, "onDestroy");
        //fuelImg.setImageDrawable(null);

        //mapLay.destroyDrawingCache();

        // map.getOverlays().;

        mapLay.removeView(map);


        RelativeLayout mainLay = (RelativeLayout) findViewById(R.id.mainLay);
        mainLay.removeAllViews();


        //Mycar.instance.mustDie(this);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           String permissions[], int[] grantResults) {
        switch (requestCode) {
            case REQUEST_ACCESS_FINE_LOCATION:
                // If request is cancelled, the result arrays are empty.
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED
                        && grantResults[1] == PackageManager.PERMISSION_GRANTED) {

                    // permission was granted, yay! Do the
                    // contacts-related task you need to do.
                    //итак уже проверил, но все равно просит проверку
                    if (ActivityCompat.checkSelfPermission(this, permissions[0])
                            != PackageManager.PERMISSION_GRANTED
                            && ActivityCompat.checkSelfPermission(this, permissions[1])
                            != PackageManager.PERMISSION_GRANTED) {
                        return;
                    }
                    locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, //обнаружена проблема
                            0, 10, locationListener);
                    try {
                        myPlace(ON_MYLOC);
                    } catch (Exception e) {
                        Log.d(LOG_TAG_4, "та да");
                    }

                } else {

                    // permission denied, boo! Disable the
                    // functionality that depends on this permission.
                }
                break;

            case REQUEST_WRITE_EXTERNAL_STORAGE:

                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {

                    // permission was granted, yay! Do the
                    // contacts-related task you need to do.
                    //чето с картой делать надо

                } else {

                    // permission denied, boo! Disable the
                    // functionality that depends on this permission.
                }
                break;

            // other 'case' lines to check for other
            // permissions this app might request
        }
    }

    private LocationListener locationListener = new LocationListener() {


        @Override
        public void onLocationChanged(Location location) {

        }

        @Override
        public void onStatusChanged(String provider, int status, Bundle extras) {

        }

        @Override
        public void onProviderEnabled(String provider) {
            Toast.makeText(getApplicationContext(), "GPS ENABLED", Toast.LENGTH_SHORT).show();
            //Log.d(LOG_TAG_4, "Enabled.................");
            myPlace(ON_MYLOC); //потом восстановить
        }

        @Override
        public void onProviderDisabled(String provider) {

            Toast.makeText(getApplicationContext(), "GPS DISABLED", Toast.LENGTH_SHORT).show();
            myPlace(OFF_MYLOC);

        }


    };

    private class LComandList extends Thread {

        HttpURLConnection urlCommands = null;
        BufferedReader readerCom = null;

        @Override
        public void run() {    //Этот метод будет выполнен в побочном потоке
            try {
                if (!Thread.interrupted()) {
                    URL url = new URL("https://mynex.ru/json.command.php?ev=get");

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
                h.removeCallbacksAndMessages(null);
            }
            //return;
        }
    }

    private class CommandsList {
        private ArrayList<HashMap<String, Object>> mComands;
        JSONArray selectedComm = null; // массив из выбранных комманд для конкретного объекта
        JSONObject jsonCommand = null;
        Boolean ST_ST = false; //присутствует команда стоп и старт --> true
        String STOP_ID; //id команды стоп
        String UNGUARD_ID; //id команды разблокировки


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

                        switch (nameCom){
                            /*case "stop":
                                ST_ST = true;
                                STOP_ID = command.getString("id");
                                break;*/
                            case "unguard":
                                UNGUARD_ID = command.getString("id");
                                break;
                            case "guard":
                                if(guard != -1){
                                    selectedComm.put(j, command);
                                    j = j + 1;
                                }
                                break;
                            default:
                                selectedComm.put(j, command);
                                j = j + 1;
                                break;
                        }

                        /*if (nameCom.equals("stop")){
                            ST_ST = true;
                            STOP_ID = command.getString("id");
                        } else{
                            selectedComm.put(j, command);
                            j = j + 1;
                        }*/
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
                        hm.put(ACTIONICON, R.drawable.key_start);
                    }
                    ;
                    if (comTitle.equals("stop")) {
                        hm.put(ACTIONICON, R.drawable.key_stop);
                    }
                    ;
                    if (comTitle.equals("lock")) {
                        hm.put(ACTIONICON, R.drawable.ic_lock_24dp);
                    }
                    ;
                    if (comTitle.equals("unlock")) {
                        hm.put(ACTIONICON, R.drawable.ic_unlock_24dp);
                    }
                    ;
                    if (comTitle.equals("guard")) {
                        hm.put(ACTIONICON, R.drawable.ic_guard_24dp);
                    }
                    ;
                    hm.put(COMNAME, comTitle);

                    //условия выбора id для стопа старта
                    /*if (ST_ST && pext > prun && comTitle.equals("start")){
                        comId = STOP_ID;
                    }*/

                    if(comTitle.equals("guard") && guard == 1){
                        comId = UNGUARD_ID;
                    }

                    hm.put(COMID, comId); // ключ/значение = comid/comId

                    mComands.add(hm);
                }

            } catch (JSONException e) {
                e.printStackTrace();
            }

            try {
/*
                SimpleAdapter adapter = new SimpleAdapter(InformActivity.this, mComands, R.layout.comandlist, new String[]{COMNAME}, new int[]{R.id.comName});
*/

                adapter = new SimpleAdapter(InformActivity.this, mComands, R.layout.buttons_list,
                        new String[]{ACTIONICON/*, COMNAME*/},
                        new int[]{R.id.actionImg/*, R.id.ll_buttons*/});
                //adapter.setViewBinder(new MyViewBinder());

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
                URL url = new URL("https://mynex.ru/json.command.php?ev=does&id=" + idComand);

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
                    URL url = new URL("https://mynex.ru/json.command.php?ev=sta&lid=" + LID);

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
                    Toast.makeText(InformActivity.this, "fail_smth", Toast.LENGTH_SHORT).show();
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

            /*pd.setIndeterminate(false);
            pd.cancel();*/

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
            //AlertDialog alert = builder.create();

            switch (result) {
                case 2:
                    pd.setIndeterminate(false);
                    pd.cancel();
                    builder.setMessage("Команда отменена");
                    //alert.show();
                    break;
                case 3:
                    pd.setMessage("Выполнено!");
                    try {
                        Thread.sleep(500);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                    pd.setIndeterminate(false);
                    pd.cancel();
                    break;
                case 4:
                    pd.setIndeterminate(false);
                    pd.cancel();
                    builder.setMessage("Ошибка выполнения");
                    //alert.show();
                    break;
                case 5:
                    pd.setIndeterminate(false);
                    pd.cancel();
                    builder.setMessage("Вышло время ожидания");
                    //alert.show();
                    break;
                case 6:
                    pd.setIndeterminate(false);
                    pd.cancel();
                    builder.setMessage("Объект не на связи");
                    //alert.show();
                    break;
                case 7:
                    pd.setIndeterminate(false);
                    pd.cancel();
                    builder.setMessage("Связь с сервером прервана");
                    //alert.show();
                    break;
                default:
                    pd.setIndeterminate(false);
                    pd.cancel();
                    builder.setMessage("Данные не получены");
                    //alert.show();
                    break;
            }
            if (result != 3){
                AlertDialog alert = builder.create();
                alert.show();
            }



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

            if (!mLCom.isAlive() && (RESULT_JSON_COMM.equals("") || RESULT_JSON_COMM.equals(SESSION_CLOSE))) {
                mLCom = new LComandList();
                mLCom.start();
            }

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


                guard = Integer.valueOf(forOneDev.getString("guard"));
                prun = Float.valueOf(forOneDev.getString("prun"));
                pext = Float.valueOf(forOneDev.getString("pext"));

                LAT = Float.valueOf(forOneDev.getString("lat"));
                LNG = Float.valueOf(forOneDev.getString("lng"));

                float course = Float.valueOf(forOneDev.getString("course"));

                markerPoint = new GeoPoint(LAT, LNG);
                GeoPoint mePoint = new GeoPoint(43.116667, 131.9);

                //mLocationOverlay.enableMyLocation();


                devMarker.setPosition(markerPoint);
                devMarker.setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_CENTER);
                devMarker.setTitle(nameObj);
                devMarker.setRotation(course);
                //devMarker.setAlpha(0);
                //mapController.setCenter(markerPoint);

                /*meMarker.setPosition(mePoint);
                meMarker.setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM);
                meMarker.setTitle("Я");*/

                if(!IN_POSITION){
                    mapController.animateTo(markerPoint);
                    IN_POSITION = true;
                }

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
/////////////////////////__проверка наличия датчика топлива__//////////////
                if (!(FUEL == null)){
                    txFuel.setText(FUEL + " Л");
                    txFuel.setTextColor(getResources().getColor(R.color.black));

                    fuelImg.setImageResource(R.drawable.fuel4_dark);

                    double d = Double.valueOf(FUEL);
                    int prgs = (int) d;

                    fuelProgress.setProgress(prgs);
                    fuelProgress.setVisibility(ProgressBar.VISIBLE);
                }

                //adapter.setViewBinder(new MyViewBinder());

                if (NET == 0) {
                    imageSignal.setImageResource(R.drawable.ic_signal_lost_24dp);
                } else {
                    switch (Integer.valueOf(signal)) {
                        case 0:
                            imageSignal.setImageResource(R.drawable.ic_signal_0_24dp); // Картинка
                            break;
                        case 1:
                            imageSignal.setImageResource(R.drawable.ic_signal_1_24dp); // Картинка
                            break;
                        case 2:
                            imageSignal.setImageResource(R.drawable.ic_signal_2_24dp); // Картинка
                            break;
                        case 3:
                            imageSignal.setImageResource(R.drawable.ic_signal_3_24dp); // Картинка
                            break;
                        case 4:
                            imageSignal.setImageResource(R.drawable.ic_signal_4_24dp); // Картинка
                            break;
                        case 5:
                            imageSignal.setImageResource(R.drawable.ic_signal_5_24dp); // Картинка
                            break;
                    }
                }

                if(Float.valueOf(speed) > 0){   ///////////////////////////////// moving
                    imageMove.setImageResource(R.drawable.ic_wheel_24dp);
                }else{
                    imageMove.setImageResource(R.drawable.ic_wheel_trnsp_24dp);
                }

                if(pext > prun){  /////////////////////////////////////////////// running
                    imageEngine.setImageResource(R.drawable.ic_key_24dp);
                }else{
                    imageEngine.setImageResource(R.drawable.ic_key_trnspr_24dp);
                }

                if (!(RESULT_JSON_COMM == null)) {
                    h.sendEmptyMessage(RUN_COMMANDS);
                    Log.d(LOG_TAG_4, "построение команд....." + RESULT_JSON_COMM);
                }

                switch(Integer.valueOf(forOneDev.getString("guard"))){  ////////////// guard
                    case 0:
                        imageGuard.setImageResource(R.drawable.ic_guard_trnsp_24dp);
                        break;
                    case 1:
                        imageGuard.setImageResource(R.drawable.ic_guard_24dp);
                        break;
                }

                switch(Integer.valueOf(forOneDev.getString("pbat"))){  ////////////// guard
                    case 0:
                        imageAdbat.setImageResource(R.drawable.ic_adbat_trnsp_24dp);
                        break;
                    case -1:
                        break;
                    default:
                        imageAdbat.setImageResource(R.drawable.ic_adbat_24dp);
                        break;
                }

                switch(Integer.valueOf(forOneDev.getString("il"))){  ////////////// guard
                    case 0:
                        imageLock.setImageResource(R.drawable.ic_unlock_24dp);
                        break;
                    case 1:
                        imageLock.setImageResource(R.drawable.ic_lock_24dp);
                        break;
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
                    URL url = new URL("https://mynex.ru/json.device.php?ev=geo&id=" + ID_OBJ);

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

    /*class MyViewBinder implements SimpleAdapter.ViewBinder {

        @TargetApi(Build.VERSION_CODES.JELLY_BEAN)
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
                                view.setBackground(getResources().getDrawable(R.drawable.button_background_active));
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
    }*/

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

    private class LdataInform extends Thread {

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

                        URL url = new URL("https://mynex.ru/json.device.php");

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
                        Log.d(LOG_TAG_4, RESULT_JSON_DEVICES);

                        if (c) {

                            if(RESULT_JSON_DEVICES.equals(SESSION_CLOSE)){
                                //h.sendEmptyMessage(TOAST_MAKE);
                                Log.d(LOG_TAG_4, "clooose");

                                //mLdata.interrupt();

                                if(RESUMED){
                                    h.sendEmptyMessage(REAUTH);
                                }

                                return;

                            }else{
                                h.sendEmptyMessage(STATUS_DOO);
                            }
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
                    h.removeCallbacksAndMessages(null);
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
                    URL url = new URL("https://mynex.ru/json.parameter.php?did=" + ID_OBJ);

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

    private class ReAuthorize extends AsyncTask<Void, Void, String> {

        HttpURLConnection urlAuth = null;
        String result;
        String cookie;
        BufferedReader reader1 = null;
        String resultAuth = "";
        Boolean statConnection;

        //Prefer load;

        String username;
        String password;

        String loginStat;

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            //progressBar2.setVisibility(ProgressBar.VISIBLE);

            //load = new Prefer();
            //load.initt(InformActivity.this);

            if(RESUMED == false){
                return;
            }

            CookieManager cookieManager = new CookieManager();
            CookieHandler.setDefault(cookieManager);

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

            pdSession.cancel();

            if (rez == null){

                if (!mLdata.isAlive() && RESUMED) {
                    mLdata = new LdataInform();
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

                    //Boolean alive = mLdata.is

                   if (!mLdata.isAlive() && RESUMED) {

                        mLdata = new LdataInform();
                        mLdata.start();
                    }

                   return;

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
                    mLdata = new LdataInform();
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

                    Log.d(LOG_TAG_4, "closing");

                    InputStream inputStream = urlClose.getInputStream();
                    StringBuffer buffer = new StringBuffer();
                    readerCls = new BufferedReader(new InputStreamReader(inputStream));
                    String line;
                    while ((line = readerCls.readLine()) != null) {
                        buffer.append(line);
                    }
                    resultClose = buffer.toString();
                    Log.d(LOG_TAG_4, resultClose);

                } else {
                    return;
                }

            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    public void myPlace(int onOff){
        this.mLocationOverlay = new MyLocationNewOverlay(new GpsMyLocationProvider(getApplicationContext()),map);

        Log.d(LOG_TAG_4, "Location.............");

        switch(onOff){
            case ON_MYLOC:
                map.getOverlays().add(this.mLocationOverlay);
                mLocationOverlay.enableMyLocation();
                break;
            case OFF_MYLOC:
                mLocationOverlay.disableMyLocation();
                map.getOverlays().remove(mLocationOverlay);
                break;
        }

    }




}
