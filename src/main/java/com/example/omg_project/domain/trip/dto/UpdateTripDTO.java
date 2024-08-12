package com.example.omg_project.domain.trip.dto;


import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

@Getter
@Setter
public class UpdateTripDTO {
    private Long id;
    private String tripName;
    private LocalDate startDate;
    private LocalDate endDate;
    private Long cityId;
    private List<TripDateDTO> tripDates;

    @Getter
    @Setter
    public static class TripDateDTO {
        private Long id;
        private LocalDate tripDate;
        private List<TripLocationDTO> tripLocations;
    }

    @Getter
    @Setter
    public static class TripLocationDTO {
        private Long id;
        private String placeName;
        private BigDecimal latitude;
        private BigDecimal longitude;
    }
}