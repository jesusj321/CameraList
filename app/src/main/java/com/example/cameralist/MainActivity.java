package com.example.cameralist;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.net.Uri;
import android.provider.MediaStore;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.TextView;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    private static final int REQUEST_PERMISSIONS = 1001;
    private File currentImageFile;
    private List<Uri> picturesPaths = new ArrayList<>();
    private PicturesAdapter picturesAdapter;
    private TextView textViewNumberOfPictures;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        textViewNumberOfPictures = (TextView) findViewById(R.id.textView_pictures);
        textViewNumberOfPictures.setText(String.format("Imagenes: %d", picturesPaths.size()));

        RecyclerView recyclerView = (RecyclerView) findViewById(R.id.recyclerView_pictures);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        picturesAdapter = new PicturesAdapter(this, picturesPaths);
        recyclerView.setAdapter(picturesAdapter);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == R.id.take_picture) {
            checkPermissions();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == MediaUtility.REQUEST_CAPTURE_IMAGE && resultCode == RESULT_OK) {
            final String currentPhotoPath = currentImageFile.getAbsolutePath();
            MediaUtility.decodeBitmapAndCompress(currentPhotoPath, new MediaUtility.DecodeBitmapAndCompressAsyncTask(30, 450, 450) {
                @Override
                public void onBitmapDecoded(Bitmap result) {

                    Intent mediaScanIntent = new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE);
                    Uri contentUri = Uri.fromFile(currentImageFile);
                    mediaScanIntent.setData(contentUri);
                    MainActivity.this.sendBroadcast(mediaScanIntent);

                    Log.i("IMAGE", "Peso de la imagen comprimida: " + ((currentImageFile.length() / 1024.0)) / 1024.0 + " MEGAS");
                    picturesPaths.add(contentUri);
                    textViewNumberOfPictures.setText(String.format("Imagenes: %d", picturesPaths.size()));
                    picturesAdapter.notifyDataSetChanged();
                }

                @Override
                public void onBitmapError(String errorMessage) {
                    DialogUtility.createSimpleDialog(MainActivity.this, "Error al comprimir el bitmap", errorMessage).show();
                }
            });
        }
    }

    private void checkPermissions() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED &&
                ActivityCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            if (!ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.CAMERA) &&
                    ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.WRITE_EXTERNAL_STORAGE)) {
                ActivityCompat.requestPermissions(this,
                        new String[]{Manifest.permission.CAMERA, Manifest.permission.WRITE_EXTERNAL_STORAGE},
                        REQUEST_PERMISSIONS);
            }
        } else {
            startCameraIntent(MediaUtility.REQUEST_CAPTURE_IMAGE);
        }
    }

    private void startCameraIntent(int requestType) {
        Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        if (takePictureIntent.resolveActivity(getPackageManager()) != null) {
            if (MediaUtility.isExternalStorageAvailable()) {
                try {
                    currentImageFile = MediaUtility.createTempImageFile();
                    takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, Uri.fromFile(currentImageFile));
                    takePictureIntent.putExtra("return-data", true);
                    startActivityForResult(Intent.createChooser(takePictureIntent, "Camara"), requestType);
                } catch (IOException e) {
                    DialogUtility.createSimpleDialog(this, "Error al crear el archivo de la foto", e.getMessage()).show();
                }
            } else {
                DialogUtility.createSimpleDialog(this, "Error en la memoria", "La memoria no está montada o es de solo lectura").show();
            }
        } else {
            DialogUtility.createSimpleDialog(this, "Camara no encontrada", "Este dispositivo no tiene camara o no tiene una app para el manejo de la camara").show();
        }
    }
}
