package hack.wonder.projectvigilantcivilian;

import android.Manifest;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Build;
import android.os.Looper;
import android.provider.Settings;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.DialogFragment;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import org.codehaus.jackson.map.ObjectMapper;

import org.json.JSONObject;
import java.io.IOException;

import java.util.List;
import java.util.Locale;
import java.util.Timer;
import java.util.TimerTask;

public class InDanger extends AppCompatActivity {

    Button SafeButton;
    AlertDialog dialog;
    static Timer timer;
    TextView Message;
    LocationManager locationManager;

    double xcoord= 0;
    double ycoord= 0;
    double zcoord= 0;
    String Id;
    String nearbyCity;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_in_danger);
        getWindow().getDecorView().setBackgroundColor(Color.RED);
        SafeButton = findViewById(R.id.SafeButton);
        Message = findViewById(R.id.DangerMessage);
        Message.setText("There is an Emergency! Follow instructions given out by the local authorities");
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.CUPCAKE) {
            Id = Settings.Secure.getString(getContentResolver(), Settings.Secure.ANDROID_ID);
        }
        SafeButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View arg0) {
                DialogFragment sdf = new SafeDialogFragment();
                sdf.show(getSupportFragmentManager(), "Safe?");
            }
        });
        locationManager = (LocationManager)getSystemService(Context.LOCATION_SERVICE);
        timer = new Timer();
        TimerTask tt = new TT();
        timer.scheduleAtFixedRate(tt, 0, 5000);
    }

    class TT extends TimerTask{
        boolean b;

        public void run(){

            if(!b){
                Looper.prepare();
                b = true;
            }
            uLocation();
        }
    }

    void uLocation(){
        if (ContextCompat.checkSelfPermission(getApplicationContext(), Manifest.permission.ACCESS_FINE_LOCATION)
                == PackageManager.PERMISSION_GRANTED &&
                ContextCompat.checkSelfPermission(getApplicationContext(), Manifest.permission.ACCESS_COARSE_LOCATION)
                == PackageManager.PERMISSION_GRANTED) {
            LocationListener locationListener = new MyLocationListener();
            try{
                ycoord = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER).getLatitude();
                xcoord = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER).getLongitude();
                zcoord = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER).getAltitude();
                locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 5000, 10, locationListener);
                RequestQueue queue = Volley.newRequestQueue(getApplicationContext());
                 String fullUrl = getString(R.string.url) + "updateLocation";
                LocationFormat LF = new LocationFormat(Id, nearbyCity, xcoord, ycoord, zcoord);
                try{
                    JsonObjectRequest jobj = new JsonObjectRequest(Request.Method.POST, fullUrl, new JSONObject(objtoJSONString(LF)), new Response.Listener<JSONObject>() {
                        @Override
                        public void onResponse(JSONObject response) {
                            //the server got our response
                        }
                    }, new Response.ErrorListener(){
                        @Override
                        public void onErrorResponse(VolleyError error) {
                            Toast.makeText(getApplicationContext(), error.toString(), Toast.LENGTH_LONG).show();
                        }
                    });
                    queue.add(jobj);
                }catch(Exception e){

                }
            }catch(SecurityException e){
                Toast.makeText(getApplicationContext(), "Security Issue", Toast.LENGTH_LONG).show();
            }
        }else{
            ActivityCompat.requestPermissions(InDanger.this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, 53254);
            ActivityCompat.requestPermissions(InDanger.this, new String[]{Manifest.permission.ACCESS_COARSE_LOCATION}, 53255);
        }
    }

    private class MyLocationListener implements LocationListener {

        @Override
        public void onLocationChanged(Location loc) {
            Toast.makeText(
                    getBaseContext(),
                    "Location changed: Lat: " + loc.getLatitude() + " Lng: "
                            + loc.getLongitude(), Toast.LENGTH_SHORT).show();
            xcoord = loc.getLongitude();
            ycoord = loc.getLatitude();

           //try to get city name from coords, not always functional
            String cityName = null;
            Geocoder gcd = new Geocoder(getBaseContext(), Locale.getDefault());
            List<Address> addresses;
            try {
                addresses = gcd.getFromLocation(loc.getLatitude(),
                        loc.getLongitude(), 1);
                if (addresses.size() > 0) {
                    cityName = addresses.get(0).getLocality();
                }
            }
            catch (IOException e) {
            }
            nearbyCity = cityName;
        }
    }

    public static class SafeDialogFragment extends DialogFragment{
        public Dialog onCreateDialog(Bundle savedInstanceState) {
            // Use the Builder class for convenient dialog construction
            AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
            builder.setMessage("By clicking okay you are signaling that you are safe and not in need of search and rescue").setTitle("Are you sure?")
                    .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int id) {
                            timer.cancel();
                            // User is Safe, start safe screen
                            Intent myIntent = new Intent(getContext(), SafeScreen.class);
                            startActivity(myIntent);
                        }
                    })
                    .setNegativeButton("I'm not safe", new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int id) {
                            // User is not yet Safe, do nothing
                        }
                    });
            // Create the AlertDialog object and return it
            return builder.create();
        }
    }

    public static String objtoJSONString(Object obj) throws IOException {
        ObjectMapper objMapper = new ObjectMapper();
        return objMapper.writeValueAsString(obj);
    }

    class  LocationFormat{
        public String phoneId, city;
        public double x, y, z;
        LocationFormat(String Id, String City, double X, double Y, double Z) {
            phoneId = Id;
            city = City;
            x = X;
            y = Y;
            z = Z;
        }
    }
}
