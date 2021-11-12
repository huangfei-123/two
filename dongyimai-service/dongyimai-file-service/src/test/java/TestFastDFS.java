import org.csource.common.MyException;
import org.csource.fastdfs.*;
import org.springframework.core.io.ClassPathResource;

import java.io.IOException;

public class TestFastDFS {
    public static void main(String[] args) throws IOException, MyException {
        // 1、加载tracker信息  加载配置文件，配置文件中的内容就是 tracker 服务的地址。
        //ClientGlobal.init("C:\\java_projects\\java-four1\\dongyimai-parent\\dongyimai-service\\dongyimai-file-service\\src\\main\\resources\\fdfs_client.conf");
        String path = new ClassPathResource("fdfs_client.conf").getPath();//获取类路径下的文件地址
        ClientGlobal.init(path);
        // 2、创建一个 TrackerClient 对象。直接 new 一个。
        TrackerClient trackerClient = new TrackerClient();
        // 3、使用 TrackerClient 对象创建连接，获得一个 TrackerServer 对象，里面包含了storage信息。
        TrackerServer trackerServer = trackerClient.getConnection();
        // 4、创建一个 StorageServer 的引用，值为 null
        StorageServer storageServer = null;
        // 5、创建一个 StorageClient 对象，需要两个参数 TrackerServer 对象、StorageServer 的引用
        StorageClient storageClient = new StorageClient(trackerServer, storageServer);
        // 6、使用 StorageClient 对象上传图片。参数1：文件路径，参数2：扩展名 参数3:扩展信息
        //扩展名不带“.”
        String[] strings = storageClient.upload_file("C:\\Users\\Public\\Pictures\\Sample Pictures/b.jpg", "jpg", null);
        // 7、返回数组。包含组名和图片的路径。
        System.out.println(strings);
        for (String string : strings) {
            System.out.println(string);
        }
    }
}
