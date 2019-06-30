package com.example.criminalintent.Models;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.CursorWrapper;
import android.database.sqlite.SQLiteDatabase;

import com.example.criminalintent.database.CrimeBaseHelper;
import com.example.criminalintent.database.CrimeCursorWrapper;
import com.example.criminalintent.database.CrimeDbSchema;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class CrimeLab
{
    private static CrimeLab sCrimeLab;

    private Context mContext;
    private SQLiteDatabase mDataBase;


    public static CrimeLab get(Context context)
    {
        if(sCrimeLab == null)
            sCrimeLab = new CrimeLab(context);

        return sCrimeLab;
    }

    private CrimeLab(Context context)
    {
        mContext = context.getApplicationContext();

        mDataBase = new CrimeBaseHelper(mContext).getWritableDatabase();
    }

    public void addCrime(Crime c)
    {
        ContentValues values = getContentValue(c);

        mDataBase.insert(CrimeDbSchema.CrimeTable.NAME, null, values);
    }

    public List<Crime> getmCrimes()
    {
        List<Crime> crimes =  new ArrayList<Crime>();

        CrimeCursorWrapper cursorWrapper = queryCrimes(null, null);

        try
        {
            cursorWrapper.moveToFirst();
            while (!cursorWrapper.isAfterLast())
            {
                crimes.add(cursorWrapper.getCrime());
                cursorWrapper.moveToNext();
            }
        }
        finally
        {
            cursorWrapper.close();
        }

        return crimes;
    }

    public Crime getCrime(UUID id)
    {
        CrimeCursorWrapper cursor = queryCrimes(CrimeDbSchema.CrimeTable.Cols.UUID + "=?",
                new String[]{id.toString()});

        try
        {
            if (cursor.getCount() == 0)
            {
                return null;
            }

            cursor.moveToFirst();
            return cursor.getCrime();
        }
        finally
        {
            cursor.close();
        }

    }

    public void updateCrime(Crime crime)
    {
        String uuidString = crime.getmID().toString();
        ContentValues values = getContentValue(crime);

        mDataBase.update(CrimeDbSchema.CrimeTable.NAME, values,
                CrimeDbSchema.CrimeTable.Cols.UUID + " = ?",
                 new String[] {uuidString});
    }



    private static ContentValues getContentValue(Crime crime)
    {
        ContentValues values =  new ContentValues();
        values.put(CrimeDbSchema.CrimeTable.Cols.UUID, crime.getmID().toString());
        values.put(CrimeDbSchema.CrimeTable.Cols.TITLE, crime.getTitle());
        values.put(CrimeDbSchema.CrimeTable.Cols.DATE, crime.getmDate().getTime());
        values.put(CrimeDbSchema.CrimeTable.Cols.SOLVED, crime.ismSolved() ? 1 : 0);
        values.put(CrimeDbSchema.CrimeTable.Cols.SUSPECT, crime.getmSuspect());

        return values;
    }

    private CrimeCursorWrapper queryCrimes(String whereClause, String[] whereArgs)
    {
        Cursor cursor = mDataBase.query(CrimeDbSchema.CrimeTable.NAME,
                null,
                whereClause,
                whereArgs,
                null,
                null,
                null );

        return new CrimeCursorWrapper(cursor);
    }

    public void deleteCrime(Crime crime)
    {
        //ContentValues values = getContentValue(crime);
        String uuidString = crime.getmID().toString();
        mDataBase.delete(
                CrimeDbSchema.CrimeTable.NAME
                , CrimeDbSchema.CrimeTable.Cols.UUID + " = ?"
                , new String[]{uuidString});
    }

    public File getPhotoFile(Crime crime)
    {
        File fileDir = mContext.getFilesDir();
        return new File(fileDir, crime.getPhotoFileName());
    }

}//end of CrimeLab class
