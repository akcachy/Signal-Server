/*
 * Copyright 2013-2020 Signal Messenger, LLC
 * SPDX-License-Identifier: AGPL-3.0-only
 */
package org.whispersystems.websocket;

import io.dropwizard.jersey.jackson.JacksonMessageBodyProvider;
import org.eclipse.jetty.websocket.api.UpgradeRequest;
import org.eclipse.jetty.websocket.servlet.ServletUpgradeRequest;
import org.eclipse.jetty.websocket.servlet.ServletUpgradeResponse;
import org.eclipse.jetty.websocket.servlet.WebSocketCreator;
import org.eclipse.jetty.websocket.servlet.WebSocketServlet;
import org.eclipse.jetty.websocket.servlet.WebSocketServletFactory;
import org.glassfish.jersey.server.ApplicationHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.whispersystems.websocket.auth.AuthenticationException;
import org.whispersystems.websocket.auth.WebSocketAuthenticator;
import org.whispersystems.websocket.auth.WebSocketAuthenticator.AuthenticationResult;
import org.whispersystems.websocket.auth.WebsocketAuthValueFactoryProvider;
import org.whispersystems.websocket.session.WebSocketSessionContextValueFactoryProvider;
import org.whispersystems.websocket.setup.WebSocketEnvironment;

import java.io.IOException;
import java.security.Principal;
import java.util.Arrays;
import java.util.Optional;

import java.util.HashMap;
import java.util.Map;
import java.util.List;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import com.fasterxml.jackson.databind.ObjectMapper;
import javax.ws.rs.WebApplicationException;

import static java.util.Optional.ofNullable;

public class WebSocketResourceProviderFactory<T extends Principal> extends WebSocketServlet implements WebSocketCreator {

  private static final Logger logger = LoggerFactory.getLogger(WebSocketResourceProviderFactory.class);

  private final WebSocketEnvironment<T> environment;
  private final ApplicationHandler      jerseyApplicationHandler;
  private final HttpClient              httpClient;
  private static final ObjectMapper mapper = new ObjectMapper();
  public WebSocketResourceProviderFactory(WebSocketEnvironment<T> environment, Class<T> principalClass) {
    this.environment = environment;

    environment.jersey().register(new WebSocketSessionContextValueFactoryProvider.Binder());
    environment.jersey().register(new WebsocketAuthValueFactoryProvider.Binder<T>(principalClass));
    environment.jersey().register(new JacksonMessageBodyProvider(environment.getObjectMapper()));

    this.jerseyApplicationHandler = new ApplicationHandler(environment.jersey());
    this.httpClient = HttpClient.newBuilder().version(HttpClient.Version.HTTP_2).build();
  }

  @Override
  public Object createWebSocket(ServletUpgradeRequest request, ServletUpgradeResponse response) {
    try {
      Optional<WebSocketAuthenticator<T>> authenticator = Optional.ofNullable(environment.getAuthenticator());
      T                                   authenticated = null;
      logger.info("################ WEBSOCKET CREATE ################");
      if (authenticator.isPresent()) {
        AuthenticationResult<T> authenticationResult = authenticator.get().authenticate(request);

        if (authenticationResult.getUser().isEmpty() && authenticationResult.isRequired()) {
          //addLoginHistory(request, "UNAUTHORIZE");
          response.sendForbidden("Unauthorized");
          return null;
        } else {
          authenticated = authenticationResult.getUser().orElse(null);
          if(authenticated != null){
            //addLoginHistory(request, "SUCCESS");
          }
          
        }
      }

      return new WebSocketResourceProvider<T>(getRemoteAddress(request),
                                              this.jerseyApplicationHandler,
                                              this.environment.getRequestLog(),
                                              authenticated,
                                              this.environment.getMessageFactory(),
                                              ofNullable(this.environment.getConnectListener()),
                                              this.environment.getIdleTimeoutMillis());
    } catch (WebApplicationException ee) {
      try {
        response.sendError(getResponse().getStatus(), ee.getMessage());
      } catch (IOException ex) {}
      return null;
    } catch (AuthenticationException | IOException e) {
      logger.warn("Authentication failure", e);
      try {
        response.sendError(500, "Failure");
      } catch (IOException ex) {}
      return null;
    }
  }

  @Override
  public void configure(WebSocketServletFactory factory) {
    factory.setCreator(this);
  }

  private String getRemoteAddress(ServletUpgradeRequest request) {
    String forwardedFor = request.getHeader("X-Forwarded-For");

    if (forwardedFor == null || forwardedFor.isBlank()) {
      return request.getRemoteAddress();
    } else {
      return Arrays.stream(forwardedFor.split(","))
                   .map(String::trim)
                   .reduce((a, b) -> b)
                   .orElseThrow();
    }
  }
  
  private void addLoginHistory(ServletUpgradeRequest servletUpgradeRequest, String status){
    URI  uri                        = URI.create("http://localhost:8086/cachy/v1/login/history");

    Map<String, List<String>> parameters = servletUpgradeRequest.getParameterMap();
    List<String>              usernames  = parameters.get("login");
    List<String>              clientName  = parameters.get("clientName");
    List<String>              deviceId  = parameters.get("deviceId");
    List<String>              deviceName  = parameters.get("deviceName");
    List<String>              lat  = parameters.get("lat");
    List<String>              longt  = parameters.get("long");
    List<String>              clientVersion  = parameters.get("clientVersion");
    String requestData = null;
    Map<String, String> requestParameters = new HashMap<>();
    requestParameters.put("uuid", usernames != null ? usernames.get(0) : "");
    requestParameters.put("clientName", clientName != null ? clientName.get(0) : "");
    requestParameters.put("deviceId", deviceId != null ? deviceId.get(0) : "");
    requestParameters.put("deviceName", deviceName != null ? deviceName.get(0) : "");
    requestParameters.put("ip", getRemoteAddress(servletUpgradeRequest));
    requestParameters.put("lat", lat != null ? lat.get(0) : "");
    requestParameters.put("longt", longt != null ? longt.get(0) : "");
    requestParameters.put("clientVersion", clientVersion != null ? clientVersion.get(0) : "");
    requestParameters.put("status", status);
    try{
      requestData = mapper.writeValueAsString(requestParameters);
    }catch(Exception e){
      return;
    }

        HttpRequest request = HttpRequest.newBuilder().uri(uri)
                .PUT(HttpRequest.BodyPublishers.ofString(requestData))
                .header("Content-Type", "application/json")
                .build();
        try {
           httpClient.sendAsync(request, HttpResponse.BodyHandlers.ofString()).thenApply(response -> {
                        if (response.statusCode() >= 202 && response.statusCode() < 300) {
                            return null;
                        }
                        return response;
                    });
           

        } catch (Exception e) {

        }
  }
}
