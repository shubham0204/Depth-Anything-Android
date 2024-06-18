from PIL import Image
import onnxruntime as ort
import numpy as np

image = Image.open( "sample-images/sample-02.png" ).convert( "RGB" )
image = image.resize( (512,512) )
image = np.expand_dims( image , axis=0 )

session = ort.InferenceSession( "fused_model_quantized.onnx" )
output_name = session.get_outputs()[0].name
input_name = session.get_inputs()[0].name

pred = session.run( [ output_name ] , { input_name: image } )
pred = np.squeeze( pred[0] )

depth_image = Image.fromarray( pred )
depth_image.save( "output.png" )
