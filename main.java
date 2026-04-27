/*
夜深人静月如钩，思君情深意未休。
梦中相会欢声笑，醒来却是泪长流。
相思成疾心难愈，愿君安好莫担忧。
此情绵绵无绝期，愿随君侧共白头。
*/

load(appPath + "/更新日志.java");
load(appPath + "/extend/使用说明.java");
load(appPath + "/extend/更新检测.java");
load(appPath + "/extend/入群弹窗.java");
//↑注释或删去这一行即可去除入群弹窗

import javax.crypto.Cipher;
import javax.crypto.spec.SecretKeySpec;
import java.security.MessageDigest;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;
import java.util.List;
import java.util.ArrayList;
import java.util.Map;
import java.util.HashMap;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import java.util.concurrent.ConcurrentHashMap;
import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.BufferedReader;
import java.io.FileReader;
import java.io.BufferedWriter;
import java.io.FileWriter;
import java.net.HttpURLConnection;
import java.net.URL;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Color;
import android.graphics.RectF;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.graphics.Matrix;
import android.graphics.Typeface;
import android.graphics.Paint.Align;
import android.graphics.drawable.GradientDrawable;
import android.app.Activity;
import android.app.Dialog;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Button;
import android.view.Gravity;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.view.MotionEvent;
import java.util.Set;
import java.util.HashSet;
import java.util.Collections;
import android.widget.Switch;
import android.widget.CompoundButton;

String signDataFileEncrypted = appPath + "/sign/data.dat";
String signSwitchFileEncrypted = appPath + "/sign/switch.dat";
String proxySignSwitchFileEncrypted = appPath + "/sign/proxy_switch.dat";
String monthlyRewardFile = appPath + "/sign/monthly_reward.dat";
boolean isNightMode = false;
AlertDialog currentDialog = null;
String currentGroupId = "";

private interface OnStateChangeListener {
    void onStateChanged(boolean isChecked);
}
private static List<Map<String, Object>> cachedUserList = null;
private static String cachedGroupId = null;
private static final ReentrantReadWriteLock dataLock = new ReentrantReadWriteLock();
private static final ReentrantReadWriteLock switchLock = new ReentrantReadWriteLock();
private static final int POINTS_FOR_LIKE_FRIEND = 10;
private static final int POINTS_FOR_LIKE_NONFRIEND = 50;
private static final int LIKES_PER_FRIEND = 10;
private static final int LIKES_PER_NONFRIEND = 50;
private static final int POINTS_FOR_DOUBLE_CARD = 10;
private static final int POINTS_FOR_TENFOLD_CARD = 60;
private static final int RANKING_PAGE_SIZE = 10;
private static final String DEFAULT_PHOTOS_DIR = appPath + "/define_photos";
private static Map<String, String> likeSessionMap = new ConcurrentHashMap<String, String>();
private static Map<String, TimerTask> buyMakeupTimerTasks = new ConcurrentHashMap<String, TimerTask>();
private static Map<String, TimerTask> likeTimerTasks = new ConcurrentHashMap<String, TimerTask>();
private String Author = "";//为了方便写了Author，可以修改这里的QQ号为你的（特权QQ说是）
private static Map<String, Integer> grabCommandCountMap = new ConcurrentHashMap<String, Integer>();
private static Map<String, Map<String, Object>> redPacketDataMap = new ConcurrentHashMap<String, Map<String, Object>>();
private static Map<String, TimerTask> redPacketTimerTasks = new ConcurrentHashMap<String, TimerTask>();
private static final ReentrantReadWriteLock redPacketLock = new ReentrantReadWriteLock();
String redPacketDataFile = appPath + "/sign/redpacket.dat";
private static int redPacketSeq = 1;
private static final String[][] ACHIEVEMENTS = {
    {"first_sign", "[初来乍到]", "首次签到", "10"},
    {"total_7", "[小有所成]", "累计签到7天", "20"},
    {"total_30", "[渐入佳境]", "累计签到30天", "50"},
    {"total_100", "[锋芒毕露]", "累计签到100天", "100"},
    {"total_365", "[签到之王]", "累计签到365天", "300"},
    {"consecutive_7", "[周常坚持]", "连续签到7天", "30"},
    {"consecutive_30", "[月度冠军]", "连续签到30天", "80"},
    {"consecutive_100", "[百日战神]", "连续签到100天", "200"},
    {"consecutive_365", "[年签大佬]", "连续签到365天", "500"}
};

addItem("签到脚本配置", "签到设置");
addItem("脚本使用说明", "脚本使用说明");
addItem("脚本更新日志", "更新日志");

public void 签到设置(String q, String u, int lx) {
    if (lx != 2) {
        toast("请在群聊中使用此功能");
        return;
    }
    
    Activity a = getActivity();
    if (a == null) return;
    
    a.runOnUiThread(new Runnable() {
        public void run() {
            currentGroupId = q;
            主弹窗();
        }
    });
}

public void 图签开关检测(String g, String u, int t) {
    if (t != 2) {
        toast("请在群聊中使用");
        return;
    }
    
    String currentGroup = getCurrentGroupUin();
    if (currentGroup.equals("0")) {
        toast("未检测到群聊");
        return;
    }
    
    String switchState = 读取签到开关配置(currentGroup);
    if (switchState.equals("开")) {
        写入签到开关配置(currentGroup, "关");
        toast("已关闭本群签到功能");
    } else {
        写入签到开关配置(currentGroup, "开");
        toast("已开启本群签到功能");
    }
}

public void 更新日志(String g, String u, int t) {
    final Activity activity = getActivity();
    if (activity == null) return;
    activity.runOnUiThread(new Runnable() {
        public void run() {
            AlertDialog.Builder builder = new AlertDialog.Builder(activity, AlertDialog.THEME_DEVICE_DEFAULT_LIGHT);
            builder.setTitle("脚本更新日志");
            builder.setMessage("此脚本禁止商用！\n\n更新日志：\n" + Update_Logs + "\n\n");
            builder.setPositiveButton("确定", null);
            builder.setCancelable(true);
            AlertDialog dialog = builder.show();
            if (dialog != null && dialog.getWindow() != null) {
                GradientDrawable bg = new GradientDrawable();
                bg.setColor(Color.parseColor(isDarkMode() ? "#FF3A3355" : "#FFFFF0F5"));
                bg.setCornerRadius(c(24));
                bg.setStroke(c(2), Color.parseColor("#40FFB6C1"));
                dialog.getWindow().setBackgroundDrawable(bg);
            }
        }
    });
}

private void 主弹窗() {
    Activity a = getActivity();
    if (a == null) return;
    
    if (currentDialog != null && currentDialog.isShowing()) {
        currentDialog.dismiss();
        currentDialog = null;
    }
    
    try {
        LinearLayout mainLayout = new LinearLayout(a);
        mainLayout.setOrientation(LinearLayout.VERTICAL);
        mainLayout.setPadding(dp(20), dp(20), dp(20), dp(20));
        
        GradientDrawable bgDrawable = new GradientDrawable();
        bgDrawable.setColor(isNightMode ? Color.parseColor("#1E1E1E") : Color.parseColor("#FFF8F0"));
        bgDrawable.setCornerRadius(dp(28));
        mainLayout.setBackground(bgDrawable);
        
        LinearLayout toolbar = new LinearLayout(a);
        toolbar.setOrientation(LinearLayout.HORIZONTAL);
        toolbar.setLayoutParams(new LinearLayout.LayoutParams(
            LinearLayout.LayoutParams.MATCH_PARENT,
            LinearLayout.LayoutParams.WRAP_CONTENT));
        toolbar.setGravity(Gravity.CENTER_VERTICAL);
        
        Button nightBtn = createIconButton(a, isNightMode ? "☀️" : "🌙", 18);
        nightBtn.setTextColor(isNightMode ? Color.parseColor("#FFB74D") : Color.parseColor("#5C6BC0"));
        LinearLayout.LayoutParams nightParams = new LinearLayout.LayoutParams(
            LinearLayout.LayoutParams.WRAP_CONTENT,
            LinearLayout.LayoutParams.WRAP_CONTENT);
        nightParams.gravity = Gravity.START;
        nightBtn.setLayoutParams(nightParams);
        toolbar.addView(nightBtn);
        
        View spacer = new View(a);
        spacer.setLayoutParams(new LinearLayout.LayoutParams(0, 1, 1));
        toolbar.addView(spacer);
        
        Button closeBtn = createIconButton(a, "保存", 20);
        closeBtn.setTextColor(isNightMode ? Color.parseColor("#FFFFFF") : Color.parseColor("#D47F6B"));
        LinearLayout.LayoutParams closeParams = new LinearLayout.LayoutParams(
            LinearLayout.LayoutParams.WRAP_CONTENT,
            LinearLayout.LayoutParams.WRAP_CONTENT);
        closeParams.gravity = Gravity.END;
        closeBtn.setLayoutParams(closeParams);
        toolbar.addView(closeBtn);
        
        mainLayout.addView(toolbar);
        
        TextView titleView = new TextView(a);
        titleView.setText("签到脚本配置");
        titleView.setTextSize(20);
        titleView.setTypeface(Typeface.DEFAULT_BOLD);
        titleView.setTextColor(isNightMode ? Color.parseColor("#FFB74D") : Color.parseColor("#D47F6B"));
        titleView.setGravity(Gravity.CENTER);
        titleView.setPadding(0, dp(8), 0, dp(12));
        mainLayout.addView(titleView);
        
        View divider = new View(a);
        LinearLayout.LayoutParams dividerParams = new LinearLayout.LayoutParams(
            LinearLayout.LayoutParams.MATCH_PARENT, dp(1));
        dividerParams.setMargins(dp(20), 0, dp(20), dp(16));
        divider.setLayoutParams(dividerParams);
        divider.setBackgroundColor(isNightMode ? Color.parseColor("#444444") : Color.parseColor("#FFE5D9"));
        mainLayout.addView(divider);
        
        final LinearLayout cardContainer = new LinearLayout(a);
        cardContainer.setOrientation(LinearLayout.VERTICAL);
        cardContainer.setLayoutParams(new LinearLayout.LayoutParams(
            LinearLayout.LayoutParams.MATCH_PARENT,
            LinearLayout.LayoutParams.WRAP_CONTENT));
        
        String switchState = 读取签到开关配置(currentGroupId);
        String proxySwitchState = 获取代签配置(currentGroupId);
        
        final boolean[] tempSignState = {switchState.equals("开")};
        final boolean[] tempProxyState = {proxySwitchState.equals("开")};
        
        LinearLayout signCard = createSwitchCard(a, "签到开关", tempSignState[0], new OnStateChangeListener() {
            public void onStateChanged(boolean isChecked) {
                tempSignState[0] = isChecked;
            }
        });
        
        LinearLayout proxyCard = createSwitchCard(a, "代签开关", tempProxyState[0], new OnStateChangeListener() {
            public void onStateChanged(boolean isChecked) {
                tempProxyState[0] = isChecked;
            }
        });
        
        cardContainer.addView(signCard);
        cardContainer.addView(proxyCard);
        
        mainLayout.addView(cardContainer);
        
        final LinearLayout btnContainer = new LinearLayout(a);
        btnContainer.setOrientation(LinearLayout.VERTICAL);
        btnContainer.setPadding(0, dp(16), 0, 0);
        mainLayout.addView(btnContainer);
        
        Button dataBtn = createGradientButton(a, "查看签到数据", 
            isNightMode ? Color.parseColor("#FFB74D") : Color.parseColor("#D47F6B"),
            isNightMode ? Color.parseColor("#FFA04D") : Color.parseColor("#C05A4A"));
        LinearLayout.LayoutParams dataBtnParams = new LinearLayout.LayoutParams(
            LinearLayout.LayoutParams.MATCH_PARENT,
            LinearLayout.LayoutParams.WRAP_CONTENT);
        dataBtn.setLayoutParams(dataBtnParams);
        dataBtn.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                显示主弹窗();
            }
        });
        btnContainer.addView(dataBtn);
        
        AlertDialog.Builder builder = new AlertDialog.Builder(a, AlertDialog.THEME_DEVICE_DEFAULT_LIGHT);
        builder.setView(mainLayout);
        builder.setCancelable(false);
        currentDialog = builder.create();
        
        if (currentDialog.getWindow() != null) {
            Window window = currentDialog.getWindow();
            window.setDimAmount(0f);
            window.setBackgroundDrawable(new android.graphics.drawable.ColorDrawable(android.graphics.Color.TRANSPARENT));
            window.setLayout((int)(a.getResources().getDisplayMetrics().widthPixels * 0.85),
                             WindowManager.LayoutParams.WRAP_CONTENT);
            window.setGravity(Gravity.CENTER);
        }
        
        currentDialog.show();
        
        nightBtn.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                isNightMode = !isNightMode;
                主弹窗();
                toast(isNightMode ? "夜间模式已开启" : "白天模式已开启");
            }
        });
        
        closeBtn.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                写入签到开关配置(currentGroupId, tempSignState[0] ? "开" : "关");
                设置代签配置(currentGroupId, tempProxyState[0] ? "开" : "关");
                
                cachedUserList = null;
                cachedGroupId = null;
                if (currentDialog != null) currentDialog.dismiss();
                currentDialog = null;
                
                toast("设置已保存");
            }
        });
        
    } catch (Exception e) {
        toast("打开配置失败: " + e.getMessage());
    }
}

private void 显示主弹窗() {
    Activity a = getActivity();
    if (a == null) return;
    
    if (currentDialog != null && currentDialog.isShowing()) {
        currentDialog.dismiss();
        currentDialog = null;
    }
    
    try {
        LinearLayout mainLayout = new LinearLayout(a);
        mainLayout.setOrientation(LinearLayout.VERTICAL);
        mainLayout.setPadding(dp(20), dp(20), dp(20), dp(20));
        
        GradientDrawable bgDrawable = new GradientDrawable();
        bgDrawable.setColor(isNightMode ? Color.parseColor("#1E1E1E") : Color.parseColor("#FFF8F0"));
        bgDrawable.setCornerRadius(dp(28));
        mainLayout.setBackground(bgDrawable);
        
        LinearLayout toolbar = new LinearLayout(a);
        toolbar.setOrientation(LinearLayout.HORIZONTAL);
        toolbar.setGravity(Gravity.CENTER_VERTICAL);
        toolbar.setLayoutParams(new LinearLayout.LayoutParams(
            LinearLayout.LayoutParams.MATCH_PARENT,
            LinearLayout.LayoutParams.WRAP_CONTENT));
        
        Button backBtn = createIconButton(a, "←", 24);
        backBtn.setTextColor(isNightMode ? Color.parseColor("#FFFFFF") : Color.parseColor("#D47F6B"));
        LinearLayout.LayoutParams backParams = new LinearLayout.LayoutParams(
            LinearLayout.LayoutParams.WRAP_CONTENT,
            LinearLayout.LayoutParams.WRAP_CONTENT);
        backParams.gravity = Gravity.START;
        backBtn.setLayoutParams(backParams);
        toolbar.addView(backBtn);
        
        View spacer = new View(a);
        spacer.setLayoutParams(new LinearLayout.LayoutParams(0, 1, 1));
        toolbar.addView(spacer);
        
        TextView titleView = new TextView(a);
        titleView.setText("签到数据");
        titleView.setTextSize(18);
        titleView.setTypeface(Typeface.DEFAULT_BOLD);
        titleView.setTextColor(isNightMode ? Color.parseColor("#FFB74D") : Color.parseColor("#D47F6B"));
        toolbar.addView(titleView);
        
        spacer = new View(a);
        spacer.setLayoutParams(new LinearLayout.LayoutParams(0, 1, 1));
        toolbar.addView(spacer);
        
        Button closeBtn = createIconButton(a, "✕", 20);
        closeBtn.setTextColor(isNightMode ? Color.parseColor("#FFFFFF") : Color.parseColor("#D47F6B"));
        LinearLayout.LayoutParams closeParams = new LinearLayout.LayoutParams(
            LinearLayout.LayoutParams.WRAP_CONTENT,
            LinearLayout.LayoutParams.WRAP_CONTENT);
        closeParams.gravity = Gravity.END;
        closeBtn.setLayoutParams(closeParams);
        toolbar.addView(closeBtn);
        
        mainLayout.addView(toolbar);
        
        View divider = new View(a);
        LinearLayout.LayoutParams dividerParams = new LinearLayout.LayoutParams(
            LinearLayout.LayoutParams.MATCH_PARENT, dp(1));
        dividerParams.setMargins(0, dp(12), 0, dp(12));
        divider.setLayoutParams(dividerParams);
        divider.setBackgroundColor(isNightMode ? Color.parseColor("#444444") : Color.parseColor("#FFE5D9"));
        mainLayout.addView(divider);
        
        final TextView loadingView = new TextView(a);
        loadingView.setText("正在加载签到数据...");
        loadingView.setTextSize(14);
        loadingView.setTextColor(isNightMode ? Color.parseColor("#AAAAAA") : Color.parseColor("#999999"));
        loadingView.setGravity(Gravity.CENTER);
        loadingView.setPadding(0, dp(40), 0, dp(40));
        mainLayout.addView(loadingView);
        
        AlertDialog.Builder builder = new AlertDialog.Builder(a, AlertDialog.THEME_DEVICE_DEFAULT_LIGHT);
        builder.setView(mainLayout);
        builder.setCancelable(false);
        currentDialog = builder.create();
        
        if (currentDialog.getWindow() != null) {
            Window window = currentDialog.getWindow();
            window.setDimAmount(0f);
            window.setBackgroundDrawable(new android.graphics.drawable.ColorDrawable(android.graphics.Color.TRANSPARENT));
            window.setLayout((int)(a.getResources().getDisplayMetrics().widthPixels * 0.9),
                             (int)(a.getResources().getDisplayMetrics().heightPixels * 0.7));
            window.setGravity(Gravity.CENTER);
        }
        
        currentDialog.show();
        
        new Thread(new Runnable() {
            public void run() {
                List<Map<String, Object>> userList;
                if (cachedUserList != null && cachedGroupId != null && cachedGroupId.equals(currentGroupId)) {
                    userList = cachedUserList;
                } else {
                    userList = loadGroupSignData(currentGroupId);
                    cachedUserList = userList;
                    cachedGroupId = currentGroupId;
                }
                
                final List<Map<String, Object>> finalUserList = userList;
                
                a.runOnUiThread(new Runnable() {
                    public void run() {
                        mainLayout.removeView(loadingView);
                        添加数据内容(a, mainLayout, finalUserList, currentDialog);
                    }
                });
            }
        }).start();
        
        backBtn.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                cachedUserList = null;
                cachedGroupId = null;
                主弹窗();
            }
        });
        
        closeBtn.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                cachedUserList = null;
                cachedGroupId = null;
                if (currentDialog != null) currentDialog.dismiss();
                currentDialog = null;
            }
        });
        
    } catch (Exception e) {
        toast("打开数据界面失败: " + e.getMessage());
    }
}

private void 添加数据内容(Activity a, LinearLayout mainLayout, List userList, final Dialog parentDialog) {
    if (userList == null || userList.size() == 0) {
        TextView emptyView = new TextView(a);
        emptyView.setText("暂无签到数据\n\n签到一下吧");
        emptyView.setTextSize(14);
        emptyView.setTextColor(isNightMode ? Color.parseColor("#AAAAAA") : Color.parseColor("#999999"));
        emptyView.setGravity(Gravity.CENTER);
        emptyView.setPadding(0, dp(40), 0, dp(40));
        emptyView.setLayoutParams(new LinearLayout.LayoutParams(
            LinearLayout.LayoutParams.MATCH_PARENT,
            LinearLayout.LayoutParams.MATCH_PARENT));
        mainLayout.addView(emptyView);
        return;
    }
    
    final int PAGE_SIZE = 10;
    final int[] currentLoaded = {0};
    final List dataList = userList;
    
    LinearLayout container = new LinearLayout(a);
    container.setOrientation(LinearLayout.VERTICAL);
    container.setLayoutParams(new LinearLayout.LayoutParams(
        LinearLayout.LayoutParams.MATCH_PARENT,
        LinearLayout.LayoutParams.MATCH_PARENT));
    
    final ScrollView scrollView = new ScrollView(a);
    scrollView.setLayoutParams(new LinearLayout.LayoutParams(
        LinearLayout.LayoutParams.MATCH_PARENT,
        0, 1));
    scrollView.setFillViewport(true);
    
    final LinearLayout itemsContainer = new LinearLayout(a);
    itemsContainer.setOrientation(LinearLayout.VERTICAL);
    itemsContainer.setLayoutParams(new LinearLayout.LayoutParams(
        LinearLayout.LayoutParams.MATCH_PARENT,
        LinearLayout.LayoutParams.WRAP_CONTENT));
    
    final TextView loadMoreTip = new TextView(a);
    loadMoreTip.setText("▼ 加载更多 ▼");
    loadMoreTip.setTextSize(12);
    loadMoreTip.setTextColor(isNightMode ? Color.parseColor("#FFB74D") : Color.parseColor("#D47F6B"));
    loadMoreTip.setGravity(Gravity.CENTER);
    loadMoreTip.setPadding(0, dp(16), 0, dp(16));
    loadMoreTip.setClickable(true);
    
    scrollView.addView(itemsContainer);
    container.addView(scrollView);
    
    LinearLayout bottomBar = new LinearLayout(a);
    bottomBar.setOrientation(LinearLayout.VERTICAL);
    bottomBar.setGravity(Gravity.CENTER);
    bottomBar.setPadding(0, dp(16), 0, dp(16));
    bottomBar.setLayoutParams(new LinearLayout.LayoutParams(
        LinearLayout.LayoutParams.MATCH_PARENT,
        LinearLayout.LayoutParams.WRAP_CONTENT));
    Button clearBtn = createMainButton(a, "清空全部", Color.parseColor("#FFB7B2"));
    clearBtn.setLayoutParams(new LinearLayout.LayoutParams(
        LinearLayout.LayoutParams.MATCH_PARENT,
        LinearLayout.LayoutParams.WRAP_CONTENT));
    clearBtn.setOnClickListener(new View.OnClickListener() {
        public void onClick(View v) {
            parentDialog.dismiss();
            显示删除全部确认弹窗();
        }
    });
    bottomBar.addView(clearBtn);
    container.addView(bottomBar);
    
    mainLayout.addView(container);
    
    Runnable loadMore = new Runnable() {
        public void run() {
            int start = currentLoaded[0];
            int end = Math.min(start + PAGE_SIZE, dataList.size());
            if (start >= end) return;
            
            if (itemsContainer.indexOfChild(loadMoreTip) != -1) {
                itemsContainer.removeView(loadMoreTip);
            }
            
            for (int i = start; i < end; i++) {
                Map<String, Object> data = (Map<String, Object>) dataList.get(i);
                String qq = (String) data.get("qq");
                String nickname = 获取实时昵称(currentGroupId, qq);
                if (nickname == null || nickname.isEmpty()) nickname = qq;
                int consecutive = (Integer) data.get("consecutive");
                int total = (Integer) data.get("total");
                String lastDate = (String) data.get("lastDate");
                
                Map<String, Object> fullData = loadUserSignData(currentGroupId, qq);
                int points = (Integer) fullData.get("points");
                int makeupCards = (Integer) fullData.get("makeupCards");
                
                LinearLayout itemRow = new LinearLayout(a);
                itemRow.setOrientation(LinearLayout.HORIZONTAL);
                itemRow.setGravity(Gravity.CENTER_VERTICAL);
                itemRow.setPadding(dp(12), dp(12), dp(12), dp(12));
                itemRow.setLayoutParams(new LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT));
                
                LinearLayout textLayout = new LinearLayout(a);
                textLayout.setOrientation(LinearLayout.VERTICAL);
                textLayout.setLayoutParams(new LinearLayout.LayoutParams(
                    0, LinearLayout.LayoutParams.WRAP_CONTENT, 1));
                
                TextView nameView = new TextView(a);
                nameView.setText(nickname);
                nameView.setTextSize(16);
                nameView.setTextColor(isNightMode ? Color.parseColor("#FFB74D") : Color.parseColor("#D47F6B"));
                nameView.setTypeface(Typeface.DEFAULT_BOLD);
                textLayout.addView(nameView);
                
                TextView infoView = new TextView(a);
                infoView.setText(String.format("连续 %d 天  |  累计 %d 天  |  积分 %d\n最后签到: %s", 
                    consecutive, total, points, lastDate));
                infoView.setTextSize(12);
                infoView.setTextColor(isNightMode ? Color.parseColor("#AAAAAA") : Color.parseColor("#888888"));
                textLayout.addView(infoView);
                
                itemRow.addView(textLayout);
                
                LinearLayout btnContainer = new LinearLayout(a);
                btnContainer.setOrientation(LinearLayout.HORIZONTAL);
                btnContainer.setGravity(Gravity.CENTER_VERTICAL);
                
                Button editBtn = new Button(a);
                editBtn.setText("编辑");
                editBtn.setTextSize(16);
                editBtn.setPadding(dp(10), dp(6), dp(10), dp(6));
                GradientDrawable editBg = new GradientDrawable();
                editBg.setCornerRadius(dp(16));
                editBg.setColor(Color.parseColor("#4CAF50"));
                editBtn.setBackground(editBg);
                
                editBtn.setTag(new String[]{qq, nickname, String.valueOf(consecutive), String.valueOf(total), String.valueOf(points), String.valueOf(makeupCards)});
                editBtn.setOnClickListener(new View.OnClickListener() {
                    public void onClick(View v) {
                        String[] userData = (String[]) v.getTag();
                        if (userData != null && userData.length >= 6) {
                            parentDialog.dismiss();
                            显示编辑弹窗(userData[0], userData[1], 
                                Integer.parseInt(userData[2]), 
                                Integer.parseInt(userData[3]),
                                Integer.parseInt(userData[4]),
                                Integer.parseInt(userData[5]));
                        }
                    }
                });
                btnContainer.addView(editBtn);
                
                View spacer = new View(a);
                spacer.setLayoutParams(new LinearLayout.LayoutParams(dp(8), 1));
                btnContainer.addView(spacer);
                
                Button deleteBtn = new Button(a);
                deleteBtn.setText("删除");
                deleteBtn.setTextSize(12);
                deleteBtn.setTextColor(Color.WHITE);
                deleteBtn.setPadding(dp(12), dp(6), dp(12), dp(6));
                GradientDrawable deleteBg = new GradientDrawable();
                deleteBg.setCornerRadius(dp(16));
                deleteBg.setColor(Color.parseColor("#EF5350"));
                deleteBtn.setBackground(deleteBg);
                
                deleteBtn.setTag(new String[]{qq, nickname});
                deleteBtn.setOnClickListener(new View.OnClickListener() {
                    public void onClick(View v) {
                        String[] userData = (String[]) v.getTag();
                        if (userData != null && userData.length >= 2) {
                            parentDialog.dismiss();
                            显示确认删除弹窗(userData[0], userData[1]);
                        }
                    }
                });
                btnContainer.addView(deleteBtn);
                
                itemRow.addView(btnContainer);
                
                itemsContainer.addView(itemRow);
                
                if (i < end - 1) {
                    View divider = new View(a);
                    divider.setBackgroundColor(isNightMode ? Color.parseColor("#444444") : Color.parseColor("#FFE5D9"));
                    LinearLayout.LayoutParams divParams = new LinearLayout.LayoutParams(
                        LinearLayout.LayoutParams.MATCH_PARENT, dp(1));
                    divParams.setMargins(dp(16), 0, dp(16), 0);
                    divider.setLayoutParams(divParams);
                    itemsContainer.addView(divider);
                }
            }
            
            currentLoaded[0] = end;
            
            if (end < dataList.size()) {
                itemsContainer.addView(loadMoreTip);
            } else {
                TextView endTip = new TextView(a);
                endTip.setText("—— 已全部加载 ——");
                endTip.setTextSize(12);
                endTip.setTextColor(isNightMode ? Color.parseColor("#AAAAAA") : Color.parseColor("#999999"));
                endTip.setGravity(Gravity.CENTER);
                endTip.setPadding(0, dp(16), 0, dp(16));
                itemsContainer.addView(endTip);
            }
        }
    };
    
    loadMoreTip.setOnClickListener(new View.OnClickListener() {
        public void onClick(View v) {
            loadMore.run();
        }
    });
    
    loadMore.run();
}

