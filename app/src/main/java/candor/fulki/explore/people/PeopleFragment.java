package candor.fulki.explore.people;


import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentChange;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;

import java.util.ArrayList;
import java.util.List;


import candor.fulki.profile.ListPeopleAdapter;
import candor.fulki.models.UserBasic;
import candor.fulki.R;
import timber.log.Timber;


public class PeopleFragment extends Fragment {

    private static final String TAG = "PeopleFragment";

    FirebaseFirestore firebaseFirestore = FirebaseFirestore.getInstance();
    //containers
    private RecyclerView mPeopleList;
    private DocumentSnapshot lastVisible = null;
    private final List<UserBasic> userList = new ArrayList<>();
    private LinearLayoutManager mLinearLayout;
    private ListPeopleAdapter mPeopleAdapter;
    private boolean isFirstPageLoad = true;
    String mUserID;


    public PeopleFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment

        View view = inflater.inflate(R.layout.fragment_people, container, false);


        mUserID = FirebaseAuth.getInstance().getCurrentUser().getUid();

        if(mUserID!=null){
            mPeopleAdapter = new ListPeopleAdapter(userList , getContext() , getActivity());
            mPeopleList = view.findViewById(R.id.fragment_peopole_recycler);
            mLinearLayout = new LinearLayoutManager(getContext());
            mPeopleList.hasFixedSize();
            mPeopleList.setLayoutManager(mLinearLayout);
            mPeopleList.setAdapter(mPeopleAdapter);


            loadFirstData();

            mPeopleList.addOnScrollListener(new RecyclerView.OnScrollListener() {
                @Override
                public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
                    Boolean reachedBottom = !recyclerView.canScrollVertically(1);
                    if(reachedBottom){
                        Log.d(TAG, "onScrolled:      reached bottom ");
                        loadMorePost();
                    }
                }
            });


        }

        return view;
    }


    private void loadFirstData(){



        Query nextQuery = firebaseFirestore.collection("ratings")
                .limit(30);
        nextQuery.addSnapshotListener(getActivity(), (documentSnapshots, e) -> {
            if(documentSnapshots!=null){
                if(!documentSnapshots.isEmpty()){
                    if(isFirstPageLoad==true){
                        lastVisible = documentSnapshots.getDocuments().get(documentSnapshots.size()-1);
                    }
                    for(DocumentChange doc: documentSnapshots.getDocumentChanges()){
                        if(doc.getType() == DocumentChange.Type.ADDED){

                            UserBasic basic = new UserBasic();
                            basic.setmUserID(doc.getDocument().getString("user_id"));
                            basic.setmUserName(doc.getDocument().getString("name"));
                            basic.setmUserThumbImage(doc.getDocument().getString("thumb_image"));
                            userList.add(basic);
                            mPeopleAdapter.notifyDataSetChanged();
                        }
                    }
                    isFirstPageLoad = false;
                }
            }else{
                Timber.d("onCreate:   document snapshot is null");
            }
        });



        //actually for rating

        /*Query nextQuery = firebaseFirestore.collection("ratings")
                .orderBy("rating" , Query.Direction.DESCENDING)
                .limit(10);
        nextQuery.addSnapshotListener(getActivity(), (documentSnapshots, e) -> {
            if(documentSnapshots!=null){
                if(!documentSnapshots.isEmpty()){
                    if(isFirstPageLoad==true){
                        lastVisible = documentSnapshots.getDocuments().get(documentSnapshots.size()-1);
                    }
                    for(DocumentChange doc: documentSnapshots.getDocumentChanges()){
                        if(doc.getType() == DocumentChange.Type.ADDED){

                            UserBasic basic = new UserBasic();
                            basic.setmUserID(doc.getDocument().getString("user_id"));
                            basic.setmUserName(doc.getDocument().getString("user_name"));
                            basic.setmUserThumbImage(doc.getDocument().getString("thumb_image"));

                            if(isFirstPageLoad){
                                userList.add(basic);
                            }else{
                                userList.add(0,basic);
                            }
                            mPeopleAdapter.notifyDataSetChanged();
                        }
                    }
                    isFirstPageLoad = false;
                }
            }else{
                Log.d(TAG, "onCreate:   document snapshot is null");
            }
        });*/
    }


    public void loadMorePost(){

       /* Query nextQuery = firebaseFirestore.collection("ratings")
                .orderBy("rating" , Query.Direction.DESCENDING)
                .startAfter(lastVisible)
                .limit(200);

        nextQuery.addSnapshotListener(getActivity() , (documentSnapshots, e) -> {
            if(!documentSnapshots.isEmpty()){
                lastVisible = documentSnapshots.getDocuments().get(documentSnapshots.size()-1);
                for(DocumentChange doc: documentSnapshots.getDocumentChanges()){
                    if(doc.getType() == DocumentChange.Type.ADDED){
                        UserBasic basic = new UserBasic();
                        basic.setmUserID(doc.getDocument().getString("user_id"));
                        basic.setmUserName(doc.getDocument().getString("user_name"));
                        basic.setmUserThumbImage(doc.getDocument().getString("thumb_image"));

                        userList.add(basic);
                        mPeopleAdapter.notifyDataSetChanged();

                    }
                }
            }
        });*/
    }





}
