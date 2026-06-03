-keep class org.maplibre.** { *; }
-keep class com.mapbox.** { *; }
-dontwarn org.maplibre.**
-dontwarn com.mapbox.**

# GeckoView (library consumers; full rules also in androidApp release shrinker config)
-keepattributes *Annotation*
-keep @interface org.mozilla.gecko.annotation.WrapForJNI
-keep @org.mozilla.gecko.annotation.WrapForJNI class *
-keepclassmembers,includedescriptorclasses class * {
    @org.mozilla.gecko.annotation.WrapForJNI *;
}
-keep @interface org.mozilla.gecko.annotation.ReflectionTarget
-keep @org.mozilla.gecko.annotation.ReflectionTarget class *
-dontwarn java.beans.BeanInfo
-dontwarn java.beans.FeatureDescriptor
-dontwarn java.beans.IntrospectionException
-dontwarn java.beans.Introspector
-dontwarn java.beans.PropertyDescriptor

-keep class androidx.media3.** { *; }
-dontwarn androidx.media3.**
-keep class com.bumptech.glide.** { *; }
-dontwarn com.bumptech.glide.**
