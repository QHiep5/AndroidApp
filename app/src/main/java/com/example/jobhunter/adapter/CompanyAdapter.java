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
import com.squareup.picasso.Picasso;

import java.util.List;

public class CompanyAdapter extends RecyclerView.Adapter<CompanyAdapter.CompanyViewHolder> {
    private List<Company> companyList;
    private Context context;
    private static final String LOGO_BASE_URL = "http://172.16.12.122:8080/storage/company/";

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

        String logoFileName = company.getLogo();
        Log.d("ADAPTER_LOGO_URL", "Company: " + company.getName() + ", FileName: " + logoFileName);

        if (logoFileName != null && !logoFileName.isEmpty()) {
            String fullLogoUrl = LOGO_BASE_URL + logoFileName;
            Log.d("ADAPTER_LOGO_URL", "Constructed URL: " + fullLogoUrl);

            Picasso.get()
                    .load(fullLogoUrl)
                    .placeholder(R.drawable.ic_company)
                    .error(R.drawable.ic_company)
                    .into(holder.imgLogo, new com.squareup.picasso.Callback() {
                        @Override
                        public void onSuccess() {
                            Log.d("PICASSO_DEBUG", "Success loading image for: " + company.getName());
                        }

                        @Override
                        public void onError(Exception e) {
                            Log.e("PICASSO_DEBUG", "Error loading image from " + fullLogoUrl, e);
                        }
                    });
        } else {
            Log.w("ADAPTER_LOGO_URL", "Logo FileName is null or empty for: " + company.getName());
            holder.imgLogo.setImageResource(R.drawable.ic_company);
        }
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
        TextView tvCompanyName, tvViewDetails;
        public CompanyViewHolder(@NonNull View itemView) {
            super(itemView);
            imgLogo = itemView.findViewById(R.id.img_logo);
            tvCompanyName = itemView.findViewById(R.id.tv_company_name);
            tvViewDetails = itemView.findViewById(R.id.tv_view_details);
        }
    }
}