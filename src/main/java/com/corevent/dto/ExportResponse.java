package com.corevent.dto;

public class ExportResponse {
  private boolean success;
  private String message;
  private byte[] fileData;
  private String fileName;
  private String mimeType;
  
  public ExportResponse() {}
  
  public ExportResponse(boolean success, String message) {
    this.success = success;
    this.message = message;
  }
  
  public ExportResponse(boolean success, byte[] fileData, String fileName, String mimeType) {
    this.success = success;
    this.fileData = fileData;
    this.fileName = fileName;
    this.mimeType = mimeType;
  }
  
  // Getters and Setters
  public boolean isSuccess() { return success; }
  public void setSuccess(boolean success) { this.success = success; }
  
  public String getMessage() { return message; }
  public void setMessage(String message) { this.message = message; }
  
  public byte[] getFileData() { return fileData; }
  public void setFileData(byte[] fileData) { this.fileData = fileData; }
  
  public String getFileName() { return fileName; }
  public void setFileName(String fileName) { this.fileName = fileName; }
  
  public String getMimeType() { return mimeType; }
  public void setMimeType(String mimeType) { this.mimeType = mimeType; }
}