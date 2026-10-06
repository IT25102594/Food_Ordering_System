package com.example.food_ordering_system.service;

import com.example.food_ordering_system.dto.*;
import com.example.food_ordering_system.entity.*;
import com.example.food_ordering_system.repository.*;
import com.example.food_ordering_system.util.CheckoutCalculatorUtil;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
public class OrderService {

    // an order that exists but hasn't been paid for yet, the kitchen never sees these
    public static final String PENDING_PAYMENT = "pending_payment";

    private final OrderRepository orderRepository;
    private final OrderItemRepository orderItemRepository;
    private final UserRepository userRepository;
    private final RestaurantRepository restaurantRepository;
    private final AddressRepository addressRepository;
    private final FoodVariantRepository foodVariantRepository;
    private final OrderStatusHistoryRepository orderStatusHistoryRepository;
    private final CartRepository cartRepository;
    private final CheckoutCalculatorUtil checkoutCalculator;
    private final ItemDiscountRepository itemDiscountRepo;

    public OrderService(OrderRepository orderRepository,
                        OrderItemRepository orderItemRepository,
                        UserRepository userRepository,
                        RestaurantRepository restaurantRepository,
                        AddressRepository addressRepository,
                        FoodVariantRepository foodVariantRepository,
                        OrderStatusHistoryRepository orderStatusHistoryRepository,
                        CartRepository cartRepository,
                        CheckoutCalculatorUtil checkoutCalculator,
                        ItemDiscountRepository itemDiscountRepo) {
        this.orderRepository = orderRepository;
        this.orderItemRepository = orderItemRepository;
        this.userRepository = userRepository;
        this.restaurantRepository = restaurantRepository;
        this.addressRepository = addressRepository;
        this.foodVariantRepository = foodVariantRepository;
        this.orderStatusHistoryRepository = orderStatusHistoryRepository;
        this.cartRepository = cartRepository;
        this.checkoutCalculator = checkoutCalculator;
        this.itemDiscountRepo = itemDiscountRepo;
    }

    // ============================================================
    // the old one-shot flow, kept so the existing /orders endpoint still compiles.
    // lock the endpoint down once card payment is live
    // ============================================================
    @Transactional
    public String createOrder(OrderRequestDto dto) {
        // no try/catch here, a failure must roll back and surface its real message
        Order order = createPendingOrder(dto);
        markOrderPaid(order.getId());
        return "Success: Order #" + order.getId() + " placed successfully for Rs. " + order.getTotalAmount();
    }

