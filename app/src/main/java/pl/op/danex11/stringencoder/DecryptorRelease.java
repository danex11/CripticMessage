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

import java.nio.charset.StandardCharsets;
import java.security.InvalidKeyException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.KeyGenerator;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;

//TODO swipeview

/**
 *
 */
public class DecryptorRelease extends AppCompatActivity {

    byte[] keyBytesFromStr;
    String cipherB64Text;
    String[] encodedSourceText;
    String decodedText;

    //copy and paste -ing
    ClipboardManager myClipboard;
    EditText ed2result, keyText;
    TextView resultTextView;
    String deencodedSourceText;


    /**
     * Algorithm setting
     *
     * @param toEncrypt
     * @return
     */
    public static final String md5(final String toEncrypt) {
        try {
            final MessageDigest hashed = MessageDigest.getInstance("md5");
            // final MessageDigest digest = MessageDigest.getInstance("sha-256");
            hashed.update(toEncrypt.getBytes());
            final byte[] bytes = hashed.digest();
            final StringBuilder sb = new StringBuilder();
            //for (int i = 0; i < bytes.length; i++) {sb.append(String.format("%02X", bytes[i]));   }
            for (byte aByte : bytes) {
                sb.append(String.format("%02X", aByte));
            }
            Log.i("tag_key_Decoder", "Decoder key " + sb.toString().toLowerCase());
            return sb.toString().toLowerCase();
        } catch (Exception exc) {
            return ""; // Impossibru!
        }
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

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //  LAYOUT  LAYOUT  LAYOUT
        if (android.os.Build.VERSION.SDK_INT >= 26) {
            setContentView(R.layout.decryptor_layout_materials);
            // >=23
            getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR);
            Log.i("tagsdk", android.os.Build.VERSION.SDK_INT + " >= 26");
        } else {
            setContentView(R.layout.decryptor_layout);
            Log.i("tagsdk", android.os.Build.VERSION.SDK_INT + " < 26");
        }


        //copy and paste -ing
        myClipboard = (ClipboardManager) getSystemService(CLIPBOARD_SERVICE);

        ed2result = findViewById(R.id.givenText);
        resultTextView = findViewById(R.id.resultText);
        keyText = findViewById(R.id.keyText);
        //given text winding and keyboard behaviour
        final EditText givenText;
        givenText = findViewById(R.id.givenText);
        givenText.setImeOptions(EditorInfo.IME_ACTION_NEXT);
        givenText.setRawInputType(InputType.TYPE_CLASS_TEXT);

