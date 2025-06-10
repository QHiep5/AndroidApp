//package com.example.jobhunter.ViewModel;
//
//import android.app.Application;
//import androidx.annotation.NonNull;
//import androidx.lifecycle.AndroidViewModel;
//import androidx.lifecycle.MutableLiveData;
//import androidx.lifecycle.LiveData;
//import com.example.jobhunter.repository.FileRepository;
//import com.example.jobhunter.api.FileApi;
//
//public class FileViewModel extends AndroidViewModel {
//    private MutableLiveData<Boolean> uploadSuccess = new MutableLiveData<>();
//    private MutableLiveData<String> error = new MutableLiveData<>();
//    private FileRepository fileRepository;
//
//    public FileViewModel(@NonNull Application application) {
//        super(application);
//        fileRepository = new FileRepository();
//    }
//
//    public LiveData<Boolean> getUploadSuccess() { return uploadSuccess; }
//    public LiveData<String> getErrorLiveData() { return error; }
//
//    public void uploadFile(android.content.Context context, String filePath, String token) {
//        fileRepository.uploadFile(context, filePath, token, new FileApi.UploadCallback() {
//            @Override
//            public void onSuccess() {
//                uploadSuccess.postValue(true);
//            }
//
//            @Override
//            public void onError(String errorMsg) {
//                error.postValue(errorMsg);
//            }
//        });
//    }
//}