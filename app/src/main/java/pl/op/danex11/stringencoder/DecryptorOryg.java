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
import java.security.InvalidKeyException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Arrays;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.KeyGenerator;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;

/**
 *
 */
public class DecryptorOryg extends AppCompatActivity {

    byte[] keyBytesFromStr;
    String cipherB64Text;
    String[] encodedSourceText;
    String decodedText;

    //copy and paste -ing
    ClipboardManager myClipboard;


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


        //  SMS PERMISSION DIALOG
        /*
        if (android.os.Build.VERSION.SDK_INT >= 23) {
            //ask for permission for sending SMS
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.SEND_SMS}, 1);
        } else {
            // do something for phones running an SDK before
        }
         */

        //copy and paste -ing
        myClipboard = (ClipboardManager) getSystemService(CLIPBOARD_SERVICE);


        //InputMethodManager inputManager = (InputMethodManager) context.getSystemService(Context.INPUT_METHOD_SERVICE);
        // inputManager.hideSoftInputFromWindow(this.getCurrentFocus().getWindowToken(),InputMethodManager.HIDE_NOT_ALWAYS);


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

    EditText ed1given, ed2result;


    /**
     * USE KEY
     *
     * @param view
     */
/*
    public void UseKey(View view) {
        try {
            setViews(findViewById(R.id.keyText));
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        }
        //clear plain message
        //ClearEditText(givenText);
    }
 */

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
    public void ClearEditText(EditText view) {
        //ed2result = findViewById(R.id.givenText);
        // ed2result.setText("");
        view.setText("");
    }

    public void ClearOnButton() {
        ed2result = findViewById(R.id.resultText);
        ed2result.setText("");
        ed1given.setText("");
    }

    String deencodedSourceText;

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
     * USE KEY
     *
     * @param view
     */
    public void UseKey(View view) {
        decode_array(findViewById(R.id.keyText));
        //clear plain message
        //ClearEditText(givenText);
    }


    /**
     * Decrypting method
     *
     * @param ciphertext
     * @return
     */
    public String De_text(String ciphertext) {
        Cipher cipher = null;
        //String[] decodedTexts = new String[ciphertext.length];


        //Typed key
        TextView editedTextView = findViewById(R.id.keyText);
        String KeyStrg = editedTextView.getText().toString();
        //Hash key
        String hashedKey = md5(KeyStrg);
        Log.i("tag_hashedKey", hashedKey);
        keyBytesFromStr = hashedKey.getBytes(StandardCharsets.UTF_8);


        try {
            cipher = Cipher.getInstance("AES/ECB/PKCS5Padding");
            //  String algorithmo = "RawByteso";
            // SecretKeySpec key = new SecretKeySpec(keyBytesFromStr, algorithmo);
        } catch (NoSuchAlgorithmException | NoSuchPaddingException e) {
            //temp placeholder since decodedTexts has no value yet, and might not get any value if deciphering fails
            decodedText = "zero";
            e.printStackTrace();
        }

        String algorithmo = "RawByteso";
        SecretKeySpec key = new SecretKeySpec(keyBytesFromStr, algorithmo);
        String decipheredTextStr;

        try {
            cipher.init(Cipher.DECRYPT_MODE, key);


            //<<@ciphertext is a value we store as a Base64 string
            byte[] cipherText = Base64.decode(ciphertext.getBytes(), Base64.DEFAULT);
            byte[] decipheredText = cipher.doFinal(cipherText);
            decipheredTextStr = decodeUTF8(decipheredText);
            //decodedTexts = decipheredTextStr;


        } catch (InvalidKeyException | BadPaddingException | IllegalBlockSizeException e) {
            //temp placeholder since decodedTexts has no value yet, and might not get any value if deciphering fails
            decodedText = "¯\\_(ツ)_/¯";// ( ఠ ͟ʖ ఠ) ";
            decipheredTextStr = "¯\\_(ツ)_/¯";
            e.printStackTrace();
        }

        return decipheredTextStr;
    }


