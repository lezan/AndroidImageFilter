package lezan.androidimagefilter;

import android.content.Context;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.Switch;
import android.widget.Toast;
/*
import org.opencv.android.OpenCVLoader;
import org.opencv.android.Utils;
import org.opencv.core.Mat;
import org.opencv.imgproc.Imgproc;*/

import java.io.FileNotFoundException;
import java.io.InputStream;

public class FilterActivity extends AppCompatActivity {

    static {
        System.loadLibrary("native-lib");
        System.loadLibrary("opencv_java3");
    }

    private boolean sourceAlreadyRead = false;

    private int filterType = 1;

    /*

    0 : no XDOG;
    1 : sì XDOG;

    */

    private ImageView imageView1;

    private Switch switchXDOG;

    private LinearLayout linearLayoutXDOG;
    private LinearLayout linearLayoutMean;

    private EditText editTextKappa;
    private EditText editTextSigma;
    private EditText editTextTau;
    private EditText editTextPhi;

    private Spinner snipperAdaptiveMethod;
    private EditText editTextBlockSize;
    private EditText editTextConstant;

    private Button buttonFilter;

    private Uri uriImageCropped;
    private InputStream stream = null;
    //private Mat source = new Mat();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_filter);

        imageView1 = (ImageView) findViewById(R.id.imageView1);

        switchXDOG = (Switch) findViewById(R.id.switchXDOG);

        linearLayoutXDOG = (LinearLayout) findViewById(R.id.linearLayoutXDOG);
        linearLayoutMean = (LinearLayout) findViewById(R.id.linearLayoutMean);

        editTextKappa = (EditText) findViewById(R.id.editTextKappa);
        editTextSigma = (EditText) findViewById(R.id.editTextSigma);
        editTextTau = (EditText) findViewById(R.id.editTextTau);
        editTextPhi = (EditText) findViewById(R.id.editTextPhi);

        snipperAdaptiveMethod = (Spinner) findViewById(R.id.snipperAdaptiveMethod);
        editTextBlockSize = (EditText) findViewById(R.id.editTextBlockSize);
        editTextConstant = (EditText) findViewById(R.id.editTextConstant);

        buttonFilter = (Button) findViewById(R.id.buttonFilter);

        switchXDOG.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {

            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if(isChecked){
                    filterType = 1;
                    linearLayoutXDOG.setVisibility(View.VISIBLE);
                    linearLayoutMean.setVisibility(View.GONE);
                    switchXDOG.setText(R.string.switchxdog);
                } else {
                    filterType = 0;
                    linearLayoutMean.setVisibility(View.VISIBLE);
                    linearLayoutXDOG.setVisibility(View.GONE);
                    switchXDOG.setText(R.string.switchmean);
                }
            }
        });

        // Create an ArrayAdapter using the string array and a default spinner layout
        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(this, R.array.methods, android.R.layout.simple_spinner_item);
        // Specify the layout to use when the list of choices appears
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        // Apply the adapter to the spinner
        snipperAdaptiveMethod.setAdapter(adapter);

        setImageView();

        handleFilter();
    }

    public void doneFilter(View view) {
        handleFilter();
    }

    private void handleFilter() {

        try {
            stream = getContentResolver().openInputStream(uriImageCropped);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }

        if(!sourceAlreadyRead) {
            handleBitmap(stream);
            sourceAlreadyRead = true;
        }

        switch(filterType) {
            case 0:
                adaptiveThresholdMethod();
                break;
            case 1:
                //xDOGMethod();
                break;
        }
    }

    private void setImageView() {

        Bundle extras = getIntent().getExtras();
        if (extras != null && extras.containsKey("uriImageCropped")) {
            uriImageCropped = Uri.parse(extras.getString("uriImageCropped"));
        }

        /*try {
            imageView1.setImageURI(uriImageCropped);
        } catch(Exception e) {
            e.printStackTrace();
        }*/

    }

    private void handleBitmap(InputStream stream) {

        /* Creo bitmap a partire dall'URI */
        BitmapFactory.Options bmpImageInputFactoryOptions = new BitmapFactory.Options();
        bmpImageInputFactoryOptions.inPreferredConfig = Bitmap.Config.ARGB_8888;
        Bitmap originalBitmapImage = BitmapFactory.decodeStream(stream, null, bmpImageInputFactoryOptions);

        /* Converto l'immagine bitmap in una matrice */
        //Utils.bitmapToMat(originalBitmapImage, source);
    }

    private void adaptiveThresholdMethod() {
        /* Creo una nuova matrice a partire dalla matrice sorgente */
        //Mat gray = new Mat();
        /* Converto la matrice sorgente in scala di grigi */
        //Imgproc.cvtColor(source, gray, Imgproc.COLOR_BGR2GRAY);

        /* Applico una sfocatura all'immagine in scale di grigi */
        //Imgproc.medianBlur(gray, gray, 5);
        //Imgproc.GaussianBlur(gray, gray, new Size(5,5), 0);

        /* Creo la matrice che conterrà il risultato finale */
        //Mat result = new Mat();
        /* Applico il threshold alla matrice in scala di grigi ottenendo un'immagine binaria */
        //Imgproc.adaptiveThreshold(gray, result, 255, checkAdaptiveMethod(), Imgproc.THRESH_BINARY, getCheckBlockSize(), getCheckConstant());

        /* Creo l'immagine bitmap a partire dalla matrice dell'immagine binaria */
        //Bitmap filterImage = Bitmap.createBitmap(result.cols(), result.rows(), Bitmap.Config.ARGB_8888);
        /* Converto la matrice a bitmap */
        //Utils.matToBitmap(result, filterImage);
        /* Setto il contenitore dell'immagine finale con il risultato ottenuto */
        //imageView1.setImageBitmap(filterImage);
    }
