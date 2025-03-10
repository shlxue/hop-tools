package vendor.hadoop;

// import org.apache.hadoop.conf.Configuration;
// import org.apache.hadoop.fs.CommonConfigurationKeys;
// import org.apache.hadoop.fs.FSDataInputStream;
// import org.apache.hadoop.fs.FileSystem;
// import org.apache.hadoop.fs.Path;
// import org.apache.hadoop.hdfs.DistributedFileSystem;
// import org.apache.hadoop.hdfs.MiniDFSCluster;
//
// import java.io.File;
// import java.io.IOException;
// import java.io.InputStream;
// import java.nio.file.Paths;

public class MiniHdfs {

  //  public static void main(String[] args) throws IOException {
  //    int port = Vfs2ForHdfs.PORT;
  //
  //    Configuration configuration = new Configuration();
  //    configuration.set(FileSystem.FS_DEFAULT_NAME_KEY, "hdfs://192.168.1.95:" + port);
  //    configuration.set(FileSystem.USER_HOME_PREFIX, "/user/shl");
  //
  //    System.setProperty("HADOOP_USER_NAME", "shl");
  ////    configuration.set(CommonConfigurationKeys.HADOOP_HTTP_STATIC_USER, "shl");
  //    configuration.set(CommonConfigurationKeys.FS_HOME_DIR_DEFAULT, "/user");
  //    MiniDFSCluster cluster = new MiniDFSCluster.Builder(configuration, new
  // File("/user/shl")).nameNodePort(port).build();
  //    cluster.waitActive();
  //    FileSystem fs = cluster.getFileSystem();
  //    Path root = new Path("/");
  //    System.out.println("======");
  //    System.out.println(fs.exists(root));
  //    System.out.println(fs.exists(new Path(root, ".")));
  ////    System.out.println(fs.exists(new Path("a.txt")));
  //    System.out.println(fs.getHomeDirectory());
  //    FSDataInputStream stream = fs.open(getPath("/user/shl/a.txt"));
  //    try {
  //      try (InputStream is = stream.getWrappedStream()) {
  //        String str = new String(is.readAllBytes());
  //        System.out.println(str);
  //      }
  //    } finally {
  //      if (stream != null) {
  //        stream.close();
  //      }
  //      fs.close();
  //      cluster.close();
  //    }
  //  }

  //  private static Path getPath(String path) {
  //    return new Path(Vfs2ForHdfs.hdfsUrl + path);
  //  }
}
