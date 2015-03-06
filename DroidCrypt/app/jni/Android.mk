LOCAL_PATH := $(call my-dir)

include $(CLEAR_VARS)

LOCAL_CPPFLAGS = -fexceptions
LOCAL_MODULE    := embedder
LOCAL_SRC_FILES := embedder.cpp 

#HUGO_like_src/HUGO_like.cpp HUGO_like_src/cost_model_config.cpp HUGO_like_src/cost_model.cpp include/base_cost_model.cpp include/base_cost_model_config.cpp include/mi_embedder.cpp include/stc_ml_c.cpp include/stc_embed_c.cpp include/stc_extract_c.cpp include/common.cpp include/image.cpp include/info_theory.cpp

LOCAL_LDLIBS    := -lm -llog -ljnigraphics
LOCAL_CFLAGS := -Iinclude/
LOCAL_DISABLE_FORMAT_STRING_CHECKS := true

include $(BUILD_SHARED_LIBRARY)