package pl.op.danex11.stringencoder;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import android.Manifest;
import android.app.Activity;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Typeface;
import android.os.Bundle;
import android.telephony.SmsManager;
import android.telephony.SubscriptionManager;
import android.text.InputType;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.TextView;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.security.InvalidKeyException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.security.spec.KeySpec;
import java.util.Arrays;
//import java.util.Base64;
import android.util.Base64;
import android.widget.Toast;

import org.bouncycastle.jcajce.provider.symmetric.AES;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.KeyGenerator;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.SecretKey;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.PBEKeySpec;
import javax.crypto.spec.SecretKeySpec;

//todo reset to new layout/activity with new icon click

//todo max keylength to say 10 characters

//  https://derekreynolds.wordpress.com/2012/06/09/how-to-have-multiple-launcher-icons-in-one-android-apk-install-for-different-activities/
//  https://developer.android.com/reference/android/widget/TextView.html#attr_android:imeOptions

// todo constrain Focused field to keyboard to hide fields below
// todo set Toast message if coded messagefield is empty

public class Encryptor extends AppCompatActivity {
    byte[] keyBytesFromStr;
    String cipherB64Text;
    String encodedSourceText;
    String[] decodedText;

    //copy and paste -ing
    ClipboardManager myClipboard;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        if (android.os.Build.VERSION.SDK_INT >= 26) {
            setContentView(R.layout.encryptor_layout_materials);
            Log.i("tagsdk", android.os.Build.VERSION.SDK_INT + " >= 26");
        } else {
            setContentView(R.layout.encryptor_layout);
            Log.i("tagsdk", android.os.Build.VERSION.SDK_INT + " < 26");
        }

