package pl.op.danex11.stringencoder;

import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.graphics.Typeface;
import android.os.Bundle;
import android.text.InputType;
import android.util.Base64;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;


//todo reset to new layout/activity with new icon click


public class EncryptorRelease extends AppCompatActivity {
    byte[] bytesKeyHashed;
    String cipherB64Text;
    String encodedSourceText;

    //copy and paste -ing
    ClipboardManager myClipboard;
    EditText ed1given, ed2result;

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

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //Set Layout
        if (android.os.Build.VERSION.SDK_INT >= 26) {
            setContentView(R.layout.encryptor_layout_materials);
            Log.i("tagsdk", android.os.Build.VERSION.SDK_INT + " >= 26");
        } else {
            setContentView(R.layout.encryptor_layout);
            Log.i("tagsdk", android.os.Build.VERSION.SDK_INT + " < 26");
        }


        //  LAYOUT  LAYOUT  LAYOUT
        //copy and paste -ing
        myClipboard = (ClipboardManager) getSystemService(CLIPBOARD_SERVICE);

        final EditText givenText;
        givenText = findViewById(R.id.givenText);
        givenText.setImeOptions(EditorInfo.IME_ACTION_NEXT);
        givenText.setRawInputType(InputType.TYPE_CLASS_TEXT);

        //        //reaction to specific action on this view  -  keyboard "Enter" reaction
        final EditText editKey;
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
                        if (actionId == EditorInfo.IME_ACTION_DONE) {
                            // handle action here
                            //todo
                            try {
                                encode_array(findViewById(R.id.keyText));
                            } finally {
                            }
                            ClearEditText(givenText);
                            //hide keyboard
                            InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                            imm.hideSoftInputFromWindow(editKey.getWindowToken(), 0);
                            handled = true;
                        }
                        return handled;
                    }
                });

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
     * @param view
     */
    public void UseKey(View view) {
        encode_array(findViewById(R.id.keyText));
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
        TextView resultTextView = findViewById(R.id.resultText);
        resultTextView.setText(encodedSourceText);
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
        //TODO use straight bytes, no need to use String
        String stringKeyHashed = hash(stringKeyRaw);
        //Log.i("tag_hashedKey", hashedKey);
        bytesKeyHashed = stringKeyHashed.getBytes(StandardCharsets.UTF_8);
        //Password-Based Key Derivation Function 2 is a key stretching algorithm
        //adding bytes to passphase to make a key out of it
        //https://stackoverflow.com/questions/29354133/how-to-fix-invalid-aes-key-length
        //https://stackoverflow.com/questions/8091519/pbkdf2-function-in-android
        String algorithmo = "AES";
        //String algorithmo = getInstance();
        SecretKeySpec secretKey = new SecretKeySpec(bytesKeyHashed, algorithmo);

        //ENCRYPT MESSAGE
        try {
            //AdvancedEncodingStandard ElectronicCodeBook
            //PKCS5Padding part is how the AES algorithm should handle the last bytes of the data to encrypt, if the data does not align with a 64 bit or 128 bit block size boundary.
            //AES supported key sizes?
            //Cipher cipher = Cipher.getInstance("AES/ECB/PKCS5Padding");
            Cipher cipher = Cipher.getInstance("AES/CBC/PKCS5Padding");
            //Salt / Initialisation Vector
            SecureRandom random = new SecureRandom();
            byte[] ivBytes = new byte[16];
            random.nextBytes(ivBytes);
            IvParameterSpec iv = new IvParameterSpec(ivBytes);
            //Cipher
            cipher.init(Cipher.ENCRYPT_MODE, secretKey, iv);
            //given message text as bytes
            byte[] plainTextBytes = plaintext.getBytes(StandardCharsets.UTF_8);
            //cipher given message
            byte[] cipherText = cipher.doFinal(plainTextBytes);
            //reformat bytes to string
            //Lifesaving String/Byte encoding
            cipherB64Text = Base64.encodeToString(cipherText, Base64.DEFAULT);

        } catch (InvalidKeyException | BadPaddingException | IllegalBlockSizeException | UnsupportedOperationException | NoSuchAlgorithmException | NoSuchPaddingException | InvalidAlgorithmParameterException e) {
            e.printStackTrace();
        }

        return cipherB64Text;
    }


}
