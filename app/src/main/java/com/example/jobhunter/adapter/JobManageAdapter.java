package com.example.jobhunter.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.example.jobhunter.R;
import com.example.jobhunter.model.Job;
import java.util.List;

public class JobManageAdapter extends RecyclerView.Adapter<JobManageAdapter.JobViewHolder> {
    private List<Job> jobList;
    private OnActionClickListener actionClickListener;

    public interface OnActionClickListener {
        void onEdit(Job job);
        void onDelete(Job job);
    }

    public JobManageAdapter(List<Job> jobList, OnActionClickListener listener) {
        this.jobList = jobList;
        this.actionClickListener = listener;
    }

    public void setData(List<Job> jobs) {
        this.jobList = jobs;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public JobViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_job_manage, parent, false);
        return new JobViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull JobViewHolder holder, int position) {
        Job job = jobList.get(position);
        holder.tvJobTitle.setText(job.getName());
        holder.tvCompanyName.setText(job.getCompany() != null ? job.getCompany().getName() : "");
        holder.tvSalary.setText(String.valueOf(job.getSalary()));

        holder.btnEdit.setOnClickListener(v -> actionClickListener.onEdit(job));
        holder.btnDelete.setOnClickListener(v -> actionClickListener.onDelete(job));
    }

    @Override
    public int getItemCount() {
        return jobList != null ? jobList.size() : 0;
    }

    static class JobViewHolder extends RecyclerView.ViewHolder {
        TextView tvJobTitle, tvCompanyName, tvSalary;
        ImageButton btnEdit, btnDelete;

        public JobViewHolder(@NonNull View itemView) {
            super(itemView);
            tvJobTitle = itemView.findViewById(R.id.tv_job_title);
            tvCompanyName = itemView.findViewById(R.id.tv_company_name);
            tvSalary = itemView.findViewById(R.id.tv_salary);
            btnEdit = itemView.findViewById(R.id.btn_edit);
            btnDelete = itemView.findViewById(R.id.btn_delete);
        }
    }
} 