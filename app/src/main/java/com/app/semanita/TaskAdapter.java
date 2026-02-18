package com.app.semanita;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.List;
import java.util.Objects;

public class TaskAdapter extends RecyclerView.Adapter<TaskAdapter.TaskViewHolder> {
    public interface OnTaskCompletedListener {
        void onTaskCompleted();
    }

    private List<Task> tasks;
    private OnTaskCompletedListener listener;

    public TaskAdapter(List<Task> tasks, OnTaskCompletedListener listener) {
        this.tasks = tasks;
        this.listener = listener;
    }

    @NonNull
    @Override
    public TaskViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.task, parent, false);
        return new TaskViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull TaskViewHolder holder, int position) {
        Task task = tasks.get(position);
        holder.title.setText(task.title);
        holder.description.setText(task.description);
        holder.time.setText(task.minutes + " Minutos");
        holder.timeText.setText(task.minutes + "m");
        holder.buttonDone.setEnabled(!task.completed);

        holder.buttonDone.setOnClickListener(v -> {
            String uid = Objects.requireNonNull(FirebaseAuth.getInstance().getCurrentUser()).getUid();
            FirebaseFirestore.getInstance()
                    .collection("users")
                    .document(uid)
                    .collection("tasks")
                    .document(task.id)
                    .update("completed", true)
                    .addOnSuccessListener(aVoid -> {
                        if (listener != null) listener.onTaskCompleted();
                    });
        });
    }

    @Override
    public int getItemCount() {
        return tasks.size();
    }

    static class TaskViewHolder extends RecyclerView.ViewHolder {
        TextView title, description, time, timeText;
        ImageButton buttonDone;

        TaskViewHolder(View itemView) {
            super(itemView);
            title = itemView.findViewById(R.id.title);
            description = itemView.findViewById(R.id.card_description);
            time = itemView.findViewById(R.id.task_time);
            timeText = itemView.findViewById(R.id.time_text);
            buttonDone = itemView.findViewById(R.id.button_done);
        }
    }
}