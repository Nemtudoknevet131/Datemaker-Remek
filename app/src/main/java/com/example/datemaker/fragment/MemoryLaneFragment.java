package com.example.datemaker.fragment;

import android.content.ContentResolver;
import android.database.Cursor;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.OpenableColumns;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.Toast;
import android.widget.TextView;

import com.example.datemaker.model.DateEventDto;
import com.example.datemaker.model.DateEventRequest;
import com.google.android.material.button.MaterialButton;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.example.datemaker.R;
import com.example.datemaker.adapter.InspirationCategoryAdapter;
import com.example.datemaker.adapter.InspirationIdeaAdapter;
import com.example.datemaker.databinding.FragmentMemoryLaneBinding;
import com.example.datemaker.model.CategoryDto;
import com.example.datemaker.model.IdeaDto;
import com.example.datemaker.model.ReactionRequest;
import com.example.datemaker.retrofit.ApiService;
import com.example.datemaker.retrofit.RetrofitClient;
import com.example.datemaker.utils.UserSessionManager;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.android.material.textfield.TextInputEditText;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class MemoryLaneFragment extends Fragment {

    private FragmentMemoryLaneBinding binding;
    private InspirationCategoryAdapter categoryAdapter;
    private ApiService apiService;
    private UserSessionManager session;

    private ActivityResultLauncher<String> imagePickerLauncher;
    private Uri selectedImageUri;
    private ImageView dialogPreviewImage;

    private List<CategoryDto> currentCategories = new ArrayList<>();

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        apiService = RetrofitClient.getRetrofitInstance(requireContext()).create(ApiService.class);
        session = new UserSessionManager(requireContext());

        imagePickerLauncher = registerForActivityResult(
                new ActivityResultContracts.GetContent(),
                uri -> {
                    if (uri != null && dialogPreviewImage != null) {
                        selectedImageUri = uri;
                        dialogPreviewImage.setImageURI(uri);
                    }
                }
        );
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        binding = FragmentMemoryLaneBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view,
                              @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        setupRecyclerView();
        setupButton();
        loadCategories();
    }

    private void setupRecyclerView() {
        categoryAdapter = new InspirationCategoryAdapter(
                requireContext(),
                new InspirationIdeaAdapter.OnIdeaActionListener() {
                    @Override
                    public void onLikeClicked(IdeaDto idea) {
                        reactToIdea(idea, "LIKE");
                    }

                    @Override
                    public void onDislikeClicked(IdeaDto idea) {
                        reactToIdea(idea, "DISLIKE"); // ✅ fixed variable name
                    }

                    @Override
                    public void onIdeaLongClicked(IdeaDto idea) {
                        showIdeaDetailsDialog(idea);
                    }
                }
        );

        binding.rvCategories.setLayoutManager(
                new LinearLayoutManager(requireContext(), LinearLayoutManager.VERTICAL, false)
        );
        binding.rvCategories.setHasFixedSize(false);
        binding.rvCategories.setAdapter(categoryAdapter);
    }

    private void setupButton() {
        binding.btnContribute.setOnClickListener(v -> {
            if (!session.isLoggedIn()) {
                Toast.makeText(requireContext(), "Please log in first", Toast.LENGTH_SHORT).show();
                return;
            }

            if (currentCategories == null || currentCategories.isEmpty()) {
                Toast.makeText(requireContext(), "Categories are still loading. Please wait.", Toast.LENGTH_SHORT).show();
                return;
            }

            showContributeDialog();
        });
    }

    private void showLoading(boolean show) {
        binding.progressBar.setVisibility(show ? View.VISIBLE : View.GONE);
        binding.rvCategories.setAlpha(show ? 0.3f : 1f);
    }

    private void loadCategories() {
        showLoading(true);
        binding.tvEmpty.setVisibility(View.GONE);

        apiService.getInspirationCategories().enqueue(new Callback<List<CategoryDto>>() {
            @Override
            public void onResponse(Call<List<CategoryDto>> call, Response<List<CategoryDto>> response) {
                showLoading(false);

                if (response.isSuccessful() && response.body() != null) {
                    currentCategories = response.body();
                    categoryAdapter.submitList(currentCategories);
                    binding.tvEmpty.setVisibility(currentCategories.isEmpty() ? View.VISIBLE : View.GONE);
                } else {
                    int errorCode = response.code();
                    android.util.Log.e("MEMORY_LANE", "Backend error occurred, code: " + errorCode);
                    Toast.makeText(requireContext(), "Server error: " + errorCode, Toast.LENGTH_LONG).show();
                    binding.tvEmpty.setVisibility(View.VISIBLE);
                }
            }

            @Override
            public void onFailure(Call<List<CategoryDto>> call, Throwable t) {
                showLoading(false);
                binding.tvEmpty.setVisibility(View.VISIBLE);
                Toast.makeText(requireContext(), "Error: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void showIdeaDetailsDialog(IdeaDto idea) {
        BottomSheetDialog dialog = new BottomSheetDialog(requireContext());
        View sheetView = getLayoutInflater().inflate(R.layout.dialog_idea_details, null);
        dialog.setContentView(sheetView);

        TextView tvTitle = sheetView.findViewById(R.id.tvDetailTitle);
        TextView tvDescription = sheetView.findViewById(R.id.tvDetailDescription);
        MaterialButton btnAdd = sheetView.findViewById(R.id.btnAddDateIdea);

        tvTitle.setText(idea.getTitle());
        tvDescription.setText(idea.getDescription());

        btnAdd.setOnClickListener(v -> {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                String todayIso = LocalDate.now().toString();

                DateEventRequest request = new DateEventRequest();
                request.setTitle(idea.getTitle());
                request.setDescription(idea.getDescription());
                request.setDate(todayIso);

                apiService.createDateEvent(request).enqueue(new Callback<DateEventDto>() {
                    @Override
                    public void onResponse(Call<DateEventDto> call, Response<DateEventDto> response) {
                        if (response.isSuccessful()) {
                            Toast.makeText(requireContext(), "Added to Calendar!", Toast.LENGTH_SHORT).show();
                            dialog.dismiss();
                        } else {
                            Toast.makeText(requireContext(), "Failed to add date", Toast.LENGTH_SHORT).show();
                        }
                    }

                    @Override
                    public void onFailure(Call<DateEventDto> call, Throwable t) {

                    }
                });
            } else {
                Toast.makeText(requireContext(), "Sorry g, not supported device for this typa action", Toast.LENGTH_SHORT).show();
            }
        });

        dialog.show();
    }

    private void showContributeDialog() {
        BottomSheetDialog dialog = new BottomSheetDialog(requireContext());
        View sheetView = getLayoutInflater().inflate(R.layout.dialog_contribute_idea, null);
        dialog.setContentView(sheetView);

        Spinner spinnerCategory = sheetView.findViewById(R.id.spinnerCategory);
        TextInputEditText etTitle = sheetView.findViewById(R.id.etTitle);
        TextInputEditText etDescription = sheetView.findViewById(R.id.etDescription);
        dialogPreviewImage = sheetView.findViewById(R.id.ivPreview);
        View btnChoosePhoto = sheetView.findViewById(R.id.btnChoosePhoto);
        View btnSubmit = sheetView.findViewById(R.id.btnSubmitIdea);

        // Fill spinner with category names
        List<String> categoryNames = new ArrayList<>();
        for (CategoryDto cat : currentCategories) {
            categoryNames.add(cat.getName());
        }
        ArrayAdapter<String> adapter = new ArrayAdapter<>(
                requireContext(),
                android.R.layout.simple_spinner_dropdown_item,
                categoryNames
        );
        spinnerCategory.setAdapter(adapter);

        // ✅ Choose photo: just open picker
        btnChoosePhoto.setOnClickListener(v -> {
            selectedImageUri = null;
            dialogPreviewImage.setImageDrawable(null);
            imagePickerLauncher.launch("image/*");
        });

        // ✅ Submit: validate + upload + create idea
        btnSubmit.setOnClickListener(v -> {
            String title = etTitle.getText() != null ? etTitle.getText().toString().trim() : "";
            String description = etDescription.getText() != null ? etDescription.getText().toString().trim() : "";

            if (title.isEmpty()) {
                etTitle.setError("Title required");
                return;
            }

            if (description.isEmpty()) {
                etDescription.setError("Description required");
                return;
            }

            if (selectedImageUri == null) {
                Toast.makeText(requireContext(), "Please choose a photo.", Toast.LENGTH_SHORT).show();
                return;
            }

            int selectedPos = spinnerCategory.getSelectedItemPosition();
            if (selectedPos < 0 || selectedPos >= currentCategories.size()) {
                Toast.makeText(requireContext(), "Please select a category.", Toast.LENGTH_SHORT).show();
                return;
            }

            Long categoryId = currentCategories.get(selectedPos).getId();
            uploadImageAndCreateIdea(dialog, categoryId, title, description);
        });

        dialog.show();
    }

    private void uploadImageAndCreateIdea(BottomSheetDialog dialog,
                                          Long categoryId,
                                          String title,
                                          String description) {
        if (selectedImageUri == null) return;

        try {
            ContentResolver resolver = requireContext().getContentResolver();
            InputStream inputStream = resolver.openInputStream(selectedImageUri);

            if (inputStream == null) {
                Toast.makeText(requireContext(), "Failed to read image.", Toast.LENGTH_SHORT).show();
                return;
            }

            String fileName = getFileNameFromUri(selectedImageUri);
            if (fileName == null || fileName.isEmpty()) {
                fileName = "idea_image.jpg";
            }

            ByteArrayOutputStream buffer = new ByteArrayOutputStream();
            byte[] data = new byte[4096];
            int nRead;

            while ((nRead = inputStream.read(data, 0, data.length)) != -1) {
                buffer.write(data, 0, nRead);
            }

            buffer.flush();
            byte[] bytes = buffer.toByteArray();

            RequestBody requestFile = RequestBody.create(bytes, MediaType.parse("image/*"));
            MultipartBody.Part body = MultipartBody.Part.createFormData("file", fileName, requestFile);

            Toast.makeText(requireContext(), "Uploading image...", Toast.LENGTH_SHORT).show();

            apiService.uploadInspirationImage(body).enqueue(new Callback<String>() {
                @Override
                public void onResponse(Call<String> call, Response<String> response) {
                    if (response.isSuccessful() && response.body() != null) {
                        String imageUrl = response.body();
                        createIdea(dialog, categoryId, title, description, imageUrl);
                    } else {
                        Toast.makeText(requireContext(), "Image upload failed.", Toast.LENGTH_SHORT).show();
                    }
                }

                @Override
                public void onFailure(Call<String> call, Throwable t) {
                    Toast.makeText(requireContext(), "Image upload error: " + t.getMessage(), Toast.LENGTH_SHORT).show();
                }
            });

        } catch (Exception e) {
            Toast.makeText(requireContext(), "Error: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }

    private void createIdea(BottomSheetDialog dialog,
                            Long categoryId,
                            String title,
                            String description,
                            String imageUrl) {
        Toast.makeText(requireContext(), "Posting idea...", Toast.LENGTH_SHORT).show();

        apiService.createInspirationIdea(categoryId, title, description, imageUrl)
                .enqueue(new Callback<IdeaDto>() {
                    @Override
                    public void onResponse(Call<IdeaDto> call, Response<IdeaDto> response) {
                        if (response.isSuccessful()) {
                            Toast.makeText(requireContext(), "Idea posted 🎉", Toast.LENGTH_SHORT).show();
                            dialog.dismiss();
                            loadCategories();
                        } else {
                            Toast.makeText(requireContext(), "Failed to post idea.", Toast.LENGTH_SHORT).show();
                        }
                    }

                    @Override
                    public void onFailure(Call<IdeaDto> call, Throwable t) {
                        Toast.makeText(requireContext(), "Error: " + t.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void reactToIdea(IdeaDto idea, String type) {
        if (!session.isLoggedIn()) {
            Toast.makeText(requireContext(), "You need to be logged in.", Toast.LENGTH_SHORT).show();
            return;
        }

        ReactionRequest request = new ReactionRequest(idea.getId(), type);

        apiService.reactToIdea(request).enqueue(new Callback<IdeaDto>() {
            @Override
            public void onResponse(Call<IdeaDto> call, Response<IdeaDto> response) {
                if (response.isSuccessful() && response.body() != null) {
                    IdeaDto updated = response.body();
                    updateIdeaInList(updated);
                } else {
                    Toast.makeText(requireContext(), "Failed to react", Toast.LENGTH_LONG).show();
                }
            }

            @Override
            public void onFailure(Call<IdeaDto> call, Throwable t) {
                Toast.makeText(requireContext(), "Error: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void updateIdeaInList(IdeaDto updated) {
        if (currentCategories == null) return;

        for (CategoryDto cat : currentCategories) {
            if (cat.getIdeas() == null) continue;

            for (int i = 0; i < cat.getIdeas().size(); i++) {
                IdeaDto idea = cat.getIdeas().get(i);
                if (idea.getId().equals(updated.getId())) {
                    cat.getIdeas().set(i, updated);
                    categoryAdapter.submitList(new ArrayList<>(currentCategories));
                    return;
                }
            }
        }
    }

    private String getFileNameFromUri(Uri uri) {
        String result = null;
        if ("content".equals(uri.getScheme())) {
            ContentResolver resolver = requireContext().getContentResolver();
            Cursor cursor = resolver.query(uri, null, null, null, null);
            try {
                if (cursor != null && cursor.moveToFirst()) {
                    int nameIndex = cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME);
                    if (nameIndex != -1) {
                        result = cursor.getString(nameIndex);
                    }
                }
            } finally {
                if (cursor != null) cursor.close();
            }
        }
        if (result == null) {
            result = uri.getLastPathSegment();
        }
        return result;
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
        dialogPreviewImage = null;
        selectedImageUri = null;
    }
}