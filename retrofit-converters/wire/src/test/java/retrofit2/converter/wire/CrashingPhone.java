// Code generated by Wire protocol buffer compiler, do not edit.
// Source file: phone.proto at 6:1
package retrofit2.converter.wire;

import com.squareup.wire.FieldEncoding;
import com.squareup.wire.Message;
import com.squareup.wire.ProtoAdapter;
import com.squareup.wire.ProtoReader;
import com.squareup.wire.ProtoWriter;
import com.squareup.wire.WireField;
import com.squareup.wire.internal.Internal;
import java.io.EOFException;
import java.io.IOException;
import okio.ByteString;

public final class CrashingPhone extends Message<CrashingPhone, CrashingPhone.Builder> {
  public static final ProtoAdapter<CrashingPhone> ADAPTER = new ProtoAdapter_CrashingPhone();

  private static final long serialVersionUID = 0L;

  public static final String DEFAULT_NUMBER = "";

  @WireField(tag = 1, adapter = "com.squareup.wire.ProtoAdapter#STRING")
  public final String number;

  public CrashingPhone(String number) {
    this(number, ByteString.EMPTY);
  }

  public CrashingPhone(String number, ByteString unknownFields) {
    super(ADAPTER, unknownFields);
    this.number = number;
  }

  @Override
  public Builder newBuilder() {
    Builder builder = new Builder();
    builder.number = number;
    builder.addUnknownFields(unknownFields());
    return builder;
  }

  @Override
  public boolean equals(Object other) {
    if (other == this) return true;
    if (!(other instanceof CrashingPhone)) return false;
    CrashingPhone o = (CrashingPhone) other;
    return Internal.equals(unknownFields(), o.unknownFields()) && Internal.equals(number, o.number);
  }

  @Override
  public int hashCode() {
    int result = super.hashCode;
    if (result == 0) {
      result = unknownFields().hashCode();
      result = result * 37 + (number != null ? number.hashCode() : 0);
      super.hashCode = result;
    }
    return result;
  }

  @Override
  public String toString() {
    StringBuilder builder = new StringBuilder();
    if (number != null) builder.append(", number=").append(number);
    return builder.replace(0, 2, "Phone{").append('}').toString();
  }

  public static final class Builder extends Message.Builder<CrashingPhone, Builder> {
    public String number;

    public Builder() {}

    public Builder number(String number) {
      this.number = number;
      return this;
    }

    @Override
    public CrashingPhone build() {
      return new CrashingPhone(number, buildUnknownFields());
    }
  }

  private static final class ProtoAdapter_CrashingPhone extends ProtoAdapter<CrashingPhone> {
    ProtoAdapter_CrashingPhone() {
      super(FieldEncoding.LENGTH_DELIMITED, CrashingPhone.class);
    }

    @Override
    public int encodedSize(CrashingPhone value) {
      return (value.number != null ? ProtoAdapter.STRING.encodedSizeWithTag(1, value.number) : 0)
          + value.unknownFields().size();
    }

    @Override
    public void encode(ProtoWriter writer, CrashingPhone value) throws IOException {
      throw new EOFException("oops!");
    }

    @Override
    public CrashingPhone decode(ProtoReader reader) throws IOException {
      Builder builder = new Builder();
      long token = reader.beginMessage();
      for (int tag; (tag = reader.nextTag()) != -1; ) {
        switch (tag) {
          case 1:
            builder.number(ProtoAdapter.STRING.decode(reader));
            break;
          default:
            {
              FieldEncoding fieldEncoding = reader.peekFieldEncoding();
              Object value = fieldEncoding.rawProtoAdapter().decode(reader);
              builder.addUnknownField(tag, fieldEncoding, value);
            }
        }
      }
      reader.endMessage(token);
      return builder.build();
    }

    @Override
    public CrashingPhone redact(CrashingPhone value) {
      Builder builder = value.newBuilder();
      builder.clearUnknownFields();
      return builder.build();
    }
  }
}