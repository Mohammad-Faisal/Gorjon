package candor.fulki.home;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityOptionsCompat;
import android.support.v4.util.Pair;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.WriteBatch;
import com.nostra13.universalimageloader.core.ImageLoader;

import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Objects;

import candor.fulki.models.Ratings;
import candor.fulki.general.Functions;
import candor.fulki.utils.GetTimeAgo;
import candor.fulki.general.MainActivity;
import candor.fulki.models.Comments;
import candor.fulki.models.Likes;
import candor.fulki.models.Notifications;
import candor.fulki.profile.ProfileActivity;
import candor.fulki.R;
import candor.fulki.utils.PreferenceManager;
import de.hdodenhof.circleimageview.CircleImageView;
import timber.log.Timber;

/**
 * Created by Mohammad Faisal on 1/29/2018.
 */

public class PostCommentAdapter extends RecyclerView.Adapter<PostCommentAdapter.PostCommentViewHolder> {



    private static final String TAG= "PostCommentAdapter";
    private List<Comments> mCommentList;
    DatabaseReference mRootRef = FirebaseDatabase.getInstance().getReference();
    Context context;
    Activity activity;
    boolean isLiked = false;


    private String mUserName , mUserID, mUserThumbImage;
    private PreferenceManager preferenceManager;


    FirebaseFirestore firebaseFirestore = FirebaseFirestore.getInstance();

    // ---- CONSTRUCTOR --//
    public PostCommentAdapter(List<Comments> mCommentList , Context context , Activity activity){
        this.mCommentList = mCommentList;
        this.context = context;
        this.activity = activity;
        preferenceManager = new PreferenceManager(context);
        mUserID =preferenceManager.getUserId();
        mUserName = preferenceManager.getUserName();
        mUserThumbImage = preferenceManager.getUserThumbImage();
    }

