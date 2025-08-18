package com.divyansh.aitourplanner;

import java.util.HashMap;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.divyansh.aitourplanner.model.TourRequest;
import com.divyansh.aitourplanner.model.TourResponse;

@RestController
@RequestMapping("/api/tour")
@CrossOrigin(origins = "http://localhost:3000")
public class TourPlanController {

    @Autowired
    private TourPlanService tourPlanService;


    @PostMapping("/plan")
    public TourResponse generateTourPlan(@RequestBody TourRequest request) {
        try {
            return tourPlanService.generatePlan(
                    request.getCity(),
                    request.getDays(),
                    request.getInterests()
            );
        } catch (Exception e) {
            e.printStackTrace();

            // Create a placeholder/fallback itinerary map
            Map<String, Map<String, Object>> fallbackItinerary = new HashMap<>();
            for (int i = 1; i <= request.getDays(); i++) {
                Map<String, Object> dayPlan = new HashMap<>();
                dayPlan.put("morning", "No plan available due to error.");
                dayPlan.put("afternoon", "No plan available due to error.");
                dayPlan.put("evening", "No plan available due to error.");
                fallbackItinerary.put("Day " + i, dayPlan);
            }

            return new TourResponse(
                    request.getCity(),
                    request.getDays(),
                    request.getInterests(),
                    fallbackItinerary
            );
        }
    }

}