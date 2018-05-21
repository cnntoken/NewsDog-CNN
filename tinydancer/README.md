# Tiny Dancer

A real time frames per second measuring library for Android that also shows a color coded metric.  This metric is based on percentage of time spent when you have dropped 2 or more frames.  If the application spends more than 5% in this state then the color turns yellow, when you have reached the 20% threshold the indicator turns red.  

## 相关修改

* 在原版TinyDancer的基础上修复一些crash bug
* 重构代码, 使逻辑更清晰
* 收集最原始的FPS数据，以及去除对ui展示的依赖
* 支持将fps数据dump到本地文件中，使之能够跟跟自动化测试对接上
* 支持 Activity页面的自动检测 (在Application中调用 TinyDancer.install(this) ) 即可

## Min SDK
**Tiny Dancer min sdk is API 16.**

## Getting started


In your `DebugApplication` class:

```java
public class DebugApplication extends Application {

  @Override public void onCreate() {

   // 自动收集每个 Activity的 fps 数据, 并且dump到本地文件中
   TinyDancer.install(this);

  // 创建 TinyDancer
   TinyDancer.create(this)
             .show();
             
   //alternatively
   TinyDancer.create(this)
      .redFlagPercentage(.1f) // set red indicator for 10%
      .startingGravity(Gravity.TOP)
      .startingXPosition(200)
      .startingYPosition(600)
      .show(this);
      

   //you can add a callback to get frame times and the calculated
   //number of dropped frames within that window
   TinyDancer.create(this)
       .addFrameDataCallback(new FrameDataCallback() {
          @Override
          public void doFrame(long previousFrameNS, long currentFrameNS, int droppedFrames) {
             //collect your stats here
          }
        })
        .show();
  }
}
```

## dump 的数据格式

当调用 `TinyDancer.install(this);` 或者 TinyDancer对象的`destroy()`函数时 时 会为每个 Activity 产生一份FPS数据,
目录为 `Environment.getExternalStorageDirectory().getPath()/应用包名/fps/`. 数据格式为

```
activity : Activity的完整名字
58,55,57,59,60,60
Max: 60
Min: 55
Avg: 58
```

代表某个Activity在各个时间段内的FPS值,最高为60（最流畅）, 最低为0, 以及最大、最小、平均的FPS值。

**You're good to go!** Tiny Dancer will show a small draggable view overlay with FPS as well as a color indicator of when FPS drop.  You can double tap the overlay to explicitly hide it.


See sample application that simulates excessive bind time:

![Tiny Dancer Sample](assets/tinydancer1.gif "Tiny Dancer Sample")

Have an project with performance issues? We'd be happy to help tune it.  