private void 显示编辑弹窗(String qq, String nickname, int consecutive, int total, int points, int makeupCards) {
    Activity a = getActivity();
    if (a == null) return;
    
    Dialog dialog = new Dialog(a);
    dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
    dialog.setCanceledOnTouchOutside(false);
    
    LinearLayout mainLayout = new LinearLayout(a);
    mainLayout.setOrientation(LinearLayout.VERTICAL);
    mainLayout.setPadding(dp(24), dp(20), dp(24), dp(20));
    
    GradientDrawable bgDrawable = new GradientDrawable();
    bgDrawable.setColor(isNightMode ? Color.parseColor("#1E1E1E") : Color.parseColor("#FFF8F0"));
    bgDrawable.setCornerRadius(dp(28));
    mainLayout.setBackground(bgDrawable);
    
    TextView titleView = new TextView(a);
    titleView.setText("编辑签到数据");
    titleView.setTextSize(18);
    titleView.setTypeface(Typeface.DEFAULT_BOLD);
    titleView.setTextColor(isNightMode ? Color.parseColor("#FFB74D") : Color.parseColor("#D47F6B"));
    titleView.setGravity(Gravity.CENTER);
    titleView.setPadding(0, 0, 0, dp(16));
    mainLayout.addView(titleView);
    
    TextView userView = new TextView(a);
    userView.setText("用户：" + nickname + " (" + qq + ")");
    userView.setTextSize(14);
    userView.setTextColor(isNightMode ? Color.parseColor("#AAAAAA") : Color.parseColor("#666666"));
    userView.setGravity(Gravity.CENTER);
    userView.setPadding(0, 0, 0, dp(20));
    mainLayout.addView(userView);
    
    LinearLayout consecutiveLayout = createEditRow(a, "连续签到", String.valueOf(consecutive));
    mainLayout.addView(consecutiveLayout);
    final android.widget.EditText consecutiveInput = (android.widget.EditText) consecutiveLayout.getChildAt(1);
    
    LinearLayout totalLayout = createEditRow(a, "累计签到", String.valueOf(total));
    mainLayout.addView(totalLayout);
    final android.widget.EditText totalInput = (android.widget.EditText) totalLayout.getChildAt(1);
    
    LinearLayout pointsLayout = createEditRow(a, "当前积分", String.valueOf(points));
    mainLayout.addView(pointsLayout);
    final android.widget.EditText pointsInput = (android.widget.EditText) pointsLayout.getChildAt(1);
    
    LinearLayout makeupLayout = createEditRow(a, "补签卡", String.valueOf(makeupCards));
    mainLayout.addView(makeupLayout);
    final android.widget.EditText makeupInput = (android.widget.EditText) makeupLayout.getChildAt(1);
    
    LinearLayout quickBtnLayout = new LinearLayout(a);
    quickBtnLayout.setOrientation(LinearLayout.HORIZONTAL);
    quickBtnLayout.setGravity(Gravity.CENTER);
    quickBtnLayout.setPadding(0, dp(16), 0, dp(8));
    
    Button add10Btn = createSmallButton(a, "+10积分", Color.parseColor("#4CAF50"));
    add10Btn.setOnClickListener(new View.OnClickListener() {
        public void onClick(View v) {
            try {
                int current = Integer.parseInt(pointsInput.getText().toString());
                pointsInput.setText(String.valueOf(current + 10));
            } catch (Exception e) {}
        }
    });
    quickBtnLayout.addView(add10Btn);
    
    View space1 = new View(a);
    space1.setLayoutParams(new LinearLayout.LayoutParams(dp(8), 1));
    quickBtnLayout.addView(space1);
    
    Button add50Btn = createSmallButton(a, "+50积分", Color.parseColor("#2196F3"));
    add50Btn.setOnClickListener(new View.OnClickListener() {
        public void onClick(View v) {
            try {
                int current = Integer.parseInt(pointsInput.getText().toString());
                pointsInput.setText(String.valueOf(current + 50));
            } catch (Exception e) {}
        }
    });
    quickBtnLayout.addView(add50Btn);
    
    View space2 = new View(a);
    space2.setLayoutParams(new LinearLayout.LayoutParams(dp(8), 1));
    quickBtnLayout.addView(space2);
    
    Button addCardBtn = createSmallButton(a, "+1补签卡", Color.parseColor("#FF9800"));
    addCardBtn.setOnClickListener(new View.OnClickListener() {
        public void onClick(View v) {
            try {
                int current = Integer.parseInt(makeupInput.getText().toString());
                makeupInput.setText(String.valueOf(current + 1));
            } catch (Exception e) {}
        }
    });
    quickBtnLayout.addView(addCardBtn);
    
    mainLayout.addView(quickBtnLayout);
    
    LinearLayout btnContainer = new LinearLayout(a);
    btnContainer.setOrientation(LinearLayout.HORIZONTAL);
    btnContainer.setGravity(Gravity.CENTER);
    btnContainer.setPadding(0, dp(16), 0, 0);
    
    Button cancelBtn = createSecondaryButton(a, "取消", 
        isNightMode ? Color.parseColor("#AAAAAA") : Color.parseColor("#999999"),
        isNightMode ? Color.parseColor("#3D3D3D") : Color.parseColor("#F5F5F5"));
    LinearLayout.LayoutParams cancelParams = new LinearLayout.LayoutParams(
        0, LinearLayout.LayoutParams.WRAP_CONTENT, 1
    );
    cancelParams.setMargins(0, 0, dp(8), 0);
    cancelBtn.setLayoutParams(cancelParams);
    
    Button confirmBtn = createMainButton(a, "保存", Color.parseColor("#4CAF50"));
    LinearLayout.LayoutParams confirmParams = new LinearLayout.LayoutParams(
        0, LinearLayout.LayoutParams.WRAP_CONTENT, 1
    );
    confirmParams.setMargins(dp(8), 0, 0, 0);
    confirmBtn.setLayoutParams(confirmParams);
    
    btnContainer.addView(cancelBtn);
    btnContainer.addView(confirmBtn);
    mainLayout.addView(btnContainer);
    
    dialog.setContentView(mainLayout);
    
    Window window = dialog.getWindow();
    if (window != null) {
        window.setBackgroundDrawable(new GradientDrawable());
        window.setLayout(WindowManager.LayoutParams.MATCH_PARENT, WindowManager.LayoutParams.WRAP_CONTENT);
        WindowManager.LayoutParams params = window.getAttributes();
        params.width = (int)(getActivity().getResources().getDisplayMetrics().widthPixels * 0.85);
        params.gravity = Gravity.CENTER;
        window.setAttributes(params);
    }
    
    final String targetQq = qq;
    final Dialog finalDialog = dialog;
    
    cancelBtn.setOnClickListener(new View.OnClickListener() {
        public void onClick(View v) {
            finalDialog.dismiss();
            显示主弹窗();
        }
    });
    
    confirmBtn.setOnClickListener(new View.OnClickListener() {
        public void onClick(View v) {
            try {
                int newConsecutive = Integer.parseInt(consecutiveInput.getText().toString());
                int newTotal = Integer.parseInt(totalInput.getText().toString());
                int newPoints = Integer.parseInt(pointsInput.getText().toString());
                int newMakeupCards = Integer.parseInt(makeupInput.getText().toString());
                
                if (newConsecutive < 0) newConsecutive = 0;
                if (newTotal < 0) newTotal = 0;
                if (newPoints < 0) newPoints = 0;
                if (newMakeupCards < 0) newMakeupCards = 0;
                
                finalDialog.dismiss();
                
                Map<String, Object> fullData = loadUserSignData(currentGroupId, targetQq);
                
                saveUserSignData(currentGroupId, targetQq,
                    (String) fullData.get("lastDate"),
                    newConsecutive,
                    newTotal,
                    newPoints,
                    (String) fullData.get("lastLikeDate"),
                    (Integer) fullData.get("likeAttempts"),
                    newMakeupCards,
                    (String) fullData.get("lastMakeupDate"),
                    (Integer) fullData.get("titleCard"),
                    (Integer) fullData.get("FiveCard"),
                    (Integer) fullData.get("luckyCard"),
                    (Integer) fullData.get("tenfoldCard"),
                    (Integer) fullData.get("doubleCard"),
                    (Integer) fullData.get("FiveCardActive"),
                    (Integer) fullData.get("tenfoldCardActive"),
                    (Integer) fullData.get("doubleCardActive"),
                    (Integer) fullData.get("monthlyBuyCount"),
                    (String) fullData.get("lastBuyMonth"),
                    (Integer) fullData.get("normalBox"),
                    (Integer) fullData.get("mediumBox"),
                    (Integer) fullData.get("advancedBox"));
                
                cachedUserList = null;
                cachedGroupId = null;
                
                toast("数据已更新");
                
                new android.os.Handler().postDelayed(new Runnable() {
                    public void run() {
                        显示主弹窗();
                    }
                }, 100);
                
            } catch (Exception e) {
                toast("输入格式错误，请检查");
            }
        }
    });
    
    dialog.show();
}

private LinearLayout createEditRow(Activity a, String label, String value) {
    LinearLayout row = new LinearLayout(a);
    row.setOrientation(LinearLayout.HORIZONTAL);
    row.setGravity(Gravity.CENTER_VERTICAL);
    row.setPadding(0, dp(8), 0, dp(8));
    
    TextView labelView = new TextView(a);
    labelView.setText(label + "：");
    labelView.setTextSize(14);
    labelView.setTextColor(isNightMode ? Color.parseColor("#CCCCCC") : Color.parseColor("#555555"));
    labelView.setLayoutParams(new LinearLayout.LayoutParams(
        0, LinearLayout.LayoutParams.WRAP_CONTENT, 1));
    row.addView(labelView);
    
    android.widget.EditText input = new android.widget.EditText(a);
    input.setText(value);
    input.setTextSize(14);
    input.setTextColor(isNightMode ? Color.WHITE : Color.BLACK);
    input.setBackgroundColor(isNightMode ? Color.parseColor("#3D3D3D") : Color.parseColor("#F0F0F0"));
    input.setPadding(dp(12), dp(8), dp(12), dp(8));
    input.setInputType(android.text.InputType.TYPE_CLASS_NUMBER);
    
    GradientDrawable inputBg = new GradientDrawable();
    inputBg.setCornerRadius(dp(8));
    inputBg.setColor(isNightMode ? Color.parseColor("#3D3D3D") : Color.parseColor("#F0F0F0"));
    input.setBackground(inputBg);
    
    LinearLayout.LayoutParams inputParams = new LinearLayout.LayoutParams(
        dp(100), LinearLayout.LayoutParams.WRAP_CONTENT);
    input.setLayoutParams(inputParams);
    
    row.addView(input);
    
    return row;
}

private Button createSmallButton(Activity a, String text, int bgColor) {
    Button btn = new Button(a);
    btn.setText(text);
    btn.setTextColor(Color.WHITE);
    btn.setTextSize(12);
    btn.setAllCaps(false);
    btn.setPadding(dp(12), dp(6), dp(12), dp(6));
    
    GradientDrawable bg = new GradientDrawable();
    bg.setCornerRadius(dp(16));
    bg.setColor(bgColor);
    btn.setBackground(bg);
    
    btn.setOnTouchListener(new View.OnTouchListener() {
        public boolean onTouch(View v, MotionEvent event) {
            switch (event.getAction()) {
                case MotionEvent.ACTION_DOWN:
                    v.setAlpha(0.7f);
                    break;
                case MotionEvent.ACTION_UP:
                case MotionEvent.ACTION_CANCEL:
                    v.setAlpha(1.0f);
                    break;
            }
            return false;
        }
    });
    
    return btn;
}

private LinearLayout createSwitchCard(Activity a, String title, boolean initialState, final OnStateChangeListener listener) {
    LinearLayout card = new LinearLayout(a);
    card.setOrientation(LinearLayout.HORIZONTAL);
    card.setPadding(dp(20), dp(14), dp(20), dp(14));
    card.setGravity(Gravity.CENTER_VERTICAL);

    GradientDrawable cardBg = new GradientDrawable();
    cardBg.setColor(isNightMode ? Color.parseColor("#2D2D2D") : Color.parseColor("#FFFFFF"));
    cardBg.setCornerRadius(dp(40));
    cardBg.setStroke(dp(1), isNightMode ? Color.parseColor("#FFB74D") : Color.parseColor("#FFB6C1"));
    card.setBackground(cardBg);

    LinearLayout.LayoutParams cardParams = new LinearLayout.LayoutParams(
            LinearLayout.LayoutParams.MATCH_PARENT,
            LinearLayout.LayoutParams.WRAP_CONTENT);
    cardParams.setMargins(0, 0, 0, dp(12));
    card.setLayoutParams(cardParams);

    TextView titleView = new TextView(a);
    titleView.setText(title);
    titleView.setTextSize(15);
    titleView.setTypeface(Typeface.DEFAULT_BOLD);
    titleView.setTextColor(isNightMode ? Color.parseColor("#FFB74D") : Color.parseColor("#D47F6B"));
    titleView.setPadding(0, 0, dp(12), 0);
    card.addView(titleView);

    LinearLayout rightLayout = new LinearLayout(a);
    rightLayout.setLayoutParams(new LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1));
    rightLayout.setGravity(Gravity.END | Gravity.CENTER_VERTICAL);
    rightLayout.setOrientation(LinearLayout.HORIZONTAL);

    final TextView statusText = new TextView(a);
    statusText.setText(initialState ? "●已开启" : "○已关闭");
    statusText.setTextSize(16);
    statusText.setTypeface(Typeface.DEFAULT_BOLD);
    statusText.setTextColor(initialState ? Color.parseColor("#4CAF50") : Color.parseColor("#F44336"));
    statusText.setPadding(0, 0, dp(8), 0);
    statusText.setClickable(true);

    final Switch switchBtn = new Switch(a);
    switchBtn.setTextOn("");
    switchBtn.setTextOff("");
    if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.JELLY_BEAN) {
        switchBtn.setShowText(false);
    }
    switchBtn.setMinWidth(0);
    switchBtn.setMinimumWidth(0);
    switchBtn.setPadding(0, 0, 0, 0);
    switchBtn.setChecked(initialState);

    switchBtn.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
        public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
            statusText.setText(isChecked ? "●已开启" : "○已关闭");
            statusText.setTextColor(isChecked ? Color.parseColor("#4CAF50") : Color.parseColor("#F44336"));
            
            if (listener != null) {
                listener.onStateChanged(isChecked);
            }
            
            if (title.equals("签到开关")) {
                toast(isChecked ? "已开启本群签到功能" : "已关闭本群签到功能");
            } else if (title.equals("代签开关")) {
                toast(isChecked ? "已开启本群代签功能" : "已关闭本群代签功能");
            }
        }
    });

    statusText.setOnClickListener(new View.OnClickListener() {
        public void onClick(View v) {
            switchBtn.setChecked(!switchBtn.isChecked());
        }
    });

    rightLayout.addView(statusText);
    rightLayout.addView(switchBtn);
    card.addView(rightLayout);

    return card;
}

private Button createGradientButton(Activity a, String text, int startColor, int endColor) {
    Button btn = new Button(a);
    btn.setText(text);
    btn.setTextColor(Color.WHITE);
    btn.setTextSize(15);
    btn.setTypeface(Typeface.DEFAULT_BOLD);
    btn.setAllCaps(false);
    btn.setPadding(dp(20), dp(12), dp(20), dp(12));
    
    android.graphics.drawable.GradientDrawable gradient = new android.graphics.drawable.GradientDrawable(
        android.graphics.drawable.GradientDrawable.Orientation.LEFT_RIGHT,
        new int[]{startColor, endColor}
    );
    gradient.setCornerRadius(dp(30));
    btn.setBackground(gradient);
    
    btn.setElevation(dp(2));
    
    btn.setOnTouchListener(new View.OnTouchListener() {
        public boolean onTouch(View v, MotionEvent event) {
            switch (event.getAction()) {
                case MotionEvent.ACTION_DOWN:
                    v.animate().scaleX(0.96f).scaleY(0.96f).setDuration(100).start();
                    v.setAlpha(0.9f);
                    break;
                case MotionEvent.ACTION_UP:
                case MotionEvent.ACTION_CANCEL:
                    v.animate().scaleX(1.0f).scaleY(1.0f).setDuration(100).start();
                    v.setAlpha(1.0f);
                    break;
            }
            return false;
        }
    });
    
    return btn;
}

private void 显示确认删除弹窗(String qq, String nickname) {
    Activity a = getActivity();
    if (a == null) return;
    
    final Dialog dialog = new Dialog(a);
    dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
    dialog.setCanceledOnTouchOutside(false);
    
    LinearLayout mainLayout = new LinearLayout(a);
    mainLayout.setOrientation(LinearLayout.VERTICAL);
    mainLayout.setPadding(dp(24), dp(20), dp(24), dp(20));
    
    GradientDrawable bgDrawable = new GradientDrawable();
    bgDrawable.setColor(isNightMode ? Color.parseColor("#1E1E1E") : Color.parseColor("#FFF8F0"));
    bgDrawable.setCornerRadius(dp(28));
    mainLayout.setBackground(bgDrawable);
    
    TextView titleView = new TextView(a);
    titleView.setText("确认删除");
    titleView.setTextSize(18);
    titleView.setTypeface(Typeface.DEFAULT_BOLD);
    titleView.setTextColor(Color.parseColor("#EF5350"));
    titleView.setGravity(Gravity.CENTER);
    titleView.setPadding(0, 0, 0, dp(16));
    mainLayout.addView(titleView);
    
    TextView msgView = new TextView(a);
    msgView.setText(String.format("确定要删除【%s】\n的签到数据吗？\n此操作不可恢复", nickname));
    msgView.setTextSize(14);
    msgView.setTextColor(isNightMode ? Color.parseColor("#AAAAAA") : Color.parseColor("#666666"));
    msgView.setGravity(Gravity.CENTER);
    msgView.setPadding(0, 0, 0, dp(24));
    mainLayout.addView(msgView);
    
    LinearLayout btnContainer = new LinearLayout(a);
    btnContainer.setOrientation(LinearLayout.HORIZONTAL);
    btnContainer.setGravity(Gravity.CENTER);
    
    Button cancelBtn = createSecondaryButton(a, "取消", 
        isNightMode ? Color.parseColor("#AAAAAA") : Color.parseColor("#999999"),
        isNightMode ? Color.parseColor("#3D3D3D") : Color.parseColor("#F5F5F5"));
    LinearLayout.LayoutParams cancelParams = new LinearLayout.LayoutParams(
        0, LinearLayout.LayoutParams.WRAP_CONTENT, 1
    );
    cancelParams.setMargins(0, 0, dp(8), 0);
    cancelBtn.setLayoutParams(cancelParams);
    
    Button confirmBtn = createMainButton(a, "删除", Color.parseColor("#EF5350"));
    LinearLayout.LayoutParams confirmParams = new LinearLayout.LayoutParams(
        0, LinearLayout.LayoutParams.WRAP_CONTENT, 1
    );
    confirmParams.setMargins(dp(8), 0, 0, 0);
    confirmBtn.setLayoutParams(confirmParams);
    
    btnContainer.addView(cancelBtn);
    btnContainer.addView(confirmBtn);
    mainLayout.addView(btnContainer);
    
    dialog.setContentView(mainLayout);
    
    Window window = dialog.getWindow();
    if (window != null) {
        window.setBackgroundDrawable(new GradientDrawable());
        window.setLayout(WindowManager.LayoutParams.MATCH_PARENT, WindowManager.LayoutParams.WRAP_CONTENT);
        WindowManager.LayoutParams params = window.getAttributes();
        params.width = (int)(getActivity().getResources().getDisplayMetrics().widthPixels * 0.8);
        params.gravity = Gravity.CENTER;
        window.setAttributes(params);
    }
    
    final String targetQq = qq;
    final String targetNickname = nickname;
    final Dialog finalDialog = dialog;
    
    cancelBtn.setOnClickListener(new View.OnClickListener() {
    public void onClick(View v) {
        finalDialog.dismiss();
        显示主弹窗();
    }
});
    
    confirmBtn.setOnClickListener(new View.OnClickListener() {
        public void onClick(View v) {
            finalDialog.dismiss();
            toast("正在删除 " + targetNickname + " 的数据...");
            删除单个用户签到数据(targetQq);
            cachedUserList = null;
            cachedGroupId = null;
            new android.os.Handler().postDelayed(new Runnable() {
                public void run() {
                    显示主弹窗();
                }
            }, 100);
        }
    });
    
    dialog.show();
}

private void 显示删除全部确认弹窗() {
    Activity a = getActivity();
    if (a == null) return;
    
    Dialog dialog = new Dialog(a);
    dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
    dialog.setCanceledOnTouchOutside(false);
    
    LinearLayout mainLayout = new LinearLayout(a);
    mainLayout.setOrientation(LinearLayout.VERTICAL);
    mainLayout.setPadding(dp(24), dp(20), dp(24), dp(20));
    
    GradientDrawable bgDrawable = new GradientDrawable();
    bgDrawable.setColor(isNightMode ? Color.parseColor("#1E1E1E") : Color.parseColor("#FFF8F0"));
    bgDrawable.setCornerRadius(dp(28));
    mainLayout.setBackground(bgDrawable);
    
    TextView titleView = new TextView(a);
    titleView.setText("确认清空");
    titleView.setTextSize(18);
    titleView.setTypeface(Typeface.DEFAULT_BOLD);
    titleView.setTextColor(Color.parseColor("#EF5350"));
    titleView.setGravity(Gravity.CENTER);
    titleView.setPadding(0, 0, 0, dp(16));
    mainLayout.addView(titleView);
    
    TextView msgView = new TextView(a);
    msgView.setText("确定要清空本群所有签到数据吗？\n此操作不可恢复");
    msgView.setTextSize(14);
    msgView.setTextColor(isNightMode ? Color.parseColor("#AAAAAA") : Color.parseColor("#666666"));
    msgView.setGravity(Gravity.CENTER);
    msgView.setPadding(0, 0, 0, dp(24));
    mainLayout.addView(msgView);
    
    LinearLayout btnContainer = new LinearLayout(a);
    btnContainer.setOrientation(LinearLayout.HORIZONTAL);
    btnContainer.setGravity(Gravity.CENTER);
    
    Button cancelBtn = createSecondaryButton(a, "取消", 
        isNightMode ? Color.parseColor("#AAAAAA") : Color.parseColor("#999999"),
        isNightMode ? Color.parseColor("#3D3D3D") : Color.parseColor("#F5F5F5"));
    LinearLayout.LayoutParams cancelParams = new LinearLayout.LayoutParams(
        0, LinearLayout.LayoutParams.WRAP_CONTENT, 1
    );
    cancelParams.setMargins(0, 0, dp(8), 0);
    cancelBtn.setLayoutParams(cancelParams);
    
    Button confirmBtn = createMainButton(a, "清空", Color.parseColor("#EF5350"));
    LinearLayout.LayoutParams confirmParams = new LinearLayout.LayoutParams(
        0, LinearLayout.LayoutParams.WRAP_CONTENT, 1
    );
    confirmParams.setMargins(dp(8), 0, 0, 0);
    confirmBtn.setLayoutParams(confirmParams);
    
    btnContainer.addView(cancelBtn);
    btnContainer.addView(confirmBtn);
    mainLayout.addView(btnContainer);
    
    dialog.setContentView(mainLayout);
    
    Window window = dialog.getWindow();
    if (window != null) {
        window.setBackgroundDrawable(new GradientDrawable());
        window.setLayout(WindowManager.LayoutParams.MATCH_PARENT, WindowManager.LayoutParams.WRAP_CONTENT);
        WindowManager.LayoutParams params = window.getAttributes();
        params.width = (int)(getActivity().getResources().getDisplayMetrics().widthPixels * 0.8);
        params.gravity = Gravity.CENTER;
        window.setAttributes(params);
    }
    
    dialog.show();
    
    cancelBtn.setOnClickListener(new View.OnClickListener() {
        public void onClick(View v) {
            dialog.dismiss();
        }
    });
    
    confirmBtn.setOnClickListener(new View.OnClickListener() {
        public void onClick(View v) {
            dialog.dismiss();
            清空当前群聊签到数据(currentGroupId);
            cachedUserList = null;
            cachedGroupId = null;
            显示主弹窗();
            toast("已清空本群签到数据");
        }
    });
}

private Button createIconButton(Activity a, String text, int textSize) {
    Button btn = new Button(a);
    btn.setText(text);
    btn.setTextSize(textSize);
    btn.setPadding(dp(8), dp(4), dp(8), dp(4));
    btn.setBackgroundDrawable(null);
    btn.setTypeface(Typeface.DEFAULT);
    
    btn.setOnTouchListener(new View.OnTouchListener() {
        public boolean onTouch(View v, MotionEvent event) {
            switch (event.getAction()) {
                case MotionEvent.ACTION_DOWN:
                    v.animate().scaleX(0.9f).scaleY(0.9f).setDuration(100).start();
                    v.setAlpha(0.7f);
                    break;
                case MotionEvent.ACTION_UP:
                case MotionEvent.ACTION_CANCEL:
                    v.animate().scaleX(1.0f).scaleY(1.0f).setDuration(100).start();
                    v.setAlpha(1.0f);
                    break;
            }
            return false;
        }
    });
    
    return btn;
}

private Button createMainButton(Activity a, String text, int bgColor) {
    Button btn = new Button(a);
    btn.setText(text);
    btn.setTextColor(Color.WHITE);
    btn.setTextSize(16);
    btn.setTypeface(Typeface.DEFAULT_BOLD);
    btn.setAllCaps(false);
    btn.setPadding(dp(16), dp(12), dp(16), dp(12));
    
    GradientDrawable bg = new GradientDrawable();
    bg.setCornerRadius(dp(24));
    bg.setColor(bgColor);
    btn.setBackground(bg);
    
    btn.setOnTouchListener(new View.OnTouchListener() {
        public boolean onTouch(View v, MotionEvent event) {
            switch (event.getAction()) {
                case MotionEvent.ACTION_DOWN:
                    v.animate().scaleX(0.95f).scaleY(0.95f).setDuration(100).start();
                    v.setAlpha(0.9f);
                    break;
                case MotionEvent.ACTION_UP:
                case MotionEvent.ACTION_CANCEL:
                    v.animate().scaleX(1.0f).scaleY(1.0f).setDuration(100).start();
                    v.setAlpha(1.0f);
                    break;
            }
            return false;
        }
    });
    
    return btn;
}

private Button createSecondaryButton(Activity a, String text, int textColor, int bgColor) {
    Button btn = new Button(a);
    btn.setText(text);
    btn.setTextColor(textColor);
    btn.setTextSize(14);
    btn.setTypeface(Typeface.DEFAULT_BOLD);
    btn.setAllCaps(false);
    btn.setPadding(dp(16), dp(10), dp(16), dp(10));
    
    GradientDrawable bg = new GradientDrawable();
    bg.setCornerRadius(dp(20));
    bg.setColor(bgColor);
    btn.setBackground(bg);
    
    btn.setOnTouchListener(new View.OnTouchListener() {
        public boolean onTouch(View v, MotionEvent event) {
            switch (event.getAction()) {
                case MotionEvent.ACTION_DOWN:
                    v.animate().scaleX(0.95f).scaleY(0.95f).setDuration(100).start();
                    v.setAlpha(0.8f);
                    break;
                case MotionEvent.ACTION_UP:
                case MotionEvent.ACTION_CANCEL:
                    v.animate().scaleX(1.0f).scaleY(1.0f).setDuration(100).start();
                    v.setAlpha(1.0f);
                    break;
            }
            return false;
        }
    });
    
    return btn;
}

private String 获取签到数据(String groupUin, String qq, String type) {
    try {
        String raw = groupUin + "_" + qq + "_" + type;
        MessageDigest md = MessageDigest.getInstance("MD5");
        byte[] hash = md.digest(raw.getBytes());
        StringBuilder hex = new StringBuilder();
        for (byte b : hash) {
            hex.append(String.format("%02x", b));
        }
        return hex.toString();
    } catch (Exception e) {
        return String.valueOf((groupUin + qq + type).hashCode());
    }
}

private String 获取签到背景(String groupUin, String qq) {
    String fileName = 获取签到数据(groupUin, qq, "origin");
    return appPath + "/sign_photos/" + fileName + ".enc";
}

private String 获取签到图片(String groupUin, String qq) {
    String fileName = 获取签到数据(groupUin, qq, "checkin");
    return appPath + "/sign_photos/" + fileName + ".tmp";
}

private String 下载签到底图(String qun, String qq) {
    try {
        String photoDir = appPath + "/sign_photos";
        File dir = new File(photoDir);
        if (!dir.exists()) dir.mkdirs();
        
        String originPath = 获取签到背景(qun, qq);
        
        String[] apiUrls = {
        "https://t.alcy.cc/pc",
        "https://t.alcy.cc/ycy",
        "https://www.yumus.cn/api/?target=img&brand=360&type=5",
        "https://www.loliapi.com/acg/pc/",
        "https://api.anosu.top/api/?sort=pc"
};
        
        List<String> apiList = new ArrayList<String>();
        for (String url : apiUrls) {
            apiList.add(url);
        }
        Collections.shuffle(apiList);
        
        int maxAttempts = Math.min(apiList.size(), 5);
        for (int attempt = 0; attempt < maxAttempts; attempt++) {
            String apiUrl = apiList.get(attempt);
            if (从API下载底图(apiUrl, originPath)) {
                return originPath;
            }
            
        }
        
        return null;
        
    } catch (Exception e) {
        return null;
    }
}

private boolean 从API下载底图(String apiUrl, String savePath) {
    HttpURLConnection conn = null;
    InputStream inputStream = null;
    FileOutputStream outputStream = null;
    
    try {
        URL url = new URL(apiUrl);
        conn = (HttpURLConnection) url.openConnection();
        conn.setConnectTimeout(15000);
        conn.setReadTimeout(15000);
        conn.setRequestProperty("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36");
        conn.setRequestProperty("Referer", "https://www.loliapi.com/");
        conn.setInstanceFollowRedirects(true);
        conn.connect();
        
        int responseCode = conn.getResponseCode();
        if (responseCode == HttpURLConnection.HTTP_OK) {
            inputStream = conn.getInputStream();
            outputStream = new FileOutputStream(savePath);
            byte[] buffer = new byte[8192];
            int bytesRead;
            int totalBytes = 0;
            while ((bytesRead = inputStream.read(buffer)) != -1) {
                outputStream.write(buffer, 0, bytesRead);
                totalBytes += bytesRead;
            }
            outputStream.flush();
            
            if (totalBytes < 10240) {
                new File(savePath).delete();
                return false;
            }
            
            BitmapFactory.Options options = new BitmapFactory.Options();
            options.inJustDecodeBounds = true;
            BitmapFactory.decodeFile(savePath, options);
            if (options.outWidth <= 0 || options.outHeight <= 0) {
                new File(savePath).delete();
                return false;
            }
            
            return true;
        }
    } catch (Exception e) {
        new File(savePath).delete();
    } finally {
        try {
            if (outputStream != null) outputStream.close();
            if (inputStream != null) inputStream.close();
            if (conn != null) conn.disconnect();
        } catch (Exception e) {}
    }
    return false;
}

private String 获取默认底图(String qun, String qq) {
    try {
        File defineDir = new File(appPath + "/define_photos");
        if (!defineDir.exists()) {
            defineDir.mkdirs();
            return null;
        }
        
        if (!defineDir.isDirectory()) {
            return null;
        }
        
        File[] files = defineDir.listFiles();
        if (files == null || files.length == 0) {
            return null;
        }
        
        List<File> imageFiles = new ArrayList<>();
        for (File file : files) {
            String name = file.getName().toLowerCase();
            if (name.endsWith(".jpg") || name.endsWith(".jpeg") || 
                name.endsWith(".png") || name.endsWith(".webp") || name.endsWith(".bmp")) {
                imageFiles.add(file);
            }
        }
        
        if (imageFiles.isEmpty()) {
            return null;
        }
        
        int index = (int)(Math.random() * imageFiles.size());
        File selectedFile = imageFiles.get(index);
        
        String originPath = 获取签到背景(qun, qq);
        File originFile = new File(originPath);
        File parentDir = originFile.getParentFile();
        if (!parentDir.exists()) {
            parentDir.mkdirs();
        }
        
        BitmapFactory.Options options = new BitmapFactory.Options();
        options.inPreferredConfig = Bitmap.Config.RGB_565;
        Bitmap source = BitmapFactory.decodeFile(selectedFile.getAbsolutePath(), options);
        
        if (source == null) {
            return null;
        }
        
        int rotation = 0;
        try {
            android.media.ExifInterface exif = new android.media.ExifInterface(selectedFile.getAbsolutePath());
            int orientation = exif.getAttributeInt(
                android.media.ExifInterface.TAG_ORIENTATION, 
                android.media.ExifInterface.ORIENTATION_NORMAL);
            switch (orientation) {
                case android.media.ExifInterface.ORIENTATION_ROTATE_90:
                    rotation = 90;
                    break;
                case android.media.ExifInterface.ORIENTATION_ROTATE_180:
                    rotation = 180;
                    break;
                case android.media.ExifInterface.ORIENTATION_ROTATE_270:
                    rotation = 270;
                    break;
            }
        } catch (Exception e) {}
        
        if (rotation != 0) {
            Matrix matrix = new Matrix();
            matrix.postRotate(rotation);
            Bitmap rotated = Bitmap.createBitmap(source, 0, 0, source.getWidth(), source.getHeight(), matrix, true);
            source.recycle();
            source = rotated;
        }
        
        FileOutputStream fos = new FileOutputStream(originPath);
        source.compress(Bitmap.CompressFormat.JPEG, 85, fos);
        fos.flush();
        fos.close();
        source.recycle();
        
        return originPath;
        
    } catch (Exception e) {
        return null;
    }
}

private boolean 设置默认背景(String savePath) {
    FileOutputStream fos = null;
    try {
        int width = 1920;
        int height = 1080;
        
        Bitmap bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(bitmap);
        
        android.graphics.drawable.GradientDrawable gradient = new android.graphics.drawable.GradientDrawable(
            android.graphics.drawable.GradientDrawable.Orientation.TOP_BOTTOM,
            new int[]{Color.parseColor("#FFB6C1"), Color.parseColor("#DDA0DD"), Color.parseColor("#87CEEB")}
        );
        gradient.setBounds(0, 0, width, height);
        gradient.draw(canvas);
        
        Paint textPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        textPaint.setColor(Color.WHITE);
        textPaint.setTextSize(60);
        textPaint.setTypeface(Typeface.DEFAULT_BOLD);
        textPaint.setTextAlign(Align.CENTER);
        textPaint.setAlpha(100);
        
        File saveFile = new File(savePath);
        File parent = saveFile.getParentFile();
        if (!parent.exists()) {
            parent.mkdirs();
        }
        
        fos = new FileOutputStream(savePath);
        bitmap.compress(Bitmap.CompressFormat.JPEG, 90, fos);
        fos.flush();
        fos.close();
        bitmap.recycle();
        
        return true;
        
    } catch (Exception e) {
        return false;
    } finally {
        try {
            if (fos != null) fos.close();
        } catch (Exception e) {}
    }
}

