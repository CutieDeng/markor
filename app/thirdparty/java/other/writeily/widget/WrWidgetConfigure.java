/*#######################################################
 * Copyright (c) 2014 Jeff Martin
 * Copyright (c) 2015 Pedro Lafuente
 * Copyright (c) 2017-2022 Gregor Santner
 *
 * Licensed under the MIT license.
 * You can get a copy of the license text here:
 *   https://opensource.org/licenses/MIT
###########################################################*/
package other.writeily.widget;

import android.appwidget.AppWidgetManager;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Parcelable;
import android.support.annotation.NonNull;
import android.support.v4.app.FragmentManager;
import android.support.v7.app.AppCompatActivity;

import net.gsantner.markor.R;
import net.gsantner.markor.ui.FilesystemViewerCreator;
import net.gsantner.markor.util.AppSettings;
import net.gsantner.markor.util.PermissionChecker;
import net.gsantner.opoc.ui.FilesystemViewerData;
import net.gsantner.opoc.util.StringUtils;

import java.io.File;

public class WrWidgetConfigure extends AppCompatActivity {
    private int _appWidgetId = AppWidgetManager.INVALID_APPWIDGET_ID;

    private static final String WIDGET_PREF_NAME = "MARKOR_WIDGET_PREF";
    private static final String WIDGET_PREFIX = "WIDGET_PATH";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        final Bundle extras = getIntent().getExtras();
        if (extras != null) {
            _appWidgetId = extras.getInt(AppWidgetManager.EXTRA_APPWIDGET_ID, AppWidgetManager.INVALID_APPWIDGET_ID);
        }

        if (_appWidgetId != AppWidgetManager.INVALID_APPWIDGET_ID) {
            showSelectionDialog();
        }
    }

    @Override
    protected void onStop() {
        // Update when done
        WrMarkorWidgetProvider.updateLauncherWidgets();
        super.onStop();
    }

    // only runs for a valid id
    private void showSelectionDialog() {
        final PermissionChecker permc = new PermissionChecker(this);
        if (permc.mkdirIfStoragePermissionGranted()) {
            final FragmentManager fragManager = getSupportFragmentManager();
            FilesystemViewerCreator.showFolderDialog(new FilesystemViewerData.SelectionListenerAdapter() {
                @Override
                public void onFsViewerSelected(String request, File file, final Integer lineNumber) {
                    setWidgetDirectory(WrWidgetConfigure.this, _appWidgetId, file);
                    setResult(RESULT_OK, new Intent().putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, _appWidgetId));
                    finish();
                }

                @Override
                public void onFsViewerConfig(FilesystemViewerData.Options dopt) {
                    dopt.titleText = R.string.select_folder;
                    dopt.rootFolder = AppSettings.get().getNotebookDirectory();
                }
            }, fragManager, this);
        }
    }

    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (new PermissionChecker(this).checkPermissionResult(requestCode, permissions, grantResults)) {
            showSelectionDialog();
        } else {
            finish();
        }
    }

    public static File getWidgetDirectory(final Context context, int id) {
        String path;

        // Try new method first
        path = context.getSharedPreferences(WIDGET_PREF_NAME, MODE_PRIVATE).getString(WIDGET_PREFIX + id, null);
        if (path != null) {
            return new File(path);
        }

        // Try old method next
        path = context.getSharedPreferences("" + id, MODE_PRIVATE).getString(WIDGET_PREFIX + id, null);
        if (path != null) {
            return new File(path);
        }

        // Fallback
        return new AppSettings(context).getNotebookDirectory();
    }

    public static void setWidgetDirectory(final Context context, int id, final File dir) {
        context.getSharedPreferences(WIDGET_PREF_NAME, MODE_PRIVATE).edit().putString(WIDGET_PREFIX + id, dir.getPath()).apply();
    }
}
