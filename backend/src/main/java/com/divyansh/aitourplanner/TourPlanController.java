package com.divyansh.aitourplanner;

import java.util.*;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

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

            // --------------------------------------------
            // MATCH THE TYPE EXACTLY:
            // Map<String, Map<String, List<TourResponse.Place>>>
            // --------------------------------------------
            Map<String, Map<String, List<TourResponse.Place>>> fallbackItinerary
                    = new HashMap<>();

            for (int i = 1; i <= request.getDays(); i++) {

                Map<String, List<TourResponse.Place>> day = new HashMap<>();

                day.put("morning", List.of(
                        new TourResponse.Place("Error", "No plan available", 0, 0)
                ));

                day.put("afternoon", List.of(
                        new TourResponse.Place("Error", "No plan available", 0, 0)
                ));

                day.put("evening", List.of(
                        new TourResponse.Place("Error", "No plan available", 0, 0)
                ));

                fallbackItinerary.put("Day " + i, day);
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
