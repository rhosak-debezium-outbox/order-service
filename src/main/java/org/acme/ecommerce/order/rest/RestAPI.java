package org.acme.ecommerce.order.rest;

import java.net.URI;
import javax.inject.Inject;
import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.acme.ecommerce.order.model.Order;
import org.acme.ecommerce.order.service.OrderService;

@Path("/order")
public class RestAPI {

    @Inject
    OrderService service;

    @POST
    @Path("/")
    @Consumes(MediaType.APPLICATION_JSON)
    public Response create(Order order) {
        Long id = service.create(order);
        return Response.created(URI.create("order/" + id)).build();
    }

    @GET
    @Path("/{id}")
    @Produces(MediaType.APPLICATION_JSON)
    public Response get(@PathParam("id") Long orderId) {
        Order order = service.get(orderId);
        return order != null ? Response.ok(order).build() : Response.status(Response.Status.NOT_FOUND).build();
    }

}
