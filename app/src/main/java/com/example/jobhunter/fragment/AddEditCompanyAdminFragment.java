package com.example.jobhunter.fragment;

import android.app.Activity;
import android.app.Dialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.text.TextUtils;
import android.text.method.ScrollingMovementMethod;
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
import com.example.jobhunter.api.ApiConfig;
import com.example.jobhunter.model.Company;
import com.example.jobhunter.ViewModel.CompanyViewModel;
import com.example.jobhunter.util.TokenManager;
import com.squareup.picasso.Picasso;
//import com.example.jobhunter.util.SharedPreferencesManager;

public class AddEditCompanyAdminFragment extends DialogFragment {

    private ImageView imgPreview;
    private TextView tvUploadImage;
    private TextView tvTitleForm;
    private EditText etName, etAddress, etDescription;
    private Button btnSubmit, btnCancel;
    private ImageView btnClose;

    private Uri selectedImageUri;
    private ActivityResultLauncher<Intent> imagePickerLauncher;

    private Company companyData;
    private CompanyViewModel companyViewModel;
    private String uploadedFileName;
    private String tempName;
    private String tempAddress;
    private String tempDescription;

    private boolean type;

    private static final String LOGO_BASE_URL = ApiConfig.FILE; // Replace with your actual base URL

    // Hàm khởi tạo Fragment và nhận dữ liệu
    public static AddEditCompanyAdminFragment newInstance(@Nullable Company company, String title,Boolean type) {
        AddEditCompanyAdminFragment fragment = new AddEditCompanyAdminFragment();
        Bundle args = new Bundle();
        if (company != null) {
            args.putSerializable("company", company);
        }
        args.putString("title", title);
        args.putBoolean("type", type);
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
                if (type) {
                    companyViewModel.createCompany(tempName, tempAddress, tempDescription, uploadedFileName);
                } else {
                    companyViewModel.updateCompany(
                        companyData.getId(),
                        tempName,
                        tempAddress,
                        tempDescription,
                        uploadedFileName
                    );
                }
            }
        });

        // Observe create result
        companyViewModel.getCreateResult().observe(this, success -> {
            if (success) {
                Toast.makeText(getContext(), companyData == null ? "Tạo mới thành công" : "Cập nhật thành công", Toast.LENGTH_SHORT).show();
                dismiss();
            }
        });

        // Observe update result
        companyViewModel.getUpdateResult().observe(this, success -> {
            if (success) {
                Toast.makeText(getContext(), "Cập nhật thành công", Toast.LENGTH_SHORT).show();
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
        btnClose = view.findViewById(R.id.btn_back);
        etDescription.setMovementMethod(new ScrollingMovementMethod());

        // Set up close button click listener
        btnClose.setOnClickListener(v -> dismiss());

        // Nhận dữ liệu từ arguments
        if (getArguments() != null) {
            String title = getArguments().getString("title", "");
            type = getArguments().getBoolean("type",false);
            Log.d("TYEPDD",String.valueOf(type));
            tvTitleForm.setText(title);

            companyData = (Company) getArguments().getSerializable("company");
            if (companyData != null) {
                etName.setText(companyData.getName());
                etAddress.setText(companyData.getAddress());
                etDescription.setText(companyData.getDescription());

                // Load company logo if exists
                // Load company logo if exists
                if (companyData.getLogo() != null && !companyData.getLogo().isEmpty()) {
                    // Đường dẫn API đầy đủ để tải ảnh logo
                    String fullLogoUrl =ApiConfig.LOGO_BASE_URL+companyData.getLogo();
                    // Tải ảnh bằng Picasso
                    Picasso.get()
                            .load(fullLogoUrl)
                            .placeholder(R.drawable.ic_company)  // hiển thị ảnh mặc định khi đang tải
                            .error(R.drawable.ic_company)        // hiển thị ảnh mặc định khi lỗi
                            .into(imgPreview);                   // view hiển thị ảnh
                }

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
            //Khong chua vao dc.
            String name = etName.getText().toString().trim();
            String address = etAddress.getText().toString().trim();
            String description = etDescription.getText().toString().trim();
            Log.d("Desciption",description);

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

            if (type) {
                if (selectedImageUri != null) {
                    companyViewModel.uploadFile(selectedImageUri, tempName);
                } else if (companyData != null) {
                    // If editing existing company and no new image, use existing logo
                    companyViewModel.createCompany(name, address, description, companyData.getLogo());
                }
            } else {
                Log.d("vaodchamupdate","okookok");
                if (selectedImageUri != null) {
                    Log.d("vaodchamupdate1","okookok");
                    Log.d("vaodchamupdateuri",String.valueOf(selectedImageUri));
                    companyViewModel.uploadFile(selectedImageUri, tempName);
                } else if (companyData != null) {
                    //Logic forUpdate Company
                    Log.d("vaodchamupdateId",String.valueOf(companyData.getId()));

                    Log.d("vaodchamupdate2","okookok");
                    Log.d("vaodchamupdate3",name);
                    Log.d("vaodchamupdate4",address);
                    Log.d("vaodchamupdate5",description);
                    Log.d("vaodchamupdate6",companyData.getLogo());

                    companyViewModel.updateCompany(
                        companyData.getId(),
                        name,
                        address,
                        description,
                        companyData.getLogo()
                    );
                }
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
