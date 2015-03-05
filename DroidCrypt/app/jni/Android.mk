LOCAL_PATH := $(call my-dir)

include $(CLEAR_VARS)

LOCAL_MODULE    := embedder
LOCAL_SRC_FILES := embedder.cpp
LOCAL_LDLIBS    := -lm -llog -ljnigraphics
LOCAL_DISABLE_FORMAT_STRING_CHECKS := true

include $(BUILD_SHARED_LIBRARY)