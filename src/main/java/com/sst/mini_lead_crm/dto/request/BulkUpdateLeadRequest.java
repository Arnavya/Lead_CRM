package com.sst.mini_lead_crm.dto.request;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class BulkUpdateLeadRequest {

    @NotBlank(message = "Lead id is required")
    private String id;

    @Size(min = 1, max = 100, message = "Name must be between 1 and 100 characters")
    private String name;

    @Email(message = "Invalid email format")
    private String email;

    private String phone;

    private String source;
}
