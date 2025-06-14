package com.example.jobhunter.adapter;

import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.Spinner;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.example.jobhunter.R;
import com.example.jobhunter.activity.JobDetailActivity;
import com.example.jobhunter.model.Job;
import com.example.jobhunter.model.Resume;
import com.example.jobhunter.util.constant.ResumeStateEnum;
import com.google.gson.Gson;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import android.widget.ArrayAdapter;
import android.widget.AdapterView;

public class ResumeAdapter extends RecyclerView.Adapter<ResumeAdapter.ResumeViewHolder> {

    private final Context context;
    private List<Resume> resumeList;
    private boolean isAdmin = false;
    private OnStateChangeListener stateChangeListener;

    public interface OnStateChangeListener {
        void onStateChange(Resume resume, ResumeStateEnum newState);
    }

    public ResumeAdapter(Context context, List<Resume> resumeList, boolean isAdmin, OnStateChangeListener listener) {
        this.context = context;
        this.resumeList = resumeList;
        this.isAdmin = isAdmin;
        this.stateChangeListener = listener;
    }

    public void setData(List<Resume> resumeList) {
        this.resumeList = resumeList;
        Log.d("ResumeAdapter", "Số lượng CV nhận được: " + (resumeList != null ? resumeList.size() : 0));
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ResumeViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_submitted_resume, parent, false);
        return new ResumeViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ResumeViewHolder holder, int position) {
        Resume resume = resumeList.get(position);
        if (resume == null || resume.getJob() == null || resume.getJob().getCompany() == null) {
            return;
        }

        Job job = resume.getJob();
        holder.companyNameTV.setText(job.getCompany().getName());
        holder.jobTitleTV.setText(job.getName());

        // Format ngày nộp
        try {
            SimpleDateFormat inputFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSSSS", Locale.getDefault());
            SimpleDateFormat outputFormat = new SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault());
            Date date = inputFormat.parse(resume.getCreatedAt());
            holder.appliedDateTV.setText("Ngày nộp: " + outputFormat.format(date));
        } catch (ParseException e) {
            holder.appliedDateTV.setText("Ngày nộp: " + resume.getCreatedAt());
        }

        holder.sttTV.setText(String.valueOf(position + 1));

        // Hiển thị trạng thái
        if (isAdmin) {
            holder.statusTV.setVisibility(View.GONE);
            holder.spinnerStatus.setVisibility(View.VISIBLE);
            ArrayAdapter<ResumeStateEnum> adapter = new ArrayAdapter<>(context, android.R.layout.simple_spinner_item, ResumeStateEnum.values());
            adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
            holder.spinnerStatus.setAdapter(adapter);
            holder.spinnerStatus.setSelection(resume.getStatus().ordinal());
            holder.spinnerStatus.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                @Override
                public void onItemSelected(AdapterView<?> parent, View view, int pos, long id) {
                    ResumeStateEnum selected = ResumeStateEnum.values()[pos];
                    if (selected != resume.getStatus() && stateChangeListener != null) {
                        Log.d("ResumeAdapter", "Admin chọn trạng thái mới: " + selected + " cho resumeId: " + resume.getId());
                        stateChangeListener.onStateChange(resume, selected);
                    }
                }
                @Override public void onNothingSelected(AdapterView<?> parent) {}
            });
        } else {
            holder.statusTV.setVisibility(View.VISIBLE);
            holder.spinnerStatus.setVisibility(View.GONE);
        ResumeStateEnum status = resume.getStatus();
        if (status != null) {
            switch (status) {
                case PENDING:
                    holder.statusTV.setText("Đang chờ");
                    holder.statusTV.setTextColor(ContextCompat.getColor(context, R.color.gray));
                    break;
                case REVIEWING:
                    holder.statusTV.setText("Đang xem xét");
                    holder.statusTV.setTextColor(ContextCompat.getColor(context, R.color.orange));
                    break;
                case APPROVED:
                    holder.statusTV.setText("Đã duyệt");
                    holder.statusTV.setTextColor(ContextCompat.getColor(context, R.color.green));
                    break;
                case REJECTED:
                    holder.statusTV.setText("Từ chối");
                    holder.statusTV.setTextColor(ContextCompat.getColor(context, R.color.red));
                    break;
                default:
                    holder.statusTV.setText("Không rõ");
                    holder.statusTV.setTextColor(Color.DKGRAY);
            }
        }
        }

        // Nút xem chi tiết
        holder.viewDetailsBtn.setOnClickListener(v -> {
            Intent intent = new Intent(context, JobDetailActivity.class);
            intent.putExtra("job", new Gson().toJson(job));
            context.startActivity(intent);
        });
    }

    @Override
    public int getItemCount() {
        return resumeList == null ? 0 : resumeList.size();
    }

    public static class ResumeViewHolder extends RecyclerView.ViewHolder {
        TextView companyNameTV, jobTitleTV, appliedDateTV, statusTV,viewDetailsBtn, sttTV;
        Spinner spinnerStatus;

        public ResumeViewHolder(@NonNull View itemView) {
            super(itemView);
            jobTitleTV = itemView.findViewById(R.id.tv_job_title);
            companyNameTV = itemView.findViewById(R.id.tv_company_name);
            appliedDateTV = itemView.findViewById(R.id.tv_submission_date);
            statusTV = itemView.findViewById(R.id.tv_status);
            viewDetailsBtn = itemView.findViewById(R.id.btn_view_details);
            sttTV = itemView.findViewById(R.id.tv_stt);
            spinnerStatus = itemView.findViewById(R.id.spinner_status);
        }
    }
}
