package pl.op.danex11.stringencoder;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

public class MainMenu extends AppCompatActivity {

    TextView info;
    Animation animFingeratEncode;
    ImageView finger;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main_menu_layout);

        //finger animation
        animFingeratEncode = AnimationUtils.loadAnimation(getApplicationContext(), R.anim.finger_at_encode);
        finger = findViewById(R.id.fingerview);
        //finger.setTranslationX(180);
        finger.startAnimation(animFingeratEncode);


        //justification of info text
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            info = findViewById(R.id.info);
            //info.setJustificationMode(JUSTIFICATION_MODE_INTER_WORD);
        }


    }


    public void EncryptActivity(View view) {
        if (android.os.Build.VERSION.SDK_INT >= 22) {
            Intent myIntent = new Intent(MainMenu.this, EncryptorAPI22.class);
            MainMenu.this.startActivity(myIntent);
        } else {
            Toast.makeText(getApplicationContext(), R.string.toast_enc_txtcopied,
                    Toast.LENGTH_SHORT).show();
        }
    }

    public void DecryptActivity(View view) {
        if (android.os.Build.VERSION.SDK_INT >= 22) {
            Intent myIntent2 = new Intent(MainMenu.this, DecryptorAPI22.class);
            MainMenu.this.startActivity(myIntent2);
        } else {
            Toast.makeText(getApplicationContext(), R.string.toast_enc_txtcopied,
                    Toast.LENGTH_SHORT).show();
        }
    }

}