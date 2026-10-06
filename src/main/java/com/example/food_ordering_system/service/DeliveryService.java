package com.example.food_ordering_system.service;

import com.example.food_ordering_system.dto.DriverJobsDto;
import com.example.food_ordering_system.dto.DriverJobsDto.Job;
import com.example.food_ordering_system.entity.*;
import com.example.food_ordering_system.repository.*;
import com.example.food_ordering_system.util.RoadDistanceUtil;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

@Service
public class DeliveryService {

    // a delivery the rider has taken but not finished: heading to the restaurant, or on the way to the customer
    private static final List<String> ACTIVE = List.of("ready", "out_for_delivery");

    private final OrderRepository orderRepo;
    private final OrderItemRepository orderItemRepo;
    private final DeliveryDriverRepository driverRepo;
    private final DeliveryRequestRepository requestRepo;
    private final ReceivesRequestRepository offerRepo;
    private final OrderService orderService;
    private final RoadDistanceUtil roadDistance;

    public DeliveryService(OrderRepository orderRepo, OrderItemRepository orderItemRepo,
                           DeliveryDriverRepository driverRepo, DeliveryRequestRepository requestRepo,
                           ReceivesRequestRepository offerRepo, OrderService orderService,
                           RoadDistanceUtil roadDistance) {
        this.orderRepo = orderRepo;
        this.orderItemRepo = orderItemRepo;
        this.driverRepo = driverRepo;
        this.requestRepo = requestRepo;
        this.offerRepo = offerRepo;
        this.orderService = orderService;
        this.roadDistance = roadDistance;
    }

    // ---------- kitchen side ----------

    // offers the order to every approved, online rider. Safe to call again, it just re-opens the offers
    @Transactional
    public int sendRequest(Integer orderId) {
        Order order = orderRepo.findById(orderId)
                .orElseThrow(() -> new IllegalArgumentException("Error: Order not found."));

        DeliveryRequest request = requestRepo.findByOrder_Id(orderId).orElseGet(() -> {
            DeliveryRequest r = new DeliveryRequest();
            r.setOrder(order);
            return requestRepo.save(r);
        });

        List<DeliveryDriver> riders = driverRepo.findByApprovalStatusAndIsOnline("approved", true);
        for (DeliveryDriver rider : riders) {
            // the table allows one row per rider per request, so a resend reuses the row
            ReceivesRequest offer = offerRepo.findByRequest_IdAndDriver_Id(request.getId(), rider.getId())
                    .orElseGet(ReceivesRequest::new);
            offer.setRequest(request);
            offer.setDriver(rider);
            offer.setStatus("offered");
            offer.setRequestedAt(Instant.now());
            offerRepo.save(offer);
        }
        return riders.size();
    }

    // the "send another request" button on the kitchen board
    @Transactional
    public String resend(Integer orderId) {
        Order order = orderRepo.findById(orderId).orElse(null);
        if (order == null) return "Error: Order not found.";
        if (!"ready".equals(order.getStatus())) return "Error: Only ready orders can be sent to riders.";
        if (order.getDriver() != null) return "Error: A rider has already accepted this order.";

        int sent = sendRequest(orderId);
        return sent == 0 ? "Error: No riders are online right now." : "Success: Request sent to " + sent + " rider(s).";
    }

    // ---------- rider side ----------

    // polled every few seconds: the current delivery, or else the open offers
    @Transactional(readOnly = true)
    public DriverJobsDto getJobs(Integer driverId) {
        DeliveryDriver driver = driverRepo.findById(driverId).orElse(null);
        if (driver == null) return new DriverJobsDto(null, List.of());

        Job active = orderRepo.findFirstByDriver_IdAndStatusIn(driverId, ACTIVE)
                .map(o -> toJob(o, requestRepo.findByOrder_Id(o.getId())
                        .map(DeliveryRequest::getDriverToRestaurantDistance).orElse(null)))
                .orElse(null);

        List<Job> offers = new ArrayList<>();
        // a busy or offline rider is not shown new offers
        if (active == null && Boolean.TRUE.equals(driver.getIsOnline())) {
            for (ReceivesRequest offer : offerRepo.findByDriver_IdAndStatus(driverId, "offered")) {
                Order o = offer.getRequest().getOrder();
                // skip offers that went stale between polls
                if (!"ready".equals(o.getStatus()) || o.getDriver() != null) continue;
                offers.add(toJob(o, estimateKm(driver, o.getRestaurant())));
            }
        }
        return new DriverJobsDto(active, offers);
    }

