/*
 * Copyright (C) 2010 Ken Ellinwood.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package kellinwood.zipsigner2;

import androidx.appcompat.app.AppCompatActivity;
import android.app.Activity;
import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.preference.PreferenceManager;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import java.io.File;

import kellinwood.logging.android.AndroidLogManager;
import kellinwood.logging.android.AndroidLogger;
import kellinwood.security.zipsigner.ZipSigner;


/** App for signing zip, apk, and/or jar files on an Android device. 
 *  This activity allows the input/output files to be selected and shows
 *  how to invoke the ZipSignerActivity to perform the actual work.
 *  
 */
public class ZipPickerActivity extends AppCompatActivity {


    protected static final int REQUEST_CODE_PICK_FILE_TO_OPEN = 1;
    protected static final int REQUEST_CODE_PICK_FILE_TO_SAVE = 2;
    protected static final int REQUEST_CODE_PICK_INOUT_FILE = 3;

    protected static final int REQUEST_CODE_SIGN_FILE = 80701;
    protected static final int REQUEST_CODE_MANAGE_KEYS = 80702;

    private static final String PREFERENCE_IN_FILE = "input_file";
    private static final String PREFERENCE_OUT_FILE = "output_file";
    private static final String PREFERENCE_KEY_INDEX = "key_index";
    private static final String PREFERENCE_ALG_INDEX = "alg_idx";

    AndroidLogger logger = null;
    KeyListSpinnerAdapter keyModeSpinnerAdapter = null;

    Spinner algorithmSpinner = null;
    ArrayAdapter algorithmSpinnerAdapter = null;
    ArrayAdapter sha1WithRsaSpinnerAdapter = null;
    ArrayAdapter allAlgorithmsSpinnerAdapter = null;

