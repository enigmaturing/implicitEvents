package android.and08.impliziteintents;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;
import android.widget.Toast;

//AND08DD S.46 Aufg.2.1.: The broadcast we are listening to is the input method changed, insted
//of the new outgoing call. I solved this so, because the broadcast new outgoing call is not present
//anymore on Android 7.0
public class MyBroadcastReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {
        //We dont need the phoneNumber anymore for my solution
        //String phoneNumber = intent.getStringExtra(Intent.EXTRA_PHONE_NUMBER);
        Toast.makeText(context, "BROADCAST RECEIVED: INPUT METHOD CHANGED", Toast.LENGTH_LONG).show();
        Log.e(getClass().getSimpleName(),"---------->Broadcast received INPUT METHOD CHANGED<------------" );
    }
}
