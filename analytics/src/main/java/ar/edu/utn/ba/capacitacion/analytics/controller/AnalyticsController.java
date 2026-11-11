package ar.edu.utn.ba.capacitacion.analytics.controller;

import ar.edu.utn.ba.capacitacion.analytics.dto.AnalyticsResumenResponse;
import ar.edu.utn.ba.capacitacion.analytics.service.AnalyticsService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/analytics")
public class AnalyticsController {

    private final AnalyticsService analyticsService;

    public AnalyticsController(AnalyticsService analyticsService) {
        this.analyticsService = analyticsService;
    }

    @GetMapping("/resumen")
    public AnalyticsResumenResponse obtenerResumen() {
        return analyticsService.obtenerResumen();
    }
}
