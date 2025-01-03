package org.apache.hop.transforms.cdc;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import org.apache.hop.transforms.cdc.domain.MixedKey;

import java.util.Set;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.function.Predicate;
import java.util.stream.IntStream;
import java.util.stream.Stream;

class KeyMap {
  private static final int[] EMPTY_INT_ARRAY = new int[0];
  private static final MKey[] EMPTY_KEY_ARRAY = new MKey[0];
  private final Object[] row;
  private final Comparable<?> raw;
  private final Comparable<?> key;
  private Type type;
  private MKey[] mKeys;
  private int[] tap;
  private KeyMap refKey;

  public KeyMap(Object[] row, Comparable<?> raw, Comparable<?> key, boolean delOp) {
    this.row = row;
    this.raw = raw;
    this.key = key;
    this.type = delOp ? Type.DELETE : Type.NONE;
    this.mKeys = EMPTY_KEY_ARRAY;
  }

  public Object[] row() {
    return row;
  }

  public Comparable raw() {
    return raw;
  }

  public Comparable key() {
    return key;
  }

  public Type type() {
    return type;
  }

  public boolean isMarked() {
    return type != Type.NONE;
  }

  public boolean match() {
    return type == Type.MATCH;
  }

  public boolean isStrictMatch() {
    return Stream.of(mKeys).allMatch(MKey::isTap);
  }

  public void mKeys(Set<Comparable<?>> mKeys) {
    if (mKeys.isEmpty()) {
      assert type == Type.NONE;
      this.type = Type.NOT_FOUND;
      this.mKeys = EMPTY_KEY_ARRAY;
    } else {
      this.mKeys = mKeys.stream().map(this::newMKey).toArray(MKey[]::new);
    }
  }

  public Comparable[] mKeys() {
    return toKeys().toArray(Comparable[]::new);
  }

  public Comparable mKey() {
    return mKeys[0].key;
  }

  public void markTap() {
    if (Stream.of(mKeys).anyMatch(MKey::isTap)) {
      this.tap = IntStream.range(0, mKeys.length).filter(i -> mKeys[i].isTap()).toArray();
    } else {
      this.tap = EMPTY_INT_ARRAY;
    }
  }

  public Comparable getRefKey() {
    return refKey != null ? refKey.key() : null;
  }

  public void mark(boolean tap, Predicate<Comparable<?>> tester) {
    this.type = Type.MATCH;
    if (tap) {
      for (MKey item : mKeys) {
        if (!item.isTap() && tester.test(item.getKey())) {
          item.setTap(true);
          break;
        }
      }
    }
  }

  public void mark(KeyMap refKey) {
    assert type == Type.NONE;
    this.type = Type.DUPLICATE;
    this.refKey = refKey;
  }

  private MKey newMKey(Comparable<?> key) {
    return MKey.builder().key(key).build();
  }

  public boolean isDelOp() {
    return type == Type.DELETE;
  }

  public boolean nonDeleteOp() {
    return !isDelOp();
  }

  public Object[] getKeys() {
    if (key instanceof MixedKey) {
      return ((MixedKey) key).getValues();
    }
    return new Object[] {key};
  }

  public int have() {
    return mKeys.length;
  }

  public boolean isMatch() {
    return mKeys.length > 0;
  }

  public boolean isMaxedMap() {
    return mKeys.length > 1;
  }

  public boolean nonMaxedMap() {
    return !isMaxedMap();
  }

  public int[] tap() {
    return tap;
  }

  public int forEach(BiConsumer<Comparable<?>, AtomicBoolean> consumer) {
    AtomicBoolean param = new AtomicBoolean(false);
    for (MKey mKey : mKeys) {
      param.set(mKey.tap);
      consumer.accept(mKey.getKey(), param);
    }
    return mKeys.length;
  }

  public int forEach(Consumer<Comparable> call) {
    for (MKey mKey : mKeys) {
      call.accept(mKey.getKey());
    }
    return mKeys.length;
  }

  public Stream<Comparable> toKeys() {
    return Stream.of(mKeys).map(MKey::getKey);
  }

  public boolean isSingle() {
    if (mKeys.length > 1) {
      return Stream.of(mKeys).filter(MKey::isTap).count() == 1;
    }
    return mKeys.length == 1;
  }

  public Object[] toKey() {
    return mKeys;
  }

  @Builder
  @Getter
  @Setter
  private static class MKey {
    private final Comparable<?> key;
    private boolean tap;

    @Override
    public String toString() {
      String value = key.toString();
      if (tap) {
        return value + "+";
      }
      return value;
    }
  }

  enum Type {
    NONE,
    DELETE,
    NOT_FOUND,
    DUPLICATE,
    MATCH;
  }
}