    // ============================================================
    // step 1 of paying by card: validate everything and save the order as pending.
    // no stock is taken and the cart stays put, so cancelling is free
    // ============================================================
    @Transactional
    public Order createPendingOrder(OrderRequestDto dto) {
        User user = userRepository.findById(dto.getUserId())
                .orElseThrow(() -> new IllegalArgumentException("Error: User not found."));
        Restaurant restaurant = restaurantRepository.findById(dto.getRestaurantId())
                .orElseThrow(() -> new IllegalArgumentException("Error: Restaurant not found."));

        // checked first so we never save an empty order
        if (dto.getItems() == null || dto.getItems().isEmpty()) {
            throw new IllegalArgumentException("Error: Order must contain at least one item.");
        }

        // ---------- where is it going? a saved address, or a pin the customer dropped ----------
        Address deliveryAddress = null;
        String addressSnapshot;
        BigDecimal lat = dto.getDeliveryLatitude();
        BigDecimal lng = dto.getDeliveryLongitude();

        if (dto.getDeliveryAddressId() != null) {
            deliveryAddress = addressRepository.findById(dto.getDeliveryAddressId())
                    .orElseThrow(() -> new IllegalArgumentException("Error: Delivery address not found."));
            if (!deliveryAddress.getUser().getId().equals(dto.getUserId())) {
                throw new IllegalArgumentException("Error: Unauthorized address access.");
            }

            addressSnapshot = deliveryAddress.getAddressLine() + ", " + deliveryAddress.getCity();
            if (deliveryAddress.getPostalCode() != null) {
                addressSnapshot += " " + deliveryAddress.getPostalCode();
            }

            // fall back to the address's own pin if the page didn't send one
            if (lat == null || lng == null) {
                lat = deliveryAddress.getLatitude();
                lng = deliveryAddress.getLongitude();
            }
        } else {
            if (dto.getDeliveryAddressText() == null || dto.getDeliveryAddressText().isBlank()) {
                throw new IllegalArgumentException("Error: Please describe where to deliver.");
            }
            addressSnapshot = dto.getDeliveryAddressText().trim();
        }

        if (lat == null || lng == null) throw new IllegalArgumentException("Error: A delivery location is required.");

        // ---------- check every item before anything is saved ----------
        List<FoodVariant> variants = new ArrayList<>();
        List<CheckoutCalculatorUtil.Line> lines = new ArrayList<>();

        for (OrderItemRequestDto itemDto : dto.getItems()) {
            if (itemDto.getQuantity() == null || itemDto.getQuantity() < 1) {
                throw new IllegalArgumentException("Error: Every item needs a quantity of at least 1.");
            }

            FoodVariant variant = foodVariantRepository.findById(itemDto.getVariantId())
                    .orElseThrow(() -> new IllegalArgumentException("Error: Invalid food variant ID " + itemDto.getVariantId()));

            if (!variant.getFoodItem().getRestaurant().getId().equals(dto.getRestaurantId())) {
                throw new IllegalArgumentException("Error: Item " + variant.getFoodItem().getName() + " does not belong to this restaurant.");
            }
            if (!variant.getAvailabilityStatus()) {
                throw new IllegalArgumentException("Error: Variant " + variant.getVariantName() + " is currently unavailable.");
            }
            // -1 means infinite stock
            if (variant.getStockQuantity() != -1 && variant.getStockQuantity() < itemDto.getQuantity()) {
                throw new IllegalArgumentException("Error: Not enough stock for " + variant.getFoodItem().getName() + " (" + variant.getVariantName() + "). Only " + variant.getStockQuantity() + " left.");
            }

            variants.add(variant);
            lines.add(new CheckoutCalculatorUtil.Line(variant, itemDto.getQuantity()));
        }

        // ---------- price it with the same engine the checkout summary uses ----------
        CheckoutCalculatorUtil.CheckoutSummary calc = checkoutCalculator.calculate(lines, dto.getAppliedItemDiscountIds());

        Order order = new Order();
        order.setUser(user);
        order.setRestaurant(restaurant);
        order.setDeliveryAddress(deliveryAddress);   // stays null for pin orders
        order.setStatus(PENDING_PAYMENT);
        order.setDeliveryInstructions(dto.getDeliveryInstructions());
        order.setDeliveryAddressSnapshot(addressSnapshot);
        order.setDeliveryLatitude(lat);
        order.setDeliveryLongitude(lng);
        order.setDeliveryFee(calc.getDeliveryFee());
        order.setTotalAmount(calc.getGrandTotal());
        order = orderRepository.save(order);

        // ---------- save the items at the price the customer will actually pay ----------
        List<OrderItem> savedItems = new ArrayList<>();
        for (int i = 0; i < dto.getItems().size(); i++) {
            OrderItemRequestDto itemDto = dto.getItems().get(i);
            FoodVariant variant = variants.get(i);
            BigDecimal unitPrice = calc.getUnitPrices().get(i);

            OrderItem orderItem = new OrderItem();
            orderItem.setOrder(order);
            orderItem.setVariant(variant);
            orderItem.setQuantity(itemDto.getQuantity());
            orderItem.setSpecialInstructions(itemDto.getSpecialInstructions());
            orderItem.setItemNameSnapshot(variant.getFoodItem().getName());
            orderItem.setVariantNameSnapshot(variant.getVariantName());
            orderItem.setPriceAtOrderTime(unitPrice);
            orderItem.setSubtotal(unitPrice.multiply(new BigDecimal(itemDto.getQuantity())));
            savedItems.add(orderItem);
        }
        orderItemRepository.saveAll(savedItems);

        return order;
    }

