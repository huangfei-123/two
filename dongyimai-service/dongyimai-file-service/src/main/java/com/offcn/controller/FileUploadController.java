package com.offcn.controller;

import com.offcn.entity.Result;
import com.offcn.entity.StatusCode;
import com.offcn.file.FastDFSFile;
import com.offcn.util.FastDFSUtil;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@RestController
@CrossOrigin//解决跨域
public class FileUploadController {
    /****
     * 文件上传
     */
    @PostMapping("/upload")
    public Result upload(@RequestParam(value = "file") MultipartFile file) throws Exception{

        //封装文件信息
        FastDFSFile fastDFSFile = new FastDFSFile(
                file.getOriginalFilename(),//文件名
                file.getBytes(), //文件字节数组
                StringUtils.getFilenameExtension(file.getOriginalFilename())// 文件扩展名
        );

        //调用FastDFSUtil工具类将文件传入到FastDFS中
        String[] path = FastDFSUtil.upload(fastDFSFile);//此处因为tracker或虚拟机的内存问题  可能已经满了   无法存了 返回空
        String url = "http://192.168.232.128:8080/"+path[0]+"/"+path[1];
        System.out.println("文件存储的url:"+url);
        return  new Result(true, StatusCode.OK,"上传成功！",url);
    }
}
