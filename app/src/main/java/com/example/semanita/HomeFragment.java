package com.example.semanita;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;
import java.util.Objects;
import java.util.TimeZone;


public class HomeFragment extends Fragment {
    private RecyclerView recyclerView;
    private TaskAdapter adapter;
    private List<Task> taskList = new ArrayList<>();
    private List<String> dayKeys = new ArrayList<>();
    private String selectedDayKey;
    private int selectedIndex = 3;
    private View lastSelectedView = null;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_home, container, false);

        LinearLayout daysContainer = view.findViewById(R.id.days_container);
        SimpleDateFormat keyFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());

        // Creamos los dias 3 antes, hoy y 7 despues
        Calendar calendar = Calendar.getInstance();
        calendar.add(Calendar.DAY_OF_MONTH, -3);

        String[] dayLetters = {"D", "L", "M", "M", "J", "V", "S"};
        SimpleDateFormat dayNumberFormat = new SimpleDateFormat("dd", Locale.getDefault());

        for (int i = 0; i < 11; i++) {
            View dayView = inflater.inflate(R.layout.item_day, daysContainer, false);
            TextView letter = dayView.findViewById(R.id.day_letter);
            TextView number = dayView.findViewById(R.id.day_number);

            int weekDay = calendar.get(Calendar.DAY_OF_WEEK) - 1;
            letter.setText(dayLetters[weekDay]);
            number.setText(dayNumberFormat.format(calendar.getTime()));

            String dayKey = keyFormat.format(calendar.getTime());
            dayKeys.add(dayKey);

            boolean isToday = (i == 3);

            if (i == selectedIndex) {
                selectedDayKey = dayKey;
            }

            if (i == selectedIndex) {
                dayView.setBackgroundResource(R.drawable.button_rounded);
                lastSelectedView = dayView;
                letter.setTextColor(ContextCompat.getColor(requireContext(), R.color.white));
                number.setTextColor(ContextCompat.getColor(requireContext(), R.color.white));
                updateHeader(view, calendar);
            } else {
                if (isToday) {
                    dayView.setBackgroundResource(R.drawable.day_current_background);
                } else {
                    dayView.setBackgroundResource(0);
                }
                letter.setTextColor(ContextCompat.getColor(requireContext(), R.color.text_inactive));
                number.setTextColor(ContextCompat.getColor(requireContext(), R.color.black));
            }

            final int index = i;
            final Calendar selectedDate = (Calendar) calendar.clone();
            dayView.setOnClickListener(v -> {
                if (lastSelectedView != null) {
                    lastSelectedView.setBackgroundResource(0);
                    TextView l = lastSelectedView.findViewById(R.id.day_letter);
                    TextView n = lastSelectedView.findViewById(R.id.day_number);
                    l.setTextColor(ContextCompat.getColor(requireContext(), R.color.text_inactive));
                    n.setTextColor(ContextCompat.getColor(requireContext(), R.color.black));
                    if (selectedIndex == 3) {
                        lastSelectedView.setBackgroundResource(R.drawable.day_current_background);
                    }
                }
                v.setBackgroundResource(R.drawable.button_rounded);
                letter.setTextColor(ContextCompat.getColor(requireContext(), R.color.white));
                number.setTextColor(ContextCompat.getColor(requireContext(), R.color.white));
                lastSelectedView = v;
                selectedIndex = index;
                selectedDayKey = dayKeys.get(index);
                updateHeader(view, selectedDate);
                loadTasks();
            });

            daysContainer.addView(dayView);
            calendar.add(Calendar.DAY_OF_MONTH, 1);
        }

        recyclerView = view.findViewById(R.id.recycler_view_tasks);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        adapter = new TaskAdapter(taskList, this::loadTasks);
        recyclerView.setAdapter(adapter);

        loadTasks();

        return view;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        getParentFragmentManager().setFragmentResultListener("task_added", this, (key, bundle) -> {
            loadTasks();
        });
    }

    public void loadTasks() {
        SimpleDateFormat keyFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
        keyFormat.setTimeZone(TimeZone.getDefault());
        String uid = Objects.requireNonNull(FirebaseAuth.getInstance().getCurrentUser()).getUid();
        FirebaseFirestore.getInstance()
                .collection("users")
                .document(uid)
                .collection("tasks")
                .whereEqualTo("day", selectedDayKey)
                .whereEqualTo("completed", false)
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    taskList.clear();
                    for (DocumentSnapshot doc : queryDocumentSnapshots) {
                        Task task = doc.toObject(Task.class);
                        if (task != null) {
                            task.id = doc.getId();
                            taskList.add(task);
                        }
                    }
                    adapter.notifyDataSetChanged();
                    TextView selectedDay = requireView().findViewById(R.id.selectedDay);
                    selectedDay.setText("Tareas: " + taskList.size());
                });
    }

    private void updateHeader(View view, Calendar date) {
        TextView dayNumber = view.findViewById(R.id.dayNumber);
        TextView dayName = view.findViewById(R.id.dayName);
        TextView dayHour = view.findViewById(R.id.dayHour);

        SimpleDateFormat dayNumberFormat = new SimpleDateFormat("dd", Locale.getDefault());
        SimpleDateFormat dayNameFormat = new SimpleDateFormat("EEEE", new Locale("es"));

        dayNumber.setText(dayNumberFormat.format(date.getTime()));
        String name = dayNameFormat.format(date.getTime());
        dayName.setText(name.substring(0, 1).toUpperCase(new Locale("es")) + name.substring(1));

        // Calcular diferencia de días
        Calendar today = Calendar.getInstance();
        today.set(Calendar.HOUR_OF_DAY, 0);
        today.set(Calendar.MINUTE, 0);
        today.set(Calendar.SECOND, 0);
        today.set(Calendar.MILLISECOND, 0);

        Calendar selected = (Calendar) date.clone();
        selected.set(Calendar.HOUR_OF_DAY, 0);
        selected.set(Calendar.MINUTE, 0);
        selected.set(Calendar.SECOND, 0);
        selected.set(Calendar.MILLISECOND, 0);

        long diffMillis = selected.getTimeInMillis() - today.getTimeInMillis();
        int diffDays = (int) (diffMillis / (24 * 60 * 60 * 1000));

        if (diffDays == 0) {
            SimpleDateFormat hourFormat = new SimpleDateFormat("HH:mm", Locale.getDefault());
            hourFormat.setTimeZone(TimeZone.getDefault());
            dayHour.setText(hourFormat.format(Calendar.getInstance().getTime()));
        } else if (diffDays == -1) {
            dayHour.setText("Ayer");
        } else if (diffDays < -1) {
            dayHour.setText("Hace " + Math.abs(diffDays) + " días");
        } else if (diffDays == 1) {
            dayHour.setText("Mañana");
        } else {
            dayHour.setText("En " + diffDays + " días");
        }
    }
}
