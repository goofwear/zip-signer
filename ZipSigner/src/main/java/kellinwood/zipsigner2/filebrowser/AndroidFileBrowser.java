package kellinwood.zipsigner2.filebrowser;

import android.app.Activity;
import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Bundle;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import java.io.File;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;
import java.util.regex.Pattern;

import kellinwood.zipsigner2.R;

/**
 * File/directory browser.
 * Rewritten to extend AppCompatActivity (was ListActivity) so it respects the app theme.
 * Added a "SELECT" button for directory mode and an "OUTPUT HERE" button for save mode.
 * Fixed file listing on Android 11+ (scoped storage).
 */
public class AndroidFileBrowser extends AppCompatActivity {

    public static final String DATA_KEY_START_PATH       = "startPath";
    public static final String DATA_KEY_REASON           = "reason";
    public static final String DATA_KEY_DIRECTORY_SELECT_MODE = "dirSelect";
    public static final String DATA_KEY_SAVE_MODE        = "saveMode";
    public static final String DATA_KEY_DEFAULT_FILENAME = "defaultFilename";

    private List<IconifiedText> directoryEntries = new ArrayList<>();
    private File currentDirectory = new File("/");
    private String reason = "";
    private boolean directorySelectionMode = false;
    private boolean saveMode = false;
    private String defaultFilename = "";
    private ListView listView;
    private Button selectButton;
    private TextView pathView;
    private static final File[] EMPTY = new File[0];

    private Pattern imagePattern, audioPattern, packagePattern, htmlPattern;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        imagePattern   = Pattern.compile(getString(R.string.fileEndingImage));
        audioPattern   = Pattern.compile(getString(R.string.fileEndingAudio));
        packagePattern = Pattern.compile(getString(R.string.fileEndingPackage));
        htmlPattern    = Pattern.compile(getString(R.string.fileEndingWebText));

        Intent i = getIntent();
        Bundle extras = i.getExtras();
        String startPath = extras != null ? extras.getString(DATA_KEY_START_PATH) : null;
        if (startPath == null) startPath = "/sdcard";
        reason = extras != null && extras.getString(DATA_KEY_REASON) != null
                ? extras.getString(DATA_KEY_REASON) : "";
        directorySelectionMode = extras != null && extras.getBoolean(DATA_KEY_DIRECTORY_SELECT_MODE, false);
        saveMode               = extras != null && extras.getBoolean(DATA_KEY_SAVE_MODE, false);
        defaultFilename        = extras != null && extras.getString(DATA_KEY_DEFAULT_FILENAME) != null
                ? extras.getString(DATA_KEY_DEFAULT_FILENAME) : "";

        // Build layout programmatically so we don't need a separate XML
        LinearLayout root = new LinearLayout(this);
        root.setOrientation(LinearLayout.VERTICAL);

        // Current path label
        pathView = new TextView(this);
        pathView.setPadding(16, 12, 16, 4);
        pathView.setTextSize(12);
        root.addView(pathView);

        // Reason/hint label
        if (!reason.isEmpty()) {
            TextView hint = new TextView(this);
            hint.setText(reason);
            hint.setPadding(16, 0, 16, 8);
            hint.setTextSize(12);
            root.addView(hint);
        }