    /**
     * Algorithm setting
     *
     * @param toEncrypt
     * @return
     */
    public static final String md5(final String toEncrypt) {
        try {
            final MessageDigest digest = MessageDigest.getInstance("md5");
            // final MessageDigest digest = MessageDigest.getInstance("sha-256");
            digest.update(toEncrypt.getBytes());
            final byte[] bytes = digest.digest();
            final StringBuilder sb = new StringBuilder();
            //for (int i = 0; i < bytes.length; i++) {sb.append(String.format("%02X", bytes[i]));   }
            for (byte aByte : bytes) {
                sb.append(String.format("%02X", aByte));
            }
            return sb.toString().toLowerCase();
        } catch (Exception exc) {
            return ""; // Impossibru!
        }
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


    //
    //
    //
    /**
     * UNUSED METHODS      * UNUSED METHODS     * UNUSED METHODS     * UNUSED METHODS     * UNUSED METHODS     * UNUSED METHODS     * UNUSED METHODS
     */
    //////////////UNUSED METHODS///////////////////vvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvv
    //////////////UNUSED METHODS///////////////////vvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvv
    //////////////UNUSED METHODS///////////////////vvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvv

    /**
     * * this is for working with arrays of strings
     *
     * @param view
     * @throws
     */
    public void encode_array(View view) {
        //>> for String
        //put hardcoded array here
        String[] userIdFirebaseArr = {""};
        // userIdFirebaseArr[0]= "StringToEncode";

        //Get text from message textfield
        TextView givenTextView = findViewById(R.id.givenText);
        String givenTextStrg = givenTextView.getText().toString();
        userIdFirebaseArr[0] = givenTextStrg;

        //String userIdFirebase = userIdFirebaseArr[0];
        Log.i("En_tagplaintext", Arrays.toString(userIdFirebaseArr));
        encodedSourceText = En_text(userIdFirebaseArr);
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

        TextView resultTextView = findViewById(R.id.resultText);
        resultTextView.setText(encodedSourceText[0]);
        //String resultTextStrg = givenTextView.getText().toString();
 */
    }

    /**
     * Encrypting method
     *
     * @param plaintext
     * @return
     */
    public String[] En_text(String[] plaintext) {
        Cipher cipher = null;
        String[] encoded64texts = new String[plaintext.length];
        Log.i("tag_array.length", String.format("tag_plaintextlength = %d", plaintext.length));
        try {
            //AdvancedEncodingStandard ElectronicCodeBook
            //PKCS5Padding part is how the AES algorithm should handle the last bytes of the data to encrypt, if the data does not align with a 64 bit or 128 bit block size boundary.
            //AES supported key sizes?
            cipher = Cipher.getInstance("AES/ECB/PKCS5Padding");
        } catch (NoSuchAlgorithmException | NoSuchPaddingException e) {
            e.printStackTrace();
        }

        //Typed key
        TextView editedTextView = findViewById(R.id.keyText);
        String KeyStrg = editedTextView.getText().toString();
        //Hash key
        String hashedKey = md5(KeyStrg);
        Log.i("tag_hashedKey", hashedKey);
        keyBytesFromStr = hashedKey.getBytes(StandardCharsets.UTF_8);

        //Password-Based Key Derivation Function 2 is a key stretching algorithm
        //adding bytes to passphase to make a key out of it
        //https://stackoverflow.com/questions/29354133/how-to-fix-invalid-aes-key-length
        //https://stackoverflow.com/questions/8091519/pbkdf2-function-in-android

        String algorithmo = "RawByteso";
        SecretKeySpec key = new SecretKeySpec(keyBytesFromStr, algorithmo);

        //ENCRYPT array in loop
        try {
            cipher.init(Cipher.ENCRYPT_MODE, key);

            //TODO this is where we do every string one after another
            for (int i = 0; i < plaintext.length; i++) {
                byte[] plainTextBytes = plaintext[i].getBytes(StandardCharsets.UTF_8);
//text as bytes
                byte[] cipherText = cipher.doFinal(plainTextBytes);
//text as string
                cipherB64Text = Base64.encodeToString(cipherText, Base64.DEFAULT);

                //String text = readFileAsString("textfile.txt");
                //String cipherB64TextFormat = "\"" + cipherB64Text.replace("\n", "")+ "\"";//.replace("\r", "");

//this is specyfic formatting for messagesMessages()
                //encoded64texts[i] = "\"" + cipherB64Text.substring(0, 24) + "\"";
                encoded64texts[i] = cipherB64Text;


                //this is general function
                //encoded64texts[i] = cipherB64Text;
            }


        } catch (InvalidKeyException | BadPaddingException | IllegalBlockSizeException e) {
            e.printStackTrace();
        }

        return encoded64texts;
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

        TextView resultTextView = findViewById(R.id.resultText);
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
/*
    private void writeToFile(String content) {
        try {
            File path = getApplicationContext().getExternalFilesDir(null); //Environment.getExternalStorageDirectory() + "/test.txt";
            File file = new File(path, "test.txt");

            if (!file.exists()) {
                file.createNewFile();
            }
            FileWriter writer = new FileWriter(file);
            writer.append(content);
            writer.flush();
            writer.close();
        } catch (IOException e) {
            Log.e("tag", "not written", e);
        }
    }
 */
}


