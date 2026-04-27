//整个脚本的信息都不用修改，有需要可以只修改UI弹窗即可
import android.app.Dialog;
import android.graphics.Color;
import android.graphics.drawable.GradientDrawable;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.LinearLayout;
import android.widget.TextView;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import org.json.JSONArray;
import org.json.JSONObject;
import java.io.FileInputStream;
import java.util.Properties;
import java.util.HashMap;
import java.util.Map;

//自动从info.prop文件中获取版本消息，不用修改，但是要确保info.prop中的ID与在线脚本中已发布的相同
String 当前版本 = "1.0.0";//默认版本信息，不用修改
try {
    File propFile = new File(appPath, "info.prop");//加载info文件
    if (propFile.exists()) {
        Properties props = new Properties();
        FileInputStream fis = new FileInputStream(propFile);
        props.load(fis);
        fis.close();
        
        String fileVersion = props.getProperty("version");//读取info中的版本信息，读取到的是旧版本的版本号
        if (fileVersion != null && !fileVersion.trim().isEmpty()) {
            当前版本 = fileVersion.trim();
        }
    }
} catch (Exception e) {
    e.printStackTrace();
}

//自动从info.prop文件中获取ID信息，不用修改
String 目标PluginId = "paper_updacheck";//默认ID信息，不用修改
try {
    File propFile = new File(appPath, "info.prop");//加载info文件
    if (propFile.exists()) {
        Properties props = new Properties();
        FileInputStream fis = new FileInputStream(propFile);
        props.load(fis);
        fis.close();
        
        String fileId = props.getProperty("id");//读取info中的ID信息，读取到的是脚本ID，如果你不改的话就可以正常工作（应该不会有人闲着没事乱改脚本id叭……）
        if (fileId != null && !fileId.trim().isEmpty()) {
            目标PluginId = fileId.trim();
        }
    }
} catch (Exception e) {
    e.printStackTrace();
}

String API地址 = "https://plugin.suzhelan.top/api/plugin/plugins?uin=2376738596&sort=time&tag=全部";//QStory在线脚本信息，不用修改，从这里面获取最新的版本号
String 更新日志地址 = "https://gitee.com/ruangcole/updatecheckin/raw/master/Update_Logs.txt";//更新日志文本地址，有需要可以自行部署

void onCheck() {
    new Thread(new Runnable() {
        public void run() {
            检查更新();
        }
    }).start();
}

//从在线链接获取ID对应的脚本信息
void 检查更新() {
    try {
        System.out.println("开始检查更新...");
        
        URL url = new URL(API地址);
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        conn.setRequestMethod("GET");
        conn.setConnectTimeout(8000);
        conn.setReadTimeout(8000);
        conn.setRequestProperty("User-Agent", "Mozilla/5.0");
        
        BufferedReader in = new BufferedReader(
            new InputStreamReader(conn.getInputStream(), "UTF-8")
        );
        StringBuilder response = new StringBuilder();
        String line;
        while ((line = in.readLine()) != null) {
            response.append(line);
        }
        in.close();
        
        JSONObject json = new JSONObject(response.toString());
        JSONArray dataArray = json.getJSONArray("data");
        
        String 远程版本 = null;
        String 脚本名称 = "";
        
        for (int i = 0; i < dataArray.length(); i++) {
            JSONObject plugin = dataArray.getJSONObject(i);
            String pluginId = plugin.getString("pluginId");
            
            if (pluginId.equals(目标PluginId)) {
                System.out.println("[更新检测]找到目标脚本");
                
                JSONObject pluginInfo = plugin.getJSONObject("pluginInfo");
                脚本名称 = pluginInfo.getString("name");
                远程版本 = pluginInfo.getString("version");
                
                System.out.println("脚本名称: " + 脚本名称);
                System.out.println("远程版本: " + 远程版本);
                System.out.println("当前版本: " + 当前版本);
                break;
            }
        }
        
        if (远程版本 != null) {
            if (isNewerVersion(远程版本, 当前版本)) {
                System.out.println("发现新版本，准备弹窗");
                final String finalScriptName = 脚本名称;
                final String finalRemoteVersion = 远程版本;
                
                //获取更新内容
                String 更新内容 = 获取更新内容(目标PluginId);
                
                getActivity().runOnUiThread(new Runnable() {
                    public void run() {
                        显示更新弹窗(finalScriptName, finalRemoteVersion, 更新内容);
                    }
                });
            } else {
                System.out.println("已是最新版本，不弹窗");
            }
        } else {
            System.out.println("未找到ID为 " + 目标PluginId + " 的脚本");
        }
        
    } catch (Exception e) {
        System.out.println("检查更新失败: " + e.getMessage());
        e.printStackTrace();
    }
}

