# LoadingView

Android material theme (`Theme.MaterialComponents`) loading indicator with customizable attributes

[ ![Download](https://api.bintray.com/packages/wojciechkolendo/maven/LoadingView/images/download.svg?version=1.0.0) ](https://bintray.com/wojciechkolendo/maven/LoadingView/1.0.0/link)

## Gradle Dependency (jCenter)

```Gradle
dependencies {
    implementation 'com.wojciechkolendo:loadingview:1.0.0'
}
```

## Using ProgressBar

XML layout file:

```xml
<com.wojciechkolendo.loadingview.LoadingView
		android:layout_width="120dp"
		android:layout_height="120dp"
		android:layout_margin="24dp" />
```


Customizable attributes:

Attribute Name         |Description                                                                                                 |Default Value
-----------------------|------------------------------------------------------------------------------------------------------------|-------------
`loading_progress`         |The color displayed most frequently  and components.                               |0
`loading_maxProgress`  |A tonal variation of the primary color.                                          |100
`loading_animDuration`       |A color that passes accessibility  primary color.   |500ms
`loading_animSwoopDuration`       |The secondary branding c     |5000ms
`loading_animSyncDuration`|A tonal variation of the secondary color.                                         |4000ms
`loading_color`     |A color of loading indicator. If colorSecondary or colorAccent are not supplied, default value is used. |#2196F3
