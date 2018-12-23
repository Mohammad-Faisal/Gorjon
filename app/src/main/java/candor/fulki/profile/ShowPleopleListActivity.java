package candor.fulki.profile;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;

import com.google.firebase.firestore.DocumentChange;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import candor.fulki.R;
import candor.fulki.models.UserBasic;
import timber.log.Timber;

public class ShowPleopleListActivity extends AppCompatActivity {



    private final List<UserBasic> userList = new ArrayList<>();
    private ListPeopleAdapter mPeopleAdapter;


    private FirebaseFirestore firebaseFirestore;
    private DocumentSnapshot lastVisible;
    String type = "";
    String userID = "";



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_show_pleople_list);



        //jar profile e asi
        type = getIntent().getStringExtra("type");
        userID = getIntent().getStringExtra("user_id");

        Timber.d("onCreate:  found type    :   %s", type);
        Timber.d("onCreate:  found user id :   %s", userID);

        Objects.requireNonNull(getSupportActionBar()).setTitle(type);
        //getSupportActionBar().setHomeButtonEnabled(true);


        mPeopleAdapter = new ListPeopleAdapter(userList , ShowPleopleListActivity.this , ShowPleopleListActivity.this);
        RecyclerView mPeopleList = findViewById(R.id.show_people_list);
        LinearLayoutManager mLinearLayout = new LinearLayoutManager(this);
        mPeopleList.hasFixedSize();
        mPeopleList.setLayoutManager(mLinearLayout);
        mPeopleList.setAdapter(mPeopleAdapter);

        loadFirstData();

        /*if(!isFirstPageLoaded)loadFirstData();
        mPeopleList.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
                Boolean reachedBottom = !recyclerView.canScrollVertically(1);
                if(reachedBottom){
                    if(isFirstPageLoaded)loadMoreData();
                }
            }
        });*/
    }

    private void loadFirstData(){
        firebaseFirestore = FirebaseFirestore.getInstance();
        Query nextQuery = firebaseFirestore.collection(type+"/" + userID + "/"+type)
                .orderBy("timestamp" , Query.Direction.DESCENDING)
                .limit(100);
        nextQuery.addSnapshotListener(ShowPleopleListActivity.this , (documentSnapshots, e) -> {
            if(!documentSnapshots.isEmpty()){
                lastVisible = documentSnapshots.getDocuments().get(documentSnapshots.size()-1);
                //isFirstPageLoaded = true;
                for(DocumentChange doc: documentSnapshots.getDocumentChanges()){
                    if(doc.getType() == DocumentChange.Type.ADDED){
                        Timber.d("loadFirstData:  found a data%s", doc.getDocument().getId());
                        UserBasic basic = new UserBasic();
                        basic.setmUserID(doc.getDocument().getString("id"));
                        basic.setmUserName(doc.getDocument().getString("name"));
                        basic.setmUserThumbImage(doc.getDocument().getString("thumb_image"));
                        userList.add(basic);
                        mPeopleAdapter.notifyDataSetChanged();
                    }
                }
            }
        });
    }

    private void loadMoreData() {
        if(lastVisible.exists()){
            Query nextQuery = firebaseFirestore.collection(type+"/" + userID + "/"+type)
                    .orderBy("timestamp" , Query.Direction.DESCENDING)
                    .startAfter(lastVisible)
                    .limit(5);
            nextQuery.addSnapshotListener(ShowPleopleListActivity.this , (documentSnapshots, e) -> {
                if(!documentSnapshots.isEmpty()){
                    lastVisible = documentSnapshots.getDocuments().get(documentSnapshots.size()-1);
                    for(DocumentChange doc: documentSnapshots.getDocumentChanges()){
                        if(doc.getType() == DocumentChange.Type.ADDED){

                            UserBasic basic = new UserBasic();
                            basic.setmUserID(doc.getDocument().getString("id"));
                            basic.setmUserName(doc.getDocument().getString("name"));
                            basic.setmUserThumbImage(doc.getDocument().getString("thumb_image"));
                            userList.add(basic);
                            mPeopleAdapter.notifyDataSetChanged();
                        }
                    }
                }
            });
        }
    }
}
