package picnroll.shilpa_cispl.com.picnroll.navigationFiles;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.Configuration;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.BaseAdapter;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
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
import com.squareup.picasso.Picasso;

import java.io.InputStream;
import java.util.ArrayList;

import picnroll.shilpa_cispl.com.picnroll.LoginActivity;

import picnroll.shilpa_cispl.com.picnroll.R;
import picnroll.shilpa_cispl.com.picnroll.customgallery.FolderImagesActivity;
import picnroll.shilpa_cispl.com.picnroll.galleries.GalleryListAdapter;
import picnroll.shilpa_cispl.com.picnroll.userlistview.UsersListActivity;


public class NavActivity extends AppCompatActivity implements View.OnClickListener{

    private DrawerLayout mDrawerLayout;
    private ListView mDrawerList;
    FloatingActionButton fab;

    private CharSequence mTitle;
    private CharSequence mDrawerTitle;
    private ActionBarDrawerToggle mDrawerToggle;
    String userId, profileImageUrl;


    //firebase auth object
    private FirebaseAuth firebaseAuth;
    private Firebase mRef;
    private DatabaseReference mDatabase;

    ImageView profileImage;
    int totalalbumcount;

    ArrayList<String> imageKeys = new ArrayList<>();
    ArrayList<String> imageUrl = new ArrayList<>();
    ArrayList<String> sharedUsersIdArray = new ArrayList<>();

    ListView lv_gallery_names;
    GalleryListAdapter list_adapter;
    ArrayList<String> strArr = new ArrayList<>();


