/*
 * Copyright (c) 1990-2012 kopiLeft Development SARL, Bizerte, Tunisia
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License version 2.1 as published by the Free Software Foundation.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA 02111-1307 USA
 *
 * $Id$
 */

package com.axelor.apps.account.ebics.xml;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.util.Calendar;

import com.axelor.apps.account.ebics.client.DefaultEbicsRootElement;
import com.axelor.apps.account.ebics.client.EbicsSession;
import com.axelor.apps.account.ebics.client.OrderType;
import com.axelor.apps.account.ebics.exception.EbicsException;
import com.axelor.apps.account.ebics.schema.h003.EbicsNoPubKeyDigestsRequestDocument;
import com.axelor.apps.account.ebics.schema.h003.EbicsNoPubKeyDigestsRequestDocument.EbicsNoPubKeyDigestsRequest;
import com.axelor.apps.account.ebics.schema.h003.EbicsNoPubKeyDigestsRequestDocument.EbicsNoPubKeyDigestsRequest.Body;
import com.axelor.apps.account.ebics.schema.h003.EbicsNoPubKeyDigestsRequestDocument.EbicsNoPubKeyDigestsRequest.Header;
import com.axelor.apps.account.ebics.schema.h003.EmptyMutableHeaderType;
import com.axelor.apps.account.ebics.schema.h003.NoPubKeyDigestsRequestStaticHeaderType;
import com.axelor.apps.account.ebics.schema.h003.OrderDetailsType;
import com.axelor.apps.account.ebics.schema.h003.ProductElementType;
import com.axelor.apps.account.ebics.schema.xmldsig.SignatureType;
import com.axelor.apps.account.ebics.utils.Utils;
import com.axelor.exception.AxelorException;

/**
 * The <code>NoPubKeyDigestsRequestElement</code> is the root element
 * for a HPB ebics server request.
 *
 * @author hachani
 *
 */
public class NoPubKeyDigestsRequestElement extends DefaultEbicsRootElement {

  /**
   * Construct a new No Public Key Digests Request element.
   * @param session the current ebics session.
   */
  public NoPubKeyDigestsRequestElement(EbicsSession session) {
    super(session);
  }

  /**
   * Returns the digest value of the authenticated XML portions.
   * @return  the digest value.
   * @throws EbicsException Failed to retrieve the digest value.
   */
  public byte[] getDigest() throws AxelorException {
    addNamespaceDecl("ds", "http://www.w3.org/2000/09/xmldsig#");

    try {
      return MessageDigest.getInstance("SHA-256", "BC").digest(Utils.canonize(toByteArray()));
    } catch (NoSuchAlgorithmException e) {
      throw new AxelorException(e.getMessage(), 1);
    } catch (NoSuchProviderException e) {
      throw new AxelorException(e.getMessage(), 1);
    }
  }

  /**
   * Sets the authentication signature of the <code>NoPubKeyDigestsRequestElement</code>
   * @param authSignature the the authentication signature.
   */
  public void setAuthSignature(SignatureType authSignature) {
    ((EbicsNoPubKeyDigestsRequestDocument)document).getEbicsNoPubKeyDigestsRequest().setAuthSignature(authSignature);
  }

  /**
   * Sets the signature value of the request.
   * @param signature the signature value
   */
  public void setSignatureValue(byte[] signature) {
    ((EbicsNoPubKeyDigestsRequestDocument)document).getEbicsNoPubKeyDigestsRequest().getAuthSignature().setSignatureValue(EbicsXmlFactory.createSignatureValueType(signature));
  }

  @Override
  public void build() throws AxelorException {
    EbicsNoPubKeyDigestsRequest			request;
    Body 					body;
    Header					header;
    EmptyMutableHeaderType 			mutable;
    NoPubKeyDigestsRequestStaticHeaderType 	xstatic;
    ProductElementType 				product;
    OrderDetailsType 				orderDetails;
    
    product = EbicsXmlFactory.creatProductElementType(session.getProduct().getLanguage(), session.getProduct().getName());
    orderDetails = EbicsXmlFactory.createOrderDetailsType("DZHNN", null, OrderType.HPB.getOrderType());
    xstatic = EbicsXmlFactory.createNoPubKeyDigestsRequestStaticHeaderType(session.getBankID(),
	                                                                   Utils.generateNonce(),
	                                                                   Calendar.getInstance(),
	                                                                   session.getUser().getEbicsPartner().getPartnerId(),
	                                                                   session.getUser().getUserId(),
	                                                                   product,
	                                                                   orderDetails,
	                                                                   session.getUser().getSecurityMedium());
    mutable = EbicsXmlFactory.createEmptyMutableHeaderType();
    header = EbicsXmlFactory.createDigestsRequestHeader(true, mutable, xstatic);
    body = EbicsXmlFactory.createDigestsRequestBody();
    request = EbicsXmlFactory.createEbicsNoPubKeyDigestsRequest(1,
	                                                        "H003",
	                                                        header,
	                                                        body);
    document = EbicsXmlFactory.createEbicsNoPubKeyDigestsRequestDocument(request);
  }

  @Override
  public byte[] toByteArray() {
    setSaveSuggestedPrefixes("http://www.w3.org/2000/09/xmldsig#", "ds");
    setSaveSuggestedPrefixes("http://www.ebics.org/H003", "");

    return super.toByteArray();
  }

  @Override
  public String getName() {
    return "NoPubKeyDigestsRequest.xml";
  }

  // --------------------------------------------------------------------
  // DATA MEMBERS
  // --------------------------------------------------------------------

  private static final long		serialVersionUID = 3177047145408329472L;
}
