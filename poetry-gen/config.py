# coding=utf8


class Config(object):
    data_path = 'data/'  # 诗歌的文本文件存放路径
    pickle_path = 'tang.npz'  # 预处理好的二进制文件
    lr = 1e-3
    weight_decay = 1e-4
    epoch = 250
    batch_size = 128
    maxlen = 125  # 超过这个长度的之后字被丢弃，小于这个长度的在前面补空格
    max_gen_len = 200  # 生成诗歌最长长度
    debug_file = '/tmp/debugp'
    model_path = 'checkpoints/'  # 预训练模型路径
    prefix_words = '郡邑浮前浦，波澜动远空。'  # 不是诗歌的组成部分，用来控制生成诗歌的意境
    start_words = '江流天地外'  # 诗歌开始
    acrostic = True  # 是否是藏头诗
    model_prefix = 'checkpoints/tang'  # 模型保存路径