private Bitmap 获取用户头像(String qq) {
    try {
        String avatarUrl = "https://q.qlogo.cn/g?b=qq&s=100&nk=" + qq;
        URL url = new URL(avatarUrl);
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        conn.setConnectTimeout(5000);
        conn.setReadTimeout(5000);
        conn.connect();
        InputStream inputStream = conn.getInputStream();
        Bitmap bitmap = BitmapFactory.decodeStream(inputStream);
        inputStream.close();
        if (bitmap != null) {
            return 获取圆形头像(bitmap);
        }
    } catch (Exception e) {}
    Bitmap defaultBitmap = Bitmap.createBitmap(100, 100, Bitmap.Config.ARGB_8888);
    Canvas canvas = new Canvas(defaultBitmap);
    Paint paint = new Paint(Paint.ANTI_ALIAS_FLAG);
    paint.setColor(Color.GRAY);
    canvas.drawCircle(50, 50, 50, paint);
    return defaultBitmap;
}

private Bitmap 获取圆形头像(Bitmap bitmap) {
    int size = Math.min(bitmap.getWidth(), bitmap.getHeight());
    Bitmap output = Bitmap.createBitmap(size, size, Bitmap.Config.ARGB_8888);
    Canvas canvas = new Canvas(output);
    Paint paint = new Paint(Paint.ANTI_ALIAS_FLAG);
    paint.setAntiAlias(true);
    RectF rectF = new RectF(0, 0, size, size);
    canvas.drawOval(rectF, paint);
    paint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.SRC_IN));
    float scale = (float) size / Math.min(bitmap.getWidth(), bitmap.getHeight());
    Matrix matrix = new Matrix();
    matrix.setScale(scale, scale);
    Bitmap scaledBitmap = Bitmap.createBitmap(bitmap, 0, 0, bitmap.getWidth(), bitmap.getHeight(), matrix, true);
    canvas.drawBitmap(scaledBitmap, (size - scaledBitmap.getWidth()) / 2, (size - scaledBitmap.getHeight()) / 2, paint);
    return output;
}

private void 发送图片消息(String groupUin, String imagePath) {
    File imgFile = new File(imagePath);
    if (!imgFile.exists()) {
        sendMsg(groupUin, "", "图片不存在");
        return;
    }
    
    try {
        sendPic(groupUin, "", imagePath);
    } catch (Exception e) {
        sendMsg(groupUin, "", "发送图片失败: " + e.getMessage());
    }
}

private String 获取实时昵称(String groupId, String qq) {
    try {
        Object info = getMemberInfo(groupId, qq);
        if (info != null) {
            try {
                java.lang.reflect.Field cardField = info.getClass().getField("CardName");
                String cardName = (String) cardField.get(info);
                if (cardName != null && !cardName.isEmpty() && !"null".equals(cardName)) {
                    return cardName;
                }
            } catch (Exception e) {}
            
            try {
                java.lang.reflect.Field nickField = info.getClass().getField("NickName");
                String nickName = (String) nickField.get(info);
                if (nickName != null && !nickName.isEmpty() && !"null".equals(nickName)) {
                    return nickName;
                }
            } catch (Exception e) {}
            
            try {
                java.lang.reflect.Field nameField = info.getClass().getField("UserName");
                String userName = (String) nameField.get(info);
                if (userName != null && !userName.isEmpty() && !"null".equals(userName)) {
                    return userName;
                }
            } catch (Exception e) {}
        }
    } catch (Exception e) {}
    
    try {
        ArrayList memberList = getGroupMemberList(groupId);
        if (memberList != null) {
            for (Object member : memberList) {
                String memberQQ = null;
                try {
                    java.lang.reflect.Field uinField = member.getClass().getField("UserUin");
                    memberQQ = (String) uinField.get(member);
                } catch (Exception e) {
                    try {
                        java.lang.reflect.Field uinField = member.getClass().getDeclaredField("UserUin");
                        uinField.setAccessible(true);
                        memberQQ = (String) uinField.get(member);
                    } catch (Exception e2) {
                        continue;
                    }
                }
                
                if (qq.equals(memberQQ)) {
                    try {
                        java.lang.reflect.Field cardField = member.getClass().getField("CardName");
                        String cardName = (String) cardField.get(member);
                        if (cardName != null && !cardName.isEmpty() && !"null".equals(cardName)) {
                            return cardName;
                        }
                    } catch (Exception e) {}
                    
                    try {
                        java.lang.reflect.Field nickField = member.getClass().getField("NickName");
                        String nickName = (String) nickField.get(member);
                        if (nickName != null && !nickName.isEmpty() && !"null".equals(nickName)) {
                            return nickName;
                        }
                    } catch (Exception e) {}
                    break;
                }
            }
        }
    } catch (Exception e) {}
    
    String fallback = getMemberName(groupId, qq);
    if (fallback != null && !fallback.isEmpty()) {
        return fallback;
    }
    
    return qq;
}

public void onMsg(MessageData msg) {
    try {
        String text = msg.MessageContent;
        String qq = msg.UserUin;
        String qun = msg.GroupUin;
        
        if (!msg.IsGroup || text == null) return;
        
        String switchState = 读取签到开关配置(qun);
        if (!switchState.equals("开")) return;
        
        String lowerText = text.toLowerCase();
        if (lowerText.equals("签到") || lowerText.equals("qd") || lowerText.equals("打卡") || 
            lowerText.equals("dk") || lowerText.equals("续火")) {
            签到方法(qun, qq);
            } else if (text.equals("我的补签卡")) {
    我的补签卡(qun, qq);
        } else if (text.equals("补签")) {
    没有补签卡购买(qun, qq, text);
        } else if (likeSessionMap.containsKey("buy_makeup_" + qun + "_" + qq) && (text.equals("是") || text.equals("否") || text.equals("y") || text.equals("n"))) {
    String sessionKey = "buy_makeup_" + qun + "_" + qq;
    String originalText = likeSessionMap.get(sessionKey);
    if (text.equals("是") || text.equals("y")) {
        likeSessionMap.remove(sessionKey);
        取消购买补签卡(sessionKey);
        
        Map<String, Object> userData = loadUserSignData(qun, qq);
        int points = (Integer) userData.get("points");
        
        if (points < 50) {
            sendMsg(qun, "", "积分不足50，无法购买补签卡~\n当前积分：" + points);
            return;
        }
        
        int newPoints = points - 50;
        int newMakeupCards = (Integer) userData.get("makeupCards") + 1;
        
        saveUserSignData(qun, qq, 
            (String) userData.get("lastDate"),
            (Integer) userData.get("consecutive"),
            (Integer) userData.get("total"),
            newPoints,
            (String) userData.get("lastLikeDate"),
            (Integer) userData.get("likeAttempts"),
            newMakeupCards,
            (String) userData.get("lastMakeupDate"),
            (Integer) userData.get("titleCard"),
            (Integer) userData.get("FiveCard"),
            (Integer) userData.get("luckyCard"),
            (Integer) userData.get("tenfoldCard"),
            (Integer) userData.get("doubleCard"),
            (Integer) userData.get("FiveCardActive"),
            (Integer) userData.get("tenfoldCardActive"),
            (Integer) userData.get("doubleCardActive"),
            (Integer) userData.get("monthlyBuyCount"),
            (String) userData.get("lastBuyMonth"),
            (Integer) userData.get("normalBox"),
            (Integer) userData.get("mediumBox"),
            (Integer) userData.get("advancedBox"));
        
        sendMsg(qun, "", "购买成功！消耗50积分\n正在为您补签...");
        
        没有补签卡购买(qun, qq, originalText);
    } else {
        likeSessionMap.remove(sessionKey);
        取消购买补签卡(sessionKey);
        sendMsg(qun, "", "已取消补签操作");
        }
    } else if (text.equals("购买补签卡")) {
        购买补签卡(qun, qq);
    } else if (text.equals("获得签到背景") || text.equals("获取签到背景") || text.startsWith("获得签到背景@") || text.startsWith("获取签到背景@") || text.startsWith("获得签到背景 @") || text.startsWith("获取签到背景 @")) {
        获取签到背景方法(qun, qq, text, msg);
    } else if (text.equals("赞我") || (likeSessionMap.containsKey(qun + "_" + qq) && (text.equals("1") || text.equals("2")))) {
        if (!qq.equals(myUin)) {
        赞我指令判断(qun, qq, text);
    } else if (text.equals("取消赞我")) {
        赞我指令判断(qun, qq, "取消");
        }
    } else if (text.equals("图签商城") || text.equals("图签商店") || text.equals("积分商城") || text.equals("积分商店")) {
        积分商城(qun, qq);
    } else if (text.equals("我的道具")) {
        我的道具(qun, qq);
    } else if (text.equals("我的成就") || text.equals("查看成就")) {
        查看我的成就(qun, qq);
    } else if (text.startsWith("兑换头衔") || text.startsWith("设置头衔")) {
        兑换头衔(qun, qq, text);
    } else if (text.equals("查看签到排名") || text.equals("签到排名") || text.equals("签到排行") || text.equals("查看签到排行")) {
        签到排名(qun, qq);
    } else if (text.equals("购买双倍卡")) {
        购买双倍卡方法(qun, qq);
    } else if (text.equals("购买十倍卡")) {
        购买十倍卡方法(qun, qq);
    } else if (text.equals("使用双倍卡")) {
        使用双倍卡方法(qun, qq);
    } else if (text.equals("使用十倍卡")) {
        使用十倍卡方法(qun, qq);
    } else if (text.equals("购买五倍卡")) {
        购买五倍卡方法(qun, qq);
    } else if (text.equals("购买幸运卡")) {
        购买幸运卡方法(qun, qq);
    } else if (text.equals("购买头衔卡")) {
        购买头衔卡方法(qun, qq);
    } else if (text.equals("使用五倍卡")) {
        使用五倍卡方法(qun, qq);
    } else if (text.equals("使用幸运卡")) {
        使用幸运卡方法(qun, qq);
    } else if (text.equals("查看我的积分") || text.equals("我的积分") || text.equals("积分查询")) {
        手动查询积分(qun, qq);
    } else if (text.equals("普通奖池") || text.equals("盲盒奖池1")) {
        盲盒奖池(qun, qq, 1);
    } else if (text.equals("中级奖池") || text.equals("盲盒奖池2")) {
        盲盒奖池(qun, qq, 2);
    } else if (text.equals("高级奖池") || text.equals("盲盒奖池3")) {
        盲盒奖池(qun, qq, 3);
    } else if (text.equals("抽盲盒") || text.equals("盲盒抽取")) {
        抽取盲盒方法(qun, qq);
    } else if (text.equals("开盲盒")) {
        开启盲盒方法(qun, qq);
    } else if (likeSessionMap.containsKey("draw_" + qun + "_" + qq) && (text.equals("1") || text.equals("2") || text.equals("3"))) {
    String sessionKey = "draw_" + qun + "_" + qq;
    if (likeSessionMap.containsKey(sessionKey)) {
        likeSessionMap.remove(sessionKey);
        取消点赞计时(sessionKey);
        绘画盲盒选择(qun, qq, Integer.parseInt(text));
        }
    } else if (likeSessionMap.containsKey("open_" + qun + "_" + qq) && (text.equals("1") || text.equals("2") || text.equals("3"))) {
        String sessionKey = "open_" + qun + "_" + qq;
    if (likeSessionMap.containsKey(sessionKey)) {
        likeSessionMap.remove(sessionKey);
        取消点赞计时(sessionKey);
        选择开启盲盒(qun, qq, Integer.parseInt(text));
        }
    } else if (text.equals("图签菜单") || text.equals("photocheckinmenu")) {
        图签菜单(qun, qq);
    } else if (text.startsWith("发红包") && text.contains("@")) {
        发送专属红包(qun, qq, text, msg);
        return;
    } else if (text.startsWith("包红包") || text.startsWith("发红包")) {
        发送拼手气红包(qun, qq, text);
    } else if (text.startsWith("抢红包")) {
        String redPacketId = text.replace("抢红包", "").trim();
        redPacketId = redPacketId.replace("红包ID：", "").replace("红包ID:", "").replace("：", ":").trim();
        
        String key = qun + "_" + redPacketId;
    int count = grabCommandCountMap.getOrDefault(key, 0);
        grabCommandCountMap.put(key, count + 1);
        
    if (qq.equals(myUin)) {
    if (count >= 1) {
        抢红包(qun, qq, text);
        }
    } else {
        抢红包(qun, qq, text);
    }
        清理抢红包指令();
    } else if (text.equals("我的红包") || text.equals("红包记录")) {
        手动查看我的红包(qun, qq);
        }
    } catch (Exception e) {}
        代签指令响应(msg);
}

private void 图签菜单(String groupId, String qq) {
    new Thread(new Runnable() {
        public void run() {
            String content = "【签到相关】\n" +
                "  签到 / qd / 打卡 / dk / 续火\n" +
                "  代签 @某人\n" +
                "  补签 / 补签 20250106\n" +
                "\n【积分相关】\n" +
                "  查看我的积分 / 我的积分\n" +
                "  赞我\n" +
                "  取消赞我\n" +
                "\n【商城相关】\n" +
                "  图签商城 / 图签商店\n" +
                "  我的道具\n" +
                "  购买补签卡 / 购买头衔卡\n" +
                "  购买双倍卡 / 购买五倍卡 / 购买十倍卡 / 购买幸运卡\n" +
                "  兑换头衔/设置头衔 头衔名\n" +
                "  使用双倍卡 / 使用五倍卡 / 使用十倍卡\n" +
                "\n【盲盒相关】\n" +
                "  抽盲盒 / 盲盒抽取\n" +
                "  开盲盒\n" +
                "  普通奖池 / 中级奖池 / 高级奖池\n" +
                "\n【积分红包】\n" + 
                "  发送「包红包 个数 金额」或「发红包 个数 金额」可发送拼手气红包\n" + 
                "  发送「发红包@XXX 金额」或「发红包 金额 @XXX」可发送专属红包\n" + 
                "  发送「抢红包 红包ID：XXXX_X」或「抢红包 XXXX_X」\n" + 
                "  发送「我的红包」可查看历史发出红包和抢到的红包\n" + 
                "\n【其他】\n" +
                "  我的补签卡\n" +
                "  获得签到背景\n" +
                "  查看签到排名 / 签到排行";
            
            String imagePath = 绘制菜单图片("图签菜单", content, groupId, qq);
            if (imagePath != null) {
                发送图片消息(groupId, imagePath);
                延迟删除签到图片(imagePath, 15000);
            } else {
                sendMsg(groupId, "", "━━━━━━━━━━━━━━\n图签菜单\n━━━━━━━━━━━━━━\n" + content + "\n━━━━━━━━━━━━━━");
            }
        }
    }).start();
}

private void 盲盒奖池(String groupId, String qq, int level) {
    new Thread(new Runnable() {
        public void run() {
            String title = "";
            String content = "";
            
            if (level == 1) {
                title = "初级盲盒奖池";
                content = "【积分奖励】\n" +
                    "1-10积分     30%\n" +
                    "11-20积分    20%\n" +
                    "21-30积分    10%\n" +
                    "31-40积分    5%\n" +
                    "\n【道具奖励】\n" +
                    "补签卡 x1    7%\n" +
                    "初级盲盒 x1  6%\n" +
                    "\n【特殊奖励】\n" +
                    "再开一次     10%\n" +
                    "\n【空盒子】\n" +
                    "什么也没有   12%\n" +
                    "\n购买价格：30积分";
            } else if (level == 2) {
                title = "中级盲盒奖池";
                content = "【积分奖励】\n" +
                    "20-50积分    20%\n" +
                    "51-80积分    18%\n" +
                    "81-100积分   12%\n" +
                    "101-150积分  8%\n" +
                    "\n【道具奖励】\n" +
                    "补签卡 x2    7%\n" +
                    "头衔卡 x1    5%\n" +
                    "五倍卡 x1    4%\n" +
                    "初级盲盒 x2  4%\n" +
                    "中级盲盒 x1  4%\n" +
                    "\n【特殊奖励】\n" +
                    "再开一次     8%\n" +
                    "\n【空盒子】\n" +
                    "什么也没有   10%\n" +
                    "\n购买价格：100积分";
            } else {
                title = "高级盲盒奖池";
                content = "【积分奖励】\n" +
                    "50-100积分   15%\n" +
                    "101-150积分  13%\n" +
                    "151-200积分  10%\n" +
                    "201-300积分  8%\n" +
                    "500积分      4%\n" +
                    "\n【道具奖励】\n" +
                    "补签卡 x3    7%\n" +
                    "补签卡 x5    5%\n" +
                    "头衔卡 x1    5%\n" +
                    "五倍卡 x2    4%\n" +
                    "幸运卡 x1    3%\n" +
                    "初级盲盒 x3  3%\n" +
                    "中级盲盒 x2  3%\n" +
                    "高级盲盒 x1  3%\n" +
                    "\n【特殊奖励】\n" +
                    "再开一次     9%\n" +
                    "\n【空盒子】\n" +
                    "什么也没有   8%\n" +
                    "\n购买价格：200积分";
            }
            
            String imagePath = 绘制菜单图片(title, content, groupId, qq);
            if (imagePath != null) {
                发送图片消息(groupId, imagePath);
                延迟删除签到图片(imagePath, 15000);
            } else {
                if (level == 1) {
                    sendMsg(groupId, "", "━━━━━━━━━━━━━━\n初级盲盒奖池\n━━━━━━━━━━━━━━\n...");
                }
            }
        }
    }).start();
}

private void 我的补签卡(String groupId, String qq) {
    Map<String, Object> userData = loadUserSignData(groupId, qq);
    int makeupCards = (Integer) userData.get("makeupCards");
    sendMsg(groupId, "", "━━━━━━━━━━━━━━\n" +
                         "                 补签卡\n" +
                         "━━━━━━━━━━━━━━\n" +
                         "当前持有：" + makeupCards + "张\n" +
                         "购买价格：50积分/张\n" +
                         "每月限购：5张\n" +
                         "━━━━━━━━━━━━━━");
}

private void 购买补签卡(String groupId, String qq) {
    Map<String, Object> userData = loadUserSignData(groupId, qq);
    int points = (Integer) userData.get("points");
    int makeupCards = (Integer) userData.get("makeupCards");
    int monthlyBuyCount = (Integer) userData.get("monthlyBuyCount");
    String lastBuyMonth = (String) userData.get("lastBuyMonth");
    
    SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM");
    String currentMonth = sdf.format(new Date());
    
    if (!currentMonth.equals(lastBuyMonth)) {
        monthlyBuyCount = 0;
    }
    
    if (monthlyBuyCount >= 5) {
        sendMsg(groupId, "", "本月已购买5张补签卡，请下个月再来~");
        return;
    }
    
    if (points < 50) {
        sendMsg(groupId, "", "积分不足50，无法购买补签卡~\n当前积分：" + points);
        return;
    }
    
    int newPoints = points - 50;
    int newMakeupCards = makeupCards + 1;
    int newMonthlyBuyCount = monthlyBuyCount + 1;
    
    String lastDate = (String) userData.get("lastDate");
    int consecutive = (Integer) userData.get("consecutive");
    int total = (Integer) userData.get("total");
    String lastLikeDate = (String) userData.get("lastLikeDate");
    int likeAttempts = (Integer) userData.get("likeAttempts");
    String lastMakeupDate = (String) userData.get("lastMakeupDate");
    int titleCard = (Integer) userData.get("titleCard");
    int FiveCard = (Integer) userData.get("FiveCard");
    int luckyCard = (Integer) userData.get("luckyCard");
    int tenfoldCard = (Integer) userData.get("tenfoldCard");
    int doubleCard = (Integer) userData.get("doubleCard");
    int FiveCardActive = (Integer) userData.get("FiveCardActive");
    int tenfoldCardActive = (Integer) userData.get("tenfoldCardActive");
    int doubleCardActive = (Integer) userData.get("doubleCardActive");
    
    saveUserSignData(groupId, qq, lastDate, consecutive, total, newPoints,
                 lastLikeDate, likeAttempts, newMakeupCards, lastMakeupDate,
                 titleCard, FiveCard, luckyCard, tenfoldCard, doubleCard,
                 FiveCardActive, tenfoldCardActive, doubleCardActive,
                 newMonthlyBuyCount, currentMonth,
                 0, 0, 0);
    
    sendMsg(groupId, "", "购买成功！\n获得：补签卡 x1\n当前补签卡：" + newMakeupCards + "张\n本月已购买：" + newMonthlyBuyCount + "/5张");
}

private void 购买补签卡时效(final String sessionKey, final String groupId, final String qq) {
    取消购买补签卡(sessionKey);
    
    final java.util.Timer timer = new java.util.Timer();
    TimerTask task = new TimerTask() {
        public void run() {
            try {
                if (likeSessionMap.containsKey(sessionKey)) {
                    likeSessionMap.remove(sessionKey);
                    sendMsg(groupId, "", "购买补签卡操作已超时取消");
                }
            } catch (Exception e) {}
            buyMakeupTimerTasks.remove(sessionKey);
            timer.cancel();
        }
    };
    buyMakeupTimerTasks.put(sessionKey, task);
    timer.schedule(task, 30000);
}

private void 取消购买补签卡(String sessionKey) {
    TimerTask task = buyMakeupTimerTasks.remove(sessionKey);
    if (task != null) {
        task.cancel();
    }
}

private void 没有补签卡购买(String groupId, String qq, String text) {
    SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
    String today = sdf.format(new Date());
    
    Map<String, Object> userData = loadUserSignData(groupId, qq);
    int makeupCards = (Integer) userData.get("makeupCards");
    String lastMakeupDate = (String) userData.get("lastMakeupDate");
    
    if (makeupCards <= 0) {
        String sessionKey = "buy_makeup_" + groupId + "_" + qq;
        likeSessionMap.put(sessionKey, text);
        
        sendMsg(groupId, "", "━━━━━━━━━━━━━━\n" +
                             "没有补签卡了\n" +
                             "━━━━━━━━━━━━━━\n" +
                             "是否花费50积分购买一张补签卡？\n" +
                             "━━━━━━━━━━━━━━\n" +
                             "回复「是」购买并补签\n" +
                             "回复「否」取消操作\n" +
                             "(30秒内未回复将自动取消)");
        
        购买补签卡时效(sessionKey, groupId, qq);
        return;
    }
    
    if (today.equals(lastMakeupDate)) {
        sendMsg(groupId, "", "今天已经使用过补签卡了，请明天再来~");
        return;
    }
    
    String targetDate = null;
    if (text.equals("补签")) {
    Calendar cal = Calendar.getInstance();
    cal.add(Calendar.DAY_OF_MONTH, -1);
    targetDate = sdf.format(cal.getTime());
        } else {
       java.util.regex.Pattern p1 = java.util.regex.Pattern.compile("补签\\s*(\\d{4}-\\d{2}-\\d{2})");
       java.util.regex.Matcher m1 = p1.matcher(text);
       java.util.regex.Pattern p2 = java.util.regex.Pattern.compile("补签\\s*(\\d{8})");
       java.util.regex.Matcher m2 = p2.matcher(text);
    
    if (m1.find()) {
        targetDate = m1.group(1);
    } else if (m2.find()) {
        String dateStr = m2.group(1);
        targetDate = dateStr.substring(0, 4) + "-" + dateStr.substring(4, 6) + "-" + dateStr.substring(6, 8);
    } else {
        sendMsg(groupId, "", "格式错误，请使用：\n补签\n补签 2025-01-06\n补签 20250106");
        return;
    }
    }
    
    try {
    int consecutive = (Integer) userData.get("consecutive");
    int total = (Integer) userData.get("total");
    int points = (Integer) userData.get("points");
    String lastLikeDate = (String) userData.get("lastLikeDate");
    int likeAttempts = (Integer) userData.get("likeAttempts");
    
    Date targetDateObj = sdf.parse(targetDate);
    Date todayDate = sdf.parse(today);
    long diff = (todayDate.getTime() - targetDateObj.getTime()) / (1000 * 3600 * 24);
    
    if (diff <= 0) {
        sendMsg(groupId, "", "只能补签过去的日期~");
        return;
    }
    if (diff > 7) {
        sendMsg(groupId, "", "只能补签最近7天内的日期~");
        return;
    }
    
    String lastDate = (String) userData.get("lastDate");
    if (targetDate.equals(lastDate)) {
        sendMsg(groupId, "", "该日期已经签过到了，不需要补签~");
        return;
    }
    
    int newMakeupCards = makeupCards - 1;
    int newTotal = total + 1;
    int newConsecutive = consecutive;
    
    String yesterday = 获取昨天日期(sdf);
    if (targetDate.equals(yesterday)) {
        newConsecutive = consecutive + 1;
    } else {
        newConsecutive = 1;
    }
    
    saveUserSignData(groupId, qq, lastDate, newConsecutive, newTotal, points,
                 lastLikeDate, likeAttempts, newMakeupCards, today,
                 0, 0, 0, 0, 0, 0, 0, 0, 0, "", 0, 0, 0);
    
    sendMsg(groupId, "", "补签成功！\n补签日期：" + targetDate + "\n剩余补签卡：" + newMakeupCards + "张");
    return;
    } catch (Exception e) {
    sendMsg(groupId, "", "日期格式错误，请使用：YYYY-MM-DD 格式");
    return;
    }
}

private void 手动查询积分(String groupId, String qq) {
    Map<String, Object> userData = loadUserSignData(groupId, qq);
    int points = (Integer) userData.get("points");
    int consecutive = (Integer) userData.get("consecutive");
    int total = (Integer) userData.get("total");
    
    sendMsg(groupId, "", "━━━━━━━━━━━━━━\n" +
                         "                         积分查询\n" +
                         "━━━━━━━━━━━━━━\n" +
                         "当前积分：" + points + "\n" +
                         "连续签到：" + consecutive + "天\n" +
                         "累计签到：" + total + "天\n" +
                         "━━━━━━━━━━━━━━");
}

private String 获取昨天日期(SimpleDateFormat sdf) {
    Calendar cal = Calendar.getInstance();
    cal.add(Calendar.DAY_OF_MONTH, -1);
    return sdf.format(cal.getTime());
}

private void 积分商城(String groupId, String qq) {
    new Thread(new Runnable() {
        public void run() {
            String content = "1. 补签卡     50积分\n" +
                "2. 头衔卡    200积分\n" +
                "3. 双倍积分卡  10积分\n" +
                "4. 五倍积分卡  30积分\n" +
                "5. 十倍积分卡  60积分\n" +
                "6. 幸运卡     30积分\n" +
                "\n相关指令：\n" +
                "  · 购买补签卡/头衔卡/双倍卡/五倍卡/十倍卡/幸运卡\n" +
                "  · 使用双倍卡/五倍卡/十倍卡\n" +
                "  · 兑换头衔 <头衔名>\n" +
                "  · 查看签到排名 / 签到排行";
            
            String imagePath = 绘制菜单图片("积分商城", content, groupId, qq);
            if (imagePath != null) {
                发送图片消息(groupId, imagePath);
                延迟删除签到图片(imagePath, 15000);
            } else {
                sendMsg(groupId, "", "━━━━━━━━━━━━━━\n积分商城\n━━━━━━━━━━━━━━\n" + content + "\n━━━━━━━━━━━━━━");
            }
        }
    }).start();
}

private void 清理抢红包指令() {
    if (grabCommandCountMap.size() > 100) {
        List<String> keys = new ArrayList<String>(grabCommandCountMap.keySet());
        for (int i = 0; i < keys.size() - 50; i++) {
            grabCommandCountMap.remove(keys.get(i));
        }
    }
}

private void 我的道具(String groupId, String qq) {
    Map<String, Object> userData = loadUserSignData(groupId, qq);
    int makeupCards = (Integer) userData.get("makeupCards");
    int titleCard = (Integer) userData.get("titleCard");
    int doubleCard = (Integer) userData.get("doubleCard");
    int fiveCard = (Integer) userData.get("FiveCard");
    int tenfoldCard = (Integer) userData.get("tenfoldCard");
    int luckyCard = (Integer) userData.get("luckyCard");
    int normalBox = (Integer) userData.get("normalBox");
    int mediumBox = (Integer) userData.get("mediumBox");
    int advancedBox = (Integer) userData.get("advancedBox");
    
    sendMsg(groupId, "", "我的道具\n" +
                         "━━━━━━━━━━━━━━\n" +
                         "补签卡：" + makeupCards + "张\n" +
                         "头衔卡：" + titleCard + "张\n" +
                         "双倍卡：" + doubleCard + "张\n" +
                         "五倍卡：" + fiveCard + "张\n" +
                         "十倍卡：" + tenfoldCard + "张\n" +
                         "幸运卡：" + luckyCard + "张\n" +
                         "━━━━━━━━━━━━━━\n" +
                         "初级盲盒：" + normalBox + "个\n" +
                         "中级盲盒：" + mediumBox + "个\n" +
                         "高级盲盒：" + advancedBox + "个\n");
}

private void 兑换头衔(String groupId, String qq, String text) {
    String title = text.replace("兑换头衔", "").trim();
    if (title.isEmpty()) {
        sendMsg(groupId, "", "请指定头衔名称\n使用方式：兑换头衔 头衔名");
        return;
    }
    
    if (title.length() > 12) {
        sendMsg(groupId, "", "头衔名称不能超过12个字符~");
        return;
    }
    
    Map<String, Object> userData = loadUserSignData(groupId, qq);
    int points = (Integer) userData.get("points");
    int titleCard = (Integer) userData.get("titleCard");
    
    if (points < 200 && titleCard <= 0) {
        sendMsg(groupId, "", "积分不足200，且没有头衔卡\n当前积分：" + points);
        return;
    }
    
    boolean success = 设置头衔(groupId, qq, title);
    
    if (success) {
        String costMsg = "";
        int newPoints = points;
        int newTitleCard = titleCard;
        
        if (titleCard > 0) {
            newTitleCard = titleCard - 1;
            costMsg = "消耗：头衔卡 x1";
        } else {
            newPoints = points - 200;
            costMsg = "消耗：200积分";
        }
        
        String lastDate = (String) userData.get("lastDate");
        int consecutive = (Integer) userData.get("consecutive");
        int total = (Integer) userData.get("total");
        String lastLikeDate = (String) userData.get("lastLikeDate");
        int likeAttempts = (Integer) userData.get("likeAttempts");
        int makeupCards = (Integer) userData.get("makeupCards");
        String lastMakeupDate = (String) userData.get("lastMakeupDate");
        int FiveCard = (Integer) userData.get("FiveCard");
        int luckyCard = (Integer) userData.get("luckyCard");
        int tenfoldCard = (Integer) userData.get("tenfoldCard");
        int doubleCard = (Integer) userData.get("doubleCard");
        int FiveCardActive = (Integer) userData.get("FiveCardActive");
        int tenfoldCardActive = (Integer) userData.get("tenfoldCardActive");
        int doubleCardActive = (Integer) userData.get("doubleCardActive");
        
        saveUserSignData(groupId, qq, lastDate, consecutive, total, newPoints,
                     lastLikeDate, likeAttempts, makeupCards, lastMakeupDate,
                     newTitleCard, FiveCard, luckyCard, tenfoldCard, doubleCard,
                     FiveCardActive, tenfoldCardActive, doubleCardActive,
                     0, "",
                     0, 0, 0);
        
        sendMsg(groupId, "", "✓兑换成功！" + costMsg + "\n已为您设置头衔：「" + title + "」");
    } else {
        sendMsg(groupId, "", "⚠设置头衔失败\n原因：可能缺少权限、可能超出字符");
    }
}

