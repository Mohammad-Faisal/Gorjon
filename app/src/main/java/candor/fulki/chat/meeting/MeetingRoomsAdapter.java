package candor.fulki.chat.meeting;

import android.content.Context;
import android.content.Intent;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.squareup.picasso.Callback;
import com.squareup.picasso.NetworkPolicy;
import com.squareup.picasso.Picasso;

import java.util.List;

import candor.fulki.R;


/**
 * Created by Mohammad Faisal on 2/8/2018.
 */

public class MeetingRoomsAdapter extends RecyclerView.Adapter<MeetingRoomsAdapter.MeetingRoomsViewHolder> {

    private List<MeetingRooms> mMeetingRoomsList;
    DatabaseReference mRootRef = FirebaseDatabase.getInstance().getReference();
    Context context;

    public MeetingRoomsAdapter(List<MeetingRooms> mMeetingRoomsList, Context context) {
        this.mMeetingRoomsList = mMeetingRoomsList;
        this.context = context;
    }


    @Override
    public MeetingRoomsViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {

        View v =  LayoutInflater.from(parent.getContext()).inflate(R.layout.item_meeting_room_single, parent , false);
        return new MeetingRoomsAdapter.MeetingRoomsViewHolder(v);
    }


    @Override
    public void onBindViewHolder(MeetingRoomsViewHolder holder, int position) {

        MeetingRooms meetingRooms = mMeetingRoomsList.get(position);
        final String meetingID = meetingRooms.getMeeting_id();
        String meetingTItle = meetingRooms.getTitle();
        String meetingTags = meetingRooms.getTag();
        String MeetingPerson = meetingRooms.getNumber_of_person();
        String meetingImage = meetingRooms.getImage_url();
        if(meetingImage==null || meetingImage.equals("default")){

        }else{
            holder.setImage(meetingImage , context);
        }

        holder.meetingTag.setText(meetingTags);
        holder.meetingTitle.setText(meetingTItle);
        holder.meetingNumberPerson.setText(MeetingPerson);


        holder.itemView.setOnClickListener(view -> {
            Intent meetingIntent= new Intent(context , MeetingActivity.class);
            meetingIntent.putExtra("meetingID" , meetingID);
            context.startActivity(meetingIntent);
        });

    }

    @Override
    public int getItemCount() {
        return mMeetingRoomsList.size();
    }

    public class MeetingRoomsViewHolder extends RecyclerView.ViewHolder {


        public TextView meetingTitle , meetingTag , meetingNumberPerson;
        public ImageView meetingImage;

        public MeetingRoomsViewHolder(View itemView) {
            super(itemView);
            meetingImage = itemView.findViewById(R.id.item_meeting_room_image);
            meetingNumberPerson = itemView.findViewById(R.id.item_meeting_room_number_of_people);
            meetingTitle  = itemView.findViewById(R.id.item_meeting_room_title);
            meetingTag =  itemView.findViewById(R.id.item_meeting_room_tags);
        }


        public void setImage(final String imageURL , final Context context ){
            if(imageURL.equals("default")){

            }
            else{
                Picasso.with(context).load(imageURL).networkPolicy(NetworkPolicy.OFFLINE)
                        .placeholder(R.drawable.ic_blank_profile).into(meetingImage, new Callback() {
                    @Override
                    public void onSuccess() {
                        //do nothing if an image is found offline
                    }
                    @Override
                    public void onError() {
                        Picasso.with(context).load(imageURL).placeholder(R.drawable.ic_blank_profile).into(meetingImage);
                    }
                });
            }
        }



    }
}
