package com.doublez.backend.service.realestate;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.PageRequest;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import com.doublez.backend.entity.realestate.RealEstate;
import com.doublez.backend.exception.IllegalOperationException;
import com.doublez.backend.exception.LimitationExceededException;
import com.doublez.backend.exception.ResourceNotFoundException;
import com.doublez.backend.repository.realestate.RealEstateRepository;
import com.doublez.backend.service.user.UserService;

import jakarta.transaction.Transactional;

@Service
@Transactional
public class FeaturedListingService {
	
	private static final Logger logger = LoggerFactory.getLogger(FeaturedListingService.class);

    private final RealEstateRepository realEstateRepository;
    private final RealEstateAuthorizationService authService; 
    private final UserService userService; 

    public FeaturedListingService(RealEstateRepository realEstateRepository, 
                                RealEstateAuthorizationService authService,
                                UserService userService) { 
        this.realEstateRepository = realEstateRepository;
        this.authService = authService;
        this.userService = userService;
    }

//    public boolean canFeatureRealEstate(Long userId, Long realEstateId) {
//        // USE AUTH SERVICE INSTEAD OF DUPLICATE LOGIC
//        return authService.canFeatureRealEstate(userId) && 
//               authService.hasRealEstateUpdateAccess(realEstateId);
//    }

//    public RealEstate featureRealEstate(Long userId, Long realEstateId, Integer featuredDays) {
//        // USE AUTH SERVICE FOR PERMISSION CHECK
//        if (!authService.canFeatureRealEstate(userId)) {
//            throw new LimitationExceededException("Featured listing limit reached");
//        }
//
//        RealEstate realEstate = realEstateRepository.findById(realEstateId)
//                .orElseThrow(() -> new ResourceNotFoundException("Real estate not found"));
//
//        // USE AUTH SERVICE FOR OWNERSHIP CHECK
//        if (!authService.isOwner(realEstateId)) {
//            throw new IllegalOperationException("User does not own this real estate");
//        }
//
//        realEstate.setFeatured(true, featuredDays);
//        return realEstateRepository.save(realEstate);
//    }

    public RealEstate unfeatureRealEstate(Long userId, Long realEstateId) {
        RealEstate realEstate = realEstateRepository.findById(realEstateId)
                .orElseThrow(() -> new ResourceNotFoundException("Real estate not found"));

        // USE AUTH SERVICE FOR OWNERSHIP CHECK
        if (!authService.isOwner(realEstateId)) {
            throw new IllegalOperationException("User does not own this real estate");
        }

        realEstate.setFeatured(false, null);
        return realEstateRepository.save(realEstate);
    }

    @Scheduled(cron = "0 0 2 * * ?")
    public void unfeatureExpiredListings() {
        List<RealEstate> expiredFeatured = realEstateRepository.findExpiredFeaturedRealEstates();

        for (RealEstate realEstate : expiredFeatured) {
            realEstate.setFeatured(false, null);
            realEstateRepository.save(realEstate);
        }

        if (!expiredFeatured.isEmpty()) {
            logger.info("Unfeatured {} expired featured listings", expiredFeatured.size());
        }
    }

    public List<RealEstate> getActiveFeaturedListings(int limit) {
        return realEstateRepository.findActiveFeaturedRealEstates(PageRequest.of(0, limit));
    }
}