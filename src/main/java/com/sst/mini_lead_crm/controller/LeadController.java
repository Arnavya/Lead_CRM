package com.sst.mini_lead_crm.controller;

import com.sst.mini_lead_crm.dto.request.CreateLeadRequest;
import com.sst.mini_lead_crm.dto.request.UpdateLeadRequest;
import com.sst.mini_lead_crm.dto.request.UpdateLeadStatusRequest;
import com.sst.mini_lead_crm.dto.response.LeadResponse;
import com.sst.mini_lead_crm.enums.LeadStatus;
import com.sst.mini_lead_crm.service.LeadService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/leads")
@RequiredArgsConstructor
public class LeadController {

    private final LeadService leadService;

    @PostMapping
    public ResponseEntity<LeadResponse> createLead(
            @Valid @RequestBody CreateLeadRequest request
    ) {

        LeadResponse response = leadService.createLead(request);

        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping
    public ResponseEntity<List<LeadResponse>> getAllLeads(
            @RequestParam(required = false) LeadStatus status
    ) {

        List<LeadResponse> leads = leadService.getAllLeads(status);

        return ResponseEntity.ok(leads);
    }

    @GetMapping("/{id}")
    public ResponseEntity<LeadResponse> getLeadById(
            @PathVariable UUID id
    ) {

        LeadResponse response = leadService.getLeadById(id);

        return ResponseEntity.ok(response);
    }

    @PutMapping("/{id}")
    public ResponseEntity<LeadResponse> updateLead(
            @PathVariable UUID id,
            @Valid @RequestBody UpdateLeadRequest request
    ) {

        LeadResponse response = leadService.updateLead(id, request);

        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteLead(
            @PathVariable UUID id
    ) {

        leadService.deleteLead(id);

        return ResponseEntity.noContent().build();
    }

    @PatchMapping("/{id}/status")
    public ResponseEntity<LeadResponse> updateLeadStatus(
            @PathVariable UUID id,
            @Valid @RequestBody UpdateLeadStatusRequest request
    ) {

        LeadResponse response = leadService.updateLeadStatus(id, request);

        return ResponseEntity.ok(response);
    }
}