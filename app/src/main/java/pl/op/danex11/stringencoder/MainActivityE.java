package pl.op.danex11.stringencoder;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;

public class MainActivityE extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (android.os.Build.VERSION.SDK_INT >= 26) {
        } else {

            Intent myIntent = new Intent(MainActivityE.this, EncryptorAPI22.class);
            MainActivityE.this.startActivity(myIntent);
        }
    }
}