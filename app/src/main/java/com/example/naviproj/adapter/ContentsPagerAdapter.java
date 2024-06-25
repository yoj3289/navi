package com.example.naviproj.adapter;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.viewpager2.adapter.FragmentStateAdapter;

import com.example.naviproj.fragment.BoardFragment;
import com.example.naviproj.fragment.DiaryFragment;
import com.example.naviproj.fragment.HomeFragment;

import org.jetbrains.annotations.NotNull;

public class ContentsPagerAdapter extends FragmentStateAdapter {
    private int mPageCount = 3;



    public ContentsPagerAdapter(AppCompatActivity fm) {
        super(fm);
        //this.mPageCount = pageCount;
    }

    @NonNull
    @NotNull
    @Override
    public Fragment createFragment(int position) {
        switch (position) {

            case 0:
                HomeFragment homeFragment = new HomeFragment();
                return homeFragment;
            case 1:
                 BoardFragment boardFragment= new BoardFragment();
                return boardFragment;
            case 2:
                DiaryFragment diaryFragment = new DiaryFragment();
                return diaryFragment;

            default:
                return null;
        }
    }

    @Override
    public long getItemId(int position) {
        return super.getItemId(position);
    }

    @Override
    public int getItemCount() {
        return mPageCount;
    }
}
