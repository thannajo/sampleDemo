package com.morefun.mpos.reader.sampleDemo.activity;

import android.app.Activity;
import android.content.Intent;
import android.content.res.AssetManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.Typeface;
import android.os.Bundle;
import android.os.Environment;
import android.text.Html;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.style.AbsoluteSizeSpan;
import android.view.Gravity;
import android.view.View;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.TextView;

import com.leon.lfilepickerlibrary.LFilePicker;
import com.leon.lfilepickerlibrary.utils.Constant;
import com.mf.mpos.pub.CommEnum;
import com.mf.mpos.pub.Controler;
import com.mf.mpos.pub.result.ShowBitMapResult;
import com.mf.mpos.utils.BmpUtil;
import com.morefun.mpos.reader.sampleDemo.R;
import com.morefun.mpos.reader.sampleDemo.utils.SweetDialogUtils;
import com.morefun.mpos.reader.sampleDemo.utils.ToastUtils;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import cn.pedant.SweetAlert.SweetAlertDialog;

public class SetBitmapActivity extends BaseActivity {

    @BindView(R.id.textView)
    TextView textView;

    @BindView(R.id.tv_bitmapTip)
    TextView tv_bitmapTip;

    @BindView(R.id.rb_ram)
    RadioButton rbRam;

    @BindView(R.id.rb_rom)
    RadioButton rbRom;

    @BindView(R.id.et_index)
    EditText etIndex;

    @BindView(R.id.et_width)
    EditText et_width;

    @BindView(R.id.et_height)
    EditText et_height;

    @BindView(R.id.et_line1View)
    EditText et_line1View;

    @BindView(R.id.et_line2View)
    EditText et_line2View;

    @BindView(R.id.et_line3View)
    EditText et_line3View;

