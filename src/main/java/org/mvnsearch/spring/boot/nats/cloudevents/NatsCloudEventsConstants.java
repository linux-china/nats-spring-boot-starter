package org.mvnsearch.spring.boot.nats.cloudevents;

import io.cloudevents.core.message.impl.MessageUtils;

import java.util.Map;

/**
 * Constants and methods used throughout the NATS binding for cloud events.
 *
 * @author linux_china
 */
public class NatsCloudEventsConstants {
  /**
   * The prefix name for CloudEvent attributes for use in properties of a NATS message.
   * please refer https://github.com/cloudevents/spec/blob/main/cloudevents/bindings/nats-protocol-binding.md
   */
  static final String CE_PREFIX = "ce-";

  static final Map<String, String> ATTRIBUTES_TO_PROPERTY_NAMES = MessageUtils.generateAttributesToHeadersMapping(CEA -> CE_PREFIX + CEA);

  static final String PROPERTY_CONTENT_TYPE = "ce-contenttype";
  static final String MESSAGE_PROPERTY_SPEC_VERSION = ATTRIBUTES_TO_PROPERTY_NAMES.get("specversion");
}
