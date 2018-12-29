package candor.fulki.fragments;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.google.firebase.firestore.DocumentChange;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;

import java.util.ArrayList;
import java.util.List;

import candor.fulki.adapters.PostsAdapter;
import candor.fulki.models.Posts;
import candor.fulki.R;

public class PostsFragment extends Fragment {



    private RecyclerView recyclerView;
    private FirebaseFirestore firebaseFirestore;
    private PostsAdapter mHomeAdapter;


    //new
    private List<Posts> posts;
    private PostsAdapter mPostsAdapter;

    public PostsFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {


        View view = inflater.inflate(R.layout.fragment_posts, container, false);



        posts = new ArrayList<>();
        recyclerView = view.findViewById(R.id.fragment_post_recycler);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        recyclerView.setNestedScrollingEnabled(false);
        mPostsAdapter = new PostsAdapter(posts, getContext(), getActivity());
        recyclerView.setAdapter(mPostsAdapter);




        loadFirstPosts();

        return view;
    }


    public void loadFirstPosts(){
        firebaseFirestore = FirebaseFirestore.getInstance();


        Query nextQuery = firebaseFirestore.collection("posts").orderBy("like_cnt" , Query.Direction.DESCENDING).limit(50);
        nextQuery.addSnapshotListener(getActivity(), (documentSnapshots, e) -> {
            if(documentSnapshots!=null){
                if(!documentSnapshots.isEmpty()){
                    for(DocumentChange doc: documentSnapshots.getDocumentChanges()){
                        if(doc.getType() == DocumentChange.Type.ADDED){
                            Posts singlePosts = doc.getDocument().toObject(Posts.class);
                            String uid = singlePosts.getPrimary_user_id();
                            posts.add(singlePosts);
                            mPostsAdapter.notifyDataSetChanged();
                        }
                    }
                }
            }else{
            }
        });
    }
}