        if (android.os.Build.VERSION.SDK_INT >= 23) {
            //ask for permission for sending SMS
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.SEND_SMS}, 1);
        } else {
            // do something for phones running an SDK before
        }
        super.onCreate(savedInstanceState);
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
        //set passworh hint font to default - it has mambojumboed
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
                                setViews(findViewById(R.id.keyText));
                            } catch (IOException | NoSuchAlgorithmException e) {
                                e.printStackTrace();
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

    EditText ed1given, ed2result, ed3phone;

    //todo integrate copy button into coded message textlayout
    //  https://material.io/develop/android/components/text-fields

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
        //ed2result = findViewById(R.id.givenText);
        // ed2result.setText("");
        view.setText("");
    }


    /**
     * Decrypting method
     *
     * @param ciphertext
     * @return
     */
    public String[] De_text(String[] ciphertext) {
        /*
        Cipher cipher = null;
        String[] decodedTexts = new String[ciphertext.length];


        //Typed key
        TextView editedTextView = findViewById(R.id.keyText);
        String KeyStrg = editedTextView.getText().toString();
        //Hash key
        String hashedKey = formatKey(KeyStrg);
        Log.i("tag_hashedKey", hashedKey);
        keyBytesFromStr = hashedKey.getBytes(StandardCharsets.UTF_8);


        try {
            cipher = Cipher.getInstance("AES/ECB/PKCS5Padding");
            //  String algorithmo = "RawByteso";
            // SecretKeySpec key = new SecretKeySpec(keyBytesFromStr, algorithmo);
        } catch (NoSuchAlgorithmException | NoSuchPaddingException e) {
            //temp placeholder since decodedTexts has no value yet, and might not get any value if deciphering fails
            decodedTexts[0] = "zero";
            e.printStackTrace();
        }

        String algorithmo = "RawByteso";
        SecretKeySpec key = new SecretKeySpec(keyBytesFromStr, algorithmo);


        try {
            cipher.init(Cipher.DECRYPT_MODE, key);


            for (int i = 0; i < ciphertext.length; i++) {
                //<<@ciphertext is a value we store as a Base64 string
                byte[] cipherText = Base64.decode(ciphertext[i].getBytes(), Base64.DEFAULT);
                byte[] decipheredText = cipher.doFinal(cipherText);
                String decipheredTextStr = decodeUTF8(decipheredText);
                decodedTexts[i] = decipheredTextStr;
            }


        } catch (InvalidKeyException | BadPaddingException | IllegalBlockSizeException e) {
            //temp placeholder since decodedTexts has no value yet, and might not get any value if deciphering fails
            decodedTexts[0] = "¯\\_(ツ)_/¯";// ( ఠ ͟ʖ ఠ) ";
            e.printStackTrace();
        }

        return decodedTexts;
         */
        String[] empty = new String[0];
        return empty;
    }

    /**
     * * this is for working with arrays of strings
     *
     * @param view
     */
    public void decode_array(View view) {
        //>> for String
        //put hardcoded array here
        String[] userIdFirebaseArr = {""};
        // userIdFirebaseArr[0]= "StringToEncode";
        //Get text from message textfield
        TextView givenTextView = findViewById(R.id.givenText);

        String givenTextStrg = givenTextView.getText().toString();
        userIdFirebaseArr[0] = givenTextStrg;
        decodedText = De_text(userIdFirebaseArr);
        Log.i("En_tagDecipheredtext", Arrays.toString(decodedText));

        TextView resultTextView = findViewById(R.id.resultText);
        resultTextView.setText(decodedText[0]);

    }


    /**
     * * this is for working with arrays of strings
     *
     * @param view
     * @throws IOException
     */
    public void setViews(View view) throws IOException, NoSuchAlgorithmException {
        //>> for String
        //Get text from message textfield
        TextView givenTextView = findViewById(R.id.givenText);
        //String givenTextStrg = givenTextView.getText().toString();
        String givenTextStrg = "PKCS5Padding part is how the AES algorithm should handle the last bytes of the data to encrypt into.";

        //THERE
        encodedSourceText = En_text(givenTextStrg);

        TextView resultTextView = findViewById(R.id.resultText);
        resultTextView.setText(encodedSourceText);
    }


    /**
     * Encrypting method
     *
     * @param plaintext
     * @return
     */
    public String En_text(String plaintext) throws NoSuchAlgorithmException {
        //"AES/ECB/PKCS5Padding"
        String algorithm = "AES/ECB/PKCS5Padding";
        Cipher cipher = null;

        try {
            //AdvancedEncodingStandard ElectronicCodeBook
            //PKCS5Padding part is how the AES algorithm should handle the last bytes of the data to encrypt into. , if the data does not align with a 64 bit or 128 bit block size boundary.
            //AES supported key sizes?
            //todo try different algorithms

            //algorithms providers to trasverse throught
            // Log.i("tagProvid", String.valueOf(Security.getProviders()) + Arrays.toString(Security.getProviders()));
            cipher = Cipher.getInstance(algorithm);

            /*
            >ALGORITHMS<
            AES
            keylength:  128/192/256 bits
            encoding 100 chars gives 154 chars
            DES
            keylength: 8 bytes
            encoding 100 chars gives 142 chars
            DESede
            keylength: 16 or 24 bytes
            encoding 100 chars gives 142 chars
            ECIES
            no Provider
            RC2
            no Provider
            RC4
            keylength: variable
            encoding 100 chars gives 138 chars
            RC5
            no Provider
            Blowfish
            keylength:
            encoding 100 chars gives 142 chars
            ECIES
            no Provider


            >when generating an instance of MessageDigest - for KeyGen <
            SHA-512
            keylength(noPadding): 128 Bytes
            SHA-256
            keylength(noPadding): 64 Bytes
            MD5
            keylength(noPadding): 32 Bytes (256 bits)

             */

            //cipher = Cipher.getInstance("AES/ECB/PKCS5Padding");
        } catch (NoSuchAlgorithmException | NoSuchPaddingException e) {
            Log.i("tagCipherExep", "e ofAlgorithm");
            Log.i("tagCipherExep", String.valueOf(e));
            e.printStackTrace();
        }

        //Typed key
        TextView editedTextView = findViewById(R.id.keyText);
        String KeyStrg = editedTextView.getText().toString();
        //Hashed key
        SecretKey hashedKey = generateKey(KeyStrg);
        Log.i("tagKey", " Key " + hashedKey);
        //Log.i("tagKey", " Key length " + hashedKey.length());
        // keyBytesFromStr = hashedKey.getBytes(StandardCharsets.UTF_8);
        //key from given bytes array
        //String keyinitalgorithmo = "RawByteso";
        //SecretKeySpec key = new SecretKeySpec(keyBytesFromStr, keyinitalgorithmo);
        // Log.i("tagKey", " secretKeyspec " + key);
        //Log.i("tagKey", " secretKeyspec length " + key.);
        //keyBytesFromStr = hashedKey.getBytes(StandardCharsets.UTF_8);
        //Log.i("tagKey", " hashedKeyArrayElementCount " + keyBytesFromStr.length);

        //Password-Based Key Derivation Function 2 is a key stretching algorithm
        //adding bytes to passphase to make a key out of it
        //https://stackoverflow.com/questions/29354133/how-to-fix-invalid-aes-key-length
        //https://stackoverflow.com/questions/8091519/pbkdf2-function-in-android

        //ENCRYPT
        try {
            cipher.init(Cipher.ENCRYPT_MODE, hashedKey);
            //TODO this is where we do every string one after another
            Log.i("tagplaintext", "plaintext before " + plaintext);
            //plain text as bytes
            byte[] plainTextBytes = plaintext.getBytes(StandardCharsets.UTF_8);
            //cipher
            byte[] cipherText = cipher.doFinal(plainTextBytes);
            //cipher text back to string
            cipherB64Text = Base64.encodeToString(cipherText, Base64.DEFAULT);
            Log.i("tagciphertext", "cipher after " + cipherB64Text);
            //String text = readFileAsString("textfile.txt");
            //String cipherB64TextFormat = "\"" + cipherB64Text.replace("\n", "")+ "\"";//.replace("\r", "");

            //this is specific formatting for messagesMessages()
            //encoded64texts[i] = "\"" + cipherB64Text.substring(0, 24) + "\"";


            //this is general function
            //encoded64texts[i] = cipherB64Text;


        } catch (InvalidKeyException | BadPaddingException | IllegalBlockSizeException e) {
            e.printStackTrace();
        }

        return cipherB64Text;
    }

    /**
     * Algorithm setting
     *
     * @param toEncrypt
     * @return
     */
    public static final SecretKey generateKey(final String toEncrypt) {
        KeyGenerator keygen = null;
        try {
            keygen = KeyGenerator.getInstance("AES");
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        }
        SecretKey key = keygen.generateKey();
        return key;

    }


    /**
     * Algorithm setting
     *
     * @param toEncrypt
     * @return
     */
    public static final SecretKey bouncyCastleKey(final String toEncrypt) {
        try {
            //return sb.toString().toLowerCase();
            //return bytes;

            //PKSDFSDF
            //byte[] keyBytesFromStr = sb.getBytes(StandardCharsets.UTF_8);
            //String algorithmo = "RawByteso";
            //  String algorithmo = "AES";
            //  SecretKeySpec key = new SecretKeySpec(bytes, algorithmo);


            // Number of PBKDF2 hardening rounds to use. Larger values increase
            // computation time. You should select a value that causes computation
            // to take >100ms.
            final int iterations = 1000;
            // Generate a 256-bit key
            final int outputKeyLength = 8;
            //generate string out of keyphrase
            char[] keyAsCharArr = toEncrypt.toCharArray();
            //generate random salt
            SecureRandom rGen = new SecureRandom();
            byte[] saltbytes = new byte[16];
            rGen.nextBytes(saltbytes);
            System.out.println("salt bytes : " + Arrays.toString(saltbytes));
            //generate key
            SecretKeyFactory secretKeyFactory = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA1");
            Log.i("tag_secretKeyFactory", secretKeyFactory.toString());
            KeySpec keySpec = new PBEKeySpec(keyAsCharArr, saltbytes, iterations, outputKeyLength);
            SecretKey secretKey = secretKeyFactory.generateSecret(keySpec);

            return secretKey;
        } catch (Exception exc) {
            //return "Algorithm exception :/ "; // Impossibru!
            return null;
        }
    }

    /**
     * * making String from bytes
     *
     * @param bytes
     * @return
     */
    String decodeUTF8(byte[] bytes) {
        return new String(bytes, StandardCharsets.UTF_8);
    }

}


