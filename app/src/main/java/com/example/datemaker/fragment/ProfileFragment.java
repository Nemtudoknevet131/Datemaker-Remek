package com.example.datemaker.fragment;

import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.hardware.lights.LightState;
import androidx.exifinterface.media.ExifInterface;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.MediaStore;
import android.provider.OpenableColumns;
import android.renderscript.ScriptGroup;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.bumptech.glide.Glide;
import com.example.datemaker.activity.AccountActivity;
import com.example.datemaker.R;
import com.example.datemaker.activity.LoginActivity;
import com.example.datemaker.activity.PartnerProfileActivity;
import com.example.datemaker.activity.PremiumStatusActivity;
import com.example.datemaker.activity.SubscriptionActivity;
import com.example.datemaker.adapter.ProfileOptionAdapter;
import com.example.datemaker.databinding.FragmentProfileBinding;
import com.example.datemaker.model.ProfileOption;
import com.example.datemaker.model.User;
import com.example.datemaker.model.UserDto;
import com.example.datemaker.retrofit.ApiService;
import com.example.datemaker.retrofit.RetrofitClient;
import com.example.datemaker.utils.UserSessionManager;
import com.google.android.material.button.MaterialButton;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class ProfileFragment extends Fragment {

    private FragmentProfileBinding binding;
    private ProfileOptionAdapter adapter;
    private UserSessionManager session;
    private ApiService apiService;

    private ActivityResultLauncher<String> pickImageLauncher;

    private static final int AVATAR_MAX_DIM = 512;
    private static final int AVATAR_QUALITY = 80;
    private static final long MAX_UPLOAD_BYTES = 5L * 1024L * 1024L;


    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        binding = FragmentProfileBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        session = new UserSessionManager(requireContext());
        apiService = RetrofitClient.getRetrofitInstance(requireContext()).create(ApiService.class);

        String fullName = session.getUserName();
        String email = session.getUserEmail();

        binding.profileName.setText(fullName != null && !fullName.isEmpty() ? fullName : "Unknown user");
        binding.profileEmail.setText(email != null && !email.isEmpty() ? email : "Fuck yourself!");

        pickImageLauncher = registerForActivityResult(
                new ActivityResultContracts.GetContent(),
                uri -> {
                    if (uri == null) return;
                    uploadAvatarViaRetrofit(uri);
                }
        );

        binding.profileImage.setOnClickListener(v -> pickImageLauncher.launch("image/*"));

        binding.profileCard.setOnClickListener(v -> {
            Intent intent = new Intent(requireContext(), AccountActivity.class);
            startActivity(intent);
        });

        List<ProfileOption> options = new ArrayList<>();

        options.add(new ProfileOption(2, R.drawable.heart_check, "Partner"));
        options.add(new ProfileOption(4, R.drawable.dollar, "Cupido"));
        options.add(new ProfileOption(3, R.drawable.logout_filled, "Logout"));

        adapter = new ProfileOptionAdapter(options, option -> {
            switch (option.id) {
                case 2:
                    Intent intentPartnerProfile = new Intent(requireContext(), PartnerProfileActivity.class);
                    startActivity(intentPartnerProfile);
                    break;
                case 4:
                    if (session.isPremium()) {
                        Intent intentStatus = new Intent(requireContext(), PremiumStatusActivity.class);
                        startActivity(intentStatus);
                    } else {
                        Intent intentSubscription = new Intent(requireContext(), SubscriptionActivity.class);
                        startActivity(intentSubscription);
                    }
                    break;
                case 3:
                    session.logout();
                    Intent intentLogout = new Intent(requireContext(), LoginActivity.class);
                    startActivity(intentLogout);
                    requireActivity().finish();
                    break;
                default:
                    break;
            }
        });

        binding.profileRecycler.setLayoutManager(new LinearLayoutManager(requireContext()));
        binding.profileRecycler.setAdapter(adapter);

        checkPartner();

        String avatarUrl = session.getAvatarUrl();

        Glide.with(this)
                .load(avatarUrl)
                .placeholder(R.drawable.profile_filled)
                .error(R.drawable.profile_filled)
                .circleCrop()
                .into(binding.profileImage);

        updatePremiumUi();
    }

    private void updatePremiumUi() {
        boolean isPremium = session.isPremium();
        String avatarUrl = session.getAvatarUrl();
        boolean hasAvatar = avatarUrl != null && !avatarUrl.isBlank();

        if (isPremium && hasAvatar) {
            binding.profileCrown.setVisibility(View.VISIBLE);
        } else {
            binding.profileCrown.setVisibility(View.GONE);
        }
    }

    private void uploadAvatarViaRetrofit(Uri uri) {
        try {
            long origSize = queryContantSize(uri);
            if (origSize > MAX_UPLOAD_BYTES) {
                Toast.makeText(requireContext(), "File too large (max 5MB)", Toast.LENGTH_SHORT).show();
                return;
            }

            byte[] bytes = compressImageForAvatar(uri, AVATAR_MAX_DIM, AVATAR_QUALITY);

            if (bytes.length > MAX_UPLOAD_BYTES) {
                Toast.makeText(requireContext(), "File too large after compression (max 5MB)", Toast.LENGTH_SHORT).show();
                return;
            }

            String mime = (Build.VERSION.SDK_INT >= 30) ? "image/webp" : "image/jpeg";
            MediaType mediaType = MediaType.get(mime);
            RequestBody req = RequestBody.create(bytes, mediaType);
            MultipartBody.Part part = MultipartBody.Part.createFormData("file", "avatar", req);

            apiService.uploadAvatar(part).enqueue(new Callback<UserDto>() {
                @Override
                public void onResponse(Call<UserDto> call, Response<UserDto> response) {
                    if (response.isSuccessful() && response.body() != null) {
                        String newUrl = response.body().getAvatarUrl();

                        new UserSessionManager(requireContext()).saveUserAvatar(newUrl);

                        Glide.with(ProfileFragment.this)
                                .load(newUrl)
                                .placeholder(R.drawable.profile_filled)
                                .error(R.drawable.profile_filled)
                                .circleCrop()
                                .into(binding.profileImage);

                        updatePremiumUi();

                        Toast.makeText(requireContext(), "Avatar updated", Toast.LENGTH_SHORT).show();
                    } else {
                        Toast.makeText(requireContext(), "Upload failed (" + response.code() + ")", Toast.LENGTH_SHORT).show();
                    }
                }

                @Override
                public void onFailure(Call<UserDto> call, Throwable t) {
                    Toast.makeText(requireContext(), "Network error: " + t.getMessage(), Toast.LENGTH_SHORT).show();
                }
            });
        } catch (Exception e) {
            Toast.makeText(requireContext(), "Error: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }

    private byte[] compressImageForAvatar(Uri uri, int maxDim, int quality) throws IOException {
        BitmapFactory.Options bounds = new BitmapFactory.Options();
        bounds.inJustDecodeBounds = true;

        try (InputStream is = requireContext().getContentResolver().openInputStream(uri)) {
            BitmapFactory.decodeStream(is, null, bounds);
        }

        int srcW = bounds.outWidth;
        int srcH = bounds.outHeight;

        if (srcW <= 0 || srcH <= 0) throw new IOException("Invalid image");

        int largest = Math.max(srcW, srcH);
        int sample = 1;
        while (largest / (sample * 2) > maxDim) sample *= 2;

        BitmapFactory.Options decode = new BitmapFactory.Options();
        decode.inSampleSize = sample;
        Bitmap bm;

        try (InputStream is2 = requireContext().getContentResolver().openInputStream(uri)) {
            bm = BitmapFactory.decodeStream(is2, null, decode);
        }

        if (bm == null) throw new IOException("Decode failed");

        bm = applyExifOrientation(uri, bm);

        int w = bm.getWidth(), h = bm.getHeight();
        float scale = (float) maxDim / Math.max(w, h);
        if (scale < 1f) {
            int nw = Math.round(w * scale), nh = Math.round(h * scale);
            bm = Bitmap.createScaledBitmap(bm, nw, nh, true);
        }

        ByteArrayOutputStream out = new ByteArrayOutputStream();
        boolean usedWebP = false;
        if (Build.VERSION.SDK_INT >= 30) {
            usedWebP = bm.compress(Bitmap.CompressFormat.WEBP_LOSSY, quality, out);
        }
        if (!usedWebP) {
            bm.compress(Bitmap.CompressFormat.JPEG, quality, out);
        }

        bm.recycle();
        return out.toByteArray();
    }

    private Bitmap applyExifOrientation(Uri uri, Bitmap bm) {
        try (InputStream is = requireContext().getContentResolver().openInputStream(uri)) {
            ExifInterface exif = new ExifInterface(is);
            int orientation = exif.getAttributeInt(ExifInterface.TAG_ORIENTATION, ExifInterface.ORIENTATION_NORMAL);
            Matrix m = new Matrix();

            switch (orientation) {
                case ExifInterface.ORIENTATION_ROTATE_90 -> m.postRotate(90);
                case ExifInterface.ORIENTATION_ROTATE_180 -> m.postRotate(180);
                case ExifInterface.ORIENTATION_ROTATE_270 -> m.postRotate(270);
                case ExifInterface.ORIENTATION_FLIP_HORIZONTAL -> m.postScale(-1, 1);
                case ExifInterface.ORIENTATION_FLIP_VERTICAL -> m.postScale(1, -1);
                default -> {}
            }

            if (!m.isIdentity()) {
                Bitmap rotated = Bitmap.createBitmap(bm, 0, 0, bm.getWidth(), bm.getHeight(), m, true);
                bm.recycle();
                return rotated;
            }
        } catch (Exception ignored) {}
        return bm;
    }

    private long queryContantSize(Uri uri) {
        Cursor c = requireContext().getContentResolver().query(uri, new String[]{OpenableColumns.SIZE}, null, null, null);

        try {
            if (c != null && c.moveToFirst()) {
                int idx = c.getColumnIndex(OpenableColumns.SIZE);
                if (idx >= 0) return c.getLong(idx);
            }
        } finally {
            if (c != null) c.close();
        }
        return -1;
    }

    @Override
    public void onResume() {
        super.onResume();
        checkPartner();
        updatePremiumUi();
    }

    private void checkPartner() {
        long userId = session.getUserId();
        if (userId == -1) {
            adapter.setPartnerEnabled(false);
            return;
        }

        apiService.isPartnered(userId).enqueue(new Callback<Boolean>() {
            @Override
            public void onResponse(Call<Boolean> call, Response<Boolean> response) {
                boolean hasPartner = response.isSuccessful()
                        && response.body() != null
                        && response.body();
                adapter.setPartnerEnabled(hasPartner);
            }

            @Override
            public void onFailure(Call<Boolean> call, Throwable t) {
                adapter.setPartnerEnabled(false);
            }
        });
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
