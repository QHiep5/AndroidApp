package com.example.jobhunter.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.example.jobhunter.R;
import com.example.jobhunter.model.Job;
import java.util.List;

public class JobListAdapter extends RecyclerView.Adapter<JobListAdapter.JobViewHolder> {
    private List<Job> jobList;
    private Context context;

    public JobListAdapter(Context context, List<Job> jobList) {
        this.context = context;
        this.jobList = jobList;
    }

    @NonNull
    @Override
    public JobViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_job, parent, false);
        return new JobViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull JobViewHolder holder, int position) {
        Job job = jobList.get(position);
        holder.txtTen.setText(job.getName());
        holder.txtLocation.setText(job.getLocation());
        holder.txtTime.setText(job.getStartDate());
        holder.txtSalary.setText(String.valueOf(job.getSalary()));
        // holder.imgHinh.setImageResource(...); // nếu có hình
    }

    @Override
    public int getItemCount() {
        return jobList == null ? 0 : jobList.size();
    }

    public void setData(List<Job> jobs) {
        this.jobList = jobs;
        notifyDataSetChanged();
    }

    public static class JobViewHolder extends RecyclerView.ViewHolder {
        TextView txtTen, txtSalary, txtTime, txtLocation;
        ImageView imgHinh;
        public JobViewHolder(@NonNull View itemView) {
            super(itemView);
            txtTen = itemView.findViewById(R.id.txtTen);
            txtSalary = itemView.findViewById(R.id.txtSalary);
            txtTime = itemView.findViewById(R.id.txtTime);
            txtLocation = itemView.findViewById(R.id.txtLocation);
            imgHinh = itemView.findViewById(R.id.imgHinh);
        }
    }
}