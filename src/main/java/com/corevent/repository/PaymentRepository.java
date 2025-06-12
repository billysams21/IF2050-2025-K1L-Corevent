package com.corevent.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.corevent.entity.Payment;

@Repository
public interface PaymentRepository extends JpaRepository<Payment, String> {
} 