#!/bin/bash
ndk="/cygdrive/c/android-ndk-r8b";
sysroot="C:\\android-ndk-r8b\\platforms\\android-8\\arch-arm";
cflags="-static";
ccomp="$ndk/toolchains/arm-linux-androideabi-4.4.3/prebuilt/windows/bin/arm-linux-androideabi-gcc --sysroot=$sysroot $cflags";
echo $ccomp
$ccomp $@

