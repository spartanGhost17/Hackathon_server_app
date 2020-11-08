package com.example.hackathon;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import android.Manifest;
import android.animation.ValueAnimator;
import android.content.Context;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.PointF;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.StrictMode;
import android.provider.SyncStateContract;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.RadioButton;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;
import android.widget.Toast;

import com.google.gson.internal.$Gson$Preconditions;
import com.mapbox.android.core.location.LocationEngine;
import com.mapbox.android.core.permissions.PermissionsListener;
import com.mapbox.geojson.Feature;
import com.mapbox.geojson.FeatureCollection;
import com.mapbox.geojson.GeoJson;
import com.mapbox.geojson.Geometry;
import com.mapbox.geojson.LineString;
import com.mapbox.geojson.Point;
import com.mapbox.mapboxsdk.Mapbox;
import com.mapbox.mapboxsdk.annotations.BubbleLayout;
import com.mapbox.mapboxsdk.annotations.MarkerOptions;
import com.mapbox.mapboxsdk.camera.CameraPosition;
import com.mapbox.mapboxsdk.camera.CameraUpdate;
import com.mapbox.mapboxsdk.camera.CameraUpdateFactory;
import com.mapbox.mapboxsdk.geometry.LatLng;
import com.mapbox.mapboxsdk.maps.MapView;
import com.mapbox.mapboxsdk.maps.MapboxMap;
import com.mapbox.mapboxsdk.maps.OnMapReadyCallback;
import com.mapbox.mapboxsdk.maps.Style;
import com.mapbox.mapboxsdk.plugins.annotation.FillOptions;
import com.mapbox.mapboxsdk.plugins.annotation.OnSymbolClickListener;
import com.mapbox.mapboxsdk.plugins.annotation.OnSymbolDragListener;
import com.mapbox.mapboxsdk.plugins.annotation.OnSymbolLongClickListener;
import com.mapbox.mapboxsdk.plugins.annotation.Symbol;
import com.mapbox.mapboxsdk.plugins.annotation.SymbolManager;
import com.mapbox.mapboxsdk.plugins.annotation.SymbolOptions;
import com.mapbox.mapboxsdk.plugins.locationlayer.LocationLayerPlugin;
import com.mapbox.mapboxsdk.plugins.markerview.MarkerView;
import com.mapbox.mapboxsdk.plugins.markerview.MarkerViewManager;
import com.mapbox.mapboxsdk.style.layers.PropertyFactory;
import com.mapbox.mapboxsdk.style.layers.SymbolLayer;
import com.mapbox.mapboxsdk.style.sources.GeoJsonSource;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.lang.ref.WeakReference;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.net.URLEncoder;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Objects;

import timber.log.Timber;

import static com.mapbox.mapboxsdk.style.expressions.Expression.eq;
import static com.mapbox.mapboxsdk.style.expressions.Expression.get;
import static com.mapbox.mapboxsdk.style.expressions.Expression.literal;
import static com.mapbox.mapboxsdk.style.layers.Property.ICON_ANCHOR_BOTTOM;
import static com.mapbox.mapboxsdk.style.layers.PropertyFactory.iconAllowOverlap;
import static com.mapbox.mapboxsdk.style.layers.PropertyFactory.iconAnchor;
import static com.mapbox.mapboxsdk.style.layers.PropertyFactory.iconColor;
import static com.mapbox.mapboxsdk.style.layers.PropertyFactory.iconImage;
import static com.mapbox.mapboxsdk.style.layers.PropertyFactory.iconOffset;
import static com.mapbox.mapboxsdk.style.layers.PropertyFactory.iconTextFit;
import static android.view.ViewGroup.LayoutParams.WRAP_CONTENT;


