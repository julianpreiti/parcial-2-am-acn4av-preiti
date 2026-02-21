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

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;
import java.util.Objects;

public class HistoryAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
    private static final int TYPE_HEADER = 0;
    private static final int TYPE_TASK = 1;

    public interface OnHistoryUpdateListener {
        void onHistoryUpdate();
    }

    private List<HistoryFragment.HistoryItem> items;
    private OnHistoryUpdateListener listener;

    public HistoryAdapter(List<HistoryFragment.HistoryItem> items, OnHistoryUpdateListener listener) {
        this.items = items;
        this.listener = listener;
    }

    @Override
    public int getItemViewType(int position) {
        return items.get(position).isHeader ? TYPE_HEADER : TYPE_TASK;
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        if (viewType == TYPE_HEADER) {
            View view = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.item_history_header, parent, false);
            return new HeaderViewHolder(view);
        } else {
            View view = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.item_history_task, parent, false);
            return new TaskViewHolder(view);
        }
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        HistoryFragment.HistoryItem item = items.get(position);

        if (holder instanceof HeaderViewHolder) {
            ((HeaderViewHolder) holder).dateText.setText(item.formattedDate);
        } else if (holder instanceof TaskViewHolder) {
            TaskViewHolder taskHolder = (TaskViewHolder) holder;
            Task task = item.task;

            taskHolder.title.setText(task.title);
            taskHolder.description.setText(task.description);
            taskHolder.time.setText(task.minutes + " " + taskHolder.itemView.getContext().getString(R.string.minutes_suffix));

            // verificamos si es para restaurar la fecha de hoy o siguientes
            SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
            String today = dateFormat.format(Calendar.getInstance().getTime());
            boolean canRestore = task.day.compareTo(today) >= 0;

            if (canRestore) {
                taskHolder.buttonRestore.setVisibility(View.VISIBLE);
                taskHolder.buttonRestore.setOnClickListener(v -> {
                    String uid = Objects.requireNonNull(FirebaseAuth.getInstance().getCurrentUser()).getUid();
                    FirebaseFirestore.getInstance()
                            .collection("users")
                            .document(uid)
                            .collection("tasks")
                            .document(task.id)
                            .update("completed", false)
                            .addOnSuccessListener(aVoid -> {
                                if (listener != null) listener.onHistoryUpdate();
                            });
                });
            } else {
                taskHolder.buttonRestore.setVisibility(View.GONE);
            }
        }
    }


    @Override
    public int getItemCount() {
        return items.size();
    }

    static class HeaderViewHolder extends RecyclerView.ViewHolder {
        TextView dateText;

        HeaderViewHolder(View itemView) {
            super(itemView);
            dateText = itemView.findViewById(R.id.history_date_header);
        }
    }

    static class TaskViewHolder extends RecyclerView.ViewHolder {
        TextView title, description, time;
        ImageButton buttonRestore;

        TaskViewHolder(View itemView) {
            super(itemView);
            title = itemView.findViewById(R.id.history_task_title);
            description = itemView.findViewById(R.id.history_task_description);
            time = itemView.findViewById(R.id.history_task_time);
            buttonRestore = itemView.findViewById(R.id.button_restore);
        }
    }
}
