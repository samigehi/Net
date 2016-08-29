package com.sgsoft.net;

import android.app.Activity;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.google.gson.JsonArray;
import com.sgsoft.net.Net.Callback;

public class MainActivity extends Activity {

    ImageView image;
    TextView textView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Utils.setBaseUrl("https://127.0.0.1:1002/SumitGehi/DesktopModules/Test/API/");
        textView = (TextView) findViewById(R.id.textView1);
        findViewById(R.id.button1).setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                loadData();
            }
        });
    }

    private void loadDrawable() {

        Net<Drawable> request = Net.create(this, "https://github.com/fluidicon.png");
        request.asDrawable(true);
        request.execute(new Callback<Drawable>() {

            @SuppressWarnings("deprecation")
            @Override
            public void onComplete(Drawable result, Exception e) {
                if (result != null) {
                    image.setBackgroundDrawable(result);
                    Utils.log("Load Drawable", "size ****  width = " + result.getIntrinsicWidth() + ", height = "
                            + result.getIntrinsicHeight());
                } else {
                    e.printStackTrace();
                }
            }
        });

    }

    private void loadData() {

        final Net<JsonArray> request = Net.with(this, "Login/user");
        // for post request
        request.add("key", "value");
        request.add("username", "sumeet");
        request.add("password", "xxxxxxxxx");
        request.execute(new Callback<JsonArray>() {

            @Override
            public void onComplete(JsonArray result, Exception e) {
                // something with result here
                if (result != null) {
                    Boolean success = Utils.getBool(result.get(0).getAsJsonObject(), "success");
                    int auth = Utils.getInt(result.get(0).getAsJsonObject(), "id");
                    String token = Utils.get(result.get(0).getAsJsonObject(), "token");

                    if (textView != null) textView.setText(result.toString());
                } else {
                    // print error in logcat
                    e.printStackTrace();
                }
            }
        });

    }

    private void loadDataSinglton() {

        Net<Object> request = Net.with(this, "Login/user/auth").add("key", "value").add("username", "sumeet")
                .add("password", "xxxxxxxxx");

        request.execute(new Callback<JsonArray>() {

            @Override
            public void onComplete(JsonArray result, Exception e) {
                if (result != null) {
                    // something with result here
                } else {
                    // print error in logcat
                    e.printStackTrace();
                }
            }
        });

    }

    private void getRequest() {

        //with no parameter create GET request
        Net<Object> request = Net.with(this, "Login/user/auth");
        request.execute(new Callback<JsonArray>() {

            @Override
            public void onComplete(JsonArray result, Exception e) {
                if (result != null) {
                    // something with result here
                } else {
                    // print error in logcat
                    e.printStackTrace();
                }
            }
        });

    }
}
