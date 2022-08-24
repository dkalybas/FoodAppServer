package com.example.newfoodappserver;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import butterknife.Optional;

import android.content.DialogInterface;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.example.newfoodappserver.EventBus.AddonSizeEditEvent;
import com.example.newfoodappserver.EventBus.SelectAddOnModel;
import com.example.newfoodappserver.EventBus.SelectSizeModel;
import com.example.newfoodappserver.EventBus.UpdateAddonModel;
import com.example.newfoodappserver.EventBus.UpdateSizeModel;
import com.example.newfoodappserver.adapter.MyAddonAdapter;
import com.example.newfoodappserver.adapter.MySizeAdapter;
import com.example.newfoodappserver.common.Common;
import com.example.newfoodappserver.model.AddonModel;
import com.example.newfoodappserver.model.SizeModel;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.database.FirebaseDatabase;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;


public class SizeAddonEditActivity extends AppCompatActivity {

    @BindView(R.id.toolbar)
    Toolbar toolbar;
    @Nullable
    @BindView(R.id.my_name)
    EditText my_name;
    @Nullable
    @BindView(R.id.my_price)
    EditText my_price;
    @BindView(R.id.btn_create)
    Button btn_create;
    @BindView(R.id.btn_edit)
    Button btn_edit;
    @BindView(R.id.recycler_addon_size)
    RecyclerView recycler_addon_size;

            //Variable
    MySizeAdapter adapter ;
    MyAddonAdapter addonAdapter;
    private int foodEditPosition = -1;
    private boolean needSave = false;
    private  boolean isAddon = false;

    //Event

    @Optional
    @OnClick(R.id.btn_create)
    void onCreateNew(){

        if (!isAddon) //Size
        {
            if (adapter!=null){

                SizeModel sizeModel = new SizeModel();
                sizeModel.setName(my_name.getText().toString());
                sizeModel.setPrice(Long.valueOf(my_price.getText().toString()));
                adapter.addNewSize(sizeModel);

            }


        }else{//here is the Addon


            if (addonAdapter!=null){

                AddonModel addonModel = new AddonModel();
                addonModel.setName(my_name.getText().toString());
                addonModel.setPrice(Long.valueOf(my_price.getText().toString()));
                addonAdapter.addNewSize(addonModel);

            }


        }


    }

    @Optional
    @OnClick(R.id.btn_edit)
    void onEdit(){

        if (!isAddon) // Size
        {
            if (adapter!=null){

                SizeModel sizeModel = new SizeModel();
                sizeModel.setName(my_name.getText().toString());
                sizeModel.setPrice(Long.valueOf(my_price.getText().toString()));
                adapter.editSize(sizeModel);
                    }
            }else{ // AddOn here


            if (addonAdapter!=null){

                AddonModel addonModel = new AddonModel();
                addonModel.setName(my_name.getText().toString());
                addonModel.setPrice(Long.valueOf(my_price.getText().toString()));
                addonAdapter.editSize(addonModel);


            }

        }



    }


