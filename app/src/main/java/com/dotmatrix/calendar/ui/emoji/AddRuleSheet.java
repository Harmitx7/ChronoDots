package com.dotmatrix.calendar.ui.emoji;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.dotmatrix.calendar.R;
import com.dotmatrix.calendar.data.model.EmojiRule;
import com.google.android.material.bottomsheet.BottomSheetDialogFragment;
import com.google.android.material.datepicker.MaterialDatePicker;
import com.google.android.material.textfield.TextInputEditText;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class AddRuleSheet extends BottomSheetDialogFragment {

    private OnRuleCreatedListener listener;
    private long selectedDate = System.currentTimeMillis();

    public interface OnRuleCreatedListener {
        void onRuleCreated(EmojiRule rule);
    }

    public void setListener(OnRuleCreatedListener listener) {
        this.listener = listener;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_add_rule, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        TextInputEditText inputEmoji = view.findViewById(R.id.input_emoji);
        TextInputEditText inputLabel = view.findViewById(R.id.input_label);
        TextInputEditText inputDate = view.findViewById(R.id.input_date);
        Button btnSave = view.findViewById(R.id.btn_save);

        // Date Picker
        SimpleDateFormat sdf = new SimpleDateFormat("MMM dd, yyyy", Locale.getDefault());
        inputDate.setText(sdf.format(new Date(selectedDate)));

        inputDate.setOnClickListener(v -> {
            MaterialDatePicker<Long> picker = MaterialDatePicker.Builder.datePicker()
                    .setTitleText("Select Date")
                    .setSelection(selectedDate)
                    .build();

            picker.addOnPositiveButtonClickListener(selection -> {
                selectedDate = selection;
                inputDate.setText(sdf.format(new Date(selectedDate)));
            });

            picker.show(getParentFragmentManager(), "DATE_PICKER");
        });

        btnSave.setOnClickListener(v -> {
            String emoji = inputEmoji.getText() != null ? inputEmoji.getText().toString() : "";
            String label = inputLabel.getText() != null ? inputLabel.getText().toString() : "";

            if (!emoji.isEmpty()) {
                // Create a basic specific date rule for now (MVP)
                // Ideally this would have date pickers
                EmojiRule rule = new EmojiRule();
                rule.setEmoji(emoji);
                rule.setLabel(label);
                rule.setStartDate(selectedDate);
                
                if (listener != null) {
                    listener.onRuleCreated(rule);
                }
                dismiss();
            }
        });
    }
}