    private final static int BMP_WIDTH = 128;
    private final static int BMP_HEIGHT = 48;
    private static Typeface fontFace;
    private LinearLayout layoutBody;
    private String fileName = Environment.getExternalStorageDirectory().getPath() + File.separator + "morefun" + File.separator + "1.bmp";
    private String mImageFilePath;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_bitmap);
        ButterKnife.bind(this);

        layoutBody = new LinearLayout(this);
        layoutBody.setBackgroundColor(Color.WHITE);
        layoutBody.setOrientation(LinearLayout.VERTICAL);
        layoutBody.setLayoutParams(new LinearLayout.LayoutParams(BMP_WIDTH, BMP_HEIGHT));

        fontFace = Typeface.createFromAsset(getAssets(), "fonts/Inter-Bold.ttf");
        setButtonName();
        tv_bitmapTip.setText(Html.fromHtml(getString(R.string.set_bitmap_tip)));
    }

    @OnClick({R.id.btn_choose_bitmap, R.id.btn_set_bitmap, R.id.btn_show_bitmap, R.id.btn_view_bitmap})
    public void onClick(View view) {
        if (!Controler.posConnected()) {
            SweetDialogUtils.showError(SetBitmapActivity.this, getString(R.string.device_not_connect));
            return;
        }
        switch (view.getId()) {
            case R.id.btn_choose_bitmap:
                showFileChooser();
                break;
            case R.id.btn_set_bitmap:
                SweetDialogUtils.showProgress(SetBitmapActivity.this, "Loading...", false);
                runOnThread(new Runnable() {
                    @Override
                    public void run() {
                        setProgress("Loading bitmap...");
                        Bitmap bitmap = getImageFile(mImageFilePath);
                        if (bitmap == null) {
                            SweetDialogUtils.changeAlertType(SetBitmapActivity.this, "Please choose bitmap", SweetAlertDialog.ERROR_TYPE);
                            return;
                        }
                        int index = Integer.parseInt(etIndex.getText().toString());
                        int width = Integer.parseInt(et_width.getText().toString());
                        int height = Integer.parseInt(et_height.getText().toString());

                        ShowBitMapResult result = Controler.setDisplayWithBitmap(getType(), index, 0, 0, width, height, bitmap);

                        if (result.commResult.equals(CommEnum.COMMRET.NOERROR)) {
                            if (result.showret == 0) {
                                SweetDialogUtils.changeAlertType(SetBitmapActivity.this, "Download finish", SweetAlertDialog.SUCCESS_TYPE);
                                return;
                            }
                        }
                        SweetDialogUtils.changeAlertType(SetBitmapActivity.this, "Download Fail", SweetAlertDialog.ERROR_TYPE);
                    }
                });
                break;
            case R.id.btn_show_bitmap:
                runOnThread(new Runnable() {
                    @Override
                    public void run() {
                        showBitmap();
                    }
                });
                break;
            case R.id.btn_view_bitmap:
                runOnThread(new Runnable() {
                    @Override
                    public void run() {
                        SweetDialogUtils.showProgress(SetBitmapActivity.this, "Loading...", false);
                        viewToBitmap();
                    }
                });
                break;
        }
    }

    @Override
    protected void setButtonName() {
    }

    private void viewToBitmap() {
        layoutBody.removeAllViews();

        addView(et_line1View.getText().toString(), Gravity.CENTER);
        addView(et_line2View.getText().toString(), Gravity.RIGHT);
        addView(et_line3View.getText().toString(), Gravity.CENTER);

        Bitmap bitmap = convertViewToBitmap(layoutBody);

        if (bitmap == null) {
            SweetDialogUtils.changeAlertType(SetBitmapActivity.this, "Please choose bitmap", SweetAlertDialog.ERROR_TYPE);
            return;
        }
        int index = Integer.parseInt(etIndex.getText().toString());
        int width = Integer.parseInt(et_width.getText().toString());
        int height = Integer.parseInt(et_height.getText().toString());

        setProgress("Loading bitmap...");
        ShowBitMapResult result = Controler.setDisplayWithBitmap(getType(), index, 0, 0, width, height, bitmap);

        if (result.commResult.equals(CommEnum.COMMRET.NOERROR)) {
            if (result.showret == 0) {
                SweetDialogUtils.changeAlertType(SetBitmapActivity.this, "Download finish", SweetAlertDialog.SUCCESS_TYPE);
                return;
            }
        }
        SweetDialogUtils.changeAlertType(SetBitmapActivity.this, "Download Fail", SweetAlertDialog.ERROR_TYPE);
    }

    public void addView(String str, int gravity) {
        int fontSize = 14;
        TextView tView = new TextView(this);
        tView.setTypeface(fontFace);
        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(128, 16);
        lp.gravity = gravity;
        tView.setLayoutParams(lp);

        SpannableString spannableString = new SpannableString(str);
        spannableString.setSpan(new AbsoluteSizeSpan(fontSize), 0,
                spannableString.toString().length(),
                Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
        tView.setPadding(0, -1, 0, -1);
        tView.setBackgroundColor(Color.WHITE);
        tView.setTextColor(Color.BLACK);
        tView.setGravity(gravity);
        tView.getPaint().setFakeBoldText(false);
        tView.setText(spannableString);
        layoutBody.addView(tView);

    }

    private Bitmap convertViewToBitmap(View view) {
        view.measure(View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED),
                View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED));
        view.layout(0, 0, view.getMeasuredWidth(), view.getMeasuredHeight());
        view.buildDrawingCache();
        Bitmap bitmap = view.getDrawingCache();
        saveBitmap(bitmap, fileName);
        return bitmap;
    }

    private void setProgress(final String result) {
        SweetDialogUtils.setProgressText(SetBitmapActivity.this, result);
    }

    private Bitmap getImageFile(String fileName) {
        if (fileName == null) {
            return null;
        }

        Bitmap bitmap = getFromSdCardFile(fileName);
        if (bitmap != null) {
            return bitmap;
        } else {
            return getImageFromAssetsFile(fileName);
        }
    }

    private Bitmap getImageFromAssetsFile(String fileName) {
        Bitmap image = null;
        AssetManager am = getResources().getAssets();

        try {
            InputStream is = am.open(fileName);
            image = BitmapFactory.decodeStream(is);
            is.close();

        } catch (IOException e) {
            e.printStackTrace();
        }

        return image;
    }

    private Bitmap getFromSdCardFile(String fileName) {
        Bitmap image = null;

        File file = new File(fileName);
        if (file.exists()) {
            try {
                InputStream is = new FileInputStream(file);
                image = BitmapFactory.decodeStream(is);
                is.close();

            } catch (IOException e) {
                e.printStackTrace();
            }
        } else {
            return null;
        }

        return image;
    }

    private String saveBitmap(Bitmap bitmap, String name) {
        try {
            File file = new File(name);
            if (!file.getParentFile().exists()) {
                file.getParentFile().mkdirs();
            }
            file.createNewFile();
            FileOutputStream fileOutputStream = new FileOutputStream(file);
            byte[] data = BmpUtil.changeToMonochromeBitmap(bitmap);
            fileOutputStream.write(data);
            fileOutputStream.close();

        } catch (Exception ex) {
            ex.printStackTrace();
        }

        return null;
    }

    private synchronized void showBitmap() {
        Bitmap bitmap = getImageFile(mImageFilePath);
        int width = Integer.parseInt(et_width.getText().toString());
        if (bitmap == null) {
            ToastUtils.show(SetBitmapActivity.this, "Please choose bitmap");
            return;
        }
        Controler.showBitmapUnblock(width, 30, BmpUtil.changeSingleBytes(bitmap));
    }

    private CommEnum.POS_SAVE_BITMAP_TYPE getType() {
        if (rbRam.isChecked()) {
            return CommEnum.POS_SAVE_BITMAP_TYPE.RAM;
        } else if (rbRom.isChecked()) {
            return CommEnum.POS_SAVE_BITMAP_TYPE.ROM;
        }
        return CommEnum.POS_SAVE_BITMAP_TYPE.ROM;
    }


    private void showFileChooser() {
        new LFilePicker()
                .withActivity(this)
                .withRequestCode(0)
                .withTitle("选择文件")
                .withMutilyMode(false)
                .withStartPath("/mnt/sdcard")//指定初始显示路径
                .withNotFoundBooks("至少选择一个文件")
                .withChooseMode(true)//文件选择模式
                //.withFileFilter(new String[]{".bin"})
                .start();
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (resultCode == Activity.RESULT_OK) {
            List<String> list = data.getStringArrayListExtra(Constant.RESULT_INFO);
            mImageFilePath = list.get(0);
            textView.setText(mImageFilePath);
        }
    }

}