/*
 * Copyright 2013-2020 Signal Messenger, LLC
 * SPDX-License-Identifier: AGPL-3.0-only
 */

package org.whispersystems.textsecuregcm.storage.mappers;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.jdbi.v3.core.mapper.RowMapper;
import org.jdbi.v3.core.statement.StatementContext;
import org.whispersystems.textsecuregcm.storage.Account;
import org.whispersystems.textsecuregcm.storage.Accounts;
import org.whispersystems.textsecuregcm.util.SystemMapper;

import java.io.IOException;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.UUID;

public class AccountRowMapper implements RowMapper<Account> {

  private static ObjectMapper mapper = SystemMapper.getMapper();

  @Override
  public Account map(ResultSet resultSet, StatementContext ctx) throws SQLException {
    try {
      Account account = mapper.readValue(resultSet.getString(Accounts.DATA), Account.class);
      account.setNumber(resultSet.getString(Accounts.NUMBER));
      account.setUuid(UUID.fromString(resultSet.getString(Accounts.UID)));
      account.setEmail(resultSet.getString(Accounts.EMAIL));
      account.setPassword(resultSet.getString(Accounts.PASSWORD));
      account.setUserName(resultSet.getString(Accounts.USERNAME));
      account.setIsEmailVerified(resultSet.getBoolean(Accounts.ISEMAILVERIFIED));
      account.setDeviceId(resultSet.getString(Accounts.DEVICEID));
      account.setDeviceName(resultSet.getString(Accounts.DEVICENAME));
      account.setReferedBy(resultSet.getString(Accounts.REFEREDBY));
      account.setIpAddrress(resultSet.getString(Accounts.IPADDRESS));
      account.setStatus(resultSet.getString(Accounts.STATUS));
      account.setClient(resultSet.getString(Accounts.CLIENT));
      account.setCountryCode(resultSet.getString(Accounts.COUNTRYCODE));
      account.setAccountType(resultSet.getString(Accounts.ACCOUNT_TYPE));
      account.setMonitization(resultSet.getString(Accounts.MONITIZATION));
      return account;
    } catch (IOException e) {
      throw new SQLException(e);
    }
  }
}
