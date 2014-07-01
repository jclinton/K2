/*
 * Copyright 2014 Google Inc. All rights reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the License
 * is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express
 * or implied. See the License for the specific language governing permissions and limitations under
 * the License.
 */

package com.google.k2crypto.keyversions;

import com.google.k2crypto.exceptions.BuilderException;
import com.google.k2crypto.exceptions.EncryptionException;

import java.util.Arrays;

import javax.crypto.KeyGenerator;
import javax.crypto.Mac;
import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;

/**
 * This class represents a hash key version in K2. It is abstract and extended by specific hash key
 * implementations such as HMACKeyVersion
 *
 * @author John Maheswaran (maheswaran@google.com)
 */

public class HMACKeyVersion extends HashKeyVersion {
  /**
   * SecretKey object representing the key matter in the HMAC key version
   */
  private SecretKey secretKey;

  /**
   * Private constructor to ensure people use generateSHA1HMAC or generateMD5HMAC to generate HMAC
   * key
   */
  private HMACKeyVersion() {
    // Do not put any code here
  }

  /**
   * String constants representing all supported hash algorithms.
   */
  public static final String HMAC_MD5 = "HmacMD5";
  public static final String HMAC_SHA1 = "HmacSHA1";
  public static final String HMAC_SHA256 = "HmacSHA256";
  public static final String HMAC_SHA384 = "HmacSHA384";
  public static final String HMAC_SHA512 = "HmacSHA512";

  /**
   * Hash algorithm for this HMAC key version
   */
  private String algorithm = HMAC_SHA1;

  /**
   * Generates a new HMAC using the SHA1 hash algorithm
   *
   * @return a new HMACKeyVersion using the SHA1 hash algorithm
   * @throws BuilderException
   */
  public static HMACKeyVersion generateHMAC(String hashAlgorithm) throws BuilderException {
    try {
      HMACKeyVersion hmac = new HMACKeyVersion();
      // Generate a key for the HMAC-SHA1 keyed-hashing algorithm
      KeyGenerator keyGen = KeyGenerator.getInstance(hashAlgorithm);
      hmac.secretKey = keyGen.generateKey();
      return hmac;
    } catch (Exception e) {
      // throw builder exception if could not build key
      throw new BuilderException("Failed to build HMACKeyVersion", e);
    }
  }

  /**
   * Generates a new HMAC using the SHA1 hash algorithm from give keyversion matter
   *
   * @param keyVersionMatter The byte array representation of the HMAC key version
   * @return an HMACKeyVersion object representing the HMAC key based on the input key version
   *         matter
   * @throws BuilderException
   */
  public static HMACKeyVersion generateHMAC(String hashAlgorithm, byte[] keyVersionMatter)
      throws BuilderException {
    try {
      HMACKeyVersion hmac = new HMACKeyVersion();
      // set the secret key based on the raw key matter
      hmac.secretKey =
          new SecretKeySpec(keyVersionMatter, 0, keyVersionMatter.length, hashAlgorithm);
      return hmac;
    } catch (Exception e) {
      // throw builder exception if could not build key
      throw new BuilderException("Failed to build HMACKeyVersion", e);
    }
  }

  /**
   * Public method to get the byte array of the HMAC key version matter
   *
   * @return The byte array representation of the HMAC key version matter
   */
  public byte[] getKeyVersionMatter() {
    return this.secretKey.getEncoded();
  }

  /**
   * Method to compute the raw HMAC on a piece of input data
   *
   * @param inputData The data on which to compute the HMAC
   * @return The byte array representation of the HMAC
   * @throws EncryptionException
   */
  public byte[] getRawHMAC(byte[] inputData) throws EncryptionException {
    try {
      // get an HMAC Mac instance using the algorithm of this HMAC key
      Mac mac = Mac.getInstance(this.algorithm);
      // now initialize with the signing key it withthe key
      mac.init(this.secretKey);
      // compute the hmac on input data bytes
      byte[] hmacsig = mac.doFinal(inputData);
      // return the HMAC
      return hmacsig;
    } catch (Exception e) {
      // catch any exceptions and throw custom exception
      throw new EncryptionException("Failed to generate HMAC signature", e);
    }
  }

  /**
   * Method that verifies a given HMAC on a piece of data
   *
   * @param inputHmac The input HMAC to verify
   * @param message The input message to check the HMAC against
   * @return True if and only if the HMAC computed on the message matches the input HMAC, false
   *         otherwise
   * @throws EncryptionException
   */
  public boolean verifyHMAC(byte[] inputHmac, byte[] message) throws EncryptionException {
    // compute the hmac on the message
    // if the input hmac matches the computed hmac return true
    if (Arrays.equals(inputHmac, getRawHMAC(message))) {
      return true;
    }
    // otherwise return false as the computed hmac differs from the input hmac
    return false;
  }
}