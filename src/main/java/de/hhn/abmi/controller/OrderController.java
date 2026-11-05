package de.hhn.abmi.controller;

import de.hhn.abmi.dto.api.GoodsReceiptResponse;
import de.hhn.abmi.dto.api.OrderProposalResponse;
import de.hhn.abmi.dto.api.PurchaseOrderResponse;
import de.hhn.abmi.service.OrderService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * REST endpoints for reordering medications of a station.
 *
 * <p>Only maps HTTP to service calls; the logic is in {@link OrderService}.
 */
@RestController
@RequestMapping("/api/orders")
public class OrderController {
    private final OrderService orderService;

    public OrderController(OrderService orderService) {
        this.orderService = orderService;
    }

    /** Shows what would be ordered, without ordering anything. */
    @GetMapping("/proposals")
    public List<OrderProposalResponse> getProposals(@RequestParam String station) {
        return orderService.getProposals(station);
    }

    /** Orders everything the proposals say. */
    @PostMapping
    public List<PurchaseOrderResponse> placeOrders(@RequestParam String station) {
        return orderService.placeOrders(station);
    }

    /** Lists the station's orders, open ones first. */
    @GetMapping
    public List<PurchaseOrderResponse> getOrders(@RequestParam String station) {
        return orderService.getOrders(station);
    }

    /** Books the delivery of an order. */
    @PostMapping("/{purchaseOrder}/goods-receipt")
    public GoodsReceiptResponse receive(@RequestParam String station, @PathVariable String purchaseOrder) {
        return orderService.receive(station, purchaseOrder);
    }
}
