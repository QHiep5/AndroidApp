package com.example.jobhunter.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.jobhunter.R;
import com.example.jobhunter.model.Company;

import java.util.List;

public class CompanyManageAdapter extends RecyclerView.Adapter<CompanyManageAdapter.CompanyViewHolder> {
    private List<Company> companyList;
    private OnActionClickListener actionClickListener;

    public interface OnActionClickListener {
        void onEdit(Company company);
        void onDelete(Company company);
    }

    public CompanyManageAdapter(List<Company> companyList, OnActionClickListener listener) {
        this.companyList = companyList;
        this.actionClickListener = listener;
    }

    public void setData(List<Company> companies) {
        this.companyList = companies;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public CompanyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_company_admin, parent, false);
        return new CompanyViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull CompanyViewHolder holder, int position) {
        Company company = companyList.get(position);
        holder.tvCompanyName.setText(company.getName());
//        holder.tvCompanyField.setText(company.getField());
        holder.tvCompanyAddress.setText(company.getAddress());
        
        holder.btnEdit.setOnClickListener(v -> actionClickListener.onEdit(company));
        holder.btnDelete.setOnClickListener(v -> actionClickListener.onDelete(company));
    }

    @Override
    public int getItemCount() {
        return companyList != null ? companyList.size() : 0;
    }

    static class CompanyViewHolder extends RecyclerView.ViewHolder {
        TextView tvCompanyName, tvCompanyField, tvCompanyAddress;
        ImageButton btnEdit, btnDelete;

        public CompanyViewHolder(@NonNull View itemView) {
            super(itemView);
            tvCompanyName = itemView.findViewById(R.id.tv_company_name);
            tvCompanyField = itemView.findViewById(R.id.tv_company_field);
            tvCompanyAddress = itemView.findViewById(R.id.tv_company_address);
            btnEdit = itemView.findViewById(R.id.btn_edit);
            btnDelete = itemView.findViewById(R.id.btn_delete);
        }
    }
} 