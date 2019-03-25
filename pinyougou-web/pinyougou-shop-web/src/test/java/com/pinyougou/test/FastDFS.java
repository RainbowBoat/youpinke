package com.pinyougou.test;

import org.csource.common.MyException;
import org.csource.fastdfs.ClientGlobal;
import org.csource.fastdfs.StorageClient;
import org.junit.Test;

import java.io.IOException;
import java.util.Arrays;

public class FastDFS {

    @Test
    public void testFastDFS() throws Exception {
        String conf_filename = "tracker_server=192.168.12.131:22122";

        ClientGlobal.init(conf_filename);

        StorageClient storageClient = new StorageClient();

        String[] arr = storageClient.upload_file("C:\\Users\\Shuqian\\Desktop\\Mybatis之工作原理 - 只会一点Java - CSDN博客.html", "jpg", null);

        System.out.println(Arrays.toString(arr));
    }
}
