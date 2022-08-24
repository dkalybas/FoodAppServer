package com.example.newfoodappserver.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.example.newfoodappserver.EventBus.SelectAddOnModel;
import com.example.newfoodappserver.EventBus.SelectSizeModel;
import com.example.newfoodappserver.EventBus.UpdateAddonModel;
import com.example.newfoodappserver.EventBus.UpdateSizeModel;
import com.example.newfoodappserver.R;
import com.example.newfoodappserver.callback.IRecyclerClickListener;
import com.example.newfoodappserver.model.AddonModel;
import com.example.newfoodappserver.model.SizeModel;

import org.greenrobot.eventbus.EventBus;

import java.util.List;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.RecyclerView;
import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.Optional;
import butterknife.Unbinder;

public class MyAddonAdapter extends RecyclerView.Adapter<MyAddonAdapter.MyViewHolder> {

    Context context;
    List<AddonModel> addonModels;
    UpdateAddonModel updateAddOnModel;
    int editPos ;


    public MyAddonAdapter(Context context, List<AddonModel> addonModels) {
        this.context = context;
        this.addonModels = addonModels;

        this.updateAddOnModel = new UpdateAddonModel();

         editPos = -1 ;
    }

    @NonNull
    @Override
    public MyAddonAdapter.MyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {

        return new MyAddonAdapter.MyViewHolder(LayoutInflater.from(context)
                .inflate(R.layout.layout_size_addon_display,parent,false));





    }

//new StringBuilder("$")
//                .append

    @Override
    public void onBindViewHolder(@NonNull MyAddonAdapter.MyViewHolder holder, int position) {
        holder.txt_name.setText(addonModels.get(position).getName());
        holder.txt_price.setText(String.valueOf(addonModels.get(position).getPrice()));

        //Event
        holder.img_delete.setOnClickListener(v -> {
            //Delete Item
            addonModels.remove(position);
            notifyItemRemoved(position);
            updateAddOnModel.setAddonModels(addonModels);  //Set for event
            EventBus.getDefault().postSticky(updateAddOnModel);  //Send event

        });


        holder.setListener((view, pos) -> {
            editPos  = position;
            EventBus.getDefault().postSticky(new SelectAddOnModel(addonModels.get(pos)));
        });


    }

    @Override
    public int getItemCount() {
        return addonModels.size();
    }

    public void addNewSize(AddonModel addonModel) {

        addonModels.add(addonModel);
        notifyItemInserted(addonModels.size()-1);
        updateAddOnModel.setAddonModels(addonModels);
        EventBus.getDefault().postSticky(updateAddOnModel);





    }

    public void editSize(AddonModel addonModel) {

        if (editPos!=-1){

            addonModels.set(editPos,addonModel);
            notifyItemChanged(editPos);
            editPos = -1 ; //reset variable after success

            //Send update
            updateAddOnModel.setAddonModels(addonModels);
            EventBus.getDefault().postSticky(updateAddOnModel);
        }





    }

    public class MyViewHolder  extends  RecyclerView.ViewHolder {

        @BindView(R.id.txt_name)
        TextView txt_name;

        @BindView(R.id.txt_price)
        TextView txt_price;
        @BindView(R.id.img_delete)
        ImageView img_delete;

        @Nullable
        Unbinder unbinder;

        IRecyclerClickListener listener;

        public void setListener(IRecyclerClickListener listener) {
            this.listener = listener;
        }


        public MyViewHolder(@NonNull View itemView) {
            super(itemView);

            unbinder = ButterKnife.bind(this,itemView);
            itemView.setOnClickListener(v -> listener.onItemClickListener(v,getAdapterPosition()));

        }



    }





}