    @NonNull
    @Override
    public PostCommentAdapter.PostCommentViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v;
        v = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_comment, parent , false);
        return new PostCommentAdapter.PostCommentViewHolder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull final PostCommentAdapter.PostCommentViewHolder holder, int position) {
        Comments c = mCommentList.get(position);
        holder.commentText.setText(c.getComment());
        final String mCurrentCommenterID = c.getUid();  //who has posted the comment
        final String mCommentId  = c.getCommentId();
        final String mPostID = c.getPostID();
        final String mTimeStamp = c.getTime_stamp();


        //setting comment time ago
        GetTimeAgo ob = new GetTimeAgo();
        long time = Long.parseLong(mTimeStamp);
        String time_ago = GetTimeAgo.getTimeAgo(time ,context);
        holder.commentTimeAgo.setText(time_ago);


        //setting user details
        FirebaseFirestore.getInstance().collection("users").document(mCurrentCommenterID).get().addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                String mUserName = task.getResult().getString("name");
                String mUserImage = task.getResult().getString("thumb_image");
                holder.setUserImage(mUserImage);
                holder.setPostUserName(mUserName);
            } else {

                Timber.d("onComplete: " + task.getException().toString());
            }
        });
        //setting like count
        FirebaseFirestore.getInstance().collection("comment_likes/" + mPostID + "/"+mCommentId).addSnapshotListener((documentSnapshots, e) -> {
            if (!documentSnapshots.isEmpty()) {
                int count = documentSnapshots.size();
                String cnt = Integer.toString(count);
                if (count == 1) {
                    holder.commentLoveCount.setText(cnt );
                } else {
                    holder.commentLoveCount.setText(cnt );
                }

            } else {
                holder.commentLoveCount.setText("0");
            }
        });
        //setting the current state of comment like
        FirebaseFirestore.getInstance().collection("comment_likes/" + mPostID + "/"+mCommentId).document(mUserID).get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                if (task.isSuccessful()) {
                    if (task.getResult().exists()) {
                        isLiked= true;
                        holder.commentLove.setBackgroundResource(R.drawable.ic_love_full);
                    } else {
                        isLiked= false;
                        holder.commentLove.setBackgroundResource(R.drawable.ic_love_empty);
                    }
                } else {
                    isLiked = false;
                    Timber.tag(TAG).w(task.getException(), "onComplete: ");
                    holder.commentLove.setBackgroundResource(R.drawable.ic_love_empty);
                }
            }
        });



        holder.commentLove.setOnClickListener(v -> {
            if(!isLiked){
                isLiked = true;
                holder.commentLove.setBackgroundResource(R.drawable.ic_love_full);
                Functions f = new Functions();

                holder.addRating(mUserID , 3);
                holder.addRating(mCurrentCommenterID , 1);

                //building comment
                String time_stamp = String.valueOf(new Date().getTime());
                DocumentReference ref = FirebaseFirestore.getInstance().collection("notifications/"+mCurrentCommenterID+"/notificatinos").document();
                String likeNotificatoinPushID = ref.getId();
                Likes mLikes = new Likes(mUserID , mUserName , mUserThumbImage ,likeNotificatoinPushID , time_stamp);
                Notifications pushNoti = new Notifications( "comment_like" ,mUserID , mCurrentCommenterID, mPostID ,likeNotificatoinPushID , time_stamp,"n"  );

                WriteBatch writeBatch = FirebaseFirestore.getInstance().batch();
                writeBatch.set(firebaseFirestore.collection("posts/"+mPostID+"/notifications").document(likeNotificatoinPushID),pushNoti);
                writeBatch.set(firebaseFirestore.collection("notifications/"+mCurrentCommenterID+"/notificatinos").document(likeNotificatoinPushID),pushNoti);
                writeBatch.set(firebaseFirestore.collection("comment_likes/" + mPostID + "/"+mCommentId).document(mUserID), mLikes);
                writeBatch.commit().addOnCompleteListener(task -> {
                    if(task.isSuccessful()){
                        Timber.d("onComplete:    comment love is successfull");
                    }else{
                        Timber.d("onComplete:   comment love is not succesful");
                    }
                });

              /*  firebaseFirestore.collection("notifications/"+mCurrentCommenterID+"/notificatinos").document(likeNotificatoinPushID).set(pushNoti);
                firebaseFirestore.collection("posts/"+mPostID+"/notifications").document(likeNotificatoinPushID).set(pushNoti);
                firebaseFirestore.collection("comment_likes/" + mPostID + "/"+mCommentId).document(mUserID).set(mLikes);*/

            }else{

                holder.addRating(mUserID , -3);
                holder.addRating(mCurrentCommenterID , -1);
                isLiked = false;
                holder.commentLove.setBackgroundResource(R.drawable.ic_love_empty);
                firebaseFirestore.collection("comment_likes/" + mPostID + "/"+mCommentId).document(mUserID).get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                        if (task.isSuccessful()) {
                            DocumentSnapshot document = task.getResult();
                            if (document.exists()) {

                                WriteBatch writeBatch = FirebaseFirestore.getInstance().batch();
                                String likeNotificatoinPushID = document.getString("notificationID");


                                if(likeNotificatoinPushID!=null){
                                    writeBatch.delete(firebaseFirestore.collection("posts/"+mPostID+"/notifications").document(likeNotificatoinPushID));
                                    writeBatch.delete(firebaseFirestore.collection("notifications/"+mCurrentCommenterID+"/notificatinos").document(likeNotificatoinPushID));
                                }
                                writeBatch.delete(firebaseFirestore.collection("comment_likes/" + mPostID + "/"+mCommentId).document(mUserID));
                                writeBatch.commit().addOnCompleteListener(new OnCompleteListener<Void>() {
                                    @Override
                                    public void onComplete(@NonNull Task<Void> task) {
                                        if(task.isSuccessful()){
                                            Timber.d("onComplete:     comment love delete successful");
                                        }else{
                                            Timber.d("onComplete:   comment delete is not succesful");
                                        }
                                    }
                                });
                            } else {
                                Timber.d("No such document");
                            }
                        } else {
                            Timber.d(task.getException(), "get failed with ");
                        }


                    }
                });
            }
        });
        holder.commentImage.setOnClickListener(view -> {
            holder.addRating(mUserID , 1);
            holder.addRating(mCurrentCommenterID , 1);
            Intent showPostIntent = new Intent(context  , ProfileActivity.class);
            showPostIntent.putExtra("userID" , mCurrentCommenterID);
            Pair< View , String > pair1 = Pair.create(holder.itemView.findViewById(R.id.comment_item_image) ,"profile_image");
            Pair< View , String > pair2 = Pair.create(holder.itemView.findViewById(R.id.comment_item_name) ,"profile_name");
            ActivityOptionsCompat optionsCompat = ActivityOptionsCompat.makeSceneTransitionAnimation(activity ,pair1 , pair2 );
            context.startActivity(showPostIntent , optionsCompat.toBundle());
        });
        holder.commentName.setOnClickListener(view -> {
            holder.addRating(mUserID , 1);
            holder.addRating(mCurrentCommenterID , 1);
            Intent showPostIntent = new Intent(context  , ProfileActivity.class);
            showPostIntent.putExtra("userID" , mCurrentCommenterID);
            Pair< View , String > pair1 = Pair.create(holder.itemView.findViewById(R.id.comment_item_image) ,"profile_image");
            Pair< View , String > pair2 = Pair.create(holder.itemView.findViewById(R.id.comment_item_name) ,"profile_name");
            ActivityOptionsCompat optionsCompat = ActivityOptionsCompat.makeSceneTransitionAnimation(activity ,pair1 , pair2 );
            context.startActivity(showPostIntent , optionsCompat.toBundle());
        });
    }


    @Override
    public int getItemCount() {
        return mCommentList.size();
    }


    public class PostCommentViewHolder  extends RecyclerView.ViewHolder{
         TextView commentText;
         CircleImageView commentImage;
         TextView commentName;
        public ImageButton commentLove;
         TextView commentLoveCount;
         TextView commentTimeAgo;
         PostCommentViewHolder(View itemView) {
            super(itemView);
            commentText = itemView.findViewById(R.id.comment_item_text);
            commentImage = itemView.findViewById(R.id.comment_item_image);
            commentName = itemView.findViewById(R.id.comment_item_name);
            commentLove = itemView.findViewById(R.id.comment_love);
            commentLoveCount = itemView.findViewById(R.id.comment_love_count);
            commentTimeAgo = itemView.findViewById(R.id.comment_item_time_ago);
        }
        public void setUserImage(String image_url) {
            ImageLoader imageLoader = ImageLoader.getInstance();
            imageLoader.displayImage(image_url, commentImage);
        }
        public void setPostUserName(String userName) {
            commentName.setText(userName);
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
        private void setLikeCount(long cnt){

            if(cnt>1){
                commentLoveCount.setText(cnt+" likes");
            }else{
                commentLoveCount.setText(cnt+" like");
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
                long curComments = post.getComment_cnt();
                long curShares = post.getShare_cnt();

                long nextLike = curLikes + factor;
                Timber.d("addLike:     like number is  " + nextLike);
                long nextComment = curComments + factor;
                long nextShare = curShares + factor;
                HashMap< String ,  Object > updateMap = new HashMap<>();
                updateMap.put("like_cnt" , nextLike);
                transaction.update(postRef , updateMap);
                return nextLike;
            }).addOnSuccessListener(this::setLikeCount);
        }

    }



}
