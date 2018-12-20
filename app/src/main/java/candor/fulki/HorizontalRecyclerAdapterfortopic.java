package candor.fulki;


import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import java.util.ArrayList;

import static android.content.ContentValues.TAG;


public class HorizontalRecyclerAdapterfortopic extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    public interface OnButtonClickListener{
        void onButtonClick(int pos);
    }
    OnButtonClickListener onButtonClickListener;

    private ArrayList<String> mList;
    private Context mContext;

    public HorizontalRecyclerAdapterfortopic(ArrayList<String> list, Context context) {
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
                View v1 = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.detail_list_item_fortopic, viewGroup, false);
                return new CellViewHolder(v1);
            }
        }
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder viewHolder, int position) {
        final CellViewHolder cellViewHolder = (CellViewHolder) viewHolder;
        cellViewHolder.button.setText(mList.get(position));
        if(position==1){
            Log.d(TAG, "onClick: buet1111");
            cellViewHolder.button.setBackground(mContext.getDrawable(R.drawable.background_user_item));

        }
        else if(position==2){
            cellViewHolder.button.setBackground(mContext.getDrawable(R.drawable.background_user_item));
        }
        else if(position==3){

            cellViewHolder.button.setBackground(mContext.getDrawable(R.drawable.background_user_item));
        }
        else if(position==4){

            cellViewHolder.button.setBackground(mContext.getDrawable(R.drawable.background_user_item));
        }
        else if(position==5){

            cellViewHolder.button.setBackground(mContext.getDrawable(R.drawable.background_user_item));
        }
        else if(position==6){
            cellViewHolder.button.setBackground(mContext.getDrawable(R.drawable.background_user_item));
        }
        else if(position==7){

            cellViewHolder.button.setBackground(mContext.getDrawable(R.drawable.background_user_item));
        }
        else if(position==8){

            cellViewHolder.button.setBackground(mContext.getDrawable(R.drawable.background_user_item));
        }
        else if(position==9){

            cellViewHolder.button.setBackground(mContext.getDrawable(R.drawable.background_user_item));
        }
        else if(position==10){

            cellViewHolder.button.setBackground(mContext.getDrawable(R.drawable.background_user_item));
        }
        else if(position==11){

            cellViewHolder.button.setBackground(mContext.getDrawable(R.drawable.background_user_item));
        }
        else if(position==0){

            cellViewHolder.button.setBackground(mContext.getDrawable(R.drawable.background_user_item));
        }

        cellViewHolder.button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                passData(cellViewHolder.getAdapterPosition());
                //passData1(button.getText().toString());

            }
        });


    }

    @Override
    public int getItemCount() {
        if (mList == null)
            return 0;
        return mList.size();
    }


    private void passData(int pos){

        try{
            onButtonClickListener = (OnButtonClickListener) mContext;
        }catch (ClassCastException e){
            Log.e(TAG, "loadMoreData: ClassCastException: " +e.getMessage() );
        }

        try{
            onButtonClickListener.onButtonClick(pos);
        }catch (NullPointerException e){
            Log.e(TAG, "loadMoreData: ClassCastException: " +e.getMessage() );
        }
    }




}