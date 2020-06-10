package com.xiezy;

public class Test {

    public static void main(String[] args) throws Exception {

        String str = "ABBCCCCCBBAB";
        char[] chars = str.toCharArray();
        StringBuilder stringBuilder = new StringBuilder().append(chars[0]);
        char pre = chars[0];
        for (int i = 1; i < chars.length; i++) {
            if (chars[i] == pre) {
                continue;
            }

            pre = chars[i];
            stringBuilder.append(chars[i]);
        }

        System.out.println(stringBuilder.toString());

//        RetryPolicy retryPolicy = new ExponentialBackoffRetry(1000, 3);
//        CuratorFramework client = CuratorFrameworkFactory.newClient("192.168.88.100:2181", retryPolicy);
//        client.start();
//
//        client.create().forPath("/test");
    }


}
