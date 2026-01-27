package io.github.drawat123.geo_logistics_orchestrator.controller;

import io.github.drawat123.geo_logistics_orchestrator.dto.DispatchResult;
import io.github.drawat123.geo_logistics_orchestrator.service.DispatchService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/orders")
public class DispatchController {
    private final DispatchService dispatchService;

    public DispatchController(DispatchService dispatchService) {
        this.dispatchService = dispatchService;
    }

    /**
     * Manually triggers a dispatch retry.
     * Only accessible by ADMINs to fix "stuck" orders.
     */
    @PreAuthorize("hasRole('ADMIN')")
    @RequestMapping(value = "/{orderId}/dispatch-retry", method = RequestMethod.POST)
    public ResponseEntity<DispatchResult> retryDispatch(@PathVariable UUID orderId) {
        // The Service is responsible for checking if the order is already assigned.
        // If the order is invalid/missing, the GlobalExceptionHandler should catch it.
        DispatchResult result = dispatchService.assignDriverToOrder(orderId);
        return ResponseEntity.ok(result);
    }
}
