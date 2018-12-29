package candor.fulki.adapters;


import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.TextView;

import com.nostra13.universalimageloader.core.ImageLoader;

import java.util.ArrayList;

import candor.fulki.R;
import candor.fulki.models.UserSearch;
import de.hdodenhof.circleimageview.CircleImageView;


public class VerticalRecyclerAdapterforsearch extends RecyclerView.Adapter<RecyclerView.ViewHolder>
        implements Filterable
{

    private ValueFilter valueFilter;

    @Override
    public Filter getFilter() {
        if (valueFilter == null) {

            valueFilter = new ValueFilter();
        }

        return valueFilter;
    }


    private ArrayList<UserSearch> mList;
    private ArrayList<UserSearch> mList1;
    private Context mContext;

    public VerticalRecyclerAdapterforsearch(ArrayList<UserSearch> list, Context context) {
        this.mList = list;
        this.mList1=list;
        mContext=context;
        getFilter();
    }



    public static class UsersViewHolder extends RecyclerView.ViewHolder {

        //View mView;
        TextView user_name;
        CircleImageView profileImage;
        TextView display_name;

        public UsersViewHolder(View itemView) {
            super(itemView);
            user_name = (TextView) itemView.findViewById(R.id.username);
            profileImage=itemView.findViewById(R.id.profile_image);
            display_name=itemView.findViewById(R.id.display_name);
        }




    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(final ViewGroup viewGroup, int viewType) {


        View v1 = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.layout_user_listitem, viewGroup, false);
        UsersViewHolder usersViewHolder=new UsersViewHolder(v1);


        return usersViewHolder;
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder viewHolder, final int position) {
        final UsersViewHolder usersViewHolder=(UsersViewHolder) viewHolder;
        ImageLoader imageLoader = ImageLoader.getInstance();

        usersViewHolder.display_name.setText(mList1.get(position).getDisplay_name());
        usersViewHolder.user_name.setText(mList1.get(position).getUsername());
        imageLoader.displayImage(mList1.get(position).getProfile_photo(),
                usersViewHolder.profileImage);


        usersViewHolder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
//                    usersViewHolder.user.setUser_id(mList1.get(position).getUser_id());
//                    usersViewHolder.user.setUsername(mList1.get(position).getUsername());
//                    Intent intent =  new Intent(mContext, ProfileActivity.class);
//                    intent.putExtra(mContext.getString(R.string.calling_activity), mContext.getString(R.string.search_activity));
//                    intent.putExtra(mContext.getString(R.string.intent_user),
//                            usersViewHolder.user);
//                    mContext.startActivity(intent);
            }
        });


    }

    @Override
    public int getItemCount() {
        if (mList1 == null)
            return 0;
        return mList1.size();
    }




    private class ValueFilter extends Filter {


        //Invoked in a worker thread to filter the data according to the constraint.
        @Override
        protected FilterResults performFiltering(CharSequence constraint) {

            FilterResults results = new FilterResults();

            if (constraint != null && constraint.length() > 0) {

                ArrayList<UserSearch> filterList = new ArrayList<>();

                for (int i = 0; i < mList.size(); i++) {

                    if(mList.get(i).getUsername().toLowerCase().startsWith(constraint.toString().toLowerCase())
                            || mList.get(i).getFirstname().toLowerCase().startsWith(constraint.toString().toLowerCase())
                            || mList.get(i).getLastname().toLowerCase().startsWith(constraint.toString().toLowerCase())
                            || mList.get(i).getMiddlename().toLowerCase().startsWith(constraint.toString().toLowerCase())
                            ){
                        filterList.add(mList.get(i));
                    }

                }


                results.count = filterList.size();

                results.values = filterList;

            } else {

                results.count = mList.size();

                results.values = mList;

            }

            return results;
        }


        //Invoked in the UI thread to publish the filtering results in the user interface.
        @SuppressWarnings("unchecked")
        @Override
        protected void publishResults(CharSequence constraint,
                                      FilterResults results) {

            mList1 = (ArrayList<UserSearch>) results.values;

            notifyDataSetChanged();


        }

    }


}