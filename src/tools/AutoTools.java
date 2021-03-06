package tools;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;

import org.apache.commons.io.IOUtils;

import tools.entity.ApkInfo;
import tools.utils.ApkUtil;

public class AutoTools {
	// 原始apk
	private static String orgapk;
	// 签名工具
	private static String keystore;
	// 用户名
	private static String username;
	// 密码
	private static String password;
	// 原始渠道号
	private static String orgchannel;
	// 原始app名称
	private static String orgappname;
	// 原始包名
	private static String orgpkgname;
	// 原始 ICON
	private static String orgIconname;
	// 原始 VersionCode
	private static String orgVersionCode;
	// 原始VersionName
	private static String orgVersionName;
	// 渠道号列表 map.txt文件读取
	private static ArrayList<String> channels = new ArrayList<String>();
	// 包名
	private static String newpkgname = null;
	// 应用名称
	private static String newappname = null;
	// 版本号
	private static String newvercode = null;
	// 版本名称
	private static String newvername = null;
	// 图标
	private static String newiconname = null;

	// 当前目录
	private static String curDir = null;

	public static void main(String[] args) {
		System.out.println("Autotools v1.2 by panyingyun@gamil.com.");
		keystore = "dino.keystore";
		username = "shlpyy";
		password = "shlpyy";
		orgchannel = "CLA";

		if (args.length < 2) {
			System.out
					.println("usage: java -jar autotools.jar orgapk  pkgname appname(可选) version iconname(可选)");
			System.out
					.println("example: java -jar autotools.jar sgz.apk  com.clemu.capcom2013  名将2013(可选) 3.2 icon1(可选)");
			return;
		}

		orgapk = args[0];
		newpkgname = args[1];
		if (args.length > 2)
			newappname = args[2];
		if (args.length > 3)
			newvername = args[3];
		if (args.length > 4)
			newiconname = args[4];

		curDir = new File("").getAbsolutePath();
		ApkInfo info = null;
		try {
			info = new ApkUtil().getApkInfo(orgapk);
		} catch (Exception e) {
			e.printStackTrace();
		}
		orgpkgname = info.getPackageName();
		orgappname = info.getApplicationLable();
		orgIconname = info.getApplicationIcon();
		orgVersionCode = info.getVersionCode();
		orgVersionName = info.getVersionName();
		System.out.println("orgapk = " + orgapk);
		System.out.println("orgchannel = " + orgchannel);
		System.out.println("orgpkgname = " + orgpkgname);
		System.out.println("orgappname = " + orgappname);
		System.out.println("orgIconname = " + orgIconname);
		System.out.println("orgVersionCode = " + orgVersionCode);
		System.out.println("orgVersionName = " + orgVersionName);
		System.out.println("curDir = " + curDir);

		// clean org files
		cleanOrgfiles();
		// read channels from map.txt
		readChannels();
		// modify channels and build apks
		autoPkgApks();
		// delete old apks
		delApks();
		// clean org files
		try {
			Thread.sleep(5000);
		} catch (InterruptedException e) {
		}
		cleanOrgfiles();
	}

	// 清理之前留下来的
	private static void cleanOrgfiles() {
		String p_main_bak = curDir + "\\AndroidManifest.xml";
		File f_mani_bak = new File(p_main_bak);
		boolean isSuccess1 = f_mani_bak.delete();
		System.out.println("delete Manifest isSuccess = " + isSuccess1);

		String dir = orgapk.split(".apk")[0];
		boolean isSuccess2 = FileUtils.deleteDirectory(dir);
		System.out.println("delete " + dir + " isSuccess = " + isSuccess2);
	}

	// 读取渠道号
	private static void readChannels() {
		File f = new File("map.txt");
		if (f.exists()) {
			try {
				BufferedReader bReader = new BufferedReader(new FileReader(f));
				String line = null;
				while ((line = bReader.readLine()) != null) {
					if (!line.isEmpty())
						channels.add(line);
				}
				bReader.close();
			} catch (Exception e) {
				e.printStackTrace();
			}
		}

		// for (int i = 0; i < channels.size(); i++) {
		// System.out.println("channels = " + channels.get(i));
		// }
	}

