package com.sst.mini_lead_crm.repository;

import com.sst.mini_lead_crm.entity.Lead;
import com.sst.mini_lead_crm.enums.LeadStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface LeadRepository extends JpaRepository<Lead, UUID> {

    List<Lead> findByStatus(LeadStatus status);
}

