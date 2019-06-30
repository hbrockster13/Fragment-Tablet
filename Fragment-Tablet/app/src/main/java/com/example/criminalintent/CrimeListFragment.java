package com.example.criminalintent;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ImageView;
import android.widget.TextView;

import com.example.criminalintent.Models.Crime;
import com.example.criminalintent.Models.CrimeLab;
import com.example.criminalintent.database.CrimeDbSchema;

import java.util.List;

public class  CrimeListFragment extends Fragment
{
    private class CrimeHolder extends RecyclerView.ViewHolder implements View.OnClickListener
    {
        private Crime mCrime;
        private TextView mTitleTextView;
        private TextView mDateTextView;
        private ImageView mCrimeSolved;

        public CrimeHolder(LayoutInflater inflater, ViewGroup parent)
        {
            super(inflater.inflate(R.layout.list_item_crime, parent, false));

            itemView.setOnClickListener(this);
            mTitleTextView = (TextView) itemView.findViewById(R.id.crime_title);
            mDateTextView = (TextView) itemView.findViewById(R.id.crime_date);
            mCrimeSolved = (ImageView) itemView.findViewById(R.id.crime_solved);
        }

        public void bind(Crime crime)
        {
            mCrime = crime;
            mTitleTextView.setText(mCrime.getTitle());
            mDateTextView.setText(mCrime.getmDate().toString());
            mCrimeSolved.setVisibility(crime.ismSolved() ? View.VISIBLE: View.INVISIBLE);
        }

        @Override
        public void onClick(View v)
        {
            Log.i(TAG, "CLICKED!!!!!!!!!!!!");
            Intent intent = CrimePagerActivity.newIntent(getActivity(), mCrime.getmID());
            startActivity(intent);

        }


    }

    private class CrimeAdapter extends RecyclerView.Adapter<CrimeHolder>
    {
        private List<Crime> mCrimes;

        public CrimeAdapter(List<Crime> crimes)
        {
            mCrimes = crimes;
        }

        @NonNull
        @Override
        public CrimeHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType)
        {
            LayoutInflater layoutInflater = LayoutInflater.from(getActivity());
            return new CrimeHolder(layoutInflater, parent);
        }

        @Override
        public void onBindViewHolder(@NonNull CrimeHolder crimeHolder, int i)
        {
            Crime crime = mCrimes.get(i);
            crimeHolder.bind(crime);
        }

        public void setmCrimes(List<Crime> crimes)
        {
            mCrimes = crimes;
        }

        @Override
        public int getItemCount()
        {
            return mCrimes.size();
        }
    }//end of private classes

    private static final String TAG = "CriminalListFragment: ";
    private RecyclerView mCrimeRecyclerView;
    private CrimeAdapter mAdapter;



    @Override
    public void onCreate( Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState)
    {
        View v = inflater.inflate(R.layout.fragment_crime_list, container, false);

        mCrimeRecyclerView = (RecyclerView) v.findViewById(R.id.crime_recycler_view);
        mCrimeRecyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));

        updateUI();

        return v;
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater)
    {
        super.onCreateOptionsMenu(menu, inflater);
        inflater.inflate(R.menu.fragment_crime_list, menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item)
    {
        switch (item.getItemId())
        {
            case R.id.new_crime:
                Crime crime = new Crime();
                CrimeLab.get(getActivity()).addCrime(crime);
                Intent intent = CrimePagerActivity.newIntent(getActivity(), crime.getmID());
                startActivity(intent);
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    public void onResume()
    {
        super.onResume();
        updateUI();
    }
    private void updateUI()
    {
        CrimeLab crimeLab = CrimeLab.get(getActivity());
        List<Crime> crimes = crimeLab.getmCrimes();

        if(mAdapter == null)
        {
            mAdapter = new CrimeAdapter(crimes);
            mCrimeRecyclerView.setAdapter(mAdapter);
        }
        else
        {
            mAdapter.setmCrimes(crimes);
            mAdapter.notifyDataSetChanged();
        }
    }

}
