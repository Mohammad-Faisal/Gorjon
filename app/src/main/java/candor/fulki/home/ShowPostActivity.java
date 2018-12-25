package candor.fulki.home;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import com.daimajia.slider.library.Animations.DescriptionAnimation;
import com.daimajia.slider.library.SliderLayout;
import com.daimajia.slider.library.SliderTypes.BaseSliderView;
import com.daimajia.slider.library.SliderTypes.TextSliderView;
import com.daimajia.slider.library.Tricks.ViewPagerEx;
import com.google.firebase.firestore.DocumentChange;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.WriteBatch;
import com.like.LikeButton;
import com.like.OnLikeListener;
import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoader;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;

import candor.fulki.models.Ratings;
import candor.fulki.models.Comments;
import candor.fulki.models.Likes;
import candor.fulki.models.Notifications;
import candor.fulki.profile.ShowPleopleListActivity;
import candor.fulki.R;
import de.hdodenhof.circleimageview.CircleImageView;
import timber.log.Timber;

public class ShowPostActivity extends AppCompatActivity implements BaseSliderView.OnSliderClickListener, ViewPagerEx.OnPageChangeListener{


    private String mPostID , mUserID ;
    private String mUserName , mUserThumbImage ;
    FirebaseFirestore firebaseFirestore = FirebaseFirestore.getInstance();



    public TextView postUserName,postCaption,postDateTime,postLocaiton,postLikeCount,postCommentCount;
    public CircleImageView postUserImage , mShowPostOwnImage;
    public LikeButton postLikeButton;

