package com.imooc.run.newyear;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnLongClickListener;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.RadioGroup.OnCheckedChangeListener;
import android.widget.RelativeLayout;
import android.widget.Toast;

import com.imooc.run.newyear.Util.PopupWindowUtil;
import com.imooc.run.newyear.Util.Util;
import com.imooc.run.newyear.constants.Constants;

import org.jetbrains.annotations.Contract;

public class PicSelectActivity extends Activity {

    private PopupWindowUtil popupWindowUtil;

    private int picId = R.drawable.pic1;

    private final Context mContext = PicSelectActivity.this;

    private RadioGroup mPics;

    private Util util;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        overridePendingTransition(R.anim.zoom_in, R.anim.zoom_out); //设置切换动画
        util = new Util(mContext, PicSelectActivity.this);
        util.smoothSwitchScreen();
        setContentView(R.layout.activity_pic_select);

        initView();

        util.showMessage(getString(R.string.long_press_hint), Toast.LENGTH_SHORT);
    }

    private void initView() {
        popupWindowUtil = new PopupWindowUtil(mContext, PicSelectActivity.this, R.layout.popupwindow_pic_select,
                RelativeLayout.LayoutParams.WRAP_CONTENT, RelativeLayout.LayoutParams.WRAP_CONTENT);
        mPics = (RadioGroup) findViewById(R.id.pics);

        Button mConfirm = (Button) findViewById(R.id.confirm);
        Button mCancel = (Button) findViewById(R.id.cancel);
        Button mRandomSelect = (Button) findViewById(R.id.random_select);

        popupWindowUtil.closeOnOutside(true); //点击空白处时关闭popupWindow

        //选择不同的图片时，改变回传的图片id
        mPics.setOnCheckedChangeListener(new OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup group, int checkedId) {
                picId = getLargePictureID(checkedId);
            }
        });

        mConfirm.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                backToMain(picId); //回传数据并返回主页面
            }
        });

        mCancel.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                PicSelectActivity.this.finish();
            }
        });

        mRandomSelect.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                final ProgressDialog progressDialog = ProgressDialog.show(mContext,
                        getString(R.string.info), getString(R.string.selecting));
                Handler handler = new Handler();
                handler.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        progressDialog.dismiss();
                        int random = util.getRandomNumber(mPics.getChildCount());
                        ((RadioButton) mPics.getChildAt(random)).setChecked(true);
                    }
                }, 2000);
            }
        });

        setOnLongClickListener(mPics);
    }

    /**
     * 为RadioGroup里的RadioButton设置长按监听器
     *
     * @param radioGroup 要添加长按监听器的RadioGroup
     */
    private void setOnLongClickListener(RadioGroup radioGroup) {
        int radioButtonCount = radioGroup.getChildCount();
        for (int i = 0; i < radioButtonCount; i++) {
            View view = radioGroup.getChildAt(i);
            if (view instanceof RadioButton) {
                view.setOnLongClickListener(new mLongClickWatcher());
            }
        }
    }

    /**
     * 通过所选择的RadioButton的id获得所对应的大图的id
     *
     * @param radioButtonID 所选择的RadioButton的id
     * @return 对应的大图的id
     */
    @Contract(pure = true)
    private int getLargePictureID(int radioButtonID) {
        return R.drawable.largep1 + radioButtonID - R.id.pic1;
    }

    /**
     * 返回主页面
     *
     * @param picId 需要回传的图片ID
     */
    private void backToMain(int picId) {
        Intent data = new Intent();
        data.putExtra("picId", String.valueOf(picId));
        setResult(Constants.RES_DEFAULT, data);
        PicSelectActivity.this.finish();
    }

    private class mLongClickWatcher implements OnLongClickListener {

        private Button mSelect;
        private ImageView mLargePic;

        private void init() {
            mSelect = (Button) popupWindowUtil.getView(R.id.select_picture_by_large_picture);
            mLargePic = (ImageView) popupWindowUtil.getView(R.id.large_pic);
        }

        @Override
        public boolean onLongClick(View v) {

            init();
            final RadioButton radioButton = (RadioButton) v;
            int largePicId = getLargePictureID(radioButton.getId()); //获得大图的id

            mSelect.setOnClickListener(new OnClickListener() {
                @Override
                public void onClick(View v) {
                    picId = getLargePictureID(radioButton.getId());
                    util.vibrate(Constants.VIBRATE_DOUBLE);
                    backToMain(picId);
                }
            });

            mLargePic.setImageDrawable(util.getDrawable(largePicId));//将大图设置到popupWindow的ImageView上

            popupWindowUtil.setAnimation(R.style.anim_menu_bottomBar);//设置popupWindow的弹出动画
            popupWindowUtil.showAtCenter(v);//设置popupWindow的弹出位置

            util.vibrate(Constants.VIBRATE_SINGLE);

            return true;
        }
    }
}
