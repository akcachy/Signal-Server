/*
 * Copyright 2013-2020 Signal Messenger, LLC
 * SPDX-License-Identifier: AGPL-3.0-only
 */

package org.whispersystems.textsecuregcm.auth;

import com.codahale.metrics.Meter;
import com.codahale.metrics.MetricRegistry;
import com.codahale.metrics.SharedMetricRegistries;
import com.google.common.annotations.VisibleForTesting;
import io.dropwizard.auth.basic.BasicCredentials;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.whispersystems.textsecuregcm.storage.Account;
import org.whispersystems.textsecuregcm.storage.AccountsManager;
import org.whispersystems.textsecuregcm.storage.Device;
import org.whispersystems.textsecuregcm.util.Constants;
import org.whispersystems.textsecuregcm.util.Util;

import java.time.Clock;
import java.time.Duration;
import java.time.temporal.ChronoUnit;
import java.util.Optional;

import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Response;

import static com.codahale.metrics.MetricRegistry.name;

public class BaseAccountAuthenticator {

  private final MetricRegistry metricRegistry               = SharedMetricRegistries.getOrCreate(Constants.METRICS_NAME);
  private final Meter          authenticationFailedMeter    = metricRegistry.meter(name(getClass(), "authentication", "failed"         ));
  private final Meter          authenticationSucceededMeter = metricRegistry.meter(name(getClass(), "authentication", "succeeded"      ));
  private final Meter          noSuchAccountMeter           = metricRegistry.meter(name(getClass(), "authentication", "noSuchAccount"  ));
  private final Meter          noSuchDeviceMeter            = metricRegistry.meter(name(getClass(), "authentication", "noSuchDevice"   ));
  private final Meter          accountDisabledMeter         = metricRegistry.meter(name(getClass(), "authentication", "accountDisabled"));
  private final Meter          deviceDisabledMeter          = metricRegistry.meter(name(getClass(), "authentication", "deviceDisabled" ));
  private final Meter          invalidAuthHeaderMeter       = metricRegistry.meter(name(getClass(), "authentication", "invalidHeader"  ));

  private final Logger logger = LoggerFactory.getLogger(BaseAccountAuthenticator.class);

  private final AccountsManager accountsManager;
  private final Clock           clock;

  public BaseAccountAuthenticator(AccountsManager accountsManager) {
    this(accountsManager, Clock.systemUTC());
  }

  @VisibleForTesting
  public BaseAccountAuthenticator(AccountsManager accountsManager, Clock clock) {
    this.accountsManager = accountsManager;
    this.clock           = clock;
  }

  public Optional<Account> authenticate(BasicCredentials basicCredentials, boolean enabledRequired) {
    try {
      AuthorizationHeader authorizationHeader = AuthorizationHeader.fromUserAndPassword(basicCredentials.getUsername(), basicCredentials.getPassword());
      Optional<Account>   account             = accountsManager.get(authorizationHeader.getIdentifier());

      if (account.isPresent()) {
        switch(account.get().getStatus()){
          case "DELETED" :                throw new WebApplicationException("Account deleted.",440);
          case "ACCOUNT_TEMP_SUSPENDED" : throw new WebApplicationException("Account Temp suspended.",441);
          case "ACCOUNT_SUSPENDED" :      throw new WebApplicationException("Account suspended.",442);
        }

      }
      
      if (!account.isPresent()) {
        noSuchAccountMeter.mark();
        return Optional.empty();
      }

      logger.info("*************** AUTHENTICATE  NUMBER "+ account.get().getNumber()+ " UUID "+ account.get().getUuid());
      Optional<Device> device = account.get().getDevice(authorizationHeader.getDeviceId());

      if (!device.isPresent()) {
        noSuchDeviceMeter.mark();
        return Optional.empty();
      }

      logger.info("*************** DEVICE PRESENT TRUE ");

      if (enabledRequired) {
        if (!device.get().isEnabled()) {
          deviceDisabledMeter.mark();
          return Optional.empty();
        }

        if (!account.get().isEnabled()) {
          accountDisabledMeter.mark();
          return Optional.empty();
        }
      }

      logger.info("**************** AFTER ENEBLE REQUIRED ");

      if (device.get().getAuthenticationCredentials().verify(basicCredentials.getPassword())) {
        authenticationSucceededMeter.mark();
        account.get().setAuthenticatedDevice(device.get());
        updateLastSeen(account.get(), device.get());
        return account;
      }

      logger.info("**************** DEVICE VATIFICATION FAILED!!!!!!!!!! ");
      authenticationFailedMeter.mark();
      return Optional.empty();
    } catch (IllegalArgumentException | InvalidAuthorizationHeaderException iae) {
      invalidAuthHeaderMeter.mark();
      return Optional.empty();
    }
  }

  @VisibleForTesting
  public void updateLastSeen(Account account, Device device) {
    final long lastSeenOffsetSeconds   = Math.abs(account.getUuid().getLeastSignificantBits()) % ChronoUnit.DAYS.getDuration().toSeconds();
    final long todayInMillisWithOffset = Util.todayInMillisGivenOffsetFromNow(clock, Duration.ofSeconds(lastSeenOffsetSeconds).negated());

    if (device.getLastSeen() < todayInMillisWithOffset) {
      device.setLastSeen(Util.todayInMillis(clock));
      account.setUpdatedAt(System.currentTimeMillis());
      accountsManager.update(account);
    }
  }

  //#region Accessor and getter methods for Cachy

  public MetricRegistry getMetricRegistry() {
    return metricRegistry;
  }

  public Meter getAuthenticationFailedMeter() {
    return authenticationFailedMeter;
  }

  public Meter getAuthenticationSucceededMeter() {
    return authenticationSucceededMeter;
  }

  public Meter getNoSuchAccountMeter() {
    return noSuchAccountMeter;
  }

  public Meter getNoSuchDeviceMeter() {
    return noSuchDeviceMeter;
  }

  public Meter getAccountDisabledMeter() {
    return accountDisabledMeter;
  }

  public Meter getDeviceDisabledMeter() {
    return deviceDisabledMeter;
  }

  public Meter getInvalidAuthHeaderMeter() {
    return invalidAuthHeaderMeter;
  }

  public Logger getLogger() {
    return logger;
  }

  public AccountsManager getAccountsManager() {
    return accountsManager;
  }

  public Clock getClock() {
    return clock;
  }


  //#endregion


}
