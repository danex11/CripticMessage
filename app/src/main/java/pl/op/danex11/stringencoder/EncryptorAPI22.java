package pl.op.danex11.stringencoder;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.annotation.SuppressLint;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.graphics.Typeface;
import android.os.Bundle;
import android.text.Editable;
import android.text.InputType;
import android.text.TextWatcher;
import android.util.Base64;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.constraintlayout.widget.ConstraintSet;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.security.InvalidKeyException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.util.Arrays;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.spec.SecretKeySpec;


//todo reset to new layout/activity with new icon click

//TODO lock layout inside layout with fixed height, to relate animations to its fixed height

public class EncryptorAPI22 extends AppCompatActivity {
    byte[] bytesKeyHashed;
    String cipherB64Text;
    String encodedSourceText;

    //copy and paste -ing
    ClipboardManager myClipboard;
    EditText ed1given, ed2result;
    TextView resultTextView;
    Button copybutton;
    EditText givenText;
    EditText editKey;

    ConstraintLayout layout;

    Animation animGiven, animCopyButton, animResult, animGivenback, animKey, animKeyback;


    /**
     * Algorithm setting
     *
     * @param toHash
     * @return
     */
    public static final String hash(final String toHash) {
        try {
            final MessageDigest hashd = MessageDigest.getInstance("md5");
            //final MessageDigest digest = MessageDigest.getInstance("sha-256");
            hashd.update(toHash.getBytes());
            //digest
            final byte[] bytes = hashd.digest();
            //string builder
            final StringBuilder sb = new StringBuilder();
            for (int i = 0; i < bytes.length; i++) {
                sb.append(String.format("%02X", bytes[i]));
            }
            //todo why to lower case?
            Log.i("tag_stringbuild_hashed", "stringbuild hashed key " + sb);
            return sb.toString().toLowerCase();
        } catch (Exception exc) {
            return "Algorithm exception :/ "; // Impossibru!
        }
    }

