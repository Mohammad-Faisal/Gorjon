package candor.fulki.adapters;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityOptionsCompat;
import android.support.v4.content.ContextCompat;
import android.support.v4.util.Pair;
import android.support.v7.widget.CardView;
import android.support.v7.widget.RecyclerView;
import android.text.Html;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.ImageLoaderConfiguration;

import java.util.List;

import candor.fulki.utils.GetTimeAgo;
import candor.fulki.models.Ratings;
import candor.fulki.activities.ShowPostActivity;
import candor.fulki.models.Notifications;
import candor.fulki.activities.ProfileActivity;
import candor.fulki.R;
import candor.fulki.utils.PreferenceManager;
import de.hdodenhof.circleimageview.CircleImageView;

/**
 * Created by Mohammad Faisal on 1/30/2018.
 */

public class NotificationsAdapter extends RecyclerView.Adapter<NotificationsAdapter.NotificationViewHolder> {

    private static final String TAG = "NotificationsAdapter";
    private List<Notifications> mNotificationList;
    private List<String> mNotificationIDs;
    private DatabaseReference mRootRef = FirebaseDatabase.getInstance().getReference();
    private Context context;
    private Activity activity;

    public ImageLoader imageLoader;
    public DisplayImageOptions postImageOptions;
    public DisplayImageOptions userImageOptions;
    private String mUserID  , mUserName , mUserThumbImage;
    private FirebaseFirestore firebaseFirestore = FirebaseFirestore.getInstance();

    private PreferenceManager preferenceManager;

    // ---- CONSTRUCTOR --//
    public NotificationsAdapter(List<Notifications> mNotificationList , List<String> mNotificationIDs, Context context , Activity activity){
        this.mNotificationList = mNotificationList;
        this.mNotificationIDs = mNotificationIDs;
        this.context = context;
        this.activity  = activity;
        preferenceManager = new PreferenceManager(context);
        mUserID = preferenceManager.getUserId();
        mUserName = preferenceManager.getUserName();
        mUserThumbImage = preferenceManager.getUserThumbImage();

        //Image loader initialization for offline feature
        ImageLoaderConfiguration config = new ImageLoaderConfiguration.Builder(context)
                .threadPoolSize(5)
                .threadPriority(Thread.MIN_PRIORITY + 2)
                .defaultDisplayImageOptions(DisplayImageOptions.createSimple())
                .build();
        userImageOptions = new DisplayImageOptions.Builder()
                .showImageOnLoading(R.drawable.ic_blank_profile)
                .showImageForEmptyUri(R.drawable.ic_blank_profile)
                .showImageOnFail(R.drawable.ic_blank_profile)
                .cacheInMemory(true)
                .cacheOnDisk(true)
                .considerExifParams(true)
                .build();
        imageLoader = ImageLoader.getInstance();
        imageLoader.init(config);

    }


