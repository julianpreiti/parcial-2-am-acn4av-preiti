package com.app.semanita;

import android.app.Dialog;
import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.DialogFragment;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.TimeZone;

public class AddTaskDialogFragment extends DialogFragment {
    private TaskAdapter.OnTaskCompletedListener listener;

    public AddTaskDialogFragment(TaskAdapter.OnTaskCompletedListener listener) {
        this.listener = listener;
    }
    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
        View view = getLayoutInflater().inflate(R.layout.fragment_add_task_dialog, null);

        Spinner spinnerDay = view.findViewById(R.id.spinner_day);
        EditText editTitle = view.findViewById(R.id.edit_title);
        EditText editDescription = view.findViewById(R.id.edit_description);
        EditText editMinutes = view.findViewById(R.id.edit_minutes);

        List<String> days = new ArrayList<>();
        List<String> dayKeys = new ArrayList<>();
        SimpleDateFormat sdf = new SimpleDateFormat("EEEE dd/MM", new Locale("es"));
        SimpleDateFormat keyFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
        keyFormat.setTimeZone(TimeZone.getDefault());
        Calendar cal = Calendar.getInstance();
        for (int i = 0; i < 8; i++) {
            days.add(sdf.format(cal.getTime()));
            dayKeys.add(keyFormat.format(cal.getTime()));
            cal.add(Calendar.DAY_OF_YEAR, 1);
        }
        ArrayAdapter<String> adapter = new ArrayAdapter<>(requireContext(),
                android.R.layout.simple_spinner_item, days);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerDay.setAdapter(adapter);

        return new AlertDialog.Builder(requireContext())
                .setTitle("Añadir Tarea")
                .setView(view)
                .setPositiveButton("Guardar", (dialog, which) -> {
                    String title = editTitle.getText().toString().trim();
                    String description = editDescription.getText().toString().trim();
                    String minutesStr = editMinutes.getText().toString().trim();
                    int selectedDayIndex = spinnerDay.getSelectedItemPosition();
                    String day = dayKeys.get(selectedDayIndex);

                    if (title.isEmpty()) {
                        Toast.makeText(getContext(), "El título no puede estar vacío", Toast.LENGTH_SHORT).show();
                        return;
                    }

                    if (minutesStr.isEmpty()) {
                        Toast.makeText(getContext(), "Ingresa los minutos", Toast.LENGTH_SHORT).show();
                        return;
                    }
                    if (minutesStr.length() > 2) {
                        Toast.makeText(getContext(), "Máximo 60 minutos", Toast.LENGTH_SHORT).show();
                        return;
                    }
                    int minutes;
                    try {
                        minutes = Integer.parseInt(minutesStr);
                    } catch (NumberFormatException e) {
                        Toast.makeText(getContext(), "Los minutos deben ser un número", Toast.LENGTH_SHORT).show();
                        return;
                    }
                    if (minutes < 1 || minutes > 60) {
                        Toast.makeText(getContext(), "Máximo 60 minutos", Toast.LENGTH_SHORT).show();
                        return;
                    }

                    String uid = Objects.requireNonNull(FirebaseAuth.getInstance().getCurrentUser()).getUid();

                    Map<String, Object> task = new HashMap<>();
                    task.put("title", title);
                    task.put("description", description);
                    task.put("minutes", minutes);
                    task.put("day", day);
                    task.put("createdAt", System.currentTimeMillis());
                    task.put("completed", false);

                    FirebaseFirestore.getInstance()
                            .collection("users")
                            .document(uid)
                            .collection("tasks")
                            .add(task)
                            .addOnSuccessListener(documentReference -> {
                                if (listener != null) listener.onTaskCompleted();
                                if (isAdded()) {
                                    Bundle result = new Bundle();
                                    result.putString("day", day);
                                    getParentFragmentManager().setFragmentResult("task_added", result);
                                }
                            });
                })
                    .setNegativeButton("Cancelar", null)
                    .create();
    }
}