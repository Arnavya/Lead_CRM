package com.sst.mini_lead_crm.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class BulkOperationResponse<T> {

    private int total;

    private int successful;

    private int failed;

    private List<BulkItemResponse<T>> results;
}