private boolean 设置头衔(String groupId, String qq, String title) {
    try {
        if (title.length() > 6) {
            return false;
        }
        
        setTitle(groupId, qq, title);
        return true;
    } catch (Exception e) {
        return false;
    }
}

private void 购买双倍卡方法(String groupId, String qq) {
    Map<String, Object> userData = loadUserSignData(groupId, qq);
    int points = (Integer) userData.get("points");
    
    if (points < POINTS_FOR_DOUBLE_CARD) {
        sendMsg(groupId, "", "积分不足" + POINTS_FOR_DOUBLE_CARD + "，购买失败\n当前积分：" + points);
        return;
    }
    
    int newPoints = points - POINTS_FOR_DOUBLE_CARD;
    int newDoubleCard = (Integer) userData.get("doubleCard") + 1;
    
    String lastDate = (String) userData.get("lastDate");
    int consecutive = (Integer) userData.get("consecutive");
    int total = (Integer) userData.get("total");
    String lastLikeDate = (String) userData.get("lastLikeDate");
    int likeAttempts = (Integer) userData.get("likeAttempts");
    int makeupCards = (Integer) userData.get("makeupCards");
    String lastMakeupDate = (String) userData.get("lastMakeupDate");
    int titleCard = (Integer) userData.get("titleCard");
    int fiveCard = (Integer) userData.get("FiveCard");
    int luckyCard = (Integer) userData.get("luckyCard");
    int tenfoldCard = (Integer) userData.get("tenfoldCard");
    int FiveCardActive = (Integer) userData.get("FiveCardActive");
    int tenfoldCardActive = (Integer) userData.get("tenfoldCardActive");
    int doubleCardActive = (Integer) userData.get("doubleCardActive");
    int monthlyBuyCount = (Integer) userData.get("monthlyBuyCount");
    String lastBuyMonth = (String) userData.get("lastBuyMonth");
    int normalBox = (Integer) userData.get("normalBox");
    int mediumBox = (Integer) userData.get("mediumBox");
    int advancedBox = (Integer) userData.get("advancedBox");
    
    saveUserSignData(groupId, qq, lastDate, consecutive, total, newPoints,
                     lastLikeDate, likeAttempts, makeupCards, lastMakeupDate,
                     titleCard, fiveCard, luckyCard, tenfoldCard, newDoubleCard,
                     FiveCardActive, tenfoldCardActive, doubleCardActive,
                     monthlyBuyCount, lastBuyMonth,
                     normalBox, mediumBox, advancedBox);
    
    sendMsg(groupId, "", "购买成功！\n消耗：" + POINTS_FOR_DOUBLE_CARD + "积分\n获得：双倍积分卡 x1");
}

private void 购买十倍卡方法(String groupId, String qq) {
    Map<String, Object> userData = loadUserSignData(groupId, qq);
    int points = (Integer) userData.get("points");
    
    if (points < POINTS_FOR_TENFOLD_CARD) {
        sendMsg(groupId, "", "积分不足" + POINTS_FOR_TENFOLD_CARD + "，购买失败\n当前积分：" + points);
        return;
    }
    
    int newPoints = points - POINTS_FOR_TENFOLD_CARD;
    int newTenfoldCard = (Integer) userData.get("tenfoldCard") + 1;
    
    String lastDate = (String) userData.get("lastDate");
    int consecutive = (Integer) userData.get("consecutive");
    int total = (Integer) userData.get("total");
    String lastLikeDate = (String) userData.get("lastLikeDate");
    int likeAttempts = (Integer) userData.get("likeAttempts");
    int makeupCards = (Integer) userData.get("makeupCards");
    String lastMakeupDate = (String) userData.get("lastMakeupDate");
    int titleCard = (Integer) userData.get("titleCard");
    int fiveCard = (Integer) userData.get("FiveCard");
    int luckyCard = (Integer) userData.get("luckyCard");
    int doubleCard = (Integer) userData.get("doubleCard");
    int FiveCardActive = (Integer) userData.get("FiveCardActive");
    int tenfoldCardActive = (Integer) userData.get("tenfoldCardActive");
    int doubleCardActive = (Integer) userData.get("doubleCardActive");
    int monthlyBuyCount = (Integer) userData.get("monthlyBuyCount");
    String lastBuyMonth = (String) userData.get("lastBuyMonth");
    int normalBox = (Integer) userData.get("normalBox");
    int mediumBox = (Integer) userData.get("mediumBox");
    int advancedBox = (Integer) userData.get("advancedBox");
    
    saveUserSignData(groupId, qq, lastDate, consecutive, total, newPoints,
                     lastLikeDate, likeAttempts, makeupCards, lastMakeupDate,
                     titleCard, fiveCard, luckyCard, newTenfoldCard, doubleCard,
                     FiveCardActive, tenfoldCardActive, doubleCardActive,
                     monthlyBuyCount, lastBuyMonth,
                     normalBox, mediumBox, advancedBox);
    
    sendMsg(groupId, "", "购买成功！\n消耗：" + POINTS_FOR_TENFOLD_CARD + "积分\n获得：十倍积分卡 x1");
}

private void 使用双倍卡方法(String groupId, String qq) {
    Map<String, Object> userData = loadUserSignData(groupId, qq);
    int doubleCard = (Integer) userData.get("doubleCard");
    
    if (doubleCard <= 0) {
        sendMsg(groupId, "", "没有双倍积分卡，请先去商城购买~");
        return;
    }
    
    int newDoubleCard = doubleCard - 1;
    
    saveUserSignData(groupId, qq, 
        (String) userData.get("lastDate"),
        (Integer) userData.get("consecutive"),
        (Integer) userData.get("total"),
        (Integer) userData.get("points"),
        (String) userData.get("lastLikeDate"),
        (Integer) userData.get("likeAttempts"),
        (Integer) userData.get("makeupCards"),
        (String) userData.get("lastMakeupDate"),
        (Integer) userData.get("titleCard"),
        (Integer) userData.get("FiveCard"),
        (Integer) userData.get("luckyCard"),
        (Integer) userData.get("tenfoldCard"),
        newDoubleCard,
        (Integer) userData.get("FiveCardActive"),
        (Integer) userData.get("tenfoldCardActive"),
        1,
        (Integer) userData.get("monthlyBuyCount"),
        (String) userData.get("lastBuyMonth"),
        (Integer) userData.get("normalBox"),
        (Integer) userData.get("mediumBox"),
        (Integer) userData.get("advancedBox"));
    
    sendMsg(groupId, "", "使用成功！\n消耗：双倍积分卡 x1\n下次签到将获得双倍积分！");
}

private void 使用十倍卡方法(String groupId, String qq) {
    Map<String, Object> userData = loadUserSignData(groupId, qq);
    int tenfoldCard = (Integer) userData.get("tenfoldCard");
    
    if (tenfoldCard <= 0) {
        sendMsg(groupId, "", "没有十倍积分卡，请先去商城购买~");
        return;
    }
    
    int newTenfoldCard = tenfoldCard - 1;
    
    saveUserSignData(groupId, qq, 
        (String) userData.get("lastDate"),
        (Integer) userData.get("consecutive"),
        (Integer) userData.get("total"),
        (Integer) userData.get("points"),
        (String) userData.get("lastLikeDate"),
        (Integer) userData.get("likeAttempts"),
        (Integer) userData.get("makeupCards"),
        (String) userData.get("lastMakeupDate"),
        (Integer) userData.get("titleCard"),
        (Integer) userData.get("FiveCard"),
        (Integer) userData.get("luckyCard"),
        newTenfoldCard,
        (Integer) userData.get("doubleCard"),
        (Integer) userData.get("FiveCardActive"),
        1,
        (Integer) userData.get("doubleCardActive"),
        (Integer) userData.get("monthlyBuyCount"),
        (String) userData.get("lastBuyMonth"),
        (Integer) userData.get("normalBox"),
        (Integer) userData.get("mediumBox"),
        (Integer) userData.get("advancedBox"));
    
    sendMsg(groupId, "", "使用成功！\n消耗：十倍积分卡 x1\n下次签到将获得十倍积分！");
}

private void 购买五倍卡方法(String groupId, String qq) {
    Map<String, Object> userData = loadUserSignData(groupId, qq);
    int points = (Integer) userData.get("points");
    
    if (points < 30) {
        sendMsg(groupId, "", "积分不足30，购买失败\n当前积分：" + points);
        return;
    }
    
    int newPoints = points - 30;
    int newFiveCard = (Integer) userData.get("FiveCard") + 1;
    
    String lastDate = (String) userData.get("lastDate");
    int consecutive = (Integer) userData.get("consecutive");
    int total = (Integer) userData.get("total");
    String lastLikeDate = (String) userData.get("lastLikeDate");
    int likeAttempts = (Integer) userData.get("likeAttempts");
    int makeupCards = (Integer) userData.get("makeupCards");
    String lastMakeupDate = (String) userData.get("lastMakeupDate");
    int titleCard = (Integer) userData.get("titleCard");
    int luckyCard = (Integer) userData.get("luckyCard");
    int tenfoldCard = (Integer) userData.get("tenfoldCard");
    int doubleCard = (Integer) userData.get("doubleCard");
    int FiveCardActive = (Integer) userData.get("FiveCardActive");
    int tenfoldCardActive = (Integer) userData.get("tenfoldCardActive");
    int doubleCardActive = (Integer) userData.get("doubleCardActive");
    int monthlyBuyCount = (Integer) userData.get("monthlyBuyCount");
    String lastBuyMonth = (String) userData.get("lastBuyMonth");
    int normalBox = (Integer) userData.get("normalBox");
    int mediumBox = (Integer) userData.get("mediumBox");
    int advancedBox = (Integer) userData.get("advancedBox");
    
    saveUserSignData(groupId, qq, lastDate, consecutive, total, newPoints,
                     lastLikeDate, likeAttempts, makeupCards, lastMakeupDate,
                     titleCard, newFiveCard, luckyCard, tenfoldCard, doubleCard,
                     FiveCardActive, tenfoldCardActive, doubleCardActive,
                     monthlyBuyCount, lastBuyMonth,
                     normalBox, mediumBox, advancedBox);
    
    sendMsg(groupId, "", "购买成功！\n消耗：30积分\n获得：五倍积分卡 x1");
}

private void 购买幸运卡方法(String groupId, String qq) {
    Map<String, Object> userData = loadUserSignData(groupId, qq);
    int points = (Integer) userData.get("points");
    
    if (points < 30) {
        sendMsg(groupId, "", "积分不足30，无法购买幸运卡~\n当前积分：" + points);
        return;
    }
    
    int newPoints = points - 30;
    int luckyCard = (Integer) userData.get("luckyCard") + 1;
    
    String lastDate = (String) userData.get("lastDate");
    int consecutive = (Integer) userData.get("consecutive");
    int total = (Integer) userData.get("total");
    String lastLikeDate = (String) userData.get("lastLikeDate");
    int likeAttempts = (Integer) userData.get("likeAttempts");
    int makeupCards = (Integer) userData.get("makeupCards");
    String lastMakeupDate = (String) userData.get("lastMakeupDate");
    int titleCard = (Integer) userData.get("titleCard");
    int FiveCard = (Integer) userData.get("FiveCard");
    int tenfoldCard = (Integer) userData.get("tenfoldCard");
    int doubleCard = (Integer) userData.get("doubleCard");
    int FiveCardActive = (Integer) userData.get("FiveCardActive");
    int tenfoldCardActive = (Integer) userData.get("tenfoldCardActive");
    int doubleCardActive = (Integer) userData.get("doubleCardActive");
    int monthlyBuyCount = (Integer) userData.get("monthlyBuyCount");
    String lastBuyMonth = (String) userData.get("lastBuyMonth");
    int normalBox = (Integer) userData.get("normalBox");
    int mediumBox = (Integer) userData.get("mediumBox");
    int advancedBox = (Integer) userData.get("advancedBox");
    
    saveUserSignData(groupId, qq, lastDate, consecutive, total, newPoints,
                 lastLikeDate, likeAttempts, makeupCards, lastMakeupDate,
                 titleCard, FiveCard, luckyCard, tenfoldCard, doubleCard,
                 FiveCardActive, tenfoldCardActive, doubleCardActive,
                 monthlyBuyCount, lastBuyMonth,
                 normalBox, mediumBox, advancedBox);
    
    sendMsg(groupId, "", "购买成功！\n消耗：30积分\n获得：幸运卡 x1");
}

private void 购买头衔卡方法(String groupId, String qq) {
    Map<String, Object> userData = loadUserSignData(groupId, qq);
    int points = (Integer) userData.get("points");
    int titleCard = (Integer) userData.get("titleCard");
    
    if (points < 200) {
        sendMsg(groupId, "", "积分不足200，无法购买头衔卡~\n当前积分：" + points);
        return;
    }
    
    int newPoints = points - 200;
    int newTitleCard = titleCard + 1;
    
    String lastDate = (String) userData.get("lastDate");
    int consecutive = (Integer) userData.get("consecutive");
    int total = (Integer) userData.get("total");
    String lastLikeDate = (String) userData.get("lastLikeDate");
    int likeAttempts = (Integer) userData.get("likeAttempts");
    int makeupCards = (Integer) userData.get("makeupCards");
    String lastMakeupDate = (String) userData.get("lastMakeupDate");
    int FiveCard = (Integer) userData.get("FiveCard");
    int luckyCard = (Integer) userData.get("luckyCard");
    int tenfoldCard = (Integer) userData.get("tenfoldCard");
    int doubleCard = (Integer) userData.get("doubleCard");
    int FiveCardActive = (Integer) userData.get("FiveCardActive");
    int tenfoldCardActive = (Integer) userData.get("tenfoldCardActive");
    int doubleCardActive = (Integer) userData.get("doubleCardActive");
    int monthlyBuyCount = (Integer) userData.get("monthlyBuyCount");
    String lastBuyMonth = (String) userData.get("lastBuyMonth");
    
    saveUserSignData(groupId, qq, lastDate, consecutive, total, newPoints,
                 lastLikeDate, likeAttempts, makeupCards, lastMakeupDate,
                 newTitleCard, FiveCard, luckyCard, tenfoldCard, doubleCard,
                 FiveCardActive, tenfoldCardActive, doubleCardActive,
                 monthlyBuyCount, lastBuyMonth,
                 0, 0, 0);
    
    sendMsg(groupId, "", "购买成功！\n消耗：200积分\n获得：头衔卡 x1");
}

private void 使用五倍卡方法(String groupId, String qq) {
    Map<String, Object> userData = loadUserSignData(groupId, qq);
    int fiveCard = (Integer) userData.get("FiveCard");
    
    if (fiveCard <= 0) {
        sendMsg(groupId, "", "没有五倍积分卡，请先去商城购买~");
        return;
    }
    
    int newFiveCard = fiveCard - 1;
    
    saveUserSignData(groupId, qq, 
        (String) userData.get("lastDate"),
        (Integer) userData.get("consecutive"),
        (Integer) userData.get("total"),
        (Integer) userData.get("points"),
        (String) userData.get("lastLikeDate"),
        (Integer) userData.get("likeAttempts"),
        (Integer) userData.get("makeupCards"),
        (String) userData.get("lastMakeupDate"),
        (Integer) userData.get("titleCard"),
        newFiveCard,
        (Integer) userData.get("luckyCard"),
        (Integer) userData.get("tenfoldCard"),
        (Integer) userData.get("doubleCard"),
        1,
        (Integer) userData.get("tenfoldCardActive"),
        (Integer) userData.get("doubleCardActive"),
        (Integer) userData.get("monthlyBuyCount"),
        (String) userData.get("lastBuyMonth"),
        (Integer) userData.get("normalBox"),
        (Integer) userData.get("mediumBox"),
        (Integer) userData.get("advancedBox"));
    
    sendMsg(groupId, "", "使用成功！\n消耗：五倍积分卡 x1\n下次签到将获得五倍积分！");
}

private void 使用幸运卡方法(String groupId, String qq) {
    try {
        Map<String, Object> userData = loadUserSignData(groupId, qq);
        int luckyCard = (Integer) userData.get("luckyCard");
        
        if (luckyCard <= 0) {
            sendMsg(groupId, "", "没有幸运卡，请先去商城购买~");
            return;
        }
        
        int newLuckyCard = luckyCard - 1;
        int random = (int)(Math.random() * 100);
        
        String lastDate = (String) userData.get("lastDate");
        int consecutive = (Integer) userData.get("consecutive");
        int total = (Integer) userData.get("total");
        int points = (Integer) userData.get("points");
        String lastLikeDate = (String) userData.get("lastLikeDate");
        int likeAttempts = (Integer) userData.get("likeAttempts");
        int makeupCards = (Integer) userData.get("makeupCards");
        String lastMakeupDate = (String) userData.get("lastMakeupDate");
        int titleCard = (Integer) userData.get("titleCard");
        int FiveCard = (Integer) userData.get("FiveCard");
        int tenfoldCard = (Integer) userData.get("tenfoldCard");
        int doubleCard = (Integer) userData.get("doubleCard");
        int FiveCardActive = (Integer) userData.get("FiveCardActive");
        int tenfoldCardActive = (Integer) userData.get("tenfoldCardActive");
        int doubleCardActive = (Integer) userData.get("doubleCardActive");
        int monthlyBuyCount = (Integer) userData.get("monthlyBuyCount");
        String lastBuyMonth = (String) userData.get("lastBuyMonth");
        int normalBox = (Integer) userData.get("normalBox");
        int mediumBox = (Integer) userData.get("mediumBox");
        int advancedBox = (Integer) userData.get("advancedBox");
        
        int newPoints = points;
        int newMakeupCards = makeupCards;
        int newTitleCard = titleCard;
        String rewardMsg = "";
        
        if (random < 65) {
            int addPoints = 10 + (int)(Math.random() * 21);
            newPoints = points + addPoints;
            rewardMsg = "获得 " + addPoints + " 积分";
        } else if (random < 70) {
            newMakeupCards = makeupCards + 1;
            rewardMsg = "获得 补签卡 x1";
        } else if (random < 73) {
            newTitleCard = titleCard + 1;
            rewardMsg = "获得 头衔卡 x1";
        } else {
            rewardMsg = "什么也没有发生... 运气不太好呢";
        }
        
        saveUserSignData(groupId, qq, lastDate, consecutive, total, newPoints,
                         lastLikeDate, likeAttempts, newMakeupCards, lastMakeupDate,
                         newTitleCard, FiveCard, newLuckyCard, tenfoldCard, doubleCard,
                         FiveCardActive, tenfoldCardActive, doubleCardActive,
                         monthlyBuyCount, lastBuyMonth,
                         normalBox, mediumBox, advancedBox);
        
        sendMsg(groupId, "", "幸运卡使用成功！\n" + rewardMsg + "\n剩余幸运卡：" + newLuckyCard);
        
    } catch (Exception e) {
        sendMsg(groupId, "", "使用幸运卡失败: " + e.toString());
    }
}

private void 获取签到背景方法(String qun, String sender, String text, MessageData msg) {
    String targetQQ = null;
    
    try {
        if (msg.mAtList != null && !msg.mAtList.isEmpty()) {
            Object atObj = msg.mAtList.get(0);
            if (atObj != null) {
                targetQQ = atObj.toString();
            }
        }
    } catch (Exception e) {}
    
    if (targetQQ == null || targetQQ.isEmpty()) {
        java.util.regex.Pattern p = java.util.regex.Pattern.compile("@(\\d+)");
        java.util.regex.Matcher m = p.matcher(text);
        if (m.find()) {
            targetQQ = m.group(1);
        }
    }
    
    if (targetQQ != null && !targetQQ.isEmpty()) {
        手动获取签到背景(qun, targetQQ);
    } else {
        手动获取签到背景(qun, sender);
    }
}

private void 签到方法(String qun, String qq) {
    try {
        java.util.Date date = new java.util.Date();
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
        String today = sdf.format(date);
        
        Map<String, Object> userData = loadUserSignData(qun, qq);
        String lastDate = (String) userData.get("lastDate");
        int consecutive = (Integer) userData.get("consecutive");
        int total = (Integer) userData.get("total");
        int points = (Integer) userData.get("points");
        
        if (today.equals(lastDate)) {
            sendMsg(qun, "", "今天已经签到过了，请明天再来叭");
            return;
        }
        
        if (!lastDate.isEmpty()) {
            try {
                Date lastDateObj = sdf.parse(lastDate);
                Date todayDate = sdf.parse(today);
                long diff = (todayDate.getTime() - lastDateObj.getTime()) / (1000 * 3600 * 24);
                if (diff == 1) {
                    consecutive++;
                } else if (diff > 1) {
                    consecutive = 1;
                }
            } catch (Exception e) {
                consecutive = 1;
            }
        } else {
            consecutive = 1;
        }
        total++;
        
        int basePoints;
        int addPoints;
        int eggBonus = 0;
        String eggMsg = "";
        
        if (qq.equals(Author)) {
            addPoints = 1000;
            basePoints = 1000;
        } else {
            int r = (int)(Math.random() * 100);
            if (r < 30) basePoints = 5;
            else if (r < 55) basePoints = 6;
            else if (r < 75) basePoints = 7;
            else if (r < 87) basePoints = 8;
            else if (r < 95) basePoints = 9;
            else basePoints = 10;
            
            if (isWeekend()) {
                basePoints = basePoints * 2;
                eggMsg = "周末双倍！";
            }
            
            String timeEgg = 时间彩蛋();
            if (!timeEgg.isEmpty()) {
                String[] parts = timeEgg.split("\\|");
                eggBonus = Integer.parseInt(parts[0]);
                if (!eggMsg.isEmpty()) {
                    eggMsg = eggMsg + " " + parts[1];
                } else {
                    eggMsg = parts[1];
                }
            }
            
            addPoints = basePoints + eggBonus;
            
            int consecutiveBonus = 计算连续签到彩蛋(consecutive);
            addPoints += consecutiveBonus;
            
            int totalBonus = Math.min(total / 10, 10);
            addPoints += totalBonus;
            
            if (!eggMsg.isEmpty()) {
                final String finalEggMsg = eggMsg;
                new Thread(new Runnable() {
                    public void run() {
                        try {
                            Thread.sleep(500);
                            /*sendMsg(qun, "", "✨ " + finalEggMsg + "\n基础积分获得额外加成！");*/
                        } catch (Exception e) {}
                    }
                }).start();
            }
        }
        
        int fiveCardActive = (Integer) userData.get("FiveCardActive");
        int doubleCardActive = (Integer) userData.get("doubleCardActive");
        int tenfoldCardActive = (Integer) userData.get("tenfoldCardActive");
        boolean isDoubleActive = (doubleCardActive == 1);
        boolean isFiveActive = (fiveCardActive == 1);
        boolean isTenfoldActive = (tenfoldCardActive == 1);
        
        int multiplier = 1;
        String multiplierMsg = "";

        if (isTenfoldActive) {
            addPoints = addPoints * 10;
            multiplier = 10;
            multiplierMsg = "十倍卡生效！";
        } else if (isFiveActive) {
            addPoints = addPoints * 5;
            multiplier = 5;
            multiplierMsg = "五倍卡生效！";
        } else if (isDoubleActive) {
            addPoints = addPoints * 2;
            multiplier = 2;
            multiplierMsg = "双倍卡生效！";
        }

        points += addPoints;
        
        String existingAchievements = (String) userData.get("achievements");
        List<String> achievementResult = 检查已解锁的成就(qun, qq, consecutive, total, existingAchievements);
        String newAchievements = achievementResult.get(0);
        int achievementReward = Integer.parseInt(achievementResult.get(1));
        
        if (achievementReward > 0) {
            points += achievementReward;
            addPoints += achievementReward;
        }
        
        检查每日盲盒彩蛋(qun, qq, userData);
        
        saveUserSignData(qun, qq, today, consecutive, total, points,
            (String) userData.get("lastLikeDate"),
            (Integer) userData.get("likeAttempts"),
            (Integer) userData.get("makeupCards"),
            (String) userData.get("lastMakeupDate"),
            (Integer) userData.get("titleCard"),
            (Integer) userData.get("FiveCard"),
            (Integer) userData.get("luckyCard"),
            (Integer) userData.get("tenfoldCard"),
            (Integer) userData.get("doubleCard"),
            0, 0, 0,
            (Integer) userData.get("monthlyBuyCount"),
            (String) userData.get("lastBuyMonth"),
            (Integer) userData.get("normalBox"),
            (Integer) userData.get("mediumBox"),
            (Integer) userData.get("advancedBox"),
            newAchievements);
        
        StringBuilder successMsg = new StringBuilder();
        successMsg.append("签到成功！");
        
        if (!multiplierMsg.isEmpty()) {
            successMsg.append(multiplierMsg);
        }
        if (!qq.equals(Author)) {
            if (isWeekend()) {
                successMsg.append("周末双倍！");
            }
            String timeEgg = 时间彩蛋();
            if (!timeEgg.isEmpty()) {
                String[] parts = timeEgg.split("\\|");
                successMsg.append(" ").append(parts[1]);
            }
        }
        
        successMsg.append("\n获得积分：").append(addPoints);
        
        if (!qq.equals(Author)) {
            int consecutiveBonus = 计算连续签到彩蛋(consecutive);
            int totalBonus = Math.min(total / 10, 10);
            
            if (consecutiveBonus > 0 || totalBonus > 0 || isWeekend() || !时间彩蛋().isEmpty()) {
                successMsg.append(" (");
                
                if (isWeekend()) {
                    successMsg.append("基础").append(basePoints/2).append("×2");
                } else {
                    successMsg.append("基础").append(basePoints);
                }
                
                String timeEgg = 时间彩蛋();
                if (!timeEgg.isEmpty()) {
                    String[] parts = timeEgg.split("\\|");
                    successMsg.append("+").append(parts[0]);
                }
                
                if (consecutiveBonus > 0) {
                    successMsg.append("+连续").append(consecutiveBonus);
                }
                if (totalBonus > 0) {
                    successMsg.append("+累计").append(totalBonus);
                }
                if (multiplier > 1) {
                    successMsg.append(")×").append(multiplier);
                } else {
                    successMsg.append(")");
                }
            }
        }
        
        successMsg.append("\n正在生成签到卡片...");
        sendMsg(qun, "", successMsg.toString());
        
        final int finalAddPoints = addPoints;
        final int finalPoints = points;
        final String finalQun = qun;
        final String finalQq = qq;
        final int finalConsecutive = consecutive;
        final int finalTotal = total;
        
        new Thread(new Runnable() {
        public void run() {
        try {
            String originPath = 下载签到底图(finalQun, finalQq);
            
            if (originPath == null) {
        originPath = 获取默认底图(finalQun, finalQq);
    
        if (originPath != null) {
            sendMsg(finalQun, "", "API图片获取失败，已使用本地默认背景");
        } else {
        String defaultPath = 获取签到背景(finalQun, finalQq);
        boolean created = 设置默认背景(defaultPath);
        
        if (created) {
            originPath = defaultPath;
            sendMsg(finalQun, "", "API图片获取失败，已使用默认背景");
        } else {
            sendMsg(finalQun, "", "图片获取失败，请稍后重试");
            return;
                }
            }
        }
            
            String nickname = 获取实时昵称(finalQun, finalQq);
            if (nickname == null || nickname.isEmpty()) {
                nickname = finalQq;
            }
            
            String checkinPath = 获取签到图片(finalQun, finalQq);
            int signOrder = 获取签到用户(finalQun, finalQq);
            int totalMembers = 获取群成员列表(finalQun);
            
            boolean success = 绘制签到图片(finalQun, finalQq, nickname, 
                finalConsecutive, finalTotal, originPath, checkinPath, 
                signOrder, totalMembers, finalAddPoints, finalPoints);
            
            if (success) {
                发送图片消息(finalQun, checkinPath);
                延迟删除签到图片(checkinPath, 10000);
            } else {
                sendMsg(finalQun, "", "生成签到图片失败");
            }
            } catch (Exception e) {
            sendMsg(finalQun, "", "签到处理异常: " + e.getMessage());
                }
            }
        }).start();
        
    } catch (Exception e) {
        sendMsg(qun, "", "签到失败: " + e.getMessage());
    }
    检查月度奖励(qun);
}

private int 计算连续签到彩蛋(int consecutive) {
    if (consecutive >= 365) {
        return 50;
    } else if (consecutive >= 100) {
        return 30;
    } else if (consecutive >= 30) {
        return 15;
    } else if (consecutive >= 15) {
        return 8;
    } else if (consecutive >= 7) {
        return 5;
    } else if (consecutive >= 3) {
        return 2;
    }
    return 0;
}

