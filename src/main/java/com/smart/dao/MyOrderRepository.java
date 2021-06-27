package com.smart.dao;

import com.smart.entity.MyOrder;
import org.springframework.data.jpa.repository.JpaRepository;

public interface MyOrderRepository extends JpaRepository<MyOrder, Long> {

    public MyOrder findByOrderId(String orderId);
}
