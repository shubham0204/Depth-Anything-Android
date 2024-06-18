from onnx import save_model , load_model
from onnxruntime.tools.symbolic_shape_infer import SymbolicShapeInference
import torch
import torchvision.transforms as transforms

class DepthAnythingPreprocessing( torch.nn.Module ):

    def __init__(self, *args, **kwargs):
        super().__init__(*args, **kwargs)
        self.normalize = transforms.Normalize(
            mean=[0.485, 0.456, 0.406],
            std=[0.229, 0.224, 0.225]
        )

    def forward( self , x ):
        w , h = x.shape[1] , x.shape[2]
        inputs = torch.permute( x , ( 0 , 3 , 1 , 2 ) )
        inputs = inputs / 255.0
        inputs = self.normalize( inputs )
        return inputs

dummy_input = torch.randn( 1 , 256 , 256 , 3 )
dummy_input = dummy_input.type( torch.uint8 )
output_path = 'preprocessing_256.onnx'
model = DepthAnythingPreprocessing()

torch.onnx.export( 
    model ,
    dummy_input,
    output_path,
    opset_version=19 ,
    do_constant_folding=True,
    input_names=[ "preop-input" ] ,
    output_names=[ "preop-output" ] ,
    verbose=True
)

save_model(
    SymbolicShapeInference.infer_shapes( load_model( output_path ) , auto_merge=True ) , 
    output_path  
)