        //reaction to specific action on this view -  keyboard "Enter" reaction
        final EditText editKey;
        editKey = (EditText) findViewById(R.id.keyText);
        //set password hint font to default - it has mambojumboed
        editKey.setTypeface(Typeface.DEFAULT);
        //set behaviour for keyboard on keyText
        //action on keyboard confirm
        editKey
                .setOnEditorActionListener(new TextView.OnEditorActionListener() {
                    @Override
                    public boolean onEditorAction(TextView v, int actionId,
                                                  KeyEvent event) {
                        boolean handled = false;
                        if (actionId == EditorInfo.IME_ACTION_DONE) {
                            // handle action here
                            //todo
                            //hide keyboard
                            InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                            imm.hideSoftInputFromWindow(editKey.getWindowToken(), 0);
                            //action
                            decode_array(findViewById(R.id.keyText));
                            ClearEditText(givenText);
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
        String text;
        text = resultTextView.getText().toString();
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
    public void ClearEditText(EditText view) {
        view.setText("");
    }

    public void ClearOnButton(View view) {
        ed2result = findViewById(R.id.resultText);
        ed2result.setText("");
        resultTextView.setText("");
    }

    /**
     * USE KEY
     *
     * @param view
     */
    public void UseKey(View view) {
        decode_array(findViewById(R.id.keyText));
    }

    /**
     * Decrypting method
     *
     * @param ciphertext
     * @return
     */
    public String De_text(String ciphertext) {
        //String[] decodedTexts = new String[ciphertext.length];


        //Typed key

        String KeyStrg = keyText.getText().toString();
        //Hash key
        String hashedKey = md5(KeyStrg);
        Log.i("tag_hashedKey", hashedKey);
        keyBytesFromStr = hashedKey.getBytes(StandardCharsets.UTF_8);


        String algorithmo = "AES";
        SecretKeySpec secretkey = new SecretKeySpec(keyBytesFromStr, algorithmo);
        //temp placeholder since decodedTexts has no value yet, and might not get any value if deciphering fails
        String decipheredTextStr = "¯\\_(ツ)_/¯";
        ;

        try {
            Cipher cipher = Cipher.getInstance("AES/ECB/PKCS5Padding");
            //Cipher cipher = Cipher.getInstance("AES/CBC/PKCS5Padding");

            cipher.init(Cipher.DECRYPT_MODE, secretkey);


            //<<@ciphertext is a value we store as a Base64 string
            //todo this is our Base64 problem
            Log.i("tag_ciphertext", "text " + ciphertext);
            byte[] cipherText = Base64.decode(ciphertext.getBytes(), Base64.DEFAULT);

            byte[] decipheredText = cipher.doFinal(cipherText);
            //char encoding??
            decipheredTextStr = decodeUTF8(decipheredText);
            //decodedTexts = decipheredTextStr;


        } catch (InvalidKeyException | BadPaddingException | IllegalBlockSizeException e) {
            //temp placeholder since decodedTexts has no value yet, and might not get any value if deciphering fails
            // decodedText = "¯\\_(ツ)_/¯";// ( ఠ ͟ʖ ఠ) ";
            // decipheredTextStr = "¯\\_(ツ)_/¯";
            e.printStackTrace();
        } catch (NoSuchPaddingException e) {
            e.printStackTrace();
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        }

        return decipheredTextStr;
    }


    /**
     * * this is for working with arrays of strings
     *
     * @
     */
    public void decode_array(View view) {

        //>> for String
        //put hardcoded array here
        //String[] userIdFirebaseArr = {""};
        // userIdFirebaseArr[0]= "StringToEncode";
        //Get text from message textfield
        TextView givenTextView = findViewById(R.id.givenText);

        String givenTextStrg = givenTextView.getText().toString();
        //userIdFirebaseArr[0] = givenTextStrg;
        //decodedText = De_text(userIdFirebaseArr);
        decodedText = De_text(givenTextStrg);
        //Log.i("En_tagDecipheredtext", Arrays.toString(decodedText));

        //resultTextView.setText(decodedText[0]);
        resultTextView.setText(decodedText);


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


    /**
     * * this is for working with arrays of strings
     *
     * @param view
     * @throws
     */
    public void setViews(View view) throws NoSuchAlgorithmException {
        //>> for String
        //Get text from message textfield
        TextView givenTextView = findViewById(R.id.givenText);
        String givenTextStrg = givenTextView.getText().toString();
        //String givenTextStrg = "PKCS5Padding part is how the AES algorithm should handle the last bytes of the data to encrypt into.";

        //THERE
        deencodedSourceText = De_text(givenTextStrg);

        TextView resultTextView = findViewById(R.id.resultText);
        resultTextView.setText(deencodedSourceText);
    }


    /**
     * Encrypting method
     *
     * @param plaintext
     * @return
     */
    public String DeEn_text(String plaintext) {
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
            cipher.init(Cipher.DECRYPT_MODE, hashedKey);
            //TODO this is where we do every string one after another
            Log.i("tagplaintext", "plaintext before " + plaintext);
            //plain text as bytes
            byte[] plainTextBytes = plaintext.getBytes(StandardCharsets.UTF_8);
            //TODO javax.crypto.IllegalBlockSizeException: error:1e06b07b:Cipher functions:EVP_DecryptFinal_ex:WRONG_FINAL_BLOCK_LENGTH
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

}


