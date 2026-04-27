//原作者：临江踏雨不返
import android.app.Activity;
import android.app.Dialog;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.GradientDrawable;
import android.net.Uri;
import android.os.Handler;
import android.os.Looper;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

String DIALOG_TITLE = "欢迎使用阿紙的「图片签到」脚本";
String DIALOG_CONTENT = "为了获取更好的体验和脚本的最新更新，请加入我们的官方交流群！\n当然不加入群聊也可以正常使用该脚本";

Map<String, String> TARGET_GROUPS = new HashMap<>();

int COLOR_BG = Color.parseColor("#FFF0F5");
int COLOR_PM = Color.parseColor("#FFB6C1");
int COLOR_SF = Color.parseColor("#FFFAFA");
int COLOR_T_P = Color.parseColor("#8B5F6B");
int COLOR_T_S = Color.parseColor("#B4868F");
int COLOR_BTN_TEXT = Color.parseColor("#FFFFFF");

void onCheck() {
    TARGET_GROUPS.put("紙团", "");
    //TARGET_GROUPS.put("暂无2群", "000000000");

    Activity activity = getActivity();
    if (activity == null) return;
    
    checkAndShowDialog(activity);
}

void checkAndShowDialog(Activity activity) {
    ArrayList joinedGroups = getGroupList();
    List<String> joinedUins = new ArrayList<>();
    
    if (joinedGroups != null) {
        for (Object info : joinedGroups) {
            joinedUins.add(String.valueOf(info.GroupUin));
        }
    }

    boolean hasJoinedAny = false;
    for (Map.Entry<String, String> entry : TARGET_GROUPS.entrySet()) {
        if (joinedUins.contains(entry.getValue())) {
            hasJoinedAny = true;
            break;
        }
    }
    
    if (hasJoinedAny) {
        return;
    }

    activity.runOnUiThread(new Runnable() {
        public void run() {
            showMd3Dialog(activity, TARGET_GROUPS);
        }
    });
}

