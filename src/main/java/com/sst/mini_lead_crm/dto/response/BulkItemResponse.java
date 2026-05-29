package com.sst.mini_lead_crm.dto.response;
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

public class BulkItemResponse<T> {

    private boolean success;

    private T data;

    private String error;
}