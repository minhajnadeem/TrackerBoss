package com.mj.minhajlib.trackerboss;

import android.provider.ContactsContract;
import android.support.constraint.solver.widgets.Snapshot;
import android.support.v4.app.FragmentActivity;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.mj.minhajlib.model.UserModel;

import java.util.ArrayList;
import java.util.List;

public class MapsActivity extends FragmentActivity implements OnMapReadyCallback {

    private List<UserModel> mUserModelList;
    private UserModel mModel;
    private GoogleMap mMap;
    private DatabaseReference mDatabaseReference;
    private ChildEventListener mChildEventListener;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);
        mUserModelList = new ArrayList<>();
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);
        mDatabaseReference = FirebaseDatabase.getInstance().getReference("users");
        mChildEventListener = new ChildEventListener() {
            @Override
            public void onChildAdded(DataSnapshot dataSnapshot, String s) {
                mUserModelList.add(dataSnapshot.getValue(UserModel.class));
                trackLocation();
                Toast.makeText(MapsActivity.this, "added", Toast.LENGTH_SHORT).show();
            }
            @Override
            public void onChildChanged(DataSnapshot dataSnapshot, String s) {
                int index = -1;
                Log.d("tracker","previous child :"+s);
                UserModel changedModel = dataSnapshot.getValue(UserModel.class);
                for (UserModel model : mUserModelList){
                    if (model.getName().equals(changedModel.getName())){
                        index = mUserModelList.indexOf(model);
                        break;
                    }
                }
                Log.d("tracker","index ="+index);
                mUserModelList.set(index,changedModel);
                trackLocation();
            }
            @Override
            public void onChildRemoved(DataSnapshot dataSnapshot) {}
            @Override
            public void onChildMoved(DataSnapshot dataSnapshot, String s) {}
            @Override
            public void onCancelled(DatabaseError databaseError) {}
        };
        mDatabaseReference.addChildEventListener(mChildEventListener);
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
    }

    private void trackLocation(){
        mMap.clear();
        for (UserModel userModel : mUserModelList){
            Log.d("tracker","adding marker for:"+userModel.getName());
            LatLng latLng = new LatLng(userModel.getLat(),userModel.getLan());
            mMap.addMarker(new MarkerOptions().position(latLng).title(userModel.getName()));
            mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(latLng,18));
        }
    }
}
