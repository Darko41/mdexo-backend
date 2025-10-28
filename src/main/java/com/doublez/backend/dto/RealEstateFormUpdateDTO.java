package com.doublez.backend.dto;

import org.springframework.web.multipart.MultipartFile;

import jakarta.validation.Valid;

public class RealEstateFormUpdateDTO {
	
	@Valid
    private RealEstateUpdateDTO updateDto;
    private MultipartFile[] images;
	
    public RealEstateUpdateDTO getUpdateDto() {
		return updateDto;
	}
	public void setUpdateDto(RealEstateUpdateDTO updateDto) {
		this.updateDto = updateDto;
	}
	public MultipartFile[] getImages() {
		return images;
	}
	public void setImages(MultipartFile[] images) {
		this.images = images;
	}
    
    

}
