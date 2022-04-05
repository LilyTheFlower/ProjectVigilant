package hack.wonder.projectvigilantcivilian;

import android.Manifest;
import android.app.AlertDialog;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Build;
import android.provider.Settings;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.NotificationManagerCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;
import android.os.Handler;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import org.codehaus.jackson.map.ObjectMapper;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.PrintStream;
import java.io.PrintWriter;
import java.util.Timer;
import java.util.TimerTask;

import static android.view.View.GONE;

public class MainActivity extends AppCompatActivity {
    TextView MainMessage;
    Timer timer;
    TimerTask timerTask;

    Button refreshButton;
    Button NameButton;
    String id;

    final Handler handler = new Handler();
    RequestQueue queue;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        MainMessage = findViewById(R.id.MainMessage);
        MainMessage.setText("There is currently no detected Emergency");
        refreshButton = findViewById(R.id.RefreshButton);
        NameButton = findViewById(R.id.UsernameButton);
        refreshButton.setVisibility(View.INVISIBLE);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.CUPCAKE) {
            id = Settings.Secure.getString(getContentResolver(), Settings.Secure.ANDROID_ID);
        }
        //we are going to use a handler to be able to run in our TimerTask

       timer = new Timer();
        initializeTimerTask();

        timer.schedule(timerTask, 5000, 5000);
        queue = Volley.newRequestQueue(this);
        sendUserInfo();
        NameButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View arg0) {
                prompt();
            }
        });
        NameButton.setVisibility(View.VISIBLE);
        checkForEmergency();
    }

    public void Notification(){
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            CharSequence name = getString(R.string.channel_name);
            String description = getString(R.string.channel_description);
            int importance = NotificationManager.IMPORTANCE_DEFAULT;
            NotificationChannel channel = new NotificationChannel("EmergencyNotif", name, importance);
            channel.setDescription(description);
            // Register the channel with the system; you can't change the importance
            // or other notification behaviors after this
            NotificationManager notificationManager = getSystemService(NotificationManager.class);
            notificationManager.createNotificationChannel(channel);
            NotificationCompat.Builder builder = new NotificationCompat.Builder(this, "EmergencyNotif")
                    .setSmallIcon(R.drawable.ic_launcher_background)
                    .setContentTitle("Emergency Notification!")
                    .setContentText("Your city has declared an emergency and would like to begin tracking your location for search and rescue efforts")
                    .setAutoCancel(false)
                    .setPriority(NotificationCompat.PRIORITY_DEFAULT);
            NotificationManagerCompat managerCompat = NotificationManagerCompat.from(MainActivity.this);
            notificationManager.notify(1, builder.build());
        }
    }

    public void initializeTimerTask() {

        timerTask = new TimerTask() {
            public void run() {

                handler.post(new Runnable() {
                    public void run() {
                       checkForEmergency();
                    }
                });
            }
        };
    }

    private void prompt(){
        AlertDialog.Builder alertName = new AlertDialog.Builder(MainActivity.this);
        AlertDialog.Builder alertCountry = new AlertDialog.Builder(MainActivity.this);
        AlertDialog.Builder alertState = new AlertDialog.Builder(MainActivity.this);
        AlertDialog.Builder alertCity = new AlertDialog.Builder(MainActivity.this);

        alertName.setTitle("Enter Full Name");
        alertName.setMessage("Enter full name or hit cancel. Hit Ok to confirm");

        alertCountry.setTitle("Enter Country");
        alertCountry.setMessage("Enter full Country name or hit cancel. Hit Ok to confirm");

        alertState.setTitle("Enter State");
        alertState.setMessage("Enter full State name or hit cancel. Hit Ok to confirm");

        alertCity.setTitle("Enter City");
        alertCity.setMessage("Enter full City name or hit cancel. Hit Ok to confirm");

        // Set an EditText view to get user input
        final EditText inputName = new EditText(MainActivity.this);
        alertName.setView(inputName);

        // Set an EditText view to get user input
        final EditText inputCountry = new EditText(MainActivity.this);
        alertCountry.setView(inputCountry);

        // Set an EditText view to get user input
        final EditText inputState = new EditText(MainActivity.this);
        alertState.setView(inputState);

        // Set an EditText view to get user input
        final EditText inputCity = new EditText(MainActivity.this);
        alertCity.setView(inputCity);

        alertName.setPositiveButton("Ok", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int whichButton) {
                char[] value = new char[inputName.length()];
                inputName.getText().getChars(0, inputName.length(), value, 0);
                String n = new String(value);
                writeData(n, new File(getApplicationContext().getCacheDir().getAbsolutePath() + "/Name"));
                alertCountry.show();
            }
        });
        alertName.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int whichButton) {
                // Canceled.
            }
        });

        alertCountry.setPositiveButton("Ok", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int whichButton) {
                char[] value = new char[inputCountry.length()];
                inputCountry.getText().getChars(0, inputCountry.length(), value, 0);
                String c = new String(value);
                writeData(c, new File(getApplicationContext().getCacheDir().getAbsolutePath() + "/Country"));
                alertState.show();
            }
        });

        alertCountry.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int whichButton) {
                // Canceled.
            }
        });

        alertState.setPositiveButton("Ok", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int whichButton) {
                char[] value = new char[inputState.length()];
                inputState.getText().getChars(0, inputState.length(), value, 0);
                String s = new String(value);
                writeData(s, new File(getApplicationContext().getCacheDir().getAbsolutePath() + "/State"));
                alertCity.show();
            }
        });

        alertState.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int whichButton) {
                // Canceled.
            }
        });

        alertCity.setPositiveButton("Ok", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int whichButton) {
                char[] value = new char[inputCity.length()];
                inputCity.getText().getChars(0, inputCity.length(), value, 0);
                String ci = new String(value);
                writeData(ci, new File(getApplicationContext().getCacheDir().getAbsolutePath() + "/City"));
                MainMessage.setText("There is currently no detected Emergency");
                sendUserInfo();
            }
        });

        alertCity.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int whichButton) {
                // Canceled.
            }
        });

        alertName.show();

    }

    public String readData(File dataFile, String errorMSG){
        try{
            FileReader fr = new FileReader(dataFile);
            String s = "";
            //use a while loop to read out the Username character by character until the file
            // reader is not "Ready", ie there is no character to read, and return the username
            while(fr.ready()){
                s = s + (char)fr.read();
            }
            return s;
        }
        catch(FileNotFoundException f){
            MainMessage.setText("Please use the Registration Button to add your info to the Database");
            return "ff";
        }catch(IOException e){
            e.printStackTrace();
        }
        return "Should not make it here, end of readData";
    }

    public void writeData(String s, File dataFile){
        try{
            PrintWriter pw = new PrintWriter(new FileOutputStream(dataFile));
            pw.print(s);
            pw.close();
        }
        catch(FileNotFoundException f){

        }catch(IOException e) {
            e.printStackTrace();
        }
    }

    void sendUserInfo(){
        String fullUrl = getString(R.string.url) + "setUserInformation";
        String fn = readData(new File(getApplicationContext().getCacheDir().getAbsolutePath()+"/Name"), "NoNameFound");


        InfoBody nb = new InfoBody(id, fn);
        try{
            JsonObjectRequest jobj = new JsonObjectRequest(Request.Method.POST, fullUrl, new JSONObject(objtoJSONString(nb)), new Response.Listener<JSONObject>() {
                @Override
                public void onResponse(JSONObject response) {
                }
            }, new Response.ErrorListener() {
                @Override
                public void onErrorResponse(VolleyError error) {
                    //Toast.makeText(getApplicationContext(), error.toString(), Toast.LENGTH_LONG).show();
                }
            });
            queue.add(jobj);
        }catch(Exception e){

        }
    }

    void checkForEmergency(){
        // Instantiate the RequestQueue.
        RequestQueue queue = Volley.newRequestQueue(this);
        String fullUrl = getString(R.string.url) + "shouldIActivate";
        ActivateAllInCityRequestBody AACRB = new ActivateAllInCityRequestBody(
                readData(new File(getApplicationContext().getCacheDir().getAbsolutePath()+"/Country"), "NoCountry"),
                readData(new File(getApplicationContext().getCacheDir().getAbsolutePath()+"/State"), "NoStateFound"),
                readData(new File(getApplicationContext().getCacheDir().getAbsolutePath()+"/City"), "NoCityFound"));
        try{
            JsonObjectRequest jobj = new JsonObjectRequest(Request.Method.POST, fullUrl, new JSONObject(objtoJSONString(AACRB)), new Response.Listener<JSONObject>() {
                @Override
                public void onResponse(JSONObject response){
                    refreshButton.setVisibility(View.INVISIBLE);
                    //if there is no emergency, the user will remain in this activity, and the no emergency text can remain
                    MainMessage.setText("There is currently no detected Emergency");
                    try{
                        if(response.getString("code").equals("1")){
                            //there is an emergency, begin the switch to the IndDanger activity
                            //Toast.makeText(getApplicationContext(), response.getString("code"), Toast.LENGTH_LONG).show();
                            //above is a debugging toast to check the code passed back by the database

                            //deactivate the timer to check for emergencies
                            if (timer != null) {
                                timer.cancel();
                                timer = null;
                            }

                            Notification();

                            Intent myIntent = new Intent(MainActivity.this, InDanger.class);
                            startActivity(myIntent);
                            //switch activities
                        }

                    }catch(JSONException j){
                        j.printStackTrace();
                    }

                }
            }, new Response.ErrorListener() {

                @Override
                public void onErrorResponse(VolleyError error) {
                    // TODO: Handle error
                    refreshButton.setVisibility(View.VISIBLE);
                   MainMessage.setText("There was an error communicating with the server");
                    refreshButton.setOnClickListener(new View.OnClickListener() {
                        public void onClick(View arg0) {
                            checkForEmergency();
                        }
                    });
                    //Toast.makeText(getApplicationContext(), error.toString(), Toast.LENGTH_LONG).show();
                }
            });
            queue.add(jobj);
        }catch(Exception e){

        }
    }

    public static String objtoJSONString(Object obj) throws IOException {
        ObjectMapper objMapper = new ObjectMapper();
        return objMapper.writeValueAsString(obj);
    }

    class  ActivateAllInCityRequestBody{
        public String id, country, state, city;
         ActivateAllInCityRequestBody(String co, String st, String ci) {
            country = co;
            state = st;
            city = ci;
        }
    }

    class InfoBody{
        public String phoneId, fullName;
        InfoBody(String pId, String fn) {
            phoneId =  pId;
            fullName = fn;
        }
    }

}
