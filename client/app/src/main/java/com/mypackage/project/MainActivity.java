package com.mypackage.project;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.graphics.Typeface;
import android.os.Build;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.TypedValue;
import android.view.Display;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.concurrent.ExecutionException;

public class MainActivity extends AppCompatActivity {
    private TextView toastMessage, text;
    private Toast toast;
    private EditText user, pass;
    private Button logIn;
    private ProgressBar progressBar;
    private int h, w;
    private MainActivity mainActivity;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        getWindow().getDecorView().setBackgroundColor(Color.parseColor("#0193D7"));
        findViewById(android.R.id.content).setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                Helper.hideSoftKeyboard(MainActivity.this);
                return false;
            }
        });
        mainActivity = this;
        Display display = getWindowManager().getDefaultDisplay();
        w = display.getWidth();
        h = display.getHeight();
        toastMessage = new TextView(this);
        toastMessage.setTextColor(Color.WHITE);
        toastMessage.setTypeface(null, Typeface.BOLD);
        toastMessage.setPadding(w * 5 / 100, h * 2 / 100, w * 5 / 100, h * 2 / 100);
        toastMessage.setTextSize((float) (w * 2 / 100));
        toast = Toast.makeText(getApplicationContext(), null,
                Toast.LENGTH_SHORT);
        toast.setGravity(Gravity.BOTTOM | Gravity.CENTER_HORIZONTAL, 0, h * 3 / 100);
        Helper helper = new Helper();
        if (!Helper.isOnline(getApplicationContext())) {
            toastMessage.setBackgroundColor(Color.parseColor("#B81102"));
            toastMessage.setText("No internet connection!");
            toast.setView(toastMessage);
            toast.show();
        }
        else {
            String[] parts = helper.getPrefs(this);
            if (parts[0] != null && parts[0].length() > 0) {
                Intent intent = new Intent(mainActivity, HomeActivity.class);
                startActivity(intent);
                finish();
            }
        }
        final RelativeLayout rl = (RelativeLayout) findViewById(R.id.rl) ;
        user = (EditText) findViewById(R.id.user);
        pass = (EditText) findViewById(R.id.pass);
        progressBar = (ProgressBar) findViewById(R.id.progressBar);
        progressBar.getLayoutParams().height = h * 95 / 100;
        progressBar.setVisibility(View.INVISIBLE);
        progressBar.getIndeterminateDrawable().setColorFilter(
                getResources().getColor(R.color.colorPrimaryDark),
                android.graphics.PorterDuff.Mode.SRC_IN);
        logIn = (Button) findViewById(R.id.logIn);
        text = (TextView) findViewById(R.id.myText);
        RelativeLayout.LayoutParams relativeParams;
        relativeParams = (RelativeLayout.LayoutParams) text.getLayoutParams();
        relativeParams.setMargins(0, h * 5 / 100, 0, 0);
        relativeParams.width = w;
        relativeParams.height = h * 10 / 100;
        text.setLayoutParams(relativeParams);
        text.setTypeface(null, Typeface.BOLD_ITALIC);
        text.setGravity(Gravity.CENTER);
        text.setTextSize(TypedValue.COMPLEX_UNIT_PX, w * 7 / 100);
        relativeParams = (RelativeLayout.LayoutParams) user.getLayoutParams();
        relativeParams.setMargins(w * 25 / 100, h * 18 / 100, 0, 0);
        relativeParams.width = w * 50 / 100;
        relativeParams.height = h * 10 / 100;
        user.setLayoutParams(relativeParams);
        user.getBackground().mutate().setColorFilter(getResources().getColor(R.color.colorPrimaryDark), PorterDuff.Mode.SRC_ATOP);
        user.setTextSize(TypedValue.COMPLEX_UNIT_PX, w * 5 / 100);


        relativeParams = (RelativeLayout.LayoutParams) pass.getLayoutParams();
        relativeParams.setMargins(w * 25 / 100, h * 30 / 100, 0, 0);
        relativeParams.width = w * 50 / 100;
        relativeParams.height = h * 10 / 100;
        pass.setLayoutParams(relativeParams);
        pass.getBackground().mutate().setColorFilter(getResources().getColor(R.color.colorPrimaryDark), PorterDuff.Mode.SRC_ATOP);
        pass.setTextSize(TypedValue.COMPLEX_UNIT_PX, w * 5 / 100);


        relativeParams = (RelativeLayout.LayoutParams) logIn.getLayoutParams();
        relativeParams.setMargins(w * 35 / 100, h * 46 / 100, 0, 0);
        relativeParams.width = w * 30 / 100;
        relativeParams.height = h * 8 / 100;
        logIn.setLayoutParams(relativeParams);
        logIn.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                if (getCurrentFocus() != null) {
                    InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                    imm.hideSoftInputFromWindow(getCurrentFocus().getWindowToken(), 0);
                }
                if (!Helper.isOnline(getApplicationContext())) {
                    toastMessage.setBackgroundColor(Color.parseColor("#B81102"));
                    toastMessage.setText("No internet connection!");
                    toast.setView(toastMessage);
                    toast.show();
                } else {
                    LoginModel model = new LoginModel();
                    model.username = user.getText().toString();
                    model.password = pass.getText().toString();
                    try {
                        String res = new Helper.Post(rl, null, "login", model).execute().get();
                        JSONObject json = new JSONObject(res);
                        if (json.getString("message").contains("Invalid"))
                        {
                            toastMessage.setBackgroundColor(Color.parseColor("#B81102"));
                            toastMessage.setText(json.getString("message"));
                            toast.setView(toastMessage);
                            toast.show();
                        }
                        else {
                            SharedPreferences pref = getApplicationContext().getSharedPreferences("MyPref", 0);
                            SharedPreferences.Editor editor = pref.edit();
                            editor.putString("access_token", json.getString("access_token"));
                            editor.putString("refresh_token", json.getString("refresh_token"));
                            editor.putString("user_id", json.getString("user_id"));
                            editor.putString("user", json.getString("user"));
                            editor.commit();
                            Intent intent = new Intent(mainActivity, HomeActivity.class);
                            startActivity(intent);
                            finish();
                        }
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    } catch (ExecutionException e) {
                        e.printStackTrace();
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }
            }
        });
    }
}
