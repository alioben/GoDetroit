package acb.godetroit.notsafe.godetroit.Utils;

import android.os.AsyncTask;
import android.os.Build;

/**
 * Created by benlalah on 26/01/17.
 */

public class AsyncTaskHelper {


    public static void executeAsyncTask(AsyncTask task){
        if (Build.VERSION.SDK_INT >= 11) {
            task.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
        } else {
            task.execute();
        }
    }
}
