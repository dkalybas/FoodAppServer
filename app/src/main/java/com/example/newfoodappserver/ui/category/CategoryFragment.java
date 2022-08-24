package com.example.newfoodappserver.ui.category;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AnimationUtils;
import android.view.animation.LayoutAnimationController;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProviders;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.Unbinder;
import dmax.dialog.SpotsDialog;

import com.bumptech.glide.Glide;
import com.example.newfoodappserver.EventBus.ToastEvent;
import com.example.newfoodappserver.R;
import com.example.newfoodappserver.adapter.MyCategoriesAdapter;
import com.example.newfoodappserver.common.Common;
import com.example.newfoodappserver.common.MySwipeHelper;
import com.example.newfoodappserver.common.SpacesItemDecoration;
import com.example.newfoodappserver.model.CategoryModel;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import org.greenrobot.eventbus.EventBus;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public class CategoryFragment extends Fragment {

    private static final int PICK_IMAGE_REQUEST =1234 ;
    private CategoryViewModel categoryViewModel;

    Unbinder unbinder;
    @BindView(R.id.recycler_menu)
    RecyclerView recycler_menu;
    AlertDialog dialog;
    LayoutAnimationController layoutAnimationController;
    MyCategoriesAdapter adapter;
    List<CategoryModel> categoryModels;
    ImageView img_category;
    private Uri imageUri=null;

    FirebaseStorage storage;
    StorageReference storageReference;


    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        categoryViewModel =
                ViewModelProviders.of(this).get(CategoryViewModel.class);
        View root = inflater.inflate(R.layout.fragment_category, container, false);

        unbinder = ButterKnife.bind(this,root);
        initViews();


        categoryViewModel.getMessageError().observe(getViewLifecycleOwner(), new Observer<String>() {
            @Override
            public void onChanged(String s) {
                Toast.makeText(getContext(),"+"+s,Toast.LENGTH_SHORT).show();
                dialog.dismiss();
            }
        });
        categoryViewModel.getCategoryListMutable().observe(getViewLifecycleOwner(),categoryModelList -> {

            dialog.dismiss();
            categoryModels = categoryModelList;
            adapter = new MyCategoriesAdapter(getContext(),categoryModels);
            recycler_menu.setAdapter(adapter) ;
            recycler_menu.setLayoutAnimation(layoutAnimationController);

        } );



        return root;
    }
    private void initViews() {
        storage = FirebaseStorage.getInstance();
        storageReference = storage.getReference();

        dialog = new SpotsDialog.Builder().setContext(getContext()).setCancelable(false).build();
        ///   dialog.show();
        layoutAnimationController = AnimationUtils.loadLayoutAnimation(getContext(),R.anim.layout_item_from_left);
        LinearLayoutManager layoutManager = new LinearLayoutManager(getContext());




        recycler_menu.setLayoutManager(layoutManager);
        recycler_menu.addItemDecoration(new DividerItemDecoration(getContext(),layoutManager.getOrientation()));

        MySwipeHelper mySwipeHelper = new MySwipeHelper(getContext(),recycler_menu,200) {
            @Override
            public void instantiateMyButton(RecyclerView.ViewHolder viewHolder, List<MyButton> buf) {
                buf.add(new MyButton(getContext(),"Delete",30,0, Color.parseColor("#333639"),
                        pos ->{

                                Common.categorySelected = categoryModels.get(pos);
                                showDeleteDialog();

                        } ));

                buf.add(new MyButton(getContext(),"Update",30,0, Color.parseColor("#560027"),
                        pos ->{

                            Common.categorySelected = categoryModels.get(pos);
                            showUpdateDialog();

                        } ));



            }
        };

        setHasOptionsMenu(true);

    }

    @Override
    public void onCreateOptionsMenu(@NonNull Menu menu, @NonNull MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
        inflater.inflate(R.menu.action_bar_menu,menu);
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == R.id.action_create){


            // Toast.makeText(getContext(),"Clicked to create categroy !!",Toast.LENGTH_SHORT).show();

            showAddDialog();
        }


        return super.onOptionsItemSelected(item);
    }

    private void showDeleteDialog() {
        androidx.appcompat.app.AlertDialog.Builder builder = new  androidx.appcompat.app.AlertDialog.Builder(getContext());
        builder.setTitle("Delete");
        builder.setMessage("Do you really want to delete this ? ");
        builder.setNegativeButton("CANCEL", (dialog, which) -> {
            dialog.dismiss();

        });
        builder.setPositiveButton("OK",(dialog1, which) -> {

                deleteCategory();

        });

        androidx.appcompat.app.AlertDialog dialog = builder.create()   ;
        dialog.show();


    }

    private void deleteCategory() {


        FirebaseDatabase.getInstance()
                .getReference(Common.RESTAURANT_REF)
                .child(Common.currentServerUser.getRestaurant())
                .child(Common.CATEGORY_REF)
                .child(Common.categorySelected.getMenu_id())
               .removeValue()
                .addOnFailureListener(e -> Toast.makeText(getContext(),""+e.getMessage(),Toast.LENGTH_SHORT).show())
                .addOnCompleteListener(task -> {

                    categoryViewModel.loadCategories();

                    EventBus.getDefault().postSticky(new ToastEvent(Common.ACTION.DELETE,false));

                });



    }

    private void showUpdateDialog() {
            androidx.appcompat.app.AlertDialog.Builder builder = new  androidx.appcompat.app.AlertDialog.Builder(getContext());
            builder.setTitle("Update");
            builder.setMessage("Please fill information ");

            View itemView = LayoutInflater.from(getContext()).inflate(R.layout.layout_update_category,null);
            EditText my_category_name = (EditText)itemView.findViewById(R.id.my_category_name);
            img_category = (ImageView)itemView.findViewById(R.id.img_category);

            //Setting DATA here
        my_category_name.setText(new StringBuilder("").append(Common.categorySelected.getName()));
        Glide.with(getContext()).load(Common.categorySelected.getImage()).into(img_category);

        //Setting Event
        img_category.setOnClickListener(v -> {

            Intent intent = new Intent();

            intent.setType("image/*");
            intent.setAction(Intent.ACTION_GET_CONTENT);
            startActivityForResult(Intent.createChooser(intent,"Select Picture "),PICK_IMAGE_REQUEST);

        });


        builder.setNegativeButton("CANCEL", (dialogInterface, which) -> dialogInterface.dismiss());
        builder.setPositiveButton("UPDATE ", (dialogInterface, which) -> {

            Map<String,Object> updateData = new HashMap<>();
            updateData.put("name",my_category_name.getText().toString());

            if (imageUri!=null){
                // here we update Firebase storage to upload images
                dialog.setMessage("Uploading");
                dialog.show();

                String  unique_name = UUID.randomUUID().toString();
                StorageReference imageFolder = storageReference.child("images/"+unique_name);

                imageFolder.putFile(imageUri)
                        .addOnFailureListener(e -> {
                            dialog.dismiss();
                            Toast.makeText(getContext(),""+e.getMessage(),Toast.LENGTH_SHORT).show();
                        }).addOnCompleteListener(task -> {


                            dialog.dismiss();
                            imageFolder.getDownloadUrl().addOnSuccessListener(uri -> {

                                updateData.put("image",uri.toString());
                                updateCategory(updateData);

                            });

                        }).addOnProgressListener(taskSnapshot -> {


                            double progress = (100.0*taskSnapshot.getBytesTransferred()/taskSnapshot.getTotalByteCount());
                            dialog.setMessage(new StringBuilder("Uploading: ").append(progress).append("%"));

                });





            }else {

                updateCategory(updateData);

            }



        });
        builder.setView(itemView);
        androidx.appcompat.app.AlertDialog dialog = builder.create()   ;
        dialog.show();


    }

    private void showAddDialog() {
        androidx.appcompat.app.AlertDialog.Builder builder = new  androidx.appcompat.app.AlertDialog.Builder(getContext());
        builder.setTitle("Create");
        builder.setMessage("Please fill information ");

        View itemView = LayoutInflater.from(getContext()).inflate(R.layout.layout_update_category,null);
        EditText my_category_name = (EditText)itemView.findViewById(R.id.my_category_name);
        img_category = (ImageView)itemView.findViewById(R.id.img_category);

        //Setting DATA here

            Glide.with(getContext()).load(R.drawable.ic_image_gray_24dp).into(img_category);

        //Setting Event
        img_category.setOnClickListener(v -> {

            Intent intent = new Intent();

            intent.setType("image/*");
            intent.setAction(Intent.ACTION_GET_CONTENT);
            startActivityForResult(Intent.createChooser(intent,"Select Picture "),PICK_IMAGE_REQUEST);

        });


        builder.setNegativeButton("CANCEL", (dialogInterface, which) -> dialogInterface.dismiss());
        builder.setPositiveButton("CREATE ", (dialogInterface, which) -> {


        CategoryModel categoryModel = new CategoryModel();
        categoryModel.setName(my_category_name.getText().toString());
        categoryModel.setFoods(new ArrayList<>());  // here we create an empty list for foodlist


            if (imageUri!=null){
                // here we update Firebase storage to upload images
                dialog.setMessage("Uploading");
                dialog.show();

                String  unique_name = UUID.randomUUID().toString();
                StorageReference imageFolder = storageReference.child("images/"+unique_name);

                imageFolder.putFile(imageUri)
                        .addOnFailureListener(e -> {
                            dialog.dismiss();
                            Toast.makeText(getContext(),""+e.getMessage(),Toast.LENGTH_SHORT).show();
                        }).addOnCompleteListener(task -> {


                    dialog.dismiss();
                    imageFolder.getDownloadUrl().addOnSuccessListener(uri -> {


                        categoryModel.setImage(uri.toString());
                       addCategory(categoryModel);

                    });

                }).addOnProgressListener(taskSnapshot -> {


                    double progress = (100.0*taskSnapshot.getBytesTransferred()/taskSnapshot.getTotalByteCount());
                    dialog.setMessage(new StringBuilder("Uploading: ").append(progress).append("%"));

                });





            }else {

                addCategory(categoryModel);

            }



        });
        builder.setView(itemView);
        androidx.appcompat.app.AlertDialog dialog = builder.create()   ;
        dialog.show();


    }

    private void updateCategory(Map<String, Object> updateData) {

        FirebaseDatabase.getInstance()
                .getReference(Common.RESTAURANT_REF)
                .child(Common.currentServerUser.getRestaurant())
                .child(Common.CATEGORY_REF)
                .child(Common.categorySelected.getMenu_id())
                .updateChildren(updateData)
                .addOnFailureListener(e -> Toast.makeText(getContext(),""+e.getMessage(),Toast.LENGTH_SHORT).show())
                .addOnCompleteListener(task -> {

                categoryViewModel.loadCategories();

                    EventBus.getDefault().postSticky(new ToastEvent(Common.ACTION.UPDATE,false));

                });




    }

    private void addCategory(CategoryModel categoryModel) {

        FirebaseDatabase.getInstance()
                .getReference(Common.RESTAURANT_REF)
                .child(Common.currentServerUser.getRestaurant())
                .child(Common.CATEGORY_REF)
                .push()
                .setValue(categoryModel)
                .addOnFailureListener(e -> Toast.makeText(getContext(),""+e.getMessage(),Toast.LENGTH_SHORT).show())
                .addOnCompleteListener(task -> {

                    categoryViewModel.loadCategories();

                    EventBus.getDefault().postSticky(new ToastEvent(Common.ACTION.CREATE,false));

                });




    }


    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode==PICK_IMAGE_REQUEST && requestCode== Activity.RESULT_OK){

                        if (data != null && data.getData() != null  ){

                                imageUri = data.getData();
                                img_category.setImageURI(imageUri);


                        }

        }


    }
}
