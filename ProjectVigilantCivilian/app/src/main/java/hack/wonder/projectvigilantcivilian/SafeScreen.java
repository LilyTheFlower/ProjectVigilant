package hack.wonder.projectvigilantcivilian;

import android.graphics.Color;
import android.provider.Settings;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
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

public class SafeScreen extends AppCompatActivity {
    TextView MainMessage;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_safe_screen);
        getWindow().getDecorView().setBackgroundColor(Color.GREEN);
        MainMessage = findViewById(R.id.mainMessage);

        RequestQueue queue = Volley.newRequestQueue(this);
        String fullUrl = getString(R.string.url) + "deactivateUser";

        String id = null;
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.CUPCAKE) {
            id = Settings.Secure.getString(getContentResolver(), Settings.Secure.ANDROID_ID);
        }
        SafeBody sb = new SafeBody(id);
        try{
            JsonObjectRequest jobj = new JsonObjectRequest(Request.Method.POST, fullUrl, new JSONObject(objtoJSONString(sb)), new Response.Listener<JSONObject>() {
                @Override
                public void onResponse(JSONObject response) {
                    //nothing to do for now
                }
            }, new Response.ErrorListener() {
                @Override
                public void onErrorResponse(VolleyError error) {
                    Toast.makeText(getApplicationContext(), error.toString(), Toast.LENGTH_LONG).show();
                }
            });
            queue.add(jobj);
        }catch(Exception e){

        }
        MainMessage.setText("Phew! Follow the directions of the local authorities and stay safe");
    }

    class SafeBody{
        public String phoneId;
        SafeBody(String Id) {
            phoneId = Id;
        }
    }

    public static String objtoJSONString(Object obj) throws IOException {
        ObjectMapper objMapper = new ObjectMapper();
        return objMapper.writeValueAsString(obj);
    }
}
