package candor.fulki.home;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.RequiresApi;
import android.support.v4.app.ActivityOptionsCompat;
import android.support.v4.util.Pair;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.daimajia.slider.library.Animations.DescriptionAnimation;
import com.daimajia.slider.library.Indicators.PagerIndicator;
import com.daimajia.slider.library.SliderLayout;
import com.daimajia.slider.library.SliderTypes.BaseSliderView;
import com.daimajia.slider.library.SliderTypes.TextSliderView;
import com.daimajia.slider.library.Tricks.ViewPagerEx;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentChange;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.WriteBatch;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.like.LikeButton;
import com.like.OnLikeListener;
import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoader;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Objects;

import candor.fulki.models.Ratings;
import candor.fulki.models.Comments;
import candor.fulki.models.Likes;
import candor.fulki.models.Notifications;
import candor.fulki.profile.ProfileActivity;
import candor.fulki.profile.ShowPleopleListActivity;
import candor.fulki.R;
import candor.fulki.utilities.GMailSender;
import de.hdodenhof.circleimageview.CircleImageView;
import timber.log.Timber;

import android.content.SharedPreferences;

import static android.content.Context.MODE_PRIVATE;

public class CombinedHomeAdapter extends RecyclerView.Adapter<CombinedHomeAdapter.ViewHolder>{

    private static final String TAG = "CombinedHomeAdapter";

    private List <CombinedPosts > data;
    private Context context;
    private Activity activity;

    private String mUserID = FirebaseAuth.getInstance().getCurrentUser().getUid();
    private FirebaseFirestore firebaseFirestore = FirebaseFirestore.getInstance();

    private String mUserName , mUserThumbImage;
    private String primaryUserName , primaryUserThumbImage;

    private ProgressDialog mProgress;



