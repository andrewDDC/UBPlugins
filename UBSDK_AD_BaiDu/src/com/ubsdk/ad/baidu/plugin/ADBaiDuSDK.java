package com.ubsdk.ad.baidu.plugin;

import java.lang.reflect.Method;

import com.duoku.alone.ssp.DuoKuAdSDK;
import com.duoku.alone.ssp.ErrorCode;
import com.duoku.alone.ssp.FastenEntity;
import com.duoku.alone.ssp.entity.ViewEntity;
import com.duoku.alone.ssp.listener.CallBackListener;
import com.duoku.alone.ssp.listener.InitListener;
import com.duoku.alone.ssp.listener.ViewClickListener;
import com.umbrella.game.ubsdk.UBSDK;
import com.umbrella.game.ubsdk.config.UBSDKConfig;
import com.umbrella.game.ubsdk.iplugin.IUBADPlugin;
import com.umbrella.game.ubsdk.listener.UBActivityListenerImpl;
import com.umbrella.game.ubsdk.pluginimpl.UBAD;
import com.umbrella.game.ubsdk.plugintype.ad.ADType;
import com.umbrella.game.ubsdk.plugintype.ad.BannerPosition;
import com.umbrella.game.ubsdk.utils.UBLogUtil;

import android.app.Activity;
import android.content.Intent;
import android.support.v4.view.ViewPager.LayoutParams;
import android.view.View;
import android.view.WindowManager;
import android.widget.FrameLayout;

public class ADBaiDuSDK implements IUBADPlugin{
	private static final String TAG=ADBaiDuSDK.class.getSimpleName();
	
	private int [] supportedADTypeArray=new int[]{ADType.AD_TYPE_BANNER,ADType.AD_TYPE_INTERSTITIAL,ADType.AD_TYPE_SPLASH,ADType.AD_TYPE_REWARDVIDEO};
	private Activity mActivity;
	private WindowManager mWM;

	private FrameLayout mBannerContainer;
	private String mBannerID;
	private int mBannerPosition=BannerPosition.TOP;//banner广告的位置

	private String mInterstitialID;
	private String mRewardVideoID;

	private ViewClickListener mBannerADListener;

