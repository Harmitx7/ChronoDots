package com.dotmatrix.calendar.ui.emoji;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.dotmatrix.calendar.R;
import com.dotmatrix.calendar.data.model.EmojiRule;

import java.util.ArrayList;
import java.util.List;

public class EmojiRuleAdapter extends RecyclerView.Adapter<EmojiRuleAdapter.RuleViewHolder> {

    private final List<EmojiRule> rules = new ArrayList<>();
    private final OnRuleClickListener listener;

    public interface OnRuleClickListener {
        void onDeleteClick(EmojiRule rule);
    }

    public EmojiRuleAdapter(OnRuleClickListener listener) {
        this.listener = listener;
    }

    public void setRules(List<EmojiRule> newRules) {
        rules.clear();
        rules.addAll(newRules);
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public RuleViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_emoji_rule, parent, false);
        return new RuleViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull RuleViewHolder holder, int position) {
        holder.bind(rules.get(position));
    }

    @Override
    public int getItemCount() {
        return rules.size();
    }

    class RuleViewHolder extends RecyclerView.ViewHolder {
        TextView emojiView;
        TextView labelView;
        TextView descView;
        ImageButton deleteBtn;

        RuleViewHolder(View itemView) {
            super(itemView);
            emojiView = itemView.findViewById(R.id.emoji);
            labelView = itemView.findViewById(R.id.rule_label);
            descView = itemView.findViewById(R.id.rule_description);
            deleteBtn = itemView.findViewById(R.id.btn_delete);
        }

        void bind(EmojiRule rule) {
            emojiView.setText(rule.getEmoji());
            labelView.setText(rule.getLabel() != null && !rule.getLabel().isEmpty() 
                    ? rule.getLabel() : "Custom Rule");
            
            // Generate description based on rule type with formatted date
            String desc;
            if (rule.getStartDate() != null && rule.getStartDate() > 0) {
                java.text.SimpleDateFormat sdf = new java.text.SimpleDateFormat("MMM dd, yyyy", java.util.Locale.getDefault());
                desc = sdf.format(new java.util.Date(rule.getStartDate()));
            } else {
                desc = rule.getDescription();
            }
            descView.setText(desc);

            deleteBtn.setOnClickListener(v -> listener.onDeleteClick(rule));
        }
    }
}
