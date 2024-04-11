package org.mvnsearch.spring.boot.nats.cloudevents;

import io.cloudevents.CloudEventData;
import io.cloudevents.SpecVersion;
import io.cloudevents.core.format.EventFormat;
import io.cloudevents.core.message.MessageWriter;
import io.cloudevents.core.v1.CloudEventV1;
import io.cloudevents.rw.CloudEventContextWriter;
import io.cloudevents.rw.CloudEventRWException;
import io.cloudevents.rw.CloudEventWriter;
import io.nats.client.Message;
import io.nats.client.impl.Headers;
import io.nats.client.impl.NatsMessage;
import io.nats.client.support.NatsConstants;

import javax.annotation.ParametersAreNonnullByDefault;


/**
 * The NatsMessageWriter class is a CloudEvents message writer for NATS.
 * It allows CloudEvents attributes, context attributes, and the event payload to be populated
 * in a NATS {@link Message} instance. This class implements the
 * {@link MessageWriter} interface for creating and completing CloudEvents messages in a
 * NATS-compatible format.
 *
 * @author linux_china
 */
@ParametersAreNonnullByDefault
public class NatsMessageWriter implements MessageWriter<CloudEventWriter<Message>, Message>, CloudEventWriter<Message> {
  private final Headers headers;
  private final NatsMessage.Builder builder;

  /**
   * Create a NATS message writer.
   *
   * @param subject message's subject.
   */
  public NatsMessageWriter(String subject) {
    this.headers = new Headers();
    this.builder = new NatsMessage.Builder().subject(subject);
  }

  @Override
  public CloudEventContextWriter withContextAttribute(String name, String value) throws CloudEventRWException {
    if (name.equals(CloudEventV1.DATACONTENTTYPE)) {
      headers.put(NatsCloudEventsConstants.PROPERTY_CONTENT_TYPE, value);
      return this;
    }
    String propertyName = NatsCloudEventsConstants.ATTRIBUTES_TO_PROPERTY_NAMES.get(name);
    if (propertyName == null) {
      propertyName = name;
    }
    headers.put(propertyName, value);
    return this;
  }

  @Override
  public CloudEventWriter<Message> create(SpecVersion version) throws CloudEventRWException {
    headers.put(NatsCloudEventsConstants.MESSAGE_PROPERTY_SPEC_VERSION, version.toString());
    return this;
  }

  @Override
  public Message setEvent(EventFormat format, byte[] value) throws CloudEventRWException {
    headers.put(NatsCloudEventsConstants.PROPERTY_CONTENT_TYPE, format.serializedContentType());
    builder.data(value);
    builder.headers(headers);
    return builder.build();
  }

  @Override
  public Message end(CloudEventData data) throws CloudEventRWException {
    builder.data(data.toBytes());
    builder.headers(headers);
    return builder.build();
  }

  @Override
  public Message end() throws CloudEventRWException {
    builder.data(NatsConstants.EMPTY_BODY);
    builder.headers(headers);
    return builder.build();
  }
}
