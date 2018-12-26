package candor.fulki.profile;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
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
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.squareup.picasso.Callback;
import com.squareup.picasso.NetworkPolicy;
import com.squareup.picasso.Picasso;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

import candor.fulki.chat.ChatActivity;
import candor.fulki.models.Ratings;
import candor.fulki.models.UserBasic;
import candor.fulki.models.Notifications;
import candor.fulki.R;
import de.hdodenhof.circleimageview.CircleImageView;
import timber.log.Timber;

import static android.content.Context.MODE_PRIVATE;


public class ListPeopleAdapter extends RecyclerView.Adapter<ListPeopleAdapter.ListPeopleVIewHolder> {

    private List<UserBasic> mUserList;
    private Context context;
    private Activity activity;


    private boolean followState = false;
    private FirebaseFirestore firebaseFirestore = FirebaseFirestore.getInstance();
    private String mUserName , mUserImage, mUserThumbImage;
    private String mUserID;


    public ListPeopleAdapter(List<UserBasic> mUserList , Context context , Activity activity){
        this.mUserList = mUserList;
        this.context  = context;
        this.activity = activity;

        SharedPreferences sp = context.getSharedPreferences(candor.fulki.general.Constants.SHARED_PREF_NAME, MODE_PRIVATE);
        mUserID = sp.getString(candor.fulki.general.Constants.Id, null);
        mUserName = sp.getString(candor.fulki.general.Constants.Name, null);
        mUserImage = sp.getString(candor.fulki.general.Constants.Image, null);
        mUserThumbImage = sp.getString(candor.fulki.general.Constants.ThumbImage, null);

    }

