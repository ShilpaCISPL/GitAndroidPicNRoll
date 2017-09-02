package picnroll.shilpa_cispl.com.picnroll.userlistview;

import android.content.Context;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.toolbox.ImageLoader;
import com.android.volley.toolbox.NetworkImageView;
import com.firebase.client.DataSnapshot;
import com.firebase.client.Firebase;
import com.firebase.client.FirebaseError;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import org.json.JSONArray;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import picnroll.shilpa_cispl.com.picnroll.R;

public class UsersListActivity extends AppCompatActivity {


    List<DataAdapter> ListOfdataAdapter;
    RecyclerView recyclerView;
    RecyclerView.LayoutManager layoutManagerOfrecyclerView;
    RecyclerView.Adapter recyclerViewadapter;
    ArrayList<String> ImageTitleNameArrayListForClick;
    private Firebase mRef, mRef1;
    private DatabaseReference mDatabase;
    ArrayList<String> userName = new ArrayList<>();
    ArrayList<String> profileImageUrl = new ArrayList<>();
    ArrayList<String> userIdArray = new ArrayList<>();
    ArrayList<String> imageKeysFromDB = new ArrayList<>();
    ArrayList<String> imageUrlFromDB = new ArrayList<>();
    ArrayList<String> shareImageUrls = new ArrayList<>();
    String userId,selectedFolderName;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_users_list);
        Firebase.setAndroidContext(this);


        selectedFolderName = getIntent().getStringExtra("folderName");
        FirebaseUser currentFirebaseUser = FirebaseAuth.getInstance().getCurrentUser();
        userId = currentFirebaseUser.getUid();
        mDatabase = FirebaseDatabase.getInstance().getReference();

        ImageTitleNameArrayListForClick = new ArrayList<>();

        ListOfdataAdapter = new ArrayList<>();

        recyclerView = (RecyclerView) findViewById(R.id.recyclerview1);

        recyclerView.setHasFixedSize(true);

        layoutManagerOfrecyclerView = new LinearLayoutManager(this);

        recyclerView.setLayoutManager(layoutManagerOfrecyclerView);

//Read profile imageurl and username
        mRef = new Firebase("https://pick-n-roll.firebaseio.com/Users");

        mRef.addValueEventListener(new com.firebase.client.ValueEventListener() {
            @Override
            public void onDataChange(com.firebase.client.DataSnapshot dataSnapshot) {


                for (DataSnapshot childDataSnapshot : dataSnapshot.getChildren()) {
                    userName.add(String.valueOf(childDataSnapshot.child("Name").getValue()));
                    profileImageUrl.add(String.valueOf(childDataSnapshot.child("profileImageUrl").getValue()));
                    userIdArray.add(childDataSnapshot.getKey());
                }

                JSONArray jsArray = new JSONArray(userName);
                ParseJSonResponse(jsArray);

            }

            @Override
            public void onCancelled(FirebaseError firebaseError) {

            }
        });

        //Read imageurls of loggedin user
        mRef1 = new Firebase("https://pick-n-roll.firebaseio.com/Files/" + userId + "");

        mRef1.addValueEventListener(new com.firebase.client.ValueEventListener() {
            @Override
            public void onDataChange(com.firebase.client.DataSnapshot dataSnapshot) {

                for (DataSnapshot childDataSnapshot : dataSnapshot.getChildren()) {

                    imageKeysFromDB.add(childDataSnapshot.getKey());
                    imageUrlFromDB.add(String.valueOf(childDataSnapshot.getValue()));

                }

                for(int j=0; j<imageKeysFromDB.size(); j++){
                    if (imageKeysFromDB.get(j).contains(selectedFolderName)){
                        shareImageUrls.add(imageUrlFromDB.get(j));
                        Toast.makeText(UsersListActivity.this,""+imageUrlFromDB.get(j)+"",Toast.LENGTH_SHORT).show();

                    }
                    else {
                        Toast.makeText(UsersListActivity.this,"No data",Toast.LENGTH_SHORT).show();
                    }
                }


            }

            @Override
            public void onCancelled(FirebaseError firebaseError) {

            }
        });


    }

    public void ParseJSonResponse(JSONArray array) {

        for (int i = 0; i < userName.size(); i++) {

            DataAdapter GetDataAdapter2 = new DataAdapter();

            GetDataAdapter2.setImageTitle(userName.get(i));
            GetDataAdapter2.setImageUrl(profileImageUrl.get(i));

            ListOfdataAdapter.add(GetDataAdapter2);
        }

        recyclerViewadapter = new RecyclerViewAdapter(ListOfdataAdapter, this);

        recyclerView.setAdapter(recyclerViewadapter);
    }


    public class RecyclerViewAdapter extends RecyclerView.Adapter<RecyclerViewAdapter.ViewHolder> {

        Context context;

        List<DataAdapter> dataAdapters;

        ImageLoader imageLoader;

        public RecyclerViewAdapter(List<DataAdapter> getDataAdapter, Context context) {

            super();
            this.dataAdapters = getDataAdapter;
            this.context = context;
        }

        @Override
        public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {

            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.cardview, parent, false);

            ViewHolder viewHolder = new ViewHolder(view);

            return viewHolder;
        }

        @Override
        public void onBindViewHolder(ViewHolder Viewholder, int position) {

            DataAdapter dataAdapterOBJ = dataAdapters.get(position);

            imageLoader = ImageAdapter.getInstance(context).getImageLoader();

            imageLoader.get(dataAdapterOBJ.getImageUrl(),
                    ImageLoader.getImageListener(
                            Viewholder.VollyImageView,//Server Image
                            R.mipmap.ic_launcher,//Before loading server image the default showing image.
                            android.R.drawable.ic_dialog_alert //Error image if requested image dose not found on server.
                    )
            );


            Viewholder.VollyImageView.setImageUrl(dataAdapterOBJ.getImageUrl(), imageLoader);

            Viewholder.ImageTitleTextView.setText(dataAdapterOBJ.getImageTitle());

        }

        @Override
        public int getItemCount() {

            return dataAdapters.size();
        }

        class ViewHolder extends RecyclerView.ViewHolder {

            public TextView ImageTitleTextView;
            public NetworkImageView VollyImageView;

            public ViewHolder(final View itemView) {

                super(itemView);

                ImageTitleTextView = (TextView) itemView.findViewById(R.id.ImageNameTextView);

                VollyImageView = (NetworkImageView) itemView.findViewById(R.id.VolleyImageView);

                itemView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {

                        int pos = getAdapterPosition();
                        mDatabase.child("Albums").child(userIdArray.get(pos)).child(String.valueOf(UUID.randomUUID())).setValue(selectedFolderName);
                        mDatabase.child("SharedUsers").child(userId).child(selectedFolderName).child(String.valueOf(UUID.randomUUID())).setValue(userIdArray.get(pos));

                        for(int m=0; m<shareImageUrls.size(); m++) {

                            mDatabase.child("Files").child(userIdArray.get(pos)).child(String.valueOf(UUID.randomUUID())).setValue(shareImageUrls.get(m));

                        }
                    }
                });

            }
        }
    }

}



