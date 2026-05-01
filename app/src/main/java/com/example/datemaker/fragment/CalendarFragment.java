package com.example.datemaker.fragment;

import android.app.AlertDialog;
import android.app.TimePickerDialog;
import android.content.DialogInterface;
import android.os.Build;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.core.content.ContextCompat;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.fragment.app.Fragment;
import android.util.DisplayMetrics;
import android.util.TypedValue;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.example.datemaker.R;
import com.example.datemaker.adapter.DayAdapter;
import com.example.datemaker.adapter.EventAdapter;
import com.example.datemaker.databinding.DialogAddEventBinding;
import com.example.datemaker.databinding.FragmentCalendarBinding;
import com.example.datemaker.model.DateEventDto;
import com.example.datemaker.model.DateEventRequest;
import com.example.datemaker.model.DayItem;
import com.example.datemaker.retrofit.ApiService;
import com.example.datemaker.retrofit.RetrofitClient;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

@RequiresApi(api = Build.VERSION_CODES.O)
public class CalendarFragment extends Fragment {

    private FragmentCalendarBinding binding;

    private ApiService apiService;
    private DayAdapter dayAdapter;
    private EventAdapter eventAdapter;

    private LocalDate currentMonth = LocalDate.now().withDayOfMonth(1);
    private LocalDate selectedDate = LocalDate.now();

    private final DateTimeFormatter monthFormatter = DateTimeFormatter.ofPattern("MMMM yyyy");
    private final DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");

    private int dayItemWidth;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        binding = FragmentCalendarBinding.inflate(inflater, container, false);

        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        apiService = RetrofitClient.getRetrofitInstance(requireContext()).create(ApiService.class);

        setupMonthHeader();
        setupDaysRecycler();
        setupEventsRecycler();
        setupFab();

