import android.widget.ScrollView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.drawable.GradientDrawable;
import android.view.Gravity;
import android.view.View;
import android.view.WindowManager;
import android.app.AlertDialog;
import android.app.Activity;
import android.widget.Button;
import android.content.DialogInterface;
import android.os.Build;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.util.List;
import java.util.ArrayList;

private static class Step {
    String text;
    List imageFileNames;
    
    Step(String text, String imageFileNames) {
        this.text = text;
        this.imageFileNames = new ArrayList();
        if (imageFileNames != null && imageFileNames.trim().length() > 0) {
            String[] images = imageFileNames.split(",");
            for (int i = 0; i < images.length; i++) {
                String trimmed = images[i].trim();
                if (trimmed.length() > 0) {
                    this.imageFileNames.add(trimmed);
                }
            }
        }
    }
    
    Step(String text, List imageFileNames) {
        this.text = text;
        this.imageFileNames = imageFileNames;
    }
}

private List getSteps() {
    List steps = new ArrayList();
    steps.add(new Step("⊙发送「图签菜单」查看完整指令", "图签菜单.jpg"));
    steps.add(new Step("1. 点击「签到脚本配置」进入配置界面，开启功能开关，开启后提示功能已开启或关闭，然后需要点击弹窗右上角“完成”保存设置", "1-2.jpg"));
    steps.add(new Step("2. 发送签到指令进行签到以及获得签到背景图片，他人的背景图也可以进行获取(前提是已经签到)", "2-2.jpg"));
    steps.add(new Step("3. 代签功能，可以帮助他人代签，也可以生成签到图", ""));
    steps.add(new Step("4. 补签功能，发送“补签”等指令可进行补签，具体指令可在「图签菜单」中了解，补签功能不会生成签到卡片和获得签到积分(小小的惩罚)", ""));
    steps.add(new Step("                不可为他人补签哦~", ""));
    steps.add(new Step("5. 积分系统(包括点赞、抽盲盒、积分商城等)，可进行积分兑换和盲盒抽取，具体指令在「图签菜单」中", "积分-2.jpg"));
    steps.add(new Step("6. 若未进行签到直接发送「获得签到背景」或「获取签到背景」则会返回签到提示", ""));
    steps.add(new Step("7. 点击「查看签到数据」可以查询并管理本群的签到数据", "5-1.jpg, 5-2.jpg"));
    steps.add(new Step("8. 为避免误操作，点击清理按钮后会弹出二次提示", "6.jpg"));
    steps.add(new Step("9. 脚本给出以下指令(有亿点点多)，其中英文指令不分大小写，也就是说发送“QD”或“DK”等大写格式或大小写混合格式也可以触发脚本(图中积分数据为测试所得，实际签到积分为随机5~10分)", "指令1.jpg, 指令2.jpg, 签到示例.jpg"));
    steps.add(new Step("10. 发送“签到排名”获得天数榜和积分榜排名（同一张图）", ""));
    steps.add(new Step("11. 脚本加载者第一次「抢红包」不会触发判断，第二次发送才会触发，避免\"开挂\"", ""));
    steps.add(new Step("12. 达到条件可以解锁成就，为保持神秘，这里不多放", "成就系统.jpg"));
    steps.add(new Step("13. 签到图片若受部分原因响应（如网络或API失效），可返回默认背景", ""));
    steps.add(new Step("14. 在签到数据管理页面可以编辑群成员的签到数据", "编辑页面.jpg"));
    steps.add(new Step("◎进入脚本目录中将“sign”和“sign_photos”文件夹备份下来(复制到脚本目录以外目录)，更新后再复制回来即可保留数据", "备份-output.jpg, 备份-input.jpg"));
    return steps;
}

private boolean isImageFile(String filePath) {
    if (filePath == null || filePath.length() == 0) {
        return false;
    }
    String lowerPath = filePath.toLowerCase();
    return lowerPath.endsWith(".jpg") || 
           lowerPath.endsWith(".jpeg") || 
           lowerPath.endsWith(".png") || 
           lowerPath.endsWith(".webp") || 
           lowerPath.endsWith(".bmp");
}