    public CombinedHomeAdapter ( List<CombinedPosts> data , Context context , Activity activity ){
        this.data = data;
        this.context = context;
        this.activity = activity;

        //load from local storage
        SharedPreferences sp = context.getSharedPreferences(candor.fulki.general.Constants.SHARED_PREF_NAME, MODE_PRIVATE);
        this.mUserID = sp.getString(candor.fulki.general.Constants.Id, null);
        this.mUserName = sp.getString(candor.fulki.general.Constants.Name, null);
        this.mUserThumbImage = sp.getString(candor.fulki.general.Constants.ThumbImage, null);
    }


    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view;
        if(viewType==0){
            view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_post_text, parent, false);
        }else{
            view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_post, parent, false);
        }
        return new CombinedHomeAdapter.ViewHolder(view);
    }

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        CombinedPosts post = data.get(position);

        HashMap<String,String> Hash_file_maps;
        Hash_file_maps = post.getPost_thumb_url();
        holder.setImages(Hash_file_maps);

        String primary_user_id = post.getPrimary_user_id();
        String secondary_user_id = post.getSecondary_user_id();

        String primary_push_id = post.getPrimary_push_id();
        String secondary_push_id = post.getSecondary_push_id();


        String timedate = post.getTime_and_date();
        String location = post.getLocation();
        String caption = post.getCaption();
        String type = post.getType();

        String privacy = post.getPrivacy();

        long like_cnt = post.getLike_cnt();
        long comment_cnt = post.getComment_cnt();
        long share_cnt = post.getShare_cnt();


        holder.setPostCaption(caption);
        holder.setPostDateTime(timedate);
        holder.setPostLikeCount(like_cnt);
        holder.setPostCommentCount(comment_cnt);



        firebaseFirestore.collection("users").document(primary_user_id).get().addOnSuccessListener(documentSnapshot -> {
            if(documentSnapshot.exists()){
                primaryUserName = documentSnapshot.getString("name");
                primaryUserThumbImage =documentSnapshot.getString("thumb_image");
                holder.setPostUserName(primaryUserName);
                holder.setUserImage(primaryUserThumbImage , holder.postUserImage);
            }
        }).addOnFailureListener(e -> {
            primaryUserName = "User Name";
            primaryUserThumbImage = "default";
            holder.setPostUserName(primaryUserName);
            holder.setUserImage(primaryUserThumbImage , holder.postUserImage);
        });


        holder.postUserImage.setOnClickListener(v -> {
            holder.addRating(mUserID , 1);
            holder.addRating(primary_user_id , 1);
            Intent showPostIntent = new Intent(context  , ProfileActivity.class);
            showPostIntent.putExtra("userID" , primary_user_id);
            Pair< View , String > pair1 = Pair.create(holder.itemView.findViewById(R.id.post_user_single_imagee) ,"profile_image");
            Pair< View , String > pair2 = Pair.create(holder.itemView.findViewById(R.id.post_user_name) ,"profile_name");
            ActivityOptionsCompat optionsCompat = ActivityOptionsCompat.makeSceneTransitionAnimation(activity ,pair1 , pair2 );
            context.startActivity(showPostIntent , optionsCompat.toBundle());

        });

        //setting user name and user image onclick listener
        holder.postUserName.setOnClickListener(v -> {
            Intent showPostIntent = new Intent(context  , ProfileActivity.class);
            showPostIntent.putExtra("userID" , primary_user_id);

            Pair< View , String > pair1 = Pair.create(holder.itemView.findViewById(R.id.post_user_single_imagee) ,"profile_image");
            Pair< View , String > pair2 = Pair.create(holder.itemView.findViewById(R.id.post_user_name) ,"profile_name");

            holder.addRating(mUserID , 1);
            holder.addRating(primary_user_id, 1);
            ActivityOptionsCompat optionsCompat = ActivityOptionsCompat.makeSceneTransitionAnimation(activity ,pair1 , pair2 );
            context.startActivity(showPostIntent , optionsCompat.toBundle());
        });

        //setting comment count
        FirebaseFirestore.getInstance().collection("comments/" + primary_push_id + "/comments").addSnapshotListener((documentSnapshots, e) -> {
            if (!documentSnapshots.isEmpty()) {
                long count = documentSnapshots.size();
                //long cnt = Integer.toString(count);
                firebaseFirestore.collection("posts").document(primary_push_id).update("comment_cnt" , count);
                if (count == 1) {
                    holder.setPostCommentCount(count);
                } else {
                    holder.setPostCommentCount(count);
                }
            } else {
                holder.setPostCommentCount(0);
            }
        });

        //setting share count
        /*FirebaseFirestore.getInstance().collection("shares/" + postPushID + "/shares").addSnapshotListener((documentSnapshots, e) -> {
            if (!documentSnapshots.isEmpty()) {
                int count = documentSnapshots.size();
                firebaseFirestore.collection("posts").document(postPushID).update("share_cnt" , count);
                String cnt = Integer.toString(count);
                if (count == 1) {
                    holder.postShareCount.setText(cnt + " share");
                } else {
                    holder.postShareCount.setText(cnt + " shares");
                }
            } else {
                holder.postShareCount.setText("0 share");
            }
        });*/


        //setting the current state of like button
        FirebaseFirestore.getInstance().collection("likes/" + primary_push_id + "/likes").document(mUserID).get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                if (task.isSuccessful()) {
                    if (task.getResult().exists()) {
                        holder.postLikeButton.setLiked(true);
                    } else {
                        holder.postLikeButton.setLiked(false);
                    }
                } else {
                    Timber.tag(TAG).w(task.getException(), "onComplete: ");
                    holder.postLikeButton.setLiked(false);
                }
            }
        });

        //handling the like onclick listener
        holder.postLikeButton.setOnLikeListener(new OnLikeListener() {
            @Override
            public void liked(LikeButton likeButton) {

                //building like
                holder.postLikeButton.setLiked(true);

                //building like
                String timestamp = String.valueOf(new Date().getTime());
                WriteBatch writeBatch  = firebaseFirestore.batch();
                DocumentReference ref = FirebaseFirestore.getInstance().collection("notifications/"+primary_user_id+"/notificatinos").document();
                String likeNotificatoinPushID = ref.getId();

                Timber.d("liked: " + mUserName);

                Likes mLikes = new Likes(mUserID ,mUserName ,mUserThumbImage ,likeNotificatoinPushID , timestamp);
                Notifications pushNoti = new Notifications( "like" ,mUserID , primary_user_id , primary_push_id ,likeNotificatoinPushID , timestamp,"n"  );


                DocumentReference notificatinoRef = firebaseFirestore.collection("notifications/"+primary_user_id+"/notificatinos").document(likeNotificatoinPushID);
                writeBatch.set(notificatinoRef, pushNoti);

                DocumentReference postNotificatinoRef =  firebaseFirestore.collection("posts/"+primary_push_id+"/notifications").document(likeNotificatoinPushID);
                writeBatch.set(postNotificatinoRef, pushNoti);

                DocumentReference postLikeRef =  firebaseFirestore.collection("likes/" + primary_push_id + "/likes").document(mUserID); //.set(mLikes);
                //firebaseFirestore.collection("likes/" + postPushID + "/likes").document(mUserID).set(mLikes);
                writeBatch.set(postLikeRef, mLikes);

                Timber.d("liked:     the object is  %s", mLikes.getTime_stamp());

                writeBatch.commit().addOnSuccessListener(aVoid -> {
                    Timber.d("liked:   like is successful");
                    holder.addRating(mUserID , 3);
                    holder.addRating(primary_user_id , 1);
                    holder.addLike(primary_push_id,1);
                }).addOnFailureListener(e -> {
                    Timber.d("liked:   like is not succesful");
                });
            }
            @Override
            public void unLiked(LikeButton likeButton) {
                holder.postLikeButton.setLiked(false);
                firebaseFirestore.collection("likes/" + primary_push_id + "/likes").document(mUserID).get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                        if(task.isSuccessful()){
                            if(task.getResult().exists()){

                                String likeNotificatoinPushID = task.getResult().getString("notificationID");
                                WriteBatch writeBatch  = firebaseFirestore.batch();

                                if(likeNotificatoinPushID != null){
                                    DocumentReference notificatinoRef = firebaseFirestore.collection("notifications/"+primary_user_id+"/notificatinos").document(likeNotificatoinPushID);
                                    writeBatch.delete(notificatinoRef);
                                    DocumentReference postNotificatinoRef =  firebaseFirestore.collection("posts/"+primary_push_id+"/notifications").document(likeNotificatoinPushID);
                                    writeBatch.delete(postNotificatinoRef);
                                }
                                DocumentReference postLikeRef =  firebaseFirestore.collection("likes/" + primary_push_id + "/likes").document(mUserID);
                                writeBatch.delete(postLikeRef);

                                writeBatch.commit().addOnSuccessListener(aVoid -> {
                                    holder.addRating(mUserID , -3);
                                    holder.addRating(primary_user_id , -1);
                                    holder.addLike(primary_push_id,-1);
                                    Timber.d("liked:   unlike is successful");

                                }).addOnFailureListener(e -> {
                                    Timber.d("liked:   unlike is not succesful");
                                });

                            }
                        }
                    }
                });

            }
        });


        holder.postLikeCount.setOnClickListener(v -> {
            Intent showPeopleIntent = new Intent(context , ShowPleopleListActivity.class);
            showPeopleIntent.putExtra("type" , "likes");
            showPeopleIntent.putExtra("user_id" ,primary_push_id );
            holder.addRating(mUserID , 2);
            context.startActivity(showPeopleIntent);
        });


        //handling the comment onclick listener
        holder.postCommentLinear.setOnClickListener(v -> {

            // Dialog commentDialog = new Dialog(context, android.R.style.Theme_Holo_Light_NoActionBar_Fullscreen);
            Dialog commentDialog = new Dialog(context, android.R.style.ThemeOverlay_Material_ActionBar);
            Objects.requireNonNull(commentDialog.getWindow()).getAttributes().windowAnimations = R.anim.fui_slide_in_right;
            commentDialog.setContentView(R.layout.comment_pop_up_dialog);

            //containers
            final RecyclerView mCommentList;
            final List<Comments> commentList = new ArrayList<>();
            LinearLayoutManager mLinearLayout;


            //--------- SETTING THE COMMENT ADAPTERS --//
            final PostCommentAdapter mPostCommentAdapter = new PostCommentAdapter(commentList, context , activity);
            mCommentList = commentDialog.findViewById(R.id.comment_list);
            mLinearLayout = new LinearLayoutManager(context);
            mCommentList.hasFixedSize();
            mCommentList.setLayoutManager(mLinearLayout);
            mCommentList.setAdapter(mPostCommentAdapter);


            //-------------LOADING COMMENTS------------//
            firebaseFirestore = FirebaseFirestore.getInstance();
            Query nextQuery = firebaseFirestore.collection("comments/"+ primary_push_id+"/comments").orderBy("time_stamp" , Query.Direction.DESCENDING);
            nextQuery.addSnapshotListener((documentSnapshots, e) -> {
                for(DocumentChange doc: documentSnapshots.getDocumentChanges()){
                    if(doc.getType() == DocumentChange.Type.ADDED){
                        Comments singleComment = doc.getDocument().toObject(Comments.class);
                        commentList.add(singleComment);
                        mPostCommentAdapter.notifyDataSetChanged();
                    }
                }
            });


            //comment onclick functionality
            final TextView commentBox = commentDialog.findViewById(R.id.comment_write);
            ImageButton commentPost = commentDialog.findViewById(R.id.comment_post);
            commentPost.setOnClickListener(view -> {

                String time_stamp = String.valueOf(new Date().getTime());


                DocumentReference notiRef = FirebaseFirestore.getInstance().collection("notifications/"+primary_user_id+"/notificatinos").document();
                String commentNotificatoinPushID = notiRef.getId();

                DocumentReference commentRef = FirebaseFirestore.getInstance().collection("comments/"+ primary_push_id +"/comments").document();
                String commentID = commentRef.getId();

                Notifications pushNoti = new Notifications( "comment" ,mUserID ,primary_user_id, primary_push_id,commentNotificatoinPushID , time_stamp,"n"  );
                Comments  comment =  new Comments(commentBox.getText().toString() , mUserID ,commentID, primary_push_id, commentNotificatoinPushID  , time_stamp);
                commentBox.setText("");


                WriteBatch writeBatch  = firebaseFirestore.batch();

                DocumentReference notificatinoRef = firebaseFirestore.collection("notifications/"+primary_user_id+"/notificatinos").document(commentNotificatoinPushID);
                writeBatch.set(notificatinoRef, pushNoti);

                DocumentReference postNotificatinoRef =  firebaseFirestore.collection("posts/"+primary_push_id+"/notifications").document(commentNotificatoinPushID);
                writeBatch.set(postNotificatinoRef, pushNoti);

                writeBatch.set(commentRef, comment);

                writeBatch.commit().addOnSuccessListener(aVoid -> {
                    Timber.d("liked:   like is successful");
                    holder.addRating(mUserID , 5);
                    holder.addRating(primary_user_id, 2);

                }).addOnFailureListener(e -> {
                    Timber.d("liked:   like is not succesful");
                });
            });
            commentDialog.show();
        });


        holder.mPostMoreOptions.setOnClickListener(v -> {

            if(mUserID.equals(primary_user_id)){
                AlertDialog.Builder builder1 = new AlertDialog.Builder(context);
                builder1.setMessage("Do you want to Delete this post?");
                builder1.setCancelable(true);
                builder1.setPositiveButton(
                        "Yes",
                        (dialog, id) -> {

                            mProgress = new ProgressDialog(context);
                            mProgress.setTitle("Deleting Post.......");
                            mProgress.setMessage("please wait while we delete your post");
                            mProgress.setCanceledOnTouchOutside(false);
                            mProgress.show();

                            //share gula delete korte hobe

                            holder.deletePost(primary_push_id);
                            if(Hash_file_maps.size()>0){
                                holder.deleteImage(primary_push_id);
                            }
                            dialog.cancel();
                        });
                builder1.setNegativeButton("No", (dialog, id) -> dialog.cancel());

                AlertDialog alert11 = builder1.create();
                alert11.show();
            }else{
                AlertDialog.Builder builder1 = new AlertDialog.Builder(context);
                builder1.setMessage("Do you want to Report this post?");
                builder1.setCancelable(true);

                builder1.setPositiveButton(
                        "Yes",
                        (dialog, id) -> {
                            holder.sendReport(primary_push_id);
                            dialog.cancel();
                        });
                builder1.setNegativeButton(
                        "No",
                        (dialog, id) -> dialog.cancel());

                AlertDialog alert11 = builder1.create();
                alert11.show();
            }
        });

        holder.postImage.setOnClickListener(v -> {
            Intent showPostIntent = new Intent(context  , ShowPostActivity.class);
            showPostIntent.putExtra("postID" , primary_push_id);

            Pair< View , String > pair1 = Pair.create(holder.itemView.findViewById(R.id.post_image) ,"post_image");
            Pair< View , String > pair2 = Pair.create(holder.itemView.findViewById(R.id.post_user_single_imagee) ,"profile_image");
            Pair< View , String > pair3 = Pair.create(holder.itemView.findViewById(R.id.post_user_name) ,"profile_name");


            holder.addRating(primary_user_id , 1);

            ActivityOptionsCompat optionsCompat = ActivityOptionsCompat.makeSceneTransitionAnimation(activity ,pair1 , pair2 , pair3);
            context.startActivity(showPostIntent , optionsCompat.toBundle());
        });

    }

    @Override
    public int getItemViewType(int position) {
        if(data.get(position).getPost_image_url().size() == 0){
            return 0;
        }else{
            return 1;
        }
    }

    @Override
    public long getItemId(int position) {
        return super.getItemId(position);
    }

    @Override
    public int getItemCount() {
        return data.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder  implements BaseSliderView.OnSliderClickListener, ViewPagerEx.OnPageChangeListener{


        ImageView mPostMoreOptions;
        TextView  postUserName;
        TextView  postCaption;
        TextView  postDateTime;
        TextView  postLocaiton;
        TextView  postLikeCount;
        TextView  postCommentCount;
        TextView  postShareCount;
        ImageView postImage;
        CircleImageView postUserImage;
        public LikeButton postLikeButton;
        ImageButton postCommentButton;
        ImageButton postShareButton;
        LinearLayout postCommentLinear;
        ProgressBar  postProgres;
        SliderLayout postSlider;

        public ViewHolder(View view) {
            super(view);
            postUserName = view.findViewById(R.id.post_user_name);
            postCaption = view.findViewById(R.id.post_caption);
            postDateTime = view.findViewById(R.id.post_time_date);
            postLocaiton = view.findViewById(R.id.post_location);
            postImage = view.findViewById(R.id.post_image);
            postUserImage = view.findViewById(R.id.post_user_single_imagee);
            postLikeButton = view.findViewById(R.id.post_like_button);
            postCommentButton = view.findViewById(R.id.post_comment_button);
            postLikeCount = view.findViewById(R.id.post_like_number);
            postCommentCount = view.findViewById(R.id.post_comment_number);
            mPostMoreOptions = view.findViewById(R.id.post_more_options);
            postShareButton = view.findViewById(R.id.post_share_button);
            postShareCount = view.findViewById(R.id.post_share_cnt);
            postCommentLinear = view.findViewById(R.id.item_post_comment_linear);
            postProgres = view.findViewById(R.id.item_post_progress);
            postSlider = view.findViewById(R.id.item_post_slider);
        }

        private void setImages(HashMap<String , String> Hash_file_maps) {
            if (Hash_file_maps.size() > 0) {
                for (String name : Hash_file_maps.keySet()) {
                    TextSliderView textSliderView = new TextSliderView(context);
                    textSliderView
                            .description(Hash_file_maps.get(name))
                            .image(name)
                            .setScaleType(BaseSliderView.ScaleType.FitCenterCrop)
                            .setOnSliderClickListener(this);
                    textSliderView.bundle(new Bundle());
                    textSliderView.getBundle()
                            .putString("extra", name);
                    postSlider.addSlider(textSliderView);
                }
                postSlider.setPresetTransformer(SliderLayout.Transformer.Accordion);
                postSlider.setPresetIndicator(SliderLayout.PresetIndicators.Center_Bottom);
                postSlider.setCustomAnimation(new DescriptionAnimation());
                postSlider.setDuration(3000);
                postSlider.addOnPageChangeListener(this);
                postSlider.setCustomIndicator((PagerIndicator) itemView.findViewById(R.id.custom_indicator));
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
                Timber.d("addLike : like number is %s", nextLike);
                HashMap< String ,  Object > updateMap = new HashMap<>();
                updateMap.put("like_cnt" , nextLike);
                transaction.update(postRef , updateMap);
                return nextLike;
            }).addOnSuccessListener(this::setPostLikeCount);
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

        private void sendReport(String postPushId){
            Thread sender = new Thread(() -> {
                try {
                    GMailSender sender1 = new GMailSender("candorappbd@gmail.com", "plan2018");
                    sender1.sendMail("Report about a post from Youth app BD",
                            "Here is a problem in the post with "+ postPushId,
                            "youremail",
                            "youthappbd@gmail.com");
                    // dialog.dismiss();
                } catch (Exception e) {
                    Timber.e("Error: %s", e.getMessage());
                }
            });
            Timber.d("sendMessage: sending mail");
            sender.start();
        }

        private void deletePost(String postPushID){


            firebaseFirestore.collection("posts").document(postPushID).collection("notifications").get().addOnSuccessListener(documentSnapshots -> {
                if(!documentSnapshots.isEmpty()){
                    WriteBatch writeBatch = firebaseFirestore.batch();
                    for(DocumentChange doc: documentSnapshots.getDocumentChanges()){
                        if(doc.getType() == DocumentChange.Type.ADDED){
                            Timber.d("loadFirstData:  found a data");
                            Notifications notifications = doc.getDocument().toObject(Notifications.class);
                            String notificationID = notifications.getNotification_id();
                            String notificationTo =notifications.getNotification_to();
                            DocumentReference notificationRef= firebaseFirestore.collection("posts").document(postPushID).collection("notifications").document(notificationID);
                            DocumentReference notificationToRef= firebaseFirestore.collection("notifications").document(notificationTo).collection("notificatinos").document(notificationID);
                            writeBatch.delete(notificationRef);
                            writeBatch.delete(notificationToRef);
                        }
                    }
                    writeBatch.commit().addOnSuccessListener(aVoid -> {
                        Timber.d("onSuccess:    writebatch succesful");
                    });
                }
            });

            WriteBatch writeBatch = firebaseFirestore.batch();

            DocumentReference postRef = firebaseFirestore.collection("posts").document(postPushID);
            writeBatch.delete(postRef);

            DocumentReference likesRef = firebaseFirestore.collection("likes").document(postPushID);
            writeBatch.delete(likesRef);

            DocumentReference commentsRef = firebaseFirestore.collection("comments").document(postPushID);
            writeBatch.delete(commentsRef);

            DocumentReference commentsLikesRef = firebaseFirestore.collection("comment_likes").document(postPushID);
            writeBatch.delete(commentsLikesRef);

            writeBatch.commit().addOnSuccessListener(aVoid -> {
                mProgress.dismiss();
                notifyDataSetChanged();
                Timber.d("onSuccess:    deletion is succesful");
                Toast.makeText(context, "Deletion Successful !", Toast.LENGTH_SHORT).show();
            }).addOnFailureListener(e -> Toast.makeText(context, "Some Error Occured please try again later", Toast.LENGTH_SHORT).show());
        }

        private void deleteImage(String postPushID){

            Timber.d("deleteImage:  called ");
            firebaseFirestore.collection("images").document("posts").collection(postPushID).get().addOnCompleteListener(task -> {
                if(task.isSuccessful()){
                    for (DocumentSnapshot document : task.getResult()) {
                        Timber.d("onComplete:   found a document for deleting image ");
                        final String  imagePath=  document.getString("imagePath");
                        final String  thumbPath=  document.getString("imageThumbPath");
                        firebaseFirestore.collection("images").document("posts").collection(postPushID).document(document.getId()).delete();

                        final StorageReference storageRef = FirebaseStorage.getInstance().getReference();
                        StorageReference imageRef = storageRef.child(imagePath);
                        imageRef.delete().addOnSuccessListener(aVoid -> {
                            StorageReference imageRef1 = storageRef.child(thumbPath);
                            imageRef1.delete();
                            firebaseFirestore.collection("images").document(mUserID).collection("posts").document(postPushID).delete();
                        }).addOnFailureListener(exception -> {
                            // Uh-oh, an error occurred!
                        });
                    }
                    Timber.d("deleteImage:   image deletion succesful");

                }
            });

        }

    }

}
