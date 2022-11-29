package com.example.glphotoeditorpro.activities;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.FragmentTransaction;
import androidx.recyclerview.widget.RecyclerView;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.MediaStore;
import android.provider.SyncStateContract;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.Toast;


import com.example.glphotoeditorpro.Interface.AddTextFragmentListener;
import com.example.glphotoeditorpro.Interface.BrushFragmentListener;
import com.example.glphotoeditorpro.Interface.EditImageFragmentListener;
import com.example.glphotoeditorpro.Interface.FiltersListFragmentListener;
import com.example.glphotoeditorpro.Interface.PreviewFragmentListener;
import com.example.glphotoeditorpro.R;
import com.example.glphotoeditorpro.adapters.RecyclerViewAdapter;
import com.example.glphotoeditorpro.fragments.AddTextFragment;
import com.example.glphotoeditorpro.fragments.BrushFragment;
import com.example.glphotoeditorpro.fragments.EditImageFragment;
import com.example.glphotoeditorpro.fragments.FiltersListFragment;
import com.example.glphotoeditorpro.fragments.PreviewFragment;
import com.example.glphotoeditorpro.utils.BitmapUtils;
import com.google.android.material.snackbar.Snackbar;
import com.karumi.dexter.Dexter;
import com.karumi.dexter.MultiplePermissionsReport;
import com.karumi.dexter.PermissionToken;
import com.karumi.dexter.listener.PermissionRequest;
import com.karumi.dexter.listener.multi.MultiplePermissionsListener;
import com.yalantis.ucrop.UCrop;
import com.zomato.photofilters.imageprocessors.Filter;
import com.zomato.photofilters.imageprocessors.subfilters.BrightnessSubFilter;
import com.zomato.photofilters.imageprocessors.subfilters.ContrastSubFilter;
import com.zomato.photofilters.imageprocessors.subfilters.SaturationSubfilter;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import ja.burhanrashid52.photoeditor.OnSaveBitmap;
import ja.burhanrashid52.photoeditor.PhotoEditor;
import ja.burhanrashid52.photoeditor.PhotoEditorView;

