# Depth-Anything V1/V2 - Android Demo

> An Android app inferencing the popular [Depth-Anything](https://arxiv.org/abs/2401.10891) model, which is used for monocular depth estimation

<table>
<tr>
<td>
<img src="https://github.com/shubham0204/Depth-Anything-Android/assets/41076823/d0b5cf01-2949-4adc-8af8-deeb4c3df1f7" alt="app_img_01">
</td>
<td>
<img src="https://github.com/shubham0204/Depth-Anything-Android/assets/41076823/5bec8e82-f8bd-426e-9aad-37a45287a033" alt="app_img_02">
</td>
<td>
<img src="https://github.com/shubham0204/Depth-Anything-Android/assets/41076823/196d741d-603e-4adb-aa64-0f73b30b1c73" alt="app_img_03">
</td>
</tr>
</table>

#### Depth-Anything V1 vs. V2

![v1_v2_compare](https://github.com/shubham0204/Depth-Anything-Android/assets/41076823/1aab0882-cdde-47c4-a7c2-918225c020de)

## Updates

* 18-06-2024: Added new models for [Depth-Anything-V2](https://arxiv.org/abs/2406.09414) with base models derived from [HuggingFace](https://huggingface.co/onnx-community/depth-anything-v2-small)
* 16-02-2024: Add models and Android demo for [Depth-Anything](https://github.com/LiheYoung/Depth-Anything)

## Project Setup

1. Clone the repository, and open the resulting directory in Android Studio

```
$> git clone --depth=1 https://github.com/shubham0204/Depth-Anything-Android
```

2. Download the ONNX models from [models release](https://github.com/shubham0204/Depth-Anything-Android/releases) and place them in the `app/src/main/assets` directory. The models are used by ONNX's `OrtSession` to load the computation graph and parameters in-memory. 


**Depth-Anything V1**: Any one of the following models can be placed in the `assets` directory:
* `model.onnx`: Depth-Anything module 
* `model_fp16.onnx`: `float16` quantized version of `model.onnx`

**Depth-Anything V2**: Check the [models-v2 release]() to download the models. The models come into two input sizes, 512 and 
256. The model suffixed with `_256` take an 256 * 256 sized image as input.

In [DepthAnything.kt](https://github.com/shubham0204/Depth-Anything-Android/blob/main/app/src/main/java/com/ml/shubham0204/depthanything/DepthAnything.kt), make the following changes to 
`inputDims` and `outputDims`, along with the name of the model given as an argument to `context.assets.open`, 

```kotlin
class DepthAnything(context: Context) {

    private val ortEnvironment = OrtEnvironment.getEnvironment()
    private val ortSession =
        ortEnvironment.createSession(context.assets.open("fused_model_uint8_256.onnx").readBytes())
    private val inputName = ortSession.inputNames.iterator().next()

    // For '_256' suffixed models
    private val inputDim = 256
    private val outputDim = 252
    // For other models
    // private val inputDim = 512
    // private val outputDim = 504

    // Other methods...
}
```

3. [Connect a device](https://developer.android.com/codelabs/basic-android-kotlin-compose-connect-device#0) to Android Studio, and select `Run Application` from the top navigation pane.

## Useful Resources

> [!NOTE]
> The app contains an ONNX model which was created by combining the pre/post-processing operations required 
> by `Depth-Anything` in a single model. To know more on how the model was built, refer [this](https://github.com/shubham0204/Google_Colab_Notebooks/blob/main/Depth_Anything_FusedOps_ONNX_Model.ipynb) notebook.

* [Depth Anything: Unleashing the Power of Large-Scale Unlabeled Data](https://arxiv.org/abs/2401.10891)
* [fabio-sim/Depth-Anything-ONNX](https://github.com/fabio-sim/Depth-Anything-ONNX)
* [ONNX Runtime: How to develop a mobile application with ONNX Runtime](https://onnxruntime.ai/docs/tutorials/mobile/)
* [ONNX Runtime: Create Float16 and Mixed Precision Models](https://onnxruntime.ai/docs/performance/model-optimizations/float16.html)
* [Build a image preprocessing model using Pytorch and integrate into your model using ONNX](https://vilsonrodrigues.medium.com/build-a-image-preprocessing-model-using-pytorch-and-integrate-into-your-model-using-onnx-2262966c642e)

### Paper Summary

#### Depth Anything V1

* MDE model trained on labeled data is used to annotate unlabeled images (62M) during training (semi-supervised learning, self learning or pseudo-labelling)
* Teacher model trained on labeled images and then used to annotate unlabeled images. Student model trained on all images (labeled + teacher-annotated)
* No performance gain observed, hence a more difficult optimization target was introduced for the student model. Unlabeled images are perturbed with (1) strong color distortions and (2) CutMix (used in image classification mostly)
* Semantic assisted perception: Improve depth estimation with auxiliary semantic segmentation task, by using one shared encoder and two separate decoders

## Citation

```
@misc{yang2024depth,
      title={Depth Anything V2}, 
      author={Lihe Yang and Bingyi Kang and Zilong Huang and Zhen Zhao and Xiaogang Xu and Jiashi Feng and Hengshuang Zhao},
      year={2024},
      eprint={2406.09414},
      archivePrefix={arXiv},
      primaryClass={id='cs.CV' full_name='Computer Vision and Pattern Recognition' is_active=True alt_name=None in_archive='cs' is_general=False description='Covers image processing, computer vision, pattern recognition, and scene understanding. Roughly includes material in ACM Subject Classes I.2.10, I.4, and I.5.'}
}
```

```
@article{depthanything,
      title={Depth Anything: Unleashing the Power of Large-Scale Unlabeled Data}, 
      author={Yang, Lihe and Kang, Bingyi and Huang, Zilong and Xu, Xiaogang and Feng, Jiashi and Zhao, Hengshuang},
      journal={arXiv:2401.10891},
      year={2024}
}
```

```
@misc{oquab2023dinov2,
  title={DINOv2: Learning Robust Visual Features without Supervision},
  author={Oquab, Maxime and Darcet, Timothée and Moutakanni, Theo and Vo, Huy V. and Szafraniec, Marc and Khalidov, Vasil and Fernandez, Pierre and Haziza, Daniel and Massa, Francisco and El-Nouby, Alaaeldin and Howes, Russell and Huang, Po-Yao and Xu, Hu and Sharma, Vasu and Li, Shang-Wen and Galuba, Wojciech and Rabbat, Mike and Assran, Mido and Ballas, Nicolas and Synnaeve, Gabriel and Misra, Ishan and Jegou, Herve and Mairal, Julien and Labatut, Patrick and Joulin, Armand and Bojanowski, Piotr},
  journal={arXiv:2304.07193},
  year={2023}
}
```
