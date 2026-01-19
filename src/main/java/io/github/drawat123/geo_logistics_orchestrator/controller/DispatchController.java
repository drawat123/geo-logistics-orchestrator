package io.github.drawat123.geo_logistics_orchestrator.controller;

import io.github.drawat123.geo_logistics_orchestrator.dto.DispatchResult;
import io.github.drawat123.geo_logistics_orchestrator.service.DispatchService;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api")
public class DispatchController {
    private final DispatchService dispatchService;

    public DispatchController(DispatchService dispatchService) {
        this.dispatchService = dispatchService;
    }

    @RequestMapping(value = "/orders/{orderId}/dispatch", method = RequestMethod.POST)
    public DispatchResult assignDriverToOrder(@PathVariable("orderId") UUID orderId) {
        return dispatchService.assignDriverToOrder(orderId);
    }
}