private boolean 绘制签到图片(String groupUin, String qq, String nickname, int consecutive, int total, String originPath, String outputPath, int signOrder, int totalMembers, int addPoints, int points) {
    FileOutputStream fos = null;
    Bitmap originBitmap = null;
    Bitmap resultBitmap = null;
    Bitmap avatar = null;
    
    try {
        File originFile = new File(originPath);
        if (!originFile.exists() || originFile.length() == 0) {
            return false;
        }
        
        BitmapFactory.Options options = new BitmapFactory.Options();
        options.inPreferredConfig = Bitmap.Config.RGB_565;
        originBitmap = BitmapFactory.decodeFile(originPath, options);
        
        if (originBitmap == null) {
            return false;
        }
        
        resultBitmap = originBitmap.copy(Bitmap.Config.ARGB_8888, true);
        if (resultBitmap == null) {
            originBitmap.recycle();
            return false;
        }
        
        Canvas canvas = new Canvas(resultBitmap);
        
        int width = resultBitmap.getWidth();
        int height = resultBitmap.getHeight();
        
        int layerWidth = (int)(width * 0.8);
        int layerHeight = (int)(height * 0.65);
        int left = (width - layerWidth) / 2;
        int top = (height - layerHeight) / 2;
        
        Paint bgPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        bgPaint.setColor(Color.WHITE);
        bgPaint.setAlpha(180);
        
        android.graphics.Path path = new android.graphics.Path();
        int radius = 20;
        path.addRoundRect(new RectF(left, top, left + layerWidth, top + layerHeight), radius, radius, android.graphics.Path.Direction.CW);
        canvas.drawPath(path, bgPaint);
        
        avatar = 获取用户头像(qq);
        int avatarSize = (int)(layerHeight * 0.3);
        if (avatarSize < 60) avatarSize = 60;
        int avatarLeft = left + (int)(layerWidth * 0.05);
        int avatarTop = top + (int)(layerHeight * 0.08);
        
        Bitmap scaledAvatar = Bitmap.createScaledBitmap(avatar, avatarSize, avatarSize, true);
        canvas.drawBitmap(scaledAvatar, avatarLeft, avatarTop, null);
        
        float avatarCenterY = avatarTop + avatarSize / 2f;
        int textAreaWidth = layerWidth - avatarSize - (int)(layerWidth * 0.08);
        
        Paint namePaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        namePaint.setColor(Color.BLACK);
        namePaint.setTextSize(avatarSize * 0.45f);
        namePaint.setTypeface(Typeface.create(Typeface.SERIF, Typeface.BOLD));
        
        Paint greetPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        greetPaint.setColor(Color.DKGRAY);
        greetPaint.setTextSize(avatarSize * 0.3f);
        greetPaint.setTypeface(Typeface.create(Typeface.SERIF, Typeface.NORMAL));
        
        Paint infoPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        infoPaint.setColor(Color.BLACK);
        infoPaint.setTextSize(avatarSize * 0.28f);
        infoPaint.setTypeface(Typeface.create(Typeface.SERIF, Typeface.NORMAL));
        
        Paint orderPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        orderPaint.setColor(Color.GRAY);
        orderPaint.setTextSize(avatarSize * 0.28f);
        orderPaint.setTypeface(Typeface.create(Typeface.SERIF, Typeface.NORMAL));
        
        Paint timePaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        timePaint.setColor(Color.BLACK);
        timePaint.setTextSize(avatarSize * 0.25f);
        timePaint.setTypeface(Typeface.create(Typeface.SERIF, Typeface.NORMAL));
        timePaint.setTextAlign(Align.RIGHT);
        
        String displayName = (nickname != null && !nickname.isEmpty()) ? nickname : qq;
        int nameX = avatarLeft + avatarSize + (int)(layerWidth * 0.03);
        
        List<String> nameLines = wrapText(displayName, textAreaWidth, namePaint);
        int nameLinesCount = nameLines.size();
        
        Paint.FontMetrics fm = namePaint.getFontMetrics();
        float lineHeight = fm.descent - fm.ascent;
        float totalNameHeight = nameLinesCount * lineHeight;
        
        float textBlockTop = avatarCenterY - totalNameHeight / 2;
        float firstLineBaseline = textBlockTop - fm.ascent;
        
        for (int i = 0; i < nameLines.size(); i++) {
            float baselineY = firstLineBaseline + i * lineHeight;
            canvas.drawText(nameLines.get(i), nameX, baselineY, namePaint);
        }

        float lastLineBaseline = firstLineBaseline + (nameLinesCount - 1) * lineHeight;
        float textBlockBottom = lastLineBaseline + fm.descent;
        
        Paint.FontMetrics greetFm = greetPaint.getFontMetrics();
        float greetLineHeight = greetFm.descent - greetFm.ascent;
        
        String greetText = 获取问候语();
        float greetY = textBlockBottom + greetLineHeight + 15;
        canvas.drawText(greetText, nameX, greetY, greetPaint);
        
        int consecutiveBonus = 0;
        if (consecutive >= 365) consecutiveBonus = 50;
        else if (consecutive >= 100) consecutiveBonus = 30;
        else if (consecutive >= 30) consecutiveBonus = 15;
        else if (consecutive >= 15) consecutiveBonus = 8;
        else if (consecutive >= 7) consecutiveBonus = 5;
        else if (consecutive >= 3) consecutiveBonus = 2;
        
        int totalBonus = 0;
        if (total >= 10) totalBonus = Math.min(total / 10, 10);
        
        Paint.FontMetrics infoFm = infoPaint.getFontMetrics();
        float infoLineHeight = infoFm.descent - infoFm.ascent;
        float lineSpacing = 8;
        
        String signText = "签到成功 +" + addPoints + "积分";
        String continuousText = "连续签到: " + consecutive + "天";
        String totalText = "总签到: " + total + "天";
        String pointsText = "总积分: " + points;
        
        float infoY = greetY + infoLineHeight + 15;
        canvas.drawText(signText, nameX, infoY, infoPaint);
        
        float line2Y = infoY + infoLineHeight + lineSpacing;
        if (consecutiveBonus > 0) {
            canvas.drawText(continuousText + " (+" + consecutiveBonus + ")", nameX, line2Y, infoPaint);
        } else {
            canvas.drawText(continuousText, nameX, line2Y, infoPaint);
        }
        
        float line3Y = line2Y + infoLineHeight + lineSpacing;
        if (totalBonus > 0) {
            canvas.drawText(totalText + " (+" + totalBonus + ")", nameX, line3Y, infoPaint);
        } else {
            canvas.drawText(totalText, nameX, line3Y, infoPaint);
        }
        
        float line4Y = line3Y + infoLineHeight + lineSpacing;
        canvas.drawText(pointsText, nameX, line4Y, infoPaint);
        
        String orderText = signOrder + "/" + totalMembers;
        int orderX = left + (int)(layerWidth * 0.05);
        int orderY = top + layerHeight - (int)(layerHeight * 0.05);
        canvas.drawText(orderText, orderX, orderY, orderPaint);
        
        java.util.Date date = new java.util.Date();
        SimpleDateFormat sdfTime = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault());
        String timeStr = sdfTime.format(date);
        
        float timeX = left + layerWidth - (int)(layerWidth * 0.05);
        float timeY = top + layerHeight - (int)(layerHeight * 0.05);
        canvas.drawText(timeStr, timeX, timeY, timePaint);
        
        File outputFile = new File(outputPath);
        File parentDir = outputFile.getParentFile();
        if (!parentDir.exists()) {
            parentDir.mkdirs();
        }
        
        if (outputFile.exists()) {
            outputFile.delete();
        }
        
        fos = new FileOutputStream(outputPath);
        resultBitmap.compress(Bitmap.CompressFormat.JPEG, 85, fos);
        fos.flush();
        fos.close();
        fos = null;
        
        return true;
        
    } catch (Exception e) {
        return false;
    } finally {
        try {
            if (fos != null) fos.close();
        } catch (Exception e) {}
        if (resultBitmap != null && !resultBitmap.isRecycled()) {
            resultBitmap.recycle();
        }
        if (originBitmap != null && !originBitmap.isRecycled()) {
            originBitmap.recycle();
        }
        if (avatar != null && !avatar.isRecycled()) {
            avatar.recycle();
        }
    }
}

private String 绘制菜单图片(String title, String content, String groupId, String qq) {
    try {
        Activity a = getActivity();
        if (a == null) return null;
        
        int width = dp(500);
        int padding = dp(20);
        int lineHeight = dp(32);
        
        String[] lines = content.split("\n");
        int contentHeight = lines.length * lineHeight + dp(80);
        int height = Math.max(dp(400), contentHeight);
        
        Bitmap bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(bitmap);
        
        int bgColor = isNightMode ? Color.parseColor("#1E1E2E") : Color.parseColor("#FFF0F5");
        canvas.drawColor(bgColor);
        
        Paint titlePaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        titlePaint.setColor(isNightMode ? Color.WHITE : Color.BLACK);
        titlePaint.setTextSize(dp(22));
        titlePaint.setTypeface(Typeface.create("serif", Typeface.BOLD));
        titlePaint.setTextAlign(Align.CENTER);
        canvas.drawText(title, width / 2, dp(38), titlePaint);
        
        Paint textPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        textPaint.setColor(isNightMode ? Color.parseColor("#EEEEEE") : Color.parseColor("#333333"));
        textPaint.setTextSize(dp(15));
        textPaint.setTypeface(Typeface.create("serif", Typeface.NORMAL));
        textPaint.setTextAlign(Align.LEFT);
        
        float y = dp(80);
        for (String line : lines) {
            canvas.drawText(line, padding, y, textPaint);
            y += lineHeight;
        }
        
        String fileName = "menu_" + System.currentTimeMillis() + ".png";
        String filePath = appPath + "/temp/" + fileName;
        File dir = new File(appPath + "/temp");
        if (!dir.exists()) dir.mkdirs();
        
        FileOutputStream fos = new FileOutputStream(filePath);
        bitmap.compress(Bitmap.CompressFormat.PNG, 90, fos);
        fos.close();
        bitmap.recycle();
        
        return filePath;
        
    } catch (Exception e) {
        sendMsg(groupId, "", "生成图片失败: " + e.getMessage());
        return null;
    }
}

private void 赞我指令判断(String groupId, String senderQQ, String text) {
    String sessionKey = groupId + "_" + senderQQ;
    SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
    String today = sdf.format(new Date());
    
    Map<String, Object> userData = loadUserSignData(groupId, senderQQ);
    String lastLikeDate = (String) userData.get("lastLikeDate");
    int likeAttempts = (Integer) userData.get("likeAttempts");
    
    if (!today.equals(lastLikeDate)) {
        likeAttempts = 0;
    }
    
    if (likeAttempts >= 3) {
        sendMsg(groupId, "", "今日赞我次数已用完，请明天再来~");
        return;
    }
    
    if (likeSessionMap.containsKey(sessionKey)) {
        String choice = text.trim();
        if (choice.equals("1")) {
            likeSessionMap.remove(sessionKey);
            取消点赞计时(sessionKey);
            点赞方法(groupId, senderQQ, true, today, likeAttempts);
        } else if (choice.equals("2")) {
            likeSessionMap.remove(sessionKey);
            取消点赞计时(sessionKey);
            点赞方法(groupId, senderQQ, false, today, likeAttempts);
        } else if (choice.equals("取消")) {
            likeSessionMap.remove(sessionKey);
            取消点赞计时(sessionKey);
            sendMsg(groupId, "", "已取消赞我操作\n[剩余尝试次数：" + (3 - likeAttempts) + "/3]");
        } else {
            sendMsg(groupId, "", "选择无效，请输入 1 或 2\n[剩余尝试次数：" + (3 - likeAttempts) + "/3]");
        }
        return;
    }
    
    if (text.equals("赞我")) {
        likeAttempts++;
        saveUserSignData(groupId, senderQQ, (String) userData.get("lastDate"), 
                (Integer) userData.get("consecutive"), (Integer) userData.get("total"), 
                (Integer) userData.get("points"), today, likeAttempts, 0, "",
                0, 0, 0, 0, 0, 0, 0, 0, 0, "", 0, 0, 0);
        
        likeSessionMap.put(sessionKey, "waiting");
        
        开始点赞计时(sessionKey, groupId, senderQQ);
        
        sendMsg(groupId, "", "请选择关系：\n1. 好友（10个赞，消耗10积分）\n2. 非好友（50个赞，消耗50积分）\n[剩余尝试次数：" + (3 - likeAttempts) + "/3]\n(60秒内未选择将自动取消)");
    }
}

private void 开始点赞计时(final String sessionKey, final String groupId, final String senderQQ) {
    取消点赞计时(sessionKey);
    
    final java.util.Timer timer = new java.util.Timer();
    TimerTask task = new TimerTask() {
        public void run() {
            try {
                if (likeSessionMap.containsKey(sessionKey)) {
                    likeSessionMap.remove(sessionKey);
                    sendMsg(groupId, "", "赞我操作已超时取消，请重新发送");
                }
            } catch (Exception e) {}
            likeTimerTasks.remove(sessionKey);
            timer.cancel();
        }
    };
    likeTimerTasks.put(sessionKey, task);
    timer.schedule(task, 60000);
}

private void 取消点赞计时(String sessionKey) {
    TimerTask task = likeTimerTasks.remove(sessionKey);
    if (task != null) {
        task.cancel();
    }
}

private void 点赞方法(String groupId, String senderQQ, boolean isFriend, String today, int currentAttempts) {
    Map<String, Object> userData = loadUserSignData(groupId, senderQQ);
    int currentPoints = (Integer) userData.get("points");
    int requiredPoints = isFriend ? POINTS_FOR_LIKE_FRIEND : POINTS_FOR_LIKE_NONFRIEND;
    int likeCount = isFriend ? LIKES_PER_FRIEND : LIKES_PER_NONFRIEND;
    
    if (currentPoints < requiredPoints) {
        int remaining = 3 - currentAttempts;
        sendMsg(groupId, "", "当前积分：" + currentPoints + "\n需要积分：" + requiredPoints + "\n积分不足，无法点赞\n[剩余尝试次数：" + remaining + "/3]");
        return;
    }
    
    int newPoints = currentPoints - requiredPoints;
    
    String lastDate = (String) userData.get("lastDate");
    int consecutive = (Integer) userData.get("consecutive");
    int total = (Integer) userData.get("total");
    saveUserSignData(groupId, senderQQ, lastDate, consecutive, total, newPoints, today, 3, 0, "",
                     0, 0, 0, 0, 0, 0, 0, 0, 0, "", 0, 0, 0);
    
    int j = likeCount / 10;
    for (int i = 0; i < j; i++) {
        sendLike(senderQQ, 10);
    }
    
    sendMsg(groupId, "", "点赞成功！\n积分扣除：" + currentPoints + " → " + newPoints + 
            "\n已为您点赞 " + likeCount + " 次\n[今日赞我次数已用完]");
    
    cachedUserList = null;
    cachedGroupId = null;
    
    String sessionKey = groupId + "_" + senderQQ;
    likeSessionMap.remove(sessionKey);
    取消点赞计时(sessionKey);
}

private List<String> wrapText(String text, int maxWidth, Paint paint) {
    List<String> lines = new ArrayList<String>();
    if (text == null || text.isEmpty()) {
        lines.add("");
        return lines;
    }
    
    char[] chars = text.toCharArray();
    StringBuilder currentLine = new StringBuilder();
    float currentWidth = 0;
    
    for (int i = 0; i < chars.length; i++) {
        char c = chars[i];
        float charWidth = paint.measureText(String.valueOf(c));
        
        if (currentWidth + charWidth <= maxWidth) {
            currentLine.append(c);
            currentWidth += charWidth;
        } else {
            lines.add(currentLine.toString());
            currentLine = new StringBuilder();
            currentLine.append(c);
            currentWidth = charWidth;
        }
    }
    
    if (currentLine.length() > 0) {
        lines.add(currentLine.toString());
    }
    
    if (lines.size() > 3) {
        lines = lines.subList(0, 3);
        String lastLine = lines.get(2);
        while (paint.measureText(lastLine + "...") > maxWidth && lastLine.length() > 0) {
            lastLine = lastLine.substring(0, lastLine.length() - 1);
        }
        lines.set(2, lastLine + "...");
    }
    
    return lines;
}

private void 手动获取签到背景(String qun, String qq) {
    String originPath = 获取签到背景(qun, qq);
    File originFile = new File(originPath);
    
    if (originFile.exists()) {
        发送图片消息(qun, originPath);
    } else {
        sendMsg(qun, "", "还没有签到背景图，请先签到获取");
    }
}

private void 签到排名(String groupId, String qq) {
    new Thread(new Runnable() {
        public void run() {
            String imagePath = 绘制排名图片(groupId);
            if (imagePath != null) {
                发送图片消息(groupId, imagePath);
                延迟删除签到图片(imagePath, 30000);
            } else {
                sendMsg(groupId, "", "生成排行榜失败~");
            }
        }
    }).start();
}

private String 绘制排名图片(String groupId) {
    try {
        Activity a = getActivity();
        if (a == null) return null;
        
        List<Map<String, Object>> allData = loadAllGroupSignData(groupId);
        if (allData == null || allData.isEmpty()) {
            sendMsg(groupId, "", "暂无签到数据~");
            return null;
        }
        
        List<Map<String, Object>> totalRank = new ArrayList<Map<String, Object>>(allData);
        Collections.sort(totalRank, new Comparator<Map<String, Object>>() {
            public int compare(Map<String, Object> o1, Map<String, Object> o2) {
                return ((Integer) o2.get("total")).compareTo((Integer) o1.get("total"));
            }
        });
        
        List<Map<String, Object>> pointsRank = new ArrayList<Map<String, Object>>(allData);
        Collections.sort(pointsRank, new Comparator<Map<String, Object>>() {
            public int compare(Map<String, Object> o1, Map<String, Object> o2) {
                return ((Integer) o2.get("points")).compareTo((Integer) o1.get("points"));
            }
        });
        
        int width = dp(700);
        int padding = dp(20);
        int lineHeight = dp(38);
        int titleHeight = dp(60);
        int headerHeight = dp(50);
        int maxRows = 15;
        
        int contentHeight = titleHeight + headerHeight + maxRows * lineHeight + dp(40);
        int height = Math.max(dp(500), contentHeight);
        
        Bitmap bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(bitmap);
        
        int bgColor = isNightMode ? Color.parseColor("#1E1E2E") : Color.parseColor("#FFF8F0");
        canvas.drawColor(bgColor);
        
        Paint titlePaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        titlePaint.setColor(isNightMode ? Color.parseColor("#FFB74D") : Color.parseColor("#D47F6B"));
        titlePaint.setTextSize(dp(26));
        titlePaint.setTypeface(Typeface.create("serif", Typeface.BOLD));
        titlePaint.setTextAlign(Align.CENTER);
        canvas.drawText("签到排行榜", width / 2, dp(45), titlePaint);
        
        int midX = width / 2;
        Paint linePaint = new Paint();
        linePaint.setColor(isNightMode ? Color.parseColor("#444444") : Color.parseColor("#FFE5D9"));
        linePaint.setStrokeWidth(dp(2));
        canvas.drawLine(midX, dp(70), midX, height - dp(20), linePaint);
        
        Paint headerPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        headerPaint.setColor(isNightMode ? Color.parseColor("#FFB74D") : Color.parseColor("#D47F6B"));
        headerPaint.setTextSize(dp(18));
        headerPaint.setTypeface(Typeface.create("serif", Typeface.BOLD));
        headerPaint.setTextAlign(Align.CENTER);
        
        canvas.drawText("天数榜", midX / 2, dp(90), headerPaint);
        canvas.drawText("积分榜", midX + midX / 2, dp(90), headerPaint);
        
        Paint subHeaderPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        subHeaderPaint.setColor(isNightMode ? Color.parseColor("#AAAAAA") : Color.parseColor("#666666"));
        subHeaderPaint.setTextSize(dp(14));
        subHeaderPaint.setTextAlign(Align.LEFT);
        
        int leftColX = padding;
        int rightColX = midX + padding;
        int startY = dp(120);
        
        canvas.drawText("排名", leftColX, startY, subHeaderPaint);
        canvas.drawText("昵称", leftColX + dp(80), startY, subHeaderPaint);
        canvas.drawText("天数", leftColX + dp(280), startY, subHeaderPaint);
        
        canvas.drawText("排名", rightColX, startY, subHeaderPaint);
        canvas.drawText("昵称", rightColX + dp(80), startY, subHeaderPaint);
        canvas.drawText("积分", rightColX + dp(280), startY, subHeaderPaint);
        
        Paint namePaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        namePaint.setColor(isNightMode ? Color.parseColor("#EEEEEE") : Color.parseColor("#333333"));
        namePaint.setTextSize(dp(14));
        namePaint.setTextAlign(Align.LEFT);
        
        Paint rankPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        rankPaint.setTextSize(dp(14));
        rankPaint.setTextAlign(Align.LEFT);
        
        int y = startY + lineHeight;
        
        for (int i = 0; i < maxRows && i < totalRank.size(); i++) {
            Map<String, Object> data = totalRank.get(i);
            String qq = (String) data.get("qq");
            String nickname = 获取实时昵称(groupId, qq);
            if (nickname == null || nickname.isEmpty()) nickname = qq;
            if (nickname.length() > 10) nickname = nickname.substring(0, 9) + "…";
            
            int rank = i + 1;
            if (rank == 1) rankPaint.setColor(Color.parseColor("#FFD700"));
            else if (rank == 2) rankPaint.setColor(Color.parseColor("#C0C0C0"));
            else if (rank == 3) rankPaint.setColor(Color.parseColor("#CD7F32"));
            else rankPaint.setColor(isNightMode ? Color.parseColor("#AAAAAA") : Color.parseColor("#666666"));
            
            canvas.drawText(String.valueOf(rank), leftColX, y, rankPaint);
            canvas.drawText(nickname, leftColX + dp(80), y, namePaint);
            canvas.drawText(String.valueOf(data.get("total")), leftColX + dp(280), y, namePaint);
            y += lineHeight;
        }
        
        y = startY + lineHeight;
        int pointsCount = 0;
        for (int i = 0; i < maxRows && i < pointsRank.size(); i++) {
            Map<String, Object> data = pointsRank.get(i);
            String qq = (String) data.get("qq");
            
            if (qq.equals(Author)) continue;
            
            String nickname = 获取实时昵称(groupId, qq);
            if (nickname == null || nickname.isEmpty()) nickname = qq;
            if (nickname.length() > 10) nickname = nickname.substring(0, 9) + "…";
            
            int rank = pointsCount + 1;
            if (rank == 1) rankPaint.setColor(Color.parseColor("#FFD700"));
            else if (rank == 2) rankPaint.setColor(Color.parseColor("#C0C0C0"));
            else if (rank == 3) rankPaint.setColor(Color.parseColor("#CD7F32"));
            else rankPaint.setColor(isNightMode ? Color.parseColor("#AAAAAA") : Color.parseColor("#666666"));
            
            canvas.drawText(String.valueOf(rank), rightColX, y, rankPaint);
            canvas.drawText(nickname, rightColX + dp(80), y, namePaint);
            canvas.drawText(String.valueOf(data.get("points")), rightColX + dp(280), y, namePaint);
            
            pointsCount++;
            y += lineHeight;
            if (pointsCount >= maxRows) break;
        }
        
        String fileName = "ranking_" + System.currentTimeMillis() + ".png";
        String filePath = appPath + "/temp/" + fileName;
        File dir = new File(appPath + "/temp");
        if (!dir.exists()) dir.mkdirs();
        
        FileOutputStream fos = new FileOutputStream(filePath);
        bitmap.compress(Bitmap.CompressFormat.PNG, 90, fos);
        fos.close();
        bitmap.recycle();
        
        return filePath;
        
    } catch (Exception e) {
        return null;
    }
}

private List<Map<String, Object>> loadAllGroupSignData(String qun) {
    List<Map<String, Object>> list = new ArrayList<Map<String, Object>>();
    dataLock.readLock().lock();
    try {
        File file = new File(signDataFileEncrypted);
        if (!file.exists()) return list;
        
        String content = 解密文件(file);
        String[] lines = content.split("\n");
        for (String line : lines) {
            if (line.trim().isEmpty()) continue;
            String[] parts = line.trim().split("\\|");
            if (parts.length >= 5 && parts[0].equals(qun)) {
                Map<String, Object> data = new HashMap<String, Object>();
                data.put("qq", parts[1]);
                data.put("total", Integer.parseInt(parts[4]));
                if (parts.length >= 6) data.put("points", Integer.parseInt(parts[5]));
                else data.put("points", 0);
                list.add(data);
            }
        }
    } catch (Exception e) {}
    finally {
        dataLock.readLock().unlock();
    }
    return list;
}

private Map<String, Object> loadUserSignData(String qun, String qq) {
    Map<String, Object> data = new HashMap<String, Object>();
    data.put("lastDate", "");
    data.put("consecutive", 0);
    data.put("total", 0);
    data.put("points", 0);
    data.put("lastLikeDate", "");
    data.put("likeAttempts", 0);
    data.put("makeupCards", 0);
    data.put("lastMakeupDate", "");
    data.put("titleCard", 0);
    data.put("doubleCard", 0);
    data.put("doubleCardActive", 0);
    data.put("FiveCard", 0);
    data.put("luckyCard", 0);
    data.put("tenfoldCard", 0);
    data.put("FiveCardActive", 0);
    data.put("tenfoldCardActive", 0);
    data.put("monthlyBuyCount", 0);
    data.put("lastBuyMonth", "");
    data.put("normalBox", 0);
    data.put("mediumBox", 0);
    data.put("advancedBox", 0);
    data.put("achievements", "");
    data.put("lastBlindBoxDate", "");
    
    dataLock.readLock().lock();
    try {
        File file = new File(signDataFileEncrypted);
        if (!file.exists()) return data;
        
        String content = 解密文件(file);
        String[] lines = content.split("\n");
        for (String line : lines) {
            if (line.trim().isEmpty()) continue;
            String[] parts = line.trim().split("\\|");
            if (parts.length >= 5 && parts[0].equals(qun) && parts[1].equals(qq)) {
                data.put("lastDate", parts[2]);
                data.put("consecutive", Integer.parseInt(parts[3]));
                data.put("total", Integer.parseInt(parts[4]));
                if (parts.length >= 6) data.put("points", Integer.parseInt(parts[5]));
                if (parts.length >= 7) data.put("lastLikeDate", parts[6]);
                if (parts.length >= 8) data.put("likeAttempts", Integer.parseInt(parts[7]));
                if (parts.length >= 9) data.put("makeupCards", Integer.parseInt(parts[8]));
                if (parts.length >= 10) data.put("lastMakeupDate", parts[9]);
                if (parts.length >= 11) data.put("titleCard", Integer.parseInt(parts[10]));
                if (parts.length >= 12) data.put("FiveCard", Integer.parseInt(parts[11]));
                if (parts.length >= 13) data.put("luckyCard", Integer.parseInt(parts[12]));
                if (parts.length >= 14) data.put("tenfoldCard", Integer.parseInt(parts[13]));
                if (parts.length >= 15) data.put("doubleCard", Integer.parseInt(parts[14]));
                if (parts.length >= 16) data.put("FiveCardActive", Integer.parseInt(parts[15]));
                if (parts.length >= 17) data.put("tenfoldCardActive", Integer.parseInt(parts[16]));
                if (parts.length >= 18) data.put("doubleCardActive", Integer.parseInt(parts[17]));
                if (parts.length >= 19) data.put("monthlyBuyCount", Integer.parseInt(parts[18]));
                if (parts.length >= 20) data.put("lastBuyMonth", parts[19]);
                if (parts.length >= 21) data.put("normalBox", Integer.parseInt(parts[20]));
                if (parts.length >= 22) data.put("mediumBox", Integer.parseInt(parts[21]));
                if (parts.length >= 23) data.put("advancedBox", Integer.parseInt(parts[22]));
                if (parts.length >= 24) data.put("achievements", parts[23]);
                if (parts.length >= 25) data.put("lastBlindBoxDate", parts[24]);
                break;
            }
        }
    } catch (Exception e) {}
    finally {
        dataLock.readLock().unlock();
    }
    return data;
}

private void saveUserSignData(String qun, String qq, String lastDate, int consecutive, int total, int points, 
                              String lastLikeDate, int likeAttempts, int makeupCards, String lastMakeupDate,
                              int titleCard, int FiveCard, int luckyCard, int tenfoldCard,
                              int doubleCard, int FiveCardActive, int tenfoldCardActive, int doubleCardActive,
                              int monthlyBuyCount, String lastBuyMonth,
                              int normalBox, int mediumBox, int advancedBox, String achievements, String lastBlindBoxDate) {
    dataLock.writeLock().lock();
    try {
        File dir = new File(appPath + "/sign");
        if (!dir.exists()) dir.mkdirs();
        
        List<String> lines = new ArrayList<String>();
        File file = new File(signDataFileEncrypted);
        
        if (file.exists()) {
            String content = 解密文件(file);
            String[] existingLines = content.split("\n");
            for (String line : existingLines) {
                if (line.trim().isEmpty()) continue;
                lines.add(line);
            }
        }
        
        boolean found = false;
        for (int i = 0; i < lines.size(); i++) {
            String[] parts = lines.get(i).split("\\|");
            if (parts.length >= 5 && parts[0].equals(qun) && parts[1].equals(qq)) {
                lines.set(i, qun + "|" + qq + "|" + lastDate + "|" + consecutive + "|" + total + "|" + 
                    points + "|" + lastLikeDate + "|" + likeAttempts + "|" + makeupCards + "|" + lastMakeupDate +
                    "|" + titleCard + "|" + FiveCard + "|" + luckyCard + "|" + tenfoldCard +
                    "|" + doubleCard + "|" + FiveCardActive + "|" + tenfoldCardActive + "|" + doubleCardActive +
                    "|" + monthlyBuyCount + "|" + lastBuyMonth +
                    "|" + normalBox + "|" + mediumBox + "|" + advancedBox + "|" + achievements + "|" + lastBlindBoxDate);
                found = true;
                break;
            }
        }
        
        if (!found) {
            lines.add(qun + "|" + qq + "|" + lastDate + "|" + consecutive + "|" + total + "|" + 
                points + "|" + lastLikeDate + "|" + likeAttempts + "|" + makeupCards + "|" + lastMakeupDate +
                "|" + titleCard + "|" + FiveCard + "|" + luckyCard + "|" + tenfoldCard +
                "|" + doubleCard + "|" + FiveCardActive + "|" + tenfoldCardActive + "|" + doubleCardActive +
                "|" + monthlyBuyCount + "|" + lastBuyMonth +
                "|" + normalBox + "|" + mediumBox + "|" + advancedBox + "|" + achievements + "|" + lastBlindBoxDate);
        }
        
        File tempFile = new File(appPath + "/sign/data_temp.txt");
        BufferedWriter writer = new BufferedWriter(new FileWriter(tempFile));
        for (String line : lines) {
            writer.write(line);
            writer.newLine();
        }
        writer.close();
        
        加密文件(tempFile, file);
        
    } catch (Exception e) {}
    finally {
        dataLock.writeLock().unlock();
    }
}

/*private void saveUserSignData(String qun, String qq, String lastDate, int consecutive, int total, int points) {
    SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
    String today = sdf.format(new Date());
    saveUserSignData(qun, qq, lastDate, consecutive, total, points, today, 0, 0, "", 
                     0, 0, 0, 0, 0, 0, 0, 0, 0, "", 0, 0, 0);
}*/

private List<Map<String, Object>> loadGroupSignData(String qun) {
    List<Map<String, Object>> list = new ArrayList<Map<String, Object>>();
    dataLock.readLock().lock();
    try {
        File file = new File(signDataFileEncrypted);
        if (!file.exists()) return list;
        
        String content = 解密文件(file);
        String[] lines = content.split("\n");
        for (String line : lines) {
            if (line.trim().isEmpty()) continue;
            String[] parts = line.trim().split("\\|");
            if (parts.length >= 5 && parts[0].equals(qun)) {
                Map<String, Object> data = new HashMap<String, Object>();
                data.put("qq", parts[1]);
                data.put("lastDate", parts[2]);
                data.put("consecutive", Integer.parseInt(parts[3]));
                data.put("total", Integer.parseInt(parts[4]));
                list.add(data);
            }
        }
    } catch (Exception e) {}
    finally {
        dataLock.readLock().unlock();
    }
    return list;
}

private void saveUserSignData(String qun, String qq, String lastDate, int consecutive, int total, int points, 
                              String lastLikeDate, int likeAttempts, int makeupCards, String lastMakeupDate,
                              int titleCard, int FiveCard, int luckyCard, int tenfoldCard,
                              int doubleCard, int FiveCardActive, int tenfoldCardActive, int doubleCardActive,
                              int monthlyBuyCount, String lastBuyMonth,
                              int normalBox, int mediumBox, int advancedBox, String achievements) {
    Map<String, Object> oldData = loadUserSignData(qun, qq);
    String lastBlindBoxDate = (String) oldData.get("lastBlindBoxDate");
    if (lastBlindBoxDate == null) lastBlindBoxDate = "";
    
    saveUserSignData(qun, qq, lastDate, consecutive, total, points,
                     lastLikeDate, likeAttempts, makeupCards, lastMakeupDate,
                     titleCard, FiveCard, luckyCard, tenfoldCard, doubleCard,
                     FiveCardActive, tenfoldCardActive, doubleCardActive,
                     monthlyBuyCount, lastBuyMonth,
                     normalBox, mediumBox, advancedBox, achievements, lastBlindBoxDate);
}