    public static final byte[] hashbytes(final String toHash) throws NoSuchAlgorithmException {
        final MessageDigest hashd = MessageDigest.getInstance("md5");
        //final MessageDigest digest = MessageDigest.getInstance("sha-256");
        hashd.update(toHash.getBytes());
        //digest
        final byte[] bytes = hashd.digest();
        Log.i("tag_bytes_hashed", "bytes hashed key " + Arrays.toString(bytes));
        return bytes;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //Set Layout
        if (android.os.Build.VERSION.SDK_INT >= 26) {
            setContentView(R.layout.encryptor_layout_materials);
            //the layout on which you are working
            layout = (ConstraintLayout) findViewById(R.id.encryptorMaterialsInnerLayout);
            Log.i("tagsdk", android.os.Build.VERSION.SDK_INT + " >= 26");
        } else {
            setContentView(R.layout.encryptor_layout);
            layout = (ConstraintLayout) findViewById(R.id.encryptorInnerLayout);
            Log.i("tagsdk", android.os.Build.VERSION.SDK_INT + " < 26");
        }


        //  LAYOUT  LAYOUT  LAYOUT
        //copy and paste -ing
        myClipboard = (ClipboardManager) getSystemService(CLIPBOARD_SERVICE);

        givenText = findViewById(R.id.givenText);
        givenText.setImeOptions(EditorInfo.IME_ACTION_NEXT);
        givenText.setRawInputType(InputType.TYPE_CLASS_TEXT);

        resultTextView = findViewById(R.id.resultText);
        resultTextView.setVisibility(View.INVISIBLE);


        copybutton = findViewById(R.id.copyButton);
        copybutton.setVisibility(View.INVISIBLE);

        //final Animation animBounce = AnimationUtils.loadAnimation(getApplicationContext(), R.anim.bounce_anim);
        animGiven = AnimationUtils.loadAnimation(getApplicationContext(), R.anim.anim_given);
        animCopyButton = AnimationUtils.loadAnimation(getApplicationContext(), R.anim.anim_copybutton);
        animResult = AnimationUtils.loadAnimation(getApplicationContext(), R.anim.anim_result);
        animGivenback = AnimationUtils.loadAnimation(getApplicationContext(), R.anim.anim_givenback);
        animKey = AnimationUtils.loadAnimation(getApplicationContext(), R.anim.anim_key);
        animKeyback = AnimationUtils.loadAnimation(getApplicationContext(), R.anim.anim_keyback);


        //        //reaction to specific action on this view  -  keyboard "Enter" reaction
        editKey = (EditText) findViewById(R.id.keyText);
        //set password hint font to default - it was mambojumboing
        editKey.setTypeface(Typeface.DEFAULT);
        //set behaviour for keyboard on keyText
        editKey
                .setOnEditorActionListener(new TextView.OnEditorActionListener() {
                    @Override
                    public boolean onEditorAction(TextView v, int actionId,
                                                  KeyEvent event) {
                        boolean handled = false;
                        if (actionId == EditorInfo.IME_ACTION_NEXT) {
                            // handle action here
                            try {

                                UseKey();

                                //encode_array(findViewById(R.id.keyText));
                            } finally {
                            }
                            //clear textview
                            //ClearEditText(givenText);
                            //hide keyboard
                            //InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                            //imm.hideSoftInputFromWindow(editKey.getWindowToken(), 0);
                            handled = true;
                        }
                        return handled;
                    }
                });


// todo: on text change in editKey, listen for editkey.length>0, than change keyButton style

        ConstraintSet constraintSet = new ConstraintSet();
        editKey.addTextChangedListener(new TextWatcher() {
            Button button;
            int buttonid;

            @SuppressLint("ResourceType")
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                Log.i("tag", "Your Text onTextChanged");
                // Fires right as the text is being changed (even supplies the range of text)

            }

            @Override
            public void beforeTextChanged(CharSequence s, int start, int count,
                                          int after) {
                Log.i("tag", "Your Text beforeTextChanged");
                // Fires right before text is changing


            }

            @Override
            public void afterTextChanged(Editable s) {
                Log.i("tag", "Your Text afterTextChanged");
                // Fires right after the text has changed

                if (button != null) layout.removeView(button);
                if (editKey.length() == 0 || editKey == null) {
                    //button style "faded"
                    buttonid = R.layout.buttonoff_key;
                    //Log.i("tag", "Your Text length=0");
                } else {
                    buttonid = R.layout.buttonon_key;
                    //button style "ready"
                    //Log.i("tag", "Your Text length>0");
                }
                button = (Button) getLayoutInflater().inflate(buttonid, null);
                button.setId(buttonid);
                button.setText(R.string.key_butt_lbl);
                //if (editKey.length() == 0) {
                //button.getBackground().setColorFilter(ContextCompat.getColor(getApplicationContext(), R.color.colorAccent), PorterDuff.Mode.MULTIPLY);
                //btn.setVisibility(View.INVISIBLE
                //}
                constraintSet.clone(layout);
                Log.i("constraitainset", String.valueOf(constraintSet));
                //constraintSet.connect(button.getId(), ConstraintSet.TOP, R.id.keyText, ConstraintSet.TOP, 0);
                Log.i("constraitainsetAfter", String.valueOf(constraintSet));
                constraintSet.applyTo(layout);
                constraintSet.clone(layout);
                //constraintSet.connect(button.getId(), ConstraintSet.TOP, R.id.keyText, ConstraintSet.TOP, 0);
                Log.i("constraitainsetAfter", String.valueOf(constraintSet));
                constraintSet.applyTo(layout);

                //add button to the layout
                layout.addView(button);
                constraintSet.clone(layout);
                //constraintSet.connect(button.getId(), ConstraintSet.TOP, R.id.keyText, ConstraintSet.TOP, 0);
                Log.i("constraitainsetAfter", String.valueOf(constraintSet));
                constraintSet.applyTo(layout);
                layout.removeView(button);
                constraintSet.clone(layout);
                //constraintSet.connect(button.getId(), ConstraintSet.TOP, R.id.keyText, ConstraintSet.TOP, 0);
                Log.i("constraitainsetAfter", String.valueOf(constraintSet));
                constraintSet.applyTo(layout);
                layout.addView(button);
                constraintSet.clone(layout);
                //constraintSet.connect(button.getId(), ConstraintSet.RIGHT, R.id.keyText, ConstraintSet.LEFT, 0);
                constraintSet.connect(button.getId(), ConstraintSet.RIGHT, R.id.givenText, ConstraintSet.RIGHT, 0);
                constraintSet.connect(button.getId(), ConstraintSet.BOTTOM, R.id.resultText, ConstraintSet.TOP, 20);
                Log.i("constraitainsetAfter", String.valueOf(constraintSet));
                constraintSet.applyTo(layout);
                //button Onclick listener
                button.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        // put code on click operation
                        UseKey();


                    }
                });
            }
        });

    }

    @Override
    protected void onPause() {
        super.onPause();
        finish();

    }


    /**
     * COPY
     */
    public void Copy(View view) {
        ed1given = findViewById(R.id.resultText);
        String text;
        text = ed1given.getText().toString();
        ClipData myClip;
        myClip = ClipData.newPlainText("text", text);
        myClipboard.setPrimaryClip(myClip);
        Toast.makeText(getApplicationContext(), "Text Copied",
                Toast.LENGTH_SHORT).show();
    }

    /**
     * PASTE
     */
    public void Paste(View view) {
        ed2result = findViewById(R.id.givenText);
        ClipData abc = myClipboard.getPrimaryClip();
        ClipData.Item item = abc.getItemAt(0);
        String text = item.getText().toString();
        ed2result.setText(text);
        Toast.makeText(getApplicationContext(), "Text Pasted",
                Toast.LENGTH_SHORT).show();

    }

    /**
     * CLEAR
     */
    public void Clear(View view) {
        ed2result = findViewById(R.id.givenText);
        ed2result.setText("");
    }

    public void ClearEditText(EditText view) {
        view.setText("");
    }

    /**
     * USE KEY
     *
     * @param
     */
    public void UseKey() {
        if (editKey.length() > 0) {
            //hide software keyboard
            InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
            imm.hideSoftInputFromWindow(editKey.getWindowToken(), 0);

            //Encode
            encode_array(editKey);
            //set layout
            resultTextView.setVisibility(View.VISIBLE);
            copybutton.setVisibility(View.VISIBLE);

            animGiven.setAnimationListener(new Animation.AnimationListener() {
                @Override
                public void onAnimationStart(Animation animation) {
                    resultTextView.setText("");
                    findViewById(R.id.keyText).startAnimation(animKey);

                }

                @Override
                public void onAnimationRepeat(Animation animation) {
                }

                @Override
                public void onAnimationEnd(Animation animation) {
                    //do stuff
                    //givenText.setVisibility(View.INVISIBLE);
                    givenText.setText("");
                    givenText.startAnimation(animGivenback);
                    resultTextView.setText(encodedSourceText);
                    //set coursot to result textview
                    resultTextView.requestFocus();
                    findViewById(R.id.keyText).startAnimation(animKeyback);

                }
            });

            copybutton.startAnimation(animCopyButton);
            resultTextView.startAnimation(animResult);

            givenText.startAnimation(animGiven);

        } else {
            Toast.makeText(getApplicationContext(), "Key should not be empty",
                    Toast.LENGTH_SHORT).show();
        }


    }

    /**
     * * this is for working with arrays of strings
     *
     * @param view
     * @throws IOException
     */
    public void encode_array(View view) {
        //>> for String
        //put hardcoded array here
        // String[] userIdFirebaseArr = {""};
        // userIdFirebaseArr[0]= "StringToEncode";

        //Get text from message textfield
        TextView givenTextView = findViewById(R.id.givenText);
        String givenTextStrg = givenTextView.getText().toString();
        // userIdFirebaseArr[0] = givenTextStrg;

        //String userIdFirebase = userIdFirebaseArr[0];
        // Log.i("En_tagplaintext", Arrays.toString(userIdFirebaseArr));
        //  encodedSourceText = En_text(userIdFirebaseArr);
        encodedSourceText = En_text(givenTextStrg);
/*
        //>> for String[]
        // array of messages
        Log.i("En_tagplaintext", Arrays.toString(messagesMessages()));
         encodedSourceText = En_text(messagesMessages());

//Logcat is not capable of displaying all records, cuts it halfway
        Log.i("En_tagcipheredtext", Arrays.toString(encodedSourceText));
        //Log.i("En_tagcipheredtext",String.valueOf(encodedSourceText ));
*/
/*
        //>> writing to file
        writeToFile(Arrays.toString(encodedSourceText));
*/
//TODO  whare to set text view
        //>>> resultTextView.setText(encodedSourceText);
        //String resultTextStrg = givenTextView.getText().toString();

    }

    /**
     * Encrypting method
     *
     * @param plaintext
     * @return
     */
    public String En_text(String plaintext) {

        //Typed key
        TextView editedTextView = findViewById(R.id.keyText);
        String stringKeyRaw = editedTextView.getText().toString();
        //HashING key
        String stringKeyHashed = hash(stringKeyRaw);

        //using straight bytes, no need to use String
        byte[] stringKeyHashedbyte = new byte[0];
        try {
            stringKeyHashedbyte = hashbytes(stringKeyRaw);
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        }

        //TODO Base64
        bytesKeyHashed = stringKeyHashed.getBytes(StandardCharsets.UTF_8);

        //Password-Based Key Derivation Function 2 is a key stretching algorithm
        //adding bytes to passphase to make a key out of it
        //https://stackoverflow.com/questions/29354133/how-to-fix-invalid-aes-key-length
        //https://stackoverflow.com/questions/8091519/pbkdf2-function-in-android
        String algorithmo = "AES";
        //String algorithmo = getInstance();

        //salt
        SecureRandom random = new SecureRandom();
        byte[] salt = new byte[16];
        random.nextBytes(salt);

        SecretKeySpec secretKey = new SecretKeySpec(bytesKeyHashed, algorithmo);
        Log.i("tag_secretKey", "secretKey " + secretKey);


        //ENCRYPT MESSAGE
        try {
            //AdvancedEncodingStandard ElectronicCodeBook
            //PKCS5Padding part is how the AES algorithm should handle the last bytes of the data to encrypt, if the data does not align with a 64 bit or 128 bit block size boundary.
            //AES supported key sizes?
            Cipher cipher = Cipher.getInstance("AES/ECB/PKCS5Padding");
            //Cipher cipher = Cipher.getInstance("AES/CBC/PKCS5Padding");
            //Salt / Initialisation Vector
            //SecureRandom random = new SecureRandom();
            //byte[] ivBytes = new byte[16];
            //random.nextBytes(ivBytes);
            //IvParameterSpec iv = new IvParameterSpec(ivBytes);
            //Cipher
            cipher.init(Cipher.ENCRYPT_MODE, secretKey);
            //given message text as bytes
            byte[] plainTextBytes = plaintext.getBytes(StandardCharsets.UTF_8);
            //cipher given message
            byte[] cipherText = cipher.doFinal(plainTextBytes);
            //reformat bytes to string
            //Lifesaving String/Byte encoding
            cipherB64Text = Base64.encodeToString(cipherText, Base64.DEFAULT);
            Log.i("tag64encodetoStr", cipherB64Text);
            //base64test
            byte[] cipherText64 = Base64.decode(cipherB64Text.getBytes(), Base64.DEFAULT);
            String cipherB64Text64 = Base64.encodeToString(cipherText64, Base64.DEFAULT);
            Log.i("tag64decodetoStr", cipherB64Text64);


        } catch (InvalidKeyException | BadPaddingException | IllegalBlockSizeException | UnsupportedOperationException | NoSuchAlgorithmException | NoSuchPaddingException e) {
            e.printStackTrace();
        }

        return cipherB64Text;
    }


}