    @Override
    public int getItemViewType(int position) {
        if( mNotificationList.get(position).getType().equals("follow")){
            //ble ble ble
            return 1;
        }
        else{
            return 1;
        }
    }
    @Override
    public NotificationsAdapter.NotificationViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View v;
        if(viewType == 1){  //follow notification
            v = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_notification, parent , false);
            return new NotificationsAdapter.NotificationViewHolder(v);
        }
        else
        return null;
    }

    @Override
    public void onBindViewHolder(final NotificationsAdapter.NotificationViewHolder holder, int position) {
        final Notifications notiItem = mNotificationList.get(position);
        final String type = notiItem.getType();


        final String notiID =notiItem.getNotification_id();
        String seen_status = notiItem.getSeen();
        if(seen_status.equals("n")){
            holder.notificatinoLinear.setBackgroundColor(context.getColor(R.color.halkagrey));
            //holder.notificationCard.setCardBackgroundColor(R.color);
            //holder.itemView.setBackgroundColor(ContextCompat.getColor(context, R.color.colorPrimaryy));
        }else{
            holder.notificatinoLinear.setBackgroundColor(context.getColor(R.color.White));
            //holder.notificationCard.setCardBackgroundColor(R.color.Grey);
            //holder.itemView.setBackgroundColor(ContextCompat.getColor(context, R.color.White));
        }

        if(type.equals("follow")){
            final String userID = notiItem.getNotification_from();
            final String online = notiItem.getTime_stamp();

            holder.notificationIcon.setImageResource(R.drawable.ic_person_add);

            FirebaseFirestore.getInstance().collection("users").document(userID).get().addOnCompleteListener(task -> {
                if (task.isSuccessful()) {
                    String userName = task.getResult().getString("name");
                    String userThumbImage = task.getResult().getString("thumb_image");
                    holder.setImage(userThumbImage , context , R.drawable.ic_blank_profile);

                    //setting time ago for comment
                    GetTimeAgo ob = new GetTimeAgo();
                    long time = Long.parseLong(online);
                    String time_ago = GetTimeAgo.getTimeAgo(time ,context);
                    holder.notificationTime.setText( time_ago);

                    String sourceString =   userName + "  has followed your profile";
                    holder.notificationText.setText(Html.fromHtml(sourceString));
                }
            });

            holder.notificationImage.setOnClickListener(view -> {
                Intent profileIntent = new Intent(context , ProfileActivity.class);
                profileIntent.putExtra("userID" , userID);
                context.startActivity(profileIntent);
            });

            holder.itemView.setOnClickListener(view -> {
                Intent profileIntent = new Intent(context , ProfileActivity.class);
                profileIntent.putExtra("userID" , userID);
                context.startActivity(profileIntent);
                holder.itemView.setBackgroundColor(ContextCompat.getColor(context, R.color.White));
                mRootRef.child("notifications").child(mUserID).child(notiID).child("seen").setValue("y").addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                    }
                });

            });
        }
        else if (type.equals("comment")  || type.equals("like")  || type.equals("share") || type.equals("comment_like") || type.equals("comment_event")){


            if(type.equals("comment") || type.equals("comment_event")){
                holder.notificationIcon.setImageResource(R.drawable.ic_comment_black);
            }else if(type.equals("like")){
                holder.notificationIcon.setImageResource(R.drawable.ic_love_full);
            }else if(type.equals("share")){
                holder.notificationIcon.setImageResource(R.drawable.ic_share_black);
            }else if(type.equals("comment_like")){
                holder.notificationIcon.setImageResource(R.drawable.ic_love_full);
            }


            final String userID = notiItem.getNotification_from();
            final String online = notiItem.getTime_stamp();
            final String postID = notiItem.getContent_id();



            FirebaseFirestore.getInstance().collection("users").document(userID).get().addOnCompleteListener(task -> {
                if (task.isSuccessful()) {
                    String userName = task.getResult().getString("name");
                    String userThumbImage = task.getResult().getString("thumb_image");
                    holder.setImage(userThumbImage , context , R.drawable.ic_blank_profile);

                    //setting time ago for comment
                    GetTimeAgo ob = new GetTimeAgo();
                    long time = Long.parseLong(online);
                    String time_ago = GetTimeAgo.getTimeAgo(time ,context);
                    holder.notificationTime.setText( time_ago);


                    Log.d(TAG, "onBindViewHolder:   found type    "+type);

                    //setting comment
                    String sourceString = "default notification";
                    if(type.equals("comment")){
                        sourceString =   userName + "  has Commented on your post";
                    } else if(type.equals("like")) {
                        sourceString =   userName + "  has Liked your post";
                    }else if( type.equals("share")){
                        sourceString =   userName + "  has Shared your post";
                    }else if(type.equals("event_comment")) {
                        sourceString =  userName  + "  has Commented on your event";
                    }else {
                        sourceString =    userName + "  has liked your comment !";
                    }
                    holder.notificationText.setText(Html.fromHtml(sourceString));
                }
            });

            holder.notificationImage.setOnClickListener(view -> {
                Intent profileIntent = new Intent(context , ProfileActivity.class);
                profileIntent.putExtra("userID" , userID);
                context.startActivity(profileIntent);
            });
            holder.itemView.setOnClickListener(view -> {
                holder.itemView.setBackgroundColor(ContextCompat.getColor(context, R.color.White));


                firebaseFirestore.collection("notifications/"+mUserID+"/notificatinos").document(notiID).update("seen" , "y").addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {

                    }
                });
                Intent showPostIntent = new Intent(context , ShowPostActivity.class);
                showPostIntent.putExtra("postID" , postID);
                Pair< View , String > pair1 = Pair.create(holder.itemView.findViewById(R.id.item_notification_image) ,"profile_image");
                ActivityOptionsCompat optionsCompat = ActivityOptionsCompat.makeSceneTransitionAnimation(activity ,pair1 );
                context.startActivity(showPostIntent , optionsCompat.toBundle());
            });
        }else if(type.equals("invitation")){  ///add something
            final String userID = notiItem.getNotification_from();
            final String online = notiItem.getTime_stamp();
            final String meetingID = notiItem.getContent_id();

            holder.notificationIcon.setImageResource(R.drawable.ic_message_black);

            mRootRef.child("users").child(userID).addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {
                    final String imageURL = dataSnapshot.child("thumb_image").getValue().toString();
                    final String userName = dataSnapshot.child("name").getValue().toString();
                    String sourceString = "default notification";
                    sourceString =  "<b>" + userName + "<b>" + "  has invited you to a group meeting";
                    holder.notificationText.setText(Html.fromHtml(sourceString));
                    holder.setImage(imageURL , context , R.drawable.ic_blank_profile);
                    ////SETTING TIME AGO OF THE NOTIFICATION -//
                    GetTimeAgo ob = new GetTimeAgo();
                    long time = Long.parseLong(online);
                    String time_ago = GetTimeAgo.getTimeAgo(time ,context);
                    holder.notificationTime.setText( time_ago);
                }
                @Override
                public void onCancelled(DatabaseError databaseError) {

                }
            });

           /* holder.itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    holder.itemView.setBackgroundColor(ContextCompat.getColor(context, R.color.White));
                    mRootRef.child("notifications").child(MainActivity.mUserID).child(notiID).child("seen").setValue("y").addOnSuccessListener(new OnSuccessListener<Void>() {
                        @Override
                        public void onSuccess(Void aVoid) {
                            Intent showPostIntent = new Intent(context , MeetingActivity.class);
                            showPostIntent.putExtra("meetingID" , meetingID);
                            context.startActivity(showPostIntent);
                        }
                    });
                }
            });*/
        }else if(type.equals("join")){

            final String userID = notiItem.getNotification_from();
            final String online = notiItem.getTime_stamp();
            final String eventID = notiItem.getContent_id();



            FirebaseFirestore.getInstance().collection("users").document(userID).get().addOnCompleteListener(task -> {
                if (task.isSuccessful()) {
                    String userName = task.getResult().getString("name");
                    String userThumbImage = task.getResult().getString("thumb_image");
                    holder.setImage(userThumbImage , context , R.drawable.ic_blank_profile);

                    //setting time ago for comment
                    GetTimeAgo ob = new GetTimeAgo();
                    long time = Long.parseLong(online);
                    String time_ago = GetTimeAgo.getTimeAgo(time ,context);
                    holder.notificationTime.setText( time_ago);
                    String sourceString = userName  + "  has Joined your event";
                    holder.notificationText.setText(Html.fromHtml(sourceString));
                }
            });

            holder.notificationImage.setOnClickListener(view -> {
                Intent profileIntent = new Intent(context , ProfileActivity.class);
                profileIntent.putExtra("userID" , userID);
                context.startActivity(profileIntent);
            });

            holder.itemView.setBackgroundColor(ContextCompat.getColor(context, R.color.White));
            firebaseFirestore.collection("notifications/"+mUserID+"/notificatinos").document(notiID).update("seen" , "y").addOnCompleteListener(new OnCompleteListener<Void>() {
                @Override
                public void onComplete(@NonNull Task<Void> task) {

                }
            });
            Intent showPostIntent = new Intent(context , ShowPostActivity.class);
            showPostIntent.putExtra("event_id" , eventID);
            Pair< View , String > pair1 = Pair.create(holder.itemView.findViewById(R.id.item_notification_image) ,"profile_image");
            ActivityOptionsCompat optionsCompat = ActivityOptionsCompat.makeSceneTransitionAnimation(activity ,pair1 );
            context.startActivity(showPostIntent , optionsCompat.toBundle());



        }else if(type.equals("comment_event")){

        }
    }


    @Override
    public int getItemCount() {
        return mNotificationList.size();
    }


    public class NotificationViewHolder  extends RecyclerView.ViewHolder{
        public TextView notificationText;
        public CircleImageView notificationImage;
        public TextView notificationTime;
        public ImageView notificationIcon;
        public CardView notificationCard;
        private LinearLayout notificatinoLinear;

        public NotificationViewHolder(View itemView) {
            super(itemView);
            notificationImage = itemView.findViewById(R.id.item_notification_image);
            notificationText = itemView.findViewById(R.id.item_notification_details);
            notificationTime = itemView.findViewById(R.id.item_notification_time_ago);
            notificationIcon = itemView.findViewById(R.id.item_notificaion_icon);
            notificationCard = itemView.findViewById(R.id.item_notification_card);
            notificatinoLinear = itemView.findViewById(R.id.item_notification_linear);
        }

        public void setImage(String imageURL , final Context context , int drawable_id ){
            imageLoader.displayImage(imageURL, notificationImage, userImageOptions);
        }


        private Task<Void> addRating(String mUserID  , int factor) {

            FirebaseFirestore firebaseFirestore = FirebaseFirestore.getInstance();
            Log.d(TAG, "addRating:   function calledd !!!!");
            final DocumentReference ratingRef = FirebaseFirestore.getInstance().collection("ratings")
                    .document(mUserID);
            return firebaseFirestore.runTransaction(transaction -> {

                Ratings ratings = transaction.get(ratingRef)
                        .toObject(Ratings.class);
                long curRating = ratings.getRating();
                long nextRating = curRating + factor;

                ratings.setRating(nextRating);
                transaction.set(ratingRef, ratings);
                return null;
            });
        }

    }



}
