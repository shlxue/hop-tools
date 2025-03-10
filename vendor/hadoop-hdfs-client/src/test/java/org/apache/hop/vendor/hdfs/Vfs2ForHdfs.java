package vendor.hadoop;

// import org.apache.commons.vfs2.FileContent;
// import org.apache.commons.vfs2.FileObject;
// import org.apache.commons.vfs2.impl.DefaultFileSystemManager;
// import org.apache.commons.vfs2.provider.hdfs.HdfsFileProvider;
//
// import java.io.IOException;
// import java.io.PrintStream;

public class Vfs2ForHdfs {
  static final int PORT = 9820;
  static final String hdfsUrl = "hdfs://192.168.1.95:" + PORT;

  //  public static void main(String[] args) throws IOException {
  //    DefaultFileSystemManager manager = new DefaultFileSystemManager();
  //    manager.addProvider("hdfs", new HdfsFileProvider());
  //    manager.init();
  //
  //    FileObject optPath = manager.resolveFile(hdfsUrl + "/opt");
  //    FileObject fileObject = optPath.resolveFile("a/b/c.jar");
  //    PrintStream stream = System.out;
  //    stream.printf("/opt folder exist: %s%n", fileObject.exists());
  //    stream.printf("/opt/a/b/c.jar exist: %s%n", fileObject.exists());
  //    for (FileObject c : optPath.getChildren()) {
  //      stream.println("  list file: " + c);
  //    }
  //    stream.printf(
  //        "/opt/hop.data.zip/projects/samples/transforms exist: %s%n",
  //        optPath.resolveFile("hop.data.zip/projects/samples/transforms").exists());
  //    try (FileContent fileContent =
  //        optPath.resolveFile("hop.data.zip/hop-config.json").getContent()) {
  //      stream.printf(
  //          "/opt/hop.data.zip/hop-config.json file size: %s%n",
  // fileContent.getByteArray().length);
  //    }
  //    manager.close();
  //  }
}
