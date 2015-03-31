package com.droidcrypt;

import android.os.Bundle;
import android.app.Fragment;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageView;


/**
 * A simple {@link Fragment} subclass.
 * Use the {@link Embed#newInstance} factory method to
 * create an instance of this fragment.
 */
public class Embed extends android.support.v4.app.Fragment {
    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @param param2 Parameter 2.
     * @return A new instance of fragment Embed.
     */
    // TODO: Rename and change types and number of parameters
    public static Embed newInstance(String param1, String param2) {
        Embed fragment = new Embed();
        Bundle args = new Bundle();
        args.putString(ARG_PARAM1, param1);
        args.putString(ARG_PARAM2, param2);
        fragment.setArguments(args);
        return fragment;
    }

    public Embed() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            mParam1 = getArguments().getString(ARG_PARAM1);
            mParam2 = getArguments().getString(ARG_PARAM2);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        final View view = inflater.inflate(R.layout.embed_fragment, container, false);

        EditText accountType = (EditText) view.findViewById(R.id.editText_accountType);
        /* Listeners for EditText fields */
        final EditText account = (EditText)view.findViewById(R.id.account);
        final EditText pass = (EditText)view.findViewById(R.id.password);
        final EditText confirm = (EditText)view.findViewById(R.id.editText_confirmPass);

        String accountTypeName = AccountInfo.getInstance().getAccountType();
        if (accountTypeName != null && !accountTypeName.equals("Other")) {
            accountType.setEnabled(false);
            accountType.setFocusable(false);
        }
        accountType.setText(accountTypeName);

        int len = 8;
        pass.setText(AccountInfo.getInstance().randomPassword(len));
        confirm.setText(AccountInfo.getInstance().randomPassword(len));

        account.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (count > 0)  //If field is not empty
                {
                    //make check mark green
                    ImageView imageView = (ImageView)view.findViewById(R.id.accountImage);
                    imageView.setImageResource(R.drawable.green);

                }
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });

        pass.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if(count > 0)   //If field is not empty
                {
                    //make check mark green
                    ImageView imageView = (ImageView)view.findViewById(R.id.passImage);
                    imageView.setImageResource(R.drawable.green);
                }
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });

        confirm.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if(s.toString().equals(pass.getText().toString()))  //Compare this text to original password field
                {
                    //make check mark green
                    ImageView imageView = (ImageView)view.findViewById(R.id.confirmImage);
                    imageView.setImageResource(R.drawable.green);
                }
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });

        return view;
    }

    @Override
    public void onResume() {
        super.onResume();

        getActivity().setTitle("Enter Account Info");
    }
}
