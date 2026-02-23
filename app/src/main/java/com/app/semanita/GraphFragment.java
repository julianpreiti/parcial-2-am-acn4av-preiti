package com.app.semanita;

import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.Objects;

public class GraphFragment extends Fragment {
    private TextView totalTasksText;
    private TextView completedTasksText;
    private TextView pendingTasksText;
    private TextView completionRateText;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_graph, container, false);

        totalTasksText = view.findViewById(R.id.total_tasks_number);
        completedTasksText = view.findViewById(R.id.completed_tasks_number);
        pendingTasksText = view.findViewById(R.id.pending_tasks_number);
        completionRateText = view.findViewById(R.id.completion_rate_number);

        loadStatistics();

        return view;
    }

    private void loadStatistics() {
        String uid = Objects.requireNonNull(FirebaseAuth.getInstance().getCurrentUser()).getUid();

        FirebaseFirestore.getInstance()
                .collection("users")
                .document(uid)
                .collection("tasks")
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    int totalTasks = queryDocumentSnapshots.size();
                    int completedTasks = 0;

                    for (DocumentSnapshot doc : queryDocumentSnapshots) {
                        Task task = doc.toObject(Task.class);
                        if (task != null && task.completed) {
                            completedTasks++;
                        }
                    }

                    int pendingTasks = totalTasks - completedTasks;
                    double completionRate = totalTasks > 0 ? (completedTasks * 100.0 / totalTasks) : 0;

                    totalTasksText.setText(String.valueOf(totalTasks));
                    completedTasksText.setText(String.valueOf(completedTasks));
                    pendingTasksText.setText(String.valueOf(pendingTasks));
                    completionRateText.setText(String.format("%.1f%%", completionRate));
                });
    }
}
