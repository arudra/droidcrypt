package com.droidcrypt;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.os.Bundle;
import android.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;


/**
 * A simple {@link Fragment} subclass.
 * Use the {@link AccountDisplay#newInstance} factory method to
 * create an instance of this fragment.
 */
public class AccountDisplay extends android.support.v4.app.Fragment {
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";
    private AccountInfo accountInfo = AccountInfo.getInstance();

    private String mParam1;

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @return A new instance of fragment AccountDisplay.
     */
    public static AccountDisplay newInstance(String param1) {
        AccountDisplay fragment = new AccountDisplay();
        Bundle args = new Bundle();
        args.putString(ARG_PARAM1, param1);
        fragment.setArguments(args);
        return fragment;
    }

    public AccountDisplay() {
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            mParam1 = getArguments().getString(ARG_PARAM1);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState)
    {
        return inflater.inflate(R.layout.display_fragment, container, false);
    }

    @Override
    public void onResume()
    {
        super.onResume();

        String account = accountInfo.getName();
        String password = accountInfo.getPassword();
        if (account != null)
            ((TextView)getActivity().findViewById(R.id.account)).setText(account);
        if (password != null)
            ((TextView)getActivity().findViewById(R.id.pass)).setText(password);
    }

}
