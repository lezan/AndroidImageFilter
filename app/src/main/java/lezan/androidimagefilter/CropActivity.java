package lezan.androidimagefilter;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.ParcelFileDescriptor;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.Toast;

import com.soundcloud.android.crop.Crop;

import java.io.File;
import java.io.FileDescriptor;
import java.io.IOException;

public class CropActivity extends AppCompatActivity {

    private ImageView imageView1;
    private Uri uriImage;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_crop);

        imageView1 = (ImageView) findViewById(R.id.imageView1);

        Bundle extras = getIntent().getExtras();
        if (extras != null && extras.containsKey("uriImage")) {
            uriImage = Uri.parse(extras.getString("uriImage"));
        }

        try {
            imageView1.setImageBitmap(getBitmapFromUri(uriImage));
        } catch(IOException e) {
            e.printStackTrace();
        }
    }

    private Bitmap getBitmapFromUri(Uri uri) throws IOException {
        ParcelFileDescriptor parcelFileDescriptor = getContentResolver().openFileDescriptor(uri, "r");
        FileDescriptor fileDescriptor = null;
        if (parcelFileDescriptor != null) {
            fileDescriptor = parcelFileDescriptor.getFileDescriptor();
        }
        Bitmap image = BitmapFactory.decodeFileDescriptor(fileDescriptor);
        if (parcelFileDescriptor != null) {
            parcelFileDescriptor.close();
        }
        return image;
    }

    public void goBack(View view) {

        finish();
    }

    //metodo per applicare il crop
    public void goForward(View view) {
        Uri destination = Uri.fromFile(new File(getCacheDir(), "cropped"));
        int height = 600;
        int width = 400;
        /*int[] imageSize = getImageSize(destination);
        if(imageSize[0] < height) {
            height = imageSize[0];
        }
        if(imageSize[1] < width) {
            width = imageSize[1];
        }*/
        Crop.of(uriImage, destination).withFixedSize(width, height).start(this);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent result) {
        if (requestCode == Crop.REQUEST_CROP) {
            handleCrop(resultCode, result);
        }
    }

    private void handleCrop(int resultCode, Intent result) {
        if (resultCode == RESULT_OK) {
            Intent intentFilter = new Intent(CropActivity.this, FilterActivity.class);
            Uri prov = Crop.getOutput(result);
            Log.d("uri image cropped", prov.toString());
            intentFilter.putExtra("uriImageCropped", Crop.getOutput(result).toString());
            startActivity(intentFilter);
        } else if (resultCode == Crop.RESULT_ERROR) {
            Toast.makeText(this, Crop.getError(result).getMessage(), Toast.LENGTH_SHORT).show();
        }
    }

    /*private int[] getImageSize(Uri uri){
        InputStream stream = null;
        try {
            stream = getContentResolver().openInputStream(uri);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
        BitmapFactory.Options options = new BitmapFactory.Options();
        options.inJustDecodeBounds = true;
        BitmapFactory.decodeStream(stream, null, options);

        int imageHeight = options.outHeight;
        int imageWidth = options.outWidth;
        int[] imageSize = new int[2];
        imageSize[0] = imageHeight;
        imageSize[1] = imageWidth;
        return imageSize;
    }*/
}