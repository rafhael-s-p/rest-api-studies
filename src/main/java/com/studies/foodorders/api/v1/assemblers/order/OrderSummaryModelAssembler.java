package com.studies.foodorders.api.v1.assemblers.order;

import com.studies.foodorders.api.v1.controllers.order.OrderController;
import com.studies.foodorders.api.v1.links.OrderLinks;
import com.studies.foodorders.api.v1.links.RestaurantLinks;
import com.studies.foodorders.api.v1.links.UserLinks;
import com.studies.foodorders.api.v1.models.order.OrderSummaryModel;
import com.studies.foodorders.core.security.ApiSecurity;
import com.studies.foodorders.domain.models.order.Order;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.hateoas.server.mvc.RepresentationModelAssemblerSupport;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.stream.Collectors;

@Component
public class OrderSummaryModelAssembler extends RepresentationModelAssemblerSupport<Order, OrderSummaryModel> {

    @Autowired
    private ModelMapper modelMapper;

    @Autowired
    private OrderLinks orderLinks;

    @Autowired
    private RestaurantLinks restaurantLinks;

    @Autowired
    private UserLinks userLinks;

    @Autowired
    private ApiSecurity apiSecurity;

    public OrderSummaryModelAssembler() {
        super(OrderController.class, OrderSummaryModel.class);
    }

    public OrderSummaryModel toModel(Order order) {

        OrderSummaryModel orderModel = createModelWithId(order.getCode(), order);
        modelMapper.map(order, orderModel);

        if (apiSecurity.isAllowedToSearchOrders())
            orderModel.add(orderLinks.linkToOrders("orders"));

        if (apiSecurity.isAllowedToSearchRestaurants())
            orderModel.getRestaurant().add(restaurantLinks.linkToRestaurant(order.getRestaurant().getId()));

        if (apiSecurity.isAllowedToSearchUsersGroupsPermissions())
            orderModel.getClient().add(userLinks.linkToUser(order.getClient().getId()));

        return orderModel;
    }

    public List<OrderSummaryModel> toCollectionModel(List<Order> orders) {
        return orders.stream()
                .map(order -> toModel(order))
                .collect(Collectors.toList());
    }

}