        // File list
        listView = new ListView(this);
        listView.setLayoutParams(new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, 0, 1f));
        root.addView(listView);

        // Bottom bar: SELECT/OUTPUT HERE button (shown only in dir/save mode)
        if (directorySelectionMode || saveMode) {
            LinearLayout bar = new LinearLayout(this);
            bar.setOrientation(LinearLayout.HORIZONTAL);
            bar.setPadding(16, 8, 16, 16);
            bar.setGravity(Gravity.CENTER);

            selectButton = new Button(this);
            selectButton.setText(saveMode ? "SAVE HERE" : "SELECT THIS FOLDER");
            selectButton.setOnClickListener(v -> confirmCurrentDirectory());
            bar.addView(selectButton);
            root.addView(bar);
        }

        setContentView(root);

        listView.setOnItemClickListener((av, v, pos, id) -> onItemClick(pos));

        browseTo(new File(startPath));
    }

    private void onItemClick(int position) {
        String selected = directoryEntries.get(position).getText();
        if (selected.equals(getString(R.string.current_dir))) {
            if (directorySelectionMode || saveMode) {
                confirmCurrentDirectory();
            } else {
                browseTo(currentDirectory); // refresh
            }
        } else if (selected.equals(getString(R.string.up_one_level))) {
            if (currentDirectory.getParent() != null)
                browseTo(currentDirectory.getParentFile());
        } else {
            File clicked = new File(currentDirectory, selected);
            browseTo(clicked);
        }
    }

    private void confirmCurrentDirectory() {
        String path = currentDirectory.getAbsolutePath();
        if (saveMode && !defaultFilename.isEmpty()) {
            path = path + (path.endsWith("/") ? "" : "/") + defaultFilename;
        }
        Intent result = new Intent();
        result.setData(Uri.parse("file://" + path));
        setResult(RESULT_OK, result);
        finish();
    }

    static class FileSorter implements Comparator<File> {
        @Override
        public int compare(File o1, File o2) {
            if (o1.isDirectory() && !o2.isDirectory()) return -1;
            if (!o1.isDirectory() && o2.isDirectory()) return 1;
            return o1.getName().compareToIgnoreCase(o2.getName());
        }
    }
    private static final FileSorter FILE_SORTER = new FileSorter();

    private void browseTo(File target) {
        if (!target.exists()) {
            Toast.makeText(this, "Path does not exist: " + target, Toast.LENGTH_SHORT).show();
            return;
        }
        if (target.isDirectory()) {
            currentDirectory = target;
            pathView.setText(target.getAbsolutePath());
            setTitle(target.getAbsolutePath());

            File[] fileArray = target.listFiles();
            if (fileArray == null) {
                // listFiles() returns null if we can't read the directory
                Toast.makeText(this,
                        "Cannot read directory — storage permission may be needed",
                        Toast.LENGTH_LONG).show();
                fileArray = EMPTY;
            }

            Set<File> files = new TreeSet<>(FILE_SORTER);
            for (File f : fileArray) files.add(f);
            fill(files);
        } else {
            // It's a file — return it
            openFile(target);
        }
    }

    private void openFile(File file) {
        try {
            Intent result = new Intent();
            result.setData(Uri.parse("file://" + file.getAbsolutePath()));
            setResult(Activity.RESULT_OK, result);
            finish();
        } catch (Exception x) {
            Toast.makeText(this, x.getMessage(), Toast.LENGTH_LONG).show();
        }
    }

    private void fill(Set<File> files) {
        directoryEntries.clear();

        String dirPath = currentDirectory.getAbsolutePath();
        if (!dirPath.endsWith("/")) dirPath += "/";
        int dirLen = dirPath.length();

        // "." = current directory (tap to select in dir/save mode, or refresh)
        directoryEntries.add(new IconifiedText(
                getString(R.string.current_dir),
                getResources().getDrawable(R.drawable.folder)));

        // ".." = up one level
        if (currentDirectory.getParent() != null)
            directoryEntries.add(new IconifiedText(
                    getString(R.string.up_one_level),
                    getResources().getDrawable(R.drawable.uponelevel)));

        for (File f : files) {
            if (f.getName().startsWith(".")) continue; // skip hidden

            Drawable icon;
            if (f.isDirectory()) {
                icon = getResources().getDrawable(R.drawable.folder);
            } else if (directorySelectionMode || saveMode) {
                continue; // in dir/save mode, only show folders
            } else {
                String name = f.getName().toLowerCase();
                if      (imagePattern.matcher(name).find())   icon = getResources().getDrawable(R.drawable.image);
                else if (htmlPattern.matcher(name).find())    icon = getResources().getDrawable(R.drawable.webtext);
                else if (packagePattern.matcher(name).find()) icon = getResources().getDrawable(R.drawable.packed);
                else if (audioPattern.matcher(name).find())   icon = getResources().getDrawable(R.drawable.audio);
                else                                          icon = getResources().getDrawable(R.drawable.text);
            }

            String displayName = f.getAbsolutePath().substring(dirLen);
            if (f.isDirectory() && !displayName.endsWith("/")) displayName += "/";
            directoryEntries.add(new IconifiedText(displayName, icon));
        }

        IconifiedTextListAdapter adapter = new IconifiedTextListAdapter(this);
        adapter.setListItems(directoryEntries);
        listView.setAdapter(adapter);
    }
}
