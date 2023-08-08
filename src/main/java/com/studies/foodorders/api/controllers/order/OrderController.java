package com.studies.foodorders.api.controllers.order;

import com.studies.foodorders.api.converter.order.OrderModelConverter;
import com.studies.foodorders.api.converter.order.OrderSummaryModelConverter;
import com.studies.foodorders.api.model.order.OrderInput;
import com.studies.foodorders.api.model.order.OrderModel;
import com.studies.foodorders.api.model.order.OrderSummaryModel;
import com.studies.foodorders.domain.exceptions.BusinessException;
import com.studies.foodorders.domain.models.order.Order;
import com.studies.foodorders.domain.models.security.User;
import com.studies.foodorders.domain.repositories.order.OrderRepository;
import com.studies.foodorders.domain.services.order.OrderService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import javax.persistence.EntityNotFoundException;
import javax.validation.Valid;
import java.util.List;

@RestController
@RequestMapping(value = "/orders")
public class OrderController {

    @Autowired
    private OrderRepository orderRepository;

    @Autowired
    private OrderService orderService;

    @Autowired
    private OrderModelConverter orderModelConverter;

    @Autowired
    private OrderSummaryModelConverter orderSummaryModelConverter;

    @GetMapping
    public List<OrderSummaryModel> list() {
        return orderSummaryModelConverter.toCollectionModel(orderRepository.findAll());
    }

    @GetMapping("/{orderId}")
    public OrderModel find(@PathVariable Long orderId) {
        return orderModelConverter.toModel(orderService.findIfExists(orderId));
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public OrderModel add(@Valid @RequestBody OrderInput orderInput) {
        try {
            Order newOrder = orderModelConverter.toDomainObject(orderInput);

            newOrder.setClient(new User());
            newOrder.getClient().setId(1L);

            newOrder = orderService.makeOrder(newOrder);

            return orderModelConverter.toModel(newOrder);
        } catch (EntityNotFoundException e) {
            throw new BusinessException(e.getMessage(), e);
        }
    }

}