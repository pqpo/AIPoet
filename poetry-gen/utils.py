#!/usr/bin/python
# coding=utf8
import tensorflow as tf
from data_utils import get_data
from config import Config
import numpy as np


def pick_top_n(preds, vocab_size, top_n=1):
    p = np.squeeze(preds)
    # 将除了top_n个预测值的位置都置为0
    p[np.argsort(p)[:-top_n]] = 0
    # 归一化概率
    p = p / np.sum(p)
    # 随机选取一个字符
    c = np.random.choice(vocab_size, 1, p=p)[0]
    return c


# [num_seqs, batch_size]
def generate_batch_data(input, batch_size):
    """
    @param input: [57580, 125]
    @param batch_size: 128
    @return input_: [124, 128]
    @return target: [124, 128]
    """
    input_ = tf.convert_to_tensor(input)
    data_set = tf.data.Dataset.from_tensor_slices(input_).shuffle(10000).repeat().batch(batch_size)
    iterator = data_set.make_one_shot_iterator()
    batch_data = iterator.get_next()
    tensor = tf.transpose(batch_data)
    input_batch, target_batch = tensor[:-1, :], tensor[1:, :]
    return input_batch, target_batch


if __name__ == '__main__':
    data, word2ix, ix2word = get_data(Config)
    x, y = generate_batch_data(data, 10)
    with tf.Session() as sess:
        for i in range(5):
            x_, y_ = sess.run([x, y])
            x_ = np.transpose(x_)
            y_ = np.transpose(y_)
            line_x = x_[0]
            line_x = [ix2word[i] for i in line_x]
            print(''.join(line_x))
            line_y = y_[0]
            line_y = [ix2word[i] for i in line_y]
            print(''.join(line_y))
