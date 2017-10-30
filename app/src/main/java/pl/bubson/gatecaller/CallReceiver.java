package pl.bubson.gatecaller;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.net.Uri;
import android.provider.ContactsContract;
import android.support.v4.app.ActivityCompat;
import android.telephony.TelephonyManager;
import android.util.Log;
import android.widget.Toast;

import com.android.internal.telephony.ITelephony;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

/**
 * Created by Kuba on 2016-10-16.
 */
public class CallReceiver extends PhonecallReceiver {

    private static final String TAG = "PhonecallReceiver";
    private static CallReceiver instance = null;
    Context currentContext;

    protected CallReceiver() {
        // Exists only to defeat instantiation.
    }

    public static CallReceiver getInstance() {
        if (instance == null) {
            instance = new CallReceiver();
        }
        return instance;
    }

    @Override
    protected void onIncomingCallReceived(Context ctx, String number, Date start) {
        currentContext = ctx;
        Toast.makeText(currentContext, "Dzwoni numer: " + number, Toast.LENGTH_SHORT).show();
        try {
            if (isNumberInContacts(number)) {
//            setTimerAndDisconnectCall(500);
                disconnectCall();
                setTimerAndCallGateNumber(1000);
            } else {
                Toast.makeText(currentContext, "Numeru nie ma na liście kontaktów", Toast.LENGTH_SHORT).show();
            }
        } catch (Exception e) {
            e.printStackTrace();
            Toast.makeText(currentContext, "Coś poszło nie tak!", Toast.LENGTH_SHORT).show();
        }
    }

    private boolean isNumberInContacts(String number) {
        List<String> contactsNumbers = new ArrayList<>();
        String numberWithoutCountryCode = number.replaceFirst("\\+48","");
        Cursor phones = currentContext.getContentResolver().query(ContactsContract.CommonDataKinds.Phone.CONTENT_URI, null, null, null, null);
        if (phones != null) {
            try {
                while (phones.moveToNext()) {
                    String num = phones.getString(phones.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER))
                            .replaceFirst("\\+48","")
                            .replaceAll("-","")
                            .replaceAll(" ","")
                            .replaceFirst("0048","");
                    contactsNumbers.add(num);
                }
            } finally {
                phones.close();
            }
        }
        Collections.sort(contactsNumbers);
        Log.v(TAG, contactsNumbers.toString());
        return contactsNumbers.contains(numberWithoutCountryCode);
    }

    private void setTimerAndDisconnectCall(long milis) {
        Timer timer = new Timer();
        timer.schedule(new TimerTask() {
            @Override
            public void run() {
                disconnectCall();
            }
        }, milis);
    }

    private void disconnectCall() {
        TelephonyManager tm = (TelephonyManager) currentContext.getSystemService(Context.TELEPHONY_SERVICE);
        try {
            Class<?> c = Class.forName(tm.getClass().getName());
            Method m = c.getDeclaredMethod("getITelephony");
            m.setAccessible(true);
            ITelephony telephonyService = (ITelephony) m.invoke(tm);
            telephonyService.endCall();
        } catch (Exception e) {
            e.printStackTrace();
        }
        Log.v(TAG, "disconnectCall() finished");
    }

    private void setTimerAndCallGateNumber(long milis) {
        Timer timer = new Timer();
        timer.schedule(new TimerTask() {
            @Override
            public void run() {
                callGateNumber();
            }
        }, milis);
    }

    private void callGateNumber() {
        Log.v(TAG, "callGateNumber() started");
        Intent callIntent = new Intent(Intent.ACTION_CALL);
        callIntent.setData(Uri.parse("tel:789484945"));
        if (ActivityCompat.checkSelfPermission(currentContext, Manifest.permission.CALL_PHONE) != PackageManager.PERMISSION_GRANTED) {
            return;
        }
        callIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        currentContext.startActivity(callIntent);
        Log.v(TAG, "callGateNumber() finished");
    }

    @Override
    protected void onIncomingCallAnswered(Context ctx, String number, Date start) {
        //
    }

    @Override
    protected void onIncomingCallEnded(Context ctx, String number, Date start, Date end) {
        //
    }

    @Override
    protected void onOutgoingCallStarted(Context ctx, String number, Date start) {
        //
    }

    @Override
    protected void onOutgoingCallEnded(Context ctx, String number, Date start, Date end) {
        //
    }

    @Override
    protected void onMissedCall(Context ctx, String number, Date start) {
        //
    }
}
