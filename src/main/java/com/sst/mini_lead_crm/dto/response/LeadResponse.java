package com.sst.mini_lead_crm.dto.response;

import com.sst.mini_lead_crm.enums.LeadStatus;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;
import java.util.UUID;

@Getter
@Builder
public class LeadResponse {

    private UUID id;

    private String name;

    private String email;

    private String phone;

    private LeadStatus status;

    private String source;

    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;
}
