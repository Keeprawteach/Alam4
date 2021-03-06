package com.alam4.groups;

import android.content.DialogInterface;
import android.support.annotation.NonNull;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.CardView;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.CheckedTextView;
import android.widget.Spinner;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.view.LayoutInflater;

import android.view.ViewGroup;
import android.widget.ImageView;

import android.widget.TextView;
import android.widget.Toast;

import com.alam4.R;
import com.alam4.add.addmembers;
import com.alam4.background.background;
import com.alam4.connection.connection;
import com.alam4.edit.editgroup;
import com.alam4.frontier.frontier;

import com.alam4.model.membermodel;

import com.alam4.pref.userpreferences;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.ProtocolException;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import com.squareup.picasso.Picasso;



public class viewgroup extends AppCompatActivity {
    ActionBar actionBar;
    String grp,name,level;
    ArrayList<String> groupmembers;
    String[] groups = {"Select", "Level 1", "Level 2", "Level 3"};
    Spinner spinner;
    RecyclerView recyclerView;
    Context context;
    AdapterClass adapterClass;
    private userpreferences prefer;
    String user;
    FloatingActionButton floatingActionButton;
    TextView textView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_viewgroup);
        grp = getIntent().getStringExtra("no");
        name = getIntent().getStringExtra("name");
        level = getIntent().getStringExtra("level");
        actionBar = getSupportActionBar();
        actionBar.setTitle(""+name);

        context = viewgroup.this;
        groupmembers = new ArrayList<>();
        actionBar.setDisplayHomeAsUpEnabled(true);

        prefer = new userpreferences(this);

        recyclerView = findViewById(R.id.members);
        RecyclerView.LayoutManager mLayoutManager = new LinearLayoutManager(getApplicationContext());
        recyclerView.setLayoutManager(mLayoutManager);
        recyclerView.setItemAnimator(new DefaultItemAnimator());
        user = prefer.getUser();
        floatingActionButton = findViewById(R.id.fab);
        floatingActionButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                addUpdate();
            }
        });
        download();


    }

    private void addUpdate() {
        AlertDialog.Builder builder = new AlertDialog.Builder(viewgroup.this);
        builder.setMessage("Are you sure you want to exit "+name+" response group?");
        builder.setPositiveButton("Yes", new DialogInterface.OnClickListener() {

            @Override
            public void onClick(DialogInterface dialog, int which) {
                wowGood(  );
            }
        });
        builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {

                dialog.dismiss();
            }
        });
        builder.show();
    }

    private void wowGood() {
        class Proceed extends AsyncTask<String, Void, String> {
            ProgressDialog loading;
            background rh = new background();

            @Override
            protected void onPreExecute() {
                super.onPreExecute();
                loading = ProgressDialog.show(viewgroup.this, "Exiting Group", "Please wait...", true, true);
            }

            @Override
            protected String doInBackground(String... bitmaps) {

                HashMap<String, String> data = new HashMap<>();
                data.put("grp", bitmaps[0]);
                data.put("user", bitmaps[1]);


                String result = rh.postRequest(connection.exitgroup(), data);
                return result;

            }

            @Override
            protected void onPostExecute(String s) {
                super.onPostExecute(s);
                loading.dismiss();
                Toast.makeText(viewgroup.this, "" + s, Toast.LENGTH_SHORT).show();
                if (s != null && s.equalsIgnoreCase("Success")) {

                    viewgroup.this.finish();
                }
            }
        }

        Proceed ui = new Proceed();
        ui.execute(grp,prefer.getUser());

    }

    private void download() {
        Background background = new Background(viewgroup.this, recyclerView);
        background.execute();

    }


    private class Background extends AsyncTask<String, String, String> {
        Context context;
        RecyclerView listView;
        HttpURLConnection httpURLConnection;
        BufferedReader bufferedReader;
        StringBuilder stringBuilder;

        public Background(Context context, RecyclerView listView) {
            this.context = context;
            this.listView = listView;
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();

        }

        @Override
        protected String doInBackground(String... params) {
            return DownloadData();
        }

        private String DownloadData() {
            stringBuilder = new StringBuilder();
            String line;
            httpURLConnection = new GetConnected().GetConnected();
            try {
                httpURLConnection.setRequestMethod("GET");
                httpURLConnection.setDoInput(true);
                httpURLConnection.setDoOutput(true);
                httpURLConnection.setConnectTimeout(120000);
                httpURLConnection.connect();

                bufferedReader = new BufferedReader(new InputStreamReader(new BufferedInputStream(httpURLConnection.getInputStream())));
                while ((line = bufferedReader.readLine()) != null) {
                    stringBuilder.append(line).append("\n");
                }
                bufferedReader.close();
                httpURLConnection.disconnect();
            } catch (ProtocolException e) {
                return null;
            } catch (IOException e) {
                return null;
            }

            return stringBuilder.toString();
        }

        protected class GetConnected {
            URL url;
            HttpURLConnection httpU;
            String sign_url = new connection(context).members();

            public HttpURLConnection GetConnected() {
                if (sign_url != null) {
                    try {
                        url = new URL(sign_url);
                        httpU = (HttpURLConnection) url.openConnection();
                    } catch (MalformedURLException e) {
                        return null;
                    } catch (IOException e) {
                        return null;
                    }
                    return httpU;
                }
                return null;
            }
        }

        @Override
        protected void onPostExecute(String s) {
            super.onPostExecute(s);
            if (s != null) {
                ParserData parserData = new ParserData(context, listView, s);
                parserData.execute(s);

            } else {
                Toast.makeText(context, "Error Encountered\nCan't reach the Server", Toast.LENGTH_SHORT).show();
            }
        }

        private class ParserData extends AsyncTask<String, Void, Boolean> {
            Context context;
            RecyclerView recyclerView;
            String jsondata;
            ArrayList<membermodel> arrayList = new ArrayList<>();

            public ParserData(Context context, RecyclerView recyclerView, String jsondata) {
                this.context = context;
                this.recyclerView = recyclerView;
                this.jsondata = jsondata;
            }

            @Override
            protected void onPreExecute() {
                super.onPreExecute();
            }

            @Override
            protected Boolean doInBackground(String... params) {
                jsondata = params[0];
                return ParseJsonDt();
            }

            @Override
            protected void onPostExecute(Boolean bool) {
                super.onPostExecute(bool);

                if (bool) {
                    adapterClass = new AdapterClass(arrayList, context, recyclerView);
                    recyclerView.setAdapter(adapterClass);
                } else {
                    Toast.makeText(context, "Encountered Error During processing", Toast.LENGTH_LONG).show();
                }
            }


            private Boolean ParseJsonDt() {
                boolean vall = false;
                try {
                    JSONArray jsonArray = new JSONArray(jsondata);
                    JSONObject jsonObject = null;
                    int i = 0;
                    membermodel sp;
                    while (i < jsonArray.length()) {

                        jsonObject = jsonArray.getJSONObject(i);
                        sp = new membermodel();
                        sp.setId(jsonObject.getString("member"));
                        sp.setImage(jsonObject.getString("image"));
                        sp.setName(jsonObject.getString("jina"));
                        String id = jsonObject.getString("groupid");
                        if (grp.equalsIgnoreCase(id)) {
                            arrayList.add(sp);
                        }

                        i++;

                    }

                    vall = true;

                } catch (JSONException e) {

                }

                return vall;
            }
        }
    }

    private class AdapterClass extends RecyclerView.Adapter<MyHoder> {
        List<membermodel> servicemodels;
        Context context;
        RecyclerView recyclerView;

        public AdapterClass(List<membermodel> servicemodels, Context context, RecyclerView recyclerView) {
            this.servicemodels = servicemodels;
            this.context = context;
            this.recyclerView = recyclerView;
        }

        @NonNull
        @Override
        public MyHoder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
            return new MyHoder(LayoutInflater.from(this.context).inflate(R.layout.viewmembers, viewGroup, false));
        }

        @Override
        public void onBindViewHolder(@NonNull final MyHoder myHoder, final int i) {
            final membermodel sm = servicemodels.get(i);
            final String name, no, image;
            name = sm.getName();
            no = sm.getId();
            image = sm.getImage();

            String sign_url = new connection(context).getimage();
            if (sign_url != null) {
                String img = sign_url + image;
                Picasso.get().load(img).into(myHoder.imageView);
            }
            myHoder.textView.setText("" + no);
            myHoder.Name.setText("" + name);



        }

        @Override
        public int getItemCount() {
            int arr = 0;
            try {
                if (servicemodels.size() == 0) {
                    arr = 0;
                } else {
                    arr = servicemodels.size();
                }
            } catch (Exception e) {
            }
            return arr;
        }


    }

    private class MyHoder extends RecyclerView.ViewHolder {
        TextView Name;
        TextView textView;
        ImageView imageView;
        CardView cardView;


        public MyHoder(@NonNull View itemView) {
            super(itemView);
            Name = itemView.findViewById(R.id.aa);
            textView = itemView.findViewById(R.id.cc);
            imageView = itemView.findViewById(R.id.bb);
            cardView = itemView.findViewById(R.id.card);


        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.viewgroup, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            Intent intent = new Intent(viewgroup.this, editgroup.class);
            Bundle bundle=new Bundle();
            bundle.putString("grp",grp);
            intent.putExtras(bundle);
            startActivity(intent);
            return true;
        }
        if (id == R.id.action_add) {

            if (level.equalsIgnoreCase("Admin")) {
                Intent intent = new Intent(viewgroup.this, addmembers.class);
                Bundle bundle = new Bundle();
                bundle.putString("grp", grp);
                intent.putExtras(bundle);
                startActivity(intent);
            }else {
                Toast.makeText(context, "You dont have permisions", Toast.LENGTH_SHORT).show();
            }
            return true;
        }
        if (id == R.id.action_config) {
            if (level.equalsIgnoreCase("Admin")) {

            Intent intent = new Intent(viewgroup.this, responsegroup.class);
            Bundle bundle = new Bundle();
            bundle.putString("grp", grp);
            intent.putExtras(bundle);
            startActivity(intent);
            }else {
                Toast.makeText(context, "You dont have permisions", Toast.LENGTH_SHORT).show();
            }
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return super.onSupportNavigateUp();
    }

    @Override
    public void onBackPressed() {

        super.onBackPressed();
    }
}
