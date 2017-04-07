package acb.godetroit.notsafe.godetroit.Utils;

import android.content.Context;
import android.graphics.Color;
import android.graphics.Typeface;
import android.net.wifi.p2p.WifiP2pManager;
import android.support.v4.content.ContextCompat;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import org.w3c.dom.Text;

import java.util.ArrayList;
import java.util.List;

import acb.godetroit.notsafe.godetroit.Entities.Alert;
import acb.godetroit.notsafe.godetroit.R;

/**
 * Created by benlalah on 25/01/17.
 */

public class AlertAdapter extends BaseAdapter {

    public List<Alert> data;
    private ArrayList<Alert> arraylist;
    private Context context;

    public AlertAdapter(Context context, List<Alert> alerts) {
        this.data = alerts;
        this.context = context;
        this.arraylist = new ArrayList<>();
        this.arraylist.addAll(data);
    }

    @Override
    public int getCount() {
        return data.size();
    }

    @Override
    public Object getItem(int i) {
        return data.get(i);
    }

    @Override
    public long getItemId(int i) {
        return i;
    }

    @Override
    public View getView(int i, View convertView, ViewGroup viewGroup) {
        View view = convertView;
        if (view == null) {
            LayoutInflater li = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            view = li.inflate(R.layout.alert_layout, null);
        } else
            view = convertView;

        final TextView distance = (TextView) view.findViewById(R.id.distance);
        final TextView alert = (TextView) view.findViewById(R.id.alert);
        final TextView score = (TextView) view.findViewById(R.id.score);
        final ImageView score_up = (ImageView) view.findViewById(R.id.up_score);


        final Alert data = (Alert) this.data.get(i);
        alert.setText(data.getContent());
        score.setText(data.getFormattedScore());
        distance.setText(data.getDistance());

        if(data.getDanger() >= 0.66) {
            alert.setTextColor(Color.rgb(255, 0, 0));
            alert.setTypeface(null, Typeface.BOLD);
        } else if(data.getDanger() >= 0.33) {
            alert.setTextColor(Color.rgb(250, 180, 0));
            alert.setTypeface(null, Typeface.BOLD);
        } else
            alert.setTextColor(Color.rgb(0, 0, 0));

        score_up.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                data.addScore();
                score_up.setEnabled(false);
                score.setText(data.getFormattedScore());
                score_up.setImageDrawable(context.getResources().getDrawable(R.drawable.up_disabled));
            }
        });

        view.setTag(data);
        return view;
    }
}
