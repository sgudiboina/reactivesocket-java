/*
 * Copyright 2016 Netflix, Inc.
 * <p>
 *  Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except in compliance with
 *  the License. You may obtain a copy of the License at
 *  <p>
 *  http://www.apache.org/licenses/LICENSE-2.0
 *  <p>
 *  Unless required by applicable law or agreed to in writing, software distributed under the License is distributed on
 *  an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the License for the
 *  specific language governing permissions and limitations under the License.
 */
package io.reactivesocket;

import io.reactivesocket.internal.PublisherUtils;
import io.reactivesocket.internal.Publishers;
import org.reactivestreams.Publisher;

import java.util.function.BiFunction;
import java.util.function.Function;

public interface RequestHandler {
    Function<Payload, Publisher<Payload>> NO_REQUEST_RESPONSE_HANDLER =
        payload -> PublisherUtils.errorPayload(new RuntimeException("No 'requestResponse' handler"));

    Function<Payload, Publisher<Payload>> NO_REQUEST_STREAM_HANDLER =
        payload -> PublisherUtils.errorPayload(new RuntimeException("No 'requestStream' handler"));

    Function<Payload, Publisher<Payload>> NO_REQUEST_SUBSCRIPTION_HANDLER =
        payload -> PublisherUtils.errorPayload(new RuntimeException("No 'requestSubscription' handler"));

    Function<Payload, Publisher<Void>> NO_FIRE_AND_FORGET_HANDLER =
        payload -> Publishers.error(new RuntimeException("No 'fireAndForget' handler"));

    BiFunction<Payload, Publisher<Payload>, Publisher<Payload>> NO_REQUEST_CHANNEL_HANDLER =
        (initialPayload, payloads) -> PublisherUtils.errorPayload(new RuntimeException("No 'requestChannel' handler"));

    Function<Payload, Publisher<Void>> NO_METADATA_PUSH_HANDLER =
        payload -> Publishers.error(new RuntimeException("No 'metadataPush' handler"));

    Publisher<Payload> handleRequestResponse(final Payload payload);

    Publisher<Payload> handleRequestStream(final Payload payload);

    Publisher<Payload> handleSubscription(final Payload payload);

    Publisher<Void> handleFireAndForget(final Payload payload);

    /**
     * @note The initialPayload will also be part of the inputs publisher.
     * It is there to simplify routing logic.
     */
    Publisher<Payload> handleChannel(final Payload initialPayload, final Publisher<Payload> inputs);

    Publisher<Void> handleMetadataPush(final Payload payload);

    class Builder {
        private Function<Payload, Publisher<Payload>> handleRequestResponse = NO_REQUEST_RESPONSE_HANDLER;
        private Function<Payload, Publisher<Payload>> handleRequestStream = NO_REQUEST_STREAM_HANDLER;
        private Function<Payload, Publisher<Payload>> handleRequestSubscription = NO_REQUEST_SUBSCRIPTION_HANDLER;
        private Function<Payload, Publisher<Void>> handleFireAndForget = NO_FIRE_AND_FORGET_HANDLER;
        private BiFunction<Payload, Publisher<Payload>, Publisher<Payload>> handleRequestChannel = NO_REQUEST_CHANNEL_HANDLER;
        private Function<Payload, Publisher<Void>> handleMetadataPush = NO_METADATA_PUSH_HANDLER;

        public Builder withRequestResponse(final Function<Payload, Publisher<Payload>> handleRequestResponse) {
            this.handleRequestResponse = handleRequestResponse;
            return this;
        }

        public Builder withRequestStream(final Function<Payload, Publisher<Payload>> handleRequestStream) {
            this.handleRequestStream = handleRequestStream;
            return this;
        }

        public Builder withRequestSubscription(final Function<Payload, Publisher<Payload>> handleRequestSubscription) {
            this.handleRequestSubscription = handleRequestSubscription;
            return this;
        }

        public Builder withFireAndForget(final Function<Payload, Publisher<Void>> handleFireAndForget) {
            this.handleFireAndForget = handleFireAndForget;
            return this;
        }

        public Builder withRequestChannel(final BiFunction<Payload, Publisher<Payload> , Publisher<Payload>> handleRequestChannel) {
            this.handleRequestChannel = handleRequestChannel;
            return this;
        }

        public Builder withMetadataPush(final Function<Payload, Publisher<Void>> handleMetadataPush) {
            this.handleMetadataPush = handleMetadataPush;
            return this;
        }

        public RequestHandler build() {
            return new RequestHandler() {
                public Publisher<Payload> handleRequestResponse(Payload payload) {
                    return handleRequestResponse.apply(payload);
                }

                public Publisher<Payload> handleRequestStream(Payload payload) {
                    return handleRequestStream.apply(payload);
                }

                public Publisher<Payload> handleSubscription(Payload payload) {
                    return handleRequestSubscription.apply(payload);
                }

                public Publisher<Void> handleFireAndForget(Payload payload) {
                    return handleFireAndForget.apply(payload);
                }

                public Publisher<Payload> handleChannel(Payload initialPayload, Publisher<Payload> inputs) {
                    return handleRequestChannel.apply(initialPayload, inputs);
                }

                public Publisher<Void> handleMetadataPush(Payload payload) {
                    return handleMetadataPush.apply(payload);
                }
            };
        }
    }
}
