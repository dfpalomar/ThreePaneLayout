package com.panels.ui;

import android.app.Activity;
import android.app.Fragment;
import android.content.res.Configuration;
import android.os.Bundle;
import android.os.Handler;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.panels.controls.ThreePaneLayout;
import com.panels.controls.ThreePaneLayout.VisibilityState;
import com.panels.R;

public class CategoriesListFragment extends Fragment implements
		OnClickListener, ThreePaneLayout.OnStateChangeListener {

	private OnCategoriesListSizeControlListener mListener;

	private ImageView mArrowSizeControl;

	private Handler mHandler = new Handler();

	public interface OnCategoriesListSizeControlListener {
		
		/**
		 * Invoked when pressing the arrow panel redirection.
		 * 
		 * <p><b>NOTA:</b> The arrow is only visible for tablets.</p>   
		 */
		void onCategoriesListSizeControlSelected();
	}

	public CategoriesListFragment() {/**/}

	public static CategoriesListFragment newInstance() {
		return new CategoriesListFragment();
	}

	@Override
	public void onAttach(Activity activity) {
		super.onAttach(activity);

		mListener = (OnCategoriesListSizeControlListener) activity;
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		
		View view = inflater.inflate(R.layout.fragment_categories_list, container, false);
		
		mArrowSizeControl = (ImageView) view.findViewById(R.id.arrowSizeControl);
		mArrowSizeControl.setOnClickListener(this);
		
		int screenOrientation = getResources().getConfiguration().orientation;
		
		if (screenOrientation == Configuration.ORIENTATION_LANDSCAPE) {
			mArrowSizeControl.setImageResource(R.drawable.ic_right);
		} else { // Configuration.ORIENTATION_PORTRAIT
			mArrowSizeControl.setImageResource(R.drawable.ic_left);
		}
		
		return view;
	}

	@Override
	public void onClick(View view) {

		if (view == mArrowSizeControl) {
			
			if (mListener != null)  mListener.onCategoriesListSizeControlSelected();
		}
	}

	@Override
	public void onBeginTransitionState(VisibilityState oldState, VisibilityState newState) { /**/ }

	@Override
	public void onNewStateVisible(final VisibilityState newState) {
		
		mHandler.postDelayed(new Runnable() {

			@Override
			public void run() {

				int screenOrientation = getResources().getConfiguration().orientation;
				
				if (screenOrientation == Configuration.ORIENTATION_LANDSCAPE) {
					
					if (newState == VisibilityState.LEFT_AND_MIDDLE_VISIBLE) {
						mArrowSizeControl.setImageResource(R.drawable.ic_right);
					} else if (newState == VisibilityState.LEFT_VISIBLE) {
						mArrowSizeControl.setImageResource(R.drawable.ic_left);
					}
		
				} else { // Configuration.ORIENTATION_PORTRAIT
					
					if (newState == VisibilityState.LEFT_VISIBLE) {
						mArrowSizeControl.setImageResource(R.drawable.ic_left);
					} else if (newState == VisibilityState.LEFT_VISIBLE) {
						mArrowSizeControl.setImageResource(R.drawable.ic_right);
					}
				}
				
			}

		}, ThreePaneLayout.ANIMATION_DURATION);
	}
}
