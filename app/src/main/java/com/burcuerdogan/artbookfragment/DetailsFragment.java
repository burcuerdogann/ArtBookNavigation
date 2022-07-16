package com.burcuerdogan.artbookfragment;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.ImageDecoder;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.activity.result.ActivityResult;
import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.navigation.NavDirections;
import androidx.navigation.Navigation;
import androidx.room.Room;

import com.burcuerdogan.artbookfragment.databinding.FragmentDetailsBinding;
import com.google.android.material.snackbar.Snackbar;

import java.io.ByteArrayOutputStream;
import java.io.IOException;

import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers;
import io.reactivex.rxjava3.disposables.CompositeDisposable;
import io.reactivex.rxjava3.schedulers.Schedulers;


public class DetailsFragment extends Fragment {

    SQLiteDatabase database;
    String info = "";
    Bitmap selectImage;
    ActivityResultLauncher<Intent> activityResultLauncher;
    ActivityResultLauncher<String> permissionLauncher;
    private FragmentDetailsBinding binding;
    private final CompositeDisposable mDisposible = new CompositeDisposable();
    ArtDatabase artDatabase;
    ArtDao artDao;
    Art artFromMain;

    public DetailsFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        registerLauncher();

        artDatabase = Room.databaseBuilder(requireContext(),ArtDatabase.class,"Arts").build();
        artDao = artDatabase.artDao();

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        binding = FragmentDetailsBinding.inflate(inflater, container, false);
        View view = binding.getRoot();
        return view;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        database = requireActivity().openOrCreateDatabase("Arts", Context.MODE_PRIVATE,null);

        if (getArguments() != null) {
            info = DetailsFragmentArgs.fromBundle(getArguments()).getInfo();
        } else {
            info = "new";
        }

        binding.saveButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                save(v);
            }
        });

        binding.imageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                selectImage(v);
            }
        });

        binding.deleteButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                delete(v);
            }
        });

        if (info.equals("new")) {
            binding.artNameText.setText("");
            binding.artistNameText.setText("");
            binding.yearText.setText("");
            binding.saveButton.setVisibility(View.VISIBLE);
            binding.deleteButton.setVisibility(View.GONE);
            binding.imageView.setImageResource(R.drawable.selectimage);
        } else {
            int artId = DetailsFragmentArgs.fromBundle(getArguments()).getArtId();
            binding.saveButton.setVisibility(View.GONE);
            binding.deleteButton.setVisibility(View.VISIBLE);

            mDisposible.add(artDao.getArtById(artId)
                    .subscribeOn(Schedulers.io())
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe(DetailsFragment.this::handleResponseWithOldArt));
        }

    }

    private void handleResponseWithOldArt(Art art) {
        artFromMain = art;
        binding.artNameText.setText(art.artname);
        binding.artistNameText.setText(art.artistName);
        binding.yearText.setText(art.year);

        Bitmap bitmap = BitmapFactory.decodeByteArray(art.image,0,art.image.length);
        binding.imageView.setImageBitmap(bitmap);
    }

    public void selectImage(View view) {
        if (ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED){
            if (ActivityCompat.shouldShowRequestPermissionRationale(requireActivity(),Manifest.permission.READ_EXTERNAL_STORAGE)){
                Snackbar.make(view,"Permission needed for gallery", Snackbar.LENGTH_INDEFINITE).setAction("Give Permission", new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        permissionLauncher.launch(Manifest.permission.READ_EXTERNAL_STORAGE);
                    }
                }).show();
            } else {
                permissionLauncher.launch(Manifest.permission.READ_EXTERNAL_STORAGE);
            }
        } else {
            Intent intentToGallery = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
            activityResultLauncher.launch(intentToGallery);
        }
    }

    private void handleResponse() {
        NavDirections action = DetailsFragmentDirections.actionDetailsFragmentToListFragment();
        Navigation.findNavController(requireView()).navigate(action);
    }

    public Bitmap makeSmallerImage(Bitmap image, int maximumSize) {
        int width = image.getWidth();
        int height = image.getHeight();
        float bitmapRatio = (float) width / (float) height;

        if (bitmapRatio > 1) {
            width = maximumSize;
            height = (int) (width / bitmapRatio);
        } else {
            height = maximumSize;
            width = (int) (height * bitmapRatio);
        }
        return Bitmap.createScaledBitmap(image,width,height,true);
    }

    public void save(View view) {

        String artName = binding.artNameText.getText().toString();
        String artistName = binding.artistNameText.getText().toString();
        String year = binding.yearText.getText().toString();

        Bitmap smallImage = makeSmallerImage(selectImage,300);

        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        smallImage.compress(Bitmap.CompressFormat.PNG,50,outputStream);
        byte[] byteArray = outputStream.toByteArray();

        Art art = new Art(artName,artistName,year,byteArray);

        mDisposible.add(artDao.insert(art)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(DetailsFragment.this::handleResponse));
    }

    public void delete(View view) {
        mDisposible.add(artDao.delete(artFromMain)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(DetailsFragment.this::handleResponse));
    }

    //REGISTERLAUNCHER

    public void registerLauncher() {
        activityResultLauncher = registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), new ActivityResultCallback<ActivityResult>() {
            @Override
            public void onActivityResult(ActivityResult result) {
                if (result.getResultCode() == Activity.RESULT_OK) {
                    Intent intentFromResult = result.getData();
                    if (intentFromResult != null) {
                        Uri imageData = intentFromResult.getData();
                        try {

                            if (Build.VERSION.SDK_INT >= 28) {
                                ImageDecoder.Source source = ImageDecoder.createSource(requireActivity().getContentResolver(),imageData);
                                selectImage = ImageDecoder.decodeBitmap(source);
                                binding.imageView.setImageBitmap(selectImage);
                            } else {
                                selectImage = MediaStore.Images.Media.getBitmap(requireActivity().getContentResolver(),imageData);
                                binding.imageView.setImageBitmap(selectImage);
                            }

                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }
                }
            }
        });

        permissionLauncher = registerForActivityResult(new ActivityResultContracts.RequestPermission(), new ActivityResultCallback<Boolean>() {
            @Override
            public void onActivityResult(Boolean result) {
                if (result) {
                    //Permission Granted
                    Intent intentToGallery = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                    //startActivityForResult(intentToGallery,2);
                    activityResultLauncher.launch(intentToGallery);
                } else {
                    //Permission Denied
                    Toast.makeText(requireActivity(),"Permission needed!", Toast.LENGTH_LONG).show();
                }
            }
        });
    }
    

    public void onDestroyView(){
        super.onDestroyView();
        binding = null;
        mDisposible.clear();
    }



}