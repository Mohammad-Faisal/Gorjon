package candor.fulki.adapters;

import android.app.Activity;
import android.content.Context;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import java.util.List;

import candor.fulki.R;
import candor.fulki.models.Categories;


public class CategoriesAdapter extends RecyclerView.Adapter<CategoriesAdapter.CategoriesViewHolder> {



    private List<Categories> mCategoryList;
    private Context context;
    private Activity activity;


    public CategoriesAdapter(List<Categories> mCategoryList , Context context , Activity activity){
        this.mCategoryList = mCategoryList;
        this.context  = context;
        this.activity = activity;
    }

    @NonNull
    @Override
    public CategoriesAdapter.CategoriesViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int i) {
        View v;
        v = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_category, parent , false);
        return new CategoriesAdapter.CategoriesViewHolder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull CategoriesAdapter.CategoriesViewHolder CategoriesViewHolder, int i) {
        Categories temp  = mCategoryList.get(i);
        if(temp.getSelected()){
            CategoriesViewHolder.linearLayout.setBackgroundResource(R.drawable.textview_selected);
            CategoriesViewHolder.categoryImage.setVisibility(View.VISIBLE);
        }else{
            CategoriesViewHolder.linearLayout.setBackgroundResource(R.drawable.textview_not_selected);
            CategoriesViewHolder.categoryImage.setVisibility(View.GONE);
        }
        CategoriesViewHolder.categoryText.setText(temp.getName());
        CategoriesViewHolder.categoryText.setOnClickListener(v -> {
            if(mCategoryList.get(i).getSelected()){
                CategoriesViewHolder.linearLayout.setBackgroundResource(R.drawable.textview_not_selected);
                CategoriesViewHolder.categoryImage.setVisibility(View.GONE);
                mCategoryList.get(i).setSelected(false);
            }else{
                CategoriesViewHolder.linearLayout.setBackgroundResource(R.drawable.textview_selected);
                CategoriesViewHolder.categoryImage.setVisibility(View.VISIBLE);
                mCategoryList.get(i).setSelected(true);
            }
        });
    }

    @Override
    public int getItemCount() {
        return mCategoryList.size();
    }

    public class CategoriesViewHolder extends RecyclerView.ViewHolder {
        TextView categoryText = itemView.findViewById(R.id.settings_category);
        ImageView categoryImage = itemView.findViewById(R.id.image_tik);
        LinearLayout linearLayout= itemView.findViewById(R.id.categoryLinear);
        public CategoriesViewHolder(@NonNull View itemView) {
            super(itemView);
        }
    }
}
