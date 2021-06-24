# LightPathAnim

光线动画效果：
1.可以直接网上下载或UI定制SVG格式图片直接导入使用
https://www.iconfont.cn/plus/user/detail?uid=41718下载svg格式文件
2.动画可以根据需求效果（现在只做了三种光流动效果）
3.最终效果可以参照附件参考视频（需要UI定制svg文件）
<com.path.androipathview.LinePathAnimView
        android:id="@+id/mianPathView"
        android:layout_width="300dp"
        android:layout_height="300dp"
        android:layout_gravity="center"
        android:scaleType="center"
        app:autoStart="true"
        app:duration="1000"
        app:fillOutline="true"
        app:infinite="true"
        app:linePathColor="@color/line_path_color"
        app:lineShader="true"
        app:lineShaderSize="5"
        app:naturalColors="true"
        app:outlineColor="@color/outline_color"
        app:pathColor="@android:color/white"
        app:pathWidth="2dp"
        app:randColor="true"
        app:stepDuaration="5"
        app:svg="@raw/main_chip" />
