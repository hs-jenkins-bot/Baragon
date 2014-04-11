package com.hubspot.baragon.service;

import com.google.common.base.Strings;
import com.google.common.collect.Lists;
import com.hubspot.baragon.models.Service;
import com.hubspot.baragon.models.BaragonRequest;

import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Response;
import java.util.Collection;

public class BaragonValidator {
  private BaragonValidator() {

  }

  public static void validateRequest(BaragonRequest request) {
    final Collection<String> errors = Lists.newArrayList();

    if (Strings.isNullOrEmpty(request.getRequestId())) {
      errors.add("requestId cannot be null or empty");
    }

    if (request.getService() == null) {
      errors.add("service cannot be null");
    } else {
      final Service service = request.getService();

      if (Strings.isNullOrEmpty(service.getId())) {
        errors.add("service.id cannot be null or empty");
      }

      if (service.getLbs() == null || service.getLbs().isEmpty()) {
        errors.add("service.lbs cannot be null or empty");
      }

      if (service.getOwners() == null || service.getOwners().isEmpty()) {
        errors.add("service.owners cannot be null or empty");
      }

      if (Strings.isNullOrEmpty(service.getRoute())) {
        errors.add("service.route cannot be null or empty");
      }
    }

    if (request.getAdd() == null) {
      errors.add("add cannot be null");
    }

    if (request.getRemove() == null) {
      errors.add("remove cannot be null");
    }

    if (!errors.isEmpty()) {
      throw new WebApplicationException(Response.status(Response.Status.BAD_REQUEST).entity(errors).build());
    }
  }
}