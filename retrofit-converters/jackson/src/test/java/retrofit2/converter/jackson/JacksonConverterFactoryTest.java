/*
 * Copyright (C) 2013 Square, Inc.
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

import static com.google.common.truth.Truth.assertThat;
import static org.junit.Assume.assumeTrue;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonToken;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.MapperFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.deser.std.StdDeserializer;
import com.fasterxml.jackson.databind.module.SimpleModule;
import com.fasterxml.jackson.databind.ser.std.StdSerializer;
import com.google.testing.junit.testparameterinjector.TestParameter;
import com.google.testing.junit.testparameterinjector.TestParameterInjector;
import java.io.EOFException;
import java.io.IOException;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.atomic.AtomicReference;
import okhttp3.mockwebserver.MockResponse;
import okhttp3.mockwebserver.MockWebServer;
import okhttp3.mockwebserver.RecordedRequest;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.http.Body;
import retrofit2.http.POST;

@RunWith(TestParameterInjector.class)
public final class JacksonConverterFactoryTest {
  interface AnInterface {
    String getName();
  }

  static class AnImplementation implements AnInterface {
    private String theName;

    AnImplementation() {}

    AnImplementation(String name) {
      theName = name;
    }

    @Override
    public String getName() {
      return theName;
    }
  }

  static class AnInterfaceSerializer extends StdSerializer<AnInterface> {
    AnInterfaceSerializer() {
      super(AnInterface.class);
    }

    @Override
    public void serialize(
        AnInterface anInterface, JsonGenerator jsonGenerator, SerializerProvider serializerProvider)
        throws IOException {
      jsonGenerator.writeStartObject();
      jsonGenerator.writeFieldName("name");
      jsonGenerator.writeString(anInterface.getName());
      jsonGenerator.writeEndObject();
    }
  }

  static class AnInterfaceDeserializer extends StdDeserializer<AnInterface> {
    AnInterfaceDeserializer() {
      super(AnInterface.class);
    }

    @Override
    public AnInterface deserialize(JsonParser jp, DeserializationContext ctxt) throws IOException {
      if (jp.getCurrentToken() != JsonToken.START_OBJECT) {
        throw new AssertionError("Expected start object.");
      }

      String name = null;

      while (jp.nextToken() != JsonToken.END_OBJECT) {
        switch (jp.getCurrentName()) {
          case "name":
            name = jp.getValueAsString();
            break;
        }
      }

      return new AnImplementation(name);
    }
  }

  static final class ErroringValue {
    final String theName;

    ErroringValue(String theName) {
      this.theName = theName;
    }
  }

  static final class ErroringValueSerializer extends StdSerializer<ErroringValue> {
    ErroringValueSerializer() {
      super(ErroringValue.class);
    }

    @Override
    public void serialize(
        ErroringValue erroringValue,
        JsonGenerator jsonGenerator,
        SerializerProvider serializerProvider)
        throws IOException {
      throw new EOFException("oops!");
    }
  }

  interface Service {
    @POST("/")
    Call<AnImplementation> anImplementation(@Body AnImplementation impl);

    @POST("/")
    Call<AnInterface> anInterface(@Body AnInterface impl);

    @POST("/")
    Call<Void> erroringValue(@Body ErroringValue value);
  }

  @Rule public final MockWebServer server = new MockWebServer();

  private final Service service;
  private final boolean streaming;

  public JacksonConverterFactoryTest(@TestParameter boolean streaming) {
    this.streaming = streaming;

    SimpleModule module = new SimpleModule();
    module.addSerializer(AnInterface.class, new AnInterfaceSerializer());
    module.addSerializer(ErroringValue.class, new ErroringValueSerializer());
    module.addDeserializer(AnInterface.class, new AnInterfaceDeserializer());
    ObjectMapper mapper = new ObjectMapper();
    mapper.registerModule(module);
    mapper.configure(MapperFeature.AUTO_DETECT_GETTERS, false);
    mapper.configure(MapperFeature.AUTO_DETECT_SETTERS, false);
    mapper.configure(MapperFeature.AUTO_DETECT_IS_GETTERS, false);
    mapper.setVisibilityChecker(
        mapper
            .getSerializationConfig()
            .getDefaultVisibilityChecker()
            .withFieldVisibility(JsonAutoDetect.Visibility.ANY));

    JacksonConverterFactory factory = JacksonConverterFactory.create(mapper);
    if (streaming) {
      factory = factory.withStreaming();
    }

    Retrofit retrofit =
        new Retrofit.Builder().baseUrl(server.url("/")).addConverterFactory(factory).build();
    service = retrofit.create(Service.class);
  }

  @Test
  public void anInterface() throws IOException, InterruptedException {
    server.enqueue(new MockResponse().setBody("{\"name\":\"value\"}"));

    Call<AnInterface> call = service.anInterface(new AnImplementation("value"));
    Response<AnInterface> response = call.execute();
    AnInterface body = response.body();
    assertThat(body.getName()).isEqualTo("value");

    RecordedRequest request = server.takeRequest();
    assertThat(request.getBody().readUtf8()).isEqualTo("{\"name\":\"value\"}");
    assertThat(request.getHeader("Content-Type")).isEqualTo("application/json; charset=UTF-8");
  }

  @Test
  public void anImplementation() throws IOException, InterruptedException {
    server.enqueue(new MockResponse().setBody("{\"theName\":\"value\"}"));

    Call<AnImplementation> call = service.anImplementation(new AnImplementation("value"));
    Response<AnImplementation> response = call.execute();
    AnImplementation body = response.body();
    assertThat(body.theName).isEqualTo("value");

    RecordedRequest request = server.takeRequest();
    // TODO figure out how to get Jackson to stop using AnInterface's serializer here.
    assertThat(request.getBody().readUtf8()).isEqualTo("{\"name\":\"value\"}");
    assertThat(request.getHeader("Content-Type")).isEqualTo("application/json; charset=UTF-8");
  }

  @Test
  public void serializeIsStreamed() throws InterruptedException {
    assumeTrue(streaming);

    Call<Void> call = service.erroringValue(new ErroringValue("hi"));

    final AtomicReference<Throwable> throwableRef = new AtomicReference<>();
    final CountDownLatch latch = new CountDownLatch(1);

    // If streaming were broken, the call to enqueue would throw the exception synchronously.
    call.enqueue(
        new Callback<Void>() {
          @Override
          public void onResponse(Call<Void> call, Response<Void> response) {
            latch.countDown();
          }

          @Override
          public void onFailure(Call<Void> call, Throwable t) {
            throwableRef.set(t);
            latch.countDown();
          }
        });
    latch.await();

    Throwable throwable = throwableRef.get();
    assertThat(throwable).isInstanceOf(EOFException.class);
    assertThat(throwable).hasMessageThat().isEqualTo("oops!");
  }
}
