package picnroll.shilpa_cispl.com.picnroll.userlistview;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Toast;

import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonArrayRequest;
import com.android.volley.toolbox.Volley;
import com.firebase.client.DataSnapshot;
import com.firebase.client.Firebase;
import com.firebase.client.FirebaseError;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

import picnroll.shilpa_cispl.com.picnroll.R;

public class UsersListActivity extends AppCompatActivity {


    List<DataAdapter> ListOfdataAdapter;

    RecyclerView recyclerView;

    String HTTP_JSON_URL = "http://androidblog.esy.es/ImageJsonData.php";

    String Image_Name_JSON = "image_title";

    String Image_URL_JSON = "image_url";

    JsonArrayRequest RequestOfJSonArray ;

    RequestQueue requestQueue ;

    View view ;

    int RecyclerViewItemPosition ;

    RecyclerView.LayoutManager layoutManagerOfrecyclerView;

    RecyclerView.Adapter recyclerViewadapter;

    ArrayList<String> ImageTitleNameArrayListForClick;
    private Firebase mRef;
    ArrayList<String> userName = new ArrayList<>();
    ArrayList<String> profileImageUrl = new ArrayList<>();
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_users_list);
        Firebase.setAndroidContext(this);

        ImageTitleNameArrayListForClick = new ArrayList<>();

        ListOfdataAdapter = new ArrayList<>();

        recyclerView = (RecyclerView) findViewById(R.id.recyclerview1);

        recyclerView.setHasFixedSize(true);

        layoutManagerOfrecyclerView = new LinearLayoutManager(this);

        recyclerView.setLayoutManager(layoutManagerOfrecyclerView);



        // Implementing Click Listener on RecyclerView.
        recyclerView.addOnItemTouchListener(new RecyclerView.OnItemTouchListener() {

            GestureDetector gestureDetector = new GestureDetector(UsersListActivity.this, new GestureDetector.SimpleOnGestureListener() {

                @Override public boolean onSingleTapUp(MotionEvent motionEvent) {

                    return true;
                }

            });
            @Override
            public boolean onInterceptTouchEvent(RecyclerView Recyclerview, MotionEvent motionEvent) {

                view = Recyclerview.findChildViewUnder(motionEvent.getX(), motionEvent.getY());

                if(view != null && gestureDetector.onTouchEvent(motionEvent)) {

                    //Getting RecyclerView Clicked Item value.
                    RecyclerViewItemPosition = Recyclerview.getChildAdapterPosition(view);

                    // Showing RecyclerView Clicked Item value using Toast.
                    Toast.makeText(UsersListActivity.this, ImageTitleNameArrayListForClick.get(RecyclerViewItemPosition), Toast.LENGTH_LONG).show();
                }

                return false;
            }

            @Override
            public void onTouchEvent(RecyclerView Recyclerview, MotionEvent motionEvent) {

            }

            @Override
            public void onRequestDisallowInterceptTouchEvent(boolean disallowIntercept) {

            }
        });


        mRef = new Firebase("https://pick-n-roll.firebaseio.com/Users");

        mRef.addValueEventListener(new com.firebase.client.ValueEventListener() {
            @Override
            public void onDataChange(com.firebase.client.DataSnapshot dataSnapshot) {


                for (DataSnapshot childDataSnapshot : dataSnapshot.getChildren()) {

                    userName.add(String.valueOf(childDataSnapshot.child("Name").getValue()));
                    profileImageUrl.add(String.valueOf(childDataSnapshot.child("profileImageUrl").getValue()));


                }


                JSONArray jsArray = new JSONArray(userName);

                ParseJSonResponse(jsArray);

            }

            @Override
            public void onCancelled(FirebaseError firebaseError) {

            }
        });


    }


    public void ParseJSonResponse(JSONArray array){

        for(int i = 0; i<userName.size(); i++) {

            DataAdapter GetDataAdapter2 = new DataAdapter();

            GetDataAdapter2.setImageTitle(userName.get(i));
            GetDataAdapter2.setImageUrl(profileImageUrl.get(i));

            ListOfdataAdapter.add(GetDataAdapter2);
        }

        recyclerViewadapter = new RecyclerViewAdapter(ListOfdataAdapter, this);

        recyclerView.setAdapter(recyclerViewadapter);
    }
}
