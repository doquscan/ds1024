package com.rental.tool.repository;

import com.rental.tool.entities.ToolCharge;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ToolChargeRepository extends JpaRepository<ToolCharge, String> {

}

