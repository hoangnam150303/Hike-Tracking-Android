package com.example.android_coursework;

import android.content.Context;
import android.view.*;
import android.widget.*;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import java.util.List;

public class ObservationAdapter extends RecyclerView.Adapter<ObservationAdapter.ObservationViewHolder> {

    private final Context context;
    private final List<ObservationModel> observations;
    private final OnObservationActionListener listener;

    // when user click butotn edit or delete, it will call those function
    public interface OnObservationActionListener {
        void onEdit(ObservationModel obs);
        void onDelete(int obsId);
    }
    // create constructor
    public ObservationAdapter(Context context, List<ObservationModel> observations, OnObservationActionListener listener) {
        this.context = context;
        this.observations = observations;
        this.listener = listener;
    }


    @NonNull
    @Override // set data to layout
    public ObservationViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_observation, parent, false);
        return new ObservationViewHolder(view);
    }

    @Override // set data to item
    public void onBindViewHolder(@NonNull ObservationViewHolder holder, int position) {
        ObservationModel obs = observations.get(position);
        holder.tvObservation.setText(obs.getObservation());
        holder.tvTime.setText(obs.getTime());
        holder.tvComment.setText(obs.getComment());

        holder.btnEdit.setOnClickListener(v -> listener.onEdit(obs));
        holder.btnDelete.setOnClickListener(v -> listener.onDelete(obs.getId()));
    }
    // get number item
    @Override
    public int getItemCount() {
        return observations.size();
    }

    // each viewHolder is represent one item in list
    static class ObservationViewHolder extends RecyclerView.ViewHolder {
        TextView tvObservation, tvTime, tvComment;
        ImageButton btnEdit, btnDelete;

        public ObservationViewHolder(@NonNull View itemView) {
            super(itemView);
            tvObservation = itemView.findViewById(R.id.tvObservation);
            tvTime = itemView.findViewById(R.id.tvTime);
            tvComment = itemView.findViewById(R.id.tvComment);
            btnEdit = itemView.findViewById(R.id.btnEditObservation);
            btnDelete = itemView.findViewById(R.id.btnDeleteObservation);
        }
    }
}
