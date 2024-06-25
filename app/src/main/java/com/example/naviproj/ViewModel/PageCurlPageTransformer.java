package com.example.naviproj.ViewModel;

import android.view.View;

import androidx.annotation.NonNull;
import androidx.viewpager2.widget.ViewPager2;

import com.example.naviproj.model.PageCurl;

public class PageCurlPageTransformer implements ViewPager2.PageTransformer {

    @Override
    public void transformPage(@NonNull View page, float position) {
        if (page instanceof PageCurl) {
            if (position > -1.0F && position < 1.0F) {
                page.setTranslationX(-position * page.getWidth());
            } else {
                page.setTranslationX(0.0F);
            }
            if (position <= 1.0F && position >= -1.0F) {
                ((PageCurl) page).setCurlFactor(position);
            }
        }
    }
}
