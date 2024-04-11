package org.mvnsearch.spring.boot.nats.cloudevents;

import io.cloudevents.SpecVersion;
import io.cloudevents.core.data.BytesCloudEventData;
import io.cloudevents.core.message.impl.BaseGenericBinaryMessageReaderImpl;
import io.nats.client.impl.Headers;
import io.nats.client.support.NatsConstants;

import java.util.Arrays;
import java.util.function.BiConsumer;

/**
 * A NATS message reader that can be read as a <em>CloudEvent</em>.
 *
 * @author linux_china
 */
public class NatsBinaryMessageReader extends BaseGenericBinaryMessageReaderImpl<String, Object> {
  private final String contentType;
  private final Headers headers;

  /**
   * Create an instance of a NATS message reader.
   *
   * @param specVersion The version of the cloud event message.
   * @param headers     The properties of the NATS message that contains.
   * @param contentType The content-type property of the NATS message or {@code null} if the message content type if unknown.
   * @param body        The message payload or {@code null}/{@link NatsConstants#EMPTY_BODY} if the message does not contain any payload.
   */
  NatsBinaryMessageReader(final SpecVersion specVersion, Headers headers,
                          final String contentType, final byte[] body) {
    super(specVersion, body != null && !Arrays.equals(NatsConstants.EMPTY_BODY, body) && body.length > 0 ? BytesCloudEventData.wrap(body) : null);
    this.contentType = contentType;
    this.headers = headers;
  }

  @Override
  protected boolean isContentTypeHeader(String key) {
    return key.equals(NatsCloudEventsConstants.PROPERTY_CONTENT_TYPE);
  }

  /**
   * Tests whether the given property key belongs to cloud events headers.
   *
   * @param key The key to test for.
   * @return True if the specified key belongs to cloud events headers.
   */
  @Override
  protected boolean isCloudEventsHeader(String key) {
    final int prefixLength = NatsCloudEventsConstants.CE_PREFIX.length();
    return key.length() > prefixLength && key.startsWith(NatsCloudEventsConstants.CE_PREFIX);
  }

  /**
   * Transforms a NATS message property key into a CloudEvents attribute or extension key.
   * <p>
   * This method removes the {@link NatsCloudEventsConstants#CE_PREFIX} prefix from the given key,
   * assuming that the key has already been determined to be a CloudEvents header by
   * {@link #isCloudEventsHeader(String)}.
   *
   * @param key The NATS message property key with the CloudEvents header prefix.
   * @return The CloudEvents attribute or extension key without the prefix.
   */
  @Override
  protected String toCloudEventsKey(String key) {
    return key.substring(NatsCloudEventsConstants.CE_PREFIX.length());
  }

  @Override
  protected void forEachHeader(BiConsumer<String, Object> fn) {
    if (contentType != null) {
      // visit the content-type message property
      fn.accept(NatsCloudEventsConstants.PROPERTY_CONTENT_TYPE, contentType);
    }
    // visit message properties
    headers.forEach((k, v) -> {
      if (k != null && v != null) {
        fn.accept(k, v);
      }
    });
  }

  /**
   * Gets the cloud event representation of the value.
   * <p>
   * This method simply returns the string representation of the type of value passed as argument.
   *
   * @param value The value of a CloudEvent attribute or extension.
   * @return The string representation of the specified value.
   */
  @Override
  protected String toCloudEventsValue(Object value) {
    return value.toString();
  }
}