	// 修改string.xml中应用名称
	private static void modifyAppname() {
		if (isEmpty(newappname))
			return;
		String dir = orgapk.split(".apk")[0];
		File packDir = new File(dir);

		String p_string = packDir.getAbsolutePath() + "\\res\\values\\"
				+ "strings.xml";
		File f_string = new File(p_string);
		try {
			String ss = IOUtils
					.toString(new FileInputStream(f_string), "UTF-8");
			// System.out.println("替换应用名前 ss = " + ss);
			ss = ss.replaceAll(orgappname, newappname);
			// System.out.println("替换应用名后 ss = " + ss);
			IOUtils.write(ss.getBytes("UTF-8"), new FileOutputStream(p_string));
			System.out.println("应用名称替换 success!!!!");
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	// 修改版本号，并且备份AndroidManifest.xml
	// 之所以要备份，是因为
	// 循环都是需要对原AndroidManifest.xml进行修改的
	private static void modifyAndroidManifest(String path, String backupPath) {
		String cpStr;
		try {
			cpStr = IOUtils.toString(new FileInputStream(path), "UTF-8");
			//替换VERSIONCODE VERSIONNAME
			if (!isEmpty(newvername)) {
				System.out.println("newvername = "+newvername);
				System.out.println("orgVersionCode = " + orgVersionCode);
				System.out.println("orgVersionName = " + orgVersionName);
				
				newvercode = "android:versionCode=\""+(Integer.valueOf(orgVersionCode).intValue()+1)+"\"";
				orgVersionCode = "android:versionCode=\""+orgVersionCode+"\"";
				newvername="android:versionName=\""+newvername+"\""; 
				orgVersionName = "android:versionName=\""+orgVersionName+"\"";
				System.out.println("newvername = "+newvername);
				System.out.println("newvercode = "+newvercode);
				System.out.println("orgVersionCode = " + orgVersionCode);
				System.out.println("orgVersionName = " + orgVersionName);
				cpStr = cpStr.replaceFirst(orgVersionCode, newvercode);
				cpStr = cpStr.replaceFirst(orgVersionName, newvername);
				System.out.println("版本号替换 success!!!!");
			}
			IOUtils.write(cpStr.getBytes("UTF-8"), new FileOutputStream(
					backupPath));
			
		} catch (FileNotFoundException e) {
			System.out.println("文件路径不存在，备份失败!!!!");
			e.printStackTrace();
		} catch (IOException e) {
			System.out.println("异常错误，文件备份失败!!!!");
			e.printStackTrace();
		}

	}

	// 修改应用图标
	private static void modifyAppicon() {
		if (isEmpty(newiconname))
			return;
		String dir = orgapk.split(".apk")[0];
		File packDir = new File(dir);
		File destFile = new File(packDir.getAbsolutePath() + File.separator
				+ orgIconname);
		File srcFile = new File(curDir + File.separator + newiconname);
		long len = FileUtils.copyFile(srcFile, destFile);
		if (len > 0) {
			System.out.println("应用图标替换 success!!!!");
		} else {
			System.out.println("应用图标替换 fail!!!!");
		}
	}

	private static void autoPkgApks() {

		// 解压 OK!!!
		String decode = "cmd.exe /C java -jar apktool.jar d -f " + orgapk;
		runCmd(decode);
		// 修改应用名
		modifyAppname();
		// 修改应用图标
		modifyAppicon();
		// 备份AndroidMainifest.xml和循环修改渠道号，打包不同的渠道号
		String dir = orgapk.split(".apk")[0];
		File packDir = new File(dir);
		try {
			String p_mani = packDir.getAbsolutePath() + "\\AndroidManifest.xml";
			String p_main_bak = curDir + "\\AndroidManifest.xml";
			// 备份AndroidMainifest.xml 并且 修改版本号
			modifyAndroidManifest(p_mani, p_main_bak);

			// 创建apk目录
			File f = new File("apk");
			if (!f.exists()) {
				f.mkdir();
			}

			// 循环修改渠道号和包名，并且重新打包签名
			for (String channel : channels) {
				System.out.println("channel = " + channel);
				String ssCp = IOUtils.toString(new FileInputStream(p_main_bak),
						"UTF-8");
				// System.out.println("修改包名和渠道号之前：ssCp = " + ssCp);
				ssCp = ssCp.replaceAll("\"" + orgpkgname + "\"", "\""
						+ newpkgname + "\"");
				ssCp = ssCp.replaceAll("\"" + orgchannel + "\"", "\"" + channel
						+ "\"");
				// System.out.println("修改包名和渠道号之后：ssCp = " + ssCp);
				IOUtils.write(ssCp.getBytes("UTF-8"), new FileOutputStream(
						p_mani));
				// 打包
				String orgname = orgapk.split(".apk")[0];
				String unsignapk = orgname + "_" + channel + "_un.apk";
				String cmdencode = String.format(
						"cmd.exe /C java -jar apktool.jar b %s %s", dir,
						unsignapk);
				runCmd(cmdencode);
				// 签名
				String signapk = orgname + "_" + channel + "_un_sign.apk";
				String cmdsign = String
						.format("cmd.exe /C jarsigner -verbose -keystore %s -storepass %s -signedjar %s %s %s",
								keystore, password, signapk, unsignapk,
								username);
				runCmd(cmdsign);
				// zipalign优化
				String zipalignapk = "./apk/" + orgname + "_" + channel
						+ ".apk";
				String cmdZipalign = String.format(
						"cmd.exe /C zipalign -f -v 4 %s %s", signapk,
						zipalignapk);
				runCmd(cmdZipalign);

				// 删除未签名的临时文件包
				File unsignfile = new File(unsignapk);
				unsignfile.delete();
				// 删除签名的临时文件包
				File signfile = new File(signapk);
				signfile.delete();
			}
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}

	}

	private static void delApks() {

	}

	// 执行命令行
	private static boolean runCmd(String cmd) {
		boolean isSuccess = false;
		System.out.println("run cmd = " + cmd + " begin");
		Runtime rt = Runtime.getRuntime();
		try {
			Process process = rt.exec(cmd);
			BufferedReader bReader = new BufferedReader(new InputStreamReader(
					process.getInputStream()));
			String msg = null;
			while ((msg = bReader.readLine()) != null) {
				System.out.println(msg);
			}
			bReader.close();
			isSuccess = true;
		} catch (Exception e) {
			e.printStackTrace();
			isSuccess = false;
		}
		if (isSuccess) {
			System.out.println("run cmd = " + cmd + " success!!!!");
		} else {
			System.out.println("run cmd = " + cmd + " fail!!!!");
		}
		return isSuccess;
	}

	private static boolean isEmpty(String str) {
		return (str == null || str.length() == 0);
	}
}
