package io.github.luizgrp.sectionedrecyclerviewadapter.demo;

import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.ads.AdListener;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdSize;
import com.google.android.gms.ads.AdView;

import java.util.ArrayList;
import java.util.List;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import io.github.luizgrp.sectionedrecyclerviewadapter.SectionParameters;
import io.github.luizgrp.sectionedrecyclerviewadapter.SectionedRecyclerViewAdapter;
import io.github.luizgrp.sectionedrecyclerviewadapter.StatelessSection;

public class Example9Fragment extends Fragment {


    private SectionedRecyclerViewAdapter sectionAdapter;
    private List<AdView> adViewList = new ArrayList<>();

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_ex9, container, false);

        sectionAdapter = new SectionedRecyclerViewAdapter();
        adViewList.clear();

        for (char alphabet = 'A'; alphabet <= 'Z'; alphabet++) {
            List<String> contacts = getContactsWithLetter(alphabet);

            if (contacts.size() > 0) {
                ContactsSection section = new ContactsSection(String.valueOf(alphabet), contacts, getContext());
                sectionAdapter.addSection(section);
                AdView adView = section.getAdView();
                if (adView != null)
                    adViewList.add(adView);
            }
        }

        loadBannerAd(0);

        RecyclerView recyclerView = view.findViewById(R.id.recyclerview);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        recyclerView.setAdapter(sectionAdapter);

        return view;

    }


    @Override
    public void onPause() {
        for (AdView item : adViewList) {
            item.pause();
        }
        super.onPause();
    }

    @Override
    public void onDestroy() {
        for (AdView item : adViewList) {
            item.destroy();
        }
        super.onDestroy();
    }

    private void loadBannerAd(final int index) {

        if (index >= adViewList.size()) {
            return;
        }


        final AdView adView = adViewList.get(index);

        // Set an AdListener on the AdView to wait for the previous banner ad
        // to finish loading before loading the next ad in the items list.
        adView.setAdListener(new AdListener() {
            @Override
            public void onAdLoaded() {
                super.onAdLoaded();
                // The previous banner ad loaded successfully, call this method again to
                // load the next ad in the items list.
                loadBannerAd(index + 1);
            }

            @Override
            public void onAdFailedToLoad(int errorCode) {
                // The previous banner ad failed to load. Call this method again to load
                // the next ad in the items list
                loadBannerAd(index + 1);
            }
        });

        // Load the banner ad.
        adView.loadAd(new AdRequest.Builder().build());
    }

    @Override
    public void onResume() {

        for (AdView item : adViewList) {
            item.resume();
        }

        if (getActivity() instanceof AppCompatActivity) {
            AppCompatActivity activity = ((AppCompatActivity) getActivity());
            if (activity.getSupportActionBar() != null)
                activity.getSupportActionBar().setTitle(R.string.nav_example9);
        }

        super.onResume();
    }

    private List<String> getContactsWithLetter(char letter) {
        List<String> contacts = new ArrayList<>();

        for (String contact : getResources().getStringArray(R.array.names)) {
            if (contact.charAt(0) == letter) {
                contacts.add(contact);
            }
        }

        return contacts;
    }


    private class ContactsSection extends StatelessSection {

        private static final String AD_UNIT_ID = "ca-app-pub-3940256099942544/6300978111";
        private final Context context;
        String title;
        List<String> list;
        private AdView adView;

        ContactsSection(String title, List<String> list, Context context) {
            super(SectionParameters.builder()
                    .itemResourceId(R.layout.section_ex1_item)
                    .advertResourceId(R.layout.banner_ad_container)
                    //.footerResourceId(R.layout.section_ex3_footer)
                    .headerResourceId(R.layout.section_ex1_header)
                    .build());


            this.context = context;
            this.title = title;
            this.list = list;

            if (getMinimumItemsPerAd() > list.size()) {
                setHasAdvert(false);
            } else {
                adView = new AdView(context);
                adView.setAdSize(AdSize.BANNER);
                adView.setAdUnitId(AD_UNIT_ID);
            }
        }

        @Override
        public int getContentItemsTotal() {
            return list.size();
        }

        @Override
        public RecyclerView.ViewHolder getItemViewHolder(View view) {
            return new ItemViewHolder(view);
        }

        @Override
        public RecyclerView.ViewHolder getAdvertViewHolder(View view) {
            return new AdViewHolder(view);
        }

        @Override
        public void onBindItemViewHolder(RecyclerView.ViewHolder holder, int position) {
            final ItemViewHolder itemHolder = (ItemViewHolder) holder;
            String name = list.get(position);
            itemHolder.tvItem.setText(name);
            itemHolder.imgItem.setImageResource(position % 2 == 0 ? R.drawable.ic_face_black_48dp : R.drawable.ic_tag_faces_black_48dp);
            itemHolder.rootView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Toast.makeText(getContext(), String.format("Clicked on position #%s of Section %s, VH1", sectionAdapter.getPositionInSection(itemHolder.getAdapterPosition()), title), Toast.LENGTH_SHORT).show();
                }
            });

        }

        public AdView getAdView() {
            return adView;
        }

        @Override
        public void onBindAdvertViewHolder(RecyclerView.ViewHolder holder) {
            AdViewHolder bannerHolder = (AdViewHolder) holder;
            ViewGroup adCardView = (ViewGroup) bannerHolder.itemView;

            if (adCardView.getChildCount() > 0) {
                adCardView.removeAllViews();
            }

            if (adView.getParent() != null) {
                ((ViewGroup) adView.getParent()).removeView(adView);
            }

            // Add the banner ad to the ad view.
            adCardView.addView(adView);
        }

        @Override
        public RecyclerView.ViewHolder getHeaderViewHolder(View view) {
            return new HeaderViewHolder(view);
        }

        @Override
        public void onBindHeaderViewHolder(RecyclerView.ViewHolder holder) {
            HeaderViewHolder headerHolder = (HeaderViewHolder) holder;
            headerHolder.tvTitle.setText(title);
        }

        @Override
        public RecyclerView.ViewHolder getFooterViewHolder(View view) {
            return new FooterViewHolder(view);
        }

        @Override
        public void onBindFooterViewHolder(RecyclerView.ViewHolder holder) {
            FooterViewHolder footerHolder = (FooterViewHolder) holder;
            footerHolder.rootView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Toast.makeText(getContext(), String.format("Clicked on footer of Section %s", title), Toast.LENGTH_SHORT).show();
                }
            });
        }
    }

    private class HeaderViewHolder extends RecyclerView.ViewHolder {

        private final TextView tvTitle;

        HeaderViewHolder(View view) {
            super(view);

            tvTitle = view.findViewById(R.id.tvTitle);
        }
    }

    public class ItemViewHolder extends RecyclerView.ViewHolder {

        private final View rootView;
        private final ImageView imgItem;
        private final TextView tvItem;

        ItemViewHolder(View view) {
            super(view);

            rootView = view;
            imgItem = view.findViewById(R.id.imgItem);
            tvItem = view.findViewById(R.id.tvItem);
        }
    }

    public class AdViewHolder extends RecyclerView.ViewHolder {

        AdViewHolder(View view) {
            super(view);
        }
    }

    private class FooterViewHolder extends RecyclerView.ViewHolder {

        private final View rootView;

        FooterViewHolder(View view) {
            super(view);

            rootView = view;
        }
    }

}
