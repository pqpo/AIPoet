<p align=center> 
  <img src="art/icon_round.png" width=100px/>
  <h2 align=center>Ai诗人</h2>
  <p align=center> 
  基于人工智能的 Android 写诗应用，无须联网随时随地写藏头诗，意境诗。如果你对此刚兴趣的话帮忙点个 star！
  </p>
</p> 

## 应用截图

<p> 
  <img src="art/Screenshot_0.jpg" width=220px/>
  <img src="art/Screenshot_1.jpg" width=220px/>
</p>  

## Ai作诗

安装包下载 -> [Release](https://github.com/pqpo/AIPoet/releases)

|导出文本|导出图片|
|:---:|:---:|
|人生不及此，曩年如故林。<br/>工艺各不遂，道心固其深。<br/>智者不可保，所以慰所任。<br/>能为东都儿，不解令所箴。<br/>|<img src="art/ai_poet_share.jpg" width=220px/>|

## 训练模型

感谢训练集诗歌来源 -> [chinese-poetry](https://github.com/chinese-poetry/chinese-poetry)  

- 下载诗歌至目录：poetry-gen/data/raw-data/
- 诗歌数据转简体中文，运行脚本 poetry-gen/raw_data_2_simplified.sh
- 数据预处理，直接使用预处理好的二进制文件：poetry-gen/tang.npz 或 运行脚本 data_utils.py 自己生成
- 开始训练，运行脚本 poetry-gen/train.py
- 训练结束后导出模型，运行脚本 poetry-gen/freeze_model.py
- 导出文字转换文件，运行脚本 poetry-gen/export_convert.py
- Android 项目工程 assets 目录下替换上述生成的 转换文件 与 模型文件

## About Me：

- Email：    pqponet@gmail.com
- GitHub：  [pqpo](https://github.com/pqpo)
- Blog：    [pqpo's notes](https://pqpo.me)
- Twitter: [Pqponet](https://twitter.com/Pqponet)
- WeChat: pqpo_me

<img src="art/qrcode_for_gh.jpg" width="200">

## License

    Copyright 2017 pqpo
    
    Licensed under the Apache License, Version 2.0 (the "License");
    you may not use this file except in compliance with the License.
    You may obtain a copy of the License at
    
       http://www.apache.org/licenses/LICENSE-2.0
    
    Unless required by applicable law or agreed to in writing, software
    distributed under the License is distributed on an "AS IS" BASIS,
    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
    See the License for the specific language governing permissions and
    limitations under the License.