private void saveUserSignData(String qun, String qq, String lastDate, int consecutive, int total, int points, 
                              String lastLikeDate, int likeAttempts, int makeupCards, String lastMakeupDate,
                              int titleCard, int FiveCard, int luckyCard, int tenfoldCard,
                              int doubleCard, int FiveCardActive, int tenfoldCardActive, int doubleCardActive,
                              int monthlyBuyCount, String lastBuyMonth,
                              int normalBox, int mediumBox, int advancedBox) {
    Map<String, Object> oldData = loadUserSignData(qun, qq);
    String achievements = (String) oldData.get("achievements");
    if (achievements == null) achievements = "";
    String lastBlindBoxDate = (String) oldData.get("lastBlindBoxDate");
    if (lastBlindBoxDate == null) lastBlindBoxDate = "";
    
    saveUserSignData(qun, qq, lastDate, consecutive, total, points,
                     lastLikeDate, likeAttempts, makeupCards, lastMakeupDate,
                     titleCard, FiveCard, luckyCard, tenfoldCard, doubleCard,
                     FiveCardActive, tenfoldCardActive, doubleCardActive,
                     monthlyBuyCount, lastBuyMonth,
                     normalBox, mediumBox, advancedBox, achievements, lastBlindBoxDate);
}

private void 删除单个用户签到数据(String qq) {
    dataLock.writeLock().lock();
    try {
        File file = new File(signDataFileEncrypted);
        if (!file.exists()) return;
        
        List<String> lines = new ArrayList<String>();
        String content = 解密文件(file);
        String[] existingLines = content.split("\n");
        boolean found = false;
        
        for (String line : existingLines) {
            if (line.trim().isEmpty()) continue;
            String[] parts = line.trim().split("\\|");
            if (parts.length >= 5 && parts[0].equals(currentGroupId) && parts[1].equals(qq)) {
                found = true;
                continue;
            }
            lines.add(line);
        }
        
        if (!found) {
            toast("未找到该用户的签到数据");
            return;
        }
        
        File tempFile = new File(appPath + "/sign/data_temp.txt");
        BufferedWriter writer = new BufferedWriter(new FileWriter(tempFile));
        for (String line : lines) {
            writer.write(line);
            writer.newLine();
        }
        writer.close();
        
        加密文件(tempFile, file);
        
        toast("已删除该用户的签到数据");
        
    } catch (Exception e) {
        toast("删除失败: " + e.getMessage());
    } finally {
        dataLock.writeLock().unlock();
    }
}

private void 清空当前群聊签到数据(String groupId) {
    dataLock.writeLock().lock();
    try {
        File file = new File(signDataFileEncrypted);
        if (!file.exists()) {
            toast("没有签到数据");
            return;
        }
        
        List<String> lines = new ArrayList<String>();
        String content = 解密文件(file);
        String[] existingLines = content.split("\n");
        
        for (String line : existingLines) {
            if (line.trim().isEmpty()) continue;
            String[] parts = line.trim().split("\\|");
            if (parts.length >= 5 && !parts[0].equals(groupId)) {
                lines.add(line);
            }
        }
        
        File tempFile = new File(appPath + "/sign/data_temp.txt");
        BufferedWriter writer = new BufferedWriter(new FileWriter(tempFile));
        for (String line : lines) {
            writer.write(line);
            writer.newLine();
        }
        writer.close();
        
        加密文件(tempFile, file);
        
        toast("已清理本群签到数据");
    } catch (Exception e) {
        toast("清理失败: " + e.getMessage());
    } finally {
        dataLock.writeLock().unlock();
    }
}

private int 获取签到用户(String qun, String qq) {
    dataLock.readLock().lock();
    try {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
        String today = sdf.format(new Date());
        
        List<String> todaySignList = new ArrayList<String>();
        
        File file = new File(signDataFileEncrypted);
        if (!file.exists()) return 1;
        
        String content = 解密文件(file);
        String[] lines = content.split("\n");
        for (String line : lines) {
            if (line.trim().isEmpty()) continue;
            String[] parts = line.trim().split("\\|");
            if (parts.length >= 5 && parts[0].equals(qun) && parts[2].equals(today)) {
                todaySignList.add(parts[1]);
            }
        }
        
        for (int i = 0; i < todaySignList.size(); i++) {
            if (todaySignList.get(i).equals(qq)) {
                return i + 1;
            }
        }
    } catch (Exception e) {}
    finally {
        dataLock.readLock().unlock();
    }
    return 1;
}

private String 读取签到开关配置(String groupId) {
    switchLock.readLock().lock();
    try {
        File file = new File(signSwitchFileEncrypted);
        if (!file.exists()) return "关";
        
        String content = 解密文件(file);
        String[] lines = content.split("\n");
        for (String line : lines) {
            if (line.trim().isEmpty()) continue;
            String[] parts = line.trim().split("=", 2);
            if (parts.length == 2 && parts[0].equals(groupId)) {
                return parts[1];
            }
        }
    } catch (Exception e) {}
    finally {
        switchLock.readLock().unlock();
    }
    return "关";
}

private void 写入签到开关配置(String groupId, String state) {
    switchLock.writeLock().lock();
    try {
        File dir = new File(appPath + "/sign");
        if (!dir.exists()) dir.mkdirs();
        
        Map<String, String> map = new HashMap<String, String>();
        File file = new File(signSwitchFileEncrypted);
        
        if (file.exists()) {
            String content = 解密文件(file);
            String[] lines = content.split("\n");
            for (String line : lines) {
                if (line.trim().isEmpty()) continue;
                String[] parts = line.trim().split("=", 2);
                if (parts.length == 2) {
                    map.put(parts[0], parts[1]);
                }
            }
        }
        
        map.put(groupId, state);
        
        File tempFile = new File(appPath + "/sign/switch_temp.txt");
        BufferedWriter writer = new BufferedWriter(new FileWriter(tempFile));
        for (Map.Entry<String, String> entry : map.entrySet()) {
            writer.write(entry.getKey() + "=" + entry.getValue());
            writer.newLine();
        }
        writer.close();
        
        加密文件(tempFile, file);
        
    } catch (Exception e) {
        toast("保存开关状态失败: " + e.getMessage());
    } finally {
        switchLock.writeLock().unlock();
    }
}

private int 获取群成员列表(String groupUin) {
    try {
        ArrayList memberList = getGroupMemberList(groupUin);
        if (memberList != null && memberList.size() > 0) {
            return memberList.size();
        }
        
        ArrayList groupList = getGroupList();
        if (groupList != null) {
            for (Object group : groupList) {
                try {
                    java.lang.reflect.Field fieldUin = group.getClass().getField("GroupUin");
                    String groupUinValue = String.valueOf(fieldUin.get(group));
                    if (groupUin.equals(groupUinValue)) {
                        try {
                            java.lang.reflect.Field fieldMemberNum = group.getClass().getField("GroupMemberNum");
                            return Integer.parseInt(String.valueOf(fieldMemberNum.get(group)));
                        } catch (Exception e1) {
                            try {
                                java.lang.reflect.Field fieldMemberCount = group.getClass().getField("MemberCount");
                                return Integer.parseInt(String.valueOf(fieldMemberCount.get(group)));
                            } catch (Exception e2) {}
                        }
                    }
                } catch (Exception e) {
                    if (group instanceof java.util.Map) {
                        java.util.Map mapGroup = (java.util.Map) group;
                        String groupUinValue = String.valueOf(mapGroup.get("GroupUin"));
                        if (groupUin.equals(groupUinValue)) {
                            Object memberNum = mapGroup.get("GroupMemberNum");
                            if (memberNum == null) memberNum = mapGroup.get("MemberNum");
                            if (memberNum == null) memberNum = mapGroup.get("memberNum");
                            if (memberNum == null) memberNum = mapGroup.get("memberCount");
                            if (memberNum == null) memberNum = mapGroup.get("MemberCount");
                            
                            if (memberNum != null) {
                                return Integer.parseInt(String.valueOf(memberNum));
                            }
                        }
                    }
                }
            }
        }
    } catch (Exception e) {}
    
    return 20;
}

private String 获取问候语() {
    java.util.Date date = new java.util.Date();
    int hour = date.getHours();
    
    if (hour >= 0 && hour < 5) {
        return "凌晨好";
    } else if (hour >= 5 && hour < 8) {
        return "早上好";
    } else if (hour >= 8 && hour < 11) {
        return "上午好";
    } else if (hour >= 11 && hour < 13) {
        return "中午好";
    } else if (hour >= 13 && hour < 18) {
        return "下午好";
    } else {
        return "晚上好";
    }
}

private int dp(int d) {
    return (int) (d * getActivity().getResources().getDisplayMetrics().density);
}

private void 延迟删除签到图片(final String filePath, long delayMillis) {
    Timer timer = new Timer();
    timer.schedule(new TimerTask() {
        public void run() {
            File file = new File(filePath);
            if (file.exists()) {
                boolean deleted = false;
                for (int i = 0; i < 10; i++) {
                    if (file.delete()) {
                        deleted = true;
                        break;
                    }
                    try { Thread.sleep(500); } catch (InterruptedException e) { e.printStackTrace(); }
                }
            }
            timer.cancel();
        }
    }, delayMillis);
}

private static final String FILE_ENCRYPT_KEY = "SignData2024Key";

private void 加密文件(File inputFile, File outputFile) {
    try {
        StringBuilder content = new StringBuilder();
        if (inputFile.exists()) {
            BufferedReader reader = new BufferedReader(new FileReader(inputFile));
            String line;
            while ((line = reader.readLine()) != null) {
                content.append(line).append("\n");
            }
            reader.close();
        }
        
        byte[] encrypted = encryptData(content.toString().getBytes("UTF-8"));
        
        FileOutputStream fos = new FileOutputStream(outputFile);
        fos.write(encrypted);
        fos.close();
        
        if (inputFile.exists()) {
            inputFile.delete();
        }
    } catch (Exception e) {}
}

private String 解密文件(File encryptedFile) {
    try {
        if (!encryptedFile.exists()) return "";
        
        FileInputStream fis = new FileInputStream(encryptedFile);
        byte[] encryptedData = new byte[(int) encryptedFile.length()];
        fis.read(encryptedData);
        fis.close();
        
        byte[] decrypted = decryptData(encryptedData);
        return new String(decrypted, "UTF-8");
    } catch (Exception e) {
        return "";
    }
}

private byte[] encryptData(byte[] data) throws Exception {
    SecretKeySpec keySpec = generateFileKey(FILE_ENCRYPT_KEY);
    Cipher cipher = Cipher.getInstance("AES/ECB/PKCS5Padding");
    cipher.init(Cipher.ENCRYPT_MODE, keySpec);
    return cipher.doFinal(data);
}

private byte[] decryptData(byte[] encryptedData) throws Exception {
    SecretKeySpec keySpec = generateFileKey(FILE_ENCRYPT_KEY);
    Cipher cipher = Cipher.getInstance("AES/ECB/PKCS5Padding");
    cipher.init(Cipher.DECRYPT_MODE, keySpec);
    return cipher.doFinal(encryptedData);
}

private SecretKeySpec generateFileKey(String key) throws Exception {
    MessageDigest sha = MessageDigest.getInstance("SHA-256");
    byte[] keyBytes = sha.digest(key.getBytes("UTF-8"));
    byte[] aesKey = new byte[16];
    System.arraycopy(keyBytes, 0, aesKey, 0, 16);
    return new SecretKeySpec(aesKey, "AES");
}

public String 获取代签配置(String groupId) {
    try {
        File file = new File(proxySignSwitchFileEncrypted);
        if (!file.exists()) return "开";
        
        String content = 解密文件(file);
        String[] lines = content.split("\n");
        for (String line : lines) {
            if (line.trim().isEmpty()) continue;
            String[] parts = line.trim().split("=", 2);
            if (parts.length == 2 && parts[0].equals(groupId)) {
                return parts[1];
            }
        }
    } catch (Exception e) {}
    return "开";
}

public void 设置代签配置(String groupId, String state) {
    try {
        File dir = new File(appPath + "/sign");
        if (!dir.exists()) dir.mkdirs();
        
        Map<String, String> map = new HashMap<String, String>();
        File file = new File(proxySignSwitchFileEncrypted);
        
        if (file.exists()) {
            String content = 解密文件(file);
            String[] lines = content.split("\n");
            for (String line : lines) {
                if (line.trim().isEmpty()) continue;
                String[] parts = line.trim().split("=", 2);
                if (parts.length == 2) {
                    map.put(parts[0], parts[1]);
                }
            }
        }
        
        map.put(groupId, state);
        
        File tempFile = new File(appPath + "/sign/proxy_switch_temp.txt");
        BufferedWriter writer = new BufferedWriter(new FileWriter(tempFile));
        for (Map.Entry<String, String> entry : map.entrySet()) {
            writer.write(entry.getKey() + "=" + entry.getValue());
            writer.newLine();
        }
        writer.close();
        
        加密文件(tempFile, file);
        
    } catch (Exception e) {}
}

public void 手动代签(String groupId, String senderQQ, String targetQQ, String senderNick) {
    if (senderQQ.equals(targetQQ)) {
        sendMsg(groupId, "", "不能为自己代签哦~");
        return;
    }
    
    String targetNick = 获取实时昵称(groupId, targetQQ);
    if (targetNick == null || targetNick.isEmpty()) {
        targetNick = targetQQ;
    }
    
    String proxySwitch = 获取代签配置(groupId);
    if (!proxySwitch.equals("开")) {
        sendMsg(groupId, "", "本群未开启代签功能~");
        return;
    }
    
    String signSwitch = 读取签到开关配置(groupId);
    if (!signSwitch.equals("开")) {
        return;
    }
    
    Map<String, Object> targetData = loadUserSignData(groupId, targetQQ);
    String lastDate = (String) targetData.get("lastDate");
    int targetPoints = (Integer) targetData.get("points");
    SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
    String today = sdf.format(new Date());
    
    if (today.equals(lastDate)) {
        sendMsg(groupId, "", "" + targetNick + " 今天已经签过到了，不能代签~");
        return;
    }
    
    int consecutive = (Integer) targetData.get("consecutive");
    int total = (Integer) targetData.get("total");
    
    if (!lastDate.isEmpty()) {
        try {
            Date lastDateObj = sdf.parse(lastDate);
            Date todayDate = sdf.parse(today);
            long diff = (todayDate.getTime() - lastDateObj.getTime()) / (1000 * 3600 * 24);
            if (diff == 1) {
                consecutive++;
            } else if (diff > 1) {
                consecutive = 1;
            }
        } catch (Exception e) {
            consecutive = 1;
        }
    } else {
        consecutive = 1;
    }
    total++;
    
    saveUserSignData(groupId, targetQQ, today, consecutive, total, targetPoints,
                 (String) targetData.get("lastLikeDate"), 
                 (Integer) targetData.get("likeAttempts"), 
                 (Integer) targetData.get("makeupCards"), 
                 (String) targetData.get("lastMakeupDate"),
                 (Integer) targetData.get("titleCard"), 
                 (Integer) targetData.get("FiveCard"), 
                 (Integer) targetData.get("luckyCard"),
                 (Integer) targetData.get("tenfoldCard"),
                 (Integer) targetData.get("doubleCard"),
                 (Integer) targetData.get("FiveCardActive"),
                 (Integer) targetData.get("tenfoldCardActive"),
                 (Integer) targetData.get("doubleCardActive"),
                 0, "",
                 0, 0, 0);
    
    String msg = "代签成功\n" +
                 "代签人: " + senderNick + "\n" +
                 "被代签人: " + targetNick;
    sendMsg(groupId, "", msg);
    
    final String finalGroupId = groupId;
    final String finalTargetQQ = targetQQ;
    final int finalConsecutive = consecutive;
    final int finalTotal = total;
    final int finalTargetPoints = targetPoints;
    
    new Thread(new Runnable() {
        public void run() {
            try {
                String originPath = 下载签到底图(finalGroupId, finalTargetQQ);
                if (originPath == null) {
            originPath = 获取默认底图(finalGroupId, finalTargetQQ);
            
            if (originPath == null) {
                String defaultPath = 获取签到背景(finalGroupId, finalTargetQQ);
                boolean created = 设置默认背景(defaultPath);
            if (created) {
            originPath = defaultPath;
                } else {
            sendMsg(finalGroupId, "", "图片下载失败，请稍后重试");
            return;
                }
            }
        }
                
                String nickname = 获取实时昵称(finalGroupId, finalTargetQQ);
                if (nickname == null || nickname.isEmpty()) {
                    nickname = finalTargetQQ;
                }
                
                String checkinPath = 获取签到图片(finalGroupId, finalTargetQQ);
                int signOrder = 获取签到用户(finalGroupId, finalTargetQQ);
                int totalMembers = 获取群成员列表(finalGroupId);
                
                boolean success = 绘制签到图片(finalGroupId, finalTargetQQ, nickname, finalConsecutive, finalTotal, originPath, checkinPath, signOrder, totalMembers, 0, finalTargetPoints);
                
                if (success) {
                    发送图片消息(finalGroupId, checkinPath);
                    延迟删除签到图片(checkinPath, 10000);
                } else {
                    sendMsg(finalGroupId, "", "生成签到卡片失败");
                }
            } catch (Exception e) {
                sendMsg(finalGroupId, "", "生成签到卡片异常: " + e.getMessage());
            }
        }
    }).start();
}

public void 代签指令响应(MessageData msg) {
    try {
        String text = msg.MessageContent;
        String senderQQ = msg.UserUin;
        String groupId = msg.GroupUin;
        
        if (!msg.IsGroup || text == null) return;
        
        String lowerText = text.toLowerCase().trim();
        
        if (!lowerText.equals("代签") && !lowerText.startsWith("代签 @") && !lowerText.startsWith("代签@")) {
            return;
        }
        
        String proxySwitch = 获取代签配置(groupId);
        if (!proxySwitch.equals("开")) {
            sendMsg(groupId, "", "本群代签功能已关闭~");
            return;
        }
        
        String targetQQ = null;
        
        if (msg.mAtList != null && !msg.mAtList.isEmpty()) {
            Object atObj = msg.mAtList.get(0);
            if (atObj != null) {
                targetQQ = atObj.toString();
            }
        }
        
        if (targetQQ == null || targetQQ.isEmpty()) {
            java.util.regex.Pattern p = java.util.regex.Pattern.compile("@(\\d+)");
            java.util.regex.Matcher m = p.matcher(text);
            if (m.find()) {
                targetQQ = m.group(1);
            }
        }
        
        if (targetQQ == null || targetQQ.isEmpty()) {
            sendMsg(groupId, "", "请 @ 要代签的人");
            return;
        }
        
        String senderNick = 获取实时昵称(groupId, senderQQ);
        if (senderNick == null || senderNick.isEmpty()) senderNick = senderQQ;
        
        手动代签(groupId, senderQQ, targetQQ, senderNick);
        
    } catch (Exception e) {
        sendMsg(msg.GroupUin, "", "代签失败: " + e.getMessage());
    }
}

private void 抽取盲盒方法(String groupId, String qq) {
    Map<String, Object> userData = loadUserSignData(groupId, qq);
    int points = (Integer) userData.get("points");
    
    sendMsg(groupId, "", "━━━━━━━━━━━━━━\n" +
                         "抽盲盒\n" +
                         "━━━━━━━━━━━━━━\n" +
                         "当前积分：" + points + "\n" +
                         "━━━━━━━━━━━━━━\n" +
                         "1. 初级盲盒（30积分）\n" +
                         "2. 中级盲盒（100积分）\n" +
                         "3. 高级盲盒（200积分）\n" +
                         "━━━━━━━━━━━━━━\n" +
                         "请发送数字选择（60秒内未选择将自动取消）");
    
    String sessionKey = "draw_" + groupId + "_" + qq;
    likeSessionMap.put(sessionKey, "waiting");
    开始绘画计时(sessionKey, groupId, qq);
}

private void 开始绘画计时(final String sessionKey, final String groupId, final String senderQQ) {
    取消点赞计时(sessionKey);
    
    final java.util.Timer timer = new java.util.Timer();
    TimerTask task = new TimerTask() {
        public void run() {
            try {
                if (likeSessionMap.containsKey(sessionKey)) {
                    likeSessionMap.remove(sessionKey);
                    sendMsg(groupId, "", "抽盲盒操作已超时取消，请重新发送");
                }
            } catch (Exception e) {}
            likeTimerTasks.remove(sessionKey);
            timer.cancel();
        }
    };
    likeTimerTasks.put(sessionKey, task);
    timer.schedule(task, 60000);
}

private void 绘画盲盒选择(String groupId, String qq, int choice) {
    Map<String, Object> userData = loadUserSignData(groupId, qq);
    int points = (Integer) userData.get("points");
    
    int cost = 0;
    String boxType = "";
    int boxLevel = 0;
    
    if (choice == 1) {
        cost = 30;
        boxType = "初级盲盒";
        boxLevel = 1;
    } else if (choice == 2) {
        cost = 100;
        boxType = "中级盲盒";
        boxLevel = 2;
    } else if (choice == 3) {
        cost = 200;
        boxType = "高级盲盒";
        boxLevel = 3;
    } else {
        sendMsg(groupId, "", "无效选择");
        return;
    }
    
    if (points < cost) {
        sendMsg(groupId, "", "积分不足" + cost + "，无法购买" + boxType + "\n当前积分：" + points);
        return;
    }
    
    int newPoints = points - cost;
    
    String result = 打开盲盒并保存奖励(groupId, qq, boxLevel, boxType, newPoints, userData);
    sendMsg(groupId, "", "购买并开启" + boxType + "\n" + result);
}

private void 开启盲盒方法(String groupId, String qq) {
    Map<String, Object> userData = loadUserSignData(groupId, qq);
    int normalBox = (Integer) userData.get("normalBox");
    int mediumBox = (Integer) userData.get("mediumBox");
    int advancedBox = (Integer) userData.get("advancedBox");
    
    sendMsg(groupId, "", "━━━━━━━━━━━━━━\n" +
                         "开盲盒\n" +
                         "━━━━━━━━━━━━━━\n" +
                         "1. 初级盲盒（剩余：" + normalBox + "个）\n" +
                         "2. 中级盲盒（剩余：" + mediumBox + "个）\n" +
                         "3. 高级盲盒（剩余：" + advancedBox + "个）\n" +
                         "━━━━━━━━━━━━━━\n" +
                         "请发送数字选择（60秒内未选择将自动取消）");
    
    String sessionKey = "open_" + groupId + "_" + qq;
    likeSessionMap.put(sessionKey, "waiting");
    开始盲盒计时(sessionKey, groupId, qq);
}

private void 开始盲盒计时(final String sessionKey, final String groupId, final String senderQQ) {
    取消点赞计时(sessionKey);
    
    final java.util.Timer timer = new java.util.Timer();
    TimerTask task = new TimerTask() {
        public void run() {
            try {
                if (likeSessionMap.containsKey(sessionKey)) {
                    likeSessionMap.remove(sessionKey);
                    sendMsg(groupId, "", "开盲盒操作已超时取消，请重新发送");
                }
            } catch (Exception e) {}
            likeTimerTasks.remove(sessionKey);
            timer.cancel();
        }
    };
    likeTimerTasks.put(sessionKey, task);
    timer.schedule(task, 60000);
}

private void 选择开启盲盒(String groupId, String qq, int choice) {
    Map<String, Object> userData = loadUserSignData(groupId, qq);
    int normalBox = (Integer) userData.get("normalBox");
    int mediumBox = (Integer) userData.get("mediumBox");
    int advancedBox = (Integer) userData.get("advancedBox");
    
    int boxCount = 0;
    String boxType = "";
    int boxLevel = 0;
    int newNormalBox = normalBox;
    int newMediumBox = mediumBox;
    int newAdvancedBox = advancedBox;
    
    if (choice == 1) {
        boxCount = normalBox;
        boxType = "初级盲盒";
        boxLevel = 1;
        if (boxCount > 0) newNormalBox = normalBox - 1;
    } else if (choice == 2) {
        boxCount = mediumBox;
        boxType = "中级盲盒";
        boxLevel = 2;
        if (boxCount > 0) newMediumBox = mediumBox - 1;
    } else if (choice == 3) {
        boxCount = advancedBox;
        boxType = "高级盲盒";
        boxLevel = 3;
        if (boxCount > 0) newAdvancedBox = advancedBox - 1;
    } else {
        sendMsg(groupId, "", "无效选择");
        return;
    }
    
    if (boxCount <= 0) {
        sendMsg(groupId, "", "没有" + boxType + "，请先去「抽盲盒」获取~");
        return;
    }
    
    int currentPoints = (Integer) userData.get("points");
    
    String result = 开启盲盒并保存奖励方法(groupId, qq, boxLevel, boxType, currentPoints, userData, 
                                                     newNormalBox, newMediumBox, newAdvancedBox);
    sendMsg(groupId, "", "开启" + boxType + "\n" + result);
}

private String 打开盲盒并保存奖励(String groupId, String qq, int boxLevel, String boxType, 
                                      int currentPoints, Map<String, Object> userData) {
    return 打开盲盒方法(groupId, qq, boxLevel, boxType, currentPoints, userData,
                       (Integer) userData.get("normalBox"),
                       (Integer) userData.get("mediumBox"),
                       (Integer) userData.get("advancedBox"));
}

private String 开启盲盒并保存奖励方法(String groupId, String qq, int boxLevel, String boxType,
                                               int currentPoints, Map<String, Object> userData,
                                               int newNormalBox, int newMediumBox, int newAdvancedBox) {
    return 打开盲盒方法(groupId, qq, boxLevel, boxType, currentPoints, userData,
                       newNormalBox, newMediumBox, newAdvancedBox);
}

private String 打开盲盒方法(String groupId, String qq, int boxLevel, String boxType,
                           int currentPoints, Map<String, Object> userData,
                           int startNormalBox, int startMediumBox, int startAdvancedBox) {
    int maxOpens = 20;
    int maxAgain = 10;
    int totalRewardValue = 0;
    StringBuilder rewardLog = new StringBuilder();
    int openCount = 0;
    int againCount = 0;
    
    String lastDate = (String) userData.get("lastDate");
    int consecutive = (Integer) userData.get("consecutive");
    int total = (Integer) userData.get("total");
    String lastLikeDate = (String) userData.get("lastLikeDate");
    int likeAttempts = (Integer) userData.get("likeAttempts");
    String lastMakeupDate = (String) userData.get("lastMakeupDate");
    int FiveCardActive = (Integer) userData.get("FiveCardActive");
    int tenfoldCardActive = (Integer) userData.get("tenfoldCardActive");
    int doubleCardActive = (Integer) userData.get("doubleCardActive");
    int monthlyBuyCount = (Integer) userData.get("monthlyBuyCount");
    String lastBuyMonth = (String) userData.get("lastBuyMonth");
    
    int finalPoints = currentPoints;
    int finalMakeupCards = (Integer) userData.get("makeupCards");
    int finalTitleCard = (Integer) userData.get("titleCard");
    int finalFiveCard = (Integer) userData.get("FiveCard");
    int finalLuckyCard = (Integer) userData.get("luckyCard");
    int finalTenfoldCard = (Integer) userData.get("tenfoldCard");
    int finalDoubleCard = (Integer) userData.get("doubleCard");
    int finalNormalBox = startNormalBox;
    int finalMediumBox = startMediumBox;
    int finalAdvancedBox = startAdvancedBox;
    
    int currentBoxLevel = boxLevel;
    
    while (openCount < maxOpens && againCount < maxAgain) {
        openCount++;
        
        int random = (int)(Math.random() * 100);
        String rewardMsg = "";
        int addPoints = 0;
        int addMakeupCards = 0;
        int addTitleCard = 0;
        int addFiveCard = 0;
        int addLuckyCard = 0;
        int addTenfoldCard = 0;
        int addDoubleCard = 0;
        int addNormalBox = 0;
        int addMediumBox = 0;
        int addAdvancedBox = 0;
        boolean again = false;
        
        if (currentBoxLevel == 1) {
            if (random < 30) {
                addPoints = 1 + (int)(Math.random() * 10);
                rewardMsg = "获得 " + addPoints + " 积分";
            } else if (random < 50) {
                addPoints = 11 + (int)(Math.random() * 10);
                rewardMsg = "获得 " + addPoints + " 积分";
            } else if (random < 60) {
                addPoints = 21 + (int)(Math.random() * 10);
                rewardMsg = "获得 " + addPoints + " 积分";
            } else if (random < 65) {
                addPoints = 31 + (int)(Math.random() * 10);
                rewardMsg = "获得 " + addPoints + " 积分";
            } else if (random < 72) {
                addMakeupCards = 1;
                rewardMsg = "获得 补签卡 x1";
            } else if (random < 78) {
                addNormalBox = 1;
                rewardMsg = "获得 初级盲盒 x1（已存入道具）";
            } else if (random < 88) {
                again = true;
                rewardMsg = "运气爆棚！获得再开一次！";
            } else {
                rewardMsg = "空盒子... 什么也没有";
            }
        } else if (currentBoxLevel == 2) {
            if (random < 20) {
                addPoints = 20 + (int)(Math.random() * 31);
                rewardMsg = "获得 " + addPoints + " 积分";
            } else if (random < 38) {
                addPoints = 51 + (int)(Math.random() * 30);
                rewardMsg = "获得 " + addPoints + " 积分";
            } else if (random < 50) {
                addPoints = 81 + (int)(Math.random() * 20);
                rewardMsg = "获得 " + addPoints + " 积分";
            } else if (random < 58) {
                addPoints = 101 + (int)(Math.random() * 50);
                rewardMsg = "获得 " + addPoints + " 积分";
            } else if (random < 65) {
                addMakeupCards = 2;
                rewardMsg = "获得 补签卡 x2";
            } else if (random < 70) {
                addTitleCard = 1;
                rewardMsg = "获得 头衔卡 x1";
            } else if (random < 74) {
                addFiveCard = 1;
                rewardMsg = "获得 五倍卡 x1";
            } else if (random < 78) {
                addNormalBox = 2;
                rewardMsg = "获得 初级盲盒 x2（已存入道具）";
            } else if (random < 82) {
                addMediumBox = 1;
                rewardMsg = "获得 中级盲盒 x1（已存入道具）";
            } else if (random < 90) {
                again = true;
                rewardMsg = "运气爆棚！获得再开一次！";
            } else {
                rewardMsg = "空盒子... 什么也没有";
            }
        } else {
            if (random < 15) {
                addPoints = 50 + (int)(Math.random() * 51);
                rewardMsg = "获得 " + addPoints + " 积分";
            } else if (random < 28) {
                addPoints = 101 + (int)(Math.random() * 50);
                rewardMsg = "获得 " + addPoints + " 积分";
            } else if (random < 38) {
                addPoints = 151 + (int)(Math.random() * 50);
                rewardMsg = "获得 " + addPoints + " 积分";
            } else if (random < 46) {
                addPoints = 201 + (int)(Math.random() * 100);
                rewardMsg = "获得 " + addPoints + " 积分";
            } else if (random < 53) {
                addMakeupCards = 3;
                rewardMsg = "获得 补签卡 x3";
            } else if (random < 58) {
                addMakeupCards = 5;
                rewardMsg = "获得 补签卡 x5";
            } else if (random < 63) {
                addTitleCard = 1;
                rewardMsg = "获得 头衔卡 x1";
            } else if (random < 67) {
                addFiveCard = 2;
                rewardMsg = "获得 五倍卡 x2";
            } else if (random < 70) {
                addLuckyCard = 1;
                rewardMsg = "获得 幸运卡 x1";
            } else if (random < 73) {
                addNormalBox = 3;
                rewardMsg = "获得 初级盲盒 x3（已存入道具）";
            } else if (random < 76) {
                addMediumBox = 2;
                rewardMsg = "获得 中级盲盒 x2（已存入道具）";
            } else if (random < 79) {
                addAdvancedBox = 1;
                rewardMsg = "获得 高级盲盒 x1（已存入道具）";
            } else if (random < 83) {
                addPoints = 500;
                rewardMsg = "欧皇！获得 500 积分！";
            } else if (random < 92) {
                again = true;
                rewardMsg = "运气爆棚！获得再开一次！";
            } else {
                rewardMsg = "空盒子... 什么也没有";
            }
        }
        
        if (addPoints > 0) {
            totalRewardValue += addPoints;
            finalPoints += addPoints;
        }
        if (addMakeupCards > 0) {
            totalRewardValue += addMakeupCards * 50;
            finalMakeupCards += addMakeupCards;
        }
        if (addTitleCard > 0) {
            totalRewardValue += addTitleCard * 200;
            finalTitleCard += addTitleCard;
        }
        if (addFiveCard > 0) {
            totalRewardValue += addFiveCard * 30;
            finalFiveCard += addFiveCard;
        }
        if (addLuckyCard > 0) {
            totalRewardValue += addLuckyCard * 30;
            finalLuckyCard += addLuckyCard;
        }
        if (addTenfoldCard > 0) {
            totalRewardValue += addTenfoldCard * 60;
            finalTenfoldCard += addTenfoldCard;
        }
        if (addDoubleCard > 0) {
            totalRewardValue += addDoubleCard * 10;
            finalDoubleCard += addDoubleCard;
        }
        if (addNormalBox > 0) {
            totalRewardValue += addNormalBox * 30;
            finalNormalBox += addNormalBox;
        }
        if (addMediumBox > 0) {
            totalRewardValue += addMediumBox * 100;
            finalMediumBox += addMediumBox;
        }
        if (addAdvancedBox > 0) {
            totalRewardValue += addAdvancedBox * 200;
            finalAdvancedBox += addAdvancedBox;
        }
        
        rewardLog.append(openCount).append("、").append(rewardMsg).append("\n");
        
        if (again) {
            againCount++;
            continue;
        } else {
            break;
        }
    }
    
    saveUserSignData(groupId, qq, lastDate, consecutive, total, finalPoints,
                     lastLikeDate, likeAttempts, finalMakeupCards, lastMakeupDate,
                     finalTitleCard, finalFiveCard, finalLuckyCard, finalTenfoldCard, finalDoubleCard,
                     FiveCardActive, tenfoldCardActive, doubleCardActive,
                     monthlyBuyCount, lastBuyMonth,
                     finalNormalBox, finalMediumBox, finalAdvancedBox);
    
    String boxIcon = boxLevel == 1 ? " " : (boxLevel == 2 ? " " : " ");
    String result = "━━━━━━━━━━━━━━\n" +
                    boxIcon + " " + boxType + " 开启结果\n" +
                    "━━━━━━━━━━━━━━\n" +
                    rewardLog.toString() +
                    "━━━━━━━━━━━━━━\n" +
                    "获得价值：" + totalRewardValue + "积分\n" +
                    "当前积分：" + finalPoints + "\n" +
                    "━━━━━━━━━━━━━━";
    return result;
}

