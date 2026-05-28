package com.sst.mini_lead_crm.dto.request;

import com.sst.mini_lead_crm.enums.LeadStatus;
import jakarta.validation.constraints.NotNull;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UpdateLeadStatusRequest {

    @NotNull(message = "Status is required")
    private LeadStatus status;
}