public class MainActivity extends AppCompatActivity implements OnMapReadyCallback, Style.OnStyleLoaded , MapboxMap.OnMapClickListener
        ,PermissionsListener, LocationListener{
    protected double lat = 0;
    protected double lng = 0;
    protected boolean permissionCheck = false;
    protected ArrayList<Station> foundResults;
    protected MapView mapView;
    protected MapboxMap mapboxMap;
    protected PermissionsListener permissionsListener;
    protected LocationEngine locationEngine;
    protected LocationLayerPlugin locationLayerPlugin;
    protected Location originLocation;

    protected TextView displayText;
    protected TableLayout table;
    protected EditText userInput;
    protected RadioButton byPostcode;
    protected RadioButton byLocation;
    protected RadioButton byName;

    protected GeoJson geoJson;
    protected static final String ICON_ID = "ICON_ID";
    protected static final String SOURCE_ID = "SOURCE_ID";

    protected static final String LAYER_ID = "LAYER_ID";

    protected ValueAnimator markerAnimator;
    protected boolean markerSelected = false;


    protected static final String GEOJSON_SOURCE_ID = "GEOJSON_SOURCE_ID";
    protected static final String MARKER_IMAGE_ID = "MARKER_IMAGE_ID";
    protected static final String MARKER_LAYER_ID = "MARKER_LAYER_ID";
    protected static final String CALLOUT_LAYER_ID = "CALLOUT_LAYER_ID";
    protected static final String PROPERTY_SELECTED = "selected";
    protected static final String PROPERTY_NAME = "name";
    protected static final String PROPERTY_CAPITAL = "capital";
    protected GeoJsonSource source;
    protected FeatureCollection featureCollection;

    protected SymbolManager symbolManager;
    protected Symbol symbol;
    protected Symbol symbol1;
    protected ArrayList<Symbol> currentSymbols = new ArrayList<Symbol>();


    protected static final String MAKI_ICON_CAFE = "cafe-15";
    protected static final String MAKI_ICON_HARBOR = "harbor-15";
    protected static final String MAKI_ICON_AIRPORT = "airport-15";
    protected MarkerViewManager markerViewManager;

    protected MarkerView markerView;

    protected int w = 0;

    protected ArrayList<Feature> currentFeatures = new ArrayList<Feature>();

    //protected MapboxDirections mapboxDirectionsClient;
    protected Handler handler = new Handler();
    protected Runnable runnable;
    private static final float NAVIGATION_LINE_WIDTH = 6;
    private static final float NAVIGATION_LINE_OPACITY = .8f;
    private static final String DRIVING_ROUTE_POLYLINE_LINE_LAYER_ID = "DRIVING_ROUTE_POLYLINE_LINE_LAYER_ID";
    private static final String DRIVING_ROUTE_POLYLINE_SOURCE_ID = "DRIVING_ROUTE_POLYLINE_SOURCE_ID";
    private static final int DRAW_SPEED_MILLISECONDS = 500;

    //protected LocationChangeListeningActivityLocationCallback callback =
    //      new LocationChangeListeningActivityLocationCallback(this);





    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Mapbox.getInstance(this, getString(R.string.access_token));//give map box application context and access token on run time
        setContentView(R.layout.activity_main);
        //map

        mapView = (MapView) findViewById(R.id.mapView);
        mapView.onCreate(savedInstanceState);
        mapView.getMapAsync(this);

        /**
         * get asynchronous from mapView Object declare new anonymous onMapReadyCallBack
         * override onMapReady, since only need to create a map once on app creation we can do it here
         * after collecting current Latitude and longitude
         */

        mapView.getMapAsync(new OnMapReadyCallback() {
            @Override
            public void onMapReady(@NonNull MapboxMap mapboxMap) {
                mapboxMap.setCameraPosition(
                        new CameraPosition.Builder()
                                .target(new LatLng(lat, lng))
                                .zoom(6)
                                .tilt(30)
                                .bearing(180) // Rotate the camera
                                .build()
                );
            }
        });

        //mapView.getMapAsync(new OnSty);
        foundResults = new ArrayList<Station>();

        //permission check
        String[] requiredPermissions = {
                Manifest.permission.INTERNET,
                Manifest.permission.ACCESS_FINE_LOCATION,
                Manifest.permission.ACCESS_COARSE_LOCATION
        };
        boolean ok = true;
        for(int i = 0; i<requiredPermissions.length; i++){
            int result = ActivityCompat.checkSelfPermission(this,requiredPermissions[i]);
            if(result != PackageManager.PERMISSION_GRANTED){
                ok = false;
            }
        }
        if(!ok){

            ActivityCompat.requestPermissions(this, requiredPermissions, 1);
            System.exit(0);
        }
        else{
            /**use Location Manager object to get position requested update by declaring a new anonymous interface
             * locationListener
             */

            LocationManager lm = (LocationManager) getSystemService(LOCATION_SERVICE);
            lm.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0, new LocationListener() {
                @Override
                public void onLocationChanged(Location location) {
                    lat = location.getLatitude();
                    lng = location.getLongitude();
                    ((TextView)findViewById(R.id.latView)).setText("Lat"+lat);
                    ((TextView)findViewById(R.id.longView)).setText("Long"+lng);
                    permissionCheck = true;

                }

                @Override
                public void onStatusChanged(String provider, int status, Bundle extras) {

                }

                @Override
                public void onProviderEnabled(String provider) {

                }

                @Override
                public void onProviderDisabled(String provider) {

                }
            });
        }





    }
    @Override
    public void onResume() {
        super.onResume();
        mapView.onResume();
    }

    @Override
    protected void onStart() {
        super.onStart();
        mapView.onStart();
    }

    @Override
    protected void onStop() {
        super.onStop();
        mapView.onStop();
    }

    @Override
    public void onPause() {
        super.onPause();
        mapView.onPause();
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        mapView.onSaveInstanceState(outState);
    }

    @Override
    public void onLowMemory() {
        super.onLowMemory();
        mapView.onLowMemory();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (mapboxMap != null) {
            mapboxMap.removeOnMapClickListener(this);
        }
        //if (locationEngine != null) {
        //  locationEngine.removeLocationUpdates(callback);
        //}
        if (markerViewManager != null) {
            markerViewManager.onDestroy();
        }
        mapView.onDestroy();
    }



    public void makeConnection(View v) throws UnsupportedEncodingException {
        //mapView.onStop();
        ArrayList<String> results = new ArrayList<String>();

        displayText = (TextView) findViewById(R.id.errorDisplay);
        table = (TableLayout) findViewById(R.id.tableMain);
        //userInput = (EditText) findViewById(R.id.userInput);
        mapView.setVisibility(MapView.INVISIBLE);//set back to invisible on every search call



        String encodedInput;
        URL host;
        displayText.setText(null);
        try {
            ConnectivityManager connManager = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
            NetworkInfo networkInfo = connManager.getActiveNetworkInfo();
            if(networkInfo!=null && networkInfo.isConnected()) {//check if network connection is available wifi/data
                byLocation = findViewById(R.id.byLocation);
                table.setVisibility(View.VISIBLE);////


                if(byLocation.isChecked()){//find by location
                    foundResults.clear();
                    currentFeatures.clear();
                    table.removeAllViews();
                    if(markerViewManager!=null && markerView !=null)
                        markerViewManager.removeMarker(markerView);//delete

                    if(permissionCheck){
                        //displayText.setText(userInput.getText().toString());
                        host = new URL("http://10.0.2.2:8080/stations?lat="+lat+"&lng="+lng);
                        performSearch(host, table);//parse to method which will call thread
                        //table.startNestedScroll(122);
                    }
                    else{
                        currentFeatures.clear();
                        //foundResults.clear();
                        table.removeAllViews();
                        displayText.setText("Action cannot be performed geolocation permission was not given");
                    }
                }
            }
            else{
                currentFeatures.clear();
                //foundResults.clear();
                displayText.setText(null);
                displayText.setText("Internet Connection could not be made");
            }

        }
        catch (MalformedURLException e) {
            System.out.println("Error1");
            e.printStackTrace();
        } catch (IOException e) {
            System.out.println("Error2");
            e.printStackTrace();
        } catch (JSONException e) {
            System.out.println("Error3 JSON EXCEPTION");
            e.printStackTrace();
        }

    }

    /**
     * hodies map from user
     * @param v
     */

    public void HideMap(View v){
        mapView.setVisibility(View.INVISIBLE);

    }

    /**
     * display map connect to show on map button
     * this method declares an AsynTask Anonymous class and does all the background work for the map
     * @param v
     */

    public void DisplayMap(View v){
        MapView map =  findViewById(R.id.mapView);
        map.setVisibility(MapView.VISIBLE);

        Button search = (Button) findViewById(R.id.searchButton);

        if(table!=null)
            table.setVisibility(TableLayout.INVISIBLE);

        /**
         * AsynkTask takes in Void in background, creates features elements and populates and an ArrayList of Feature obj for the
         * onPostExecute method
         */
        new AsyncTask<Void, Void, ArrayList<Feature>>() {

            @Override
            protected ArrayList<Feature> doInBackground(Void... voids) {

                ArrayList<Feature> myFeatures = new ArrayList<Feature>();
                for(Station es : foundResults){//takes first item parse (first list)

                    Feature establishement = Feature.fromGeometry(Point.fromLngLat(es.LONGITUDE(),es.LATITUDE() ));//gives a location on the map to the object

                    //concatenate name, longitude and latitude as string with key NAME_RATING in StringProperty for use in icon clicked behaviour
                    establishement.addStringProperty("NAME_RATING", es.GETNAME()+":"+String.valueOf(es.LONGITUDE())+":"+String.valueOf(es.LATITUDE()));//concatenate our restaurant name and rating
                    myFeatures.add(establishement);

                }

                return myFeatures;
            }

            @Override
            protected void onPostExecute(final ArrayList<Feature> features) {
                super.onPostExecute(features);
                final String PROPERTY_KEY = "NAME_RATING";
                final String PROPERTY_SELECTED_KEY = "PROPERTY_SELECTED";
                final String ICON_ID = "ICON_ID";
                MainActivity activity = MainActivity.this;//get strong reference to activity
                if(activity == null || activity.isFinishing() || features.isEmpty()) {//check if main thread is not finishing or null
                    return;
                }
                /**
                 * get Asyncrounus from and implements anonymous onMapReadyCallback interface
                 */
                MainActivity.this.mapView.getMapAsync(new OnMapReadyCallback() {
                    //override onMapReady and setStyle
                    @Override
                    public void onMapReady(@NonNull final MapboxMap mapboxMap) {

                        mapboxMap.setStyle(new
                                        Style.Builder().fromUri("mapbox://styles/mapbox/cjf4m44iw0uza2spb3q0a7s41")
                                        .withImage(ICON_ID, BitmapFactory.decodeResource(MainActivity.this.getResources(), R.drawable.mapbox_marker_icon_default))
                                        // Adding a GeoJson source for the SymbolLayer icons.
                                        .withSource(new GeoJsonSource("SOURCE_ID",
                                                FeatureCollection.fromFeatures(features)))
                                        //adding the actual SymbolLayer to the map style.the match the expression will check
                                        //the icon property key and the use the partern for actual icon id.
                                        .withLayer(new SymbolLayer("LAYER_ID", "SOURCE_ID")
                                                .withProperties(PropertyFactory.iconImage(ICON_ID),
                                                        iconAllowOverlap(true),
                                                        iconOffset(new Float[]{0f, -9f}))
                                        ), new Style.OnStyleLoaded() {
                                    @Override
                                    public void onStyleLoaded(@NonNull Style style) {

                                        //my map is now set up and style can be chosen
                                        MainActivity.this.mapboxMap = mapboxMap;
                                        mapboxMap.addOnMapClickListener(MainActivity.this);
                                        Toast.makeText(MainActivity.this, "MAP WAS SET UP AND LOADED", Toast.LENGTH_LONG).show();
                                    }
                                }
                        );
                    }

                });
                currentFeatures.addAll(features);//update current features...
            }
        }.execute();

    }

    /**
     * method between makeConnection method and ApiRequestAsynkTask object
     * declare an
     * @param host
     * @param table
     * @throws IOException
     * @throws JSONException
     */

    public void performSearch(URL host, TableLayout table) throws IOException, JSONException {
        ApiRequestAsynkTask task = new ApiRequestAsynkTask(MainActivity.this);//parse current context and create object
        task.execute(host);//parse url
    }


    @Override
    public void onExplanationNeeded(List<String> permissionsToExplain) {

    }

    @Override
    public void onPermissionResult(boolean granted) {

    }

    /**
     * calls the behaviour of icon click through handleClickIcon
     * @param point
     * @return boolean
     */

    @Override
    public boolean onMapClick(@NonNull LatLng point) {
        return handleClickIcon(mapboxMap.getProjection().toScreenLocation(point));
    }

    /**
     * Helper method for onMapClick, decodes the content stored in StringProperty of Feature object
     * checks wich icon is currently being clicked and create a markerView(infoBox) for in on the map
     * @param screenPoint
     * @return
     */
    private boolean handleClickIcon(PointF screenPoint){//on map click behaviour
        List<Feature> features = mapboxMap.queryRenderedFeatures(screenPoint, "LAYER_ID");//all created features
        if(markerViewManager!=null && markerView !=null)
            markerViewManager.removeMarker(markerView);//delete all markerView objects because we don't if this 1st or Nth call
        //mapboxMap.
        final String PROPERTY_KEY = "NAME_RATING";
        if(!features.isEmpty()){
            String name = features.get(0).getStringProperty(PROPERTY_KEY); //currently selected item
            List<Feature> featureList = currentFeatures;//get all features...

            if(featureList != null){ //if list of features is not empty
                //check if the name currently rendered and the one in the list are the same and
                for(int i = 0; i < featureList.size(); i++){
                    if(featureList.get(i).getStringProperty(PROPERTY_KEY).equals(name)){//content loaded in property string is same(name+lat+long)
                        markerViewManager = new MarkerViewManager(mapView, mapboxMap);

                        //find and inflate custom layout in marker_view_bubble.xml
                        View customView = LayoutInflater.from(MainActivity.this).inflate(
                                R.layout.marker_view_bubble, null);
                        customView.setLayoutParams(new FrameLayout.LayoutParams(WRAP_CONTENT, WRAP_CONTENT));

                        //set some colors
                        customView.setBackgroundColor(Color.WHITE);
                        customView.setOutlineSpotShadowColor(Color.BLACK);
                        customView.setOutlineAmbientShadowColor(Color.DKGRAY);

                        String test = features.get(0).getStringProperty(PROPERTY_KEY);//get string (name+long+lat)
                        String[] x = new String[6];
                        x = test.split(":");//split all my inforamtion
                        String nameOF = x[0];//name
                        //String ratingOf = x[1];
                        double longi = Double.parseDouble(x[1]);//longitude
                        double lati = Double.parseDouble(x[2]);//latitude
                        TextView titleTextView = customView.findViewById(R.id.marker_window_title);//get View through reference of parent
                        titleTextView.setText("STATION : "+nameOF+" \n");
                        titleTextView.setTextColor(Color.BLACK);

                        TextView subTitle = customView.findViewById(R.id.marker_window_snippet);//get view through reference of it's parent

                        subTitle.setText(" \n");

                        Toast.makeText(MainActivity.this, x[1]+":"+x[2], Toast.LENGTH_LONG).show();//toast message to check it works


                        markerView = new MarkerView((new LatLng(lati, longi)), customView);
                        markerViewManager.addMarker(markerView);//add costum layout as marker marView

                    }
                }

            }
            return true;
        }
        else{
            Toast.makeText(MainActivity.this, "No results were found", Toast.LENGTH_LONG).show();
            return false;
        }
    }

    @Override
    public void onMapReady(@NonNull MapboxMap mapboxMap) {
    }

    @Override
    public void onStyleLoaded(@NonNull Style style) {
    }

    @Override
    public void onLocationChanged(Location location) {

        CameraUpdate cameraUpdate = new CameraUpdate() {
            @Nullable
            @Override
            public CameraPosition getCameraPosition(@NonNull MapboxMap mapboxMap) {
                CameraPosition position = new CameraPosition.Builder()
                        .target(new LatLng(lat, lng)) // Sets the new camera position
                        .zoom(10) // Sets the zoom
                        .bearing(180) // Rotate the camera
                        .tilt(30) // Set the camera tilt
                        .build(); // Creates a CameraPosition from the builder
                return position;
            }
        };

        mapboxMap.easeCamera(cameraUpdate);
    }

    @Override
    public void onStatusChanged(String provider, int status, Bundle extras) {

    }

    @Override
    public void onProviderEnabled(String provider) {

    }

    @Override
    public void onProviderDisabled(String provider) {

    }
}

