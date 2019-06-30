package com.example.criminalintent;

import android.support.v4.app.Fragment;


public class CrimeListActivity extends SingleFragmentActivity
{
    private static final String TAG = "CriminalListActivity: ";
    @Override
    public Fragment createFragment()
    {
        return new CrimeListFragment();
    }

}