private void 发送拼手气红包(String groupId, String senderQQ, String text) {
    try {
        String[] parts = text.trim().split("\\s+");
        if (parts.length < 3) {
            sendMsg(groupId, "", "格式错误！\n正确格式：包红包 个数 积分");
            return;
        }
        
        int count = 0;
        int totalPoints = 0;
        
        try {
            count = Integer.parseInt(parts[1]);
            totalPoints = Integer.parseInt(parts[2]);
        } catch (Exception e) {
            sendMsg(groupId, "", "请输入有效的数字");
            return;
        }
        
        if (count <= 0 || totalPoints <= 0) {
            sendMsg(groupId, "", "红包个数和积分必须大于0");
            return;
        }
        
        if (count > 50) {
            sendMsg(groupId, "", "红包个数最多为50个");
            return;
        }
        
        if (totalPoints < count) {
            sendMsg(groupId, "", "总积分不能小于红包个数");
            return;
        }
        
        Map<String, Object> userData = loadUserSignData(groupId, senderQQ);
        int senderPoints = (Integer) userData.get("points");
        
        if (senderPoints < totalPoints) {
            sendMsg(groupId, "", "积分不足！\n当前积分：" + senderPoints + "\n需要积分：" + totalPoints);
            return;
        }
        
        int newSenderPoints = senderPoints - totalPoints;
        saveUserSignData(groupId, senderQQ,
            (String) userData.get("lastDate"),
            (Integer) userData.get("consecutive"),
            (Integer) userData.get("total"),
            newSenderPoints,
            (String) userData.get("lastLikeDate"),
            (Integer) userData.get("likeAttempts"),
            (Integer) userData.get("makeupCards"),
            (String) userData.get("lastMakeupDate"),
            (Integer) userData.get("titleCard"),
            (Integer) userData.get("FiveCard"),
            (Integer) userData.get("luckyCard"),
            (Integer) userData.get("tenfoldCard"),
            (Integer) userData.get("doubleCard"),
            (Integer) userData.get("FiveCardActive"),
            (Integer) userData.get("tenfoldCardActive"),
            (Integer) userData.get("doubleCardActive"),
            (Integer) userData.get("monthlyBuyCount"),
            (String) userData.get("lastBuyMonth"),
            (Integer) userData.get("normalBox"),
            (Integer) userData.get("mediumBox"),
            (Integer) userData.get("advancedBox"));
        
        String groupShort = groupId.length() >= 4 ? groupId.substring(groupId.length() - 4) : groupId;
        String redPacketId = groupShort + "_" + (redPacketSeq++);
        
        List<Integer> pointList = new ArrayList<Integer>();
        if (totalPoints == count) {
            for (int i = 0; i < count; i++) pointList.add(1);
        } else {
            java.util.Random random = new java.util.Random();
            int remaining = totalPoints;
            int remainingCount = count;
            
            for (int i = 0; i < count - 1; i++) {
                int avg = remaining / remainingCount;
                int max = avg * 2;
                
                if (max > remaining - (remainingCount - 1)) {
                    max = remaining - (remainingCount - 1);
                }
                
                int p;
                if (max > 1) {
                    p = random.nextInt(max - 1) + 1;
                } else {
                    p = 1;
                }
                
                pointList.add(p);
                remaining -= p;
                remainingCount--;
            }
            pointList.add(remaining);
            
            Collections.shuffle(pointList);
        }
        
        Map<String, Object> packet = new HashMap<String, Object>();
        packet.put("id", redPacketId);
        packet.put("groupId", groupId);
        packet.put("sender", senderQQ);
        packet.put("total", totalPoints);
        packet.put("count", count);
        packet.put("remaining", count);
        packet.put("points", pointList);
        packet.put("grabbed", new ArrayList<String>());
        packet.put("grabbedPoints", new ArrayList<Integer>());
        packet.put("createTime", System.currentTimeMillis());
        packet.put("status", "active");
        
        redPacketDataMap.put(redPacketId, packet);
        我的红包();
        
        String senderNick = 获取实时昵称(groupId, senderQQ);
        if (senderNick == null || senderNick.isEmpty()) senderNick = senderQQ;
        
        String imagePath = 绘制拼手气红包图片(groupId, senderNick, totalPoints, count, redPacketId);
        if (imagePath != null) {
            发送图片消息(groupId, imagePath);
            延迟删除签到图片(imagePath, 15000);
        }
        
        sendMsg(groupId, "", "抢红包 红包ID：" + redPacketId);
        
        开始红包倒计时(redPacketId, 3600000);
        
    } catch (Exception e) {
        sendMsg(groupId, "", "发红包失败：" + e.getMessage());
    }
}

private void 发送专属红包(String groupId, String senderQQ, String text, MessageData msg) {
    try {
        java.util.regex.Pattern pointPattern = java.util.regex.Pattern.compile("发红包\\s*(\\d+)\\s*@");
        java.util.regex.Matcher pointMatcher = pointPattern.matcher(text);
        
        int points = 0;
        if (pointMatcher.find()) {
            points = Integer.parseInt(pointMatcher.group(1));
        } else {
            java.util.regex.Pattern p2 = java.util.regex.Pattern.compile("发红包\\s*@\\S+\\s*(\\d+)");
            java.util.regex.Matcher m2 = p2.matcher(text);
            if (m2.find()) {
                points = Integer.parseInt(m2.group(1));
            } else {
                sendMsg(groupId, "", "格式错误！正确格式：发红包@XXX 积分数");
                return;
            }
        }
        if (points < 0) {
            sendMsg(groupId, "", "想抢钱？");
            return;
        }
        
        if (points = 0) {
            sendMsg(groupId, "", "泥搁这包空气呢？");
            return;
        }
        
        String targetQQ = null;
        
        if (msg.mAtList != null && !msg.mAtList.isEmpty()) {
            Object atObj = msg.mAtList.get(0);
            if (atObj != null) {
                targetQQ = atObj.toString();
            }
        }
        
        if (targetQQ == null || targetQQ.isEmpty()) {
            java.util.regex.Pattern p = java.util.regex.Pattern.compile("@(\\d+)");
            java.util.regex.Matcher m = p.matcher(text);
            if (m.find()) {
                targetQQ = m.group(1);
            }
        }
        
        if (targetQQ == null || targetQQ.isEmpty()) {
            sendMsg(groupId, "", "请选择接收用户");
            return;
        }
        
        if (senderQQ.equals(targetQQ)) {
            sendMsg(groupId, "", "不能给自己发红包哦~");
            return;
        }
        
        Map<String, Object> senderData = loadUserSignData(groupId, senderQQ);
        int senderPoints = (Integer) senderData.get("points");
        
        if (senderPoints < points) {
            sendMsg(groupId, "", "积分不足！需要积分：" + points);
            return;
        }
        
        int newSenderPoints = senderPoints - points;
        saveUserSignData(groupId, senderQQ,
            (String) senderData.get("lastDate"),
            (Integer) senderData.get("consecutive"),
            (Integer) senderData.get("total"),
            newSenderPoints,
            (String) senderData.get("lastLikeDate"),
            (Integer) senderData.get("likeAttempts"),
            (Integer) senderData.get("makeupCards"),
            (String) senderData.get("lastMakeupDate"),
            (Integer) senderData.get("titleCard"),
            (Integer) senderData.get("FiveCard"),
            (Integer) senderData.get("luckyCard"),
            (Integer) senderData.get("tenfoldCard"),
            (Integer) senderData.get("doubleCard"),
            (Integer) senderData.get("FiveCardActive"),
            (Integer) senderData.get("tenfoldCardActive"),
            (Integer) senderData.get("doubleCardActive"),
            (Integer) senderData.get("monthlyBuyCount"),
            (String) senderData.get("lastBuyMonth"),
            (Integer) senderData.get("normalBox"),
            (Integer) senderData.get("mediumBox"),
            (Integer) senderData.get("advancedBox"));
        
        Map<String, Object> targetData = loadUserSignData(groupId, targetQQ);
        int newTargetPoints = (Integer) targetData.get("points") + points;
        
        saveUserSignData(groupId, targetQQ,
            (String) targetData.get("lastDate"),
            (Integer) targetData.get("consecutive"),
            (Integer) targetData.get("total"),
            newTargetPoints,
            (String) targetData.get("lastLikeDate"),
            (Integer) targetData.get("likeAttempts"),
            (Integer) targetData.get("makeupCards"),
            (String) targetData.get("lastMakeupDate"),
            (Integer) targetData.get("titleCard"),
            (Integer) targetData.get("FiveCard"),
            (Integer) targetData.get("luckyCard"),
            (Integer) targetData.get("tenfoldCard"),
            (Integer) targetData.get("doubleCard"),
            (Integer) targetData.get("FiveCardActive"),
            (Integer) targetData.get("tenfoldCardActive"),
            (Integer) targetData.get("doubleCardActive"),
            (Integer) targetData.get("monthlyBuyCount"),
            (String) targetData.get("lastBuyMonth"),
            (Integer) targetData.get("normalBox"),
            (Integer) targetData.get("mediumBox"),
            (Integer) targetData.get("advancedBox"));
            
        String senderNick = 获取实时昵称(groupId, senderQQ);
        if (senderNick == null || senderNick.isEmpty()) senderNick = senderQQ;
        
        String targetNick = 获取实时昵称(groupId, targetQQ);
        if (targetNick == null || targetNick.isEmpty()) targetNick = targetQQ;
        
        String groupShort = groupId.length() >= 4 ? groupId.substring(groupId.length() - 4) : groupId;
        String redPacketId = "ex_" + groupShort + "_" + (redPacketSeq++);
        
        String imagePath = 绘制专属红包图片(groupId, senderNick, targetNick, points, redPacketId);
        if (imagePath != null) {
            发送图片消息(groupId, imagePath);
            延迟删除签到图片(imagePath, 15000);
        }
        
    } catch (Exception e) {
        sendMsg(groupId, "", "发送专属红包失败：" + e.getMessage());
    }
}

private void 抢红包(String groupId, String grabberQQ, String text) {
    try {
        String redPacketId = text.replace("抢红包", "").trim();
        redPacketId = redPacketId.replace("红包ID：", "").replace("红包ID:", "").replace("：", ":").trim();
        
        if (redPacketId.isEmpty()) {
            sendMsg(groupId, "", "请输入红包ID\n格式：抢红包 红包ID：XXXX");
            return;
        }
        
        redPacketLock.writeLock().lock();
        try {
            Map<String, Object> packet = redPacketDataMap.get(redPacketId);
            if (packet == null) {
                sendMsg(groupId, "", "红包不存在或已过期");
                return;
            }
            
            String status = (String) packet.get("status");
            if (!"active".equals(status)) {
                sendMsg(groupId, "", "红包已过期或已领完");
                return;
            }
            
            List<String> grabbed = (List<String>) packet.get("grabbed");
            if (grabbed.contains(grabberQQ)) {
                sendMsg(groupId, "", "你已经抢过这个红包了");
                return;
            }
            
            int remaining = (Integer) packet.get("remaining");
            if (remaining <= 0) {
                packet.put("status", "finished");
                sendMsg(groupId, "", "红包已被领完");
                return;
            }
            
            List<Integer> points = (List<Integer>) packet.get("points");
            int grabPoints = points.get(points.size() - remaining);
            
            grabbed.add(grabberQQ);
            ((List<Integer>) packet.get("grabbedPoints")).add(grabPoints);
            packet.put("remaining", remaining - 1);
            
            if (remaining - 1 <= 0) {
                packet.put("status", "finished");
                取消红包计时(redPacketId);
            }
            
            我的红包();
            
            Map<String, Object> userData = loadUserSignData(groupId, grabberQQ);
            int newPoints = (Integer) userData.get("points") + grabPoints;
            
            saveUserSignData(groupId, grabberQQ,
                (String) userData.get("lastDate"),
                (Integer) userData.get("consecutive"),
                (Integer) userData.get("total"),
                newPoints,
                (String) userData.get("lastLikeDate"),
                (Integer) userData.get("likeAttempts"),
                (Integer) userData.get("makeupCards"),
                (String) userData.get("lastMakeupDate"),
                (Integer) userData.get("titleCard"),
                (Integer) userData.get("FiveCard"),
                (Integer) userData.get("luckyCard"),
                (Integer) userData.get("tenfoldCard"),
                (Integer) userData.get("doubleCard"),
                (Integer) userData.get("FiveCardActive"),
                (Integer) userData.get("tenfoldCardActive"),
                (Integer) userData.get("doubleCardActive"),
                (Integer) userData.get("monthlyBuyCount"),
                (String) userData.get("lastBuyMonth"),
                (Integer) userData.get("normalBox"),
                (Integer) userData.get("mediumBox"),
                (Integer) userData.get("advancedBox"));
            
            String grabberNick = 获取实时昵称(groupId, grabberQQ);
            if (grabberNick == null || grabberNick.isEmpty()) grabberNick = grabberQQ;
            
            sendMsg(groupId, "", "🎉 " + grabberNick + "抢到了" + grabPoints + "积分！");
            
            if (remaining - 1 <= 0) {
                String senderQQ = (String) packet.get("sender");
                String senderNick = 获取实时昵称(groupId, senderQQ);
                if (senderNick == null || senderNick.isEmpty()) senderNick = senderQQ;
                sendMsg(groupId, "", senderNick + "的红包已被领完！");
            }
            
        } finally {
            redPacketLock.writeLock().unlock();
        }
        
    } catch (Exception e) {
        sendMsg(groupId, "", "抢红包失败：" + e.getMessage());
    }
}

private void 开始红包倒计时(final String redPacketId, long delayMillis) {
    取消红包计时(redPacketId);
    
    Timer timer = new Timer();
    TimerTask task = new TimerTask() {
        public void run() {
            redPacketLock.writeLock().lock();
            try {
                Map<String, Object> packet = redPacketDataMap.get(redPacketId);
                if (packet != null && "active".equals(packet.get("status"))) {
                    packet.put("status", "expired");
                    过期红包返回(packet);
                    我的红包();
                }
            } catch (Exception e) {
            } finally {
                redPacketLock.writeLock().unlock();
            }
            redPacketTimerTasks.remove(redPacketId);
        }
    };
    redPacketTimerTasks.put(redPacketId, task);
    timer.schedule(task, delayMillis);
}

private void 取消红包计时(String redPacketId) {
    TimerTask task = redPacketTimerTasks.remove(redPacketId);
    if (task != null) task.cancel();
}

private void 我的红包() {
    redPacketLock.readLock().lock();
    try {
        File file = new File(redPacketDataFile);
        File dir = file.getParentFile();
        if (!dir.exists()) dir.mkdirs();
        
        StringBuilder sb = new StringBuilder();
        for (Map.Entry<String, Map<String, Object>> entry : redPacketDataMap.entrySet()) {
            Map<String, Object> p = entry.getValue();
            sb.append(p.get("id")).append("|");
            sb.append(p.get("groupId")).append("|");
            sb.append(p.get("sender")).append("|");
            sb.append(p.get("total")).append("|");
            sb.append(p.get("count")).append("|");
            sb.append(p.get("remaining")).append("|");
            sb.append(p.get("createTime")).append("|");
            sb.append(p.get("status")).append("|");
            
            List<Integer> points = (List<Integer>) p.get("points");
            for (int i = 0; i < points.size(); i++) {
                sb.append(points.get(i));
                if (i < points.size() - 1) sb.append(",");
            }
            sb.append("|");
            
            List<String> grabbed = (List<String>) p.get("grabbed");
            for (int i = 0; i < grabbed.size(); i++) {
                sb.append(grabbed.get(i));
                if (i < grabbed.size() - 1) sb.append(",");
            }
            sb.append("|");
            
            List<Integer> grabbedPoints = (List<Integer>) p.get("grabbedPoints");
            for (int i = 0; i < grabbedPoints.size(); i++) {
                sb.append(grabbedPoints.get(i));
                if (i < grabbedPoints.size() - 1) sb.append(",");
            }
            sb.append("\n");
        }
        
        保存数据到文件(sb.toString(), file);
    } catch (Exception e) {} finally {
        redPacketLock.readLock().unlock();
    }
}

private void 加载红包数据() {
    redPacketLock.writeLock().lock();
    try {
        File file = new File(redPacketDataFile);
        if (!file.exists()) return;
        
        String content = 从文件获取数据(file);
        if (content == null || content.isEmpty()) return;
        
        String[] lines = content.split("\n");
        long now = System.currentTimeMillis();
        
        for (String line : lines) {
            if (line.trim().isEmpty()) continue;
            String[] parts = line.split("\\|");
            if (parts.length < 11) continue;
            
            String status = parts[7];
            long createTime = Long.parseLong(parts[6]);
            
            if (now - createTime > 86400000) continue;
            
            Map<String, Object> packet = new HashMap<String, Object>();
            packet.put("id", parts[0]);
            packet.put("groupId", parts[1]);
            packet.put("sender", parts[2]);
            packet.put("total", Integer.parseInt(parts[3]));
            packet.put("count", Integer.parseInt(parts[4]));
            packet.put("remaining", Integer.parseInt(parts[5]));
            packet.put("createTime", createTime);
            packet.put("status", status);
            
            List<Integer> points = new ArrayList<Integer>();
            if (!parts[8].isEmpty()) {
                for (String s : parts[8].split(",")) {
                    points.add(Integer.parseInt(s));
                }
            }
            packet.put("points", points);
            
            List<String> grabbed = new ArrayList<String>();
            if (parts.length > 9 && !parts[9].isEmpty()) {
                for (String s : parts[9].split(",")) {
                    grabbed.add(s);
                }
            }
            packet.put("grabbed", grabbed);
            
            List<Integer> grabbedPoints = new ArrayList<Integer>();
            if (parts.length > 10 && !parts[10].isEmpty()) {
                for (String s : parts[10].split(",")) {
                    grabbedPoints.add(Integer.parseInt(s));
                }
            }
            packet.put("grabbedPoints", grabbedPoints);
            
            redPacketDataMap.put(parts[0], packet);
            
            if ("active".equals(status)) {
                long elapsed = now - createTime;
                long remaining = 3600000 - elapsed;
                if (remaining > 0) {
                    开始红包倒计时(parts[0], remaining);
                } else {
                    packet.put("status", "expired");
                    过期红包返回(packet);
                    我的红包();
                }
            }
        }
    } catch (Exception e) {
        sendMsg("", "", "加载红包数据异常: " + e.getMessage());
    } finally {
        redPacketLock.writeLock().unlock();
    }
}

private void 过期红包返回(Map<String, Object> packet) {
    try {
        int total = (Integer) packet.get("total");
        List<Integer> grabbedPoints = (List<Integer>) packet.get("grabbedPoints");
        
        int totalGrabbed = 0;
        if (grabbedPoints != null && !grabbedPoints.isEmpty()) {
            for (int points : grabbedPoints) {
                totalGrabbed += points;
            }
        }
        
        int returnPoints = total - totalGrabbed;
        
        if (returnPoints > 0) {
            String groupId = (String) packet.get("groupId");
            String senderQQ = (String) packet.get("sender");
            String redPacketId = (String) packet.get("id");
            
            Map<String, Object> userData = loadUserSignData(groupId, senderQQ);
            int newPoints = (Integer) userData.get("points") + returnPoints;
            
            saveUserSignData(groupId, senderQQ,
                (String) userData.get("lastDate"),
                (Integer) userData.get("consecutive"),
                (Integer) userData.get("total"),
                newPoints,
                (String) userData.get("lastLikeDate"),
                (Integer) userData.get("likeAttempts"),
                (Integer) userData.get("makeupCards"),
                (String) userData.get("lastMakeupDate"),
                (Integer) userData.get("titleCard"),
                (Integer) userData.get("FiveCard"),
                (Integer) userData.get("luckyCard"),
                (Integer) userData.get("tenfoldCard"),
                (Integer) userData.get("doubleCard"),
                (Integer) userData.get("FiveCardActive"),
                (Integer) userData.get("tenfoldCardActive"),
                (Integer) userData.get("doubleCardActive"),
                (Integer) userData.get("monthlyBuyCount"),
                (String) userData.get("lastBuyMonth"),
                (Integer) userData.get("normalBox"),
                (Integer) userData.get("mediumBox"),
                (Integer) userData.get("advancedBox"));
            
            String senderNick = 获取实时昵称(groupId, senderQQ);
            if (senderNick == null || senderNick.isEmpty()) senderNick = senderQQ;
            
            sendMsg(groupId, "", "⏰红包 " + redPacketId + " 已过期\n" +
                     "总积分：" + total + "\n" +
                     "已抢积分：" + totalGrabbed + "\n" +
                     "退回积分：" + returnPoints);
        }
    } catch (Exception e) {
        sendMsg((String) packet.get("groupId"), "", "红包退款异常: " + e.getMessage());
    }
}

private void 手动查看我的红包(String groupId, String qq) {
    new Thread(new Runnable() {
        public void run() {
            String nickname = 获取实时昵称(groupId, qq);
            if (nickname == null || nickname.isEmpty()) nickname = qq;
            
            StringBuilder sentInfo = new StringBuilder();
            StringBuilder grabbedInfo = new StringBuilder();
            
            redPacketLock.readLock().lock();
            try {
                for (Map<String, Object> p : redPacketDataMap.values()) {
                if (!p.get("groupId").equals(groupId)) {
                        continue;
                    }
                    
                    if (p.get("sender").equals(qq)) {
                        String id = (String) p.get("id");
                        int total = (Integer) p.get("total");
                        int remaining = (Integer) p.get("remaining");
                        String status = (String) p.get("status");
                        
                        String statusText = "进行中";
                        if ("finished".equals(status)) statusText = "已领完";
                        else if ("expired".equals(status)) statusText = "已过期";
                        
                        sentInfo.append(id).append(" | 总额:").append(total)
                               .append(" | 剩余:").append(remaining)
                               .append(" | ").append(statusText).append("\n");
                    }
                }
                
                for (Map<String, Object> p : redPacketDataMap.values()) {
                if (!p.get("groupId").equals(groupId)) {
                        continue;
                    }
                    
                    List<String> grabbed = (List<String>) p.get("grabbed");
                    List<Integer> grabbedPoints = (List<Integer>) p.get("grabbedPoints");
                    
                    for (int i = 0; i < grabbed.size(); i++) {
                        if (grabbed.get(i).equals(qq)) {
                            String senderQQ = (String) p.get("sender");
                            String senderNick = 获取实时昵称(groupId, senderQQ);
                            if (senderNick == null || senderNick.isEmpty()) senderNick = senderQQ;
                            
                            int points = grabbedPoints.get(i);
                            String id = (String) p.get("id");
                            
                            grabbedInfo.append("来自 ").append(senderNick)
                                      .append(" | 金额:").append(points)
                                      .append(" | ID:").append(id).append("\n");
                        }
                    }
                }
            } finally {
                redPacketLock.readLock().unlock();
            }
            
            if (sentInfo.length() == 0) sentInfo.append("暂无发出的红包");
            if (grabbedInfo.length() == 0) grabbedInfo.append("暂无抢到的红包");
            
            String content = "【发出的红包】\n" + sentInfo.toString() + "\n【抢到的红包】\n" + grabbedInfo.toString();
            
            String imagePath = 绘制我的红包图片(groupId, nickname, content);
            if (imagePath != null) {
                发送图片消息(groupId, imagePath);
                延迟删除签到图片(imagePath, 20000);
            } else {
                sendMsg(groupId, "", "红包记录 - " + nickname + "\n\n" + content);
            }
        }
    }).start();
}

private String 绘制拼手气红包图片(String groupId, String senderNick, int total, int count, String redPacketId) {
    try {
        Activity a = getActivity();
        if (a == null) return null;
        
        int width = dp(500);
        int height = dp(350);
        
        Bitmap bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(bitmap);
        
        int bgColor = isNightMode ? Color.parseColor("#1E1E2E") : Color.parseColor("#FFF8F0");
        canvas.drawColor(bgColor);
        
        Paint borderPaint = new Paint();
        borderPaint.setColor(Color.parseColor("#FF6B6B"));
        borderPaint.setStyle(Paint.Style.STROKE);
        borderPaint.setStrokeWidth(dp(3));
        canvas.drawRoundRect(dp(10), dp(10), width - dp(10), height - dp(10), dp(20), dp(20), borderPaint);
        
        Paint emojiPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        emojiPaint.setTextSize(dp(50));
        emojiPaint.setTextAlign(Align.CENTER);
        canvas.drawText("🧧", width / 2, dp(70), emojiPaint);
        
        Paint titlePaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        titlePaint.setColor(isNightMode ? Color.parseColor("#FFB74D") : Color.parseColor("#D47F6B"));
        titlePaint.setTextSize(dp(22));
        titlePaint.setTypeface(Typeface.create(Typeface.SERIF, Typeface.BOLD));
        titlePaint.setTextAlign(Align.CENTER);
        canvas.drawText(senderNick + " 发了一个签到红包", width / 2, dp(130), titlePaint);
        
        Paint infoPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        infoPaint.setColor(isNightMode ? Color.WHITE : Color.BLACK);
        infoPaint.setTextSize(dp(18));
        infoPaint.setTypeface(Typeface.create(Typeface.SERIF, Typeface.NORMAL));
        infoPaint.setTextAlign(Align.CENTER);
        
        canvas.drawText("总积分：" + total, width / 2, dp(180), infoPaint);
        canvas.drawText("红包个数：" + count, width / 2, dp(220), infoPaint);
        canvas.drawText("有效期：1小时", width / 2, dp(260), infoPaint);
        
        Paint tipPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        tipPaint.setColor(isNightMode ? Color.parseColor("#AAAAAA") : Color.parseColor("#666666"));
        tipPaint.setTextSize(dp(14));
        tipPaint.setTypeface(Typeface.create(Typeface.SERIF, Typeface.NORMAL));
        tipPaint.setTextAlign(Align.CENTER);
        canvas.drawText("复制消息发送即可抢红包", width / 2, dp(310), tipPaint);
        
        String fileName = "redpacket_" + System.currentTimeMillis() + ".png";
        String filePath = appPath + "/temp/" + fileName;
        File dir = new File(appPath + "/temp");
        if (!dir.exists()) dir.mkdirs();
        
        FileOutputStream fos = new FileOutputStream(filePath);
        bitmap.compress(Bitmap.CompressFormat.PNG, 90, fos);
        fos.close();
        bitmap.recycle();
        
        return filePath;
        
    } catch (Exception e) {
        return null;
    }
}