    @Transactional
    public String accept(Integer orderId, Integer driverId) {
        DeliveryDriver driver = driverRepo.findById(driverId).orElse(null);
        if (driver == null || !"approved".equals(driver.getApprovalStatus())) return "Error: You are not an approved rider.";
        if (orderRepo.findFirstByDriver_IdAndStatusIn(driverId, ACTIVE).isPresent()) return "Error: Finish your current delivery first.";

        DeliveryRequest request = requestRepo.findByOrder_Id(orderId).orElse(null);
        if (request == null) return "Error: This order has no delivery request.";
        ReceivesRequest offer = offerRepo.findByRequest_IdAndDriver_Id(request.getId(), driverId).orElse(null);
        if (offer == null || !"offered".equals(offer.getStatus())) return "Error: This offer is no longer available.";

        // the database decides the winner, so two riders tapping together can't both get it
        if (orderRepo.claim(orderId, driver) == 0) {
            offer.setStatus("expired");
            return "Error: Another rider already took this order.";
        }

        offer.setStatus("accepted");
        offerRepo.expireOthers(request.getId(), driverId);

        // the real road distance, worked out once and kept on the request
        Restaurant restaurant = orderRepo.findById(orderId).orElseThrow().getRestaurant();
        if (driver.getLatitude() != null && restaurant.getLatitude() != null) {
            request.setDriverToRestaurantDistance(roadDistance.roadKm(
                    driver.getLatitude().doubleValue(), driver.getLongitude().doubleValue(),
                    restaurant.getLatitude().doubleValue(), restaurant.getLongitude().doubleValue()));
        }
        return "Success: Delivery accepted.";
    }

    @Transactional
    public String decline(Integer orderId, Integer driverId) {
        DeliveryRequest request = requestRepo.findByOrder_Id(orderId).orElse(null);
        if (request == null) return "Error: This order has no delivery request.";
        offerRepo.findByRequest_IdAndDriver_Id(request.getId(), driverId).ifPresent(offer -> {
            if ("offered".equals(offer.getStatus())) offer.setStatus("declined");
        });
        return "Success: Offer declined.";
    }

    // picked up, then delivered. Only the rider who owns the order can move it, and only forward
    @Transactional
    public String advance(Integer orderId, Integer driverId, String newStatus) {
        Order order = orderRepo.findById(orderId).orElse(null);
        if (order == null || order.getDriver() == null || !order.getDriver().getId().equals(driverId)) {
            return "Error: This is not your delivery.";
        }
        boolean allowed = ("out_for_delivery".equals(newStatus) && "ready".equals(order.getStatus()))
                || ("delivered".equals(newStatus) && "out_for_delivery".equals(order.getStatus()));
        if (!allowed) return "Error: That step isn't allowed right now.";

        return orderService.updateOrderStatus(orderId, newStatus, driverId);
    }

    // ---------- helpers ----------

    // straight-line estimate, cheap enough to run on every poll
    private BigDecimal estimateKm(DeliveryDriver driver, Restaurant restaurant) {
        if (driver.getLatitude() == null || restaurant.getLatitude() == null) return null;
        return roadDistance.straightKm(
                driver.getLatitude().doubleValue(), driver.getLongitude().doubleValue(),
                restaurant.getLatitude().doubleValue(), restaurant.getLongitude().doubleValue());
    }

    private Job toJob(Order o, BigDecimal driverKm) {
        Restaurant r = o.getRestaurant();
        int itemCount = orderItemRepo.findByOrder_Id(o.getId()).stream().mapToInt(OrderItem::getQuantity).sum();
        return new Job(o.getId(), o.getStatus(), r.getName(), r.getAddressLine() + ", " + r.getCity(),
                r.getLatitude(), r.getLongitude(),
                o.getDeliveryAddressSnapshot(), o.getDeliveryLatitude(), o.getDeliveryLongitude(),
                o.getDeliveryInstructions(), driverKm, o.getRestaurantToCustomerDistance(),
                o.getDeliveryFee(), itemCount);
    }
}