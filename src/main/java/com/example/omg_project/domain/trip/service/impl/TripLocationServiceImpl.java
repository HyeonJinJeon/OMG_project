package com.example.omg_project.domain.trip.service.impl;

import com.example.omg_project.domain.trip.dto.TripLocationDto;
import com.example.omg_project.domain.trip.entity.TripLocation;
import com.example.omg_project.domain.trip.repository.TripLocationRepository;
import com.example.omg_project.domain.trip.service.TripLocationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;

@Service
@RequiredArgsConstructor
@Slf4j
public class TripLocationServiceImpl implements TripLocationService {

    private final TripLocationRepository tripLocationRepository;

    /**
     * !! Repository 저장 로직 개발 필요 !!
     * 선택된 장소 정보를 개별 장소마다 저장
     */
    @Override
    @Transactional
    public void saveLocation(TripLocationDto tripLocationDto) {
        log.info("place name: " + tripLocationDto.getName());
        log.info("longitude: " + tripLocationDto.getLongitude());
        log.info("latitude: " + tripLocationDto.getLatitude());

        TripLocation tripLocation = new TripLocation();
//        tripLocation.setTripDate();
        tripLocation.setPlaceName(tripLocationDto.getName());
        tripLocation.setLongitude(BigDecimal.valueOf(tripLocationDto.getLongitude()));
        tripLocation.setLatitude(BigDecimal.valueOf(tripLocationDto.getLatitude()));
//        tripLocation.setPlaceReviews();

//        tripLocationRepository.save(tripLocation);

    }

}
