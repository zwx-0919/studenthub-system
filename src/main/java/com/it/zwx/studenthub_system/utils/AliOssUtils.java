package com.it.zwx.studenthub_system.utils;

import com.aliyun.oss.*;
import com.aliyun.oss.common.auth.*;
import com.aliyun.oss.common.comm.SignVersion;
import com.aliyun.oss.model.PutObjectRequest;
import com.aliyun.oss.model.PutObjectResult;

import java.io.InputStream;

public class AliOssUtils {

    //地域节点
    private static final String ENDPOINT = "https://oss-cn-beijing.aliyuncs.com";
    private static final String ACCESS_KEY_ID = "";
    private static final String ACCESS_KEY_SECRET = "";
    //存储空间名称
    private static final String BUCKET_NAME = "java-web-zwx";


    //返回值类型，如果图片上传成功，需要得到访问地址
    public static String uploadFile(String objectName, InputStream stream) throws Exception {

        CredentialsProvider credentialsProvider = new DefaultCredentialProvider(ACCESS_KEY_ID, ACCESS_KEY_SECRET);

        //地域
        String region = "cn-beijing";

        // 创建OSSClient实例：创建了对象上传的客户端
        ClientBuilderConfiguration clientBuilderConfiguration = new ClientBuilderConfiguration();
        clientBuilderConfiguration.setSignatureVersion(SignVersion.V4);
        OSS ossClient = OSSClientBuilder.create()
                .endpoint(ENDPOINT)
                .credentialsProvider(credentialsProvider)
                .clientConfiguration(clientBuilderConfiguration)
                .region(region)
                .build();

        //声明返回值
        String url = "";

        try {

            // 对象存储的请求类。把如上的转为输入流，包含上传对象等信息
            PutObjectRequest putObjectRequest = new PutObjectRequest(BUCKET_NAME, objectName,stream);
            // 上传图片。调用ossClient把请求对象传递到服务器中
            PutObjectResult result = ossClient.putObject(putObjectRequest);

            //result成功执行，没有报错，就需要返回一个访问地址
            url = "https://" + BUCKET_NAME + "." + ENDPOINT.substring(ENDPOINT.lastIndexOf("/") + 1)  + "/"  + objectName;

        } catch (OSSException oe) {
            System.out.println("Caught an OSSException, which means your request made it to OSS, "
                    + "but was rejected with an error response for some reason.");
            System.out.println("Error Message:" + oe.getErrorMessage());
            System.out.println("Error Code:" + oe.getErrorCode());
            System.out.println("Request ID:" + oe.getRequestId());
            System.out.println("Host ID:" + oe.getHostId());
        } catch (ClientException ce) {
            System.out.println("Caught an ClientException, which means the client encountered "
                    + "a serious internal problem while trying to communicate with OSS, "
                    + "such as not being able to access the network.");
            System.out.println("Error Message:" + ce.getMessage());
        } finally {
            if (ossClient != null) {
                ossClient.shutdown();
            }
        }
        return url;
    }

    //删除阿里云文件方法 需要通过文件名称删除
    public static void remove(String url) throws Exception{
        //截取文件名， 从 / 往后截取
        url = url.substring(url.lastIndexOf("/") + 1);
        //创建Oss客户端实例,地域节点，秘钥，值
        OSS ossClient = new OSSClientBuilder().build(ENDPOINT, ACCESS_KEY_ID, ACCESS_KEY_SECRET);
        //删除文件
        ossClient.deleteObject(BUCKET_NAME,url);
        //关闭客户端
        ossClient.shutdown();
    }
}