        updateMonthHeader();
        loadDaysForMonth();
        loadEventsForDate(selectedDate);
    }

    private void setupMonthHeader() {
        binding.btnPrevMonth.setOnClickListener(v -> {
            currentMonth = currentMonth.minusMonths(1);
            selectedDate = currentMonth;
            updateMonthHeader();
            loadDaysForMonth();
            loadEventsForDate(selectedDate);
        });

        binding.btnNextMonth.setOnClickListener(v -> {
            currentMonth = currentMonth.plusMonths(1);
            selectedDate = currentMonth;
            updateMonthHeader();
            loadDaysForMonth();
            loadEventsForDate(selectedDate);
        });

        binding.tvMonth.setOnClickListener(v -> {
            Toast.makeText(getContext(), "Month picker coming soon \uD83D\uDD1C", Toast.LENGTH_SHORT).show();
        });
    }

    private void setupDaysRecycler() {
        binding.rvDays.setLayoutManager(
                new LinearLayoutManager(getContext(), LinearLayoutManager.HORIZONTAL, false)
        );

        DisplayMetrics dm = getResources().getDisplayMetrics();
        int screenWidth = dm.widthPixels;

        int horizontalPaddingPx = (int) TypedValue.applyDimension(
                TypedValue.COMPLEX_UNIT_DIP,
                32,
                dm
        );

        dayItemWidth = (screenWidth - horizontalPaddingPx) / 5;

        dayAdapter = new DayAdapter(new ArrayList<>(), date -> {
            selectedDate = date;
            loadEventsForDate(date);
        }, dayItemWidth);

        binding.rvDays.setAdapter(dayAdapter);
    }

    private void setupEventsRecycler() {
        binding.rvEvents.setLayoutManager(new LinearLayoutManager(getContext()));

        eventAdapter = new EventAdapter((event, newColor) -> {
            event.setColorHex(newColor);
            updateEventColor(event);
        }, this::showDeleteDialog);

        binding.rvEvents.setAdapter(eventAdapter);
    }

    private void setupFab() {
        binding.fabAddEvent.setOnClickListener(v -> showCreateEventDialog());
    }

    private void updateMonthHeader() {
        binding.tvMonth.setText(currentMonth.format(monthFormatter));
    }

    private void loadDaysForMonth() {
        List<DayItem> days = new ArrayList<>();
        int length = currentMonth.lengthOfMonth();

        int selectedIndex = 0;

        for (int i = 1; i <= length; i++) {
            LocalDate d = currentMonth.withDayOfMonth(i);
            boolean selected = d.equals(selectedDate);
            if (selected) {
                selectedIndex = i - 1;
            }
            days.add(new DayItem(d, selected));
        }

        dayAdapter = new DayAdapter(days, date -> {
            selectedDate = date;
            loadEventsForDate(date);
        }, dayItemWidth);

        binding.rvDays.setAdapter(dayAdapter);

        final int indexToScroll = selectedIndex;
        binding.rvDays.post(() -> binding.rvDays.scrollToPosition(indexToScroll));
    }

    private void loadEventsForDate(LocalDate selectedDate) {
        String iso = selectedDate.format(dateFormatter);

        apiService.getDateEventsForDay(iso).enqueue(new Callback<List<DateEventDto>>() {
            @Override
            public void onResponse(Call<List<DateEventDto>> call, Response<List<DateEventDto>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    eventAdapter.setEvents(response.body());
                } else {
                    eventAdapter.setEvents(new ArrayList<>());
                }
            }

            @Override
            public void onFailure(Call<List<DateEventDto>> call, Throwable t) {
                if (getContext() != null) {
                    Toast.makeText(getContext(), "Error loading date", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    private void showCreateEventDialog() {
        if (getContext() == null) return;

        DialogAddEventBinding dialogBinding = DialogAddEventBinding.inflate(LayoutInflater.from(getContext()));

        final String[] selectedTime = {""};

        dialogBinding.tvTimeLabel.setText(getString(R.string.time_not_set));

        dialogBinding.btnPickTime.setOnClickListener(v -> {
            Calendar calendar = Calendar.getInstance();
            int hour = calendar.get(Calendar.HOUR_OF_DAY);
            int minute = calendar.get(Calendar.MINUTE);

            TimePickerDialog dialog = new TimePickerDialog(
                    getContext(),
                    (view, hourOfDay, minuteOfHour) -> {
                        selectedTime[0] = String.format(Locale.getDefault(), "%02d:%02d", hourOfDay, minuteOfHour);
                        dialogBinding.tvTimeLabel.setText("Time: " + selectedTime[0]);
                    },
                    hour,
                    minute,
                    true
            );

            dialog.show();

            dialog.getButton(DialogInterface.BUTTON_NEGATIVE)
                    .setTextColor(ContextCompat.getColor(getContext(), R.color.white));

            dialog.getButton(DialogInterface.BUTTON_POSITIVE)
                    .setTextColor(ContextCompat.getColor(getContext(), R.color.white));
        });

        new AlertDialog.Builder(getContext())
                .setTitle(R.string.add_date)
                .setView(dialogBinding.getRoot())
                .setPositiveButton(R.string.save, (d, which) -> {
                    String title = dialogBinding.etTitle.getText().toString().trim();
                    String desc = dialogBinding.etDescription.getText().toString().trim();

                    DateEventRequest request = new DateEventRequest(
                            selectedDate.format(dateFormatter),
                            title,
                            desc,
                            "#9C27B0",
                            selectedTime[0]
                    );

                    apiService.createDateEvent(request).enqueue(new Callback<DateEventDto>() {
                        @Override
                        public void onResponse(Call<DateEventDto> call,
                                               Response<DateEventDto> response) {
                            loadEventsForDate(selectedDate);
                        }

                        @Override
                        public void onFailure(Call<DateEventDto> call, Throwable t) {
                            if (getContext() != null) {
                                Toast.makeText(getContext(),
                                        R.string.error_saving_date,
                                        Toast.LENGTH_SHORT).show();
                            }
                        }
                    });
                })
                .setNegativeButton(R.string.cancel, null)
                .show();
    }

    private void updateEventColor(DateEventDto event) {
        DateEventRequest body = new DateEventRequest();
        body.setColorHex(event.getColorHex());

        apiService.updateDateEvent(event.getId(), body).enqueue(new Callback<DateEventDto>() {
            @Override
            public void onResponse(Call<DateEventDto> call, Response<DateEventDto> response) {
                loadEventsForDate(selectedDate);
            }

            @Override
            public void onFailure(Call<DateEventDto> call, Throwable t) {
                if (getContext() != null) {
                    Toast.makeText(getContext(),
                            R.string.error_updating_color,
                            Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    private void showDeleteDialog(DateEventDto event) {
        if (getContext() == null) return;

        new AlertDialog.Builder(getContext())
                .setTitle("Delete date")
                .setMessage("Are you sure you want to delete this date?")
                .setPositiveButton("Delete", (dialog, which) -> {
                    apiService.deleteDateEvent(event.getId()).enqueue(new Callback<Void>() {
                        @Override
                        public void onResponse(Call<Void> call, Response<Void> response) {
                            loadEventsForDate(selectedDate);
                        }

                        @Override
                        public void onFailure(Call<Void> call, Throwable t) {
                            if (getContext() != null) {
                                Toast.makeText(getContext(),
                                        "Error deleting date",
                                        Toast.LENGTH_SHORT).show();
                            }
                        }
                    });
                })
                .setNegativeButton("Cancel", null)
                .show();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}