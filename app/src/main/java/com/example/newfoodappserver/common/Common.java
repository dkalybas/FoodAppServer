package com.example.newfoodappserver.common;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.Typeface;
import android.net.Uri;
import android.os.Build;
import android.provider.OpenableColumns;
import android.telephony.gsm.GsmCellLocation;
import android.text.SpannableString;
import android.text.SpannableStringBuilder;
import android.text.Spanned;
import android.text.style.ForegroundColorSpan;
import android.text.style.StyleSpan;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.example.newfoodappserver.HomeActivity;
import com.example.newfoodappserver.R;
import com.example.newfoodappserver.SizeAddonEditActivity;
import com.example.newfoodappserver.model.AddonModel;
import com.example.newfoodappserver.model.BestDealsModel;
import com.example.newfoodappserver.model.CartItem;
import com.example.newfoodappserver.model.CategoryModel;
import com.example.newfoodappserver.model.DiscountModel;
import com.example.newfoodappserver.model.FoodModel;
import com.example.newfoodappserver.model.MostPopularModel;
import com.example.newfoodappserver.model.ServerUserModel;
import com.example.newfoodappserver.model.SizeModel;
import com.example.newfoodappserver.model.TokenModel;
import com.google.firebase.database.FirebaseDatabase;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.itextpdf.text.BadElementException;
import com.itextpdf.text.Document;
import com.itextpdf.text.DocumentException;
import com.itextpdf.text.Image;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.lang.reflect.Type;
import java.net.MalformedURLException;
import java.util.List;
import java.util.concurrent.ExecutionException;

import androidx.core.app.NotificationCompat;
import androidx.fragment.app.FragmentActivity;
import io.reactivex.Observable;
import io.reactivex.ObservableSource;
import com.google.gson.Gson;
import java.lang.reflect.Type;


public class Common {
    public static final String SERVER_REF = "Server";
    public static final String CATEGORY_REF ="Category" ;
    public static final int DEFAULT_COLUMN_COUNT = 0 ;
    public static final int FULL_WIDTH_COLUMN = 1;
    public static final String ORDER_REF = "Orders";
    public static final String NOTI_TITLE ="title" ;
    public static final String NOTI_CONTENT ="content" ;
    public static final String TOKEN_REF = "Tokens" ;
    public static final String SHIPPER ="Shippers" ;
    public static final String SHIPPING_ORDER_REF = "ShippingOrder" ;
    public static final String IS_OPEN_ACTIVITY_NEW_ORDER = "IsOpenActivityNewOrder" ;
    public static final String BEST_DEALS = "BestDeals";  // like Firebase
    public static final String MOST_POPULAR = "MostPopular";
    public static final String IS_SEND_IMAGE = "IS_SEND_IMAGE" ;
    public static final String IMAGE_URL = "IMAGE_URL" ;
    public static final String RESTAURANT_REF = "Restaurant" ;
    public static final String CHAT_REF = "Chat" ;
    public static final String KEY_ROOM_ID = "CHAT_ROOM_ID";
    public static final String KEY_CHAT_USER = "CHAT_SENDER";
    public static final String CHAT_DETAIL_REF = "ChatDetail";
    public static final String DISCOUNT = "Discount";
    public static final String FILE_PRINT = "last_order_print.pdf";
    public static final String LOCATION_REF = "Location";


    public static ServerUserModel currentServerUser;

    public static CategoryModel categorySelected;
    public static FoodModel selectedFood;
    public static BestDealsModel bestDealsSelected;
    public static MostPopularModel mostPopularSelected;
    public static DiscountModel discountSelected;

    public static String getAppPath(Context context) {
        File dir = new File(context.getExternalFilesDir(null).getPath()
                    +File.separator
                    +context.getResources().getString(R.string.app_name)
                    +File.separator );

        if (!dir.exists())
            dir.mkdir();

        return  dir.getPath()+File.separator ;


    }

    public static Observable<CartItem> getBitMapFromUrl(Context context, CartItem cartItem, Document document) {

        return Observable.fromCallable(()-> {


            Bitmap bitmap = Glide.with(context)
                    .asBitmap()
                    .load(cartItem.getFoodImage())
                    .submit().get();

            Image image = Image.getInstance(bitmapToByteArray(bitmap));
            image.scaleAbsolute(80, 80);
            document.add(image);


            return  cartItem;


        });



    }