            //Menu
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.addon_size_menu,menu);

        return true;


    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
            switch (item.getItemId()){

                case R.id.action_save:
                    saveData();
                    break;
                case android.R.id.home:{

                    if (needSave){

                        AlertDialog.Builder builder = new AlertDialog.Builder(this);
                        builder.setTitle("Cancel?")
                                .setMessage("DO you really want close without saving ?")
                                .setNegativeButton("CANCEL", (dialogInterface, which) -> dialogInterface.dismiss())
                                .setPositiveButton("OK",((dialogInterface, which) -> {

                                    needSave = false;
                                    closeActivity();



                                }));
                        AlertDialog dialog = builder.create();
                        dialog.show();
                    }else
                        {

                            closeActivity();

                    }


                }
                    break;


            }

        return super.onOptionsItemSelected(item);


    }

    private void saveData() {
                if (foodEditPosition!= -1){

                    Common.categorySelected.getFoods().set(foodEditPosition,Common.selectedFood); // =Saving food to category

                    Map<String,Object> updateData = new HashMap<>();
                    updateData.put("foods",Common.categorySelected.getFoods());

                    FirebaseDatabase.getInstance()
                            .getReference(Common.RESTAURANT_REF)
                            .child(Common.currentServerUser.getRestaurant())
                            .child(Common.CATEGORY_REF)
                            .child(Common.categorySelected.getMenu_id())
                            .updateChildren(updateData)
                            .addOnFailureListener(e -> Toast.
                                    makeText(SizeAddonEditActivity.this,""+e.getMessage(),Toast.LENGTH_SHORT).
                                    show()).addOnCompleteListener(task -> {

                                if (task.isSuccessful()){

                                    Toast.makeText(this,"Reload Success!! ",Toast.LENGTH_SHORT).show();
                                    needSave = false;
                                    my_price.setText("0");
                                    my_name.setText("");

                                }


                            });


                }




    }

    private void closeActivity() {
        my_name.setText("");
        my_price.setText("0");
        finish();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_size_addon_edit);


        init();


    }

    private void init() {

        ButterKnife.bind(this);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowCustomEnabled(true);

        recycler_addon_size.setHasFixedSize(true);
        LinearLayoutManager layoutManager = new LinearLayoutManager(this);
        recycler_addon_size.setLayoutManager(layoutManager);
        recycler_addon_size.addItemDecoration(new DividerItemDecoration(this,layoutManager.getOrientation()));


    }

    // Registering Event here


    @Override
    protected void onStart() {
        super.onStart();
        if (!EventBus.getDefault().isRegistered(this))
            EventBus.getDefault().register(this);


    }


    @Override
    protected void onStop() {
        EventBus.getDefault().removeStickyEvent(UpdateSizeModel.class);
        EventBus.getDefault().removeStickyEvent(UpdateAddonModel.class);
        if (EventBus.getDefault().isRegistered(this))
            EventBus.getDefault().unregister(this);

        super.onStop();
    }

    //Receiving Event

    @Subscribe(sticky = true,threadMode = ThreadMode.MAIN)
    public void onAddonSizeReceive(AddonSizeEditEvent event){
        if (!event.isAddon())// if event is size
        {

            if (Common.selectedFood.getSize()==null)
                    Common.selectedFood.setSize(new ArrayList<>());

                adapter = new MySizeAdapter(this,Common.selectedFood.getSize());
                foodEditPosition = event.getPos(); // save food edit o update
                recycler_addon_size.setAdapter(adapter);

                isAddon = event.isAddon();



        }else {// else if it is addOn

            if (Common.selectedFood.getAddon()==null)
                Common.selectedFood.setAddon( new ArrayList<>());

                addonAdapter = new MyAddonAdapter(this,Common.selectedFood.getAddon());
                foodEditPosition = event.getPos(); // save food edit o update
                recycler_addon_size.setAdapter(addonAdapter);

                isAddon = event.isAddon();



        }




    }


    @Subscribe(sticky = true,threadMode = ThreadMode.MAIN)
    public void onSizeModelUpdate(UpdateSizeModel event){

        if (event.getSizeModelList()!=null){


            needSave=true;
            Common.selectedFood.setSize(event.getSizeModelList());

        }

    }


    @Subscribe(sticky = true,threadMode = ThreadMode.MAIN)
    public void onAddonModelUpdate(UpdateAddonModel event){

        if (event.getAddonModels()!=null){


            needSave=true;
            Common.selectedFood.setAddon(event.getAddonModels());

        }

    }




    @Subscribe(sticky = true,threadMode = ThreadMode.MAIN)
    public void onSelectSizeModelUpdate(SelectSizeModel event){

            if (event.getSizeModel()!=null){

                my_name.setText(event.getSizeModel().getName());
                my_price.setText(String.valueOf(event.getSizeModel().getPrice()));

                btn_edit.setEnabled(true);

            }else {
                btn_edit.setEnabled(false);
            }
    }

    @Subscribe(sticky = true,threadMode = ThreadMode.MAIN)
    public void onSelectAddonUpdate(SelectAddOnModel event){

        if (event.getAddonModel()!=null){

            my_name.setText(event.getAddonModel().getName());
            my_price.setText(String.valueOf(event.getAddonModel().getPrice()));

            btn_edit.setEnabled(true);

        }else {
            btn_edit.setEnabled(false);
        }
    }






}
