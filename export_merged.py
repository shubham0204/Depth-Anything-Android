from onnx import compose
import onnx

base = onnx.load_model("hf-models/model_int8.onnx")
base = onnx.version_converter.convert_version( base , 19 )

preprocessing = onnx.load_model( "preprocessing_256.onnx" )
postprocessing = onnx.load_model( "postprocessing.onnx" )

preprocessing = compose.add_prefix( preprocessing , prefix="pre-" )
postprocessing = compose.add_prefix( postprocessing , prefix="post-" )

merged_model_1 = compose.merge_models( preprocessing , base , io_map=[ ( "pre-preop-output" , "pixel_values" ) ] )
merged_model_2 = compose.merge_models( merged_model_1 , postprocessing , io_map=[ ( "predicted_depth" , "post-postop-input" ) ])

onnx.save_model( merged_model_2 , "fused_model_int8_256.onnx" )