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
import com.example.jobhunter.activity.JobDetailActivity;
import com.example.jobhunter.model.Job;
import com.squareup.picasso.Picasso;
import android.content.Intent;

import java.util.List;
import java.text.NumberFormat;
import java.util.Locale;
import android.util.Log;

public class JobListAdapter extends RecyclerView.Adapter<JobListAdapter.JobViewHolder> {
    private List<Job> jobList;
    private Context context;
    private static final String LOGO_BASE_URL = "http://192.168.0.114:8080/storage/company/";

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
        if (job != null) {
            holder.txtTen.setText(job.getName());
            holder.txtLocation.setText(job.getLocation());
            holder.txtTime.setText(job.getStartDate());

            // Format salary for display
            NumberFormat currencyFormat = NumberFormat.getCurrencyInstance(new Locale("vi", "VN"));
            holder.txtSalary.setText(currencyFormat.format(job.getSalary()));

            if (job.getCompany() != null && job.getCompany().getLogo() != null && !job.getCompany().getLogo().isEmpty()) {
                String logoFileName = job.getCompany().getLogo();
                String fullLogoUrl = LOGO_BASE_URL + logoFileName;

                Picasso.get()
                    .load(fullLogoUrl)
                    .placeholder(R.drawable.ic_company)
                    .error(R.drawable.ic_company)
                    .into(holder.imgHinh);
            } else {
                holder.imgHinh.setImageResource(R.drawable.ic_company);
            }

            holder.itemView.setOnClickListener(v -> {
                Intent intent = new Intent(context, JobDetailActivity.class);
                intent.putExtra("JOB_ID", job.getId());
                context.startActivity(intent);
            });
        }
    }

    @Override
    public int getItemCount() {
        int count = jobList == null ? 0 : jobList.size();
        Log.d("JOB_ADAPTER_DEBUG", "getItemCount: " + count);
        return count;
    }

    public void setData(List<Job> jobs) {
        this.jobList = jobs;
        Log.d("JOB_ADAPTER_DEBUG", "setData called with " + (jobs == null ? 0 : jobs.size()) + " items.");
        notifyDataSetChanged();
    }

    public static class JobViewHolder extends RecyclerView.ViewHolder {
        ImageView imgHinh;
        TextView txtTen, txtLocation, txtSalary, txtTime;

        public JobViewHolder(@NonNull View itemView) {
            super(itemView);
            imgHinh = itemView.findViewById(R.id.imgHinh);
            txtTen = itemView.findViewById(R.id.txtTen);
            txtLocation = itemView.findViewById(R.id.txtLocation);
            txtSalary = itemView.findViewById(R.id.txtSalary);
            txtTime = itemView.findViewById(R.id.txtTime);
        }
    }
}