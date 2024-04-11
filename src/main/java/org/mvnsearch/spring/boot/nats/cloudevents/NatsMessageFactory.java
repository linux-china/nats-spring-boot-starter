package org.mvnsearch.spring.boot.nats.cloudevents;

import io.cloudevents.core.message.MessageReader;
import io.cloudevents.core.message.MessageWriter;
import io.cloudevents.core.message.impl.GenericStructuredMessageReader;
import io.cloudevents.core.message.impl.MessageUtils;
import io.cloudevents.rw.CloudEventWriter;
import io.nats.client.Message;
import io.nats.client.impl.Headers;

/**
 * A factory class providing convenience methods for creating {@link MessageReader} and {@link MessageWriter} instances
 * based on NATS {@link Message} and {@link Message}.
 *
 * @author linux_china
 */
public class NatsMessageFactory {
  private NatsMessageFactory() {

  }

  /**
   * Creates a {@link MessageReader} to read a Nats {@link Message}.
   *
   * @param message The NATS {@link Message} to read from.
   * @return A {@link MessageReader} that can read the given {@link Message} to a {@link io.cloudevents.CloudEvent} representation.
   */
  public static MessageReader createReader(final Message message) {
    byte[] body = message.getData();
    final Headers headers = message.getHeaders();
    final String contentType = headers.getFirst(NatsCloudEventsConstants.PROPERTY_CONTENT_TYPE);
    return createReader(contentType, headers, body);
  }

  /**
   * Creates a {@link MessageReader} using the content type, properties, and body of a NATS {@link Message}.
   *
   * @param contentType The content type of the message payload.
   * @param headers     The properties of the NATS message containing CloudEvent metadata (attributes and/or extensions).
   * @param body        The message body as byte array.
   * @return A {@link MessageReader} capable of parsing a {@link io.cloudevents.CloudEvent} from the content-type, properties, and payload of a NATS message.
   */
  public static MessageReader createReader(final String contentType, final Headers headers, final byte[] body) {
    return MessageUtils.parseStructuredOrBinaryMessage(
      () -> contentType,
      format -> new GenericStructuredMessageReader(format, body),
      () -> headers.getFirst(NatsCloudEventsConstants.MESSAGE_PROPERTY_SPEC_VERSION),
      sv -> new NatsBinaryMessageReader(sv, headers, contentType, body)
    );
  }

  /**
   * Creates a {@link MessageWriter} instance capable of translating a {@link io.cloudevents.CloudEvent} to a NATS {@link Message}.
   *
   * @param subject The subject to which the created NATS message will be sent.
   * @return A {@link MessageWriter} capable of converting a {@link io.cloudevents.CloudEvent} to a NATS {@link Message}.
   */
  public static MessageWriter<CloudEventWriter<Message>, Message> createWriter(final String subject) {
    return new NatsMessageWriter(subject);
  }
}
