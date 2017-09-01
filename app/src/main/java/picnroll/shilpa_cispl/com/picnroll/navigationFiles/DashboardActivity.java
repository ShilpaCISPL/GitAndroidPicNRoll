package picnroll.shilpa_cispl.com.picnroll.navigationFiles;

import android.content.DialogInterface;
import android.content.Intent;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Toast;

import com.firebase.client.Firebase;
import com.firebase.client.FirebaseError;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;

import picnroll.shilpa_cispl.com.picnroll.R;


public class DashboardActivity extends AppCompatActivity implements View.OnClickListener, AdapterView.OnItemClickListener {

    private Button bt;
    private ListView lv;
    private ArrayList<String> strArr;
    private ArrayAdapter<String> adapter;
    private EditText et;
    private DatabaseReference mDatabase;

    private FirebaseAuth mAuth;
    private Firebase mRef;
    String userId;
    int totalalbumcount;
    ArrayList<String> imageKeys;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_dashboard);

        bt = (Button) findViewById(R.id.button1);
        lv = (ListView) findViewById(R.id.listView1);
        et = (EditText) findViewById(R.id.editText1);
        bt.setOnClickListener(this);
        lv.setOnItemClickListener(this);
        Firebase.setAndroidContext(this);
        strArr = new ArrayList<String>();
        imageKeys = new ArrayList<String>();

        FirebaseUser currentFirebaseUser = FirebaseAuth.getInstance().getCurrentUser();
        userId = currentFirebaseUser.getUid();
        mDatabase = FirebaseDatabase.getInstance().getReference();

        mRef = new Firebase("https://pick-n-roll.firebaseio.com/Albums/" + userId + "");

        mRef.addValueEventListener(new com.firebase.client.ValueEventListener() {
            @Override
            public void onDataChange(com.firebase.client.DataSnapshot dataSnapshot) {

                strArr.clear();

                if (dataSnapshot.getChildrenCount() == 0) {
                    Toast.makeText(DashboardActivity.this, "No folders", Toast.LENGTH_SHORT).show();

                } else {

                    for (int k = 0; k < dataSnapshot.getChildrenCount(); k++) {
                        totalalbumcount = (int) dataSnapshot.getChildrenCount();
                        strArr.add(String.valueOf(dataSnapshot.child(String.valueOf(k)).getValue()));


                    }


                    adapter = new ArrayAdapter<String>(getApplicationContext(),
                            android.R.layout.simple_list_item_1, strArr);
                    Log.d("tag", "strArr value" + strArr.toString());


                    lv.setAdapter(adapter);
                    adapter.notifyDataSetChanged();


                }

            }

            @Override
            public void onCancelled(FirebaseError firebaseError) {

            }
        });


        //Get all imagekeys at your "Files" root node
        DatabaseReference ref = FirebaseDatabase.getInstance().getReference();
        DatabaseReference countRef = ref.child("Files").child(userId);
        countRef.addListenerForSingleValueEvent(
                new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        for (DataSnapshot objSnapshot : dataSnapshot.getChildren()) {
                            Object obj = objSnapshot.getKey();
                            imageKeys.add(String.valueOf(obj));
                            Log.d("tag", "keys are" + obj);
                        }
                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {
                        //handle databaseError
                    }
                });


    }

    @Override
    public void onClick(View view) {

        mDatabase.child("Albums").child(userId).child(String.valueOf(totalalbumcount)).setValue(et.getText().toString());

    }

    @Override
    public void onItemClick(AdapterView<?> adapterView, View view, final int i, long l) {


        AlertDialog.Builder builder1 = new AlertDialog.Builder(this);
        builder1.setMessage("Write your message here.");
        builder1.setCancelable(false);


        builder1.setPositiveButton(
                "Yes",
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {


                        Intent userlist = new Intent(DashboardActivity.this, UserListActivity.class);
                        startActivity(userlist);
//                        Log.d("tag","keys fb"+imageKeys.size() +String.valueOf((lv.getItemAtPosition(i))) );
//                        if (imageKeys.contains(String.valueOf((lv.getItemAtPosition(i))))) {
//                            Intent userlist = new Intent(DashboardActivity.this, UserListActivity.class);
//                            startActivity(userlist);
//                   }
// else {
//                            Intent uploadphoto = new Intent(DashboardActivity.this, ViewUploadPhotosActivity.class);
//                            uploadphoto.putExtra("selectedFolderName", String.valueOf((lv.getItemAtPosition(i))));
//                            uploadphoto.putExtra("selectedFolderPosition", String.valueOf(i));
//                            startActivity(uploadphoto);
//                        }
                        dialog.cancel();
                    }
                });

        builder1.setNegativeButton(
                "No",
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        Intent uploadphoto = new Intent(DashboardActivity.this, ViewUploadPhotosActivity.class);
                        uploadphoto.putExtra("selectedFolderName", String.valueOf((lv.getItemAtPosition(i))));
                        uploadphoto.putExtra("selectedFolderPosition", String.valueOf(i));
                        startActivity(uploadphoto);
                      //  dialog.cancel();
                    }
                });

        AlertDialog alert11 = builder1.create();
        builder1.show();

//        String selectedFolderName = String.valueOf((lv.getItemAtPosition(i)));
//        Log.d("tag", "main value" + selectedFolderName);
//        Intent uploadphoto = new Intent(DashboardActivity.this,ViewUploadPhotosActivity.class);
//        uploadphoto.putExtra("selectedFolderName",selectedFolderName);
//        uploadphoto.putExtra("selectedFolderPosition",String.valueOf(i));
//        startActivity(uploadphoto);

    }
}
