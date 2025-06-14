package com.example.jobhunter.fragment;

import android.app.Activity;
import android.app.Dialog;
import android.content.Intent;
import android.graphics.drawable.ColorDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;
import androidx.lifecycle.ViewModelProvider;

import com.example.jobhunter.R;
import com.example.jobhunter.model.Company;
import com.example.jobhunter.ViewModel.CompanyViewModel;
//import com.example.jobhunter.util.SharedPreferencesManager;

public class AddCompanyFragment extends DialogFragment {

    private ImageView imgPreview;
    private TextView tvUploadImage;
    private TextView tvTitleForm;
    private EditText etName, etAddress, etDescription;
    private Button btnSubmit, btnCancel;

    private Uri selectedImageUri;
    private ActivityResultLauncher<Intent> imagePickerLauncher;

    private Company companyData;
    private CompanyViewModel companyViewModel;
    private String uploadedFileName;
    private String tempName;
    private String tempAddress;
    private String tempDescription;

    // Hàm khởi tạo Fragment và nhận dữ liệu
    public static AddCompanyFragment newInstance(@Nullable Company company, String title) {
        AddCompanyFragment fragment = new AddCompanyFragment();
        Bundle args = new Bundle();
        if (company != null) {
            args.putSerializable("company", company);
        }
        args.putString("title", title);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        companyViewModel = new ViewModelProvider(this).get(CompanyViewModel.class);

        // Observe upload result
        companyViewModel.getUploadResult().observe(this, fileName -> {
            if (fileName != null) {
                uploadedFileName = fileName;
                // After successful upload, create company with the file name
                companyViewModel.createCompany(tempName, tempAddress, tempDescription, uploadedFileName);
            }
        });

        // Observe create result
        companyViewModel.getCreateResult().observe(this, success -> {
            if (success) {
                Toast.makeText(getContext(), companyData == null ? "Tạo mới thành công" : "Cập nhật thành công", Toast.LENGTH_SHORT).show();
                dismiss();
            }
        });

        // Observe error
        companyViewModel.getErrorLiveData().observe(this, error -> {
            if (error != null) {
                Toast.makeText(getContext(), error, Toast.LENGTH_SHORT).show();
            }
        });

        // Đăng ký nhận ảnh từ thư viện
        imagePickerLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                result -> {
                    if (result.getResultCode() == Activity.RESULT_OK && result.getData() != null) {
                        selectedImageUri = result.getData().getData();
                        imgPreview.setImageURI(selectedImageUri);
                    }
                }
        );
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.add_company_form, container, false);

        // Ánh xạ View
        tvTitleForm = view.findViewById(R.id.tittleForm);
        etName = view.findViewById(R.id.et_company_name);
        etAddress = view.findViewById(R.id.et_company_address);
        etDescription = view.findViewById(R.id.et_company_description);
        btnSubmit = view.findViewById(R.id.btn_submit_company);
        btnCancel = view.findViewById(R.id.btn_cancel_company);
        tvUploadImage = view.findViewById(R.id.tv_upload_image);
        imgPreview = view.findViewById(R.id.img_company_preview);

        // Nhận dữ liệu từ arguments
        if (getArguments() != null) {
            String title = getArguments().getString("title", "");
            tvTitleForm.setText(title);

            companyData = (Company) getArguments().getSerializable("company");
            if (companyData != null) {
                etName.setText(companyData.getName());
                etAddress.setText(companyData.getAddress());
                etDescription.setText(companyData.getDescription());
                // TODO: load ảnh từ URL nếu có
            }
        }

        // Bắt sự kiện chọn ảnh
        tvUploadImage.setOnClickListener(v -> {
            Intent intent = new Intent(Intent.ACTION_PICK);
            intent.setType("image/*");
            imagePickerLauncher.launch(intent);
        });

        // Nút hủy
        btnCancel.setOnClickListener(v -> dismiss());

        // Nút submit
        btnSubmit.setOnClickListener(v -> {
            String name = etName.getText().toString().trim();
            String address = etAddress.getText().toString().trim();
            String description = etDescription.getText().toString().trim();

            if (TextUtils.isEmpty(name) || TextUtils.isEmpty(address)) {
                Toast.makeText(getContext(), "Vui lòng điền đầy đủ thông tin", Toast.LENGTH_SHORT).show();
                return;
            }

            if (selectedImageUri == null && companyData == null) {
                Toast.makeText(getContext(), "Vui lòng chọn ảnh", Toast.LENGTH_SHORT).show();
                return;
            }

            // Store the form data temporarily
            tempName = name;
            tempAddress = address;
            tempDescription = description;
            Log.d("CompanyName",tempName);

            Log.d("ImageURL", String.valueOf(selectedImageUri));
            // If we have a new image, upload it first
            if (selectedImageUri != null) {
                Log.d("chayvao1","Chayvao1");
                companyViewModel.uploadFile(selectedImageUri,tempName);

            } else if (companyData != null) {
                Log.d("chayvao2","Chayvao2");
                // If editing existing company and no new image, use existing logo
                companyViewModel.createCompany(name, address, description, companyData.getLogo());
            }
        });

        return view;
    }

    @Override
    public void onStart() {
        super.onStart();
        if (getDialog() != null && getDialog().getWindow() != null) {
            Window window = getDialog().getWindow();
            DisplayMetrics metrics = new DisplayMetrics();
            requireActivity().getWindowManager().getDefaultDisplay().getMetrics(metrics);

            int width = (int) (metrics.widthPixels * 0.9);
            int height = (int) (metrics.heightPixels * 0.8);

            window.setLayout(width, height);
            window.setGravity(Gravity.CENTER);
//            window.setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        }
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        Dialog dialog = super.onCreateDialog(savedInstanceState);
        dialog.setCanceledOnTouchOutside(false);
        return dialog;
    }
}