private String 绘制专属红包图片(String groupId, String senderNick, String targetNick, int points, String redPacketId) {
    try {
        Activity a = getActivity();
        if (a == null) return null;
        
        int width = dp(500);
        int height = dp(380);
        
        Bitmap bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(bitmap);
        
        int bgColor = isNightMode ? Color.parseColor("#2D1B2E") : Color.parseColor("#FFF8E7");
        canvas.drawColor(bgColor);
        
        Paint borderPaint = new Paint();
        borderPaint.setColor(Color.parseColor("#FFD700"));
        borderPaint.setStyle(Paint.Style.STROKE);
        borderPaint.setStrokeWidth(dp(3));
        canvas.drawRoundRect(dp(10), dp(10), width - dp(10), height - dp(10), dp(20), dp(20), borderPaint);
        
        Paint innerBorderPaint = new Paint();
        innerBorderPaint.setColor(Color.parseColor("#FFD700"));
        innerBorderPaint.setStyle(Paint.Style.STROKE);
        innerBorderPaint.setStrokeWidth(dp(1));
        innerBorderPaint.setAlpha(100);
        canvas.drawRoundRect(dp(18), dp(18), width - dp(18), height - dp(18), dp(16), dp(16), innerBorderPaint);
        
        Paint emojiPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        emojiPaint.setTextSize(dp(55));
        emojiPaint.setTextAlign(Align.CENTER);
        canvas.drawText("🧧", width / 2, dp(75), emojiPaint);
        
        Paint titlePaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        titlePaint.setColor(Color.parseColor("#FFD700"));
        titlePaint.setTextSize(dp(24));
        titlePaint.setTypeface(Typeface.create(Typeface.SERIF, Typeface.BOLD));
        titlePaint.setTextAlign(Align.CENTER);
        canvas.drawText("专属红包", width / 2, dp(125), titlePaint);
        
        Paint senderLabelPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        senderLabelPaint.setColor(isNightMode ? Color.parseColor("#AAAAAA") : Color.parseColor("#888888"));
        senderLabelPaint.setTextSize(dp(14));
        senderLabelPaint.setTypeface(Typeface.create(Typeface.SERIF, Typeface.NORMAL));
        senderLabelPaint.setTextAlign(Align.CENTER);
        canvas.drawText("发送者", width / 2, dp(165), senderLabelPaint);
        
        Paint senderPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        senderPaint.setColor(isNightMode ? Color.parseColor("#FFB74D") : Color.parseColor("#D47F6B"));
        senderPaint.setTextSize(dp(18));
        senderPaint.setTypeface(Typeface.create(Typeface.SERIF, Typeface.BOLD));
        senderPaint.setTextAlign(Align.CENTER);
        canvas.drawText(senderNick, width / 2, dp(195), senderPaint);
        
        Paint arrowPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        arrowPaint.setColor(Color.parseColor("#FFD700"));
        arrowPaint.setTextSize(dp(22));
        arrowPaint.setTextAlign(Align.CENTER);
        canvas.drawText("⬇", width / 2, dp(230), arrowPaint);
        
        Paint targetLabelPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        targetLabelPaint.setColor(isNightMode ? Color.parseColor("#AAAAAA") : Color.parseColor("#888888"));
        targetLabelPaint.setTextSize(dp(14));
        targetLabelPaint.setTypeface(Typeface.create(Typeface.SERIF, Typeface.NORMAL));
        targetLabelPaint.setTextAlign(Align.CENTER);
        canvas.drawText("接收者", width / 2, dp(265), targetLabelPaint);
        
        Paint targetPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        targetPaint.setColor(isNightMode ? Color.parseColor("#4CAF50") : Color.parseColor("#2E7D32"));
        targetPaint.setTextSize(dp(18));
        targetPaint.setTypeface(Typeface.create(Typeface.SERIF, Typeface.BOLD));
        targetPaint.setTextAlign(Align.CENTER);
        canvas.drawText(targetNick, width / 2, dp(295), targetPaint);
        
        Paint pointsPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        pointsPaint.setColor(Color.parseColor("#FFD700"));
        pointsPaint.setTextSize(dp(26));
        pointsPaint.setTypeface(Typeface.create(Typeface.SERIF, Typeface.BOLD));
        pointsPaint.setTextAlign(Align.CENTER);
        canvas.drawText(points + " 积分", width / 2, dp(345), pointsPaint);
        
        Paint tipPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        tipPaint.setColor(isNightMode ? Color.parseColor("#666666") : Color.parseColor("#999999"));
        tipPaint.setTextSize(dp(12));
        tipPaint.setTypeface(Typeface.create(Typeface.SERIF, Typeface.ITALIC));
        tipPaint.setTextAlign(Align.CENTER);
        canvas.drawText("✨ 积分已自动转入对方账户 ✨", width / 2, dp(370), tipPaint);
        
        String fileName = "ex_redpacket_" + System.currentTimeMillis() + ".png";
        String filePath = appPath + "/temp/" + fileName;
        File dir = new File(appPath + "/temp");
        if (!dir.exists()) dir.mkdirs();
        
        FileOutputStream fos = new FileOutputStream(filePath);
        bitmap.compress(Bitmap.CompressFormat.PNG, 90, fos);
        fos.close();
        bitmap.recycle();
        
        return filePath;
        
    } catch (Exception e) {
        return null;
    }
}

private String 绘制我的红包图片(String groupId, String nickname, String content) {
    try {
        /*Activity a = getActivity();
        if (a == null) return null;*/
        
        String[] sections = content.split("【抢到的红包】");
        String sentSection = sections[0].replace("【发出的红包】", "").trim();
        String grabbedSection = sections.length > 1 ? sections[1].trim() : "";
        
        String[] sentLines = sentSection.split("\n");
        String[] grabbedLines = grabbedSection.split("\n");
        
        int lineHeight = dp(28);
        int padding = dp(20);
        int midPadding = dp(30);
        
        Paint measurePaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        measurePaint.setTextSize(dp(14));
        measurePaint.setTypeface(Typeface.create(Typeface.SERIF, Typeface.NORMAL));
        
        float leftMaxWidth = 0;
        for (String line : sentLines) {
            if (!line.isEmpty() && !line.equals("暂无发出的红包")) {
                float textWidth = measurePaint.measureText(line);
                if (textWidth > leftMaxWidth) leftMaxWidth = textWidth;
            }
        }
        if (leftMaxWidth == 0) leftMaxWidth = measurePaint.measureText("暂无发出的红包");
        
        float rightMaxWidth = 0;
        for (String line : grabbedLines) {
            if (!line.isEmpty() && !line.equals("暂无抢到的红包")) {
                float textWidth = measurePaint.measureText(line);
                if (textWidth > rightMaxWidth) rightMaxWidth = textWidth;
            }
        }
        if (rightMaxWidth == 0) rightMaxWidth = measurePaint.measureText("暂无抢到的红包");
        
        int singleColWidth = (int) (Math.max(leftMaxWidth, rightMaxWidth) + padding * 2 + midPadding / 2);
        singleColWidth = Math.max(singleColWidth, dp(260));
        int width = singleColWidth * 2;
        
        int leftLines = 0;
        for (String line : sentLines) {
            if (!line.isEmpty() && !line.equals("暂无发出的红包")) leftLines++;
        }
        if (leftLines == 0) leftLines = 1;
        
        int rightLines = 0;
        for (String line : grabbedLines) {
            if (!line.isEmpty() && !line.equals("暂无抢到的红包")) rightLines++;
        }
        if (rightLines == 0) rightLines = 1;
        
        int maxLines = Math.max(leftLines, rightLines);
        int height = Math.max(dp(400), maxLines * lineHeight + dp(100));
        
        Bitmap bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(bitmap);
        
        int bgColor = isNightMode ? Color.parseColor("#1E1E2E") : Color.parseColor("#FFF8F0");
        canvas.drawColor(bgColor);
        
        Paint titlePaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        titlePaint.setColor(isNightMode ? Color.parseColor("#FFB74D") : Color.parseColor("#D47F6B"));
        titlePaint.setTextSize(dp(22));
        titlePaint.setTypeface(Typeface.create(Typeface.SERIF, Typeface.BOLD));
        titlePaint.setTextAlign(Align.CENTER);
        canvas.drawText("红包记录 - " + nickname, width / 2, dp(40), titlePaint);
        
        Paint leftHeaderPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        leftHeaderPaint.setColor(isNightMode ? Color.parseColor("#FFB74D") : Color.parseColor("#D47F6B"));
        leftHeaderPaint.setTextSize(dp(16));
        leftHeaderPaint.setTypeface(Typeface.create(Typeface.SERIF, Typeface.BOLD));
        leftHeaderPaint.setTextAlign(Align.LEFT);
        canvas.drawText("［发出的红包］", padding, dp(70), leftHeaderPaint);
        
        Paint rightHeaderPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        rightHeaderPaint.setColor(isNightMode ? Color.parseColor("#FFB74D") : Color.parseColor("#D47F6B"));
        rightHeaderPaint.setTextSize(dp(16));
        rightHeaderPaint.setTypeface(Typeface.create(Typeface.SERIF, Typeface.BOLD));
        rightHeaderPaint.setTextAlign(Align.LEFT);
        canvas.drawText("［抢到的红包］", width / 2 + padding, dp(70), rightHeaderPaint);
        
        Paint contentPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        contentPaint.setColor(isNightMode ? Color.parseColor("#EEEEEE") : Color.parseColor("#333333"));
        contentPaint.setTextSize(dp(14));
        contentPaint.setTypeface(Typeface.create(Typeface.SERIF, Typeface.NORMAL));
        contentPaint.setTextAlign(Align.LEFT);
        
        float y = dp(95);
        if (sentLines.length == 0 || (sentLines.length == 1 && sentLines[0].equals("暂无发出的红包"))) {
            canvas.drawText("暂无发出的红包", padding, y, contentPaint);
        } else {
            for (String line : sentLines) {
                if (line.isEmpty()) continue;
                canvas.drawText(line, padding, y, contentPaint);
                y += lineHeight;
            }
        }
        
        y = dp(95);
        if (grabbedLines.length == 0 || (grabbedLines.length == 1 && grabbedLines[0].equals("暂无抢到的红包"))) {
            canvas.drawText("暂无抢到的红包", width / 2 + padding, y, contentPaint);
        } else {
            for (String line : grabbedLines) {
                if (line.isEmpty()) continue;
                canvas.drawText(line, width / 2 + padding, y, contentPaint);
                y += lineHeight;
            }
        }
        
        String fileName = "myredpacket_" + System.currentTimeMillis() + ".png";
        String filePath = appPath + "/temp/" + fileName;
        File dir = new File(appPath + "/temp");
        if (!dir.exists()) dir.mkdirs();
        
        FileOutputStream fos = new FileOutputStream(filePath);
        bitmap.compress(Bitmap.CompressFormat.PNG, 90, fos);
        fos.close();
        bitmap.recycle();
        
        return filePath;
        
    } catch (Exception e) {
        return null;
    }
}

private void 保存数据到文件(String data, File file) {
    try {
        byte[] encrypted = encryptData(data.getBytes("UTF-8"));
        FileOutputStream fos = new FileOutputStream(file);
        fos.write(encrypted);
        fos.close();
    } catch (Exception e) {}
}

private String 从文件获取数据(File file) {
    try {
        FileInputStream fis = new FileInputStream(file);
        byte[] encrypted = new byte[(int) file.length()];
        fis.read(encrypted);
        fis.close();
        byte[] decrypted = decryptData(encrypted);
        return new String(decrypted, "UTF-8");
    } catch (Exception e) {
        return "";
    }
}

private List<String> 检查已解锁的成就(String groupId, String qq, int consecutive, int total, String existingAchievements) {
    List<String> unlockedList = new ArrayList<String>();
    if (existingAchievements == null) existingAchievements = "";
    
    String[] unlocked = existingAchievements.split(",");
    Set<String> unlockedSet = new HashSet<String>();
    for (String s : unlocked) {
        if (!s.isEmpty()) unlockedSet.add(s);
    }
    
    List<String> newAchievements = new ArrayList<String>();
    int totalReward = 0;
    
    for (String[] ach : ACHIEVEMENTS) {
        String id = ach[0];
        String name = ach[1];
        int reward = Integer.parseInt(ach[3]);
        
        if (unlockedSet.contains(id)) continue;
        
        boolean shouldUnlock = false;
        
        if (id.equals("first_sign") && total >= 1) {
            shouldUnlock = true;
        } else if (id.equals("total_7") && total >= 7) {
            shouldUnlock = true;
        } else if (id.equals("total_30") && total >= 30) {
            shouldUnlock = true;
        } else if (id.equals("total_100") && total >= 100) {
            shouldUnlock = true;
        } else if (id.equals("total_365") && total >= 365) {
            shouldUnlock = true;
        } else if (id.equals("consecutive_7") && consecutive >= 7) {
            shouldUnlock = true;
        } else if (id.equals("consecutive_30") && consecutive >= 30) {
            shouldUnlock = true;
        } else if (id.equals("consecutive_100") && consecutive >= 100) {
            shouldUnlock = true;
        } else if (id.equals("consecutive_365") && consecutive >= 365) {
            shouldUnlock = true;
        }
        
        if (shouldUnlock) {
            unlockedSet.add(id);
            newAchievements.add(name);
            totalReward += reward;
            
            String nickname = 获取实时昵称(groupId, qq);
            if (nickname == null || nickname.isEmpty()) nickname = qq;
            sendMsg(groupId, "", "🎉" + nickname + "解锁成就" + name + "！\n获得" + reward + "积分奖励！");
        }
    }
    
    StringBuilder sb = new StringBuilder();
    for (String s : unlockedSet) {
        if (sb.length() > 0) sb.append(",");
        sb.append(s);
    }
    unlockedList.add(sb.toString());
    unlockedList.add(String.valueOf(totalReward));
    unlockedList.addAll(newAchievements);
    
    return unlockedList;
}

private void 查看我的成就(String groupId, String qq) {
    new Thread(new Runnable() {
        public void run() {
            Map<String, Object> userData = loadUserSignData(groupId, qq);
            String achievements = (String) userData.get("achievements");
            int consecutive = (Integer) userData.get("consecutive");
            int total = (Integer) userData.get("total");
            
            String nickname = 获取实时昵称(groupId, qq);
            if (nickname == null || nickname.isEmpty()) nickname = qq;
            
            Set<String> unlockedSet = new HashSet<String>();
            if (achievements != null && !achievements.isEmpty()) {
                for (String s : achievements.split(",")) {
                    if (!s.isEmpty()) unlockedSet.add(s);
                }
            }
            
            StringBuilder content = new StringBuilder();
            content.append("【已解锁成就】\n");
            
            int unlockedCount = 0;
            for (String[] ach : ACHIEVEMENTS) {
                String id = ach[0];
                String name = ach[1];
                String desc = ach[2];
                String reward = ach[3];
                
                if (unlockedSet.contains(id)) {
                    content.append("✓").append(name).append("（").append(reward).append("积分）\n");
                    unlockedCount++;
                }
            }
            
            if (unlockedCount == 0) {
                content.append("暂无成就，快去签到吧！\n");
            }
            
            content.append("\n【未解锁成就】\n");
            for (String[] ach : ACHIEVEMENTS) {
                String id = ach[0];
                String name = ach[1];
                String desc = ach[2];
                String reward = ach[3];
                
                if (!unlockedSet.contains(id)) {
                    String progress = "";
                    if (id.equals("first_sign")) {
                        progress = total >= 1 ? "已完成" : "未完成";
                    } else if (id.startsWith("total_")) {
                        int target = Integer.parseInt(id.split("_")[1]);
                        progress = total + "/" + target;
                    } else if (id.startsWith("consecutive_")) {
                        int target = Integer.parseInt(id.split("_")[1]);
                        progress = consecutive + "/" + target;
                    }
                    content.append("🔒 ").append(name).append("（").append(desc).append("，").append(progress).append("）\n");
                }
            }
            
            content.append("\n连续签到：").append(consecutive).append("天");
            content.append("\n累计签到：").append(total).append("天");
            
            String imagePath = 绘制我的成就图片(groupId, nickname, content.toString());
            if (imagePath != null) {
                发送图片消息(groupId, imagePath);
                延迟删除签到图片(imagePath, 20000);
            } else {
                sendMsg(groupId, "", "🏆 成就系统 - " + nickname + "\n" + content.toString());
            }
        }
    }).start();
}

private String 绘制我的成就图片(String groupId, String nickname, String content) {
    try {
        Activity a = getActivity();
        if (a == null) return null;
        
        String[] lines = content.split("\n");
        int lineHeight = dp(28);
        int padding = dp(20);
        int width = dp(550);
        int height = Math.max(dp(500), lines.length * lineHeight + dp(120));
        
        Bitmap bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(bitmap);
        
        int bgColor = isNightMode ? Color.parseColor("#1E1E2E") : Color.parseColor("#FFF8F0");
        canvas.drawColor(bgColor);
        
        Paint titlePaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        titlePaint.setColor(isNightMode ? Color.parseColor("#FFD700") : Color.parseColor("#D47F6B"));
        titlePaint.setTextSize(dp(24));
        titlePaint.setTypeface(Typeface.create(Typeface.SERIF, Typeface.BOLD));
        titlePaint.setTextAlign(Align.CENTER);
        canvas.drawText("🏆 我的成就", width / 2, dp(40), titlePaint);
        
        Paint namePaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        namePaint.setColor(isNightMode ? Color.parseColor("#FFB74D") : Color.parseColor("#D47F6B"));
        namePaint.setTextSize(dp(18));
        namePaint.setTypeface(Typeface.create(Typeface.SERIF, Typeface.BOLD));
        namePaint.setTextAlign(Align.CENTER);
        canvas.drawText(nickname, width / 2, dp(70), namePaint);
        
        Paint linePaint = new Paint();
        linePaint.setColor(isNightMode ? Color.parseColor("#444444") : Color.parseColor("#FFE5D9"));
        linePaint.setStrokeWidth(dp(1));
        canvas.drawLine(padding, dp(85), width - padding, dp(85), linePaint);
        
        Paint contentPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        contentPaint.setColor(isNightMode ? Color.parseColor("#EEEEEE") : Color.parseColor("#333333"));
        contentPaint.setTextSize(dp(14));
        contentPaint.setTypeface(Typeface.create(Typeface.SERIF, Typeface.NORMAL));
        contentPaint.setTextAlign(Align.LEFT);
        
        float y = dp(110);
        for (String line : lines) {
            if (line.startsWith("［")) {
                Paint headerPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
                headerPaint.setColor(isNightMode ? Color.parseColor("#FFD700") : Color.parseColor("#D47F6B"));
                headerPaint.setTextSize(dp(16));
                headerPaint.setTypeface(Typeface.create(Typeface.SERIF, Typeface.BOLD));
                canvas.drawText(line, padding, y, headerPaint);
            } else {
                canvas.drawText(line, padding + dp(10), y, contentPaint);
            }
            y += lineHeight;
        }
        
        String fileName = "achievement_" + System.currentTimeMillis() + ".png";
        String filePath = appPath + "/temp/" + fileName;
        File dir = new File(appPath + "/temp");
        if (!dir.exists()) dir.mkdirs();
        
        FileOutputStream fos = new FileOutputStream(filePath);
        bitmap.compress(Bitmap.CompressFormat.PNG, 90, fos);
        fos.close();
        bitmap.recycle();
        
        return filePath;
        
    } catch (Exception e) {
        return null;
    }
}

private void 检查月度奖励(String groupId) {
    new Thread(new Runnable() {
        public void run() {
            try {
                SimpleDateFormat monthFormat = new SimpleDateFormat("yyyy-MM");
                Calendar cal = Calendar.getInstance();
                String currentMonth = monthFormat.format(cal.getTime());
                
                String lastSettleMonth = 获取最后签到月份(groupId);
                
                if (currentMonth.equals(lastSettleMonth)) {
                    return;
                }
                
                if (lastSettleMonth.isEmpty()) {
                    保存最后签到月份(groupId, currentMonth);
                    return;
                }
                
                cal.add(Calendar.MONTH, -1);
                String lastMonth = monthFormat.format(cal.getTime());
                
                if (!lastMonth.equals(lastSettleMonth)) {
                    保存最后签到月份(groupId, currentMonth);
                    return;
                }
                
                List<Map<String, Object>> allData = loadAllGroupSignData(groupId);
                if (allData == null || allData.isEmpty()) {
                    保存最后签到月份(groupId, currentMonth);
                    return;
                }
                
                List<Map<String, Object>> sortedList = new ArrayList<Map<String, Object>>();
                for (Map<String, Object> data : allData) {
                    String qq = (String) data.get("qq");
                    if (qq.equals(Author)) continue;
                    
                    Map<String, Object> fullData = loadUserSignData(groupId, qq);
                    int total = (Integer) fullData.get("total");
                    
                    Map<String, Object> rankData = new HashMap<String, Object>();
                    rankData.put("qq", qq);
                    rankData.put("total", total);
                    rankData.put("points", fullData.get("points"));
                    sortedList.add(rankData);
                }
                
                if (sortedList.isEmpty()) {
                    保存最后签到月份(groupId, currentMonth);
                    return;
                }
                
                Collections.sort(sortedList, new Comparator<Map<String, Object>>() {
                    public int compare(Map<String, Object> o1, Map<String, Object> o2) {
                        return ((Integer) o2.get("total")).compareTo((Integer) o1.get("total"));
                    }
                });
                
                int[] rewards = {100, 50, 20};
                List<Map<String, Object>> winners = new ArrayList<Map<String, Object>>();
                
                for (int i = 0; i < Math.min(3, sortedList.size()); i++) {
                    Map<String, Object> data = sortedList.get(i);
                    String qq = (String) data.get("qq");
                    int reward = rewards[i];
                    
                    Map<String, Object> userData = loadUserSignData(groupId, qq);
                    int newPoints = (Integer) userData.get("points") + reward;
                    
                    saveUserSignData(groupId, qq,
                        (String) userData.get("lastDate"),
                        (Integer) userData.get("consecutive"),
                        (Integer) userData.get("total"),
                        newPoints,
                        (String) userData.get("lastLikeDate"),
                        (Integer) userData.get("likeAttempts"),
                        (Integer) userData.get("makeupCards"),
                        (String) userData.get("lastMakeupDate"),
                        (Integer) userData.get("titleCard"),
                        (Integer) userData.get("FiveCard"),
                        (Integer) userData.get("luckyCard"),
                        (Integer) userData.get("tenfoldCard"),
                        (Integer) userData.get("doubleCard"),
                        (Integer) userData.get("FiveCardActive"),
                        (Integer) userData.get("tenfoldCardActive"),
                        (Integer) userData.get("doubleCardActive"),
                        (Integer) userData.get("monthlyBuyCount"),
                        (String) userData.get("lastBuyMonth"),
                        (Integer) userData.get("normalBox"),
                        (Integer) userData.get("mediumBox"),
                        (Integer) userData.get("advancedBox"));
                    
                    Map<String, Object> winner = new HashMap<String, Object>();
                    winner.put("rank", i + 1);
                    winner.put("qq", qq);
                    winner.put("total", data.get("total"));
                    winner.put("reward", reward);
                    winners.add(winner);
                }
                
                保存最后签到月份(groupId, currentMonth);
                
                if (!winners.isEmpty()) {
                    发送月度奖励通知(groupId, lastMonth, winners, sortedList);
                }
                
            } catch (Exception e) {
                sendMsg(groupId, "", "月度奖励结算失败: " + e.getMessage());
            }
        }
    }).start();
}

private String 获取最后签到月份(String groupId) {
    try {
        File file = new File(monthlyRewardFile);
        if (!file.exists()) return "";
        
        String content = 解密文件(file);
        String[] lines = content.split("\n");
        for (String line : lines) {
            if (line.trim().isEmpty()) continue;
            String[] parts = line.trim().split("=", 2);
            if (parts.length == 2 && parts[0].equals(groupId)) {
                return parts[1];
            }
        }
    } catch (Exception e) {}
    return "";
}

private void 保存最后签到月份(String groupId, String month) {
    try {
        File dir = new File(appPath + "/sign");
        if (!dir.exists()) dir.mkdirs();
        
        Map<String, String> map = new HashMap<String, String>();
        File file = new File(monthlyRewardFile);
        
        if (file.exists()) {
            String content = 解密文件(file);
            String[] lines = content.split("\n");
            for (String line : lines) {
                if (line.trim().isEmpty()) continue;
                String[] parts = line.trim().split("=", 2);
                if (parts.length == 2) {
                    map.put(parts[0], parts[1]);
                }
            }
        }
        
        map.put(groupId, month);
        
        File tempFile = new File(appPath + "/sign/monthly_temp.txt");
        BufferedWriter writer = new BufferedWriter(new FileWriter(tempFile));
        for (Map.Entry<String, String> entry : map.entrySet()) {
            writer.write(entry.getKey() + "=" + entry.getValue());
            writer.newLine();
        }
        writer.close();
        
        加密文件(tempFile, file);
        
    } catch (Exception e) {}
}

private void 发送月度奖励通知(String groupId, String month, List<Map<String, Object>> winners, List<Map<String, Object>> allRank) {
    try {
        StringBuilder content = new StringBuilder();
        content.append("【上月签到排行榜】\n\n");
        
        int showCount = Math.min(10, allRank.size());
        for (int i = 0; i < showCount; i++) {
            Map<String, Object> data = allRank.get(i);
            String qq = (String) data.get("qq");
            String nickname = 获取实时昵称(groupId, qq);
            if (nickname == null || nickname.isEmpty()) nickname = qq;
            if (nickname.length() > 8) nickname = nickname.substring(0, 7) + "…";
            
            int total = (Integer) data.get("total");
            
            String medal = "";
            if (i == 0) medal = "🥇";
            else if (i == 1) medal = "🥈";
            else if (i == 2) medal = "🥉";
            else medal = (i + 1) + ".";
            
            content.append(medal).append(" ").append(nickname).append("  ").append(total).append("天\n");
        }
        
        content.append("\n【获奖名单】\n");
        for (Map<String, Object> winner : winners) {
            int rank = (Integer) winner.get("rank");
            String qq = (String) winner.get("qq");
            String nickname = 获取实时昵称(groupId, qq);
            if (nickname == null || nickname.isEmpty()) nickname = qq;
            
            int total = (Integer) winner.get("total");
            int reward = (Integer) winner.get("reward");
            
            String medal = rank == 1 ? "🥇" : (rank == 2 ? "🥈" : "🥉");
            content.append(medal).append(" ").append(nickname).append("  +").append(reward).append("积分\n");
        }
        
        content.append("\n奖励已自动发放！");
        
        String imagePath = 绘制月度奖励图片(groupId, month, content.toString());
        if (imagePath != null) {
            发送图片消息(groupId, imagePath);
            延迟删除签到图片(imagePath, 30000);
        } else {
            sendMsg(groupId, "", "" + month + " 月度奖励\n\n" + content.toString());
        }
        
    } catch (Exception e) {
        sendMsg(groupId, "", "发送奖励通知失败");
    }
}

private String 绘制月度奖励图片(String groupId, String month, String content) {
    try {
        Activity a = getActivity();
        if (a == null) return null;
        
        String[] lines = content.split("\n");
        int lineHeight = dp(30);
        int padding = dp(20);
        int width = dp(550);
        int height = Math.max(dp(500), lines.length * lineHeight + dp(120));
        
        Bitmap bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(bitmap);
        
        int bgColor = isNightMode ? Color.parseColor("#1E1E2E") : Color.parseColor("#FFF8F0");
        canvas.drawColor(bgColor);
        
        Paint titlePaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        titlePaint.setColor(isNightMode ? Color.parseColor("#FFD700") : Color.parseColor("#D47F6B"));
        titlePaint.setTextSize(dp(24));
        titlePaint.setTypeface(Typeface.create(Typeface.SERIF, Typeface.BOLD));
        titlePaint.setTextAlign(Align.CENTER);
        canvas.drawText("" + month + " 月度奖励", width / 2, dp(40), titlePaint);
        
        Paint linePaint = new Paint();
        linePaint.setColor(isNightMode ? Color.parseColor("#444444") : Color.parseColor("#FFE5D9"));
        linePaint.setStrokeWidth(dp(1));
        canvas.drawLine(padding, dp(55), width - padding, dp(55), linePaint);
        
        Paint contentPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        contentPaint.setColor(isNightMode ? Color.parseColor("#EEEEEE") : Color.parseColor("#333333"));
        contentPaint.setTextSize(dp(15));
        contentPaint.setTypeface(Typeface.create(Typeface.SERIF, Typeface.NORMAL));
        contentPaint.setTextAlign(Align.LEFT);
        
        float y = dp(80);
        for (String line : lines) {
            if (line.startsWith("［")) {
                Paint headerPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
                headerPaint.setColor(isNightMode ? Color.parseColor("#FFD700") : Color.parseColor("#D47F6B"));
                headerPaint.setTextSize(dp(18));
                headerPaint.setTypeface(Typeface.create(Typeface.SERIF, Typeface.BOLD));
                canvas.drawText(line, padding, y, headerPaint);
            } else if (line.startsWith("🥇") || line.startsWith("🥈") || line.startsWith("🥉")) {
                Paint winnerPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
                winnerPaint.setColor(isNightMode ? Color.parseColor("#FFD700") : Color.parseColor("#D47F6B"));
                winnerPaint.setTextSize(dp(16));
                winnerPaint.setTypeface(Typeface.create(Typeface.SERIF, Typeface.BOLD));
                canvas.drawText(line, padding + dp(10), y, winnerPaint);
            } else {
                canvas.drawText(line, padding + dp(10), y, contentPaint);
            }
            y += lineHeight;
        }
        
        String fileName = "monthly_reward_" + System.currentTimeMillis() + ".png";
        String filePath = appPath + "/temp/" + fileName;
        File dir = new File(appPath + "/temp");
        if (!dir.exists()) dir.mkdirs();
        
        FileOutputStream fos = new FileOutputStream(filePath);
        bitmap.compress(Bitmap.CompressFormat.PNG, 90, fos);
        fos.close();
        bitmap.recycle();
        
        return filePath;
        
    } catch (Exception e) {
        return null;
    }
}

private void 检查每日盲盒彩蛋(String groupId, String qq, Map<String, Object> userData) {
    try {
        String lastBlindBoxDate = (String) userData.get("lastBlindBoxDate");
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
        String today = sdf.format(new Date());
        
        if (today.equals(lastBlindBoxDate)) {
            return;
        }
        
        int random = (int)(Math.random() * 100);
        String boxType = "";
        
        if (random < 10) {
            boxType = "初级盲盒";
            userData.put("normalBox", (Integer) userData.get("normalBox") + 1);
        } else if (random < 15) {
            boxType = "中级盲盒";
            userData.put("mediumBox", (Integer) userData.get("mediumBox") + 1);
        } else if (random < 16) {
            boxType = "高级盲盒";
            userData.put("advancedBox", (Integer) userData.get("advancedBox") + 1);
        }
        
        userData.put("lastBlindBoxDate", today);
        
        if (!boxType.isEmpty()) {
            String nickname = 获取实时昵称(groupId, qq);
            if (nickname == null || nickname.isEmpty()) nickname = qq;
            sendMsg(groupId, "", "彩蛋：恭喜 " + nickname + " 签到获得【" + boxType + "】一个");
        }
        
    } catch (Exception e) {
    }
}

private boolean isWeekend() {
    Calendar cal = Calendar.getInstance();
    int dayOfWeek = cal.get(Calendar.DAY_OF_WEEK);
    return dayOfWeek == Calendar.SATURDAY || dayOfWeek == Calendar.SUNDAY;
}

private String 时间彩蛋() {
    Calendar cal = Calendar.getInstance();
    int hour = cal.get(Calendar.HOUR_OF_DAY);
    
    if (hour >= 6 && hour < 7) {
        return "5|早起奖励 +5";
    } else if (hour >= 23 || hour < 1) {
        return "3|熬夜能手 +3";
    } else if (hour >= 1 && hour < 3) {
        return "5|？？？ +5";
    }
    return "";
}

private void 开始清理签到图片计划() {
    new Thread(new Runnable() {
        public void run() {
            try {
                Thread.sleep(10000);
            } catch (Exception e) {}
            Timer cleanupTimer = new Timer();
            cleanupTimer.schedule(new TimerTask() {
                public void run() {
                    try {
                        cleangenerateImages();
                    } catch (Exception e) {
                    }
                }
            }, 0, 1800000);
        }
    }).start();
}

private void cleangenerateImages() {
    try {
        File signPhotosDir = new File(appPath + "/sign_photos");
        if (signPhotosDir.exists() && signPhotosDir.isDirectory()) {
            File[] files = signPhotosDir.listFiles();
            if (files != null) {
                long now = System.currentTimeMillis();
                for (File file : files) {
                    String name = file.getName();
                    if (name.endsWith(".tmp")) {
                        if (now - file.lastModified() > 1800000) {
                            file.delete();
                        }
                    }
                }
            }
        }
        
        File tempDir = new File(appPath + "/temp");
        if (tempDir.exists() && tempDir.isDirectory()) {
            File[] files = tempDir.listFiles();
            if (files != null) {
                long now = System.currentTimeMillis();
                for (File file : files) {
                    if (now - file.lastModified() > 3600000) {
                        file.delete();
                    }
                }
            }
        }
    } catch (Exception e) {}
}

加载红包数据();
开始清理签到图片计划();

toast("签到脚本已加载");