//package com.example.jobhunter.api;
//
//import android.content.Context;
//import net.gotev.uploadservice.MultipartUploadRequest;
//import net.gotev.uploadservice.UploadNotificationConfig;
//import net.gotev.uploadservice.UploadServiceBroadcastReceiver;
//import net.gotev.uploadservice.UploadInfo;
//import net.gotev.uploadservice.ServerResponse;
//import net.gotev.uploadservice.UploadServiceSingleBroadcastReceiver;
//import java.util.UUID;
//
//public class FileApi {
//    public interface UploadCallback {
//        void onSuccess();
//        void onError(String errorMsg);
//    }
//
//    public static void uploadFile(Context context, String filePath, String token, UploadCallback callback) {
//        try {
//            String uploadId = UUID.randomUUID().toString();
//            new MultipartUploadRequest(context, uploadId, ApiConfig.FILE)
//                    .addFileToUpload(filePath, "file")
//                    .addHeader("Authorization", "Bearer " + token)
//                    .setMaxRetries(2)
//                    .setNotificationConfig(new UploadNotificationConfig())
//                    .setDelegate(new UploadServiceSingleBroadcastReceiver.Delegate() {
//                        @Override
//                        public void onProgress(Context context, UploadInfo uploadInfo) {}
//
//                        @Override
//                        public void onError(Context context, UploadInfo uploadInfo, Throwable exception) {
//                            if (callback != null) callback.onError(exception.getMessage());
//                        }
//
//                        @Override
//                        public void onCompleted(Context context, UploadInfo uploadInfo, ServerResponse serverResponse) {
//                            if (callback != null) callback.onSuccess();
//                        }
//
//                        @Override
//                        public void onCancelled(Context context, UploadInfo uploadInfo) {
//                            if (callback != null) callback.onError("Upload cancelled");
//                        }
//                    })
//                    .startUpload();
//        } catch (Exception e) {
//            if (callback != null) callback.onError(e.getMessage());
//        }
//    }
//}