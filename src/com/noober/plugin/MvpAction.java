package com.noober.plugin;

import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.CommonDataKeys;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.Messages;
import com.intellij.openapi.vfs.VirtualFile;

import java.io.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class MvpAction extends AnAction {
    Project project;
    VirtualFile selectGroup;

    private final String JAVA_PATH = "/app/src/main/java/";

    private final String KOTLIN_PATH = "/app/src/main/kotlin/";

    @Override
    public void actionPerformed(AnActionEvent e) {
        project = e.getProject();
        selectGroup = CommonDataKeys.VIRTUAL_FILE.getData(e.getDataContext());
        String fileName = Messages.showInputDialog(project, "请输入类名前缀", "NewMvpGroup", Messages.getQuestionIcon());
        if (fileName == null || fileName.equals("")) {
            System.out.print("没有输入内容");
            return;
        }
        String packageName = "";
        if (selectGroup.getPath().indexOf(JAVA_PATH) >= 0) {
            packageName = selectGroup.getPath().substring(selectGroup.getPath().indexOf(JAVA_PATH) + JAVA_PATH.length()).replace("/", ".");
        } else if (selectGroup.getPath().indexOf(KOTLIN_PATH) >= 0) {
            packageName = selectGroup.getPath().substring(selectGroup.getPath().indexOf(KOTLIN_PATH) + KOTLIN_PATH.length()).replace("/", ".");
        } else {
            packageName = selectGroup.getPath().replace(project.getBasePath(), "").replace("/src/", "").replace("/", ".");
        }
        createClassMvp(fileName, packageName);
        selectGroup.refresh(false, true);
    }


    /**
     * 创建MVP架构
     */
    private void createClassMvp(String fileName, String packageName) {
        String path = selectGroup.getPath();
        String layoutName = "activity_" + camel2Underline(fileName);

        // 获取文件字符串内容
        String view = readTemplate("View.txt").replace("&package&", packageName).replace("&Prefix&", fileName);
        String presenter = readTemplate("Presenter.txt").replace("&package&", packageName).replace("&Prefix&", fileName);
        String activity = readTemplate("Activity.txt").replace("&package&", packageName).replace("&Prefix&", fileName)
                .replace("&layoutName&", layoutName);
        String layoutPath = project.getBasePath() + "/app/src/main/res/layout/";
        String layout = readTemplate("layout.txt");

        // 写入文件
        writetoFile(view, path, fileName + "View.kt");
        writetoFile(presenter, path, fileName + "Presenter.kt");
        writetoFile(activity, path, fileName + "Activity.kt");
        writetoFile(layout, layoutPath, layoutName + ".xml");
    }


    /**
     * 获取文件内容
     */
    private String readTemplate(String filename) {
        InputStream in = null;
        in = this.getClass().getResourceAsStream("template/" + filename);
        String content = "";
        try {
            content = new String(readStream(in));
        } catch (Exception e) {
        }
        return content;
    }

    private byte[] readStream(InputStream inStream) throws Exception {
        ByteArrayOutputStream outSteam = new ByteArrayOutputStream();
        try {
            byte[] buffer = new byte[1024];
            int len = -1;
            while ((len = inStream.read(buffer)) != -1) {
                outSteam.write(buffer, 0, len);
                System.out.println(new String(buffer));
            }

        } catch (IOException e) {
        } finally {
            outSteam.close();
            inStream.close();
        }
        return outSteam.toByteArray();
    }

    /**
     * 写入文件
     */
    private void writetoFile(String content, String filepath, String filename) {
        try {
            File floder = new File(filepath);
            // if file doesnt exists, then create it
            if (!floder.exists()) {
                floder.mkdirs();
            }
            File file = new File(filepath + "/" + filename);
            if (!file.exists()) {
                file.createNewFile();
            }

            FileWriter fw = new FileWriter(file.getAbsoluteFile());
            BufferedWriter bw = new BufferedWriter(fw);
            bw.write(content);
            bw.close();

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * 驼峰法转下划线
     *
     * @param line 源字符串
     * @return 转换后的字符串
     */
    public static String camel2Underline(String line) {
        if (line == null || "".equals(line)) {
            return "";
        }
        line = String.valueOf(line.charAt(0)).toUpperCase().concat(line.substring(1));
        StringBuffer sb = new StringBuffer();
        Pattern pattern = Pattern.compile("[A-Z]([a-z\\d]+)?");
        Matcher matcher = pattern.matcher(line);
        while (matcher.find()) {
            String word = matcher.group();
            sb.append(word.toLowerCase());
            sb.append(matcher.end() == line.length() ? "" : "_");
        }
        return sb.toString();
    }
}