//从远程文本获取更新内容
String 获取更新内容(String pluginId) {
    try {
        URL url = new URL(更新日志地址);
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        conn.setRequestMethod("GET");
        conn.setConnectTimeout(8000);
        conn.setReadTimeout(8000);
        conn.setRequestProperty("User-Agent", "Mozilla/5.0");
        
        BufferedReader in = new BufferedReader(
            new InputStreamReader(conn.getInputStream(), "UTF-8")
        );
        
        Map<String, String> updateMap = new HashMap<String, String>();
        String line;
        while ((line = in.readLine()) != null) {
            if (line.trim().isEmpty() || line.trim().startsWith("#")) {
                continue;
            }
            
            if (line.contains("=")) {
                int equalIndex = line.indexOf("=");
                String key = line.substring(0, equalIndex).trim();
                String value = line.substring(equalIndex + 1).trim();
                
                if (key.startsWith("\"") && key.endsWith("\"")) {
                    key = key.substring(1, key.length() - 1);
                }
                if (value.startsWith("\"") && value.endsWith("\"")) {
                    value = value.substring(1, value.length() - 1);
                }
                
                value = value.replace("\\n", "\n");
                value = value.replace("\\t", "\t");
                value = value.replace("\\r", "\r");
                
                updateMap.put(key, value);
            }
        }
        in.close();
        
        //根据pluginId获取对应的更新内容
        String updateContent = updateMap.get(pluginId);
        if (updateContent != null && !updateContent.isEmpty()) {
            return updateContent + "\n\n为获得最佳体验，请前往「在线脚本」更新到最新版本\n当你看到本条更新提示而在「在线脚本」没有找到最新版本脚本，说明你用的是旧版本而且脚本审核还未通过";
        } else {
            return "为获得最佳体验，请前往「在线脚本」更新到最新版本\n当你看到本条更新提示而在「在线脚本」没有找到最新版本脚本，说明你用的是旧版本而且脚本审核还未通过";
        }
        
    } catch (Exception e) {
        System.out.println("获取更新内容失败: " + e.getMessage());
        return "为获得最佳体验，请前往「在线脚本」更新到最新版本\n当你看到本条更新提示而在「在线脚本」没有找到最新版本脚本，说明你用的是旧版本而且脚本审核还未通过";
    }
}

//比较当前版本和最新版本，检测到新的版本就会执行弹窗方法
boolean isNewerVersion(String 远程版本, String 当前版本) {
    try {
        String[] 远程数组 = 远程版本.split("\\.");
        String[] 当前数组 = 当前版本.split("\\.");
        
        int 最大长度 = Math.max(远程数组.length, 当前数组.length);
        
        for (int i = 0; i < 最大长度; i++) {
            int 远程数字 = i < 远程数组.length ? Integer.parseInt(远程数组[i]) : 0;
            int 当前数字 = i < 当前数组.length ? Integer.parseInt(当前数组[i]) : 0;
            
            if (远程数字 > 当前数字) {
                return true;
            } else if (远程数字 < 当前数字) {
                return false;
            }
        }
        return false;
    } catch (Exception e) {
        return !远程版本.equals(当前版本);
    }
}

//显示弹窗
void 显示更新弹窗(String 脚本名称, String 远程版本, String 更新内容) {
    Activity activity = getActivity();
    if (activity == null) return;
    
    Dialog dialog = new Dialog(activity);
    dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
    dialog.setCancelable(false);
    
    LinearLayout root = new LinearLayout(activity);
    root.setOrientation(LinearLayout.VERTICAL);
    root.setPadding(50, 50, 50, 50);
    
    GradientDrawable rootBg = new GradientDrawable();
    rootBg.setColor(Color.parseColor("#FFFFFF"));
    rootBg.setCornerRadius(40f);
    root.setBackground(rootBg);
    
    TextView title = new TextView(activity);
    title.setText("「" + 脚本名称 + "」更新提醒");
    title.setTextSize(18);
    title.setGravity(Gravity.CENTER);
    title.setPadding(0, 10, 0, 10);
    title.setTextColor(Color.parseColor("#000000"));
    title.getPaint().setFakeBoldText(true);
    root.addView(title);
    
    TextView versionInfo = new TextView(activity);
    versionInfo.setText("当前版本: " + 当前版本 + "\n最新版本: " + 远程版本);
    versionInfo.setTextSize(14);
    versionInfo.setGravity(Gravity.CENTER);
    versionInfo.setPadding(0, 10, 0, 20);
    versionInfo.setTextColor(Color.parseColor("#666666"));
    root.addView(versionInfo);
    
    TextView content = new TextView(activity);
    content.setText(更新内容);
    content.setTextSize(14);
    content.setGravity(Gravity.CENTER);
    content.setPadding(0, 0, 0, 30);
    content.setTextColor(Color.parseColor("#333333"));
    root.addView(content);
    
    TextView closeBtn = new TextView(activity);
    closeBtn.setText("确定");
    closeBtn.setTextSize(16);
    closeBtn.setTextColor(Color.WHITE);
    closeBtn.setGravity(Gravity.CENTER);
    closeBtn.setPadding(0, 15, 0, 15);
    
    GradientDrawable btnBg = new GradientDrawable();
    btnBg.setColor(Color.parseColor("#4A90E2"));
    btnBg.setCornerRadius(30f);
    closeBtn.setBackground(btnBg);
    
    closeBtn.setOnClickListener(new View.OnClickListener() {
        public void onClick(View v) {
            dialog.dismiss();
        }
    });
    
    root.addView(closeBtn);
    dialog.setContentView(root);
    dialog.show();
    
    Window window = dialog.getWindow();
    if (window != null) {
        window.setLayout(
            (int)(activity.getResources().getDisplayMetrics().widthPixels * 0.8),
            ViewGroup.LayoutParams.WRAP_CONTENT
        );
        window.setBackgroundDrawable(new android.graphics.drawable.ColorDrawable(Color.TRANSPARENT));
    }
}

onCheck();