public void 脚本使用说明(String g, String u, int t) {
    final Activity activity = getActivity();
    if (activity == null || activity.isFinishing()) {
        return;
    }
    
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1) {
        if (activity.isDestroyed()) {
            return;
        }
    }
    
    activity.runOnUiThread(new Runnable() {
        public void run() {
            AlertDialog dialog = null;
            try {
                int screenWidth = activity.getResources().getDisplayMetrics().widthPixels;
                
                ScrollView scrollView = new ScrollView(activity);
                scrollView.setLayoutParams(new LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT, 
                    LinearLayout.LayoutParams.WRAP_CONTENT));
                scrollView.setFillViewport(true);
                
                LinearLayout layout = new LinearLayout(activity);
                layout.setOrientation(LinearLayout.VERTICAL);
                layout.setPadding(dp2px(20), dp2px(20), dp2px(20), dp2px(20));
                
                List steps = getSteps();
                for (int i = 0; i < steps.size(); i++) {
                    Step step = (Step) steps.get(i);
                    
                    TextView textView = new TextView(activity);
                    textView.setText(step.text);
                    textView.setTextSize(14);
                    textView.setTextColor(Color.parseColor("#333333"));
                    int topPadding = (i == 0) ? 0 : dp2px(16);
                    textView.setPadding(0, topPadding, 0, dp2px(12));
                    layout.addView(textView);
                    
                    if (step.imageFileNames != null && step.imageFileNames.size() > 0) {
                        for (int j = 0; j < step.imageFileNames.size(); j++) {
                            String imageFileName = (String) step.imageFileNames.get(j);
                            String imagePath = appPath + "/materials/" + imageFileName;
                            
                            ImageView imageView = createSmallImageView(activity, imagePath, screenWidth);
                            if (imageView != null) {
                                final String finalImagePath = imagePath;
                                imageView.setOnClickListener(new View.OnClickListener() {
                                    public void onClick(View v) {
                                        showFullImageZoom(finalImagePath);
                                    }
                                });
                                layout.addView(imageView);
                            } else {
                                TextView placeholder = createImagePlaceholder(activity, imageFileName);
                                layout.addView(placeholder);
                            }
                        }
                    }
                }
                
                scrollView.addView(layout);
                
                int theme;
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.ICE_CREAM_SANDWICH) {
                    theme = AlertDialog.THEME_DEVICE_DEFAULT_LIGHT;
                } else {
                    theme = AlertDialog.THEME_HOLO_LIGHT;
                }
                
                AlertDialog.Builder builder = new AlertDialog.Builder(activity, theme);
                builder.setTitle("脚本使用说明");
                builder.setView(scrollView);
                builder.setPositiveButton("确定", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                });
                builder.setCancelable(true);
                dialog = builder.create();
                dialog.show();
                
                WindowManager.LayoutParams layoutParams = dialog.getWindow().getAttributes();
                layoutParams.width = (int) (screenWidth * 0.9);
                layoutParams.height = (int) (screenWidth * 1.2);
                layoutParams.gravity = Gravity.CENTER;
                dialog.getWindow().setAttributes(layoutParams);
                
            } catch (Exception e) {
                toast("显示失败：" + e.getMessage());
                if (dialog != null && dialog.isShowing()) {
                    try {
                        dialog.dismiss();
                    } catch (Exception ex) {}
                }
            }
        }
    });
}

private ImageView createSmallImageView(Activity activity, String imagePath, int screenWidth) {
    if (activity == null || activity.isFinishing()) {
        return null;
    }
    
    Bitmap bitmap = null;
    try {
        File imageFile = new File(imagePath);
        if (!imageFile.exists() || !imageFile.canRead()) {
            return null;
        }
        
        if (!isValidImageFile(imagePath)) {
            return null;
        }
        
        int targetWidth = (int) (screenWidth * 0.3);
        bitmap = decodeSampledBitmapFromFile(imagePath, targetWidth, -1);
        
        if (bitmap != null && !bitmap.isRecycled()) {
            ImageView imageView = new ImageView(activity);
            imageView.setImageBitmap(bitmap);
            imageView.setScaleType(ImageView.ScaleType.FIT_CENTER);
            imageView.setAdjustViewBounds(true);
            
            LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                targetWidth,
                LinearLayout.LayoutParams.WRAP_CONTENT);
            params.gravity = Gravity.CENTER_HORIZONTAL;
            params.setMargins(0, dp2px(8), 0, dp2px(16));
            imageView.setLayoutParams(params);
            
            return imageView;
        }
    } catch (OutOfMemoryError e) {
        System.gc();
        e.printStackTrace();
    } catch (Exception e) {
        e.printStackTrace();
    }
    
    if (bitmap != null && !bitmap.isRecycled()) {
        bitmap.recycle();
    }
    return null;
}

