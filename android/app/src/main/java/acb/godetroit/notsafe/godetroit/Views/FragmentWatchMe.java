package acb.godetroit.notsafe.godetroit.Views;

import android.app.Activity;
import android.app.ActivityManager;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.provider.Contacts;
import android.provider.ContactsContract;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.telephony.SmsManager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.google.gson.Gson;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Timer;
import java.util.TimerTask;

import acb.godetroit.notsafe.godetroit.Entities.Contact;
import acb.godetroit.notsafe.godetroit.R;
import acb.godetroit.notsafe.godetroit.Utils.GPSTracker;
import acb.godetroit.notsafe.godetroit.Utils.SMSSender;

import static android.app.Activity.RESULT_OK;
import static android.content.Context.MODE_PRIVATE;


public class FragmentWatchMe extends Fragment {

    // Managin the service
    private Contact selected_contact;

    // Views controllers
    private RelativeLayout layoutWatchMe;
    private TextView watchmeText, textGuideContacts;
    private Button stopButt;
    private Button select_con;

    // Managing the contacts
    private static  int PICK_CONTACT = 1;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view =  inflater.inflate(R.layout.fragment_watch_me, container, false);

        layoutWatchMe = (RelativeLayout) view.findViewById(R.id.layout_watchme);
        textGuideContacts = (TextView) view.findViewById(R.id.textGuideContacts);
        watchmeText = (TextView) view.findViewById(R.id.watchme_text);
        stopButt = (Button) view.findViewById(R.id.stop_butt);
        select_con = (Button) view.findViewById(R.id.select_con);
        select_con.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent i = new Intent(Intent.ACTION_PICK, ContactsContract.CommonDataKinds.Phone.CONTENT_URI);
                startActivityForResult(i, PICK_CONTACT);
            }
        });

        return view;
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        // Check if service is already running
        if(isMyServiceRunning(SMSSender.class)){
            Log.i("SMSSender", "Service is running.");
            selected_contact = getContact();
            layoutWatchMe.setVisibility(View.VISIBLE);
            select_con.setVisibility(View.INVISIBLE);
            textGuideContacts.setVisibility(View.INVISIBLE);
        } else
            Log.i("SMSSender", "Service is not runing");

        // Listener for the stop button
        stopButt.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                destructContact();
                Intent intent = new Intent(getActivity(), SMSSender.class);
                getActivity().stopService(intent);
                select_con.setVisibility(View.VISIBLE);
                layoutWatchMe.setVisibility(View.INVISIBLE);
                textGuideContacts.setVisibility(View.VISIBLE);
            }
        });
    }

    /****** WATCH ME FEATURES ******/

    public void watchMe(){
        //Save the contact in the preferences + start the service
        saveContact(selected_contact);
        Intent intent = new Intent(getActivity(), SMSSender.class);
        intent.putExtra("contact", selected_contact);
        getActivity().startService(intent);

        // Show the view
        //listView.setVisibility(View.INVISIBLE);
        select_con.setVisibility(View.INVISIBLE);
        layoutWatchMe.setVisibility(View.VISIBLE);
        textGuideContacts.setVisibility(View.INVISIBLE);
        watchmeText.setText("Your real-time location is been transmitted to "+selected_contact.getName()+".");
    }

    /****** PERMISSION + BASE ADAPTER + READ CONTACTS ******/

    @Override
    public void onActivityResult(int reqCode, int resultCode, Intent data) {
        super.onActivityResult(reqCode, resultCode, data);
        if (reqCode == PICK_CONTACT && resultCode == RESULT_OK) {
            Uri contactUri = data.getData();
            Cursor cursor = getActivity().getContentResolver().query(contactUri, null, null, null, null);
            cursor.moveToFirst();
            int columnNumber = cursor.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER);
            int columnName = cursor.getColumnIndex(ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME);

            String name = cursor.getString(columnName);
            String number = cursor.getString(columnNumber);

            selected_contact  = new Contact();
            selected_contact.setName(name);
            selected_contact.setPhone(number);

            if(selected_contact != null)
                watchMe();

        }
    }

    private void saveContact(Contact contact){
        SharedPreferences mPrefs = getActivity().getPreferences(MODE_PRIVATE);
        SharedPreferences.Editor prefsEditor = mPrefs.edit();
        Gson gson = new Gson();
        String json = gson.toJson(contact);
        prefsEditor.putString("watchme_contact", json);
        prefsEditor.commit();
    }

    private Contact getContact(){
        SharedPreferences mPrefs = getActivity().getPreferences(MODE_PRIVATE);
        Gson gson = new Gson();
        String json = mPrefs.getString("watchme_contact", "");
        if(json == "")
            return null;
        Contact obj = gson.fromJson(json, Contact.class);
        return obj;
    }

    private void destructContact(){
        SharedPreferences mySPrefs = PreferenceManager.getDefaultSharedPreferences(getActivity());
        SharedPreferences.Editor editor = mySPrefs.edit();
        editor.remove("watchme_contact");
        editor.apply();
    }

    private boolean isMyServiceRunning(Class<?> serviceClass) {
        ActivityManager manager = (ActivityManager) getActivity().getSystemService(Context.ACTIVITY_SERVICE);
        for (ActivityManager.RunningServiceInfo service : manager.getRunningServices(Integer.MAX_VALUE)) {
            if (serviceClass.getName().equals(service.service.getClassName())) {
                return true;
            }
        }
        return false;
    }

}


