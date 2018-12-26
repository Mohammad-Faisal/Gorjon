package candor.fulki.chat.meeting;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.support.v7.widget.GridLayoutManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import candor.fulki.general.MainActivity;
import candor.fulki.R;
import candor.fulki.models.MeetingRooms;
import candor.fulki.utils.PreferenceManager;

public class MeetingFragment extends Fragment {




    //for recyclcer view
    private List<MeetingRooms> meetingRooms;
    private MeetingRoomsAdapter meetingRoomsAdapter;

    // --- FIREBASE ----//
    DatabaseReference mRootRef;
    private String mUserID;

    public MeetingFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view =  inflater.inflate(R.layout.fragment_meeting, container, false);



        //setting RecyclerView
        meetingRooms = new ArrayList<>();
        android.support.v7.widget.RecyclerView recyclerView = view.findViewById(R.id.meeting_recycler);
        recyclerView.hasFixedSize();
        recyclerView.setLayoutManager(new GridLayoutManager(getContext() , 2));
        meetingRoomsAdapter = new MeetingRoomsAdapter(meetingRooms, getContext());
        recyclerView.setAdapter(meetingRoomsAdapter);

        mUserID = new PreferenceManager(Objects.requireNonNull(getContext())).getUserId();
        mRootRef = FirebaseDatabase.getInstance().getReference();


        mRootRef.child("meetings").addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(@NonNull DataSnapshot dataSnapshot, String s) {
                MeetingRooms meetings = dataSnapshot.getValue(MeetingRooms.class);
                meetingRooms.add(0,meetings);
                meetingRoomsAdapter.notifyDataSetChanged();
            }

            @Override
            public void onChildChanged(@NonNull DataSnapshot dataSnapshot, String s) {

            }

            @Override
            public void onChildRemoved(@NonNull DataSnapshot dataSnapshot) {

            }

            @Override
            public void onChildMoved(@NonNull DataSnapshot dataSnapshot, String s) {

            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });


        return view;
    }

}
