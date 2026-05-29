package com.sst.mini_lead_crm.mapper;

import com.sst.mini_lead_crm.dto.request.CreateLeadRequest;
import com.sst.mini_lead_crm.dto.request.UpdateLeadRequest;
import com.sst.mini_lead_crm.dto.response.LeadResponse;
import com.sst.mini_lead_crm.entity.Lead;
import org.springframework.stereotype.Component;

@Component

public class LeadMapper {

    public Lead createRequestToEntity(CreateLeadRequest request) {
        return Lead.builder()
                .name(request.getName())
                .email(request.getEmail())
                .phone(request.getPhone())
                .source(request.getSource())
                .build();
    }

    public LeadResponse entityToResponse(Lead lead) {
        return LeadResponse.builder()
                .id(lead.getId())
                .name(lead.getName())
                .email(lead.getEmail())
                .phone(lead.getPhone())
                .status(lead.getStatus())
                .source(lead.getSource())
                .createdAt(lead.getCreatedAt())
                .updatedAt(lead.getUpdatedAt())
                .build();
    }

    public void updateEntityFromRequest(UpdateLeadRequest request, Lead lead) {

        if (request.getName() != null) {
            lead.setName(request.getName());
        }

        if (request.getEmail() != null) {
            lead.setEmail(request.getEmail());
        }

        if (request.getPhone() != null) {
            lead.setPhone(request.getPhone());
        }

        if (request.getSource() != null) {
            lead.setSource(request.getSource());
        }
    }
}