/**
 * Station object class takes in string name , double latitude, double longitude
 */
class Station {
    String Name;
    double Latitude;
    double Longitude;
    Station(String Name, double Latitude, double Longitude){
        this.Name = Name;
        this.Latitude = Latitude;
        this.Longitude = Longitude;
    }

    public String GETNAME(){ return this.Name; }
    public double LATITUDE(){ return this.Latitude; }
    public double LONGITUDE(){ return this.Longitude; }
}


/**
 * ApiRequestAsynTask takes in a URL, and constructed by parsing a reference to the main thread
 */

class ApiRequestAsynkTask extends AsyncTask<URL, Void , BufferedReader> {

    // WeakReference collection of type mainActivity
    private WeakReference<MainActivity> activityWeakReference;
    ApiRequestAsynkTask(MainActivity activity){
        activityWeakReference = new WeakReference<MainActivity>(activity);
    }
    @Override
    protected void onPreExecute() {
        super.onPreExecute();

        MainActivity activity = activityWeakReference.get();//get strong reference to activity, now we have access to all our variables
        //in the main thread including all View objects

        //if the below conditions are met we need to leave this thread because our activity is either inexistant or finishing
        if(activity == null || activity.isFinishing()) {
            return;
        }

        /**
         * Clear all previously found results (Stations) as well as all added symbols
         * because this could be the first call of this method or the Nth call
         */

        activity.foundResults.clear();//clear last results
        for(Symbol s : activity.currentSymbols){
            //symbolManager.deleteAll();
            activity.symbolManager.delete(activity.symbol);/////////////////>>>>>>>>>>
        }
        activity.currentSymbols.clear();
    }

