package com.winlator.cmod.xenvironment;

import android.content.Context;

import com.winlator.cmod.MainActivity;
import com.winlator.cmod.R;
import com.winlator.cmod.SettingsFragment;
import com.winlator.cmod.container.Container;
import com.winlator.cmod.container.ContainerManager;
import com.winlator.cmod.contents.AdrenotoolsManager;
import com.winlator.cmod.core.AppUtils;
import com.winlator.cmod.core.DownloadProgressDialog;
import com.winlator.cmod.core.FileUtils;
import com.winlator.cmod.core.PreloaderDialog;
import com.winlator.cmod.core.TarCompressorUtils;
import com.winlator.cmod.core.WineInfo;

import org.json.JSONObject;

import java.io.File;
import java.util.ArrayList;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicLong;

public abstract class ImageFsInstaller {
    public static final byte LATEST_VERSION = 21;

    private static void resetContainerImgVersions(Context context) {
        ContainerManager manager = new ContainerManager(context);
        for (Container container : manager.getContainers()) {
            String imgVersion = container.getExtra("imgVersion");
            String wineVersion = container.getWineVersion();
            if (!imgVersion.isEmpty() && WineInfo.isMainWineVersion(wineVersion) && Short.parseShort(imgVersion) <= 5) {
                container.putExtra("wineprefixNeedsUpdate", "t");
            }

            container.putExtra("imgVersion", null);
            container.saveData();
        }
    }

    public static void installWineFromAssets(final MainActivity activity) {
        String[] versions = activity.getResources().getStringArray(R.array.wine_entries);
        File rootDir = ImageFs.find(activity).getRootDir();
        for (String version : versions) {
            File outFile = new File(rootDir, "/opt/" + version);
            outFile.mkdirs();
            TarCompressorUtils.extract(TarCompressorUtils.Type.XZ, activity, version + ".txz", outFile);
        }
    }

    public static void installDriversFromAssets(final MainActivity activity) {
        AdrenotoolsManager adrenotoolsManager = new AdrenotoolsManager(activity);
        String[] adrenotoolsAssetDrivers = activity.getResources().getStringArray(R.array.wrapper_graphics_driver_version_entries);

        for (String driver : adrenotoolsAssetDrivers)
            adrenotoolsManager.extractDriverFromResources(driver);
    }

    public static void installFromAssets(final MainActivity activity) {
        AppUtils.keepScreenOn(activity);
        ImageFs imageFs = ImageFs.find(activity);
        File rootDir = imageFs.getRootDir();

        SettingsFragment.resetEmulatorsVersion(activity);

        final DownloadProgressDialog dialog = new DownloadProgressDialog(activity);
        dialog.show(R.string.installing_system_files);
        Executors.newSingleThreadExecutor().execute(() -> {
            clearRootDir(rootDir);
            final byte compressionRatio = 22;
            final long contentLength = (long)(FileUtils.getSize(activity, "imagefs.txz") * (100.0f / compressionRatio));
            AtomicLong totalSizeRef = new AtomicLong();

            boolean success = TarCompressorUtils.extract(TarCompressorUtils.Type.XZ, activity, "imagefs.txz", rootDir, (file, size) -> {
                if (size > 0) {
                    long totalSize = totalSizeRef.addAndGet(size);
                    final int progress = (int)(((float)totalSize / contentLength) * 100);
                    activity.runOnUiThread(() -> dialog.setProgress(progress));
                }
                return file;
            });

            if (success) {
                installWineFromAssets(activity);
                installDriversFromAssets(activity);
                imageFs.createImgVersionFile(LATEST_VERSION);
                resetContainerImgVersions(activity);
                ensureDefaultContainer(activity, rootDir);
                extractPolytoriaClient(activity);
            }
            else AppUtils.showToast(activity, R.string.unable_to_install_system_files);

            dialog.closeOnUiThread();
        });
    }

