package com.doublez.backend.response;

public class ApiResponse<T> {
	
	private String status;
	private String message;
	private T data;
	
	public ApiResponse(String status, String message, T data) {
		super();
		this.status = status;
		this.message = message;
		this.data = data;
	}
	
	public static <T> ApiResponse<T> success(T data) {
		return new ApiResponse<T>("success", null, data);
	}
	
	public static <T> ApiResponse<T> error(String message) {
		return new ApiResponse<T>("error", message, null);
	}
	
	public static <T> ApiResponse<T> error(String message, T details) {
        return new ApiResponse<T>("error", message, details);
    }

	public String getStatus() {
		return status;
	}

	public void setStatus(String status) {
		this.status = status;
	}

	public String getMessage() {
		return message;
	}

	public void setMessage(String message) {
		this.message = message;
	}

	public T getData() {
		return data;
	}

	public void setData(T data) {
		this.data = data;
	}
	
}
