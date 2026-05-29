package com.sst.mini_lead_crm.service;

import org.springframework.transaction.annotation.Transactional;
import com.sst.mini_lead_crm.dto.request.CreateLeadRequest;
import com.sst.mini_lead_crm.dto.request.UpdateLeadRequest;
import com.sst.mini_lead_crm.dto.request.UpdateLeadStatusRequest;
import com.sst.mini_lead_crm.dto.response.LeadResponse;
import com.sst.mini_lead_crm.entity.Lead;
import com.sst.mini_lead_crm.enums.LeadStatus;
import com.sst.mini_lead_crm.exception.InvalidStatusTransitionException;
import com.sst.mini_lead_crm.exception.ResourceNotFoundException;
import com.sst.mini_lead_crm.mapper.LeadMapper;
import com.sst.mini_lead_crm.repository.LeadRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

import com.sst.mini_lead_crm.dto.request.BulkUpdateLeadRequest;
import com.sst.mini_lead_crm.dto.response.BulkItemResponse;
import com.sst.mini_lead_crm.dto.response.BulkOperationResponse;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validator;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Service
@RequiredArgsConstructor

public class LeadService {

    private final LeadRepository leadRepository;
    private final LeadMapper leadMapper;
    private final Validator validator;

    private final Map<UUID, LeadResponse> cache = new ConcurrentHashMap<>();

    @Transactional
    public LeadResponse createLead(CreateLeadRequest request) {

        Lead lead = leadMapper.createRequestToEntity(request);

        Lead savedLead = leadRepository.save(lead);

        return leadMapper.entityToResponse(savedLead);
    }

    public List<LeadResponse> getAllLeads(LeadStatus status) {

        List<Lead> leads;

        if (status != null) {
            leads = leadRepository.findByStatus(status);
        } else {
            leads = leadRepository.findAll();
        }

        return leads.stream()
                .map(leadMapper::entityToResponse)
                .toList();
    }

    public LeadResponse getLeadById(UUID id) {

        // Check cache first
        if (cache.containsKey(id)) {
            System.out.println("Returning lead from cache for id: " + id);
            return cache.get(id);
        }

        System.out.println("Fetching lead from database for id: " + id);


        Lead lead = leadRepository.findById(id)
                .orElseThrow(() ->
                        new ResourceNotFoundException("Lead not found with id: " + id)
                );

        LeadResponse response = leadMapper.entityToResponse(lead);

        // Store in cache
        cache.put(id, response);

        return response;
    }

    @Transactional
    public LeadResponse updateLead(UUID id, UpdateLeadRequest request) {

        Lead lead = leadRepository.findById(id)
                .orElseThrow(() ->
                        new ResourceNotFoundException("Lead not found with id: " + id)
                );

        leadMapper.updateEntityFromRequest(request, lead);

        Lead updatedLead = leadRepository.save(lead);

        LeadResponse response = leadMapper.entityToResponse(updatedLead);
        // Update cache
        cache.put(id, response);

        return response;
    }

    @Transactional
    public void deleteLead(UUID id) {

        Lead lead = leadRepository.findById(id)
                .orElseThrow(() ->
                        new ResourceNotFoundException("Lead not found with id: " + id)
                );

        leadRepository.delete(lead);

        // Remove from cache
        cache.remove(id);
    }

    @Transactional
    public LeadResponse updateLeadStatus(UUID id, UpdateLeadStatusRequest request) {

        Lead lead = leadRepository.findById(id)
                .orElseThrow(() ->
                        new ResourceNotFoundException("Lead not found with id: " + id)
                );

        LeadStatus currentStatus = lead.getStatus();
        LeadStatus newStatus = request.getStatus();

        if (!currentStatus.canTransitionTo(newStatus)) {
            throw new InvalidStatusTransitionException(
                    "Cannot transition lead status from "
                            + currentStatus
                            + " to "
                            + newStatus
            );
        }

        lead.setStatus(newStatus);

        Lead updatedLead = leadRepository.save(lead);

        LeadResponse response = leadMapper.entityToResponse(updatedLead);

        // Update cache
        cache.put(id, response);

        return response;
    }


    public BulkOperationResponse<LeadResponse> bulkCreateLeads(
            List<CreateLeadRequest> requests) {

        List<BulkItemResponse<LeadResponse>> results = new ArrayList<>();

        int successful = 0;
        int failed = 0;

        for (CreateLeadRequest request : requests) {

            try {
                Set<ConstraintViolation<CreateLeadRequest>> violations =
                        validator.validate(request);

                if (!violations.isEmpty()) {

                    String errorMessage = violations.stream()
                            .map(v ->
                                    v.getPropertyPath() + ": " + v.getMessage()
                            )
                            .collect(Collectors.joining(", "));

                    results.add(
                            BulkItemResponse.<LeadResponse>builder()
                                    .success(false)
                                    .error(errorMessage)
                                    .build()
                    );

                    failed++;
                    continue;
                }

                LeadResponse response = createLead(request);

                results.add(
                        BulkItemResponse.<LeadResponse>builder()
                                .success(true)
                                .data(response)
                                .build()
                );

                successful++;

            } catch (Exception ex) {

                results.add(
                        BulkItemResponse.<LeadResponse>builder()
                                .success(false)
                                .error(ex.getMessage())
                                .build()
                );

                failed++;
            }
        }

        return BulkOperationResponse.<LeadResponse>builder()
                .total(requests.size())
                .successful(successful)
                .failed(failed)
                .results(results)
                .build();
    }



    public BulkOperationResponse <LeadResponse> bulkUpdateLeads(
            List <BulkUpdateLeadRequest> requests) {
        List <BulkItemResponse<LeadResponse>> results = new ArrayList<>();
        int successful = 0;
        int failed = 0;
        for (BulkUpdateLeadRequest request: requests) {
            try {
                Set < ConstraintViolation <BulkUpdateLeadRequest>> violations = validator.validate(request);
                if (!violations.isEmpty()) {

                    String errorMessage = violations.stream()
                            .map(v -> v.getPropertyPath() + ": " + v.getMessage())
                            .collect(Collectors.joining(", "));

                    results.add(BulkItemResponse.<LeadResponse> builder()
                            .success(false)
                            .error(errorMessage).build());

                    failed++;
                    continue;
                }
                UpdateLeadRequest updateRequest = UpdateLeadRequest.builder()
                        .name(request.getName())
                        .email(request.getEmail())
                        .phone(request.getPhone())
                        .source(request.getSource())
                        .build();

                //Adding Manual UUID Parsing as request.getId() is now a string to support partial success.
                UUID leadId;
                try {
                    leadId = UUID.fromString(request.getId());
                } catch (IllegalArgumentException ex) {
                    results.add(BulkItemResponse. < LeadResponse > builder().success(false).error("Invalid UUID format: " + request.getId()).build());
                    failed++;
                    continue;
                }
                LeadResponse response = updateLead(leadId, updateRequest);

                results.add(BulkItemResponse.<LeadResponse> builder().success(true).data(response).build());
                successful++;
            } catch (Exception ex) {
                results.add(BulkItemResponse. <LeadResponse>builder()
                        .success(false)
                        .error(ex.getMessage()).build());
                failed++;
            }
        }
        return BulkOperationResponse.<LeadResponse>builder()
                .total(requests.size())
                .successful(successful)
                .failed(failed)
                .results(results)
                .build();
    }

}