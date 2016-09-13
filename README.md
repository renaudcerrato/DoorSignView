# DoorSignView

Create static door signs using [`DoorSignView`](/library/src/main/java/com/mypopsy/doorsignview/DoorSignView.java) or, if you're into fancy things, give a try to [`AnimatedDoorSignView`](/library/src/main/java/com/mypopsy/doorsignview/AnimatedDoorSignView.java) for adding a cool animation based on the device orientation sensor(s).
 **Already in production at [Popsy](https://play.google.com/store/apps/details?id=com.mypopsy.android).**

[DEMO APK](https://github.com/renaudcerrato/DoorSignView/raw/master/sample/sample-debug.apk)

<table>
<tr>
<td><p align="center"><img src="/assets/sample.png" height="500"/></p></td>
<td><p align="center"><img src="/assets/sample.gif" height="500"/></p></td>
</tr>
</table>

# Usage 
Add an [`AnimatedDoorSignView`](/library/src/main/java/com/mypopsy/doorsignview/AnimatedDoorSignView.java) (or a [`DoorSignView`](/library/src/main/java/com/mypopsy/doorsignview/DoorSignView.java)) to your view hierarchy, be sure that `android:width` is set to `match_parent` or any fixed value:

```xml
...
  <com.mypopsy.doorsignview.AnimatedDoorSignView
          android:id="@+id/doorsign"
          android:layout_width="150dp"
          android:layout_height="wrap_content"
          android:layout_gravity="center"
          android:text="Sorry!\nWe're closed!"
          android:textSize="15sp"
          android:textColor="#ffffff"
          app:dsv_signColor="#00adef"
          app:dsv_textFont="fonts/BrannbollFet.ttf"
          app:dsv_stringsWidth="3dp"
          app:dsv_pinRadius="10dp"
          app:dsv_cornerRadius="10dp"
          app:dsv_textSpacingMult="1.2"
          app:dsv_shadowSize="3dp"
          />
...
```

[`AnimatedDoorSignView`](/library/src/main/java/com/mypopsy/doorsignview/AnimatedDoorSignView.java) use [Rebound](http://facebook.github.io/rebound/) under the hood, you can easily configure friction and tension through xml:

```xml
...
  <com.mypopsy.doorsignview.AnimatedDoorSignView
          ...
          app:adsv_friction="100"
          app:adsv_tension="4"
          ...
          />
...
```

See the [all the supported attributes](/library/src/main/res/values/attrs.xml), and don't forget to look at the [sample](/sample).



# Install

This repository can be found on JitPack:

https://jitpack.io/#renaudcerrato/DoorSignView

Add it in your root build.gradle at the end of repositories:

```javascript
allprojects {
		repositories {
			...
			maven { url "https://jitpack.io" }
		}
	}
```

Add the dependency:
```javascript
dependencies {
  compile 'com.github.renaudcerrato:DoorSignView:1.0.0'
}
```


