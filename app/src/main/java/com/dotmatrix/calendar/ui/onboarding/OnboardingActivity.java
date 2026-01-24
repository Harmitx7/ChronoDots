package com.dotmatrix.calendar.ui.onboarding;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.RecyclerView;
import androidx.viewpager2.widget.ViewPager2;

import com.dotmatrix.calendar.R;
import com.dotmatrix.calendar.data.preferences.AppPreferences;
import com.dotmatrix.calendar.ui.fluid.FluidCalendarActivity;

/**
 * Onboarding activity for first-time users.
 */
public class OnboardingActivity extends AppCompatActivity {

    private ViewPager2 viewPager;
    private AppPreferences preferences;

    private static final int[] TITLES = {
        R.string.transform_home_screen,
        R.string.widget_year_name,
        R.string.widget_month_name,
        R.string.widget_progress_name
    };

    private static final int[] DESCRIPTIONS = {
        R.string.welcome_desc,
        R.string.widget_year_description,
        R.string.widget_month_description,
        R.string.widget_progress_description
    };

    private static final String[] EMOJIS = {"📅", "📆", "🗓️", "📊"};

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_onboarding);

        preferences = AppPreferences.getInstance(this);
        viewPager = findViewById(R.id.view_pager);
        
        setupViewPager();
    }

    private void setupViewPager() {
        OnboardingAdapter adapter = new OnboardingAdapter();
        viewPager.setAdapter(adapter);
    }

    private void finishOnboarding() {
        preferences.setFirstLaunchCompleted();
        preferences.setOnboardingCompleted(true);
        
        startActivity(new Intent(this, FluidCalendarActivity.class));
        finish();
    }

    class OnboardingAdapter extends RecyclerView.Adapter<OnboardingAdapter.PageViewHolder> {

        @NonNull
        @Override
        public PageViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.item_onboarding_page, parent, false);
            return new PageViewHolder(view);
        }

        @Override
        public void onBindViewHolder(@NonNull PageViewHolder holder, int position) {
            holder.bind(position);
        }

        @Override
        public int getItemCount() {
            return TITLES.length;
        }

        class PageViewHolder extends RecyclerView.ViewHolder {
            TextView titleView;
            TextView descView;
            TextView imageView;

            PageViewHolder(View itemView) {
                super(itemView);
                titleView = itemView.findViewById(R.id.page_title);
                descView = itemView.findViewById(R.id.page_description);
                imageView = itemView.findViewById(R.id.page_image);
            }

            void bind(int position) {
                titleView.setText(TITLES[position]);
                descView.setText(DESCRIPTIONS[position]);
                
                // Use emoji as placeholder image
                imageView.setText(EMOJIS[position]);

                // Add button on last page
                if (position == TITLES.length - 1) {
                    Button btn = new Button(OnboardingActivity.this);
                    btn.setText(R.string.get_started);
                    btn.setOnClickListener(v -> finishOnboarding());
                    
                    ViewGroup parent = (ViewGroup) itemView;
                    parent.addView(btn);
                }
            }
        }
    }
}
