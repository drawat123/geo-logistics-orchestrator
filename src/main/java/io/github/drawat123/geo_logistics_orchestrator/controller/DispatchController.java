package io.github.drawat123.geo_logistics_orchestrator.controller;

import io.github.drawat123.geo_logistics_orchestrator.dto.DispatchResult;
import io.github.drawat123.geo_logistics_orchestrator.service.DispatchService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api")
public class DispatchController {
    private final DispatchService dispatchService;

    public DispatchController(DispatchService dispatchService) {
        this.dispatchService = dispatchService;
    }

    @RequestMapping(value = "/orders/{orderId}/dispatch-retry", method = RequestMethod.POST)
    public ResponseEntity<DispatchResult> retryDispatch(@PathVariable UUID orderId) {
        // 1. Check if it's already assigned (Don't double dispatch)
        // 2. Call the service manually
        DispatchResult result = dispatchService.assignDriverToOrder(orderId);
        return ResponseEntity.ok(result);
    }
}