    @NonNull
    @Override
    public ListPeopleVIewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {

        View v;
        v = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_list_person, parent , false);
        return new ListPeopleVIewHolder(v);
    }

    @SuppressLint("SetTextI18n")
    @Override
    public void onBindViewHolder(@NonNull ListPeopleVIewHolder holder, int position) {

        UserBasic c = mUserList.get(position);
        String mListUserID = c.getmUserID();
        String mListUserName = c.getmUserName();
        String mListUserThumbImage = c.getmUserThumbImage();

        Timber.d("onBindViewHolder:  user id : %s", mListUserID);
        Timber.d("onBindViewHolder:  user name : %s", mListUserName);
        Timber.d("onBindViewHolder:  user thumb image : %s", mListUserThumbImage);



        firebaseFirestore.collection("ratings").document(mListUserID).addSnapshotListener((documentSnapshot, e) -> {
            if(e!=null){
                Timber.d("onEvent: some error occured whgile getching rating data");
            }else{
                if(documentSnapshot.exists()){
                    String rating = documentSnapshot.get("rating").toString();
                    holder.userRatingText.setText("rating  "+rating);
                }else{
                    Random rand = new Random();
                    int n = rand.nextInt(1000)+134;
                    String s = String.valueOf(n);
                    holder.userRatingText.setText("rating  "+s);
                }
            }
        });



        holder.setImage(mListUserThumbImage , context);
        holder.setName(mListUserName);
        holder.setFollowBtn( mListUserID);


        holder.msgBtn.setOnClickListener(v -> {
            holder.addRating(mUserID , 1);
            holder.addRating(mListUserID , 1);
            Intent chatIntent = new Intent(activity , ChatActivity.class);
            chatIntent.putExtra("user_id" , mListUserID);
            context.startActivity(chatIntent);
        });

        holder.userNameText.setOnClickListener(v -> {
            holder.addRating(mUserID , 1);
            holder.addRating(mListUserID , 2);
            Intent showPostIntent = new Intent(context  , ProfileActivity.class);
            showPostIntent.putExtra("userID" , mListUserID);
            Pair< View , String > pair1 = Pair.create(holder.itemView.findViewById(R.id.item_list_person_image) ,"profile_image");
            Pair< View , String > pair2 = Pair.create(holder.itemView.findViewById(R.id.item_list_person_name) ,"profile_name");
            ActivityOptionsCompat optionsCompat = ActivityOptionsCompat.makeSceneTransitionAnimation(activity ,pair1 , pair2 );
            context.startActivity(showPostIntent , optionsCompat.toBundle());
        });
        holder.userImage.setOnClickListener(v -> {
            holder.addRating(mUserID , 1);
            holder.addRating(mListUserID , 2);
            Intent showPostIntent = new Intent(context  , ProfileActivity.class);
            showPostIntent.putExtra("userID" , mListUserID);
            Pair< View , String > pair1 = Pair.create(holder.itemView.findViewById(R.id.item_list_person_image) ,"profile_image");
            Pair< View , String > pair2 = Pair.create(holder.itemView.findViewById(R.id.item_list_person_name) ,"profile_name");
            ActivityOptionsCompat optionsCompat = ActivityOptionsCompat.makeSceneTransitionAnimation(activity ,pair1 , pair2 );
            context.startActivity(showPostIntent , optionsCompat.toBundle());
        });


        holder.followBtn.setOnClickListener(v -> {
            if(!followState){   //currently not following after click i will follow this person


                holder.addRating(mUserID , 15);
                holder.addRating(mListUserID , 5);

                followState = true;
                holder.followBtn.setBackgroundResource(R.drawable.follow_checked);


                //  --------- GETTING THE DATE AND TIME ----------//
                Calendar c1 = Calendar.getInstance();
                SimpleDateFormat df = new SimpleDateFormat("dd-MMM-yyyy");
                String formattedDate = df.format(c1.getTime());

                String time_stamp = String.valueOf(new Date().getTime());
                DocumentReference ref = FirebaseFirestore.getInstance().collection("notifications/"+mListUserID+"/notificatinos").document();
                String followNotificatoinPushID = ref.getId();
                Notifications pushNoti = new Notifications( "follow" ,mUserID , mListUserID , mListUserID ,followNotificatoinPushID , time_stamp,"n"  );
                firebaseFirestore.collection("notifications/"+mListUserID+"/notificatinos").document(followNotificatoinPushID).set(pushNoti);


                //------- SETTING THE INFORMATION THAT NOW I AM FOLLOWING THIS ID ------//
                Map<String, String> followingData = new HashMap<>();
                followingData.put("id" , mListUserID);
                followingData.put("date" , formattedDate);
                followingData.put("notificationID", followNotificatoinPushID);
                followingData.put("name" , mListUserName);
                followingData.put("thumb_image" , mListUserThumbImage);
                followingData.put("timestamp" , time_stamp);

                firebaseFirestore.collection("followings/" + mUserID + "/followings").document(mListUserID).set(followingData);

                //------- SETTING THE INFORMATION THAT NOW I AM FA FOLLOWER OF THIS ID ------//
                Map<String, String> followerData = new HashMap<>();
                followerData.put("id" , mUserID);
                followerData.put("date" , formattedDate);
                followerData.put("notificationID", followNotificatoinPushID);
                followerData.put("name" , mUserName);
                followerData.put("thumb_image" ,mUserThumbImage);
                followerData.put("timestamp" , time_stamp);

                firebaseFirestore.collection("followers/" + mListUserID + "/followers").document(mUserID).set(followerData);



            }else{  //currently following this person after clickhin i will not fololw


                holder.addRating(mUserID , -15);
                holder.addRating(mListUserID , -5);
                followState = false;
                holder.followBtn.setBackgroundResource(R.drawable.user_followings);

                firebaseFirestore.collection("followers/" + mListUserID + "/followers").document(mUserID).get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                        if(task.isSuccessful()){
                            if(task.getResult().exists()){
                                String  followNotificatoinPushID = task.getResult().getString("notificationID");
                                if(followNotificatoinPushID!=null){
                                    firebaseFirestore.collection("notifications/"+mListUserID+"/notificatinos").document(followNotificatoinPushID).delete();
                                }
                                firebaseFirestore.collection("followings/" + mUserID + "/followings").document(mListUserID).delete();
                                firebaseFirestore.collection("followers/" + mListUserID + "/followers").document(mUserID).delete();
                            }
                        }
                    }
                });


            }
        });
    }

    @Override
    public int getItemCount() {
        return mUserList.size();
    }

    public class ListPeopleVIewHolder extends RecyclerView.ViewHolder {

        TextView userNameText;
        CircleImageView userImage;
        ImageButton followBtn;
        ImageButton msgBtn;
        TextView userRatingText;

        ListPeopleVIewHolder(View itemView) {
            super(itemView);
            userNameText = itemView.findViewById(R.id.item_list_person_name);
            userImage =itemView.findViewById(R.id.item_list_person_image);
            followBtn = itemView.findViewById(R.id.item_list_person_followbtn);
            msgBtn = itemView.findViewById(R.id.item_list_person_msgwbtn);
            userRatingText = itemView.findViewById(R.id.item_list_person_rating);
        }

        public void setImage(final String imageURL, final Context context){
                Picasso.with(context).load(imageURL).networkPolicy(NetworkPolicy.OFFLINE)
                        .placeholder(R.drawable.ic_blank_profile).into(userImage, new Callback() {
                    @Override
                    public void onSuccess() {
                        //do nothing if an image is found offline
                    }
                    @Override
                    public void onError() {
                        Picasso.with(context).load(imageURL).placeholder(R.drawable.ic_blank_profile).into(userImage);
                    }
                });
        }
        public void setName(final String name){
            userNameText.setText(name);
        }
        void setFollowBtn(String mListUserID){
            Timber.d("setFollowBtn:     " + mUserID + "    " + mListUserID);
            if(mUserID.equals(mListUserID)){
                followBtn.setVisibility(View.GONE);
                msgBtn.setVisibility(View.GONE);
            }else{
                FirebaseFirestore.getInstance().collection("followings/" + mUserID + "/followings").document(mListUserID).get().addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        if (task.getResult().exists()) {
                            followState = true;
                            followBtn.setBackgroundResource(R.drawable.follow_checked);
                        } else{
                            followState = false;
                            followBtn.setBackgroundResource(R.drawable.user_followings);
                        }
                    }
                });
            }
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


    }
}