    /**
     * Override doInBackground for the heavy networking, this is moving our work from the main thread to
     * a Background Thread
     * @param urls
     * @return Buffereader to be used later and decode the results to update the UI in OnPostExecute
     */

    @Override
    protected BufferedReader doInBackground(URL... urls) {
        URLConnection tc = null;
        BufferedReader in = null;
        try {
            tc = urls[0].openConnection();
            InputStreamReader isReader = new InputStreamReader(tc.getInputStream());
            in = new BufferedReader(isReader);
        } catch (IOException e) {
            e.printStackTrace();
        }

        return in;
    }

    /**
     * Override onPostExecute to allow us to re-access the main UI thread
     *
     * @param bufferedReader (Takes in a BufferedReader) to update the UI with new Information
     */

    @Override
    protected void onPostExecute(BufferedReader bufferedReader) {
        super.onPostExecute(bufferedReader);

        MainActivity activity = activityWeakReference.get();//get strong reference to activity
        if(activity == null || activity.isFinishing()) {
            return;
        }

        try{
            String line;
            int c = 0;
            while((line = bufferedReader.readLine())!=null){
                JSONArray jArray = new JSONArray(line);
                System.out.println(">>>>>>>  "+ c++);

                for(int i = 0; i < jArray.length(); i++){
                    JSONObject jObject = (JSONObject) jArray.get(i);
                    /**add new table row and set layout parameters programatically
                     * and set some layout params
                     */
                    TableRow row = new TableRow(activity);
                    row.setLayoutParams((new TableRow.LayoutParams(TableRow.LayoutParams.MATCH_PARENT, TableRow.LayoutParams.WRAP_CONTENT)));
                    TextView tvBName = new TextView(activity);
                    tvBName.setText("Station Name :    "+jObject.getString("StationName")+"\n");
                    tvBName.setTextColor(Color.WHITE);
                    tvBName.setLayoutParams(new TableRow.LayoutParams(TableRow.LayoutParams.MATCH_PARENT, TableRow.LayoutParams.WRAP_CONTENT));
                    tvBName.setPadding(0,0,150, 0);


                    activity.table.addView(row);//row as as view to table
                    row.addView(tvBName);//add textView as view in row

                    String Latitude = jObject.getString("Latitude");
                    String Longitude = jObject.getString("Longitude");
                    String name = jObject.getString("StationName");
                    TextView test = activity.findViewById(R.id.byLocation);


                    double lat = Double.parseDouble(Latitude);
                    double longt = Double.parseDouble(Longitude);
                    Station station = new Station(name,lat,longt);
                    activity.foundResults.add(station);

                }
            }
            bufferedReader.close();//close connection


        }
        catch (MalformedURLException e) {
            System.out.println("Error1");
            e.printStackTrace();
        } catch (IOException e) {
            System.out.println("Error2");
            e.printStackTrace();
        } catch (JSONException e) {
            System.out.println("Error3 JSON EXCEPTION");
            e.printStackTrace();
        }

    }
}