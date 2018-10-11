package com.seikoshadow.apps.textthrough.Database;
import android.app.Application;
import android.os.AsyncTask;

import java.util.List;

import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;

public class AlertViewModel extends AndroidViewModel {
    private String TAG = "AlertViewModel";
    private AppDatabase db;
    private final LiveData<List<Alert>> alerts;

    public AlertViewModel(Application application) {
        super(application);
        db = AppDatabase.getInstance(this.getApplication());

        alerts = db.alertModel().getAll();
    }

    public LiveData<List<Alert>> getAlertsList() {
        return alerts;
    }

    public void deleteItem(Alert alert) {
        new deleteAsyncTask(db).execute(alert);
    }

    private static class deleteAsyncTask extends AsyncTask<Alert, Void, Void> {
        private AppDatabase db;

        deleteAsyncTask(AppDatabase appDatabase) {
            db = appDatabase;
        }

        @Override
        protected Void doInBackground(final Alert... params) {
            db.alertModel().delete(params[0]);
            return null;
        }
    }
}
