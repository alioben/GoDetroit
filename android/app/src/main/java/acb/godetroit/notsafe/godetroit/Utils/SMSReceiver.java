package acb.godetroit.notsafe.godetroit.Utils;

import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.support.v4.app.NotificationCompat;
import android.telephony.SmsMessage;

import acb.godetroit.notsafe.godetroit.Entities.Contact;
import acb.godetroit.notsafe.godetroit.Views.WatchActivity;

public class SMSReceiver extends BroadcastReceiver {

    public static boolean isWatching = false;

    private Contact friend_to_watch;

    private SharedPreferences preferences;

    public SMSReceiver(){
        super();
    }

    public String getName(Context context, String phone){
        String name = "";
        Uri lookupUri = Uri.withAppendedPath(ContactsContract.PhoneLookup.CONTENT_FILTER_URI, Uri.encode(phone));
        Cursor c = context.getContentResolver().query(lookupUri, new String[]{ContactsContract.Data.DISPLAY_NAME},null,null,null);
        try {
            c.moveToFirst();
            name = c.getString(0);
        } catch (Exception e) {
            e.printStackTrace();
        }finally{
            c.close();
            return name;
        }
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        if(isWatching)
            return;
        if(intent.getAction().equals("android.provider.Telephony.SMS_RECEIVED")){
            Bundle bundle = intent.getExtras();
            SmsMessage[] msgs = null;
            String msg_from;
            if (bundle != null){
                try{
                    Object[] pdus = (Object[]) bundle.get("pdus");
                    msgs = new SmsMessage[pdus.length];
                    for(int i=0; i<msgs.length; i++){
                        msgs[i] = SmsMessage.createFromPdu((byte[])pdus[i]);

                        msg_from = msgs[i].getOriginatingAddress();
                        String msgBody = msgs[i].getMessageBody();
                        String user_from = " "+getName(context, msg_from);

                        if(msgBody.length() > 10 && msgBody.substring(0, 10).equals("#smsalert#")){
                            NotificationCompat.Builder builder = new NotificationCompat.Builder(context)
                                    .setSmallIcon(android.R.drawable.ic_dialog_alert)
                                    .setContentTitle("Your friend"+user_from+" needs help!")
                                    .setContentText("Click here to follow your friend"+user_from+".");

                            friend_to_watch = new Contact();
                            friend_to_watch.setName(user_from);
                            friend_to_watch.setPhone(msg_from);

                            Intent notificationIntent = new Intent(context, WatchActivity.class);
                            notificationIntent.putExtra("contact", friend_to_watch);
                            PendingIntent contentIntent = PendingIntent.getActivity(context, 0, notificationIntent,
                                    PendingIntent.FLAG_UPDATE_CURRENT);
                            builder.setContentIntent(contentIntent);

                            // Add as notification
                            NotificationManager manager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
                            manager.notify(0, builder.build());
                        }
                    }
                }catch(Exception e){
                    e.printStackTrace();
                }
            }
        }
    }
}