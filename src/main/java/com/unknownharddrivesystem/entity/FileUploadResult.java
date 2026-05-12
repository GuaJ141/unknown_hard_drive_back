package com.unknownharddrivesystem.entity;

//仅作网页上传文件后的返回参数使用
public class FileUploadResult {


    private int status;
    //0：上传失败 1：上传成功 2：空间不足 3：重名

    private String fileName;

    public int getStatus() {
        return status;
    }

    public void setStatus(int status) {
        this.status = status;
    }

    public String getFileName() {
        return fileName;
    }

    public void setFileName(String fileName) {
        this.fileName = fileName;
    }
}
