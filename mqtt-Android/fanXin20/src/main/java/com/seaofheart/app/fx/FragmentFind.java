package com.seaofheart.app.fx;





import com.seaofheart.app.MYApplication;
import com.seaofheart.app.R;
 
import com.seaofheart.app.comments.SocialMainActivity;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.View.OnClickListener;
import android.widget.Button;

public class FragmentFind  extends Fragment{
 
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		return inflater.inflate(R.layout.fragment_find, container, false);
	}

	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);
	 
		getView().findViewById(R.id.re_friends).setOnClickListener(new OnClickListener(){

            @Override
            public void onClick(View v) {
                      String userID=MYApplication.getInstance().getUserName(); 
                      if(!TextUtils.isEmpty(userID)){
                       
                          startActivity(new Intent(getActivity(),SocialMainActivity.class).putExtra("userID", userID));
                        
                      }
            }
            
            
        });
    
		}
	
	
	 
}
