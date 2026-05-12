package com.pingyu.infodiet.model.auth;

/**
 * 当前登录用户上下文
 */
public final class LoginUserContext {

    private static final ThreadLocal<LoginUser> LOGIN_USER_THREAD_LOCAL = new ThreadLocal<>();

    private LoginUserContext() {
    }

    /**
     * 设置当前用户
     */
    public static void set(LoginUser loginUser) {
        LOGIN_USER_THREAD_LOCAL.set(loginUser);
    }

    /**
     * 获取当前用户
     */
    public static LoginUser get() {
        return LOGIN_USER_THREAD_LOCAL.get();
    }

    /**
     * 获取当前用户 ID
     */
    public static Long getUserId() {
        LoginUser loginUser = get();
        return loginUser == null ? null : loginUser.getUserId();
    }

    /**
     * 获取当前角色
     */
    public static String getRole() {
        LoginUser loginUser = get();
        return loginUser == null ? null : loginUser.getRole();
    }

    /**
     * 清理上下文
     */
    public static void clear() {
        LOGIN_USER_THREAD_LOCAL.remove();
    }
}
