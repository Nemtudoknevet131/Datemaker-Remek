package com.example.datemaker.fragment;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.example.datemaker.adapter.InboxRequestAdapter;
import com.example.datemaker.databinding.FragmentInboxBinding;
import com.example.datemaker.model.PartnerRequestDto;
import com.example.datemaker.retrofit.ApiService;
import com.example.datemaker.retrofit.RetrofitClient;
import com.example.datemaker.utils.UserSessionManager;

import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class InboxFragment extends Fragment {

    private FragmentInboxBinding binding;
    private InboxRequestAdapter adapter;
    private ApiService apiService;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {

        binding = FragmentInboxBinding.inflate(inflater, container, false);

        return binding.getRoot();
    }

    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        apiService = RetrofitClient.getRetrofitInstance(requireContext()).create(ApiService.class);

        binding.recyclerInbox.setLayoutManager(new LinearLayoutManager(requireContext()));

        adapter = new InboxRequestAdapter(
                new ArrayList<>(),
                this::onAcceptClicked,
                this::onRejectClicked
        );

        binding.recyclerInbox.setAdapter(adapter);

        loadRequests();
    }

    private void loadRequests() {
        UserSessionManager session = new UserSessionManager(requireContext());
        long userId = session.getUserId();
        if (userId == -1) {
            binding.recyclerInbox.setVisibility(View.GONE);
            return;
        }

        Log.d("INBOX", "Loading partner requests for userId=" + userId);

        apiService.getPartnerRequests(userId).enqueue(new Callback<List<PartnerRequestDto>>() {
            @Override
            public void onResponse(Call<List<PartnerRequestDto>> call, Response<List<PartnerRequestDto>> response) {
                if (binding == null || !isAdded()) return;

                if (response.isSuccessful() && response.body() != null) {
                    List<PartnerRequestDto> list = response.body();
                    adapter.setItems(list);
                    binding.recyclerInbox.setVisibility(
                            list.isEmpty() ? View.GONE : View.VISIBLE
                    );
                    Log.d("INBOX", "Received " + list.size() + " requests from backend");
                } else {
                    Log.e("INBOX", "Response not successful. code=" + response.code());
                    binding.recyclerInbox.setVisibility(View.GONE);
                }
            }

            @Override
            public void onFailure(Call<List<PartnerRequestDto>> call, Throwable t) {
                if (binding == null || !isAdded()) return;

                Log.e("INBOX", "Failed to load requests: " + t.getMessage(), t);
                binding.recyclerInbox.setVisibility(View.GONE);
            }
        });
    }

    private void onAcceptClicked(PartnerRequestDto item) {
        apiService.acceptPartnerRequest(item.getId()).enqueue(new Callback<Void>() {
            @Override
            public void onResponse(Call<Void> call, Response<Void> response) {
                if (binding == null || !isAdded()) return;
                loadRequests();
            }

            @Override
            public void onFailure(Call<Void> call, Throwable t) {
                if (binding == null || !isAdded()) return;
            }
        });
    }

    private void onRejectClicked(PartnerRequestDto item) {
        apiService.rejectPartnerRequest(item.getId()).enqueue(new Callback<Void>() {
            @Override
            public void onResponse(Call<Void> call, Response<Void> response) {
                if (binding == null || !isAdded()) return;
                loadRequests();
            }

            @Override
            public void onFailure(Call<Void> call, Throwable t) {
                if (binding == null || !isAdded()) return;
            }
        });
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
