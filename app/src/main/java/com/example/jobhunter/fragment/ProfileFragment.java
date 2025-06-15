package com.example.jobhunter.fragment;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.fragment.app.Fragment;
import com.example.jobhunter.R;
import com.example.jobhunter.util.TokenManager;
import com.example.jobhunter.activity.LoginActivity;

public class ProfileFragment extends Fragment {
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_profile, container, false);

        ImageView imgAvatar = view.findViewById(R.id.img_avatar);
        TextView tvName = view.findViewById(R.id.tv_name);
        TextView tvUserId = view.findViewById(R.id.tv_user_id);
        TextView tvEmail = view.findViewById(R.id.tv_email);
        TextView tvPhone = view.findViewById(R.id.tv_phone);
        TextView tvAddress = view.findViewById(R.id.tv_address);
        Button btnLogout = view.findViewById(R.id.btn_logout);
        Button btnEditProfile = view.findViewById(R.id.btn_editProfile);
        Button btnLogin = view.findViewById(R.id.btn_login);
        View infoLayout = view.findViewById(R.id.layout_user_info);

        SharedPreferences prefs = getContext().getSharedPreferences("jobhunter_prefs", getContext().MODE_PRIVATE);
        String token = TokenManager.getToken(getContext());
        if (token != null) {
            String name = prefs.getString("user_name", "Tên người dùng");
            int userId = prefs.getInt("user_id", 0);
            String email = prefs.getString("user_email", "Chưa cập nhật");
            String phone = prefs.getString("user_phone", "Chưa cập nhật");
            String address = prefs.getString("user_address", "Chưa cập nhật");
            tvName.setText(name);
            tvUserId.setText("Mã ứng viên: " + userId);
            tvEmail.setText(email);
            tvPhone.setText(phone);
            tvAddress.setText(address);
            infoLayout.setVisibility(View.VISIBLE);
            btnLogout.setVisibility(View.VISIBLE);
            btnEditProfile.setVisibility(View.VISIBLE);
            btnLogin.setVisibility(View.GONE);
        } else {
            infoLayout.setVisibility(View.GONE);
            btnLogout.setVisibility(View.GONE);
            btnEditProfile.setVisibility(View.GONE);
            btnLogin.setVisibility(View.VISIBLE);
            btnLogin.setOnClickListener(v -> {
                Intent intent = new Intent(getActivity(), LoginActivity.class);
                startActivity(intent);
            });
        }

        btnLogout.setOnClickListener(v -> {
            TokenManager.clearToken(getContext());
            prefs.edit().clear().apply();
            Intent intent = new Intent(getActivity(), LoginActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
            requireActivity().finish();
        });

        btnEditProfile.setOnClickListener(v -> {
            int userId = prefs.getInt("user_id", 0);
            Intent intent = new Intent(getActivity(), com.example.jobhunter.activity.ProfileDetailActivity.class);
            intent.putExtra("user_id", userId);
            startActivity(intent);
        });

        return view;
    }
}