/*
    private int checkAdaptiveMethod() {
        String method = snipperAdaptiveMethod.getSelectedItem().toString();
        if(method.equals("Mean")) {
            return Imgproc.ADAPTIVE_THRESH_MEAN_C;
        }
        return Imgproc.ADAPTIVE_THRESH_GAUSSIAN_C;
    }
*/
    private int getCheckBlockSize() {
        int blockSize = Integer.parseInt(editTextBlockSize.getText().toString());
        if((blockSize % 2) == 1 && blockSize > 0) {
            return blockSize;
        }

        Context context = getApplicationContext();
        CharSequence text = "Invalid number for block size. Going to use default value";
        int duration = Toast.LENGTH_SHORT;

        Toast toast = Toast.makeText(context, text, duration);
        toast.show();
        return 51;
    }

    private double getCheckConstant() {
        return Double.parseDouble(editTextConstant.getText().toString());
    }
/*
    private void xDOGMethod() {

        double kappa = Double.parseDouble(editTextKappa.getText().toString());
        double sigma = Double.parseDouble(editTextSigma.getText().toString());
        double tau = Double.parseDouble(editTextTau.getText().toString());
        double phi = Double.parseDouble(editTextPhi.getText().toString());

        //String imageSourcePath = getPath(uriImageCropped);

        if (!OpenCVLoader.initDebug()) {
            Log.d("OpenCV", "\n OpenCVLoader.initDebug(), not working.");
        } else {
            Log.d("OpenCV", "\n OpenCVLoader.initDebug(), WORKING.");

            String xdogOutputImagePath = xdog(imageSourcePath, Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES) + "/Artuino_photo", kappa, sigma, tau, phi);

            if(!xdogOutputImagePath.equals("False")) {
                imageView1.setImageDrawable(null);
                imageView1.setImageURI(Uri.parse(xdogOutputImagePath));
            }
        }
    }
*/
    private String getPath(Uri uri) {
        if(uri == null) {
            return null;
        }
        String projection[] = {MediaStore.Images.Media.DATA};
        try (Cursor cursor = getContentResolver().query(uri, projection, null, null, null)) {
            if (cursor != null) {
                int columnIndex = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATA);
                cursor.moveToFirst();
                return cursor.getString(columnIndex);
            }
        }
        return uri.getPath();
    }

    public void goBack(View view) {

        finish();
    }
    public void sendArduino() {

    }

    /**
     * A native method that is implemented by the 'native-lib' native library,
     * which is packaged with this application.
     */

    //public native String xdog(String imageSourcePath, String path, double kappa, double sigma, double tau, double phi);
}
