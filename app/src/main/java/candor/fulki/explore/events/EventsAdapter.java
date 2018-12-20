package candor.fulki.explore.events;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.support.v7.widget.CardView;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.squareup.picasso.Callback;
import com.squareup.picasso.NetworkPolicy;
import com.squareup.picasso.Picasso;

import java.util.List;

import candor.fulki.explore.people.Ratings;
import candor.fulki.R;

public class EventsAdapter extends RecyclerView.Adapter<EventsAdapter.EventsViewHolder> {


    private static final String TAG = "EventsAdapter";
    private List<Events> eventsList;
    DatabaseReference mRootRef = FirebaseDatabase.getInstance().getReference();
    Context context;
    Activity activity;
    String mUserID = FirebaseAuth.getInstance().getCurrentUser().getUid();



    public EventsAdapter(List<Events> eventsList, Context context , Activity activity) {
        this.eventsList = eventsList;
        this.context = context;
        this.activity = activity;
    }

    @Override
    public EventsViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {

        View v =  LayoutInflater.from(parent.getContext()).inflate(R.layout.item_event, parent , false);
        return new EventsAdapter.EventsViewHolder(v);
        }

    @Override
    public void onBindViewHolder(EventsViewHolder holder, int position) {
        Events events = eventsList.get(position);

        String title = events.getTitle();
        long cnt = events.getPeople_cnt();
        String location = events.getLocation();
        String date = events.getTime_and_date();
        String thumb_image_url = events.getImage_url();
        String eventID = events.getEvent_push_id();

        Log.d(TAG, "onBindViewHolder:    event people count  "+cnt);

        holder.eventDate.setText(date);
        holder.eventLocation.setText(location);
        holder.eventTitle.setText(title);
        holder.setPeopleCnt(cnt);
        holder.setCommentCnt(eventID);
        holder.setImage(thumb_image_url , context);

        holder.eventCard.setOnClickListener(v -> {
            holder.addRating(mUserID , 1);
            Intent showEventIntent = new Intent( context ,ShowEventActivity.class);
            showEventIntent.putExtra("event_id"  , eventID);
            context.startActivity(showEventIntent);
        });

    }

    @Override
    public int getItemCount() {
        return eventsList.size();
    }

    public class EventsViewHolder extends RecyclerView.ViewHolder {

        CardView eventCard;
        ImageView eventImage;
        TextView eventTitle , eventDate , eventLocation, eventPeopleCnt , eventCommentCnt;



        public EventsViewHolder(View itemView) {
            super(itemView);

            eventCard = itemView.findViewById(R.id.item_event_card);
            eventImage = itemView.findViewById(R.id.item_event_image);
            eventDate = itemView.findViewById(R.id.item_event_date);
            eventLocation = itemView.findViewById(R.id.item_event_locaiton);
            eventPeopleCnt = itemView.findViewById(R.id.item_event_people_cnt);
            eventTitle = itemView.findViewById(R.id.item_event_title);
            eventCommentCnt = itemView.findViewById(R.id.item_event_comment_cnt);
        }


        public void  setPeopleCnt(long cnt){
            eventPeopleCnt.setText(cnt+" people");
        }


        public void  setCommentCnt(String eventID){

            FirebaseFirestore.getInstance().collection("comments/" + eventID + "/comments").addSnapshotListener((documentSnapshots, e) -> {
                if (!documentSnapshots.isEmpty()) {
                    int count = documentSnapshots.size();
                    eventCommentCnt.setText(""+count+" comments");
                } else {
                    eventCommentCnt.setText(""+"0"+" comment");
                }
            });

        }


        public void setImage(final String imageURL , final Context context ){
            if(imageURL.equals("default")){

            }
            else{
                Picasso.with(context).load(imageURL).networkPolicy(NetworkPolicy.OFFLINE)
                        .placeholder(R.drawable.ic_camera_icon).into(eventImage, new Callback() {
                    @Override
                    public void onSuccess() {
                        //do nothing if an image is found offline
                    }
                    @Override
                    public void onError() {
                        Picasso.with(context).load(imageURL).placeholder(R.drawable.ic_blank_profile).into(eventImage);
                    }
                });
            }
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
