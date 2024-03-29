# Depth-Anything - Android Demo

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


## Project Setup

1. Clone the repository, and open the resulting directory in Android Studio

```
$> git clone --depth=1 https://github.com/shubham0204/Depth-Anything-Android
```

2. Download the ONNX models from [Releases](https://github.com/shubham0204/Depth-Anything-Android/releases) and place them in the `app/src/main/assets` directory. The models are used by ONNX's `OrtSession` to load the computatio- graph and parameters in-memory. Any one of the following models can be placed in the `assets` directory:

* `model.onnx`: Depth-Anything module 
* `model_fp16.onnx`: `float16` quantized version of `model.onnx`

3. [Connect a device](https://developer.android.com/codelabs/basic-android-kotlin-compose-connect-device#0) to Android Studio, and select `Run Application` from the top navigation pane.

## Useful Resources

* [Depth Anything: Unleashing the Power of Large-Scale Unlabeled Data](https://arxiv.org/abs/2401.10891)
* [fabio-sim/Depth-Anything-ONNX](https://github.com/fabio-sim/Depth-Anything-ONNX)
* [ONNX Runtime: How to develop a mobile application with ONNX Runtime](https://onnxruntime.ai/docs/tutorials/mobile/)
* [ONNX Runtime: Create Float16 and Mixed Precision Models](https://onnxruntime.ai/docs/performance/model-optimizations/float16.html)

## Citation

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