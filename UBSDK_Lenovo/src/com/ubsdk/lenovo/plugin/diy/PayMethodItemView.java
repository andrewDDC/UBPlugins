package com.ubsdk.lenovo.plugin.diy;


import com.umbrella.game.ubsdk.plugintype.pay.PayMethod;
import com.umbrella.game.ubsdk.plugintype.pay.diy.PayMethodItem;
import com.umbrella.game.ubsdk.utils.ResUtil;

import android.content.Context;
import android.graphics.Typeface;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

/**
 * PayMethodItem 支付方式封装控件
 * @author qingshanliao
 */
public class PayMethodItemView extends LinearLayout{

	private Context mContext;
	private LayoutInflater mInflater;
	private ImageView mPayMethodLogo;
	private TextView mPayMethodName;
	private ImageView mSelectFlag;//标记当前支付方式是否选中
	public PayMethodItemView(Context context) {
		super(context);
		this.mContext=context;
		mInflater = LayoutInflater.from(context);
		initView();
	}
	
	private void initView() {
		mPayMethodItemContainer = mInflater.inflate(ResUtil.getLayoutId(mContext,"lenovo_pay_view_paymethoditem"), this,true);
		mPayMethodLogo = (ImageView) mPayMethodItemContainer.findViewById(ResUtil.getViewID(mContext,"img_payMethodLogo"));
		mPayMethodName = (TextView) mPayMethodItemContainer.findViewById(ResUtil.getViewID(mContext, "tv_payMethodName"));
		mSelectFlag = (ImageView) mPayMethodItemContainer.findViewById(ResUtil.getViewID(mContext, "img_payMethodSelectFlag"));
		mPayMethodName .setTypeface(Typeface.defaultFromStyle(Typeface.BOLD));  
	}
	
	private PayMethodItem mPayMethodItem;
	private View mPayMethodItemContainer;
	public void setPayMethodItem(PayMethodItem payMethodItem){
		this.mPayMethodItem=payMethodItem;
		mPayMethodName.setText(payMethodItem.getName());
		if (payMethodItem.isSelect()) {
			mPayMethodItemContainer.setSelected(true);
			mSelectFlag.setSelected(true);
		}else{
			mPayMethodItemContainer.setSelected(false);
			mSelectFlag.setSelected(false);
		}
		
		if (PayMethod.ALIPAY==payMethodItem.getID()) {
			mPayMethodLogo.setImageResource(ResUtil.getDrawableId(mContext, "lenovo_pay_ic_ali_logo"));
		}else if(PayMethod.WEIXING==payMethodItem.getID()){
			mPayMethodLogo.setImageResource(ResUtil.getDrawableId(mContext, "lenovo_pay_ic_wx_logo"));
		}else if(PayMethod.QQ==payMethodItem.getID()){
			mPayMethodLogo.setImageResource(ResUtil.getDrawableId(mContext, "lenovo_pay_ic_qq_logo"));
		}
	}
	
	public PayMethodItem getPayMethodItem(){
		return mPayMethodItem;
	}
}
