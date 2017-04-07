package acb.godetroit.notsafe.godetroit.Utils;

import android.content.Context;
import android.os.AsyncTask;
import android.widget.ProgressBar;

/**
 * Created by benlalah on 26/01/17.
 */

public class ScoreUpTask extends AsyncTask<Integer, Void, Void> {

    @Override
    protected Void doInBackground(Integer... params) {
        JSONHelper.scoreUp(params[0]);
        return null;
    }

    @Override
    protected void onPostExecute(Void danger) {

    }

    @Override
    protected void onPreExecute() {
    }

}
