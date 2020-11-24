package pl.op.danex11.stringencoder;

import android.app.Activity;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.telephony.SmsManager;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

public class SendSMS {
    //View view = this.view;
    //SendSMS sendsms = new SendSMS(view);//.Send(view, "","");
    boolean nophoneno = false;
    boolean wrongphoneno = false;

    public SendSMS(View view) {
    }


    //todo get rid of sms sending to go by Google policy :(

    /**
     * SMS SENDING
     */
    public void Send(View view, String number, String messageToSend) {
        //message
        //giventext = findViewById(R.id.resultText);
        /*
        giventext.setText("GabeSechan in looking at this further, it seems both may be correct. " +
                "Several sources confirm data sms messages are sent over cell data channels. " +
                "Others show that data is sometimes sent over the same channels as text, but with protocol additions. " +
                "The Android base source has at least some references to this second technique. " +
                "A few of the references I found: android.stackexchange.com/questions/9108/what-is-a-data-sms gsm-modem.de/sms-pdu-mode.html androidauthority.com/what-is-sms-280988 – Hod Jan ");
         */
        //String text;
        //text = giventext.getText().toString();
        //String messageToSend = text;
        //phone no
        ///ed3phone = findViewById(R.id.phoneText);
        //String phoneNo;
        //phoneNo = ed3phone.getText().toString();
        //String number = "+48" + phoneNo;
        //String number = phoneNo;
        if (number == null) {
            //Toast.makeText(getBaseContext(), "No phone number",
            //       Toast.LENGTH_LONG).show();
            nophoneno = true;
        } else {   //sms sending
//todo
            //   PendingIntent sentPI = PendingIntent.getBroadcast(this, 0, new Intent("SMS_SENT"), 0);
            //   PendingIntent deliveredPI = PendingIntent.getBroadcast(this, 0, new Intent("SMS_DELIVERED"), 0);
//  https://mobiforge.com/design-development/sms-messaging-android
            //SmsManager.getDefault().sendTextMessage(number, null, messageToSend, sentPI, deliveredPI);
            if (number.length() != 9) {
                //Toast.makeText(getBaseContext(), "Wrong phone number",
                //        Toast.LENGTH_LONG).show();
                nophoneno = true;
            } else {
                //sendSMS(number, messageToSend, getContext());
            }
            // Toast.makeText(getApplicationContext(), "SMS sended successfully",
            //  Toast.LENGTH_LONG).show();}

        }
    }

    //---sends an SMS message to another device---
    private void sendSMS(String phoneNumber, String message, Context context) {
        String SENT = "SMS_SENT";
        String DELIVERED = "SMS_DELIVERED";

        PendingIntent sentPI = PendingIntent.getBroadcast(context, 0,
                new Intent(SENT), 0);

        PendingIntent deliveredPI = PendingIntent.getBroadcast(context, 0,
                new Intent(DELIVERED), 0);

        //---when the SMS has been sent---
        /*
        registerReceiver(new BroadcastReceiver() {
            @Override
            public void onReceive(Context arg0, Intent arg1) {
                switch (getResultCode()) {
                    case Activity.RESULT_OK:
                        Toast.makeText(getBaseContext(), "SMS sent",
                                Toast.LENGTH_SHORT).show();
                        break;
                    case SmsManager.RESULT_ERROR_GENERIC_FAILURE:
                        Toast.makeText(getBaseContext(), "Generic failure (SMS too long?)",
                                Toast.LENGTH_SHORT).show();
                        break;
                    case SmsManager.RESULT_ERROR_NO_SERVICE:
                        Toast.makeText(getBaseContext(), "No service",
                                Toast.LENGTH_SHORT).show();
                        break;
                    case SmsManager.RESULT_ERROR_NULL_PDU:
                        Toast.makeText(getBaseContext(), "Null PDU",
                                Toast.LENGTH_SHORT).show();
                        break;
                    case SmsManager.RESULT_ERROR_RADIO_OFF:
                        Toast.makeText(getBaseContext(), "Radio off",
                                Toast.LENGTH_SHORT).show();
                        break;
                }
            }
        }, new IntentFilter(SENT));

        //---when the SMS has been delivered---
        registerReceiver(new BroadcastReceiver() {
            @Override
            public void onReceive(Context arg0, Intent arg1) {
                switch (getResultCode()) {
                    case Activity.RESULT_OK:
                        Toast.makeText(getBaseContext(), "SMS delivered",
                                Toast.LENGTH_SHORT).show();
                        break;
                    case Activity.RESULT_CANCELED:
                        Toast.makeText(getBaseContext(), "SMS not delivered",
                                Toast.LENGTH_SHORT).show();
                        break;
                }
            }
        }, new IntentFilter(DELIVERED));

        SmsManager sms = SmsManager.getDefault();
        sms.sendTextMessage(phoneNumber, null, message, sentPI, deliveredPI);
         */
    }
}
    /*
    public void Send(View view) {
        //message
        ed1given = findViewById(R.id.resultText);
        String text;
        text = ed1given.getText().toString();
        String messageToSend = text;
        //phone no

        ed3phone = findViewById(R.id.phoneText);
        String phoneNo;
        phoneNo = ed3phone.getText().toString();
        String number = "+48" + phoneNo;
        //sending
        SmsManager.getDefault().sendTextMessage(number, null, messageToSend, null, null);
        Toast.makeText(getApplicationContext(), "SMS sended successfully",
                Toast.LENGTH_LONG).show();
    }

     */