void showMd3Dialog(Activity activity, Map<String, String> groups) {
    Dialog dialog = new Dialog(activity);
    dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
    dialog.setCancelable(false);
    dialog.setCanceledOnTouchOutside(false);
    
    LinearLayout root = new LinearLayout(activity);
    root.setOrientation(LinearLayout.VERTICAL);
    root.setPadding(dp(30), dp(30), dp(30), dp(30));
    
    GradientDrawable rootBg = new GradientDrawable();
    rootBg.setColor(COLOR_BG);
    rootBg.setCornerRadius(dp(33));
    root.setBackground(rootBg);
    
    TextView titleView = new TextView(activity);
    titleView.setText(DIALOG_TITLE);
    titleView.setTextSize(20);
    titleView.setTextColor(COLOR_T_P);
    titleView.setTypeface(Typeface.create("sans-serif-medium", Typeface.NORMAL));
    titleView.setGravity(Gravity.CENTER);
    titleView.setPadding(0, 0, 0, dp(12));
    root.addView(titleView);
    
    TextView contentView = new TextView(activity);
    contentView.setText(DIALOG_CONTENT);
    contentView.setTextSize(14);
    contentView.setTextColor(COLOR_T_S);
    contentView.setPadding(0, 0, 0, dp(24));
    contentView.setLineSpacing(0, 1.3f);
    root.addView(contentView);

    ScrollView scrollView = new ScrollView(activity);
    scrollView.setBackgroundColor(Color.TRANSPARENT);
    
    LinearLayout groupListLayout = new LinearLayout(activity);
    groupListLayout.setOrientation(LinearLayout.VERTICAL);
    groupListLayout.setPadding(0, 0, 0, 0);

    for (Map.Entry<String, String> entry : groups.entrySet()) {
        String name = entry.getKey();
        String uin = entry.getValue();

        LinearLayout itemLayout = new LinearLayout(activity);
        itemLayout.setOrientation(LinearLayout.VERTICAL);
        itemLayout.setPadding(dp(24), dp(20), dp(24), dp(20));
        LinearLayout.LayoutParams itemParams = new LinearLayout.LayoutParams(
            ViewGroup.LayoutParams.MATCH_PARENT,
            ViewGroup.LayoutParams.WRAP_CONTENT
        );
        itemParams.setMargins(0, 0, 0, dp(12));
        itemLayout.setLayoutParams(itemParams);

        GradientDrawable itemBg = new GradientDrawable();
        itemBg.setColor(COLOR_SF);
        itemBg.setCornerRadius(dp(20));
        itemBg.setStroke(dp(1), Color.parseColor("#FFB6C1"));
        itemLayout.setBackground(itemBg);

        TextView nameView = new TextView(activity);
        nameView.setText(name);
        nameView.setTextSize(16);
        nameView.setTextColor(COLOR_T_P);
        nameView.setTypeface(Typeface.create("sans-serif-medium", Typeface.NORMAL));
        nameView.setPadding(0, 0, 0, dp(4));

        TextView uinView = new TextView(activity);
        uinView.setText("群号: " + uin);
        uinView.setTextSize(12);
        uinView.setTextColor(COLOR_T_S);

        itemLayout.addView(nameView);
        itemLayout.addView(uinView);

        itemLayout.setTag(uin);
        
        itemLayout.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                try {
                    String groupUin = (String) v.getTag();
                    Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse("mqqapi://card/show_pslcard?src_type=internal&version=1&uin=" + groupUin + "&card_type=group&source=qrcode"));
                    activity.startActivity(intent);
                } catch (Exception e) {
                    toast("跳转失败，请检查QQ版本");
                }
            }
        });

        groupListLayout.addView(itemLayout);
    }
    
    scrollView.addView(groupListLayout);
    
    LinearLayout.LayoutParams scrollParams = new LinearLayout.LayoutParams(
        ViewGroup.LayoutParams.MATCH_PARENT,
        0, 1.0f 
    );
    scrollParams.setMargins(0, 0, 0, dp(8));
    root.addView(scrollView, scrollParams);
    
    LinearLayout buttonContainer = new LinearLayout(activity);
    buttonContainer.setOrientation(LinearLayout.HORIZONTAL);
    buttonContainer.setGravity(Gravity.CENTER);
    buttonContainer.setPadding(0, dp(16), 0, 0);
    
    TextView closeBtn = new TextView(activity);
    closeBtn.setText("关闭");
    closeBtn.setTextSize(15);
    closeBtn.setTextColor(COLOR_BTN_TEXT);
    closeBtn.setGravity(Gravity.CENTER);
    closeBtn.setPadding(0, dp(14), 0, dp(14));

    GradientDrawable btnBg = new GradientDrawable();
    btnBg.setColor(COLOR_PM);
    btnBg.setCornerRadius(dp(25));
    closeBtn.setBackground(btnBg);

    LinearLayout.LayoutParams btnParams = new LinearLayout.LayoutParams(
        ViewGroup.LayoutParams.MATCH_PARENT,
        ViewGroup.LayoutParams.WRAP_CONTENT
    );
    closeBtn.setLayoutParams(btnParams);

    closeBtn.setOnClickListener(new View.OnClickListener() {
        public void onClick(View v) {
            dialog.dismiss();
        }
    });
    
    buttonContainer.addView(closeBtn);
    root.addView(buttonContainer);

    dialog.setContentView(root);
    dialog.show();

    Window window = dialog.getWindow();
    if (window != null) {
        window.setLayout(
            (int)(activity.getResources().getDisplayMetrics().widthPixels * 0.85),
            ViewGroup.LayoutParams.WRAP_CONTENT
        );
        window.setBackgroundDrawable(new android.graphics.drawable.ColorDrawable(Color.TRANSPARENT));
    }
}

private int dp(int d) {
    Activity a = getActivity();
    if (a == null) return d;
    float density = a.getResources().getDisplayMetrics().density;
    return (int) (d * density + 0.5f); 
}

onCheck();