    private static void ensureDefaultContainer(Context context, File rootDir) {
        File containerDir = new File(rootDir, "home/xuser-1");
        File configFile = new File(containerDir, ".container");
        if (configFile.exists()) return;

        containerDir.mkdirs();
        byte[] jsonBytes = FileUtils.read(context, "default_container.json");
        if (jsonBytes == null) return;
        String json = new String(jsonBytes, java.nio.charset.StandardCharsets.UTF_8);
        FileUtils.writeString(configFile, json);

        // extract wine prefix pattern into the specific container directory
        try {
            JSONObject data = new JSONObject(json);
            String wineVersion = data.optString("wineVersion", WineInfo.MAIN_WINE_VERSION.identifier());
            String containerPattern = wineVersion + "_container_pattern.tzst";
            boolean result = TarCompressorUtils.extract(TarCompressorUtils.Type.ZSTD, context, containerPattern, containerDir);
            if (!result) {
                WineInfo wineInfo = WineInfo.fromIdentifier(context, null, wineVersion);
                File prefixPack = new File(wineInfo.path + "/prefixPack.txz");
                result = TarCompressorUtils.extract(TarCompressorUtils.Type.XZ, prefixPack, containerDir);
            }

            // copy Wine DLLs from the Wine binary to the container prefi
            if (result) {
                WineInfo wineInfo = WineInfo.fromIdentifier(context, null, wineVersion);
                String system32Src = wineInfo.isArm64EC() ? "aarch64-windows" : "x86_64-windows";
                File system32Dir = new File(containerDir, ".wine/drive_c/windows/system32");
                File syswow64Dir = new File(containerDir, ".wine/drive_c/windows/syswow64");
                system32Dir.mkdirs();
                syswow64Dir.mkdirs();

                copyWineDlls(new File(wineInfo.path + "/lib/wine/" + system32Src), system32Dir);
                copyWineDlls(new File(wineInfo.path + "/lib/wine/i386-windows"), syswow64Dir);
            }
        } catch (Exception e) {
            android.util.Log.e("ImageFsInstaller", "Failed to extract container pattern", e);
        }
    }

    private static void extractPolytoriaClient(Context context) {
        ImageFs imageFs = ImageFs.find(context);
        File rootDir = imageFs.getRootDir();
        File polytoriaDir = new File(rootDir, "home/xuser-1/.wine/drive_c/users/xuser/AppData/Roaming/Polytoria/Client/1.5.6");
        File marker = new File(polytoriaDir, ".polytoria_installed");

        if (marker.exists()) return;

        polytoriaDir.mkdirs();
        boolean success = TarCompressorUtils.extract(TarCompressorUtils.Type.XZ, context, "polytoria_client.txz", polytoriaDir);
        if (success) {
            try {
                marker.createNewFile();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    public static void installPolytoriaClient(final MainActivity activity) {
        ImageFs imageFs = ImageFs.find(activity);
        File rootDir = imageFs.getRootDir();
        File marker = new File(rootDir, "home/xuser-1/.wine/drive_c/users/xuser/AppData/Roaming/Polytoria/Client/1.5.6/.polytoria_installed");

        ensureDefaultContainer(activity, rootDir);

        if (marker.exists()) return;

        final PreloaderDialog dialog = activity.preloaderDialog;
        dialog.show(R.string.installing_system_files);
        Executors.newSingleThreadExecutor().execute(() -> {
            extractPolytoriaClient(activity);
            dialog.closeOnUiThread();
        });
    }

    public static void installIfNeeded(final MainActivity activity) {
        ImageFs imageFs = ImageFs.find(activity);
        if (!imageFs.isValid() || imageFs.getVersion() < LATEST_VERSION) installFromAssets(activity);
        else installPolytoriaClient(activity);
    }

    private static void clearOptDir(File optDir) {
        File[] files = optDir.listFiles();
        if (files != null) {
            for (File file : files) {
                if (file.getName().equals("installed-wine")) continue;
                FileUtils.delete(file);
            }
        }
    }

    private static void clearRootDir(File rootDir) {
        if (rootDir.isDirectory()) {
            File[] files = rootDir.listFiles();
            if (files != null) {
                for (File file : files) {
                    if (file.isDirectory()) {
                        String name = file.getName();
                        if (name.equals("home")) {
                            continue;
                        }
                    }
                    FileUtils.delete(file);
                }
            }
        }
        else rootDir.mkdirs();
    }

    private static void copyWineDlls(File srcDir, File dstDir) {
        if (!srcDir.isDirectory()) return;
        File[] files = srcDir.listFiles(File::isFile);
        if (files == null) return;
        for (File file : files) {
            File dst = new File(dstDir, file.getName());
            if (!dst.exists()) FileUtils.copy(file, dst);
        }
    }
}