    private static byte[] bitmapToByteArray(Bitmap bitmap) {

        ByteArrayOutputStream stream = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.PNG,100,stream);
        return stream.toByteArray();


    }

    public static String formatSizeJsonToString(String foodSize) {

            if (foodSize.equals("Default"))
                return foodSize;
            else {

                Gson gson =  new Gson();
                SizeModel sizeModel = gson.fromJson(foodSize,SizeModel.class);
                return sizeModel.getName();
            }


    }

    public static String formatAddOnJsonToString(String foodAddon) {

        if (foodAddon.equals("Default"))
            return foodAddon;

        else {

            StringBuilder stringBuilder = new StringBuilder();



            Gson gson =  new Gson();
            Type collection_type = new TypeToken<List<AddonModel>>(){}.getType();
            List<AddonModel> addonModels = gson.fromJson(foodAddon,collection_type);
            for (AddonModel addonModel :addonModels)
                stringBuilder.append(addonModel.getName()).append(",");

            return stringBuilder.substring(0,stringBuilder.length()-1);   //removing last ","

        }
    }

    public enum ACTION{
        CREATE,
        UPDATE,
        DELETE


    }




    public static void setSpanString(String welcome, String name, TextView textView) {

        SpannableStringBuilder builder = new SpannableStringBuilder();
        builder.append(welcome);
        SpannableString spannableString = new SpannableString(name);
        StyleSpan boldSpan = new StyleSpan(Typeface.BOLD);
        spannableString.setSpan(boldSpan,0,name.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
        builder.append(spannableString);
        textView.setText(builder,TextView.BufferType.SPANNABLE);


    }


    public static void setSpanStringColor(String welcome, String name, TextView textView, int color ) {

        SpannableStringBuilder builder = new SpannableStringBuilder();
        builder.append(welcome);
        SpannableString spannableString = new SpannableString(name);
        StyleSpan boldSpan = new StyleSpan(Typeface.BOLD);
        spannableString.setSpan(boldSpan,0,name.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
        spannableString.setSpan(new ForegroundColorSpan(color),0,name.length(),Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);

        builder.append(spannableString);
        textView.setText(builder,TextView.BufferType.SPANNABLE);




    }

    public static String convertStatusToString(int orderStatus) {

        switch (orderStatus)
        {

            case 0:
                return "Placed ";
            case 1:
                return "Shipping ";
            case 2:
                return "Shipped ";
            case 3:
                return "Canceled ";
                 default:
                     return "Error";

        }




    }

    public static void showNotification(Context context, int id, String title, String content, Intent intent) {

        PendingIntent pendingIntent = null;
        if (intent!=null)
            pendingIntent = PendingIntent.getActivity(context,id,intent,PendingIntent.FLAG_UPDATE_CURRENT);
        String NOTIFICATION_CHANNEL_ID = "my_newFoodApp_v2";
        NotificationManager notificationManager = (NotificationManager)context.getSystemService(Context.NOTIFICATION_SERVICE);
        if (Build.VERSION.SDK_INT>=Build.VERSION_CODES.O){

            NotificationChannel notificationChannel = new NotificationChannel(NOTIFICATION_CHANNEL_ID,
                    "New Food App Vol 2",NotificationManager.IMPORTANCE_DEFAULT);
            notificationChannel.setDescription("New Food App Vol 2");
            notificationChannel.enableLights(true);
            notificationChannel.setLightColor(Color.RED);
            notificationChannel.setVibrationPattern(new long[]{0,1000,500,1000});
            notificationChannel.enableVibration(true);


            notificationManager.createNotificationChannel(notificationChannel);

        }

        NotificationCompat.Builder builder = new NotificationCompat.Builder(context,NOTIFICATION_CHANNEL_ID);
        builder.setContentTitle(title)
                .setContentText(content)
                .setAutoCancel(true)
                .setSmallIcon(R.mipmap.ic_launcher_round)
                .setLargeIcon(BitmapFactory.decodeResource(context.getResources(), R.drawable.ic_restaurant_menu_black_24dp));

        if (pendingIntent!=null)
            builder.setContentIntent(pendingIntent);
        Notification notification = builder.build();
        notificationManager.notify(id,notification);

    }


    public static void updateToken(Context context, String newToken,boolean isServer, boolean isShipper) {

        FirebaseDatabase.getInstance()
                .getReference(Common.TOKEN_REF)
                .child(Common.currentServerUser.getUid())
                .setValue(new TokenModel(Common.currentServerUser.getPhone(),newToken,isServer,isShipper))
                .addOnFailureListener(e -> {
                    Toast.makeText(context,""+e.getMessage(),Toast.LENGTH_SHORT).show();


                });

    }

    public static String createTopicOrder() {

        return new StringBuilder("/topics/new_order").toString();

    }


    public static String getNewsTopic() {

        return new StringBuilder("/topics/")
                .append(Common.currentServerUser.getRestaurant())
                .append("_")
                .append("news")
                .toString();
    }

    public static String getFileName(ContentResolver contentResolver, Uri fileUri) {

        String result = null;

        if (fileUri.getScheme().equals("content")){

            Cursor cursor = contentResolver.query(fileUri,null,null,null,null);
            try {
                if (cursor!=null && cursor.moveToFirst())
                    result  = cursor.getString(cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME));

            }finally {
                cursor.close();
            }


        }

        if (result == null){

            result = fileUri.getPath();
            int cut = result.lastIndexOf('/');
            if (cut !=-1)
                result = result.substring(cut+1);

        }

        return result;

    }
}
