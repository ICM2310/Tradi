package com.example.traddiapp;

import android.content.Context;
import android.view.ActionProvider;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.CheckBox;

public class CheckboxActionProvider extends ActionProvider {
    private CheckboxClickListener mListener;
    private CheckBox checkBox;
    private Context mContext;

    public CheckboxActionProvider(Context context) {
        super(context);
        mContext = context;
    }

    @Override
    public View onCreateActionView() {
        LayoutInflater layoutInflater = LayoutInflater.from(mContext);
        View view = layoutInflater.inflate(R.layout.checkbox_layout, null);
        checkBox = view.findViewById(R.id.checkBox);

        checkBox.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                boolean isChecked = checkBox.isChecked();
                if (mListener != null) {
                    mListener.onCheckboxClicked(isChecked);
                }
            }
        });

        return view;
    }

    public void setCheckboxClickListener(CheckboxClickListener listener) {
        mListener = listener;
    }

    public interface CheckboxClickListener {
        void onCheckboxClicked(boolean isChecked);
    }
}


