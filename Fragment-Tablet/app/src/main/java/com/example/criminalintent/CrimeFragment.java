package com.example.criminalintent;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.content.FileProvider;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;

import com.example.criminalintent.Models.Crime;
import com.example.criminalintent.Models.CrimeLab;
import com.example.criminalintent.utils.PictureUtils;

import android.text.format.DateFormat;
import android.widget.ImageButton;
import android.widget.ImageView;

import java.io.File;
import java.util.Date;
import java.util.List;
import java.util.UUID;

public class CrimeFragment extends Fragment
{

    private static final int REQUEST_CONTACT = 1;
    private static final String ARG_CRIME_ID = "crime_id";
    private static final String DIALOG_DATE = "dialog_date";
    private static final int REQUEST_DATE = 0;
    private static final int REQUEST_PHOTO = 2;

    private Crime mCrime;
    private File mPhotoFile;

    private EditText mTitleField;
    private Button mDateButton;
    private CheckBox mSolvedCheckBox;
    private Button mSaveButton;
    private Button mReportButton;
    private Button mSuspectButton;
    private ImageButton mPhotoButton;
    private ImageView mPhotoView;


    public static CrimeFragment newInstance(UUID crimeID)
    {
        Bundle args = new Bundle();
        args.putSerializable(ARG_CRIME_ID, crimeID);
        CrimeFragment fragment = new CrimeFragment();
        fragment.setArguments(args);
        return fragment;
    }

