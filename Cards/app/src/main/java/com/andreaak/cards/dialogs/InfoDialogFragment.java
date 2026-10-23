package com.andreaak.cards.dialogs;

import android.app.Dialog;
import android.os.Bundle;
import android.text.Spannable;
import android.text.SpannableStringBuilder;
import android.text.style.LeadingMarginSpan;
import android.text.style.TabStopSpan;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;

import com.andreaak.cards.R;

public class InfoDialogFragment extends DialogFragment {

    private static final String ARG_INFO = "arg_info";

    public static InfoDialogFragment newInstance(String info) {
        InfoDialogFragment fragment = new InfoDialogFragment();
        Bundle args = new Bundle();
        args.putString(ARG_INFO, info != null ? info : "");
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setStyle(DialogFragment.STYLE_NO_TITLE, 0);
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.popup_design, container, false);

        TextView popupTextView = view.findViewById(R.id.textPopup);
        String info = getArguments() != null ? getArguments().getString(ARG_INFO, "") : "";

        int indentPx = (int) TypedValue.applyDimension(
                TypedValue.COMPLEX_UNIT_DIP,
                32,
                getResources().getDisplayMetrics()
        );

        SpannableStringBuilder builder = new SpannableStringBuilder();
        String[] lines = info.split("\r?\n");

        for (String line : lines) {
            int start = builder.length();
            builder.append(line).append("\n");
            int end = builder.length();

            if (line.startsWith("\t")) {
                builder.setSpan(
                        new TabStopSpan.Standard(indentPx),
                        start,
                        end,
                        Spannable.SPAN_EXCLUSIVE_EXCLUSIVE
                );

                builder.setSpan(
                        new LeadingMarginSpan.Standard(0, indentPx),
                        start,
                        end,
                        Spannable.SPAN_EXCLUSIVE_EXCLUSIVE
                );
            }
        }

        popupTextView.setText(builder);

        view.setOnClickListener(v -> dismiss());

        return view;
    }

    @Override
    public void onStart() {
        super.onStart();
        Dialog dialog = getDialog();
        if (dialog != null && dialog.getWindow() != null) {
            dialog.getWindow().setLayout(
                    WindowManager.LayoutParams.MATCH_PARENT,
                    WindowManager.LayoutParams.MATCH_PARENT
            );
        }
    }
}