    static {
        AndroidLogManager.overrideCategory("ZipSigner");
    }

    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.zip_picker);

        logger = AndroidLogManager.getAndroidLogger(ZipPickerActivity.class);
        logger.setToastContext(getBaseContext());
        logger.setInfoToastEnabled(true);

        // Request storage permission on Android 11+ so the file browser can list files.
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.R) {
            if (!android.os.Environment.isExternalStorageManager()) {
                try {
                    android.content.Intent intent = new android.content.Intent(
                            android.provider.Settings.ACTION_MANAGE_APP_ALL_FILES_ACCESS_PERMISSION,
                            android.net.Uri.parse("package:" + getPackageName()));
                    startActivity(intent);
                } catch (Exception e) {
                    android.content.Intent intent = new android.content.Intent(
                            android.provider.Settings.ACTION_MANAGE_ALL_FILES_ACCESS_PERMISSION);
                    startActivity(intent);
                }
            }
        } else if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.M) {
            if (checkSelfPermission(android.Manifest.permission.READ_EXTERNAL_STORAGE)
                    != android.content.pm.PackageManager.PERMISSION_GRANTED) {
                requestPermissions(new String[]{
                        android.Manifest.permission.READ_EXTERNAL_STORAGE,
                        android.Manifest.permission.WRITE_EXTERNAL_STORAGE}, 1);
            }
        }

        Button createButton = (Button)findViewById(R.id.SignButton);
        createButton.setOnClickListener( new OnClickListener() {
            public void onClick( View view) {
                invokeZipSignerActivity();
            }
        });

        String extStorageDir = Environment.getExternalStorageDirectory().toString();
        // Strip /mnt from /sdcard
        if (extStorageDir.startsWith("/mnt/sdcard")) extStorageDir = extStorageDir.substring(4);
        
        final SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getBaseContext());
        String inputFile = prefs.getString(PREFERENCE_IN_FILE, extStorageDir + "/unsigned.zip");
        String outputFile = prefs.getString(PREFERENCE_OUT_FILE, extStorageDir + "/signed.zip");
        int keyIndex = prefs.getInt(PREFERENCE_KEY_INDEX, 0);

        EditText inputText = (EditText)findViewById(R.id.InFileEditText);
        inputText.setText( inputFile);

        EditText outputText = (EditText)findViewById(R.id.OutFileEditText);
        outputText.setText( outputFile);

        Button button = (Button) findViewById(R.id.OpenPickButton);
        button.setOnClickListener(new View.OnClickListener() {
            public void onClick(View arg0) {
                pickInputFile();
            }
        });

        button = (Button) findViewById(R.id.SaveAsPickButton);
        button.setOnClickListener(new View.OnClickListener() {
            public void onClick(View arg0) {
                pickOutputFile();
            }
        });      
        
        button = (Button) findViewById(R.id.InOutPickButton);
        button.setOnClickListener(new View.OnClickListener() {
            public void onClick(View arg0) {
                pickInputOutputFiles();
            }
        });        
        
        Spinner spinner = (Spinner) findViewById(R.id.KeyModeSpinner);
        keyModeSpinnerAdapter = KeyListSpinnerAdapter.createInstance(this, android.R.layout.simple_spinner_item);
        keyModeSpinnerAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinner.setAdapter(keyModeSpinnerAdapter);
        if (keyIndex >= keyModeSpinnerAdapter.getCount()) keyIndex = 0;
        spinner.setSelection(keyIndex);
        spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view,
                                       int position, long id) {
                SharedPreferences.Editor editor = prefs.edit();
                editor.putInt( PREFERENCE_KEY_INDEX, position);
                editor.commit();
                updateAlgorithmSpinner(position);
            }
            @Override
            public void onNothingSelected(AdapterView<?> adapter) {}
        });

        algorithmSpinner = (Spinner) findViewById(R.id.CertSignatureAlgorithm);
        sha1WithRsaSpinnerAdapter = ArrayAdapter.createFromResource(this,
            R.array.Sha1WithRsaAlgorithmArray, android.R.layout.simple_spinner_item);
        sha1WithRsaSpinnerAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        allAlgorithmsSpinnerAdapter = ArrayAdapter.createFromResource(this,
            R.array.AllShaWithRsaAlgorithmsArray, android.R.layout.simple_spinner_item);
        allAlgorithmsSpinnerAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);

        updateAlgorithmSpinner(keyIndex);


        algorithmSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view,
                                       int position, long id) {
                if (algorithmSpinnerAdapter.getCount() > 1) {
                    SharedPreferences.Editor editor = prefs.edit();
                    editor.putInt( PREFERENCE_ALG_INDEX, position);
                    editor.commit();
                }
            }
            @Override
            public void onNothingSelected(AdapterView<?> adapter) {}
        });
    }

    private void updateAlgorithmSpinner(int keyIndex) {
        int selection = 0;
        ArrayAdapter newAdapter = null;
        if (keyIndex < ZipSigner.SUPPORTED_KEY_MODES.length) {
            if (algorithmSpinnerAdapter != sha1WithRsaSpinnerAdapter) {
                newAdapter = sha1WithRsaSpinnerAdapter;
                selection = 0;
                TextView tv = (TextView)findViewById(R.id.SignatureAlgorithmTextView);
                tv.setVisibility(View.INVISIBLE);
                algorithmSpinner.setVisibility(View.INVISIBLE);
            }
        } else {
            if (algorithmSpinnerAdapter != allAlgorithmsSpinnerAdapter) {
                newAdapter = allAlgorithmsSpinnerAdapter;
                SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getBaseContext());
                selection = prefs.getInt(PREFERENCE_ALG_INDEX, 0);
                TextView tv = (TextView)findViewById(R.id.SignatureAlgorithmTextView);
                tv.setVisibility(View.VISIBLE);
                algorithmSpinner.setVisibility(View.VISIBLE);
            }
        }
        if (newAdapter != null) {
            algorithmSpinnerAdapter = newAdapter;
            algorithmSpinner.setAdapter(algorithmSpinnerAdapter);
            algorithmSpinner.setSelection(selection);
        }
    }

    private String getInputFilename() {
        return ((EditText)findViewById(R.id.InFileEditText)).getText().toString();
    }

    private String getOutputFilename() {
        return ((EditText)findViewById(R.id.OutFileEditText)).getText().toString();
    }

    private void invokeZipSignerActivity() {
        try {

            String inputFile = getInputFilename();
            String outputFile = getOutputFilename();

            // Save the input,output file names to preferences
            SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getBaseContext());
            SharedPreferences.Editor editor = prefs.edit();
            editor.putString(PREFERENCE_IN_FILE, inputFile);
            editor.putString(PREFERENCE_OUT_FILE, outputFile);            
            editor.commit();            

            // Refuse to do anything if the external storage device is not writable (external storage = /sdcard).
            if (Environment.MEDIA_MOUNTED_READ_ONLY.equals(Environment.getExternalStorageState())) {
                logger.error( getResources().getString(R.string.ExtStorageIsReadOnly));
                return;
            }

            // Check if v2/v3 signing is enabled and input is an APK
            android.widget.CheckBox v2v3Checkbox = findViewById(R.id.EnableV2V3Checkbox);
            boolean enableV2V3 = v2v3Checkbox != null && v2v3Checkbox.isChecked();
            boolean isApk = inputFile.toLowerCase().endsWith(".apk");

            KeyEntry keyEntry = (KeyEntry)((Spinner)this.findViewById(R.id.KeyModeSpinner)).getSelectedItem();
            
            // Use modern APK signer for v2/v3 if enabled and it's an APK
            if (enableV2V3 && isApk && !keyEntry.isBuiltIn()) {
                signApkWithV2V3(inputFile, outputFile, keyEntry);
                return;
            }

            // Otherwise use the old v1-only signing path
            // Launch the ZipSignerActivity to perform the signature operation.
            Intent i = new Intent("kellinwood.zipsigner.action.SIGN_FILE");

            // Required parameters - input and output files.  The filenames must be different (e.g., 
            // you can't sign the file and save the output to itself).
            i.putExtra("inputFile", inputFile);
            i.putExtra("outputFile", outputFile);

            // Optional parameters...

            // keyMode defaults to "testkey" if not specified
            logger.debug(keyEntry.getDisplayName() + ", id="+keyEntry.getId());
            i.putExtra("keyMode", keyEntry.getDisplayName());

            // If "showProgressItems" is true, then the ZipSignerActivity displays the names of files in the
            // zip as they are generated/copied during the signature process.
            i.putExtra("showProgressItems", "true");

            // Set the result code used to indicate that auto-key selection failed.  This will default to
            // RESULT_FIRST_USER if not set (same code used to signal an error).
            i.putExtra("autoKeyFailRC", RESULT_FIRST_USER+1);

            // Defaults to "SHA1withRSA" if not specified, and is ignored for the built-in keys
            String signatureAlgorithm = (String)((Spinner)this.findViewById(R.id.CertSignatureAlgorithm)).getSelectedItem();
            i.putExtra("signatureAlgorithm", signatureAlgorithm);

            // If two non-builtin keys have the same name, using the ID ensures that we sign with the one selected.
            i.putExtra("customKeyId", keyEntry.getId());

            // Activity is started and the result is returned via a call to onActivityResult(), below.
            startActivityForResult(i, REQUEST_CODE_SIGN_FILE);

        }
        catch (Throwable x) {
            logger.error( x.getClass().getName() + ": " + x.getMessage(), x);
        }

    }


    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.options_menu, menu);
        return true;
    }

    /* Handles item selections */
    public boolean onOptionsItemSelected(MenuItem item) 
    {
        int id = item.getItemId();
        if (id == R.id.MenuItemShowHelp) {
            String targetURL = getString(R.string.AboutZipSignerDocUrl);
            Intent wsi = new Intent( Intent.ACTION_VIEW, Uri.parse(targetURL));
            startActivity(wsi);
            return true;
        } else if (id == R.id.MenuItemManageKeys) {
            Intent mki = new Intent("kellinwood.zipsigner.action.MANAGE_KEYS");
            startActivityForResult(mki, REQUEST_CODE_MANAGE_KEYS);
            return true;
        } else if (id == R.id.MenuItemAbout) {
            AboutDialog.show(this);
            return true;
        }
        return false;
    }



    /**
     * Receives the result of other activities started with startActivityForResult(...)
     */
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        Uri uri;

        switch (resultCode)
        {
        case RESULT_OK:

            switch (requestCode) {
            case REQUEST_CODE_PICK_FILE_TO_OPEN:
                // obtain the filename
                uri = data == null ? null : data.getData();
                if (uri != null) {
                    ((EditText)findViewById(R.id.InFileEditText)).setText(uri.getPath());
                }				
                break;
            case REQUEST_CODE_PICK_FILE_TO_SAVE:
                // obtain the filename
                uri = data == null ? null : data.getData();
                if (uri != null) {
                    ((EditText)findViewById(R.id.OutFileEditText)).setText(uri.getPath());
                }				
                break;
            case REQUEST_CODE_PICK_INOUT_FILE:
                // obtain the filename
                uri = data == null ? null : data.getData();
                if (uri != null) {
                    String filename = uri.getPath();
                    ((EditText)findViewById(R.id.InFileEditText)).setText(filename);
                    // auto set output file ... "input.zip" becomes "input-signed.zip"
                    int pos = filename.lastIndexOf('.');
                    if (pos > 0) {
                        filename = filename.substring(0, pos) + "-signed" + filename.substring(pos); 
                    }
                    ((EditText)findViewById(R.id.OutFileEditText)).setText(filename);
                }               
                break;                
            case REQUEST_CODE_SIGN_FILE:
                logger.info(getResources().getString( R.string.FileSigningSuccess));
                break;
            case REQUEST_CODE_MANAGE_KEYS:
                keyModeSpinnerAdapter.changeData();
                break;
            default:
                logger.error("onActivityResult, RESULT_OK, unknown requestCode " + requestCode);
                break;
            }
            break;
        case RESULT_CANCELED:   // signing operation canceled
            switch (requestCode) {
            case REQUEST_CODE_SIGN_FILE:
                logger.info(getResources().getString(R.string.FileSigningCancelled));
                break;
            case REQUEST_CODE_PICK_FILE_TO_OPEN:
                break;
            case REQUEST_CODE_PICK_FILE_TO_SAVE:
                break;
            case REQUEST_CODE_PICK_INOUT_FILE:
                break;
            case REQUEST_CODE_MANAGE_KEYS:
                keyModeSpinnerAdapter.changeData();
                break;
            default:
                logger.error("onActivityResult, RESULT_CANCELED, unknown requestCode " + requestCode);
                break;
            }
            break;
        case RESULT_FIRST_USER: // error during signing operation
            switch (requestCode) {
            case REQUEST_CODE_SIGN_FILE:
                // ZipSignerActivity displays a toast upon exiting with an error, so we probably don't need to do this.
                String errorMessage = data.getStringExtra("errorMessage");
                logger.debug("Error during file signing: " + errorMessage);
                break;
            default:
                logger.error("onActivityResult, RESULT_FIRST_USER, unknown requestCode " + requestCode);
                break;
            }
            break;
        case RESULT_FIRST_USER+1: // error with auto-key selection
            switch (requestCode) {
            case REQUEST_CODE_SIGN_FILE:
                // TODO display alert dialog?
                // String errorMessage = data.getStringExtra("errorMessage");
                String errorMessage = String.format( getResources().getString(R.string.KeySelectionMessage), getInputFilename());
                AlertDialogUtil.alertDialog(this, getResources().getString(R.string.KeySelectionError), errorMessage);
                break;
            default:
                logger.error("onActivityResult, RESULT_FIRST_USER+1, unknown requestCode " + requestCode);
                break;
            }
            break;            
        default:
            logger.error("onActivityResult, unknown resultCode " + resultCode + ", requestCode = " + requestCode);
        }

    }

    public static void launchFileBrowser(Activity parentActivity, String reason, int requestCode, String samplePath) {
        try {
            String startPath = android.os.Environment.getExternalStorageDirectory().getAbsolutePath();
            if (samplePath != null && samplePath.length() > 0) {
                File f = new File(samplePath);
                String parent = f.getParent();
                if (parent != null) startPath = parent;
            }
            Intent intent = new Intent("kellinwood.zipsigner.action.BROWSE_FILE");
            intent.putExtra("startPath", startPath);
            intent.putExtra("reason", reason);
            parentActivity.startActivityForResult(intent, requestCode);
        } catch (ActivityNotFoundException e) {
            Toast.makeText(parentActivity, e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }

    public static void launchFileBrowserSaveMode(Activity parentActivity, String reason, int requestCode, String samplePath) {
        try {
            String startPath = android.os.Environment.getExternalStorageDirectory().getAbsolutePath();
            String defaultFilename = "signed.zip";
            if (samplePath != null && samplePath.length() > 0) {
                File f = new File(samplePath);
                String parent = f.getParent();
                if (parent != null) startPath = parent;
                defaultFilename = f.getName();
            }
            Intent intent = new Intent("kellinwood.zipsigner.action.BROWSE_FILE");
            intent.putExtra("startPath", startPath);
            intent.putExtra("reason", reason);
            intent.putExtra("saveMode", true);
            intent.putExtra("defaultFilename", defaultFilename);
            parentActivity.startActivityForResult(intent, requestCode);
        } catch (ActivityNotFoundException e) {
            Toast.makeText(parentActivity, e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }


    private void pickInputFile(){
        launchFileBrowser( this, getResources().getString(R.string.BrowserSelectInput), REQUEST_CODE_PICK_FILE_TO_OPEN, getInputFilename());
    }


    private void pickOutputFile() {
        launchFileBrowserSaveMode( this, getResources().getString(R.string.BrowserSelectOutput), REQUEST_CODE_PICK_FILE_TO_SAVE, getOutputFilename());
    }

    private void pickInputOutputFiles() {
        launchFileBrowser( this, getResources().getString(R.string.BrowserSelectInput), REQUEST_CODE_PICK_INOUT_FILE, getOutputFilename());
    }


}
    /**
     * Sign an APK with v2/v3 signatures using a custom key.
     */
    private void signApkWithV2V3(String inputFile, String outputFile, KeyEntry keyEntry) {
        new Thread(() -> {
            try {
                // Get keystore details from database
                kellinwood.zipsigner2.customkeys.CustomKeysDataSource dataSource = 
                    new kellinwood.zipsigner2.customkeys.CustomKeysDataSource(this);
                dataSource.open();
                kellinwood.zipsigner2.customkeys.Keystore keystore = dataSource.getKeystoreByAliasId(keyEntry.getId());
                dataSource.close();

                if (keystore == null) {
                    runOnUiThread(() -> {
                        android.widget.Toast.makeText(this, "Keystore not found", android.widget.Toast.LENGTH_LONG).show();
                    });
                    return;
                }

                // Get passwords
                String keystorePassword = kellinwood.zipsigner2.customkeys.PasswordObfuscator.getInstance()
                        .unobfuscate(keystore.getObfuscatedPassword(), keystore.getFilename());
                
                kellinwood.zipsigner2.customkeys.Alias alias = null;
                for (kellinwood.zipsigner2.customkeys.Alias a : keystore.getAliases()) {
                    if (a.getId() == keyEntry.getId()) {
                        alias = a;
                        break;
                    }
                }

                if (alias == null) {
                    runOnUiThread(() -> {
                        android.widget.Toast.makeText(this, "Key alias not found", android.widget.Toast.LENGTH_LONG).show();
                    });
                    return;
                }

                String keyPassword = alias.getObfuscatedKeyPassword() != null
                        ? kellinwood.zipsigner2.customkeys.PasswordObfuscator.getInstance()
                                .unobfuscate(alias.getObfuscatedKeyPassword(), keystore.getFilename())
                        : keystorePassword;

                // Sign with v1+v2+v3
                runOnUiThread(() -> {
                    android.widget.Toast.makeText(this, "Signing with v2/v3...", android.widget.Toast.LENGTH_SHORT).show();
                });

                ModernApkSigner.sign(
                        new java.io.File(inputFile),
                        new java.io.File(outputFile),
                        new java.io.File(keystore.getFilename()),
                        keystorePassword,
                        alias.getName(),
                        keyPassword
                );

                runOnUiThread(() -> {
                    android.widget.Toast.makeText(this, 
                            "APK signed successfully with v1+v2+v3 signatures!", 
                            android.widget.Toast.LENGTH_LONG).show();
                });

            } catch (Exception e) {
                logger.error("Failed to sign APK with v2/v3", e);
                runOnUiThread(() -> {
                    android.widget.Toast.makeText(this, 
                            "Signing failed: " + e.getMessage(), 
                            android.widget.Toast.LENGTH_LONG).show();
                });
            }
        }).start();
    }
}
