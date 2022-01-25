/*
 * Copyright 2022 Google, Inc. All rights reserved.
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */
package com.fitbit.bluetooth.fbgatt.exception;
/**
 * This error is given whenever we are missing a needed permission.
 * In case you receive this you will need to re-start the needed component
 */
public class MissingPermission extends BitGattStartException {
  public MissingPermission(String permission) {
    super("You are missing " + permission + " permission");
  }
}