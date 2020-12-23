package pl.op.danex11.stringencoder;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;

public class MainActivityD extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (android.os.Build.VERSION.SDK_INT >= 26) {
        } else {
            //todo this is a dummy activity -- programm 'close app' reaction to back button
            //fire activity
            Intent myIntent = new Intent(MainActivityD.this, DecryptorAPI22.class);
            MainActivityD.this.startActivity(myIntent);
        }
    }
}