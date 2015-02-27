#!/bin/bash

kerType=$(uname -m)
# if [ "${kerType}" == "x86_64" ]; then
# 	libsPath="../lib/GLNXA64"
# elif [ "${kerType}" == "i686" ]; then
#     libsPath="../lib/GLNX86"
# else
# 	echo "Something is wrong with system identification"
# fi
libsPath="../lib/GLNXA64"
# release
cd executable
g++ -o2 ../HUGO_like_src/HUGO_like.cpp ../HUGO_like_src/cost_model_config.cpp ../HUGO_like_src/cost_model.cpp ../include/base_cost_model.cpp ../include/base_cost_model_config.cpp ../include/mi_embedder.cpp ../include/stc_ml_c.cpp ../include/stc_embed_c.cpp ../include/stc_extract_c.cpp ../include/common.cpp ../include/image.cpp ../include/info_theory.cpp -o HUGO_like -I../include -L${libsPath} -lboost_program_options-mt -lboost_filesystem-mt -lboost_system-mt

#g++ -o3 ../HUGO_like_src/HUGO_like.cpp ../HUGO_like_src/cost_model_config.cpp ../HUGO_like_src/cost_model.cpp ../include/base_cost_model.cpp ../include/base_cost_model_config.cpp ../include/mi_embedder.cpp ../include/stc_ml_c.cpp ../include/stc_embed_c.cpp ../include/stc_extract_c.cpp ../include/common.cpp ../include/image.cpp ../include/info_theory.cpp -o HUGO_like -I../include -L${libsPath} -lboost_program_options-mt -lboost_filesystem-mt -lboost_system-mt