private Bitmap decodeSampledBitmapFromFile(String filePath, int reqWidth, int reqHeight) {
    BitmapFactory.Options options = new BitmapFactory.Options();
    options.inJustDecodeBounds = true;
    options.inPreferredConfig = Bitmap.Config.RGB_565;
    
    try {
        BitmapFactory.decodeFile(filePath, options);
    } catch (Exception e) {
        e.printStackTrace();
        return null;
    }
    
    options.inSampleSize = calculateInSampleSize(options, reqWidth, reqHeight);
    
    options.inJustDecodeBounds = false;
    options.inPreferredConfig = Bitmap.Config.RGB_565;
    options.inPurgeable = true;
    options.inInputShareable = true;
    options.inDither = true;
    options.inScaled = true;
    
    try {
        return BitmapFactory.decodeFile(filePath, options);
    } catch (OutOfMemoryError e) {
        options.inSampleSize *= 2;
        try {
            System.gc();
            return BitmapFactory.decodeFile(filePath, options);
        } catch (OutOfMemoryError e2) {
            e2.printStackTrace();
            return null;
        }
    } catch (Exception e) {
        e.printStackTrace();
        return null;
    }
}

private int calculateInSampleSize(BitmapFactory.Options options, int reqWidth, int reqHeight) {
    int height = options.outHeight;
    int width = options.outWidth;
    int inSampleSize = 1;
    
    if (reqWidth <= 0 && reqHeight <= 0) {
        return inSampleSize;
    }
    
    if (reqWidth > 0 && width > reqWidth) {
        inSampleSize = Math.round((float) width / (float) reqWidth);
    }
    
    if (reqHeight > 0 && height > reqHeight) {
        int heightSampleSize = Math.round((float) height / (float) reqHeight);
        inSampleSize = Math.max(inSampleSize, heightSampleSize);
    }
    
    int powerOfTwo = 1;
    while (powerOfTwo < inSampleSize) {
        powerOfTwo *= 2;
    }
    
    return powerOfTwo;
}

private boolean isValidImageFile(String filePath) {
    if (filePath == null || filePath.length() == 0) {
        return false;
    }
    
    String lowerPath = filePath.toLowerCase();
    boolean validExtension = false;
    
    if (lowerPath.endsWith(".jpg") || lowerPath.endsWith(".jpeg") || 
        lowerPath.endsWith(".png") || lowerPath.endsWith(".webp") || 
        lowerPath.endsWith(".bmp")) {
        validExtension = true;
    }
    
    if (!validExtension) {
        return false;
    }
    
    File file = new File(filePath);
    if (!file.exists() || file.length() == 0) {
        return false;
    }
    
    if (file.length() > 20 * 1024 * 1024) {
        return false;
    }
    
    return verifyImageHeader(filePath);
}

private boolean verifyImageHeader(String filePath) {
    InputStream is = null;
    try {
        is = new FileInputStream(filePath);
        byte[] header = new byte[8];
        int read = is.read(header);
        
        if (read >= 2) {
            if (header[0] == (byte) 0xFF && header[1] == (byte) 0xD8) {
                return true;
            }
            if (header[0] == (byte) 0x89 && header[1] == (byte) 0x50 && 
                header[2] == (byte) 0x4E && header[3] == (byte) 0x47) {
                return true;
            }
            if (header[0] == (byte) 0x47 && header[1] == (byte) 0x49 && header[2] == (byte) 0x46) {
                return true;
            }
            if (header[0] == (byte) 0x42 && header[1] == (byte) 0x4D) {
                return true;
            }
            if (read >= 12 && header[0] == (byte) 0x52 && header[1] == (byte) 0x49 && 
                header[2] == (byte) 0x46 && header[3] == (byte) 0x46) {
                return true;
            }
        }
    } catch (Exception e) {
        e.printStackTrace();
    } finally {
        if (is != null) {
            try { is.close(); } catch (Exception e) {}
        }
    }
    return true;
}

