package com.example.newfoodappserver.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.example.newfoodappserver.R;
import com.example.newfoodappserver.callback.IRecyclerClickListener;
import com.example.newfoodappserver.common.Common;
import com.example.newfoodappserver.model.BestDealsModel;
import com.example.newfoodappserver.model.FoodModel;
import com.example.newfoodappserver.model.MostPopularModel;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.database.FirebaseDatabase;

import net.cachapa.expandablelayout.ExpandableLayout;

import org.greenrobot.eventbus.EventBus;

import java.util.List;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.Unbinder;

public class MyFoodListAdapter extends RecyclerView.Adapter<MyFoodListAdapter.MyViewHolder> {

    private Context context;
    private List<FoodModel> foodModelList;

    private ExpandableLayout lastExpandable;


    public MyFoodListAdapter(Context context, List<FoodModel> foodModelList) {
        this.context = context;
        this.foodModelList = foodModelList;

    }

    @NonNull
    @Override
    public MyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new MyViewHolder(LayoutInflater.from(context)
                .inflate(R.layout.layout_food_item,parent,false));
    }

    @Override
    public void onBindViewHolder(@NonNull MyViewHolder holder, int position) {
        Glide.with(context).load(foodModelList.get(position).getImage()).into(holder.img_food_image);
        holder.txt_food_price.setText(new StringBuilder("$")
                .append(foodModelList.get(position).getPrice()));
        holder.txt_food_name.setText(new StringBuilder("")
                .append(foodModelList.get(position).getName()));

        //Hew the Event

        holder.setListener((view, pos) -> {

             Common.selectedFood = foodModelList.get(pos);
            Common.selectedFood.setKey(String.valueOf(pos));

            // Showing Expandable layout

            if (lastExpandable!=null&&lastExpandable.isExpanded())lastExpandable.collapse();

            if (!holder.expandable_layout.isExpanded()){

                holder.expandable_layout.setSelected(true);
                holder.expandable_layout.expand();

            }else {

                holder.expandable_layout.collapse();
                holder.expandable_layout.setSelected(false);

            }

            lastExpandable = holder.expandable_layout;



        });

        holder.btn_best_deal.setOnClickListener(v -> {

            makeFoodToBestDealOfRestaurant(foodModelList.get(position));

        });

        holder.btn_most_popular.setOnClickListener(v -> {

            makeFoodToMostPopularOfRestaurant(foodModelList.get(position));

        });


    }

    private void makeFoodToMostPopularOfRestaurant(FoodModel foodModel) {

        MostPopularModel mostPopularModel = new MostPopularModel();
        mostPopularModel.setName(foodModel.getName());
        mostPopularModel.setMenu_id(Common.categorySelected.getMenu_id());
        mostPopularModel.setFood_id(foodModel.getId());
        mostPopularModel.setImage(foodModel.getImage());

        FirebaseDatabase.getInstance()
                .getReference(Common.RESTAURANT_REF)
                .child(Common.currentServerUser.getRestaurant())
                .child(Common.MOST_POPULAR)
                .child(new StringBuilder(mostPopularModel.getMenu_id()).append("_").append(mostPopularModel.getFood_id()).toString())   // using menu_foodid to key
                .setValue(mostPopularModel)
                .addOnFailureListener(e -> {
                    Toast.makeText(context,"" + e.getMessage(),Toast.LENGTH_SHORT).show();
                }).addOnSuccessListener(unused -> {
            Toast.makeText(context," Creation of Most Popular successful ",Toast.LENGTH_SHORT).show();
        });


    }

    private void makeFoodToBestDealOfRestaurant(FoodModel foodModel) {

        BestDealsModel bestDealsModel = new BestDealsModel();
        bestDealsModel.setName(foodModel.getName());
        bestDealsModel.setMenu_id(Common.categorySelected.getMenu_id());
        bestDealsModel.setFood_id(foodModel.getId());
        bestDealsModel.setImage(foodModel.getImage());

        FirebaseDatabase.getInstance()
                .getReference(Common.RESTAURANT_REF)
                .child(Common.currentServerUser.getRestaurant())
                .child(Common.BEST_DEALS)
                .child(new StringBuilder(bestDealsModel.getMenu_id()).append("_").append(bestDealsModel.getFood_id()).toString())   // using menu_foodid to key
                .setValue(bestDealsModel)
                .addOnFailureListener(e -> {
                    Toast.makeText(context,"" + e.getMessage(),Toast.LENGTH_SHORT).show();
                }).addOnSuccessListener(unused -> {
                 Toast.makeText(context," Creation of Best Deal successful ",Toast.LENGTH_SHORT).show();
                });

    }

    @Override
    public int getItemCount() {
        return foodModelList.size();
    }

    public FoodModel getItemAtPosition(int pos){

        return foodModelList.get(pos);

    }

    public class MyViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {

        private Unbinder unbinder;
        @BindView(R.id.txt_food_name)
        TextView txt_food_name;
        @BindView(R.id.txt_food_price)
        TextView txt_food_price;
        @BindView(R.id.img_food_image)
        ImageView img_food_image;
        @BindView(R.id.expandable_layout)
        ExpandableLayout expandable_layout;
        @BindView(R.id.btn_best_deal)
        Button btn_best_deal;
        @BindView(R.id.btn_most_popular)
        Button btn_most_popular;


        IRecyclerClickListener listener ;


        public void setListener(IRecyclerClickListener listener) {
            this.listener = listener;
        }


        public MyViewHolder(@NonNull View itemView) {
            super(itemView);
            unbinder = ButterKnife.bind(this,itemView);
            itemView.setOnClickListener(this);


        }


        @Override
        public void onClick(View v) {
            listener.onItemClickListener(v,getAdapterPosition());
        }
    }
}
