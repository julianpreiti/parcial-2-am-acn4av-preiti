package com.app.semanita;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
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
            HeaderViewHolder headerHolder = (HeaderViewHolder) holder;
            headerHolder.dayName.setText(item.dayName);
            headerHolder.dateText.setText(item.formattedDate);

            //  cambiar el color del día si es "Hoy"
            if (item.isToday) {
                int mainColor = headerHolder.itemView.getContext().getColor(R.color.main);
                headerHolder.dayName.setTextColor(mainColor);
            } else {
                int blackColor = headerHolder.itemView.getContext().getColor(R.color.black);
                headerHolder.dayName.setTextColor(blackColor);
            }
        } else if (holder instanceof TaskViewHolder) {
            TaskViewHolder taskHolder = (TaskViewHolder) holder;
            Task task = item.task;

            taskHolder.title.setText(task.title);
            taskHolder.description.setText(task.description);
            taskHolder.time.setText(task.minutes + " " + taskHolder.itemView.getContext().getString(R.string.minutes_suffix));

            // Verificar si es hoy
            SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
            String today = dateFormat.format(Calendar.getInstance().getTime());
            boolean isToday = task.day.equals(today);

            // Cambiar el fondo y mostrar/ocultar etiqueta según si está completada
            if (task.completed) {
                taskHolder.cardView.setCardBackgroundColor(taskHolder.itemView.getContext().getColor(R.color.completed_background));
                // Solo mostrar "Completado" si NO es de hoy
                if (isToday) {
                    taskHolder.completedLabel.setVisibility(View.GONE);
                } else {
                    taskHolder.completedLabel.setVisibility(View.VISIBLE);
                }
            } else {
                taskHolder.cardView.setCardBackgroundColor(taskHolder.itemView.getContext().getColor(R.color.incomplete_background));
                taskHolder.completedLabel.setVisibility(View.GONE);
            }

            // Verifica si es para restaurar la fecha de hoy o siguientes
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
        TextView dayName;
        TextView dateText;

        HeaderViewHolder(View itemView) {
            super(itemView);
            dayName = itemView.findViewById(R.id.history_day_name);
            dateText = itemView.findViewById(R.id.history_date);
        }
    }

    static class TaskViewHolder extends RecyclerView.ViewHolder {
        CardView cardView;
        TextView title, description, time, completedLabel;
        ImageButton buttonRestore;

        TaskViewHolder(View itemView) {
            super(itemView);
            cardView = itemView.findViewById(R.id.history_task_card);
            title = itemView.findViewById(R.id.history_task_title);
            description = itemView.findViewById(R.id.history_task_description);
            time = itemView.findViewById(R.id.history_task_time);
            completedLabel = itemView.findViewById(R.id.history_task_completed_label);
            buttonRestore = itemView.findViewById(R.id.button_restore);
        }
    }
}
