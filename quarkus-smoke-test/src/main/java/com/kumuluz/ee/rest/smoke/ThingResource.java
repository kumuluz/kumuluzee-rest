package com.kumuluz.ee.rest.smoke;

import com.kumuluz.ee.rest.beans.QueryParameters;
import com.kumuluz.ee.rest.utils.JPAUtils;
import jakarta.inject.Inject;
import jakarta.persistence.EntityManager;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.Context;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.core.UriInfo;

import java.util.List;

@Path("/things")
@Produces(MediaType.APPLICATION_JSON)
public class ThingResource {

    @Inject
    EntityManager em;

    @Context
    UriInfo uriInfo;

    /**
     * Returns the ids of the things matching the query string, e.g. {@code ?filter=name:like:'a%'&order=id DESC},
     * with the total count in {@code X-Total-Count}.
     */
    @GET
    public Response list() {
        QueryParameters q = QueryParameters.queryEncoded(uriInfo.getRequestUri().getRawQuery()).build();

        List<Integer> ids = JPAUtils.queryEntities(em, Thing.class, q).stream().map(Thing::getId).toList();
        long count = JPAUtils.queryEntitiesCount(em, Thing.class, q);

        return Response.ok(ids).header("X-Total-Count", count).build();
    }
}