    // ============================================================
    // step 2: the money is in. make the order real.
    // ============================================================
    @Transactional
    public Order markOrderPaid(Integer orderId) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new IllegalArgumentException("Error: Order not found."));

        // already placed (a page refresh, or stripe told us twice), nothing more to do
        if (!PENDING_PAYMENT.equals(order.getStatus())) return order;

        List<OrderItem> items = orderItemRepository.findByOrder_Id(orderId);

        // stock is checked again, it may have changed while the customer was on the stripe page
        for (OrderItem item : items) {
            FoodVariant variant = item.getVariant();
            if (variant.getStockQuantity() != -1 && variant.getStockQuantity() < item.getQuantity()) {
                throw new IllegalStateException("Sorry, " + item.getItemNameSnapshot() + " (" + item.getVariantNameSnapshot() + ") just sold out. Your payment has been refunded.");
            }
        }

        // now it's safe to take the stock off
        for (OrderItem item : items) {
            FoodVariant variant = item.getVariant();
            if (variant.getStockQuantity() != -1) {
                variant.setStockQuantity(variant.getStockQuantity() - item.getQuantity());
                foodVariantRepository.save(variant);
            }
        }

        order.setStatus("placed");
        orderRepository.save(order);
        recordHistory(order, "placed");

        // the cart has done its job
        cartRepository.findByUserId(order.getUser().getId()).ifPresent(cartRepository::delete);
        return order;
    }

    // the customer backed out of paying, drop the pending order and leave their cart alone
    @Transactional
    public void cancelPendingOrder(Integer orderId) {
        orderRepository.findById(orderId).ifPresent(order -> {
            if (PENDING_PAYMENT.equals(order.getStatus())) {
                order.setStatus("cancelled");
                orderRepository.save(order);
                recordHistory(order, "cancelled");
            }
        });
    }

    private void recordHistory(Order order, String status) {
        OrderStatusHistory history = new OrderStatusHistory();
        history.setOrder(order);
        history.setStatus(status);
        orderStatusHistoryRepository.save(history);
    }

    // ============================================================
    // reading orders (unpaid ones are hidden from customers and the kitchen)
    // ============================================================
    public List<OrderResponseDto> getUserOrders(Integer userId) {
        return orderRepository.findByUser_Id(userId).stream()
                .filter(o -> !PENDING_PAYMENT.equals(o.getStatus()))
                .map(this::mapToResponseDto)
                .toList();
    }

    // Fetch orders for the Kitchen Dashboard queue
    public List<OrderResponseDto> getRestaurantOrders(Integer restaurantId, String status) {
        List<Order> orders;
        if (status != null && !status.isEmpty()) {
            orders = orderRepository.findByRestaurant_IdAndStatus(restaurantId, status);
        } else {
            orders = orderRepository.findByRestaurant_Id(restaurantId);
        }
        return orders.stream()
                .filter(o -> !PENDING_PAYMENT.equals(o.getStatus()))
                .map(this::mapToResponseDto)
                .toList();
    }

    // Update status from the Kitchen Dashboard
    @Transactional
    public String updateOrderStatus(Integer orderId, String newStatus, Integer requesterId) {
        Optional<Order> orderOpt = orderRepository.findById(orderId);
        if (orderOpt.isEmpty()) return "Error: Order not found.";

        Order order = orderOpt.get();
        order.setStatus(newStatus);
        orderRepository.save(order);
        recordHistory(order, newStatus);

        return "Success: Order status updated to " + newStatus;
    }

    private OrderResponseDto mapToResponseDto(Order order) {
        OrderResponseDto dto = new OrderResponseDto();
        dto.setOrderId(order.getId());
        dto.setRestaurantId(order.getRestaurant().getId());
        dto.setRestaurantName(order.getRestaurant().getName());
        dto.setStatus(order.getStatus());
        dto.setPlacedAt(order.getPlacedAt());
        dto.setDeliveryAddressSnapshot(order.getDeliveryAddressSnapshot());
        dto.setDeliveryInstructions(order.getDeliveryInstructions());
        dto.setDeliveryFee(order.getDeliveryFee());
        dto.setTotalAmount(order.getTotalAmount());
        dto.setDeliveryLatitude(order.getDeliveryLatitude());
        dto.setDeliveryLongitude(order.getDeliveryLongitude());
        if (order.getDriver() != null) {
            dto.setDriverId(order.getDriver().getId());
        }

        List<OrderItemResponseDto> itemDtos = orderItemRepository.findByOrder_Id(order.getId()).stream().map(item -> {
            OrderItemResponseDto itemDto = new OrderItemResponseDto();
            itemDto.setOrderItemId(item.getId());

            // ---> Added Mapping <---
            if (item.getVariant() != null) {
                itemDto.setVariantId(item.getVariant().getId());
            }

            itemDto.setItemName(item.getItemNameSnapshot());
            itemDto.setVariantName(item.getVariantNameSnapshot());
            itemDto.setQuantity(item.getQuantity());
            itemDto.setPriceAtOrderTime(item.getPriceAtOrderTime());
            itemDto.setSubtotal(item.getSubtotal());
            itemDto.setSpecialInstructions(item.getSpecialInstructions());
            return itemDto;
        }).toList();

        dto.setItems(itemDtos);
        return dto;
    }

    public CheckoutSummaryResponseDto getCheckoutSummary(Integer userId, List<Integer> appliedDiscountIds) {
        // 1. Fetch the user's active cart
        Cart cart = cartRepository.findByUserId(userId)
                .orElseThrow(() -> new RuntimeException("Cart not found for user."));

        // 2. Run the math through our dedicated pricing engine
        CheckoutCalculatorUtil.CheckoutSummary calcResult = checkoutCalculator.calculateTotal(cart.getItems(), appliedDiscountIds);

        // 3. Find all available discounts for items in this cart so the UI can draw the cards
        List<Integer> cartFoodItemIds = cart.getItems().stream()
                .map(item -> item.getFoodVariant().getFoodItem().getId())
                .distinct()
                .toList();

        List<AvailableDiscountDto> availableDiscounts = itemDiscountRepo.findActiveDiscountsForItems(cartFoodItemIds, Instant.now())
                .stream()
                .map((ItemDiscount discount) -> {
                    AvailableDiscountDto dDto = new AvailableDiscountDto();
                    dDto.setDiscountId(discount.getId());
                    dDto.setFoodItemId(discount.getFoodItem().getId());
                    dDto.setFoodItemName(discount.getFoodItem().getName());
                    dDto.setPercentage(discount.getPercentage());
                    dDto.setDescription(discount.getPercentage().stripTrailingZeros().toPlainString() + "% off " + discount.getFoodItem().getName() + "!");
                    return dDto;
                }).toList();

        // 4. Package it all up for the frontend
        CheckoutSummaryResponseDto response = new CheckoutSummaryResponseDto();
        response.setSubtotal(calcResult.getSubtotal());
        response.setDiscountTotal(calcResult.getItemDiscountTotal());
        response.setDeliveryFee(calcResult.getDeliveryFee());
        response.setDeliveryDiscountPercent(calcResult.getDeliveryDiscountPercent());
        response.setDeliveryDiscountAmount(calcResult.getDeliveryDiscountAmount());
        response.setGrandTotal(calcResult.getGrandTotal());
        response.setAvailableItemDiscounts(availableDiscounts);

        return response;
    }
}