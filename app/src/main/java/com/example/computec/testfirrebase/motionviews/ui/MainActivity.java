package com.example.computec.testfirrebase.motionviews.ui;

import android.Manifest;
import android.annotation.TargetApi;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.graphics.PointF;
import android.graphics.RectF;
import android.graphics.drawable.BitmapDrawable;
import android.media.ExifInterface;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.support.annotation.ColorInt;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.content.res.ResourcesCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.WindowManager;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.target.BitmapImageViewTarget;
import com.example.computec.testfirrebase.R;
import com.example.computec.testfirrebase.SportLifeFeed;
import com.example.computec.testfirrebase.User;
import com.example.computec.testfirrebase.motionviews.color.SpectrumPalette;
import com.example.computec.testfirrebase.motionviews.ui.adapter.FontsAdapter;
import com.example.computec.testfirrebase.motionviews.ui.adapter.ThumbnailsAdapter;
import com.example.computec.testfirrebase.motionviews.utils.FontProvider;
import com.example.computec.testfirrebase.motionviews.viewmodel.Font;
import com.example.computec.testfirrebase.motionviews.viewmodel.Layer;
import com.example.computec.testfirrebase.motionviews.viewmodel.TextLayer;
import com.example.computec.testfirrebase.motionviews.viewmodel.ThumbnailItem;
import com.example.computec.testfirrebase.motionviews.widget.MotionView;
import com.example.computec.testfirrebase.motionviews.widget.OnSwipeTouchListener;
import com.example.computec.testfirrebase.motionviews.widget.entity.ImageEntity;
import com.example.computec.testfirrebase.motionviews.widget.entity.MotionEntity;
import com.example.computec.testfirrebase.motionviews.widget.entity.TextEntity;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.zomato.photofilters.SampleFilters;
import com.zomato.photofilters.imageprocessors.Filter;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.UUID;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

import static com.yalantis.ucrop.util.BitmapLoadUtils.calculateInSampleSize;

