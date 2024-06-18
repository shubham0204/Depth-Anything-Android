import torch

class DepthAnythingPostprocessing( torch.nn.Module ):

    def __init__(self, *args, **kwargs):
        super().__init__(*args, **kwargs)

    def forward( self , x ):
        min_ = torch.min( x )
        inputs = ( (x - min_) / ( torch.max(x) - min_ ) ) * 255
        inputs = inputs.type( torch.uint8 )
        inputs = torch.permute( inputs , [ 0 , 1 , 2 ] )
        return inputs

dummy_input = torch.randn( 1 , 518 , 518 )
output_path = 'postprocessing.onnx'
model = DepthAnythingPostprocessing()

torch.onnx.export( 
    model ,
    dummy_input,
    output_path,
    opset_version=19 ,
    do_constant_folding=True,
    input_names=[ "postop-input" ] ,
    output_names=[ "postop-output" ] ,
    verbose=True
)