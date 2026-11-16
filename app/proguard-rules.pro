# Business Empire R8 rules.

# --- kotlinx.serialization ---
# The library ships its own consumer rules; these keep our @Serializable save models and
# navigation routes safe even if R8 gets more aggressive in future versions.
-keepattributes *Annotation*, InnerClasses, Signature, Exceptions
-dontnote kotlinx.serialization.**

-keepclassmembers @kotlinx.serialization.Serializable class com.businessempire.game.** {
    *** Companion;
    *** INSTANCE;
    kotlinx.serialization.KSerializer serializer(...);
}
-keepclasseswithmembers class com.businessempire.game.** {
    kotlinx.serialization.KSerializer serializer(...);
}
-keep,includedescriptorclasses class com.businessempire.game.**$$serializer { *; }

# Enum names are stored in the save file. Keep them stable.
-keepclassmembers enum com.businessempire.game.** {
    <fields>;
    public static **[] values();
    public static ** valueOf(java.lang.String);
}
