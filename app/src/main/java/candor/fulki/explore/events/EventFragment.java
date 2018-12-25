package candor.fulki.explore.events;


import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.widget.GridLayoutManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.google.firebase.firestore.DocumentChange;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;

import java.util.ArrayList;
import java.util.List;

import candor.fulki.R;
import candor.fulki.models.Events;

public class EventFragment extends Fragment {



    private List<Events> eventsList;
    private EventsAdapter eventsAdapter;
    FirebaseFirestore firebaseFirestore;


    public EventFragment() {
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View mView = inflater.inflate(R.layout.fragment_event, container, false);



        firebaseFirestore = FirebaseFirestore.getInstance();

        //setting RecyclerView
        eventsList = new ArrayList<>();
        android.support.v7.widget.RecyclerView recyclerView = mView.findViewById(R.id.fragment_event_recycler);
        recyclerView.hasFixedSize();
        recyclerView.setLayoutManager(new GridLayoutManager(getContext() , 1));
        eventsAdapter = new EventsAdapter(eventsList, getContext() , getActivity());
        recyclerView.setAdapter(eventsAdapter);

        loadEvents();

        return mView;
    }



    public void loadEvents(){
        firebaseFirestore = FirebaseFirestore.getInstance();
        Query nextQuery = firebaseFirestore.collection("events").orderBy("timestamp" , Query.Direction.DESCENDING).limit(100);
        nextQuery.addSnapshotListener(getActivity() , (documentSnapshots, e) -> {
            if(!documentSnapshots.isEmpty()){

                for(DocumentChange doc: documentSnapshots.getDocumentChanges()){
                    if(doc.getType() == DocumentChange.Type.ADDED){
                        Events singleEvents = doc.getDocument().toObject(Events.class);
                        eventsList.add(singleEvents);
                        eventsAdapter.notifyDataSetChanged();
                    }
                }
            }
        });
    }




}
