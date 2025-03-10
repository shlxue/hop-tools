package vendor.hadoop;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.CommonConfigurationKeys;
import org.apache.hadoop.fs.FileSystem;
import org.apache.hadoop.fs.FsStatus;
import org.apache.hadoop.fs.Path;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

class FsTest {
  static FileSystem fs;

  @BeforeAll
  static void beforeAll() throws IOException {
    Configuration configuration = new Configuration();
    configuration.set(FileSystem.FS_DEFAULT_NAME_KEY, Vfs2ForHdfs.hdfsUrl);
    configuration.set(FileSystem.USER_HOME_PREFIX, "/user/shl");
    System.setProperty("HADOOP_USER_NAME", "shl");
    configuration.set(CommonConfigurationKeys.FS_HOME_DIR_DEFAULT, "/user");
    fs = FileSystem.get(hdfsPath("/").toUri(), configuration);
  }

  @AfterAll
  static void afterAll() throws IOException {
    fs.close();
  }

  @Test
  void existFile() throws IOException {
    Assertions.assertTrue(fs.exists(hdfsPath("/user/hadoop")));
  }

  @Test
  void echoText() throws IOException {
    Path path = hdfsPath("/user/shl/a.txt");
    try (InputStream stream = fs.open(path)) {
      System.out.println(new String(stream.readAllBytes()));
    }
  }

  @Test
  void testCreateDir() throws IOException {
    Path path = hdfsPath("/user/shl/opt");
    if (fs.exists(path)) {
      fs.delete(path);
    }
    fs.mkdirs(path);
  }

  @Test
  void testWrite() throws IOException {
    Path path = hdfsPath("/user/shl/c.txt");
    try (OutputStream stream = fs.create(path)) {
      stream.write("a test message".getBytes());
    }
  }

  @Test
  void testFileStatus() throws IOException {
    Path path = hdfsPath("/user/hadoop");
    FsStatus status = fs.getStatus(path);
    System.out.println(status);
  }

  private static Path hdfsPath(String path) {
    return new Path(Vfs2ForHdfs.hdfsUrl + path);
  }
}
