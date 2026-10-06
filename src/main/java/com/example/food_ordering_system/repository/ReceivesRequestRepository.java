package com.example.food_ordering_system.repository;

import com.example.food_ordering_system.entity.ReceivesRequest;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.util.List;
import java.util.Optional;

public interface ReceivesRequestRepository extends JpaRepository<ReceivesRequest, Integer> {

    Optional<ReceivesRequest> findByRequest_IdAndDriver_Id(Integer requestId, Integer driverId);

    List<ReceivesRequest> findByDriver_IdAndStatus(Integer driverId, String status);

    // once one rider accepts, everyone else's open offer for this order closes
    @Modifying
    @Query("update ReceivesRequest r set r.status = 'expired' where r.request.id = :requestId and r.status = 'offered' and r.driver.id <> :driverId")
    void expireOthers(@Param("requestId") Integer requestId, @Param("driverId") Integer driverId);
}