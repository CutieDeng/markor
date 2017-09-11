package net.gsantner.markor.util;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.widget.Toast;

import net.gsantner.markor.R;
import net.gsantner.markor.activity.FilesystemListFragment;
import net.gsantner.markor.model.Constants;

import java.io.File;

public class RenameBroadcastReceiver extends BroadcastReceiver {

    private FilesystemListFragment filesystemListFragment;

    public RenameBroadcastReceiver(FilesystemListFragment filesystemListFragment) {
        super();
        this.filesystemListFragment = filesystemListFragment;
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        if (intent.getAction().equals(Constants.RENAME_DIALOG_TAG)) {
            String newName = intent.getStringExtra(Constants.RENAME_NEW_NAME);
            File sourceFile = new File(intent.getStringExtra(Constants.SOURCE_FILE));
            File targetFile = new File(sourceFile.getParent(), newName);

            if (targetFile.exists()) {
                Toast.makeText(context, context.getString(R.string.rename_error_target_already_exists), Toast.LENGTH_LONG).show();
                filesystemListFragment.finishActionMode();
                return;
            }

            if (sourceFile.renameTo(targetFile)) {
                Toast.makeText(context, context.getString(R.string.rename_success), Toast.LENGTH_LONG).show();
                filesystemListFragment.listFilesInDirectory(filesystemListFragment.getCurrentDir());
            } else {
                Toast.makeText(context, context.getString(R.string.rename_fail), Toast.LENGTH_LONG).show();
            }
            filesystemListFragment.finishActionMode();
        }
    }
}
