package picnroll.shilpa_cispl.com.picnroll.navigationFiles;

import android.provider.MediaStore;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.Iterator;

import picnroll.shilpa_cispl.com.picnroll.R;


public class DashboardActivity extends AppCompatActivity {

    private Button bt;
    private ListView lv;
    private ArrayList<String> strArr;
    private ArrayAdapter<String> adapter;
    private EditText et;
    private DatabaseReference mDatabase;

    private FirebaseAuth mAuth;
    String userId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_dashboard);

        bt = (Button) findViewById(R.id.button1);
        lv = (ListView) findViewById(R.id.listView1);
        et = (EditText) findViewById(R.id.editText1);

        mDatabase = FirebaseDatabase.getInstance().getReference("Albums");


        FirebaseUser currentFirebaseUser = FirebaseAuth.getInstance().getCurrentUser() ;
         userId =  currentFirebaseUser.getUid();

        Toast.makeText(this, "" + currentFirebaseUser.getUid(), Toast.LENGTH_SHORT).show();


        ArrayList albumnames = new ArrayList();
        mDatabase.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {



            }

            @Override


            public void onCancelled(DatabaseError databaseError) {

            }
        });

        strArr = new ArrayList<String>();

        adapter = new ArrayAdapter<String>(getApplicationContext(),
                android.R.layout.simple_list_item_1, strArr);
        lv.setAdapter(adapter);
        bt.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View arg0) {
                // TODO Auto-generated method stub
                strArr.add(et.getText().toString());
                mDatabase.child("Albums").child(userId).child(String.valueOf(adapter.getCount())).setValue(et.getText().toString());
                adapter.notifyDataSetChanged();

            }
        });

    }
}
