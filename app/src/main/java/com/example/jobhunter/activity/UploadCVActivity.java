package com.example.jobhunter.activity;

import android.app.Activity;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.provider.OpenableColumns;
import android.view.View;
import android.widget.*;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;
import com.example.jobhunter.R;
import com.example.jobhunter.ViewModel.UploadCVViewModel;
import androidx.appcompat.app.AlertDialog;
import androidx.cardview.widget.CardView;
import com.google.android.material.card.MaterialCardView;

public class UploadCVActivity extends AppCompatActivity {
    private static final int PICK_FILE_REQUEST = 1;
    private Uri selectedFileUri;
    private String selectedFileName;
    private String uploadedFileName;

    private TextView tvAttachCV;
    private MaterialCardView cvAttachCV;
    private Button btnConfirmApply;
    private ProgressBar progressBar;

    private UploadCVViewModel viewModel;

    private String userEmail;
    private long userId;
    private long jobId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_upload);

        tvAttachCV = findViewById(R.id.tv_attach_cv);
        cvAttachCV = findViewById(R.id.btn_attach_cv);
        btnConfirmApply = findViewById(R.id.btn_confirm_apply);
        progressBar = findViewById(R.id.progressBar);

        viewModel = new ViewModelProvider(this).get(UploadCVViewModel.class);

        userId = getIntent().getLongExtra("userId", 0);
        jobId = getIntent().getLongExtra("jobId", 0);
        userEmail = getIntent().getStringExtra("userEmail");
        if (userId == 0 || jobId == 0 || userEmail == null || userEmail.isEmpty()) {
            Toast.makeText(this, "Thiếu thông tin người dùng hoặc công việc!", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        cvAttachCV.setOnClickListener(v -> openFilePicker());
        btnConfirmApply.setOnClickListener(v -> {
            if (selectedFileUri != null && selectedFileName != null) {
                viewModel.uploadFileToServer(getApplication(), selectedFileUri, selectedFileName);
            } else {
                Toast.makeText(this, "Vui lòng chọn file CV", Toast.LENGTH_SHORT).show();
            }
        });

        viewModel.getIsLoading().observe(this, isLoading -> {
            progressBar.setVisibility(isLoading ? View.VISIBLE : View.GONE);
            btnConfirmApply.setEnabled(!isLoading);
            cvAttachCV.setEnabled(!isLoading);
        });

        viewModel.getUploadResult().observe(this, fileName -> {
            if (fileName != null) {
                uploadedFileName = fileName;
                Toast.makeText(this, "Upload file thành công", Toast.LENGTH_SHORT).show();
                viewModel.submitResume(userEmail, uploadedFileName, userId, jobId);
            } else {
                Toast.makeText(this, "Upload file thất bại", Toast.LENGTH_SHORT).show();
            }
        });

        viewModel.getSubmitResult().observe(this, result -> {
            if ("success".equals(result)) {
                showSuccessDialog();
            } else if ("fail".equals(result)) {
                showErrorDialog();
            }
        });
    }

    private void openFilePicker() {
        Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
        intent.setType("*/*");
        String[] mimeTypes = { "application/pdf", "application/msword",
                "application/vnd.openxmlformats-officedocument.wordprocessingml.document" };
        intent.putExtra(Intent.EXTRA_MIME_TYPES, mimeTypes);
        startActivityForResult(intent, PICK_FILE_REQUEST);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == PICK_FILE_REQUEST && resultCode == RESULT_OK && data != null) {
            selectedFileUri = data.getData();
            selectedFileName = getFileNameFromUri(selectedFileUri);
            tvAttachCV.setText(selectedFileName);
        }
    }

    private String getFileNameFromUri(Uri uri) {
        String result = null;
        if (uri.getScheme().equals("content")) {
            try (Cursor cursor = getContentResolver().query(uri, null, null, null, null)) {
                if (cursor != null && cursor.moveToFirst()) {
                    int nameIndex = cursor.getColumnIndexOrThrow(OpenableColumns.DISPLAY_NAME);
                    result = cursor.getString(nameIndex);
                }
            }
        }
        if (result == null) {
            result = uri.getPath();
            int cut = result.lastIndexOf('/');
            if (cut != -1) {
                result = result.substring(cut + 1);
            }
        }
        return result;
    }

    private void showSuccessDialog() {
        new AlertDialog.Builder(this)
                .setTitle("Nộp CV thành công")
                .setMessage("Bạn đã nộp CV thành công! Chúng tôi sẽ liên hệ với bạn sớm.")
                .setPositiveButton("OK", (dialog, which) -> {
                    dialog.dismiss();
                    finish();
                })
                .setCancelable(false)
                .show();
    }

    private void showErrorDialog() {
        new AlertDialog.Builder(this)
                .setTitle("Nộp CV thất bại")
                .setMessage("Đã có lỗi xảy ra khi nộp CV. Vui lòng thử lại sau.")
                .setPositiveButton("OK", (dialog, which) -> dialog.dismiss())
                .setCancelable(false)
                .show();
    }
}