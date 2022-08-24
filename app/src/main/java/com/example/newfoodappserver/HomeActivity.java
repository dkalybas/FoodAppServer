package com.example.newfoodappserver;

import android.Manifest;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.net.Uri;
import android.os.Bundle;
import android.print.PrintAttributes;
import android.print.PrintDocumentAdapter;
import android.print.PrintManager;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.Menu;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.RadioButton;
import android.widget.TextView;
import android.widget.Toast;

import com.example.newfoodappserver.EventBus.CategoryClick;
import com.example.newfoodappserver.EventBus.ChangeMenuClick;
import com.example.newfoodappserver.EventBus.PrintOrderEvent;
import com.example.newfoodappserver.EventBus.ToastEvent;
import com.example.newfoodappserver.adapter.PdfDocumentAdapter;
import com.example.newfoodappserver.common.Common;
import com.example.newfoodappserver.common.PDFUtils;
import com.example.newfoodappserver.model.FCMSendData;
import com.example.newfoodappserver.model.OrderModel;
import com.example.newfoodappserver.model.RestaurantLocationModel;
import com.example.newfoodappserver.remote.IFCMService;
import com.example.newfoodappserver.remote.RetrofitFCMClient;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.tasks.CancellationToken;
import com.google.android.gms.tasks.OnCanceledListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.OnTokenCanceledListener;
import com.google.android.material.navigation.NavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.iid.FirebaseInstanceId;
import com.google.firebase.messaging.FirebaseMessaging;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.itextpdf.text.BaseColor;
import com.itextpdf.text.Document;
import com.itextpdf.text.DocumentException;
import com.itextpdf.text.Element;
import com.itextpdf.text.Font;
import com.itextpdf.text.PageSize;
import com.itextpdf.text.pdf.BaseFont;
import com.itextpdf.text.pdf.PdfDocument;
import com.itextpdf.text.pdf.PdfICCBased;
import com.itextpdf.text.pdf.PdfWriter;
import com.karumi.dexter.Dexter;
import com.karumi.dexter.PermissionToken;
import com.karumi.dexter.listener.PermissionDeniedResponse;
import com.karumi.dexter.listener.PermissionGrantedResponse;
import com.karumi.dexter.listener.PermissionRequest;
import com.karumi.dexter.listener.single.PermissionListener;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.lang.annotation.Documented;
import java.text.SimpleDateFormat;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.core.app.ActivityCompat;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.navigation.ui.AppBarConfiguration;
import androidx.navigation.ui.NavigationUI;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import butterknife.ButterKnife;
import butterknife.OnClick;
import io.reactivex.Observable;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.schedulers.Schedulers;

public class HomeActivity extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener {

    private static final int PICK_IMAGE_REQUEST = 7171;

    private AppBarConfiguration mAppBarConfiguration;
    private DrawerLayout drawer;
    private NavigationView navigationView;
    private NavController navController;
    private int menuClick = -1;

    private ImageView img_upload;
    private CompositeDisposable compositeDisposable = new CompositeDisposable();
    private IFCMService ifcmService;
    private Uri imgUri = null;

    private FirebaseStorage storage;
    private StorageReference storageReference;
    private AlertDialog dialog;

    @OnClick(R.id.fab_chat)
    void onOpenChatList() {

        startActivity(new Intent(this, ChatListActivity.class));

    }


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        ButterKnife.bind(this);


        init();


        drawer = findViewById(R.id.drawer_layout);
        navigationView = findViewById(R.id.nav_view);
        // Passing each menu ID as a set of Ids because each
        // menu should be considered as top level destinations.
        mAppBarConfiguration = new AppBarConfiguration.Builder(
                R.id.nav_category, R.id.nav_food_list, R.id.nav_order, R.id.nav_shipper)
                .setDrawerLayout(drawer)
                .build();
        navController = Navigation.findNavController(this, R.id.nav_host_fragment);
        NavigationUI.setupActionBarWithNavController(this, navController, mAppBarConfiguration);
        NavigationUI.setupWithNavController(navigationView, navController);
        navigationView.setNavigationItemSelectedListener(this);
        navigationView.bringToFront();

