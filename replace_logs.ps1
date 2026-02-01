# 批量替换Log调用为Logger调用的PowerShell脚本

$files = @(
    "MainActivity.kt",
    "WechatAccessibilityService.kt", 
    "CommonAppsActivity.kt",
    "ContactDetailActivity.kt",
    "ContactsActivity.kt",
    "AddContactActivity.kt",
    "location\LocationManager.kt"
)

$baseDir = "c:\Users\17554\AndroidStudioProjects\OnePass\app\src\main\java\com\example\onepass\"

foreach ($file in $files) {
    $filePath = Join-Path $baseDir $file
    
    if (Test-Path $filePath) {
        Write-Host "处理文件: $file"
        
        $content = Get-Content $filePath -Raw
        
        # 替换 import android.util.Log 为 import com.example.onepass.Logger
        $content = $content -replace 'import android\.util\.Log', 'import com.example.onepass.Logger'
        
        # 替换 Log.d(TAG, 为 Logger.d(
        $content = $content -replace 'Log\.d\(TAG, ', 'Logger.d('
        
        # 替换 Log.e(TAG, 为 Logger.e(
        $content = $content -replace 'Log\.e\(TAG, ', 'Logger.e('
        
        # 替换 Log.i(TAG, 为 Logger.i(
        $content = $content -replace 'Log\.i\(TAG, ', 'Logger.i('
        
        # 替换 Log.w(TAG, 为 Logger.w(
        $content = $content -replace 'Log\.w\(TAG, ', 'Logger.w('
        
        # 替换 Log.v(TAG, 为 Logger.v(
        $content = $content -replace 'Log\.v\(TAG, ', 'Logger.v('
        
        # 替换 android.util.Log.i("TTS_DEBUG", 为 Logger.i("TTS_DEBUG",
        $content = $content -replace 'android\.util\.Log\.i\("TTS_DEBUG", ', 'Logger.i("TTS_DEBUG", '
        
        # 替换 android.util.Log.e("TTS_DEBUG", 为 Logger.e("TTS_DEBUG",
        $content = $content -replace 'android\.util\.Log\.e\("TTS_DEBUG", ', 'Logger.e("TTS_DEBUG", '
        
        # 替换 android.util.Log.d("TTS_DEBUG", 为 Logger.d("TTS_DEBUG",
        $content = $content -replace 'android\.util\.Log\.d\("TTS_DEBUG", ', 'Logger.d("TTS_DEBUG", '
        
        # 替换 android.util.Log.w("TTS_DEBUG", 为 Logger.w("TTS_DEBUG",
        $content = $content -replace 'android\.util\.Log\.w\("TTS_DEBUG", ', 'Logger.w("TTS_DEBUG", '
        
        # 替换 android.util.Log.v("TTS_DEBUG", 为 Logger.v("TTS_DEBUG",
        $content = $content -replace 'android\.util\.Log\.v\("TTS_DEBUG", ', 'Logger.v("TTS_DEBUG", '
        
        # 替换 android.util.Log.i("TTS_DEBUG", 为 Logger.i("TTS_DEBUG",
        $content = $content -replace 'android\.util\.Log\.i\("TTS_DEBUG", ', 'Logger.i("TTS_DEBUG", '
        
        # 替换 android.util.Log.e("TTS_DEBUG", 为 Logger.e("TTS_DEBUG",
        $content = $content -replace 'android\.util\.Log\.e\("TTS_DEBUG", ', 'Logger.e("TTS_DEBUG", '
        
        # 替换 android.util.Log.d("TTS_DEBUG", 为 Logger.d("TTS_DEBUG",
        $content = $content -replace 'android\.util\.Log\.d\("TTS_DEBUG", ', 'Logger.d("TTS_DEBUG", '
        
        # 替换 android.util.Log.w("TTS_DEBUG", 为 Logger.w("TTS_DEBUG",
        $content = $content -replace 'android\.util\.Log\.w\("TTS_DEBUG", ', 'Logger.w("TTS_DEBUG", '
        
        # 替换 android.util.Log.v("TTS_DEBUG", 为 Logger.v("TTS_DEBUG",
        $content = $content -replace 'android\.util\.Log\.v\("TTS_DEBUG", ', 'Logger.v("TTS_DEBUG", '
        
        # 替换 android.util.Log.i("TAG", 为 Logger.i("TAG",
        $content = $content -replace 'android\.util\.Log\.i\("TAG", ', 'Logger.i("TAG", '
        
        # 替换 android.util.Log.e("TAG", 为 Logger.e("TAG",
        $content = $content -replace 'android\.util\.Log\.e\("TAG", ', 'Logger.e("TAG", '
        
        # 替换 android.util.Log.d("TAG", 为 Logger.d("TAG",
        $content = $content -replace 'android\.util\.Log\.d\("TAG", ', 'Logger.d("TAG", '
        
        # 替换 android.util.Log.w("TAG", 为 Logger.w("TAG",
        $content = $content -replace 'android\.util\.Log\.w\("TAG", ', 'Logger.w("TAG", '
        
        # 替换 android.util.Log.v("TAG", 为 Logger.v("TAG",
        $content = $content -replace 'android\.util\.Log\.v\("TAG", ', 'Logger.v("TAG", '
        
        Set-Content $filePath $content -NoNewline
        Write-Host "完成: $file"
    } else {
        Write-Host "文件不存在: $filePath"
    }
}

Write-Host "所有文件处理完成！"