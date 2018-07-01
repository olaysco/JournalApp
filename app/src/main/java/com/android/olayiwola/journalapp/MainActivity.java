package com.android.olayiwola.journalapp;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Build;
import android.support.annotation.NonNull;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.DividerItemDecoration;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.support.v7.widget.TooltipCompat;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.android.olayiwola.journalapp.Data.JournalEntry;
import com.firebase.ui.firestore.FirestoreRecyclerAdapter;
import com.firebase.ui.firestore.FirestoreRecyclerOptions;
import com.google.android.gms.auth.api.Auth;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.ListenerRegistration;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.List;

import javax.annotation.Nullable;

public class MainActivity extends AppCompatActivity implements EntryAdapter.CardClickListener, GoogleApiClient.OnConnectionFailedListener {

    private static final String TAG = MainActivity.class.getSimpleName();


    //CONSTANTS TO BE USED FOR INTENTS TO UPDATE ACTIVITY
    public static final String UPDATE_TITLE = "JOURNAL_TITLE";
    public static final String UPDATE_CONTENT = "JOURNAL_CONTENT";
    public static final String UPDATE_DB_ID = "JOURNAL_ID";
    public static final String UPDATE_DATE = "JOURNAL_DATE";

    //constants declaration for checking intent from login activity
    private static final String SIGN_IN_OPTION = "SIGN_IN_OPTION";
    private static final String SIGN_IN_ID = "SIGN_IN_ID";
    private static final String SIGN_IN_EMAIL = "SIGN_IN_EMAIL";

    private FirebaseAuth mFirebaseAuth;
    private FirebaseUser mUser;
    private RecyclerView mRecyclerView;
    private EntryAdapter mEntryAdapter;
    private FirebaseFirestore mFirestoreDb;
    private ProgressBar mProgressBar;
    private List<JournalEntry> mjournalEntries;
    private ListenerRegistration mFirestoreListener;
    private GoogleApiClient mGoogleApiClient;
    private LinearLayout mLinearEmptyDisp;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        mFirebaseAuth = FirebaseAuth.getInstance();
        mFirestoreDb = FirebaseFirestore.getInstance();
        mUser = mFirebaseAuth.getCurrentUser();
        mProgressBar = findViewById(R.id.progress_bar);

        mLinearEmptyDisp = findViewById(R.id.emptyEntryDisplay);
        mLinearEmptyDisp.setVisibility(View.GONE);

        Toolbar toolbar = findViewById(R.id.main_toolbar);
        setSupportActionBar(toolbar);
        //hides the App title
        getSupportActionBar().setDisplayShowTitleEnabled(false);
        //displays the app drawable logo
        toolbar.setNavigationIcon(R.drawable.journal_app_logo);

        if(mUser == null){
            Intent loginIntent = new Intent(MainActivity.this, LoginActivity.class);
            startActivity(loginIntent);
            finish();
        }

        mRecyclerView = findViewById(R.id.recyclerViewEntry);
        mEntryAdapter = new EntryAdapter(this, this);

        //To set user profile name in the Toolbar and display a tooltip when log pressed
        TextView profileText = findViewById(R.id.profileName);
        profileText.setText(mUser.getEmail());
        TooltipCompat.setTooltipText(profileText, mUser.getEmail());

        LinearLayoutManager llm = new LinearLayoutManager(this);
        llm.setSmoothScrollbarEnabled(true);
        mRecyclerView.setHasFixedSize(true);
        mRecyclerView.setLayoutManager(llm);
        mRecyclerView.setAdapter(mEntryAdapter);
        FloatingActionButton fabButton = findViewById(R.id.fab);

        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(getString(R.string.WEB_CLIENT_ID))
                .requestEmail()
                .build();

        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .enableAutoManage(this, this)
                .addApi(Auth.GOOGLE_SIGN_IN_API, gso)
                .build();

