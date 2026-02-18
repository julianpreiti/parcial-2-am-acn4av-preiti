package com.app.semanita;

import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;

public class HistoryFragment extends Fragment {
    private RecyclerView recyclerView;
    private HistoryAdapter adapter;
    private List<HistoryItem> historyItems = new ArrayList<>();

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_history, container, false);

        recyclerView = view.findViewById(R.id.recycler_view_history);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        adapter = new HistoryAdapter(historyItems, this::loadHistory);
        recyclerView.setAdapter(adapter);

        loadHistory();

        return view;
    }

    private void loadHistory() {
        String uid = Objects.requireNonNull(FirebaseAuth.getInstance().getCurrentUser()).getUid();

        FirebaseFirestore.getInstance()
                .collection("users")
                .document(uid)
                .collection("tasks")
                .whereEqualTo("completed", true)
                .orderBy("day", Query.Direction.DESCENDING)
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    // aca las agrupamos por dia
                    LinkedHashMap<String, List<Task>> tasksByDay = new LinkedHashMap<>();

                    for (DocumentSnapshot doc : queryDocumentSnapshots) {
                        Task task = doc.toObject(Task.class);
                        if (task != null) {
                            task.id = doc.getId();

                            if (!tasksByDay.containsKey(task.day)) {
                                tasksByDay.put(task.day, new ArrayList<>());
                            }
                            tasksByDay.get(task.day).add(task);
                        }
                    }

                    historyItems.clear();
                    SimpleDateFormat inputFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
                    SimpleDateFormat outputFormat = new SimpleDateFormat("EEEE d 'de' MMMM", new Locale("es"));

                    for (Map.Entry<String, List<Task>> entry : tasksByDay.entrySet()) {
                        try {
                            Date date = inputFormat.parse(entry.getKey());
                            String formattedDate = outputFormat.format(date);
                            formattedDate = formattedDate.substring(0, 1).toUpperCase() + formattedDate.substring(1);

                            historyItems.add(new HistoryItem(entry.getKey(), formattedDate, true));

                            for (Task task : entry.getValue()) {
                                historyItems.add(new HistoryItem(entry.getKey(), task));
                            }
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }

                    adapter.notifyDataSetChanged();
                });
    }

    // auxiliares para headers y tasks
    static class HistoryItem {
        String dateKey;
        String formattedDate;
        Task task;
        boolean isHeader;

        // constructor para header y tareas
        HistoryItem(String dateKey, String formattedDate, boolean isHeader) {
            this.dateKey = dateKey;
            this.formattedDate = formattedDate;
            this.isHeader = isHeader;
        }

        HistoryItem(String dateKey, Task task) {
            this.dateKey = dateKey;
            this.task = task;
            this.isHeader = false;
        }
    }
}
