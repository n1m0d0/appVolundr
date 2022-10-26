package com.ajatic.volunder;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.app.ProgressDialog;
import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ListView;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class Events extends AppCompatActivity {
    ListView lvEvents;
    Button btnForm;

    Util util;
    Integer user_id;
    String email, token;

    ArrayList<EventObject> items = new ArrayList<EventObject>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_events);

        lvEvents = findViewById(R.id.lvEvents);
        btnForm = findViewById(R.id.btnForm);

        util = new Util(Events.this);
        Cursor cursor = util.getSession();
        user_id = cursor.getInt(1);
        email = cursor.getString(2);
        token = cursor.getString(3);

        events("event");

        lvEvents.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                EventObject form = (EventObject) adapterView.getItemAtPosition(i);
                Integer event_id = form.getId();

                Intent intent = new Intent(Events.this, Event.class);
                intent.putExtra("event_id", event_id);
                startActivity(intent);
                finish();
            }
        });

        btnForm.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(Events.this, Forms.class);
                startActivity(intent);
                finish();
            }
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main, menu);
        MenuItem User = menu.findItem(R.id.userEmail);
        User.setTitle(email);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        int id = item.getItemId();
        switch (id) {
            case R.id.logout:

                return true;

            default:
                return super.onOptionsItemSelected(item);
        }
    }

    private void events(String direction) {

        String url = getString(R.string.url) + "/" + direction;

        ProgressDialog progressDialog;
        progressDialog = new ProgressDialog(this);
        //progressDialog.setMax(100);
        progressDialog.setMessage("Cargando...");
        progressDialog.setTitle("Revisando la Informacion");
        progressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
        progressDialog.show();
        progressDialog.setCancelable(false);

        RequestQueue requestQueue;
        requestQueue = Volley.newRequestQueue(this);
        JsonObjectRequest jsonObjectRequest;

        jsonObjectRequest = new JsonObjectRequest(Request.Method.GET, url, null, new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject response) {
                progressDialog.dismiss();
                Log.w("response", "" + response);
                try {
                    if (response.getInt("code") == 200) {
                        JSONArray forms = response.getJSONArray("data");
                        for (int i = 0; i < forms.length(); i++) {
                            JSONObject form = forms.getJSONObject(i);
                            Integer id = form.getInt("id");
                            Integer child_id = null;
                            String color = "#F59CA9";
                            String registered = form.getString("registered");
                            String formName = form.getString("form_name");
                            String userName = form.getString("user_name");

                            items.add(new EventObject(id, child_id, color, formName, userName, registered));
                        }

                        EventsAdapter eventsAdapter = new EventsAdapter(Events.this, items);
                        lvEvents.setAdapter(eventsAdapter);
                    } else {
                        Toast.makeText(Events.this, "", Toast.LENGTH_LONG).show();
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Log.w("error", "" + error);
                progressDialog.dismiss();
            }
        }) {
            @Override
            public Map<String, String> getHeaders() {
                Map<String, String> params = new HashMap<>();
                params.put("Content-Type", "application/json");
                params.put("Authorization", "Bearer " + token);
                return params;
            }
        };

        requestQueue.add(jsonObjectRequest);
    }
}