private GradientDrawable createRoundBackground(int color, int radius) {
    GradientDrawable drawable = new GradientDrawable();
    drawable.setColor(color);
    drawable.setCornerRadius(dp2px(radius));
    return drawable;
}

private void setViewBackground(View view, GradientDrawable drawable) {
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
        view.setBackground(drawable);
    } else {
        view.setBackgroundDrawable(drawable);
    }
}

private void showFullImageZoom(String imagePath) {
    final Activity activity = getActivity();
    if (activity == null || activity.isFinishing()) {
        return;
    }
    
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1) {
        if (activity.isDestroyed()) {
            return;
        }
    }
    
    AlertDialog dialog = null;
    try {
        File imageFile = new File(imagePath);
        if (!imageFile.exists() || !isValidImageFile(imagePath)) {
            toast("图片文件不存在或已损坏");
            return;
        }
        
        int screenWidth = activity.getResources().getDisplayMetrics().widthPixels;
        int screenHeight = activity.getResources().getDisplayMetrics().heightPixels;
        int targetWidth = (int) (screenWidth * 0.8);
        
        BitmapFactory.Options options = new BitmapFactory.Options();
        options.inJustDecodeBounds = true;
        BitmapFactory.decodeFile(imagePath, options);
        
        int originalWidth = options.outWidth;
        int originalHeight = options.outHeight;
        
        Bitmap bitmap = decodeSampledBitmapFromFile(imagePath, targetWidth, -1);
        
        if (bitmap != null && !bitmap.isRecycled()) {
            int theme;
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.ICE_CREAM_SANDWICH) {
                theme = AlertDialog.THEME_DEVICE_DEFAULT_LIGHT;
            } else {
                theme = AlertDialog.THEME_HOLO_LIGHT;
            }
            
            AlertDialog.Builder builder = new AlertDialog.Builder(activity, theme);
            
            LinearLayout mainLayout = new LinearLayout(activity);
            mainLayout.setOrientation(LinearLayout.VERTICAL);
            mainLayout.setBackgroundColor(Color.WHITE);
            mainLayout.setPadding(dp2px(20), dp2px(20), dp2px(20), dp2px(20));
            
            GradientDrawable dialogBg = createRoundBackground(Color.WHITE, 24);
            setViewBackground(mainLayout, dialogBg);
            
            TextView titleView = new TextView(activity);
            titleView.setText("查看图片");
            titleView.setTextSize(18);
            titleView.setTextColor(Color.parseColor("#333333"));
            titleView.setGravity(Gravity.CENTER);
            titleView.setPadding(0, 0, 0, dp2px(16));
            mainLayout.addView(titleView);
            
            int imageWidth = targetWidth;
            int imageHeight;
            if (originalWidth > 0) {
                imageHeight = (int) ((float) originalHeight / originalWidth * imageWidth);
            } else {
                imageHeight = bitmap.getHeight() * imageWidth / bitmap.getWidth();
            }
            
            int titleHeight = dp2px(18 + 16);
            int buttonHeight = dp2px(12 + 12 + 16);
            int paddingVertical = dp2px(20 + 20);
            int imageMargins = dp2px(10 + 10);
            int otherElementsHeight = titleHeight + buttonHeight + paddingVertical + imageMargins;
            int maxAvailableHeight = screenHeight - otherElementsHeight;
            
            LinearLayout imageContainer = new LinearLayout(activity);
            imageContainer.setOrientation(LinearLayout.VERTICAL);
            imageContainer.setGravity(Gravity.CENTER_HORIZONTAL);
            
            ImageView imageView = new ImageView(activity);
            imageView.setImageBitmap(bitmap);
            imageView.setScaleType(ImageView.ScaleType.FIT_CENTER);
            imageView.setAdjustViewBounds(true);
            
            LinearLayout.LayoutParams imageParams = new LinearLayout.LayoutParams(
                imageWidth,
                imageHeight);
            imageParams.gravity = Gravity.CENTER_HORIZONTAL;
            imageParams.setMargins(0, dp2px(10), 0, dp2px(10));
            imageView.setLayoutParams(imageParams);
            imageContainer.addView(imageView);
            
            if (imageHeight > maxAvailableHeight) {
                ScrollView scrollView = new ScrollView(activity);
                scrollView.setLayoutParams(new LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT,
                    maxAvailableHeight));
                scrollView.setVerticalScrollBarEnabled(true);
                scrollView.setFillViewport(false);
                scrollView.addView(imageContainer);
                mainLayout.addView(scrollView);
            } else {
                mainLayout.addView(imageContainer);
            }
            
            Button closeButton = new Button(activity);
            closeButton.setText("关闭");
            closeButton.setTextSize(14);
            closeButton.setTextColor(Color.WHITE);
            
            GradientDrawable buttonBg = createRoundBackground(Color.parseColor("#FF5722"), 8);
            setViewBackground(closeButton, buttonBg);
            closeButton.setPadding(dp2px(30), dp2px(12), dp2px(30), dp2px(12));
            
            LinearLayout.LayoutParams buttonParams = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.WRAP_CONTENT,
                LinearLayout.LayoutParams.WRAP_CONTENT);
            buttonParams.gravity = Gravity.CENTER_HORIZONTAL;
            buttonParams.setMargins(0, dp2px(16), 0, 0);
            closeButton.setLayoutParams(buttonParams);
            
            mainLayout.addView(closeButton);
            
            builder.setView(mainLayout);
            builder.setCancelable(true);
            
            final AlertDialog finalDialog = builder.create();
            closeButton.setOnClickListener(new View.OnClickListener() {
                public void onClick(View v) {
                    finalDialog.dismiss();
                }
            });
            
            finalDialog.show();
            
            WindowManager.LayoutParams params = finalDialog.getWindow().getAttributes();
            params.width = (int) (screenWidth * 0.9);
            params.height = WindowManager.LayoutParams.WRAP_CONTENT;
            params.gravity = Gravity.CENTER;
            finalDialog.getWindow().setAttributes(params);
            finalDialog.getWindow().setBackgroundDrawableResource(android.R.color.transparent);
            
            dialog = finalDialog;
        } else {
            toast("图片加载失败");
        }
    } catch (OutOfMemoryError e) {
        System.gc();
        toast("图片过大，内存不足");
    } catch (Exception e) {
        toast("图片加载失败：" + e.getMessage());
    }
}

