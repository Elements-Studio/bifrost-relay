package org.starcoin.bifrost.types;


import org.starcoin.types.AccountAddress;
import org.starcoin.types.TokenCode;

public final class CrossChainDepositEvent {
    public final TokenCode token_code;
    public final AccountAddress from;
    public final com.novi.serde.Bytes to;
    public final AccountAddress owner;
    public final java.math.@com.novi.serde.Unsigned @com.novi.serde.Int128 BigInteger value;
    public final @com.novi.serde.Unsigned Byte to_chain;

    public CrossChainDepositEvent(TokenCode token_code, AccountAddress from, com.novi.serde.Bytes to, AccountAddress owner, java.math.@com.novi.serde.Unsigned @com.novi.serde.Int128 BigInteger value, @com.novi.serde.Unsigned Byte to_chain) {
        java.util.Objects.requireNonNull(token_code, "token_code must not be null");
        java.util.Objects.requireNonNull(from, "from must not be null");
        java.util.Objects.requireNonNull(to, "to must not be null");
        java.util.Objects.requireNonNull(owner, "owner must not be null");
        java.util.Objects.requireNonNull(value, "value must not be null");
        java.util.Objects.requireNonNull(to_chain, "to_chain must not be null");
        this.token_code = token_code;
        this.from = from;
        this.to = to;
        this.owner = owner;
        this.value = value;
        this.to_chain = to_chain;
    }

    public void serialize(com.novi.serde.Serializer serializer) throws com.novi.serde.SerializationError {
        serializer.increase_container_depth();
        token_code.serialize(serializer);
        from.serialize(serializer);
        serializer.serialize_bytes(to);
        owner.serialize(serializer);
        serializer.serialize_u128(value);
        serializer.serialize_u8(to_chain);
        serializer.decrease_container_depth();
    }

    public byte[] bcsSerialize() throws com.novi.serde.SerializationError {
        com.novi.serde.Serializer serializer = new com.novi.bcs.BcsSerializer();
        serialize(serializer);
        return serializer.get_bytes();
    }

    public static CrossChainDepositEvent deserialize(com.novi.serde.Deserializer deserializer) throws com.novi.serde.DeserializationError {
        deserializer.increase_container_depth();
        Builder builder = new Builder();
        builder.token_code = TokenCode.deserialize(deserializer);
        builder.from = AccountAddress.deserialize(deserializer);
        builder.to = deserializer.deserialize_bytes();
        builder.owner = AccountAddress.deserialize(deserializer);
        builder.value = deserializer.deserialize_u128();
        builder.to_chain = deserializer.deserialize_u8();
        deserializer.decrease_container_depth();
        return builder.build();
    }

    public static CrossChainDepositEvent bcsDeserialize(byte[] input) throws com.novi.serde.DeserializationError {
        if (input == null) {
             throw new com.novi.serde.DeserializationError("Cannot deserialize null array");
        }
        com.novi.serde.Deserializer deserializer = new com.novi.bcs.BcsDeserializer(input);
        CrossChainDepositEvent value = deserialize(deserializer);
        if (deserializer.get_buffer_offset() < input.length) {
             throw new com.novi.serde.DeserializationError("Some input bytes were not read");
        }
        return value;
    }

    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null) return false;
        if (getClass() != obj.getClass()) return false;
        CrossChainDepositEvent other = (CrossChainDepositEvent) obj;
        if (!java.util.Objects.equals(this.token_code, other.token_code)) { return false; }
        if (!java.util.Objects.equals(this.from, other.from)) { return false; }
        if (!java.util.Objects.equals(this.to, other.to)) { return false; }
        if (!java.util.Objects.equals(this.owner, other.owner)) { return false; }
        if (!java.util.Objects.equals(this.value, other.value)) { return false; }
        if (!java.util.Objects.equals(this.to_chain, other.to_chain)) { return false; }
        return true;
    }

    public int hashCode() {
        int value = 7;
        value = 31 * value + (this.token_code != null ? this.token_code.hashCode() : 0);
        value = 31 * value + (this.from != null ? this.from.hashCode() : 0);
        value = 31 * value + (this.to != null ? this.to.hashCode() : 0);
        value = 31 * value + (this.owner != null ? this.owner.hashCode() : 0);
        value = 31 * value + (this.value != null ? this.value.hashCode() : 0);
        value = 31 * value + (this.to_chain != null ? this.to_chain.hashCode() : 0);
        return value;
    }

    public static final class Builder {
        public TokenCode token_code;
        public AccountAddress from;
        public com.novi.serde.Bytes to;
        public AccountAddress owner;
        public java.math.@com.novi.serde.Unsigned @com.novi.serde.Int128 BigInteger value;
        public @com.novi.serde.Unsigned Byte to_chain;

        public CrossChainDepositEvent build() {
            return new CrossChainDepositEvent(
                token_code,
                from,
                to,
                owner,
                value,
                to_chain
            );
        }
    }
}

