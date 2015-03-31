package com.hubspot.baragon.service.resources;

import java.util.Arrays;
import java.util.List;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

import com.amazonaws.AmazonClientException;
import com.amazonaws.services.elasticloadbalancing.AmazonElasticLoadBalancingClient;
import com.amazonaws.services.elasticloadbalancing.model.DescribeLoadBalancersRequest;
import com.amazonaws.services.elasticloadbalancing.model.DescribeLoadBalancersResult;
import com.amazonaws.services.elasticloadbalancing.model.LoadBalancerDescription;
import com.google.common.base.Optional;
import com.google.inject.Inject;
import com.google.inject.name.Named;
import com.hubspot.baragon.BaragonDataModule;
import com.hubspot.baragon.config.ElbConfiguration;
import com.hubspot.baragon.service.exceptions.BaragonNotFoundException;
import com.hubspot.baragon.service.exceptions.BaragonWebException;

@Path("/elbs")
@Produces(MediaType.APPLICATION_JSON)
public class ElbResource {
  private final AmazonElasticLoadBalancingClient elbClient;
  private final Optional<ElbConfiguration> config;

  @Inject
  public ElbResource(@Named(BaragonDataModule.BARAGON_AWS_ELB_CLIENT)AmazonElasticLoadBalancingClient elbClient,
                     Optional<ElbConfiguration> config) {
    this.elbClient = elbClient;
    this.config = config;
  }


  @GET
  public List<LoadBalancerDescription> getAllElbs() {
    if (config.isPresent()) {
      return elbClient.describeLoadBalancers().getLoadBalancerDescriptions();
    } else {
      throw new BaragonWebException("ElbSync is not currently enabled");
    }
  }

  @GET
  @Path("/{elbName}")
  public LoadBalancerDescription getElb(@PathParam("elbName") String elbName) {
    if (config.isPresent()) {
      try {
        DescribeLoadBalancersRequest request = new DescribeLoadBalancersRequest(Arrays.asList(elbName));
        DescribeLoadBalancersResult result = elbClient.describeLoadBalancers(request);
        for (LoadBalancerDescription elb : result.getLoadBalancerDescriptions()) {
          if (elb.getLoadBalancerName().equals(elbName)) {
            return elb;
          }
        }
      } catch (AmazonClientException e) {
        throw new BaragonWebException(String.format("AWS Client Error: %s", e));
      }
      throw new BaragonNotFoundException(String.format("ELB with name %s not found", elbName));
    } else {
      throw new BaragonWebException("ElbSync is not currently enabled");
    }
  }
}
