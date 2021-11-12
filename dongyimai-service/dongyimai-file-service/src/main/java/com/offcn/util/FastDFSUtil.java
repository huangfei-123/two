package com.offcn.util;

//实现文件管理

import com.offcn.file.FastDFSFile;
import org.csource.common.MyException;
import org.csource.common.NameValuePair;
import org.csource.fastdfs.*;
import org.springframework.core.io.ClassPathResource;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;

/**
 * 上传
 * 下载
 * 删除
 * 文件信息获取
 * Storage信息获取
 * Tracer信息获取
 */
public class FastDFSUtil {

    /**
     * 1.加载Tracer连接信息
     */
    static {
        try {
            //加载fdfs_client.conf中的tarcker信息
            String path = new ClassPathResource("fdfs_client.conf").getPath();
            ClientGlobal.init(path);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * 1.文件上传
     * @param fastDFSFile 需要上传的文件
     */
    public static String[] upload(FastDFSFile fastDFSFile) throws Exception{
        //为文件添加附加参数
        NameValuePair[] meta_list = new NameValuePair[1];
         meta_list[0] = new NameValuePair("拍摄地址", "北京");

        //创建TrackClient客户端对象
        TrackerClient trackerClient = new TrackerClient();

        //获取链接信息（含Stirage）
        TrackerServer trackerServer = trackerClient.getConnection();

        //获取storage信息，创建StorageClient对象，存储storageClient连接信息
        StorageClient storageClient = new StorageClient(trackerServer, null);

        //通过StorageClient访问Storage 实现文件上传 获取文件上传后的存储信息
        /***
         * 1.上传文件的字节数组
         * 2.文件的扩展名 jpg png
         * 3.附加参数 比如地址
         */
        String[] strings = storageClient.upload_file(fastDFSFile.getContent(), fastDFSFile.getExt(), meta_list);
        return strings;
    }

    /***
     * 2.文件下载
     * @param groupName 文件的组名 如 group1
     * @param remoteFileName 文件存储的路径名 如 M00/00/00/wKjogGFdxsCAeIm8AAJH_LJh2Zk073.png
     */
    public static InputStream downloadFile(String groupName, String remoteFileName)throws Exception{
        //创建TrackerClient对象 访问TrackerServer
        TrackerClient trackerClient = new TrackerClient();

        //获取TrackServer连接对象
        TrackerServer trackerServer = trackerClient.getConnection();

        //获取storage信息，创建storageClient存储Storage信息
        StorageClient storageClient = new StorageClient(trackerServer, null);

        //下载文件
        byte[] bytes = storageClient.download_file(groupName, remoteFileName);
        return new ByteArrayInputStream(bytes);

    }

    /***
     * 3.获取文件信息
     * @param groupName 文件的组名 如 group1
     * @param remoteFileName 文件存储的路径名 如 M00/00/00/wKjogGFdxsCAeIm8AAJH_LJh2Zk073.png
     */
    public static FileInfo getFileInfo(String groupName,String remoteFileName) throws IOException, MyException {
        //创建TrackerClient对象 访问TrackerServer
        TrackerClient trackerClient = new TrackerClient();

        //获取TrackServer连接对象
        TrackerServer trackerServer = trackerClient.getConnection();

        //获取storage信息，创建storageClient存储Storage信息
        StorageClient storageClient = new StorageClient(trackerServer, null);

        //获取文件信息
        FileInfo file_info = storageClient.get_file_info(groupName, remoteFileName);
        return file_info;
    }

    /****
     * 删除文件
     * @param groupName 文件的组名 如 group1
     * @param remoteFileName 文件存储的路径名 如 M00/00/00/wKjogGFdxsCAeIm8AAJH_LJh2Zk073.png
     */
    public static void deleteFile(String groupName,String remoteFileName) throws Exception {
        //创建TrackerClient对象 访问TrackerServer
        TrackerClient trackerClient = new TrackerClient();

        //获取TrackServer连接对象
        TrackerServer trackerServer = trackerClient.getConnection();

        //获取storage信息，创建storageClient存储Storage信息
        StorageClient storageClient = new StorageClient(trackerServer, null);

        //删除文件
        storageClient.delete_file(groupName,remoteFileName);
    }

    /***
     * 4.相关测试
     * @param args
     */
    public static void main(String[] args) {
        try {
//            //信息获取测试
//            FileInfo info = getFileInfo("group1", "M00/00/00/wKjogGFdxsCAeIm8AAJH_LJh2Zk073.png");
//            System.out.println(info.getFileSize());
//            System.out.println(info.getCreateTimestamp());
//            System.out.println(info.getSourceIpAddr());
//
//            //文件下载测试
//            InputStream inputStream = downloadFile("group1", "M00/00/00/wKjogGFdxsCAeIm8AAJH_LJh2Zk073.png");
//            //将文件字节流写入到本地磁盘
//            FileOutputStream outputStream = new FileOutputStream("E:/aaa.png");
//            //定义缓冲区
//            byte[] by = new byte[1024];
//            int len = 0;
//            while((len = inputStream.read(by))!=-1){
//                outputStream.write(by,0,len);
//            }
//            outputStream.flush();
//            outputStream.close();
//            inputStream.close();

            //文件删除（需要将上面的代码注释后再测试）
            deleteFile("group1","M00/00/00/wKjogGFdxsCAeIm8AAJH_LJh2Zk073.png");

        } catch (Exception e) {
            e.printStackTrace();
        }

    }
}