        View headerView = navigationView.getHeaderView(0);
        TextView txt_user = (TextView) headerView.findViewById(R.id.txt_user);
        Common.setSpanString("Hey", Common.currentServerUser.getName(), txt_user); //Copy this function from client app

        menuClick = R.id.nav_category;  // By default


        checkIsOpenFromActivity();

    }

    private void init() {

        ifcmService = RetrofitFCMClient.getInstance().create(IFCMService.class);
        storage = FirebaseStorage.getInstance();
        storageReference = storage.getReference();
        subscribeToTopic(Common.createTopicOrder());
        updateToken();


        dialog = new AlertDialog.Builder(this).setCancelable(false)
                .setMessage("Please wait ....")
                .create();

    }

    private void checkIsOpenFromActivity() {

        boolean isOpenFromNewOrder = getIntent().getBooleanExtra(Common.IS_OPEN_ACTIVITY_NEW_ORDER, false);
        if (isOpenFromNewOrder) {

            navController.popBackStack();
            navController.navigate(R.id.nav_order);
            menuClick = R.id.nav_order;

        }

    }

    private void updateToken() {


        FirebaseInstanceId.getInstance()
                .getInstanceId()
                .addOnFailureListener(e -> Toast.makeText(HomeActivity.this, "" + e.getMessage(), Toast.LENGTH_SHORT).show())
                .addOnSuccessListener(instanceIdResult -> {

                    Common.updateToken(HomeActivity.this, instanceIdResult.getToken(),
                            true,
                            false);

                });


    }

    private void subscribeToTopic(String topicOrder) {

        FirebaseMessaging.getInstance()
                .subscribeToTopic(topicOrder)
                .addOnFailureListener(e -> {
                    Toast.makeText(this, "" + e.getMessage(), Toast.LENGTH_SHORT).show();

                })
                .addOnCompleteListener(task -> {
                    if (!task.isSuccessful())
                        Toast.makeText(this, "Failed" + task.isSuccessful(), Toast.LENGTH_SHORT).show();

                });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {


        return true;
    }

    @Override
    public boolean onSupportNavigateUp() {
        NavController navController = Navigation.findNavController(this, R.id.nav_host_fragment);
        return NavigationUI.navigateUp(navController, mAppBarConfiguration)
                || super.onSupportNavigateUp();
    }

    @Override
    protected void onStart() {
        super.onStart();

        EventBus.getDefault().register(this);

    }

    @Override
    protected void onStop() {
        EventBus.getDefault().removeAllStickyEvents();   // fixing this event bus because
        // it will always be called after onActivityResult
        EventBus.getDefault().unregister(this);
        compositeDisposable.clear();
        super.onStop();

    }

    @Subscribe(sticky = true, threadMode = ThreadMode.MAIN)
    public void onCategoryClick(CategoryClick event) {

        if (event.isSuccess()) {

            if (menuClick != R.id.nav_food_list) {

                navController.navigate(R.id.nav_food_list);
                menuClick = R.id.nav_food_list;

            }
        }


    }

    @Subscribe(sticky = true, threadMode = ThreadMode.MAIN)
    public void onToastEvent(ToastEvent event) {

        if (event.getAction() == Common.ACTION.CREATE) {

            Toast.makeText(this, "Create successfull!", Toast.LENGTH_SHORT).show();


        } else if (event.getAction() == Common.ACTION.UPDATE) {

            Toast.makeText(this, "Delete Successfully happened!", Toast.LENGTH_SHORT).show();


        } else {

            EventBus.getDefault().postSticky(new ChangeMenuClick(event.isFromFoodList()));
        }

    }


    @Subscribe(sticky = true, threadMode = ThreadMode.MAIN)
    public void onChangeMenuClick(ChangeMenuClick event) {

        if (event.isFromFoodList()) {
            //Clearing
            navController.popBackStack(R.id.nav_category, true);
            navController.navigate(R.id.nav_category);


        } else {
            //Clearing
            navController.popBackStack(R.id.nav_food_list, true);
            navController.navigate(R.id.nav_food_list);


        }

        menuClick = -1;
    }


    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {

        item.setChecked(true);
        drawer.closeDrawers();
        switch (item.getItemId()) {

            case R.id.nav_category:
                if (item.getItemId() != menuClick) {
                    navController.popBackStack(); //Remove all back stack
                    navController.navigate(R.id.nav_category);
                }
                break;
            case R.id.nav_order:
                if (item.getItemId() != menuClick) {

                    navController.popBackStack(); //Remove all back stack
                    navController.navigate(R.id.nav_order);

                }
                break;
            case R.id.nav_shipper:
                if (item.getItemId() != menuClick) {

                    navController.popBackStack(); //Remove all back stack
                    navController.navigate(R.id.nav_shipper);

                }
                break;

            case R.id.nav_best_deals:
                if (item.getItemId() != menuClick) {

                    navController.popBackStack(); //Remove all back stack
                    navController.navigate(R.id.nav_best_deals);

                }
                break;

            case R.id.nav_most_popular:
                if (item.getItemId() != menuClick) {

                    navController.popBackStack(); //Remove all back stack
                    navController.navigate(R.id.nav_most_popular);

                }
                break;

            case R.id.nav_discount:
                if (item.getItemId() != menuClick) {

                    navController.popBackStack(); //Remove all back stack
                    navController.navigate(R.id.nav_discount);

                }
                break;

            case R.id.nav_location:

                showUpdateLocationDialog();

                break;

            case R.id.nav_send_news:
                showNewsDialog();
                break;


            case R.id.nav_sign_out:
                signOut();

                break;
            default:
                menuClick = -1;
                break;


        }

        menuClick = item.getItemId();
        return true;
    }

    private void showUpdateLocationDialog() {

        androidx.appcompat.app.AlertDialog.Builder builder = new androidx.appcompat.app.AlertDialog.Builder(this);
        builder.setTitle("Update Location");
        builder.setMessage("Do you want to update your location to this restaurant ? ");

        builder.setNegativeButton("NO", (dialog, which) -> {
            dialog.dismiss();
        });

        builder.setPositiveButton("YES", (dialog, which) -> {


            Dexter.withContext(HomeActivity.this)
                    .withPermission(Manifest.permission.ACCESS_FINE_LOCATION)
                    .withListener(new PermissionListener() {
                        @Override
                        public void onPermissionGranted(PermissionGrantedResponse permissionGrantedResponse) {


                            FusedLocationProviderClient fusedLocationProviderClient =
                                    LocationServices.getFusedLocationProviderClient(HomeActivity.this);

                            if (ActivityCompat.checkSelfPermission(HomeActivity.this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(HomeActivity.this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                                // TODO: Consider calling

                                return;
                            }
                            fusedLocationProviderClient.getCurrentLocation(LocationRequest.PRIORITY_HIGH_ACCURACY, new CancellationToken() {
                                @Override
                                public boolean isCancellationRequested() {
                                    return false;
                                }

                                @NonNull
                                @Override
                                public CancellationToken onCanceledRequested(@NonNull OnTokenCanceledListener onTokenCanceledListener) {
                                    return null;
                                }
                            }).addOnSuccessListener(location -> {

                                // Updating Firebase here

                                FirebaseDatabase.getInstance()
                                .getReference(Common.RESTAURANT_REF)
                                .child(Common.currentServerUser.getRestaurant())
                                .child(Common.LOCATION_REF)
                                .setValue(new RestaurantLocationModel(location.getLatitude(),location.getLongitude()))
                                        .addOnSuccessListener(new OnSuccessListener<Void>() {
                                            @Override
                                            public void onSuccess(Void unused) {
                                                Toast.makeText(HomeActivity.this," Update of Location Successful ", Toast.LENGTH_SHORT).show();
                                            }
                                        }).addOnFailureListener(new OnFailureListener() {
                                    @Override
                                    public void onFailure(@NonNull Exception e) {
                                        Toast.makeText(HomeActivity.this," Failed to update location " + e.getMessage(), Toast.LENGTH_SHORT).show();

                                    }
                                });


                            }).addOnFailureListener(e -> Toast.makeText(HomeActivity.this, "" + e.getMessage(), Toast.LENGTH_SHORT).show());

                        }

                        @Override
                        public void onPermissionDenied(PermissionDeniedResponse permissionDeniedResponse) {
                            Toast.makeText(HomeActivity.this,"You must allow this permission ",Toast.LENGTH_SHORT).show();
                        }

                        @Override
                        public void onPermissionRationaleShouldBeShown(PermissionRequest permissionRequest, PermissionToken permissionToken) {

                        }
                    }).check();



        });


        AlertDialog dialog = builder.create();
        dialog.show();

    }

    private void showNewsDialog() {

        androidx.appcompat.app.AlertDialog.Builder builder = new  androidx.appcompat.app.AlertDialog.Builder(this);
        builder.setTitle("News System");
        builder.setMessage("Sending news notifications to all clients ");

        View itemView = LayoutInflater.from(this).inflate(R.layout.layout_news_system,null);

        //Views
        EditText my_title = (EditText)itemView.findViewById(R.id.my_title);
        EditText my_content = (EditText)itemView.findViewById(R.id.my_content);
        EditText my_link = (EditText)itemView.findViewById(R.id.my_link);
        img_upload = (ImageView)itemView.findViewById(R.id.img_upload);
        RadioButton rdi_none = (RadioButton)itemView.findViewById(R.id.rdi_none);
        RadioButton rdi_link = (RadioButton)itemView.findViewById(R.id.rdi_link);
        RadioButton rdi_upload = (RadioButton)itemView.findViewById(R.id.rdi_image);


        //Events
             rdi_none.setOnClickListener(v -> {
            my_link.setVisibility(View.GONE);
            img_upload.setVisibility(View.GONE);
        });
            rdi_link.setOnClickListener(v -> {
            my_link.setVisibility(View.VISIBLE);
            img_upload.setVisibility(View.GONE);

        });
            rdi_upload.setOnClickListener(v -> {
            my_link.setVisibility(View.GONE);
            img_upload.setVisibility(View.VISIBLE);

        });

            img_upload.setOnClickListener(v -> {
                Intent intent = new Intent();
                intent.setType("image/*");
                intent.setAction(Intent.ACTION_GET_CONTENT);
                startActivityForResult(Intent.createChooser(intent,"Select a picture"),PICK_IMAGE_REQUEST);
            });

            builder.setView(itemView);
            builder.setNegativeButton("CANCEL", (dialog, which) -> {
            dialog.dismiss();
            });

            builder.setPositiveButton("SEND", (dialog, which) -> {

                if (rdi_none.isChecked()){

                    sendNews(my_title.getText().toString(),my_content.getText().toString());

                }
                else if (rdi_link.isChecked()){

                    sendNews(my_title.getText().toString(),my_content.getText().toString(),my_link.getText().toString());

                }
                else if(rdi_upload.isChecked()){

                    if (imgUri != null){

                        AlertDialog dialog1 = new AlertDialog.Builder(this).setMessage("Uploading ...").create();
                        dialog1.show();

                        String file_name = UUID.randomUUID().toString();
                        StorageReference newsImages = storageReference.child("news/"+file_name);
                        newsImages.putFile(imgUri)
                                .addOnFailureListener(e -> {
                                    dialog1.dismiss();
                                Toast.makeText(this,""+e.getMessage(),Toast.LENGTH_SHORT).show();

                                }).addOnSuccessListener(taskSnapshot -> {

                                        dialog.dismiss();
                                        newsImages.getDownloadUrl().addOnSuccessListener(uri -> sendNews(my_title.getText().toString(),my_content.getText().toString(),uri.toString()));

                                }).addOnProgressListener(taskSnapshot -> {

                                    double progress = Math.round(100.0 * taskSnapshot.getBytesTransferred()/taskSnapshot.getTotalByteCount());
                                    dialog1.setMessage(new StringBuilder("Uploading ..").append(progress).append("%"));

                                });

                    }

                }


            });

            AlertDialog dialog = builder.create();
            dialog.show();

    }

    private void sendNews(String title, String content, String url) {

        Map<String,String> notificationData = new HashMap<String,String>();
        notificationData.put(Common.NOTI_TITLE,title);
        notificationData.put(Common.NOTI_CONTENT,content);
        notificationData.put(Common.IS_SEND_IMAGE,"true");
        notificationData.put(Common.IMAGE_URL,url);

        FCMSendData fcmSendData = new FCMSendData(Common.getNewsTopic(),notificationData);

        AlertDialog dialog = new AlertDialog.Builder(this).setMessage("Waiting....").create();
        dialog.show();

        compositeDisposable.add(ifcmService.sendNotification(fcmSendData)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(fcmResponse -> {
                    dialog.dismiss();
                    if (fcmResponse.getMessage_id() != 0 )
                        Toast.makeText(this,"News has been sent ",Toast.LENGTH_SHORT).show();
                    else
                        Toast.makeText(this,"News failed to send  ",Toast.LENGTH_SHORT).show();

                },throwable -> {
                    dialog.dismiss();
                    Toast.makeText(this,"" +throwable.getMessage(),Toast.LENGTH_SHORT).show();
                }));



    }

    private void sendNews(String title, String content) {

        Map<String,String> notificationData = new HashMap<String,String>();
        notificationData.put(Common.NOTI_TITLE,title);
        notificationData.put(Common.NOTI_CONTENT,content);
        notificationData.put(Common.IS_SEND_IMAGE,"false");

        FCMSendData fcmSendData = new FCMSendData(Common.getNewsTopic(),notificationData);

        AlertDialog dialog = new AlertDialog.Builder(this).setMessage("Waiting....").create();
        dialog.show();

        compositeDisposable.add(ifcmService.sendNotification(fcmSendData)
        .subscribeOn(Schedulers.io())
        .observeOn(AndroidSchedulers.mainThread())
        .subscribe(fcmResponse -> {
            dialog.dismiss();
            if (fcmResponse.getMessage_id() != 0 )
                Toast.makeText(this,"News has been sent ",Toast.LENGTH_SHORT).show();
            else
                Toast.makeText(this,"News failed to send  ",Toast.LENGTH_SHORT).show();

        },throwable -> {
            dialog.dismiss();
            Toast.makeText(this,"" +throwable.getMessage(),Toast.LENGTH_SHORT).show();
        }));

    }

    private void signOut() {


        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("SignOut")
                .setMessage("Do you really want to sign out ?")
                .setNegativeButton("CANCEL", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                }).setPositiveButton("OK", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {

                Common.selectedFood = null;
                Common.categorySelected = null;
                Common.currentServerUser = null;
                FirebaseAuth.getInstance().signOut();

                Intent intent = new Intent(HomeActivity.this,MainActivity.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK|Intent.FLAG_ACTIVITY_CLEAR_TASK);
                startActivity(intent);
                finish();

            }
        });

        AlertDialog dialog = builder.create();
        dialog.show();



    }

    @Subscribe(sticky = true,threadMode = ThreadMode.MAIN)
    public void onPrintEventListener(PrintOrderEvent event){

            createPDFFile(event.getPath(),event.getOrderModel());

    }

    private void createPDFFile(String path, OrderModel orderModel) {

        dialog.show();

        if (new File(path).exists())
            new File(path).delete();

        try {

            Document document = new Document();
            //Saving
            PdfWriter.getInstance(document,new FileOutputStream(path));
            //Opening
            document.open();

            //Setting
            document.setPageSize(PageSize.A4);
            document.addCreationDate();
            document.addAuthor("Eat It V2");
            document.addCreator(Common.currentServerUser.getName());

            //Font Setting
            BaseColor colorAccent = new BaseColor(0,153,201,255);
            float fontSize = 20.0f;

            //Custom font
            BaseFont fontName = BaseFont.createFont("assets/fonts/brandon_medium.otf","UTF-8",BaseFont.EMBEDDED);

            //Creating title of Document
            Font titleFont = new Font(fontName,36.0f,Font.NORMAL,BaseColor.BLACK);
            PDFUtils.addNewItem(document,"Order Details", Element.ALIGN_CENTER,titleFont);

                //Adding More Staff
            Font orderNumberfont = new Font(fontName,fontSize,Font.NORMAL,colorAccent);
            PDFUtils.addNewItem(document,"Order No :",Element.ALIGN_LEFT,orderNumberfont);
            Font orderNumberValueFont = new Font(fontName,20,Font.NORMAL,BaseColor.BLACK);
            PDFUtils.addNewItem(document,orderModel.getKey(),Element.ALIGN_LEFT,orderNumberValueFont);

            PDFUtils.addLineSeperator(document);

            //Date
            PDFUtils.addNewItem(document,"Order Date",Element.ALIGN_LEFT,orderNumberfont);
            PDFUtils.addNewItem(document,new SimpleDateFormat("dd/MM/yyyy").format(orderModel.getCreateDate()),Element.ALIGN_LEFT,orderNumberValueFont);

            PDFUtils.addLineSeperator(document);

            //Account Name
            PDFUtils.addNewItem(document,"Account Name ",Element.ALIGN_LEFT,orderNumberfont);
            PDFUtils.addNewItem(document,orderModel.getUserName(),Element.ALIGN_LEFT,orderNumberValueFont);

            PDFUtils.addLineSeperator(document);

            // Add product and detail
            PDFUtils.addLineSpace(document);
            PDFUtils.addNewItem(document,"Product Detail ",Element.ALIGN_CENTER,titleFont);
            PDFUtils.addLineSeperator(document);


            // Here is the code to fetch Image from firebase but we can comment that out  if we consdier that is not necessary to put image
            Observable.fromIterable(orderModel.getCartItemList())
                    .flatMap(cartItem -> Common.getBitMapFromUrl(HomeActivity.this,cartItem,document))
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe(cartItem -> { // On Next
                                        //Here on each item we will put details and
                        //here it is Food name
                            PDFUtils.addNewItemWithLeftAndRight(document,cartItem.getFoodName(),
                                   ("(0.0%)"),
                                    titleFont,
                                   orderNumberValueFont);
                        //here it is Food size and add on
                        PDFUtils.addNewItemWithLeftAndRight(document,
                                "Size",
                                Common.formatSizeJsonToString(cartItem.getFoodSize()),
                                titleFont,
                                orderNumberValueFont);
                        PDFUtils.addNewItemWithLeftAndRight(document,
                                "AddOn",
                                Common.formatAddOnJsonToString(cartItem.getFoodAddon()),
                                titleFont,
                                orderNumberValueFont);

                    //Food Price
                         // its format is like 1*30=30
                        PDFUtils.addNewItemWithLeftAndRight(document,
                                new StringBuilder()
                                .append(cartItem.getFoodQuantity())
                                .append("*")
                                .append(cartItem.getFoodPrice()+ cartItem.getFoodExtraPrice())
                                .toString(),
                                new StringBuilder()
                                .append(cartItem.getFoodQuantity()*(cartItem.getFoodExtraPrice()+cartItem.getFoodPrice()))
                                .toString(),
                                titleFont,
                                orderNumberValueFont);

                    PDFUtils.addLineSpace(document);


                    },throwable -> {  //On Error
                                dialog.dismiss();
                                Toast.makeText(this,throwable.getMessage(),Toast.LENGTH_SHORT).show();


                    },()->{// On Complete

                            //WHen its complete we append total
                            PDFUtils.addLineSpace(document);
                            PDFUtils.addLineSpace(document);
                            PDFUtils.addLineSpace(document);

                        PDFUtils.addNewItemWithLeftAndRight(document,"Total",
                                new StringBuilder()
                                .append(orderModel.getTotalPayment()).toString(),
                                titleFont,
                                titleFont
                                    );

                        //CLosing

                        document.close();
                        dialog.dismiss();
                        Toast.makeText(this,"Success",Toast.LENGTH_SHORT).show();

                        printPDF();

                    });


        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (DocumentException e) {
            e.printStackTrace();
        }


    }

    private void printPDF() {

        PrintManager printManager = (PrintManager)getSystemService(Context.PRINT_SERVICE);
        try {
            PrintDocumentAdapter printDocumentAdapter = new PdfDocumentAdapter(this,new StringBuilder(Common.getAppPath(this))
            .append(Common.FILE_PRINT).toString());
            printManager.print("Document",printDocumentAdapter,new PrintAttributes.Builder().build());

        }catch (Exception exception)
        {

            exception.printStackTrace();

        }


    }


}
