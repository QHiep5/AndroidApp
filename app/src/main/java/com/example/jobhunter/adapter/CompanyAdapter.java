package com.example.jobhunter.adapter;

import static java.lang.Math.log;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.example.jobhunter.R;
import com.example.jobhunter.model.Company;
import java.util.List;

public class CompanyAdapter extends RecyclerView.Adapter<CompanyAdapter.CompanyViewHolder> {
    private List<Company> companyList;
    private Context context;

    public CompanyAdapter(Context context, List<Company> companyList) {
        this.context = context;
        this.companyList = companyList;
    }

    @NonNull
    @Override
    public CompanyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_company, parent, false);
        return new CompanyViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull CompanyViewHolder holder, int position) {
        Company company = companyList.get(position);
        holder.tvCompanyName.setText(company.getName());
        // holder.tvIndustry.setText("Ngành nghề: " + company.getIndustry());
        // holder.tvPositions.setText("Đang tuyển: " + company.getNumPositions() + " vị trí");
        holder.tvViewDetails.setText("View details");
        // holder.imgLogo.setImageResource(company.getLogoResId()); // Nếu có hình
    }

    @Override
    public int getItemCount() {
        return companyList == null ? 0 : companyList.size();
    }

    public void setData(List<Company> companies) {
        this.companyList = companies;
        notifyDataSetChanged();
    }

    public static class CompanyViewHolder extends RecyclerView.ViewHolder {
        ImageView imgLogo;
        TextView tvCompanyName, tvIndustry, tvPositions, tvViewDetails;
        public CompanyViewHolder(@NonNull View itemView) {
            super(itemView);
            imgLogo = itemView.findViewById(R.id.img_logo);
            tvCompanyName = itemView.findViewById(R.id.tv_company_name);
            tvIndustry = itemView.findViewById(R.id.tv_industry);
            tvPositions = itemView.findViewById(R.id.tv_positions);
            tvViewDetails = itemView.findViewById(R.id.tv_view_details);
        }
    }
}