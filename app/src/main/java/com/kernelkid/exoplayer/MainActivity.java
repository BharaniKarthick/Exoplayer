package com.kernelkid.exoplayer;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;

public class MainActivity extends AppCompatActivity {

    EditText editText;
    String url="";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        editText=findViewById(R.id.url);
    }

    public void playVideo(View view) {
        Intent i=new Intent(this,ExoPlayer.class);
        i.putExtra("url", editText.getText().toString());
        startActivity(i);
    }


}