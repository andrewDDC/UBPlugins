package com.ubsdk.lenovo.plugin.diy;

import java.util.ArrayList;

import com.umbrella.game.ubsdk.plugintype.pay.diy.PayMethodItem;
import com.umbrella.game.ubsdk.utils.DisplayUtil;
import com.umbrella.game.ubsdk.utils.ResUtil;
import com.umbrella.game.ubsdk.utils.TextUtil;
import com.umbrella.game.ubsdk.utils.UBLogUtil;

import android.app.Dialog;
import android.content.Context;
import android.graphics.Typeface;
import android.text.Spannable;
import android.text.SpannableStringBuilder;
import android.text.style.ForegroundColorSpan;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.LinearLayout.LayoutParams;
import android.widget.TextView;

public class PayDialog extends Dialog{
	
	private Context mContext; 
	private LayoutInflater mInflater;
	private View mContainer;
	private ImageView mCloseBtn;
	private TextView mPayOrderInfo;
	private LinearLayout mPayMethodItemContainer;
	private TextView mLogo;

	public PayDialog(Context context) {
		super(context,ResUtil.getStyleId(context,"LenovoPayDialog"));
		this.mContext=context;
		
		mInflater = LayoutInflater.from(context);
		
		initView();
		setListener();
	}

	private void initView() {
		mContainer = mInflater.inflate(ResUtil.getLayoutId(mContext, "lenovo_pay_dialog_pay"), null);
		mCloseBtn = (ImageView) mContainer.findViewById(ResUtil.getViewID(mContext,"img_close"));
		TextView title = (TextView) mContainer.findViewById(ResUtil.getViewID(mContext,"tv_payTitle"));
		mPayOrderInfo = (TextView) mContainer.findViewById(ResUtil.getViewID(mContext,"tv_payOrderInfo"));
		mPayMethodItemContainer = (LinearLayout) mContainer.findViewById(ResUtil.getViewID(mContext,"ll_payMethodItemContainer"));
		mLogo = (TextView) mContainer.findViewById(ResUtil.getViewID(mContext,"tv_logo"));
		
		title.setTypeface(Typeface.defaultFromStyle(Typeface.BOLD));  
		
		requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(mContainer);
        show();
        
        setCanceledOnTouchOutside(false);
        setCancelable(false);
        
        Window w = getWindow();
        WindowManager.LayoutParams lp = w.getAttributes();
        int screenWidth = DisplayUtil.screenWidth;
        int screenHeight = DisplayUtil.screenHeight;
        boolean isHorizontal = DisplayUtil.isHorizontal();
        if (isHorizontal) {
			lp.width=screenWidth*3/5;
//			lp.height=screenHeight*4/5;
		}else{
			lp.width=screenWidth;
//			lp.height=screenHeight*3/5;s
		}
        
        lp.gravity = Gravity.CENTER;
//        onWindowAttributesChanged(lp);
        w.setAttributes(lp);

        UBLogUtil.logI("screenWidth="+screenWidth);
        UBLogUtil.logI("screenHeight="+screenHeight);
        UBLogUtil.logI("lp.width="+lp.width);
        UBLogUtil.logI("lp.height="+lp.height);
	}
	
	private void setListener() {
		mCloseBtn.setOnClickListener(new View.OnClickListener() {
			
			@Override
			public void onClick(View v) {
				if (mPayDialogClickListener!=null) {
					mPayDialogClickListener.onClose();
				}
			}
		});
	}

	private PayDialogClickListener mPayDialogClickListener;
	
	public void setPayDialogClickListener(PayDialogClickListener payDialogClickListener) {
		this.mPayDialogClickListener=payDialogClickListener;
	}
	
	/**
	 * 当前选中的PayType
	 */
	private PayMethodItem mCurrentPayMethodItem;
	
	public PayMethodItem getCurrentPayMethodItem(){
		return mCurrentPayMethodItem;
	}
	
	private ArrayList<PayMethodItem> mPayMethodItemList;
	public void setPayMethodItemList(ArrayList<PayMethodItem> payMethodItemList){
		mPayMethodItemList=payMethodItemList;
		if (mPayMethodItemList!=null) {
			mPayMethodItemContainer.removeAllViews();
			for (PayMethodItem payMethodItem : payMethodItemList) {
				final PayMethodItemView payMethodItemView = new PayMethodItemView(mContext);
				payMethodItemView.setPayMethodItem(payMethodItem);
				
				LayoutParams layoutParams = new LinearLayout.LayoutParams(DisplayUtil.dip2px(100), DisplayUtil.dip2px(90));
				layoutParams.rightMargin=DisplayUtil.dip2px(12);
				layoutParams.leftMargin=DisplayUtil.dip2px(12);
				
				mPayMethodItemContainer.addView(payMethodItemView,layoutParams);
				
				payMethodItemView.setOnClickListener(new View.OnClickListener() {
					@Override
					public void onClick(View v) {
						mCurrentPayMethodItem = payMethodItemView.getPayMethodItem();
//						重新构造数据更新视图
						updatePayMethodItemList(mCurrentPayMethodItem);
						setPayMethodItemList(mPayMethodItemList);
						if (mPayDialogClickListener!=null) {
							mPayDialogClickListener.onPay(mCurrentPayMethodItem);
						}
					}
				});
			}
		}
	}
	
	private void updatePayMethodItemList(PayMethodItem selectMethodItem){
		if (mPayMethodItemList==null||mPayMethodItemList.size()<=0) {
			return;
		}
		for (PayMethodItem payMethodItem : mPayMethodItemList) {
			if (payMethodItem.getID()==selectMethodItem.getID()) {
				payMethodItem.setSelect(true);
			}else{
				payMethodItem.setSelect(false);
			}
		}
	}
	
	/**
	 * 更新支付信息
	 * @param ubUserInfo
	 * @param orderInfo
	 */
	public void updatePayInfoStatus(String productName,double amount){
		mLogo.setText("客服电话:400-816-9910		客服QQ:2246833903");//写死
		
		if (TextUtil.isEmpty(productName)&&amount<=0) {
			mPayOrderInfo.setVisibility(View.GONE);
		}else{
			mPayOrderInfo.setVisibility(View.VISIBLE);
			SpannableStringBuilder sb = new SpannableStringBuilder();
			
			sb.append("商品：").append(productName).append("        ");
			sb.append("价格：").append(amount+"").append("元");
			sb.setSpan(new ForegroundColorSpan(0xa01296db), 3,3+productName.length(),Spannable.SPAN_INCLUSIVE_EXCLUSIVE );
			sb.setSpan(new ForegroundColorSpan(0xa01296db), 14+productName.length(),sb.length(),Spannable.SPAN_INCLUSIVE_EXCLUSIVE );
			mPayOrderInfo.setText(sb);
		}
	}
}
