/*
 * Copyright (C) 2015 Square, Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package retrofit2.converter.jackson;

import com.fasterxml.jackson.databind.ObjectWriter;
import java.io.IOException;
import okhttp3.MediaType;
import okhttp3.RequestBody;
import retrofit2.Converter;

final class JacksonRequestBodyConverter<T> implements Converter<T, RequestBody> {
  private final ObjectWriter adapter;
  private final MediaType mediaType;
  private final boolean streaming;

  JacksonRequestBodyConverter(ObjectWriter adapter, MediaType mediaType, boolean streaming) {
    this.adapter = adapter;
    this.mediaType = mediaType;
    this.streaming = streaming;
  }

  @Override
  public RequestBody convert(T value) throws IOException {
    if (streaming) {
      return new JacksonStreamingRequestBody(adapter, value, mediaType);
    }

    byte[] bytes = adapter.writeValueAsBytes(value);
    return RequestBody.create(mediaType, bytes);
  }
}