        //FAB TO OPEN THE ADD ENTRY ACTIVITY
        fabButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // Create a new intent to start an AddTaskActivity
                Intent addEntryIntent = new Intent(MainActivity.this, AddEntryActivity.class);
                startActivity(addEntryIntent);
            }
        });

        //THIS CALLS THE GETALL ENTRIES METHOD TO FETCH ALL ENTRY ITEM TO THE ADAPTER
        getAllEntries();

        mFirestoreListener = mFirestoreDb.collection("entries").orderBy("lastModifiedDate", Query.Direction.DESCENDING)

                .addSnapshotListener(new EventListener<QuerySnapshot>() {
                    @Override
                    public void onEvent(@Nullable QuerySnapshot queryDocumentSnapshots, @Nullable FirebaseFirestoreException e) {
                        if(e != null){
                            Log.e(TAG, "error occured while listening for changes",e);
                            return;
                        }
                        mjournalEntries = new ArrayList<>();

                        for(DocumentSnapshot doc : queryDocumentSnapshots){
                            JournalEntry journalEntry = doc.toObject(JournalEntry.class);
                            journalEntry.setId(doc.getId());
                            mjournalEntries.add(journalEntry);
                        }
                        List<JournalEntry> userJournalEntries = getEntryForUser(mjournalEntries);
                        checkToDisplayEmptyView(userJournalEntries);
                        mEntryAdapter.setData(userJournalEntries);
                        mRecyclerView.setAdapter(mEntryAdapter);
                       mEntryAdapter.notifyDataSetChanged();

                    }
                });


    }

    /******
     *
     * Checks if entry item is empty
     * if empty it sets the Empty Entry Display LinearLayout visibility to VISIBLE
     * else sets it to GONE
     * @param journalEntries
     */
    private void checkToDisplayEmptyView(List<JournalEntry> journalEntries) {
        TextView text = findViewById(R.id.emptyEntryDisplayText);
        ImageView icon = findViewById(R.id.emptyEntryDisplayIcon);
        if(journalEntries.isEmpty() || journalEntries == null){
            mLinearEmptyDisp.setVisibility(View.VISIBLE);
            text.setVisibility(View.VISIBLE);
            icon.setVisibility(View.VISIBLE);
        }else{
            mLinearEmptyDisp.setVisibility(View.INVISIBLE);
            text.setVisibility(View.INVISIBLE);
            icon.setVisibility(View.INVISIBLE);
        }
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main_menu, menu);
        return true;
    }

    /******
     *
     * Gets all entries on creation of this activity
     * and populates the adapter with data
     */
    private void getAllEntries(){
        mProgressBar.setVisibility(View.VISIBLE);
        mFirestoreDb.collection("entries")
                .orderBy("lastModifiedDate", Query.Direction.DESCENDING)
                .get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
            @Override
            public void onComplete(@NonNull Task<QuerySnapshot> task) {
                mProgressBar.setVisibility(View.INVISIBLE);
                if(task.isSuccessful()){
                    List<JournalEntry> allJournalEntries = task.getResult().toObjects(JournalEntry.class);
                    List<JournalEntry> userJournalEntries = getEntryForUser(allJournalEntries);

                    checkToDisplayEmptyView(userJournalEntries);
                    mEntryAdapter.setData(userJournalEntries);
                    mEntryAdapter.notifyDataSetChanged();
                    mRecyclerView.setAdapter(mEntryAdapter);
                    Log.d("check", String.valueOf(allJournalEntries.size()));
                }
            }
        });
    }

    /******
     *
     * Gets Journal Entry for user,  by matching currently signed in user Id
     * with Id used in creating this entry
     * Though there is a whereEqualTo method that can be used with the Firestor query but it behaves
     * abnormally when used with orderBy
     * @param allJournalEntries
     * @return journalEntry
     */
    private List<JournalEntry> getEntryForUser(List<JournalEntry> allJournalEntries) {
        List<JournalEntry> journalEntry = new ArrayList<>();
        for(JournalEntry je: allJournalEntries){
            Log.d("ids",je.getUserId()+"=="+mUser.getUid());
            if(je.getUserId().equals(mUser.getUid())){
                journalEntry.add(je);
            }
        }
        return journalEntry;
    }

    /*********
     *
     * This method opens the Add Activity and populate it with entry data for
     * update
     * @param entry
     */
    private void updateEntry(JournalEntry entry) {
        Intent updateEntryIntent = new Intent(MainActivity.this, AddEntryActivity.class);
        updateEntryIntent.putExtra(UPDATE_DB_ID, entry.getId());
        updateEntryIntent.putExtra(UPDATE_TITLE, entry.getTitle());
        updateEntryIntent.putExtra(UPDATE_CONTENT, entry.getContent());
        updateEntryIntent.putExtra(UPDATE_DATE, entry.getLastModifiedDate());
        startActivity(updateEntryIntent);

    }

    @Override
    public void onItemClickListener(JournalEntry entry) {
        //Toast.makeText(MainActivity.this, " view clicked", Toast.LENGTH_SHORT).show();
        updateEntry(entry);
        Log.d(UPDATE_DB_ID,entry.getId());
    }


    @Override
    protected void onStart() {
        super.onStart();

    }

    @Override
    protected void onStop() {
        super.onStop();

    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mFirestoreListener.remove();
    }

    /***
     * Signs user out when sign out item is clicked from the Menu item
     * @param menuItem
     */
    public void signOut(MenuItem menuItem){
        mFirebaseAuth.signOut();
        if(LoginActivity.mGoogleApiClient != null) {
            Auth.GoogleSignInApi.signOut(mGoogleApiClient);
        }
        Intent loginIntent = new Intent(MainActivity.this, LoginActivity.class);
        loginIntent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        startActivity(loginIntent);
        finish();
    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {
        Toast.makeText(MainActivity.this, "Network connection error", Toast.LENGTH_LONG).show();
    }
}
