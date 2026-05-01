package com.example.datemaker.fragment;

import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.GridLayoutManager;

import com.example.datemaker.R;
import com.example.datemaker.adapter.ProgramsAdapter;
import com.example.datemaker.databinding.FragmentOwnProgramsBinding;
import com.example.datemaker.model.ProgramItem;
import com.example.datemaker.retrofit.ApiService;
import com.example.datemaker.retrofit.RetrofitClient;
import com.example.datemaker.utils.UserSessionManager;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import okhttp3.MediaType;
import okhttp3.RequestBody;
import okhttp3.MultipartBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class OwnProgramsFragment extends Fragment {
    private FragmentOwnProgramsBinding binding;
    private ProgramsAdapter adapter;
    private final List<ProgramItem> items = new ArrayList<>();
    private ActivityResultLauncher<String> imagePickerLauncher;
    private int currentAddPosition = -1;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        binding = FragmentOwnProgramsBinding.inflate(inflater, container, false);

        items.add(new ProgramItem(true));

        adapter = new ProgramsAdapter(items, position -> {
            currentAddPosition = position;
            imagePickerLauncher.launch("image/*");
        }, position -> showBoxOptionsDialog(position));

        GridLayoutManager gridLayoutManager = new GridLayoutManager(getContext(), 2);
        binding.boxRecyclerView.setLayoutManager(gridLayoutManager);
        binding.boxRecyclerView.setAdapter(adapter);

        imagePickerLauncher = registerForActivityResult(
                new ActivityResultContracts.GetContent(),
                uri -> {
                    if (uri != null && currentAddPosition >= 0) {
                        String caption = "Was here at " + new SimpleDateFormat("MMMM dd", Locale.getDefault()).format(new Date());
                        items.set(currentAddPosition, new ProgramItem(uri, caption));
                        adapter.notifyItemChanged(currentAddPosition);
                        items.add(new ProgramItem(true));
                        adapter.notifyItemInserted(items.size() - 1);

                        binding.boxRecyclerView.scrollToPosition(items.size() - 1);

                        UserSessionManager session = new UserSessionManager(requireContext());
                        long userId = session.getUserId();
                        long partnerId = session.getPartnerId();
                        uploadImageToServer(userId, partnerId, items.get(currentAddPosition));
                    }
                }
        );

        loadSharedImages();

        return binding.getRoot();
    }

    private void showBoxOptionsDialog(int position) {
        AlertDialog dialog = new MaterialAlertDialogBuilder(requireContext())
                .setTitle("Choose an option.")
                .setMessage("Do you want to delete this box?")
                .setPositiveButton("Delete box", (d, which) -> {
                    deleteBox(position);
                    d.dismiss();
                })
                .setNegativeButton("Cancel", (d, which) -> {
                    d.dismiss();
                })
                .create();

        dialog.show();

        dialog.getButton(AlertDialog.BUTTON_POSITIVE)
                .setTextColor(ContextCompat.getColor(requireContext(), R.color.gray));
        dialog.getButton(AlertDialog.BUTTON_NEGATIVE)
                .setTextColor(ContextCompat.getColor(requireContext(), R.color.gray));
    }

    private void deleteBox(int position) {
        if (position >= 0 && position < items.size()) {
            ProgramItem item = items.get(position);

            if (!item.isPlaceHolder() && item.getId() >= 0) {
                deleteImageFromServer(item.getId());
            }

            items.remove(position);
            adapter.notifyItemRemoved(position);
            adapter.notifyItemRangeChanged(position, items.size());

            if (items.isEmpty() || !items.get(items.size() - 1).isPlaceHolder()) {
                items.add(new ProgramItem(true));
                adapter.notifyItemInserted(items.size() - 1);
            }
        }
    }

    private void uploadImageToServer(long userId, long partnerId, ProgramItem item) {
        try {
            Uri imageUri = item.getImageUri();
            File file = createTempFileFromUri(imageUri);
            if (file == null || !file.exists()) {
                Toast.makeText(requireContext(), "Could not read image file", Toast.LENGTH_SHORT).show();
                return;
            }

            RequestBody requestFile = RequestBody.create(file, MediaType.parse("image/*"));
            MultipartBody.Part body = MultipartBody.Part.createFormData("image", file.getName(), requestFile);

            ApiService apiService = RetrofitClient.getRetrofitInstance(requireContext()).create(ApiService.class);

            Call<Void> call = apiService.uploadImage(
                    String.valueOf(userId),
                    String.valueOf(partnerId),
                    item.getCaption(),
                    body
            );

            call.enqueue(new Callback<Void>() {
                @Override
                public void onResponse(Call<Void> call, Response<Void> response) {
                    if (response.isSuccessful()) {
                        Toast.makeText(requireContext(), "Image uploaded successfully", Toast.LENGTH_SHORT).show();
                        // IMPORTANT: fetch again so we get the real ID from server
                        loadSharedImages();
                    } else {
                        Toast.makeText(requireContext(), "Image upload failed: " + response.code(), Toast.LENGTH_SHORT).show();
                    }
                }

                @Override
                public void onFailure(Call<Void> call, Throwable t) {
                    Toast.makeText(requireContext(), "Error: " + t.getMessage(), Toast.LENGTH_SHORT).show();
                }
            });

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private String getRealPathFromUri(Uri uri) {
        File file = new File(requireContext().getCacheDir(), "temp_upload.jpg");
        try (var inputStream = requireContext().getContentResolver().openInputStream(uri);
             var outputStream = requireContext().openFileOutput("temp_upload.jpg", 0)) {
            if (inputStream != null) {
                byte[] buffer = new byte[4096];
                int len;
                while ((len = inputStream.read(buffer)) > 0) {
                    outputStream.write(buffer, 0, len);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return file.getAbsolutePath();
    }

    private File createTempFileFromUri(Uri uri) throws IOException {
        File tempFile = new File(requireContext().getCacheDir(), "temp_upload.jpg");

        try (java.io.InputStream inputStream = requireContext().getContentResolver().openInputStream(uri);
             java.io.OutputStream outputStream = new java.io.FileOutputStream(tempFile)) {

            byte[] buffer = new byte[4096];
            int bytesRead;
            while ((bytesRead = inputStream.read(buffer)) != -1) {
                outputStream.write(buffer, 0, bytesRead);
            }
        }

        return tempFile;
    }

    private void loadSharedImages() {
        UserSessionManager sessions = new UserSessionManager(requireContext());
        long userId = sessions.getUserId();

        ApiService apiService = RetrofitClient.getRetrofitInstance(requireContext()).create(ApiService.class);
        Call<List<ProgramItem>> call = apiService.getImagesForUser(userId);

        call.enqueue(new Callback<List<ProgramItem>>() {
            @Override
            public void onResponse(Call<List<ProgramItem>> call, Response<List<ProgramItem>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    List<ProgramItem> newItems = new ArrayList<>(response.body());

                    for (ProgramItem item : newItems) {
                        item.setIsPlaceHolder(false);
                    }

                    newItems.add(new ProgramItem(true));

                    DiffUtil.DiffResult diffResult = DiffUtil.calculateDiff(new DiffUtil.Callback() {
                        @Override
                        public int getOldListSize() {
                            return items.size();
                        }

                        @Override
                        public int getNewListSize() {
                            return newItems.size();
                        }

                        @Override
                        public boolean areItemsTheSame(int oldItemPosition, int newItemPosition) {
                            Uri oldUri = items.get(oldItemPosition).getImageUri();
                            Uri newUri = newItems.get(newItemPosition).getImageUri();

                            if (items.get(oldItemPosition).isPlaceHolder() && newItems.get(newItemPosition).isPlaceHolder()) {
                                return true;
                            }

                            return oldUri != null && oldUri.equals(newUri);
                        }

                        @Override
                        public boolean areContentsTheSame(int oldItemPosition, int newItemPosition) {
                            ProgramItem oldItem = items.get(oldItemPosition);
                            ProgramItem newItem = newItems.get(newItemPosition);

                            // caption és placeholder összehasonlítása
                            return oldItem.isPlaceHolder() == newItem.isPlaceHolder()
                                    && ((oldItem.getCaption() == null && newItem.getCaption() == null)
                                    || (oldItem.getCaption() != null && oldItem.getCaption().equals(newItem.getCaption())));
                        }
                    });

                    items.clear();
                    items.addAll(newItems);
                    diffResult.dispatchUpdatesTo(adapter);
                }
            }

            @Override
            public void onFailure(Call<List<ProgramItem>> call, Throwable t) {
                Toast.makeText(requireContext(), "Error: " + t.getMessage(), Toast.LENGTH_SHORT).show();
                Log.e("LOAD_ERROR", "Error loading shared images", t);
            }
        });
    }

    private void deleteImageFromServer(long imageId) {
        ApiService apiService = RetrofitClient.getRetrofitInstance(requireContext()).create(ApiService.class);
        Call<Void> call = apiService.deleteImage(imageId);

        call.enqueue(new Callback<Void>() {
            @Override
            public void onResponse(Call<Void> call, Response<Void> response) {
                if (response.isSuccessful()) {
                    Toast.makeText(requireContext(), "Image deleted successfully.", Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(requireContext(), "Failed to delete image: " + response.code(), Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<Void> call, Throwable t) {
                Toast.makeText(requireContext(), "Error deleting image: " + t.getMessage(), Toast.LENGTH_SHORT).show();
                Log.e("DELETE_ERROR", "Error deleting image", t);
            }
        });
    }
}
