package com.example.newfoodappserver.view_holder;

import android.view.View;
import android.widget.TextView;

import com.example.newfoodappserver.R;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.Unbinder;
import de.hdodenhof.circleimageview.CircleImageView;

public class ChatTextHolder extends RecyclerView.ViewHolder {

    private Unbinder unbinder ;
    @BindView(R.id.txt_time)
    public TextView txt_time;
    @BindView(R.id.txt_email)
    public   TextView txt_email;
    @BindView(R.id.txt_chat_message)
    public  TextView txt_chat_message;
    @BindView(R.id.profile_image)
    public CircleImageView profile_image;




    public ChatTextHolder(@NonNull View itemView) {
        super(itemView);
        unbinder = ButterKnife.bind(this,itemView);

    }








}
