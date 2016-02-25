package com.imooc.run.newyear;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.os.Bundle;
import android.view.Gravity;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.PopupWindow;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.RelativeLayout;
import android.widget.Toast;

import com.imooc.run.newyear.Util.Util;
import com.imooc.run.newyear.constants.Constants;

import org.jetbrains.annotations.Contract;

public class PicSelectActivity extends Activity {

    private PopupWindow mPopupWindow;

    private int picId;

    private Context mContext;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_pic_select);

        mContext = PicSelectActivity.this;
        picId = R.drawable.pic1;

        initView();

        Toast.makeText(PicSelectActivity.this, getResources().getString(R.string.long_press_hint1), Toast.LENGTH_SHORT).show();
    }

    private void initView() {
        RadioGroup mPics = (RadioGroup) findViewById(R.id.pics);

        Button mConfirm = (Button) findViewById(R.id.confirm);
        Button mCancel = (Button) findViewById(R.id.cancel);

        View popupView = getLayoutInflater().inflate(R.layout.popupwindow, new RelativeLayout(mContext), false);

        mPopupWindow = new PopupWindow(popupView, RelativeLayout.LayoutParams.WRAP_CONTENT,
                RelativeLayout.LayoutParams.WRAP_CONTENT, true);
        //点击空白处时关闭popupWindow
        mPopupWindow.setTouchable(true);
        mPopupWindow.setOutsideTouchable(true);
        mPopupWindow.setBackgroundDrawable(new BitmapDrawable(getResources(), (Bitmap) null));

        //选择不同的图片时，改变回传的图片id
        mPics.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup group, int checkedId) {
                picId = getLargePictureID(checkedId);
            }
        });

        mConfirm.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {//回传数据
                Intent data = new Intent();
                data.putExtra("picId", String.valueOf(picId));
                setResult(Constants.DEFAULT_RESULT, data);
                PicSelectActivity.this.finish();
            }
        });

        mCancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                PicSelectActivity.this.finish();
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
                view.setOnLongClickListener(new LongClick());
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

    private class LongClick implements View.OnLongClickListener {

        @Override
        public boolean onLongClick(View v) {

            int largePicId = getLargePictureID(v.getId()); //获得大图的id

            //将大图设置到popupWindow的ImageView上
            ((ImageView) mPopupWindow.getContentView().findViewById(R.id.large_pic))
                    .setImageDrawable(Util.getDrawable(mContext, largePicId));

            mPopupWindow.setAnimationStyle(R.style.anim_menu_bottombar);//设置popupWindow的弹出动画
            mPopupWindow.showAtLocation(v.getRootView(), Gravity.CENTER, 0, 0); //设置popupWindow的弹出位置
            mPopupWindow.update(0, 0, mPopupWindow.getWidth(), mPopupWindow.getHeight());
            mPopupWindow.showAsDropDown(v);

            Util.vibrate(PicSelectActivity.this, 2);

            return true;
        }
    }
}