private TextView createImagePlaceholder(Activity activity, String imageFileName) {
    TextView placeholder = new TextView(activity);
    placeholder.setText("图片缺失\n你可以补充或删去代码中引用图片的部分：" + imageFileName);
    placeholder.setTextSize(11);
    placeholder.setTextColor(Color.parseColor("#FF6B6B"));
    placeholder.setGravity(Gravity.CENTER);
    placeholder.setPadding(0, dp2px(8), 0, dp2px(16));
    placeholder.setBackgroundColor(Color.parseColor("#FFF0F0"));
    placeholder.setMinHeight(dp2px(80));
    LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
        LinearLayout.LayoutParams.MATCH_PARENT,
        LinearLayout.LayoutParams.WRAP_CONTENT);
    params.setMargins(0, dp2px(8), 0, dp2px(16));
    placeholder.setLayoutParams(params);
    return placeholder;
}

private int dp2px(int dp) {
    try {
        Activity activity = getActivity();
        if (activity != null) {
            android.util.DisplayMetrics metrics = activity.getResources().getDisplayMetrics();
            return (int) (dp * metrics.density + 0.5f);
        }
    } catch (Exception e) {}
    return dp * 2;
}

private void toast(String message) {
    try {
        final Activity activity = getActivity();
        if (activity != null && !activity.isFinishing()) {
            final String msg = message;
            activity.runOnUiThread(new Runnable() {
                public void run() {
                    android.widget.Toast.makeText(activity, msg, android.widget.Toast.LENGTH_SHORT).show();
                }
            });
        }
    } catch (Exception e) {
        e.printStackTrace();
    }
}