	private ADBaiDuSDK(Activity activity){
		mActivity=activity;
		mWM=(WindowManager) activity.getSystemService(Activity.WINDOW_SERVICE);
	
		try {
			setActivityListener();
//			加载百度广告参数
			loadADParams();
//			初始化广告插件
			initAD();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	private void initAD(){
		UBLogUtil.logI(TAG+"----->initAD");
//		bannerAD
		mBannerContainer = new FrameLayout(mActivity);
		android.widget.FrameLayout.LayoutParams layoutParams = new FrameLayout.LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT);
		mBannerContainer.setLayoutParams(layoutParams);

		mBannerADListener = new ViewClickListener() {
				
				@Override
				public void onSuccess(String adID) {
					UBLogUtil.logI(TAG+"----->showBannerView----->onSuccess----->adID="+adID);
					UBAD.getInstance().getUBADCallback().onShow(ADType.AD_TYPE_BANNER, "Banner AD show success!");
				}
				
				@Override
				public void onFailed(int errorCode) {
					UBLogUtil.logI(TAG+"----->showBannerView----->onFailed----->errorCode:"+errorCode);
					UBAD.getInstance().getUBADCallback().onFailed(ADType.AD_TYPE_BANNER,"Banner AD show failed:errorCode="+errorCode);
				}
				
				@Override
				public void onClick(int type) {
					if (type==1) {//关闭广告
						UBLogUtil.logI(TAG+"----->onClick----->type=1,user close");
						UBAD.getInstance().getUBADCallback().onClosed(ADType.AD_TYPE_BANNER, "Banner AD user closed!");
					}else if(type==2){//点击广告
						UBLogUtil.logI(TAG+"----->onClick----->type=2,user click");
						UBAD.getInstance().getUBADCallback().onClick(ADType.AD_TYPE_BANNER, "Banner AD user click!");
					}
				}
			};
		
//		InterstitialAD
		mInterstitialAdListener = new ViewClickListener() {
						
				@Override
				public void onSuccess(String adID) {
					UBLogUtil.logI(TAG+"----->showInterstitial----->onSuccess----->adID="+adID);
					UBAD.getInstance().getUBADCallback().onShow(ADType.AD_TYPE_INTERSTITIAL, "Interstitial AD show success!");
				}
				
				@Override
				public void onFailed(int errorCode) {
					UBLogUtil.logI(TAG+"----->showFullScreen----->errorCode="+errorCode);
					UBAD.getInstance().getUBADCallback().onFailed(ADType.AD_TYPE_INTERSTITIAL, "Interstitial AD show failed:errorCode="+errorCode);
				}
				
				@Override
				public void onClick(int type) {
					if (type==1) {//用户关闭
						UBLogUtil.logI(TAG+"----->onClick----->type=1,user close");
						UBAD.getInstance().getUBADCallback().onClosed(ADType.AD_TYPE_INTERSTITIAL, "Interstitial AD　user closed!");
					}else if(type==2){//用户点击
						UBLogUtil.logI(TAG+"----->onClick----->type=2,user click");
						UBAD.getInstance().getUBADCallback().onClick(ADType.AD_TYPE_INTERSTITIAL, "Interstitial AD user click!");
					}
				}
			};
			
//			RewardVideoAD
			mRewardVideoADListener = new CallBackListener() {
				
				@Override
				public void onReady() {
					UBLogUtil.logI(TAG+"----->showVideoAD----->onReady");
					 DuoKuAdSDK.getInstance().showVideoImmediate(mActivity,mRewardVideoEntity);
				}
				
				@Override
				public void onFailMsg(String msg) {
					UBLogUtil.logI(TAG+"----->showVideoAD----->onFailMsg----->msg="+msg);
					UBAD.getInstance().getUBADCallback().onFailed(ADType.AD_TYPE_REWARDVIDEO, "RewardVideo AD show failed:msg="+msg);
				}
				
				@Override
				public void onComplete() {
					UBLogUtil.logI(TAG+"----->showVideoAD----->onComplete");
					UBAD.getInstance().getUBADCallback().onComplete(ADType.AD_TYPE_REWARDVIDEO, "RewardVideo AD complete");
				}
				
				@Override
				public void onClick(int type) {
					if (type==1) {
						UBLogUtil.logI(TAG+"----->showVideoAD----->onClick----->type=1,user close");
						UBAD.getInstance().getUBADCallback().onClosed(ADType.AD_TYPE_REWARDVIDEO,"RewardVideo AD　user closed!");
					}else if(type==2){
						UBLogUtil.logI(TAG+"----->showVideoAD----->onClick----->type=2,user click");
						UBAD.getInstance().getUBADCallback().onClick(ADType.AD_TYPE_REWARDVIDEO, "RewardVideo AD user click!");
					}
				}
			};
				
//		初始化百度广告
		DuoKuAdSDK.getInstance().init(mActivity, new InitListener() {
			@Override
			public void onBack(int code, String desc) {
				if (ErrorCode.SUCCESS_CODE==code) {//初始化成功
					UBLogUtil.logI(TAG+"----->BaiDu AD init success!");
				}else{//初始化失败
					UBLogUtil.logI(TAG+"----->BaiDu AD init failed!");
				}
			}
		});
	}
	
	/**
	 * 加载百度广告参数
	 */
	private void loadADParams(){
		UBLogUtil.logI(TAG+"----->loadADParams");
		mBannerID = UBSDKConfig.getInstance().getParamMap().get("AD_BaiDu_Banner_ID");
		mInterstitialID = UBSDKConfig.getInstance().getParamMap().get("AD_BaiDu_Interstitial_ID");
		mRewardVideoID = UBSDKConfig.getInstance().getParamMap().get("AD_BaiDu_RewardVideo_ID");
		mBannerPosition = Integer.parseInt(UBSDKConfig.getInstance().getParamMap().get("AD_BaiDu_Banner_Position"));
	}
	
	private void setActivityListener(){
		UBLogUtil.logI(TAG+"----->setActivityListener");
		UBSDK.getInstance().setUBActivityListener(new UBActivityListenerImpl(){
			@Override
			public void onDestroy() {
				UBLogUtil.logI(TAG+"----->onDestory");
//		        DuoKuAdSDK.getInstance().onDestoryBanner();
		        DuoKuAdSDK.getInstance().onDestoryBlock();
		        DuoKuAdSDK.getInstance().onDestoryVideo();
		        mWM=null;
		        mBannerContainer=null;
				super.onDestroy();
			}
		});
	}

	@Override
	public boolean isSupportMethod(String methodName,Object[] args) {
        UBLogUtil.logI(TAG+"----->isSupportMethod");
        Class<?> [] parameterTypes=null;
        if (args!=null&&args.length>0) {
        	parameterTypes=new Class<?>[args.length];
			for(int i=0;i<args.length;i++){
				parameterTypes[i]=args[i].getClass();
			}
		}
        
        try {
			Method method = getClass().getDeclaredMethod(methodName, parameterTypes);
			return method==null?false:true;
		} catch (NoSuchMethodException e) {
			e.printStackTrace();
		}
		return false;
	}

	@Override
	public Object callMethod(String methodName, Object[] args) {
		UBLogUtil.logI(TAG+"----->callMethod");
		Class<?>[] paramterTypes=null;
		if (args!=null&&args.length>0) {
			paramterTypes=new Class<?>[args.length];
			for (int i=0;i<args.length;i++) {
				paramterTypes[i]=args[i].getClass();
			}
		}
		
		try {
			Method method = getClass().getDeclaredMethod(methodName, paramterTypes);
			return method.invoke(this, args);
		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;
	}

	@Override
	public boolean isSupportADType(int adType) {
		UBLogUtil.logI(TAG+"----->isSupportADType");
		if (supportedADTypeArray!=null&&supportedADTypeArray.length>0) {
			for (int i : supportedADTypeArray) {
				if (i==adType) {
					return true;
				}
			}
		}
		return false;
	}

	@Override
	public void showADWithADType(int adType) {
		UBLogUtil.logI(TAG+"----->showADWithADType");
		hideADWithADType(adType);//显示之前先隐藏广告
		switch (adType) {
		case ADType.AD_TYPE_BANNER:
			showBannerAD();
			break;
		case ADType.AD_TYPE_INTERSTITIAL:
			showInterstitialAD();
			break;
		case ADType.AD_TYPE_REWARDVIDEO:
			showVideoAD();
			break;
		case ADType.AD_TYPE_SPLASH:
			showSplashAD();
			break;
		default:
			break;
		}
	}
	
	/**
	 * 展示闪屏广告
	 */
	private void showSplashAD() {
		UBLogUtil.logI(TAG+"----->showSplashAD");
		mActivity.startActivity(new Intent(mActivity,ADBaiDuSplashActivity.class));
	}

	/**
	 *  展示激励视频广告
	 */
	private void showVideoAD() {
		UBLogUtil.logI(TAG+"----->showVideoAD");
		if (mRewardVideoEntity==null) {
			mRewardVideoEntity = new ViewEntity();
			mRewardVideoEntity.setType(FastenEntity.VIEW_VIDEO);
			mRewardVideoEntity.setDirection(FastenEntity.VIEW_VERTICAL);
			mRewardVideoEntity.setSeatId(Integer.parseInt(mRewardVideoID));
		}
		DuoKuAdSDK.getInstance().cacheVideo(mActivity, mRewardVideoEntity, mRewardVideoADListener);
	}

	/**
	 * 展示插屏广告
	 */
	private void showInterstitialAD() {
		UBLogUtil.logI(TAG+"----->showInterstitialAD");
		ViewEntity viewEntity = new ViewEntity();
		viewEntity.setType(FastenEntity.VIEW_BLOCK);
		viewEntity.setDirection(FastenEntity.VIEW_HORIZONTAL);
		viewEntity.setSeatId(Integer.parseInt(mInterstitialID));
		DuoKuAdSDK.getInstance().showBlockView(mActivity, viewEntity, mInterstitialAdListener);
	}

	private boolean isBannerInit=false;//banner广告是否初始化

	private ViewClickListener mInterstitialAdListener;

	private CallBackListener mRewardVideoADListener;

	private ViewEntity mRewardVideoEntity;
	/**
	 * 显示Banner广告
	 */
	private void showBannerAD(){
		UBLogUtil.logI(TAG+"----->showBannerAD");
		
		if (!isBannerInit) {
//			ADHelper.addBannerView(mWM, mBannerContainer,mBannerPosition);//只添加一次
			ViewEntity viewEntity = new ViewEntity();
			viewEntity.setType(FastenEntity.VIEW_BANNER);//banner 类型
			viewEntity.setDirection(FastenEntity.VIEW_HORIZONTAL);//展示方向
			
			if (BannerPosition.TOP==mBannerPosition) {
				viewEntity.setPostion(FastenEntity.POSTION_TOP);//展示位置
			}else if(BannerPosition.BOTTOM==mBannerPosition){
				viewEntity.setPostion(FastenEntity.POSTION_BOTTOM);//展示位置
			}else{
				viewEntity.setPostion(FastenEntity.POSTION_TOP);//展示位置
			}
			
			viewEntity.setSeatId(Integer.parseInt(mBannerID));//广告位id
			
			DuoKuAdSDK.getInstance().showBannerView(mActivity, viewEntity, mBannerContainer,mBannerADListener);
			
			isBannerInit=true;
		}
		
		mBannerContainer.setVisibility(View.VISIBLE);
	}

	@Override
	public void hideADWithADType(int adType) {
		UBLogUtil.logI(TAG+"----->hideADWithADType");
		
		switch (adType) {
		case ADType.AD_TYPE_BANNER:
			hideBannerAD();
			break;
		case ADType.AD_TYPE_INTERSTITIAL:
			hideInterstitialAD();
			break;
		case ADType.AD_TYPE_REWARDVIDEO:
			hideRewardVideoAD();
			break;
		case ADType.AD_TYPE_SPLASH:
			hideSplashAD();
			break;
		default:
			break;
		}
	}
	private void hideInterstitialAD() {
		UBLogUtil.logI(TAG+"----->hideInterstitialAD");
	}
	
	private void hideSplashAD() {
		UBLogUtil.logI(TAG+"----->hideSplashAD");
	}
	
	private void hideRewardVideoAD() {
		UBLogUtil.logI(TAG+"----->hideRewardVideoAD");
	}
	
	/**
	 * 隐藏Banner广告
	 */
	private void hideBannerAD(){
		UBLogUtil.logI(TAG+"----->hideBannerAD");
		mBannerContainer.setVisibility(View.GONE);
//		DuoKuAdSDK.getInstance().hideBannerView(mActivity, mBannerContainer);
		
/*		DuoKuAdSDK.getInstance().hideBannerView(mActivity, mBannerContainer);
		
        if (null != mBannerContainer) {
        	mBannerContainer.removeAllViews();
            if (null != mWM) {
                mWM.removeView(mBannerContainer);
            }
            mBannerContainer.destroyDrawingCache();
            mBannerContainer = null;
        }*/
	}
}