public class MainActivity extends AppCompatActivity implements FiltersListFragmentListener,
        EditImageFragmentListener, RecyclerViewAdapter.OnCardClickListener, BrushFragmentListener,
        AddTextFragmentListener, PreviewFragmentListener {

    private static final String TAG = "myLog";
    public static final String pictureName = "flash.jpg";
    public static final int PERMISSION_PICK_IMAGE = 1000;

    PhotoEditorView photoEditorView;
    PhotoEditor photoEditor;
    FrameLayout coordinatorLayout;

    Bitmap originalBitmap, filteredBitmap, finalBitmap;
    RecyclerView recyclerView;
    RecyclerViewAdapter adapter;

    FiltersListFragment filtersListFragment;
    EditImageFragment editImageFragment;
    BrushFragment brushFragment;
    AddTextFragment addTextFragment;
    PreviewFragment previewFragment;

    boolean mIsEraser = false;

    Uri image_selected_uri;
    int brightnessFinal = 0;
    float saturationFinal = 1.0f;
    float contrastFinal = 1.0f;

    static {
        System.loadLibrary("NativeImageProcessor");
    }

    private ArrayList<String> mNames = new ArrayList<>();
    private ArrayList<Drawable> mImages = new ArrayList<>();

    @Override
    protected void onResume() {
        super.onResume();
        Log.d(TAG, "onResume: true");
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.editor_layout);

        previewFragment = PreviewFragment.getInstance();
        previewFragment.setListener(this);

        FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
        ft
                .setCustomAnimations(R.anim.slide_in_top, R.anim.slide_in_right)
                .replace(R.id.coordinator, previewFragment, PreviewFragment.TAG)
                .commit();

        photoEditorView = findViewById(R.id.image_preview);
        photoEditor = new PhotoEditor.Builder(this, photoEditorView)
                .setPinchTextScalable(true)
                .build();
        coordinatorLayout = findViewById(R.id.coordinator);

        loadImage();

        fillIn();
        initRecyclerView();
    }

    private void loadImage() {
        originalBitmap = BitmapUtils.getBitmapFromAssets(this, pictureName, 300, 300);
        filteredBitmap = originalBitmap.copy(Bitmap.Config.ARGB_8888, true);
        finalBitmap = originalBitmap.copy(Bitmap.Config.ARGB_8888, true);
        photoEditorView.getSource().setImageBitmap(originalBitmap);
    }

    private void fillIn() {
        mNames.add("Галерея");
        mImages.add(getDrawable(R.drawable.ic_gallery));

        mNames.add("Сохранить");
        mImages.add(getDrawable(R.drawable.ic_baseline_save_24));

        mNames.add("Параметры");
        mImages.add(getDrawable(R.drawable.parametrs));

        mNames.add("Фильтры");
        mImages.add(getDrawable(R.drawable.filters));

        mNames.add("Текст");
        mImages.add(getDrawable(R.drawable.text));

        mNames.add("Кисть");
        mImages.add(getDrawable(R.drawable.ic_brush));

        mNames.add("О программе");
        mImages.add(getDrawable(R.drawable.info));
    }

    private void initRecyclerView() {
        Log.d(TAG, "initialize recycler view");
        recyclerView = findViewById(R.id.recyclerView);
        adapter = new RecyclerViewAdapter(mNames, mImages, this);
        adapter.setOnCardClickListener(this);
        recyclerView.setAdapter(adapter);
    }

    @Override
    public void onCardClick(View v, String action) {
        Log.d(TAG, "main activity onCardClick");
        if (action.equals("Сохранить")) {
            saveImageToGallery();
        } else if (action.equals("Галерея")) {
            openImageFromGallery();
        } else if (action.equals("Параметры")) {
            filtersListFragment = null;
            brushFragment = null;
            addTextFragment = null;

            FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
            if (editImageFragment != null) {
                ft
                        .setCustomAnimations(R.anim.slide_in_top, R.anim.slide_in_bottom)
                        .remove(editImageFragment)
                        .commit();
                editImageFragment = null;
            } else {
                editImageFragment = new EditImageFragment();
                editImageFragment.setListener(MainActivity.this);
                ft
                        .setCustomAnimations(R.anim.slide_in_top, R.anim.slide_in_bottom)
                        .replace(R.id.coordinator, editImageFragment, EditImageFragment.TAG)
                        .commit();
            }
        } else if (action.equals("Фильтры")) {
            editImageFragment = null;
            brushFragment = null;
            addTextFragment = null;

            FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
            if (filtersListFragment != null) {
                ft
                        .setCustomAnimations(R.anim.slide_in_top, R.anim.slide_in_bottom)
                        .remove(filtersListFragment)
                        .commit();
                filtersListFragment = null;
            } else {
                filtersListFragment = FiltersListFragment.getInstance();
                filtersListFragment.setListener(MainActivity.this);
                ft
                        .setCustomAnimations(R.anim.slide_in_top, R.anim.slide_in_bottom)
                        .replace(R.id.coordinator, filtersListFragment, FiltersListFragment.TAG)
                        .commit();
            }
        } else if (action.equals("О программе")) {
            startActivity(new Intent(this, AboutActivity.class));
        } else if (action.equals("Кисть")) {
            editImageFragment = null;
            filtersListFragment = null;
            addTextFragment = null;

            FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
            if (brushFragment != null) {
                ft
                        .setCustomAnimations(R.anim.slide_in_top, R.anim.slide_in_bottom)
                        .remove(brushFragment)
                        .commit();
                brushFragment = null;
            } else {
                photoEditor.setBrushDrawingMode(true);
                brushFragment = BrushFragment.getInstance();
                brushFragment.setListener(this);
                ft
                        .setCustomAnimations(R.anim.slide_in_top, R.anim.slide_in_bottom)
                        .replace(R.id.coordinator, brushFragment, BrushFragment.TAG)
                        .commit();
            }
        } else if (action.equals("Текст")){
            editImageFragment = null;
            filtersListFragment = null;
            brushFragment = null;

            FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
            if (addTextFragment != null){
                InputMethodManager imm = (InputMethodManager) this.getSystemService(Context.INPUT_METHOD_SERVICE);
                imm.hideSoftInputFromWindow(v.getWindowToken(), 0);
                ft
                        .setCustomAnimations(R.anim.slide_in_top, R.anim.slide_in_bottom)
                        .remove(addTextFragment)
                        .commit();
                addTextFragment = null;
            } else {
                addTextFragment = AddTextFragment.getInstance();
                addTextFragment.setListener(this);
                ft
                        .setCustomAnimations(R.anim.slide_in_top, R.anim.slide_in_bottom)
                        .replace(R.id.coordinator, addTextFragment, AddTextFragment.TAG)
                        .commit();
            }
        } else if (action.equals("Кадрировать")){
//            startCrop(image_selected_uri);
            //НЕ РАБОТАЕТ
        }
    }

    private void startCrop(Uri uri) {
        String destinationFileName = new StringBuilder(UUID.randomUUID().toString()).append(".jpg").toString();
        UCrop ucrop = UCrop.of(uri, Uri.fromFile(new File(getCacheDir(), destinationFileName)));
        ucrop.start(MainActivity.this);
    }

    @Override
    public void onBrightnessChanged(int brightness) {
        brightnessFinal = brightness;
        Filter myFilter = new Filter();
        myFilter.addSubFilter(new BrightnessSubFilter(brightness));
        photoEditorView.getSource().setImageBitmap(myFilter.processFilter(finalBitmap.copy(Bitmap.Config.ARGB_8888, true)));
    }

    @Override
    public void onSaturationChanged(float saturation) {
        saturationFinal = saturation;
        Filter myFilter = new Filter();
        myFilter.addSubFilter(new SaturationSubfilter(saturation));
        photoEditorView.getSource().setImageBitmap(myFilter.processFilter(finalBitmap.copy(Bitmap.Config.ARGB_8888, true)));
    }

    @Override
    public void onContrastChanged(float contrast) {
        contrastFinal = contrast;
        Filter myFilter = new Filter();
        myFilter.addSubFilter(new ContrastSubFilter(contrast));
        photoEditorView.getSource().setImageBitmap(myFilter.processFilter(finalBitmap.copy(Bitmap.Config.ARGB_8888, true)));
    }

    @Override
    public void onEditStarted() {

    }

    @Override
    public void onEditCompleted() {
        Bitmap bitmap = filteredBitmap.copy(Bitmap.Config.ARGB_8888, true);
        Filter myFilter = new Filter();
        myFilter.addSubFilter(new BrightnessSubFilter(brightnessFinal));
        myFilter.addSubFilter(new SaturationSubfilter(saturationFinal));
        myFilter.addSubFilter(new ContrastSubFilter(contrastFinal));

        finalBitmap = myFilter.processFilter(bitmap);
    }

    @Override
    public void onFilterSelected(Filter filter) {
        resetControl();
        filteredBitmap = originalBitmap.copy(Bitmap.Config.ARGB_8888, true);
        photoEditorView.getSource().setImageBitmap(filter.processFilter(finalBitmap));
        finalBitmap = filteredBitmap.copy(Bitmap.Config.ARGB_8888, true);
    }

    private void resetControl() {
        if (editImageFragment != null)
            editImageFragment.resetControls();
        brightnessFinal = 0;
        saturationFinal = 1.0f;
        contrastFinal = 1.0f;
    }

    public void openImageFromGallery() {
        Dexter.withActivity(this)
                .withPermissions(Manifest.permission.READ_EXTERNAL_STORAGE,
                        Manifest.permission.WRITE_EXTERNAL_STORAGE)
                .withListener(new MultiplePermissionsListener() {
                    @Override
                    public void onPermissionsChecked(MultiplePermissionsReport report) {
                        if (report.areAllPermissionsGranted()) {
                            Intent intent = new Intent(Intent.ACTION_PICK);
                            intent.setType("image/*");
                            startActivityForResult(intent, PERMISSION_PICK_IMAGE);
                        } else {
                            Toast.makeText(MainActivity.this, "Permission denied", Toast.LENGTH_SHORT).show();
                        }
                    }

                    @Override
                    public void onPermissionRationaleShouldBeShown(List<PermissionRequest> list, PermissionToken permissionToken) {
                        permissionToken.continuePermissionRequest();
                    }
                }).check();
    }

    public void saveImageToGallery() {
        Dexter.withActivity(this)
                .withPermissions(Manifest.permission.READ_EXTERNAL_STORAGE,
                        Manifest.permission.WRITE_EXTERNAL_STORAGE)
                .withListener(new MultiplePermissionsListener() {
                    @Override
                    public void onPermissionsChecked(MultiplePermissionsReport report) {
                        if (report.areAllPermissionsGranted()) {
                            photoEditor.saveAsBitmap(new OnSaveBitmap() {
                                @Override
                                public void onBitmapReady(Bitmap saveBitmap) {
                                    try {
                                        photoEditorView.getSource().setImageBitmap(saveBitmap);

                                        BitmapDrawable drawable = (BitmapDrawable) photoEditorView.getSource().getDrawable();
                                        Bitmap bitmap = drawable.getBitmap();
                                        final String path = BitmapUtils.insertImage(getContentResolver(),
                                                bitmap,
                                                System.currentTimeMillis() + "_profile.jpg",
                                                null);
                                        if (!TextUtils.isEmpty(path)) {
                                            Snackbar snackbar = Snackbar.make(coordinatorLayout,
                                                    "Изображение сохранено!",
                                                    Snackbar.LENGTH_LONG)
                                                    .setAction("Открыть", new View.OnClickListener() {
                                                        @Override
                                                        public void onClick(View v) {
                                                            openImage(path);
                                                        }
                                                    });
                                            snackbar.show();
                                        } else {
                                            Snackbar snackbar = Snackbar.make(coordinatorLayout,
                                                    "Произошла ошибка при сохранении!",
                                                    Snackbar.LENGTH_LONG);
                                            snackbar.show();
                                        }
                                    } catch (IOException e) {
                                        e.printStackTrace();
                                    }
                                }

                                @Override
                                public void onFailure(Exception e) {

                                }
                            });
                        } else {
                            Toast.makeText(MainActivity.this, "Отказано в доступе", Toast.LENGTH_SHORT).show();
                        }
                    }

                    @Override
                    public void onPermissionRationaleShouldBeShown(List<PermissionRequest> list, PermissionToken permissionToken) {
                        permissionToken.continuePermissionRequest();
                    }
                }).check();
    }

    public void openImage(String path) {
//        Intent intent = new Intent();
//        intent.setAction(Intent.ACTION_VIEW);
//        intent.setDataAndType(Uri.parse(path), "image/*");
//        startActivity(intent);
        Intent photoPickerIntent = new Intent(Intent.ACTION_PICK);
        photoPickerIntent.setType("image/*");
        startActivityForResult(photoPickerIntent, 101);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_OK) {
            Uri selectedImage = data.getData();
            if (requestCode == UCrop.REQUEST_CROP){
                handleCropResultData(data);
            }
            else {
                try {
                    Log.d(TAG, "DATA: " + data.getData().toString());
//                    Bitmap bitmap = BitmapUtils.getBitmapFromGallery(this, data.getData(), 800, 800);
                    image_selected_uri = data.getData();
                    Bitmap bitmap = MediaStore.Images.Media.getBitmap(getContentResolver(), selectedImage);
                    originalBitmap.recycle();
                    finalBitmap.recycle();
                    filteredBitmap.recycle();

                    originalBitmap = bitmap.copy(Bitmap.Config.ARGB_8888, true);
                    finalBitmap = originalBitmap.copy(Bitmap.Config.ARGB_8888, true);
                    filteredBitmap = originalBitmap.copy(Bitmap.Config.ARGB_8888, true);
                    photoEditorView.getSource().setImageBitmap(originalBitmap);
                    bitmap.recycle();
                } catch (FileNotFoundException e) {
                    e.printStackTrace();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }

//            filtersListFragment.displayThumbnail(originalBitmap);
        }
        else if (requestCode == UCrop.REQUEST_CROP){
            handleCropError(data);
        }
    }

    private void handleCropError(Intent data) {
        final Throwable cropError = UCrop.getError(data);
        if (cropError != null){
            Toast.makeText(this, "" + cropError.getMessage(), Toast.LENGTH_SHORT).show();
        } else {
            Toast.makeText(this, "Неизвестная ошибка", Toast.LENGTH_SHORT).show();
        }
    }

    private void handleCropResultData(Intent data) {
        final Uri resultUri = UCrop.getOutput(data);
        if (resultUri != null)
            photoEditorView.getSource().setImageURI(resultUri);
        else
            Toast.makeText(this, "Данные не получены", Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onBrushSizeChangedListener(float size) {
        if (!mIsEraser) {
            photoEditor.setBrushSize(size);
            photoEditor.setBrushDrawingMode(true);
        } else {
            photoEditor.setBrushEraserSize(size);
            photoEditor.brushEraser();
        }
    }

    @Override
    public void onBrushOpacityChangedListener(int opacity) {
        if (!mIsEraser)
            photoEditor.setOpacity(opacity);
    }

    @Override
    public void onBrushColorChangedListener(int color) {
        if (!mIsEraser)
        photoEditor.setBrushColor(color);
    }

    @Override
    public void onBrushStateChangedListener(boolean isEraser) {
        if (isEraser) {
            photoEditor.brushEraser();
            mIsEraser = true;
        } else {
            photoEditor.setBrushDrawingMode(true);
            mIsEraser = false;
        }
    }

    @Override
    public void onAddTextButtonClick(String text, int color) {
        photoEditor.addText(text, color);
    }

    @Override
    public void onGalleryButtonCLickListener() {
        openImageFromGallery();
    }
}