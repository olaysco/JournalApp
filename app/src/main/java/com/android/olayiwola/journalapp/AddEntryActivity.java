package com.android.olayiwola.journalapp;

import android.app.ProgressDialog;
import android.content.Intent;
import android.icu.text.SimpleDateFormat;
import android.os.Bundle;

import android.support.annotation.NonNull;
import android.support.v4.app.ShareCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.EditText;
import android.widget.Toast;

import com.android.olayiwola.journalapp.Data.JournalEntry;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;

import org.w3c.dom.Text;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * Created by olayiwola on 6/28/2018.
 */

public class AddEntryActivity extends AppCompatActivity {

    static final String TAG = "AddEntryActivity";

    public static final String UPDATE_TITLE = "JOURNAL_TITLE";
    public static final String UPDATE_CONTENT = "JOURNAL_CONTENT";
    public static final String UPDATE_DB_ID = "JOURNAL_ID";
    public ProgressDialog mProgressDialog;

    EditText mEntryTitle;
    EditText mEntryContent;
    private FirebaseAuth mFirebaseAuth;
    private FirebaseUser mUser;
    private FirebaseFirestore mFirestoreDb;

    Menu topMenu;
    private String db_id = null;

    Intent intent;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_entry);
        mFirebaseAuth = FirebaseAuth.getInstance();
        mFirestoreDb = FirebaseFirestore.getInstance();
        mUser = mFirebaseAuth.getCurrentUser();
        if(mUser == null){
            Intent loginIntent = new Intent(AddEntryActivity.this, LoginActivity.class);
            startActivity(loginIntent);
            finish();
        }
        Toolbar toolbar = findViewById(R.id.main_toolbar);
        setSupportActionBar(toolbar);
        //hides the App title
        getSupportActionBar().setDisplayShowTitleEnabled(false);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        //displays the app drawable logo
        toolbar.setNavigationIcon(R.drawable.journal_app_logo);

        mEntryContent = findViewById(R.id.new_entry_content);
        mEntryTitle = findViewById(R.id.new_entry_title);

        intent = getIntent();
        if (intent != null && intent.hasExtra(UPDATE_DB_ID)) {
            String title = intent.getStringExtra(UPDATE_TITLE).toString();
            String content = intent.getStringExtra(UPDATE_CONTENT).toString();
            db_id = intent.getStringExtra(UPDATE_DB_ID).toString();
            mEntryTitle.setText(title);
            mEntryContent.setText(content);

        }

    }

    /*******
     *
     *
     * Performs update to database
     */
    private void updateEntry() {
        final JournalEntry journalEntry = coupleEntryForUpdate();
        showProgress();
        mEntryContent.clearFocus();
        mEntryTitle.clearFocus();
        journalEntry.setId(db_id);
        Log.d("Sav",db_id);
        mFirestoreDb.collection("entries").whereEqualTo("id", db_id)
        .get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {

            @Override
            public void onComplete(@NonNull Task<QuerySnapshot> task) {
                if(task.isSuccessful()){
                    List<DocumentSnapshot> docs = task.getResult().getDocuments();

                    for(DocumentSnapshot ds: docs){
                        ds.getReference().update("content", journalEntry.getContent());
                        ds.getReference().update("title", journalEntry.getTitle());

                        hideProgress();
                        goToMainActivity();
                    }
                }else{
                    hideProgress();
                    Toast.makeText(getApplicationContext(),"Update Failed Check your internet connection", Toast.LENGTH_LONG);
                }
            }

        });
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.add_menu, menu);
        topMenu = menu;

        if (intent != null && intent.hasExtra(UPDATE_DB_ID)) {

            if(topMenu != null){
                topMenu.findItem(R.id.delete_option).setVisible(true);
                topMenu.findItem(R.id.save_option).setTitle("UPDATE");
                topMenu.findItem(R.id.save_option).setOnMenuItemClickListener(new MenuItem.OnMenuItemClickListener() {
                    @Override
                    public boolean onMenuItemClick(MenuItem menuItem) {
                        updateEntry();
                        return true;
                    }
                });
            }
        }

        return true;
    }

    public void saveEntry(MenuItem menuItem){
        mEntryContent.clearFocus();
        mEntryTitle.clearFocus();
        if(TextUtils.isEmpty(mEntryContent.getText().toString().trim()) || TextUtils.isEmpty(mEntryTitle.getText().toString().trim())){
            Toast.makeText(AddEntryActivity.this, "content and title is empty", Toast.LENGTH_SHORT).show();
            return;
        }
        showProgress();
        JournalEntry newEntry = coupleEntry();
        newEntry.setId(mFirestoreDb.collection("entries").document().getId());
        mFirestoreDb.collection("entries").add(newEntry)
        .addOnSuccessListener(new OnSuccessListener<DocumentReference>() {
            @Override
            public void onSuccess(DocumentReference documentReference) {
                Log.d(TAG, "entry added to db: "+
                documentReference.getId());
                Toast.makeText(AddEntryActivity.this,"Journal saved", Toast.LENGTH_SHORT).show();
                hideProgress();
                goToMainActivity();
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Log.d(TAG, "entry adding new entry to journal", e);
                Toast.makeText(AddEntryActivity.this,"Could not add new entry, try again", Toast.LENGTH_SHORT).show();
                hideProgress();
            }
        });
    }

    /*****
     *
     * Shares The current entry using the ShareCompat Class
     * @param menuItem
     */
    public void shareEntry(MenuItem menuItem){
        String mimeType = "text/plain";
        if(TextUtils.isEmpty(mEntryContent.getText().toString().trim()) ){
            Toast.makeText(AddEntryActivity.this, "content is empty", Toast.LENGTH_SHORT).show();
            return;
        }
        /* This is just the title of the window that will pop up when we call startActivity */
        String title = "Share your journal entry";
        String textToShare = mEntryContent.getText().toString();
        /* ShareCompat.IntentBuilder provides a fluent API for creating Intents */
        ShareCompat.IntentBuilder

                .from(this)
                .setType(mimeType)
                .setChooserTitle(title)
                .setText(textToShare)
                .startChooser();
    }

    /***
     * Signs user out when sign out item is clicked from the Menu item
     * @param menuItem
     */
    public void signOut(MenuItem menuItem){
        mFirebaseAuth.signOut();
        Intent loginIntent = new Intent(AddEntryActivity.this, LoginActivity.class);
        loginIntent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        finish();
        startActivity(loginIntent);
    }

    /******
     *
     * Couples new entry item fields together for easy adding to database
     * @return JournalEntry
     */
    private JournalEntry coupleEntry() {
        final JournalEntry je = new JournalEntry();
        je.setContent(mEntryContent.getText().toString());
        je.setTitle(mEntryTitle.getText().toString());
        je.setLastModifiedDate(new Date());
        je.setUserId(mUser.getUid());
        return je;

    }

    /******
     *
     * Couples new entry item fields together for easy update in database
     * could have used the coupleEntry for add but not all fields are needed for update
     * @return JournalEntry
     */
    private JournalEntry coupleEntryForUpdate() {
        final JournalEntry je = new JournalEntry();
        je.setContent(mEntryContent.getText().toString());
        je.setTitle(mEntryTitle.getText().toString());
        return je;
    }



    /******
     *
     * Shows Progress dialog
     * @return void
     */
    public void showProgress(){
        if(mProgressDialog == null){
            mProgressDialog = new ProgressDialog(this);
            mProgressDialog.setMessage("please wait!");
            mProgressDialog.setIndeterminate(true);
        }
        mProgressDialog.show();
    }

    /******
     *
     * Hides Progress dialog
     * @return void
     */
    public void hideProgress(){
        if(mProgressDialog != null && mProgressDialog.isShowing()){
            mProgressDialog.dismiss();
        }
    }


    /******
     *
     * Deletes the current entry that is being displayed
     * This method is called when the delete menu item is clicked, sets from the
     * menu XML file
     * @param  item
     * @return void
     */
    public void deleteEntry(MenuItem item) {
        if(db_id == null){
            //If entry id from the da
            return;
        }else{
            mFirestoreDb.collection("entries").whereEqualTo("id", db_id)
                    .get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {

                @Override
                public void onComplete(@NonNull Task<QuerySnapshot> task) {
                    if(task.isSuccessful()){
                        List<DocumentSnapshot> docs = task.getResult().getDocuments();

                        for(DocumentSnapshot ds: docs){
                            ds.getReference().delete();
                            Toast.makeText(getApplicationContext(),"Entry deleted successfully", Toast.LENGTH_SHORT)
                                    .show();
                            goToMainActivity();
                        }
                    }else{
                        hideProgress();
                        Toast.makeText(AddEntryActivity.this,"Update Failed Check your internet connection", Toast.LENGTH_LONG);
                    }
                }

            });
        }
    }

    /*******
     *
     *
     * Opens the Main Activity
     */
    private void goToMainActivity() {
        Intent mainActivityIntent = new Intent(AddEntryActivity.this, MainActivity.class);
        mainActivityIntent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        startActivity(mainActivityIntent);
    }

    /*******
     *
     * Navigates User back to Main Activity
     *
     * @return boolean
     */
    @Override
    public boolean onSupportNavigateUp() {
        finish();
        return true;
    }
}
