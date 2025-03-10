package vendor.hadoop;

// import org.apache.commons.vfs2.FileObject;
// import org.apache.commons.vfs2.FileSystemException;
// import org.apache.commons.vfs2.impl.DefaultFileSystemManager;
// import org.apache.commons.vfs2.provider.hdfs.HdfsFileProvider;
// import org.apache.hadoop.conf.Configuration;
// import org.apache.hadoop.fs.FSDataOutputStream;
// import org.apache.hadoop.fs.FileSystem;
// import org.apache.hadoop.fs.Path;
// import org.apache.hadoop.hdfs.MiniDFSCluster;
// import org.junit.jupiter.api.*;
//
// import java.io.IOException;
//
// import static org.junit.jupiter.api.Assertions.assertFalse;
// import static org.junit.jupiter.api.Assertions.assertTrue;

class HdfsIT {
  //  static int PORT = 9820;
  //  static String hdfsUrl = String.format("hdfs://192.168.1.95:%d", PORT);
  //  static DefaultFileSystemManager manager;
  //  static MiniDFSCluster cluster;
  //  static FileSystem hdfs;
  //
  //  @BeforeAll
  //  static void beforeAll() throws IOException {
  //    System.setProperty("log4j.configuration", "log4j.properties");
  //    Configuration configuration = new Configuration();
  //    configuration.set(FileSystem.FS_DEFAULT_NAME_KEY, hdfsUrl);
  //    cluster = new MiniDFSCluster.Builder(configuration).nameNodePort(PORT).build();
  //    cluster.waitActive();
  //
  //    manager = new DefaultFileSystemManager();
  //    manager.addProvider("hdfs", new HdfsFileProvider());
  //    manager.init();
  //    hdfs = cluster.getFileSystem();
  //  }
  //
  //  @AfterAll
  //  static void afterAll() throws IOException {
  //    hdfs.close();
  //    cluster.close();
  //  }
  //
  //  @BeforeEach
  //  void setUp() throws IOException {
  //    initHdfs();
  //  }
  //
  //  @Test
  //  void testResolvePath() throws FileSystemException {
  //    FileObject rootPath = manager.resolveFile(hdfsUrl + "/opt");
  //    manager.setBaseFile(rootPath);
  //    Assertions.assertEquals(
  //        hdfsUrl + "/opt/a/b/c.jar", rootPath.resolveFile("a/b/c.jar").toString());
  //
  //    assertTrue(rootPath.exists());
  //    assertTrue(rootPath.resolveFile("a.jar").exists());
  //    assertFalse(rootPath.resolveFile("a/b/c.jar").exists());
  //  }
  //
  //  private void initHdfs() throws IOException {
  //    Path basePath = new Path("/usr/shl/opt");
  //    hdfs.mkdirs(basePath);
  //    FSDataOutputStream stream =
  //    hdfs.create(new Path(basePath, "a.jar"));
  //    stream.write("a test".getBytes());
  //    stream.hflush();
  //    stream.close();
  //  }
}