public class MainActivity extends AppCompatActivity implements TextEditorDialogFragment.OnTextLayerCallback,
        ThumbnailCallback, MotionView.MotionViewCallback, SpectrumPalette.OnColorSelectedListener {

    static {
        System.loadLibrary("NativeImageProcessor");
    }

    public static final int SELECT_STICKER_REQUEST_CODE = 123;
    private static final String PROFILE_PATH = "profile_file_path";

    @BindView(R.id.filterV)
    View filterV;
    @BindView(R.id.main_motion_view)
    MotionView motionView;
    @BindView(R.id.frameL)
    View imageLayout;
    @BindView(R.id.profileIV)
    ImageView profileIV;
    @BindView(R.id.deleteIV)
    ImageView deleteIV;
    @BindView(R.id.palette)
    SpectrumPalette spectrumPalette;
    @BindView(R.id.scrollV)
    ScrollView scrollV;
    @BindView(R.id.createET)
    EditText createET;
    @BindView(R.id.changeFontIV)
    ImageView changeFontIV;

    private FontProvider fontProvider;
    StorageReference mStorageRef;
    DatabaseReference myRef;
    PointF currentP;
    ArrayList<ThumbnailItem> thumbs = new ArrayList<>();
    OnSwipeTouchListener onSwipeTouchListener;
    User user;

    public static Intent newInstance(Context context, String path) {
        Intent intent = new Intent(context, MainActivity.class);
        intent.putExtra(PROFILE_PATH, path);
        return intent;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);

        ButterKnife.bind(this);
        this.fontProvider = new FontProvider(getResources());

        String profileFilePath = getIntent().getStringExtra(PROFILE_PATH);
        File profilePicFile = new File(profileFilePath);

        if (profilePicFile.exists()) {
            String resizedImagePath = resizeAndCompressImageBeforeSend(this,
                    profileFilePath, profilePicFile.getName());
            getCircularImage(profileIV, resizedImagePath);
        } else {
            Toast.makeText(this, "Invalid Image file", Toast.LENGTH_SHORT).show();
        }

        motionView.setMotionViewCallback(this);
        spectrumPalette.setOnColorSelectedListener(this);

        user = new User("amrbarakat", "amr", 28, new Date());
        mStorageRef = FirebaseStorage.getInstance().getReference();
        myRef = FirebaseDatabase.getInstance().getReference();

        onSwipeTouchListener = new OnSwipeTouchListener(this) {
            public void onSwipeRight() {
                int position = getPosition();
                if (position > 0) {
                    setPosition(position - 1);
                    profileIV.setImageBitmap(thumbs.get(getPosition()).getImage());
                }
            }

            public void onSwipeLeft() {
                int position = getPosition();
                if (position < thumbs.size() - 1) {
                    setPosition(position + 1);
                    profileIV.setImageBitmap(thumbs.get(getPosition()).getImage());
                }
            }
        };
        filterV.setOnTouchListener(onSwipeTouchListener);
    }

    @OnClick({R.id.privateB, R.id.publicB})
    public void completePreview() {
        ProgressDialog dialog = new ProgressDialog(MainActivity.this);
        dialog.setMessage("Upload Snap");
        dialog.setCanceledOnTouchOutside(false);
        dialog.show();

        motionView.unselectEntity();
        Bitmap bitmap = saveImage();
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.JPEG, 100, baos);
        byte[] data = baos.toByteArray();
        StorageReference imageRef = mStorageRef.child(UUID.randomUUID() + ".jpg");

        UploadTask uploadTask = imageRef.putBytes(data);
        uploadTask.addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception exception) {
                // Handle unsuccessful uploads
                Log.d("fail", exception.getMessage());
            }
        }).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
            @Override
            public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                Uri downloadUrl = taskSnapshot.getDownloadUrl();
                SportLifeFeed snap = new SportLifeFeed(null, user.getId(), downloadUrl.toString(),
                        0, 0, 0);
                myRef.child("sport_life").child("public")
                        .child("snap")
                        .push().setValue(snap);
                start();
            }
        });
    }

    private void start() {
        finish();
    }

    @TargetApi(Build.VERSION_CODES.M)
    public void requestPermissionsSafely(String[] permissions, int requestCode) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            requestPermissions(permissions, requestCode);
        }
    }

    @TargetApi(Build.VERSION_CODES.M)
    public boolean hasPermission(String permission) {
        return Build.VERSION.SDK_INT < Build.VERSION_CODES.M ||
                checkSelfPermission(permission) == PackageManager.PERMISSION_GRANTED;
    }

    private void bindDataToAdapter() {
        final Context context = this;
        Handler handler = new Handler();
        Runnable r = new Runnable() {
            public void run() {
                Bitmap bitmap = ((BitmapDrawable) profileIV.getDrawable()).getBitmap();
                bitmap = bitmap.copy(Bitmap.Config.ARGB_8888, true);

                thumbs.add(getThumbnailItem(bitmap, null));
                thumbs.add(getThumbnailItem(bitmap, SampleFilters.getStarLitFilter()));
                thumbs.add(getThumbnailItem(bitmap, SampleFilters.getBlueMessFilter()));
                thumbs.add(getThumbnailItem(bitmap, SampleFilters.getAweStruckVibeFilter()));
                thumbs.add(getThumbnailItem(bitmap, SampleFilters.getLimeStutterFilter()));
                thumbs.add(getThumbnailItem(bitmap, SampleFilters.getNightWhisperFilter()));

                ThumbnailsAdapter adapter = new ThumbnailsAdapter(thumbs, (ThumbnailCallback) context);
                adapter.notifyDataSetChanged();
            }
        };
        handler.post(r);
    }

    @NonNull
    private ThumbnailItem getThumbnailItem(Bitmap bitmap, Filter filter) {
        ThumbnailItem thumbnailItem;
        Bitmap thumbImage1 = Bitmap.createScaledBitmap(bitmap, bitmap.getWidth(), bitmap.getHeight(), false);
        thumbnailItem = new ThumbnailItem(thumbImage1, filter);
        return thumbnailItem;
    }

    private void addSticker(final int stickerResId) {
        motionView.post(new Runnable() {
            @Override
            public void run() {
                Layer layer = new Layer();
                Bitmap pica = BitmapFactory.decodeResource(getResources(), stickerResId);

                ImageEntity entity = new ImageEntity(layer, pica, motionView.getWidth(), motionView.getHeight());

                motionView.addEntityAndPosition(entity);
            }
        });
    }

    @OnClick(R.id.addStickerIV)
    public void addSticker() {
        Intent intent = new Intent(this, StickerSelectActivity.class);
        startActivityForResult(intent, SELECT_STICKER_REQUEST_CODE);
    }

    @OnClick(R.id.addFilterIV)
    public void addFilterIV() {
        if (filterV.getVisibility() == View.GONE) {
            filterV.setVisibility(View.VISIBLE);
            motionView.unselectEntity();
            spectrumPalette.setVisibility(View.GONE);
        } else
            filterV.setVisibility(View.GONE);
    }

    @OnClick(R.id.deleteIV)
    public void deleteEntity() {
        MotionEntity entity = motionView.getSelectedEntity();
        if (entity != null)
            motionView.deletedSelectedEntity();
        deleteIV.setVisibility(View.GONE);
        spectrumPalette.setVisibility(View.GONE);
    }

    private void startTextEntityEditing() {

        try {
            final TextEntity textEntity = currentTextEntity();

            final PointF oldPostion = textEntity.absoluteCenter();
            currentP = oldPostion;
            PointF center = textEntity.absoluteCenter();
            center.y = center.y * 100F;
            textEntity.moveCenterTo(center);


            createET.setText(textEntity.getLayer().getText());
            createET.setSelection(createET.getText().length());
            spectrumPalette.setVisibility(View.GONE);
            filterV.setVisibility(View.GONE);
            createET.setVisibility(View.VISIBLE);
            createET.setSingleLine();

            createET.setImeOptions(EditorInfo.IME_ACTION_DONE);
            final InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
            imm.showSoftInput(createET, InputMethodManager.SHOW_FORCED);

            createET.requestFocus();
            textEntity.moveCenterTo(center);

            createET.setOnEditorActionListener(new TextView.OnEditorActionListener() {
                @Override
                public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                    if (actionId == EditorInfo.IME_ACTION_DONE) {

                        TextLayer textLayer = textEntity.getLayer();
                        if (!v.getText().toString().equals(textLayer.getText())) {
                            textLayer.setText(v.getText().toString());
                            textEntity.updateEntity();
                            motionView.invalidate();
                        }
                        textEntity.moveCenterTo(oldPostion);
                        v.setVisibility(View.GONE);
                    }
                    return false;
                }
            });

        } catch (Exception e) {

        }
    }

    @Nullable
    private TextEntity currentTextEntity() {
        if (motionView != null && motionView.getSelectedEntity() instanceof TextEntity) {
            return ((TextEntity) motionView.getSelectedEntity());
        } else {
            return null;
        }
    }

    @OnClick(R.id.addTextIV)
    protected void addTextSticker() {
        spectrumPalette.setVisibility(View.GONE);
        filterV.setVisibility(View.GONE);
        createET.setVisibility(View.VISIBLE);
        createET.requestFocus();
        createET.setSingleLine();
        createET.setImeOptions(EditorInfo.IME_ACTION_DONE);
        final InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
        imm.showSoftInput(createET, InputMethodManager.SHOW_FORCED);

        createET.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                if (actionId == EditorInfo.IME_ACTION_DONE) {
                    if (v.getText().toString() != null && !v.getText().toString().isEmpty()) {
                        TextLayer textLayer = createTextLayer(v.getText().toString());
                        TextEntity textEntity = new TextEntity(textLayer, motionView.getWidth(),
                                motionView.getHeight(), fontProvider);
                        textEntity.setColor(ResourcesCompat.getColor(getResources(), R.color.color_6, null));

                        motionView.addEntityAndPosition(textEntity);

                        // redraw
                        motionView.invalidate();
                        v.setVisibility(View.GONE);
                        v.setText("");
                    }
                }
                return false;
            }
        });
    }

    @OnClick(R.id.changeFontIV)
    public void changeTextEntityFont() {
        final List<String> fonts = fontProvider.getFontNames();
        FontsAdapter fontsAdapter = new FontsAdapter(this, fonts, fontProvider);
        new AlertDialog.Builder(this)
                .setTitle(R.string.select_font)
                .setAdapter(fontsAdapter, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int which) {
                        TextEntity textEntity = currentTextEntity();
                        if (textEntity != null) {
                            textEntity.getLayer().getFont().setTypeface(fonts.get(which));
                            textEntity.updateEntity();
                            motionView.invalidate();
                        }
                    }
                })
                .show();
    }

    private TextLayer createTextLayer(String message) {
        TextLayer textLayer = new TextLayer();
        Font font = new Font();

        font.setColor(TextLayer.Limits.INITIAL_FONT_COLOR);
        font.setSize(TextLayer.Limits.INITIAL_FONT_SIZE);
        font.setTypeface(fontProvider.getDefaultFontName());

        textLayer.setFont(font);

        textLayer.setText(message);

        return textLayer;
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_OK) {
            if (requestCode == SELECT_STICKER_REQUEST_CODE) {
                if (data != null) {
                    int stickerId = data.getIntExtra(StickerSelectActivity.EXTRA_STICKER_ID, 0);
                    if (stickerId != 0) {
                        addSticker(stickerId);
                    }
                }
            }
        }
    }

    @Override
    public void textChanged(@NonNull String text) {
        TextEntity textEntity = currentTextEntity();
        if (textEntity != null) {
            TextLayer textLayer = textEntity.getLayer();
            if (!text.equals(textLayer.getText())) {
                textLayer.setText(text);
                textEntity.updateEntity();
                motionView.invalidate();
            }
        }
    }

    public Bitmap viewToBitmap(View view) {
        RectF bitmapRect = new RectF();
        bitmapRect.right = profileIV.getDrawable().getIntrinsicWidth();
        bitmapRect.bottom = profileIV.getDrawable().getIntrinsicHeight();

        Matrix m = profileIV.getImageMatrix();
        m.mapRect(bitmapRect);

        int width = (int) bitmapRect.width();
        int height = (int) bitmapRect.height();

        int startWidth = (profileIV.getWidth() - width) / 2;
        int startHeight = (profileIV.getHeight() - height) / 2;

        Bitmap bitmap = Bitmap.createBitmap(view.getWidth(), view.getHeight(), Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(bitmap);
        view.draw(canvas);
        Bitmap cropBitmap = Bitmap.createBitmap(bitmap, startWidth, startHeight, width, height);
        return cropBitmap;
    }

    @Override
    public void onThumbnailClick(Bitmap bitmap) {
        profileIV.setImageBitmap(bitmap);
    }

    public static String resizeAndCompressImageBeforeSend(Context context, String filePath, String fileName) {
        final int MAX_IMAGE_SIZE = 150 * 1024;

        final BitmapFactory.Options options = new BitmapFactory.Options();
        options.inJustDecodeBounds = true;
        BitmapFactory.decodeFile(filePath, options);

        options.inSampleSize = calculateInSampleSize(options, 800, 800);

        options.inJustDecodeBounds = false;
        options.inPreferredConfig = Bitmap.Config.ARGB_8888;

        Bitmap bmpPic = BitmapFactory.decodeFile(filePath, options);

        int compressQuality = 100;
        int streamLength;
        do {
            ByteArrayOutputStream bmpStream = new ByteArrayOutputStream();
            bmpPic.compress(Bitmap.CompressFormat.JPEG, compressQuality, bmpStream);
            byte[] bmpPicByteArray = bmpStream.toByteArray();
            streamLength = bmpPicByteArray.length;
            compressQuality -= 5;
            Log.d("compressBitmap", "Quality: " + compressQuality);
            Log.d("compressBitmap", "Size: " + streamLength / 1024 + " kb");
        } while (streamLength >= MAX_IMAGE_SIZE && compressQuality > 5);

        bmpPic = rotateBitmap(bmpPic, filePath);

        try {
            Log.d("compressBitmap", "cacheDir: " + context.getCacheDir());
            FileOutputStream bmpFile = new FileOutputStream(context.getCacheDir() + fileName);
            bmpPic.compress(Bitmap.CompressFormat.JPEG, compressQuality, bmpFile);
            bmpFile.flush();
            bmpFile.close();
        } catch (Exception e) {
            Log.e("compressBitmap", "Error on saving file");
        }

        return context.getCacheDir() + fileName;
    }

    /**
     * Rotate a bitmap based on orientation metadata.
     */
    public static Bitmap rotateBitmap(Bitmap bitmap, String filePath) {
        try {
            ExifInterface exif = new ExifInterface(filePath);
            int orientation = exif.getAttributeInt(ExifInterface.TAG_ORIENTATION, ExifInterface.ORIENTATION_NORMAL);

            Matrix matrix = new Matrix();
            switch (orientation) {
                case ExifInterface.ORIENTATION_FLIP_HORIZONTAL:
                    matrix.setScale(-1, 1);
                    break;
                case ExifInterface.ORIENTATION_ROTATE_180:
                    matrix.setRotate(180);
                    break;
                case ExifInterface.ORIENTATION_FLIP_VERTICAL:
                    matrix.setRotate(180);
                    matrix.postScale(-1, 1);
                    break;
                case ExifInterface.ORIENTATION_TRANSPOSE:
                    matrix.setRotate(90);
                    matrix.postScale(-1, 1);
                    break;
                case ExifInterface.ORIENTATION_ROTATE_90:
                    matrix.setRotate(90);
                    break;
                case ExifInterface.ORIENTATION_TRANSVERSE:
                    matrix.setRotate(-90);
                    matrix.postScale(-1, 1);
                    break;
                case ExifInterface.ORIENTATION_ROTATE_270:
                    matrix.setRotate(-90);
                    break;
                case ExifInterface.ORIENTATION_NORMAL:
                case ExifInterface.ORIENTATION_UNDEFINED:
                default:
                    return bitmap;
            }

            try {
                Bitmap oriented = Bitmap.createBitmap(bitmap, 0, 0, bitmap.getWidth(), bitmap.getHeight(), matrix, true);
                bitmap.recycle();
                return oriented;
            } catch (OutOfMemoryError e) {
                e.printStackTrace();
                return bitmap;
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return bitmap;
    }

    public void getCircularImage(final ImageView imageView, String imageUrl) {
        final Context context = imageView.getContext();
        Glide.with(context)
                .load(imageUrl)
                .asBitmap()
                .into(new BitmapImageViewTarget(imageView) {
                    @Override
                    protected void setResource(Bitmap resource) {
                        imageView.setImageBitmap(resource);
                        bindDataToAdapter();
                    }
                });
    }

    @Override
    public void onEntitySelected(@Nullable MotionEntity entity) {
        if (entity instanceof TextEntity) {
            deleteIV.setVisibility(View.VISIBLE);
            spectrumPalette.setVisibility(View.VISIBLE);
            filterV.setVisibility(View.GONE);
            changeFontIV.setVisibility(View.VISIBLE);
            spectrumPalette.setSelectedColor(((TextEntity) entity).getColor());
        } else if (entity instanceof ImageEntity) {
            deleteIV.setVisibility(View.VISIBLE);
            spectrumPalette.setVisibility(View.GONE);
            changeFontIV.setVisibility(View.GONE);
        } else {
            deleteIV.setVisibility(View.GONE);
            spectrumPalette.setVisibility(View.GONE);
            changeFontIV.setVisibility(View.GONE);
        }
    }

    @Override
    public void onEntityDoubleTap(@NonNull MotionEntity entity) {

        startTextEntityEditing();

        if (entity instanceof TextEntity && spectrumPalette.getVisibility() != View.VISIBLE) {
            filterV.setVisibility(View.GONE);
        }
    }

    @Override
    public void onColorSelected(@ColorInt int color) {
        TextEntity textEntity = currentTextEntity();
        if (textEntity == null) {
            return;
        }
        textEntity.getLayer().getFont().setColor(color);
        textEntity.updateEntity();
        motionView.invalidate();
        textEntity.setIsSelected(true);
        textEntity.setColor(color);
    }

    Bitmap saveImage() {

        requestPermissionsSafely(new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, 1);

        if (hasPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE)) {
            Bitmap bitmap = viewToBitmap(imageLayout);
            File file;
            try {

                File path = Environment.getExternalStorageDirectory();
                File dir = new File(path + "/360Sport/");

                if (!dir.exists())
                    dir.mkdir();

                file = new File(dir, "Snap_" + "with_compression" + ".jpg");

                OutputStream stream = null;
                stream = new FileOutputStream(file);

                bitmap.compress(Bitmap.CompressFormat.JPEG, 100, stream);
                stream.flush();
                stream.close();
                return bitmap;
            } catch (Exception e) {
                Toast.makeText(this, "Image Saved Failed", Toast.LENGTH_SHORT).show();
                e.printStackTrace();
            }
        } else {
            Toast.makeText(this, "has no permission", Toast.LENGTH_SHORT).show();
        }
        return null;
    }
}