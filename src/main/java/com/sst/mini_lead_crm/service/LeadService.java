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

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class LeadService {

    private final LeadRepository leadRepository;
    private final LeadMapper leadMapper;

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

        Lead lead = leadRepository.findById(id)
                .orElseThrow(() ->
                        new ResourceNotFoundException("Lead not found with id: " + id)
                );

        return leadMapper.entityToResponse(lead);
    }

    @Transactional
    public LeadResponse updateLead(UUID id, UpdateLeadRequest request) {

        Lead lead = leadRepository.findById(id)
                .orElseThrow(() ->
                        new ResourceNotFoundException("Lead not found with id: " + id)
                );

        leadMapper.updateEntityFromRequest(request, lead);

        Lead updatedLead = leadRepository.save(lead);

        return leadMapper.entityToResponse(updatedLead);
    }

    @Transactional
    public void deleteLead(UUID id) {

        Lead lead = leadRepository.findById(id)
                .orElseThrow(() ->
                        new ResourceNotFoundException("Lead not found with id: " + id)
                );

        leadRepository.delete(lead);
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

        return leadMapper.entityToResponse(updatedLead);
    }
}