    SliderLayout postSlider;
    String primary_user_id , secondary_user_id , primary_push_id , secondary_push_id , timedate , location , caption , type , privacy;
    long like_cnt, comment_cnt ,share_cnt;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_show_post);


        postUserName =findViewById(R.id.post_user_name);
        postCaption = findViewById(R.id.post_caption);
        postDateTime =findViewById(R.id.post_time_date);
        postLocaiton = findViewById(R.id.post_location);
        postUserImage = findViewById(R.id.post_user_single_imagee);
        postLikeButton = findViewById(R.id.post_like_button);
        postLikeCount = findViewById(R.id.post_like_number);
        postCommentCount = findViewById(R.id.show_post_comment_count);
        mShowPostOwnImage = findViewById(R.id.show_post_own_image);
        postSlider = findViewById(R.id.post_slider);


        mPostID = getIntent().getStringExtra("postID");
        loadDetails();


        postLikeCount.setOnClickListener(v -> {
            addRating(mUserID , 1);
            Intent showPeopleIntent = new Intent(ShowPostActivity.this , ShowPleopleListActivity.class);
            showPeopleIntent.putExtra("type" , "likes");
            showPeopleIntent.putExtra("user_id" ,mPostID );
            startActivity(showPeopleIntent);
        });


        firebaseFirestore.collection("posts").document(mPostID).get().addOnCompleteListener(task -> {
            if(task.isSuccessful()){
                if(task.getResult().exists()){

                    CombinedPosts post = task.getResult()
                            .toObject(CombinedPosts.class);


                    HashMap<String,String> Hash_file_maps;
                    Hash_file_maps = post.getPost_thumb_url();
                    setImages(Hash_file_maps);


                    primary_user_id = post.getPrimary_user_id();
                    secondary_user_id = post.getSecondary_user_id();
                    primary_push_id = post.getPrimary_push_id();
                    secondary_push_id = post.getSecondary_push_id();
                    timedate = post.getTime_and_date();
                    location = post.getLocation();
                    caption = post.getCaption();
                    type = post.getType();
                    privacy = post.getPrivacy();
                    like_cnt = post.getLike_cnt();
                    comment_cnt = post.getComment_cnt();
                    share_cnt = post.getShare_cnt();


                    setPostCaption(caption);
                    setPostDateTime(timedate);
                    setPostLikeCount(like_cnt);
                    setPostCommentCount(comment_cnt);


                    firebaseFirestore.collection("users").document(primary_user_id).get().addOnSuccessListener(documentSnapshot -> {
                        if(documentSnapshot.exists()){
                            String primaryUserName = documentSnapshot.getString("name");
                            String primaryUserThumbImage =documentSnapshot.getString("thumb_image");
                            setPostUserName(primaryUserName);
                            setUserImage(primaryUserThumbImage , postUserImage);
                        }
                    }).addOnFailureListener(e -> {
                        String primaryUserName = "User Name";
                        String primaryUserThumbImage = "default";
                        setPostUserName(primaryUserName);
                        setUserImage(primaryUserThumbImage , postUserImage);
                    });


                }else{
                    Timber.d("onComplete: an error occured while loading the image");
                }
            }
        });

        //setting the current state of like button
        FirebaseFirestore.getInstance().collection("likes/" + mPostID + "/likes").document(mUserID).get().addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                if (task.getResult().exists()) {
                    postLikeButton.setLiked(true);
                } else {
                    postLikeButton.setLiked(false);
                }
            } else {
                postLikeButton.setLiked(false);
            }
        });

        //handling the like onclick listener
        postLikeButton.setOnLikeListener(new OnLikeListener() {
            @Override
            public void liked(LikeButton likeButton) {



                //building like
                postLikeButton.setLiked(true);

                //building like
                String time_stamp = String.valueOf(new Date().getTime());
                DocumentReference ref = FirebaseFirestore.getInstance().collection("notifications/"+primary_user_id+"/notificatinos").document();
                String likeNotificatoinPushID = ref.getId();

                Likes mLikes = new Likes(mUserID , mUserName , mUserThumbImage ,likeNotificatoinPushID , time_stamp);
                Notifications pushNoti = new Notifications( "like" ,mUserID ,primary_user_id , mPostID ,likeNotificatoinPushID , time_stamp,"n"  );


                WriteBatch writeBatch  = firebaseFirestore.batch();

                DocumentReference notificatinoRef = firebaseFirestore.collection("notifications/"+primary_user_id+"/notificatinos").document(likeNotificatoinPushID);
                writeBatch.set(notificatinoRef, pushNoti);

                DocumentReference postNotificatinoRef =  firebaseFirestore.collection("posts/"+mPostID+"/notifications").document(likeNotificatoinPushID);
                writeBatch.set(postNotificatinoRef, pushNoti);

                DocumentReference postLikeRef =  firebaseFirestore.collection("likes/" + mPostID + "/likes").document(mUserID);
                writeBatch.set(postLikeRef, mLikes);

                writeBatch.commit().addOnSuccessListener(aVoid -> {
                    addRating(mUserID , 3);
                    addRating( primary_user_id , 1);
                    addLike(mPostID , 1);
                    Timber.d("liked:   like is successful");

                }).addOnFailureListener(e -> Timber.d("liked:   like is not succesful"));
            }
            @Override
            public void unLiked(LikeButton likeButton) {
               postLikeButton.setLiked(false);
                firebaseFirestore.collection("likes/" + mPostID + "/likes").document(mUserID).get().addOnCompleteListener(task -> {
                    if(task.isSuccessful()){
                        if(task.getResult().exists()){
                            String likeNotificatoinPushID = task.getResult().getString("notificationID");
                            WriteBatch writeBatch = FirebaseFirestore.getInstance().batch();
                            if(likeNotificatoinPushID!=null){
                                writeBatch.delete(firebaseFirestore.collection("notifications/"+primary_user_id+"/notificatinos").document(likeNotificatoinPushID));
                                writeBatch.delete(firebaseFirestore.collection("posts/"+mPostID+"/notifications").document(likeNotificatoinPushID));
                            }
                            writeBatch.delete(firebaseFirestore.collection("likes/" + mPostID + "/likes").document(mUserID));
                            writeBatch.commit().addOnSuccessListener(aVoid -> {
                                addRating(mUserID , -3);
                                addRating( primary_user_id , -1);
                                addLike(mPostID , -1);
                            });
                        }
                    }
                });
            }
        });



        //setting comment count
        FirebaseFirestore.getInstance().collection("comments/" + primary_push_id + "/comments").addSnapshotListener((documentSnapshots, e) -> {
            if (!documentSnapshots.isEmpty()) {
                long count = documentSnapshots.size();
                firebaseFirestore.collection("posts").document(primary_push_id).update("comment_cnt" , count);
                if (count == 1) {
                    setPostCommentCount(count);
                } else {
                    setPostCommentCount(count);
                }
            } else {
                setPostCommentCount(0);
            }
        });

        //setting comment list
        final RecyclerView mCommentList;
        final List<Comments> commentList = new ArrayList<>();
        LinearLayoutManager mLinearLayout;


        //--------- SETTING THE COMMENT ADAPTERS --//
        final PostCommentAdapter mPostCommentAdapter = new PostCommentAdapter(commentList, this , ShowPostActivity.this);
        mCommentList = findViewById(R.id.show_post_recycler);
        mLinearLayout = new LinearLayoutManager(this);
        mCommentList.hasFixedSize();
        mCommentList.setLayoutManager(mLinearLayout);
        mCommentList.setAdapter(mPostCommentAdapter);


        //-------------LOADING COMMENTS------------//
        firebaseFirestore = FirebaseFirestore.getInstance();
        Query nextQuery = firebaseFirestore.collection("comments/"+mPostID+"/comments").orderBy("time_stamp" , Query.Direction.DESCENDING);
        nextQuery.addSnapshotListener((documentSnapshots, e) -> {
            for(DocumentChange doc: documentSnapshots.getDocumentChanges()){
                if(doc.getType() == DocumentChange.Type.ADDED){
                    Comments singleComment = doc.getDocument().toObject(Comments.class);
                    commentList.add(singleComment);
                    mPostCommentAdapter.notifyDataSetChanged();
                }
            }
        });


        //posting comments
        final TextView commentBox = findViewById(R.id.comment_write);
        ImageButton commentPost = findViewById(R.id.comment_post);
        commentPost.setOnClickListener(view -> {

            addRating(mUserID , 5);
            addRating( primary_user_id , 2);

            String time_stamp = String.valueOf(new Date().getTime());

            DocumentReference notiRef = FirebaseFirestore.getInstance().collection("notifications/"+primary_user_id+"/notificatinos").document();
            String commentNotificatoinPushID = notiRef.getId();

            DocumentReference commentRef = FirebaseFirestore.getInstance().collection("comments/"+mPostID+"/comments").document();
            String commentID = commentRef.getId();

            Notifications pushNoti = new Notifications( "comment" ,mUserID ,primary_user_id, mPostID ,commentNotificatoinPushID , time_stamp,"n"  );
            Comments  comment =  new Comments(commentBox.getText().toString() , mUserID ,commentID, mPostID  , commentNotificatoinPushID  , time_stamp);
            commentBox.setText("");

            WriteBatch writeBatch  = firebaseFirestore.batch();

            writeBatch.set(notiRef, pushNoti);
            DocumentReference postNotificatinoRef =  firebaseFirestore.collection("posts/"+mPostID+"/notifications").document(commentNotificatoinPushID);
            writeBatch.set(postNotificatinoRef, pushNoti);

            writeBatch.set(commentRef, comment);

            writeBatch.commit().addOnSuccessListener(aVoid -> Timber.d("liked:   like is successful")).addOnFailureListener(e -> Timber.d("liked:   like is not succesful"));


        });

    }


    private void setPostDateTime(String dateTime) {
        postDateTime.setText(dateTime);
    }

    @SuppressLint("SetTextI18n")
    private  void setPostLikeCount(long currentLikesCount) {
        if(currentLikesCount>1){
            postLikeCount.setText(""+currentLikesCount+" likes");
        }else{
            postLikeCount.setText(""+currentLikesCount+" like");
        }
    }

    @SuppressLint("SetTextI18n")
    private  void setPostCommentCount(long currentCommentsCount) {
        if(currentCommentsCount>1){
            postCommentCount.setText(""+currentCommentsCount+" comments");
        }else{
            postCommentCount.setText(""+currentCommentsCount+" comment");
        }
    }
    private void setPostCaption(String Caption) {
        postCaption.setText(Caption);
    }

    private void setUserImage(String image_url , ImageView imageView) {
        if(image_url!=null){
            if(image_url.equals("default")){
                Timber.d("setUserImage:    visibility gone but caption is  %s", image_url);
                //postImage.setVisibility(View.GONE);
            }else{
                DisplayImageOptions options = new DisplayImageOptions.Builder()
                        .cacheInMemory(true)
                        .cacheOnDisk(true)
                        .build();
                ImageLoader.getInstance().displayImage(image_url, imageView, options);
            }
        }
    }
    private void setPostUserName(String userName) {
        postUserName.setText(userName);
    }


    private void setImages(HashMap<String , String> Hash_file_maps) {
        if (Hash_file_maps.size() > 0) {
            for (String name : Hash_file_maps.keySet()) {
                TextSliderView textSliderView = new TextSliderView(this);
                textSliderView
                        .description(Hash_file_maps.get(name))
                        .image(name)
                        .setScaleType(BaseSliderView.ScaleType.FitCenterCrop)
                        .setOnSliderClickListener( this);
                textSliderView.bundle(new Bundle());
                textSliderView.getBundle()
                        .putString("extra", name);
                postSlider.addSlider(textSliderView);
            }
            postSlider.setPresetTransformer(SliderLayout.Transformer.Accordion);
            postSlider.setPresetIndicator(SliderLayout.PresetIndicators.Center_Bottom);
            postSlider.setCustomAnimation(new DescriptionAnimation());
            postSlider.setDuration(3000);
            postSlider.addOnPageChangeListener(ShowPostActivity.this);
            postSlider.setCustomIndicator( findViewById(R.id.custom_indicator));
        } else {
            if(postSlider!=null)postSlider.setVisibility(View.GONE);

        }
    }
    @Override
    public void onSliderClick(BaseSliderView slider) {

    }
    @Override
    public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {

    }
    @Override
    public void onPageSelected(int position) {

    }
    @Override
    public void onPageScrollStateChanged(int state) {

    }


    private void addRating(String mUserID  , int factor) {

        FirebaseFirestore firebaseFirestore = FirebaseFirestore.getInstance();
        Timber.d("addRating:   function calledd !!!!");
        final DocumentReference ratingRef = FirebaseFirestore.getInstance().collection("ratings")
                .document(mUserID);
        firebaseFirestore.runTransaction(transaction -> {

            Ratings ratings = transaction.get(ratingRef)
                    .toObject(Ratings.class);
            long curRating = ratings.getRating();
            long nextRating = curRating + factor;

            ratings.setRating(nextRating);
            transaction.set(ratingRef, ratings);
            return null;
        });
    }

    @SuppressLint("SetTextI18n")
    private void setLikeCount(long likeCnt){
        if(likeCnt>1){
            postLikeCount.setText(""+likeCnt+" likes");
        }else{
            postLikeCount.setText(""+likeCnt+" like");
        }
    }

    private void addLike( String mPostID , int factor) {
        FirebaseFirestore firebaseFirestore = FirebaseFirestore.getInstance();
        Timber.d("addLike:   function calledd !!!!");
        final DocumentReference postRef = FirebaseFirestore.getInstance().collection("posts")
                .document(mPostID);

        firebaseFirestore.runTransaction(transaction -> {
            CombinedPosts post = transaction.get(postRef)
                    .toObject(CombinedPosts.class);
            long curLikes = post.getLike_cnt();
            long nextLike = curLikes + factor;

            Timber.d("addLike:     like number is  %s", nextLike);
            HashMap< String ,  Object > updateMap = new HashMap<>();

            updateMap.put("like_cnt" , nextLike);
            transaction.update(postRef , updateMap);

            return nextLike;
        }).addOnSuccessListener(this::setLikeCount);
    }

    private void loadDetails(){
        android.content.SharedPreferences sp = getSharedPreferences(candor.fulki.general.Constants.SHARED_PREF_NAME, MODE_PRIVATE);
        mUserID = sp.getString(candor.fulki.general.Constants.Id, null);
        mUserName = sp.getString(candor.fulki.general.Constants.Name, null);
        mUserThumbImage = sp.getString(candor.fulki.general.Constants.ThumbImage, null);
    }


}


