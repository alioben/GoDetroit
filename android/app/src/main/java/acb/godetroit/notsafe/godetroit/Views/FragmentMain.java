package acb.godetroit.notsafe.godetroit.Views;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.annotation.StringDef;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.ImageButton;
import android.widget.RadioButton;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.common.GooglePlayServicesNotAvailableException;
import com.google.android.gms.common.GooglePlayServicesRepairableException;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.location.places.Place;
import com.google.android.gms.location.places.ui.PlaceAutocomplete;
import com.google.android.gms.maps.model.LatLng;

import acb.godetroit.notsafe.godetroit.R;
import acb.godetroit.notsafe.godetroit.Utils.GPSTracker;

import static android.app.Activity.RESULT_CANCELED;
import static android.app.Activity.RESULT_OK;

public class FragmentMain extends Fragment {

    static final int PERMISSION_WATCH_ME = 1;
    static final String[] WATCH_PERMISSIONS = new String[]{
            android.Manifest.permission.READ_SMS,
            android.Manifest.permission.SEND_SMS};

    // Autocomplete feature
    static final int PLACE_AUTOCOMPLETE_REQUEST_CODE = 2;
    private TextView fromBox, toBox;
    private int requestBox;
    private LatLng originPlace, destPlace;

    // Find Path
    private Button findPath;
    private ImageButton findPathBike, findPathWalk;
    private ImageButton findPathCar, findPathBus;
    private CheckBox myPosition;
    private GPSTracker gps;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_main, container, false);

        gps = new GPSTracker(getActivity());

        // Initializing the ediboxes
        fromBox = (TextView) view.findViewById(R.id.from);
        toBox = (TextView) view.findViewById(R.id.to);
        fromBox.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(myPosition.isChecked()){
                    fromBox.setText("");
                    originPlace = null;
                    myPosition.setChecked(false);
                }
                requestBox = 1;
                findPlace();
            }
        });

        toBox.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                requestBox = 2;
                findPlace();
            }
        });

        // Checkbox
        myPosition = (CheckBox) view.findViewById(R.id.checkPosition);
        myPosition.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                if(b) {
                    originPlace = new LatLng(gps.getLatitude(), gps.getLongitude());
                    fromBox.setText("My Position");
                }else {
                    originPlace = null;
                    fromBox.setText("");
                }
            }
        });

        // Initialiing buttons
        findPathBike = (ImageButton) view.findViewById(R.id.findPathBike);
        findPathWalk = (ImageButton) view.findViewById(R.id.findPathWalk);
        findPathCar = (ImageButton) view.findViewById(R.id.findPathDrive);
        findPathBus = (ImageButton) view.findViewById(R.id.findPathTransit);

        findPathCar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startPathActivity(PathActivity.TRAVELING.DRIVING);
            }
        });
        findPathWalk.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startPathActivity(PathActivity.TRAVELING.WALKING);
            }
        });
        findPathBus.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startPathActivity(PathActivity.TRAVELING.TRANSIT);
            }
        });
        findPathBike.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startPathActivity(PathActivity.TRAVELING.BIKING);
            }
        });

        return view;
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == PLACE_AUTOCOMPLETE_REQUEST_CODE) {
            if (resultCode == RESULT_OK) {
                Place place = PlaceAutocomplete.getPlace(getActivity(), data);
                //Log.i("PLACE", "Place: " + place.getName());
                if(requestBox == 1) {
                    fromBox.setText(place.getName());
                    originPlace = place.getLatLng();
                }else if(requestBox == 2) {
                    toBox.setText(place.getName());
                    destPlace = place.getLatLng();
                }
            } else if (resultCode == PlaceAutocomplete.RESULT_ERROR) {
                Status status = PlaceAutocomplete.getStatus(getActivity(), data);
                // TODO: Handle the error.
                //Log.i("PLACE", status.getStatusMessage());

            } else if (resultCode == RESULT_CANCELED) {
                // The user canceled the operation.
            }
        }
    }

    public void findPlace() {
        try {
            Intent intent =
                    new PlaceAutocomplete.IntentBuilder(PlaceAutocomplete.MODE_FULLSCREEN)
                            .build(getActivity());
            startActivityForResult(intent, PLACE_AUTOCOMPLETE_REQUEST_CODE);
        } catch (GooglePlayServicesRepairableException e) {
            // TODO: Handle the error.
        } catch (GooglePlayServicesNotAvailableException e) {
            // TODO: Handle the error.
        }
    }

    public void startPathActivity(PathActivity.TRAVELING mode){
        if(originPlace == null || destPlace == null)
            Toast.makeText(getActivity(), "Please select an origin and a destination.", Toast.LENGTH_SHORT).show();
        else {
            Intent intent = new Intent(getActivity(), PathActivity.class);
            intent.putExtra("startLat", originPlace.latitude);
            intent.putExtra("startLon", originPlace.longitude);
            intent.putExtra("destLat", destPlace.latitude);
            intent.putExtra("destLon", destPlace.longitude);
            intent.putExtra("mode", mode);
            startActivity(intent);
        }
    }
}
