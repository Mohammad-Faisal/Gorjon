package candor.fulki.search;


import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import java.util.ArrayList;


import candor.fulki.R;

import static android.content.ContentValues.TAG;


public class HorizontalRecyclerAdapterforsearch extends RecyclerView.Adapter<RecyclerView.ViewHolder> {



    public interface OnButtonClickListener1{
        void onButtonClick1(String text);
    }
    OnButtonClickListener1 onButtonClickListener1;

    private ArrayList<String> mList;
    private Context mContext;

    public HorizontalRecyclerAdapterforsearch(ArrayList<String> list, Context context) {
        this.mList = list;
        mContext=context;
    }

    private class CellViewHolder extends RecyclerView.ViewHolder  {
        private Button button;

        public CellViewHolder(View itemView) {
            super(itemView);
            button=itemView.findViewById(R.id.button);
        }

    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(final ViewGroup viewGroup, int viewType) {

        switch (viewType) {
            default: {
                View v1 = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.detail_list_item_for_search, viewGroup, false);
                return new CellViewHolder(v1);
            }
        }
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder viewHolder, int position) {
        final CellViewHolder cellViewHolder = (CellViewHolder) viewHolder;
        cellViewHolder.button.setText(mList.get(position));

        cellViewHolder.button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Button button=(Button)v;
                passData1(button.getText().toString());

            }
        });


    }

    @Override
    public int getItemCount() {
        if (mList == null)
            return 0;
        return mList.size();
    }




    private void passData1(String text){

        try{
            onButtonClickListener1 = (OnButtonClickListener1) mContext;
        }catch (ClassCastException e){
            Log.e(TAG, "loadMoreData: ClassCastException: " +e.getMessage() );
        }

        try{
            onButtonClickListener1.onButtonClick1(text);
        }catch (NullPointerException e){
            Log.e(TAG, "loadMoreData: ClassCastException: " +e.getMessage() );
        }
    }


}