    public static int [] language_images={R.drawable.ic_launcher,
            R.drawable.ic_launcher,
            R.drawable.ic_launcher,
            R.drawable.ic_launcher,
            R.drawable.ic_launcher,
            R.drawable.ic_launcher,
            R.drawable.ic_launcher,
            R.drawable.ic_launcher,
            R.mipmap.ic_launcher};

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_nav);

        Firebase.setAndroidContext(this);
        FirebaseUser currentFirebaseUser = FirebaseAuth.getInstance().getCurrentUser();
        userId = currentFirebaseUser.getUid();
        //initializing firebase authentication object
        firebaseAuth = FirebaseAuth.getInstance();
        mTitle = mDrawerTitle = getTitle();
         final ProgressDialog mProgressDialog = new ProgressDialog(this);

        mDrawerLayout = (DrawerLayout) findViewById(R.id.drawer_layout);
        mDrawerList = (ListView) findViewById(R.id.left_drawer);
        LayoutInflater inflater = getLayoutInflater();
        View listHeaderView = inflater.inflate(R.layout.header_list, null, false);
        profileImage = (ImageView) listHeaderView.findViewById(R.id.circleView);
        lv_gallery_names = (ListView) findViewById(R.id.lv_languages);

        fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(this);

        //Read profile imge url from firebase
        mRef = new Firebase("https://pick-n-roll.firebaseio.com/Users/" + userId + "/profileImageUrl");

        mRef.addValueEventListener(new com.firebase.client.ValueEventListener() {
            @Override
            public void onDataChange(com.firebase.client.DataSnapshot dataSnapshot) {

                profileImageUrl = (String) dataSnapshot.getValue();
                Log.d("tag", "profileImageUrl" + profileImageUrl);
                new DownloadImage().execute(profileImageUrl);


            }

            @Override
            public void onCancelled(FirebaseError firebaseError) {

            }
        });


        mDrawerList.addHeaderView(listHeaderView);

        ArrayList<ItemObject> listViewItems = new ArrayList<ItemObject>();

        listViewItems.add(new ItemObject("Gallery", R.drawable.appicon));
        listViewItems.add(new ItemObject("MyProfile", R.drawable.myprofile1));
        listViewItems.add(new ItemObject("Map", R.drawable.map1));
        listViewItems.add(new ItemObject("Logout", R.drawable.logout));

        mDrawerList.setAdapter(new CustomAdapter(this, listViewItems));

        mDrawerToggle = new ActionBarDrawerToggle(NavActivity.this, mDrawerLayout, R.string.drawer_open, R.string.drawer_close) {

            /** Called when a drawer has settled in a completely closed state. */
            public void onDrawerClosed(View view) {
                super.onDrawerClosed(view);
                getSupportActionBar().setTitle(mTitle);
                View v = (View) findViewById(R.id.listFolders);
                v.setVisibility(View.VISIBLE);
                invalidateOptionsMenu(); // creates call to onPrepareOptionsMenu()

            }

            /** Called when a drawer has settled in a completely open state. */
            public void onDrawerOpened(View drawerView) {
                super.onDrawerOpened(drawerView);
                getSupportActionBar().setTitle(mDrawerTitle);
                View v = (View) findViewById(R.id.listFolders);
                v.setVisibility(View.INVISIBLE);
                invalidateOptionsMenu(); // creates call to onPrepareOptionsMenu()
            }
        };

        // Set the drawer toggle as the DrawerListener
        mDrawerLayout.setDrawerListener(mDrawerToggle);
        mDrawerToggle.setDrawerIndicatorEnabled(true);

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setHomeButtonEnabled(true);

        mDrawerList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {


                selectItemFragment(position);


            }
        });

        mProgressDialog.setMessage("Loading ...");
        mProgressDialog.show();
        mDatabase = FirebaseDatabase.getInstance().getReference();

        mRef = new Firebase("https://pick-n-roll.firebaseio.com/Albums/" + userId + "");

        mRef.addValueEventListener(new com.firebase.client.ValueEventListener() {
            @Override
            public void onDataChange(com.firebase.client.DataSnapshot dataSnapshot) {

                strArr.clear();

                if (dataSnapshot.getChildrenCount() == 0) {

                    AlertDialog.Builder builder1 = new AlertDialog.Builder(NavActivity.this);
                    builder1.setTitle("No folders");
                    builder1.setMessage("Add New Folder");
                    builder1.setCancelable(false);

                    builder1.setPositiveButton(
                            "Ok",
                            new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog, int id) {
                                    dialog.cancel();

                                }
                            });

                    builder1.setNegativeButton(
                            "Cancel",
                            new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog, int id) {
                                    dialog.cancel();
                                }
                            });


                    builder1.setIcon(R.drawable.appicon);
                    builder1.show();

                } else {
                    mProgressDialog.dismiss();
                    for (int k = 0; k < dataSnapshot.getChildrenCount(); k++) {
                        totalalbumcount = (int) dataSnapshot.getChildrenCount();
                        strArr.add(String.valueOf(dataSnapshot.child(String.valueOf(k)).getValue()));
                        Log.d("tag", "strarr" + String.valueOf(dataSnapshot.child(String.valueOf(k)).getValue()));

                    }

                    list_adapter = new GalleryListAdapter(NavActivity.this,strArr, language_images);

                    lv_gallery_names.setAdapter(list_adapter);
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

                        if (dataSnapshot.exists()) {
                            for (DataSnapshot objSnapshot : dataSnapshot.getChildren()) {
                                Object obj = objSnapshot.getKey();
                                imageKeys.add(String.valueOf(obj));
                                imageUrl.add(String.valueOf(objSnapshot.getValue()));
                                Log.d("tag", "imagekeys" + imageKeys.toString() + "\n" + objSnapshot.getValue());

                            }
                        } else {

                            imageKeys = null;
                        }

                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {
                        //handle databaseError
                    }
                });


        lv_gallery_names.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, final int i, long l) {

                AlertDialog.Builder builder1 = new AlertDialog.Builder(NavActivity.this);
                builder1.setMessage("Want to share folder?");
                builder1.setCancelable(false);


                builder1.setNegativeButton(
                        "No",
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id) {

                                //Get all shared user's usedId
                                //Get all imagekeys at your "Files" root node
                                Intent uploadphoto = new Intent(NavActivity.this, FolderImagesActivity.class);
                                uploadphoto.putExtra("selectedFolderName", String.valueOf((lv_gallery_names.getItemAtPosition(i))));
                                uploadphoto.putExtra("selectedFolderPosition", String.valueOf(i));
                                uploadphoto.putStringArrayListExtra("sharedUsersIdArray", sharedUsersIdArray);
                                uploadphoto.putStringArrayListExtra("imageKeys", imageKeys);
                                uploadphoto.putStringArrayListExtra("imageUrl", imageUrl);
                                startActivity(uploadphoto);

                                dialog.cancel();
                            }
                        });

                builder1.setPositiveButton(
                        "Yes",
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id) {

                                if (imageKeys != null) {

                                    for (int k = 0; k < imageKeys.size(); k++) {
                                        if (imageKeys.get(k).contains(String.valueOf((lv_gallery_names.getItemAtPosition(i))))) {
                                            Intent userlist = new Intent(NavActivity.this, UsersListActivity.class);
                                            userlist.putExtra("folderName", String.valueOf((lv_gallery_names.getItemAtPosition(i))));


                                            startActivity(userlist);
                                        }
                                    }

                                } else {
                                    Intent uploadphoto = new Intent(NavActivity.this, FolderImagesActivity.class);
                                    uploadphoto.putExtra("selectedFolderName", String.valueOf((lv_gallery_names.getItemAtPosition(i))));
                                    uploadphoto.putExtra("selectedFolderPosition", String.valueOf(i));
                                    uploadphoto.putStringArrayListExtra("sharedUsersIdArray", sharedUsersIdArray);
                                    startActivity(uploadphoto);

                                }

                                dialog.cancel();
                            }
                        });


                builder1.setIcon(R.drawable.appicon);
                builder1.show();


            }
        });
    }
    private void selectItemFragment(int position) {

        Fragment fragment = null;
        FragmentManager fragmentManager = getSupportFragmentManager();
        switch (position) {

            case 1:

                Intent ii = new Intent(NavActivity.this, DashboardActivity.class);
                startActivity(ii);
                //fragmentManager.beginTransaction().replace(R.id.main_fragment_container, fragment).commit();

                break;
            case 2:
                View v = (View) findViewById(R.id.listFolders);
                v.setVisibility(View.INVISIBLE);
                fragment = new DefaultFragment();
                fragmentManager.beginTransaction().replace(R.id.main_fragment_container, fragment).commit();
                break;
            case 3:
                Intent iii = new Intent(NavActivity.this, MapsActivity.class);
                startActivity(iii);
                break;

            case 4:
                //logging out the user
                firebaseAuth.signOut();
                //closing activity
                finish();
                //starting login activity
                Intent i = new Intent(NavActivity.this, LoginActivity.class);
                startActivity(i);
                break;
        }

        mDrawerList.setItemChecked(position, true);
//        setTitle(titles[position]);
        mDrawerLayout.closeDrawer(mDrawerList);
    }


    @Override
    public void onClick(View view) {
        // get prompts.xml view
        LayoutInflater li = LayoutInflater.from(this);
        View promptsView = li.inflate(R.layout.add_folder_nav_popup, null);

        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(
                this);

        // set prompts.xml to alertdialog builder
        alertDialogBuilder.setView(promptsView);

        final EditText userInput = (EditText) promptsView
                .findViewById(R.id.editTextDialogUserInput);

        // set dialog message
        alertDialogBuilder
                .setCancelable(false)
                .setPositiveButton("OK",
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id) {
                                // get user input and set it to result
                                // edit text
                                mDatabase.child("Albums").child(userId).child(String.valueOf(totalalbumcount)).setValue(userInput.getText().toString());
                                //  result.setText(userInput.getText());
                            }
                        })
                .setNegativeButton("Cancel",
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id) {
                                dialog.cancel();
                            }
                        });

        // create alert dialog
        AlertDialog alertDialog = alertDialogBuilder.create();

        // show it
        alertDialog.show();


    }


    // DownloadImage AsyncTask
    class DownloadImage extends AsyncTask<String, Void, Bitmap> {

        @Override
        protected void onPreExecute() {
            super.onPreExecute();

        }

        @Override
        protected Bitmap doInBackground(String... URL) {

            String imageURL = URL[0];

            Bitmap bitmap = null;
            try {
                // Download Image from URL
                InputStream input = new java.net.URL(imageURL).openStream();
                // Decode Bitmap
                bitmap = BitmapFactory.decodeStream(input);
            } catch (Exception e) {
                e.printStackTrace();
            }
            return bitmap;
        }

        @Override
        protected void onPostExecute(Bitmap result) {
            // Set the bitmap into ImageView
            //  image.setImageBitmap(result);

            profileImage.setImageBitmap(result);

        }
    }




    @Override
    public void setTitle(CharSequence title) {
        mTitle = title;
        getSupportActionBar().setTitle(mTitle);
    }

    @Override
    protected void onPostCreate(Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);
        // Sync the toggle state after onRestoreInstanceState has occurred.
        mDrawerToggle.syncState();
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        mDrawerToggle.onConfigurationChanged(newConfig);
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        // If the nav drawer is open, hide action items related to the content view
        boolean drawerOpen = mDrawerLayout.isDrawerOpen(mDrawerList);
        return super.onPrepareOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }
        if (mDrawerToggle.onOptionsItemSelected(item)) {

            return true;
        }

        return super.onOptionsItemSelected(item);
    }


    public class GalleryListAdapter extends BaseAdapter {
        ArrayList<String> result = new ArrayList<>();
        Context context;
        int [] imageId;
        private  LayoutInflater inflater=null;

        public GalleryListAdapter(NavActivity mainActivity, ArrayList prgmNameList, int[] prgmImages) {
// TODO Auto-generated constructor stub
            result=prgmNameList;
            context=mainActivity;
            imageId=prgmImages;
            inflater = ( LayoutInflater )context.
                    getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        }

        @Override
        public int getCount() {
// TODO Auto-generated method stub
            return result.size();
        }

        @Override
        public Object getItem(int position) {
// TODO Auto-generated method stub
            return position;
        }

        @Override
        public long getItemId(int position) {
// TODO Auto-generated method stub
            return position;
        }

        public class Holder
        {
            TextView tv_language;
            ImageView im_language;
        }
        @Override
        public View getView(final int position, View convertView, ViewGroup parent) {
// TODO Auto-generated method stub
            Holder holder=new Holder();
            View view;
            view = inflater.inflate(R.layout.layout_gallery_list, null);

            holder.tv_language=(TextView) view.findViewById(R.id.tv_language);
            holder.im_language=(ImageView) view.findViewById(R.id.im_language);

            holder.tv_language.setText(result.get(position));
            Picasso.with(context).load(imageId[position]).into(holder.im_language);


            return view;
        }


    }

}



