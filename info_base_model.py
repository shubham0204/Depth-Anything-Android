import onnxruntime as ort

session = ort.InferenceSession("hf-models/model_q4f16.onnx")
print( session.get_inputs()[0].shape )
print( session.get_outputs()[0].shape )