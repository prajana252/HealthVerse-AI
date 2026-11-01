package com.healthverse.controller;

import com.healthverse.dto.BmiRequest;
import com.healthverse.dto.BmiResponse;
import com.healthverse.service.BmiCalculatorService;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/bmi")
public class BmiController {

    private final BmiCalculatorService service;

    public BmiController(BmiCalculatorService service) {
        this.service = service;
    }

    @PostMapping
    public BmiResponse calculate(@Valid @RequestBody BmiRequest request) {
        return service.calculate(request);
    }
}
