# MapLibre / native map stack
-keep class org.maplibre.** { *; }
-keep class com.mapbox.** { *; }
-dontwarn org.maplibre.**
-dontwarn com.mapbox.**

# JNI / native
-keepclasseswithmembernames class * {
    native <methods>;
}

# GeckoView ships proguard.txt in the AAR; these mirror Mozilla’s JNI / reflection entry points for R8.
-keepattributes *Annotation*
-keep @interface org.mozilla.gecko.annotation.WrapForJNI
-keep @org.mozilla.gecko.annotation.WrapForJNI class *
-keepclassmembers,includedescriptorclasses class * {
    @org.mozilla.gecko.annotation.WrapForJNI *;
}
-keepclasseswithmembers,includedescriptorclasses class * {
    @org.mozilla.gecko.annotation.WrapForJNI <methods>;
}
-keepclasseswithmembers,includedescriptorclasses class * {
    @org.mozilla.gecko.annotation.WrapForJNI <fields>;
}
-keepclassmembers,includedescriptorclasses @org.mozilla.gecko.annotation.WrapForJNI class * {
    *;
}
-keep @interface org.mozilla.gecko.annotation.ReflectionTarget
-keep @org.mozilla.gecko.annotation.ReflectionTarget class *
-keepclassmembers class * {
    @org.mozilla.gecko.annotation.ReflectionTarget *;
}
-keepclassmembers @org.mozilla.gecko.annotation.ReflectionTarget class * {
    *;
}
-dontwarn java.beans.BeanInfo
-dontwarn java.beans.FeatureDescriptor
-dontwarn java.beans.IntrospectionException
-dontwarn java.beans.Introspector
-dontwarn java.beans.PropertyDescriptor

# Media3 / ExoPlayer
-keep class androidx.media3.** { *; }
-dontwarn androidx.media3.**

# Glide
-keep public class * implements com.bumptech.glide.module.GlideModule
-keep class * extends com.bumptech.glide.module.AppGlideModule { <init>(...); }
-keep public enum com.bumptech.glide.load.ImageHeaderParser$** { **[] $VALUES; public *; }
-keep class com.bumptech.glide.load.data.ParcelFileDescriptorRewinder$InternalRewinder { *** rewind(); }
-keep class com.bumptech.glide.** { *; }
-dontwarn com.bumptech.glide.**