    public void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        UUID crimeID = (UUID) getArguments().getSerializable(ARG_CRIME_ID);
        mCrime = CrimeLab.get(getActivity()).getCrime(crimeID);
        setHasOptionsMenu(true);
        mPhotoFile = CrimeLab.get(getActivity()).getPhotoFile(mCrime);
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull final LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState)
    {
        View v = inflater.inflate(R.layout.fragment_crime, container, false);

        mTitleField = v.findViewById(R.id.crime_title);
        mTitleField.setText(mCrime.getTitle());
        mTitleField.addTextChangedListener(new TextWatcher()
        {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2)
            {
                //this is useless
            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2)
            {
                mCrime.setTitle(charSequence.toString());
            }

            @Override
            public void afterTextChanged(Editable editable)
            {
                //this is useless
            }
        });

        mDateButton = v.findViewById(R.id.crime_date);
        updateDate();
        mDateButton.setOnClickListener( new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                FragmentManager fragmentManager = getFragmentManager();
                DatePickerFragement dialog = DatePickerFragement.newInstance(mCrime.getmDate());
                dialog.setTargetFragment(CrimeFragment.this, REQUEST_DATE);
                dialog.show(fragmentManager, DIALOG_DATE);
            }
        });

        mSolvedCheckBox = v.findViewById(R.id.crime_solved);
        mSolvedCheckBox.setChecked(mCrime.ismSolved());
        mSolvedCheckBox.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener()
        {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean b)
            {
                mCrime.setmSolved(b);
            }
        });
        mSaveButton = v.findViewById(R.id.save_button);
        mSaveButton.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                getActivity().finish();
            }
        });

        mReportButton = (Button) v.findViewById(R.id.crime_report);
        mReportButton.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                Intent intent = new Intent(Intent.ACTION_SEND);//asking the OS what application can send something
                intent.setType("text/plain");//what TYPE application can send plain text
                intent.putExtra(Intent.EXTRA_TEXT, getCrimeReport());//tells the OS what we want the message to be
                intent.putExtra(Intent.EXTRA_SUBJECT, getString(R.string.crime_report_subject));//tells the OS what subject we want the mesage to be.
                intent = Intent.createChooser(intent, getString(R.string.send_report));//this will allow the user to choose what application they want to use
                startActivity(intent);//start the activity

            }
        });

        final Intent pickContact = new Intent(Intent.ACTION_PICK, ContactsContract.Contacts.CONTENT_URI);
        mSuspectButton = (Button) v.findViewById(R.id.suspect_button);
        mSuspectButton.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View view)
            {
                startActivityForResult(pickContact, REQUEST_CONTACT);
            }
        });

        if (mCrime.getmSuspect() != null)
            mSuspectButton.setText(mCrime.getmSuspect());

        //create a package manager to see if this device has a contact application
        //if it does not then disable the suspect button
        PackageManager packageManager = getActivity().getPackageManager();
        if (packageManager.resolveActivity(pickContact, PackageManager.MATCH_DEFAULT_ONLY) == null)
            mSuspectButton.setEnabled(false);

        mPhotoButton = (ImageButton) v.findViewById(R.id.crime_camera);
        final Intent captureImage = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        boolean canTakePhoto = mPhotoFile!= null && captureImage.resolveActivity(packageManager) != null;//check whether there is a fiel we can save a photo to and if we can take a picture witha  camera
        mPhotoButton.setEnabled(canTakePhoto);
        mPhotoButton.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View view)
            {
                Uri uri = FileProvider.getUriForFile(getActivity(), "criminalintent.FileProvider", mPhotoFile);//takes the file path and it translaltes to an uri that the camera can read
                captureImage.putExtra(MediaStore.EXTRA_OUTPUT, uri);

                List<ResolveInfo> cameraActivities = getActivity().getPackageManager().queryIntentActivities(captureImage, PackageManager.MATCH_DEFAULT_ONLY);

                for (ResolveInfo activity: cameraActivities)
                {
                    getActivity().grantUriPermission(activity.activityInfo.packageName, uri, Intent.FLAG_GRANT_PERSISTABLE_URI_PERMISSION);//grant permission to write to all files
                }

                startActivityForResult(captureImage, REQUEST_PHOTO);
            }
        });

        mPhotoView = (ImageView) v.findViewById(R.id.crime_photo);
        ViewTreeObserver observer  = mPhotoView.getViewTreeObserver();
        observer.addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener()
        {

            @Override
            public void onGlobalLayout()
            {
                updatePhotoView();
            }
        });
        return v;
    }

    @Override
    public void onPause()
    {
        super.onPause();

        CrimeLab.get(getActivity()).updateCrime(mCrime);

    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data)
    {
        if(resultCode != Activity.RESULT_OK)
        {
            return;
        }
        if(requestCode == REQUEST_DATE)
        {
            Date date = (Date) data.getSerializableExtra(DatePickerFragement.EXTRA_DATE);
            mCrime.setmDate(date);
            updateDate();

        }
        else if (requestCode == REQUEST_CONTACT && data != null)
        {
            Uri contactUri = data.getData();//used to help locate the contact
            String[] queryFields = new String[]{ContactsContract.Contacts.DISPLAY_NAME};
            Cursor c = getActivity().getContentResolver().query(contactUri, queryFields, null, null, null);

            try
            {
                if (c.getCount() == 0 )
                {
                    return;
                }
                c.moveToFirst();
                String suspect = c.getString(0);
                mSuspectButton.setText(suspect);

            }finally
            {
                c.close();
            }
        }
        else if (requestCode == REQUEST_PHOTO)
        {
            Uri uri = FileProvider.getUriForFile(getActivity(), "criminalintent.FileProvider", mPhotoFile);
            getActivity().revokeUriPermission(uri, Intent.FLAG_GRANT_WRITE_URI_PERMISSION);
            updatePhotoView();
        }
    }

    private void updateDate()
    {
        mDateButton.setText(mCrime.getmDate().toString());
    }

    private void buildAlertDialog()
    {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());

        builder.setTitle("Delete Crime");
        builder.setMessage("Are you sure you want to delete this Crime?");

        builder.setPositiveButton("YES", new DialogInterface.OnClickListener()
        {
            @Override
            public void onClick(DialogInterface dialog, int which)
            {
                dialog.dismiss();
                CrimeLab.get(getActivity()).deleteCrime(mCrime);
                getActivity().finish();
            }
        });

        builder.setNegativeButton("CANCEL", new DialogInterface.OnClickListener()
        {
            @Override
            public void onClick(DialogInterface dialog, int which)
            {
                dialog.dismiss();
                getActivity().finish();
            }
        });

        AlertDialog alertDialog = builder.create();
        alertDialog.show();
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater)
    {
        super.onCreateOptionsMenu(menu, inflater);
        inflater.inflate(R.menu.fragment_crime, menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item)
    {
        switch (item.getItemId())
        {
            case R.id.delete_crimes:
                buildAlertDialog();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    private String getCrimeReport()
    {
        String solvedString = null;

        if (mCrime.ismSolved())
            solvedString = getString(R.string.crime_report_solved);
        else
            solvedString = getString(R.string.crime_report_unsolved);

        String dateFormat = "EEE, MMM, dd";
        String dateString = DateFormat.format(dateFormat, mCrime.getmDate()).toString();

        String suspect = mCrime.getmSuspect();

        if (suspect == null)
            suspect = getString(R.string.crime_Report_no_suspect);
        else
            suspect = getString(R.string.crime_report_suspect, suspect);

        String report = getString(R.string.crime_report, mCrime.getTitle(), dateString, solvedString, suspect);

        return  report;
    }

    private void updatePhotoView()
    {
        if (mPhotoView == null || !mPhotoFile.exists())
        {
            mPhotoView.setImageDrawable(null);
        }
        else
        {
            Bitmap bitmap = PictureUtils.getScaledBitmap(mPhotoFile.getPath(), mPhotoView.getMeasuredWidth(), mPhotoView.getMeasuredHeight());
            mPhotoView.setImageBitmap(bitmap);
        }
    }
}
