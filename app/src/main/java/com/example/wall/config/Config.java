package com.example.wall.config;

import android.annotation.SuppressLint;

import java.text.SimpleDateFormat;

public class Config {
    /**
     * 主机名应改为本机的ip地址（注意不要使用localhost）
     */
    public static final String baseUrl = "http://192.168.0.124:8086/api/";
    public static final String rsUrl = "http://192.168.0.124:8086/image/";
    public static final int IS_TASK = 0;
    public static final int IS_QUESTION = 1;
    public static final int IS_REPEAT = 2;
    public static final String WAIT_PASSED = "待审核";
    public static final String WAIT_USER_PASSED = "待采纳";
    public static final String USER_PASSED = "已采纳";
    public static final String QUESTION_WAIT_SOLVE="未解决";
    public static final String QUESTION_SOLVE = "已解决";
    public static final String TASK_WAIT_SOLVE="待接受";
    public static final String TASK_SOLVE="已完成";
    public static final String TASK_DOING_SOLVE="进行中";
    public static final String NOT_PASSED="不通过";
    @SuppressLint("SimpleDateFormat")
    public static final SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd hh:mm");
}
