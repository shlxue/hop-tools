package org.apache.hop.transforms.cdc;

import lombok.Getter;
import org.apache.hop.transforms.cdc.domain.MixedKey;
import org.apache.hop.transforms.cdc.domain.RefKey;

import java.io.PrintStream;
import java.util.*;
import java.util.concurrent.BlockingQueue;
import java.util.function.Function;

@Getter
class BatchMarker {
  private final Map<Comparable<?>, List<RefKey>> mKeyToRefKey;
  private final int sKeyCount;
  private int mKeyCount;
  private int delete;
  private int notFound;
  private int duplicate;
  private int single;
  private int multi;

  BatchMarker(BlockingQueue<KeyMap> queue) {
    this.sKeyCount = queue.size();
    mark(queue);
    this.mKeyToRefKey = new LinkedHashMap<>();
  }

  public boolean marked() {
    return sKeyCount == delete + notFound + duplicate + single + multi;
  }

  public List<RefKey> dispatch(
      BlockingQueue<KeyMap> queue, Map<Comparable<?>, List<RefKey>> mKeyToRefKey) {
    List<RefKey> refKeys = new ArrayList<>();
    for (KeyMap item : queue) {
      if (item.type() != KeyMap.Type.MATCH) {
        continue;
      }
      RefKey refKey = new RefKey(item.raw(), item.key(), item.mKeys());
      item.forEach(
          (k, tap) -> mKeyToRefKey.computeIfAbsent(k, mk -> new LinkedList<>()).add(refKey));
      refKeys.add(refKey);
    }
    return refKeys;
  }

  @Override
  public String toString() {
    return String.format(
        "key= %d -> {M=%d(%d+%d), D=%d, NDF=%d, DUP=%d}",
        sKeyCount, mKeyCount, single, multi, delete, notFound, duplicate);
  }

  void mark(BlockingQueue<KeyMap> queue) {
    Map<Comparable<?>, List<KeyMap>> mKeyMap = new HashMap<>();
    prepareMark(queue, mKeyMap);

    mKeyCount = mKeyMap.size();
    mKeyMap.forEach((k, v) -> v.get(0).mark(true, mk -> mk.equals(k)));
    mKeyMap.values().stream().filter(keyMaps -> keyMaps.size() > 1).forEach(this::markDuplicateMap);
    for (KeyMap keyMap : queue) {
      KeyMap.Type type = keyMap.type();
      if (type == KeyMap.Type.NONE) {
        keyMap.mark(false, null);
      } else if (type == KeyMap.Type.MATCH) {
        keyMap.markTap();
      }
    }

    for (KeyMap keyMap : queue) {
      KeyMap.Type type = keyMap.type();
      if (type == KeyMap.Type.MATCH) {
        if (keyMap.have() == 1 || keyMap.isSingle()) {
          single++;
        } else {
          multi++;
        }
      } else if (type == KeyMap.Type.DUPLICATE) {
        duplicate++;
      }
    }
  }

  private void prepareMark(BlockingQueue<KeyMap> queue, Map<Comparable<?>, List<KeyMap>> mKeyMap) {
    for (KeyMap item : queue) {
      if (item.nonDeleteOp()) {
        if (item.isMatch()) {
          item.forEach(mk -> mKeyMap.computeIfAbsent(mk, k -> new ArrayList<>()).add(item));
          mKeyCount++;
        } else {
          notFound++;
        }
      } else {
        delete++;
      }
    }
  }

  private void markDuplicateMap(List<KeyMap> keyMaps) {
    Map<Integer, Deque<KeyMap>> mKeyCountMap = new LinkedHashMap<>();
    for (KeyMap item : keyMaps) {
      mKeyCountMap.computeIfAbsent(item.have(), k -> new ArrayDeque<>()).push(item);
    }
    for (Map.Entry<Integer, Deque<KeyMap>> entry : mKeyCountMap.entrySet()) {
      if (entry.getValue().size() > 1) {
        matchSameMap(entry.getValue(), matchKeyTester(entry.getKey() == 1));
      }
    }
  }

  private void matchSameMap(Deque<KeyMap> keyMaps, Function<KeyMap, Object> matchKeyGetter) {
    Map<Object, KeyMap> map = new HashMap<>(keyMaps.size());
    while (!keyMaps.isEmpty()) {
      KeyMap item = keyMaps.pollLast();
      Object matchKey = matchKeyGetter.apply(item);
      KeyMap exist = map.get(matchKey);
      if (exist == null) {
        map.put(matchKey, item);
      } else if (!item.isMarked()) {
        item.mark(exist);
      }
    }
  }

  private Function<KeyMap, Object> matchKeyTester(boolean onlyOneMap) {
    return onlyOneMap ? KeyMap::mKey : this::getMatchKey;
  }

  private Object getMatchKey(KeyMap item) {
    return MixedKey.of(item.toKeys().sorted().toArray());
  }

  private void dump(BlockingQueue<KeyMap> queue) {
    queue.stream()
        .filter(keyMap -> keyMap.type() != KeyMap.Type.DELETE)
        //        .filter(keyMap -> keyMap.type() == KeyMap.Type.MATCH)
        //        .filter(KeyMap::isMatch)
        .forEach(keyMap -> echo(System.out, keyMap));

    assert queue.stream()
        .noneMatch(
            keyMap -> {
              if (keyMap.type() == KeyMap.Type.NONE) {
                echo(System.out, keyMap);
                return true;
              }
              return false;
            });
    assert marked();
  }

  private void echo(PrintStream stream, KeyMap item) {
    Object v = "";
    if (item.getRefKey() != null) {
      v = item.getRefKey();
    }
    stream.printf("%s: %s, %s --> %s\n", item.key(), item.type(), Arrays.toString(item.toKey()), v);
  }
}
